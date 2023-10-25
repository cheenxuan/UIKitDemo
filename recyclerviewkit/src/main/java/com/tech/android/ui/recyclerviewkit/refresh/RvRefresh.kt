package com.tech.android.ui.recyclerviewkit.refresh

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
interface RvRefresh {
    /**
     * 刷新时 是否禁止滚动
     *
     * @param disableRefreshScroll
     */
    fun setDisableRefreshScroll(disableRefreshScroll: Boolean)

    /**
     * 刷新完成
     */
    fun refreshFinished()

    /**
     * 设置刷新监听器
     * @param listener
     */
    fun setRefreshListener(listener: RefreshListener)

    /**
     * 设置下拉刷新的视图
     * @param rvOverView
     */
    fun setRefreshOverView(rvOverView: RvOverView?)

    interface RefreshListener {
        fun onRefresh()
        fun enableRefresh(): Boolean
    }
}