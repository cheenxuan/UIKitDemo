package com.baidu.ocr.sdk.tool;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_STR = "AES/ECB/PKCS5Padding";

    public AESUtil() {
    }

    public static String generateBase64EnKeyFromStr(String session) throws Exception {
        byte[] bytes = generateEnKey(session.getBytes());
        return Base64Util.byte2String(bytes);
    }

    public static byte[] generateEnKey(byte[] session) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(session);
        kgen.init(256, secureRandom);
        SecretKey secretKey = kgen.generateKey();
        byte[] bytes = secretKey.getEncoded();
        return bytes;
    }

    public static byte[] encrypt(byte[] src, byte[] aesKey) throws Exception {
        Cipher cipher = getCipher(aesKey, 1);
        byte[] ret = cipher.doFinal(src);
        return ret;
    }

    public static byte[] decrypt(byte[] src, byte[] aesKey) throws Exception {
        Cipher cipher = getCipher(aesKey, 2);
        byte[] original = cipher.doFinal(src);
        return original;
    }

    private static Cipher getCipher(byte[] aesKey, int mode) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(mode, secretKeySpec);
        return cipher;
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];

            for(int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte)(high * 16 + low);
            }

            return result;
        }
    }
}
