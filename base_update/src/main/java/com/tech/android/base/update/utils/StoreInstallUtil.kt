package com.tech.android.base.update.utils

import android.content.Context
import android.content.pm.PackageInfo


/**
 * @auther: QinjianXuan
 * @date  : 2023/10/28 .
 * <P>
 * Description:
 * <P>
 */
object StoreInstallUtil {

    const val MARKET_PACKAGER_NAME_HUA_WEI = "com.huawei.appmarket"
    const val MARKET_PACKAGER_NAME_XIAO_MI = "com.xiaomi.market"
    const val MARKET_PACKAGER_NAME_OPPO = "com.oppo.market"
    const val MARKET_PACKAGER_NAME_VIVO = "com.bbk.appstore"
    const val MARKET_PACKAGER_NAME_ONE_PLUS = "com.heytap.market"
    const val MARKET_PACKAGER_NAME_SAMSUNG = "com.sec.android.app.samsungapps"

//    <!--华为应用市场-->
//    <package android:name="com.huawei.appmarket" />
//    <!--小米应用商店-->
//    <package android:name="com.xiaomi.market" />
//    <!--OPPO软件商店-->
//    <package android:name="com.oppo.market" />
//    <!--VIVO应用商店-->
//    <package android:name="com.bbk.appstore" />
//    <!--三星应用商店-->
//    <package android:name="com.sec.android.app.samsungapps" />

    /**
     * 获取应用程序名称
     */
    fun getAppName(context: Context): String? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val labelRes = packageInfo.applicationInfo.labelRes
            context.resources.getString(labelRes)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun checkIsInstalledAppMarket(context: Context?, marketPackageName: String): Boolean {
        if (context == null) return false
        var packageInfo: PackageInfo?
        try {
            packageInfo = context.packageManager.getPackageInfo(marketPackageName, 0)
        } catch (e: Exception) {
            return false
            e.printStackTrace()
        }
        return packageInfo != null
    }

}