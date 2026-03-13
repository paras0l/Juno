package com.juno.app.data.remote

import android.content.Context
import android.net.Uri
import com.juno.app.data.local.entity.WordEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelImportService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class ImportResult(
        val success: Boolean,
        val importedCount: Int = 0,
        val skippedCount: Int = 0,
        val errorMessage: String? = null,
        val words: List<WordEntity> = emptyList()
    )

    suspend fun importWords(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult(
                    success = false,
                    errorMessage = "无法打开文件"
                )

            val words = parseExcel(inputStream)
            inputStream.close()

            if (words.isEmpty()) {
                return@withContext ImportResult(
                    success = false,
                    errorMessage = "Excel文件中没有找到有效的单词数据"
                )
            }

            ImportResult(
                success = true,
                importedCount = words.size,
                words = words
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                errorMessage = "解析Excel文件失败: ${e.message}"
            )
        }
    }

    private fun parseExcel(inputStream: InputStream): List<WordEntity> {
        val words = mutableListOf<WordEntity>()

        WorkbookFactory.create(inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            
            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue

                val wordCell = row.getCell(0) ?: continue
                val word = getCellValueAsString(wordCell).trim()
                if (word.isBlank()) continue

                val phoneticCell = row.getCell(1)
                val phonetic = phoneticCell?.let { getCellValueAsString(it).trim() }?.takeIf { it.isNotBlank() }

                val meaningCell = row.getCell(2) ?: continue
                val meaning = getCellValueAsString(meaningCell).trim()
                if (meaning.isBlank()) continue

                val exampleCell = row.getCell(3)
                val example = exampleCell?.let { getCellValueAsString(it).trim() }?.takeIf { it.isNotBlank() }

                val translationCell = row.getCell(4)
                val translation = translationCell?.let { getCellValueAsString(it).trim() }?.takeIf { it.isNotBlank() }

                val wordEntity = WordEntity(
                    word = word,
                    phonetic = phonetic,
                    meaning = meaning,
                    example = example,
                    translation = translation,
                    difficulty = 1,
                    category = null,
                    tags = null,
                    isLearned = false
                )

                words.add(wordEntity)
            }
        }

        return words
    }

    private fun getCellValueAsString(cell: org.apache.poi.ss.usermodel.Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue ?: ""
            CellType.NUMERIC -> {
                cell.numericCellValue.toLong().toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue ?: ""
                } catch (e: Exception) {
                    cell.numericCellValue.toString()
                }
            }
            CellType.BLANK -> ""
            CellType._NONE -> ""
            else -> ""
        }
    }

    companion object {
        val SUPPORTED_EXTENSIONS = listOf("xlsx", "xls")
        
        val SUPPORTED_MIME_TYPES = listOf(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
        )
    }
}
