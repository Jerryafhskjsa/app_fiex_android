package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class RecommendPeopleFilter(var code: String, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        const val KEY = "RecommendPeopleFilter"
        //    public final static RecommendPeopleFilter ALL = new RecommendPeopleFilter(null, "全部");
        lateinit var LEVEL_01: RecommendPeopleFilter
        lateinit   var LEVEL_02: RecommendPeopleFilter
        fun init(context: Context) {
            LEVEL_01 = RecommendPeopleFilter("1", context.getString(R.string.filter_recommend_level_01))
            LEVEL_02 = RecommendPeopleFilter("2", context.getString(R.string.filter_recommend_level_02))
        }

        fun getDefaultFilterEntity(selectItem: RecommendPeopleFilter?): FilterEntity<RecommendPeopleFilter> {
            val entity: FilterEntity<RecommendPeopleFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<RecommendPeopleFilter>()
            //        data.add(ALL);
            data.add(LEVEL_01)
            data.add(LEVEL_02)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}