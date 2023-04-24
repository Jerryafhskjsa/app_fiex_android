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
import com.black.util.NumberUtil
import com.black.wallet.R
import com.fbsex.exchange.databinding.ActivityChooseDetailsBinding


@Route(value = [RouterConstData.PAYMENTDETAILS])

class PaymentDetails: BaseActivity(), View.OnClickListener{
    private var binding: ActivityChooseDetailsBinding? = null
    private var order: payOrder? = null
    private var direction: String? = null
    private var bank: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_choose_details)
        order = intent.getParcelableExtra(ConstData.WALLET)
        bank = intent.getStringExtra(ConstData.BIRTH)
        direction = intent.getStringExtra(ConstData.TITLE)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.barA?.setOnClickListener(this)
        if (direction == "B") {
           // binding?.choose?.setText(order?.bankCode)
            binding?.fee?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.fee, 8, 4, 2)
                ) + order?.coin
            )
            binding?.choose?.setText(bank)
            binding?.amount?.setText(
                "1.0000" + order?.ccyNo + String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.price, 8, 8, 2)
                ) + order?.coin
            )
            binding?.type?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.orderAmount, 8, 4, 2)
                ) + order?.ccyNo
            )
            binding?.time?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.orderAmount!! * order?.price!!, 8, 4, 2)
                ) + order?.coin
            )
        }
        else{
           // binding?.choose?.setText(order?.bankCode)
            binding?.fee?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.fee, 8, 4, 2)
                ) + order?.coin
            )
            binding?.choose?.setText(order?.bankCode)
            binding?.amount?.setText(
                "1.0000" + order?.coin + String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.price, 8, 8, 2)
                ) + order?.ccyNo
            )
            binding?.type?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.orderAmount, 8, 4, 2)
                ) + order?.coin
            )
            binding?.time?.setText(
                String.format(
                    " ≈ %S ",
                    NumberUtil.formatNumberDynamicScaleNoGroup(order?.orderAmount!! * order?.price!!, 8, 4, 2)
                ) + order?.ccyNo
            )
        }
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
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS ) {
                        if (direction == "B") {
                            val bundle = Bundle()
                            bundle.putString(ConstData.URL, returnData.data)
                            bundle.putString(ConstData.TITLE, "Pay Bank")
                            BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle)
                                .go(mContext)
                            finish()
                        }
                        else{
                            FryingUtil.showToast(mContext, "Checkout Success")
                        }
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

}