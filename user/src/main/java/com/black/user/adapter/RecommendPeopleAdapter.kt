package com.black.user.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.user.RecommendPeopleDetail
import com.black.user.R
import com.black.user.databinding.ListItemRecommendPeopleBinding
import com.black.util.CommonUtil

class RecommendPeopleAdapter(context: Context, variableId: Int, data: ArrayList<RecommendPeopleDetail?>?) : BaseRecycleDataBindAdapter<RecommendPeopleDetail?, ListItemRecommendPeopleBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_recommend_people
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemRecommendPeopleBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val viewHolder: ListItemRecommendPeopleBinding? = holder.dataBing
        val detail = getItem(position)
        viewHolder?.account?.setText(if (detail?.username == null) "" else detail.username)
        viewHolder?.date?.setText(if (detail?.createTime == null) "" else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", detail.createTime!!))
        viewHolder?.level?.setText(detail?.getVerifyDisplay(context))
    }
}