package com.black.base.model.clutter

import java.util.*
import kotlin.collections.ArrayList

/**
 *  {"s": "BTC_USDT","k": [{"p": "22881.95","t": 1660100400000}]}
}
 */
class HomeTickersKline {
        var s: String? = null
        var k: ArrayList<Kdata>? = null
        inner class Kdata {
                var p:String? = null
                var t:String? = null
        }
}
