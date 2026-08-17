package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NamazVaktiApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var prayerRepository: PrayerRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        prayerRepository = PrayerRepository(database)
        settingsRepository = SettingsRepository(this)

        NotificationHelper.createNotificationChannels(this)

        applicationScope.launch {
            AlarmScheduler.scheduleAllPrayerAlarms(
                this@NamazVaktiApp,
                settingsRepository.settings.value
            )
        }
    }
}
