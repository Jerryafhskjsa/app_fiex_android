package com.black.base.model.trade

import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex

class TradeOrderResult {
    var items: ArrayList<TradeOrderFiex?>? = null
    var page: Int? = null
    var ps: Int? = null
    var total:Int? = null
}
