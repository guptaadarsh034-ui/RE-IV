package com.reiv.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

/** Left joystick + 4 action buttons. Emits normalized events via listeners. */
class TouchOverlayView(ctx: Context) : View(ctx) {
    var onStick: ((x: Float, y: Float) -> Unit)? = null
    var onButton: ((name: String, down: Boolean) -> Unit)? = null

    private val base = Paint().apply { color = 0x55FFFFFF; isAntiAlias = true }
    private val knob = Paint().apply { color = 0xAAFFFFFF.toInt(); isAntiAlias = true }
    private val btn = Paint().apply { color = 0x55FFFFFF; isAntiAlias = true }
    private val txt = Paint().apply { color = 0xFFFFFFFF.toInt(); textSize = 36f; textAlign = Paint.Align.CENTER }

    private var stickId = -1
    private var cx = 0f; private var cy = 0f; private var kx = 0f; private var ky = 0f
    private val radius get() = min(width, height) * 0.15f
    private val pressed = mutableMapOf<Int, String>()

    private data class Btn(val name: String, val fx: Float, val fy: Float)
    private val buttons = listOf(Btn("A", .90f, .80f), Btn("B", .82f, .62f), Btn("X", .74f, .80f), Btn("Y", .82f, .92f))
    private val btnR get() = min(width, height) * 0.08f

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        cx = w * .15f; cy = h * .72f; kx = cx; ky = cy
    }

    override fun onDraw(c: Canvas) {
        c.drawCircle(cx, cy, radius, base)
        c.drawCircle(kx, ky, radius * .45f, knob)
        buttons.forEach {
            c.drawCircle(width * it.fx, height * it.fy, btnR, btn)
            c.drawText(it.name, width * it.fx, height * it.fy + 12f, txt)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val i = e.actionIndex; val id = e.getPointerId(i)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = e.getX(i); val y = e.getY(i)
                val b = buttons.firstOrNull { hypot(x - width * it.fx, y - height * it.fy) < btnR * 1.3f }
                if (b != null) { pressed[id] = b.name; onButton?.invoke(b.name, true) }
                else if (x < width / 2 && stickId == -1) { stickId = id; moveStick(x, y) }
            }
            MotionEvent.ACTION_MOVE -> for (p in 0 until e.pointerCount)
                if (e.getPointerId(p) == stickId) moveStick(e.getX(p), e.getY(p))
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                pressed.remove(id)?.let { onButton?.invoke(it, false) }
                if (id == stickId) { stickId = -1; kx = cx; ky = cy; onStick?.invoke(0f, 0f); invalidate() }
            }
        }
        return true
    }

    private fun moveStick(x: Float, y: Float) {
        var dx = x - cx; var dy = y - cy
        val d = hypot(dx, dy)
        if (d > radius) { dx *= radius / d; dy *= radius / d }
        kx = cx + dx; ky = cy + dy
        onStick?.invoke(dx / radius, dy / radius)
        invalidate()
    }
}
