package com.juno.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.juno.app.ui.screens.addword.AddWordScreen
import com.juno.app.ui.screens.anchor.AnchorResultScreen
import com.juno.app.ui.screens.camera.CameraScreen
import com.juno.app.ui.screens.chat.ChatScreen
import com.juno.app.ui.screens.flashcard.FlashcardScreen
import com.juno.app.ui.screens.focus.FocusModeScreen
import com.juno.app.ui.screens.home.HomeScreen
import com.juno.app.ui.screens.profile.ProfileScreen
import com.juno.app.ui.screens.pronunciation.PronunciationScreen
import com.juno.app.ui.screens.review.ReviewScreen
import com.juno.app.ui.screens.settings.SettingsScreen
import com.juno.app.ui.screens.story.StoryDetailScreen
import com.juno.app.ui.screens.story.StoryScreen
import com.juno.app.ui.screens.tutor.TutorSelectionScreen
import com.juno.app.ui.screens.wordlist.WordListScreen

@Composable
fun JunoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFlashcard = { navController.navigate(Screen.Flashcard.route) },
                onNavigateToReview = { navController.navigate(Screen.Review.route) },
                onNavigateToStory = { navController.navigate(Screen.Story.route) },
                onNavigateToWordList = { navController.navigate(Screen.WordList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTutorSelection = { navController.navigate(Screen.TutorSelection.route) },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToFocusMode = { navController.navigate(Screen.FocusMode.route) }
            )
        }

        composable(Screen.Flashcard.route) {
            FlashcardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPronunciation = { navController.navigate(Screen.Pronunciation.route) }
            )
        }

        composable(Screen.WordList.route) {
            WordListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddWord = { navController.navigate(Screen.AddWord.route) },
                onNavigateToEditWord = { wordId ->
                    navController.navigate(Screen.EditWord.createRoute(wordId))
                }
            )
        }

        composable(Screen.AddWord.route) {
            AddWordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditWord.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) {
            AddWordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Story.route) {
            StoryScreen(
                onNavigateToStoryDetail = { storyId ->
                    navController.navigate(Screen.StoryDetail.createRoute(storyId))
                }
            )
        }

        composable(
            route = Screen.StoryDetail.route,
            arguments = listOf(navArgument("storyId") { type = NavType.LongType })
        ) {
            StoryDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Pronunciation.route) {
            PronunciationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TutorSelection.route) {
            TutorSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onTutorSelected = { tutorId ->
                    navController.navigate(Screen.Chat.createRoute(tutorId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId") ?: return@composable
            ChatScreen(
                tutorId = tutorId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = {
                    navController.navigate(Screen.AnchorResult.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AnchorResult.route) {
            AnchorResultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.FocusMode.route) {
            FocusModeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
