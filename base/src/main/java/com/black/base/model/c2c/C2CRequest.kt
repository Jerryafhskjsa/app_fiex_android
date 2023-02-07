package com.black.base.model.c2c

import com.black.base.model.BaseAdapterItem

class C2CRequest: BaseAdapterItem() {
    var asc: Boolean? = false
    var orderBy: String? = null
    var page: Int? = null
    var size:Int? = null
}