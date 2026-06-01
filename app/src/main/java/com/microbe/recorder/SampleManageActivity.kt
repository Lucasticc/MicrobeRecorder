package com.microbe.recorder

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.microbe.recorder.adapter.SampleTypeAdapter
import com.microbe.recorder.database.AppDatabase
import com.microbe.recorder.database.SampleTypeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SampleManageActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: SampleTypeAdapter
    private lateinit var rvSamples: RecyclerView
    private lateinit var emptyState: View
    private lateinit var tvCount: TextView
    private lateinit var etNewSample: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_manage)

        database = AppDatabase.getDatabase(this)

        // Toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Views
        etNewSample = findViewById(R.id.etNewSample)
        rvSamples = findViewById(R.id.rvSamples)
        emptyState = findViewById(R.id.emptyState)
        tvCount = findViewById(R.id.tvCount)

        // RecyclerView
        adapter = SampleTypeAdapter(
            onDelete = { sample -> showDeleteDialog(sample) }
        )
        rvSamples.layoutManager = LinearLayoutManager(this)
        rvSamples.adapter = adapter

        // 添加按钮
        findViewById<View>(R.id.btnAdd).setOnClickListener {
            addSample()
        }

        loadData()
    }

    private fun addSample() {
        val name = etNewSample.text?.toString()?.trim() ?: ""
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入样品名称", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existing = withContext(Dispatchers.IO) {
                database.sampleTypeDao().getAllSampleNames()
            }
            if (existing.contains(name)) {
                Toast.makeText(this@SampleManageActivity, "该样品已存在", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                database.sampleTypeDao().insert(SampleTypeEntity(name = name))
            }
            etNewSample.text?.clear()
            Toast.makeText(this@SampleManageActivity, "已添加: $name", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private fun showDeleteDialog(sample: SampleTypeEntity) {
        AlertDialog.Builder(this)
            .setTitle("删除样品")
            .setMessage("确定要删除「${sample.name}」吗？")
            .setPositiveButton("删除") { _, _ -> deleteSample(sample) }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteSample(sample: SampleTypeEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.sampleTypeDao().delete(sample)
            }
            Toast.makeText(this@SampleManageActivity, "已删除", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val samples = withContext(Dispatchers.IO) {
                database.sampleTypeDao().getAllSampleTypes()
            }
            withContext(Dispatchers.Main) {
                adapter.updateData(samples)
                tvCount.text = "共 ${samples.size} 种样品"
                emptyState.visibility = if (samples.isEmpty()) View.VISIBLE else View.GONE
                rvSamples.visibility = if (samples.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }
}
