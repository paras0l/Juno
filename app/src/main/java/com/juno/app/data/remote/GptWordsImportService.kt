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
                    // Skip unparseable lines
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
