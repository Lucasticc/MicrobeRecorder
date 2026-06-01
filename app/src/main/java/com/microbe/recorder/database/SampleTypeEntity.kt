package com.microbe.recorder.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample_types")
data class SampleTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,           // 样品名称
    val category: String = "",  // 分类（可选）
    val createdAt: Long = System.currentTimeMillis()
)
