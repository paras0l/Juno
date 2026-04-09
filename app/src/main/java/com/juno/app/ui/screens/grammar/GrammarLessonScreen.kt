package com.juno.app.ui.screens.grammar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mikepenz.markdown.m3.Markdown
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarLessonScreen(
    lessonId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToNextLesson: (Long) -> Unit,
    viewModel: GrammarLessonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.lesson != null) {
                        Column {
                            Text(
                                uiState.lesson!!.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1
                            )
                        }
                    }
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
            return@Scaffold
        }

        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                (fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400)) { it / 8 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "lesson_step"
        ) { step ->
            when (step) {
                LessonStep.CONTENT -> ContentStep(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues),
                    onProceed = viewModel::proceedToExercises
                )
                LessonStep.EXERCISES -> ExercisesStep(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues),
                    onSelectAnswer = viewModel::selectAnswer,
                    onSubmit = viewModel::submitLesson
                )
                LessonStep.RESULT -> ResultStep(
                    uiState = uiState,
                    modifier = Modifier.padding(paddingValues),
                    onRetry = viewModel::retryLesson,
                    onNextLesson = {
                        val nextId = uiState.nextLessonId
                        if (nextId != null) {
                            onNavigateToNextLesson(nextId)
                        } else {
                            onNavigateBack()
                        }
                    },
                    onBack = onNavigateBack
                )
            }
        }
    }
}

// ─── Content Step ───────────────────────────────────────────────────────────

@Composable
private fun ContentStep(
    uiState: GrammarLessonUiState,
    modifier: Modifier,
    onProceed: () -> Unit
) {
    val lesson = uiState.lesson ?: return
    val examples = parseExamples(lesson.examples)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Section: 概念讲解
        SectionHeader(title = "概念讲解")
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                Markdown(
                    content = lesson.content,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: 例句
        if (examples.isNotEmpty()) {
            SectionHeader(title = "例句")
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    examples.forEachIndexed { index, (en, zh) ->
                        if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6))
                                    .padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = en,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = zh,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // CTA Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFF9D42), Color(0xFFFF6B00))
                    )
                )
        ) {
            Button(
                onClick = onProceed,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "检查学习成果",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Exercises Step ──────────────────────────────────────────────────────────

@Composable
private fun ExercisesStep(
    uiState: GrammarLessonUiState,
    modifier: Modifier,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmit: () -> Unit
) {
    val exercises = uiState.exercises
    val answers = uiState.answers
    val allAnswered = exercises.isNotEmpty() && exercises.all { answers.containsKey(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SectionHeader(title = "练习题")
        Text(
            text = "共 ${exercises.size} 题，全部作答后提交",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            exercises.forEach { ex ->
                val answered = answers.containsKey(ex.id)
                Box(
                    modifier = Modifier
                        .size(if (answered) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (answered) Color(0xFF8B5CF6)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                )
            }
        }

        exercises.forEachIndexed { index, exercise ->
            ExerciseCard(
                index = index + 1,
                exercise = exercise,
                selectedOption = answers[exercise.id],
                onSelect = { option -> onSelectAnswer(exercise.id, option) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (allAnswered)
                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                    else
                        Brush.linearGradient(listOf(Color(0xFFCBBDEF), Color(0xFFCBBDEF)))
                )
        ) {
            Button(
                onClick = onSubmit,
                enabled = allAnswered,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "提交答案",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExerciseCard(
    index: Int,
    exercise: Exercise,
    selectedOption: Int?,
    onSelect: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF8B5CF6)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = exercise.question,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            exercise.options.forEachIndexed { optIndex, option ->
                val isSelected = selectedOption == optIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) Color(0xFF8B5CF6)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(optIndex) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF8B5CF6)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color(0xFF8B5CF6)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color(0xFF6D28D9)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (optIndex < exercise.options.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ─── Result Step ─────────────────────────────────────────────────────────────

@Composable
private fun ResultStep(
    uiState: GrammarLessonUiState,
    modifier: Modifier,
    onRetry: () -> Unit,
    onNextLesson: () -> Unit,
    onBack: () -> Unit
) {
    val result = uiState.result ?: return
    val exercises = uiState.exercises
    val passed = result.scoreRate >= 0.6f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Score card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (passed)
                                listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                            else
                                listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                        )
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (passed) "🎉" else "💪",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (passed) "太棒了！" else "继续加油！",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "正确率 ${result.correct}/${result.total}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Score progress
                    LinearProgressIndicator(
                        progress = { result.scoreRate },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Detailed results
        SectionHeader(title = "题目解析")
        Spacer(modifier = Modifier.height(12.dp))

        exercises.forEach { exercise ->
            val userAnswer = result.answers[exercise.id]
            val correctAnswer = exercise.answer
            val isCorrect = userAnswer == correctAnswer

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isCorrect) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exercise.question,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isCorrect) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "你的答案：${exercise.options.getOrElse(userAnswer ?: -1) { "未作答" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "正确答案：${exercise.options.getOrElse(correctAnswer) { "" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF22C55E)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCorrect)
                                    Color(0xFF22C55E).copy(alpha = 0.08f)
                                else
                                    Color(0xFFEF4444).copy(alpha = 0.08f)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 ${exercise.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("重新学习", fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                    )
            ) {
                Button(
                    onClick = if (uiState.nextLessonId != null) onNextLesson else onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = if (uiState.nextLessonId != null) "下一关" else "完成",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun parseExamples(examplesJson: String): List<Pair<String, String>> {
    return try {
        val arr = JSONArray(examplesJson)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            obj.getString("en") to obj.getString("zh")
        }
    } catch (e: Exception) {
        emptyList()
    }
}
