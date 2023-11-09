package com.tech.android.ui.banner.indicator

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.tech.android.ui.banner.R

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class CircleIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), Indicator<FrameLayout> {

    private val VWC = ViewGroup.LayoutParams.WRAP_CONTENT

    /***
     * 正常状态下的指示点
     */
    var mPointNormal: Int = R.drawable.banner_point_normal

    /***
     * 选中状态下的指示点
     */
    var mPointSelected: Int = R.drawable.banner_point_select

    /***
     * 指示点左右内间距
     */
    private var mPointLeftRightPadding = 0

    /***
     * 指示点上下内间距
     */
    private var mPointTopBottomPadding = 0

    init {
        mPointLeftRightPadding = dp2px(5f, context.resources)
        mPointTopBottomPadding = dp2px(8f, context.resources)
    }

    override fun get(): FrameLayout {
        return this
    }

    override fun onInflate(count: Int) {
        removeAllViews()
        if (count < 2) {
            return
        }
        val groupView = LinearLayout(context)
        groupView.orientation = LinearLayout.HORIZONTAL
        var imageView: ImageView
        val imgViewParam = LinearLayout.LayoutParams(VWC, VWC)
        imgViewParam.gravity = Gravity.CENTER_VERTICAL
        imgViewParam.setMargins(
            mPointLeftRightPadding,
            mPointTopBottomPadding,
            mPointLeftRightPadding,
            mPointTopBottomPadding
        )
        for (i in 0 until count) {
            imageView = ImageView(context)
            imageView.layoutParams = imgViewParam
            if (i == 0) {
                imageView.setImageResource(mPointSelected)
            } else {
                imageView.setImageResource(mPointNormal)
            }
            groupView.addView(imageView)
        }
        val groupParam = LayoutParams(VWC, VWC)
        groupParam.gravity = Gravity.CENTER or Gravity.BOTTOM
        addView(groupView, groupParam)
    }

    override fun onPointChange(current: Int, count: Int) {
        //存放指示器的线性布局
        val viewGroup = getChildAt(0) as ViewGroup
        for (i in 0 until viewGroup.childCount) {
            val imageView = viewGroup.getChildAt(i) as ImageView
            if (i == current) {
                imageView.setImageResource(mPointSelected)
            } else {
                imageView.setImageResource(mPointNormal)
            }
            //重新布局一下
            imageView.requestLayout()
        }
    }

    private fun dp2px(dp: Float, resources: Resources): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .toInt()
    }
}