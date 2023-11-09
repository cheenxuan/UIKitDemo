package com.baidu.ocr.sdk.tool;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * @auther: xuan
 * @date : 2023/11/1 .
 * <p>
 * Description:
 * <p>
 */
public class RSAUtil {
    private static final String ALGORITHM_RSA = "RSA";

    public RSAUtil() {
    }

    public static byte[] rsaEncrypt(byte[] data, String key) {
        byte[] ret = null;
        if (data != null && data.length > 0 && key != null) {
            try {
                Key publicKey = loadPublicKey(key);
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(1, publicKey);
                ret = cipher.doFinal(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public static byte[] rsaDecrypt(byte[] data, String key) {
        byte[] ret = null;
        if (data != null && data.length > 0 && key != null) {
            try {
                Key privateKey = loadPrivateKey(key);
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(2, privateKey);
                ret = cipher.doFinal(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public static PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        byte[] buffer = Base64Util.string2Byte(publicKeyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        byte[] buffer = Base64Util.string2Byte(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
