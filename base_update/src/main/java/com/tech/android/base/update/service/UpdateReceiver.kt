package com.tech.android.base.update.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import com.tech.android.base.update.AppUpdate
import com.tech.android.base.update.R
import com.tech.android.base.update.utils.StoreInstallUtil

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/28 .
 * <P>
 * Description: 
 * <P>
 */
class UpdateReceiver : BroadcastReceiver() {
    private var lastProgress = 0
    
    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.getAction()
        val notifyId = 1
        if (context.packageName + DOWNLOAD_ONLY == action) {
            //下载
            val progress: Int = intent.getIntExtra(PROGRESS, 0)
            val systemService: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (progress != -1) {
                lastProgress = progress
            }
            showNotification(context, notifyId, progress, notificationChannel, systemService)

            // 下载完成
            if (progress == 100) {
                downloadComplete(context, notifyId, systemService)
            }
        } else if (context.packageName + RE_DOWNLOAD == action) {
            //TODO 重新下载
            AppUpdate.reDownload(context)
        } else if (context.packageName + CANCEL_DOWNLOAD == action) {
            //取消下载
            val systemService: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            downloadComplete(context, notifyId, systemService)
        }
    }

    /**
     * 下载完成
     *
     * @param context
     * @param notifyId
     * @param systemService
     */
    private fun downloadComplete(
        context: Context,
        notifyId: Int,
        systemService: NotificationManager
    ) {
        systemService.cancel(notifyId)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            systemService.deleteNotificationChannel(notificationChannel)
        }
    }

    /**
     * 显示通知栏
     *
     * @param context
     * @param id
     * @param progress
     * @param notificationChannel
     * @param systemService
     */
    private fun showNotification(
        context: Context,
        id: Int,
        progress: Int,
        notificationChannel: String,
        systemService: NotificationManager
    ) {
        val notificationName = "notification"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                notificationName,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(false)
            channel.setShowBadge(false)
            channel.enableVibration(false)
            systemService.createNotificationChannel(channel)
        }
        val builder = Notification.Builder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationChannel)
        }

        //设置图标
        val notificationIconRes: Int = AppUpdate.getUpdateConfig()?.notificationIconRes ?: -1
        if (notificationIconRes != 0) {
            builder.setSmallIcon(notificationIconRes)
            builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, notificationIconRes))
        } else {
            builder.setSmallIcon(android.R.mipmap.sym_def_app_icon)
            builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, android.R.mipmap.sym_def_app_icon))
        }

        // 设置进度
        builder.setProgress(100, lastProgress, false)
        builder.setWhen(System.currentTimeMillis())
        builder.setShowWhen(true)
        builder.setAutoCancel(false)
        if (progress == -1) {
            val intent = Intent(context.packageName + RE_DOWNLOAD)
            intent.setPackage(context.packageName)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.setContentIntent(pendingIntent)
            // 通知栏标题
            builder.setContentTitle(context.resources.getString(R.string.update_download_fail))
        } else {
            // 通知栏标题
            builder.setContentTitle((StoreInstallUtil.getAppName(context) + " " + context.resources.getString(R.string.update_has_download) + progress).toString() + "%")
        }

        // 设置只响一次
        builder.setOnlyAlertOnce(true)
        val build = builder.build()
        systemService.notify(id, build)
    }

    companion object {
        private const val notificationChannel = "10000"

        /**
         * 进度key
         */
        const val PROGRESS = "app.progress"

        /**
         * ACTION_UPDATE
         */
        const val DOWNLOAD_ONLY = "app.update"

        /**
         * ACTION_RE_DOWNLOAD
         */
        const val RE_DOWNLOAD = "app.re_download"

        /**
         * 取消下载
         */
        const val CANCEL_DOWNLOAD = "app.download_cancel"
        const val REQUEST_CODE = 1001

        /**
         * 发送进度
         *
         * @param context
         * @param progress
         */
        fun send(context: Context, progress: Int) {
            val intent = Intent(context.packageName + DOWNLOAD_ONLY)
            intent.putExtra(PROGRESS, progress)
            context.sendBroadcast(intent)
        }

        /**
         * 取消下载
         *
         * @param context
         */
        fun cancelDownload(context: Context) {
            val intent = Intent(context.packageName + CANCEL_DOWNLOAD)
            context.sendBroadcast(intent)
        }
    }
}