package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NamazVaktiApp
import com.example.data.local.entity.DhikrEntity
import com.example.data.local.entity.KazaRecordEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.QuranProgressEntity
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.repository.UserSettings
import com.example.data.repository.WeeklyPrayerAnalytics
import com.example.model.CityLocation
import com.example.model.PrayerType
import com.example.model.TurkishCities
import com.example.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MainUiState(
    val currentSchedule: DailyPrayerSchedule? = null,
    val selectedCity: CityLocation = TurkishCities.defaultCity,
    val userSettings: UserSettings = UserSettings(),
    val todayRecord: PrayerRecordEntity? = null,
    val weeklyAnalytics: WeeklyPrayerAnalytics? = null,
    val latestQuranProgress: QuranProgressEntity? = null,
    val dhikrList: List<DhikrEntity> = emptyList(),
    val activeDhikr: DhikrEntity? = null,
    val kazaRecord: KazaRecordEntity? = null,
    val compassOrientation: CompassOrientation = CompassOrientation(),
    val qiblaBearing: Double = 0.0,
    val distanceToKaabaKm: Double = 0.0,
    val isAlignedWithQibla: Boolean = false,
    val selectedDate: Calendar = Calendar.getInstance(),
    val isManualCompassAngle: Boolean = false,
    val manualCompassAngle: Float = 0f,
    // Gemini & Voice States
    val geminiMessages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = "gemini",
            text = "Selamün Aleyküm! Ben 'İkizler Zekası' (Gemini Live) Manevi Rehberinizim. Namaz vakitleri, kıble, namazların kılınışı, dualar, hadisler veya günlük ibadetleriniz hakkında sesli veya yazılı her şeyi sorabilirsiniz. Size nasıl yardımcı olabilirim?"
        )
    ),
    val isGeminiLoading: Boolean = false,
    val speechUiState: SpeechUiState = SpeechUiState(),
    val isGpsDetecting: Boolean = false,
    val gpsStatusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NamazVaktiApp
    private val prayerRepo = app.prayerRepository
    private val settingsRepo = app.settingsRepository
    private val compassSensorManager = CompassSensorManager(application)
    val voiceSpeechManager = VoiceSpeechManager(application)
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Collect user settings
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                val city = TurkishCities.list.find { it.name.equals(settings.cityName, ignoreCase = true) }
                    ?: CityLocation(settings.cityName, "Türkiye", settings.latitude, settings.longitude, settings.timeZoneOffset)

                val qiblaAngle = QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude)
                val distKm = QiblaCalculator.calculateDistanceToKaabaKm(city.latitude, city.longitude)

                _uiState.update {
                    it.copy(
                        userSettings = settings,
                        selectedCity = city,
                        qiblaBearing = qiblaAngle,
                        distanceToKaabaKm = distKm
                    )
                }
                recalculateSchedule()
            }
        }

        // Collect today's prayer record
        viewModelScope.launch {
            prayerRepo.getTodayPrayerRecord().collect { record ->
                _uiState.update { it.copy(todayRecord = record) }
            }
        }

        // Collect weekly records for analytics
        viewModelScope.launch {
            prayerRepo.allPrayers.collect { list ->
                val analytics = prayerRepo.computeWeeklyAnalytics(list)
                _uiState.update { it.copy(weeklyAnalytics = analytics) }
            }
        }

        // Collect Quran progress
        viewModelScope.launch {
            prayerRepo.latestQuranProgress.collect { qp ->
                _uiState.update { it.copy(latestQuranProgress = qp) }
            }
        }

        // Collect Dhikrs
        viewModelScope.launch {
            prayerRepo.allDhikrs.collect { list ->
                _uiState.update {
                    val active = if (it.activeDhikr != null) {
                        list.find { d -> d.id == it.activeDhikr.id } ?: list.firstOrNull()
                    } else {
                        list.firstOrNull()
                    }
                    it.copy(dhikrList = list, activeDhikr = active)
                }
            }
        }

        // Collect Kaza
        viewModelScope.launch {
            prayerRepo.kazaRecord.collect { kaza ->
                _uiState.update { it.copy(kazaRecord = kaza) }
            }
        }

        // Live Compass Sensor collection (Only updates when compass is active)
        viewModelScope.launch {
            compassSensorManager.orientationFlow.collect { orientation ->
                if (!_uiState.value.isManualCompassAngle) {
                    checkQiblaAlignment(orientation.azimuthDegrees)
                    _uiState.update { it.copy(compassOrientation = orientation) }
                }
            }
        }

        // Voice Speech State collection
        viewModelScope.launch {
            voiceSpeechManager.speechState.collect { speechState ->
                _uiState.update { it.copy(speechUiState = speechState) }
                if (speechState.recognizedText.isNotBlank() && !speechState.isListening) {
                    val recognized = speechState.recognizedText
                    voiceSpeechManager.clearRecognizedText()
                    askGemini(recognized, autoSpeak = true)
                }
            }
        }

        // 1-second ticking timer for live countdown
        viewModelScope.launch {
            while (isActive) {
                recalculateSchedule()
                delay(1000)
            }
        }
    }

    private fun recalculateSchedule() {
        val city = _uiState.value.selectedCity
        val cal = _uiState.value.selectedDate
        val schedule = PrayerCalculator.calculatePrayerTimes(
            latitude = city.latitude,
            longitude = city.longitude,
            timeZone = city.timeZoneOffsetHours,
            calendar = cal
        )
        _uiState.update { it.copy(currentSchedule = schedule) }
    }

    fun selectCity(city: CityLocation) {
        settingsRepo.updateCity(city)
        val qibla = QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude)
        val dist = QiblaCalculator.calculateDistanceToKaabaKm(city.latitude, city.longitude)
        _uiState.update {
            it.copy(
                selectedCity = city,
                qiblaBearing = qibla,
                distanceToKaabaKm = dist,
                gpsStatusMessage = null
            )
        }
        recalculateSchedule()
        AlarmScheduler.scheduleAllPrayerAlarms(getApplication(), _uiState.value.userSettings)
    }

    fun detectAndApplyGpsLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGpsDetecting = true, gpsStatusMessage = "GPS konumu alınıyor...") }
            try {
                val location = LocationHelper.getCurrentLocation(context)
                if (location != null) {
                    val city = LocationHelper.resolveCityFromLocation(location)
                    selectCity(city)
                    _uiState.update {
                        it.copy(
                            isGpsDetecting = false,
                            gpsStatusMessage = "Konum otomatik güncellendi: ${city.name}"
                        )
                    }
                    performHapticFeedback(50)
                } else {
                    _uiState.update {
                        it.copy(
                            isGpsDetecting = false,
                            gpsStatusMessage = "GPS sinyali alınamadı. Lütfen konum iznini ve GPS servisini kontrol edin."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGpsDetecting = false,
                        gpsStatusMessage = "Konum hatası: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // --- Gemini AI & Live Voice Methods ---
    fun askGemini(prompt: String, autoSpeak: Boolean = false) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = prompt.trim())
        _uiState.update {
            it.copy(
                geminiMessages = it.geminiMessages + userMsg,
                isGeminiLoading = true
            )
        }

        viewModelScope.launch {
            // Build conversation history for context
            val history = _uiState.value.geminiMessages.takeLast(6).map {
                GeminiContent(
                    role = if (it.sender == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = it.text))
                )
            }

            val currentCity = _uiState.value.selectedCity.name
            val activePrayer = _uiState.value.currentSchedule?.currentActivePrayer?.titleTr ?: ""
            val nextPrayer = _uiState.value.currentSchedule?.nextPrayer?.type?.titleTr ?: ""
            val nextPrayerTime = _uiState.value.currentSchedule?.nextPrayer?.timeFormatted ?: ""
            val qiblaAngle = String.format("%.1f", _uiState.value.qiblaBearing)

            val contextData = "Kullanıcı Konumu: $currentCity, Kıble Açısı: $qiblaAngle derece, Şimdiki Vakit: $activePrayer, Sıradaki Vakit: $nextPrayer (Saat $nextPrayerTime)."

            val result = GeminiApiClient.askGemini(
                userPrompt = prompt,
                conversationHistory = history,
                customContext = contextData
            )

            val replyText = result.getOrElse {
                "Bağlantı sırasında bir aksaklık oldu. Lütfen sorunuzu tekrar deneyiniz."
            }

            val geminiMsg = ChatMessage(sender = "gemini", text = replyText)
            _uiState.update {
                it.copy(
                    geminiMessages = it.geminiMessages + geminiMsg,
                    isGeminiLoading = false
                )
            }

            if (autoSpeak) {
                voiceSpeechManager.speakText(replyText)
            }
        }
    }

    fun speakGeminiMessage(text: String) {
        voiceSpeechManager.speakText(text)
    }

    fun stopSpeaking() {
        voiceSpeechManager.stopSpeaking()
    }

    fun startVoiceListening() {
        voiceSpeechManager.startListening()
    }

    fun stopVoiceListening() {
        voiceSpeechManager.stopListening()
    }

    fun clearGeminiChat() {
        voiceSpeechManager.stopSpeaking()
        _uiState.update {
            it.copy(
                geminiMessages = listOf(
                    ChatMessage(
                        sender = "gemini",
                        text = "Sohbet sıfırlandı. İkizler Zekası (Gemini Live) olarak her an manevi sorularınızı yanıtlamaya ve sesli rehberlik sunmaya hazırım."
                    )
                )
            )
        }
    }

    fun performSpiritualAnalysis() {
        val totalWeekPrayers = _uiState.value.weeklyAnalytics?.totalCompletedPrayers ?: 0
        val streak = _uiState.value.weeklyAnalytics?.currentStreakDays ?: 0
        val quranPage = _uiState.value.latestQuranProgress?.lastPageRead ?: 1
        val kazaRecord = _uiState.value.kazaRecord

        val analysisPrompt = "Lütfen ibadet analizimi yap ve manevi tavsiyelerde bulun: " +
                "Bu hafta $totalWeekPrayers vakit namaz kıldım. Kesintisiz devamlılık serim: $streak gün. " +
                "Kur'an-ı Kerim'de $quranPage. sayfadayım. " +
                (if (kazaRecord != null) "Kaza namazı borçlarım: Sabah: ${kazaRecord.fajrKaza}, Öğle: ${kazaRecord.dhuhrKaza}, İkindi: ${kazaRecord.asrKaza}, Akşam: ${kazaRecord.maghribKaza}, Yatsı: ${kazaRecord.ishaKaza}. " else "") +
                "Bana manevi gelişimim için cesaret verici, motive edici ve hadis/ayet ışığında bir haftalık değerlendirme sun."

        askGemini(analysisPrompt, autoSpeak = true)
    }

    fun setSelectedDate(calendar: Calendar) {
        _uiState.update { it.copy(selectedDate = calendar) }
        recalculateSchedule()
    }

    fun togglePrayerCompleted(type: PrayerType) {
        viewModelScope.launch {
            prayerRepo.togglePrayerDone(type)
            performHapticFeedback(50)
        }
    }

    fun toggleJamaat(type: PrayerType) {
        viewModelScope.launch {
            prayerRepo.toggleJamaat(type)
            performHapticFeedback(30)
        }
    }

    // Dhikr methods
    fun selectActiveDhikr(dhikr: DhikrEntity) {
        _uiState.update { it.copy(activeDhikr = dhikr) }
    }

    fun countDhikr() {
        val active = _uiState.value.activeDhikr ?: return
        viewModelScope.launch {
            performHapticFeedback(35)
            prayerRepo.incrementDhikr(active)
        }
    }

    fun resetActiveDhikr() {
        val active = _uiState.value.activeDhikr ?: return
        viewModelScope.launch {
            prayerRepo.resetDhikr(active)
            performHapticFeedback(60)
        }
    }

    fun createNewDhikr(title: String, arabic: String, meaning: String, target: Int, category: String) {
        viewModelScope.launch {
            prayerRepo.addNewDhikr(title, arabic, meaning, target, category)
        }
    }

    // Quran methods
    fun saveQuranReading(page: Int, juz: Int, surah: String, pagesReadToday: Int) {
        viewModelScope.launch {
            prayerRepo.saveQuranProgress(page, juz, surah, pagesReadToday)
            performHapticFeedback(40)
        }
    }

    // Kaza methods
    fun updateKaza(
        fajr: Int = 0,
        dhuhr: Int = 0,
        asr: Int = 0,
        maghrib: Int = 0,
        isha: Int = 0,
        witr: Int = 0,
        fasting: Int = 0
    ) {
        viewModelScope.launch {
            prayerRepo.updateKazaCount(
                fajrDelta = fajr,
                dhuhrDelta = dhuhr,
                asrDelta = asr,
                maghribDelta = maghrib,
                ishaDelta = isha,
                witrDelta = witr,
                fastingDelta = fasting,
                currentRecord = _uiState.value.kazaRecord
            )
            performHapticFeedback(25)
        }
    }

    // Settings
    fun updateNotificationToggle(
        fajr: Boolean? = null,
        sunrise: Boolean? = null,
        dhuhr: Boolean? = null,
        asr: Boolean? = null,
        maghrib: Boolean? = null,
        isha: Boolean? = null,
        early15: Boolean? = null,
        sound: String? = null
    ) {
        settingsRepo.updateNotification(fajr, sunrise, dhuhr, asr, maghrib, isha, early15, sound)
        AlarmScheduler.scheduleAllPrayerAlarms(getApplication(), _uiState.value.userSettings)
    }

    fun updateWidgetVisibility(
        ayah: Boolean? = null,
        dhikr: Boolean? = null,
        quran: Boolean? = null,
        kaza: Boolean? = null,
        qibla: Boolean? = null
    ) {
        settingsRepo.updateWidgetVisibility(ayah, dhikr, quran, kaza, qibla)
    }

    fun updateTheme(mode: String) {
        settingsRepo.updateThemeMode(mode)
    }

    fun sendTestNotification() {
        NotificationHelper.showTestNotification(getApplication(), _uiState.value.selectedCity.name)
    }

    fun setManualCompassAngle(angle: Float) {
        _uiState.update {
            it.copy(
                isManualCompassAngle = true,
                manualCompassAngle = angle,
                compassOrientation = CompassOrientation(azimuthDegrees = angle, isSensorAvailable = true)
            )
        }
        checkQiblaAlignment(angle)
    }

    fun startCompass() {
        compassSensorManager.startListening()
    }

    fun stopCompass() {
        compassSensorManager.stopListening()
    }

    fun useHardwareSensor() {
        _uiState.update { it.copy(isManualCompassAngle = false) }
        compassSensorManager.startListening()
    }

    private var lastVibratedAligned = false
    private fun checkQiblaAlignment(azimuth: Float) {
        val qiblaBearing = _uiState.value.qiblaBearing
        var diff = Math.abs(azimuth - qiblaBearing)
        if (diff > 180) diff = 360 - diff
        val isAligned = diff <= 4.0 // within 4 degrees

        if (isAligned && !lastVibratedAligned) {
            performHapticFeedback(100)
            lastVibratedAligned = true
        } else if (!isAligned) {
            lastVibratedAligned = false
        }

        _uiState.update { it.copy(isAlignedWithQibla = isAligned) }
    }

    private fun performHapticFeedback(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassSensorManager.stopListening()
        voiceSpeechManager.shutdown()
    }
}

