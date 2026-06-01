package com.microbe.recorder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.microbe.recorder.adapter.RecordAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recordAdapter: RecordAdapter

    private lateinit var etSearch: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var tvRecordCount: TextView
    private lateinit var rvRecords: RecyclerView
    private lateinit var emptyState: View
    private lateinit var batchBar: View
    private lateinit var tvSelectCount: TextView
    private lateinit var btnSelectAll: MaterialButton
    private lateinit var btnBatchDelete: MaterialButton
    private lateinit var btnCancelSelect: MaterialButton
    private lateinit var tvSelectMode: TextView

    private var allRecords: List<RecordEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        database = AppDatabase.getDatabase(this)
        initViews()
        setupRecyclerView()
        setupSearch()
        loadAllRecords()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        ivClearSearch = findViewById(R.id.ivClearSearch)
        tvRecordCount = findViewById(R.id.tvRecordCount)
        rvRecords = findViewById(R.id.rvRecords)
        emptyState = findViewById(R.id.emptyState)
        batchBar = findViewById(R.id.batchBar)
        tvSelectCount = findViewById(R.id.tvSelectCount)
        btnSelectAll = findViewById(R.id.btnSelectAll)
        btnBatchDelete = findViewById(R.id.btnBatchDelete)
        btnCancelSelect = findViewById(R.id.btnCancelSelect)
        tvSelectMode = findViewById(R.id.tvSelectMode)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 批量操作按钮
        btnSelectAll.setOnClickListener { recordAdapter.selectAll() }
        btnBatchDelete.setOnClickListener { batchDelete() }
        btnCancelSelect.setOnClickListener { exitSelectionMode() }

        // 选择模式入口
        tvSelectMode.setOnClickListener { enterSelectionMode() }
    }

    private fun setupRecyclerView() {
        recordAdapter = RecordAdapter(
            onItemClick = { record ->
                if (recordAdapter.isInSelectionMode()) {
                    recordAdapter.toggleSelection(record.id)
                } else {
                    val intent = Intent(this, RecordDetailActivity::class.java)
                    intent.putExtra("record_id", record.id)
                    startActivity(intent)
                }
            }
        )

        recordAdapter.setOnSelectionChangedListener { count ->
            tvSelectCount.text = "已选 $count 条"
            if (count == 0 && recordAdapter.isInSelectionMode()) {
                exitSelectionMode()
            }
        }

        rvRecords.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = recordAdapter
        }

        // 长按进入选择模式
        recordAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {})
    }

    // 长按列表项进入选择模式（在 item 布局中通过 itemView 的 longClick 处理）
    // 这里改为：点击工具栏的「选择」按钮进入

    private fun enterSelectionMode() {
        recordAdapter.setSelectionMode(true)
        batchBar.visibility = View.VISIBLE
        tvSelectCount.text = "已选 0 条"
    }

    private fun exitSelectionMode() {
        recordAdapter.setSelectionMode(false)
        batchBar.visibility = View.GONE
    }

    private fun batchDelete() {
        val selectedIds = recordAdapter.getSelectedIds()
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("批量删除")
            .setMessage("确定要删除选中的 ${selectedIds.size} 条记录吗？此操作不可撤销。")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        selectedIds.forEach { id ->
                            database.recordDao().deleteById(id)
                        }
                    }
                    Toast.makeText(this@HistoryActivity, "已删除 ${selectedIds.size} 条记录", Toast.LENGTH_SHORT).show()
                    exitSelectionMode()
                    loadAllRecords()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.isEmpty()) showAllRecords() else searchRecords(keyword)
                ivClearSearch.visibility = if (keyword.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        ivClearSearch.setOnClickListener { etSearch.text.clear() }
    }

    private fun loadAllRecords() {
        lifecycleScope.launch {
            allRecords = withContext(Dispatchers.IO) { database.recordDao().getAllRecords() }
            withContext(Dispatchers.Main) { updateUI(allRecords) }
        }
    }

    private fun showAllRecords() { updateUI(allRecords) }

    private fun searchRecords(keyword: String) {
        val filtered = allRecords.filter {
            it.plantingDate.contains(keyword, ignoreCase = true) ||
                    it.treatmentGroup.contains(keyword, ignoreCase = true) ||
                    it.observationResult.contains(keyword, ignoreCase = true)
        }
        updateUI(filtered)
    }

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
        loadAllRecords()
    }

    override fun onBackPressed() {
        if (recordAdapter.isInSelectionMode()) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }
}
