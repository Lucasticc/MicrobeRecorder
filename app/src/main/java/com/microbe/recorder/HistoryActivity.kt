package com.microbe.recorder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microbe.recorder.adapter.RecordAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recordAdapter: RecordAdapter

    // Views
    private lateinit var etSearch: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var tvRecordCount: TextView
    private lateinit var rvRecords: RecyclerView
    private lateinit var emptyState: View

    private var allRecords: List<RecordEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 初始化数据库
        database = AppDatabase.getDatabase(this)

        // 初始化Views
        initViews()

        // 设置RecyclerView
        setupRecyclerView()

        // 设置搜索功能
        setupSearch()

        // 加载所有记录
        loadAllRecords()
    }

    /**
     * 初始化Views
     */
    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        ivClearSearch = findViewById(R.id.ivClearSearch)
        tvRecordCount = findViewById(R.id.tvRecordCount)
        rvRecords = findViewById(R.id.rvRecords)
        emptyState = findViewById(R.id.emptyState)

        // 设置toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
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

        rvRecords.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = recordAdapter
        }
    }

    /**
     * 设置搜索功能
     */
    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.isEmpty()) {
                    showAllRecords()
                } else {
                    searchRecords(keyword)
                }

                // 显示/隐藏清除按钮
                ivClearSearch.visibility = if (keyword.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 清除搜索按钮
        ivClearSearch.setOnClickListener {
            etSearch.text.clear()
        }
    }

    /**
     * 加载所有记录
     */
    private fun loadAllRecords() {
        lifecycleScope.launch {
            allRecords = withContext(Dispatchers.IO) {
                database.recordDao().getAllRecords()
            }

            withContext(Dispatchers.Main) {
                updateUI(allRecords)
            }
        }
    }

    /**
     * 显示所有记录
     */
    private fun showAllRecords() {
        updateUI(allRecords)
    }

    /**
     * 搜索记录
     */
    private fun searchRecords(keyword: String) {
        val filteredRecords = allRecords.filter { record ->
            record.experimentNumber.contains(keyword, ignoreCase = true) ||
                    record.sampleName.contains(keyword, ignoreCase = true) ||
                    record.observationResult.contains(keyword, ignoreCase = true) ||
                    record.description.contains(keyword, ignoreCase = true)
        }
        updateUI(filteredRecords)
    }

    /**
     * 更新UI
     */
    private fun updateUI(records: List<RecordEntity>) {
        if (records.isEmpty()) {
            rvRecords.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            tvRecordCount.text = "共 0 条记录"
        } else {
            rvRecords.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            tvRecordCount.text = "共 ${records.size} 条记录"
            recordAdapter.updateRecords(records)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到页面时刷新数据
        loadAllRecords()
    }
}
