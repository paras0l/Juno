package com.juno.app.ui.screens.grammar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juno.app.data.local.entity.GrammarStageEntity

// Stage accent colors per spec appendix
private val stageColors = listOf(
    Color(0xFF4CAF50), // 阶段1 绿色
    Color(0xFF2196F3), // 阶段2 蓝色
    Color(0xFFFF9800), // 阶段3 橙色
    Color(0xFF9C27B0), // 阶段4 紫色
    Color(0xFFF44336), // 阶段5 红色
    Color(0xFFFFD700)  // 阶段6 金色
)

private val stageEmojis = listOf("📗", "📘", "📙", "📕", "📚", "🏆")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLesson: (Long) -> Unit,
    viewModel: GrammarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "语法学习",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "选择阶段开始闯关学习 🎯",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(uiState.stages) { index, stage ->
                    StageCard(
                        stage = stage,
                        color = stageColors.getOrElse(index) { Color(0xFF9E9E9E) },
                        emoji = stageEmojis.getOrElse(index) { "📖" },
                        animDelay = index * 80,
                        onClick = {
                            if (stage.isUnlocked) {
                                viewModel.loadLessonsForStage(stage.id)
                                // Navigate to first uncompleted lesson
                                onNavigateToLesson(getFirstLessonId(stage))
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

private fun getFirstLessonId(stage: GrammarStageEntity): Long {
    // Lesson IDs follow pattern: stageId*100 + order (e.g., stage 1 lesson 1 = 101)
    return stage.id * 100 + 1
}

@Composable
private fun StageCard(
    stage: GrammarStageEntity,
    color: Color,
    emoji: String,
    animDelay: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animDelay.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "stage_alpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else 30f,
        animationSpec = tween(400),
        label = "stage_translate"
    )

    val progress = if (stage.totalLessons > 0) {
        stage.completedLessons.toFloat() / stage.totalLessons
    } else 0f

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha; translationY = translateY }
            .shadow(
                elevation = if (stage.isUnlocked) 8.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (stage.isUnlocked) color.copy(alpha = 0.25f) else Color.Transparent,
                spotColor = if (stage.isUnlocked) color.copy(alpha = 0.25f) else Color.Transparent
            )
            .clickable(enabled = stage.isUnlocked) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (stage.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (stage.isUnlocked)
                                color.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (stage.isUnlocked) {
                        Text(text = emoji, fontSize = 26.sp)
                    } else {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "已锁定",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stage.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (stage.isUnlocked)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stage.level,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (stage.isUnlocked)
                            color.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stage.isUnlocked) {
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已完成 ${stage.completedLessons}/${stage.totalLessons} 关",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            } else {
                Text(
                    text = "完成上一阶段解锁",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
