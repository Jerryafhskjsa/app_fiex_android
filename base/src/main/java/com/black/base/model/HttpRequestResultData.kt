package com.black.base.model

class HttpRequestResultData<T : Any?> : HttpRequestResultBase() {
    var total_pages: Int? = null
    var c_page: Int? = null
    var data: T? = null
}
