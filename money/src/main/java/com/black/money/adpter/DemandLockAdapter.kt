package com.black.money.adpter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.DemandLock
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.R
import com.black.money.databinding.ListItemDemandLockBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class DemandLockAdapter(context: Context, variableId: Int, data: ArrayList<DemandLock?>?) : BaseRecycleDataBindAdapter<DemandLock?, ListItemDemandLockBinding>(context, variableId, data) {
    private var onDemandChangeOutListener: OnDemandChangeOutListener? = null
    private var status = 0
    private var colorStatusIn = 0
    private var colorStatusEnd: Int = 0
    private var bgStatusIn: Drawable? = null
    private var bgStatusEnd: Drawable? = null
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun resetSkinResources() {
        super.resetSkinResources()
        colorStatusIn = SkinCompatResources.getColor(context, R.color.C1)
        colorStatusEnd = SkinCompatResources.getColor(context, R.color.T5)
        bgStatusIn = SkinCompatResources.getDrawable(context, R.drawable.bg_regular_status_in)
        bgStatusEnd = SkinCompatResources.getDrawable(context, R.drawable.bg_regular_status_end)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_demand_lock
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemDemandLockBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val demand = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, demand?.coinType)
        viewHolder?.coinType?.setText(if (demand?.coinType == null) nullAmount else demand.coinType)
        viewHolder?.rate?.setText(String.format("%s%%", if (demand?.currentRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demand.currentRate!! * 100, 2)))
        viewHolder?.status?.setText(if (demand == null) nullAmount else DemandLock.getStatusText(context, status))
        if (status == 2) {
            viewHolder?.status?.background = bgStatusEnd
            viewHolder?.status?.setTextColor(colorStatusEnd)
            viewHolder?.hint?.visibility = View.GONE
        } else {
            viewHolder?.status?.background = bgStatusIn
            viewHolder?.status?.setTextColor(colorStatusIn)
            viewHolder?.hint?.visibility = View.VISIBLE
        }

        viewHolder?.totalAmount?.setText(if (demand?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.amount, 9, 2, 8))
        viewHolder?.totalReward?.setText(if (demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.totalInterestAmount, 9, 2, 8))
        viewHolder?.rewardYesterday?.setText(if (demand?.lastInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.lastInterestAmount, 9, 2, 8))
        viewHolder?.rewardToday?.setText(if (demand?.nextInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.nextInterestAmount, 9, 2, 8))
        viewHolder?.lockDate?.setText(if (demand?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", demand.createTime!!))

        viewHolder?.totalRewardTitle?.setText(String.format("累计收益(%s)", if (demand?.distributionCoinType == null) nullAmount else demand.distributionCoinType))
        viewHolder?.rewardYesterdayTitle?.setText(String.format("昨日收益(%s)", if (demand?.distributionCoinType == null) nullAmount else demand.distributionCoinType))
        viewHolder?.rewardTodayTitle?.setText(String.format("今日收益(%s)", if (demand?.distributionCoinType == null) nullAmount else demand.distributionCoinType))
        if (demand?.remainingDay == null || demand.remainingDay == 0 || demand.nextRate == null || demand.nextRate == 0.0) {
            viewHolder?.hint?.visibility = View.GONE
            viewHolder?.hint?.setText("")
        } else {
            viewHolder?.hint?.visibility = View.VISIBLE
            viewHolder?.hint?.setText(String.format("%s天后利率调整为%s%%", if (demand.remainingDay == null) nullAmount else NumberUtil.formatNumberNoGroup(demand.remainingDay), if (demand.nextRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demand.nextRate!! * 100, 2)))
        }
        viewHolder?.btnChangeOut?.setOnClickListener {
            demand?.let {
                onDemandChangeOutListener?.onDemandChangeOut(demand)
            }
        }
        viewHolder?.btnChangeOut?.visibility = if (status == 1) View.VISIBLE else View.GONE
        viewHolder?.bottomLine?.visibility = if (status == 1) View.VISIBLE else View.GONE
    }

    fun setStatus(status: Int) {
        this.status = status
    }

    fun setOnDemandChangeOutListener(onDemandChangeOutListener: OnDemandChangeOutListener?) {
        this.onDemandChangeOutListener = onDemandChangeOutListener
    }

    interface OnDemandChangeOutListener {
        fun onDemandChangeOut(demandLock: DemandLock)
    }

}