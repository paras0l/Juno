# GPT 词库集成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 8,714 条 GPT 词库词条导入 Juno 应用，支持富文本详情展示，词条融入现有学习流程。

**Architecture:** 扩展 WordEntity 新增 `gptContent` 字段存储完整 Markdown 内容，新建 GptWordsImportService 从 assets 解析 JSONL 并批量入库，新建 GptImportScreen 导入确认页和 GptWordDetailScreen 富文本详情页。

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, AnnotatedString

---

### Task 1: 扩展 WordEntity — 新增 gptContent 字段

**Files:**
- Modify: `app/src/main/java/com/juno/app/data/local/entity/WordEntity.kt`

- [ ] **Step 1: 在 WordEntity 末尾新增 gptContent 字段**

打开 `app/src/main/java/com/juno/app/data/local/entity/WordEntity.kt`，在 `tags` 字段后新增一行：

```kotlin
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String? = null,
    val meaning: String,
    val example: String? = null,
    val translation: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val difficulty: Int = 1, // 1-5 scale
    val isLearned: Boolean = false,
    val lastStudiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String? = null,
    val tags: String? = null, // Comma-separated tags
    val gptContent: String? = null // GPT 词库完整 Markdown 内容
)
```

- [ ] **Step 2: 验证编译**

运行 `lsp_diagnostics` 检查 `WordEntity.kt`，确保无错误。

---

### Task 2: Room Migration v2 → v3 + 更新 JunoDatabase

**Files:**
- Modify: `app/src/main/java/com/juno/app/data/local/JunoDatabase.kt`
- Modify: `app/src/main/java/com/juno/app/di/AppModule.kt`

- [ ] **Step 1: 更新 JunoDatabase 版本号为 3**

打开 `app/src/main/java/com/juno/app/data/local/JunoDatabase.kt`，将 `version = 2` 改为 `version = 3`：

```kotlin
@Database(
    entities = [
        WordEntity::class,
        ReviewRecordEntity::class,
        UserProgressEntity::class,
        StoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class JunoDatabase : RoomDatabase() {
```

- [ ] **Step 2: 在 AppModule 中添加 MIGRATION_2_3**

打开 `app/src/main/java/com/juno/app/di/AppModule.kt`，在 `provideJunoDatabase` 方法中添加新的 Migration：

```kotlin
@Provides
@Singleton
fun provideJunoDatabase(
    @ApplicationContext context: Context
): JunoDatabase {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN lastStudiedDate INTEGER")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE words ADD COLUMN gptContent TEXT")
        }
    }

    return Room.databaseBuilder(
        context,
        JunoDatabase::class.java,
        JunoDatabase.DATABASE_NAME
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
}
```

注意：`.addMigrations(MIGRATION_1_2)` 改为 `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)`。

- [ ] **Step 3: 验证编译**

运行 `lsp_diagnostics` 检查两个文件，确保无错误。

---

### Task 3: WordDao 新增查询方法

**Files:**
- Modify: `app/src/main/java/com/juno/app/data/local/dao/WordDao.kt`

- [ ] **Step 1: 新增 getWordWithGptContent 查询**

打开 `app/src/main/java/com/juno/app/data/local/dao/WordDao.kt`，在文件末尾（`deleteAllWords` 之后）添加：

```kotlin
@Query("SELECT * FROM words WHERE id = :id AND gptContent IS NOT NULL LIMIT 1")
suspend fun getWordWithGptContent(id: Long): WordEntity?
```

- [ ] **Step 2: 验证编译**

运行 `lsp_diagnostics` 检查 `WordDao.kt`。

---

### Task 4: GptWordsImportService — JSONL 解析与批量导入

**Files:**
- Create: `app/src/main/java/com/juno/app/data/remote/GptWordsImportService.kt`
- Create: `app/src/main/assets/gptwords.json`

- [ ] **Step 1: 复制 gptwords.json 到 assets**

```bash
mkdir -p app/src/main/assets
cp docs/gptwords.json app/src/main/assets/gptwords.json
```

