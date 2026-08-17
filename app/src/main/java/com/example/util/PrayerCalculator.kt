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
 * Offline astronomical prayer-time calculation.
 *
 * Important: the previous implementation used the wrong Asr formula: it passed
 * an angle into the generic solar-angle routine although Asr is defined from
 * the shadow-length condition. That could produce nonsensical values such as
 * 00:55. Asr is now calculated directly from the shadow factor.
 */
object PrayerCalculator {
    private const val FAJR_ANGLE = 18.0
    private const val ISHA_ANGLE = 17.0
    private const val ASR_SHADOW_FACTOR = 1.0
    private const val SUN_ALTITUDE = -0.833

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        timeZone: Double = 3.0,
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val julianDate = getJulianDate(year, month, day)
        val d = julianDate - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val rightAscension = Math.toDegrees(
            atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))
        ) / 15.0
        val fixedRA = fixHour(rightAscension)
        val eqOfTime = q / 15.0 - fixedRA
        val noon = fixHour(12.0 + timeZone - longitude / 15.0 - eqOfTime)

        val fajrHour = noon - solarHourAngle(latitude, declination, -FAJR_ANGLE)
        val sunriseHour = noon - solarHourAngle(latitude, declination, SUN_ALTITUDE)
        val dhuhrHour = noon

        // Correct Asr calculation for shadow factor 1 (standard/Diyanet-style
        // calculation): altitude = -atan(1 / (factor + tan(|lat-dec|))).
        val solarAltitudeAtNoon = -Math.toDegrees(
            atan(1.0 / (ASR_SHADOW_FACTOR + tan(Math.toRadians(abs(latitude - declination)))))
        )
        val asrHour = noon + solarHourAngle(latitude, declination, solarAltitudeAtNoon)

        val maghribHour = noon + solarHourAngle(latitude, declination, SUN_ALTITUDE)
        val ishaHour = noon + solarHourAngle(latitude, declination, -ISHA_ANGLE)

        val fajrTime = createPrayerTime(calendar, PrayerType.FAJR, fajrHour)
        val sunriseTime = createPrayerTime(calendar, PrayerType.SUNRISE, sunriseHour)
        val dhuhrTime = createPrayerTime(calendar, PrayerType.DHUHR, dhuhrHour)
        val asrTime = createPrayerTime(calendar, PrayerType.ASR, asrHour)
        val maghribTime = createPrayerTime(calendar, PrayerType.MAGHRIB, maghribHour)
        val ishaTime = createPrayerTime(calendar, PrayerType.ISHA, ishaHour)

        val prayers = listOf(fajrTime, sunriseTime, dhuhrTime, asrTime, maghribTime, ishaTime)
        val nowMillis = System.currentTimeMillis()
        var nextPrayer = fajrTime
        var currentActive = PrayerType.ISHA
        var foundNext = false
        var previousPrayerMillis = ishaTime.calendarTime.timeInMillis - 86_400_000L

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

        val totalIntervalSec = max(1L, (nextPrayer.calendarTime.timeInMillis - previousPrayerMillis) / 1000L)
        val remainingSec = max(0L, (nextPrayer.calendarTime.timeInMillis - nowMillis) / 1000L)
        val elapsedSec = (totalIntervalSec - remainingSec).coerceIn(0L, totalIntervalSec)
        val progress = (elapsedSec.toFloat() / totalIntervalSec.toFloat()).coerceIn(0f, 1f)

        val monthsTr = arrayOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
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

    private fun solarHourAngle(lat: Double, dec: Double, altitude: Double): Double {
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val altRad = Math.toRadians(altitude)
        val denominator = cos(latRad) * cos(decRad)
        val cosH = ((sin(altRad) - sin(latRad) * sin(decRad)) / denominator)
        return when {
            cosH <= -1.0 -> 12.0
            cosH >= 1.0 -> 0.0
            else -> Math.toDegrees(acos(cosH)) / 15.0
        }
    }

    private fun createPrayerTime(baseCal: Calendar, type: PrayerType, fractionalHour: Double): SinglePrayerTime {
        val totalMinutes = Math.round(fractionalHour * 60.0).toInt()
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

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixHour(a: Double): Double {
        var res = a - 24.0 * floor(a / 24.0)
        if (res < 0) res += 24.0
        return res
    }

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }
}
