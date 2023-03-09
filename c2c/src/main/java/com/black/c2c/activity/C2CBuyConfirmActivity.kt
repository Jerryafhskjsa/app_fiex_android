package com.black.c2c.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyConfirmBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_BUY_CONFRIM])
class C2CBuyConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cBuyConfirmBinding? = null
    private var id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buy_confirm)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.btnConfirmNew?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.wallet?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
        getPayChoose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm_new) {
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
        }
        if (id == R.id.action_bar_back) {
            val intent = Intent(this, C2CNewActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (id == R.id.wallet) {
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }    }
    private fun getPayChoose() {
        C2CApiServiceHelper.getC2CDetails(mContext, id,  object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.id?.setText(id)
                    binding?.coinType?.setText(returnData.data?.coinType)
                    binding?.account?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                    binding?.price?.setText(returnData.data?.price.toString())
                    binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                    val time = TimeUtil.getTime(returnData.data?.createTime)
                    binding?.createTime?.setText(time)
                    binding?.realName?.setText(returnData.data?.otherSideRealName)
                    binding?.totalAmount?.setText(returnData.data?.otherSideAllOrders30Days.toString())
                    val payMethod = returnData.data?.payMethod
                    if (payMethod == 1){
                        binding?.paymentMethod?.setText(getString(R.string.id_pay))
                        binding?.ma?.visibility = View.VISIBLE
                    }
                    else if (payMethod == 2){
                        binding?.paymentMethod?.setText(getString(R.string.wei_xin))
                        binding?.ma?.visibility = View.VISIBLE
                    }
                    else{
                        binding?.paymentMethod?.setText(getString(R.string.cards))
                        binding?.ma?.visibility = View.GONE
                    }
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}