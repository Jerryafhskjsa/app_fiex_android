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

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemWalletWithdrawAddressBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val viewHolder: ListItemWalletWithdrawAddressBinding? = holder.dataBing
        val address: WalletWithdrawAddress? = getItem(position)
        viewHolder?.addrContent?.setText(if (address?.name == null) "" else address.name)
        viewHolder?.addrType?.setText(if (address?.coinWallet == null) "" else address.coinWallet)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_wallet_withdraw_address
    }
}