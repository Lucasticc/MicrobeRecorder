package com.microbe.recorder.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 相机相关辅助工具。
 *
 * 注意：createImageFile / getCurrentPhotoPath / clearCurrentPhoto 已废弃，
 * CameraActivity 直接管理自己的文件路径并通过 Intent extra 返回。
 */
object CameraHelper {

    /**
     * 生成照片文件夹名: 种植日期_处理组_记录日期
     */
    fun generateFolderName(plantingDate: String, treatmentGroup: String): String {
        val recordDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "${plantingDate}_${treatmentGroup}_${recordDate}"
    }
}
