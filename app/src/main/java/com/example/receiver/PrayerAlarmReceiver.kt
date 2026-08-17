package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.SettingsRepository
import com.example.model.PrayerType
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settingsRepo = SettingsRepository(context)
            AlarmScheduler.scheduleAllPrayerAlarms(context, settingsRepo.settings.value)
            return
        }

        val prayerTypeName = intent.getStringExtra("PRAYER_TYPE") ?: return
        val cityName = intent.getStringExtra("CITY_NAME") ?: "Şehir"
        val timeFormatted = intent.getStringExtra("TIME_FORMATTED") ?: ""
        val isEarly = intent.getBooleanExtra("IS_EARLY", false)

        try {
            val prayerType = PrayerType.valueOf(prayerTypeName)
            NotificationHelper.showPrayerNotification(
                context = context,
                prayerType = prayerType,
                cityName = cityName,
                timeFormatted = timeFormatted,
                isEarlyReminder = isEarly
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Reschedule alarms for upcoming window
        val settingsRepo = SettingsRepository(context)
        AlarmScheduler.scheduleAllPrayerAlarms(context, settingsRepo.settings.value)
    }
}
