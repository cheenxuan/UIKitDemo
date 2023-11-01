package com.tech.android.base.camerakit.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.tech.android.base.camerakit.utils.DimensUtil

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
class FrameOverlayView : View {

    companion object {
        private const val CORNER_LEFT_TOP = 1
        private const val CORNER_RIGHT_TOP = 2
        private const val CORNER_RIGHT_BOTTOM = 3
        private const val CORNER_LEFT_BOTTOM = 4
    }

    private var currentCorner = -1
    var margin = 20
    var cornerLength = 100
    var cornerLineWidth = 6

    private val maskColor = Color.argb(180, 0, 0, 0)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eraser = Paint(Paint.ANTI_ALIAS_FLAG)
    private var gestureDetector: GestureDetector? = null
    private val touchRect = RectF()
    private val frameRect = RectF()

    private var onFrameChangeListener: OnFrameChangeListener? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.setStrokeWidth(6f)

        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {}

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init()
    }

    interface OnFrameChangeListener {
        fun onFrameChange(newFrame: RectF?)
    }

    fun getFrameRect(): Rect? {
        val rect = Rect()
        rect.left = frameRect.left.toInt()
        rect.top = frameRect.top.toInt()
        rect.right = frameRect.right.toInt()
        rect.bottom = frameRect.bottom.toInt()
        return rect
    }

    private val onGestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            translate(distanceX, distanceY)
            return true
        }
    }

    fun setOnFrameChangeListener(onFrameChangeListener: OnFrameChangeListener?) {
        this.onFrameChangeListener = onFrameChangeListener
    }

    private fun init() {
        gestureDetector = GestureDetector(context, onGestureListener)
        cornerLength = DimensUtil.dpToPx(18)
        cornerLineWidth = DimensUtil.dpToPx(3)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetFrameRect(w, h)
    }

    private fun resetFrameRect(w: Int, h: Int) {
        if (shapeType == 1) {
            frameRect.left = (w * 0.05).toInt().toFloat()
            frameRect.top = (h * 0.25).toInt().toFloat()
        } else {
            frameRect.left = (w * 0.2).toInt().toFloat()
            frameRect.top = (h * 0.2).toInt().toFloat()
        }
        frameRect.right = w - frameRect.left
        frameRect.bottom = h - frameRect.top
    }

    private var shapeType = 0

    fun setTypeWide() {
        shapeType = 1
    }


    private fun translate(x: Float, y: Float) {
        var x = x
        var y = y
        if (x > 0) {
            // moving left;
            if (frameRect.left - x < margin) {
                x = frameRect.left - margin
            }
        } else {
            if (frameRect.right - x > width - margin) {
                x = frameRect.right - width + margin
            }
        }
        if (y > 0) {
            if (frameRect.top - y < margin) {
                y = frameRect.top - margin
            }
        } else {
            if (frameRect.bottom - y > height - margin) {
                y = frameRect.bottom - height + margin
            }
        }
        frameRect.offset(-x, -y)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(maskColor)
        paint.strokeWidth = DimensUtil.dpToPx(1).toFloat()
        canvas.drawRect(frameRect, paint)
        canvas.drawRect(frameRect, eraser)
        drawCorners(canvas)
    }

    private fun drawCorners(canvas: Canvas) {
        paint.strokeWidth = cornerLineWidth.toFloat()
        // left top
        drawLine(canvas, frameRect.left - cornerLineWidth / 2, frameRect.top, cornerLength, 0)
        drawLine(canvas, frameRect.left, frameRect.top, 0, cornerLength)

        // right top
        drawLine(canvas, frameRect.right + cornerLineWidth / 2, frameRect.top, -cornerLength, 0)
        drawLine(canvas, frameRect.right, frameRect.top, 0, cornerLength)

        // right bottom
        drawLine(canvas, frameRect.right, frameRect.bottom, 0, -cornerLength)
        drawLine(canvas, frameRect.right + cornerLineWidth / 2, frameRect.bottom, -cornerLength, 0)

        // left bottom
        drawLine(canvas, frameRect.left - cornerLineWidth / 2, frameRect.bottom, cornerLength, 0)
        drawLine(canvas, frameRect.left, frameRect.bottom, 0, -cornerLength)
    }

    private fun drawLine(canvas: Canvas, x: Float, y: Float, dx: Int, dy: Int) {
        canvas.drawLine(x, y, x + dx, y + dy, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = handleDown(event)
        val ex = 60f
        val rectExtend = RectF(
            frameRect.left - ex, frameRect.top - ex,
            frameRect.right + ex, frameRect.bottom + ex
        )
        if (!result) {
            if (rectExtend.contains(event.x, event.y)) {
                gestureDetector!!.onTouchEvent(event)
                return true
            }
        }
        return result
    }

    private fun handleDown(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> currentCorner = -1
            MotionEvent.ACTION_DOWN -> {
                val radius = cornerLength.toFloat()
                touchRect[event.x - radius, event.y - radius, event.x + radius] = event.y + radius
                if (touchRect.contains(frameRect.left, frameRect.top)) {
                    currentCorner = CORNER_LEFT_TOP
                    return true
                }
                if (touchRect.contains(frameRect.right, frameRect.top)) {
                    currentCorner = CORNER_RIGHT_TOP
                    return true
                }
                if (touchRect.contains(frameRect.right, frameRect.bottom)) {
                    currentCorner = CORNER_RIGHT_BOTTOM
                    return true
                }
                if (touchRect.contains(frameRect.left, frameRect.bottom)) {
                    currentCorner = CORNER_LEFT_BOTTOM
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> return handleScale(event)
            else -> {}
        }
        return false
    }

    private fun handleScale(event: MotionEvent): Boolean {
        return when (currentCorner) {
            CORNER_LEFT_TOP -> {
                scaleTo(event.x, event.y, frameRect.right, frameRect.bottom)
                true
            }
            CORNER_RIGHT_TOP -> {
                scaleTo(frameRect.left, event.y, event.x, frameRect.bottom)
                true
            }
            CORNER_RIGHT_BOTTOM -> {
                scaleTo(frameRect.left, frameRect.top, event.x, event.y)
                true
            }
            CORNER_LEFT_BOTTOM -> {
                scaleTo(event.x, frameRect.top, frameRect.right, event.y)
                true
            }
            else -> false
        }
    }

    private fun scaleTo(left: Float, top: Float, right: Float, bottom: Float) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (bottom - top < getMinimumFrameHeight()) {
            top = frameRect.top
            bottom = frameRect.bottom
        }
        if (right - left < getMinimumFrameWidth()) {
            left = frameRect.left
            right = frameRect.right
        }
        left = Math.max(margin.toFloat(), left)
        top = Math.max(margin.toFloat(), top)
        right = Math.min((width - margin).toFloat(), right)
        bottom = Math.min((height - margin).toFloat(), bottom)
        frameRect[left, top, right] = bottom
        invalidate()
    }

    private fun notifyFrameChange() {
        if (onFrameChangeListener != null) {
            onFrameChangeListener!!.onFrameChange(frameRect)
        }
    }

    private fun getMinimumFrameWidth(): Float {
        return 2.4f * cornerLength
    }

    private fun getMinimumFrameHeight(): Float {
        return 2.4f * cornerLength
    }
}