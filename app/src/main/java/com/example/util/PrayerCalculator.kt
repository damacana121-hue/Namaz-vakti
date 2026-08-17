package com.example.util

import com.example.model.PrayerType
import java.util.Calendar
import kotlin.math.*

data class SinglePrayerTime(
    val type: PrayerType,
    val timeFormatted: String,
    val hour: Int,
    val minute: Int,
    val calendarTime: Calendar
)

data class DailyPrayerSchedule(
    val dateString: String,
    val hijriDateString: String,
    val fajr: SinglePrayerTime,
    val sunrise: SinglePrayerTime,
    val dhuhr: SinglePrayerTime,
    val asr: SinglePrayerTime,
    val maghrib: SinglePrayerTime,
    val isha: SinglePrayerTime,
    val currentActivePrayer: PrayerType,
    val nextPrayer: SinglePrayerTime,
    val secondsRemainingToNext: Long,
    val progressPercentToNext: Float
)

/**
 * Offline prayer-time calculator following Diyanet's published calculation
 * conventions. No network request is required.
 *
 * Diyanet states that its calendars use the first appearance of true dawn for
 * imsak, no temkin for imsak/yatsı, 7 minutes at sunrise/sunset, 4 minutes at
 * ikindi and 5 minutes after solar noon for öğle. See Diyanet's official
 * "İbadet Vakitlerinde Temkin" explanation.
 */
object PrayerCalculator {
    private const val FAJR_ANGLE = 18.0
    private const val ISHA_ANGLE = 17.0
    private const val SUN_ALTITUDE = -0.833
    private const val ASR_SHADOW_FACTOR = 1.0

