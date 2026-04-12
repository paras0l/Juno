package com.juno.app.ui.screens.flashcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.PI
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPronunciation: (String?) -> Unit,
    onNavigateToWordList: () -> Unit,
    viewModel: FlashcardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.checkAndReloadIfNeeded()
    }

    Scaffold(
        // Minimal top bar replacement inside the Box later
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日背词",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigateToPronunciation(uiState.currentWord?.word) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "发音",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (uiState.totalWords > 0) {
                        Text(
                            text = "${uiState.currentIndex + 1}/${uiState.totalWords}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp) // Leave space for header
            ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.isComplete -> {
                    CompletionScreen(
                        rememberedCount = uiState.rememberedCount,
                        forgotCount = uiState.forgotCount,
                        totalWords = uiState.totalWords,
                        onRestart = { viewModel.restart() },
                        onNavigateBack = { viewModel.loadReviewWords() } // Map 'Review' button to loadReviewWords
                    )
                }
                uiState.words.isEmpty() -> {
                    EmptyStateScreen(onNavigateToWordList = onNavigateToWordList)
                }
                else -> {
                    SwipeableFlashcardContent(
                        uiState = uiState,
                        onFlipCard = { viewModel.flipCard() },
                        onRemembered = { viewModel.processRemembered() },
                        onForgot = { viewModel.processForgot() }
                    )
                }
            }
            }
        }
    }
}

class SquircleShape(private val n: Double = 4.0) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val a = w / 2.0
        val b = h / 2.0
        val points = 200
        for (i in 0..points) {
            val t = i * 2 * PI / points
            val x = a * abs(cos(t)).pow(2.0 / n) * sign(cos(t)) + a
            val y = b * abs(sin(t)).pow(2.0 / n) * sign(sin(t)) + b
            if (i == 0) path.moveTo(x.toFloat(), y.toFloat())
            else path.lineTo(x.toFloat(), y.toFloat())
        }
        path.close()
        return Outline.Generic(path)
    }
}

enum class InteractionState { FRONT, SPELLING, RESULT }

