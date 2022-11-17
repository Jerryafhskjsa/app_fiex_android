package com.black.base.model

class HttpRequestResultBean<T : Any?> : HttpRequestResultBase() {
    var msgInfo: String? = null
    var returnCode: Int? = null
    var result: T? = null

}
