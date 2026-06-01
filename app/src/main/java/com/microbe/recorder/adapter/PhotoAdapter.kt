package com.microbe.recorder.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.microbe.recorder.R
import java.io.File

class PhotoAdapter(
    private var photos: MutableList<String> = mutableListOf(),
    private val onDeleteClick: ((Int) -> Unit)? = null,
    private val isEditable: Boolean = true
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        val ivDelete: View = itemView.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoPath = photos[position]

        // 加载照片
        Glide.with(holder.itemView.context)
            .load(File(photoPath))
            .centerCrop()
            .into(holder.ivPhoto)

        // 删除按钮
        if (isEditable && onDeleteClick != null) {
            holder.ivDelete.visibility = View.VISIBLE
            holder.ivDelete.setOnClickListener {
                onDeleteClick.invoke(position)
            }
        } else {
            holder.ivDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = photos.size

    /**
     * 添加照片
     */
    fun addPhoto(photoPath: String) {
        photos.add(photoPath)
        notifyItemInserted(photos.size - 1)
    }

    /**
     * 删除照片
     */
    fun removePhoto(position: Int) {
        if (position in 0 until photos.size) {
            photos.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, photos.size)
        }
    }

    /**
     * 获取所有照片路径
     */
    fun getPhotoPaths(): List<String> = photos.toList()

    /**
     * 更新照片列表
     */
    fun updatePhotos(newPhotos: List<String>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }
}
