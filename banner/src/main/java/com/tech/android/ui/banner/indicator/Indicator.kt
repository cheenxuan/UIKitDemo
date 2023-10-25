package com.tech.android.ui.banner.indicator

import android.view.View

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description: 实现这个接口来定义你需要样式的指示器
 * <P>
 */
interface Indicator<T : View> {

    fun get(): T

    /***
     * 初始化Indicator
     * @param count  幻灯片数量
     */
    fun onInflate(count: Int)

    /***
     * 幻灯片切换回调
     * @param current  切换到的幻灯片位置
     * @param count  幻灯片数量
     */
    fun onPointChange(current: Int, count: Int)
}