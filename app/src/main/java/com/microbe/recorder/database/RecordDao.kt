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

    @Query("SELECT * FROM microbe_records ORDER BY createdAt DESC, id DESC")
    suspend fun getAllRecords(): List<RecordEntity>

    @Query("SELECT * FROM microbe_records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Query("DELETE FROM microbe_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM microbe_records WHERE plantingDate = :plantingDate ORDER BY createdAt DESC, id DESC")
    suspend fun getRecordsByPlantingDate(plantingDate: String): List<RecordEntity>

    @Query("SELECT * FROM microbe_records WHERE treatmentGroup = :group ORDER BY createdAt DESC, id DESC")
    suspend fun getRecordsByTreatmentGroup(group: String): List<RecordEntity>

    @Query("SELECT * FROM microbe_records WHERE plantingDate = :plantingDate AND treatmentGroup = :group ORDER BY createdAt DESC, id DESC")
    suspend fun getRecordsByPlantingAndGroup(plantingDate: String, group: String): List<RecordEntity>

    @Query("SELECT DISTINCT plantingDate FROM microbe_records ORDER BY plantingDate DESC")
    suspend fun getAllPlantingDates(): List<String>

    @Query("SELECT DISTINCT treatmentGroup FROM microbe_records ORDER BY treatmentGroup ASC")
    suspend fun getAllTreatmentGroups(): List<String>

    @Query("SELECT * FROM microbe_records WHERE plantingDate = :plantingDate ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByPlantingDate(plantingDate: String): RecordEntity?

    @Query("SELECT * FROM microbe_records WHERE treatmentGroup = :group AND createdAt < :beforeTime ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByTreatmentGroupBefore(group: String, beforeTime: Long): RecordEntity?

    @Query("SELECT * FROM microbe_records WHERE plantingDate = :plantingDate AND treatmentGroup = :group AND createdAt < :beforeTime AND id != :excludeId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByPlantingAndGroupBefore(plantingDate: String, group: String, beforeTime: Long, excludeId: Long = -1): RecordEntity?

    @Query("UPDATE microbe_records SET treatmentGroup = :newName WHERE treatmentGroup = :oldName AND plantingDate = :plantingDate")
    suspend fun updateTreatmentGroup(oldName: String, newName: String, plantingDate: String)
}
