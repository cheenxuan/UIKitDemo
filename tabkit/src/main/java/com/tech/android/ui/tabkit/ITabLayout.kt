package com.tech.android.ui.tabkit

import android.view.ViewGroup

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
interface ITabLayout<Tab : ViewGroup, D> {

    fun findTab(data: D): Tab?

    fun addTabSelectedChangeListener(listener: OnTabSelectedListener<D>)

    fun defaultSelect(defaultInfo: D)

    fun inflateInfo(infoList: List<D>)

    open interface OnTabSelectedListener<D> {
        fun onTabSelectedChange(index: Int, preInfo: D?, nextInfo: D)
    }
}