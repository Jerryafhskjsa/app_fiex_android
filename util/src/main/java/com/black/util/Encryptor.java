package com.black.util;

import java.util.Arrays;

/**
 * 一种弱加密器，采用字节移位和合并来变换字符串字节数组，再通过密码本对应，实现字符串加解密
 */
public class Encryptor {
    private final static char[] CODE_BOOK = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '='};

    /**
     * 加密字符串
     *
     * @param plainText 明文
     * @param key       密钥
     * @return 加密结果
     */
    public static String encrypt(String plainText, String key) {
        if (plainText == null) {
            return null;
        }
        //获取源字符串字节
        byte[] sourceBytes = plainText.getBytes();
        //获取源key字节
        byte[] keyBytes = key.getBytes();
        //key字节含1数量
        int keyTrueLength = 0;
        for (int i = 0; i < keyBytes.length; i++) {
            byte keyByte = keyBytes[i];
            for (int j = 0; j < 8; j++) {
                if ((keyByte & (1 << (7 - j))) != 0) {
                    keyTrueLength++;
                }
            }
        }
        int sourceAndKeyLength = sourceBytes.length * 8 + keyTrueLength;
        int cipherTextLength = sourceAndKeyLength / 24;
        if (sourceAndKeyLength % 24 != 0) {
            cipherTextLength++;
        }
        //密文字节长度
        int cipherTextBytesLength = cipherTextLength * 24;
        //额外补充 字节长度
        int extraBytesLenght = cipherTextBytesLength - sourceAndKeyLength;
        //密文字节转化字符
        byte[] cipherTextBytes = new byte[cipherTextLength * 3 + 3];
        //原文加key加补位字节，字符串
        System.arraycopy(sourceBytes, 0, cipherTextBytes, 0, sourceBytes.length);
        //key 值1数量位全补1
        for (int i = 0; i < keyTrueLength / 8; i++) {
            cipherTextBytes[sourceBytes.length + i] = (byte) 0xff;
        }
        byte temp = 0;
        for (int i = 0; i < keyTrueLength % 8; i++) {
            temp |= 1 << (7 - i);
        }
        if (temp != 0) {
            cipherTextBytes[sourceBytes.length + keyTrueLength / 8] = temp;
        }
        //额外补充位全补0
        for (int i = 0; i < extraBytesLenght / 8; i++) {
            cipherTextBytes[sourceBytes.length + keyTrueLength / 8 + (temp == 0 ? 0 : 1) + i] = 0;
        }
        //补充3个字节长度标识额外补0长度，额外补0长度不会大于24，不会数据溢出
        cipherTextBytes[cipherTextLength * 3] = (byte) ((extraBytesLenght & 0xff0000) >>> 16);
        cipherTextBytes[cipherTextLength * 3 + 1] = (byte) ((extraBytesLenght & 0xff00) >>> 8);
        cipherTextBytes[cipherTextLength * 3 + 2] = (byte) (extraBytesLenght & 0xff);
        //正向合并
        for (int i = 0; i < 5; i++) {
            cipherTextBytes = transformForward(cipherTextBytes, keyBytes);
        }
        //将加密字节数组组装成字符串
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cipherTextBytes.length; i = i + 3) {
            sb.append(CODE_BOOK[(byte) ((cipherTextBytes[i] & 0xff) >>> 2)]);
            sb.append(CODE_BOOK[(byte) (((cipherTextBytes[i] & 0x03) << 4) | ((cipherTextBytes[i + 1] & 0xff) >>> 4))]);
            sb.append(CODE_BOOK[(byte) (((cipherTextBytes[i + 1] & 0x0f) << 2) | ((cipherTextBytes[i + 2] & 0xff) >>> 6))]);
            sb.append(CODE_BOOK[(byte) (cipherTextBytes[i + 2] & 0x3f)]);
        }
        return sb.toString();
    }

    /**
     * 数组变换
     *
     * @param source   源数组
     * @param keyBytes 密钥字节数组
     * @return 变换后数组
     */
    public static byte[] transformForward(byte[] source, byte[] keyBytes) {
        int length = source.length;
        byte[] result = new byte[length];
        //将原文与密钥合并
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (source[i] ^ keyBytes[i % keyBytes.length]);
        }
        //移位
        for (int i = 0; i < result.length / 2; i++) {
            byte temp1 = result[i];
            byte temp2 = result[result.length - 1 - i];
            //字节1前4位加字节4前二位为字节1
            result[i] = (byte) (((temp1 & 0xf0)) | ((temp2 & 0xff) >>> 4));
            //字节2后4位加字节2后4位为字节2
            result[source.length - 1 - i] = (byte) (((temp1 & 0x0f) << 4) | ((temp2 & 0x0f)));
        }
        return result;
    }

    /**
     * 解密字符串
     *
     * @param cipherText 密文
     * @param key        密钥
     * @return 解密结果
     */
    public static String decrypt(String cipherText, String key) {
        if (cipherText == null) {
            return null;
        }
        //密文字符数组
        char[] chars = cipherText.toCharArray();
        //密文长度必须为3的倍数
        if (chars.length % 4 != 0) {
            throw new RuntimeException("decrypt failed，cipher text error!");
        }
        //获取源key字节
        byte[] keyBytes = key.getBytes();
        //key字节含1数量
        int keyTrueLength = 0;
        for (int i = 0; i < keyBytes.length; i++) {
            byte keyByte = keyBytes[i];
            for (int j = 0; j < 8; j++) {
                if ((keyByte & (1 << (7 - j))) != 0) {
                    keyTrueLength++;
                }
            }
        }
        //解析密文字符数组，得到byte数组
        byte[] cipherTextBytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            byte b = 0;
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                b = (byte) (chars[i] - 'A');
            } else if (chars[i] >= 'a' && chars[i] <= 'z') {
                b = (byte) (chars[i] - 'a' + 26);
            } else if (chars[i] >= '0' && chars[i] <= '9') {
                b = (byte) (chars[i] - '0' + 52);
            } else if (chars[i] == '+') {
                b = 62;
            } else if (chars[i] == '=') {
                b = 63;
            }
            cipherTextBytes[i] = b;
        }
        //合并密文字节数组
        byte[] cipherTextNewBytes = new byte[cipherTextBytes.length * 6 / 8];
        for (int i = 0; i < cipherTextNewBytes.length; i = i + 3) {
            cipherTextNewBytes[i] = (byte) ((cipherTextBytes[i * 4 / 3] << 2) | ((cipherTextBytes[i * 4 / 3 + 1] & 0xff) >>> 4));
            cipherTextNewBytes[i + 1] = (byte) ((cipherTextBytes[i * 4 / 3 + 1] << 4) | ((cipherTextBytes[i * 4 / 3 + 2] & 0xff) >>> 2));
            cipherTextNewBytes[i + 2] = (byte) ((cipherTextBytes[i * 4 / 3 + 2] << 6) | ((cipherTextBytes[i * 4 / 3 + 3] & 0xff)));
        }
        //逆向合并运算
        for (int i = 0; i < 5; i++) {
            cipherTextNewBytes = transformBackward(cipherTextNewBytes, keyBytes);
        }
        //添加额外字节长度
        int extraLength = (cipherTextNewBytes[cipherTextNewBytes.length - 3] << 16)
                | (cipherTextNewBytes[cipherTextNewBytes.length - 2] << 8)
                | cipherTextNewBytes[cipherTextNewBytes.length - 1];
        //额外字节长度大于24则不正确
        if (extraLength > 24) {
            throw new RuntimeException("decrypt failed，password error!");
        }
        int plainTextBytesLength = (cipherTextNewBytes.length * 8 - keyTrueLength - extraLength - 24) / 8;
        return new String(cipherTextNewBytes, 0, plainTextBytesLength);
    }

    /**
     * 反数组移位
     *
     * @param source   源数组
     * @param keyBytes 密钥字节数组
     * @return 结果数组
     */
    public static byte[] transformBackward(byte[] source, byte[] keyBytes) {
        int length = source.length;
        byte[] result;
        //复制源数组
        result = Arrays.copyOf(source, length);
        //移位
        for (int i = 0; i < length / 2; i++) {
            byte temp1 = result[i];
            byte temp2 = result[length - 1 - i];
            //字节1前4位加字节4前二位为字节1
            result[i] = (byte) (((temp1 & 0xf0)) | ((temp2 & 0xff) >>> 4));
            //字节2后4位加字节2后4位为字节2
            result[length - 1 - i] = (byte) (((temp1 & 0x0f) << 4) | ((temp2 & 0x0f)));
        }
        //合并
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (result[i] ^ keyBytes[i % keyBytes.length]);
        }
        return result;
    }
}
