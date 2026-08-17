package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.SettingsRepository
import com.example.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                val settingsRepo = SettingsRepository(context)
                AlarmScheduler.scheduleAllPrayerAlarms(context, settingsRepo.settings.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
