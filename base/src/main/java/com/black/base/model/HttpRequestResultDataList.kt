package com.black.base.model

import java.util.*

class HttpRequestResultDataList<T : Any?> : HttpRequestResultBase() {
    var total_pages = 0
    var c_page = 0
    var data: ArrayList<T>? = null
    var result: ArrayList<T>? = null

}