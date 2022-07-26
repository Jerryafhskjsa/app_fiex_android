package com.black.base.model.filter

import android.content.Context
import com.black.base.lib.filter.FilterEntity
import java.util.*

//委托全部，买 卖
class EntrustType(var code: String?, var text: String) {
    override fun toString(): String {
        return text
    }

    override fun equals(other: Any?): Boolean {
        if (other is EntrustType) {
            return code == null && other.code == null || code != null && other.code != null && code == other.code
        }
        return false
    }

    companion object {
        const val KEY = "EntrustType"
        lateinit var ALL: EntrustType
        lateinit var COIN: EntrustType
        lateinit var LEVER: EntrustType

        fun init(context: Context?) {
            ALL = EntrustType(null, "全部委托")
            COIN = EntrustType("PHYSICAL", "币币委托")
            LEVER = EntrustType("ISOLATED", "杠杆委托")
        }

        fun getDefaultFilterEntity(selectItem: EntrustType?): FilterEntity<EntrustType> {
            val entity: FilterEntity<EntrustType> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<EntrustType>()
            data.add(ALL)
            data.add(COIN)
            data.add(LEVER)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}