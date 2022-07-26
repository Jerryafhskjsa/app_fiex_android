package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

//委托全部，买 卖
class DemandStatus(var code: String, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        //只能单选，默认2利息（0:存币,1:提币,2:利息）
        const val KEY = "DemandStatus"
        lateinit var INTO: DemandStatus
        lateinit var OUT: DemandStatus
        lateinit var REWARD: DemandStatus

        @JvmStatic
        fun init(context: Context) {
            INTO = DemandStatus("5", context.getString(R.string.filter_demand_into))
            OUT = DemandStatus("6", context.getString(R.string.filter_demand_out))
            REWARD = DemandStatus("3", context.getString(R.string.filter_demand_reward))
        }

        fun getDefaultFilterEntity(selectItem: DemandStatus?): FilterEntity<DemandStatus> {
            val entity: FilterEntity<DemandStatus> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<DemandStatus>()
            data.add(REWARD)
            data.add(INTO)
            data.add(OUT)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}