- [ ] **Step 2: 创建 GptWordsImportService**

新建 `app/src/main/java/com/juno/app/data/remote/GptWordsImportService.kt`：

```kotlin
package com.juno.app.data.remote

import android.content.Context
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GptWordsImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordRepository: WordRepository
) {

    data class ImportResult(
        val success: Boolean,
        val totalCount: Int = 0,
        val importedCount: Int = 0,
        val duplicateCount: Int = 0,
        val errorMessage: String? = null
    )

    suspend fun importWords(): ImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("gptwords.json")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            
            val words = mutableListOf<WordEntity>()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                if (trimmed.isEmpty()) continue
                
                try {
                    val json = JSONObject(trimmed)
                    val word = json.getString("word")
                    val content = json.getString("content")
                    
                    if (word.isBlank()) continue
                    
                    // 提取首段非空文本作为 meaning
                    val meaning = extractMeaning(content)
                    
                    words.add(
                        WordEntity(
                            word = word,
                            meaning = meaning,
                            gptContent = content,
                            difficulty = 1,
                            isLearned = false
                        )
                    )
                } catch (e: Exception) {
                    // 跳过解析失败的行
                }
            }
            reader.close()
            inputStream.close()
            
            if (words.isEmpty()) {
                return@withContext ImportResult(
                    success = false,
                    totalCount = 0,
                    errorMessage = "词库文件中没有找到有效的数据"
                )
            }
            
            // 去重
            val existingWords = wordRepository.getAllWordsList().map { it.lowercase() }.toSet()
            val newWords = words.filter { it.word.lowercase() !in existingWords }
            val duplicateCount = words.size - newWords.size
            
            if (newWords.isNotEmpty()) {
                wordRepository.insertWords(newWords)
            }
            
            ImportResult(
                success = true,
                totalCount = words.size,
                importedCount = newWords.size,
                duplicateCount = duplicateCount
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorMessage = "导入失败: ${e.message}"
            )
        }
    }

    /**
     * 从 Markdown 内容中提取首段非空文本作为 meaning
     * 跳过 ### 标题行，取第一个非空非标题行
     */
    private fun extractMeaning(content: String): String {
        val lines = content.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (trimmed.startsWith("#")) continue
            if (trimmed.length > 200) {
                return trimmed.take(200) + "..."
            }
            return trimmed
        }
        return "暂无释义"
    }
}
```

- [ ] **Step 3: 验证编译**

运行 `lsp_diagnostics` 检查 `GptWordsImportService.kt`。

---

### Task 5: 导航路由 — 新增 GptImport 和 GptWordDetail

**Files:**
- Modify: `app/src/main/java/com/juno/app/ui/navigation/Screen.kt`

- [ ] **Step 1: 新增两条路由**

打开 `app/src/main/java/com/juno/app/ui/navigation/Screen.kt`，在类末尾添加：

```kotlin
    data object GptImport : Screen("gpt_import")
    data object GptWordDetail : Screen("gpt_word_detail/{wordId}") {
        fun createRoute(wordId: Long) = "gpt_word_detail/$wordId"
    }
```

完整文件变为：

```kotlin
package com.juno.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Flashcard : Screen("flashcard")
    data object WordList : Screen("word_list?filter={filter}") {
        fun createRoute(filter: String? = null): String {
            return if (filter != null) "word_list?filter=$filter" else "word_list?filter="
        }
    }
    data object AddWord : Screen("add_word")
    data object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Long) = "edit_word/$wordId"
    }
    data object Review : Screen("review")
    data object Story : Screen("story")
    data object StoryDetail : Screen("story_detail/{storyId}") {
        fun createRoute(storyId: Long) = "story_detail/$storyId"
    }
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object Pronunciation : Screen("pronunciation?word={word}") {
        fun createRoute(word: String? = null): String {
            return if (word != null) "pronunciation?word=$word" else "pronunciation"
        }
    }
    data object TutorSelection : Screen("tutor_selection")
    data object Chat : Screen("chat/{tutorId}") {
        fun createRoute(tutorId: String) = "chat/$tutorId"
    }
    data object Camera : Screen("camera")
    data object AnchorResult : Screen("anchor_result")
    data object FocusMode : Screen("focus_mode")
    data object PermissionGuide : Screen("permission_guide")
    data object OcrHistory : Screen("ocr_history")
    data object GptImport : Screen("gpt_import")
    data object GptWordDetail : Screen("gpt_word_detail/{wordId}") {
        fun createRoute(wordId: Long) = "gpt_word_detail/$wordId"
    }
}
```

