package com.microbe.recorder.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "microbe_records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 种植日期
    val plantingDate: String = "",

    // 处理组
    val treatmentGroup: String = "",

    // 观察记录
    val observationResult: String = "",

    // 备注/调整
    val notes: String = "",

    // 照片路径（最多5张，逗号分隔）
    val photoPaths: String = "",

    // 录音文件路径
    val audioPath: String = "",

    // 记录日期（创建时间）
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)
