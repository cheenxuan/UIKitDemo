package com.tech.android.ui.recyclerviewkit.refresh

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
abstract class RvOverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :FrameLayout(context, attrs, defStyleAttr){

    enum class RefreshState {
        /**
         * 初始态
         */
        STATE_INIT,

        /**
         * Header展示的状态
         */
        STATE_VISIBLE,

        /**
         * 刷新中的状态
         */
        STATE_OVER,

        /**
         * 超出可刷新的距离的状态
         */
        STATE_REFRESH,

        /**
         * 超出刷新位置松开收后的状态
         */
        STATE_OVER_RELEASE
    }

    private var mState = RefreshState.STATE_INIT

    /**
     * 触发下拉舒心束缚的最小高度
     */
    var mPullRefreshHeight:Int

    /**
     * 最小阻尼
     */
    var minDamp = 1.6f

    /**
     * 最大阻尼
     */
    var maxDamp = 2.2f

    init {
        mPullRefreshHeight = dp2px(66f, resources)
        init()
    }

    /**
     * 初始化
     */
    abstract fun init()

    abstract fun onScroll(scrollY: Int, mPullRefreshHeight: Int)

    /**
     * 显示Overlay
     */
    abstract fun onVisible()

    /**
     * 超过Overlay，释放就会加载
     */
    abstract fun onOver()

    /**
     * 正在刷新
     */
    abstract fun onRefresh()

    /**
     * 刷新完成
     */
    abstract fun onFinish()

    open fun setState(mState: RefreshState) {
        this.mState = mState
    }

    open fun getState(): RefreshState? {
        return mState
    }

    private fun dp2px(dp: Float, resources: Resources): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .toInt()
    }
}