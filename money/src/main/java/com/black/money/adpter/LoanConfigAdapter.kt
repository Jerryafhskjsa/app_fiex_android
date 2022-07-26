package com.black.money.adpter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.LoanConfig
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.BR
import com.black.money.R
import com.black.money.databinding.ListItemLoanConfigBinding

class LoanConfigAdapter(context: Context, variableId: Int, data: ArrayList<LoanConfig?>?) : BaseRecycleDataBindAdapter<LoanConfig?, ListItemLoanConfigBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_loan_config
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLoanConfigBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val config = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, config?.coinType)
        viewHolder?.coinType?.setText(if (config?.coinType == null) nullAmount else config.coinType)
        if (viewHolder?.subRecyclerView?.adapter is LoanConfigSubAdapter) {
            val adapter = viewHolder?.subRecyclerView.adapter as LoanConfigSubAdapter
            adapter.data = config?.borrowCoinTypeList
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ListItemLoanConfigBinding> {
        val holder = super.onCreateViewHolder(parent, viewType)
        val viewHolder = holder.dataBing

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true

        viewHolder?.subRecyclerView?.layoutManager = layoutManager
        val adapter = LoanConfigSubAdapter(context, BR.listItemLoanConfigSubModel, null)

        viewHolder?.subRecyclerView?.adapter = adapter
        viewHolder?.subRecyclerView?.isNestedScrollingEnabled = false
        viewHolder?.subRecyclerView?.setHasFixedSize(true)
        viewHolder?.subRecyclerView?.isFocusable = false
        return holder
    }
}