    // Diyanet temkin rules, in minutes.
    private const val SUNRISE_TEMKIN_MIN = -7.0
    private const val MAGHRIB_TEMKIN_MIN = 7.0
    private const val DHUHR_TEMKIN_MIN = 5.0
    private const val ASR_TEMKIN_MIN = 4.0

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        timeZone: Double = 3.0,
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // NOAA-style solar equations are stable, fast and fully offline.
        val gamma = 2.0 * PI / 365.0 * (dayOfYear - 1)
        val equationOfTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2.0 * gamma) -
                0.040849 * sin(2.0 * gamma)
        )
        val declination =
            0.006918 -
                0.399912 * cos(gamma) +
                0.070257 * sin(gamma) -
                0.006758 * cos(2.0 * gamma) +
                0.000907 * sin(2.0 * gamma) -
                0.002697 * cos(3.0 * gamma) +
                0.00148 * sin(3.0 * gamma)

        // Solar noon in local civil time.
        val solarNoonMinutes = 720.0 - 4.0 * longitude - equationOfTime + timeZone * 60.0

        val fajrRaw = solarNoonMinutes - solarTimeDifferenceMinutes(latitude, declination, -FAJR_ANGLE)
        val sunriseRaw = solarNoonMinutes - solarTimeDifferenceMinutes(latitude, declination, SUN_ALTITUDE)
        val dhuhrRaw = solarNoonMinutes + DHUHR_TEMKIN_MIN

        // Asr shadow factor 1 (one mithl). The altitude is POSITIVE here;
        // using a negative altitude was the source of the old 00:55-style bug.
        val solarAltitudeAtAsr = Math.toDegrees(
            atan(
                1.0 / (
                    ASR_SHADOW_FACTOR +
                        tan(abs(Math.toRadians(latitude) - declination))
                )
            )
        )
        val asrRaw = solarNoonMinutes +
            solarTimeDifferenceMinutes(latitude, declination, solarAltitudeAtAsr) +
            ASR_TEMKIN_MIN

        val maghribRaw = solarNoonMinutes +
            solarTimeDifferenceMinutes(latitude, declination, SUN_ALTITUDE) +
            MAGHRIB_TEMKIN_MIN
        val ishaRaw = solarNoonMinutes +
            solarTimeDifferenceMinutes(latitude, declination, -ISHA_ANGLE)

        val fajrTime = createPrayerTime(calendar, PrayerType.FAJR, fajrRaw)
        val sunriseTime = createPrayerTime(calendar, PrayerType.SUNRISE, sunriseRaw + SUNRISE_TEMKIN_MIN)
        val dhuhrTime = createPrayerTime(calendar, PrayerType.DHUHR, dhuhrRaw)
        val asrTime = createPrayerTime(calendar, PrayerType.ASR, asrRaw)
        val maghribTime = createPrayerTime(calendar, PrayerType.MAGHRIB, maghribRaw)
        val ishaTime = createPrayerTime(calendar, PrayerType.ISHA, ishaRaw)

        val prayers = listOf(fajrTime, sunriseTime, dhuhrTime, asrTime, maghribTime, ishaTime)
        val nowMillis = System.currentTimeMillis()
        var nextPrayer = fajrTime
        var currentActive = PrayerType.ISHA
        var foundNext = false
        var previousPrayerMillis = ishaTime.calendarTime.timeInMillis - 86_400_000L

        // For today's schedule, use the phone's real clock. For another selected
        // date, show that date's first upcoming prayer rather than comparing it
        // with today's clock.
        val isToday = calendar.get(Calendar.ERA) == Calendar.getInstance().get(Calendar.ERA) &&
            calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            for (i in prayers.indices) {
                val p = prayers[i]
                if (p.calendarTime.timeInMillis > nowMillis) {
                    nextPrayer = p
                    currentActive = if (i == 0) PrayerType.ISHA else prayers[i - 1].type
                    previousPrayerMillis = if (i == 0) {
                        ishaTime.calendarTime.timeInMillis - 86_400_000L
                    } else prayers[i - 1].calendarTime.timeInMillis
                    foundNext = true
                    break
                }
            }
        } else {
            nextPrayer = prayers.first()
            currentActive = PrayerType.ISHA
            previousPrayerMillis = (fajrTime.calendarTime.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.timeInMillis
            foundNext = true
        }

        if (!foundNext) {
            val tomorrow = (fajrTime.calendarTime.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            nextPrayer = SinglePrayerTime(
                PrayerType.FAJR,
                fajrTime.timeFormatted,
                fajrTime.hour,
                fajrTime.minute,
                tomorrow
            )
            currentActive = PrayerType.ISHA
            previousPrayerMillis = ishaTime.calendarTime.timeInMillis
        }

        val remainingSec = if (isToday) {
            max(0L, (nextPrayer.calendarTime.timeInMillis - nowMillis) / 1000L)
        } else {
            max(0L, (nextPrayer.calendarTime.timeInMillis - calendar.timeInMillis) / 1000L)
        }
        val totalIntervalSec = max(
            1L,
            (nextPrayer.calendarTime.timeInMillis - previousPrayerMillis) / 1000L
        )
        val elapsedSec = (totalIntervalSec - remainingSec).coerceIn(0L, totalIntervalSec)
        val progress = (elapsedSec.toFloat() / totalIntervalSec.toFloat()).coerceIn(0f, 1f)

        val monthsTr = arrayOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        val daysTr = arrayOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")
        val dateFormatted = "$day ${monthsTr[month - 1]} $year, ${daysTr[calendar.get(Calendar.DAY_OF_WEEK) - 1]}"

        return DailyPrayerSchedule(
            dateString = dateFormatted,
            hijriDateString = HijriCalendarUtil.getHijriDate(calendar),
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            currentActivePrayer = currentActive,
            nextPrayer = nextPrayer,
            secondsRemainingToNext = remainingSec,
            progressPercentToNext = progress
        )
    }

    private fun solarTimeDifferenceMinutes(lat: Double, dec: Double, altitude: Double): Double {
        val latRad = Math.toRadians(lat)
        val cosH = (
            sin(Math.toRadians(altitude)) -
                sin(latRad) * sin(dec)
            ) / (cos(latRad) * cos(dec))
        return when {
            cosH <= -1.0 -> 720.0
            cosH >= 1.0 -> 0.0
            else -> Math.toDegrees(acos(cosH)) * 4.0
        }
    }

    private fun createPrayerTime(
        baseCal: Calendar,
        type: PrayerType,
        totalMinutesFromMidnight: Double
    ): SinglePrayerTime {
        val totalMinutes = Math.round(totalMinutesFromMidnight).toInt()
        val normalizedMin = ((totalMinutes % 1440) + 1440) % 1440
        val h = normalizedMin / 60
        val m = normalizedMin % 60
        val cal = (baseCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return SinglePrayerTime(type, String.format("%02d:%02d", h, m), h, m, cal)
    }
}
