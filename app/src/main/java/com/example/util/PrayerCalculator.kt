package com.example.util

import com.example.model.PrayerType
import java.util.Calendar
import java.util.Date
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

object PrayerCalculator {

    // Standard Diyanet Method: Fajr = 18°, Isha = 17°
    private const val FAJR_ANGLE = 18.0
    private const val ISHA_ANGLE = 17.0

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        timeZone: Double = 3.0,
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date
        val julianDate = getJulianDate(year, month, day)
        val d = julianDate - 2451545.0

        // Sun calculations
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val rightAscension = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val fixedRA = fixHour(rightAscension)
        val eqOfTime = (q / 15.0) - fixedRA

        // Solar noon (Dhuhr)
        val noon = fixHour(12.0 + timeZone - (longitude / 15.0) - eqOfTime)

        // Fajr (İmsak)
        val fajrDiff = getHourAngleForSunAngle(latitude, declination, -FAJR_ANGLE)
        val fajrHour = noon - fajrDiff

        // Sunrise (Güneş) - angle is -0.833°
        val sunriseDiff = getHourAngleForSunAngle(latitude, declination, -0.833)
        val sunriseHour = noon - sunriseDiff

        // Asr (İkindi) - Shadow factor 1.0 (Standard/Shafi/Diyanet)
        val asrAngle = -Math.toDegrees(atan(1.0 + tan(Math.toRadians(abs(latitude - declination)))))
        val asrDiff = getHourAngleForSunAngle(latitude, declination, asrAngle)
        val asrHour = noon + asrDiff

        // Maghrib (Akşam) - Sunset angle -0.833°
        val maghribHour = noon + sunriseDiff

        // Isha (Yatsı)
        val ishaDiff = getHourAngleForSunAngle(latitude, declination, -ISHA_ANGLE)
        val ishaHour = noon + ishaDiff

        // Create time objects
        val fajrTime = createPrayerTime(calendar, PrayerType.FAJR, fajrHour)
        val sunriseTime = createPrayerTime(calendar, PrayerType.SUNRISE, sunriseHour)
        val dhuhrTime = createPrayerTime(calendar, PrayerType.DHUHR, noon)
        val asrTime = createPrayerTime(calendar, PrayerType.ASR, asrHour)
        val maghribTime = createPrayerTime(calendar, PrayerType.MAGHRIB, maghribHour)
        val ishaTime = createPrayerTime(calendar, PrayerType.ISHA, ishaHour)

        val prayers = listOf(fajrTime, sunriseTime, dhuhrTime, asrTime, maghribTime, ishaTime)
        val nowMillis = System.currentTimeMillis()

        // Determine next prayer and progress
        var nextPrayer = fajrTime
        var currentActive = PrayerType.ISHA
        var foundNext = false
        var previousPrayerMillis = ishaTime.calendarTime.timeInMillis - 24 * 3600 * 1000

        for (i in prayers.indices) {
            val p = prayers[i]
            if (p.calendarTime.timeInMillis > nowMillis) {
                nextPrayer = p
                currentActive = if (i == 0) PrayerType.ISHA else prayers[i - 1].type
                previousPrayerMillis = if (i == 0) {
                    ishaTime.calendarTime.timeInMillis - 24 * 3600 * 1000
                } else {
                    prayers[i - 1].calendarTime.timeInMillis
                }
                foundNext = true
                break
            }
        }

        if (!foundNext) {
            // Next is tomorrow's Fajr
            val calTomorrowFajr = (fajrTime.calendarTime.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            nextPrayer = SinglePrayerTime(
                PrayerType.FAJR,
                fajrTime.timeFormatted,
                fajrTime.hour,
                fajrTime.minute,
                calTomorrowFajr
            )
            currentActive = PrayerType.ISHA
            previousPrayerMillis = ishaTime.calendarTime.timeInMillis
        }

        val totalIntervalSec = max(1L, (nextPrayer.calendarTime.timeInMillis - previousPrayerMillis) / 1000)
        val remainingSec = max(0L, (nextPrayer.calendarTime.timeInMillis - nowMillis) / 1000)
        val elapsedSec = totalIntervalSec - remainingSec
        val progress = (elapsedSec.toFloat() / totalIntervalSec.toFloat()).coerceIn(0f, 1f)

        val hijriDateStr = HijriCalendarUtil.getHijriDate(calendar)
        val monthsTr = arrayOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        val daysTr = arrayOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi")
        val dayOfWeek = daysTr[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val dateFormatted = "$day ${monthsTr[month - 1]} $year, $dayOfWeek"

        return DailyPrayerSchedule(
            dateString = dateFormatted,
            hijriDateString = hijriDateStr,
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

        val formatted = String.format("%02d:%02d", h, m)
        return SinglePrayerTime(type, formatted, h, m, cal)
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun getHourAngleForSunAngle(lat: Double, dec: Double, angle: Double): Double {
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val angleRad = Math.toRadians(angle)

        val top = sin(angleRad) - sin(latRad) * sin(decRad)
        val bottom = cos(latRad) * cos(decRad)
        val cosH = top / bottom

        if (cosH > 1.0) return 0.0
        if (cosH < -1.0) return 12.0
        return Math.toDegrees(acos(cosH)) / 15.0
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
