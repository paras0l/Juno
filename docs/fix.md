# Juno v1.0 调试与修复清单

## 一、测试清单 (Todo List)

### P0 - 核心功能 (必须测试)

| # | 功能模块 | 测试要点 | 优先级 |
|---|----------|----------|--------|
| 1 | 单词卡学习 | 翻转动画、滑动手势、触觉反馈、学习进度保存 | 🔴 高 |
| 2 | 复习功能 | SRS算法触发、间隔计算、复习提醒 | 🔴 高 |
| 3 | 词表管理 | 增删改查、搜索、分类筛选 | 🔴 高 |
| 4 | Excel导入 | 批量导入、格式验证、重复处理 | 🔴 高 |

### P1 - 主要功能

| # | 功能模块 | 测试要点 | 优先级 |
|---|----------|----------|--------|
| 5 | AI导师对话 | 导师选择、消息发送、回复显示 | 🟡 中 |
| 6 | 视觉锚定 | 相机拍照、图像识别、单词显示 | 🟡 中 |
| 7 | 主题切换 | 4种主题切换、持久化、动态生效 | 🟡 中 |
| 8 | 专注模式 | 计时器、暂停恢复、统计保存 | 🟡 中 |
| 9 | TTS语音 | 自动播放、音色选择、音量控制 | 🟡 中 |

### P2 - 辅助功能

| # | 功能模块 | 测试要点 | 优先级 |
|---|----------|----------|--------|
| 10 | 发音评测 | 录音、对比、打分显示 | 🟢 低 |
| 11 | 链接解析 | URL识别、内容提取、词汇提取 | 🟢 低 |
| 12 | OCR悬浮窗 | 权限申请、文字识别、快捷入口 | 🟢 低 |
| 13 | 美学卡片 | 展示、分享、保存到相册 | 🟢 低 |

---

## 二、已修复问题 (v1.0.1)

### 2.1 编译警告修复 ✅

| 文件 | 警告 | 状态 |
|------|------|------|
| `AiChatService.kt:81` | `userLevel` 未使用 | ✅ 已添加 @Suppress |
| `VisualAnchorService.kt:473` | `capitalizedWord` 未使用 | ✅ 已删除 |
| `CameraViewModel.kt:43` | `context` 参数未使用 | ✅ 已添加 @Suppress |
| `FlashcardScreen.kt:269` | 参数命名冲突 | ✅ 已修复 (cardRotation) |
| `OcrAccessibilityService.kt` | `recycle()` 已废弃 | ✅ 已添加 @Suppress |

### 2.2 依赖升级

- AGP: 8.2.2 → 8.3.0 (修复构建问题)
- Kotlin: 1.9.22 → 1.9.23
- Compose Compiler: 1.5.8 → 1.5.10

### 2.3 图标准告

- 由于 material-icons-extended 版本限制，`Icons.Default.MenuBook` 和 `Icons.Default.TrendingUp` 暂时保留，添加 @Suppress 抑制警告

---

## 三、待完善模块 (Issues)

### 2.1 AI 聊天服务 - 需要接入真实 LLM

**文件**: `app/src/main/java/com/juno/app/data/remote/AiChatService.kt`

**当前状态**: 模板式回复，非真实 AI

```kotlin
// 问题: 使用预定义模板，不是真正的 LLM
fun generateGreeting(tutorId: String): String {
    val templates = greetingTemplates[tutorId] ?: ...
    return templates.random()  // 随机选择模板
}
```

**修复建议**:
- [ ] 接入 OpenAI API (GPT-4)
- [ ] 或接入 DeepSeek API
- [ ] 或使用 Gemini Nano (离线)

---

### 2.2 视觉锚定 - 词库有限

**文件**: `app/src/main/java/com/juno/app/data/remote/VisualAnchorService.kt`

**当前状态**: 硬编码约30个常见物品单词

```kotlin
// 问题: 只有30个物品，数据应从API或数据库获取
private val knowledgeDatabase = mapOf(
    "cat" to AnchorContent(...),
    "dog" to AnchorContent(...),
    // ... 约30个
)
```

**修复建议**:
- [ ] 扩展词库到500+常见物品
- [ ] 或接入 Wikipedia API 获取词条
- [ ] 或接入真实 LLM 动态生成内容

---

### 2.3 链接解析 - 字幕获取不完整

**文件**: `app/src/main/java/com/juno/app/data/remote/LinkParserService.kt`

