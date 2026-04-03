package com.juno.app.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator

/**
 * Custom View that renders an aurora-gradient glow ring around a dark inner circle.
 *
 * Visual layers (inside-out):
 *   1. Deep navy inner circle  (solid fill)
 *   2. Thin white inner stroke (subtle border)
 *   3. Rotating SweepGradient ring (aurora: blue → purple → cyan)
 *
 * Animations:
 *   - Continuous rotation of the gradient sweep (4 s / rev)
 *   - Breathing alpha on the glow ring (0.5 → 1.0, 2.4 s, reverse)
 */
class AuroraPulseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── State ────────────────────────────────────────────────────────────────
    private var sweepAngle = 0f
    private var glowAlpha = 0.7f

    // ── Paints ───────────────────────────────────────────────────────────────
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    // ── Animators ────────────────────────────────────────────────────────────
    private val rotationAnim = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 4000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            sweepAngle = it.animatedValue as Float
            invalidate()
        }
    }

    private val breathAnim = ValueAnimator.ofFloat(0.45f, 1.0f).apply {
        duration = 2400
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            glowAlpha = it.animatedValue as Float
            invalidate()
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        rotationAnim.start()
        breathAnim.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rotationAnim.cancel()
        breathAnim.cancel()
    }

    // ── Drawing ──────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val outerR = minOf(cx, cy) - 2f
        val ringW = outerR * 0.20f          // glow ring width = 20 % of radius
        val innerR = outerR - ringW - 3f    // inner circle radius

        // 1. Primary blue background circle
        bgPaint.color = android.graphics.Color.parseColor("#2196F3")
        bgPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, innerR, bgPaint)

        // 2. Rotating aurora glow ring
        canvas.save()
        canvas.rotate(sweepAngle, cx, cy)

        val auroraColors = intArrayOf(
            0xFF1565C0.toInt(),  // deep blue
            0xFF7B1FA2.toInt(),  // violet
            0xFF00838F.toInt(),  // teal/cyan
            0xFF1A237E.toInt(),  // indigo
            0x001565C0.toInt(),  // fade to transparent
            0xFF1565C0.toInt()   // close gradient loop
        )
        ringPaint.shader = SweepGradient(cx, cy, auroraColors, null)
        ringPaint.strokeWidth = ringW
        ringPaint.alpha = (glowAlpha * 255).toInt()
        canvas.drawCircle(cx, cy, outerR - ringW / 2f, ringPaint)
        canvas.restore()

        // 3. Subtle white inner border
        borderPaint.color = Color.argb((glowAlpha * 50).toInt(), 255, 255, 255)
        canvas.drawCircle(cx, cy, innerR, borderPaint)
    }
}
