package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import com.black.util.NumberUtil
import java.util.*

class DateFilter(rangeTime: Long?, text: String) {
    companion object {
        const val KEY = "DateFilter"
        lateinit var ALL: DateFilter
        lateinit var DAYS_3: DateFilter
        lateinit var DAYS_15: DateFilter
        lateinit var DAYS_30: DateFilter
        lateinit var DAYS_60: DateFilter

        @JvmStatic
        fun init(context: Context) {
            ALL = DateFilter(null, context.getString(R.string.total))
            DAYS_3 = DateFilter(3L * 24 * 3600 * 1000, context.getString(R.string.filter_date_3))
            DAYS_15 = DateFilter(15L * 24 * 3600 * 1000, context.getString(R.string.filter_date_15))
            DAYS_30 = DateFilter(30L * 24 * 3600 * 1000, context.getString(R.string.filter_date_30))
            DAYS_60 = DateFilter(60L * 24 * 3600 * 1000, context.getString(R.string.filter_date_60))
        }

        fun getDefaultFilterEntity(selectItem: DateFilter?): FilterEntity<DateFilter> {
            val entity: FilterEntity<DateFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<DateFilter>()
            data.add(ALL)
            data.add(DAYS_3)
            data.add(DAYS_15)
            data.add(DAYS_30)
            data.add(DAYS_60)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }

        fun getDefaultFilterEntity(selectItem: DateFilter?, addAll: Boolean): FilterEntity<DateFilter> {
            val entity: FilterEntity<DateFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<DateFilter>()
            if (addAll) {
                data.add(ALL)
            }
            data.add(DAYS_3)
            data.add(DAYS_15)
            data.add(DAYS_30)
            data.add(DAYS_60)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

    var startTime: String? = null
    var endTime: String? = null
    var text: String

    init {
        if (rangeTime == null) {
            endTime = null
            startTime = null
        } else {
            endTime = NumberUtil.formatNumberNoGroup(System.currentTimeMillis())
            startTime = NumberUtil.formatNumberNoGroup(System.currentTimeMillis() - rangeTime)
        }
        this.text = text
    }

    override fun toString(): String {
        return text
    }
}