package com.juno.app.data.remote

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Data class representing a recognized label from image analysis
 */
data class RecognizedLabel(
    val label: String,
    val confidence: Float
)

/**
 * Service for image recognition using ML Kit Image Labeling
 */
object ImageRecognitionService {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    /**
     * Recognize objects in a bitmap image
     * @param bitmap The bitmap image to analyze
     * @return List of RecognizedLabel with label name and confidence
     */
    fun recognizeObjects(
        bitmap: Bitmap,
        onResult: (Result<List<RecognizedLabel>>) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val recognizedLabels = labels.map { label ->
                    RecognizedLabel(
                        label = label.text,
                        confidence = label.confidence
                    )
                }.sortedByDescending { it.confidence }

                if (recognizedLabels.isNotEmpty()) {
                    onResult(Result.success(recognizedLabels))
                } else {
                    onResult(Result.failure(Exception("No objects recognized in image")))
                }
            }
            .addOnFailureListener { e ->
                onResult(Result.failure(e))
            }
    }

    /**
     * Suspend version of recognizeObjects for coroutine usage
     */
    suspend fun recognizeObjectsSuspend(bitmap: Bitmap): Result<List<RecognizedLabel>> {
        return suspendCancellableCoroutine { continuation ->
            recognizeObjects(bitmap) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
    }

    fun close() {
        labeler.close()
    }
}
