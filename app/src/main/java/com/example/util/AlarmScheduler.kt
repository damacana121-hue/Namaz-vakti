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

    private const val REQUEST_CODE_TODAY_MAIN = 0
    private const val REQUEST_CODE_TODAY_EARLY = 100
    private const val REQUEST_CODE_TOMORROW_MAIN = 200
    private const val REQUEST_CODE_TOMORROW_EARLY = 300

    fun cancelAllPrayerAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val baseIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.aistudio.namazvakti.ACTION_PRAYER_ALARM"
        }

        for (type in PrayerType.values()) {
            val codes = listOf(
                REQUEST_CODE_TODAY_MAIN + type.ordinal,
                REQUEST_CODE_TODAY_EARLY + type.ordinal,
                REQUEST_CODE_TOMORROW_MAIN + type.ordinal,
                REQUEST_CODE_TOMORROW_EARLY + type.ordinal
            )
            for (code in codes) {
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    baseIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }

    fun scheduleAllPrayerAlarms(
        context: Context,
        settings: UserSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // First cancel any obsolete/orphaned alarms
        cancelAllPrayerAlarms(context)

        val nowMillis = System.currentTimeMillis()

        // 1. Calculate today's schedule
        val calToday = Calendar.getInstance()
        val todaySchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = settings.latitude,
            longitude = settings.longitude,
            timeZone = settings.timeZoneOffset,
            calendar = calToday
        )

        // 2. Calculate tomorrow's schedule (guarantees upcoming Fajr and next-day prayers are never missed)
        val calTomorrow = (Calendar.getInstance()).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = settings.latitude,
            longitude = settings.longitude,
            timeZone = settings.timeZoneOffset,
            calendar = calTomorrow
        )

        val schedulesToProcess = listOf(
            Pair(todaySchedule, Pair(REQUEST_CODE_TODAY_MAIN, REQUEST_CODE_TODAY_EARLY)),
            Pair(tomorrowSchedule, Pair(REQUEST_CODE_TOMORROW_MAIN, REQUEST_CODE_TOMORROW_EARLY))
        )

        for ((schedule, codeOffsets) in schedulesToProcess) {
            val (mainBaseCode, earlyBaseCode) = codeOffsets

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

                // Schedule Main Prayer Alarm
                if (triggerTime > nowMillis) {
                    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                        action = "com.aistudio.namazvakti.ACTION_PRAYER_ALARM"
                        putExtra("PRAYER_TYPE", type.name)
                        putExtra("CITY_NAME", settings.cityName)
                        putExtra("TIME_FORMATTED", prayerTime.timeFormatted)
                        putExtra("IS_EARLY", false)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        mainBaseCode + type.ordinal,
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
                        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }

                // Schedule 15-minute Early Reminder Alarm
                if (settings.earlyReminder15Min && isEnabled) {
                    val earlyCal = (prayerTime.calendarTime.clone() as Calendar).apply {
                        add(Calendar.MINUTE, -15)
                    }
                    val earlyTrigger = earlyCal.timeInMillis
                    if (earlyTrigger > nowMillis) {
                        val earlyIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                            action = "com.aistudio.namazvakti.ACTION_PRAYER_ALARM"
                            putExtra("PRAYER_TYPE", type.name)
                            putExtra("CITY_NAME", settings.cityName)
                            putExtra("TIME_FORMATTED", prayerTime.timeFormatted)
                            putExtra("IS_EARLY", true)
                        }
                        val earlyPendingIntent = PendingIntent.getBroadcast(
                            context,
                            earlyBaseCode + type.ordinal,
                            earlyIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    earlyTrigger,
                                    earlyPendingIntent
                                )
                            } else {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    earlyTrigger,
                                    earlyPendingIntent
                                )
                            }
                        } catch (e: Exception) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, earlyTrigger, earlyPendingIntent)
                        }
                    }
                }
            }
        }
    }
}
