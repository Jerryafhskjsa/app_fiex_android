package com.black.base.model.socket


class KLineData {
    var s // ok 成功弄，no_data 失败 无数据
            : String? = null
    var t: LongArray? = null
    var c: DoubleArray? = null
    var o: DoubleArray? = null
    var h: DoubleArray? = null
    var l: DoubleArray? = null
    var v: LongArray? = null
    //    t: times
    val isOk: Boolean
        get() = "ok".equals(s, ignoreCase = true)
//    c:close
//    o:open
//    h:high
//    l：low
//    v：交易量
}