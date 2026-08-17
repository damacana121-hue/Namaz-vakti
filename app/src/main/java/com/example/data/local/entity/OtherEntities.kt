package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran_progress")
data class QuranProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "YYYY-MM-DD"
    val lastPageRead: Int = 1,
    val lastJuz: Int = 1,
    val lastSurah: String = "Fâtiha",
    val pagesReadToday: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "dhikrs")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val arabicText: String,
    val turkishMeaning: String,
    val targetCount: Int = 33,
    val currentCount: Int = 0,
    val totalCyclesCompleted: Int = 0,
    val category: String = "Genel"
)

@Entity(tableName = "kaza_records")
data class KazaRecordEntity(
    @PrimaryKey val id: Int = 1,
    val fajrKaza: Int = 0,
    val dhuhrKaza: Int = 0,
    val asrKaza: Int = 0,
    val maghribKaza: Int = 0,
    val ishaKaza: Int = 0,
    val witrKaza: Int = 0,
    val fastingKaza: Int = 0
) {
    val totalPrayers: Int
        get() = fajrKaza + dhuhrKaza + asrKaza + maghribKaza + ishaKaza + witrKaza
}
