# 语法学习模块设计文档

**创建时间**: 2026-04-09
**状态**: 已批准

---

## 1. 概述

基于 `docs/grammar/` 文件夹的语法内容，在系统中增加语法学习模块。

**核心要求**:
1. 带有闯关趣味性
2. 用户可视化进度展示
3. UI保持系统原有模块一致（参考 `docs/rules/frontend.md`）
4. 模块入口放在单词页面
5. 原单词Tab改名为"学习"

---

## 2. 架构设计

### 2.1 页面结构

```
MainScreen
├── 学习Tab (原"单词"Tab)
│   ├── 学习页面 (StudyScreen)
│   │   ├── 今日背词入口卡片
│   │   └── 语法学习入口卡片
│   ├── FlashcardScreen (今日背词) - 无底部导航
│   └── GrammarScreen (语法学习入口)
│       └── GrammarLessonScreen (具体关卡学习)
├── AI伴学Tab
└── 仪表盘Tab
```

### 2.2 导航变化

- MainScreen底部导航：单词 → 学习
- 新增 StudyScreen 作为"学习"Tab的首页
- StudyScreen 包含两个子Tab切换："今日背词" | "语法学习"
- 进入具体模块后移除底部导航（参考StoryDetailScreen方式）

---

## 3. 页面详情

### 3.1 StudyScreen（学习首页）

**位置**: 新增页面
**路径**: `app/src/main/java/com/juno/app/ui/screens/study/`

**UI结构**:
```
┌─────────────────────────────────┐
│  学习                           │
│                                 │
│   ┌─────────────────────────┐   │
│   │    📚 今日背词          │   │
│   │    今天学习 · 15个单词  │   │
│   │    [开始学习]           │   │
│   └─────────────────────────┘   │
│                                 │
│   ┌─────────────────────────┐   │
│   │    📖 语法学习          │   │
│   │    6个阶段 · 闯关学习   │   │
│   │    [开始学习]           │   │
│   └─────────────────────────┘   │
│                                 │
└─────────────────────────────────┘
```

**组件**:
- 两个入口卡片（大圆角24dp，参考frontend.md）
- 卡片使用渐变强调色（#FF9D42 → #FF6B00）

### 3.2 GrammarScreen（语法学习阶段选择）

**位置**: 新增页面
**路径**: `app/src/main/java/com/juno/app/ui/screens/grammar/`

**UI结构**:
```
┌─────────────────────────────────┐
│  ← 语法学习             🔍 搜索  │
├─────────────────────────────────┤
│                                 │
│  ┌─────────────────────────┐   │
│  │ 📚 第一阶段：构建句子骨架  │   │
│  │ 初级·入门                │   │
│  │ ████████░░░░░░░  62%     │   │
│  │ 已完成 5/8 关            │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 📕 第二阶段：掌握时间... │   │
│  │ 初级·核心                │   │
│  │ ████░░░░░░░░░░░  25%     │   │
│  │ 已完成 2/8 关            │   │
│  └─────────────────────────┘   │
│                                 │
│  ... (共6个阶段卡片)           │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🔒 第四阶段：深度理解... │   │
│  │ 中级·进阶                │   │
│  │ 完成上一阶段解锁         │   │
│  └─────────────────────────┘   │
│                                 │
└─────────────────────────────────┘
```

**阶段卡片组件**:
- 圆角: 24dp
- 阴影: 弥散光阴影（参考frontend.md Rule 2）
- 进度条: LinearProgressIndicator，圆角4dp
- 完成度颜色: 使用渐变色（主色调紫色）
- 未解锁状态: 灰色+🔒图标

**阶段数据**:
- 第一阶段：构建句子骨架（初级·入门）- 8个关卡
- 第二阶段：掌握时间与状态（初级·核心）- 8个关卡
- 第三阶段：搭建复杂句子（中级·过渡）- 8个关卡
- 第四阶段：深度理解被动与虚拟（中级·进阶）- 8个关卡
- 第五阶段：精通名词性从句与定语从句（高级·核心）- 8个关卡
- 第六阶段：高阶结构与特殊句式（高级·拔尖）- 8个关卡

### 3.3 GrammarLessonScreen（关卡学习）

**位置**: 新增页面
**路径**: `app/src/main/java/com/juno/app/ui/screens/grammar/`

**学习流程**（新知识点A模式）:

```
┌─────────────────────────────────┐
│  ← 第一阶段：五大基本句型   1/8 │
├─────────────────────────────────┤
│  ● ● ○ ○ ○ ○ ○ ○             ← 关卡进度点
├─────────────────────────────────┤
│                                 │
│  【概念讲解】                   │
│  ───────────────────────────    │
│  五种基本句型：                 │
│  1. 主语 + 谓语（SV）           │
│  2. 主语 + 谓语 + 宾语（SVO）    │
│  3. 主语 + 谓语 + 双宾语（SVOO） │
│  ...                           │
│                                 │
│  【例句】                       │
│  ───────────────────────────    │
│  • The sun rises.              │
│  • She likes apples.           │
│  • She gave me a book.         │
│                                 │
├─────────────────────────────────┤
│  [检查学习成果]                 ← 按钮（渐变色）
└─────────────────────────────────┘
        ↓
┌─────────────────────────────────┐
│  【练习】                        │
│  ───────────────────────────    │
│  1. The children are playing   │
│     in the park.               │
│     请判断这是什么句型：        │
│     ○ 主谓结构                  │
│     ○ 主谓宾结构                │
│     ○ 主谓双宾                  │
│     ○ 主谓宾补                  │
│                                 │
│  ... (共3-5题)                  │
│                                 │
├─────────────────────────────────┤
│  [提交]                         │
└─────────────────────────────────┘

        ↓
┌─────────────────────────────────┐
│  【结果】                        │
│  ───────────────────────────    │
│  ✓ 正确: 4/5                   │
│                                 │
│  ┌─ 题目解析 ─────────────────┐  │
│  │ 1. ✓ 正确               │  │
│  │ 2. ✗ 正确答案：B        │  │
│  │ 3. ✓ 正确               │  │
│  │ ...                     │  │
│  └──────────────────────────┘   │
│                                 │
│  [重新学习] [下一关]           │
└─────────────────────────────────┘
```

**动画**:
- 题目切换: fadeIn + slideIn 动画
- 提交结果: Spring Animation + 阴影变化（参考frontend.md Rule 5）

---

## 4. 数据模型

### 4.1 GrammarStageEntity

```kotlin
@Entity(tableName = "grammar_stages")
data class GrammarStageEntity(
    @PrimaryKey
    val id: Long,
    val name: String,           // "第一阶段：构建句子骨架"
    val level: String,         // "初级·入门"
    val order: Int,            // 1-6
    val totalLessons: Int,      // 总关卡数
    val completedLessons: Int, // 已完成关卡数
    val isUnlocked: Boolean,   // 是否解锁
    val completedAt: Long?    // 完成时间戳
)
```

### 4.2 GrammarLessonEntity

```kotlin
@Entity(tableName = "grammar_lessons")
data class GrammarLessonEntity(
    @PrimaryKey
    val id: Long,
    val stageId: Long,         // 所属阶段ID
    val title: String,         // "五大基本句型"
    val order: Int,            // 本阶段内的顺序
    val content: String,      // 语法点讲解（Markdown）
    val examples: String,     // 例句（JSON数组）
    val exercises: String,    // 练习题（JSON数组）
    val isCompleted: Boolean, // 是否完成
    val correctRate: Float,    // 正确率
    val lastPracticedAt: Long? // 上次练习时间
)
```

### 4.3 GrammarProgressEntity

```kotlin
@Entity(tableName = "grammar_progress")
data class GrammarProgressEntity(
    @PrimaryKey
    val id: Long = 1,
    val currentStageId: Long,  // 当��进行中的阶段
    val currentLessonId: Long,  // 当前进行中的关卡
    val totalStudyTime: Long,  // 总学习时间（秒）
    val lastStudyDate: Long      // 上次学习日期
)
```

---

## 5. 数据库与DAO

### 5.1 JunoDatabase

在现有数据库中添加新表：

```kotlin
@Database(
    entities = [
        // 现有entities...
        GrammarStageEntity::class,
        GrammarLessonEntity::class,
        GrammarProgressEntity::class
    ],
    version = 2  // 升级版本
)
abstract class JunoDatabase : RoomDatabase() {
    abstract fun grammarStageDao(): GrammarStageDao
    abstract fun grammarLessonDao(): GrammarLessonDao
    abstract fun grammarProgressDao(): GrammarProgressDao
}
```

### 5.2 DAO接口

