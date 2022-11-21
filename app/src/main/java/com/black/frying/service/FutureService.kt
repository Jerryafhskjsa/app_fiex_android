package com.black.frying.service

import android.content.Context
import android.util.Log
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.DepthBean
import com.black.base.model.future.SymbolBean
import com.black.frying.model.OrderItem
import com.black.util.Callback
import io.reactivex.Observable

object FutureService {

    var symbolList: ArrayList<SymbolBean>? = null
    val buyList: ArrayList<OrderItem>? = null
    val sellList: ArrayList<OrderItem>? = null

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
        Log.d("ttttttt-->contractSize", contractSize.toString());
//        Observable.zip()
        FutureApiServiceHelper.getDepthData(context, symbol, 30, false,
            object : Callback<HttpRequestResultBean<DepthBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->error", error.toString());
                }
                override fun callback(returnData: HttpRequestResultBean<DepthBean?>?) {
                    if (returnData != null) {
                        Log.d("ttttttt-->callback", returnData.result?.s + "---")
                        val a = returnData.result?.a
                        if (a != null) {
                            for (buy in a) {
                                Log.d("ttttttt-->callback", buy.toString())
                            }
                        }
                    };
                }
            })
    }


}