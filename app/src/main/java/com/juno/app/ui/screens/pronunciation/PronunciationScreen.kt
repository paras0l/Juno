package com.juno.app.ui.screens.pronunciation

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreen(
    onNavigateBack: () -> Unit,
    targetWord: String? = null,
    viewModel: PronunciationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(targetWord) {
        viewModel.setTargetWord(targetWord)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发音练习") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isComplete -> {
                    CompletionContent(
                        averageScore = uiState.averageScore,
                        onRestart = { viewModel.restart() },
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 16.dp)
                    ) {
                        if (uiState.words.isNotEmpty()) {
                            SearchBar(
                                searchQuery = uiState.searchQuery,
                                onSearchQueryChange = { viewModel.searchWords(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        when {
                            uiState.searchQuery.isNotEmpty() -> {
                                if (uiState.filteredWords.isEmpty()) {
                                    EmptySearchContent(
                                        onNavigateBack = { viewModel.searchWords("") },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    WordListContent(
                                        words = uiState.filteredWords,
                                        onWordClick = { viewModel.selectWord(it) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            uiState.currentWord != null -> {
                                PronunciationContent(
                                    uiState = uiState,
                                    onPlayOriginal = { viewModel.playOriginal() },
                                    onPlayRecording = { viewModel.playRecording() },
                                    onStartRecording = { viewModel.startRecording() },
                                    onStopRecording = { viewModel.stopRecording() },
                                    onCancelRecording = { viewModel.cancelRecording() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            uiState.words.isEmpty() -> {
                                EmptyContent(
                                    onNavigateBack = onNavigateBack,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                WordListContent(
                                    words = uiState.filteredWords,
                                    onWordClick = { viewModel.selectWord(it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 2.dp
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "搜索单词...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun PronunciationContent(
    uiState: PronunciationUiState,
    onPlayOriginal: () -> Unit,
    onPlayRecording: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentWord = uiState.currentWord
    var isPressed by remember { mutableStateOf(false) }

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                isPressed = true
                onStartRecording()
                val success = tryAwaitRelease()
                isPressed = false
                if (success) {
                    onStopRecording()
                } else {
                    onCancelRecording()
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentWord?.word ?: "",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!currentWord?.phonetic.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentWord?.phonetic ?: "",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                 if (!currentWord?.definitions.isNullOrEmpty()) {
                     Spacer(modifier = Modifier.height(12.dp))
                     Text(
                         text = currentWord?.definitions ?: "",
                         fontSize = 14.sp,
                         textAlign = TextAlign.Center,
                         color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                     )
                 }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable(enabled = !uiState.isRecording) { onPlayOriginal() },
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                            ),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AudioWaveIcon(
                            isPlaying = uiState.isOriginalPlaying,
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "播放原声",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (uiState.recordingFilePath != null && !uiState.isRecording) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onPlayRecording() },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                                ),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AudioWaveIcon(
                                isPlaying = uiState.isRecordingPlaying,
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "回放录音",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        val buttonScale by animateFloatAsState(
            targetValue = if (isPressed) 1.15f else 1f,
            animationSpec = tween(150),
            label = "buttonScale"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .scale(buttonScale)
                .then(gestureModifier)
        ) {
            if (uiState.isRecording) {
                RecordingPulseAnimation()
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = if (uiState.isRecording) Color(0xFFF44336) else Color(0xFF2196F3),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (uiState.isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (uiState.isRecording) "Recording" else "Hold to Record",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        if (uiState.isRecording) {
            Spacer(modifier = Modifier.height(8.dp))

            val durationSeconds = (uiState.recordingDurationMs / 1000).toInt()
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "松开结束，上滑取消",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            Text(
                text = "按住录音，上滑取消",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CompletionContent(
    averageScore: Int,
    onRestart: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "练习完成!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(28.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "平均得分",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$averageScore%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable { onRestart() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "再来一组",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clickable { onNavigateBack() },
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                            ),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "返回",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "没有可练习的单词",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请先添加一些单词",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))
        Surface(
            modifier = Modifier
                .clickable { onNavigateBack() }
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("返回", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun EmptySearchContent(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "未找到匹配的单词",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "试试其他搜索词",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))
        Surface(
            modifier = Modifier
                .clickable { onNavigateBack() }
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("返回", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "出错了",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Surface(
            modifier = Modifier
                .clickable { onNavigateBack() }
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF44336), Color(0xFFE57373))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("返回", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun WordListContent(
    words: List<com.juno.app.data.local.entity.WordEntity>,
    onWordClick: (com.juno.app.data.local.entity.WordEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(words) { word ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWordClick(word) },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!word.phonetic.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = word.phonetic,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                         if (!word.definitions.isNullOrEmpty()) {
                             Spacer(modifier = Modifier.height(4.dp))
                             Text(
                                 text = word.definitions,
                                 fontSize = 13.sp,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                 maxLines = 1
                             )
                         }
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "练习",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioWaveIcon(
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                phase = (phase + 0.1f) % (2 * kotlin.math.PI.toFloat())
                kotlinx.coroutines.delay(50)
            }
        }
    }
    
    val barColor = if (isPlaying) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    
    Canvas(modifier = modifier) {
        val barWidth = size.width / 7
        val spacing = barWidth * 0.3f
        val maxHeight = size.height
        
        val heights = if (isPlaying) {
            listOf(
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 0.5f)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 1.0f)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 1.5f)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 2.0f)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 2.5f)),
                0.3f + 0.7f * kotlin.math.abs(kotlin.math.sin(phase + 3.0f))
            )
        } else {
            listOf(0.3f, 0.5f, 0.7f, 1.0f, 0.7f, 0.5f, 0.3f)
        }
        
        heights.forEachIndexed { index, fraction ->
            val h = (maxHeight * fraction.coerceIn(0.1f, 1f)).coerceAtLeast(3f)
            val x = index * (barWidth + spacing)
            drawRect(
                color = barColor.copy(alpha = fraction.coerceIn(0.3f, 1f)),
                topLeft = Offset(x, (maxHeight - h) / 2),
                size = Size(barWidth, h)
            )
        }
    }
}

@Composable
private fun RecordingPulseAnimation() {
    var pulsePhase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            pulsePhase = (pulsePhase + 0.05f) % 1f
            kotlinx.coroutines.delay(30)
        }
    }
    
    val scale = 1f + 0.5f * pulsePhase
    val alpha = 0.5f * (1f - pulsePhase)
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(
                    Color(0xFFF44336).copy(alpha = alpha),
                    CircleShape
                )
        )
    }
}
