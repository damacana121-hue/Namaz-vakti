package com.example.util

import java.util.Calendar
import kotlin.math.floor

object HijriCalendarUtil {

    private val hijriMonthsTr = arrayOf(
        "Muharrem", "Safer", "Rebîülevvel", "Rebîülâhir",
        "Cemâziyelevvel", "Cemâziyelâhir", "Recep", "Şaban",
        "Ramazan", "Şevval", "Zilkade", "Zilhicce"
    )

    fun getHijriDate(calendar: Calendar = Calendar.getInstance()): String {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        // Convert Gregorian to Julian Day Number
        var myYear = y
        var myMonth = m
        if (myMonth < 3) {
            myYear -= 1
            myMonth += 12
        }
        val a = floor(myYear / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (myYear + 4716)) + floor(30.6001 * (myMonth + 1)) + d + b - 1524

        // Julian Day to Hijri conversion (Kuwaiti algorithm)
        val epoch = 1948439.5
        val daysSinceEpoch = jd - epoch
        val cycle = floor(daysSinceEpoch / 10631.0)
        val cycleRemaining = daysSinceEpoch - cycle * 10631.0
        val hijriYearInCycle = floor((cycleRemaining + 0.5) / 354.366)
        val hijriYear = (cycle * 30 + hijriYearInCycle + 1).toInt()

        val yearDay = cycleRemaining - floor(hijriYearInCycle * 354.366)
        val hijriMonth = (floor((yearDay + 0.5) / 29.5)).toInt().coerceIn(0, 11)
        val hijriDay = (yearDay - floor(hijriMonth * 29.5) + 1).toInt().coerceIn(1, 30)

        val monthName = hijriMonthsTr[hijriMonth]
        return "$hijriDay $monthName $hijriYear"
    }

    fun getUpcomingSpecialIslamicEvents(calendar: Calendar = Calendar.getInstance()): List<Pair<String, String>> {
        return listOf(
            "Cuma Günü" to "Haftalık Mübarek İbadet ve Dua Günü",
            "Pazartesi & Perşembe" to "Sünnet Orucu ve İbadet Vakti",
            "Kadir Gecesi" to "Bin aydan daha hayırlı mübarek gece",
            "Mevlid Kandili" to "Peygamber Efendimiz'in (s.a.v) Dünyaya Teşrifi",
            "Regaip Kandili" to "Üç Ayların ve Rahmet Kapılarının Başlangıcı",
            "Miraç Kandili" to "Namazın Farz Kılındığı Şerefli Gece",
            "Berat Kandili" to "Af, Mağfiret ve Kurtuluş Gecesi"
        )
    }
}
