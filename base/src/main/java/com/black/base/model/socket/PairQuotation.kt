package com.black.base.model.socket

class PairQuotation {
    var s: String? = null//交易对
    var o: String? = null//open 开盘价
    var c: String? = null//cloes 收盘价
    var h: String? = null//high 最高价
    var l: String? = null//low 最低价
    var a: String? = null//amount 成交量
    var v:String? = null//volume 成交额
    var r:String? = null//change 涨跌幅
    var t:Long? = null//时间戳

    var ap:String? = null//卖一价格
    var bp:String? = null//买一价格
    var i:String? = null//指数价格
    var m:String? = null//标记价格
    override fun toString(): String {
        return "PairQuotation(s=$s, o=$o, c=$c, h=$h, l=$l, a=$a, v=$v, r=$r, t=$t, ap=$ap, bp=$bp, i=$i, m=$m)"
    }


}
