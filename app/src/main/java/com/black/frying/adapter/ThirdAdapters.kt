package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.payOrder
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.DepositListBinding
import skin.support.content.res.SkinCompatResources

class ThirdAdapters (context: Context, variableId: Int, data: ArrayList<payOrder?>?) : BaseRecycleDataBindAdapter<payOrder?, DepositListBinding>(context, variableId, data) {
    private var c1 = 0
    private var t5 = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.deposit_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<DepositListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val walletBill = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.action?.setText(if (walletBill?.orderType == "B") getString(R.string.buy_02) else getString(R.string.sell))
        viewHolder?.action?.setTextColor(if (walletBill?.orderType == "B") c1 else t5)
        viewHolder?.direction?.setText(if (walletBill?.payStatus == -1) "Failed" else if (walletBill?.payStatus == -2) "Audit failed" else if (walletBill?.payStatus == 0) "Success" else if (walletBill?.payStatus == 2) "Confirming" else if (walletBill?.payStatus == 3) "Status wait pass" else if (walletBill?.payStatus == 4) "Status Pass"  else "Submit")
        viewHolder?.amount?.setText(if (walletBill?.orderType == "B") walletBill.amount.toString() + walletBill.coin else walletBill?.amount.toString() + walletBill?.ccyNo)
        viewHolder?.accountType?.setText(if (walletBill?.orderType == "B") walletBill.orderAmount.toString() + walletBill.ccyNo else walletBill?.orderAmount.toString() + walletBill?.coin)
        viewHolder?.id?.setText(walletBill?.id)
        viewHolder?.date?.setText(if(walletBill?.createTime == null) null else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", walletBill.createTime!!))
    }

}