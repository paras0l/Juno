package com.juno.app.service

import com.juno.app.data.model.OcrRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * In-process singleton that holds OCR recognition history for the session.
 * Accessible from both [FloatingWindowService] and the Compose UI layer
 * without requiring a database.
 */
object OcrHistoryManager {

    private val _records = MutableStateFlow<List<OcrRecord>>(emptyList())
    val records: StateFlow<List<OcrRecord>> = _records.asStateFlow()

    /** Prepend a new record (newest-first order). */
    fun addRecord(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val record = OcrRecord(
            id = UUID.randomUUID().toString(),
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        _records.value = listOf(record) + _records.value
    }

    fun deleteRecord(id: String) {
        _records.value = _records.value.filter { it.id != id }
    }

    fun clearAll() {
        _records.value = emptyList()
    }
}
