package com.juno.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juno.app.ui.theme.AccentBlueEnd
import com.juno.app.ui.theme.AccentBlueStart
import com.juno.app.ui.theme.AccentOrangeEnd
import com.juno.app.ui.theme.AccentOrangeStart

private val CardShape = RoundedCornerShape(24.dp)
private val SmallCardShape = RoundedCornerShape(20.dp)

@Composable
fun HomeScreen(
    onNavigateToFlashcard: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToStory: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTutorSelection: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFocusMode: () -> Unit,
    onNavigateToLearnedWords: () -> Unit,
    onNavigateToMasteredWords: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FF),
                        Color(0xFFF0F4FF)
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    GreetingSection(
                        streak = uiState.currentStreak,
                        wordsLearnedToday = uiState.wordsLearnedToday,
                        dailyGoal = uiState.dailyGoal,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }

                item {
                    DailyProgressCard(
                        wordsLearnedToday = uiState.wordsLearnedToday,
                        dailyGoal = uiState.dailyGoal
                    )
                }

                item {
                    QuickActionsSection(
                        dueReviewsCount = uiState.dueReviewsCount,
                        onNavigateToFlashcard = onNavigateToFlashcard,
                        onNavigateToReview = onNavigateToReview,
                        onNavigateToCamera = onNavigateToCamera,
                        onNavigateToFocusMode = onNavigateToFocusMode
                    )
                }

                item {
                    StoryCard(onNavigateToStory = onNavigateToStory)
                }

                item {
                    TutorCard(onNavigateToTutorSelection = onNavigateToTutorSelection)
                }

                item {
                    WordListQuickAccess(onNavigateToWordList = onNavigateToWordList)
                }

                item {
                    StatsOverview(
                        totalWordsLearned = uiState.totalWordsLearned,
                        masteredWords = uiState.masteredWords,
                        wordsReviewedToday = uiState.wordsReviewedToday,
                        onLearnedWordsClick = onNavigateToLearnedWords,
                        onMasteredWordsClick = onNavigateToMasteredWords,
                        onReviewedTodayClick = onNavigateToReview
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(
    @Suppress("UNUSED_PARAMETER") streak: Int,
    wordsLearnedToday: Int,
    dailyGoal: Int,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "欢迎回来!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (wordsLearnedToday >= dailyGoal) "今日目标已完成!" else "今日已学习 $wordsLearnedToday / $dailyGoal 个单词",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun DailyProgressCard(
    wordsLearnedToday: Int,
    dailyGoal: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "$wordsLearnedToday / $dailyGoal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (wordsLearnedToday.toFloat() / dailyGoal).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (wordsLearnedToday >= dailyGoal) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFF2196F3)
                },
                trackColor = Color(0xFFE8EEFF)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    dueReviewsCount: Int,
    onNavigateToFlashcard: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFocusMode: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.School,
                title = "背单词",
                subtitle = "学习新单词",
                gradientColors = listOf(AccentOrangeStart, AccentOrangeEnd),
                onClick = onNavigateToFlashcard
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Refresh,
                title = "复习",
                subtitle = if (dueReviewsCount > 0) "$dueReviewsCount 待复习" else "暂无复习",
                gradientColors = listOf(AccentBlueStart, AccentBlueEnd),
                onClick = onNavigateToReview
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToFocusMode,
                shape = SmallCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Focus Mode",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "专注模式",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "番茄钟",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCamera,
                shape = SmallCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Visual Anchor",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "视觉锚定",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "拍照学单词",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = SmallCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = gradientColors.map { it.copy(alpha = 0.1f) })
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(colors = gradientColors),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun StoryCard(onNavigateToStory: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToStory),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9C27B0).copy(alpha = 0.1f),
                            Color(0xFF673AB7).copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF9C27B0), Color(0xFF673AB7))),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = "AI Story",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI 故事阅读",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "通过有趣的故事学习英语",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
            Button(
                onClick = onNavigateToStory,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start"
                )
            }
        }
    }
}

@Composable
private fun WordListQuickAccess(onNavigateToWordList: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToWordList),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = "Word List",
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "单词本",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "查看全部 >",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun StatsOverview(
    totalWordsLearned: Int,
    masteredWords: Int,
    wordsReviewedToday: Int,
    onLearnedWordsClick: () -> Unit,
    onMasteredWordsClick: () -> Unit,
    onReviewedTodayClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "学习统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    value = totalWordsLearned.toString(),
                    label = "已学单词",
                    valueColor = Color(0xFF2196F3),
                    onClick = onLearnedWordsClick
                )
                StatItem(
                    icon = Icons.Default.School,
                    value = masteredWords.toString(),
                    label = "已掌握",
                    valueColor = Color(0xFF4CAF50),
                    onClick = onMasteredWordsClick
                )
                StatItem(
                    icon = Icons.Default.Refresh,
                    value = wordsReviewedToday.toString(),
                    label = "今日复习",
                    valueColor = Color(0xFF9C27B0),
                    onClick = onReviewedTodayClick
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    valueColor: Color = Color(0xFF2196F3),
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(12.dp)
        } else {
            Modifier.padding(12.dp)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = valueColor,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF888888)
        )
    }
}

@Composable
private fun TutorCard(onNavigateToTutorSelection: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToTutorSelection),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.85f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2196F3).copy(alpha = 0.1f),
                            Color(0xFF03A9F4).copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "AI Tutor",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI 导师对话",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = "与AI导师聊天学习英语",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
            FilledTonalButton(
                onClick = onNavigateToTutorSelection,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start"
                )
            }
        }
    }
}