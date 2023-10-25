package com.tech.android.ui.camerakit.utils

import android.content.res.Resources

/**
 * @auther: xuan
 * @date  : 2023/10/23 .
 * <P>
 * Description:
 * <P>
 */
object DimensUtil {
    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}