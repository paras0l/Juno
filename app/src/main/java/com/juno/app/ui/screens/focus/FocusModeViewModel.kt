package com.juno.app.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusModeUiState(
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0
)

@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusModeUiState())
    val uiState: StateFlow<FocusModeUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            preferencesManager.focusModeDuration.collect { _ ->
            }
        }
    }

    fun saveFocusSession(durationMinutes: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                totalSessions = currentState.totalSessions + 1,
                totalMinutes = currentState.totalMinutes + durationMinutes
            )
        }
    }
}
