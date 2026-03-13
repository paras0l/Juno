package com.juno.app.data.local

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var currentFilePath: String? = null

    fun startRecording(): String? {
        if (_isRecording.value) {
            return currentFilePath
        }

        val audioDir = File(context.filesDir, "recordings")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val fileName = "recording_${System.currentTimeMillis()}.m4a"
        val file = File(audioDir, fileName)
        currentFilePath = file.absolutePath

        mediaRecorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(currentFilePath)

            try {
                prepare()
                start()
                _isRecording.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                release()
                currentFilePath = null
                return null
            }
        }

        return currentFilePath
    }

    fun stopRecording(): String? {
        if (!_isRecording.value) {
            return null
        }

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
            currentFilePath
        } catch (e: Exception) {
            e.printStackTrace()
            cancelRecording()
            null
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            mediaRecorder?.release()
        }
        mediaRecorder = null
        _isRecording.value = false

        currentFilePath?.let { path ->
            File(path).delete()
        }
        currentFilePath = null
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    fun release() {
        cancelRecording()
    }
}
