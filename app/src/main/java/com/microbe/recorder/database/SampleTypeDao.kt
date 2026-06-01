package com.microbe.recorder.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SampleTypeDao {

    @Query("SELECT * FROM sample_types ORDER BY name ASC")
    suspend fun getAllSampleTypes(): List<SampleTypeEntity>

    @Query("SELECT name FROM sample_types ORDER BY name ASC")
    suspend fun getAllSampleNames(): List<String>

    @Insert
    suspend fun insert(sampleType: SampleTypeEntity)

    @Delete
    suspend fun delete(sampleType: SampleTypeEntity)

    @Query("DELETE FROM sample_types WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM sample_types")
    suspend fun getCount(): Int
}
