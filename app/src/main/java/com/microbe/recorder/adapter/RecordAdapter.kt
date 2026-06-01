package com.microbe.recorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.microbe.recorder.R
import com.microbe.recorder.database.RecordEntity
import com.microbe.recorder.util.FileHelper

class RecordAdapter(
    private var records: List<RecordEntity> = emptyList(),
    private val onItemClick: (RecordEntity) -> Unit
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvExperimentNumber: TextView = itemView.findViewById(R.id.tvExperimentNumber)
        val tvSampleName: TextView = itemView.findViewById(R.id.tvSampleName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvObservationPreview: TextView = itemView.findViewById(R.id.tvObservationPreview)
        val chipCultureTime: Chip = itemView.findViewById(R.id.chipCultureTime)
        val ivPhotoIcon: ImageView = itemView.findViewById(R.id.ivPhotoIcon)
        val ivAudioIcon: ImageView = itemView.findViewById(R.id.ivAudioIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        holder.tvExperimentNumber.text = record.experimentNumber
        holder.tvSampleName.text = record.sampleName
        holder.tvDate.text = FileHelper.formatDate(record.createdAt)
        holder.tvObservationPreview.text = record.observationResult

        // 培养时间标签
        if (record.cultureTime.isNotEmpty()) {
            holder.chipCultureTime.visibility = View.VISIBLE
            holder.chipCultureTime.text = record.cultureTime
        } else {
            holder.chipCultureTime.visibility = View.GONE
        }

        // 照片图标
        if (record.photoPaths.isNotEmpty()) {
            holder.ivPhotoIcon.visibility = View.VISIBLE
        } else {
            holder.ivPhotoIcon.visibility = View.GONE
        }

        // 录音图标
        if (record.audioPath.isNotEmpty()) {
            holder.ivAudioIcon.visibility = View.VISIBLE
        } else {
            holder.ivAudioIcon.visibility = View.GONE
        }

        // 点击事件
        holder.itemView.setOnClickListener {
            onItemClick(record)
        }
    }

    override fun getItemCount(): Int = records.size

    /**
     * 更新数据
     */
    fun updateRecords(newRecords: List<RecordEntity>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
