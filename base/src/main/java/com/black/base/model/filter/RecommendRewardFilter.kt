package com.black.base.model.filter

import android.content.Context
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class RecommendRewardFilter(var code: String?, var text: String) {
    override fun toString(): String {
        return text
    }

    companion object {
        //1:合伙人佣金奖励,2:活期宝,3:存币生息)
//2, "活期宝";3, "存币生息" 1, "合伙人佣金奖励";后台默认1
        const val KEY = "RecommendRewardFilter"
        lateinit var ALL: RecommendRewardFilter
        //    public static RecommendRewardFilter LEVEL_01;
        lateinit var LEVEL_02: RecommendRewardFilter
        lateinit var LEVEL_03: RecommendRewardFilter
        fun init(context: Context) {
            ALL = RecommendRewardFilter(null, context.getString(R.string.total))
            //        LEVEL_01 = new RecommendRewardFilter("1", context.getString(R.string.filter_recommend_reward_level_01));
            LEVEL_02 = RecommendRewardFilter("2", context.getString(R.string.filter_recommend_reward_level_02))
            LEVEL_03 = RecommendRewardFilter("3", context.getString(R.string.filter_recommend_reward_level_03))
        }

        fun getDefaultFilterEntity(selectItem: RecommendRewardFilter?): FilterEntity<RecommendRewardFilter> {
            val entity: FilterEntity<RecommendRewardFilter> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<RecommendRewardFilter>()
            //        data.add(ALL);
            data.add(LEVEL_02)
            data.add(LEVEL_03)
            //        data.add(LEVEL_01);
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }

}