package com.black.base.model

import kotlin.collections.ArrayList

class PagingData<T>{
    var data: ArrayList<T>? = null
    var list: ArrayList<T>? = null
    var items:ArrayList<T>? = null
    var records:ArrayList<T>? = null
    var pageRequest: Paging? = null
    var totalCount = 0
    var total = 0
    var listTotal = 0
    var page = 0
    var pageSize = 0
    var ps = 0
    var more: Boolean? = null

    class Paging {
        var page = 0
        var size = 0
        var orderBy: String? = null
        var asc = false
    }
}