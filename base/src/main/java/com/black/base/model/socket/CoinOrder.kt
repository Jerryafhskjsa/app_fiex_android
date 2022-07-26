package com.black.base.model.socket

import android.text.TextUtils
import java.util.*

class CoinOrder : HashMap<String?, ArrayList<String?>?>() {
    private fun initOrderMap() {
        synchronized(coinOrderMap) {
            if (coinOrderMap.isEmpty()) { //目前支持0,1类型
                val list0 = get("0")
                val list0Size = list0?.size ?: 0
                for (i in 0 until list0Size) {
                    coinOrderMap[list0!![i]] = i
                }
                val list1 = get("1")
                val list1Size = list1?.size ?: 0
                for (i in 0 until list1Size) {
                    coinOrderMap[list1!![i]] = i + list0Size
                }
            }
        }
    }

    fun getCoinOrder(coinType: String?): Int? {
        synchronized(coinOrderMap) {
            if (TextUtils.isEmpty(coinType)) {
                return null
            }
            initOrderMap()
            return coinOrderMap[coinType]
        }
    }

    fun getOrder(pair: String?): Int {
        if (pair == null) {
            return MAX_ORDER
        }
        val arr = pair.split("_").toTypedArray()
        if (arr.size > 1) {
            val name = arr[0]
            val setName = arr[1]
            var order = 0
            //增加交易区的序号
            order += if ("USDT" == setName) {
                USDT_START_ORDER
            } else if ("ETH" == setName) {
                ETH_START_ORDER
            } else {
                return MAX_ORDER
            }
            val coinOrder = getCoinOrder(name)
            return if (coinOrder == null) {
                MAX_ORDER
            } else {
                order + coinOrder
            }
        }
        return MAX_ORDER
    }

    companion object {
        const val MAX_ORDER = 1000000000
        const val USDT_START_ORDER = 0
        const val ETH_START_ORDER = 1000
        private val coinOrderMap = HashMap<String?, Int>()
    }
}