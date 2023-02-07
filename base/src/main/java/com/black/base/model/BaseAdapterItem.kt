package com.black.base.model

import java.util.*

abstract class BaseAdapterItem {

    companion object {
        const val TYPE_DEFAULT = 0
        const val TYPE_QUOTATION = 1
        const val TYPE_BANNER = 2
        const val TYPE_TRANSACTION = 3
        const val SPOT_ACCOUNT = 4
        const val FINANCIAL_RECORD = 5
        const val C2C_DETAIL = 6
        const val BANK_CARD = 7
        const val COUNTRY_CODE = 8
        const val TRADE_ORDER = 9
        const val PAIR_STATUS = 10
        const val NOTICE = 11
        const val MINING_PROFIT_RECORD = 12
        const val MINING_PROFIT_DOUBLE_RECORD = 13
        const val C2C_ORDER = 14
        const val C2C_SELLER = 15
        const val C2C_AD = 16
        const val C2C_ORDER_ITEM_M_RIGHT = 0 //商家在右边
        const val C2C_ORDER_ITEM_C_LEFT = 1 //用户在左边
        const val C2C_ORDER_ITEM_M_LEFT = 2 //商家在左边
        const val C2C_ORDER_ITEM_C_RIGHT = 3 //用户在右边

        fun getTypeCount(itemList: MutableList<out BaseAdapterItem?>?): Int {
            val typeList = ArrayList<Int>()
            if (itemList != null && itemList.isNotEmpty()) {
                for (item in itemList) {
                    val type: Int = item?.getType() ?: TYPE_DEFAULT
                    if (!typeList.contains(type)) {
                        typeList.add(type)
                    }
                }
            }
            return typeList.size
        }
    }

    open fun getType(): Int {
        return TYPE_DEFAULT
    }
}