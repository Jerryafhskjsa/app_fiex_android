package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.C2CSellerMsg
import com.black.base.model.c2c.PayInfo
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R

import com.black.c2c.databinding.ActivityPayMethodBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY])
class C2CPayMethodActivity : BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityPayMethodBinding? = null
    private var list: ArrayList<PayInfo?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay_method)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnConfirmNew?.setOnClickListener(this)
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.get_method)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm){
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY2).go(this)
        }
        if (id == R.id.btn_confirm_new){
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY2).go(this)
        }
    }
    private fun getAllPay(){
        C2CApiServiceHelper.getSellerMsg(mContext, object : NormalCallback<HttpRequestResultData<C2CSellerMsg?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CSellerMsg?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    list = returnData.data?.list
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

}