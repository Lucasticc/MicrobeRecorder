package com.microbe.recorder.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "microbe_records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 实验编号
    val experimentNumber: String,

    // 样品名称
    val sampleName: String,

    // 培养时间
    val cultureTime: String,

    // 观察结果
    val observationResult: String,

    // 备注
    val notes: String = "",

    // 实验描述（语音转文字）
    val description: String = "",

    // 照片路径（多个用逗号分隔）
    val photoPaths: String = "",

    // 录音文件路径
    val audioPath: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)
