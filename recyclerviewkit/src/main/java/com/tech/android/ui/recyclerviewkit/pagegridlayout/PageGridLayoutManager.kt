package com.tech.android.ui.recyclerviewkit.pagegridlayout

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.DEBUG
import com.tech.android.ui.recyclerviewkit.pagegridlayout.LayoutConfig.TAG

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class PageGridLayoutManager @JvmOverloads constructor(
    @IntRange(from = 1) rows: Int,
    @IntRange(from = 1) columns: Int,
    @Orientation orientation: Int = HORIZONTAL,
    reverseLayout: Boolean = false,
) : LayoutManager(), ScrollVectorProvider {


    companion object {
        val UN_SET = 0

        /**
         * 水平滑动
         */
        const val HORIZONTAL = RecyclerView.HORIZONTAL

        /**
         * 垂直滑动
         */
        const val VERTICAL = RecyclerView.VERTICAL

        /**
         * @see .mCurrentPagerIndex
         */
        const val NO_ITEM = -1
        const val NO_PAGER_COUNT = 0
    }


    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @IntDef(HORIZONTAL, VERTICAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Orientation

    private var mPageGridSnapHelper: PageGridSnapHelper? = null

    /**
     * 当前滑动方向
     */
    @Orientation
    private var mOrientation = HORIZONTAL

    /**
     * 行数
     */
    @IntRange(from = 1)
    private var mRows = 1

    /**
     * 列数
     */
    @IntRange(from = 1)
    private var mColumns = 1

    /**
     * 一页的数量 [.mRows] * [.mColumns]
     */
    private var mOnePageSize = 0

    /**
     * 总页数
     */
    private var mPageCount = NO_PAGER_COUNT

    /**
     * 当前页码下标
     * 从0开始
     */
    private var mCurrentPageIndex = NO_ITEM

    /**
     * item的宽度
     */
    private var mItemWidth = UN_SET

    /**
     * item的高度
     */
    private var mItemHeight = UN_SET

    /**
     * 一个ItemView的所有ItemDecoration占用的宽度(px)
     */
    private var mItemWidthUsed = 0

    /**
     * 一个ItemView的所有ItemDecoration占用的高度(px)
     */
    private var mItemHeightUsed = 0

    /**
     * 用于保存一些状态
     */
    private var mLayoutState: LayoutState

    private var mLayoutChunkResult: LayoutChunkResult

    /**
     * 用于计算锚点坐标
     * [.mShouldReverseLayout] 为false：左上角第一个view的位置
     * [.mShouldReverseLayout] 为true：右上角第一个view的位置
     */
    private val mStartSnapRect = Rect()

    /**
     * 用于计算锚点坐标
     * [.mShouldReverseLayout] 为false：右下角最后一个view的位置
     * [.mShouldReverseLayout] 为true：左上角最后一个view的位置
     */
    private val mEndSnapRect = Rect()

    private var mRecyclerView: RecyclerView? = null

    /**
     * 定义是否应从头到尾计算布局
     *
     * @see .mShouldReverseLayout
     */
    private var mReverseLayout = false

    /**
     * 这保留了 PageGridLayoutManager 应该如何开始布局视图的最终值。
     * 它是通过检查 [.getReverseLayout] 和 View 的布局方向来计算的。
     */
    protected var mShouldReverseLayout = false

    private var mPageChangedListener: PageChangedListener? = null

    /**
     * 计算多出来的宽度，因为在均分的时候，存在除不尽的情况，要减去多出来的这部分大小，一般也就为几px
     * 不减去的话，会导致翻页计算不触发
     *
     * @see .onMeasure
     */
    private var diffWidth = 0

    /**
     * 计算多出来的高度，因为在均分的时候，存在除不尽的情况，要减去多出来的这部分大小，一般也就为几px
     * 不减去的话，会导致翻页计算不触发
     *
     * @see .onMeasure
     */
    private var diffHeight = 0

    /**
     * 是否启用处理滑动冲突滑动冲突，默认开启
     * 只会在[RecyclerView] 在可滑动布局[.isInScrollingContainer]中起作用
     */
    private var isHandlingSlidingConflictsEnabled = true
    private var mMillisecondPreInch: Float = PageGridSmoothScroller.MILLISECONDS_PER_INCH
    private var mMaxScrollOnFlingDuration: Int =
        PageGridSmoothScroller.MAX_SCROLL_ON_FLING_DURATION

    private val onChildAttachStateChangeListener: OnChildAttachStateChangeListener =
        object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val layoutParams = view.layoutParams as LayoutParams
                //判断ItemLayout的宽高是否是match_parent
                check(
                    !(layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT
                            || layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT)
                ) { "Item layout  must fill the whole PageGridLayoutManager (use match_parent)" }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                // nothing
            }
        }

    private var onItemTouchListener: OnItemTouchListener? = null

    init {
        mLayoutState = createLayoutState()
        mLayoutChunkResult = createLayoutChunkResult()
        setRows(rows)
        setColumns(columns)
        setOrientation(orientation)
        setReverseLayout(reverseLayout)
    }

    /**
     * @return 子布局LayoutParams，默认全部填充，子布局会根据[.mRows]和[.mColumns] 均分RecyclerView
     */
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(
        c: Context?,
        attrs: AttributeSet?,
    ): RecyclerView.LayoutParams {
        return LayoutParams(c, attrs)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): RecyclerView.LayoutParams {
        return when (lp) {
            is RecyclerView.LayoutParams -> {
                LayoutParams(lp as RecyclerView.LayoutParams?)
            }
            is MarginLayoutParams -> {
                LayoutParams(lp as MarginLayoutParams?)
            }
            else -> {
                LayoutParams(lp)
            }
        }
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return lp is LayoutParams
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow: ")
        }
        //默认先这么设置
        view.setHasFixedSize(true)
        if (isInScrollingContainer(view)) {
            //在一个可滑动的布局中
            if (isHandlingSlidingConflictsEnabled) {
                onItemTouchListener = ItemTouchListener(this, view)
                view.addOnItemTouchListener(onItemTouchListener!!)
            } else {
                //不启用的话可以自行解决
                if (DEBUG) {
                    Log.w(TAG, "isHandlingSlidingConflictsEnabled: false.")
                }
            }
        }
        view.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        mPageGridSnapHelper = PageGridSnapHelper()
        mPageGridSnapHelper!!.attachToRecyclerView(view)
        mRecyclerView = view
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: State,
        widthSpec: Int,
        heightSpec: Int,
    ) {
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)
        //判断RecyclerView的宽度和高度是不是精确值
        if (widthMode == View.MeasureSpec.EXACTLY && heightMode == View.MeasureSpec.EXACTLY) {
            val realWidth = widthSize - paddingStart - paddingEnd
            val realHeight = heightSize - paddingTop - paddingBottom
            //均分宽
            mItemWidth = if (mColumns > 0) realWidth / mColumns else 0
            //均分高
            mItemHeight = if (mRows > 0) realHeight / mRows else 0

            //重置下宽高，因为在均分的时候，存在除不尽的情况，要减去多出来的这部分大小，一般也就为几px
            //不减去的话，会导致翻页计算不触发
            diffWidth = realWidth - mItemWidth * mColumns
            diffHeight = realHeight - mItemHeight * mRows
            mItemWidthUsed = realWidth - diffWidth - mItemWidth
            mItemHeightUsed = realHeight - diffHeight - mItemHeight
        } else {
            mItemWidth = UN_SET
            mItemHeight = UN_SET
            diffWidth = 0
            diffHeight = 0
            mItemWidthUsed = 0
            mItemHeightUsed = 0
            if (DEBUG) {
                Log.w(
                    TAG,
                    "onMeasure-width or height is not exactly, widthMode: $widthMode, heightMode: $heightMode"
                )
            }
        }
        if (DEBUG) {
            Log.d(
                TAG,
                "onMeasure-widthMode: $widthMode, heightMode: $heightMode, originalWidthSize: $widthSize,originalHeightSize: $heightSize,diffWidth: $diffWidth,diffHeight: $diffHeight,mItemWidth: $mItemWidth,mItemHeight: $mItemHeight,mStartSnapRect:$mStartSnapRect,mEndSnapRect:$mEndSnapRect"
            )
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: State) {
        if (DEBUG) {
            Log.d(TAG, "onLayoutChildren: $state")
        }
        check(!(mItemWidth == UN_SET || mItemHeight == UN_SET)) { "RecyclerView's width and height must be exactly." }
        val itemCount = itemCount
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            setPageCount(NO_PAGER_COUNT)
            setCurrentPageIndex(NO_ITEM)
            return
        }
        if (state.isPreLayout) {
            return
        }

        // resolve layout direction
        resolveShouldLayoutReverse()

        //计算锚点的坐标
        if (mShouldReverseLayout) {
            //右上角第一个view的位置
            mStartSnapRect[width - paddingEnd - mItemWidth, paddingTop, width - paddingEnd] =
                paddingTop + mItemHeight
            //左下角最后一个view的位置
            mEndSnapRect[paddingStart, height - paddingBottom - mItemHeight, paddingStart + mItemWidth] =
                height - paddingBottom
        } else {
            //左上角第一个view的位置
            mStartSnapRect[paddingStart, paddingTop, paddingStart + mItemWidth] =
                paddingTop + mItemHeight
            //右下角最后一个view的位置
            mEndSnapRect[width - paddingEnd - mItemWidth, height - paddingBottom - mItemHeight, width - paddingEnd] =
                height - paddingBottom
        }

        //计算总页数
        var pageCount = itemCount / mOnePageSize
        if (itemCount % mOnePageSize != 0) {
            ++pageCount
        }

        //计算需要补充空间
        mLayoutState.replenishDelta = 0
        if (pageCount > 1) {
            //超过一页，计算补充空间距离
            val remain = itemCount % mOnePageSize
            var replenish = 0
            if (remain != 0) {
                var i = remain / mColumns
                val k = remain % mColumns
                replenish = if (mOrientation == HORIZONTAL) {
                    if (i == 0) (mColumns - k) * mItemWidth else 0
                } else {
                    if (k > 0) {
                        ++i
                    }
                    (mRows - i) * mItemHeight
                }
            }
            mLayoutState.replenishDelta = replenish
        }
        mLayoutState.mRecycle = false
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END
        mLayoutState.mAvailable = getEnd()
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN
        var pageIndex = mCurrentPageIndex
        pageIndex = if (pageIndex == NO_ITEM) {
            0
        } else {
            //取上次PageIndex和最大MaxPageIndex中最小值。
            Math.min(pageIndex, getMaxPageIndex())
        }
        val firstView: View? = if (!isIdle() && childCount != 0) {
            //滑动中的更新状态
            getChildClosestToStart()
        } else {
            //没有子view或者不在滑动状态
            null
        }

        //计算首个位置的偏移量，主要是为了方便child layout，计算出目标位置的上一个位置的坐标
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (mShouldReverseLayout) {
            if (firstView == null) {
                //按页且从右上角开始布局
                mLayoutState.mCurrentPosition = pageIndex * mOnePageSize
                val calculateClipOffset = calculateClipOffset(true, mLayoutState.mCurrentPosition)
                if (mOrientation == RecyclerView.HORIZONTAL) {
                    bottom = height - paddingBottom
                    left = width - paddingEnd + calculateClipOffset
                } else {
                    bottom = paddingTop - calculateClipOffset
                    left = paddingStart
                }
            } else {
                //计算布局偏移量
                val position = getPosition(firstView)
                mLayoutState.mCurrentPosition = position
                val rect = mLayoutState.mOffsetRect
                val calculateClipOffset = calculateClipOffset(true, mLayoutState.mCurrentPosition)
                getDecoratedBoundsWithMargins(firstView, rect)
                if (mOrientation == RecyclerView.HORIZONTAL) {
                    if (isNeedMoveToNextSpan(position)) {
                        //为了方便计算
                        bottom = height - paddingBottom
                        left = rect.right + calculateClipOffset
                    } else {
                        bottom = rect.top
                        left = rect.left
                    }
                } else {
                    if (isNeedMoveToNextSpan(position)) {
                        //为了方便计算
                        bottom = rect.top - calculateClipOffset
                        left = paddingStart
                    } else {
                        bottom = rect.bottom
                        left = rect.right
                    }
                }
                //追加额外的滑动空间
                val scrollingOffset: Int
                scrollingOffset = if (mOrientation == HORIZONTAL) {
                    getDecoratedStart(firstView) - getEndAfterPadding()
                } else {
                    getDecoratedStart(firstView)
                }
                mLayoutState.mAvailable -= scrollingOffset
            }
            top = bottom - mItemHeight
            right = left + mItemWidth
        } else {
            if (firstView == null) {
                //按页且从左上角开始布局
                mLayoutState.mCurrentPosition = pageIndex * mOnePageSize
                val calculateClipOffset = calculateClipOffset(true, mLayoutState.mCurrentPosition)
                if (mOrientation == RecyclerView.HORIZONTAL) {
                    bottom = height - paddingBottom
                    right = paddingStart - calculateClipOffset
                } else {
                    bottom = paddingTop - calculateClipOffset
                    right = width - paddingEnd
                }
            } else {
                //计算布局偏移量
                val position = getPosition(firstView)
                mLayoutState.mCurrentPosition = position
                val rect = mLayoutState.mOffsetRect
                val calculateClipOffset = calculateClipOffset(true, mLayoutState.mCurrentPosition)
                getDecoratedBoundsWithMargins(firstView, rect)
                if (mOrientation == RecyclerView.HORIZONTAL) {
                    if (isNeedMoveToNextSpan(position)) {
                        //为了方便计算
                        bottom = height - paddingBottom
                        right = rect.left - calculateClipOffset
                    } else {
                        bottom = rect.top
                        right = rect.right
                    }
                } else {
                    if (isNeedMoveToNextSpan(position)) {
                        //为了方便计算
                        bottom = rect.top - calculateClipOffset
                        right = width - paddingEnd
                    } else {
                        bottom = rect.bottom
                        right = rect.left
                    }
                }
                //追加额外的滑动空间
                val scrollingOffset = getDecoratedStart(firstView)
                mLayoutState.mAvailable -= scrollingOffset
            }
            top = bottom - mItemHeight
            left = right - mItemWidth
        }
        mLayoutState.setOffsetRect(left, top, right, bottom)
        if (DEBUG) {
            Log.i(
                TAG,
                "onLayoutChildren-pageCount:" + pageCount + ",mLayoutState.mAvailable: " + mLayoutState.mAvailable
            )
        }

        //回收views
        detachAndScrapAttachedViews(recycler)
        //填充views
        fill(recycler, state)
        if (DEBUG) {
            Log.i(
                TAG,
                "onLayoutChildren: childCount:" + childCount + ",recycler.scrapList.size:" + recycler.scrapList.size + ",mLayoutState.replenishDelta:" + mLayoutState.replenishDelta
            )
        }
        if (firstView == null) {
            //移动状态不更新页数和页码
            setPageCount(pageCount)
            setCurrentPageIndex(pageIndex)
        }
    }

    override fun onLayoutCompleted(state: State?) {}

    override fun findViewByPosition(position: Int): View? {
        val childCount = childCount
        if (childCount == 0) {
            return null
        }
        val firstChild = getPosition(getChildAt(0)!!)
        val viewPosition = position - firstChild
        if (viewPosition in 0 until childCount) {
            val child = getChildAt(viewPosition)
            if (getPosition(child!!) == position) {
                return child
            }
        }
        return super.findViewByPosition(position)
    }

    override fun computeHorizontalScrollOffset(state: State): Int {
        return computeScrollOffset(state)
    }

    override fun computeVerticalScrollOffset(state: State): Int {
        return computeScrollOffset(state)
    }

    override fun computeHorizontalScrollExtent(state: State): Int {
        return computeScrollExtent(state)
    }

    override fun computeVerticalScrollExtent(state: State): Int {
        return computeScrollExtent(state)
    }

    override fun computeVerticalScrollRange(state: State): Int {
        return computeScrollRange(state)
    }

    override fun computeHorizontalScrollRange(state: State): Int {
        return computeScrollRange(state)
    }

    override fun onSaveInstanceState(): Parcelable {
        if (DEBUG) {
            Log.d(TAG, "onSaveInstanceState: ")
        }
        val state = SavedState()
        state.mOrientation = mOrientation
        state.mRows = mRows
        state.mColumns = mColumns
        state.mCurrentPageIndex = mCurrentPageIndex
        state.mReverseLayout = mReverseLayout
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && state is SavedState) {
            mOrientation = state.mOrientation
            mRows = state.mRows ?: 0
            mColumns = state.mColumns ?: 0
            calculateOnePageSize()
            setCurrentPageIndex(state.mCurrentPageIndex)
            mReverseLayout = state.mReverseLayout
            requestLayout()
            if (DEBUG) {
                Log.d(TAG, "onRestoreInstanceState: loaded saved state")
            }
        }
    }

    override fun scrollToPosition(position: Int) {
        assertNotInLayoutOrScroll(null)

        //先找到目标position所在第几页
        val pageIndex = getPageIndexByPosition(position)
        scrollToPageIndex(pageIndex)
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: State?,
        position: Int,
    ) {
        assertNotInLayoutOrScroll(null)

        //先找到目标position所在第几页
        val pageIndex = getPageIndexByPosition(position)
        smoothScrollToPageIndex(pageIndex)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: State,
    ): Int {
        return if (mOrientation == VERTICAL) {
            //垂直滑动不处理
            0
        } else scrollBy(dx, recycler, state)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: State,
    ): Int {
        return if (mOrientation == HORIZONTAL) {
            //水平滑动不处理
            0
        } else scrollBy(dy, recycler, state)
    }

    override fun onScrollStateChanged(state: Int) {
        when (state) {
            SCROLL_STATE_IDLE -> {}
            SCROLL_STATE_DRAGGING -> {}
            SCROLL_STATE_SETTLING -> {}
        }
    }

    override fun canScrollHorizontally(): Boolean {
        return mOrientation == RecyclerView.HORIZONTAL
    }

    override fun canScrollVertically(): Boolean {
        return mOrientation == RecyclerView.VERTICAL
    }

    override fun getWidth(): Int {
        return super.getWidth() - getDiffWidth()
    }

    override fun getHeight(): Int {
        return super.getHeight() - getDiffHeight()
    }

    @CallSuper
    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        if (DEBUG) {
            Log.w(TAG, "onDetachedFromWindow: ")
        }
        if (mRecyclerView != null) {
            if (onItemTouchListener != null) {
                mRecyclerView!!.removeOnItemTouchListener(onItemTouchListener!!)
            }
            mRecyclerView!!.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
            mRecyclerView = null
        }
        mPageGridSnapHelper?.attachToRecyclerView(null)
        mPageGridSnapHelper = null
        //这里不能置为null，因为在ViewPager2嵌套Fragment使用，
        //部分情况下Fragment不回调onDestroyView，但会导致onDetachedFromWindow触发。
        //所以如果想置null，请调用{@link #setPageChangedListener(null)}
