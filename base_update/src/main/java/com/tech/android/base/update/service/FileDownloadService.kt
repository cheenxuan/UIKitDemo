package com.tech.android.base.update.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.tech.android.base.update.download.FileDownloadManager
import com.tech.android.base.update.interfaces.DownloadCallback
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * @auther: xuan
 * @date  : 2023/10/30 .
 * <P>
 * Description:
 * <P>
 */
class FileDownloadService : Service() {

    private val TAG = "FileDownloadService"
    private val binder = LocalBinder()
    private val updateReceiver = UpdateReceiver()
    private lateinit var okHttpClient: OkHttpClient

    //安装apk权限时版本判断条件
    private val installApkJudgeRule: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.R

    inner class LocalBinder : Binder() {

        fun getService(): FileDownloadService {
            return this@FileDownloadService
        }
    }

    override fun onCreate() {
        super.onCreate()
        okHttpClient = OkHttpClient()
        registerReceiver(updateReceiver, IntentFilter(packageName + UpdateReceiver.DOWNLOAD_ONLY))
        registerReceiver(updateReceiver, IntentFilter(packageName + UpdateReceiver.RE_DOWNLOAD))
        registerReceiver(updateReceiver, IntentFilter(packageName + UpdateReceiver.CANCEL_DOWNLOAD))
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun downloadFile(
        downloadUrl: String,
        destinationPath: String,
        callback: DownloadCallback? = null,
    ) {
        
        startDownload(downloadUrl, destinationPath, callback)
    }

    private fun startDownload(
        downloadUrl: String,
        destinationPath: String,
        callback: DownloadCallback? = null,
    ) {
        val client: OkHttpClient = FileDownloadManager.createDownloadClient()

        val request: Request = Request.Builder()
            .url(downloadUrl)
            .build()

        try {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    UpdateReceiver.send(this@FileDownloadService, -1)
                    callback?.onDownloadFailed(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        UpdateReceiver.send(this@FileDownloadService, -1)
                        callback?.onDownloadFailed("Download failed with code: " + response.code)
                        return
                    }

                    response.body?.let { responseBody ->
                        try {
                            val storagePrefix =
                                destinationPath.substring(0, destinationPath.lastIndexOf("/") + 1)

                            val file: File = File(storagePrefix)
                            if (!file.exists()) {
                                file.mkdir()
                            }

                            val contentLength = responseBody.contentLength()
                            if (contentLength <= 0) {
                                UpdateReceiver.send(this@FileDownloadService, -1)
                                callback?.onDownloadFailed("apk length is error!")
                                return@let
                            }

                            val apkFile = File(destinationPath)
                            if (apkFile.exists() && apkFile.length() == contentLength) {
                                UpdateReceiver.send(this@FileDownloadService, 100)
                                callback?.onDownloadCompleted(applicationContext, destinationPath)
                                return@let
                            }

                            val outputStream = FileOutputStream(apkFile)
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            var totalBytesRead: Long = 0
                            var initProgress = 0
                            while (responseBody.byteStream().read(buffer)
                                    .also { bytesRead = it } != -1
                            ) {
                                outputStream.write(buffer, 0, bytesRead)

                                totalBytesRead += bytesRead
                                val progress = (totalBytesRead * 100 / contentLength).toInt()

                                if (initProgress != progress) {
                                    // Notify the UI of the download progress
                                    UpdateReceiver.send(this@FileDownloadService, initProgress)
                                    callback?.onProgressUpdate(initProgress)
                                    initProgress = progress
                                }
                            }

                            outputStream.flush()
                            outputStream.close()
                            UpdateReceiver.send(this@FileDownloadService, 100)
                            callback?.onDownloadCompleted(applicationContext, destinationPath)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            UpdateReceiver.send(this@FileDownloadService, -1)
                            callback?.onDownloadFailed(e.message)
                        }
                    }
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateReceiver)
    }
}