package com.microbe.recorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microbe.recorder.R
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.util.FileHelper

class RecordAdapter(
    private var records: List<RecordEntity> = emptyList(),
    private val onItemClick: (RecordEntity) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    private var selectionMode = false
    private val selectedIds = mutableSetOf<Long>()
    private var onSelectionChanged: ((Int) -> Unit)? = null

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMainTitle: TextView = itemView.findViewById(R.id.tvExperimentNumber) // 复用ID
        val tvSubInfo: TextView = itemView.findViewById(R.id.tvSampleName) // 复用ID
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvObservationPreview: TextView = itemView.findViewById(R.id.tvObservationPreview)
        val chipCultureTime: TextView = itemView.findViewById(R.id.chipCultureTime)
        val ivPhotoIcon: TextView = itemView.findViewById(R.id.ivPhotoIcon)
        val ivAudioIcon: TextView = itemView.findViewById(R.id.ivAudioIcon)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        // 主标题：处理组
        holder.tvMainTitle.text = record.treatmentGroup
        // 副信息：种植日期
        holder.tvSubInfo.text = "种植: ${record.plantingDate}"
        holder.tvDate.text = FileHelper.formatDate(record.createdAt)
        holder.tvObservationPreview.text = record.observationResult

        // 培养时间标签 → 显示照片数
        val photoCount = if (record.photoPaths.isNotEmpty()) record.photoPaths.split(",").size else 0
        if (photoCount > 0) {
            holder.chipCultureTime.visibility = View.VISIBLE
            holder.chipCultureTime.text = "📷 $photoCount"
        } else {
            holder.chipCultureTime.visibility = View.GONE
        }

        holder.ivPhotoIcon.visibility = if (record.photoPaths.isNotEmpty()) View.VISIBLE else View.GONE
        holder.ivAudioIcon.visibility = if (record.audioPath.isNotEmpty()) View.VISIBLE else View.GONE

        // 选择模式
        if (selectionMode) {
            holder.cbSelect.visibility = View.VISIBLE
            holder.cbSelect.isChecked = selectedIds.contains(record.id)
            holder.cbSelect.setOnClickListener { toggleSelection(record.id) }
            holder.itemView.setOnClickListener { toggleSelection(record.id) }
        } else {
            holder.cbSelect.visibility = View.GONE
            holder.itemView.setOnClickListener { onItemClick(record) }
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<RecordEntity>) { records = newRecords; notifyDataSetChanged() }

    fun setSelectionMode(enabled: Boolean) {
        selectionMode = enabled; selectedIds.clear(); notifyDataSetChanged()
    }

    fun isInSelectionMode(): Boolean = selectionMode

    fun toggleSelection(id: Long) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedIds.size)
    }

    fun selectAll() {
        selectedIds.clear()
        records.forEach { selectedIds.add(it.id) }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedIds.size)
    }

    fun getSelectedIds(): Set<Long> = selectedIds.toSet()

    fun setOnSelectionChangedListener(listener: (Int) -> Unit) { onSelectionChanged = listener }
}
