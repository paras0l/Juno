package com.juno.app.data.model

data class OcrRecord(
    val id: String,
    val text: String,
    val timestamp: Long   // System.currentTimeMillis()
)
