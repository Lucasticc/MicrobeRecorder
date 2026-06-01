package com.microbe.recorder

import android.Manifest
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.microbe.recorder.adapter.PhotoAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.database.SampleTypeEntity
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

    // Views
    private lateinit var etExperimentNumber: TextInputEditText
    private lateinit var actvSampleName: AutoCompleteTextView
    private lateinit var etCultureTime: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var rvPhotos: RecyclerView
    private lateinit var btnVoiceInput: MaterialButton
    private lateinit var tvVoiceStatus: TextView
    private lateinit var btnTakePhoto: MaterialButton
    private lateinit var btnRecord: MaterialButton
    private lateinit var layoutRecording: View
    private lateinit var tvRecordingTime: TextView
    private lateinit var tvAudioInfo: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var cardYesterdayRef: MaterialCardView
    private lateinit var tvYesterdayRef: TextView
    private lateinit var tvYesterdayDate: TextView

    // 状态
    private var isRecording = false
    private var currentAudioPath: String? = null
    private val photoPaths = mutableListOf<String>()

    // 编辑模式
    private var editRecordId: Long = -1
    private var isEditMode = false

    // 录音计时器
    private val handler = Handler(Looper.getMainLooper())
    private var recordingStartTime = 0L
    private val recordingTimerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - recordingStartTime
            val seconds = (elapsed / 1000).toInt()
            val minutes = seconds / 60
            val secs = seconds % 60
            tvRecordingTime.text = String.format("录音中... %02d:%02d", minutes, secs)
            handler.postDelayed(this, 1000)
        }
    }

    // ===== 拍照 =====
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        try {
            if (success) {
                val photoPath = CameraHelper.getCurrentPhotoPath()
                if (photoPath != null && File(photoPath).exists()) {
                    photoPaths.add(photoPath)
                    photoAdapter.addPhoto(photoPath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "拍照处理失败", Toast.LENGTH_SHORT).show()
        } finally {
            CameraHelper.clearCurrentPhoto()
        }
    }

    // ===== 从相册选择 =====
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        try {
            if (uri != null) {
                // 复制到 app 私有目录
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir = File(getExternalFilesDir(null), "images")
                if (!storageDir.exists()) storageDir.mkdirs()
                val destFile = File(storageDir, "PICK_${timeStamp}.jpg")

                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }

                if (destFile.exists()) {
                    photoPaths.add(destFile.absolutePath)
                    photoAdapter.addPhoto(destFile.absolutePath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "选择图片失败", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== 系统语音识别 =====
    private val voiceRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                val current = etDescription.text.toString()
                if (current.isEmpty()) {
                    etDescription.setText(text)
                } else {
                    etDescription.setText("$current\n$text")
                }
                etDescription.setSelection(etDescription.text?.length ?: 0)
                tvVoiceStatus.text = "✅ 识别完成: $text"
                tvVoiceStatus.visibility = View.VISIBLE
                handler.postDelayed({ tvVoiceStatus.visibility = View.GONE }, 3000)
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
        private const val AUDIO_PERMISSION_CODE = 102

        // 保存状态的 Key
        private const val KEY_PHOTO_PATHS = "photo_paths"
        private const val KEY_AUDIO_PATH = "audio_path"
    }

    // ===== 保存状态（防止 Activity 被回收后数据丢失）=====
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(KEY_PHOTO_PATHS, ArrayList(photoPaths))
        outState.putString(KEY_AUDIO_PATH, currentAudioPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        database = AppDatabase.getDatabase(this)
        audioRecorderHelper = AudioRecorderHelper(this)

        // 恢复被回收的状态
        if (savedInstanceState != null) {
            val savedPaths = savedInstanceState.getStringArrayList(KEY_PHOTO_PATHS)
            if (savedPaths != null) {
                photoPaths.addAll(savedPaths)
            }
            currentAudioPath = savedInstanceState.getString(KEY_AUDIO_PATH)
        }

        // 检查是否编辑模式
        editRecordId = intent.getLongExtra("record_id", -1)
        isEditMode = editRecordId > 0

        initViews()
        setupPhotoRecyclerView()
        setupClickListeners()
        loadSampleTypes()

        if (isEditMode) {
            loadRecordForEdit()
        } else if (savedInstanceState == null) {
            // 只在首次创建时生成编号，恢复时不覆盖
            generateExperimentNumber()
        }
    }

    private fun initViews() {
        etExperimentNumber = findViewById(R.id.etExperimentNumber)
        actvSampleName = findViewById(R.id.actvSampleName)
        etCultureTime = findViewById(R.id.etCultureTime)
        etDescription = findViewById(R.id.etDescription)
        etNotes = findViewById(R.id.etNotes)
        rvPhotos = findViewById(R.id.rvPhotos)
        btnVoiceInput = findViewById(R.id.btnVoiceInput)
        tvVoiceStatus = findViewById(R.id.tvVoiceStatus)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnRecord = findViewById(R.id.btnRecord)
        layoutRecording = findViewById(R.id.layoutRecording)
        tvRecordingTime = findViewById(R.id.tvRecordingTime)
        tvAudioInfo = findViewById(R.id.tvAudioInfo)
        btnSave = findViewById(R.id.btnSave)
        cardYesterdayRef = findViewById(R.id.cardYesterdayRef)
        tvYesterdayRef = findViewById(R.id.tvYesterdayRef)
        tvYesterdayDate = findViewById(R.id.tvYesterdayDate)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        if (isEditMode) toolbar.title = "编辑实验记录"
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 管理样品按钮
        findViewById<View>(R.id.tvManageSamples).setOnClickListener {
            startActivity(Intent(this, SampleManageActivity::class.java))
        }

        // 恢复录音信息显示
        if (currentAudioPath != null && File(currentAudioPath!!).exists()) {
            val file = File(currentAudioPath!!)
            tvAudioInfo.visibility = View.VISIBLE
            tvAudioInfo.text = "已有录音: ${file.name} (${FileHelper.formatFileSize(file.length())})"
        }

        // 样品选择变化时加载昨日参考
        actvSampleName.setOnItemClickListener { _, _, _, _ -> loadYesterdayReference() }
        actvSampleName.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) loadYesterdayReference() }
    }

    private fun loadSampleTypes() {
        lifecycleScope.launch {
            val names = withContext(Dispatchers.IO) { database.sampleTypeDao().getAllSampleNames() }
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@AddRecordActivity, android.R.layout.simple_dropdown_item_1line, names)
                actvSampleName.setAdapter(adapter)
            }
        }
    }

    private fun loadYesterdayReference() {
        val sampleName = actvSampleName.text.toString().trim()
        if (sampleName.isEmpty()) { cardYesterdayRef.visibility = View.GONE; return }

        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) { database.recordDao().getRecordsBySampleName(sampleName) }

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val yesterdayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
            val yesterdayEnd = cal.timeInMillis

            val yesterdayRecord = records.find { it.createdAt in yesterdayStart..yesterdayEnd } ?: records.firstOrNull()

            withContext(Dispatchers.Main) {
                if (yesterdayRecord != null) {
                    cardYesterdayRef.visibility = View.VISIBLE
                    tvYesterdayRef.text = "观察: ${yesterdayRecord.observationResult}\n描述: ${yesterdayRecord.description}"
                    tvYesterdayDate.text = "📅 ${FileHelper.formatDateTime(yesterdayRecord.createdAt)}"
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
                    etExperimentNumber.setText(record.experimentNumber)
                    actvSampleName.setText(record.sampleName)
                    etCultureTime.setText(record.cultureTime)
                    // 合并观察结果和描述
                    val merged = listOf(record.observationResult, record.description)
                        .filter { it.isNotEmpty() }
                        .joinToString("\n")
                    etDescription.setText(merged)
                    etNotes.setText(record.notes)

                    if (record.photoPaths.isNotEmpty()) {
                        val paths = record.photoPaths.split(",")
                        photoPaths.clear()
                        photoPaths.addAll(paths)
                        photoAdapter.notifyDataSetChanged()
                    }

                    if (record.audioPath.isNotEmpty()) {
                        currentAudioPath = record.audioPath
                        val file = File(record.audioPath)
                        tvAudioInfo.visibility = View.VISIBLE
                        tvAudioInfo.text = "已有录音: ${file.name}"
                    }

                    btnSave.text = "✅ 更新记录"
                    loadYesterdayReference()
                }
            }
        }
    }

    private fun generateExperimentNumber() {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val random = (1000..9999).random()
        etExperimentNumber.setText("EXP-${dateStr}-${random}")
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            photos = photoPaths.toMutableList(),
            onDeleteClick = { position -> photoAdapter.removePhoto(position); photoPaths.removeAt(position) },
            isEditable = true
        )
        rvPhotos.apply {
            layoutManager = LinearLayoutManager(this@AddRecordActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        btnVoiceInput.setOnClickListener { startVoiceInput() }
        btnTakePhoto.setOnClickListener { takePhoto() }
        findViewById<View>(R.id.btnPickImage).setOnClickListener { pickImageLauncher.launch("image/*") }
        btnRecord.setOnClickListener { if (isRecording) stopRecording() else startRecording() }
        btnSave.setOnClickListener { saveRecord() }
    }

    // ===== 语音输入：自动匹配机型 =====
    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出实验描述")
        }

        val manufacturer = Build.MANUFACTURER.lowercase()

        // 三星：Bixby 语音
        if (manufacturer.contains("samsung")) {
            try {
                intent.component = android.content.ComponentName(
                    "com.samsung.android.bixby.agent",
                    "com.samsung.android.bixby.agent.mainui.voiceinteraction.RecognitionServiceTrampoline"
                )
                voiceRecognitionLauncher.launch(intent)
                return
            } catch (_: Exception) { }
        }

        // 魅族：Aicy 语音
        if (manufacturer.contains("meizu")) {
            try {
                intent.component = android.content.ComponentName(
                    "com.meizu.voiceassistant",
                    "com.meizu.voiceassistant.speech.RecognitionService"
                )
                voiceRecognitionLauncher.launch(intent)
                return
            } catch (_: Exception) { }
        }

        // 通用兜底：系统默认语音
        try {
            voiceRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            android.app.AlertDialog.Builder(this)
                .setTitle("语音识别不可用")
                .setMessage("您的设备未安装语音识别服务。\n\n请安装「讯飞语记」或「百度输入法」等带语音识别的应用。\n\n也可以直接手动输入。")
                .setPositiveButton("确定", null)
                .show()
        }
    }

    // ===== 拍照 =====
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return
        }
        try {
            val (uri, _) = CameraHelper.createImageFile(this)
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "无法启动相机: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== 录音 =====
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
            return
        }
        val audioPath = audioRecorderHelper.startRecording()
        if (audioPath != null) {
            isRecording = true
            currentAudioPath = audioPath
            btnRecord.text = "⏹ 停止录音"
            layoutRecording.visibility = View.VISIBLE
            recordingStartTime = System.currentTimeMillis()
            handler.post(recordingTimerRunnable)
        } else {
            Toast.makeText(this, "录音启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        val audioPath = audioRecorderHelper.stopRecording()
        isRecording = false
        btnRecord.text = "🎙️ 开始录音"
        layoutRecording.visibility = View.GONE
        handler.removeCallbacks(recordingTimerRunnable)

        if (audioPath != null) {
            currentAudioPath = audioPath
            val file = File(audioPath)
            tvAudioInfo.visibility = View.VISIBLE
            tvAudioInfo.text = "录音: ${file.name} (${FileHelper.formatFileSize(file.length())})"
            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== 保存 =====
    private fun saveRecord() {
        val experimentNumber = etExperimentNumber.text.toString().trim()
        val sampleName = actvSampleName.text.toString().trim()
        val cultureTime = etCultureTime.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        if (experimentNumber.isEmpty()) { etExperimentNumber.error = "请输入实验编号"; return }
        if (sampleName.isEmpty()) { actvSampleName.error = "请选择或输入样品名称"; return }
        if (photoPaths.isEmpty()) { Toast.makeText(this, "请至少拍摄一张实验照片", Toast.LENGTH_SHORT).show(); return }

        // 统一存到 description 字段，observationResult 留空
        val observationResult = ""
        val desc = description

        lifecycleScope.launch {
            val existingNames = withContext(Dispatchers.IO) { database.sampleTypeDao().getAllSampleNames() }
            if (!existingNames.contains(sampleName)) {
                withContext(Dispatchers.IO) { database.sampleTypeDao().insert(SampleTypeEntity(name = sampleName)) }
            }

            if (isEditMode) {
                val existing = withContext(Dispatchers.IO) { database.recordDao().getRecordById(editRecordId) }
                if (existing != null) {
                    val updated = existing.copy(
                        experimentNumber = experimentNumber, sampleName = sampleName,
                        cultureTime = cultureTime, observationResult = observationResult,
                        description = desc, notes = notes,
                        photoPaths = photoPaths.joinToString(","), audioPath = currentAudioPath ?: "",
                        updatedAt = System.currentTimeMillis()
                    )
                    withContext(Dispatchers.IO) { database.recordDao().update(updated) }
                    Toast.makeText(this@AddRecordActivity, "记录已更新", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                val record = RecordEntity(
                    experimentNumber = experimentNumber, sampleName = sampleName,
                    cultureTime = cultureTime, observationResult = observationResult,
                    description = desc, notes = notes,
                    photoPaths = photoPaths.joinToString(","), audioPath = currentAudioPath ?: "",
                    createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                )
                val id = withContext(Dispatchers.IO) { database.recordDao().insert(record) }
                if (id > 0) {
                    Toast.makeText(this@AddRecordActivity, "记录保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddRecordActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) takePhoto()
                else Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_LONG).show()
            }
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startRecording()
                else Toast.makeText(this, "需要录音权限才能录音", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSampleTypes()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) audioRecorderHelper.cancelRecording()
        handler.removeCallbacks(recordingTimerRunnable)
    }
}
