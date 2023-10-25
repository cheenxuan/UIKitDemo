package com.tech.android.ui.tabkit.top

import android.graphics.Bitmap
import com.tech.android.ui.tabkit.TabInfo

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/18 .
 * <P>
 * Description:
 * <P>
 */
class TabTopInfo<Color> @JvmOverloads constructor(
    val name: String? = null,
    val defaultColor: Color? = null,
    val tintColor: Color? = null,
    val defaultBitmap: Bitmap? = null,
    val selectedBitmap: Bitmap? = null,
) : TabInfo() {

    enum class TabType {
        BITMAP, TEXT
    }

    var tabType: TabType? =
        if (defaultBitmap != null && selectedBitmap != null) TabType.BITMAP else TabType.TEXT
    var defaultSize = 15f
    var selectedSize = 15f

}