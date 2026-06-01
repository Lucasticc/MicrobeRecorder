package com.microbe.recorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.microbe.recorder.adapter.RecordAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.util.ExcelExporter
import com.microbe.recorder.util.FileHelper
import com.microbe.recorder.util.WordExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recordAdapter: RecordAdapter

    // Views
    private lateinit var tvTotalCount: TextView
    private lateinit var tvTodayCount: TextView
    private lateinit var tvWeekCount: TextView
    private lateinit var rvRecentRecords: RecyclerView
    private lateinit var emptyState: View

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化数据库
        database = AppDatabase.getDatabase(this)

        // 初始化Views
        initViews()

        // 设置RecyclerView
        setupRecyclerView()

        // 设置点击事件
        setupClickListeners()

        // 请求权限
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        // 每次回到主界面时刷新数据
        loadRecentRecords()
        loadStatistics()
    }

    /**
     * 初始化Views
     */
    private fun initViews() {
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvTodayCount = findViewById(R.id.tvTodayCount)
        tvWeekCount = findViewById(R.id.tvWeekCount)
        rvRecentRecords = findViewById(R.id.rvRecentRecords)
        emptyState = findViewById(R.id.emptyState)

        // 设置toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        recordAdapter = RecordAdapter { record ->
            // 点击记录跳转到详情
            val intent = Intent(this, RecordDetailActivity::class.java)
            intent.putExtra("record_id", record.id)
            startActivity(intent)
        }

        rvRecentRecords.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recordAdapter
        }
    }

    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        // 新建记录按钮
        findViewById<MaterialButton>(R.id.btnAddRecord).setOnClickListener {
            val intent = Intent(this, AddRecordActivity::class.java)
            startActivity(intent)
        }

        // 历史记录按钮
        findViewById<MaterialButton>(R.id.btnHistory).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // 导出Excel按钮
        findViewById<MaterialButton>(R.id.btnExportExcel).setOnClickListener {
            exportToExcel()
        }

        // 导出Word按钮
        findViewById<MaterialButton>(R.id.btnExportWord).setOnClickListener {
            exportToWord()
        }

        // 样品管理
        findViewById<View>(R.id.tvManageSamples).setOnClickListener {
            val intent = Intent(this, SampleManageActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 加载最近记录
     */
    private fun loadRecentRecords() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                database.recordDao().getAllRecords().take(10) // 只显示最近10条
            }

            withContext(Dispatchers.Main) {
                if (records.isEmpty()) {
                    rvRecentRecords.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                } else {
                    rvRecentRecords.visibility = View.VISIBLE
                    emptyState.visibility = View.GONE
                    recordAdapter.updateRecords(records)
                }
            }
        }
    }

    /**
     * 加载统计数据
     */
    private fun loadStatistics() {
        lifecycleScope.launch {
            val allRecords = withContext(Dispatchers.IO) {
                database.recordDao().getAllRecords()
            }

            withContext(Dispatchers.Main) {
                // 总记录数
                tvTotalCount.text = allRecords.size.toString()

                // 今日记录数
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayCount = allRecords.count { record ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(record.createdAt)) == today
                }
                tvTodayCount.text = todayCount.toString()

                // 本周记录数
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                val weekStart = cal.timeInMillis
                val weekCount = allRecords.count { it.createdAt >= weekStart }
                tvWeekCount.text = weekCount.toString()
            }
        }
    }

    /**
     * 导出Excel
     */
    private fun exportToExcel() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                database.recordDao().getAllRecords()
            }

            if (records.isEmpty()) {
                Toast.makeText(this@MainActivity, "没有可导出的记录", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val file = withContext(Dispatchers.IO) {
                ExcelExporter.exportToExcel(this@MainActivity, records)
            }

            withContext(Dispatchers.Main) {
                if (file != null) {
                    Toast.makeText(this@MainActivity, "Excel导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                    try {
                        FileHelper.shareFile(this@MainActivity, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "文件已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Excel导出失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 导出Word
     */
    private fun exportToWord() {
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                database.recordDao().getAllRecords()
            }

            if (records.isEmpty()) {
                Toast.makeText(this@MainActivity, "没有可导出的记录", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val file = withContext(Dispatchers.IO) {
                WordExporter.exportToWord(this@MainActivity, records)
            }

            withContext(Dispatchers.Main) {
                if (file != null) {
                    Toast.makeText(this@MainActivity, "Word导出成功: ${file.name}", Toast.LENGTH_LONG).show()
                    try {
                        FileHelper.shareFile(this@MainActivity, file, "application/msword")
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "文件已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Word导出失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 请求权限
     */
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    when (permission) {
                        Manifest.permission.CAMERA -> {
                            Toast.makeText(this, "相机权限已授予", Toast.LENGTH_SHORT).show()
                        }
                        Manifest.permission.RECORD_AUDIO -> {
                            Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    when (permission) {
                        Manifest.permission.CAMERA -> {
                            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_LONG).show()
                        }
                        Manifest.permission.RECORD_AUDIO -> {
                            Toast.makeText(this, "需要录音权限才能录音", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
