package com.tech.android.base.uikitdemo.recyclerview

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tech.android.base.uikitdemo.R
import com.tech.android.ui.recyclerviewkit.RvEmptyView
import com.tech.android.ui.recyclerviewkit.RvRecyclerView
import com.tech.android.ui.recyclerviewkit.item.RvAdapter
import com.tech.android.ui.recyclerviewkit.item.RvDataItem
import com.tech.android.ui.recyclerviewkit.refresh.RvOverView
import com.tech.android.ui.recyclerviewkit.refresh.RvRefresh
import com.tech.android.ui.recyclerviewkit.refresh.RvRefreshLayout
import com.tech.android.ui.recyclerviewkit.refresh.RvTextOverView

open class ListDemoActivity : AppCompatActivity(), RvRefresh.RefreshListener {

    var pageIndex: Int = 1
    lateinit var uiAdapter: RvAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var refreshHeaderView: RvTextOverView
    private var refreshLayout: RvRefreshLayout? = null
    private var recyclerView: RvRecyclerView? = null
    private var emptyView: RvEmptyView? = null
    private var loadingView: ContentLoadingProgressBar? = null


    companion object {
        const val PREFETCH_SIZE = 5
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_demo)
        initUI()
    }

    private fun initUI() {
        this.refreshLayout = findViewById(R.id.refresh_layout)
        this.recyclerView = findViewById(R.id.recycler_view)
        this.emptyView = findViewById(R.id.empty_view)
        this.loadingView = findViewById(R.id.content_Loading)
//        toolBar?.changeTitle(getToolBarTitle())
        refreshHeaderView = RvTextOverView(this)
        refreshLayout?.setRefreshOverView(refreshHeaderView)
        refreshLayout?.setRefreshListener(this)
        layoutManager = createLayoutManager()
        uiAdapter = RvAdapter(this)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = uiAdapter
        emptyView?.visibility = View.GONE
//        emptyView?.setIcon(R.string.if_detail)
        emptyView?.setText("没有找到数据，要不刷新试试看")
        emptyView?.setButton("刷新", View.OnClickListener {
            onRefresh()
        })
        loadingView?.visibility = View.VISIBLE
        pageIndex = 1
    }

    fun finishRefresh(dataItem: List<RvDataItem<*, out RecyclerView.ViewHolder>>?) {
        val success = dataItem != null && dataItem.isNotEmpty()
        val refresh = pageIndex == 1

        if (dataItem == null || dataItem.size < getPageSie()) {
            recyclerView?.setCanLoadingMore(false)
        } else {
            recyclerView?.setCanLoadingMore(true)
        }

        if (refresh) {
            loadingView?.visibility = View.GONE
            refreshLayout?.refreshFinished()
            uiAdapter.clearItems()
            if (success) {
                emptyView?.visibility = View.GONE
                uiAdapter.addItems(dataItem!!, true)
            } else {
                //判断列表上是否已经有数据 没有 显示 emptyVIew
                if (uiAdapter.itemCount <= 0) {
                    emptyView?.visibility = View.VISIBLE
                }
            }
        } else {
            if (success) {
                uiAdapter.addItems(dataItem!!, true)
            }
            recyclerView?.loadFinished(success)
        }
    }

    fun enableLoadMore(callback: () -> Unit) {
        //这里可以直接这么写吗？
        //为了方式同时下拉刷新和上拉加载 的请求 此处该做处理
        recyclerView?.enableLoadMore({
            if (refreshHeaderView.getState() == RvOverView.RefreshState.STATE_REFRESH) {

                //正在刷新
                recyclerView?.loadFinished(false)
                return@enableLoadMore
            }
            pageIndex++
            callback()
        }, PREFETCH_SIZE)

    }

    fun disableLoadMore() {
        recyclerView?.disableLoadMore()
    }

    open fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    @CallSuper
    override fun onRefresh() {
        if (recyclerView?.isLoadingMore() == true) {
            //正在分页
            refreshLayout?.post {
                refreshLayout?.refreshFinished()
            }
            return
        }
        pageIndex = 1
    }

    fun getPageSie(): Int {
        return 10
    }

    open fun getToolBarTitle(): String? {
        return ""
    }

}
