package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SpeechUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val recognizedText: String = "",
    val speechError: String? = null,
    val audioRmsLevel: Float = 0f
)

class VoiceSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechState = MutableStateFlow(SpeechUiState())
    val speechState: StateFlow<SpeechUiState> = _speechState.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            tts = null
        }
        try {
            initSpeechRecognizer()
        } catch (e: Exception) {
            speechRecognizer = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("tr", "TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(0.95f)
            isTtsReady = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _speechState.value = _speechState.value.copy(isSpeaking = true)
                }

                override fun onDone(utteranceId: String?) {
                    _speechState.value = _speechState.value.copy(isSpeaking = false)
                }

                override fun onError(utteranceId: String?) {
                    _speechState.value = _speechState.value.copy(isSpeaking = false)
                }
            })
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _speechState.value = _speechState.value.copy(isListening = true, speechError = null)
                    }

                    override fun onBeginningOfSpeech() {
                        _speechState.value = _speechState.value.copy(isListening = true)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _speechState.value = _speechState.value.copy(audioRmsLevel = (rmsdB / 10f).coerceIn(0f, 1f))
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _speechState.value = _speechState.value.copy(isListening = false, audioRmsLevel = 0f)
                    }

                    override fun onError(error: Int) {
                        val errMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Ses anlaşılamadı, lütfen tekrar deneyin"
                            SpeechRecognizer.ERROR_NETWORK -> "Ağ bağlantı hatası"
                            SpeechRecognizer.ERROR_AUDIO -> "Mikrofon ses kayıt hatası"
                            else -> "Dinleme tamamlandı"
                        }
                        _speechState.value = _speechState.value.copy(
                            isListening = false,
                            speechError = errMsg,
                            audioRmsLevel = 0f
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _speechState.value = _speechState.value.copy(
                            isListening = false,
                            recognizedText = text,
                            audioRmsLevel = 0f
                        )
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotEmpty()) {
                            _speechState.value = _speechState.value.copy(recognizedText = text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        stopSpeaking()
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Sorunuzu veya talebinizi sesli olarak söyleyin...")
        }
        try {
            speechRecognizer?.startListening(intent)
            _speechState.value = _speechState.value.copy(isListening = true, speechError = null, recognizedText = "")
        } catch (e: Exception) {
            _speechState.value = _speechState.value.copy(isListening = false, speechError = e.localizedMessage)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        _speechState.value = _speechState.value.copy(isListening = false, audioRmsLevel = 0f)
    }

    fun speakText(text: String) {
        if (!isTtsReady || tts == null) return
        stopSpeaking()
        // Clean markdown symbols for natural TTS speech
        val cleanText = text.replace("*", "").replace("#", "").replace("`", "").trim()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "GeminiSpeech_${System.currentTimeMillis()}")
        }
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "GeminiSpeech_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _speechState.value = _speechState.value.copy(isSpeaking = false)
    }

    fun clearRecognizedText() {
        _speechState.value = _speechState.value.copy(recognizedText = "")
    }

    fun shutdown() {
        try {
            stopSpeaking()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // ignore
        }
    }
}
