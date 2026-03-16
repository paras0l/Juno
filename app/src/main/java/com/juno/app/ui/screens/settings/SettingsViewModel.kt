package com.juno.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.PreferencesManager
import com.juno.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val darkMode: Boolean = false,
    val themeMode: String = "light",
    val notificationsEnabled: Boolean = true,
    val dailyGoal: Int = 10,
    val storyStyle: String = "adventure",
    val difficulty: Int = 1,
    val soundEffectsEnabled: Boolean = true,
    val showPhonetics: Boolean = true,
    val showTranslation: Boolean = true
) {
    val isDarkMode: Boolean
        get() = themeMode == "dark"
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val appearanceFlow = combine(
                preferencesManager.darkMode,
                preferencesManager.themeMode,
                preferencesManager.soundEffectsEnabled
            ) { darkMode, themeMode, soundEffects ->
                Triple(darkMode, themeMode, soundEffects)
            }

            val learningFlow = combine(
                preferencesManager.notificationsEnabled,
                preferencesManager.dailyGoal,
                preferencesManager.storyStyle,
                preferencesManager.difficultyLevel
            ) { notifications, dailyGoal, style, difficulty ->
                Quad(notifications, dailyGoal, style, difficulty)
            }

            val displayFlow = combine(
                preferencesManager.showPhonetics,
                preferencesManager.showTranslation
            ) { phonetics, translation ->
                Pair(phonetics, translation)
            }

            combine(appearanceFlow, learningFlow, displayFlow) { appearance, learning, display ->
                SettingsUiState(
                    isLoading = false,
                    darkMode = appearance.first,
                    themeMode = appearance.second,
                    notificationsEnabled = learning.n1,
                    dailyGoal = learning.n2,
                    storyStyle = learning.s1,
                    difficulty = learning.n3,
                    soundEffectsEnabled = appearance.third,
                    showPhonetics = display.first,
                    showTranslation = display.second
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SettingsUiState()
            ).collect { state ->
                _uiState.value = state
            }
        }
    }

    private data class Triple<A, B, C>(val first: A, val second: B, val third: C)

    private data class Quad<A, B, C, D>(val n1: A, val n2: B, val s1: C, val n3: D)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(if (enabled) "dark" else "light")
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            preferencesManager.setDailyGoal(goal)
            userRepository.updateDailyGoal(goal)
        }
    }

    fun setStoryStyle(style: String) {
        viewModelScope.launch {
            preferencesManager.setStoryStyle(style)
        }
    }

    fun setDifficulty(level: Int) {
        viewModelScope.launch {
            preferencesManager.setDifficultyLevel(level)
        }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEffectsEnabled(enabled)
        }
    }

    fun setShowPhonetics(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowPhonetics(show)
        }
    }

    fun setShowTranslation(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowTranslation(show)
        }
    }
}
