package com.juno.app.ui.screens.pronunciation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.VoiceRecordingManager
import com.juno.app.data.repository.TtsService
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class PronunciationUiState(
    val isLoading: Boolean = true,
    val words: List<com.juno.app.data.local.entity.WordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val currentWord: com.juno.app.data.local.entity.WordEntity? = null,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val recognizedText: String = "",
    val score: Int? = null,
    val totalScore: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val totalWords: Int
        get() = words.size

    val progress: Float
        get() = if (words.isEmpty()) 0f else (currentIndex.toFloat() / words.size)

    val averageScore: Int
        get() = if (currentIndex == 0) 0 else (totalScore / currentIndex)
}

@HiltViewModel
class PronunciationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordRepository: WordRepository,
    private val ttsService: TtsService,
    private val voiceRecordingManager: VoiceRecordingManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PronunciationUiState())
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var recordedWord: String? = null
    private val completedScores = mutableListOf<Int>()

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val words = wordRepository.getAllWords().first()
                if (words.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = emptyList(),
                        isComplete = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = words,
                        currentWord = words.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load words"
                )
            }
        }
    }

    fun playOriginal() {
        val word = _uiState.value.currentWord?.word ?: return
        _uiState.value = _uiState.value.copy(isPlaying = true)
        ttsService.speak(word)
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(isPlaying = false)
        }
    }

    fun startRecording() {
        if (_uiState.value.isRecording) return

        // Check record audio permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.value = _uiState.value.copy(
                error = "需要麦克风权限才能录音，请在设置中授予录音权限"
            )
            return
        }

        try {
            val filePath = voiceRecordingManager.startRecording()
            if (filePath == null) {
                _uiState.value = _uiState.value.copy(
                    error = "录音初始化失败，请检查麦克风是否被其他应用占用"
                )
                return
            }
            _uiState.value = _uiState.value.copy(
                isRecording = true,
                recognizedText = "",
                score = null
            )
            startSpeechRecognition()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "录音启动失败: ${e.message ?: "未知错误"}"
            )
        }
    }

    fun stopRecording() {
        try {
            voiceRecordingManager.stopRecording()
        } catch (e: Exception) {
            // Swallow errors on stop to avoid crash
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(isRecording = false)
        stopSpeechRecognition()
    }

    private fun startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _uiState.value = _uiState.value.copy(
                error = "Speech recognition not available",
                isRecording = false
            )
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        else -> "Recognition error: $error"
                    }
                    _uiState.value = _uiState.value.copy(
                        isRecording = false,
                        error = errorMessage
                    )
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognized = matches?.firstOrNull() ?: ""
                    processRecognitionResult(recognized)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    if (partial.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(recognizedText = partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    private fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun processRecognitionResult(recognized: String) {
        val targetWord = _uiState.value.currentWord?.word ?: return

        _uiState.value = _uiState.value.copy(recognizedText = recognized)

        if (recognized.isNotEmpty()) {
            val score = calculateScore(targetWord, recognized)
            completedScores.add(score)
            val totalScore = completedScores.sum()

            _uiState.value = _uiState.value.copy(
                score = score,
                totalScore = totalScore
            )
        }
    }

    private fun calculateScore(target: String, recognized: String): Int {
        val targetLower = target.lowercase().trim()
        val recognizedLower = recognized.lowercase().trim()

        if (targetLower == recognizedLower) return 100

        val distance = levenshteinDistance(targetLower, recognizedLower)
        val maxLength = maxOf(targetLower.length, recognizedLower.length)

        if (maxLength == 0) return 100

        val similarity = 1.0 - (distance.toDouble() / maxLength.toDouble())
        return (similarity * 100).toInt().coerceIn(0, 100)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[m][n]
    }

    fun nextWord() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.words.size) {
            _uiState.value = currentState.copy(
                isComplete = true,
                currentWord = null
            )
        } else {
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                currentWord = currentState.words[nextIndex],
                recognizedText = "",
                score = null
            )
        }
    }

    fun restart() {
        completedScores.clear()
        _uiState.value = PronunciationUiState(isLoading = true)
        loadWords()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeechRecognition()
        voiceRecordingManager.release()
    }
}
