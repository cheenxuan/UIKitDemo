package com.tech.android.ui.tabkit.top

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.tech.android.ui.tabkit.ITab
import com.tech.android.ui.tabkit.R

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
class TabTop @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), ITab<TabTopInfo<*>> {
    private var tabInfo: TabTopInfo<*>? = null
    private var tabImageView: ImageView? = null
    private var tabNameView: TextView? = null
    private var indicator: View? = null

    init {
        gravity = Gravity.CENTER
        LayoutInflater.from(context).inflate(R.layout.tab_top, this)
        tabImageView = findViewById(R.id.iv_image)
        tabNameView = findViewById(R.id.tv_trans_type)
        indicator = findViewById(R.id.tab_top_indicator)
    }


    override fun setTabInfo(data: TabTopInfo<*>) {
        tabInfo = data
        inflateInfo(false, true)
    }

    private fun inflateInfo(selected: Boolean, init: Boolean) {
        if (tabInfo?.tabType === TabTopInfo.TabType.TEXT) {
            if (init) {
                tabImageView?.visibility = GONE
                tabNameView?.visibility = VISIBLE
                if (!TextUtils.isEmpty(tabInfo?.name)) {
                    tabNameView?.setText(tabInfo?.name)
                }
            }
            if (selected) {
                indicator?.visibility = VISIBLE
                tabNameView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabInfo?.selectedSize ?: 15f)
                tabNameView?.setTextColor(getTextColor(tabInfo?.tintColor!!))
            } else {
                indicator!!.visibility = GONE
                tabNameView!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, tabInfo?.defaultSize ?: 15f)
                tabNameView!!.setTextColor(getTextColor(tabInfo?.defaultColor!!))
            }
        } else if (tabInfo?.tabType === TabTopInfo.TabType.BITMAP) {
            if (init) {
                tabNameView!!.visibility = GONE
                tabImageView!!.visibility = VISIBLE
            }
            if (selected) {
                tabImageView!!.setImageBitmap(tabInfo?.selectedBitmap)
            } else {
                tabImageView!!.setImageBitmap(tabInfo?.defaultBitmap)
            }
        }
    }

    fun getTabInfo(): TabTopInfo<*>? {
        return tabInfo
    }

    fun getTabImageView(): ImageView? {
        return tabImageView
    }

    fun getTabNameView(): TextView? {
        return tabNameView
    }

    override fun resetHeight(height: Int) {
        val layoutParams = layoutParams
        layoutParams.height = height
        setLayoutParams(layoutParams)
        getTabNameView()!!.visibility = GONE
    }

    override fun onTabSelectedChange(index: Int, preInfo: TabTopInfo<*>?, nextInfo: TabTopInfo<*>) {
        if (preInfo !== tabInfo && nextInfo !== tabInfo || preInfo === nextInfo) {
            return
        }
        if (preInfo === tabInfo) {
            inflateInfo(false, false)
        } else {
            inflateInfo(true, false)
        }
    }

    @ColorInt
    private fun getTextColor(color: Any): Int {
        return if (color is String) {
            Color.parseColor(color)
        } else {
            color as Int
        }
    }

    override fun resetWidth(width: Int) {
        val layoutParams = layoutParams
        layoutParams.width = width
        setLayoutParams(layoutParams)
        getTabNameView()!!.visibility = GONE
    }
}