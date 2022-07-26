package com.black.money.adpter

import android.content.Context
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.PromotionsBuy
import com.black.base.util.ImageLoader
import com.black.base.util.UrlConfig
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsBuyBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class PromotionsBuyListAdapter(context: Context, variableId: Int, data: ArrayList<PromotionsBuy?>?) : BaseRecycleDataBindAdapter<PromotionsBuy?, ListItemPromotionsBuyBinding>(context, variableId, data) {
    companion object {
        const val STATUS = "status"
    }

    private var c1 = 0
    private var c2: Int = 0
    private var c5: Int = 0
    private var t5: Int = 0
    private var imageLoader: ImageLoader? = null
    private var onPromotionsBuyListener: OnPromotionsBuyListener? = null
    private var loseTime: Long = 0

    init {
        imageLoader = ImageLoader(context)
    }

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        c2 = SkinCompatResources.getColor(context, R.color.C2)
        c5 = SkinCompatResources.getColor(context, R.color.C5)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_promotions_buy
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsBuyBinding>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val payload = payloads[0].toString()
            if (STATUS == payload) {
                val promotions = getItem(position)
                val viewHolder = holder.dataBing
                imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + promotions?.listImageUrl)
                val status = promotions?.statusCode
                val color = if (status == 1) t5 else if (status == 2) c1 else if (status == 3) c2 else c5
                viewHolder?.status?.setBackgroundColor(color)
                viewHolder?.status?.text = promotions?.getStatusDisplay(context)
                val startTime = promotions?.startTime ?: 0
                val endTime = promotions?.endTime ?: 0
                val thisTime = (promotions?.systemTime ?: 0) + getLoseTime()
                when (status) {
                    1 -> {
                        viewHolder?.btnBuy?.isEnabled = true
                        viewHolder?.btnBuy?.text = getString(R.string.buy_immediately, getTimeDisplay(endTime, thisTime))
                        viewHolder?.btnBuy?.visibility = View.VISIBLE
                    }
                    2 -> {
                        viewHolder?.btnBuy?.isEnabled = false
                        viewHolder?.btnBuy?.text = promotions.getStatusDisplay(context)
                        viewHolder?.btnBuy?.visibility = View.VISIBLE
                    }
                    3 -> {
                        viewHolder?.btnBuy?.isEnabled = false
                        viewHolder?.btnBuy?.text = promotions.getStatusDisplay(context)
                        viewHolder?.btnBuy?.visibility = View.VISIBLE
                    }
                    else -> {
                        viewHolder?.btnBuy?.isEnabled = false
                        viewHolder?.btnBuy?.text = getString(R.string.start_wait, getTimeDisplay(startTime, thisTime))
                        viewHolder?.btnBuy?.visibility = View.VISIBLE
                    }
                }
            } else {
                onBindViewHolder(holder, position)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsBuyBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val promotions = getItem(position)
        val viewHolder = holder.dataBing
        imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + promotions?.listImageUrl)
        val status = promotions?.statusCode
        val color = if (status == 1) t5 else if (status == 2) c1 else if (status == 3) c2 else c5
        viewHolder?.status?.setBackgroundColor(color)
        viewHolder?.status?.text = promotions?.getStatusDisplay(context)
        viewHolder?.project?.text = if (promotions?.coinType == null) "" else promotions.coinType
        val amount = (NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.nowAmount == null) 0 else promotions.nowAmount, 6, 2, 2)
                + "/"
                + NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.totalAmount == null) 0 else promotions.totalAmount, 6, 2, 2))
        viewHolder?.amount?.text = amount
        viewHolder?.price?.text = promotions?.priceDisplay
        viewHolder?.startDate?.text = if (promotions?.startTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", promotions.startTime!!)
        viewHolder?.endDate?.text = if (promotions?.endTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", promotions.endTime!!)
        viewHolder?.btnBuy?.setOnClickListener {
            onPromotionsBuyListener?.onBuy(promotions)
        }
        val startTime = promotions?.startTime ?: 0
        val endTime = promotions?.endTime ?: 0
        val thisTime = (promotions?.systemTime ?: 0) + getLoseTime()
        when (status) {
            1 -> {
                viewHolder?.btnBuy?.isEnabled = true
                viewHolder?.btnBuy?.text = getString(R.string.buy_immediately, getTimeDisplay(endTime, thisTime))
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            2 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.text = promotions.getStatusDisplay(context)
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            3 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.text = promotions.getStatusDisplay(context)
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            else -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.text = getString(R.string.start_wait, getTimeDisplay(startTime, thisTime))
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
        }
    }

    fun setOnPromotionsBuyListener(onPromotionsBuyListener: OnPromotionsBuyListener?) {
        this.onPromotionsBuyListener = onPromotionsBuyListener
    }

    private fun getTimeDisplay(finishTime: Long, thisTime: Long): String? {
        var result = "00:00:00"
        var rangeTime = finishTime - thisTime
        if (rangeTime <= 0) {
            return "00:00:00"
        }
        if (rangeTime > 1000) {
            rangeTime /= 1000
            val d = (rangeTime / 24 / 3600).toInt()
            rangeTime %= (24 * 3600)
            val sb = StringBuilder()
            if (d > 0) {
                sb.append(d).append(" ")
            }
            val h = (rangeTime / 3600).toInt()
            rangeTime %= 3600
            val m = (rangeTime / 60).toInt()
            rangeTime %= 60
            val s = rangeTime.toInt()
            sb.append(CommonUtil.twoBit(h)).append(":").append(CommonUtil.twoBit(m)).append(":").append(CommonUtil.twoBit(s))
            result = sb.toString()
        }
        return result
    }

    fun setLoseTime(loseTime: Long) {
        this.loseTime = loseTime
    }

    fun getLoseTime(): Long {
        return loseTime
    }

    interface OnPromotionsBuyListener {
        fun onBuy(promotions: PromotionsBuy?)
    }
}