- [ ] **Step 2: 验证编译**

运行 `lsp_diagnostics` 检查 `Screen.kt`。

---

### Task 6: 注册新路由到 JunoNavHost

**Files:**
- Modify: `app/src/main/java/com/juno/app/ui/navigation/JunoNavHost.kt`

- [ ] **Step 1: 添加 import 和路由注册**

打开 `app/src/main/java/com/juno/app/ui/navigation/JunoNavHost.kt`，在 import 区域添加：

```kotlin
import com.juno.app.ui.screens.gptimport.GptImportScreen
import com.juno.app.ui.screens.gptworddetail.GptWordDetailScreen
```

在 NavHost 的 `composable` 块末尾（`OcrHistory` 之后、`}` 之前）添加：

```kotlin
        composable(Screen.GptImport.route) {
            GptImportScreen(
                onNavigateBack = { navController.popBackStack() },
                onImportComplete = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GptWordDetail.route,
            arguments = listOf(navArgument("wordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getLong("wordId") ?: return@composable
            GptWordDetailScreen(
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
```

- [ ] **Step 2: 验证编译**

运行 `lsp_diagnostics` 检查 `JunoNavHost.kt`（此时 GptImportScreen 和 GptWordDetailScreen 尚未创建，会有 import 错误，可暂时注释掉 import 行，等 Task 9-10 完成后再取消注释）。

---

### Task 7: WordListScreen 导入入口 — 改为选择对话框

**Files:**
- Modify: `app/src/main/java/com/juno/app/ui/screens/wordlist/WordListScreen.kt`
- Modify: `app/src/main/java/com/juno/app/ui/screens/wordlist/WordListViewModel.kt`

- [ ] **Step 1: 在 WordListViewModel 中新增导航事件流**

打开 `app/src/main/java/com/juno/app/ui/screens/wordlist/WordListViewModel.kt`，添加：

```kotlin
    private val _navigateToGptImport = MutableStateFlow(false)
    val navigateToGptImport: StateFlow<Boolean> = _navigateToGptImport.asStateFlow()

    fun onNavigateToGptImport() {
        _navigateToGptImport.value = true
    }

    fun onGptImportNavigated() {
        _navigateToGptImport.value = false
    }
```

- [ ] **Step 2: 修改 WordListScreen 导入按钮行为**

打开 `app/src/main/java/com/juno/app/ui/screens/wordlist/WordListScreen.kt`。

在 `WordListScreen` composable 函数的参数列表中新增：

```kotlin
    onNavigateToGptImport: () -> Unit,
```

在状态收集区新增：

```kotlin
    val navigateToGptImport by viewModel.navigateToGptImport.collectAsState()
```

在 `LaunchedEffect(importResult)` 之后新增：

```kotlin
    LaunchedEffect(navigateToGptImport) {
        if (navigateToGptImport) {
            onNavigateToGptImport()
            viewModel.onGptImportNavigated()
        }
    }
```

将现有的 Excel 导入 IconButton 的 `onClick` 改为弹出选择对话框。替换整个 actions 块（约第 118-157 行）：

```kotlin
                actions = {
                    if (isFilteredMode) {
                        IconButton(
                            onClick = {
                                viewModel.exportWords(context, words)
                            },
                            enabled = words.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "导出"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.showImportDialog() },
                            enabled = !isImporting
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = "导入"
                                )
                            }
                        }
                    }
                }
```

