package com.black.base.model.filter

import android.content.Context
import android.text.TextUtils
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class CoinFilter(var code: String?, var text: String?) {
    override fun equals(other: Any?): Boolean {
        return if (other is CoinFilter) {
            TextUtils.equals(code, other.code)
        } else super.equals(other)
    }

    override fun toString(): String {
        return text ?: ""
    }

    companion object {
        const val KEY = "CoinFilter"
        var ALL: CoinFilter? = null
        @JvmStatic
        fun init(context: Context) {
            ALL = CoinFilter(null, context.getString(R.string.total))
        }

//    public static FilterEntity getDefaultFilterEntity() {
//        FilterEntity entity = new FilterEntity();
//        entity.key = KEY;
//        ArrayList<CoinFilter> data = new ArrayList<>();
//        data.add(ALL);
//        data.add(BTC);
//        data.add(ETH);
//        data.add(EOS);
//        data.add(USDT);
//        entity.data = data;
//        return entity;
//    }
        fun getDefaultFilterEntity(stringList: ArrayList<*>?, selectItem: CoinFilter?): FilterEntity<CoinFilter> {
            val entity: FilterEntity<CoinFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<CoinFilter>()
            //        data.add(ALL);
            if (stringList != null) {
                for (i in stringList.indices) {
                    val coinType = stringList[i]?.toString()
                    if (coinType != null) {
                        data.add(CoinFilter(coinType, coinType))
                    }
                }
            }
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}
