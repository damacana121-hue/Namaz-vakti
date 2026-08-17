package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.KazaRecordEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.QuranProgressEntity
import com.example.model.PrayerType
import com.example.util.DailyPrayerSchedule
import com.example.util.PrayerCalculator
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeeklyPrayerAnalytics(
    val dayLabels: List<String>,
    val dailyCompletedCounts: List<Int>, // 0 to 5 for each of past 7 days
    val totalPossiblePrayers: Int = 35,
    val totalCompletedPrayers: Int,
    val weeklySuccessRatePercent: Int,
    val currentStreakDays: Int,
    val mostConsistentPrayer: String,
    val jamaatCount: Int
)

class PrayerRepository(private val database: AppDatabase) {

    private val prayerDao = database.prayerRecordDao()
    private val quranDao = database.quranProgressDao()
    private val dhikrDao = database.dhikrDao()
    private val kazaDao = database.kazaRecordDao()

    val allPrayers: Flow<List<PrayerRecordEntity>> = prayerDao.getRecentRecords()
    val quranHistory: Flow<List<QuranProgressEntity>> = quranDao.getReadingHistory()
    val latestQuranProgress: Flow<QuranProgressEntity?> = quranDao.getLatestProgress()
    val allDhikrs: Flow<List<DhikrEntity>> = dhikrDao.getAllDhikrs()
    val kazaRecord: Flow<KazaRecordEntity?> = kazaDao.getKazaRecord()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getTodayPrayerRecord(dateStr: String = getTodayDateString()): Flow<PrayerRecordEntity?> {
        return prayerDao.getRecordByDate(dateStr)
    }

    suspend fun togglePrayerDone(prayerType: PrayerType, dateStr: String = getTodayDateString()) {
        val current = prayerDao.getRecordByDateDirect(dateStr) ?: PrayerRecordEntity(date = dateStr)
        val updated = when (prayerType) {
            PrayerType.FAJR -> current.copy(fajrDone = !current.fajrDone)
            PrayerType.SUNRISE -> current // Sunrise is not a fard prayer
            PrayerType.DHUHR -> current.copy(dhuhrDone = !current.dhuhrDone)
            PrayerType.ASR -> current.copy(asrDone = !current.asrDone)
            PrayerType.MAGHRIB -> current.copy(maghribDone = !current.maghribDone)
            PrayerType.ISHA -> current.copy(ishaDone = !current.ishaDone)
        }
        prayerDao.insertOrUpdate(updated)
    }

    suspend fun toggleJamaat(prayerType: PrayerType, dateStr: String = getTodayDateString()) {
        val current = prayerDao.getRecordByDateDirect(dateStr) ?: PrayerRecordEntity(date = dateStr)
        val updated = when (prayerType) {
            PrayerType.FAJR -> current.copy(fajrJamaat = !current.fajrJamaat)
            PrayerType.SUNRISE -> current
            PrayerType.DHUHR -> current.copy(dhuhrJamaat = !current.dhuhrJamaat)
            PrayerType.ASR -> current.copy(asrJamaat = !current.asrJamaat)
            PrayerType.MAGHRIB -> current.copy(maghribJamaat = !current.maghribJamaat)
            PrayerType.ISHA -> current.copy(ishaJamaat = !current.ishaJamaat)
        }
        prayerDao.insertOrUpdate(updated)
    }

    // Quran operations
    suspend fun saveQuranProgress(page: Int, juz: Int, surah: String, pagesReadToday: Int) {
        val dateStr = getTodayDateString()
        quranDao.insertProgress(
            QuranProgressEntity(
                date = dateStr,
                lastPageRead = page.coerceIn(1, 604),
                lastJuz = juz.coerceIn(1, 30),
                lastSurah = surah,
                pagesReadToday = pagesReadToday
            )
        )
    }

    // Dhikr operations
    suspend fun incrementDhikr(dhikr: DhikrEntity) {
        val newCount = dhikr.currentCount + 1
        if (newCount >= dhikr.targetCount) {
            dhikrDao.updateDhikr(
                dhikr.copy(
                    currentCount = 0,
                    totalCyclesCompleted = dhikr.totalCyclesCompleted + 1
                )
            )
        } else {
            dhikrDao.updateDhikr(dhikr.copy(currentCount = newCount))
        }
    }

