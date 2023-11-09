package com.tech.android.ui.banner

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tech.android.ui.banner.indicator.Indicator

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * 核心问题
 * 1 如何实现UI的高度订制
 * 2 作为有限个item如何实现无限轮播？
 * 3 Banner需要展示网络图片，如何将网络图片和Banner组件进行解耦
 * 4 指示器样式各异，如何实现指示器的高度订制
 * 5 如何设置ViewPager的滚动速度
 * <P>
 */
open class Banner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), IBanner {

    private var delegate: BannerDelegate

    init {
        delegate = BannerDelegate(context, this)
        initCustomAttrs(context, attrs)
    }

    /***
     * XML 配置的属性的初始化
     * @param context
     * @param attrs
     */
    private fun initCustomAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.banner)
        val autoPlay = typedArray.getBoolean(R.styleable.banner_autoPlay, true)
        val loop = typedArray.getBoolean(R.styleable.banner_loop, true)
        val intervalTime = typedArray.getInteger(R.styleable.banner_intervalTime, -1)
        val defaultBannerRes = typedArray.getResourceId(R.styleable.banner_default_banner, -1)
        setAutoPlay(autoPlay)
        setLoop(loop)
        setIntervalTime(intervalTime)
        setDefaultBanner(defaultBannerRes)
        typedArray.recycle()
    }

    override fun setBannerData(layoutResId: Int, models: List<BannerMo?>) {
        delegate.setBannerData(layoutResId, models)
    }

    override fun setBannerData(models: List<BannerMo?>) {
        delegate.setBannerData(models)
    }

    override fun setIndicator(indicator: Indicator<*>) {
        delegate.setIndicator(indicator)
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        delegate.setAutoPlay(autoPlay)
    }

    override fun setLoop(loop: Boolean) {
        delegate.setLoop(loop)
    }

    override fun setIntervalTime(intervalTime: Int) {
        delegate.setIntervalTime(intervalTime)
    }

    override fun setDefaultBanner(bannerResId: Int) {
        delegate.setDefaultBanner(bannerResId)
    }

    override fun setBindAdapter(bindAdapter: IBindAdapter?) {
        delegate.setBindAdapter(bindAdapter)
    }

    override fun setScrollDuration(duration: Int) {
        delegate.setScrollDuration(duration)
    }

    override fun setOnPageChangeListener(onPageChangeListener: OnPageChangeListener?) {
        delegate.setOnPageChangeListener(onPageChangeListener)
    }

    override fun setOnBannerClickListener(onBannerClickListener: IBanner.OnBannerClickListener?) {
        delegate.setOnBannerClickListener(onBannerClickListener)
    }

    override fun startPlay() {
        delegate.startPlay()
    }

    override fun stopPlay() {
        delegate.stopPlay()
    }
}