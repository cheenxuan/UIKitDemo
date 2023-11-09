package com.tech.android.ui.recyclerviewkit.refresh

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
object ScrollUtil {
    /**
     * 判断Child是否发生了滚动
     *
     * @param child
     * @return
     */
    fun childScrolled(child: View): Boolean {
        if (child is AdapterView<*>) {
            val adapterView = child
            if (adapterView.firstVisiblePosition != 0
                || adapterView.firstVisiblePosition == 0 && adapterView.getChildAt(0) != null && adapterView.getChildAt(
                    0
                ).top < 0
            ) {
                return true
            }
        } else if (child.scrollY > 0) {
            return true
        }
        if (child is RecyclerView) {
            val recyclerView = child
            val view = recyclerView.getChildAt(0)
            val firstPosition = recyclerView.getChildAdapterPosition(view)
            return firstPosition != 0 || view.top != 0
        }
        return false
    }

    /**
     * 查找可以滚动的child
     *
     * @param viewGroup
     * @return 可以滚动的child
     */
    fun findScrollableChild(viewGroup: ViewGroup): View? {
        var child = viewGroup.getChildAt(1)
        if (child is RecyclerView || child is AdapterView<*>) {
            return child
        }
        if (child is ViewGroup) {
            val tempChild = child.getChildAt(0)
            if (child is RecyclerView || child is AdapterView<*>) {
                child = tempChild
            }
        }
        return child
    }
}