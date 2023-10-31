package com.tech.android.base.update.interfaces

import android.content.Context

/**
 * @auther: QinjianXuan
 * @date  : 2023/10/30 .
 * <P>
 * Description:
 * <P>
 */
interface DownloadCallback {
    fun onProgressUpdate(progress: Int)
    fun onDownloadCompleted(context: Context, filePath: String)
    fun onDownloadFailed(error: String?)
}