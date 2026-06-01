package com.microbe.recorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
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
import com.microbe.recorder.util.AudioRecorderHelper
import com.microbe.recorder.util.CameraHelper
import com.microbe.recorder.util.SpeechToTextHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AddRecordActivity : AppCompatActivity(), SpeechToTextHelper.SpeechToTextListener {

    private lateinit var database: AppDatabase
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var audioRecorderHelper: AudioRecorderHelper
    private lateinit var speechToTextHelper: SpeechToTextHelper

    // Views
    private lateinit var etExperimentNumber: TextInputEditText
    private lateinit var etSampleName: TextInputEditText
    private lateinit var etCultureTime: TextInputEditText
    private lateinit var etObservationResult: TextInputEditText
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

    // 状态
    private var isRecording = false
    private var isVoiceListening = false
    private var currentAudioPath: String? = null
    private val photoPaths = mutableListOf<String>()

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

    // 相机拍照结果
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val photoPath = CameraHelper.getCurrentPhotoPath()
            if (photoPath != null) {
                photoPaths.add(photoPath)
                photoAdapter.addPhoto(photoPath)
            }
        }
        CameraHelper.clearCurrentPhoto()
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
        private const val AUDIO_PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)

        // 初始化数据库
        database = AppDatabase.getDatabase(this)

        // 初始化工具类
        audioRecorderHelper = AudioRecorderHelper(this)
        speechToTextHelper = SpeechToTextHelper(this, this)

        // 初始化Views
        initViews()

        // 设置RecyclerView
        setupPhotoRecyclerView()

        // 设置点击事件
        setupClickListeners()
    }

    /**
     * 初始化Views
     */
    private fun initViews() {
        etExperimentNumber = findViewById(R.id.etExperimentNumber)
        etSampleName = findViewById(R.id.etSampleName)
        etCultureTime = findViewById(R.id.etCultureTime)
        etObservationResult = findViewById(R.id.etObservationResult)
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

        // 设置toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // 自动生成实验编号
        generateExperimentNumber()
    }

    /**
     * 生成实验编号
     */
    private fun generateExperimentNumber() {
        val timestamp = System.currentTimeMillis()
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        val random = (1000..9999).random()
        etExperimentNumber.setText("EXP-${dateStr}-${random}")
    }

    /**
     * 设置照片RecyclerView
     */
    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            photos = photoPaths.toMutableList(),
            onDeleteClick = { position ->
                photoAdapter.removePhoto(position)
                photoPaths.removeAt(position)
            },
            isEditable = true
        )

        rvPhotos.apply {
            layoutManager = LinearLayoutManager(this@AddRecordActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }
    }

    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        // 语音输入按钮
        btnVoiceInput.setOnClickListener {
            if (isVoiceListening) {
                stopVoiceInput()
            } else {
                startVoiceInput()
            }
        }

        // 拍照按钮
        btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        // 录音按钮
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        // 保存按钮
        btnSave.setOnClickListener {
            saveRecord()
        }
    }

    /**
     * 开始语音输入
     */
    private fun startVoiceInput() {
        if (!speechToTextHelper.isSpeechRecognitionAvailable()) {
            Toast.makeText(this, "设备不支持语音识别", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
            return
        }

        speechToTextHelper.startListening()
        isVoiceListening = true
        btnVoiceInput.text = "停止语音输入"
        tvVoiceStatus.visibility = View.VISIBLE
        tvVoiceStatus.text = "正在聆听..."
    }

    /**
     * 停止语音输入
     */
    private fun stopVoiceInput() {
        speechToTextHelper.stopListening()
        isVoiceListening = false
        btnVoiceInput.text = "开始语音输入"
        tvVoiceStatus.visibility = View.GONE
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            return
        }

        val (uri, file) = CameraHelper.createImageFile(this)
        takePictureLauncher.launch(uri)
    }

    /**
     * 开始录音
     */
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_CODE)
            return
        }

        val audioPath = audioRecorderHelper.startRecording()
        if (audioPath != null) {
            isRecording = true
            currentAudioPath = audioPath
            btnRecord.text = "停止录音"
            layoutRecording.visibility = View.VISIBLE

            // 开始计时
            recordingStartTime = System.currentTimeMillis()
            handler.post(recordingTimerRunnable)
        } else {
            Toast.makeText(this, "录音启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 停止录音
     */
    private fun stopRecording() {
        val audioPath = audioRecorderHelper.stopRecording()
        isRecording = false
        btnRecord.text = "开始录音"
        layoutRecording.visibility = View.GONE

        // 停止计时
        handler.removeCallbacks(recordingTimerRunnable)

        if (audioPath != null) {
            currentAudioPath = audioPath
            val file = File(audioPath)
            tvAudioInfo.visibility = View.VISIBLE
            tvAudioInfo.text = "录音文件: ${file.name} (${com.microbe.recorder.util.FileHelper.formatFileSize(file.length())})"
            Toast.makeText(this, "录音已保存", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 保存记录
     */
    private fun saveRecord() {
        val experimentNumber = etExperimentNumber.text.toString().trim()
        val sampleName = etSampleName.text.toString().trim()
        val cultureTime = etCultureTime.text.toString().trim()
        val observationResult = etObservationResult.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        // 验证必填字段
        if (experimentNumber.isEmpty()) {
            etExperimentNumber.error = "请输入实验编号"
            return
        }
        if (sampleName.isEmpty()) {
            etSampleName.error = "请输入样品名称"
            return
        }
        if (observationResult.isEmpty()) {
            etObservationResult.error = "请输入观察结果"
            return
        }

        // 创建记录
        val record = RecordEntity(
            experimentNumber = experimentNumber,
            sampleName = sampleName,
            cultureTime = cultureTime,
            observationResult = observationResult,
            notes = notes,
            description = description,
            photoPaths = photoPaths.joinToString(","),
            audioPath = currentAudioPath ?: "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // 保存到数据库
        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) {
                database.recordDao().insert(record)
            }

            withContext(Dispatchers.Main) {
                if (id > 0) {
                    Toast.makeText(this@AddRecordActivity, "记录保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddRecordActivity, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // SpeechToTextListener 实现
    override fun onSpeechResult(text: String) {
        runOnUiThread {
            val currentText = etDescription.text.toString()
            if (currentText.isEmpty()) {
                etDescription.setText(text)
            } else {
                etDescription.setText("$currentText $text")
            }
            etDescription.setSelection(etDescription.text?.length ?: 0)
            tvVoiceStatus.text = "识别完成"
        }
    }

    override fun onSpeechPartialResult(text: String) {
        runOnUiThread {
            tvVoiceStatus.text = "识别中: $text"
        }
    }

    override fun onSpeechError(error: String) {
        runOnUiThread {
            tvVoiceStatus.text = "错误: $error"
            isVoiceListening = false
            btnVoiceInput.text = "开始语音输入"
        }
    }

    override fun onSpeechStarted() {
        runOnUiThread {
            tvVoiceStatus.text = "正在聆听..."
        }
    }

    override fun onSpeechEnded() {
        runOnUiThread {
            tvVoiceStatus.text = "识别完成"
            isVoiceListening = false
            btnVoiceInput.text = "开始语音输入"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_LONG).show()
                }
            }
            AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限授予后，根据当前状态决定是开始录音还是语音识别
                    if (!isRecording) {
                        startRecording()
                    }
                } else {
                    Toast.makeText(this, "需要录音权限才能录音", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理资源
        if (isRecording) {
            audioRecorderHelper.cancelRecording()
        }
        handler.removeCallbacks(recordingTimerRunnable)
        speechToTextHelper.destroy()
    }
}
