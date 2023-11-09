package com.tech.android.ui.recyclerviewkit.item

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
abstract class RvDataItem<Data, VH : RecyclerView.ViewHolder>(val data: Data? = null) {
    var rvAdapter: RvAdapter? = null
    var mData: Data? = null
    fun setAdapter(adapter: RvAdapter) {
        this.rvAdapter = adapter
    }

    init {
        this.mData = data
    }

    /***
     * 绑定数据的方法
     */
    abstract fun onBindData(holder: VH, position: Int)

    /***
     * 返回该item的布局资源id
     */
    open fun getItemLayoutRes(): Int {
        return -1
    }

    /***
     * 返回该item的视图view
     */
    open fun getItemView(parent: ViewGroup): View? {
        return null
    }

    /***
     * 刷新列表
     */
    fun refreshItem() {
        if (rvAdapter != null) {
            rvAdapter!!.refreshItem(this)
        }
    }

    /***
     *  从列表移除
     */
    fun removeItem() {
        if (rvAdapter != null) {
            rvAdapter!!.removeItem(this)
        }
    }

    /***
     * 改item在列表上占据几列
     */
    open fun getSpanSize(): Int {
        return 0
    }

    /**
     * 该item被滑进屏幕
     */
    open fun onViewAttachedToWindow(holder: VH) {

    }

    /**
     * 该item被滑出屏幕
     */
    open fun onViewDetachedFromWindow(holder: VH) {

    }

    open fun onCreateViewHolder(parent: ViewGroup): VH? {
        return null
    }

}