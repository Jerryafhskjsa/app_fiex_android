package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.graphics.toColorInt
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.PayInfo
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.widget.SpanCheckedTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBillsCancelBinding
import com.black.c2c.databinding.ActivityC2cOrderBinding
import com.black.c2c.util.C2CHandleCheckHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.C2C_BILL_CANCEL])
class C2CBillCancelActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cBillsCancelBinding? = null
    private var id2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_bills_cancel)
        id2 = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.pay?.setOnClickListener(this)
        binding?.adId?.setText(id2)
        getC2COIV2(id2)
    }


    override fun getTitleText(): String? {
        return getString(R.string.order_canceled)
    }

    override fun onClick(v: View) {
        val id = v.id

    }

    //订单详情
    fun getC2COIV2(id: String?){
        C2CApiServiceHelper.getC2CDetails(
            mContext,
            id,
            object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.coinType?.setText(returnData.data?.coinType)
                        binding?.account?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                        binding?.price?.setText(returnData.data?.price.toString())
                        binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                        val time = TimeUtil.getTime(returnData.data?.createTime)
                        binding?.createTime?.setText(time)
                        binding?.name?.setText(returnData.data?.otherSideRealName)
                        binding?.accountTotal?.setText(returnData.data?.otherSideCompletedOrders30Days.toString())
                        val c1 = SkinCompatResources.getColor(context, R.color.T13)
                        val t5 = SkinCompatResources.getColor(context, R.color.T5)
                        if (returnData.data?.direction == "B")
                        {
                            binding?.direction?.setText(getString(R.string.buy_02))
                            binding?.sellNull?.visibility = View.VISIBLE
                            binding?.direction?.setTextColor(c1)
                            val payMethod = returnData.data?.payMethod
                            if (payMethod == 1){
                                binding?.payName?.setText(getString(R.string.id_pay))
                                binding?.aliColor?.visibility = View.VISIBLE
                            }
                            else if (payMethod == 2){
                                binding?.payName?.setText(getString(R.string.wei_xin))
                                binding?.weiXinColor?.visibility = View.VISIBLE
                            }
                            else{
                                binding?.payName?.setText(getString(R.string.cards))
                                binding?.cardsColor?.visibility = View.VISIBLE
                            }
                        }
                        if (returnData.data?.direction == "S") {
                            binding?.direction?.setText(getString(R.string.sell))
                            binding?.direction?.setTextColor(t5)
                        }
                    } else {

                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }
}