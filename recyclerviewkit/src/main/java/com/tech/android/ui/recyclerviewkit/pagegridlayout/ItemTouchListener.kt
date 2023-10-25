package com.tech.android.ui.recyclerviewkit.pagegridlayout

import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.DEBUG
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.TAG

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class ItemTouchListener(
    private val layoutManager: PageGridLayoutManager,
    private val recyclerView: RecyclerView,
) : SimpleOnItemTouchListener() {

    private var mScrollPointerId = 0
    private var mInitialTouchX = 0
    private var mInitialTouchY = 0

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val actionMasked = e.actionMasked
        val actionIndex = e.actionIndex
        if (DEBUG) {
            Log.i(
                TAG,
                "onInterceptTouchEvent-actionMasked: $actionMasked, actionIndex: $actionIndex"
            )
        }
        when (actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                mScrollPointerId = e.getPointerId(actionIndex)
                mInitialTouchX = (e.getX(actionIndex) + 0.5f).toInt()
                mInitialTouchY = (e.getY(actionIndex) + 0.5f).toInt()
                recyclerView.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val index = e.findPointerIndex(mScrollPointerId)
                if (index < 0) {
                    return false
                }
                val x = (e.getX(index) + 0.5f).toInt()
                val y = (e.getY(index) + 0.5f).toInt()
                val dx = x - mInitialTouchX
                val dy = y - mInitialTouchY
                if (layoutManager.canScrollHorizontally()) {
                    recyclerView.parent.requestDisallowInterceptTouchEvent(
                        recyclerView.canScrollHorizontally(
                            -dx
                        )
                    )
                }
                if (layoutManager.canScrollVertically()) {
                    recyclerView.parent.requestDisallowInterceptTouchEvent(
                        recyclerView.canScrollVertically(
                            -dy
                        )
                    )
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onPointerUp(e)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {}
        }
        return false
    }

    private fun onPointerUp(e: MotionEvent) {
        val actionIndex = e.actionIndex
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            val newIndex = if (actionIndex == 0) 1 else 0
            mScrollPointerId = e.getPointerId(newIndex)
            mInitialTouchX = (e.getX(newIndex) + 0.5f).toInt()
            mInitialTouchY = (e.getY(newIndex) + 0.5f).toInt()
        }
    }
}