package com.black.money.adpter

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.PromotionsRush
import com.black.base.util.ImageLoader
import com.black.base.util.TimeUtil
import com.black.base.util.UrlConfig
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsRushBinding
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.NumberUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import skin.support.content.res.SkinCompatResources

class PromotionsListAdapter(context: Context, variableId: Int, data: ArrayList<PromotionsRush?>?) : BaseRecycleDataBindAdapter<PromotionsRush?, ListItemPromotionsRushBinding>(context, variableId, data) {
    companion object {
        const val STATUS = "status"
    }

    private var c1 = 0
    private var c2: Int = 0
    private var c5: Int = 0
    private var t5: Int = 0
    private var imageLoader: ImageLoader? = null
    private var onPromotionsRushListener: OnPromotionsRushListener? = null
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
        return R.layout.list_item_promotions_rush
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsRushBinding>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val payload = payloads[0].toString()
            if (STATUS == payload) {
                val promotions = getItem(position)
                val viewHolder = holder.dataBing
                val status = promotions?.statusCode
                val color = if (status == 1) t5 else if (status == 2) c1 else if (status == 3) c2 else c5
                viewHolder?.status?.setBackgroundColor(color)
                viewHolder?.status?.setText(promotions?.getStatusDisplay(context))
                val startTime = promotions?.startTime?.time ?: 0
                val endTime = promotions?.endTime?.time ?: 0
                val thisTime = (promotions?.thisTime ?: 0) + getLoseTime()
                when (status) {
                    1 -> {
                        viewHolder?.btnRush?.isEnabled = true
                        viewHolder?.btnRush?.setText(getString(R.string.rush_immediately, getTimeDisplay(endTime, thisTime)))
                        viewHolder?.btnRush?.visibility = View.VISIBLE
                    }
                    2 -> {
                        viewHolder?.btnRush?.isEnabled = false
                        viewHolder?.btnRush?.setText(" ")
                        viewHolder?.btnRush?.visibility = View.GONE
                    }
                    3 -> {
                        viewHolder?.btnRush?.isEnabled = false
                        viewHolder?.btnRush?.setText(" ")
                        viewHolder?.btnRush?.visibility = View.GONE
                    }
                    else -> {
                        viewHolder?.btnRush?.isEnabled = false
                        viewHolder?.btnRush?.setText(getString(R.string.start_wait, getTimeDisplay(startTime, thisTime)))
                        viewHolder?.btnRush?.visibility = View.VISIBLE
                    }
                }
            } else {
                onBindViewHolder(holder, position)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsRushBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val promotions = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.icon?.let {
            Glide.with(context)
                .load(Uri.parse(UrlConfig.getHost(context) + promotions?.imageUrl))
                //.apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                .into(it)
        }
        // imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + promotions!!.imageUrl )
        val status = promotions?.statusCode
        val color = if (status == 1) t5 else if (status == 2) c1 else if (status == 3) c2 else c5
        viewHolder?.status?.setBackgroundColor(color)
        viewHolder?.status?.setText(promotions?.getStatusDisplay(context))
        val totalAmount = (NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.nowFinancing == null) 0 else promotions.nowFinancing, 6, 2, 2)
                + "/"
                + NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.totalFinancing == null) 0 else promotions.totalFinancing, 6, 2, 2))
        viewHolder?.totalAmount?.setText(totalAmount)
        val price = (1.toString() + (if (promotions?.distributionCoinType == null) "" else promotions.distributionCoinType)
                + " = "
                + NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.price == null) 0 else promotions.price, 6, 4, 4)
                + if (promotions?.coinType == null) "" else promotions.coinType)
        viewHolder?.price?.setText(price)
        viewHolder?.startDate?.setText(if (promotions?.startTime == null) nullAmount else TimeUtil.getTime(promotions.startTime))
        viewHolder?.endDate?.setText(if (promotions?.endTime == null) nullAmount else TimeUtil.getTime(promotions.endTime))
        /*
            userAmount：//用户可抢额度
    userFinancingAmount：//用户已抢购额度
    userTotalAmount://用户最大额度
             */
        val financing = (NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.userFinancingAmount == null) 0 else promotions.userFinancingAmount, 6, 0, 2)
                + "/"
                + NumberUtil.formatNumberDynamicScaleNoGroup(if (promotions?.userTotalAmout == null) 0 else promotions.userTotalAmout, 6, 0, 2))
        viewHolder?.financing?.setText(financing)

        viewHolder?.btnRush?.setOnClickListener {
            onPromotionsRushListener?.onRush(promotions)
        }

        val startTime = promotions?.startTime?.time ?: 0
        val endTime = promotions?.endTime?.time ?: 0
        val thisTime = (promotions?.thisTime ?: 0) + getLoseTime()
        when (status) {
            1 -> {
                viewHolder?.btnRush?.isEnabled = true
                viewHolder?.btnRush?.setText(getString(R.string.rush_immediately, getTimeDisplay(endTime, thisTime)))
                viewHolder?.btnRush?.visibility = View.VISIBLE
            }
            2 -> {
                viewHolder?.btnRush?.isEnabled = false
                viewHolder?.btnRush?.setText(" ")
                viewHolder?.btnRush?.visibility = View.GONE
            }
            3 -> {
                viewHolder?.btnRush?.isEnabled = false
                viewHolder?.btnRush?.setText(" ")
                viewHolder?.btnRush?.visibility = View.GONE
            }
            else -> {
                viewHolder?.btnRush?.isEnabled = false
                viewHolder?.btnRush?.setText(getString(R.string.start_wait, getTimeDisplay(startTime, thisTime)))
                viewHolder?.btnRush?.visibility = View.VISIBLE
            }
        }
    }

    fun setOnPromotionsRushListener(onPromotionsRushListener: OnPromotionsRushListener?) {
        this.onPromotionsRushListener = onPromotionsRushListener
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
                sb.append(d).append("Day ")
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

    interface OnPromotionsRushListener {
        fun onRush(promotions: PromotionsRush?)
    }
}