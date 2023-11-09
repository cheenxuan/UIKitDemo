package com.tech.android.ui.tabkit.bottom

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
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
class TabBottom : RelativeLayout, ITab<TabBottomInfo<*>> {

    private var tabNameView: TextView
    private var tabIconView: TextView
    private var tabImageView: ImageView
    private var tabInfo: TabBottomInfo<*>? = null

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0) {}

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        LayoutInflater.from(context).inflate(R.layout.tab_bottom, this)
        tabImageView = findViewById(R.id.iv_iamge)
        tabIconView = findViewById(R.id.tv_icon)
        tabNameView = findViewById(R.id.tv_trans_type)
    }

    override fun setTabInfo(data: TabBottomInfo<*>) {
        this.tabInfo = data
        inflateInfo(selected = false, init = true)

    }

    private fun inflateInfo(selected: Boolean, init: Boolean) {
        if (tabInfo!!.tabType == TabBottomInfo.TabType.ICON) {
            if (init) {
                tabImageView.visibility = View.GONE
                tabIconView.visibility = View.VISIBLE
                val typeface: Typeface =
                    Typeface.createFromAsset(context.assets, tabInfo!!.iconFont)
                tabIconView.typeface = typeface
                if (TextUtils.isEmpty(tabInfo!!.name)) {
                    tabNameView.setText(tabInfo!!.name)
                }
            }

            if (selected) {
                tabIconView.text =
                    if (TextUtils.isEmpty(tabInfo!!.selectedIconName)) tabInfo!!.defaultIconName else tabInfo!!.selectedIconName
                tabIconView.setTextColor(getTextColor(tabInfo!!.tintColor))
                tabNameView.setTextColor(getTextColor(tabInfo!!.tintColor))
            } else {
                tabIconView.text = tabInfo!!.defaultIconName
                tabIconView.setTextColor(getTextColor(tabInfo!!.defaultColor))
                tabNameView.setTextColor(getTextColor(tabInfo!!.defaultColor))
            }
        } else if (tabInfo!!.tabType == TabBottomInfo.TabType.BITMAP) {
            if (init) {
                tabIconView.visibility = View.GONE
                tabImageView.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(tabInfo!!.name)) {
                    tabNameView.text = tabInfo!!.name
                }
            }

            if (selected) {
                tabImageView.setImageBitmap(tabInfo!!.selectedBitmap)
                tabNameView.setTextColor(getTextColor(tabInfo!!.tintColor))
            } else {
                tabImageView.setImageBitmap(tabInfo!!.defaultBitmap)
                tabNameView.setTextColor(getTextColor(tabInfo!!.defaultColor))
            }
        }
    }

    override fun resetHeight(height: Int) {
        val layoutParams: ViewGroup.LayoutParams = layoutParams
        layoutParams.height = height
        setLayoutParams(layoutParams)
        getTabNameView().visibility = View.GONE
    }

    override fun onTabSelectedChange(
        index: Int,
        preInfo: TabBottomInfo<*>?,
        nextInfo: TabBottomInfo<*>,
    ) {
        if (preInfo != tabInfo && nextInfo != tabInfo || preInfo == nextInfo) {
            return
        }

        if (preInfo == tabInfo) {
            inflateInfo(selected = false, init = false)
        } else {
            inflateInfo(selected = true, init = false)
        }
    }

    fun getTabInfo(): TabBottomInfo<*> {
        return tabInfo!!
    }

    fun getTabImageView(): ImageView {
        return tabImageView
    }

    fun getTabIconView(): TextView {
        return tabIconView
    }

    fun getTabNameView(): TextView {
        return tabNameView
    }

    @ColorInt
    private fun getTextColor(color: Any?): Int {
        return if (color is String) {
            Color.parseColor(color)
        } else {
            color as Int
        }
    }

    override fun resetWidth(width: Int) {
        val layoutParams: ViewGroup.LayoutParams = layoutParams
        layoutParams.width = width
        setLayoutParams(layoutParams)
        getTabNameView().visibility = View.GONE
    }


}