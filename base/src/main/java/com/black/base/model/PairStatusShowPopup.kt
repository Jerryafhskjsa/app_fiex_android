package com.black.base.model

import android.content.Context
import com.black.base.R
import com.black.base.model.socket.PairStatus
import java.util.*

//交易对，弹窗显示
class PairStatusShowPopup(context: Context, pairStatus: PairStatus) : PairStatus() {
    init {
        pairName = pairStatus.pairName
        pair = pairStatus.pair
        currentPrice = pairStatus.currentPrice
        setCurrentPriceCNY(pairStatus.currentPriceCNY, context.getString(R.string.number_default))
        firstPriceToday = pairStatus.firstPriceToday
        lastPrice = pairStatus.lastPrice
        maxPrice = pairStatus.maxPrice
        minPrice = pairStatus.minPrice
        totalAmount = pairStatus.totalAmount
        priceChangeSinceToday = pairStatus.priceChangeSinceToday
        statDate = pairStatus.statDate
        order_no = pairStatus.order_no
        supportingPrecisionList = pairStatus.supportingPrecisionList
        is_dear = pairStatus.is_dear
        leverConfEntity = pairStatus.leverConfEntity
    }

    fun updateData(pairStatus: PairStatus) {
        currentPrice = pairStatus.currentPrice
        priceChangeSinceToday = pairStatus.priceChangeSinceToday
        order_no = pairStatus.order_no
        supportingPrecisionList = pairStatus.supportingPrecisionList
        is_dear = pairStatus.is_dear
    }

    companion object {
        var COMPARATOR = Comparator<PairStatusShowPopup?> { o1, o2 -> if (o1?.order_no == null || o2?.order_no == null) 0 else o1.order_no - o2.order_no }
    }
}
