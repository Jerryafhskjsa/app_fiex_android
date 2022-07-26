package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class FinancialType(var code: String?, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        const val KEY = "FinancialType"
        lateinit var ALL: FinancialType
        lateinit var INTO: FinancialType
        lateinit var OUT: FinancialType
        fun init(context: Context) {
            ALL = FinancialType(null, context.getString(R.string.total))
            INTO = FinancialType("1", context.getString(R.string.filter_financial_into))
            OUT = FinancialType("-1", context.getString(R.string.filter_financial_out))
        }

        fun getDefaultFilterEntity(selectItem: FinancialType?): FilterEntity<FinancialType> {
            val entity: FilterEntity<FinancialType> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<FinancialType>()
            data.add(ALL)
            data.add(INTO)
            data.add(OUT)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}