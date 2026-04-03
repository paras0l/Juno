package com.juno.app.ui.screens.ai

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juno.app.service.FloatingWindowService

@Composable
fun AiScreen(
    onNavigateToTutorSelection: () -> Unit,
    onNavigateToStory: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToOcrHistory: () -> Unit   // ← new: navigate to OCR history page
) {
    val context = LocalContext.current

    // NOTE: OCR callback is registered by OcrHistoryScreen (DisposableEffect).
    // AiScreen only starts the service and navigates — no dialog needed here.

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "AI 伴学",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "通过情境对话、图像识别和生成故事，让单词在脑海中生根发芽。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AiFeatureCard(
                title = "追问式外教",
                description = "拒绝「单向反馈」，AI导师会根据你刚背的单词主动发问，打破孤岛效应。",
                icon = Icons.AutoMirrored.Outlined.Chat,
                onClick = onNavigateToTutorSelection
            )
        }

        item {
            AiFeatureCard(
                title = "情境重现",
                description = "拍下书桌或窗外，AI 会把生词编进连贯冷笑话中。",
                icon = Icons.Outlined.CameraAlt,
                onClick = onNavigateToCamera
            )
        }

        item {
            AiFeatureCard(
                title = "全局悬浮取词",
                description = "开启悬浮球后，在任何 App 或网页随时识别文字，识别结果保存到取词记录。",
                icon = Icons.Outlined.PictureInPicture,
                onClick = {
                    // 1. Start the floating window service
                    val serviceIntent = Intent(context, FloatingWindowService::class.java).apply {
                        action = FloatingWindowService.ACTION_START
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                    // 2. Navigate to history page so user can see results
                    onNavigateToOcrHistory()
                }
            )
        }

        item {
            AiFeatureCard(
                title = "单词故事",
                description = "将难记的单词串联成荒诞有趣的小故事，图像化记忆。",
                icon = Icons.Outlined.AutoStories,
                onClick = onNavigateToStory
            )
        }
    }
}

@Composable
private fun AiFeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
