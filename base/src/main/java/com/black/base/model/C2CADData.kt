package com.black.base.model

import kotlin.collections.ArrayList

class C2CADData<T> {
    var data:  ArrayList<T>? = null
    var pageRequest: ArrayList<T>? = null
    var total = 0
    var more: Boolean? = null

}