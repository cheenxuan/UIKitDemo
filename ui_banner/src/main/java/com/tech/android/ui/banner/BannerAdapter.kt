package com.tech.android.ui.banner

import android.content.Context
import android.os.SystemClock
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewpager.widget.PagerAdapter

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
open class BannerAdapter(val mContext: Context) : PagerAdapter() {
    
    private var mCacheViews = SparseArray<BannerViewHolder>()
    private var mBannerClickListener: IBanner.OnBannerClickListener? = null
    private var mBindAdapter: IBindAdapter? = null
    private var models: List<BannerMo?>? = ArrayList()

    //fix: data.size == 2
    private var fakeModels: List<BannerMo?>? = ArrayList()

    //No quick clicks
    private var lastClickTime: Long = 0
    private val interval = 1000L

    fun setBannerData(models: List<BannerMo?>) {
        this.models = models

        //fix: data.size == 2
        if (models.size == 2) {
            fakeModels = models
        }

        //初始化数据
        initCachedView()
        notifyDataSetChanged()
    }

    fun setBindAdapter(bindAdapter: IBindAdapter?) {
        mBindAdapter = bindAdapter
    }


    fun setOnBannerClickListener(onBannerClickListener: IBanner.OnBannerClickListener?) {
        mBannerClickListener = onBannerClickListener
    }

    fun setLayoutResId(@LayoutRes layoutResId: Int) {
        mLayoutResId = layoutResId
    }

    fun setAutoPlay(autoPlay: Boolean) {
        mAutoPlay = autoPlay
    }


    fun setLoop(loop: Boolean) {
        mLoop = loop
    }

    /***
     * 是否开启自动轮播
     */
    private var mAutoPlay = true

    /***
     * 非自动轮播状态下是否可以循环切换
     */
    private var mLoop = false

    private var mLayoutResId = -1


    override fun getCount(): Int {
        //无限轮播
        return if (mAutoPlay) Int.MAX_VALUE else if (mLoop) Int.MAX_VALUE else getRealCount()
    }

    /***
     * 获取Banner页面的数量
     * @return
     */
    fun getRealCount(): Int {
        return if (models == null) 0 else models!!.size + if (fakeModels == null) 0 else fakeModels!!.size
    }

    /***
     * 获取fake Banner页面的数量
     * @return
     */
    fun getFakeCount(): Int {
        return if (fakeModels == null) 0 else fakeModels!!.size
    }


    /***
     * 获取初次展示的item的位置
     * @return
     */
    fun getFirstItem(): Int {
        return Int.MAX_VALUE / 2 - Int.MAX_VALUE / 2 % getRealCount()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    /***
     * 实例化item的方法
     * @param container
     * @param position
     * @return
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var realPosition = position
        if (getRealCount() > 0) {
            realPosition = position % getRealCount()
        }
        val viewHolder = mCacheViews[realPosition]
        if (container == viewHolder.rootView.parent) {
            container.removeView(viewHolder.rootView)
        }

        //fix: data.size == 2
        if (realPosition >= getFakeCount()) {
            realPosition -= getFakeCount()
        }
        onBind(viewHolder, models!![realPosition]!!, realPosition)
        if (viewHolder.rootView.parent != null) {
            (viewHolder.rootView.parent as ViewGroup).removeView(viewHolder.rootView)
        }
        container.addView(viewHolder.rootView)
        return viewHolder.rootView
    }

    override fun getItemPosition(`object`: Any): Int {
        //让item每次都能刷新
        return POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    private fun onBind(viewHolder: BannerViewHolder, bannerMo: BannerMo, position: Int) {
        viewHolder.rootView.setOnClickListener { v: View? ->
            if (mBannerClickListener != null) {
                //No quick clicks
                val currentTime = SystemClock.elapsedRealtime()
                if (currentTime - lastClickTime > interval) {
                    mBannerClickListener?.onBannerClick(viewHolder, bannerMo, position)
                    lastClickTime = currentTime
                }
            }
        }
        if (mBindAdapter != null) {
            mBindAdapter!!.onBind(viewHolder, bannerMo, position)
        }
    }

    private fun initCachedView() {
        mCacheViews = SparseArray()
        for (i in models!!.indices) {
            val viewHolder = BannerViewHolder(createView(LayoutInflater.from(mContext), null))
            mCacheViews.put(i, viewHolder)
        }
        //fix：data.size == 2 添加fakeView
        for (i in fakeModels!!.indices) {
            val viewHolder = BannerViewHolder(createView(LayoutInflater.from(mContext), null))
            mCacheViews.put(models!!.size + i, viewHolder)
        }
    }

    private fun createView(layoutInflater: LayoutInflater, parent: ViewGroup?): View {
        require(mLayoutResId != -1) { "you must be set setLayoutResId first" }
        return layoutInflater.inflate(mLayoutResId, parent, false)
    }

    class BannerViewHolder(var rootView: View) {
        var viewSparseArray: SparseArray<View?>? = null
            private set

        fun <V : View?> findViewById(id: Int): V? {
            if (rootView !is ViewGroup) {
                return rootView as V
            }
            if (viewSparseArray == null) {
                viewSparseArray = SparseArray()
            }
            var childView = viewSparseArray!![id] as V?
            if (childView == null) {
                childView = rootView.findViewById(id)
                viewSparseArray!!.put(id, childView)
            }
            return childView
        }
    }
}