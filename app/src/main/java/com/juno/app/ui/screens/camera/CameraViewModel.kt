package com.juno.app.ui.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.juno.app.data.remote.AnchorContent
import com.juno.app.data.remote.ImageRecognitionService
import com.juno.app.data.remote.RecognizedLabel
import com.juno.app.data.remote.VisualAnchorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.CameraReady)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _recognizedLabels = MutableStateFlow<List<RecognizedLabel>>(emptyList())
    val recognizedLabels: StateFlow<List<RecognizedLabel>> = _recognizedLabels.asStateFlow()

    private val _anchorContents = MutableStateFlow<List<AnchorContent>>(emptyList())
    val anchorContents: StateFlow<List<AnchorContent>> = _anchorContents.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    fun captureImage(
        imageCapture: ImageCapture,
        @Suppress("UNUSED_PARAMETER") context: Context,
        executor: java.util.concurrent.ExecutorService
    ) {
        _uiState.value = CameraUiState.Capturing

        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = imageProxyToBitmap(image)
                    image.close()

                    if (bitmap != null) {
                        _capturedBitmap.value = bitmap
                        processImage(bitmap)
                    } else {
                        _uiState.value = CameraUiState.Error("Failed to process image")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _uiState.value = CameraUiState.Error(exception.message ?: "Capture failed")
                }
            }
        )
    }

    private fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Processing

            try {
                val result = ImageRecognitionService.recognizeObjectsSuspend(bitmap)

                result.fold(
                    onSuccess = { labels ->
                        _recognizedLabels.value = labels

                        if (labels.isEmpty()) {
                            _uiState.value = CameraUiState.Error("未识别到物体，请重试")
                            return@fold
                        }

                        val contents = VisualAnchorService.generateAnchorContent(labels)
                        _anchorContents.value = contents

                        _uiState.value = CameraUiState.Success
                    },
                    onFailure = { error ->
                        _uiState.value = CameraUiState.Error(error.message ?: "Recognition failed")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        return try {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val rotationDegrees = image.imageInfo.rotationDegrees
            if (rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.CameraReady
    }

    fun clearResults() {
        _recognizedLabels.value = emptyList()
        _anchorContents.value = emptyList()
        _capturedBitmap.value = null
    }
}
