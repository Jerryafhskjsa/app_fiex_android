package com.black.base.model

import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CRequest
import kotlin.collections.ArrayList

class C2CADData<T> {
    var data:  ArrayList<T>? = null
    var more: Boolean? = false
    var pageRequest: ArrayList<C2CRequest?>? = null
    var total = 0

}