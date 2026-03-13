package com.juno.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility service for capturing screen text when user selects text.
 * Provides listener interface for text detection.
 */
@Suppress("DEPRECATION")
class OcrAccessibilityService : AccessibilityService() {

    companion object {
        var instance: OcrAccessibilityService? = null
            private set
        
        var listener: OcrTextListener? = null
    }

    interface OcrTextListener {
        fun onTextSelected(text: String)
        fun onTextCaptured(bitmap: Bitmap)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                handleTextSelection(event)
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                handleTextFocus(event)
            }
        }
    }

    private fun handleTextSelection(event: AccessibilityEvent) {
        val source = event.source ?: return
        
        // Try to get selected text
        val selectedText = getSelectedText(source)
        if (!selectedText.isNullOrBlank()) {
            listener?.onTextSelected(selectedText)
        }
        
        source.recycle()
    }

    private fun handleTextFocus(event: AccessibilityEvent) {
        val source = event.source ?: return
        
        // Get text content of focused view
        val text = source.text?.toString()
        if (!text.isNullOrBlank()) {
            listener?.onTextSelected(text)
        }
        
        source.recycle()
    }

    private fun getSelectedText(node: AccessibilityNodeInfo): String? {
        // Check if node has selection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val selectionStart = node.textSelectionStart
            val selectionEnd = node.textSelectionEnd
            
            if (selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd) {
                val text = node.text?.toString()
                if (!text.isNullOrBlank()) {
                    return text.substring(selectionStart, selectionEnd)
                }
            }
        }
        
        // Recursively check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = getSelectedText(child)
            child.recycle()
            if (result != null) {
                return result
            }
        }
        
        return null
    }

    /**
     * Capture current screen as bitmap for OCR processing
     */
    fun captureScreen(): Bitmap? {
        return try {
            val rootNode = rootInActiveWindow ?: return null
            val text = getAllVisibleText()
            if (text.isNotBlank()) {
                listener?.onTextSelected(text)
            }
            rootNode.recycle()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get all visible text on screen
     */
    fun getAllVisibleText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val textBuilder = StringBuilder()
        extractTextFromNode(rootNode, textBuilder)
        rootNode.recycle()
        return textBuilder.toString().trim()
    }

    private fun extractTextFromNode(node: AccessibilityNodeInfo, builder: StringBuilder) {
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            builder.append(text).append(" ")
        }
        
        val contentDesc = node.contentDescription?.toString()
        if (!contentDesc.isNullOrBlank()) {
            builder.append(contentDesc).append(" ")
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractTextFromNode(child, builder)
            child.recycle()
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        listener = null
    }
}