- [ ] **Step 3: 添加导入选择对话框**

在 `WordListScreen` 文件末尾（`FilterEmptyState` 之后）添加新的对话框 composable：

```kotlin
@Composable
private fun ImportTypeDialog(
    onDismiss: () -> Unit,
    onExcelImport: () -> Unit,
    onGptImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择导入方式") },
        text = {
            Column {
                TextButton(
                    onClick = onExcelImport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从 Excel 文件导入")
                }
                TextButton(
                    onClick = onGptImport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从 GPT 词库导入 (8,714 条)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
```

在 `WordListScreen` 中，在现有的 `showDeleteDialog` AlertDialog 之后添加：

```kotlin
    if (viewModel.showImportDialog) {
        ImportTypeDialog(
            onDismiss = { viewModel.dismissImportDialog() },
            onExcelImport = {
                viewModel.dismissImportDialog()
                excelPickerLauncher.launch(
                    arrayOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel"
                    )
                )
            },
            onGptImport = {
                viewModel.dismissImportDialog()
                onNavigateToGptImport()
            }
        )
    }
```

- [ ] **Step 4: 在 WordListViewModel 中添加对话框状态**

在 `WordListViewModel` 中添加：

```kotlin
    private val _showImportDialog = MutableStateFlow(false)
    val showImportDialog: StateFlow<Boolean> = _showImportDialog.asStateFlow()

    fun showImportDialog() {
        _showImportDialog.value = true
    }

    fun dismissImportDialog() {
        _showImportDialog.value = false
    }
```

- [ ] **Step 5: 验证编译**

运行 `lsp_diagnostics` 检查两个文件。

---

### Task 8: MarkdownUtils — 轻量 Markdown → AnnotatedString 解析器

**Files:**
- Create: `app/src/main/java/com/juno/app/ui/utils/MarkdownUtils.kt`

- [ ] **Step 1: 创建 MarkdownUtils**

新建 `app/src/main/java/com/juno/app/ui/utils/MarkdownUtils.kt`：

```kotlin
package com.juno.app.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * 轻量 Markdown 解析器，仅处理 GPT 词库内容中使用的语法：
 * - ### 标题 → 加粗 + 较大字号
 * - **加粗** → FontWeight.Bold
 * - 1. / * 列表 → 保留序号/符号
 * - 普通文本 → 默认样式
 */
object MarkdownUtils {

    /**
     * 将 Markdown 内容按章节分割
     * 返回 List<Pair<章节标题, 章节内容>>
     */
    fun parseSections(content: String): List<Pair<String, String>> {
        val sections = mutableListOf<Pair<String, String>>()
        val lines = content.split("\n")
        
        var currentTitle = ""
        var currentContent = StringBuilder()
        
        for (line in lines) {
            val trimmed = line.trim()
            
            if (trimmed.startsWith("### ")) {
                // 保存上一个章节
                if (currentTitle.isNotEmpty()) {
                    sections.add(currentTitle to currentContent.toString().trim())
                }
                currentTitle = trimmed.removePrefix("### ").trim()
                currentContent = StringBuilder()
            } else if (trimmed.startsWith("## ") || trimmed.startsWith("# ")) {
                if (currentTitle.isNotEmpty()) {
                    sections.add(currentTitle to currentContent.toString().trim())
                }
                currentTitle = trimmed.replace(Regex("^#+ "), "").trim()
                currentContent = StringBuilder()
            } else {
                if (trimmed.isNotEmpty()) {
                    currentContent.appendLine(trimmed)
                }
            }
        }
        
        // 保存最后一个章节
        if (currentTitle.isNotEmpty()) {
            sections.add(currentTitle to currentContent.toString().trim())
        }
        
        return sections
    }

    /**
     * 将单行 Markdown 文本转换为 AnnotatedString
     * 支持 **加粗** 语法
     */
    fun parseLine(text: String): AnnotatedString {
        return buildAnnotatedString {
            val regex = Regex("\\*\\*(.+?)\\*\\*")
            var lastIndex = 0
            
            for (match in regex.findAll(text)) {
                // 加粗前的普通文本
                if (match.range.first > lastIndex) {
                    append(text.substring(lastIndex, match.range.first))
                }
                // 加粗文本
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                lastIndex = match.range.last + 1
            }
            
            // 剩余文本
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
}
```

