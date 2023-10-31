package com.tech.android.base.update.download

import com.tech.android.base.update.interfaces.ProgressListener
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager


/**
 * @auther: xuan
 * @date  : 2023/10/30 .
 * <P>
 * Description:
 * <P>
 */
object FileDownloadManager {

    val BUFFER_SIZE = 8192

    fun createReadProgressDownloadClient(listener: ProgressListener): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(Interceptor { chain ->
                val originalResponse: Response = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(ProgressResponseBody(originalResponse.body!!, listener))
                    .build()
            })
            .sslSocketFactory(trustAllSSLSocketFactory(), trustAllManager)
            .hostnameVerifier(getHostnameVerifier())
            .build()
    }

    fun createDownloadClient(): OkHttpClient {
        return OkHttpClient.Builder()
//            .addNetworkInterceptor(Interceptor { chain ->
//                val originalResponse: Response = chain.proceed(chain.request())
//                originalResponse.newBuilder()
//                    .body(ProgressResponseBody(originalResponse.body!!, listener))
//                    .build()
//            })
            .sslSocketFactory(trustAllSSLSocketFactory(), trustAllManager)
            .hostnameVerifier(getHostnameVerifier())
            .build()
    }

    fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { hostname, sslSession -> true }
    }

    private fun trustAllSSLSocketFactory(): SSLSocketFactory {
        val sc: SSLContext = SSLContext.getInstance("TLS")
        sc.init(null, arrayOf(trustAllManager), SecureRandom())
        return sc.socketFactory
    }

    private val trustAllManager: X509TrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String?) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }
}