---
name: englishwords
description: Manages shadcn components and projects — adding, searching, fixing, debugging, styling, and composing UI. Provides project context, component docs, and usage examples. Applies when working with shadcn/ui, component registries, presets, --preset codes, or any project with a components.json file. Also triggers for "shadcn init", "create an app with --preset", or "switch to --preset".
user-invocable: false
---
## 1. 基本信息 (Basic Info)
* **Skill 名称**: `englishwords`
* **版本**: 1.0.0
* **描述**: 一个用于自动化处理英语词汇数据的专业工具。它能读取 Excel 文件中的基础单词，通过 LLM 深度增强语义信息，并生成结构化的 JSON 知识库。
* **核心能力**: 数据解析、语义扩展、词根词缀分析、结构化输出。

## 2. 输入参数 (Input Parameters)
Agent 调用此 Skill 时需提供以下参数：

| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| `input_excel_path` | String | 是 | 待处理的 Excel 文件路径（需包含 word, phonetic, definition 列） |
| `output_json_path` | String | 是 | 最终生成的 JSON 文件存储路径 |
| `batch_size` | Integer | 否 | 每次并发处理的单词数量，默认为 5 |

## 3. 处理逻辑 (Execution Logic)

Skill 内部执行逻辑如下：

1.  **数据挂载**: 使用 Python `pandas` 读取 `input_excel_path`。
2.  **原子化处理**:
    * 读取 `word`（单词）、`phonetic`（音标）、`definition`（基础释义）。
    * 即使 Excel 已有音标和释义，Skill 仍会核对音标的准确性（IPA 标准）。
3.  **LLM 知识增强**:
    * 将 `definition` 从字符串转换为对象数组（如 `[{"pos": "v", "mean": "..."}]`）。
    * 生成符合语境的高质量**例句**及**翻译**。
    * 执行 **Etymology 分析**（解析词根 `Root` 和词缀 `Affix`）。
    * 检索常用 **Collocations**（词组搭配）。
4.  **容错处理**: 若某词汇没有常见搭配，该字段返回空数组 `[]`。
5.  **序列化**: 将所有处理后的行聚合，生成标准的 JSON 文件。

---

## 4. 输出数据结构 (Output Schema)

Skill 最终输出的 JSON 数组中，每个对象必须严格遵守以下 schema：

```json
{
  "word": "String",                 // 单词
  "phonetic": "String",             // 音标，格式如 /ˈeniweɪ/
  "definitions": [                  // 释义数组
    {
      "pos": "String",              // 词性缩写 (n./v./adj./adv.等)
      "mean": "String"              // 中文释义
    }
  ],
  "sentence": "String",             // 英文例句
  "sentence_translation": "String", // 例句翻译
  "etymology": "String",            // 词根词缀分析 (例如: ad- "to" + pretium "price")
  "collocations": ["String"]        // 词组搭配数组，无则为空 []
}
```

---

## 5. 提示词模板 (Internal Prompt Template)

Skill 在执行增强任务时调用的底层指令：

> **Role**: 你是一个资深的英语辞书编纂家。
> **Input**: 单词 {{word}}, 音标 {{phonetic}}, 原始释义 {{definition}}。
> **Task**:
> 1. 解析原始释义，按词性拆分为数组。
> 2. 为该词创建一个中等难度的例句，并翻译成中文。
> 3. 分析其词源或词根词缀，帮助用户记忆。
> 4. 列出 2-3 个最常用的词组搭配。
     > **Constraint**: 必须以纯 JSON 格式返回，不得包含任何解释性文字。

---

