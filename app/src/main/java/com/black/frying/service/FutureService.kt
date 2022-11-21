package com.black.frying.service

import android.content.Context
import android.util.Log
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.DepthBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.future.SymbolBean
import com.black.frying.model.OrderItem
import com.black.util.Callback
import io.reactivex.Observable
import java.math.BigDecimal

object FutureService {

    var symbolList: ArrayList<SymbolBean>? = null
    var buyList: ArrayList<OrderItem>? = null
    var sellList: ArrayList<OrderItem>? = null

    var markPriceBeanList: ArrayList<MarkPriceBean>? = null
    var markPrice: MarkPriceBean? = null

    fun getSymbolList(context: Context?) {
        FutureApiServiceHelper.getSymbolList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<SymbolBean>?>?>() {
                override fun error(type: Int, error: Any?) {

                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<SymbolBean>?>?) {
                    if (returnData != null) {
                        symbolList = returnData.result
                    }
                }
            })
    }

    fun initSymbol(context: Context?) {
        if (symbolList == null) {
            getSymbolList(context);
        }
    }

    fun initMarkPrice(context: Context?) {
        FutureApiServiceHelper.getMarkPrice(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    TODO("Not yet implemented")
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<MarkPriceBean>?>?) {
                    if (returnData != null) {
                        markPriceBeanList = returnData.result
                    }
                }
            })
    }

    fun getMarkPrice(symbol: String) {
        for (markPriceBean in markPriceBeanList!!) {
            if (markPriceBean.s.equals(symbol)) {
                markPrice = markPriceBean
                break
            }
        }
    }

    /**
     * 获取交易对的合约面值
     */
    fun getSymbolValue(symbol: String): Double? {
        var contractSize: Double? = null
        for (symbolItem in symbolList!!) {
            if (symbolItem.symbol.equals(symbol)) {
                contractSize = symbolItem.contractSize.toDouble()
                break
            }
        }
        return contractSize
    }

    fun getDepthOrder(context: Context?, symbol: String) {
        var contractSize = getSymbolValue(symbol)
        getMarkPrice(symbol);
        Log.d("ttttttt-->contractSize", contractSize.toString());
//        Observable.zip()
        FutureApiServiceHelper.getDepthData(context, symbol, 30, false,
            object : Callback<HttpRequestResultBean<DepthBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<DepthBean?>?) {
                    if (returnData != null) {
                        buyList = ArrayList()
                        sellList = ArrayList()
                        val a = returnData.result?.a
                        val b = returnData.result?.b

                        if (a != null) {
                            var t = 0.0;
                            for (buy in a) {
                                var price = buy?.get(0)
                                var count = buy?.get(1)
                                var quantity =
                                    BigDecimal(count).multiply(BigDecimal(contractSize.toString()))
                                        .multiply(BigDecimal(markPrice?.p))
                                t = t.toBigDecimal().add(quantity).toDouble()
                                var orderBean =
                                    price?.let { OrderItem(it.toDouble(), quantity.toDouble(), t) }
                                if (orderBean != null) {
                                    buyList!!.add(orderBean)
                                }
                            }
                        }
                        if (b != null) {
                            var t = 0.0;
                            for (sell in b) {
                                var price = sell?.get(0)
                                var count = sell?.get(1)
                                var quantity =
                                    BigDecimal(count).multiply(BigDecimal(contractSize.toString()))
                                        .multiply(BigDecimal(markPrice?.p))
                                t = t.toBigDecimal().add(quantity).toDouble()
                                var orderBean =
                                    price?.let { OrderItem(it.toDouble(), quantity.toDouble(), t) }
                                if (orderBean != null) {
                                    sellList!!.add(orderBean)
                                }
                            }
                        }
                    }
                    Log.d("ttttttt-->contractSize", buyList.toString());
                    Log.d("ttttttt-->contractSize", sellList.toString());
                }
            })
    }


}