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
     * 创建照片文件，归档到指定子文件夹
     * @param subfolder 子文件夹名，如 "2024-01-15_A组_2024-01-20"
     */
    fun createImageFile(context: Context, subfolder: String = ""): Pair<Uri, File> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "CAMERA_${timeStamp}"

        val imagesRoot = File(context.getExternalFilesDir(null), "images")
        val storageDir = if (subfolder.isNotEmpty()) File(imagesRoot, subfolder) else imagesRoot
        if (!storageDir.exists()) storageDir.mkdirs()

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

    fun getCurrentPhotoUri(): Uri? = currentPhotoUri
    fun getCurrentPhotoPath(): String? = currentPhotoFile?.absolutePath
    fun clearCurrentPhoto() { currentPhotoUri = null; currentPhotoFile = null }

    /**
     * 生成照片文件夹名: 种植日期_处理组_记录日期
     */
    fun generateFolderName(plantingDate: String, treatmentGroup: String): String {
        val recordDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "${plantingDate}_${treatmentGroup}_${recordDate}"
    }
}
