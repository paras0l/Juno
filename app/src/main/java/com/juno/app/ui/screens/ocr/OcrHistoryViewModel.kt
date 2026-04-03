package com.juno.app.ui.screens.ocr

import androidx.lifecycle.ViewModel
import com.juno.app.service.OcrHistoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OcrHistoryViewModel @Inject constructor() : ViewModel() {

    val records: StateFlow<List<com.juno.app.data.model.OcrRecord>> =
        OcrHistoryManager.records

    fun deleteRecord(id: String) = OcrHistoryManager.deleteRecord(id)

    fun clearAll() = OcrHistoryManager.clearAll()
}
