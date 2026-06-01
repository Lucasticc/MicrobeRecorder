package com.microbe.recorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.microbe.recorder.R
import com.microbe.recorder.database.SampleTypeEntity

class SampleTypeAdapter(
    private var samples: List<SampleTypeEntity> = emptyList(),
    private val onDelete: (SampleTypeEntity) -> Unit
) : RecyclerView.Adapter<SampleTypeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSampleName: TextView = itemView.findViewById(R.id.tvSampleName)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sample_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sample = samples[position]
        holder.tvSampleName.text = sample.name
        holder.ivDelete.setOnClickListener { onDelete(sample) }
    }

    override fun getItemCount(): Int = samples.size

    fun updateData(newSamples: List<SampleTypeEntity>) {
        samples = newSamples
        notifyDataSetChanged()
    }
}
