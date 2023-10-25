package com.tech.android.ui.tabkit.bottom

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.annotation.NonNull
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.tech.android.ui.tabkit.ITabLayout
import com.tech.android.ui.tabkit.R


/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
class TabBottomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), ITabLayout<TabBottom, TabBottomInfo<*>> {

    companion object {
        const val TAG_TAB_BOTTOM = "TAG_TAB_BOTTOM"
    }

    private var bottomAlpha = 1f
    private var bottomLineHeight = 0.5f
    private var tabBottomHeight = 49f
    private var bottomLineColor = "#dfe0e1"
    private var selectedInfo: TabBottomInfo<*>? = null
    private var infoList: List<TabBottomInfo<*>>? = null
    private val tabSelectedListeners =
        mutableListOf<ITabLayout.OnTabSelectedListener<TabBottomInfo<*>>>()

    override fun findTab(data: TabBottomInfo<*>): TabBottom? {
        val ll = findViewWithTag<ViewGroup>(TAG_TAB_BOTTOM)
        for (child: View in ll.children) {
            if (child is TabBottom) {
                if (child.getTabInfo() == data) {
                    return child
                }
            }
        }
        return null
    }

    override fun addTabSelectedChangeListener(listener: ITabLayout.OnTabSelectedListener<TabBottomInfo<*>>) {
        tabSelectedListeners.add(listener)
    }

    override fun defaultSelect(defaultInfo: TabBottomInfo<*>) {
        onSelected(defaultInfo)
    }

    override fun inflateInfo(infoList: List<TabBottomInfo<*>>) {
        if (infoList.isEmpty()) {
            return
        }

        this.infoList = infoList
        //移除之前已经添加的view
        val size = childCount - 1
        for (i in size downTo 1) {
            removeViewAt(i)
        }

        selectedInfo = null
        setBackGround()
        //清楚之前添加TabBottom listener tips：java foreach remove的问题
        val iterator: MutableIterator<ITabLayout.OnTabSelectedListener<TabBottomInfo<*>>> =
            tabSelectedListeners.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() is TabBottom) {
                iterator.remove()
            }
        }
        val height = dp2px(tabBottomHeight, resources)
        val fl = FrameLayout(context)
        fl.tag = TAG_TAB_BOTTOM
        val width = getDisplayWidthInPx(context) / infoList.size
        for ((index, value) in infoList.withIndex()) {
            val info = infoList.get(index)
            //tips: 为何不用LinearLayout 当动态改变child大小后gravity.bottom会失效
            val params = LayoutParams(width, height)
            params.gravity = Gravity.BOTTOM
            params.leftMargin = index * width

            val tabBottom = TabBottom(context)
            tabSelectedListeners.add(tabBottom)
            tabBottom.setTabInfo(info)
            fl.addView(tabBottom, params)
            tabBottom.setOnClickListener {
                onSelected(info)
            }

        }
        val flParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        flParams.gravity = Gravity.BOTTOM
        addBottomLine()
        addView(fl, flParams)
        fixContentView()
    }

    private fun addBottomLine() {
        val bottomLine = View(context)
        bottomLine.setBackgroundColor(Color.parseColor(bottomLineColor))
        val params =
            LayoutParams(LayoutParams.MATCH_PARENT, dp2px(bottomLineHeight, resources))
        params.gravity = Gravity.BOTTOM
        params.bottomMargin = dp2px(tabBottomHeight - bottomLineHeight, resources)
        addView(bottomLine, params)
        bottomLine.alpha = bottomAlpha
    }

    private fun onSelected(nextinfo: TabBottomInfo<*>) {
        for (listener in tabSelectedListeners) {
            listener.onTabSelectedChange(infoList!!.indexOf(nextinfo), selectedInfo, nextinfo)
        }
        this.selectedInfo = nextinfo
    }

    private fun setBackGround() {
        val view = LayoutInflater.from(context).inflate(R.layout.tab_bottom_layout_bg, null)
        val params =
            LayoutParams(LayoutParams.MATCH_PARENT, dp2px(tabBottomHeight, resources))
        params.gravity = Gravity.BOTTOM
        addView(view, params)
        view.alpha = bottomAlpha
    }

    fun setTabAlpha(alpha: Float) {
        this.bottomAlpha = alpha
    }

    fun setTabHeight(tabHeight: Float) {
        this.tabBottomHeight = tabBottomHeight
    }

    fun setTabLineColor(tabLineColor: String) {
        this.bottomLineColor = tabLineColor
    }

    private fun fixContentView() {
        if (getChildAt(0) !is ViewGroup) {
            return
        }

        val rootView: ViewGroup = getChildAt(0) as ViewGroup
        var targetView: ViewGroup? = findTypeView(rootView, RecyclerView::class.java)

        if (targetView == null) {
            targetView = findTypeView(rootView, ScrollView::class.java)
        }

        if (targetView == null) {
            targetView = findTypeView(rootView, AbsListView::class.java)
        }

        if (targetView != null && (targetView is RecyclerView || targetView is ScrollView || targetView is AbsListView)) {
            targetView.setPadding(0, 0, 0, dp2px(tabBottomHeight, resources))
            targetView.clipToPadding = false
        }
    }

    fun clipBottomPadding(targetView: ViewGroup?) {
        if (targetView != null) {
            targetView.setPadding(0, 0, 0, dp2px(tabBottomHeight, resources))
            targetView.clipToPadding = false
        }
    }

    private fun getDisplayWidthInPx(@NonNull context: Context): Int {
        val wm: WindowManager? = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm != null) {
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.x
        }

        return 0
    }

    private fun dp2px(dp: Float, resources: Resources): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .toInt()
    }

    /**
     * 获取指定类型的子View
     * @param group ViewGroup
     * @param clazz 如：RecyclerView
     *
     * @return 指定类型的View
     */
    private fun <T> findTypeView(group: ViewGroup?, clazz: Class<T>): T? {
        if (group == null) {
            return null
        }

        val deque = ArrayDeque<View>()
        deque.add(group)
        while (!deque.isEmpty()) {
            val node = deque.removeFirst()
            if (clazz.isInstance(node)) {
                return clazz.cast(node)
            } else if (node is ViewGroup) {
                val container: ViewGroup = node
                for (child in container.children) {
                    deque.add(child)
                }
            }
        }
        return null
    }

}