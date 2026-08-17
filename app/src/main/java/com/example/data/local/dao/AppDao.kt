package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.KazaRecordEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.QuranProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerRecordDao {
    @Query("SELECT * FROM prayer_records WHERE date = :date LIMIT 1")
    fun getRecordByDate(date: String): Flow<PrayerRecordEntity?>

    @Query("SELECT * FROM prayer_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDateDirect(date: String): PrayerRecordEntity?

    @Query("SELECT * FROM prayer_records ORDER BY date DESC LIMIT 30")
    fun getRecentRecords(): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayer_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<PrayerRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PrayerRecordEntity)
}

@Dao
interface QuranProgressDao {
    @Query("SELECT * FROM quran_progress ORDER BY id DESC LIMIT 1")
    fun getLatestProgress(): Flow<QuranProgressEntity?>

    @Query("SELECT * FROM quran_progress ORDER BY date DESC LIMIT 30")
    fun getReadingHistory(): Flow<List<QuranProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: QuranProgressEntity)
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikrs ORDER BY id ASC")
    fun getAllDhikrs(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikrs WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: Long): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikr(dhikr: DhikrEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dhikrs: List<DhikrEntity>)

    @Update
    suspend fun updateDhikr(dhikr: DhikrEntity)

    @Delete
    suspend fun deleteDhikr(dhikr: DhikrEntity)
}

@Dao
interface KazaRecordDao {
    @Query("SELECT * FROM kaza_records WHERE id = 1 LIMIT 1")
    fun getKazaRecord(): Flow<KazaRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(kazaRecord: KazaRecordEntity)
}
