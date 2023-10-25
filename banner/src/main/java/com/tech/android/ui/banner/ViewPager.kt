package com.tech.android.ui.banner

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description: 实现ViewPager的自动翻页
 * <P>
 */
class ViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewPager(context, attrs) {

    //滚动的时间间隔
    private var mIntervalTime = 0

    //是否开启自动轮播
    private var mAutoPlay = true

    // 标志ViewPager是否被onlayout过
    private var isLayout = false

    //通过handler实现自动播放
    private val mHandler = Handler()

    private val mRunnable: Runnable = object : Runnable {
        override fun run() {
            next()
            //  切换到下一个，延迟几秒在执行
            mHandler.postDelayed(this, mIntervalTime.toLong())
        }
    }

    fun setAutoPlay(autoPlay: Boolean) {
        mAutoPlay = autoPlay
        if (!mAutoPlay) {
            mHandler.removeCallbacks(mRunnable)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> start()
            MotionEvent.ACTION_DOWN -> stop()
        }
        return super.onTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isLayout && (adapter?.count ?: 0) > 0) {
            try {
                val mScroller = ViewPager::class.java.getDeclaredField("mFirstLayout")
                mScroller.isAccessible = true
                mScroller[this] = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDetachedFromWindow() {
        if ((context as Activity).isFinishing) {
            super.onDetachedFromWindow()
        }
        stop()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        isLayout = true
    }

    fun setScrollDuration(duration: Int) {
        try {
            val scrollerFiled = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerFiled.isAccessible = true
            scrollerFiled[this] = BannerScroller(context, duration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setIntervalTime(mIntervalTime: Int) {
        this.mIntervalTime = mIntervalTime
    }

    fun start() {
        mHandler.removeCallbacksAndMessages(null)
        if (mAutoPlay) {
            mHandler.postDelayed(mRunnable, mIntervalTime.toLong())
        }
    }

    fun stop() {
        mHandler.removeCallbacksAndMessages(null)
    }

    private operator fun next(): Int {
        var nextPosition = -1
        if (adapter == null || adapter!!.count <= 1) {
            stop()
            return nextPosition
        }
        nextPosition = currentItem + 1
        //下一个索引大于adapter的view的最大数量的时候重新开始
        if (nextPosition >= adapter!!.count) {
            // 获取第一个item的索引
            nextPosition = (adapter as BannerAdapter).getFirstItem()
        }
        setCurrentItem(nextPosition, true)
        return nextPosition
    }
}