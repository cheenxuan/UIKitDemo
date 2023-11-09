package com.baidu.ocr.sdk.tool;

import android.util.Base64;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class Base64Util {
    public Base64Util() {
    }

    public static byte[] encodeBase64(String source) {
        byte[] encodeBase64 = new byte[0];

        try {
            encodeBase64 = Base64.encode(source.getBytes(), Base64.NO_WRAP);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return encodeBase64;
    }

    public static String byte2String(byte[] source) {
        byte[] encodeBase64 = new byte[0];

        try {
            encodeBase64 = Base64.encode(source, Base64.NO_WRAP);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return new String(encodeBase64);
    }

    public static String encodeBase64TOString(String source) {
        return new String(encodeBase64(source));
    }

    public static byte[] string2Byte(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    public static String decodeBase64TOString(String base64) {
        return new String(string2Byte(base64));
    }
}