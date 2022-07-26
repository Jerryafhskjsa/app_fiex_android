package com.black.im.util

import com.tencent.imsdk.log.QLog

object TUIKitLog : QLog() {
    private const val PRE = "TUIKit-"

    private fun mixTag(tag: String?): String {
        return PRE + tag
    }

    /**
     * 打印INFO级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    internal fun v(strTag: String?, strInfo: String?) {
        QLog.v(mixTag(strTag), strInfo)
    }

    /**
     * 打印DEBUG级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    internal fun d(strTag: String, strInfo: String?) {
        QLog.d(mixTag(strTag), strInfo)
    }

    /**
     * 打印INFO级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    internal fun i(strTag: String, strInfo: String?) {
        QLog.i(mixTag(strTag), strInfo)
    }

    /**
     * 打印WARN级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    internal fun w(strTag: String, strInfo: String?) {
        QLog.w(mixTag(strTag), strInfo)
    }

    /**
     * 打印WARN级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    fun w(strTag: String, strInfo: String, e: Throwable) {
        QLog.w(mixTag(strTag), strInfo + e.message)
    }

    /**
     * 打印ERROR级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    internal fun e(strTag: String, strInfo: String?) {
        QLog.e(mixTag(strTag), strInfo)
    }
}