package com.sparkiai.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class VoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                // Handle different error types if needed
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                    if (matches.isNotEmpty()) {
                        _recognizedText.value = matches[0]
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            }
        }
    }

    fun startListening() {
        if (!_isListening.value) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun speak(text: String) {
        if (textToSpeech != null && !_isSpeaking.value) {
            _isSpeaking.value = true
            textToSpeech?.setOnUtteranceProgressListener(object :
                android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SparkiFire_TTS")
        }
    }

    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}