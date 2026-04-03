# Juno v1.0.1 Bug 修复清单

## 已修复问题 (11/11) ✅

### ✅ Bug #1: Excel批量导入单词重复问题
- **问题**: 重复导入相同单词没有过滤
- **修复**: 在 ExcelImportService 中添加已存在单词检查，过滤重复项
- **修改文件**: `WordDao.kt`, `WordRepository.kt`, `WordRepositoryImpl.kt`, `ExcelImportService.kt`

### ✅ Bug #2: 专注模式切换时长问题
- **问题**: 切换时长UI不更新；60分钟超出屏幕
- **修复**: 
  - 添加 LaunchedEffect 监听时长变化
  - 自适应圆环大小
- **修改文件**: `FocusModeScreen.kt`

### ✅ Bug #3: 视觉锚定相机一直转圈
- **问题**: 拍照后 Processing 状态无超时保护，缺少权限检查，无法从卡死状态恢复
- **修复**:
  - 新增相机权限前置检查，权限不足时显示友好中文提示
  - 新增 30 秒处理超时 `timeoutJob` 兜底机制，超时自动恢复到 Error 状态
  - 防止在 Capturing/Processing 状态下重复触发拍照
  - `imageProxyToBitmap` 返回 null 时增加日志与友好提示
  - 错误信息全部汉化
- **修改文件**: `CameraViewModel.kt`

### ✅ Bug #4: 滑动卡片体验问题
- **问题**: 滑动后偏移量不重置导致下一张卡片位置偏移；操作提示不清晰；没有按钮备选入口
- **修复**:
  - `offsetX` 增加 `remember(uiState.currentIndex)` key，卡片切换后自动归零
  - 提示文案改为根据翻牌状态动态切换（"点击查看释义" vs "左滑/右滑"）
  - 翻牌后增加"没记住"/"记住了"按钮作为不擅长滑动用户的备选入口
  - 修复`rememberedCount/forgotCount` 统计 Bug（原逻辑错误地基于 `isFlipped` 而非实际操作）
- **修改文件**: `FlashcardScreen.kt`, `FlashcardViewModel.kt`

### ✅ Bug #5: 麦克风录音闪退
- **问题**: 未检查 RECORD_AUDIO 权限直接调用 MediaRecorder 导致 SecurityException 崩溃
- **修复**:
  - 在 `startRecording()` 前增加 `ContextCompat.checkSelfPermission` 权限前置检查
  - 用 `try-catch` 包裹 `startRecording()` 和 `stopRecording()` 全链路调用
  - 检查 `startRecording()` 返回值，null 表示麦克风被占用时给出明确中文提示
- **修改文件**: `PronunciationViewModel.kt`

