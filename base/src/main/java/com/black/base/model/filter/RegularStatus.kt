package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

//委托全部，买 卖
class RegularStatus(var code: String?, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        const val KEY = "RegularStatus"
        lateinit var ALL: RegularStatus
        lateinit var NOT_START: RegularStatus
        lateinit var GETTING: RegularStatus
        lateinit var DOING: RegularStatus
        lateinit var END: RegularStatus
        fun init(context: Context) {
            ALL = RegularStatus(null, context.getString(R.string.total))
            NOT_START = RegularStatus("0", context.getString(R.string.filter_regular_not_start))
            GETTING = RegularStatus("1", context.getString(R.string.filter_regular_getting))
            DOING = RegularStatus("3", context.getString(R.string.filter_regular_doing))
            END = RegularStatus("2", context.getString(R.string.filter_regular_end))
        }

        fun getDefaultFilterEntity(selectItem: RegularStatus?): FilterEntity<RegularStatus> {
            val entity: FilterEntity<RegularStatus> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<RegularStatus>()
            data.add(ALL)
            data.add(NOT_START)
            data.add(GETTING)
            data.add(DOING)
            data.add(END)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}