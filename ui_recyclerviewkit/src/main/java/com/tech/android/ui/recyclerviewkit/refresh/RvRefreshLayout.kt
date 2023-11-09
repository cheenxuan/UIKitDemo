package com.tech.android.ui.recyclerviewkit.refresh

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.Scroller

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
open class RvRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), RvRefresh {

    private var mState: RvOverView.RefreshState? = null
    private var mGestureDetector: GestureDetector? = null
    private var mRefreshListener: RvRefresh.RefreshListener? = null
    protected var mOverView: RvOverView? = null
    private var mLastY = 0
    private var disableRefreshScroll = false
    private var mAutoScroller: AutoScroller? = null
    private var refreshTime = 0L
    private val mHandler = Handler(Looper.getMainLooper())
    private val rvGestureDetector: RvGestureDetector = object : RvGestureDetector() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float,
        ): Boolean {
            if (Math.abs(distanceX) > Math.abs(distanceY) || mRefreshListener != null && mRefreshListener?.enableRefresh() == false) {
                //横向滑动， 或者刷新被禁止则不处理
                return false
            }
            if (disableRefreshScroll && mState === RvOverView.RefreshState.STATE_REFRESH) {
                //刷新时是否禁止滚动
                return true
            }
            val head = getChildAt(0)
            val child = ScrollUtil.findScrollableChild(this@RvRefreshLayout)
            if (ScrollUtil.childScrolled(child!!)) {
                //如果列表发生了滚动则不处理
                return false
            }

            //没有刷新或者没有达到可以刷新的距离，且头部已经划出或下拉
            return if ((mState !== RvOverView.RefreshState.STATE_REFRESH
                        || head.bottom <= (mOverView?.mPullRefreshHeight ?: 0))
                && (head.bottom > 0 || distanceY <= 0.0f)
            ) {
                //如果还在滑动中
                if (mState !== RvOverView.RefreshState.STATE_OVER_RELEASE) {
                    val seed: Int = if (child.top < (mOverView?.mPullRefreshHeight ?: 0)) {
                        (mLastY / (mOverView?.minDamp ?: 1.6f)).toInt()
                    } else {
                        (mLastY / (mOverView?.maxDamp ?: 2.2f)).toInt()
                    }
                    //如果是正在刷新状态，则不允许在滑动的时候改变状态
                    val bool = moveDown(seed, true)
                    mLastY = -distanceY.toInt()
                    bool
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    init {
        mGestureDetector = GestureDetector(context, rvGestureDetector)
        mAutoScroller = AutoScroller()
    }

    override fun setDisableRefreshScroll(disableRefreshScroll: Boolean) {
        this.disableRefreshScroll = disableRefreshScroll
    }

    override fun refreshFinished() {
        val time = System.currentTimeMillis() - refreshTime
        val delay: Long = if (time in 1..399) {
            time
        } else {
            0
        }
        mHandler.postDelayed({
            val head = getChildAt(0)
            val bottom = head.bottom
            if (bottom > 0) {
                recover(bottom)
            }
            mOverView?.onFinish()
            mOverView?.setState(RvOverView.RefreshState.STATE_INIT)
            mState = RvOverView.RefreshState.STATE_INIT
        }, delay)
    }

    override fun setRefreshListener(listener: RvRefresh.RefreshListener) {
        mRefreshListener = listener
    }

    override fun setRefreshOverView(rvOverView: RvOverView?) {
        if (mOverView != null) {
            removeView(mOverView)
        }
        mOverView = rvOverView
        val params =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(mOverView, 0, params)
    }

    /**
     * 根据偏移量移动header与child
     * @param offsetY 偏移量
     * @param nonAuto 是否非自动滚动触发
     * @return
     */
    private fun moveDown(offsetY: Int, nonAuto: Boolean): Boolean {
        var tempOffsetY = offsetY
        val head = getChildAt(0)
        val child = getChildAt(1)
        val childTop = child.top + tempOffsetY
        if (childTop <= 0) {
            //异常情况的补充
            tempOffsetY = -child.top
            //移动head与child的位置到原始位置
            head.offsetTopAndBottom(tempOffsetY)
            child.offsetTopAndBottom(tempOffsetY)
            if (mState !== RvOverView.RefreshState.STATE_REFRESH) {
                mOverView?.setState(RvOverView.RefreshState.STATE_INIT)
                mState = RvOverView.RefreshState.STATE_INIT
            }
        } else if (mState === RvOverView.RefreshState.STATE_REFRESH
            && childTop > (mOverView?.mPullRefreshHeight ?: 0)
        ) {
            //如果正在下拉刷新中，进制继续下拉
            return false
        } else if (childTop <= (mOverView?.mPullRefreshHeight ?: 0)) {
            //还没超出设定的刷新距离
            if (mState !== RvOverView.RefreshState.STATE_VISIBLE && nonAuto) {
                //头部开始显示
                mOverView?.onVisible()
                mOverView?.setState(RvOverView.RefreshState.STATE_VISIBLE)
                mState = RvOverView.RefreshState.STATE_VISIBLE
            }
            head.offsetTopAndBottom(tempOffsetY)
            child.offsetTopAndBottom(tempOffsetY)
            if (childTop == mOverView?.mPullRefreshHeight && mState === RvOverView.RefreshState.STATE_OVER_RELEASE) {
                //下拉刷新完成
                refresh()
            }
        } else {
            if (mState !== RvOverView.RefreshState.STATE_OVER && nonAuto) {
                //超出刷新位置
                mOverView?.onOver()
                mOverView?.setState(RvOverView.RefreshState.STATE_OVER)
                mState = RvOverView.RefreshState.STATE_OVER
            }
            head.offsetTopAndBottom(tempOffsetY)
            child.offsetTopAndBottom(tempOffsetY)
        }
        if (mOverView != null) {
            mOverView!!.onScroll(head.bottom, mOverView!!.mPullRefreshHeight)
        }
        return true
    }

    /**
     * 刷新
     */
    private fun refresh() {
        if (mRefreshListener != null) {
            refreshTime = System.currentTimeMillis()
            mOverView?.onRefresh()
            mOverView?.setState(RvOverView.RefreshState.STATE_REFRESH)
            mState = RvOverView.RefreshState.STATE_REFRESH
            mHandler.postDelayed({
                mRefreshListener?.onRefresh()
            }, 200)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val head = getChildAt(0)
        if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL || ev.action == MotionEvent.ACTION_POINTER_INDEX_MASK) {
            //松开手
            if (head.bottom > 0) {
                if (mState !== RvOverView.RefreshState.STATE_REFRESH) { //非正在刷新的状态
                    recover(head.bottom)
                    return false
                }
            }
            mLastY = 0
        }
        val consumed = mGestureDetector!!.onTouchEvent(ev)
        if (consumed || mState !== RvOverView.RefreshState.STATE_INIT && mState !== RvOverView.RefreshState.STATE_REFRESH && head.bottom != 0) {
            ev.action = MotionEvent.ACTION_CANCEL
            return super.dispatchTouchEvent(ev)
        }
        return if (consumed) {
            true
        } else {
            super.dispatchTouchEvent(ev)
        }
    }

    private fun recover(dis: Int) {
        if (mRefreshListener != null && dis > (mOverView?.mPullRefreshHeight ?: 0)) {
            //滚动到指定位置 dis - mHiOverView.mPullRefreshHeight
//            mAutoScroller = new AutoScroller();
            mAutoScroller!!.recover(dis - (mOverView?.mPullRefreshHeight ?: 0))
            mOverView?.setState(RvOverView.RefreshState.STATE_OVER_RELEASE)
            mState = RvOverView.RefreshState.STATE_OVER_RELEASE
        } else {
//            mAutoScroller = new AutoScroller();
            mAutoScroller!!.recover(dis)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        //定义head和child的排列位置
        val head = getChildAt(0)
        val child = getChildAt(1)
        if (head != null && child != null) {
            val childTop = child.top
            if (mState === RvOverView.RefreshState.STATE_REFRESH) {
                head.layout(
                    0,
                    (mOverView?.mPullRefreshHeight  ?: 0)- head.measuredHeight,
                    right,
                    mOverView?.mPullRefreshHeight ?: 0
                )
                child.layout(
                    0,
                    mOverView?.mPullRefreshHeight ?: 0,
                    right,
                    (mOverView?.mPullRefreshHeight ?: 0) + child.measuredHeight
                )
            } else {
                head.layout(0, childTop - head.measuredHeight, right, childTop)
                child.layout(0, childTop, right, childTop + child.measuredHeight)
            }
            var other: View
            for (i in 2 until childCount) {
                other = getChildAt(i)
                other.layout(0, top, right, bottom)
            }
        }
    }

    inner class AutoScroller : Runnable {
        private val mScroller: Scroller = Scroller(context, LinearInterpolator())

        @Volatile
        private var mLastY = 0
        var isFinished: Boolean
            private set

        init {
            isFinished = true
        }

        @Synchronized
        override fun run() {
//            System.out.println("AutoScroll:::: run:: mScroller.computeScrollOffset() = " + mScroller.computeScrollOffset());
            if (mScroller.computeScrollOffset()) { //还未完成滚动
//                System.out.println("AutoScroll:::: run:: -> moveDown() mLastY = " + mLastY +" mScroller.getCurrY() = " + mScroller.getCurrY());
                moveDown(mLastY - mScroller.currY, false)
                //                System.out.println("--------------mLastY = mScroller.getCurrY()--------------------");
                mLastY = mScroller.currY
                post(this)
            } else {
                removeCallbacks(this)
                isFinished = true
            }
        }

        fun recover(dis: Int) {
            if (dis < 0) {
                return
            }
            removeCallbacks(this)
            //            System.out.println("--------------mLastY = 0--------------------");
            mLastY = 0
            isFinished = false
            //            System.out.println("AutoScroll:::: recover:: mLastY = " + mLastY +" mIsFinished = " + mIsFinished + " dis = " + dis);
            mScroller.startScroll(0, 0, 0, dis, 300)
            post(this)
        }
    }
}