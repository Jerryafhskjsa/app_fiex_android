package com.black.base.model

import com.black.base.model.c2c.PayInfo

class HttpRequestResultString : HttpRequestResultBase() {
    var data: String? = null
    var list: ArrayList<PayInfo?>? = null
}
