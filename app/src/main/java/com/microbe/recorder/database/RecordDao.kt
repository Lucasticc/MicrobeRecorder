package com.microbe.recorder.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)

    @Query("SELECT * FROM microbe_records ORDER BY createdAt DESC")
    suspend fun getAllRecords(): List<RecordEntity>

    @Query("SELECT * FROM microbe_records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Query("SELECT * FROM microbe_records WHERE sampleName LIKE '%' || :keyword || '%' OR experimentNumber LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    suspend fun searchRecords(keyword: String): List<RecordEntity>

    @Query("DELETE FROM microbe_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM microbe_records")
    suspend fun getRecordCount(): Int
}