**当前状态**: YouTube 字幕提取为占位实现

```kotlin
// 问题: 只能获取部分视频，复杂视频失败
private fun fetchYouTubeCaptions(videoId: String): String {
    // This is a placeholder implementation
    // 需要使用 YouTube Data API
}
```

**修复建议**:
- [ ] 申请 YouTube Data API Key
- [ ] 或使用第三方字幕下载服务
- [ ] 添加更多网站支持 (TED, Coursera等)

---

### 2.4 发音评测 - 对比算法简单

**文件**: `app/src/main/java/com/juno/app/ui/screens/pronunciation/PronunciationScreen.kt`

**当前状态**: 基于音频波形简单对比，未使用 Whisper API

**修复建议**:
- [ ] 接入 OpenAI Whisper API 进行语音转文字
- [ ] 使用更复杂的音素对比算法
- [ ] 添加发音薄弱点分析

---

### 2.5 主题系统 - 莫兰迪/极简主题配色可优化

**文件**: `app/src/main/java/com/juno/app/ui/theme/Color.kt`

**当前状态**: 莫兰迪和极简主题为初版，可能不协调

**修复建议**:
- [ ] 优化莫兰迪色系配色
- [ ] 完善极简主题对比度
- [ ] 添加主题预览图

---

### 2.6 专注模式 - 统计数据未持久化

**文件**: `app/src/main/java/com/juno/app/ui/screens/focus/FocusModeViewModel.kt`

**当前状态**: 统计数据仅存于内存，退出后丢失

```kotlin
// 问题: 统计数据未保存到 DataStore
fun saveFocusSession(durationMinutes: Int) {
    _uiState.value = currentState.copy(...)  // 仅修改内存状态
}
```

**修复建议**:
- [ ] 将统计数据保存到 PreferencesManager
- [ ] 添加历史记录查询
- [ ] 展示连续专注天数

---

### 2.7 OCR 悬浮窗 - 需要权限适配

**文件**: `app/src/main/java/com/juno/app/service/OcrAccessibilityService.kt`

**当前状态**: 需要手动开启辅助功能权限

**修复建议**:
- [ ] 添加权限引导UI
- [ ] 处理权限被拒绝场景
- [ ] 优化悬浮窗显示位置

---

## 三、编译警告 (已修复 ✅)

### 3.1 未使用的变量/参数

| 文件 | 警告 | 状态 |
|------|------|------|
| `AiChatService.kt:81` | `userLevel` 未使用 | ✅ 已添加 @Suppress |
| `AiChatService.kt:83` | `tutor` 变量未使用 | ✅ 已删除 |
| `VisualAnchorService.kt:473` | `capitalizedWord` 未使用 | ✅ 已删除 |
| `CameraViewModel.kt:43` | `context` 参数未使用 | ✅ 已添加 @Suppress |
| `FlashcardScreen.kt:269` | `rotation` 参数冲突 | ✅ 已修复 (cardRotation) |

### 3.2 过时的 API

| 文件 | 警告 | 状态 |
|------|------|------|
| 多处 | `Icons.Default.MenuBook` 已废弃 | ⚠️ 因库版本限制，保留原样 |
| 多处 | `Icons.Default.TrendingUp` 已废弃 | ⚠️ 因库版本限制，保留原样 |
| `OcrAccessibilityService.kt` | `recycle()` 已废弃 | ✅ 已添加 @Suppress |

---

## 四、性能优化建议

| 问题 | 建议 |
|------|------|
| APK 体积 108MB | 启用 R8 压缩、移除未用资源 |
| 启动速度 | 添加 Splash Screen、懒加载 |
| 图片加载 | 使用 Coil/Glide 缓存 |
| 数据库 | 添加 Room 索引优化查询 |

---

## 五、测试检查表

### 安装测试
- [ ] 在不同 Android 版本测试 (11, 12, 13, 14)
- [ ] 在不同分辨率设备测试
- [ ] 测试覆盖安装与卸载

### 功能测试
- [ ] 每个 Screen 页面渲染正常
- [ ] 导航跳转无崩溃
- [ ] 数据持久化正确
- [ ] 权限申请正常

### 边界测试
- [ ] 空数据场景 (无单词时)
- [ ] 网络异常处理
- [ ] 大量数据 (1000+ 单词) 性能
- [ ] 快速连续点击

---

*文档更新时间: 2026-03-13*
*版本: 1.0.1*
