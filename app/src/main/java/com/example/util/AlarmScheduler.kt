package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.repository.UserSettings
import com.example.model.PrayerType
import com.example.receiver.PrayerAlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAllPrayerAlarms(
        context: Context,
        settings: UserSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val schedule = PrayerCalculator.calculatePrayerTimes(
            latitude = settings.latitude,
            longitude = settings.longitude,
            timeZone = settings.timeZoneOffset
        )

        val prayersWithSettings = listOf(
            Triple(PrayerType.FAJR, schedule.fajr, settings.notifFajr),
            Triple(PrayerType.SUNRISE, schedule.sunrise, settings.notifSunrise),
            Triple(PrayerType.DHUHR, schedule.dhuhr, settings.notifDhuhr),
            Triple(PrayerType.ASR, schedule.asr, settings.notifAsr),
            Triple(PrayerType.MAGHRIB, schedule.maghrib, settings.notifMaghrib),
            Triple(PrayerType.ISHA, schedule.isha, settings.notifIsha)
        )

        for ((type, prayerTime, isEnabled) in prayersWithSettings) {
            if (!isEnabled) continue

            val triggerTime = prayerTime.calendarTime.timeInMillis
            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = "com.aistudio.namazvakti.ACTION_PRAYER_ALARM"
                    putExtra("PRAYER_TYPE", type.name)
                    putExtra("CITY_NAME", settings.cityName)
                    putExtra("TIME_FORMATTED", prayerTime.timeFormatted)
                    putExtra("IS_EARLY", false)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    type.ordinal,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Fallback for strict alarm permission
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }

            // Schedule 15 minute early reminder if requested
            if (settings.earlyReminder15Min && isEnabled) {
                val earlyCal = (prayerTime.calendarTime.clone() as Calendar).apply {
                    add(Calendar.MINUTE, -15)
                }
                val earlyTrigger = earlyCal.timeInMillis
                if (earlyTrigger > System.currentTimeMillis()) {
                    val earlyIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = "com.aistudio.namazvakti.ACTION_PRAYER_ALARM"
                        putExtra("PRAYER_TYPE", type.name)
                        putExtra("CITY_NAME", settings.cityName)
                        putExtra("TIME_FORMATTED", prayerTime.timeFormatted)
                        putExtra("IS_EARLY", true)
                    }
                    val earlyPendingIntent = PendingIntent.getBroadcast(
                        context,
                        100 + type.ordinal,
                        earlyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    try {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, earlyTrigger, earlyPendingIntent)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
    }
}
