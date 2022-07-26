package com.black.im.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * MD5工具类
 */
object MD5Utils {
    // 十六进制下数字到字符的映射数组
    private val hexDigits = arrayOf("0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")
    /**
     * 指定算法为MD5的MessageDigest
     */
    private var messageDigest: MessageDigest? = null

    /**
     * * 获取文件的MD5值
     *
     * @param file 目标文件
     * @return MD5字符串
     */
    fun getFileMD5String(file: File): String {
        var ret = ""
        var `in`: FileInputStream? = null
        var ch: FileChannel? = null
        try {
            `in` = FileInputStream(file)
            ch = `in`.channel
            val byteBuffer: ByteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
                    file.length())
            messageDigest!!.update(byteBuffer)
            ret = bytesToHex(messageDigest!!.digest())
        } catch (e: IOException) {
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                }
            }
            if (ch != null) {
                try {
                    ch.close()
                } catch (e: IOException) {
                }
            }
        }
        return ret
    }

    /**
     * * 获取文件的MD5值
     *
     * @param fileName 目标文件的完整名称
     * @return MD5字符串
     */
    fun getFileMD5String(fileName: String?): String {
        return getFileMD5String(File(fileName))
    }

    /**
     * * MD5加密以byte数组表示的字符串
     *
     * @param sourceStr 目标字符串
     * @return MD5加密后的字符串
     */
    fun getMD5String(sourceStr: String): String {
        return getMD5String(sourceStr.toByteArray())
    }

    /**
     * * MD5加密以byte数组表示的字符串
     *
     * @param bytes 目标byte数组
     * @return MD5加密后的字符串
     */
    fun getMD5String(bytes: ByteArray?): String {
        messageDigest!!.update(bytes)
        return bytesToHex(messageDigest!!.digest())
    }

    /**
     * * 校验密码与其MD5是否一致
     *
     * @param pwd 密码字符串
     * @param md5 基准MD5值
     * @return 检验结果
     */
    fun checkPassword(pwd: String, md5: String?): Boolean {
        return getMD5String(pwd).equals(md5, ignoreCase = true)
    }

    /**
     * * 校验密码与其MD5是否一致
     *
     * @param pwd 以字符数组表示的密码
     * @param md5 基准MD5值
     * @return 检验结果
     */
    fun checkPassword(pwd: CharArray?, md5: String?): Boolean {
        return checkPassword(String(pwd!!), md5)
    }

    /**
     * * 检验文件的MD5值
     *
     * @param file 目标文件
     * @param md5  基准MD5值
     * @return 检验结果
     */
    fun checkFileMD5(file: File, md5: String?): Boolean {
        return getFileMD5String(file).equals(md5, ignoreCase = true)
    }

    /**
     * * 检验文件的MD5值
     *
     * @param fileName 目标文件的完整名称
     * @param md5      基准MD5值
     * @return 检验结果
     */
    fun checkFileMD5(fileName: String?, md5: String?): Boolean {
        return checkFileMD5(File(fileName), md5)
    }
    /**
     * * 将字节数组中指定区间的子数组转换成16进制字符串
     *
     * @param bytes 目标字节数组
     * @param start 起始位置（包括该位置）
     * @param end   结束位置（不包括该位置）
     * @return 转换结果
     */
    /**
     * * 将字节数组转换成16进制字符串
     *
     * @param bytes 目标字节数组
     * @return 转换结果
     */
    @JvmOverloads
    fun bytesToHex(bytes: ByteArray, start: Int = 0, end: Int = bytes.size): String {
        val sb = StringBuilder()
        for (i in start until start + end) {
            sb.append(byteToHex(bytes[i]))
        }
        return sb.toString()
    }

    /**
     * * 将单个字节码转换成16进制字符串
     *
     * @param bt 目标字节
     * @return 转换结果
     */
    fun byteToHex(bt: Byte): String {
        return hexDigits[(bt and 0xf0.toByte()).toInt() shr 4] + "" + hexDigits[(bt and 0xf.toByte()).toInt()]
    }

    /**
     * 将url转成文件名
     *
     * @param urlStr
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    fun parseUrlToFileName(urlStr: String): String { // 创建具有指定算法名称的信息摘要,MD5处理
        val md = MessageDigest.getInstance("MD5")
        // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
        val results = md.digest(urlStr.toByteArray(StandardCharsets.UTF_8))
        return byteArrayToHexString(results)
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @param b 字节数组
     * @return 十六进制字符串
     */
    private fun byteArrayToHexString(b: ByteArray): String {
        val resultSb = StringBuffer()
        for (i in b.indices) {
            resultSb.append(byteToHexString(b[i]))
        }
        return resultSb.toString()
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private fun byteToHexString(b: Byte): String {
        var n = b.toInt()
        if (n < 0) n = 256 + n
        val d1 = n / 16
        val d2 = n % 16
        return hexDigits[d1] + hexDigits[d2]
    }

    /** * 初始化messageDigest的加密算法为MD5  */
    init {
        try {
            messageDigest = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
        }
    }
}