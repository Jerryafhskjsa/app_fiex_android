package com.black.frying.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.payOrder
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.R
import com.fbsex.exchange.databinding.ActivityChooseDetailsBinding


@Route(value = [RouterConstData.PAYMENTDETAILS])

class PaymentDetails: BaseActivity(), View.OnClickListener{
    private var binding: ActivityChooseDetailsBinding? = null
    private var order: payOrder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_choose_details)
        order = intent.getParcelableExtra(ConstData.WALLET)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.barA?.setOnClickListener(this)
        binding?.choose?.setText(order?.bankCode)
        binding?.coinType?.setText(order?.bankCode)
        binding?.amount?.setText(order?.price.toString())
        binding?.type?.setText(order?.orderAmount.toString())
        binding?.time?.setText(order?.amount.toString())

    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.pay_details)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.bar_a -> {
                binding?.barA?.isChecked = binding?.barA?.isChecked == false
            }
            R.id.btn_submit -> {
                if (binding?.barA?.isChecked == false) {
                    FryingUtil.showToast(mContext,getString(R.string.mian3))
                } else {
                    getUrl()

                }


            }
        }
    }
    private fun getUrl(){
            WalletApiServiceHelper.getDepositConfirm(mContext, order?.id , object : NormalCallback<HttpRequestResultString?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        val bundle = Bundle()
                        bundle.putString(ConstData.TITLE, returnData.data)
                        BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

}