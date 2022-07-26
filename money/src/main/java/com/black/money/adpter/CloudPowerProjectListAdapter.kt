package com.black.money.adpter

import android.content.Context
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.CloudPowerProject
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.R
import com.black.money.databinding.ListItemCloudPowerProjectBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class CloudPowerProjectListAdapter(context: Context, variableId: Int, data: ArrayList<CloudPowerProject?>?) : BaseRecycleDataBindAdapter<CloudPowerProject?, ListItemCloudPowerProjectBinding>(context, variableId, data) {
    companion object {
        const val STATUS = "status"
    }

    private var c1 = 0
    private var c2: Int = 0
    private var c5: Int = 0
    private var t5: Int = 0
    private var imageLoader: ImageLoader? = null
    private var onCloudPowerProjectHandleListener: OnCloudPowerProjectHandleListener? = null
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
        return R.layout.list_item_cloud_power_project
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerProjectBinding>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerProjectBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val project = getItem(position)
        val viewHolder = holder.dataBing
        val status = project?.statusCode ?: 0
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, project?.distributionCoinType)
        viewHolder?.coinType?.setText(if (project?.distributionCoinType == null) nullAmount else project.distributionCoinType)
        viewHolder?.powerCoin?.setText(String.format("%s 云挖矿", if (project?.interestCoinType == null) nullAmount else project.interestCoinType))
        viewHolder?.power?.setText(String.format("1%s = 1T算力", if (project?.distributionCoinType == null) nullAmount else project.distributionCoinType))
        viewHolder?.powerFee?.setText(String.format("%s %s/T/天",
                if (project?.price == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(project.price, 9, 0, 8),
                if (project?.coinType == null) nullAmount else project.coinType))
        viewHolder?.interest?.setText(String.format("%s %s/T/天",
                if (project?.expectedInterest == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(project.expectedInterest, 9, 0, 8),
                "USDT"))

        viewHolder?.powerTotal?.setText(String.format("%sTH/S", if (project?.totalFinancing == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(project.totalFinancing, 9, 0, 8)))
        viewHolder?.powerSell?.setText(String.format("%sTH/S", if (project?.nowFinancing == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(project.nowFinancing, 9, 0, 8)))
        viewHolder?.mining?.setText(if (project?.startTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", project.startTime!!))
        viewHolder?.cycle?.setText(String.format("%s天", if (project?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(project.day)))
        viewHolder?.startTime?.setText(if (project?.buyStartTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", project.buyStartTime!!))
        viewHolder?.endTime?.setText(if (project?.buyEndTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", project.buyEndTime!!))
        viewHolder?.btnBuy?.setOnClickListener {
            if (onCloudPowerProjectHandleListener != null && project != null) {
                onCloudPowerProjectHandleListener!!.onRush(project)
            }
        }
        //0未开始  1售卖中    2售卖结束   3完成（挖矿中）  4完成
        //0未开始  1售卖中    2售卖结束   3完成（挖矿中）  4完成
        when (status) {
            0 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.setText("立即抢购")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            1 -> {
                viewHolder?.btnBuy?.isEnabled = true
                viewHolder?.btnBuy?.setText("立即抢购")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            2 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.setText("已售罄")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            3 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.setText("挖矿中")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            4 -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.setText("已到期")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
            else -> {
                viewHolder?.btnBuy?.isEnabled = false
                viewHolder?.btnBuy?.setText("")
                viewHolder?.btnBuy?.visibility = View.VISIBLE
            }
        }
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
                sb.append(d).append("天 ")
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

    fun setOnPromotionsHandleListener(onCloudPowerProjectHandleListener: OnCloudPowerProjectHandleListener?) {
        this.onCloudPowerProjectHandleListener = onCloudPowerProjectHandleListener
    }

    fun setLoseTime(loseTime: Long) {
        this.loseTime = loseTime
    }

    fun getLoseTime(): Long {
        return loseTime
    }

    interface OnCloudPowerProjectHandleListener {
        fun onRush(project: CloudPowerProject)
    }
}