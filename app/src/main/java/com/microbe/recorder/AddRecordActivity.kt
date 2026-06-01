package com.microbe.recorder

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddRecordActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var audioRecorderHelper: AudioRecorderHelper

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

    private var isRecording = false
    private var currentAudioPath: String? = null
    private val photoPaths = mutableListOf<String>()
    private var editRecordId: Long = -1
    private var isEditMode = false

    // 记录日期（可修改）
    private var creationCalendar = Calendar.getInstance()

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

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        try {
            if (success) {
                val path = CameraHelper.getCurrentPhotoPath()
                if (path != null && File(path).exists() && photoPaths.size < 5) {
                    photoPaths.add(path)
                    photoAdapter.addPhoto(path)
                    updatePhotoCount()
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        finally { CameraHelper.clearCurrentPhoto() }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        try {
            if (uri != null && photoPaths.size < 5) {
                val folder = getPhotoFolder()
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imagesRoot = File(getExternalFilesDir(null), "images")
                val storageDir = if (folder.isNotEmpty()) File(imagesRoot, folder) else imagesRoot
                if (!storageDir.exists()) storageDir.mkdirs()
                val destFile = File(storageDir, "PICK_${timeStamp}.jpg")
                contentResolver.openInputStream(uri)?.use { input -> destFile.outputStream().use { output -> input.copyTo(output) } }
                if (destFile.exists()) {
                    photoPaths.add(destFile.absolutePath)
                    photoAdapter.addPhoto(destFile.absolutePath)
                    updatePhotoCount()
                }
            } else if (photoPaths.size >= 5) {
                Toast.makeText(this, "最多5张照片", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) { e.printStackTrace() }
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

        if (savedInstanceState != null) {
            savedInstanceState.getStringArrayList("photo_paths")?.let { photoPaths.addAll(it) }
            currentAudioPath = savedInstanceState.getString("audio_path")
        }

        editRecordId = intent.getLongExtra("record_id", -1)
        isEditMode = editRecordId > 0

        initViews()
        setupPhotoRecyclerView()
        setupClickListeners()
        loadFilterDates()
        loadTreatmentGroups()

        if (isEditMode) loadRecordForEdit()
        else if (savedInstanceState == null) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            etPlantingDate.setText(today)
            etCreationDate.setText(today)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("photo_paths", ArrayList(photoPaths))
        outState.putString("audio_path", currentAudioPath)
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
            val cal = Calendar.getInstance()
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
            DatePickerDialog(this, { _, year, month, day ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                etCreationDate.setText(dateStr)
                creationCalendar.set(year, month, day)
                loadLastReference() // 记录日期变化时刷新参照
            }, creationCalendar.get(Calendar.YEAR), creationCalendar.get(Calendar.MONTH), creationCalendar.get(Calendar.DAY_OF_MONTH)).show()
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
     * 加载上次实验参照：同一处理组在记录日期之前最近的一条记录
     */
    private fun loadLastReference() {
        val group = actvTreatmentGroup.text.toString().trim()
        if (group.isEmpty()) { cardYesterdayRef.visibility = View.GONE; return }

        // 用当前记录日期的 23:59:59 作为上界，找之前最近的一条
        val beforeTime = creationCalendar.timeInMillis + 24 * 60 * 60 * 1000 - 1

        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) { database.recordDao().getLatestByTreatmentGroupBefore(group, beforeTime) }

            withContext(Dispatchers.Main) {
                if (record != null) {
                    cardYesterdayRef.visibility = View.VISIBLE
                    val dateInfo = "种植: ${record.plantingDate}  记录: ${FileHelper.formatDate(record.createdAt)}"
                    tvYesterdayRef.text = "上次观察: ${record.observationResult}\n📅 $dateInfo"

                    if (record.photoPaths.isNotEmpty()) {
                        val paths = record.photoPaths.split(",")
                        val refAdapter = PhotoAdapter(photos = paths.toMutableList(), isEditable = false)
                        rvYesterdayPhotos.layoutManager = LinearLayoutManager(this@AddRecordActivity, LinearLayoutManager.HORIZONTAL, false)
                        rvYesterdayPhotos.adapter = refAdapter
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
                        photoAdapter.notifyDataSetChanged()
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
            isEditable = true
        )
        rvPhotos.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPhotos.adapter = photoAdapter
    }

    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener { startVoiceInput() }
        btnTakePhoto.setOnClickListener { takePhoto() }
        findViewById<View>(R.id.btnPickImage).setOnClickListener { pickImageLauncher.launch("image/*") }
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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (isRetry) "请重新说出" else "请说出观察记录")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        val mfr = Build.MANUFACTURER.lowercase()
        val services = when {
            mfr.contains("samsung") -> listOf(
                "com.samsung.android.bixby.agent" to "com.samsung.android.bixby.agent.mainui.voiceinteraction.RecognitionServiceTrampoline",
                "com.samsung.android.vassistant" to "com.samsung.android.vassistant.service.VoiceRecognitionService"
            )
            mfr.contains("meizu") -> listOf(
                "com.meizu.voiceassistant" to "com.meizu.voiceassistant.speech.RecognitionService"
            )
            mfr.contains("xiaomi") || mfr.contains("redmi") -> listOf(
                "com.miui.voiceassist" to "com.miui.voiceassist.VoiceRecognitionService"
            )
            mfr.contains("huawei") || mfr.contains("honor") -> listOf(
                "com.huawei.vassistant" to "com.huawei.vassistant.service.VoiceRecognitionService"
            )
            else -> emptyList()
        }

        for ((pkg, cls) in services) {
            try {
                intent.component = android.content.ComponentName(pkg, cls)
                voiceRecognitionLauncher.launch(intent)
                return
            } catch (_: Exception) { continue }
        }

        try {
            intent.component = null
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
            val folder = getPhotoFolder()
            val (uri, _) = CameraHelper.createImageFile(this, folder)
            takePictureLauncher.launch(uri)
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
            isRecording = true; currentAudioPath = audioPath
            btnRecord.text = "⏹ 停止录音"; layoutRecording.visibility = View.VISIBLE
            recordingStartTime = System.currentTimeMillis(); handler.post(recordingTimerRunnable)
        }
    }

    private fun stopRecording() {
        audioRecorderHelper.stopRecording()
        isRecording = false; btnRecord.text = "🎙️ 开始录音"; layoutRecording.visibility = View.GONE
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
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilterDates()
        loadTreatmentGroups()
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
