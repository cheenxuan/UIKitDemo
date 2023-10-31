package com.tech.android.base.update.utils

import android.os.Build
import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.*

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/28 .
 * <P>
 * Description: 手机厂商工具类
 * <P>
 */
object DeviceInfoUtil {
    //华为
    const val BRAND_HUA_WEI = "HUAWEI"

    //荣耀
    const val BRAND_HONOR = "HONOR"

    //小米
    const val BRAND_XIAO_MI = "Xiaomi"

    //OPPO
    const val BRAND_OPPO = "OPPO"

    //一加
    const val BRAND_ONE_PLUS = "OnePlus"

    //RealMe 真我
    const val BRAND_REAL_ME = "realme"

    //VIVO
    const val BRAND_VIVO = "vivo"
    
    //三星
    const val BRAND_SAMSUNG = "samsung"


    const val HARMONY_OS = "harmony"

    //中文-中国
    const val LANGUAGE_ZH = "zh"

    ///获取手机型号
    fun getSystemModel(): String? = Build.MODEL

    //获取手机品牌
    fun getDeviceBrand() = Build.BRAND

    //获取手机厂商
    private fun getDeviceManufacturer(): String? = Build.MANUFACTURER

    //获取当前手机系统语言。 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
    fun getSystemLanguage() = Locale.getDefault().language

    //是否为中文环境
    fun IsChineseLanguage() = LANGUAGE_ZH == getSystemLanguage()

    //是否是荣耀设备
    fun isHonorDevice() = getDeviceManufacturer().equals(BRAND_HONOR, ignoreCase = true)

    //是否是小米设备
    fun isXiaomiDevice() = getDeviceManufacturer().equals(BRAND_XIAO_MI, ignoreCase = true)

    //是否是oppo设备
    //realme 是oppo的海外品牌后面脱离了；一加是oppo的独立运营品牌。因此判断它们是需要单独判断
    fun isOppoDevice() = getDeviceManufacturer().equals(BRAND_OPPO, ignoreCase = true)

    //是否是一加手机
    fun isOnePlusDevice() = getDeviceManufacturer().equals(BRAND_ONE_PLUS, ignoreCase = true)

    //是否是realme手机
    fun isRealmeDevice() = getDeviceManufacturer().equals(BRAND_REAL_ME, ignoreCase = true)

    //是否是vivo设备
    fun isVivoDevice() = getDeviceManufacturer().equals(BRAND_VIVO, ignoreCase = true)

    //是否是华为设备
    fun isHuaweiDevice() = getDeviceManufacturer().equals(BRAND_HUA_WEI, ignoreCase = true)
    
    //是否为三星设备
    fun isSamsungDevice() = getDeviceManufacturer().equals(BRAND_SAMSUNG, ignoreCase = true)

    //检查当前手机是否为鸿蒙系统
    fun isHarmonyOS(): Boolean {
        try {
            val clz = Class.forName("com.huawei.system.BuildEx")
            val method: Method = clz.getMethod("getOsBrand")
            return HARMONY_OS == method.invoke(clz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    //是否为MIUI系统
    fun checkIsMiui() = !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))

    //是否为EMUI或者MagicUI
    fun checkIsEmuiOrMagicUI(): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            //官方方案，但是只适用于api31以上（Android 12）
            try {
                val clazz = Class.forName("com.hihonor.android.os.Build")
                true
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                false
            }
        } else {
            //网上方案，测试了 荣耀畅玩8C
            //荣耀20s、荣耀x40 、荣耀v30 pro 四台机型，均可正常判断
            !TextUtils.isEmpty(getSystemProperty("ro.build.version.emui"))
        }
    }

    private fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }


}