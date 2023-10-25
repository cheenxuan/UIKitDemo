package com.tech.android.ui.banner

import android.content.Context
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tech.android.ui.banner.indicator.CircleIndicator
import com.tech.android.ui.banner.indicator.Indicator

/**
 * @auther: xuan
 * @date  : 2023/10/17 .
 * <P>
 * Description: Banner的控制器
 * 辅助Banner完成各种功能的控制
 * 将Banner的一些逻辑内聚在这，保证暴露给使用者的Banner干净整洁
 * <P>
 */
class BannerDelegate(val mContext: Context, val mBanner: Banner) : IBanner, OnPageChangeListener {
    
    private var mAdapter: BannerAdapter? = null
    private var mIndicator: Indicator<*>? = null
    private var mAutoPlay = false
    private var mLoop = false
    private var mBannerMos: List<BannerMo?> = ArrayList()
    private var mOnPageChangeListener: OnPageChangeListener? = null
    private var mIntervalTime = 5000

    private var mDefaultBanner = -1
    private var mOnBannerClickListener: IBanner.OnBannerClickListener? = null
    private var mViewPager: ViewPager? = null
    private var mScrollDuration = -1
    private var bindAdapter: IBindAdapter? = null


    override fun setBannerData(layoutResId: Int, models: List<BannerMo?>) {
        mBannerMos = models
        init(layoutResId)
    }

    override fun setBannerData(models: List<BannerMo?>) {
        setBannerData(R.layout.banner_item_image, models)
    }

    fun setAdapter(adapter: BannerAdapter?) {
        mAdapter = adapter
    }

    override fun setOnPageChangeListener(onPageChangeListener: OnPageChangeListener?) {
        mOnPageChangeListener = onPageChangeListener
    }

    override fun setOnBannerClickListener(onBannerClickListener: IBanner.OnBannerClickListener?) {
        mOnBannerClickListener = onBannerClickListener
    }

    override fun startPlay() {
        if (mViewPager != null) {
            mViewPager!!.start()
        }
    }

    override fun stopPlay() {
        if (mViewPager != null) {
            mViewPager!!.stop()
        }
    }

    override fun setHiIndicator(haIndicator: Indicator<*>) {
        mIndicator = haIndicator
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        mAutoPlay = autoPlay
        if (mAdapter != null) {
            mAdapter?.setAutoPlay(autoPlay)
        }
        if (mViewPager != null) {
            mViewPager!!.setAutoPlay(autoPlay)
        }
    }

    override fun setLoop(loop: Boolean) {
        mLoop = loop
    }

    override fun setIntervalTime(intervalTime: Int) {
        if (intervalTime > 0) {
            mIntervalTime = intervalTime
        }
    }

    override fun setDefaultBanner(bannerResId: Int) {
        if (bannerResId > 0) {
            mDefaultBanner = bannerResId
        }
    }

    override fun setBindAdapter(bindAdapter: IBindAdapter?) {
        this.bindAdapter = bindAdapter
        if (mAdapter != null) {
            mAdapter?.setBindAdapter(bindAdapter)
        }
    }

    /***
     * 设置ViewPager内部的切换速度
     * @param duration
     */
    override fun setScrollDuration(duration: Int) {
        mScrollDuration = duration
        if (mViewPager != null && duration > 0) {
            mViewPager!!.setScrollDuration(duration)
        }
    }


    private fun init(layoutResId: Int) {
        if (mAdapter == null) {
            mAdapter = BannerAdapter(mContext)
        }
        if (mIndicator == null) {
            mIndicator = CircleIndicator(mContext)
        }
        mIndicator?.onInflate(mBannerMos.size)
        mAdapter?.setLayoutResId(layoutResId)
        mAdapter?.setBindAdapter(bindAdapter)
        mAdapter?.setBannerData(mBannerMos)
        mAdapter?.setAutoPlay(mBannerMos.size > 1)
        mAdapter?.setLoop(mBannerMos.size > 1)
        mAdapter?.setOnBannerClickListener(mOnBannerClickListener)
        mViewPager = ViewPager(mContext)
        mViewPager?.setIntervalTime(mIntervalTime)
        mViewPager?.setAutoPlay(mBannerMos.size > 1)
        mViewPager?.addOnPageChangeListener(this)
        mViewPager?.adapter = mAdapter
        if (mScrollDuration > 0) {
            mViewPager!!.setScrollDuration(mScrollDuration)
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        if (mLoop || mAutoPlay && mAdapter?.getRealCount() != 0) {
            //无限轮播关键点：使第一张能反向滑动到最后一张，以达到无限滚动的效果
            val firstItem: Int = mAdapter!!.getFirstItem()
            mViewPager!!.setCurrentItem(firstItem, false)
        }
        // 清除所有的View
        mBanner.removeAllViews()
        mBanner.addView(mViewPager, params)
        mBanner.addView(mIndicator?.get(), params)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (mOnPageChangeListener != null && mAdapter?.getRealCount() != 0) {
            mOnPageChangeListener!!.onPageScrolled(
                position / mAdapter!!.getRealCount(),
                positionOffset,
                positionOffsetPixels
            )
        }
    }

    override fun onPageSelected(position: Int) {
        var tempIndex = position
        if (mAdapter?.getRealCount() == 0) {
            return
        }
        tempIndex = tempIndex % mAdapter!!.getRealCount()
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageSelected(tempIndex)
        }
        if (mIndicator != null && mAdapter != null) {
            //fix：data.size == 2
            mIndicator!!.onPointChange(
                if (tempIndex >= mAdapter!!.getFakeCount()) tempIndex - mAdapter!!.getFakeCount() else tempIndex,
                mAdapter!!.getRealCount() - mAdapter!!.getFakeCount()
            )
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageScrollStateChanged(state)
        }
    }
}