### ✅ Bug #6: 背单词页面按钮UI
- **问题**: 无操作按钮，只能通过滑动操作，不友好
- **修复**: (与 Bug #4 合并修复) 翻牌后新增 "没记住"/"记住了" 双按钮
- **修改文件**: `FlashcardScreen.kt`

### ✅ Bug #7: 复习页面状态不准确
- **问题**: 无待复习单词时错误显示"所有复习任务已完成"（`isComplete=true`），用户误以为自己完成了任务
- **修复**:
  - `loadDueReviews()` 中无待复习时设置 `isComplete=false`，走 EmptyReviewScreen 分支
  - EmptyReviewScreen 文案改为："暂无待复习的单词"+"新学的单词会根据记忆曲线自动安排复习"
- **修改文件**: `ReviewViewModel.kt`, `ReviewScreen.kt`

### ✅ Bug #8: 今日复习数据显示27
- **问题**: 跨天打开 App 时每日统计未重置，导致前几天的数据累积显示
- **修复**:
  - 在 `initializeProgress()` 中增加日期检查调用 `checkAndResetDailyStats()`
  - 修复 `lastStudyDate` 为 null（首次使用/导入数据）时不触发 reset 的边界问题
- **修改文件**: `UserRepositoryImpl.kt`

### ✅ Bug #9: 深色模式切换按钮无反应
- **问题**: 深色模式toggle保存到darkMode但theme未读取
- **修复**: setDarkMode()改为设置themeMode
- **修改文件**: `SettingsViewModel.kt`, `SettingsScreen.kt`

### ✅ Bug #10: OCR悬浮窗功能不明显
- **问题**: 通知文案为英文且无操作指引，用户不知如何使用悬浮窗
- **修复**:
  - 通知标题/内容全面汉化："Juno OCR 悬浮窗 已激活"
  - 通知扩展文本增加三步使用说明 (BigTextStyle)
  - 无障碍服务未开启时错误提示改为详细中文引导路径
- **修改文件**: `FloatingWindowService.kt`

### ✅ Bug #11: 关于版本点击无反应
- **问题**: 版本号可点击但无反应
- **修复**: 添加关于对话框，更新版本号1.0.1
- **修改文件**: `SettingsScreen.kt`

---

*更新时间: 2026-03-16*
*版本: 1.0.1*
*状态: 全部修复完成 🎉*

---

# Juno v1.0.2 Bug 修复清单

## 已修复问题 (5/5) ✅

### ✅ Bug #12: 悬浮窗无法拖动
- **问题**: `setupFloatingBehavior()` 的 `OnTouchListener` 完全缺少 `ACTION_MOVE` 分支，手指滑动时窗口位置从未更新
- **修复**:
  - 新增 `ACTION_MOVE` 处理：记录手指起点与窗口起点，实时调用 `windowManager.updateViewLayout()`
  - 以 8px 为阈值区分"点击"和"拖动"，避免误触发 OCR
  - 拖动结束后将最终坐标通过 `preferencesManager.setFloatWindowPosition()` 持久化
- **修改文件**: `FloatingWindowService.kt`

### ✅ Bug #13: 长按菜单永远无法触发（setOnLongClickListener 死代码）
- **问题**: `setOnTouchListener` 在 `ACTION_DOWN` 返回 `true` 完整消费了事件，View 内部的 `onTouchEvent` 不再运行，长按 Runnable 从未被 post，`setOnLongClickListener` 是死代码
- **修复**:
  - 移除 `setOnLongClickListener`
  - 在 `ACTION_DOWN` 时用 `Handler.postDelayed()` 手动调度长按，超时时间取 `ViewConfiguration.getLongPressTimeout()`
  - 拖动时（`ACTION_MOVE` 位移 > 8px）取消 pending 长按
- **修改文件**: `FloatingWindowService.kt`

### ✅ Bug #14: 长按松手后误触 OCR
- **问题**: 长按弹出菜单后，手指松开时 `ACTION_UP` 触发，`isDragging=false`，会意外调用 `captureAndRecognize()`
- **修复**:
  - 新增 `isLongPressed` 标志位，长按回调执行时设为 `true`
  - `ACTION_UP` 中三路分支：拖动结束 / 长按释放（静默）/ 短按触发 OCR
- **修改文件**: `FloatingWindowService.kt`

### ✅ Bug #15: captureScreen() 永远返回 null，OCR 从未真正执行
- **问题**: `OcrAccessibilityService.captureScreen()` 在第 121 行硬编码 `return null`，Bitmap 路径需要 `MediaProjection API`，无障碍服务无法直接截图；文字通过 `listener` 通道传递但 `listener` 从未被赋值，文字彻底丢失
- **修复**:
  - 在 `captureAndRecognize()` 中，当 bitmap 为 null 时（当前必然如此），直接调用 `accessibilityService.getAllVisibleText()` 提取无障碍树文字
  - ML Kit OCR 路径保留，作为未来接入 MediaProjection 后的升级入口
  - 失败时给出用户友好的中文提示
- **修改文件**: `FloatingWindowService.kt`

### ✅ Bug #16: onTextRecognized 回调从未注册，OCR 结果永远丢失
- **问题**: `FloatingWindowService.onTextRecognized` 是 companion object 中的静态 lambda，但整个项目（包括 `AiScreen`）没有任何地方赋值，即使 OCR 成功，结果也无处消费，用户看不到任何反馈
- **修复**:
  - 在 `AiScreen` 中通过 `DisposableEffect` 注册回调，将识别文字存入 `recognizedText` 状态
  - 新增 `OcrResultDialog` Composable，`recognizedText` 非空时自动弹出展示识别结果
  - `onDispose` 中清空回调，防止 lambda 持有已销毁的 context 造成泄漏
- **修改文件**: `AiScreen.kt`

---

*更新时间: 2026-04-03*
*版本: 1.0.2*
*状态: 全部修复完成 🎉*
