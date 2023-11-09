package com.tech.android.ui.recyclerviewkit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tech.android.ui.recyclerviewkit.item.RvAdapter

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class RvRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private var isLoadingMore = false
    private var canLoadingMore = false
    private var footerView: View? = null
    private var loadMoreScrollListener: OnScrollListener? = null

    inner class LoadMoreScrollListener(val prefetchSize: Int, val callback: () -> Unit) :
        OnScrollListener() {
        private val rvAdapter = adapter as RvAdapter
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            if (!canLoadingMore) {
                return
            }

            //根据当前的滑动状态  决定要不要添加footer view 要不要执行上拉加载分页的动作
            if (isLoadingMore) {
                return
            }

            //判断当前列表显示的item的数量  以显示的item的数量小于0
            val totalItemCount = rvAdapter.itemCount
            if (totalItemCount <= 0) {
                return
            }

            // 此时 咱们需要在滑动的状态为 拖动状态时，就要判断要不要添加 footer
            // 为了防止滑动到底部的时候footer  view 显示不出来
            // 1 判断列表能不能滑动，
            val canScrollVertical = recyclerView.canScrollVertically(1)
            // 还有一种特殊的情况  canScrollVertical  是检查他能不能继续向下滑动
            // 特殊情况是 咱们的列表已经滑动到底部了，但是分页失败
            val lastVisibilityItem = findLastVisibleItem(recyclerView)
            if (lastVisibilityItem <= 0) return
            val arriveBottom = lastVisibilityItem >= totalItemCount - 1

            if (newState == SCROLL_STATE_DRAGGING && (canScrollVertical || arriveBottom)) {
                //判断recyclerView是不是能滑动
                addFooterView()
            }
            //不能在滑动停止的时候添加 footer view
            if (newState != SCROLL_STATE_IDLE) {
                return
            }
            //预加载
            val arrivePrefetchPosition = totalItemCount - lastVisibilityItem <= prefetchSize
            if (!arrivePrefetchPosition) {
                return
            }

            isLoadingMore = true
            //预加载的回调
            callback()
        }

        private fun findLastVisibleItem(recyclerView: RecyclerView): Int {
            when (val layoutManager = recyclerView.layoutManager) {
                is LinearLayoutManager -> {
                    return layoutManager.findLastVisibleItemPosition()
                }
                is StaggeredGridLayoutManager -> {
                    return layoutManager.findLastVisibleItemPositions(null)[0]
                }

            }
            return -1
        }

        private fun addFooterView() {
            val footerView = getFooterView()
            //有个坑   在边界场景下 会出现多次添加
            // 主要是为了避免 removeFooterView不及时，在边界场景下可能出现 footerview还没从recyclerView上移除掉 我们就有调用了addFooterView()
            // 造成的重复添加的问题，此时会抛出 add  view must call removeView from it parent first exception
            if (footerView.parent != null) {
                footerView.post {
                    addFooterView()
                }
            } else {
                rvAdapter.addFooterView(footerView)
            }
        }


        private fun getFooterView(): View {
            if (footerView == null) {
                footerView = LayoutInflater.from(context)
                    .inflate(R.layout.rvkit_loading_footer, this@RvRecyclerView, false)
            }
            return footerView!!
        }
    }

    fun addHeaderView(@LayoutRes layoutRes: Int) {
        val rvAdapter = adapter as RvAdapter
        rvAdapter.addHeaderView(getHeaderView(layoutRes))
    }

    private fun getHeaderView(layoutRes: Int): View {
        return LayoutInflater.from(context)
            .inflate(layoutRes, this@RvRecyclerView, false)
    }


    fun enableLoadMore(callback: () -> Unit, prefetchSize: Int) {
        if (adapter !is RvAdapter) {
//            VLog.e("enableLoadMore must use hiadapter")
            return
        }
        canLoadingMore = true
        loadMoreScrollListener = LoadMoreScrollListener(prefetchSize, callback)
        addOnScrollListener(loadMoreScrollListener!!)
    }


    fun disableLoadMore() {
        if (adapter !is RvAdapter) {
//            VLog.e("enableLoadMore must be hiadapter")
            return
        }
        val rvAdapter = adapter as RvAdapter
        footerView?.let {
            if (footerView!!.parent != null) {
                rvAdapter.removeFooterView(footerView!!)
            }
        }
        loadMoreScrollListener?.let {
            removeOnScrollListener(loadMoreScrollListener!!)
            loadMoreScrollListener = null
            footerView = null
            isLoadingMore = false
        }
    }

    fun isLoadingMore(): Boolean {
        return isLoadingMore
    }

    fun setCanLoadingMore(canLoadingMore: Boolean) {
        this.canLoadingMore = canLoadingMore
    }

    fun loadFinished(success: Boolean) {

        if (adapter !is RvAdapter) {
//            VLog.e("enableLoadMore must be hiadapter")
            return
        }

        val rvAdapter = adapter as RvAdapter
        if (!success) {
            footerView?.let {
                if (footerView!!.parent != null) {
                    rvAdapter.removeFooterView(footerView!!)
                }
            }
        } else {
            isLoadingMore = false
            footerView?.let {
                if (footerView!!.parent != null) {
                    rvAdapter.removeFooterView(footerView!!)
                }
            }
        }
    }

}