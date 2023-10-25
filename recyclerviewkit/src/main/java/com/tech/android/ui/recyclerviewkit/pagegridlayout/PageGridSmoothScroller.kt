package com.tech.android.ui.recyclerviewkit.pagegridlayout

import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.DEBUG
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.TAG

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class PageGridSmoothScroller(
    mRecyclerView: RecyclerView,
    private val mLayoutManager: PageGridLayoutManager,
) : LinearSmoothScroller(mRecyclerView.context) {
    
    companion object {
        /**
         * @see .calculateSpeedPerPixel
         */
        val MILLISECONDS_PER_INCH = 100f

        /**
         * @see .calculateTimeForScrolling
         */
        val MAX_SCROLL_ON_FLING_DURATION = 500 //ms

        fun calculateDx(manager: PageGridLayoutManager, snapRect: Rect, targetRect: Rect): Int {
            return if (!manager.canScrollHorizontally()) {
                0
            } else targetRect.left - snapRect.left
        }

        fun calculateDy(manager: PageGridLayoutManager, snapRect: Rect, targetRect: Rect): Int {
            return if (!manager.canScrollVertically()) {
                0
            } else targetRect.top - snapRect.top
        }

        fun calculateDx(
            manager: PageGridLayoutManager,
            snapRect: Rect,
            targetRect: Rect,
            isLayoutToEnd: Boolean,
        ): Int {
            if (!manager.canScrollHorizontally()) {
                return 0
            }
            return if (isLayoutToEnd) targetRect.left - snapRect.left else targetRect.right - snapRect.right
        }

        fun calculateDy(
            manager: PageGridLayoutManager,
            snapRect: Rect,
            targetRect: Rect,
            isLayoutToEnd: Boolean,
        ): Int {
            if (!manager.canScrollVertically()) {
                return 0
            }
            return if (isLayoutToEnd) targetRect.top - snapRect.top else targetRect.bottom - snapRect.bottom
        }
    }
    
    /**
     * 该方法会在targetSnapView被layout出来的时候调用。
     *
     * @param targetView targetSnapView
     * @param state
     * @param action
     */
    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val layoutManager = layoutManager
        if (layoutManager is PageGridLayoutManager) {
            val manager = layoutManager
            val targetPosition = manager.getPosition(targetView)
            val pointF = computeScrollVectorForPosition(targetPosition)
                ?: return  //为null，则不处理
            var isLayoutToEnd = pointF.x > 0 || pointF.y > 0
            if (manager.shouldHorizontallyReverseLayout()) {
                isLayoutToEnd = !isLayoutToEnd
            }
            val snapRect: Rect
            snapRect = if (isLayoutToEnd) {
                manager.getStartSnapRect()
            } else {
                manager.getEndSnapRect()
            }
            val targetRect = Rect()
            layoutManager.getDecoratedBoundsWithMargins(targetView, targetRect)
            val dx = calculateDx(manager, snapRect, targetRect, isLayoutToEnd)
            val dy = calculateDy(manager, snapRect, targetRect, isLayoutToEnd)
            val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
            if (DEBUG) {
                Log.i(
                    TAG,
                    "onTargetFound-targetPosition:$targetPosition, dx:$dx,dy:$dy,time:$time,isLayoutToEnd:$isLayoutToEnd,snapRect:$snapRect,targetRect:$targetRect"
                )
            }
            if (time > 0) {
                action.update(dx, dy, time, mDecelerateInterpolator)
            } else {
                //说明滑动完成，计算页标
                manager.calculateCurrentPageIndexByPosition(targetPosition)
            }
        }
    }

    /**
     * 不可过小，不然可能会出现划过再回退的情况
     *
     * @param displayMetrics
     * @return 值越大，滚动速率越慢，反之
     */
    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        val speed = mLayoutManager.getMillisecondPreInch() / displayMetrics.densityDpi
        if (DEBUG) {
            Log.i(TAG, "calculateSpeedPerPixel-speed: $speed")
        }
        return speed
    }

    /**
     * 为避免长时间滚动，设置一个最大滚动时间
     *
     * @param dx 滚动的像素距离
     * @return 值越大，滑动时间越长，滚动速率越慢，反之
     */
    override fun calculateTimeForScrolling(dx: Int): Int {
        val time = Math.min(
            mLayoutManager.getMaxScrollOnFlingDuration(),
            super.calculateTimeForScrolling(dx)
        )
        Log.i(TAG, "calculateTimeForScrolling-time: $time")
        return time
    }

    
}