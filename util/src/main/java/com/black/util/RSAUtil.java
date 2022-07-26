package com.black.util;

import android.text.TextUtils;
import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAUtil {
    public final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyJdVXw+TvnFnE2ElRmJflkFO2" +
            "0wSjJzS+XV887at9jTKa+EhpSg7aCNgCcFQfCEVpHVKegR4s02+4RH4q+y0gP/yI" +
            "jAdyKAh16gC9NiM83WpN/PfBCOon55bIJI5G0OBi1I0el+3rpBqtRRzlRfiOXi4C" +
            "6pmO0ayVmP5rVNASsQIDAQAB";

    //构建Cipher实例时所传入的的字符串，默认为"RSA/NONE/PKCS1Padding"
    private static String sTransform = "RSA/ECB/PKCS1Padding";

    //进行Base64转码时的flag设置，默认为Base64.DEFAULT
    private static int sBase64Mode = Base64.DEFAULT;
    private final static PublicKey publicKey;

    static {
        publicKey = keyStrToPublicKey(PUBLIC_KEY);
    }

    /**
     * 加密或解密数据的通用方法
     *
     * @param srcData 待处理的数据
     * @param key     公钥或者私钥
     * @param mode    指定是加密还是解密，值为Cipher.ENCRYPT_MODE或者Cipher.DECRYPT_MODE
     */
    private static byte[] processData(byte[] srcData, Key key, int mode) {
        //用来保存处理结果
        byte[] resultBytes = null;
        try {
            //获取Cipher实例
            Cipher cipher = Cipher.getInstance(sTransform);
            //初始化Cipher，mode指定是加密还是解密，key为公钥或私钥
            cipher.init(mode, key);
            //处理数据
            resultBytes = cipher.doFinal(srcData);
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        }
        return resultBytes;
    }

    /**
     * 使用公钥加密数据，结果用Base64转码
     */
    public static String encryptDataByPublicKey(String source) {
        int length = TextUtils.isEmpty(source) ? 0 : source.length();
        int stepCount = 100;
        byte[] resultBytes = new byte[0];
        if (length > stepCount) {
            int offset = 0;
            while (offset < length) {
                String subStr = source.substring(offset, Math.min(offset + stepCount, length));
                resultBytes = addByteArray(resultBytes, processData(subStr.getBytes(), publicKey, Cipher.ENCRYPT_MODE));
                offset += stepCount;
            }
        } else {
            resultBytes = processData(source.getBytes(), publicKey, Cipher.ENCRYPT_MODE);
        }
        return Base64.encodeToString(resultBytes, sBase64Mode);
    }

    private static byte[] addByteArray(byte[] oldBytes, byte[] addBytes) {
        int oldBytesLength = oldBytes.length;
        int addBytesLength = addBytes.length;
        if (oldBytesLength == 0) {
            return addBytes;
        }
        if (addBytesLength == 0) {
            return oldBytes;
        }
        byte[] newBytes = new byte[oldBytesLength + addBytesLength];
        System.arraycopy(oldBytes, 0, newBytes, 0, oldBytesLength);
        System.arraycopy(addBytes, 0, newBytes, oldBytesLength, addBytesLength);
        return newBytes;
    }

    /**
     * 将字符串形式的公钥转换为公钥对象
     */
    public static PublicKey keyStrToPublicKey(String publicKeyStr) {
        PublicKey publicKey = null;
        byte[] keyBytes = Base64.decode(publicKeyStr, sBase64Mode);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
        }
        return publicKey;
    }
}