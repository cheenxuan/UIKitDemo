package com.tech.android.base.update.model

import java.io.Serializable

/**
 * @auther: xuan
 * @date  : 2023/10/28 .
 * <P>
 * Description:
 * <P>
 */
data class UpdateConfig(
    val debug: Boolean = true,
    val isUpdateFromStore: Boolean = true,
    val targetBrandList: MutableList<String> = mutableListOf(),
    var apkUrl: String = "",
    var filePrefix: String = "",
    var filePath: String = "",
    val isShowNotification: Boolean = true,
    val notificationIconRes: Int = -1,
    val requestHeaders: Map<String, Any> = mutableMapOf(),
    val requestParams: Map<String, Any> = mutableMapOf(),
    val isAutoDownloadBackground: Boolean = false,
    val downloadProgressShowType: Int = 1,
) : Serializable {


    val STORE_TYPE = "STORE"
    val DOWNLOAD_TYPE = "DOWNLOAD_APK"

    fun checkUpdateConfigCorrect(type: String): Boolean {
        if (STORE_TYPE == type) { //商店升级配置检测
            return isUpdateFromStore && targetBrandList.isNotEmpty()
        }

        if (DOWNLOAD_TYPE == type) {
            return apkUrl.isNotBlank()
        }

        return true
    }
}
