package com.juno.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "juno_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val DAILY_GOAL = intPreferencesKey("daily_goal")
        private val STORY_STYLE = stringPreferencesKey("story_style")
        private val DIFFICULTY_LEVEL = intPreferencesKey("difficulty_level")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val REVIEW_REMINDER_TIME = stringPreferencesKey("review_reminder_time")
        private val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        private val AUTO_PLAY_AUDIO = booleanPreferencesKey("auto_play_audio")
        private val SHOW_PHONETICS = booleanPreferencesKey("show_phonetics")
        private val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        private val LEARNING_MODE = stringPreferencesKey("learning_mode")
        private val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        private val USER_ID = stringPreferencesKey("user_id")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val FOCUS_MODE_DURATION = intPreferencesKey("focus_mode_duration")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val FLOAT_WINDOW_X = intPreferencesKey("float_window_x")
        private val FLOAT_WINDOW_Y = intPreferencesKey("float_window_y")
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "light"
    }

    val focusModeDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FOCUS_MODE_DURATION] ?: 25
    }

    val dailyGoal: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_GOAL] ?: 10
    }

    val storyStyle: Flow<String> = dataStore.data.map { preferences ->
        preferences[STORY_STYLE] ?: "adventure"
    }

    val difficultyLevel: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DIFFICULTY_LEVEL] ?: 1
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val reviewReminderTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[REVIEW_REMINDER_TIME] ?: "09:00"
    }

    val soundEffectsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_EFFECTS_ENABLED] ?: true
    }

    val autoPlayAudio: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_PLAY_AUDIO] ?: false
    }

    val showPhonetics: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_PHONETICS] ?: true
    }

    val showTranslation: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_TRANSLATION] ?: true
    }

    val learningMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[LEARNING_MODE] ?: "spaced_repetition"
    }

    val lastSyncTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: ""
    }

    val userId: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setDailyGoal(goal: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_GOAL] = goal
        }
    }

    suspend fun setStoryStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[STORY_STYLE] = style
        }
    }

    suspend fun setDifficultyLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[DIFFICULTY_LEVEL] = level
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setReviewReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[REVIEW_REMINDER_TIME] = time
        }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setAutoPlayAudio(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PLAY_AUDIO] = enabled
        }
    }

    suspend fun setShowPhonetics(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PHONETICS] = show
        }
    }

    suspend fun setShowTranslation(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_TRANSLATION] = show
        }
    }

    suspend fun setLearningMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[LEARNING_MODE] = mode
        }
    }

    suspend fun setLastSyncTime(time: String) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = time
        }
    }

    suspend fun setUserId(id: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = id
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setFocusModeDuration(duration: Int) {
        dataStore.edit { preferences ->
            preferences[FOCUS_MODE_DURATION] = duration
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val floatWindowX: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FLOAT_WINDOW_X] ?: 100
    }

    val floatWindowY: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FLOAT_WINDOW_Y] ?: 200
    }

    suspend fun setFloatWindowPosition(x: Int, y: Int) {
        dataStore.edit { preferences ->
            preferences[FLOAT_WINDOW_X] = x
            preferences[FLOAT_WINDOW_Y] = y
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
