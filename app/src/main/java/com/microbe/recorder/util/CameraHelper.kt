package com.microbe.recorder.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {

    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null

    /**
     * 创建临时照片文件并返回 Uri
     */
    fun createImageFile(context: Context): Pair<Uri, File> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "MICROBE_${timeStamp}"

        val storageDir = File(context.getExternalFilesDir(null), "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        currentPhotoUri = imageUri
        currentPhotoFile = imageFile

        return Pair(imageUri, imageFile)
    }

    /**
     * 获取当前照片 Uri
     */
    fun getCurrentPhotoUri(): Uri? = currentPhotoUri

    /**
     * 获取当前照片文件路径
     */
    fun getCurrentPhotoPath(): String? = currentPhotoFile?.absolutePath

    /**
     * 清除当前照片引用
     */
    fun clearCurrentPhoto() {
        currentPhotoUri = null
        currentPhotoFile = null
    }

    /**
     * 获取所有保存的照片文件
     */
    fun getImageFiles(context: Context): List<File> {
        val storageDir = File(context.getExternalFilesDir(null), "images")
        return if (storageDir.exists()) {
            storageDir.listFiles()?.filter { it.isFile && it.extension == "jpg" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 删除照片文件
     */
    fun deleteImageFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
