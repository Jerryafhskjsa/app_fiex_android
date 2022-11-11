package com.black.base.model.trade

import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex

class TradeOrderHistoryResult {
    var items: ArrayList<TradeOrderFiex?>? = null
    var hasPrev: Boolean? = null
    var hasNext: Boolean? = null
}
