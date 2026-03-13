package com.juno.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.remote.AiChatService
import com.juno.app.data.remote.model.ChatMessage
import com.juno.app.data.remote.model.TutorProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val currentTutor: TutorProfile? = null,
    val learnedWords: List<String> = emptyList(),
    val userLevel: Int = 1,
    val error: String? = null,
    val availableTutors: List<TutorProfile> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiChatService: AiChatService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentTutor = MutableStateFlow<TutorProfile?>(null)
    val currentTutor: StateFlow<TutorProfile?> = _currentTutor.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _learnedWords = MutableStateFlow<List<String>>(emptyList())
    val learnedWords: StateFlow<List<String>> = _learnedWords.asStateFlow()

    private val _userLevel = MutableStateFlow(1)
    val userLevel: StateFlow<Int> = _userLevel.asStateFlow()

    val availableTutors: StateFlow<List<TutorProfile>> = aiChatService.getAllTutors()
        .let { tutors ->
            MutableStateFlow(tutors)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun selectTutor(tutorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tutor = aiChatService.getTutorById(tutorId)
                _currentTutor.value = tutor
                
                if (_messages.value.isEmpty()) {
                    tutor?.let {
                        val greeting = aiChatService.createGreetingMessage(it.id)
                        _messages.value = listOf(greeting)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to select tutor"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            content = content,
            isFromUser = true
        )
        
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = aiChatService.processUserResponse(
                    message = content,
                    context = _messages.value
                )
                
                val tutorMessage = ChatMessage(
                    content = response,
                    isFromUser = false
                )
                
                _messages.value = _messages.value + tutorMessage
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to get response"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentTutor = _currentTutor.value
                if (currentTutor != null && _messages.value.isEmpty()) {
                    val greeting = aiChatService.createGreetingMessage(currentTutor.id)
                    _messages.value = listOf(greeting)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateProactiveQuestion() {
        viewModelScope.launch {
            val tutor = _currentTutor.value ?: return@launch
            
            val question = aiChatService.generateProactiveQuestion(
                tutorId = tutor.id,
                learnedWords = _learnedWords.value,
                userLevel = _userLevel.value
            )
            
            val questionMessage = ChatMessage(
                content = question,
                isFromUser = false
            )
            
            _messages.value = _messages.value + questionMessage
        }
    }

    fun updateLearnedWords(words: List<String>) {
        _learnedWords.value = words
    }

    fun updateUserLevel(level: Int) {
        _userLevel.value = level
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = emptyList()
        _currentTutor.value?.let {
            val greeting = aiChatService.createGreetingMessage(it.id)
            _messages.value = listOf(greeting)
        }
    }
}
