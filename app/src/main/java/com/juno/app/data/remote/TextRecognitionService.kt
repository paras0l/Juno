package com.juno.app.data.remote

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object TextRecognitionService {

    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeFromBitmap(
        bitmap: Bitmap,
        onResult: (Result<String>) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                if (extractedText.isNotBlank()) {
                    onResult(Result.success(extractedText))
                } else {
                    onResult(Result.failure(Exception("No text found in image")))
                }
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }

    suspend fun recognizeFromBitmapSuspend(bitmap: Bitmap): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            recognizeFromBitmap(bitmap) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
    }

    fun close() {
        recognizer.close()
    }
}
