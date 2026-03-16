package com.juno.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.juno.app.ui.screens.dashboard.DashboardScreen
import com.juno.app.ui.screens.flashcard.FlashcardScreen
import com.juno.app.ui.screens.ai.AiScreen

@Composable
fun MainScreen(

    onNavigateToReview: () -> Unit,
    onNavigateToStory: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTutorSelection: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFocusMode: () -> Unit,
    onNavigateToLearnedWords: () -> Unit,
    onNavigateToMasteredWords: () -> Unit,
    onNavigateToPronunciation: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                tonalElevation = 0.dp, // Flat appearance
                // Remove shadow for clean Morandi look
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                // Flashcards (Home)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Style,
                            contentDescription = "背单词"
                        )
                    },
                    // Label visually omitted if not selected for breathing room
                    label = { if (selectedTab == 0) Text("单词") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // AI Features
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI伴学"
                        )
                    },
                    label = { if (selectedTab == 1) Text("AI伴学") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Dashboard
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Dashboard,
                            contentDescription = "仪表盘"
                        )
                    },
                    label = { if (selectedTab == 2) Text("仪表盘") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> FlashcardScreen(
                    onNavigateBack = { /* No-op here since it's the root */ },
                    onNavigateToPronunciation = onNavigateToPronunciation,
                    onNavigateToWordList = onNavigateToWordList
                )
                1 -> AiScreen(
                    onNavigateToTutorSelection = onNavigateToTutorSelection,
                    onNavigateToStory = onNavigateToStory,
                    onNavigateToCamera = onNavigateToCamera
                )
                2 -> DashboardScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToWordList = onNavigateToWordList,
                    onNavigateToReview = onNavigateToReview,
                    onNavigateToFocusMode = onNavigateToFocusMode,
                    onNavigateToLearnedWords = onNavigateToLearnedWords,
                    onNavigateToMasteredWords = onNavigateToMasteredWords
                )
            }
        }
    }
}
