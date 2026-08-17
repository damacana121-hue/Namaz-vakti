package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CityLocation
import com.example.model.TurkishCities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val cityName: String = "İstanbul",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val timeZoneOffset: Double = 3.0,
    // Notification toggles
    val notifFajr: Boolean = true,
    val notifSunrise: Boolean = false,
    val notifDhuhr: Boolean = true,
    val notifAsr: Boolean = true,
    val notifMaghrib: Boolean = true,
    val notifIsha: Boolean = true,
    val earlyReminder15Min: Boolean = true,
    val soundType: String = "Ezan", // "Ezan", "Bip / Sesli", "Sadece Titreşim", "Sessiz"
    // Home screen customizable widgets
    val showAyahHadithWidget: Boolean = true,
    val showFastDhikrWidget: Boolean = true,
    val showQuranProgressWidget: Boolean = true,
    val showKazaWidget: Boolean = true,
    val showQiblaCardWidget: Boolean = true,
    // Theme
    val themeMode: String = "Sistem" // "Sistem", "Karanlık", "Aydınlık"
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("namaz_vakti_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            cityName = prefs.getString("city_name", "İstanbul") ?: "İstanbul",
            latitude = prefs.getFloat("city_lat", 41.0082f).toDouble(),
            longitude = prefs.getFloat("city_lon", 28.9784f).toDouble(),
            timeZoneOffset = prefs.getFloat("city_tz", 3.0f).toDouble(),
            notifFajr = prefs.getBoolean("notif_fajr", true),
            notifSunrise = prefs.getBoolean("notif_sunrise", false),
            notifDhuhr = prefs.getBoolean("notif_dhuhr", true),
            notifAsr = prefs.getBoolean("notif_asr", true),
            notifMaghrib = prefs.getBoolean("notif_maghrib", true),
            notifIsha = prefs.getBoolean("notif_isha", true),
            earlyReminder15Min = prefs.getBoolean("early_reminder_15", true),
            soundType = prefs.getString("sound_type", "Ezan") ?: "Ezan",
            showAyahHadithWidget = prefs.getBoolean("widget_ayah", true),
            showFastDhikrWidget = prefs.getBoolean("widget_dhikr", true),
            showQuranProgressWidget = prefs.getBoolean("widget_quran", true),
            showKazaWidget = prefs.getBoolean("widget_kaza", true),
            showQiblaCardWidget = prefs.getBoolean("widget_qibla", true),
            themeMode = prefs.getString("theme_mode", "Sistem") ?: "Sistem"
        )
    }

    fun updateCity(city: CityLocation) {
        prefs.edit()
            .putString("city_name", city.name)
            .putFloat("city_lat", city.latitude.toFloat())
            .putFloat("city_lon", city.longitude.toFloat())
            .putFloat("city_tz", city.timeZoneOffsetHours.toFloat())
            .apply()
        _settings.value = loadSettings()
    }

    fun updateNotification(
        fajr: Boolean? = null,
        sunrise: Boolean? = null,
        dhuhr: Boolean? = null,
        asr: Boolean? = null,
        maghrib: Boolean? = null,
        isha: Boolean? = null,
        early15: Boolean? = null,
        sound: String? = null
    ) {
        val editor = prefs.edit()
        fajr?.let { editor.putBoolean("notif_fajr", it) }
        sunrise?.let { editor.putBoolean("notif_sunrise", it) }
        dhuhr?.let { editor.putBoolean("notif_dhuhr", it) }
        asr?.let { editor.putBoolean("notif_asr", it) }
        maghrib?.let { editor.putBoolean("notif_maghrib", it) }
        isha?.let { editor.putBoolean("notif_isha", it) }
        early15?.let { editor.putBoolean("early_reminder_15", it) }
        sound?.let { editor.putString("sound_type", it) }
        editor.apply()
        _settings.value = loadSettings()
    }

    fun updateWidgetVisibility(
        ayah: Boolean? = null,
        dhikr: Boolean? = null,
        quran: Boolean? = null,
        kaza: Boolean? = null,
        qibla: Boolean? = null
    ) {
        val editor = prefs.edit()
        ayah?.let { editor.putBoolean("widget_ayah", it) }
        dhikr?.let { editor.putBoolean("widget_dhikr", it) }
        quran?.let { editor.putBoolean("widget_quran", it) }
        kaza?.let { editor.putBoolean("widget_kaza", it) }
        qibla?.let { editor.putBoolean("widget_qibla", it) }
        editor.apply()
        _settings.value = loadSettings()
    }

    fun updateThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _settings.value = loadSettings()
    }
}
