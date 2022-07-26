package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

//委托全部，买 卖
class EntrustStatus(var code: Boolean, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        const val KEY = "EntrustStatus"
        lateinit var NEW: EntrustStatus
        lateinit  var HIS: EntrustStatus
        fun init(context: Context) {
            NEW = EntrustStatus(false, context.getString(R.string.filter_entrust_new))
            HIS = EntrustStatus(true, context.getString(R.string.filter_entrust_his))
        }

        fun getDefaultFilterEntity(selectItem: EntrustStatus?): FilterEntity<EntrustStatus> {
            val entity: FilterEntity<EntrustStatus> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<EntrustStatus>()
            data.add(NEW)
            data.add(HIS)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}