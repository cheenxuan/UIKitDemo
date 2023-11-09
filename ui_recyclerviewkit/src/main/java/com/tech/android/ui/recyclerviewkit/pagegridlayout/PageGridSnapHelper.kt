package com.tech.android.ui.recyclerviewkit.pagegridlayout

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.DEBUG
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.TAG
import java.util.*

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class PageGridSnapHelper : SnapHelper() {
    private var mRecyclerView: RecyclerView? = null

    /**
     * 存放锚点位置的view，一般数量为1或2个
     */
    private val snapList: MutableList<View> = ArrayList(2)

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): RecyclerView.SmoothScroller? {
        if (layoutManager !is PageGridLayoutManager) {
            return null
        }
        return if (mRecyclerView != null)
            PageGridSmoothScroller(mRecyclerView!!, layoutManager)
        else null
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int,
    ): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        if (layoutManager !is PageGridLayoutManager) {
            return RecyclerView.NO_POSITION
        }
        if (layoutManager.getLayoutState()!!.mLastScrollDelta == 0) {
            //说明无法滑动了，到头或滑动到底
            return RecyclerView.NO_POSITION
        }
        val calculateScrollDistance = calculateScrollDistance(velocityX, velocityY)
        //计算滑动的距离
        var scrollDistance =
            if (layoutManager.canScrollHorizontally()) calculateScrollDistance[0] else calculateScrollDistance[1]
        if (layoutManager.shouldHorizontallyReverseLayout()) {
            //取反
            scrollDistance = -scrollDistance
        }

        //滑动方向是否向前
        val forwardDirection = isForwardFling(layoutManager, velocityX, velocityY)
        //布局中心位置，水平滑动为X轴坐标，垂直滑动为Y轴坐标
        val layoutCenter = getLayoutCenter(layoutManager)
        reacquireSnapList(layoutManager)

        //目标位置
        var targetPosition = RecyclerView.NO_POSITION
        when (snapList.size) {
            1 -> {
                val view = snapList[0]
                val position = layoutManager.getPosition(view)
                if (forwardDirection) {
                    //方向向前
                    if (scrollDistance >= layoutCenter) {
                        //计算滑动的距离直接超过布局一半值
                        targetPosition = position
                    } else {
                        if (layoutManager.shouldHorizontallyReverseLayout()) {
                            //水平滑动需要反转的情况
                            val viewDecoratedEnd = getViewDecoratedEnd(layoutManager, view)
                            if (viewDecoratedEnd + scrollDistance >= layoutCenter) {
                                //view的结束线+scrollDistance大于于中间线，
                                //即view在中间线的左边或者上边
                                targetPosition = position
                            } else {
                                //寻找上一个锚点位置
                                targetPosition = position - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            }
                        } else {
                            val viewDecoratedStart = getViewDecoratedStart(layoutManager, view)
                            if (viewDecoratedStart - scrollDistance <= layoutCenter) {
                                //view的起始线-scrollDistance 小于中间线，
                                //即view在中间线的左边或者上边
                                targetPosition = position
                            } else {
                                //寻找上一个锚点位置
                                targetPosition = position - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            }
                        }
                    }
                } else {
                    //方向向后
                    if (Math.abs(scrollDistance) >= layoutCenter) {
                        //计算滑动的距离直接超过布局一半值
                        targetPosition = position - 1
                        if (targetPosition < 0) {
                            targetPosition = RecyclerView.NO_POSITION
                        }
                    } else {
                        if (layoutManager.shouldHorizontallyReverseLayout()) {
                            //水平滑动需要反转的情况
                            val viewDecoratedStart = getViewDecoratedStart(layoutManager, view)
                            if (viewDecoratedStart - Math.abs(scrollDistance) < layoutCenter) {
                                //寻找上一个锚点位置
                                targetPosition = position - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            } else {
                                targetPosition = position
                            }
                        } else {
                            val viewDecoratedEnd = getViewDecoratedEnd(layoutManager, view)
                            if (viewDecoratedEnd + Math.abs(scrollDistance) > layoutCenter) {
                                //寻找上一个锚点位置
                                targetPosition = position - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            } else {
                                targetPosition = position
                            }
                        }
                    }
                }
            }
            2 -> {
                val preView = snapList[0]
                val nextView = snapList[1]
                val nextPosition = layoutManager.getPosition(nextView)
                if (layoutManager.shouldHorizontallyReverseLayout()) {
                    if (forwardDirection) {
                        //方向向前
                        if (scrollDistance >= layoutCenter) {
                            //计算滑动的距离直接超过布局一半值
                            targetPosition = nextPosition
                        } else {
                            val viewDecoratedEnd2 = getViewDecoratedEnd(layoutManager, nextView)
                            if (viewDecoratedEnd2 + scrollDistance >= layoutCenter) {
                                //view的结束线+scrollDistance 大于中间线，
                                //即view在中间线的左边或者上边
                                targetPosition = nextPosition
                            } else {
                                targetPosition = nextPosition - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            }
                        }
                    } else {
                        if (Math.abs(scrollDistance) >= layoutCenter) {
                            targetPosition = nextPosition - 1
                            if (targetPosition < 0) {
                                targetPosition = RecyclerView.NO_POSITION
                            }
                        } else {
                            val viewDecoratedStart1 = getViewDecoratedStart(layoutManager, preView)
                            if (viewDecoratedStart1 - Math.abs(scrollDistance) <= layoutCenter) {
                                targetPosition = nextPosition - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            } else {
                                targetPosition = nextPosition
                            }
                        }
                    }
                } else {
                    if (forwardDirection) {
                        //方向向前
                        if (scrollDistance >= layoutCenter) {
                            //计算滑动的距离直接超过布局一半值
                            targetPosition = nextPosition
                        } else {
                            val viewDecoratedStart2 = getViewDecoratedStart(layoutManager, nextView)
                            if (viewDecoratedStart2 - scrollDistance <= layoutCenter) {
                                //view的起始线-scrollDistance 小于中间线，
                                //即view在中间线的左边或者上边
                                targetPosition = nextPosition
                            } else {
                                targetPosition = nextPosition - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            }
                        }
                    } else {
                        if (Math.abs(scrollDistance) >= layoutCenter) {
                            targetPosition = nextPosition - 1
                            if (targetPosition < 0) {
                                targetPosition = RecyclerView.NO_POSITION
                            }
                        } else {
                            val viewDecoratedEnd1 = getViewDecoratedEnd(layoutManager, preView)
                            if (viewDecoratedEnd1 + Math.abs(scrollDistance) >= layoutCenter) {
                                targetPosition = nextPosition - 1
                                if (targetPosition < 0) {
                                    targetPosition = RecyclerView.NO_POSITION
                                }
                            } else {
                                targetPosition = nextPosition
                            }
                        }
                    }
                }
            }
            3 ->                 //1行*1列可能出现的情况
                targetPosition = layoutManager.getPosition(snapList[1])
            else -> if (DEBUG) {
                Log.w(
                    TAG,
                    "findTargetSnapPosition-snapList.size: " + snapList.size
                )
            }
        }
        if (DEBUG) {
            Log.d(
                TAG,
                "findTargetSnapPosition->forwardDirection:" + forwardDirection + ",targetPosition:" + targetPosition + ",velocityX: " + velocityX + ",velocityY: " + velocityY + ",scrollDistance:" + scrollDistance + ",snapList:" + snapList.size
            )
        }
        snapList.clear()
        return targetPosition
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        var snapView: View? = null
        if (layoutManager is PageGridLayoutManager) {
            reacquireSnapList(layoutManager)
            when (snapList.size) {
                1 -> {
                    snapView = snapList[0]
                }
                2 -> {
                    //布局中心位置，水平滑动为X轴坐标，垂直滑动为Y轴坐标
                    val layoutCenter = getLayoutCenter(layoutManager)
                    val preView = snapList[0]
                    val nextView = snapList[1]
                    val rect = Rect()
                    layoutManager.getDecoratedBoundsWithMargins(nextView, rect)
                    snapView = if (layoutManager.shouldHorizontallyReverseLayout()) {
                        val viewDecoratedEnd2 = getViewDecoratedEnd(layoutManager, nextView)
                        if (viewDecoratedEnd2 <= layoutCenter) preView else nextView
                    } else {
                        val viewDecoratedStart2 = getViewDecoratedStart(layoutManager, nextView)
                        if (viewDecoratedStart2 <= layoutCenter) nextView else preView
                    }
                }
                3 ->    //1行*1列可能出现的情况
                    snapView = snapList[1]
                else -> if (DEBUG) {
                    Log.w(
                        TAG,
                        "findSnapView wrong -> snapList.size: " + snapList.size
                    )
                }
            }
            if (DEBUG) {
                Log.i(
                    TAG,
                    "findSnapView: position:" + (if (snapView != null) layoutManager.getPosition(
                        snapView
                    ) else RecyclerView.NO_POSITION) + ", snapList.size:" + snapList.size
                )
            }
            snapList.clear()
        }
        return snapView
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View,
    ): IntArray {
        val snapDistance = IntArray(2)
        val targetPosition = layoutManager.getPosition(targetView)
        if (layoutManager is PageGridLayoutManager) {
            //布局中心位置，水平滑动为X轴坐标，垂直滑动为Y轴坐标
            val layoutCenter = getLayoutCenter(layoutManager)
            val dx: Int
            val dy: Int
            val targetRect = Rect()
            layoutManager.getDecoratedBoundsWithMargins(targetView, targetRect)
            if (layoutManager.shouldHorizontallyReverseLayout()) {
                val viewDecoratedEnd = getViewDecoratedEnd(layoutManager, targetView)
                if (viewDecoratedEnd >= layoutCenter) {
                    //向前回退
                    val snapRect = layoutManager.getStartSnapRect()
                    dx = PageGridSmoothScroller.calculateDx(layoutManager, snapRect, targetRect)
                    dy = PageGridSmoothScroller.calculateDy(layoutManager, snapRect, targetRect)
                } else {
                    //向后前进
                    dx = -calculateDxToNextPage(layoutManager, targetRect)
                    dy = -calculateDyToNextPage(layoutManager, targetRect)
                }
            } else {
                val viewDecoratedStart = getViewDecoratedStart(layoutManager, targetView)
                if (viewDecoratedStart <= layoutCenter) {
                    //向前回退
                    val snapRect = layoutManager.getStartSnapRect()
                    dx = PageGridSmoothScroller.calculateDx(layoutManager, snapRect, targetRect)
                    dy = PageGridSmoothScroller.calculateDy(layoutManager, snapRect, targetRect)
                } else {
                    //向后前进
                    dx = -calculateDxToNextPage(layoutManager, targetRect)
                    dy = -calculateDyToNextPage(layoutManager, targetRect)
                }
            }
            snapDistance[0] = dx
            snapDistance[1] = dy
            if (snapDistance[0] == 0 && snapDistance[1] == 0) {
                //说明滑动完成，计算页标
                layoutManager.calculateCurrentPageIndexByPosition(targetPosition)
            }
            if (DEBUG) {
                Log.i(
                    TAG,
                    "calculateDistanceToFinalSnap-targetView: " + targetPosition + ",snapDistance: " + Arrays.toString(
                        snapDistance
                    )
                )
            }
        }
        return snapDistance
    }

    private fun isForwardFling(
        layoutManager: PageGridLayoutManager,
        velocityX: Int,
        velocityY: Int,
    ): Boolean {
        return if (layoutManager.canScrollHorizontally()) (if (layoutManager.getShouldReverseLayout()) velocityX < 0 else velocityX > 0) else velocityY > 0
    }

    /***
     * 获取锚点view
     * @param manager
     */
    private fun reacquireSnapList(manager: PageGridLayoutManager) {
        if (!snapList.isEmpty()) {
            snapList.clear()
        }
        val childCount = manager.childCount
        for (i in 0 until childCount) {
            val child = manager.getChildAt(i) ?: continue
            //先去寻找符合锚点位置的view
            if (manager.getPosition(child) % manager.getOnePageSize() == 0) {
                snapList.add(child)
            }
        }
    }

    fun calculateDxToNextPage(layoutManager: PageGridLayoutManager, targetRect: Rect): Int {
        return if (!layoutManager.canScrollHorizontally()) {
            0
        } else getLayoutEndAfterPadding(layoutManager) - targetRect.left
    }

    fun calculateDyToNextPage(layoutManager: PageGridLayoutManager, targetRect: Rect): Int {
        return if (!layoutManager.canScrollVertically()) {
            0
        } else getLayoutEndAfterPadding(layoutManager) - targetRect.top
    }

    /**
     * 计算targetView中心位置到布局中心位置的距离
     *
     * @param layoutManager
     * @param targetView
     * @return
     */
    fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View): Int {
        //布局中心位置，水平滑动为X轴坐标，垂直滑动为Y轴坐标
        val layoutCenter = getLayoutCenter(layoutManager)
        val childCenter = getChildViewCenter(layoutManager, targetView)
        return childCenter - layoutCenter
    }

    fun getLayoutCenter(layoutManager: RecyclerView.LayoutManager): Int {
        return getLayoutStartAfterPadding(layoutManager) + getLayoutTotalSpace(layoutManager) / 2
    }

    fun getLayoutStartAfterPadding(layoutManager: RecyclerView.LayoutManager): Int {
        return if (layoutManager.canScrollHorizontally()) layoutManager.paddingStart else layoutManager.paddingTop
    }

    fun getLayoutEndAfterPadding(layoutManager: RecyclerView.LayoutManager): Int {
        return if (layoutManager.canScrollHorizontally()) layoutManager.width - layoutManager.paddingEnd else layoutManager.height - layoutManager.paddingBottom
    }

    fun getLayoutTotalSpace(layoutManager: RecyclerView.LayoutManager): Int {
        return if (layoutManager.canScrollHorizontally()) layoutManager.width - layoutManager.paddingStart - layoutManager.paddingEnd else layoutManager.height - layoutManager.paddingTop - layoutManager.paddingBottom
    }

    fun getChildViewCenter(layoutManager: RecyclerView.LayoutManager, targetView: View): Int {
        return getViewDecoratedStart(layoutManager, targetView) + getViewDecoratedMeasurement(
            layoutManager,
            targetView
        ) / 2
    }

    fun getViewDecoratedStart(layoutManager: RecyclerView.LayoutManager, view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return if (layoutManager.canScrollHorizontally()) {
            layoutManager.getDecoratedLeft(view) - params.leftMargin
        } else {
            layoutManager.getDecoratedTop(view) - params.topMargin
        }
    }

    fun getViewDecoratedEnd(layoutManager: RecyclerView.LayoutManager, view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return if (layoutManager.canScrollHorizontally()) {
            layoutManager.getDecoratedRight(view) - params.rightMargin
        } else {
            layoutManager.getDecoratedBottom(view) - params.bottomMargin
        }
    }

    fun getViewDecoratedMeasurement(layoutManager: RecyclerView.LayoutManager, view: View): Int {
        val params = view.layoutParams as RecyclerView.LayoutParams
        return if (layoutManager.canScrollHorizontally()) {
            layoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin
        } else {
            layoutManager.getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin
        }
    }
}