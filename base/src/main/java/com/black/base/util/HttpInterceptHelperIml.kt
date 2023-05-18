package com.black.base.util

import com.black.base.model.HttpRequestResultBase
import com.black.net.BlackHttpException
import com.black.net.HttpInterceptHelper
import com.black.net.HttpRequestResult
import com.google.gson.Gson
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException

class HttpInterceptHelperIml : HttpInterceptHelper {
    private val gson: Gson = Gson()
    @Throws(IOException::class)
    override fun intercept(builder: OkHttpClient.Builder, response: Response?) {
        if (response == null) {
            return
        }
        val responseBody = response.body() ?: return
        val contentLength = responseBody.contentLength()
        if (bodyEncoded(response.headers())) {
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()
            var charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = try {
                    contentType.charset(UTF8)
                } catch (e: UnsupportedCharsetException) {
                    return
                }
            }
            if (!isPlaintext(buffer)) {
                return
            }
            if (contentLength != 0L) {
                val resultString = buffer.clone().readString(charset)
                try {
                    val data = gson.fromJson(resultString, HttpRequestResultBase::class.java)
                    if (data.code == HttpRequestResult.ERROR_MISS_MONEY_PASSWORD_CODE) {
                        throw BlackHttpException(HttpRequestResult.ERROR_MISS_MONEY_PASSWORD_CODE, builder, response)
                    }
                } catch (e: Exception) {
                    if (e is BlackHttpException) {
                        throw e
                    }
                }
            }
        }
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
        fun isPlaintext(buffer: Buffer): Boolean {
            return try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                true
            } catch (e: EOFException) {
                false // Truncated UTF-8 sequence.
            }
        }
    }

}