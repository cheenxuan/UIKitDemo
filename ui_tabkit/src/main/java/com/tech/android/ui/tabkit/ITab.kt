package com.tech.android.ui.tabkit

import androidx.annotation.Px


/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
interface ITab<D> : ITabLayout.OnTabSelectedListener<D> {

    fun setTabInfo(data: D)

    /**
     * 动态修改某个item的大小
     * @param height item的高度
     */
    fun resetHeight(@Px height: Int)


    /**
     * 动态修改某个item的大小
     * @param width item的宽度
     */
    fun resetWidth(@Px width: Int)

}