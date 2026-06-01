package com.microbe.recorder.model

/**
 * 微生物实验记录数据模型
 */
data class MicrobeRecord(
    val id: Long = 0,
    val experimentNumber: String,      // 实验编号
    val sampleName: String,            // 样品名称
    val cultureTime: String,           // 培养时间
    val observationResult: String,     // 观察结果
    val notes: String = "",            // 备注
    val description: String = "",      // 实验描述（语音转文字）
    val photoPaths: List<String> = emptyList(),  // 照片路径列表
    val audioPath: String = "",        // 录音文件路径
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val updatedAt: Long = System.currentTimeMillis()   // 更新时间
) {
    /**
     * 获取照片路径字符串（用于数据库存储）
     */
    fun getPhotoPathsString(): String {
        return photoPaths.joinToString(",")
    }

    /**
     * 从字符串解析照片路径列表
     */
    companion object {
        fun parsePhotoPaths(photoPathsString: String): List<String> {
            return if (photoPathsString.isEmpty()) {
                emptyList()
            } else {
                photoPathsString.split(",").filter { it.isNotEmpty() }
            }
        }
    }
}
