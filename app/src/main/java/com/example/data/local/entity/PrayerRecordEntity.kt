package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_records")
data class PrayerRecordEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val fajrDone: Boolean = false,
    val dhuhrDone: Boolean = false,
    val asrDone: Boolean = false,
    val maghribDone: Boolean = false,
    val ishaDone: Boolean = false,
    val fajrJamaat: Boolean = false,
    val dhuhrJamaat: Boolean = false,
    val asrJamaat: Boolean = false,
    val maghribJamaat: Boolean = false,
    val ishaJamaat: Boolean = false,
    val notes: String = ""
) {
    fun completedCount(): Int {
        var count = 0
        if (fajrDone) count++
        if (dhuhrDone) count++
        if (asrDone) count++
        if (maghribDone) count++
        if (ishaDone) count++
        return count
    }
}
