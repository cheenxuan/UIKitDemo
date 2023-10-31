package com.tech.android.base.update.download

import android.os.Handler
import android.os.Looper
import com.tech.android.base.update.interfaces.ProgressListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*


/**
 * @auther: xuan
 * @date  : 2023/10/30 .
 * <P>
 * Description:
 * <P>
 */
class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener,
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                progressListener.let {
                    val progress = (100f * totalBytesRead / responseBody.contentLength()).toInt()
                    // Ensure this is running on the main thread for UI updates
                    it.onProgress(progress)
                }
                return bytesRead
            }
        }
    }
}