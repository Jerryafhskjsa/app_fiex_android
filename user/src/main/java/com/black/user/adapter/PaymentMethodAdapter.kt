package com.black.user.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.model.user.PaymentMethod
import com.black.user.R
import com.black.user.databinding.ListItemPaymentMethodBinding

class PaymentMethodAdapter(context: Context, variableId: Int, data: ArrayList<PaymentMethod?>?) : BaseRecycleDataBindAdapter<PaymentMethod?, ListItemPaymentMethodBinding>(context, variableId, data) {
    private var onMethodItemClickListener: OnMethodItemClickListener? = null
    override fun getResourceId(): Int {
        return R.layout.list_item_payment_method
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPaymentMethodBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val paymentMethod = getItem(position)
        val viewHolder = holder.dataBing
        paymentMethod?.payIconRes?.let {
            viewHolder?.typeIcon?.setImageResource(paymentMethod.payIconRes)
        }
        val type = if (TextUtils.equals(PaymentMethod.BANK, paymentMethod?.type)) paymentMethod?.bankName else paymentMethod?.getPayTypeText(context)
        viewHolder?.typeName?.setText(type ?: nullAmount)
        if (paymentMethod?.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE) {
            viewHolder?.isActive?.isChecked = true
            viewHolder?.isActive?.text = "已激活"
        } else {
            viewHolder?.isActive?.isChecked = false
            viewHolder?.isActive?.text = "未激活"
        }
        viewHolder?.isActive?.setOnClickListener {
            onMethodItemClickListener?.onStatusUpdate(paymentMethod)
        }
        if (TextUtils.equals(PaymentMethod.BANK, paymentMethod?.type)) {
            viewHolder?.batchBankName?.text = if (paymentMethod?.branchBankName == null) nullAmount else paymentMethod.branchBankName
            viewHolder?.batchBankName?.visibility = View.VISIBLE
        } else {
            viewHolder?.batchBankName?.visibility = View.GONE
        }
        viewHolder?.name?.text = if (paymentMethod?.payeeName == null) nullAmount else paymentMethod.payeeName
        viewHolder?.cardNo?.setText(if (paymentMethod?.account == null) nullAmount else paymentMethod.account)
        viewHolder?.qrcode?.visibility = if (TextUtils.isEmpty(paymentMethod?.url)) View.GONE else View.VISIBLE
        viewHolder?.qrcode?.setOnClickListener {
            onMethodItemClickListener?.onQRCodeClick(paymentMethod)
        }
        viewHolder?.itemDelete?.setOnClickListener {
            onMethodItemClickListener?.deleteClick(paymentMethod)
        }
        viewHolder?.contentLayout?.setOnClickListener {
            onMethodItemClickListener?.onItemClick(recyclerView, holder.itemView, position, paymentMethod)
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnMethodItemClickListener?) {
        super.setOnItemClickListener(onItemClickListener)
        this.onMethodItemClickListener = onItemClickListener
    }

    interface OnMethodItemClickListener : OnItemClickListener {
        fun deleteClick(paymentMethod: PaymentMethod?)
        fun onQRCodeClick(paymentMethod: PaymentMethod?)
        fun onStatusUpdate(paymentMethod: PaymentMethod?)
    }
}