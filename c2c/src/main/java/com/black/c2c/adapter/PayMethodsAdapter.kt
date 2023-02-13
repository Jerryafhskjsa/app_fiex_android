package com.black.c2c.adapter

import android.content.Context
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CSellerMsg
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.C2cPayMethodsListBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter

class PayMethodsAdapter(context: Context, variableId: Int, data: ArrayList<C2CSellerMsg?>?) : BaseRecycleDataBindAdapter<C2CSellerMsg?, C2cPayMethodsListBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.c2c_pay_methods_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<C2cPayMethodsListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val list = getItem(position)?.list
        val c2COrder = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.num1?.setText(c2COrder?.merchantName)
        val paymentTypeList = c2COrder?.merchantName
        if (paymentTypeList != null && paymentTypeList == getString(R.string.cards)) {
            viewHolder?.cards?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
            viewHolder?.ma?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== getString(R.string.id_pay)) {
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.ma?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== getString(R.string.wei_xin)) {
            viewHolder?.weiXin?.visibility = View.VISIBLE
            viewHolder?.ma?.visibility = View.VISIBLE
            viewHolder?.bank?.visibility = View.GONE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.GONE
        }
        viewHolder?.bianJi?.setOnClickListener{
            v ->
            if (paymentTypeList != null && paymentTypeList == getString(R.string.cards))
            {
                BlackRouter.getInstance().build(RouterConstData.C2C_CARDS).go(context)
            }
                if (paymentTypeList != null && paymentTypeList== getString(R.string.id_pay))
                {
                    BlackRouter.getInstance().build(RouterConstData.C2C_ALI).go(context)
                }
                    if (paymentTypeList != null && paymentTypeList ==  getString(R.string.wei_xin))
                    {
                        BlackRouter.getInstance().build(RouterConstData.C2C_WEIXIN).go(context)
                    }
        }
        viewHolder?.delete?.setOnClickListener{
                v ->
            C2CApiServiceHelper.getSellerMsg(context, object : NormalCallback<HttpRequestResultData<C2CSellerMsg?>?>(context) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2CSellerMsg?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(context, "此收款方式已删除！")
                    } else {

                        FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }
        }
    }
