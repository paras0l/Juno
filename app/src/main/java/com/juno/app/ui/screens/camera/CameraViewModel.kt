package com.juno.app.ui.screens.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.remote.AnchorContent
import com.juno.app.data.remote.ImageRecognitionService
import com.juno.app.data.remote.RecognizedLabel
import com.juno.app.data.remote.VisualAnchorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
        private const val PROCESSING_TIMEOUT_MS = 30_000L
    }

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.CameraReady)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _recognizedLabels = MutableStateFlow<List<RecognizedLabel>>(emptyList())
    val recognizedLabels: StateFlow<List<RecognizedLabel>> = _recognizedLabels.asStateFlow()

    private val _anchorContents = MutableStateFlow<List<AnchorContent>>(emptyList())
    val anchorContents: StateFlow<List<AnchorContent>> = _anchorContents.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    private var processingJob: Job? = null
    private var timeoutJob: Job? = null

    fun captureImage(
        imageCapture: ImageCapture,
        context: Context,
        executor: java.util.concurrent.ExecutorService
    ) {
        // Check camera permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.value = CameraUiState.Error("需要相机权限才能使用此功能，请在设置中授予相机权限")
            return
        }

        // Guard: prevent double capture
        val currentState = _uiState.value
        if (currentState is CameraUiState.Capturing || currentState is CameraUiState.Processing) {
            Log.w(TAG, "Capture already in progress, ignoring")
            return
        }

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
                        _uiState.value = CameraUiState.Error("图片处理失败，请确保光线充足后重试")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed", exception)
                    _uiState.value = CameraUiState.Error(
                        "拍照失败: ${exception.message ?: "未知错误"}，请重试"
                    )
                }
            }
        )
    }

    private fun processImage(bitmap: Bitmap) {
        // Cancel any previous processing
        processingJob?.cancel()
        timeoutJob?.cancel()

        processingJob = viewModelScope.launch {
            _uiState.value = CameraUiState.Processing

            try {
                val result = ImageRecognitionService.recognizeObjectsSuspend(bitmap)

                result.fold(
                    onSuccess = { labels ->
                        _recognizedLabels.value = labels

                        if (labels.isEmpty()) {
                            _uiState.value = CameraUiState.Error("未识别到物体，请对准物品后重试")
                            return@fold
                        }

                        val contents = VisualAnchorService.generateAnchorContent(labels)
                        _anchorContents.value = contents
                        timeoutJob?.cancel()

                        _uiState.value = CameraUiState.Success
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Recognition failed", error)
                        _uiState.value = CameraUiState.Error(
                            "识别失败: ${error.message ?: "未知错误"}，请重试"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Processing error", e)
                _uiState.value = CameraUiState.Error(
                    "处理异常: ${e.message ?: "未知错误"}，请重试"
                )
            }
        }

        // Timeout guard: if processing takes too long, auto-recover
        timeoutJob = viewModelScope.launch {
            delay(PROCESSING_TIMEOUT_MS)
            val current = _uiState.value
            if (current is CameraUiState.Processing || current is CameraUiState.Capturing) {
                Log.w(TAG, "Processing timed out, resetting state")
                processingJob?.cancel()
                _uiState.value = CameraUiState.Error("处理超时，请重试")
            }
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        return try {
            val buffer: ByteBuffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap == null) {
                Log.e(TAG, "BitmapFactory.decodeByteArray returned null")
                return null
            }

            val rotationDegrees = image.imageInfo.rotationDegrees
            if (rotationDegrees != 0) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "imageProxyToBitmap failed", e)
            null
        }
    }

    fun resetState() {
        processingJob?.cancel()
        timeoutJob?.cancel()
        _uiState.value = CameraUiState.CameraReady
    }

    fun clearResults() {
        _recognizedLabels.value = emptyList()
        _anchorContents.value = emptyList()
        _capturedBitmap.value = null
    }

    override fun onCleared() {
        super.onCleared()
        processingJob?.cancel()
        timeoutJob?.cancel()
    }
}
