package com.tech.android.base.update

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.FileProvider
import com.tech.android.base.update.interfaces.DownloadCallback
import com.tech.android.base.update.model.UpdateConfig
import com.tech.android.base.update.service.FileDownloadService
import com.tech.android.base.update.service.FileDownloadService.LocalBinder
import com.tech.android.base.update.utils.DeviceInfoUtil
import com.tech.android.base.update.utils.StoreInstallUtil
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * @auther: QinjianXuan
 * @date  : 2023/10/28 .
 * <P>
 * Description: App Update 实例类
 * <P>
 */
object AppUpdate {

    const val STORE_TYPE = "STORE"
    const val DOWNLOAD_TYPE = "DOWNLOAD_APK"

    private var updateConfig: UpdateConfig? = null
    private var fileDownloadService: FileDownloadService? = null
    private var isBound = false
    private var updateCallback: DownloadCallback? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private val downloadCallback = object : DownloadCallback {
        override fun onProgressUpdate(progress: Int) {
            mHandler.post {
                updateCallback?.onProgressUpdate(progress)
            }
        }

        override fun onDownloadCompleted(context: Context, filePath: String) {
            mHandler.post {
                updateCallback?.onDownloadCompleted(context, filePath)
            }
            if (isBound) {
                try {
                    context.applicationContext.unbindService(serviceConnection);
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isBound = false;
                }
            }
            val apkFile = File(filePath)
            installApkFile(context, apkFile)
        }


        override fun onDownloadFailed(error: String?) {
            mHandler.post {
                updateCallback?.onDownloadFailed(error)
            }
        }

    }
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as LocalBinder
            fileDownloadService = binder.getService()
            isBound = true
            fileDownloadService?.downloadFile(
                downloadUrl = getUpdateConfig()?.apkUrl!!,
                destinationPath = getUpdateConfig()?.filePath!!,
                callback = downloadCallback
            )
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            fileDownloadService = null
            isBound = false
        }
    }

    fun setUpdateConfig(updateConfig: UpdateConfig? = null): AppUpdate {
        this.updateConfig = updateConfig
        return this
    }

    fun getUpdateConfig(): UpdateConfig? = updateConfig

    fun setUpdateCallback(callback: DownloadCallback): AppUpdate {
        this.updateCallback = callback
        return this
    }

    fun update(context: Context) {
        //检查升级配置是否完整
        //优先使用商店更新
        if (storeUpdate(context)) {
            //商店更新无效后，选择apk下载更新
            apkUpdate(context)
        }
    }

    fun storeUpdate(context: Context): Boolean {
        return !checkConfig(STORE_TYPE) || !checkCanUpdateFromStore(context.applicationContext)
    }

    private fun checkConfig(type: String): Boolean {
        if (updateConfig == null) return false
        if (updateConfig?.checkUpdateConfigCorrect(type) != true) return false
        return true
    }

    private fun checkCanUpdateFromStore(context: Context): Boolean {
        val targetBrandList = updateConfig?.targetBrandList
        val targetDevice = targetBrandList?.joinToString(separator = ", ")

        if (targetBrandList.isNullOrEmpty()) return false
        //检查手机是否为中文-中国
        if (!DeviceInfoUtil.IsChineseLanguage()) return false
        //华为手机
        if (targetDevice!!.contains(
                DeviceInfoUtil.BRAND_HUA_WEI,
                ignoreCase = true
            ) //目标市场是否存在华为应用市场
            && DeviceInfoUtil.isHuaweiDevice() //手机是否为华为手机
            && DeviceInfoUtil.isHarmonyOS()    //手机是否为鸿蒙OS
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_HUA_WEI
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_HUA_WEI)
        }

        //荣耀手机
        if (targetDevice.contains(DeviceInfoUtil.BRAND_HONOR, ignoreCase = true) //目标市场是否存在荣耀应用市场
            && DeviceInfoUtil.isHonorDevice() //手机是否为荣耀手机
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_HUA_WEI
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_HUA_WEI)
        }

        //小米手机
        if (targetDevice.contains(DeviceInfoUtil.BRAND_XIAO_MI, ignoreCase = true) //目标市场是否存在小米应用市场
            && DeviceInfoUtil.isXiaomiDevice() //手机是否为小米手机
            && DeviceInfoUtil.checkIsMiui()    //手机是否为MIUI系统
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_XIAO_MI
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_XIAO_MI)
        }

        //OPPO手机
        if (targetDevice.contains(DeviceInfoUtil.BRAND_OPPO, ignoreCase = true) //目标市场是否存在OPPO应用市场
            && DeviceInfoUtil.isOppoDevice() //手机是否为OPPO手机
            && (StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_OPPO
            ) || StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_ONE_PLUS
            ))
        ) {

            return if (updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_OPPO)) {
                true
            } else updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_ONE_PLUS)

        }


        //VIVO手机
        if (targetDevice.contains(DeviceInfoUtil.BRAND_VIVO, ignoreCase = true) //目标市场是否存在VIVO应用市场
            && DeviceInfoUtil.isVivoDevice() //手机是否为VIVO手机
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_VIVO
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_VIVO)
        }

        //三星手机
        if (targetDevice.contains(DeviceInfoUtil.BRAND_SAMSUNG, ignoreCase = true) //目标市场是否存在三星应用市场
            && DeviceInfoUtil.isSamsungDevice() //手机是否为三星手机
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_SAMSUNG
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_SAMSUNG)
        }


        //OnePlus
        if (targetDevice.contains(
                DeviceInfoUtil.BRAND_ONE_PLUS,
                ignoreCase = true
            ) //目标市场是否存在OnePlus应用市场
            && DeviceInfoUtil.isOnePlusDevice() //手机是否为OnePlus手机
            && StoreInstallUtil.checkIsInstalledAppMarket(
                context,
                StoreInstallUtil.MARKET_PACKAGER_NAME_ONE_PLUS
            )
        ) {
            return updateFormStore(context, StoreInstallUtil.MARKET_PACKAGER_NAME_ONE_PLUS)
        }

        return false
    }

    private fun updateFormStore(context: Context, marketPkg: String?): Boolean {
        return launchAppDetail(context, marketPkg)
    }

    /**
     * 启动到应用商店app详情界面
     * @param appPkg 目标App的包名
     * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    private fun launchAppDetail(context: Context, marketPkg: String?): Boolean {
        try {
            val appPkg =
                if (getUpdateConfig()?.packageName.isNullOrBlank()) context.packageName else getUpdateConfig()?.packageName
            if (appPkg.isNullOrBlank()) return false
            val targetUri =
                if (getUpdateConfig()?.targetStoreUri.isNullOrBlank()) "market://details?id=$appPkg" else getUpdateConfig()?.targetStoreUri
            val uri: Uri = Uri.parse(targetUri)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (marketPkg?.isNotBlank() == true) {
                intent.setPackage(marketPkg)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun apkUpdate(context: Context) {
        if (!checkConfig(DOWNLOAD_TYPE)) return

        val applicationID: String = context.packageName

        val downloadApkUrlMd5 = getUpperMD5Str16(getUpdateConfig()?.apkUrl + applicationID)

        val filePrefix = "${context.getExternalFilesDir(null)?.path}/apk/"
        val filePath = "$filePrefix${applicationID}_$downloadApkUrlMd5.apk"
        updateConfig?.filePrefix = filePrefix
        updateConfig?.filePath = filePath

        if (!isBound) {
            val intent = Intent(context, FileDownloadService::class.java)
            context.applicationContext.bindService(
                intent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        } else {
            fileDownloadService?.downloadFile(
                downloadUrl = getUpdateConfig()?.apkUrl!!,
                destinationPath = getUpdateConfig()?.filePath!!,
                callback = downloadCallback
            )
        }
    }

    fun reDownload(context: Context) {
        apkUpdate(context)
    }

    private fun getUpperMD5Str16(str: String): String? {
        var messageDigest: MessageDigest? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val byteArray = messageDigest!!.digest()
        val md5StrBuff = StringBuilder()
        for (b in byteArray) {
            if (Integer.toHexString(0xFF and b.toInt()).length == 1) md5StrBuff.append("0").append(
                Integer.toHexString(0xFF and b.toInt())
            ) else md5StrBuff.append(Integer.toHexString(0xFF and b.toInt()))
        }
        return md5StrBuff.toString().uppercase(Locale.getDefault()).substring(8, 24)
    }

    fun installApkFile(context: Context, file: File) {

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            var uri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri =
                    FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                uri = Uri.fromFile(file)
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            if (context.packageManager.queryIntentActivities(intent, 0).size > 0) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}