- [ ] **Step 2: 验证编译**

运行 `lsp_diagnostics` 检查 `MarkdownUtils.kt`。

---

### Task 9: GptImportScreen — 导入确认 + 进度页

**Files:**
- Create: `app/src/main/java/com/juno/app/ui/screens/gptimport/GptImportScreen.kt`
- Create: `app/src/main/java/com/juno/app/ui/screens/gptimport/GptImportViewModel.kt`

- [ ] **Step 1: 创建 GptImportViewModel**

新建 `app/src/main/java/com/juno/app/ui/screens/gptimport/GptImportViewModel.kt`：

```kotlin
package com.juno.app.ui.screens.gptimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.remote.GptWordsImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GptImportUiState(
    val isImporting: Boolean = false,
    val totalCount: Int = 0,
    val importedCount: Int = 0,
    val duplicateCount: Int = 0,
    val success: Boolean? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GptImportViewModel @Inject constructor(
    private val gptWordsImportService: GptWordsImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GptImportUiState())
    val uiState: StateFlow<GptImportUiState> = _uiState.asStateFlow()

    fun startImport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, success = null, errorMessage = null)
            
            val result = gptWordsImportService.importWords()
            
            _uiState.value = _uiState.value.copy(
                isImporting = false,
                totalCount = result.totalCount,
                importedCount = result.importedCount,
                duplicateCount = result.duplicateCount,
                success = result.success,
                errorMessage = result.errorMessage
            )
        }
    }

    fun reset() {
        _uiState.value = GptImportUiState()
    }
}
```

- [ ] **Step 2: 创建 GptImportScreen**

新建 `app/src/main/java/com/juno/app/ui/screens/gptimport/GptImportScreen.kt`：

```kotlin
package com.juno.app.ui.screens.gptimport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GptImportScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: GptImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success == true) {
            // 导入完成后等待片刻再返回
            kotlinx.coroutines.delay(1500)
            onImportComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入 GPT 词库") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                if (uiState.isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "正在导入...",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else if (uiState.success == true) {
                    Text(
                        text = "导入完成",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "共 ${uiState.totalCount} 条词条",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "成功导入 ${uiState.importedCount} 条",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.duplicateCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "跳过 ${uiState.duplicateCount} 条已存在词条",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else if (uiState.success == false) {
                    Text(
                        text = "导入失败",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.errorMessage ?: "未知错误",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("重试")
                    }
                } else {
                    Text(
                        text = "GPT 词库",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "共 8,714 条词条",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "导入后将与现有词库合并\n重复词条自动跳过",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.startImport() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("开始导入")
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: 验证编译**

运行 `lsp_diagnostics` 检查两个文件。

---

### Task 10: GptWordDetailScreen — 富文本详情页

**Files:**
- Create: `app/src/main/java/com/juno/app/ui/screens/gptworddetail/GptWordDetailScreen.kt`
- Create: `app/src/main/java/com/juno/app/ui/screens/gptworddetail/GptWordDetailViewModel.kt`

- [ ] **Step 1: 创建 GptWordDetailViewModel**

新建 `app/src/main/java/com/juno/app/ui/screens/gptworddetail/GptWordDetailViewModel.kt`：

```kotlin
package com.juno.app.ui.screens.gptworddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GptWordDetailUiState(
    val isLoading: Boolean = true,
    val word: WordEntity? = null,
    val error: String? = null
)

