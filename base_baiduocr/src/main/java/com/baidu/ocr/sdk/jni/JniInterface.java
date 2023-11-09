package com.baidu.ocr.sdk.jni;

import android.content.Context;

import com.baidu.ocr.sdk.exception.SDKError;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class JniInterface {
    private static Throwable loadLibraryError;

    public JniInterface() {
    }

    public static Throwable getLoadLibraryError() {
        return loadLibraryError;
    }

    public native byte[] init(Context var1, String var2);

    public native byte[] initWithBin(Context var1, String var2) throws SDKError;

    public native byte[] initWithBinLic(Context var1, String var2, String var3) throws SDKError;

    public native String getToken(Context var1);

    public native String getTokenFromLicense(Context var1, byte[] var2, int var3);

    static {
        try {
            System.loadLibrary("ocr-sdk");
            loadLibraryError = null;
        } catch (Throwable var1) {
            loadLibraryError = var1;
        }

    }
}
