package com.tech.android.base.camerakit.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.tech.android.base.camerakit.R

/**
 * @auther: xuan
 * @date  : 2023/10/19 .
 * <P>
 * Description:
 * <P>
 */
class CameraLayout : FrameLayout {

    companion object {
        const val ORIENTATION_PORTRAIT = 0
        const val ORIENTATION_HORIZONTAL = 1
    }

    private var orientation = ORIENTATION_PORTRAIT
    private var contentView: View? = null
    private var centerView: View? = null
    private var leftDownView: View? = null
    private var rightUpView: View? = null
    private var idCardExamView: View? = null
    private var idCardBackExamView: View? = null
    private var bankCardExamView: View? = null

    private var contentViewId = -1
    private var centerViewId = -1
    private var leftDownViewId = -1
    private var rightUpViewId = -1
    private var idCardExamViewId = -1
    private var idCardBackExamViewId = -1
    private var bankCardExamViewId = -1

    private val backgroundRect = Rect()
    private val paint: Paint = Paint()

    init {
        setWillNotDraw(false)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(83, 0, 0, 0)
    }

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {}

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        parseAttrs(attributeSet)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.cameralayout)
        try {
            contentViewId = a.getResourceId(R.styleable.cameralayout_contentView, -1)
            centerViewId = a.getResourceId(R.styleable.cameralayout_centerView, -1)
            leftDownViewId = a.getResourceId(R.styleable.cameralayout_leftDownView, -1)
            rightUpViewId = a.getResourceId(R.styleable.cameralayout_rightUpView, -1)
            idCardExamViewId = a.getResourceId(R.styleable.cameralayout_idCardExamView, -1)
            idCardBackExamViewId = a.getResourceId(R.styleable.cameralayout_idCardBackExamView, -1)
            bankCardExamViewId = a.getResourceId(R.styleable.cameralayout_bankCardExamView, -1)
        } finally {
            a.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        contentView = findViewById(contentViewId)
        if (centerViewId != -1) {
            centerView = findViewById(centerViewId)
        }
        leftDownView = findViewById(leftDownViewId)
        rightUpView = findViewById(rightUpViewId)
        idCardExamView = findViewById(idCardExamViewId)
        idCardBackExamView = findViewById(idCardBackExamViewId)
        bankCardExamView = findViewById(bankCardExamViewId)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = width
        val height = height
        var left: Int
        var top: Int
        val leftDownViewLayoutParams = leftDownView?.layoutParams as MarginLayoutParams
        val rightUpViewLayoutParams = rightUpView?.layoutParams as MarginLayoutParams
        if (r < b) {
            //按钮需要的高度
            var heightLeft = height - applyUnit(TypedValue.COMPLEX_UNIT_DIP, 60f)
            //padding
            heightLeft -= applyUnit(TypedValue.COMPLEX_UNIT_DIP, 40f)

            if (idCardExamView?.visibility == VISIBLE || idCardBackExamView?.visibility == VISIBLE || bankCardExamView?.visibility == VISIBLE) {
                heightLeft -= applyUnit(TypedValue.COMPLEX_UNIT_DIP, 140f)
            }

            var contentHeight = Math.min(heightLeft , width * 4 / 3)
            contentView?.layout(l, t, r, contentHeight)
            backgroundRect.left = 0
            backgroundRect.top = contentHeight
            backgroundRect.right = width
            backgroundRect.bottom = height


            if (idCardExamView != null && idCardExamView?.visibility == VISIBLE) {
                val examHeight = applyUnit(TypedValue.COMPLEX_UNIT_DIP, 140f)
                idCardExamView?.layout(l, contentHeight, r, contentHeight + examHeight)
                contentHeight += examHeight
            }
            if (idCardBackExamView != null && idCardBackExamView?.visibility == VISIBLE) {
                val examHeight = applyUnit(TypedValue.COMPLEX_UNIT_DIP, 140f)
                idCardBackExamView?.layout(l, contentHeight, r, contentHeight + examHeight)
                contentHeight += examHeight
            }
            if (bankCardExamView != null && bankCardExamView?.visibility == VISIBLE) {
                val examHeight = applyUnit(TypedValue.COMPLEX_UNIT_DIP, 140f)
                bankCardExamView?.layout(l, contentHeight, r, contentHeight + examHeight)
                contentHeight += examHeight
            }

            val buttonHeight = height - contentHeight
            // layout centerView;
            if (centerView != null) {
                left = (width - centerView!!.measuredWidth) / 2
                top = contentHeight + (buttonHeight - centerView!!.measuredHeight) / 2
                centerView?.layout(
                    left,
                    top,
                    left + centerView!!.measuredWidth,
                    top + centerView!!.measuredHeight
                )
            }
            // layout leftDownView
            if (leftDownView != null) {
                left = leftDownViewLayoutParams.leftMargin
                top = contentHeight + (buttonHeight - leftDownView!!.measuredHeight) / 2
                leftDownView?.layout(
                    left,
                    top,
                    left + leftDownView!!.measuredWidth,
                    top + leftDownView!!.measuredHeight
                )
            }

            // layout rightUpView
            if (rightUpView != null) {
                left =
                    width - (rightUpView?.measuredWidth ?: 0) - rightUpViewLayoutParams.rightMargin
                top = contentHeight + (buttonHeight - rightUpView!!.measuredHeight) / 2
                rightUpView?.layout(
                    left,
                    top,
                    left + rightUpView!!.measuredWidth,
                    top + rightUpView!!.measuredHeight
                )
            }
        } else {
            val contentWidth = height * 4 / 3
            val widthLeft = width - contentWidth
            contentView?.layout(l, t, contentWidth, height)
            backgroundRect.left = contentWidth
            backgroundRect.top = 0
            backgroundRect.right = width
            backgroundRect.bottom = height

            // layout centerView
            if (centerView != null) {
                left = contentWidth + (widthLeft - centerView!!.measuredWidth) / 2
                top = (height - (centerView?.measuredHeight ?: 0)) / 2
                centerView?.layout(
                    left,
                    top,
                    left + centerView!!.measuredWidth,
                    top + centerView!!.measuredHeight
                )
            }
            // layout leftDownView
            if (leftDownView != null) {
                left = contentWidth + (widthLeft - leftDownView!!.measuredWidth) / 2
                top = height - leftDownView!!.measuredHeight - leftDownViewLayoutParams.bottomMargin
                leftDownView?.layout(
                    left,
                    top,
                    left + leftDownView!!.measuredWidth,
                    top + leftDownView!!.measuredHeight
                )
            }

            // layout rightUpView
            if (rightUpView != null) {
                left = contentWidth + (widthLeft - rightUpView!!.measuredWidth) / 2
                top = rightUpViewLayoutParams.topMargin
                rightUpView?.layout(
                    left,
                    top,
                    left + rightUpView!!.measuredWidth,
                    top + rightUpView!!.measuredHeight
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(backgroundRect, paint)
    }

    fun setOrientation(orientation: Int) {
        if (this.orientation == orientation) {
            return
        }
        this.orientation = orientation
        requestLayout()
    }

    private fun applyUnit(unit: Int, value: Float): Int {
        return TypedValue.applyDimension(unit, value, resources.displayMetrics).toInt()
    }
}