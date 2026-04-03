package com.juno.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Flashcard : Screen("flashcard")
    data object WordList : Screen("word_list?filter={filter}") {
        fun createRoute(filter: String? = null): String {
            return if (filter != null) "word_list?filter=$filter" else "word_list?filter="
        }
    }
    data object AddWord : Screen("add_word")
    data object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Long) = "edit_word/$wordId"
    }
    data object Review : Screen("review")
    data object Story : Screen("story")
    data object StoryDetail : Screen("story_detail/{storyId}") {
        fun createRoute(storyId: Long) = "story_detail/$storyId"
    }
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object Pronunciation : Screen("pronunciation?word={word}") {
        fun createRoute(word: String? = null): String {
            return if (word != null) "pronunciation?word=$word" else "pronunciation"
        }
    }
    data object TutorSelection : Screen("tutor_selection")
    data object Chat : Screen("chat/{tutorId}") {
        fun createRoute(tutorId: String) = "chat/$tutorId"
    }
    data object Camera : Screen("camera")
    data object AnchorResult : Screen("anchor_result")
    data object FocusMode : Screen("focus_mode")
    data object PermissionGuide : Screen("permission_guide")
    data object OcrHistory : Screen("ocr_history")
}
