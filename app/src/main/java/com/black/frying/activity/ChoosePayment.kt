package com.black.frying.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.*
import com.black.base.model.wallet.WalletBill
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.R
import com.fbsex.exchange.databinding.ActivityChoosePaymentBinding
import com.fbsex.exchange.databinding.ActivityThreePaymentBinding

@Route(value = [RouterConstData.CHOOSEPAYMENT])
class ChoosePayment: BaseActivity(), View.OnClickListener{
    private var binding: ActivityChoosePaymentBinding? = null
    private var list3: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_choose_payment)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.extractAddress?.setOnClickListener(this)

    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.choose_rade)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btn_confirm -> {
                getDepositCreate()
            }

            R.id.extract_address -> {
                list3?.add("Sounth African online banking")
                DeepControllerWindow(mContext as Activity, null, "Sounth African online banking" , list3, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()

                        binding?.extractAddress?.setText(item)

                    }

                }).show()
            }

        }
    }
    private fun getDepositCreate() {
        val amount = intent.getStringExtra(ConstData.TITLE)
        val payVO = PayVO()
        payVO.orderAmount = amount
        WalletApiServiceHelper.getDepositCreate(mContext, payVO, object : NormalCallback<HttpRequestResultData<payOrder?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<payOrder?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val bundle = Bundle()
                    val order = returnData.data
                    bundle.putParcelable(ConstData.WALLET,order)
                    BlackRouter.getInstance().build(RouterConstData.PAYMENTDETAILS).with(bundle).go(mContext)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

}