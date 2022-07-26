package com.black.base.util

import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object HttpUtil {
    fun copyStream(url: String?, f: File?) {
        var fileOutputStream: FileOutputStream? = null
        var inputStream: InputStream? = null
        try {
            inputStream = getInputStream(url)
            val data = ByteArray(1024)
            var len = 0
            fileOutputStream = FileOutputStream(f)
            while (inputStream!!.read(data).also { len = it } != -1) {
                fileOutputStream.write(data, 0, len)
            }
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    fun copyFile(src: File?, dest: File?) {
        try {
            val inputStream = FileInputStream(src)
            copyStream(inputStream, dest)
        } catch (e: FileNotFoundException) {
        }
    }

    fun copyStream(inputStream: InputStream?, f: File?) {
        var fileOutputStream: FileOutputStream? = null
        try {
            val data = ByteArray(1024)
            var len = 0
            fileOutputStream = FileOutputStream(f)
            while (inputStream!!.read(data).also { len = it } != -1) {
                fileOutputStream.write(data, 0, len)
            }
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                }
            }
        }
    }

    fun getInputStream(path: String?): InputStream? {
        val url: URL
        try {
            url = URL(path)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 3000
            urlConnection.requestMethod = "GET"
            urlConnection.doInput = true // 表示从服务器获取数据
            urlConnection.connect()
            if (urlConnection.responseCode == 200) return urlConnection.inputStream
        } catch (e: MalformedURLException) {
        } catch (e: IOException) {
        } catch (e: Exception) {
        }
        return null
    }
}