```kotlin
@Dao
interface GrammarStageDao {
    @Query("SELECT * FROM grammar_stages ORDER BY `order`")
    fun getAllStages(): Flow<List<GrammarStageEntity>>
    
    @Query("SELECT * FROM grammar_stages WHERE id = :id")
    suspend fun getStageById(id: Long): GrammarStageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: GrammarStageEntity)
    
    @Update
    suspend fun updateStage(stage: GrammarStageEntity)
}

@Dao
interface GrammarLessonDao {
    @Query("SELECT * FROM grammar_lessons WHERE stageId = :stageId ORDER BY `order`")
    fun getLessonsByStage(stageId: Long): Flow<List<GrammarLessonEntity>>
    
    @Query("SELECT * FROM grammar_lessons WHERE id = :id")
    suspend fun getLessonById(id: Long): GrammarLessonEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: GrammarLessonEntity)
    
    @Update
    suspend fun updateLesson(lesson: GrammarLessonEntity)
}

@Dao
interface GrammarProgressDao {
    @Query("SELECT * FROM grammar_progress WHERE id = 1")
    fun getProgress(): Flow<GrammarProgressEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: GrammarProgressEntity)
}
```

---

## 6. 导航与路由

### 6.1 Screen定义

```kotlin
sealed class Screen(val route: String) {
    // 现有screens...
    
    // 语法学习
    data object Study : Screen("study")
    data object Grammar : Screen("grammar")
    data object GrammarLesson : Screen("grammar_lesson/{lessonId}") {
        fun createRoute(lessonId: Long) = "grammar_lesson/$lessonId"
    }
}
```

### 6.2 JunoNavHost

```kotlin
composable(Screen.Study.route) {
    StudyScreen(
        onNavigateToFlashcard = { navController.navigate(Screen.Flashcard.route) },
        onNavigateToGrammar = { navController.navigate(Screen.Grammar.route) }
    )
}

composable(Screen.Grammar.route) {
    GrammarScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToLesson = { lessonId -> 
            navController.navigate(Screen.GrammarLesson.createRoute(lessonId))
        }
    )
}

composable(Screen.GrammarLesson.route) { backStackEntry ->
    val lessonId = backStackEntry.arguments?.getString("lessonId")?.toLongOrNull() ?: return@composable
    GrammarLessonScreen(
        lessonId = lessonId,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToNextLesson = { nextId ->
            navController.navigate(Screen.GrammarLesson.createRoute(nextId)) {
                popUpTo(Screen.GrammarLesson.route) { inclusive = true }
            }
        }
    )
}
```

---

## 7. 初始化数据

### 7.1 Grammar数据导入

在AppModule或Application中初始化grammar数据：

```kotlin
object GrammarDataInitializer {
    fun initializeStages() = listOf(
        GrammarStageEntity(
            id = 1,
            name = "第一阶段：构建句子骨架",
            level = "初级·入门",
            order = 1,
            totalLessons = 8,
            completedLessons = 0,
            isUnlocked = true  // 第一个阶段默认解锁
        ),
        GrammarStageEntity(
            id = 2,
            name = "第二阶段：掌握时间与状态",
            level = "初级·核心",
            order = 2,
            totalLessons = 8,
            completedLessons = 0,
            isUnlocked = false
        ),
        // ... 6个阶段
    )
    
    fun initializeLessons() = listOf(
        // 第一阶段8个关卡
        GrammarLessonEntity(
            id = 1,
            stageId = 1,
            title = "五大基本句型",
            order = 1,
            content = readMarkdownFile("docs/grammar/第一阶段/01_句子基本结构.md"),
            examples = readJsonFile("docs/grammar/第一阶段/01_句子基本结构.json"),
            exercises = readJsonFile("docs/grammar/第一阶段/01_练习.json")
        ),
        // ...
    )
}
```

**注意**: 语法内容从 `docs/grammar/` Markdown文件解析，可以：
1. 手动转换JSON格式存储
2. 或者在运行时动态解析Markdown

推荐方案：预先将grammar内容转换为JSON格式存储在 `assets/grammar/` 目录，初始化时导入数据库。

---

## 8. ViewModel

### 8.1 StudyViewModel

```kotlin
@HiltViewModel
class StudyViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val grammarProgressDao: GrammarProgressDao
) : ViewModel() {
    val todayWordCount: StateFlow<Int>
    val grammarStageCount: StateFlow<Int>
    val isLoading: StateFlow<Boolean>
    
    fun getTodayWords(): Flow<List<Word>>
    fun getGrammarStages(): Flow<List<GrammarStageEntity>>
}
```

### 8.2 GrammarViewModel

