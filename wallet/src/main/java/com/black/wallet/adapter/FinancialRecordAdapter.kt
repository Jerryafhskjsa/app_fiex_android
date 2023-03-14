package com.black.wallet.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.FinancialRecord
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemFinancialRecordBinding
import skin.support.content.res.SkinCompatResources
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FinancialRecordAdapter(context: Context, variableId: Int, data: ArrayList<FinancialRecord?>?) : BaseRecycleDataBindAdapter<FinancialRecord?, ListItemFinancialRecordBinding>(context, variableId, data) {
    private var onHandleClickListener: OnHandleClickListener? = null
    private var c1 = 0
    private var t5: Int = 0
    private var isInto = false

    init {
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_financial_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemFinancialRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder: ListItemFinancialRecordBinding? = holder.dataBing
        val isFailed = record?.txStatus == null || "-1".equals(record.txStatus, ignoreCase = true) || "2".equals(record.txStatus, ignoreCase = true)
//        boolean isInto = record.txType != null && record.txType == 1;
        //        boolean isInto = record.txType != null && record.txType == 1;
        val color = if (isFailed) t5 else if (isInto) colorWin else colorLost
        val type = if (isInto) getString(R.string.wallet_bill_deposit) else getString(R.string.filter_financial_out)
        if (TextUtils.equals("1", record?.txStatus) && !isInto) {
            viewHolder?.revoke?.visibility = View.VISIBLE
            viewHolder?.revoke?.setOnClickListener {
                if (onHandleClickListener != null) {
                    onHandleClickListener!!.onHandleClick(record)
                }
            }
            viewHolder?.revokeLayout?.visibility = View.VISIBLE
        } else {
            viewHolder?.revoke?.visibility = View.GONE
            viewHolder?.revoke?.setOnClickListener(null)
            viewHolder?.revokeLayout?.visibility = View.GONE
        }
        viewHolder?.coinType?.setText(String.format("%s %s", type, if (record?.txCoin == null) "" else record.txCoin))
        viewHolder?.coinType?.setTextColor(color)
        viewHolder?.count?.setText(String.format("%s%s", if (isInto) "+" else "-", NumberUtil.formatNumberNoGroup(record?.txAmount)))
        viewHolder?.count?.setTextColor(color)
        ////0已完成 1待审核 2已取消 3确认中 4审核通过 5转账中 -1 失败
        viewHolder?.status?.setText(record?.getStatusText(context))
        viewHolder?.status?.setTextColor(color)
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val date = df.parse(record?.createdTime)
        val df1 = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK)
        val date1 = df1.parse(date.toString())
        val df2: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        viewHolder?.time?.setText(df2.format(date1))
//        holder.timeView.setTextColor(normalColor);
        //        holder.timeView.setTextColor(normalColor);
        viewHolder?.confirmAmount?.setText(if (record?.txFee == null) nullAmount else NumberUtil.formatNumberNoGroup(record.txFee))
        viewHolder?.areaInfo?.setText(if (record?.txCoin == null) nullAmount else record.txCoin)
    }

    fun setInto(into: Boolean) {
        isInto = into
    }

    fun setOnHandleClickListener(onHandleClickListener: OnHandleClickListener?) {
        this.onHandleClickListener = onHandleClickListener
    }

    interface OnHandleClickListener {
        fun onHandleClick(financialRecord: FinancialRecord?)
    }
}