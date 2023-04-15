package com.black.c2c.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CSellerMsg
import com.black.base.model.c2c.PayInfo
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.C2cPayMethodsListBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter

class PayMethodsAdapter(context: Context, variableId: Int, data: ArrayList<PayInfo?>?) : BaseRecycleDataBindAdapter<PayInfo?, C2cPayMethodsListBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.c2c_pay_methods_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<C2cPayMethodsListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2COrder = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.num1?.setText(c2COrder?.name)
        val paymentTypeList = c2COrder?.type
        viewHolder?.num2?.setText(c2COrder?.account)
        viewHolder?.num3?.setText(c2COrder?.depositBank)
        if (paymentTypeList != null && paymentTypeList == 0) {
            viewHolder?.cards?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
            viewHolder?.ma?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== 1) {
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.ma?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
            val image = c2COrder.receiptImage?.filterNot {it == '[' || it == ']' }
            val imageBytes = image?.toByteArray()
            val image2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
            viewHolder?.num6?.setImageBitmap(image2)
        }
        if (paymentTypeList != null && paymentTypeList== 2) {
            viewHolder?.weiXin?.visibility = View.VISIBLE
            viewHolder?.ma?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.GONE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.GONE
            val image = c2COrder.receiptImage?.filterNot {it == '[' || it == ']' }
            val imageBytes = image?.toByteArray()
            val image2 = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes!!.size)
            viewHolder?.num6?.setImageBitmap(image2)
        }
        viewHolder?.bianJi?.setOnClickListener{
            v ->
            if (paymentTypeList != null && paymentTypeList == 0)
            {
                val bundle = Bundle()
                bundle.putParcelable(ConstData.COIN_TYPE, c2COrder)
                BlackRouter.getInstance().build(RouterConstData.C2C_CARDS).go(context)
            }
                if (paymentTypeList != null && paymentTypeList== 1)
                {
                    val bundle = Bundle()
                    bundle.putParcelable(ConstData.COIN_TYPE, c2COrder)
                    BlackRouter.getInstance().build(RouterConstData.C2C_ALI).go(context)
                }
                    if (paymentTypeList != null && paymentTypeList ==  2)
                    {
                        val bundle = Bundle()
                        bundle.putParcelable(ConstData.COIN_TYPE, c2COrder)
                        BlackRouter.getInstance().build(RouterConstData.C2C_WEIXIN).go(context)
                    }
        }
        viewHolder?.delete?.setOnClickListener{
                v ->
            val id = c2COrder?.id
            C2CApiServiceHelper.getPayDelete(context,id, object : NormalCallback<HttpRequestResultString?>(context) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(context, returnData.msg)
                    } else {

                        FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }
        }
    }
