package com.black.base.model

import com.black.net.HttpRequestResult

open class HttpRequestResultBase : HttpRequestResult() {
    var timestamp: Long? = null
    var status: Long? = null
    var error: String? = null
    var message: String? = null
    var path: String? = null
    var code: Int? = null
    var msg: String? = null

    fun is403Error(): Boolean {
        return code == null && status == 403L
    }
    fun isCodeOk(): Boolean {
       return code == 0
    }
}
