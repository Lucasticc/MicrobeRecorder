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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.microbe.recorder.adapter.RecordAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.util.HistoryHelper
import com.microbe.recorder.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 历史记录列表页面
 *
 * 使用 ViewModel 保留搜索关键词和选择模式状态，横竖屏旋转时不会丢失。
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var historyHelper: HistoryHelper
    private lateinit var recordAdapter: RecordAdapter
    private lateinit var viewModel: HistoryViewModel

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
        historyHelper = HistoryHelper(this, database)
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        initViews()
        setupRecyclerView()
        setupSearch()
        loadAllRecords()

        // 迁移 onBackPressed → OnBackPressedCallback
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (recordAdapter.isInSelectionMode()) {
                    exitSelectionMode()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // 从 ViewModel 恢复搜索关键词
        val savedKeyword = viewModel.searchKeyword.value ?: ""
        if (savedKeyword.isNotEmpty()) {
            etSearch.setText(savedKeyword)
        }
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

    private fun enterSelectionMode() {
        recordAdapter.setSelectionMode(true)
        viewModel.isInSelectionMode.value = true
        batchBar.visibility = View.VISIBLE
        tvSelectCount.text = "已选 0 条"
    }

    private fun exitSelectionMode() {
        recordAdapter.setSelectionMode(false)
        viewModel.isInSelectionMode.value = false
        batchBar.visibility = View.GONE
    }

    private fun batchDelete() {
        val selectedIds = recordAdapter.getSelectedIds()
        historyHelper.showBatchDeleteDialog(selectedIds) { ids ->
            lifecycleScope.launch {
                historyHelper.batchDelete(ids) {
                    exitSelectionMode()
                    loadAllRecords()
                }
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                viewModel.searchKeyword.value = keyword
                updateUI(historyHelper.filterRecords(allRecords, keyword))
                ivClearSearch.visibility = if (keyword.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        ivClearSearch.setOnClickListener { etSearch.text.clear() }
    }

    private fun loadAllRecords() {
        lifecycleScope.launch {
            allRecords = withContext(Dispatchers.IO) { database.recordDao().getAllRecords() }
            withContext(Dispatchers.Main) {
                val keyword = viewModel.searchKeyword.value ?: ""
                updateUI(historyHelper.filterRecords(allRecords, keyword))
            }
        }
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
        if (HistoryViewModel.needsRefresh) {
            HistoryViewModel.needsRefresh = false
            loadAllRecords()
        }
    }
}
