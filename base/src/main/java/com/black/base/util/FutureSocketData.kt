package com.black.base.util

import com.black.base.model.future.MarkPriceBean
import com.black.base.model.future.TickerBean
import com.black.base.model.socket.PairStatusNew
import com.google.gson.Gson

object FutureSocketData {

    private var TAG = FutureSocketData::class.java.simpleName
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }

    var markPrice: MarkPriceBean? = null

    //所有现货行情交易对socket更新数据缓存
    var tickerList: MutableMap<String, TickerBean> = HashMap()

    var tickerBean: TickerBean? = null

    fun onMarkPriceChange(markPriceBean: MarkPriceBean?) {
        markPrice = markPriceBean
    }

    fun onTicketChange(tickerBean: TickerBean) {
        if (tickerList.contains(tickerBean.s)) {
            tickerList[tickerBean.s] = tickerBean
        } else {
            tickerList.put(tickerBean.s, tickerBean)
        }
    }

}