package com.tech.android.base.update.model

import java.io.Serializable

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/28 .
 * <P>
 * Description:
 * <P>
 */
data class UpdateConfig(
    val isUpdateFromStore: Boolean = true,
    val packageName:String = "",
    val targetStoreUri:String = "",
    val targetBrandList: String = "",
    var apkUrl: String = "",
    var filePrefix: String = "",
    var filePath: String = "",
    val isShowNotification: Boolean = true,
    val notificationIconRes: Int = -1,
) : Serializable {


    val STORE_TYPE = "STORE"
    val DOWNLOAD_TYPE = "DOWNLOAD_APK"

    fun checkUpdateConfigCorrect(type: String): Boolean {
        if (STORE_TYPE == type) { //商店升级配置检测
            return isUpdateFromStore && targetBrandList.isNotBlank()
        }

        if (DOWNLOAD_TYPE == type) {
            return apkUrl.isNotBlank()
        }

        return true
    }
}
