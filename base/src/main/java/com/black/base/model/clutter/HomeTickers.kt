package com.black.base.model.clutter

import java.util.*

/**
 * [{"t":1659882582193,"s":"ETH_USDT","c":"0","h":"0","l":"0","a":"0","v":"0","o":"0","r":"0"}]
 */
class HomeTickers {
        var t: String? = null//时间戳
        var s: String? = null//交易对
        var c: String? = null//cloes 收盘价[当前价格]
        var h: String? = null//high 最高价
        var l: String? = null//low 最低价
        var a: String? = null//amount 成交量
        var v: String? = null//volume 成交额
        var o: String? = null// open 开盘价
        var r: String? = null//rate 涨跌幅
}
