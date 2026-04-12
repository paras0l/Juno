package com.juno.app.data.remote

import android.content.Context
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
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
            val reader = java.io.InputStreamReader(inputStream, "UTF-8")
            val jsonArray = Gson().fromJson(reader, JsonArray::class.java)
            
            val words = mutableListOf<WordEntity>()
            
            for (jsonElement in jsonArray) {
                 val jsonObject = jsonElement as JsonObject
                 
                 val word = jsonObject.get("word").getAsString()
                 if (word.isBlank()) continue
                 
                 val phonetic = jsonObject.get("phonetic").getAsString()
                 
                 // Extract definitions from JSON array and format as string
                 val definitions = jsonObject.getAsJsonArray("definitions")
                     .map { 
                         val obj = it.asJsonObject
                         "${obj.get("pos").getAsString()}: ${obj.get("mean").getAsString()}"
                     }
                     .joinToString("; ")
                 
                 val sentence = jsonObject.get("sentence").getAsString()
                 val sentenceTranslation = jsonObject.get("sentence_translation").getAsString()
                 val etymology = jsonObject.get("etymology").getAsString()
                 
                 // Extract collocations from JSON array and format as string
                 val collocations = jsonObject.getAsJsonArray("collocations")
                     .map { it.getAsString() }
                     .joinToString(", ")
                
                words.add(
                    WordEntity(
                        word = word,
                        phonetic = phonetic,
                        definitions = definitions,
                        sentence = sentence,
                        sentenceTranslation = sentenceTranslation,
                        etymology = etymology,
                        collocations = collocations,
                        difficulty = 1,
                        isLearned = false
                    )
                )
            }
            
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
}