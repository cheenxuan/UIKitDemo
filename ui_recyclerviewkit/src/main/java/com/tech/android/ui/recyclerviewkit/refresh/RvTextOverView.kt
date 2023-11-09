package com.tech.android.ui.recyclerviewkit.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.tech.android.ui.recyclerviewkit.R

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
class RvTextOverView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
):RvOverView(context, attrs, defStyleAttr) {
    
    private var mText: TextView? = null
    private var mRotateView: ImageView? = null

    override fun init() {
        LayoutInflater.from(context).inflate(R.layout.rvkit_refresh_overview, this, true)
        mText = findViewById(R.id.rv_refresh_text)
        mRotateView = findViewById(R.id.rv_refresh_rotate)
    }

    override fun onScroll(scrollY: Int, mPullRefreshHeight: Int) {}

    override fun onVisible() {
        mText!!.text = "下拉刷新"
    }

    override fun onOver() {
        mText!!.text = "松开刷新"
    }

    override fun onRefresh() {
        mText!!.text = "正在刷新..."
        val operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rvkit_refresh_rotate_anim)
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        mRotateView!!.startAnimation(operatingAnim)
    }

    override fun onFinish() {
        mRotateView!!.clearAnimation()
    }
}