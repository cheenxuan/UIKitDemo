package com.tech.android.ui.banner

import android.content.Context
import android.widget.Scroller

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 *         mDuration : 值越大 滑动越慢
 * <P>
 */
class BannerScroller(context: Context, private var mDuration: Int = 5000) : Scroller(context) {

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, mDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }
}