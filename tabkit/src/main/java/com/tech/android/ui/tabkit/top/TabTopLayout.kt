package com.tech.android.ui.tabkit.top

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import com.tech.android.ui.tabkit.ITabLayout
import com.tech.android.ui.tabkit.R

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
class TabTopLayout @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
) : HorizontalScrollView(context, attrs, defStyleAttr), ITabLayout<TabTop, TabTopInfo<*>> {
    private val tabSelectedListeners: MutableList<ITabLayout.OnTabSelectedListener<TabTopInfo<*>>> =
        ArrayList<ITabLayout.OnTabSelectedListener<TabTopInfo<*>>>()
    private var selectedInfo: TabTopInfo<*>? = null
    private var infoList: List<TabTopInfo<*>?>? = null
    private var canScroll = true
    private var fullScreen = false
    
    init {
        init()
        isVerticalScrollBarEnabled = false
    }

    private fun init() {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.tabtoplayout)
        canScroll = typedArray.getBoolean(R.styleable.tabtoplayout_canScroll, true)
        fullScreen = typedArray.getBoolean(R.styleable.tabtoplayout_fullScrenn, false)
        isNestedScrollingEnabled = canScroll
        typedArray.recycle()
    }

    override fun findTab(data: TabTopInfo<*>): TabTop? {
        val ll: ViewGroup = getRootLayout(false)
        for (i in 0 until ll.childCount) {
            val child = ll.getChildAt(i)
            if (child is TabTop) {
                val tab = child
                if (tab.getTabInfo() === data) {
                    return tab
                }
            }
        }
        return null
    }

    override fun addTabSelectedChangeListener(listener: ITabLayout.OnTabSelectedListener<TabTopInfo<*>>) {
        tabSelectedListeners.add(listener)
    }

    override fun defaultSelect(defaultInfo: TabTopInfo<*>) {
        onSelected(defaultInfo)
    }

    private fun onSelected(nextInfo: TabTopInfo<*>) {
        for (listener in tabSelectedListeners) {
            listener.onTabSelectedChange(infoList!!.indexOf(nextInfo), selectedInfo, nextInfo)
        }
        selectedInfo = nextInfo
        autoScroll(nextInfo)
    }

    var tabWith = 0

    /**
     * 自动滚动，实现点击的位置能够自动滚动以展示前后2个
     *
     * @param nextInfo 点击tab的info
     */
    private fun autoScroll(nextInfo: TabTopInfo<*>) {
        val tabTop = findTab(nextInfo) ?: return
        val index = infoList!!.indexOf(nextInfo)
        val loc = IntArray(2)
        tabTop.getLocationInWindow(loc)
        if (tabWith == 0) {
            tabWith = tabTop.width
        }

        //判断点击了屏幕左侧还是右侧
        val scrollWidth: Int = if (loc[0] + tabWith / 2 > getDisplayWidthInPx(context) / 2) {
                rangeScrollWidth(index, 2)
            } else {
                rangeScrollWidth(index, -2)
            }
        smoothScrollTo(scrollX + scrollWidth, 0)
    }

    /**
     * 获取可滚动的范围
     *
     * @param index 从第几个开始
     * @param range 向前向后的范围
     * @return 可滚动的范围
     */
    private fun rangeScrollWidth(index: Int, range: Int): Int {
        var scrollWidth = 0
        for (i in 0..Math.abs(range)) {
            var next: Int
            next = if (range < 0) {
                range + i + index
            } else {
                range - i + index
            }
            if (next >= 0 && next < infoList!!.size) {
                if (range < 0) {
                    scrollWidth -= scrollWidth(next, false)
                } else {
                    scrollWidth += scrollWidth(next, true)
                }
            }
        }
        return scrollWidth
    }

    /**
     * 指定位置的控件可滚动的距离
     *
     * @param index   指定位置的控件
     * @param toRight 是否是点击了屏幕右侧
     * @return 可滚动的距离
     */
    private fun scrollWidth(index: Int, toRight: Boolean): Int {
        val target = findTab(infoList!![index]!!) ?: return 0
        val rect = Rect()
        val visible = target.getLocalVisibleRect(rect)
        return if (!visible) {
            tabWith
        } else {
            tabWith - (rect.right - rect.left)
        }
    }

    private fun getRootLayout(clear: Boolean): LinearLayout {
        var rootView = getChildAt(0) as LinearLayout
        if (rootView == null) {
            rootView = LinearLayout(context)
            rootView.orientation = LinearLayout.HORIZONTAL
            if (fullScreen) {
                val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                params.gravity = Gravity.CENTER
                addView(rootView, params)
                isFillViewport = true
            } else {
                val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                params.gravity = Gravity.CENTER_VERTICAL
                addView(rootView, params)
            }
        } else if (clear) {
            rootView.removeAllViews()
        }
        return rootView
    }


    override fun inflateInfo(infoList: List<TabTopInfo<*>>) {
        if (infoList.isEmpty()) return
        this.infoList = infoList
        val linearLayout = getRootLayout(true)
        selectedInfo = null
        //清除之前添加的HiTabBottom listener tips：java foreach remove的问题
        val iterator: MutableIterator<ITabLayout.OnTabSelectedListener<TabTopInfo<*>>> =
            tabSelectedListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() is TabTop) {
                iterator.remove()
            }
        }
        for (i in infoList.indices) {
            val info = infoList[i]
            val tab = TabTop(context)
            tabSelectedListeners.add(tab)
            tab.setTabInfo(info)
            if (fullScreen) {
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
                params.gravity = Gravity.CENTER
                params.weight = 1f
                linearLayout.addView(tab, params)
            } else {
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                linearLayout.addView(tab, params)
            }
            tab.setOnClickListener { onSelected(info) }
        }
    }

    fun getDisplayWidthInPx(@NonNull context: Context): Int {
        val wm: WindowManager? = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null) {
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.x
        }

        return 0
    }

}