package com.microbe.recorder

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.microbe.recorder.adapter.PhotoAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.database.TreatmentGroupEntity
import com.microbe.recorder.util.AudioRecorderHelper
import com.microbe.recorder.util.CameraHelper
import com.microbe.recorder.util.FileHelper
import com.microbe.recorder.viewmodel.AddRecordViewModel
import com.microbe.recorder.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 实验记录新增/编辑页面
 *
 * 使用 ViewModel + LiveData 托管表单数据，横竖屏旋转时：
 * - ViewModel 不会被销毁，表单填写内容完整保留
 * - 照片列表、录音路径、编辑模式状态跨配置变更留存
 * - 原有数据库存取逻辑不变，仅优化页面临时数据生命周期
 */
class AddRecordActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var viewModel: AddRecordViewModel

    private lateinit var etPlantingDate: TextInputEditText
    private lateinit var etCreationDate: TextInputEditText
    private lateinit var actvTreatmentGroup: AutoCompleteTextView
    private lateinit var actvFilterDate: AutoCompleteTextView
    private lateinit var etObservation: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var rvPhotos: RecyclerView
    private lateinit var btnVoiceInput: MaterialButton
    private lateinit var tvVoiceStatus: TextView
    private lateinit var tvPhotoCount: TextView
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnRecord: MaterialButton
    private lateinit var layoutRecording: View
    private lateinit var tvRecordingTime: TextView
    private lateinit var tvAudioInfo: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var cardYesterdayRef: View
    private lateinit var tvYesterdayRef: TextView
    private lateinit var rvYesterdayPhotos: RecyclerView
    private var refAdapter: PhotoAdapter? = null

    // 便捷访问 ViewModel 数据（保持与原代码一致的变量名）
    private val photoPaths get() = viewModel.photoPaths.value!!
    private var isRecording
        get() = false // 录音状态不跨旋转保留（录音器会随 Activity 销毁而停止）
        set(_) {}
    private var currentAudioPath: String?
        get() = viewModel.audioPath.value
        set(value) { viewModel.audioPath.value = value }
    private var editRecordId: Long
        get() = viewModel.editRecordId.value ?: -1
        set(value) { viewModel.editRecordId.value = value }
    private var isEditMode: Boolean
        get() = viewModel.isEditMode.value ?: false
        set(value) { viewModel.isEditMode.value = value }
    private var creationCalendar: Calendar
        get() = Calendar.getInstance().apply { timeInMillis = viewModel.creationTimestamp.value ?: System.currentTimeMillis() }
        set(value) { viewModel.creationTimestamp.value = value.timeInMillis }

    private val handler = Handler(Looper.getMainLooper())
    private var recordingStartTime = 0L
    private val recordingTimerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - recordingStartTime
            val seconds = (elapsed / 1000).toInt()
            tvRecordingTime.text = String.format("录音中... %02d:%02d", seconds / 60, seconds % 60)
            handler.postDelayed(this, 1000)
        }
    }

    private fun getPhotoFolder(): String {
        val planting = etPlantingDate.text.toString().trim()
        val group = actvTreatmentGroup.text.toString().trim()
        return if (planting.isNotEmpty() && group.isNotEmpty()) {
            CameraHelper.generateFolderName(planting, group)
        } else ""
    }

    // 自定义相机（支持缩放）
    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            if (result.resultCode == RESULT_OK) {
                val photoPath = result.data?.getStringExtra(CameraActivity.EXTRA_PHOTO_PATH)
                if (photoPath != null && File(photoPath).exists() && photoPaths.size < 5) {
                    // CameraActivity 已直接保存到 getExternalFilesDir，不需要复制
                    photoPaths.add(photoPath)
                    photoAdapter.addPhoto(photoPath)
                    updatePhotoCount()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // 多选图片
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        try {
            if (!uris.isNullOrEmpty()) {
                lifecycleScope.launch {
                    val addedPaths = withContext(Dispatchers.IO) {
                        val folder = getPhotoFolder()
                        val imagesRoot = File(getExternalFilesDir(null), "images")
                        val storageDir = if (folder.isNotEmpty()) File(imagesRoot, folder) else imagesRoot
                        if (!storageDir.exists()) storageDir.mkdirs()

                        val result = mutableListOf<String>()
                        var added = 0
                        for (uri in uris) {
                            if (photoPaths.size + added >= 5) break
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
                            val destFile = File(storageDir, "PICK_${timeStamp}_${added}.jpg")
                            contentResolver.openInputStream(uri)?.use { input -> destFile.outputStream().use { output -> input.copyTo(output) } }
                            if (destFile.exists()) {
                                result.add(destFile.absolutePath)
                                added++
                            }
                        }
                        result
                    }
                    withContext(Dispatchers.Main) {
                        for (path in addedPaths) {
                            photoPaths.add(path)
                            photoAdapter.addPhoto(path)
                        }
                        updatePhotoCount()
                        if (uris.size > 5) Toast.makeText(this@AddRecordActivity, "最多5张照片，已选择前5张", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // 图片查看器返回（删除后更新）
    private val imageViewerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val updated = result.data?.getStringArrayListExtra("updated_photos")
            if (updated != null) {
                photoPaths.clear()
                photoPaths.addAll(updated)
                photoAdapter.updatePhotos(photoPaths)
                updatePhotoCount()
            }
        }
    }

    private val voiceRecognitionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    val current = etObservation.text.toString()
                    etObservation.setText(if (current.isEmpty()) text else "$current\n$text")
                    etObservation.setSelection(etObservation.text?.length ?: 0)
                    tvVoiceStatus.text = "✅ 识别完成"
                    tvVoiceStatus.visibility = View.VISIBLE
                    handler.postDelayed({ tvVoiceStatus.visibility = View.GONE }, 2000)
                }
            }
            else -> {
                tvVoiceStatus.text = "⚠ 识别失败，点击重试"
                tvVoiceStatus.visibility = View.VISIBLE
                tvVoiceStatus.setOnClickListener { tryVoiceRecognition(true); tvVoiceStatus.visibility = View.GONE }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        database = AppDatabase.getDatabase(this)
        audioRecorderHelper = AudioRecorderHelper(this)

        // 获取 ViewModel（跨旋转保留数据）
        viewModel = ViewModelProvider(this)[AddRecordViewModel::class.java]

        // 从 Intent 获取编辑模式参数（Intent 在旋转时保留）
        if (viewModel.editRecordId.value == -1L) {
            editRecordId = intent.getLongExtra("record_id", -1)
            isEditMode = editRecordId > 0
        }

        initViews()
        setupPhotoRecyclerView()
        setupClickListeners()
        setupTextWatchers()
        restoreStateFromViewModel()
        loadFilterDates()
        loadTreatmentGroups()

        if (isEditMode && savedInstanceState == null) loadRecordForEdit()

        // 迁移 onBackPressed → OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedInput()) {
                    AlertDialog.Builder(this@AddRecordActivity)
                        .setTitle("放弃记录？")
                        .setMessage("当前有未保存的内容，确定要退出吗？")
                        .setPositiveButton("退出") { _, _ ->
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                        .setNegativeButton("继续编辑", null)
                        .show()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    /**
     * 从 ViewModel 恢复表单状态到 UI 控件
     * 横竖屏旋转后，ViewModel 数据仍在，重新填充到重建的 View 中
     */
    private fun restoreStateFromViewModel() {
        viewModel.plantingDate.value?.let { if (it.isNotEmpty()) etPlantingDate.setText(it) }
        viewModel.creationDate.value?.let { if (it.isNotEmpty()) etCreationDate.setText(it) }
        viewModel.treatmentGroup.value?.let { if (it.isNotEmpty()) actvTreatmentGroup.setText(it) }
        viewModel.filterDate.value?.let { if (it.isNotEmpty()) actvFilterDate.setText(it) }
        viewModel.observation.value?.let { if (it.isNotEmpty()) etObservation.setText(it) }
        viewModel.notes.value?.let { if (it.isNotEmpty()) etNotes.setText(it) }

        // 恢复照片列表
        viewModel.photoPaths.value?.let { paths ->
            if (paths.isNotEmpty()) {
                photoAdapter.updatePhotos(paths)
                updatePhotoCount()
            }
        }

        // 恢复录音状态
        viewModel.audioPath.value?.let { path ->
            if (File(path).exists()) {
                tvAudioInfo.visibility = View.VISIBLE
                tvAudioInfo.text = "已有录音: ${File(path).name}"
            }
        }

        // 非编辑模式且首次进入时，设置默认日期
        if (!isEditMode && viewModel.plantingDate.value.isNullOrEmpty()) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            etPlantingDate.setText(today)
            etCreationDate.setText(today)
            viewModel.plantingDate.value = today
            viewModel.creationDate.value = today
        }
    }

    /**
     * 添加 TextWatcher 监听输入变化，实时同步到 ViewModel
     * 确保旋转后 ViewModel 中的数据是最新的
     */
    private fun setupTextWatchers() {
        etPlantingDate.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.plantingDate.value = s?.toString() ?: ""
            }
        })
        etCreationDate.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.creationDate.value = s?.toString() ?: ""
            }
        })
        actvTreatmentGroup.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.treatmentGroup.value = s?.toString() ?: ""
            }
        })
        actvFilterDate.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterDate.value = s?.toString() ?: ""
            }
        })
        etObservation.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.observation.value = s?.toString() ?: ""
            }
        })
        etNotes.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                viewModel.notes.value = s?.toString() ?: ""
            }
        })
    }

    /** TextWatcher 简化基类，只重写 afterTextChanged */
    private abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun initViews() {
        etPlantingDate = findViewById(R.id.etPlantingDate)
        etCreationDate = findViewById(R.id.etCreationDate)
        actvTreatmentGroup = findViewById(R.id.actvTreatmentGroup)
        actvFilterDate = findViewById(R.id.actvFilterDate)
        etObservation = findViewById(R.id.etObservation)
        etNotes = findViewById(R.id.etNotes)
        rvPhotos = findViewById(R.id.rvPhotos)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        tvVoiceStatus = findViewById(R.id.tvVoiceStatus)
        tvPhotoCount = findViewById(R.id.tvPhotoCount)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnRecord = findViewById(R.id.btnRecord)
        layoutRecording = findViewById(R.id.layoutRecording)
        tvRecordingTime = findViewById(R.id.tvRecordingTime)
        tvAudioInfo = findViewById(R.id.tvAudioInfo)
        btnSave = findViewById(R.id.btnSave)
        cardYesterdayRef = findViewById(R.id.cardYesterdayRef)
        tvYesterdayRef = findViewById(R.id.tvYesterdayRef)
        rvYesterdayPhotos = findViewById(R.id.rvYesterdayPhotos)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        if (isEditMode) toolbar.title = "编辑实验记录"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 种植日期选择器
        etPlantingDate.setOnClickListener {
            val cal = creationCalendar
            DatePickerDialog(this, { _, year, month, day ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                etPlantingDate.setText(dateStr)
                actvTreatmentGroup.setText("")
                loadTreatmentGroupsForDate(dateStr)
                loadLastReference()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 记录日期选择器（默认今天，可修改）
        etCreationDate.setOnClickListener {
            val cal = creationCalendar
            DatePickerDialog(this, { _, year, month, day ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                etCreationDate.setText(dateStr)
                creationCalendar = Calendar.getInstance().apply { set(year, month, day) }
                loadLastReference() // 记录日期变化时刷新参照
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 顶部筛选下拉框 - 选择已有的种植日期
        actvFilterDate.setOnItemClickListener { _, _, _, _ ->
            val selectedDate = actvFilterDate.text.toString().trim()
            if (selectedDate.isNotEmpty()) {
                etPlantingDate.setText(selectedDate)
                actvTreatmentGroup.setText("")
                loadTreatmentGroupsForDate(selectedDate)
                loadLastReference()
            }
        }

        // 管理处理组
        findViewById<View>(R.id.tvManageGroups).setOnClickListener {
            startActivity(Intent(this, TreatmentGroupManageActivity::class.java))
        }

        // 处理组选择变化
        actvTreatmentGroup.setOnItemClickListener { _, _, _, _ -> loadLastReference() }

        updatePhotoCount()

        // 恢复录音
        if (currentAudioPath != null && File(currentAudioPath!!).exists()) {
            tvAudioInfo.visibility = View.VISIBLE
            tvAudioInfo.text = "已有录音: ${File(currentAudioPath!!).name}"
        }
    }

    private fun updatePhotoCount() {
        tvPhotoCount.text = "  （${photoPaths.size}/5）*必填"
    }

    /**
     * 加载顶部筛选：实验管理中有处理组数据的种植日期
     */
    private fun loadFilterDates() {
        lifecycleScope.launch {
            val dates = withContext(Dispatchers.IO) { database.treatmentGroupDao().getAllPlantingDates() }
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@AddRecordActivity, android.R.layout.simple_dropdown_item_1line, dates)
                actvFilterDate.setAdapter(adapter)
            }
        }
    }

    private fun loadTreatmentGroups() {
        val date = etPlantingDate.text.toString().trim()
        if (date.isNotEmpty()) {
            loadTreatmentGroupsForDate(date)
        }
    }

    private fun loadTreatmentGroupsForDate(plantingDate: String) {
        lifecycleScope.launch {
            val names = withContext(Dispatchers.IO) { database.treatmentGroupDao().getNamesByPlantingDate(plantingDate) }
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@AddRecordActivity, android.R.layout.simple_dropdown_item_1line, names)
                actvTreatmentGroup.setAdapter(adapter)
            }
        }
    }

    /**
     * 加载上次实验参照：同一实验（同一种植日期 + 同一处理组）在记录日期之前最近的一条记录
     */
    private fun loadLastReference() {
        val group = actvTreatmentGroup.text.toString().trim()
        val planting = etPlantingDate.text.toString().trim()
        if (group.isEmpty() || planting.isEmpty()) { cardYesterdayRef.visibility = View.GONE; return }

        // 用当前记录日期的 23:59:59 作为上界，找之前最近的一条
        val beforeTime = creationCalendar.timeInMillis + 24 * 60 * 60 * 1000 - 1
        val excludeId = if (isEditMode) editRecordId else -1L

        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) { database.recordDao().getLatestByPlantingAndGroupBefore(planting, group, beforeTime, excludeId) }

            withContext(Dispatchers.Main) {
                if (record != null) {
                    cardYesterdayRef.visibility = View.VISIBLE
                    val dateInfo = "种植: ${record.plantingDate}  记录: ${FileHelper.formatDate(record.createdAt)}"
                    tvYesterdayRef.text = "上次观察: ${record.observationResult}\n📅 $dateInfo"

                    if (record.photoPaths.isNotEmpty()) {
                        val paths = record.photoPaths.split(",")
                        if (refAdapter == null) {
                            refAdapter = PhotoAdapter(photos = paths.toMutableList(), isEditable = false,
                                onPhotoClick = { pos ->
                                    val intent = Intent(this@AddRecordActivity, ImageViewerActivity::class.java)
                                    intent.putStringArrayListExtra("photos", ArrayList(paths))
                                    intent.putExtra("position", pos)
                                    intent.putExtra("isEditable", false)
                                    startActivity(intent)
                                })
                            rvYesterdayPhotos.layoutManager = LinearLayoutManager(this@AddRecordActivity, LinearLayoutManager.HORIZONTAL, false)
                            rvYesterdayPhotos.adapter = refAdapter
                        } else {
                            refAdapter!!.updatePhotos(paths)
                        }
                        rvYesterdayPhotos.visibility = View.VISIBLE
                    } else {
                        rvYesterdayPhotos.visibility = View.GONE
                    }
                } else {
                    cardYesterdayRef.visibility = View.GONE
                }
            }
        }
    }

    private fun loadRecordForEdit() {
        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) { database.recordDao().getRecordById(editRecordId) }
            withContext(Dispatchers.Main) {
                if (record != null) {
                    etPlantingDate.setText(record.plantingDate)
                    actvTreatmentGroup.setText(record.treatmentGroup)
                    etObservation.setText(record.observationResult)
                    etNotes.setText(record.notes)

                    // 恢复记录日期
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = record.createdAt
                    creationCalendar = cal
                    etCreationDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time))

                    if (record.photoPaths.isNotEmpty()) {
                        photoPaths.clear()
                        photoPaths.addAll(record.photoPaths.split(","))
                        photoAdapter.updatePhotos(photoPaths)
                        updatePhotoCount()
                    }
                    if (record.audioPath.isNotEmpty()) {
                        currentAudioPath = record.audioPath
                        tvAudioInfo.visibility = View.VISIBLE
                        tvAudioInfo.text = "已有录音: ${File(record.audioPath).name}"
                    }
                    btnSave.text = "✅ 更新记录"
                    loadLastReference()
                }
            }
        }
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            photos = photoPaths.toMutableList(),
            onDeleteClick = { position ->
                photoAdapter.removePhoto(position)
                photoPaths.removeAt(position)
                updatePhotoCount()
            },
            onPhotoClick = { position ->
                val intent = Intent(this, ImageViewerActivity::class.java)
                intent.putStringArrayListExtra("photos", ArrayList(photoPaths))
                intent.putExtra("position", position)
                intent.putExtra("isEditable", true)
                imageViewerLauncher.launch(intent)
            },
            isEditable = true
        )
        rvPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPhotos.adapter = photoAdapter
    }

    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener { startVoiceInput() }
        btnTakePhoto.setOnClickListener { takePhoto() }
        findViewById<View>(R.id.btnPickImage).setOnClickListener { pickImageLauncher.launch(arrayOf("image/*")) }
        btnRecord.setOnClickListener { if (isRecording) stopRecording() else startRecording() }
        btnSave.setOnClickListener { saveRecord() }
    }

    // ===== 语音 =====
    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 102)
            return
        }
        tryVoiceRecognition(false)
    }

    private fun tryVoiceRecognition(isRetry: Boolean) {
        // 优先尝试搜狗语音识别
        try {
            val sogouIntent = Intent("com.sogou.android.inputmethod.action.VOICE_RECOGNIZE").apply {
                setPackage("com.sogou.android.inputmethod")
            }
            voiceRecognitionLauncher.launch(sogouIntent)
            return
        } catch (_: Exception) {}

        // 搜狗不可用，回退到系统语音识别
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isRetry) "请重新说出" else "请说出观察记录")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            voiceRecognitionLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "语音识别不可用，请手动输入", Toast.LENGTH_LONG).show()
        }
    }

    // ===== 拍照 =====
    private fun takePhoto() {
        if (photoPaths.size >= 5) { Toast.makeText(this, "最多5张照片", Toast.LENGTH_SHORT).show(); return }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        try {
            // 启动自定义相机（支持缩放）
            val intent = Intent(this, CameraActivity::class.java)
            cameraActivityLauncher.launch(intent)
        } catch (e: Exception) { Toast.makeText(this, "无法启动相机", Toast.LENGTH_SHORT).show() }
    }

    // ===== 录音 =====
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 102)
            return
        }
        val audioPath = audioRecorderHelper.startRecording()
        if (audioPath != null) {
            currentAudioPath = audioPath
            btnRecord.text = "⏹ 停止录音"; layoutRecording.visibility = View.VISIBLE
            recordingStartTime = System.currentTimeMillis(); handler.post(recordingTimerRunnable)
        }
    }

    private fun stopRecording() {
        audioRecorderHelper.stopRecording()
        btnRecord.text = "🎙️ 开始录音"; layoutRecording.visibility = View.GONE
        handler.removeCallbacks(recordingTimerRunnable)
        if (currentAudioPath != null) {
            tvAudioInfo.visibility = View.VISIBLE
            tvAudioInfo.text = "录音: ${File(currentAudioPath!!).name}"
        }
    }

    // ===== 保存 =====
    private fun saveRecord() {
        val plantingDate = etPlantingDate.text.toString().trim()
        val treatmentGroup = actvTreatmentGroup.text.toString().trim()
        val observation = etObservation.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (plantingDate.isEmpty()) { etPlantingDate.error = "请选择种植日期"; return }
        if (treatmentGroup.isEmpty()) { actvTreatmentGroup.error = "请选择处理组"; return }
        if (photoPaths.isEmpty()) { Toast.makeText(this, "请至少拍摄一张实验照片", Toast.LENGTH_SHORT).show(); return }

        // 记录日期：用户选择的日期，转为时间戳（当天 00:00:00）
        val creationDateStr = etCreationDate.text.toString().trim()
        val creationTimestamp = try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(creationDateStr)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        lifecycleScope.launch {
            // 自动保存处理组
            val existing = withContext(Dispatchers.IO) { database.treatmentGroupDao().getNamesByPlantingDate(plantingDate) }
            if (!existing.contains(treatmentGroup)) {
                withContext(Dispatchers.IO) { database.treatmentGroupDao().insert(TreatmentGroupEntity(name = treatmentGroup, plantingDate = plantingDate)) }
            }

            if (isEditMode) {
                val old = withContext(Dispatchers.IO) { database.recordDao().getRecordById(editRecordId) }
                if (old != null) {
                    val updated = old.copy(
                        plantingDate = plantingDate, treatmentGroup = treatmentGroup,
                        observationResult = observation, notes = notes,
                        photoPaths = photoPaths.joinToString(","), audioPath = currentAudioPath ?: "",
                        createdAt = creationTimestamp, updatedAt = System.currentTimeMillis()
                    )
                    withContext(Dispatchers.IO) { database.recordDao().update(updated) }
                    Toast.makeText(this@AddRecordActivity, "已更新", Toast.LENGTH_SHORT).show()
                    HistoryViewModel.needsRefresh = true
                    finish()
                }
            } else {
                val record = RecordEntity(
                    plantingDate = plantingDate, treatmentGroup = treatmentGroup,
                    observationResult = observation, notes = notes,
                    photoPaths = photoPaths.joinToString(","), audioPath = currentAudioPath ?: "",
                    createdAt = creationTimestamp, updatedAt = System.currentTimeMillis()
                )
                val id = withContext(Dispatchers.IO) { database.recordDao().insert(record) }
                if (id > 0) {
                    Toast.makeText(this@AddRecordActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    HistoryViewModel.needsRefresh = true
                    finish()
                }
            }
        }
    }

    private fun hasUnsavedInput(): Boolean {
        // 种植日期和记录日期是系统默认填写的，不算用户输入
        if (actvTreatmentGroup.text?.isNotBlank() == true) return true
        if (etObservation.text?.isNotBlank() == true) return true
        if (etNotes.text?.isNotBlank() == true) return true
        if (photoPaths.isNotEmpty()) return true
        if (currentAudioPath != null) return true
        return false
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) audioRecorderHelper.cancelRecording()
        handler.removeCallbacks(recordingTimerRunnable)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 101) takePhoto()
        }
    }
}
