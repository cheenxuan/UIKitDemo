package com.tech.android.ui.banner

import androidx.annotation.LayoutRes
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tech.android.ui.banner.indicator.Indicator

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description: IBanner组件对外提供功能的接口
 * <P>
 */
interface IBanner {

    /***
     * 设置Banner组件绑定的数据
     * @param layoutResId
     * @param models
     */
    fun setBannerData(@LayoutRes layoutResId: Int, models: List<BannerMo?>)

    fun setBannerData(models: List<BannerMo?>)

    fun setIndicator(haIndicator: Indicator<*>)

    fun setAutoPlay(autoPlay: Boolean)

    fun setLoop(loop: Boolean)

    fun setIntervalTime(intervalTime: Int)

    fun setDefaultBanner(bannerResId: Int)

    fun setBindAdapter(bindAdapter: IBindAdapter?)

    fun setScrollDuration(duration: Int)

    fun setOnPageChangeListener(onPageChangeListener: OnPageChangeListener?)

    fun setOnBannerClickListener(onBannerClickListener: OnBannerClickListener?)

    fun startPlay()

    fun stopPlay()

    interface OnBannerClickListener {
        fun onBannerClick(
            viewHolder: BannerAdapter.BannerViewHolder,
            bannerMo: BannerMo,
            position: Int,
        )
    }
}