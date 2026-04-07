# GPT 词库集成设计

## 概述

将 `docs/gptwords.json`（8,714 条 JSONL 格式的英语词条）集成到 Juno 应用的核心学习流程中。每条词条包含完整的中文语言学分析：词义、例句、词根、词缀、文化背景、变形、记忆辅助、小故事。

用户可一次性全量导入，词条入库后参与 flashcard、复习、搜索等所有现有功能。

## 架构决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 数据模型 | 扩展 WordEntity 新增 `gptContent` 字段 | 最小改动，词条融入现有学习流程 |
| 导入方式 | 全量导入，自动去重 | 8,714 条数据应全部可用，去重避免冗余 |
| JSONL 来源 | `app/src/main/assets/gptwords.json` | 打包进 APK，无需网络 |
| Markdown 渲染 | 轻量手动解析（AnnotatedString） | 内容结构固定，零额外依赖 |
| 数据库迁移 | Room Migration v2 → v3 | ALTER TABLE 添加 gptContent 列 |

## 数据层

### WordEntity 扩展

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
    val difficulty: Int = 1,
    val isLearned: Boolean = false,
    val lastStudiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String? = null,
    val tags: String? = null,
    val gptContent: String? = null  // 新增：gptwords.json 的完整 Markdown 内容
)
```

### Room Migration (v2 → v3)

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE words ADD COLUMN gptContent TEXT")
    }
}
```

### WordDao 新增查询

```kotlin
@Query("SELECT * FROM words WHERE id = :id AND gptContent IS NOT NULL LIMIT 1")
suspend fun getWordWithGptContent(id: Long): WordEntity?
```

### GptWordsImportService

新建 `data/remote/GptWordsImportService.kt`：

- 从 `assets/gptwords.json` 读取 JSONL（每行一个 JSON 对象）
- 解析 `{word, content}` 为 `WordEntity`
- 从 content 提取首段非空文本作为 `meaning`（兼容列表展示）
- 去重：`word.lowercase()` 比对已有词条
- 批量 `insertWords()` 入库
- 返回 `ImportResult(totalCount, importedCount, duplicateCount, errorMessage)`

## UI 层

### 导航路由

```kotlin
data object GptWordDetail : Screen("gpt_word_detail/{wordId}") {
    fun createRoute(wordId: Long) = "gpt_word_detail/$wordId"
}
data object GptImport : Screen("gpt_import")
```

### GptImportScreen — 导入确认页

```
Scaffold
├─ TopAppBar: "导入 GPT 词库" + 返回按钮
├─ Content (Column, padding 16dp):
│  ├─ Text: "共 8,714 条词条"
│  ├─ Text: "导入后将与现有词库合并，重复词条自动跳过"
│  ├─ Spacer
│  ├─ Button: "开始导入" (primary, full width)
│  └─ 导入中: CircularProgressIndicator + 进度文字
└─ SnackbarHost
```

- 导入完成后 Snackbar 提示结果，自动返回上一页
- 导入按钮在 `WordListScreen` 中通过 AlertDialog 触发导航

### GptWordDetailScreen — 富文本详情页

```
Scaffold
├─ TopAppBar: 单词标题 + 返回按钮
└─ Content (LazyColumn, padding 16dp):
   ├─ 单词大字 (titleLarge, bold)
   ├─ Divider
   ├─ 各章节卡片 (Card, spacedBy 12dp):
   │  ├─ 📖 分析词义
   │  ├─ 📝 列举例句
   │  ├─ 🔍 词根分析
   │  ├─ 📚 发展历史和文化背景
   │  ├─ 🔄 单词变形
   │  ├─ 💡 记忆辅助
   │  └─ 📖 小故事
   └─ Button: "加入学习" (如果 isLearned=false)
```

### MarkdownUtils

轻量解析器，仅处理以下 Markdown 语法：

| 语法 | 渲染 |
|------|------|
| `### 标题` | 加粗 + 较大字号 + 顶部间距 |
| `**加粗**` | FontWeight.Bold |
| `1. ` / `* ` 列表 | 缩进 + 前置序号/符号 |
| 普通文本 | bodyMedium, onSurfaceVariant |
| 空行 | 间距 |

输出 `AnnotatedString`，用 `SelectableText` 或 `Text` 展示。

### GptImportViewModel

```kotlin
@HiltViewModel
class GptImportViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val gptWordsImportService: GptWordsImportService
) : ViewModel() {
    // StateFlow: isImporting, totalCount, importedCount, duplicateCount, result
    // importGptWords() → 调用 Service → 批量插入 → 返回结果
}
```

遵循现有 ViewModel 模式：`@HiltViewModel`、StateFlow 状态管理、`viewModelScope.launch` 异步操作。

### WordListScreen 入口修改

将现有 Excel 导入按钮的点击行为改为弹出 AlertDialog：

```
选择导入方式
├─ 从 Excel 文件导入
└─ 从 GPT 词库导入 (8,714 条)
```

选择 GPT 词库后导航到 `GptImport` 页面。

## 数据流

```
assets/gptwords.json (JSONL)
    ↓ GptWordsImportService.parse()
List<WordEntity> (去重 + 提取 meaning)
    ↓ WordRepository.insertWords()
Room DB (words.gptContent)
    ↓ GptWordDetailScreen 读取
MarkdownUtils.parse() → AnnotatedString
    ↓ LazyColumn 渲染
用户可见
```

## 涉及文件清单

| 文件 | 操作 |
|------|------|
| `data/local/entity/WordEntity.kt` | 修改：新增 gptContent 字段 |
| `data/local/JunoDatabase.kt` | 修改：version 2→3 + Migration |
| `data/local/dao/WordDao.kt` | 修改：新增 getWordWithGptContent 查询 |
| `data/remote/GptWordsImportService.kt` | 新建 |
| `ui/navigation/Screen.kt` | 修改：新增两条路由 |
| `ui/navigation/JunoNavHost.kt` | 修改：注册新路由 |
| `ui/screens/wordlist/WordListScreen.kt` | 修改：导入按钮改为选择对话框 |
| `ui/screens/wordlist/WordListViewModel.kt` | 修改：新增 importGptWords() |
| `ui/screens/gptimport/GptImportScreen.kt` | 新建 |
| `ui/screens/gptimport/GptImportViewModel.kt` | 新建 |
| `ui/screens/gptworddetail/GptWordDetailScreen.kt` | 新建 |
| `ui/utils/MarkdownUtils.kt` | 新建 |
| `app/src/main/assets/gptwords.json` | 新建：复制 docs/gptwords.json |
