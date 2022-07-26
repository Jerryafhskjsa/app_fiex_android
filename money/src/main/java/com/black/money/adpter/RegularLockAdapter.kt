package com.black.money.adpter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.RegularLock
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.R
import com.black.money.databinding.ListItemRegularLockBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class RegularLockAdapter(context: Context, variableId: Int, data: ArrayList<RegularLock?>?) : BaseRecycleDataBindAdapter<RegularLock?, ListItemRegularLockBinding>(context, variableId, data) {
    private var colorStatusIn = 0
    private var colorStatusEnd: Int = 0
    private var bgStatusIn: Drawable? = null
    private var bgStatusEnd: Drawable? = null
    private var onRegularChangeOutListener: OnRegularChangeOutListener? = null
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
        return R.layout.list_item_regular_lock
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemRegularLockBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val regularLock = getItem(position)
        val viewHolder = holder.dataBing
        val statusInt = if (regularLock?.status == null) 0 else regularLock.status
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, regularLock?.coinType)
        viewHolder?.coinType?.setText(if (regularLock?.coinType == null) nullAmount else regularLock.coinType)
        viewHolder?.rate?.setText(if (regularLock?.annualrate == null) nullAmount else regularLock.annualrate)
        viewHolder?.status?.setText(regularLock?.getStatusText(context) ?: nullAmount)
        if (RegularLock.isEnd(regularLock)) {
            viewHolder?.status?.background = bgStatusEnd
            viewHolder?.status?.setTextColor(colorStatusEnd)
        } else {
            viewHolder?.status?.background = bgStatusIn
            viewHolder?.status?.setTextColor(colorStatusIn)
        }
        viewHolder?.totalAmount?.setText(if (regularLock?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regularLock.amount, 9, 2, 8))
        viewHolder?.rewardTitle?.setText(if (RegularLock.isEnd(regularLock)) "收益" else "预计收益")
        viewHolder?.reward?.setText(if (regularLock?.interest == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regularLock.interest, 9, 2, 8))
        if (statusInt == 4) {
            viewHolder?.breakLayout?.visibility = View.VISIBLE
            viewHolder?.breakAmount?.setText(if (regularLock?.defaultAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regularLock.defaultAmount, 9, 2, 8))
        } else {
            viewHolder?.breakLayout?.visibility = View.GONE
            viewHolder?.breakAmount?.setText(nullAmount)
        }
        viewHolder?.lockDate?.setText(if (regularLock?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd", regularLock.createdTime!!))
        viewHolder?.lockDay?.setText(if (regularLock?.days == null) nullAmount else regularLock.days)
        viewHolder?.unlockDateTitle?.setText(if (statusInt == 4) "取出日" else if (statusInt == 5) "取出日" else "到期日")
        if (RegularLock.isEnd(regularLock)) {
            viewHolder?.unlockDate?.setText(if (regularLock?.endTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd", regularLock.endTime!!))
            viewHolder?.btnChangeOut?.visibility = View.GONE
            viewHolder?.bottomLine?.visibility = View.GONE
        } else {
            viewHolder?.unlockDate?.setText(if (regularLock?.endTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd", regularLock.endTime!!))
            viewHolder?.btnChangeOut?.visibility = View.VISIBLE
            viewHolder?.bottomLine?.visibility = View.VISIBLE
        }
        viewHolder?.hint?.setText(String.format("违约费率 %s%%", if (regularLock?.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regularLock.defaultRate!! * 100, 2)))
        viewHolder?.hint?.visibility = if (statusInt == 5) View.GONE else View.VISIBLE
        viewHolder?.btnChangeOut?.setOnClickListener {
            regularLock?.let {
                onRegularChangeOutListener?.onRegularChangeOut(regularLock)
            }
        }
    }

    fun setOnRegularChangeOutListener(onRegularChangeOutListener: OnRegularChangeOutListener?) {
        this.onRegularChangeOutListener = onRegularChangeOutListener
    }

    interface OnRegularChangeOutListener {
        fun onRegularChangeOut(regularLock: RegularLock)
    }
}