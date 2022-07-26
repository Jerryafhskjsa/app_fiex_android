package com.black.community.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.community.FactionItem
import com.black.base.util.ImageLoader
import com.black.base.util.UrlConfig
import com.black.community.R
import com.black.community.databinding.ListItemFactionListBinding
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class FactionListAdapter(context: Context, variableId: Int, data: ArrayList<FactionItem?>?) : BaseRecycleDataBindAdapter<FactionItem?, ListItemFactionListBinding>(context, variableId, data) {
    companion object {
        const val STATUS = "status"
    }

    private var imageLoader: ImageLoader? = null
    private var loseTime: Long = 0
    private var bgWaiting: Drawable? = null
    private var bgDoing: Drawable? = null
    private var colorWaiting = 0
    private var colorDoing: Int = 0

    init {
        imageLoader = ImageLoader(context)
    }

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgWaiting = SkinCompatResources.getDrawable(context, R.drawable.bg_faction_status_waiting)
        bgDoing = SkinCompatResources.getDrawable(context, R.drawable.bg_faction_status_doing)
        colorWaiting = -0xe73e3e
        colorDoing = -0x594a9
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_faction_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemFactionListBinding>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val payload = payloads[0].toString()
            if (STATUS == payload) {
                val faction = getItem(position)
                val status = faction?.ownerStatusCode
                val viewHolder = holder.dataBing
                viewHolder?.status?.setText(faction?.getStatusDisplay(context))
                val endTime = when (status) {
                    0 -> faction.nextOwnerChangeTime ?: 0
                    2 -> faction.nextOwnerChangeFinishedTime ?: 0
                    else -> 0
                }
                val thisTime: Long = (faction?.thisTime ?: 0) + getLoseTime()
                viewHolder?.time?.setText(getTimeDisplay(endTime, thisTime))
                if (true == faction?.isWaiting(thisTime) || true == faction?.isChoosing(thisTime)) {
                    viewHolder?.status?.visibility = View.VISIBLE
                    viewHolder?.time?.visibility = View.VISIBLE
                    if (faction?.isWaiting(thisTime)) {
                        viewHolder?.status?.background = bgWaiting
                        viewHolder?.time?.setTextColor(colorWaiting)
                    } else if (faction?.isChoosing(thisTime)) {
                        viewHolder?.status?.background = bgDoing
                        viewHolder?.time?.setTextColor(colorDoing)
                    }
                } else {
                    viewHolder?.status?.visibility = View.INVISIBLE
                    viewHolder?.time?.visibility = View.INVISIBLE
                }
            } else {
                onBindViewHolder(holder, position)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemFactionListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val faction = getItem(position)
        val viewHolder = holder.dataBing
        imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + faction?.leagueLogo)
        viewHolder?.name?.setText(if (faction?.name == null) nullAmount else faction?.name)
        val status = faction?.ownerStatusCode
        viewHolder?.status?.setText(faction?.getStatusDisplay(context))
        val endTime = when (status) {
            0 -> faction?.nextOwnerChangeTime ?: 0
            2 -> faction?.nextOwnerChangeFinishedTime ?: 0
            else -> 0
        }
        val thisTime = (faction?.thisTime ?: 0) + getLoseTime()
        viewHolder?.time?.setText(getTimeDisplay(endTime, thisTime))
        if (true == faction?.isWaiting(thisTime) || true == faction?.isChoosing(thisTime)) {
            viewHolder?.status?.visibility = View.VISIBLE
            viewHolder?.time?.visibility = View.VISIBLE
            if (faction?.isWaiting(thisTime)) {
                viewHolder?.status?.background = bgWaiting
                viewHolder?.time?.setTextColor(colorWaiting)
            } else if (faction?.isChoosing(thisTime)) {
                viewHolder?.status?.background = bgDoing
                viewHolder?.time?.setTextColor(colorDoing)
            }
        } else {
            viewHolder?.status?.visibility = View.INVISIBLE
            viewHolder?.time?.visibility = View.INVISIBLE
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
}