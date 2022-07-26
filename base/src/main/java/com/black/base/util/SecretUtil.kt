package com.black.base.util

import com.black.util.CommonUtil
import kotlin.experimental.and

object SecretUtil {
    fun getSecretString(key: String?): String? {
        val bytes = getCertificate(key)
        return bytes?.let { String(it) }
    }

    fun getSecretMD5(key: String?): String? {
        val bytes = getCertificate(key)
        return if (bytes == null) null else CommonUtil.MD5(bytes)
    }

    external fun getCertificate(key: String?): ByteArray?
    fun printBytes(bytes: ByteArray?): String? {
        if (bytes == null) {
            return null
        }
        val sb = StringBuilder()
        sb.append("[")
        for (i in bytes.indices) {
            if (i == 0) {
                sb.append("").append(bytes[i]).append("")
            } else {
                sb.append(",").append("").append(bytes[i]).append("")
            }
        }
        sb.append("]")
        return sb.toString()
    }

    private fun toShort(by: Byte): Short {
        return (by.toShort() and 0x00ff.toShort())
    }

    fun printUnsignedBytes(bytes: ByteArray?): String? {
        if (bytes == null) {
            return null
        }
        val sb = StringBuilder()
        sb.append("[")
        for (i in bytes.indices) {
            if (i == 0) {
                sb.append("").append(toShort(bytes[i]).toInt()).append("")
            } else {
                sb.append(",").append("").append(toShort(bytes[i]).toInt()).append("")
            }
        }
        sb.append("]")
        return sb.toString()
    }

    init {
        System.loadLibrary("fryingSecret")
    }
}