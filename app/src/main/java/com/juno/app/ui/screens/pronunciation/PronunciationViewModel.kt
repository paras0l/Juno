package com.juno.app.ui.screens.pronunciation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.VoiceRecordingManager
import com.juno.app.data.repository.TtsService
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class PronunciationUiState(
    val isLoading: Boolean = true,
    val words: List<WordEntity> = emptyList(),
    val currentWord: WordEntity? = null,
    val isOriginalPlaying: Boolean = false,
    val isRecordingPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val recordingFilePath: String? = null,
    val recordingDurationMs: Long = 0,
    val isComplete: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filteredWords: List<WordEntity> = emptyList()
) {
    val averageScore: Int
        get() = 0
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

    private var mediaPlayer: MediaPlayer? = null
    private var passedWord: String? = null
    private var recordingStartTime: Long = 0
    private var timerJob: kotlinx.coroutines.Job? = null

    fun setTargetWord(word: String?) {
        android.util.Log.d("PronunciationVM", "setTargetWord called with: $word")
        passedWord = word
        loadAllWords()
    }

    private fun loadAllWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val allWords = wordRepository.getAllWords().first()
                val targetWord = passedWord
                val current = if (targetWord != null && targetWord.isNotEmpty()) {
                    allWords.find { it.word.equals(targetWord, ignoreCase = true) }
                } else {
                    allWords.firstOrNull()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    words = allWords,
                    filteredWords = allWords.take(20),
                    currentWord = current,
                    error = if (allWords.isEmpty()) "没有可练习的单词" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载单词失败"
                )
            }
        }
    }

    fun searchWords(query: String) {
        val allWords = _uiState.value.words
        val filtered = if (query.isEmpty()) {
            allWords.take(20)
        } else {
            allWords.filter { it.word.contains(query, ignoreCase = true) }.take(20)
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredWords = filtered
        )
    }

    fun selectWord(word: WordEntity) {
        _uiState.value = _uiState.value.copy(
            currentWord = word,
            recordingFilePath = null,
            recordingDurationMs = 0,
            searchQuery = "" // Reset search when a word is selected
        )
    }

    fun playOriginal() {
        val word = _uiState.value.currentWord?.word ?: return
        _uiState.value = _uiState.value.copy(isOriginalPlaying = true)
        ttsService.speak(word)
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(isOriginalPlaying = false)
        }
    }

    fun playRecording() {
        val filePath = _uiState.value.recordingFilePath ?: return
        if (!File(filePath).exists()) {
            _uiState.value = _uiState.value.copy(error = "录音文件不存在")
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(isRecordingPlaying = false)
                }
            }
            _uiState.value = _uiState.value.copy(isRecordingPlaying = true)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "播放录音失败: ${e.message}")
        }
    }

    fun startRecording() {
        android.util.Log.d("PronunciationVM", "startRecording called")
        
        if (_uiState.value.isRecording) {
            android.util.Log.d("PronunciationVM", "Already recording, returning")
            return
        }

        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
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
            recordingStartTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                isRecording = true,
                recordingFilePath = filePath,
                recordingDurationMs = 0
            )
            
            // Start a timer to dynamically update the recording duration
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_uiState.value.isRecording) {
                    kotlinx.coroutines.delay(100)
                    _uiState.value = _uiState.value.copy(
                        recordingDurationMs = System.currentTimeMillis() - recordingStartTime
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "录音启动失败: ${e.message ?: "未知错误"}"
            )
        }
    }

    fun stopRecording() {
        timerJob?.cancel()
        try {
            voiceRecordingManager.stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val duration = if (recordingStartTime > 0) System.currentTimeMillis() - recordingStartTime else 0
        recordingStartTime = 0

        if (duration < 200) {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "录入时间太短", android.widget.Toast.LENGTH_SHORT).show()
            }
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                recordingDurationMs = 0,
                recordingFilePath = null
            )
        } else {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "录音成功", android.widget.Toast.LENGTH_SHORT).show()
            }
            _uiState.value = _uiState.value.copy(
                isRecording = false,
                recordingDurationMs = duration
            )
        }
    }

    fun cancelRecording() {
        timerJob?.cancel()
        try {
            voiceRecordingManager.stopRecording()
        } catch (e: Exception) {
            android.util.Log.e("PronunciationVM", "cancelRecording exception: ${e.message}", e)
        }
        
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.widget.Toast.makeText(context, "已取消录音", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            recordingFilePath = null,
            recordingDurationMs = 0
        )
        recordingStartTime = 0
    }

    fun restart() {
        _uiState.value = PronunciationUiState(isLoading = true)
        loadAllWords()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecordingManager.release()
        mediaPlayer?.release()
    }
}