//        mPageChangedListener = null;
    }

    /**
     * 设置监听回调
     *
     * @param listener
     */
    fun setPageChangedListener(listener: PageChangedListener?) {
        mPageChangedListener = listener
    }

    /**
     * 是否启用处理滑动冲突滑动冲突，默认true
     * 这个方法必须要在[RecyclerView.setLayoutManager] 之前调用，否则无效
     * you must call this method before [RecyclerView.setLayoutManager]
     *
     * @param enabled 是否启用
     * @see .isInScrollingContainer
     * @see .onAttachedToWindow
     */
    fun setHandlingSlidingConflictsEnabled(enabled: Boolean) {
        isHandlingSlidingConflictsEnabled = enabled
    }

    fun isHandlingSlidingConflictsEnabled(): Boolean {
        return isHandlingSlidingConflictsEnabled
    }

    /**
     * 设置滑动每像素需要花费的时间，不可过小，不然可能会出现划过再回退的情况
     * 默认值：[PageGridSmoothScroller.MILLISECONDS_PER_INCH]
     *
     *
     * set millisecond pre inch. not too small.
     * default value: [PageGridSmoothScroller.MILLISECONDS_PER_INCH]
     *
     * @param millisecondPreInch 值越大，滚动速率越慢，反之
     * @see PageGridSmoothScroller.calculateSpeedPerPixel
     */
    fun setMillisecondPreInch(@FloatRange(from = 1.0) millisecondPreInch: Float) {
        mMillisecondPreInch = Math.max(1f, millisecondPreInch)
    }

    /**
     * @return 滑动每像素需要花费的时间
     * @see PageGridSmoothScroller.calculateSpeedPerPixel
     */
    fun getMillisecondPreInch(): Float {
        return mMillisecondPreInch
    }

    /**
     * 设置最大滚动时间，如果您想此值无效，请使用[Integer.MAX_VALUE]
     * 默认值：[PageGridSmoothScroller.MAX_SCROLL_ON_FLING_DURATION]，单位：毫秒
     *
     *
     * set max scroll on fling duration.If you want this value to expire, use [Integer.MAX_VALUE]
     * default value: [PageGridSmoothScroller.MAX_SCROLL_ON_FLING_DURATION],Unit: ms
     *
     * @param maxScrollOnFlingDuration 值越大，滑动时间越长，滚动速率越慢，反之
     * @see PageGridSmoothScroller.calculateTimeForScrolling
     */
    fun setMaxScrollOnFlingDuration(@IntRange(from = 1) maxScrollOnFlingDuration: Int) {
        mMaxScrollOnFlingDuration = Math.max(1, maxScrollOnFlingDuration)
    }

    /**
     * @return 最大滚动时间
     * @see PageGridSmoothScroller.calculateTimeForScrolling
     */
    fun getMaxScrollOnFlingDuration(): Int {
        return mMaxScrollOnFlingDuration
    }

    fun getItemWidth(): Int {
        return mItemWidth
    }

    fun getItemHeight(): Int {
        return mItemHeight
    }

    /**
     * 计算一页的数量
     */
    private fun calculateOnePageSize() {
        mOnePageSize = mRows * mColumns
    }

    /**
     * @return 一页的数量
     */
    @IntRange(from = 1)
    fun getOnePageSize(): Int {
        return mOnePageSize
    }

    fun setColumns(@IntRange(from = 1) columns: Int) {
        assertNotInLayoutOrScroll(null)
        if (mColumns == columns) {
            return
        }
        mColumns = Math.max(columns, 1)
        mPageCount = NO_PAGER_COUNT
        mCurrentPageIndex = NO_ITEM
        calculateOnePageSize()
        requestLayout()
    }

    /**
     * @return 列数
     */
    @IntRange(from = 1)
    fun getColumns(): Int {
        return mColumns
    }

    fun setRows(@IntRange(from = 1) rows: Int) {
        assertNotInLayoutOrScroll(null)
        if (mRows == rows) {
            return
        }
        mRows = Math.max(rows, 1)
        mPageCount = NO_PAGER_COUNT
        mCurrentPageIndex = NO_ITEM
        calculateOnePageSize()
        requestLayout()
    }

    /**
     * @return 行数
     */
    @IntRange(from = 1)
    fun getRows(): Int {
        return mRows
    }

    /**
     * 设置滑动方向
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(@Orientation orientation: Int) {
        assertNotInLayoutOrScroll(null)
        require(!(orientation != HORIZONTAL && orientation != VERTICAL)) { "invalid orientation:$orientation" }
        if (orientation != mOrientation) {
            mOrientation = orientation
            requestLayout()
        }
    }

    @Orientation
    fun getOrientation(): Int {
        return mOrientation
    }

    fun setReverseLayout(reverseLayout: Boolean) {
        assertNotInLayoutOrScroll(null)
        if (reverseLayout == mReverseLayout) {
            return
        }
        mReverseLayout = reverseLayout
        requestLayout()
    }

    fun getReverseLayout(): Boolean {
        return mReverseLayout
    }

    /**
     * @param position position
     * @return 获取当前position所在页下标
     */
    fun getPageIndexByPosition(position: Int): Int {
        return position / mOnePageSize
    }

    /**
     * @return 获取最大页数
     */
    fun getMaxPageIndex(): Int {
        return getPageIndexByPosition(itemCount - 1)
    }

    /**
     * 直接滚到第几页
     *
     * @param pageIndex 第几页
     */
    fun scrollToPageIndex(@IntRange(from = 0) pageIndex: Int) {
        var tempIndex = pageIndex
        assertNotInLayoutOrScroll(null)

        //先找到目标position所在第几页
        tempIndex = Math.min(Math.max(tempIndex, 0), getMaxPageIndex())
        if (tempIndex == mCurrentPageIndex) {
            //同一页直接return
            return
        }
        setCurrentPageIndex(tempIndex)
        requestLayout()
    }

    /**
     * 直接滚动到上一页
     */
    fun scrollToPrePage() {
        assertNotInLayoutOrScroll(null)
        scrollToPageIndex(mCurrentPageIndex - 1)
    }

    /**
     * 直接滚动到下一页
     */
    fun scrollToNextPage() {
        assertNotInLayoutOrScroll(null)
        scrollToPageIndex(mCurrentPageIndex + 1)
    }

    /**
     * 平滑滚到第几页，为避免长时间滚动，会预先跳转到就近位置，默认3页
     *
     * @param pageIndex 第几页，下标从0开始
     */
    fun smoothScrollToPageIndex(@IntRange(from = 0) pageIndex: Int) {
        var tempIndex = pageIndex
        assertNotInLayoutOrScroll(null)
        tempIndex = Math.min(Math.max(tempIndex, 0), getMaxPageIndex())
        val previousIndex = mCurrentPageIndex
        if (tempIndex == previousIndex) {
            //同一页直接return
            return
        }
        val isLayoutToEnd = tempIndex > previousIndex
        if (Math.abs(tempIndex - previousIndex) > 3) {
            //先就近直接跳转
            val transitionIndex = if (tempIndex > previousIndex) tempIndex - 3 else tempIndex + 3
            scrollToPageIndex(transitionIndex)
            if (mRecyclerView != null) {
                mRecyclerView!!.post(
                    SmoothScrollToPosition(
                        getPositionByPageIndex(tempIndex, isLayoutToEnd), this,
                        mRecyclerView!!
                    )
                )
            }
        } else {
            if (mRecyclerView != null) {
                val smoothScroller = PageGridSmoothScroller(mRecyclerView!!, this)
                smoothScroller.targetPosition = getPositionByPageIndex(tempIndex, isLayoutToEnd)
                startSmoothScroll(smoothScroller)
            }
        }
    }

    /**
     * 平滑到上一页
     */
    fun smoothScrollToPrePage() {
        assertNotInLayoutOrScroll(null)
        smoothScrollToPageIndex(mCurrentPageIndex - 1)
    }

    /**
     * 平滑到下一页
     */
    fun smoothScrollToNextPage() {
        assertNotInLayoutOrScroll(null)
        smoothScrollToPageIndex(mCurrentPageIndex + 1)
    }

    fun createLayoutState(): LayoutState {
        return LayoutState()
    }

    fun createLayoutChunkResult(): LayoutChunkResult {
        return LayoutChunkResult()
    }

    fun isLayoutRTL(): Boolean {
        return layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    /**
     * 设置总页数
     *
     * @param pageCount
     */
    private fun setPageCount(pageCount: Int) {
        if (mPageCount == pageCount) {
            return
        }
        mPageCount = pageCount
        if (mPageChangedListener != null) {
            mPageChangedListener!!.onPageCountChanged(pageCount)
        }
    }

    /**
     * 返回总页数
     *
     * @return 0：[.getItemCount] is 0
     */
    @IntRange(from = 0)
    fun getPageCount(): Int {
        return Math.max(mPageCount, 0)
    }

    /**
     * 设置当前页码
     *
     * @param pageIndex 页码
     */
    private fun setCurrentPageIndex(pageIndex: Int) {
        if (mCurrentPageIndex == pageIndex) {
            return
        }
        val prePageIndex = mCurrentPageIndex
        mCurrentPageIndex = pageIndex
        if (mPageChangedListener != null) {
            mPageChangedListener!!.onPageIndexSelected(prePageIndex, pageIndex)
        }
    }

    /**
     * 获取当前的页码
     *
     * @return -1：[.getItemCount] is 0,[.NO_ITEM] . else [.mCurrentPageIndex]
     */
    @IntRange(from = -1)
    fun getCurrentPageIndex(): Int {
        return mCurrentPageIndex
    }

    /**
     * 由于View类中这个方法无法使用，直接copy处理
     *
     * @param view
     * @return 判断view是不是处在一个可滑动的布局中
     * @see ViewGroup.shouldDelayChildPressedState
     */
    private fun isInScrollingContainer(view: View): Boolean {
        var p = view.parent
        while (p is ViewGroup) {
            if (p.shouldDelayChildPressedState()) {
                return true
            }
            p = p.getParent()
        }
        return false
    }

    /**
     * 根据页码下标获取position
     *
     * @param pageIndex    页码
     * @param isLayoutToEnd true:页的第一个位置，false:页的最后一个位置
     * @return
     */
    private fun getPositionByPageIndex(pageIndex: Int, isLayoutToEnd: Boolean): Int {
        return if (isLayoutToEnd) pageIndex * mOnePageSize else pageIndex * mOnePageSize + mOnePageSize - 1
    }

    fun getDiffWidth(): Int {
        return Math.max(diffWidth, 0)
    }

    fun getDiffHeight(): Int {
        return Math.max(diffHeight, 0)
    }

    /**
     * 获取真实宽度
     *
     * @return
     */
    private fun getRealWidth(): Int {
        return width - paddingStart - paddingEnd
    }

    /**
     * 获取真实高度
     *
     * @return
     */
    private fun getRealHeight(): Int {
        return height - paddingTop - paddingBottom
    }

    /**
     * 填充布局
     *
     * @param recycler
     * @param state
     * @return 添加的像素数，用于滚动
     */
    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val layoutState = mLayoutState
        val start = layoutState.mAvailable
        var remainingSpace = layoutState.mAvailable
        val layoutChunkResult = mLayoutChunkResult
        while (remainingSpace > 0 && layoutState.hasMore(state)) {
            if (mShouldReverseLayout) {
                reverseLayoutChunk(recycler, state, layoutState, layoutChunkResult)
            } else {
                layoutChunk(recycler, state, layoutState, layoutChunkResult)
            }
            layoutState.mAvailable -= layoutChunkResult.mConsumed
            remainingSpace -= layoutChunkResult.mConsumed
        }
        val layoutToEnd = layoutState.mLayoutDirection == LayoutState.LAYOUT_END
        //因为最后一列或者一行可能只绘制了收尾的一个，补满
        while (layoutState.hasMore(state)) {
            val isNeedMoveSpan =
                if (layoutToEnd) isNeedMoveToNextSpan(layoutState.mCurrentPosition) else isNeedMoveToPreSpan(
                    layoutState.mCurrentPosition
                )
            if (isNeedMoveSpan) {
                //如果需要切换行或列，直接退出
                break
            }
            if (mShouldReverseLayout) {
                reverseLayoutChunk(recycler, state, layoutState, layoutChunkResult)
            } else {
                layoutChunk(recycler, state, layoutState, layoutChunkResult)
            }
        }
        //回收View
        recycleViews(recycler)
        return start - layoutState.mAvailable
    }

    /**
     * 正项布局
     *
     * @param recycler
     * @param state
     * @param layoutState
     * @param layoutChunkResult
     * @see .layoutChunk
     * @see .mShouldReverseLayout
     */
    private fun layoutChunk(
        recycler: RecyclerView.Recycler,
        state: State,
        layoutState: LayoutState?,
        layoutChunkResult: LayoutChunkResult?,
    ) {
        val layoutToEnd = layoutState!!.mLayoutDirection == LayoutState.LAYOUT_END
        val position = layoutState.mCurrentPosition
        val view = layoutState.next(recycler)
        if (layoutToEnd) {
            addView(view)
        } else {
            addView(view, 0)
        }
        layoutState.mCurrentPosition = if (layoutToEnd) layoutState.getNextPosition(
            position,
            mOrientation,
            mRows,
            mColumns,
            state
        ) else layoutState.getPrePosition(position, mOrientation, mRows, mColumns, state)
        measureChildWithMargins(view, mItemWidthUsed, mItemHeightUsed)
        //是否需要换行或者换列
        val isNeedMoveSpan =
            if (layoutToEnd) isNeedMoveToNextSpan(position) else isNeedMoveToPreSpan(position)
        layoutChunkResult!!.mConsumed =
            if (isNeedMoveSpan) if (mOrientation == HORIZONTAL) mItemWidth else mItemHeight else 0

        //记录的上一个View的位置
        val rect = layoutState.mOffsetRect
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (mOrientation == HORIZONTAL) {
            //水平滑动
            if (layoutToEnd) {
                //向后填充，绘制方向：从上到下
                if (isNeedMoveSpan) {
                    //下一列绘制，从头部开始
                    left = rect.left + mItemWidth + calculateClipOffset(true, position)
                    top = paddingTop
                } else {
                    //当前列绘制
                    left = rect.left
                    top = rect.bottom
                }
                right = left + mItemWidth
                bottom = top + mItemHeight
            } else {
                //向前填充，绘制方向：从下到上
                if (isNeedMoveSpan) {
                    //上一列绘制，从底部开启
                    left = rect.left - mItemWidth - calculateClipOffset(false, position)
                    bottom = height - paddingBottom
                } else {
                    //当前列绘制
                    left = rect.left
                    bottom = rect.top
                }
                top = bottom - mItemHeight
                right = left + mItemWidth
            }
        } else {
            if (layoutToEnd) {
                //向下填充，绘制方向：从左到右
                if (isNeedMoveSpan) {
                    //下一行绘制，从头部开始
                    left = paddingStart
                    top = rect.bottom + calculateClipOffset(true, position)
                } else {
                    //当前行绘制
                    left = rect.left + mItemWidth
                    top = rect.top
                }
                right = left + mItemWidth
                bottom = top + mItemHeight
            } else {
                //向上填充，绘制方向：从右到左
                if (isNeedMoveSpan) {
                    //上一行绘制，从尾部开始
                    right = width - paddingEnd
                    left = right - mItemWidth
                    bottom = rect.top - calculateClipOffset(false, position)
                    top = bottom - mItemHeight
                } else {
                    //当前行绘制
                    left = rect.left - mItemWidth
                    top = rect.top
                    right = left + mItemWidth
                    bottom = top + mItemHeight
                }
            }
        }
        layoutState.setOffsetRect(left, top, right, bottom)
        layoutDecoratedWithMargins(view, left, top, right, bottom)
    }

    /**
     * 反向布局
     *
     * @param recycler
     * @param state
     * @param layoutState
     * @param layoutChunkResult
     * @see .layoutChunk
     * @see .mShouldReverseLayout
     */
    private fun reverseLayoutChunk(
        recycler: RecyclerView.Recycler,
        state: State,
        layoutState: LayoutState?,
        layoutChunkResult: LayoutChunkResult?,
    ) {
        //仅处理水平反向滑动，垂直仅改变排列顺序
        val layoutToEnd = layoutState!!.mLayoutDirection == LayoutState.LAYOUT_END
        val position = layoutState.mCurrentPosition
        val view = layoutState.next(recycler)
        if (layoutToEnd) {
            addView(view)
        } else {
            addView(view, 0)
        }
        layoutState.mCurrentPosition = if (layoutToEnd) layoutState.getNextPosition(
            position,
            mOrientation,
            mRows,
            mColumns,
            state
        ) else layoutState.getPrePosition(position, mOrientation, mRows, mColumns, state)
        measureChildWithMargins(view, mItemWidthUsed, mItemHeightUsed)
        //是否需要换行或者换列
        val isNeedMoveSpan =
            if (layoutToEnd) isNeedMoveToNextSpan(position) else isNeedMoveToPreSpan(position)
        layoutChunkResult!!.mConsumed =
            if (isNeedMoveSpan) if (mOrientation == HORIZONTAL) mItemWidth else mItemHeight else 0

        //记录的上一个View的位置
        val rect = layoutState.mOffsetRect
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (mOrientation == HORIZONTAL) {
            //水平滑动
            if (layoutToEnd) {
                //向前填充，绘制方向：从上到下
                if (isNeedMoveSpan) {
                    //上一列绘制，从头部开始
                    left = rect.left - mItemWidth - calculateClipOffset(true, position)
                    top = paddingTop
                } else {
                    //当前列绘制
                    left = rect.left
                    top = rect.bottom
                }
                right = left + mItemWidth
                bottom = top + mItemHeight
            } else {
                //向后填充，绘制方向：从下到上
                if (isNeedMoveSpan) {
                    //下一列绘制，从底部开启
                    left = rect.left + mItemWidth + calculateClipOffset(false, position)
                    bottom = height - paddingBottom
                } else {
                    //当前列绘制
                    left = rect.left
                    bottom = rect.top
                }
                top = bottom - mItemHeight
                right = left + mItemWidth
            }
        } else {
            if (layoutToEnd) {
                //向下填充，绘制方向：从右到左
                if (isNeedMoveSpan) {
                    //下一行绘制，从尾部开始
                    right = width - paddingEnd
                    top = rect.bottom + calculateClipOffset(true, position)
                } else {
                    //当前行绘制，向前布局
                    right = rect.left
                    top = rect.top
                }
                left = right - mItemWidth
                bottom = top + mItemHeight
            } else {
                //向上填充，绘制方向：从左到右
                if (isNeedMoveSpan) {
                    //上一行绘制，从头部开始
                    left = paddingStart
                    right = left + mItemWidth
                    bottom = rect.top - calculateClipOffset(false, position)
                    top = bottom - mItemHeight
                } else {
                    //当前行绘制，向后布局
                    left = rect.right
                    right = left + mItemWidth
                    top = rect.top
                    bottom = top + mItemHeight
                }
            }
        }
        layoutState.setOffsetRect(left, top, right, bottom)
        layoutDecoratedWithMargins(view, left, top, right, bottom)
    }

    /**
     * @param delta    手指滑动的距离
     * @param recycler
     * @param state
     * @return
     */
    private fun scrollBy(
        delta: Int,
        recycler: RecyclerView.Recycler,
        state: State,
    ): Int {
        if (childCount == 0 || delta == 0 || mPageCount == 1) {
            return 0
        }
        mLayoutState.mRecycle = true
        val layoutDirection: Int
        layoutDirection = if (shouldHorizontallyReverseLayout()) {
            if (delta > 0) LayoutState.LAYOUT_START else LayoutState.LAYOUT_END
        } else {
            if (delta > 0) LayoutState.LAYOUT_END else LayoutState.LAYOUT_START
        }
        mLayoutState.mLayoutDirection = layoutDirection
        val layoutToEnd = layoutDirection == LayoutState.LAYOUT_END
        val absDelta = Math.abs(delta)
        if (DEBUG) {
            Log.i(
                TAG,
                "scrollBy -> before : childCount:" + childCount + ",recycler.scrapList.size:" + recycler.scrapList.size + ",delta:" + delta
            )
        }
        updateLayoutState(layoutToEnd, absDelta, true, state)
        var consumed = mLayoutState.mScrollingOffset + fill(recycler, state)
        if (layoutToEnd) {
            //向后滑动，添加补充距离
            consumed += mLayoutState.replenishDelta
        }
        if (consumed < 0) {
            return 0
        }
        //是否已经完全填充到头部或者尾部，滑动的像素>消费的像素
        val isOver = absDelta > consumed
        //计算实际可移动值
        val scrolled = if (isOver) layoutDirection * consumed else delta
        //移动
        offsetChildren(-scrolled)
        mLayoutState.mLastScrollDelta = scrolled

        //回收view，此步骤在移动之后
        recycleViews(recycler)
        if (DEBUG) {
            Log.i(
                TAG,
                "scrollBy -> end : childCount:" + childCount + ",recycler.scrapList.size:" + recycler.scrapList.size + ",delta:" + delta + ",scrolled:" + scrolled
            )
        }
        return scrolled
    }

    private fun updateLayoutState(
        layoutToEnd: Boolean, requiredSpace: Int,
        canUseExistingSpace: Boolean, state: State,
    ) {
        val child: View?
        //计算在不添加新view的情况下可以滚动多少（与布局无关）
        val scrollingOffset: Int
        if (layoutToEnd) {
            child = getChildClosestToEnd()
            scrollingOffset = if (shouldHorizontallyReverseLayout()) {
                -getDecoratedStart(child) + getStartAfterPadding()
            } else {
                getDecoratedEnd(child) - getEndAfterPadding()
            }
        } else {
            child = getChildClosestToStart()
            scrollingOffset = if (shouldHorizontallyReverseLayout()) {
                getDecoratedEnd(child) - getEndAfterPadding()
            } else {
                -getDecoratedStart(child) + getStartAfterPadding()
            }
        }
        getDecoratedBoundsWithMargins(child!!, mLayoutState.mOffsetRect)
        mLayoutState.mCurrentPosition = if (layoutToEnd) mLayoutState.getNextPosition(
            getPosition(
                child
            ), mOrientation, mRows, mColumns, state
        ) else mLayoutState.getPrePosition(
            getPosition(
                child
            ), mOrientation, mRows, mColumns, state
        )
        mLayoutState.mAvailable = requiredSpace
        if (canUseExistingSpace) {
            mLayoutState.mAvailable -= scrollingOffset
        }
        mLayoutState.mScrollingOffset = scrollingOffset
    }

    private fun getChildClosestToEnd(): View? {
        return getChildAt(childCount - 1)
    }

    private fun getChildClosestToStart(): View? {
        return getChildAt(0)
    }

    /**
     * 回收View
     *
     * @param recycler
     */
    private fun recycleViews(recycler: RecyclerView.Recycler) {
        //是否回收view
        if (!mLayoutState.mRecycle) {
            return
        }
        if (shouldHorizontallyReverseLayout()) {
            if (mLayoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                //水平向右或者垂直向下滑动
                recycleViewsFromStart(recycler)
            } else {
                //水平向左或者垂直向上滑动
                recycleViewsFromEnd(recycler)
            }
        } else {
            if (mLayoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                //水平向左或者垂直向上滑动
                recycleViewsFromEnd(recycler)
            } else {
                //水平向右或者垂直向下滑动
                recycleViewsFromStart(recycler)
            }
        }
    }

    private fun recycleViewsFromStart(recycler: RecyclerView.Recycler) {
        //如果clipToPadding==false，则不计算padding
        val clipToPadding = clipToPadding
        val start = if (clipToPadding) getStartAfterPadding() else 0
        val childCount = childCount
        for (i in childCount - 1 downTo 0) {
            val childAt = getChildAt(i)
            if (childAt != null) {
                val decorated = getDecoratedEnd(childAt)
                if (decorated >= start) {
                    continue
                }
                if (DEBUG) {
                    Log.w(
                        TAG,
                        "recycleViewsFromStart-removeAndRecycleViewAt: " + i + ", position: " + getPosition(
                            childAt
                        )
                    )
                }
                removeAndRecycleViewAt(i, recycler)
                //                removeAndRecycleView(childAt, recycler);
            }
        }
    }

    private fun recycleViewsFromEnd(recycler: RecyclerView.Recycler) {
        //如果clipToPadding==false，则不计算padding
        val clipToPadding = clipToPadding
        val end =
            if (clipToPadding) getEndAfterPadding() else if (mOrientation == HORIZONTAL) width else height
        val childCount = childCount
        for (i in childCount - 1 downTo 0) {
            val childAt = getChildAt(i)
            if (childAt != null) {
                val decorated = getDecoratedStart(childAt)
                if (decorated <= end) {
                    continue
                }
                if (DEBUG) {
                    Log.w(
                        TAG,
                        "recycleViewsFromEnd-removeAndRecycleViewAt: " + i + ", position: " + getPosition(
                            childAt
                        )
                    )
                }
                removeAndRecycleViewAt(i, recycler)
                //                removeAndRecycleView(childAt, recycler);
            }
        }
    }

    private fun getDecoratedEnd(child: View?): Int {
        val params = child!!.layoutParams as LayoutParams
        return if (mOrientation == HORIZONTAL) getDecoratedRight(child) + params.rightMargin else getDecoratedBottom(
            child
        ) + params.bottomMargin
    }

    private fun getDecoratedStart(child: View?): Int {
        val params = child!!.layoutParams as LayoutParams
        return if (mOrientation == HORIZONTAL) getDecoratedLeft(child) - params.leftMargin else getDecoratedTop(
            child
        ) - params.topMargin
    }

    private fun getEndAfterPadding(): Int {
        return if (mOrientation == HORIZONTAL) width - paddingEnd else height - paddingBottom
    }

    private fun getStartAfterPadding(): Int {
        return if (mOrientation == HORIZONTAL) paddingStart else paddingTop
    }

    private fun getClipToPaddingSize(): Int {
        return if (mOrientation == HORIZONTAL) paddingStart + paddingEnd else paddingTop + paddingBottom
    }

    /**
     * 计算[.getClipToPadding]==false时偏移量
     *
     * @param layoutToEnd 是否是向后布局
     * @param position    position
     * @return offset
     */
    private fun calculateClipOffset(layoutToEnd: Boolean, position: Int): Int {
        val clipToPadding = clipToPadding
        return if (!clipToPadding && position % mOnePageSize == if (layoutToEnd) 0 else mOnePageSize - 1) getClipToPaddingSize() else 0
    }

    private fun getEnd(): Int {
        return if (mOrientation == HORIZONTAL) getRealWidth() else getRealHeight()
    }

    /**
     * 移动Children
     *
     * @param delta 移动偏移量
     */
    private fun offsetChildren(delta: Int) {
        if (mOrientation == HORIZONTAL) {
            offsetChildrenHorizontal(delta)
        } else {
            offsetChildrenVertical(delta)
        }
    }

    /**
     * @return 当前Recycler是否是静止状态
     */
    private fun isIdle(): Boolean {
        return mRecyclerView == null || mRecyclerView!!.scrollState == SCROLL_STATE_IDLE
    }

    /**
     * @param position
     * @return 是否需要换到下一行或列
     */
    private fun isNeedMoveToNextSpan(position: Int): Boolean {
        return if (mOrientation == HORIZONTAL) {
            val surplus = position % mOnePageSize
            val rowIndex = surplus / mColumns
            //是否在最后一行
            rowIndex == 0
        } else {
            position % mColumns == 0
        }
    }

    /**
     * @param position
     * @return 是否需要换到上一行或列
     */
    private fun isNeedMoveToPreSpan(position: Int): Boolean {
        return if (mOrientation == HORIZONTAL) {
            val surplus = position % mOnePageSize
            //在第几行
            val rowIndex = surplus / mColumns
            //是否在第一行
            rowIndex == mRows - 1
        } else {
            position % mColumns == mColumns - 1
        }
    }

    private fun computeScrollOffset(state: State): Int {
        if (childCount == 0 || state.itemCount == 0) {
            return 0
        }
        val firstView = getChildAt(0) ?: return 0
        val position = getPosition(firstView)
        val avgSize = getEnd().toFloat() / if (mOrientation == HORIZONTAL) mColumns else mRows
        val index: Int
        index = if (mOrientation == HORIZONTAL) {
            //所在第几列
            val pageIndex = getPageIndexByPosition(position)
            pageIndex * mColumns + position % mColumns
        } else {
            //所在第几行
            position / mColumns
        }
        val scrollOffset: Int
        scrollOffset = if (shouldHorizontallyReverseLayout()) {
            val scrollRange = computeScrollRange(state) - computeScrollExtent(state)
            scrollRange - Math.round(index * avgSize + (getDecoratedEnd(firstView) - getEndAfterPadding()))
        } else {
            Math.round(index * avgSize + (getStartAfterPadding() - getDecoratedStart(firstView)))
        }
        if (DEBUG) {
            Log.i(TAG, "computeScrollOffset: $scrollOffset")
        }
        return scrollOffset
    }

    private fun computeScrollExtent(state: State): Int {
        if (childCount == 0 || state.itemCount == 0) {
            return 0
        }
        val scrollExtent = getEnd()
        if (DEBUG) {
            Log.i(TAG, "computeScrollExtent: $scrollExtent")
        }
        return scrollExtent
    }

    private fun computeScrollRange(state: State): Int {
        if (childCount == 0 || state.itemCount == 0) {
            return 0
        }
        val scrollRange = Math.max(mPageCount, 0) * getEnd()
        if (DEBUG) {
            Log.i(TAG, "computeScrollRange: $scrollRange")
        }
        return scrollRange
    }

    private fun resolveShouldLayoutReverse() {
        mShouldReverseLayout = if (mOrientation == VERTICAL || !isLayoutRTL()) {
            mReverseLayout
        } else {
            //水平滑动且是RTL
            !mReverseLayout
        }
    }

    fun getShouldReverseLayout(): Boolean {
        return mShouldReverseLayout
    }

    /**
     * @return 左上角第一个view的位置
     */
    fun getStartSnapRect(): Rect {
        return mStartSnapRect
    }

    /**
     * @return 右下角最后一个view的位置
     */
    fun getEndSnapRect(): Rect {
        return mEndSnapRect
    }

    /**
     * 根据下标计算页码
     *
     * @param position
     */
    fun calculateCurrentPageIndexByPosition(position: Int) {
        setCurrentPageIndex(getPageIndexByPosition(position))
    }

    fun getLayoutState(): LayoutState? {
        return mLayoutState
    }

    /**
     * @return 是否水平方向反转布局
     */
    fun shouldHorizontallyReverseLayout(): Boolean {
        return mShouldReverseLayout && mOrientation == HORIZONTAL
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        val childCount = childCount
        if (childCount == 0) {
            return null
        }
        var firstSnapPosition = NO_POSITION
        for (i in childCount - 1 downTo 0) {
            val childAt = getChildAt(i)
            if (childAt != null) {
                val position = getPosition(childAt)
                if (position % mOnePageSize == 0) {
                    firstSnapPosition = position
                    break
                }
            }
        }
        if (firstSnapPosition == NO_POSITION) {
            return null
        }
        var direction = if (targetPosition < firstSnapPosition) -1f else 1f
        if (shouldHorizontallyReverseLayout()) {
            direction = -direction
        }
        if (DEBUG) {
            Log.w(
                TAG,
                "computeScrollVectorForPosition-firstSnapPosition: $firstSnapPosition, targetPosition:$targetPosition,mOrientation :$mOrientation, direction:$direction"
            )
        }
        return if (mOrientation == HORIZONTAL) {
            PointF(direction, 0f)
        } else {
            PointF(0f, direction)
        }
    }

    /**
     * 自定义LayoutParams
     */
    class LayoutParams : RecyclerView.LayoutParams {
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {}
        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: MarginLayoutParams?) : super(source) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
        constructor(source: RecyclerView.LayoutParams?) : super(source) {}
    }

    private class SmoothScrollToPosition internal constructor(
        private val mPosition: Int,
        private val mLayoutManager: PageGridLayoutManager,
        private val mRecyclerView: RecyclerView,
    ) :
        Runnable {
        override fun run() {
            val smoothScroller = PageGridSmoothScroller(mRecyclerView, mLayoutManager)
            smoothScroller.setTargetPosition(mPosition)
            mLayoutManager.startSmoothScroll(smoothScroller)
        }
    }

    class LayoutState {
        /**
         * 可填充的View空间大小
         */
        var mAvailable = 0

        /**
         * 是否需要回收View
         */
        var mRecycle = false
        var mCurrentPosition = 0

        /**
         * 布局的填充方向
         * 值为 [.LAYOUT_START] or [.LAYOUT_END]
         */
        var mLayoutDirection = 0

        /**
         * 在滚动状态下构造布局状态时使用。
         * 它应该设置我们可以在不创建新视图的情况下进行滚动量。
         * 有效的视图回收需要设置
         */
        var mScrollingOffset = 0

        /**
         * 开始绘制的坐标位置
         */
        val mOffsetRect = Rect()

        /**
         * 最近一次的滑动数量
         */
        var mLastScrollDelta = 0

        /**
         * 需要补充滑动的距离
         */
        var replenishDelta = 0
        fun setOffsetRect(left: Int, top: Int, right: Int, bottom: Int) {
            mOffsetRect[left, top, right] = bottom
        }

        fun next(recycler: RecyclerView.Recycler): View {
            return recycler.getViewForPosition(mCurrentPosition)
        }

        fun hasMore(state: RecyclerView.State): Boolean {
            return mCurrentPosition >= 0 && mCurrentPosition < state.itemCount
        }

        /**
         * @param currentPosition 当前的位置
         * @param orientation     方向
         * @param rows            行数
         * @param columns         列数
         * @param state           状态
         * @return 下一个位置
         */
        fun getNextPosition(
            currentPosition: Int,
            orientation: Int,
            rows: Int,
            columns: Int,
            state: RecyclerView.State,
        ): Int {
            var position: Int
            val onePageSize = rows * columns
            if (orientation == HORIZONTAL) {
                val surplus = currentPosition % onePageSize
                //水平滑动
                //向后追加item
                if (surplus == onePageSize - 1) {
                    //一页的最后一个位置
                    position = currentPosition + 1
                } else {
                    //在第几列
                    val columnsIndex = currentPosition % columns
                    //在第几行
                    val rowIndex = surplus / columns
                    //是否在最后一行
                    val isLastRow = rowIndex == rows - 1
                    if (isLastRow) {
                        position = currentPosition - rowIndex * columns + 1
                    } else {
                        position = currentPosition + columns
                        if (position >= state.itemCount) {
                            //越界了
                            if (columnsIndex != columns - 1) {
                                //如果不是最后一列，计算换行位置
                                position = currentPosition - rowIndex * columns + 1
                            }
                        }
                    }
                }
            } else {
                //垂直滑动
                position = currentPosition + 1
            }
            return position
        }

        /**
         * @param currentPosition 当前的位置
         * @param orientation     方向
         * @param rows            行数
         * @param columns         列数
         * @param state           状态
         * @return 上一个位置
         */
        fun getPrePosition(
            currentPosition: Int,
            orientation: Int,
            rows: Int,
            columns: Int,
            state: State?,
        ): Int {
            val position: Int
            val onePageSize = rows * columns
            position = if (orientation == HORIZONTAL) {
                val surplus = currentPosition % onePageSize
                //水平滑动
                //向前追加item
                if (surplus == 0) {
                    //一页的第一个位置
                    currentPosition - 1
                } else {
                    //在第几行
                    val rowIndex = surplus / columns
                    //是否在第一行
                    val isFirstRow = rowIndex == 0
                    if (isFirstRow) {
                        currentPosition - 1 + (rows - 1) * columns
                    } else {
                        currentPosition - columns
                    }
                }
            } else {
                //垂直滑动
                currentPosition - 1
            }
            return position
        }

        companion object {
            const val LAYOUT_START = -1
            const val LAYOUT_END = 1
            const val SCROLLING_OFFSET_NaN = Int.MIN_VALUE
        }
    }

    class LayoutChunkResult {
        var mConsumed = 0
        protected var mFinished = false
        protected var mIgnoreConsumed = false
        protected var mFocusable = false
        protected fun resetInternal() {
            mConsumed = 0
            mFinished = false
            mIgnoreConsumed = false
            mFocusable = false
        }
    }

    /**
     * @see RecyclerView.LayoutManager.onSaveInstanceState
     * @see RecyclerView.LayoutManager.onRestoreInstanceState
     */
    data class SavedState(
        var mOrientation: Int = 0, //当前滑动方向
        var mRows: Int = 0, //行数
        var mColumns: Int = 0, //列数
        var mCurrentPageIndex: Int = NO_ITEM, //当前页码下标,从0开始
        var mReverseLayout: Boolean = false,
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        ) {
        }

        override fun describeContents(): Int {
            return 0
        }
        
        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(mOrientation)
            dest.writeInt(mRows)
            dest.writeInt(mColumns)
            dest.writeInt(mCurrentPageIndex)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        override fun toString(): String {
            return "SavedState{" +
                    "mOrientation=" + mOrientation +
                    ", mRows=" + mRows +
                    ", mColumns=" + mColumns +
                    ", mCurrentPageIndex=" + mCurrentPageIndex +
                    '}'
        }
    }

    interface PageChangedListener {
        /**
         * 页面总数量变化
         *
         * @param pageCount 页面总数，从1开始，为0时说明无数据，{[.NO_PAGER_COUNT]}
         */
        fun onPageCountChanged(@IntRange(from = 0) pageCount: Int)

        /**
         * 选中的页面下标
         *
         * @param prePageIndex     上次的页码，当{[.getItemCount]}为0时，为-1，{[.NO_ITEM]}
         * @param currentPageIndex 当前的页码，当{[.getItemCount]}为0时，为-1，{[.NO_ITEM]}
         */
        fun onPageIndexSelected(
            @IntRange(from = -1) prePageIndex: Int,
            @IntRange(from = -1) currentPageIndex: Int,
        )
    }
}