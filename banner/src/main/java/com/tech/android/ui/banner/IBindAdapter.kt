package com.tech.android.ui.banner

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description: Banner的数据绑定接口  基于这个接口可以实现数据的绑定和框架层的解耦
 * <P>
 */
interface IBindAdapter {

    fun onBind(viewHolder: BannerAdapter.BannerViewHolder, mo: BannerMo, position: Int)
}