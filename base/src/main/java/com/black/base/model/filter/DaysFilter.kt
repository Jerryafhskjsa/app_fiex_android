package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class DaysFilter(var code: String?, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        const val KEY = "DaysFilter"
        lateinit var ALL: DaysFilter
        lateinit var DAYS_30: DaysFilter
        lateinit var DAYS_60: DaysFilter
        lateinit var DAYS_90: DaysFilter
        lateinit var DAYS_180: DaysFilter

        @JvmStatic
        fun init(context: Context) {
            ALL = DaysFilter(null, context.getString(R.string.total))
            DAYS_30 = DaysFilter("30", context.getString(R.string.filter_day_30))
            DAYS_60 = DaysFilter("60", context.getString(R.string.filter_day_60))
            DAYS_90 = DaysFilter("90", context.getString(R.string.filter_day_90))
            DAYS_180 = DaysFilter("180", context.getString(R.string.filter_day_180))
        }

        fun getDefaultFilterEntity(selectItem: DaysFilter?): FilterEntity<DaysFilter> {
            val entity: FilterEntity<DaysFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<DaysFilter>()
            data.add(ALL)
            data.add(DAYS_30)
            data.add(DAYS_60)
            data.add(DAYS_90)
            data.add(DAYS_180)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}