@Composable
private fun SwipeableFlashcardContent(
    uiState: FlashcardUiState,
    onFlipCard: () -> Unit,
    onRemembered: () -> Unit,
    onForgot: () -> Unit
) {
    var interactionState by remember(uiState.currentIndex) { mutableStateOf(InteractionState.FRONT) }
    var isRememberedResult by remember(uiState.currentIndex) { mutableStateOf(true) }
    var offsetX by remember(uiState.currentIndex) { mutableFloatStateOf(0f) }
    
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 150.dp.toPx() }
    val haptic = LocalHapticFeedback.current

    val animatedOffsetX by animateFloatAsState(
        targetValue = if (interactionState == InteractionState.FRONT) offsetX else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "swipeOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { uiState.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        val currentWord = uiState.currentWord ?: return

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = interactionState == InteractionState.RESULT,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400)) { it / 8 } + scaleIn(initialScale = 0.9f, animationSpec = tween(400)))
                            .togetherWith(fadeOut(tween(200)))
                    } else {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    }
                },
                label = "resultTransition"
            ) { isResultState ->
                if (!isResultState) {
                    val isSpelling = interactionState == InteractionState.SPELLING
                    val flipRotation by animateFloatAsState(
                        targetValue = if (isSpelling) 180f else 0f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        label = "flipAnimation"
                    )

                    Box(modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = flipRotation
                            cameraDistance = 12f * density.density
                        }
                    ) {
                        if (flipRotation <= 90f) {
                            val rotation = (animatedOffsetX / 30f).coerceIn(-15f, 15f)
                    val scale = 1f - (abs(animatedOffsetX) / swipeThreshold * 0.1f).coerceIn(0f, 0.1f)
                    val swipeIndicatorAlpha = (abs(animatedOffsetX) / swipeThreshold).coerceIn(0f, 1f)

                    if (swipeIndicatorAlpha > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (animatedOffsetX < 0) {
                                        Color(0xFF81C784).copy(alpha = swipeIndicatorAlpha * 0.4f) 
                                    } else {
                                        Color(0xFFE57373).copy(alpha = swipeIndicatorAlpha * 0.4f) 
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                )
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                            .graphicsLayer {
                                rotationZ = rotation
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                cameraDistance = 12f * density.density
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (offsetX < -swipeThreshold) {
                                            isRememberedResult = true
                                            interactionState = InteractionState.RESULT
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else if (offsetX > swipeThreshold) {
                                            isRememberedResult = false
                                            interactionState = InteractionState.RESULT
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        offsetX = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount.x
                                    }
                                )
                            }
                            .clickable {
                                interactionState = InteractionState.SPELLING
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentWord.word,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!currentWord.phonetic.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = currentWord.phonetic,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                        } else {
                            Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                                var spellingText by remember { mutableStateOf("") }
                                var isError by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { interactionState = InteractionState.FRONT }, 
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "点击空白处可翻回",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                             Text(
                                 text = currentWord.definitions ?: "暂无释义",
                                 style = MaterialTheme.typography.headlineSmall,
                                 textAlign = TextAlign.Center
                             )
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            OutlinedTextField(
                                value = spellingText,
                                onValueChange = { 
                                    spellingText = it
                                    isError = false 
                                },
                                isError = isError,
                                singleLine = true,
                                label = { Text("输入单词以完成拼写") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (spellingText.trim().equals(currentWord.word, ignoreCase = true)) {
                                            isRememberedResult = true
                                            interactionState = InteractionState.RESULT
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            isError = true
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            if (isError) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "拼写错误，请重试或点击空白处返回",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            Box(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(48.dp)
                                    .clip(SquircleShape(5.0))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = SquircleShape(5.0)
                                    )
                                    .clickable {
                                        if (spellingText.trim().equals(currentWord.word, ignoreCase = true)) {
                                            isRememberedResult = true
                                            interactionState = InteractionState.RESULT
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } else {
                                            isError = true
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                    .padding(horizontal = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "提交拼写",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = currentWord.word,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                currentWord.phonetic?.let { phonetic ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = phonetic,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "中文释义",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                 text = currentWord.definitions ?: "暂无释义",
                                 style = MaterialTheme.typography.titleLarge,
                                 fontWeight = FontWeight.Medium
                             )
                            
                             if (!currentWord.sentence.isNullOrEmpty()) {
                                 Spacer(modifier = Modifier.height(24.dp))
                                 Text(
                                     text = "例句",
                                     style = MaterialTheme.typography.labelMedium,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                                 Spacer(modifier = Modifier.height(8.dp))
                                 Text(
                                     text = currentWord.sentence,
                                     style = MaterialTheme.typography.bodyLarge,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                                     lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                 )
                             }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Box(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .align(Alignment.CenterHorizontally)
                                    .height(56.dp)
                                    .clip(SquircleShape(5.0))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        shape = SquircleShape(5.0)
                                    )
                                    .clickable {
                                        if (isRememberedResult) onRemembered() else onForgot()
                                    }
                                    .padding(horizontal = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "下一个单词",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (interactionState == InteractionState.FRONT) {
            Text(
                text = "← 左滑：认识 | 点击：拼写 | 右滑：不认识 →",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CompletionScreen(
    rememberedCount: Int,
    forgotCount: Int,
    totalWords: Int,
    onRestart: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Complete",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "太棒了!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "本次学习已完成",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = rememberedCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "记住了",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = forgotCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "没记住",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalWords.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "总计",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(SquircleShape(5.0))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.8f),
                        shape = SquircleShape(5.0)
                    )
                    .clickable { onRestart() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "再来一组",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(SquircleShape(5.0))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.8f),
                        shape = SquircleShape(5.0)
                    )
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "回顾",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateScreen(onNavigateToWordList: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "没有可学习的单词",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "请先添加一些单词",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(56.dp)
                .clip(SquircleShape(5.0))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.8f),
                    shape = SquircleShape(5.0)
                )
                .clickable { onNavigateToWordList() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "去单词库添加",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
