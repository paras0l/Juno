package com.juno.app.service

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.app.NotificationCompat
import com.juno.app.MainActivity
import com.juno.app.R
import com.juno.app.data.local.PreferencesManager
import com.juno.app.data.remote.TextRecognitionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FloatingWindowService : Service() {

    // ─── Companion ────────────────────────────────────────────────────────────
    companion object {
        const val CHANNEL_ID = "floating_window_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.juno.app.ACTION_START_FLOATING"
        const val ACTION_STOP = "com.juno.app.ACTION_STOP_FLOATING"

        @SuppressLint("StaticFieldLeak")
        var instance: FloatingWindowService? = null
            private set

        /** Set by the OcrHistoryScreen via DisposableEffect. */
        var onTextRecognized: ((String) -> Unit)? = null
    }

    // ─── State ────────────────────────────────────────────────────────────────
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isMenuVisible = false
    private var snapAnimator: ValueAnimator? = null

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Peek distance: px the button hides behind edge when snapped */
    private val peekPx by lazy {
        (14 * Resources.getSystem().displayMetrics.density).toInt()
    }
    private val density by lazy { Resources.getSystem().displayMetrics.density }

    // ─── Lifecycle ────────────────────────────────────────────────────────────
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, createNotification())
        serviceScope.launch { showFloatingWindow() }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        snapAnimator?.cancel()
        floatingView?.let { windowManager?.removeView(it) }
        floatingView = null
        instance = null
        serviceScope.cancel()
    }

    // ─── Notification ─────────────────────────────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Floating OCR", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "OCR floating window service" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun createNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, FloatingWindowService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Juno 取词已激活")
            .setContentText("点击悬浮球识别 · 长按弹出菜单")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "关闭", stop)
            .build()
    }

    // ─── Window ───────────────────────────────────────────────────────────────
    private suspend fun showFloatingWindow() {
        if (floatingView != null) return

        val savedX = preferencesManager.floatWindowX.first()
        val savedY = preferencesManager.floatWindowY.first()

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null)

        // Aurora dot center is at marginTop(174dp)+39dp = 213dp from container top.
        // Old layout had aurora at ~55dp from 110dp-tall container top.
        // Extra height shift = 213 - 55 = 158dp so aurora stays at same screen Y.
        val containerExtraH = (158 * density).toInt()

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = maxOf(0, savedY - containerExtraH)
        }

        windowManager?.addView(floatingView, params)
        setupBehavior()
    }

    // ─── Touch & Gesture ──────────────────────────────────────────────────────
    @SuppressLint("ClickableViewAccessibility")
    private fun setupBehavior() {
        val dot = floatingView?.findViewById<View>(R.id.floatingDot) ?: return
        val scanBubble = floatingView?.findViewById<View>(R.id.scanBubble) ?: return
        val closeBubble = floatingView?.findViewById<View>(R.id.closeBubble) ?: return

        // ── Drag tracking ──────────────────────────────────────────────────
        var initTouchX = 0f
        var initTouchY = 0f
        var initWinX = 0
        var initWinY = 0
        var isDragging = false

        // ── Long-press (manual: touch listener consumes ACTION_DOWN) ───────
        var isLongPressed = false
        val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
        val longPressHandler = Handler(Looper.getMainLooper())
        val longPressRunnable = Runnable {
            isLongPressed = true
            showRadialMenu(dot, scanBubble, closeBubble)
        }

        // ── Aurora button touch ────────────────────────────────────────────
        dot.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tap while menu open → close menu
                    if (isMenuVisible) {
                        hideRadialMenu(scanBubble, closeBubble)
                        return@setOnTouchListener true
                    }
                    initTouchX = event.rawX
                    initTouchY = event.rawY
                    initWinX = params?.x ?: 0
                    initWinY = params?.y ?: 0
                    isDragging = false
                    isLongPressed = false
                    view.animate().scaleX(1.12f).scaleY(1.12f).setDuration(120).start()
                    longPressHandler.postDelayed(longPressRunnable, longPressTimeout)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initTouchX
                    val dy = event.rawY - initTouchY
                    if (!isDragging && (Math.abs(dx) > 8 || Math.abs(dy) > 8)) {
                        isDragging = true
                        longPressHandler.removeCallbacks(longPressRunnable)
                    }
                    if (isDragging) {
                        params?.let { p ->
                            p.x = (initWinX + dx).toInt()
                            p.y = (initWinY + dy).toInt()
                            try { windowManager?.updateViewLayout(floatingView, p) }
                            catch (_: Exception) {}
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    longPressHandler.removeCallbacks(longPressRunnable)
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    when {
                        isDragging -> snapToNearestEdge()
                        isLongPressed -> { /* menu already shown */ }
                        else -> captureAndRecognize()
                    }
                    isDragging = false
                    isLongPressed = false
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    longPressHandler.removeCallbacks(longPressRunnable)
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    isDragging = false
                    isLongPressed = false
                    if (!isMenuVisible) snapToNearestEdge()
                    true
                }

                else -> false
            }
        }

        // ── Radial bubble clicks ───────────────────────────────────────────
        scanBubble.setOnClickListener {
            hideRadialMenu(scanBubble, closeBubble)
            captureAndRecognize()
        }
        closeBubble.setOnClickListener {
            hideRadialMenu(scanBubble, closeBubble)
            stopSelf()
        }
    }

    // ─── Radial Menu ──────────────────────────────────────────────────────────
    /**
     * Vertical radial menu: bubbles spring UP from the aurora button.
     * Both bubbles are stacked above floatingDot in the 240×210dp container,
     * so they always appear on-screen regardless of left/right snap position.
     *
     * Animation: translationY from aurora-button area → resting Y (scale+alpha
     * overlay gives spring feel via OvershootInterpolator).
     */
    private fun showRadialMenu(
        dot: View,
        scanBubble: View,
        closeBubble: View
    ) {
        if (isMenuVisible) return
        isMenuVisible = true

        // Per-bubble translationY derived from layout geometry (all values in dp):
        //   floatingDot center = 174 + 39 = 213dp from container top
        //   scanBubble  center =  81 + 26 = 107dp  → offset = 213-107 = 106dp
        //   closeBubble center =  10 + 26 =  36dp  → offset = 213-36  = 177dp
        val scanOffsetY  = (106 * density)
        val closeOffsetY = (177 * density)

        // bringToFront() ensures bubbles render ABOVE floatingDot during animation
        // even though floatingDot has higher elevation in XML z-order.
        scanBubble.bringToFront()
        closeBubble.bringToFront()

        scanBubble.apply {
            visibility = View.VISIBLE
            scaleX = 0f; scaleY = 0f; alpha = 0f
            translationY = scanOffsetY
            animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .translationY(0f)
                .setDuration(380)
                .setInterpolator(OvershootInterpolator(2.2f))
                .start()
        }
        closeBubble.apply {
            visibility = View.VISIBLE
            scaleX = 0f; scaleY = 0f; alpha = 0f
            translationY = closeOffsetY
            animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .translationY(0f)
                .setDuration(420)
                .setInterpolator(OvershootInterpolator(2.0f))
                .setStartDelay(60)
                .start()
        }
        dot.animate().scaleX(0.88f).scaleY(0.88f).setDuration(200).start()

        // Auto-dismiss after 4 s if user takes no action
        val autoHide = Runnable { if (isMenuVisible) hideRadialMenu(scanBubble, closeBubble) }
        dot.tag = autoHide
        dot.postDelayed(autoHide, 4000)
    }

    private fun hideRadialMenu(scanBubble: View, closeBubble: View) {
        if (!isMenuVisible) return
        isMenuVisible = false

        val dot = floatingView?.findViewById<View>(R.id.floatingDot)
        (dot?.tag as? Runnable)?.let { dot.removeCallbacks(it) }

        val scanOffsetY  = (106 * density)
        val closeOffsetY = (177 * density)

        scanBubble.animate()
            .scaleX(0f).scaleY(0f).alpha(0f)
            .translationY(scanOffsetY)
            .setDuration(200)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                scanBubble.visibility = View.INVISIBLE
                // Restore z-order: floatingDot should be on top when menu is hidden
                floatingView?.findViewById<View>(R.id.floatingDot)?.bringToFront()
            }
            .start()

        closeBubble.animate()
            .scaleX(0f).scaleY(0f).alpha(0f)
            .translationY(closeOffsetY)
            .setDuration(180)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { closeBubble.visibility = View.INVISIBLE }
            .start()

        dot?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.start()
    }

    // ─── Edge Snap ────────────────────────────────────────────────────────────
    private fun snapToNearestEdge() {
        val screenWidth = getScreenWidth()
        // Use actual measured window dimensions
        val winW = floatingView?.width?.takeIf { it > 0 } ?: (240 * density).toInt()
        val dotW = floatingView?.findViewById<View>(R.id.floatingDot)
            ?.width?.takeIf { it > 0 } ?: (78 * density).toInt()

        // Aurora button is gravity=center_horizontal → dotOffset = (winW-dotW)/2
        val dotOffset = (winW - dotW) / 2
        val edgePx = peekPx

        val currentX = params?.x ?: 0
        // Aurora button center in screen X coords
        val dotCenterX = currentX + dotOffset + dotW / 2
        val targetX = if (dotCenterX < screenWidth / 2) {
            edgePx - dotOffset          // left snap: dot's left edge = edgePx
        } else {
            screenWidth - dotW - edgePx - dotOffset  // right snap: dot's right edge = edgePx
        }

        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(currentX, targetX).apply {
            duration = 380
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { anim ->
                params?.let { p ->
                    p.x = anim.animatedValue as Int
                    try { windowManager?.updateViewLayout(floatingView, p) }
                    catch (_: Exception) {}
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    serviceScope.launch {
                        preferencesManager.setFloatWindowPosition(
                            targetX, params?.y ?: 0
                        )
                    }
                }
            })
            start()
        }
    }

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager?.currentWindowMetrics?.bounds?.width()
                ?: Resources.getSystem().displayMetrics.widthPixels
        } else {
            @Suppress("DEPRECATION")
            val p = Point()
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.getSize(p)
            p.x
        }
    }

    // ─── OCR ──────────────────────────────────────────────────────────────────
    private fun captureAndRecognize() {
        val accessibility = OcrAccessibilityService.instance
        if (accessibility == null) {
            // Bug 1 fix: save error hint to history unconditionally
            val hint = "无障碍服务未启用\n请前往系统设置 → 无障碍 → Juno OCR"
            OcrHistoryManager.addRecord(hint)
            onTextRecognized?.invoke(hint)
            return
        }

        val bitmap = accessibility.captureScreen()
        if (bitmap != null) {
            TextRecognitionService.recognizeFromBitmap(bitmap) { result ->
                result.onSuccess { text ->
                    // Bug 1 fix: always persist — callback may be null when user leaves screen
                    OcrHistoryManager.addRecord(text)
                    onTextRecognized?.invoke(text)
                }.onFailure {
                    val fallback = accessibility.getAllVisibleText()
                    val out = if (fallback.isNotBlank()) fallback else "识别失败，请重试"
                    OcrHistoryManager.addRecord(out)
                    onTextRecognized?.invoke(out)
                }
            }
        } else {
            val text = accessibility.getAllVisibleText()
            val out = if (text.isNotBlank()) text else "未找到可识别的文字"
            OcrHistoryManager.addRecord(out)
            onTextRecognized?.invoke(out)
        }
    }
}