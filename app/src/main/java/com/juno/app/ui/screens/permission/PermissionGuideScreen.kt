package com.juno.app.ui.screens.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.juno.app.data.local.PreferencesManager
import kotlinx.coroutines.launch

data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val permission: String?,       // null for special permissions
    val isSpecial: Boolean = false, // e.g., SYSTEM_ALERT_WINDOW
    val isOptional: Boolean = false
)

@Composable
fun PermissionGuideScreen(
    preferencesManager: PreferencesManager,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionItems = remember {
        buildList {
            add(
                PermissionItem(
                    icon = Icons.Default.Mic,
                    title = "麦克风权限",
                    description = "用于发音练习和语音评测，AI 会倾听你的发音并给出反馈",
                    permission = Manifest.permission.RECORD_AUDIO
                )
            )
            add(
                PermissionItem(
                    icon = Icons.Default.CameraAlt,
                    title = "相机权限",
                    description = "用于「实景视觉锚定」功能，拍摄物品后 AI 帮你标注英文标签",
                    permission = Manifest.permission.CAMERA
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionItem(
                        icon = Icons.Default.Notifications,
                        title = "通知权限",
                        description = "用于复习提醒和学习打卡通知，帮助你坚持学习习惯",
                        permission = Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }
            add(
                PermissionItem(
                    icon = Icons.Default.Layers,
                    title = "悬浮窗权限",
                    description = "用于 OCR 屏幕取词功能，在任何 App 中即刻查词收藏",
                    permission = null,
                    isSpecial = true,
                    isOptional = true
                )
            )
        }
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = permissionItems.size

    // Track permission states
    var permissionStates by remember {
        mutableStateOf(permissionItems.map { item ->
            if (item.isSpecial) {
                Settings.canDrawOverlays(context)
            } else if (item.permission != null) {
                ContextCompat.checkSelfPermission(context, item.permission) ==
                        PackageManager.PERMISSION_GRANTED
            } else false
        })
    }

    // Refresh states when returning from settings
    fun refreshPermissionStates() {
        permissionStates = permissionItems.map { item ->
            if (item.isSpecial) {
                Settings.canDrawOverlays(context)
            } else if (item.permission != null) {
                ContextCompat.checkSelfPermission(context, item.permission) ==
                        PackageManager.PERMISSION_GRANTED
            } else false
        }
    }

    val currentItem = permissionItems.getOrNull(currentStep)
    val progress by animateFloatAsState(
        targetValue = (currentStep.toFloat() + 1) / totalSteps,
        animationSpec = tween(400),
        label = "progress"
    )

    // Permission launcher for runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshPermissionStates()
        if (currentStep < totalSteps - 1) {
            currentStep++
        } else {
            scope.launch {
                preferencesManager.setOnboardingCompleted(true)
                onComplete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Text(
                text = "👋",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "欢迎来到 Juno",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "为了给你最好的学习体验\n我们需要以下权限",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "第 ${currentStep + 1} 步 / 共 $totalSteps 步",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${((currentStep.toFloat() + 1) / totalSteps * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Current permission card
            if (currentItem != null) {
                val isGranted = permissionStates.getOrNull(currentStep) ?: false

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300))
                ) {
                    PermissionCard(
                        item = currentItem,
                        isGranted = isGranted
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                if (isGranted) {
                    // Already granted
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "已授权",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (currentStep < totalSteps - 1) {
                                currentStep++
                            } else {
                                scope.launch {
                                    preferencesManager.setOnboardingCompleted(true)
                                    onComplete()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentStep < totalSteps - 1) "下一步" else "开始学习 🚀",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    // Need to request
                    Button(
                        onClick = {
                            if (currentItem.isSpecial) {
                                // Open overlay permission settings
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else if (currentItem.permission != null) {
                                permissionLauncher.launch(currentItem.permission)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentItem.isSpecial) "前往设置开启" else "授予权限",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (currentItem.isOptional) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                if (currentStep < totalSteps - 1) {
                                    currentStep++
                                } else {
                                    scope.launch {
                                        preferencesManager.setOnboardingCompleted(true)
                                        onComplete()
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "暂时跳过",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permission overview dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                permissionItems.forEachIndexed { index, _ ->
                    val dotGranted = permissionStates.getOrNull(index) ?: false
                    val isCurrent = index == currentStep

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isCurrent) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    dotGranted -> Color(0xFF4CAF50)
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skip all button
            TextButton(
                onClick = {
                    scope.launch {
                        preferencesManager.setOnboardingCompleted(true)
                        onComplete()
                    }
                }
            ) {
                Text(
                    text = "跳过全部，稍后在设置中开启",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Refresh on resume (e.g., after returning from Settings)
    LaunchedEffect(currentStep) {
        refreshPermissionStates()
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItem,
    isGranted: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(36.dp),
                    tint = if (isGranted) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (item.isOptional) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "可选",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
