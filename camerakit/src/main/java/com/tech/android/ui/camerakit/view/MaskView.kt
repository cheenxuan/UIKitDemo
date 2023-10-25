package com.tech.android.ui.camerakit.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.core.content.res.ResourcesCompat
import com.tech.android.ui.camerakit.R
import java.io.File

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
class MaskView : View {

    companion object {
        const val MASK_TYPE_NONE = 0
        const val MASK_TYPE_ID_CARD_FRONT = 1
        const val MASK_TYPE_ID_CARD_BACK = 2
        const val MASK_TYPE_BANK_CARD = 11
    }

    @IntDef(
        MASK_TYPE_NONE,
        MASK_TYPE_ID_CARD_FRONT,
        MASK_TYPE_ID_CARD_BACK,
        MASK_TYPE_BANK_CARD,
    )
    internal annotation class MaskType

    private var lineColor = Color.WHITE

    private var maskType = MASK_TYPE_ID_CARD_FRONT

    private var maskColor = Color.argb(100, 0, 0, 0)

//    private val eraser = Paint(Paint.ANTI_ALIAS_FLAG)
//    private val pen = Paint(Paint.ANTI_ALIAS_FLAG)

    private val frame = Rect()
    private val path = Path()

//    private var locatorDrawable: Drawable? = null

    init {
        // 硬件加速不支持，图层混合。
        setLayerType(LAYER_TYPE_SOFTWARE, null)

//        pen.color = Color.WHITE
//        pen.style = Paint.Style.STROKE
//        pen.setStrokeWidth(6f)
//
//        eraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
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

    private fun init() {
//        locatorDrawable =
//            ResourcesCompat.getDrawable(resources, R.drawable.camera_id_card_locator_front, null)
    }

    fun getFrameRect(): Rect {
        return if (maskType == MASK_TYPE_NONE) {
            Rect(0, 0, width, height)
        } else {
            Rect(frame)
        }
    }

    fun setLineColor(lineColor: Int) {
        this.lineColor = lineColor
    }

    fun setMaskColor(maskColor: Int) {
        this.maskColor = maskColor
    }

    fun getFrameRectExtend(): Rect {
        val rc = Rect(frame)
        val widthExtend = ((frame.right - frame.left) * 0.02f).toInt()
        val heightExtend = ((frame.bottom - frame.top) * 0.02f).toInt()
        rc.left -= widthExtend
        rc.right += widthExtend
        rc.top -= heightExtend
        rc.bottom += heightExtend
        return rc
    }

    fun setMaskType(@MaskType maskType: Int) {
        this.maskType = maskType
//        when (maskType) {
//            MASK_TYPE_ID_CARD_FRONT -> locatorDrawable = ResourcesCompat.getDrawable(
//                resources,
//                R.drawable.camera_id_card_locator_front, null
//            )
//            MASK_TYPE_ID_CARD_BACK -> locatorDrawable = ResourcesCompat.getDrawable(
//                resources,
//                R.drawable.camera_id_card_locator_back, null
//            )
//            MASK_TYPE_BANK_CARD -> {}
//            MASK_TYPE_NONE -> {}
//            else -> {}
//        }
        invalidate()
    }

    fun getMaskType(): Int {
        return maskType
    }

    fun setOrientation(@CameraView.Orientation orientation: Int) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
//            val ratio = if (h > w) 0.9f else 0.72f
//            val width = (w * ratio).toInt()
//            val height = width * 400 / 620
//            val left = (w - width) / 2
//            val top = (h - height) / 2
//            val right = width + left
//            val bottom = height + top
            frame.left = 0
            frame.top = 0
            frame.right = w
            frame.bottom = h
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        val frame = frame
//        val width = frame.width()
//        val height = frame.height()
//        val left = frame.left
//        val top = frame.top
//        val right = frame.right
//        val bottom = frame.bottom
//        canvas.drawColor(maskColor)
//        fillRectRound(
//            left.toFloat(),
//            top.toFloat(),
//            right.toFloat(),
//            bottom.toFloat(),
//            30f,
//            30f,
//            false
//        )
//        canvas.drawPath(path, pen)
//        canvas.drawPath(path, eraser)
//        if (maskType == MASK_TYPE_ID_CARD_FRONT) {
//            locatorDrawable!!.setBounds(
//                (left + 601f / 1006 * width).toInt(),
//                (top + 110f / 632 * height).toInt(),
//                (left + 963f / 1006 * width).toInt(),
//                (top + 476f / 632 * height).toInt()
//            )
//        } else if (maskType == MASK_TYPE_ID_CARD_BACK) {
//            locatorDrawable!!.setBounds(
//                (left + 51f / 1006 * width).toInt(),
//                (top + 48f / 632 * height).toInt(),
//                (left + 250f / 1006 * width).toInt(),
//                (top + 262f / 632 * height).toInt()
//            )
//        }
//        if (locatorDrawable != null) {
//            locatorDrawable!!.draw(canvas)
//        }
    }


    private fun fillRectRound(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        rx: Float,
        ry: Float,
        conformToOriginalPost: Boolean,
    ): Path {
        var rx = rx
        var ry = ry
        path.reset()
        if (rx < 0) {
            rx = 0f
        }
        if (ry < 0) {
            ry = 0f
        }
        val width = right - left
        val height = bottom - top
        if (rx > width / 2) {
            rx = width / 2
        }
        if (ry > height / 2) {
            ry = height / 2
        }
        val widthMinusCorners = width - 2 * rx
        val heightMinusCorners = height - 2 * ry
        path.moveTo(right, top + ry)
        path.rQuadTo(0f, -ry, -rx, -ry)
        path.rLineTo(-widthMinusCorners, 0f)
        path.rQuadTo(-rx, 0f, -rx, ry)
        path.rLineTo(0f, heightMinusCorners)
        if (conformToOriginalPost) {
            path.rLineTo(0f, ry)
            path.rLineTo(width, 0f)
            path.rLineTo(0f, -ry)
        } else {
            path.rQuadTo(0f, ry, rx, ry)
            path.rLineTo(widthMinusCorners, 0f)
            path.rQuadTo(rx, 0f, rx, -ry)
        }
        path.rLineTo(0f, -heightMinusCorners)
        path.close()
        return path
    }


    private fun capture(file: File) {}
}