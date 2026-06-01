package com.microbe.recorder

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.microbe.recorder.adapter.PhotoAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var recordId: Long = -1
    private var currentRecord: RecordEntity? = null
    private var mediaPlayer: MediaPlayer? = null

    // Views
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailSubtitle: TextView
    private lateinit var tvExperimentNumber: TextView
    private lateinit var tvSampleName: TextView
    private lateinit var tvCultureTime: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvObservationResult: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvNotes: TextView
    private lateinit var rvPhotos: RecyclerView
    private lateinit var cardDescription: MaterialCardView
    private lateinit var cardPhotos: MaterialCardView
    private lateinit var cardAudio: MaterialCardView
    private lateinit var cardNotes: MaterialCardView
    private lateinit var btnPlayAudio: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnEdit: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        // 获取记录ID
        recordId = intent.getLongExtra("record_id", -1)
        if (recordId == -1L) {
            Toast.makeText(this, "记录不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 初始化数据库
        database = AppDatabase.getDatabase(this)

        // 初始化Views
        initViews()

        // 加载记录数据
        loadRecord()
    }

    /**
     * 初始化Views
     */
    private fun initViews() {
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        tvDetailSubtitle = findViewById(R.id.tvDetailSubtitle)
        tvExperimentNumber = findViewById(R.id.tvExperimentNumber)
        tvSampleName = findViewById(R.id.tvSampleName)
        tvCultureTime = findViewById(R.id.tvCultureTime)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        tvObservationResult = findViewById(R.id.tvObservationResult)
        tvDescription = findViewById(R.id.tvDescription)
        tvNotes = findViewById(R.id.tvNotes)
        rvPhotos = findViewById(R.id.rvPhotos)
        cardDescription = findViewById(R.id.cardDescription)
        cardPhotos = findViewById(R.id.cardPhotos)
        cardAudio = findViewById(R.id.cardAudio)
        cardNotes = findViewById(R.id.cardNotes)
        btnPlayAudio = findViewById(R.id.btnPlayAudio)
        btnDelete = findViewById(R.id.btnDelete)
        btnEdit = findViewById(R.id.btnEdit)

        // 设置toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // 删除按钮
        btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        // 编辑按钮
        btnEdit.setOnClickListener {
            val intent = android.content.Intent(this, AddRecordActivity::class.java)
            intent.putExtra("record_id", recordId)
            startActivity(intent)
        }

        // 播放录音按钮
        btnPlayAudio.setOnClickListener {
            playAudio()
        }
    }

    /**
     * 加载记录数据
     */
    private fun loadRecord() {
        lifecycleScope.launch {
            val record = withContext(Dispatchers.IO) {
                database.recordDao().getRecordById(recordId)
            }

            withContext(Dispatchers.Main) {
                if (record != null) {
                    currentRecord = record
                    displayRecord(record)
                } else {
                    Toast.makeText(this@RecordDetailActivity, "记录不存在", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    /**
     * 显示记录数据
     */
    private fun displayRecord(record: RecordEntity) {
        // 标题
        tvDetailTitle.text = record.experimentNumber
        tvDetailSubtitle.text = "${record.sampleName} · ${FileHelper.formatDateTime(record.createdAt)}"

        // 基本信息
        tvExperimentNumber.text = record.experimentNumber
        tvSampleName.text = record.sampleName
        tvCultureTime.text = record.cultureTime
        tvCreatedAt.text = FileHelper.formatDateTime(record.createdAt)

        // 观察结果
        tvObservationResult.text = record.observationResult

        // 实验描述
        if (record.description.isNotEmpty()) {
            cardDescription.visibility = View.VISIBLE
            tvDescription.text = record.description
        } else {
            cardDescription.visibility = View.GONE
        }

        // 照片
        if (record.photoPaths.isNotEmpty()) {
            cardPhotos.visibility = View.VISIBLE
            val photoPaths = record.photoPaths.split(",")
            val photoAdapter = PhotoAdapter(
                photos = photoPaths.toMutableList(),
                isEditable = false
            )
            rvPhotos.apply {
                layoutManager = LinearLayoutManager(this@RecordDetailActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = photoAdapter
            }
        } else {
            cardPhotos.visibility = View.GONE
        }

        // 录音
        if (record.audioPath.isNotEmpty()) {
            cardAudio.visibility = View.VISIBLE
            val audioFile = File(record.audioPath)
            if (audioFile.exists()) {
                btnPlayAudio.isEnabled = true
                btnPlayAudio.text = "播放录音 (${audioFile.name})"
            } else {
                btnPlayAudio.isEnabled = false
                btnPlayAudio.text = "录音文件不存在"
            }
        } else {
            cardAudio.visibility = View.GONE
        }

        // 备注
        if (record.notes.isNotEmpty()) {
            cardNotes.visibility = View.VISIBLE
            tvNotes.text = record.notes
        } else {
            cardNotes.visibility = View.GONE
        }
    }

    /**
     * 播放录音
     */
    private fun playAudio() {
        val audioPath = currentRecord?.audioPath ?: return
        val audioFile = File(audioPath)

        if (!audioFile.exists()) {
            Toast.makeText(this, "录音文件不存在", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                btnPlayAudio.text = "播放录音"
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                start()

                setOnCompletionListener {
                    btnPlayAudio.text = "播放录音"
                    release()
                    mediaPlayer = null
                }
            }

            btnPlayAudio.text = "停止播放"
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "播放失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("删除记录")
            .setMessage("确定要删除这条记录吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                deleteRecord()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 删除记录
     */
    private fun deleteRecord() {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    database.recordDao().deleteById(recordId)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    // 删除关联的文件
                    deleteAssociatedFiles()
                    Toast.makeText(this@RecordDetailActivity, "记录已删除", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RecordDetailActivity, "删除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 删除关联的文件（照片、录音）
     */
    private fun deleteAssociatedFiles() {
        currentRecord?.let { record ->
            // 删除照片
            if (record.photoPaths.isNotEmpty()) {
                record.photoPaths.split(",").forEach { path ->
                    File(path).delete()
                }
            }

            // 删除录音
            if (record.audioPath.isNotEmpty()) {
                File(record.audioPath).delete()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
