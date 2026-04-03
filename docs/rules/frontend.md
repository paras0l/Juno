# Rule 1: 容器的“呼吸感”与大圆角 (The "Bento" Grid)
不要使用锋利的直角或小圆角。界面应该由一系列具有物理厚度感的卡片组成。

核心参数：RoundedCornerShape 统一设定在 20dp 到 28dp 之间。

Compose 实现：

Kotlin

Surface(
shape = RoundedCornerShape(24.dp),
color = Color.White.copy(alpha = 0.8f), // 配合背景实现通透感
tonalElevation = 2.dp,
modifier = Modifier.padding(16.dp).fillMaxWidth()
) {
// 内容
}
Juno 应用：单词卡片、学习进度模块、AI 对话气泡都应采用这种大圆角设计。

# Rule 2: 弥散光阴影与玻璃拟态 (Depth & Glassmorphism)
放弃传统的纯黑硬边缘阴影，转而使用带有色彩倾向的弥散阴影。

设计要点：

背景使用柔和的浅色渐变（如浅紫到浅蓝）。

卡片使用半透明白色，并叠加 Modifier.blur()（Android 12+ 支持）。

Compose 技巧：
利用 GraphicsLayer 实现细腻的投影，或者自定义 Modifier 绘制多层阴影来模拟图片中的立体感。

Kotlin

Modifier.graphicsLayer {
shadowElevation = 8f
shape = RoundedCornerShape(24.dp)
clip = true
}.background(Brush.verticalGradient(...))
# Rule 3: 极简的视觉层级 (Visual Hierarchy)
图片中文字不多，但“一眼就能看出重点”。

字号对比：标题使用 FontWeight.Bold 且字号较大（如 20sp），次要信息（如时间、分值）使用中性灰（Color.Gray）且字号较小（12sp-14sp）。

图标点缀：在关键位置使用 3D 质感图标。

Juno 应用：在“每日单词”界面，单词本身要极大、极黑；而音标和释义则可以稍微淡化。

# Rule 4: 渐变色作为功能导向 (Accent Gradients)
图片中橙色和紫色的渐变并不是乱用的，它们起到了“视觉锚点”的作用。

配色逻辑：

主色调：纯白 + 极浅灰（背景）。

强调色：使用具有活力的渐变色（例如：#FF9D42 到 #FF6B00 用于“开始学习”按钮）。

Compose 实现：

Kotlin

val activeGradient = Brush.linearGradient(
colors = listOf(Color(0xFFFF9D42), Color(0xFFFF6B00))
)
Box(modifier = Modifier.background(activeGradient))
# Rule 5: 状态反馈的灵动感 (Micro-interactions)
图片右上角的紫色随机按钮带有一种“动态感”，这暗示了交互。

交互准则：在 Compose 中大量使用 animateContentSize() 和 animateFloatAsState。

Juno 应用：当用户完成一个英语任务时，卡片不应该只是消失，而是应该伴随着轻微的缩放弹簧动画（Spring Animation）和阴影颜色的变化。