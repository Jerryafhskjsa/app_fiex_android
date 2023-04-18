package com.black.c2c.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMessage
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.databinding.ListC2cMessageBinding
import com.black.net.HttpRequestResult
import com.black.util.CommonUtil
import com.bumptech.glide.Glide
import com.google.zxing.WriterException
import skin.support.content.res.SkinCompatResources
import java.lang.Byte.decode
import java.lang.Long.decode
import java.util.*
import kotlin.collections.ArrayList

class C2CMessageAdapter(context: Context, variableId: Int, data: ArrayList<C2CMessage?>?) : BaseRecycleDataBindAdapter<C2CMessage?, ListC2cMessageBinding?>(context, variableId, data) {
    private var c1 = 0
    private var t5: Int = 0
    private var viewHolder: ListC2cMessageBinding? = null
    private var userInfo: UserInfo? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_c2c_message
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cMessageBinding?>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2COrder = getItem(position)
        userInfo = CookieUtil.getUserInfo(context)
        viewHolder = holder.dataBing
        if (c2COrder!!.source == 1 && c2COrder.type == 1) {
            viewHolder?.buy?.visibility = View.GONE
            viewHolder?.time?.setText(TimeUtil.getTime(c2COrder.createTime))
            viewHolder?.message2?.setText(c2COrder.note)
            getC2COIV2(c2COrder.orderId)
        }
        else if (c2COrder.source == -1 && c2COrder.type == 1){
            viewHolder?.sell?.visibility = View.GONE
            viewHolder?.buy?.visibility = View.GONE
            viewHolder?.time?.setText(TimeUtil.getTime(c2COrder.createTime))
            viewHolder?.message2?.setText(c2COrder.note)
        }
        else if (c2COrder.source == 1 && c2COrder.type == 2){
            viewHolder?.buy?.visibility = View.GONE
            getC2COIV2(c2COrder.orderId)
            viewHolder?.time?.setText(TimeUtil.getTime(c2COrder.createTime))
            val image = c2COrder.note
            viewHolder?.image?.let {
                Glide.with(context)
                    .load(Uri.parse(image))
                    //.apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                    .into(it)
            }
        }
        else if (c2COrder.source == -1 && c2COrder.type == 2){
            viewHolder?.sell?.visibility = View.GONE
            viewHolder?.buy?.visibility = View.GONE
            viewHolder?.time?.setText(TimeUtil.getTime(c2COrder.createTime))
            val image = c2COrder.note
            viewHolder?.image?.let {
                Glide.with(context)
                    .load(Uri.parse(image))
                    //.apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                    .into(it)
            }
        }
        else if (c2COrder.source == 0 && c2COrder.type == 1){
            viewHolder?.sell?.visibility = View.GONE
            viewHolder?.time2?.setText(TimeUtil.getTime(c2COrder.createTime))
            viewHolder?.message?.setText(c2COrder.note)
            viewHolder?.name?.setText(userInfo?.realName!![0].toString())
        }
        else {
            viewHolder?.buy?.visibility = View.GONE
            viewHolder?.time2?.setText(TimeUtil.getTime(c2COrder.createTime))
            viewHolder?.name?.setText(userInfo?.realName!![0].toString())
            val image = c2COrder.note
            viewHolder?.image2?.let {
                Glide.with(context)
                    .load(Uri.parse(image))
                    //.apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                    .into(it)
            }
        }
    }
       private fun getC2COIV2(id: String?) {
            C2CApiServiceHelper.getC2CDetails(
                context,
                id,
                object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(context) {
                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                    }

                    override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            viewHolder?.name2?.text = returnData.data?.otherSideRealName!![0].toString()
                        } else {

                            FryingUtil.showToast(
                                context,
                                if (returnData == null) "null" else returnData.msg
                            )
                        }
                    }
                })
        }

}