    suspend fun resetDhikr(dhikr: DhikrEntity) {
        dhikrDao.updateDhikr(dhikr.copy(currentCount = 0))
    }

    suspend fun addNewDhikr(title: String, arabic: String, meaning: String, target: Int, category: String) {
        dhikrDao.insertDhikr(
            DhikrEntity(
                title = title,
                arabicText = arabic,
                turkishMeaning = meaning,
                targetCount = target,
                category = category
            )
        )
    }

    // Kaza operations
    suspend fun updateKazaCount(
        fajrDelta: Int = 0,
        dhuhrDelta: Int = 0,
        asrDelta: Int = 0,
        maghribDelta: Int = 0,
        ishaDelta: Int = 0,
        witrDelta: Int = 0,
        fastingDelta: Int = 0,
        currentRecord: KazaRecordEntity?
    ) {
        val current = currentRecord ?: KazaRecordEntity(id = 1)
        val updated = current.copy(
            fajrKaza = (current.fajrKaza + fajrDelta).coerceAtLeast(0),
            dhuhrKaza = (current.dhuhrKaza + dhuhrDelta).coerceAtLeast(0),
            asrKaza = (current.asrKaza + asrDelta).coerceAtLeast(0),
            maghribKaza = (current.maghribKaza + maghribDelta).coerceAtLeast(0),
            ishaKaza = (current.ishaKaza + ishaDelta).coerceAtLeast(0),
            witrKaza = (current.witrKaza + witrDelta).coerceAtLeast(0),
            fastingKaza = (current.fastingKaza + fastingDelta).coerceAtLeast(0)
        )
        kazaDao.insertOrUpdate(updated)
    }

    fun computeWeeklyAnalytics(records: List<PrayerRecordEntity>): WeeklyPrayerAnalytics {
        val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        val dailyCounts = mutableListOf<Int>()
        val labels = mutableListOf<String>()
        var totalCompleted = 0
        var totalJamaat = 0
        var fajrCount = 0
        var dhuhrCount = 0
        var asrCount = 0
        var maghribCount = 0
        var ishaCount = 0

        // Build last 7 days from 6 days ago up to today
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(c.time)
            val dayOfWeek = c.get(Calendar.DAY_OF_WEEK) // 1 is Sunday, 2 is Monday
            val labelIndex = (dayOfWeek + 5) % 7 // 0=Monday
            labels.add(dayNames[labelIndex])

            val rec = records.find { it.date == dateStr }
            val count = rec?.completedCount() ?: 0
            dailyCounts.add(count)
            totalCompleted += count

            rec?.let {
                if (it.fajrDone) fajrCount++
                if (it.dhuhrDone) dhuhrCount++
                if (it.asrDone) asrCount++
                if (it.maghribDone) maghribCount++
                if (it.ishaDone) ishaCount++
                if (it.fajrJamaat) totalJamaat++
                if (it.dhuhrJamaat) totalJamaat++
                if (it.asrJamaat) totalJamaat++
                if (it.maghribJamaat) totalJamaat++
                if (it.ishaJamaat) totalJamaat++
            }
        }

        val successRate = if (totalCompleted > 0) ((totalCompleted.toFloat() / 35f) * 100).toInt().coerceIn(0, 100) else 0

        // Calculate current streak
        var streak = 0
        for (i in dailyCounts.indices.reversed()) {
            if (dailyCounts[i] >= 3) {
                streak++
            } else {
                break
            }
        }

        // Most consistent prayer
        val countsMap = mapOf(
            "Sabah" to fajrCount,
            "Öğle" to dhuhrCount,
            "İkindi" to asrCount,
            "Akşam" to maghribCount,
            "Yatsı" to ishaCount
        )
        val mostConsistent = countsMap.maxByOrNull { it.value }?.key ?: "Sabah"

        return WeeklyPrayerAnalytics(
            dayLabels = labels,
            dailyCompletedCounts = dailyCounts,
            totalCompletedPrayers = totalCompleted,
            weeklySuccessRatePercent = successRate,
            currentStreakDays = streak,
            mostConsistentPrayer = mostConsistent,
            jamaatCount = totalJamaat
        )
    }
}