@HiltViewModel
class GptWordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val wordId: Long = savedStateHandle.get<Long>("wordId") ?: 0

    private val _uiState = MutableStateFlow(GptWordDetailUiState())
    val uiState: StateFlow<GptWordDetailUiState> = _uiState.asStateFlow()

    init {
        loadWord()
    }

    private fun loadWord() {
        viewModelScope.launch {
            try {
                val word = wordRepository.getWordByIdSync(wordId)
                _uiState.value = GptWordDetailUiState(
                    isLoading = false,
                    word = word,
                    error = if (word == null) "词条不存在" else null
                )
            } catch (e: Exception) {
                _uiState.value = GptWordDetailUiState(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun markAsLearned() {
        val word = _uiState.value.word ?: return
        viewModelScope.launch {
            wordRepository.updateLearnedStatus(word.id, true)
            _uiState.value = _uiState.value.copy(
                word = word.copy(isLearned = true)
            )
        }
    }
}
```

- [ ] **Step 2: 创建 GptWordDetailScreen**

新建 `app/src/main/java/com/juno/app/ui/screens/gptworddetail/GptWordDetailScreen.kt`：

```kotlin
package com.juno.app.ui.screens.gptworddetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.ui.utils.MarkdownUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GptWordDetailScreen(
    wordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: GptWordDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.word?.word ?: "单词详情",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.word != null -> {
                WordDetailContent(
                    word = uiState.word!!,
                    onMarkLearned = { viewModel.markAsLearned() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun WordDetailContent(
    word: WordEntity,
    onMarkLearned: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 单词标题
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (!word.phonetic.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = word.phonetic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // 基础释义
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📖 释义",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // GPT 富内容章节
        if (!word.gptContent.isNullOrBlank()) {
            val sections = MarkdownUtils.parseSections(word.gptContent!!)
            
            sections.forEach { (title, content) ->
                SectionCard(title = title, content = content)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 加入学习按钮
        if (!word.isLearned) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onMarkLearned,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("加入学习")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val lines = content.split("\n")
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    Text(
                        text = MarkdownUtils.parseLine(line.trim()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: 验证编译**

运行 `lsp_diagnostics` 检查两个文件。

---

### Task 11: 最终验证 + 提交

- [ ] **Step 1: 取消 Task 6 中的临时注释**

确保 `JunoNavHost.kt` 中的 import 语句已取消注释：

```kotlin
import com.juno.app.ui.screens.gptimport.GptImportScreen
import com.juno.app.ui.screens.gptworddetail.GptWordDetailScreen
```

- [ ] **Step 2: 全项目编译验证**

运行 Android 编译命令：

```bash
cd /Users/binge/GraduationProjects/Juno && ./gradlew assembleDebug --no-daemon 2>&1 | tail -30
```

预期：BUILD SUCCESSFUL。如有编译错误，逐一修复。

- [ ] **Step 3: 提交**

```bash
git add -A
git commit -m "feat: integrate GPT words dictionary with import and detail view

- Add gptContent field to WordEntity
- Room migration v2→v3
- GptWordsImportService for JSONL parsing
- GptImportScreen for bulk import
- GptWordDetailScreen with markdown rendering
- Import selection dialog in WordListScreen"
```

---

## 任务依赖关系

```
Task 1 (WordEntity) ──→ Task 2 (Migration) ──→ Task 3 (DAO)
                                                    ↓
Task 4 (ImportService) ←────────────────────────────┘
       ↓
Task 5 (Routes) ──→ Task 6 (NavHost)
       ↓                    ↓
Task 7 (WordList)    Task 8 (MarkdownUtils)
                            ↓
              ┌─────────────┴─────────────┐
              ↓                           ↓
    Task 9 (GptImport)          Task 10 (GptWordDetail)
                                         ↓
                                   Task 11 (验证+提交)
```

**可并行执行：**
- Task 1-2-3-4 为数据层链路，顺序执行
- Task 5-6 为导航层，可与 Task 8 并行
- Task 7 依赖 Task 5 的路由定义
- Task 9 依赖 Task 4 的 Service
- Task 10 依赖 Task 3 的 DAO + Task 8 的 MarkdownUtils
