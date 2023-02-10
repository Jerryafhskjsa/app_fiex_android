package com.black.base.model

import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CRequest
import kotlin.collections.ArrayList

class C2CADData<T> {
    var data:  ArrayList<T>? = null
    var more: Boolean? = false
    var pageRequest: Paging? = null
    var total = 0
    class Paging {
        var page = 0
        var size = 0
        var orderBy: String? = null
        var asc = false
    }
}