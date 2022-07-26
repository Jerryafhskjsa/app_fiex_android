package com.black.wallet.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.adapter.interfaces.OnSwipeItemClickListener
import com.black.base.model.wallet.WalletWithdrawAddress
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletWithdrawAddressBinding

class WalletAddressAdapter(context: Context, variableId: Int, data: ArrayList<WalletWithdrawAddress?>?) : BaseRecycleDataBindAdapter<WalletWithdrawAddress?, ListItemWalletWithdrawAddressBinding>(context, variableId, data) {
    private var onSwipeItemClickListener: OnSwipeItemClickListener? = null

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemWalletWithdrawAddressBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val viewHolder: ListItemWalletWithdrawAddressBinding? = holder.dataBing
        val address: WalletWithdrawAddress? = getItem(position)
        viewHolder?.contentLayout?.name?.setText(if (address?.name == null) "" else address.name)
        viewHolder?.contentLayout?.address?.setText(if (address?.coinWallet == null) "" else address.coinWallet)

        viewHolder?.contentLayout?.root?.setOnClickListener {
            if (onSwipeItemClickListener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    onSwipeItemClickListener?.onItemClick(recyclerView, it, position, address)
                }
            }
        }
        viewHolder?.deleteLayout?.delete?.setOnClickListener {
            if (onSwipeItemClickListener != null) {
                onSwipeItemClickListener?.deleteClick(position)
            }
        }
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_wallet_withdraw_address
    }

    fun setOnSwipeItemClickListener(onSwipeItemClickListener: OnSwipeItemClickListener) {
        super.setOnItemClickListener(onSwipeItemClickListener)
        this.onSwipeItemClickListener = onSwipeItemClickListener
    }
}