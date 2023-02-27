package com.black.c2c.adapter

import android.annotation.SuppressLint
import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2CBills
import com.black.c2c.R
import com.black.c2c.databinding.ListC2cBillsBinding
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class C2CBillsAdapter(context: Context, variableId: Int, data: ArrayList<C2CBills?>?) : BaseRecycleDataBindAdapter<C2CBills?, ListC2cBillsBinding>(context, variableId, data) {
    private var c1 = 0
    private var t5 = 0
    override fun getResourceId(): Int {
        return R.layout.list_c2c_bills
    }
    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cBillsBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CBills = getItem(position)
        val viewHolder = holder.dataBing
        if (c2CBills?.direction == "B") {
            viewHolder?.buy?.setText(getString(R.string.buy_02))
            viewHolder?.buy?.setTextColor(c1)
        }
        if (c2CBills?.direction == "S") {
            viewHolder?.buy?.setText(getString(R.string.sell))
            viewHolder?.buy?.setTextColor(t5)
        }
        val df: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val date = df.parse(c2CBills?.createTime )
        val df1 = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK)
        val date1 = df1.parse(date.toString())
        val df2: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        viewHolder?.time?.setText(df2.format(date1))
        viewHolder?.coinType?.setText(c2CBills?.coinType)
        viewHolder?.account?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price, 8, 2, 8)))
        viewHolder?.amount?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.amount, 8, 2, 8)))
        viewHolder?.money?.setText("￥" + String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price!! * c2CBills.amount!!, 8, 2, 8)))
        viewHolder?.status?.setText(c2CBills?.getStatusText(context))
        viewHolder?.billsNum?.setText(c2CBills?.id)
    }
}