```kotlin
@HiltViewModel
class GrammarViewModel @Inject constructor(
    private val grammarStageDao: GrammarStageDao,
    private val grammarLessonDao: GrammarLessonDao
) : ViewModel() {
    val stages: StateFlow<List<GrammarStageEntity>>
    val isLoading: StateFlow<Boolean>
    
    fun getStageWithLessons(stageId: Long): Flow<List<GrammarLessonEntity>>
}
```

### 8.3 GrammarLessonViewModel

```kotlin
@HiltViewModel
class GrammarLessonViewModel @Inject constructor(
    private val grammarLessonDao: GrammarLessonDao,
    private val grammarProgressDao: GrammarProgressDao
) : ViewModel() {
    val lesson: StateFlow<GrammarLessonEntity?>
    val currentExerciseIndex: StateFlow<Int>
    val answers: StateFlow<Map<Int, Int>>
    val result: StateFlow<ExerciseResult?>
    
    fun submitAnswer(exerciseId: Int, answer: Int)
    fun submitLesson(): ExerciseResult
    fun nextLesson(): Long?
}
```

---

## 9. UI组件规格（参考frontend.md）

### 9.1 阶段卡片

```kotlin
Card(
    shape = RoundedCornerShape(24.dp),
    modifier = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            shadowElevation = 8f
        }
        .background(Brush.verticalGradient(...)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    // 内容
}
```

### 9.2 进度条

```kotlin
LinearProgressIndicator(
    progress = { progress },
    modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp)),
    color = MaterialTheme.colorScheme.primary,
    trackColor = MaterialTheme.colorScheme.surfaceVariant
)
```

### 9.3 开始学习按钮

```kotlin
Box(
    modifier = Modifier
        .clip(RoundedCornerShape(24.dp))
        .background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFF9D42), Color(0xFFFF6B00))
            )
        )
) {
    Text("开始学习", color = Color.White)
}
```

### 9.4 动画（参考Rule 5）

```kotlin
AnimatedContent(
    targetState = currentStep,
    transitionSpec = {
        (fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400)) { it / 8 })
            .togetherWith(fadeOut(tween(200)))
    }
) { step -> /* 内容 */ }
```

---

## 10. 文件清单

### 新增文件

```
app/src/main/java/com/juno/app/ui/screens/study/
├── StudyScreen.kt
├── StudyViewModel.kt

app/src/main/java/com/juno/app/ui/screens/grammar/
├── GrammarScreen.kt
├── GrammarViewModel.kt
├── GrammarLessonScreen.kt
├── GrammarLessonViewModel.kt

app/src/main/java/com/juno/app/data/local/entity/
├── GrammarStageEntity.kt
├── GrammarLessonEntity.kt
├── GrammarProgressEntity.kt

app/src/main/java/com/juno/app/data/local/dao/
├── GrammarStageDao.kt
├── GrammarLessonDao.kt
├── GrammarProgressDao.kt

app/src/main/java/com/juno/app/data/repository/
├── GrammarRepository.kt (可选)

app/src/main/java/com/juno/app/di/
├── AppModule.kt (添加新Dao)
```

### 修改文件

```
app/src/main/java/com/juno/app/ui/screens/main/MainScreen.kt
- 将"单词"Tab改为"学习"
- Tab内容改为StudyScreen

app/src/main/java/com/juno/app/ui/navigation/Screen.kt
- 添加新Screen定义

app/src/main/java/com/juno/app/ui/navigation/JunoNavHost.kt
- 添加新路由

app/src/main/java/com/juno/app/data/local/JunoDatabase.kt
- 添加新Entity和Dao
```

---

## 11. 实现顺序

1. **数据层** - Entity + DAO + Database
2. **导航** - Screen定义 + JunoNavHost
3. **StudyScreen** - 学习首页入口
4. **GrammarScreen** - 阶段选择页面
5. **GrammarLessonScreen** - 关卡学习页面
6. **数据初始化** - Grammar数据导入
7. **集成测试** - 整体流程测试

---

## 12. 待确认事项

- [ ] grammar内容JSON格式的定义
- [ ] 练习题的答案验证逻辑
- [ ] 闯关完成后的解锁机制
- [ ] 是否需要错题本功能

---

## 附录：UI配色参考

| 用途 | 颜色 |
|------|------|
| 阶段1（入门） | 绿色 #4CAF50 |
| 阶段2（核心） | 蓝色 #2196F3 |
| 阶段3（过渡） | 橙色 #FF9800 |
| 阶段4（进阶） | 紫色 #9C27B0 |
| 阶段5（高级核心） | 红色 #F44336 |
| 阶段6（拔尖） | 金色 #FFD700 |

---

*文档版本: 1.0*