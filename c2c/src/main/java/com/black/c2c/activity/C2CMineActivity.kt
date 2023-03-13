package com.black.c2c.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.*
import com.black.base.model.c2c.C2CUserInfo
import com.black.base.model.c2c.CoinVOS
import com.black.base.model.c2c.UserCoinAccount
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.UrlConfig
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMineBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_MINE])
class C2CMineActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityC2cMineBinding? = null
    private var userInfo = UserInfo()
    private val money = WalletApiServiceHelper.userBalanceWrapperCache.spotBalance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_mine)
        binding?.linTransfer?.setOnClickListener(this)
        userInfo = CookieUtil.getUserInfo(this)!!
        binding?.id?.setText(userInfo.id)
        getC2CUserInfo()
        getC2CUserAccount()
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.lin_transfer){
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
        }
    }

    private fun getC2CUserInfo(){
        C2CApiServiceHelper.getC2CUserInfo(mContext, object : NormalCallback<HttpRequestResultData<C2CUserInfo?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CUserInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.c2cAvailable?.setText(returnData.data?.coinVOS!![0]?.avAmount.toString() + "/")
                    binding?.unable?.setText(returnData.data?.coinVOS!![0]?.frAmount.toString())
                    binding?.order?.setText(returnData.data?.allOrders.toString())
                    binding?.orderConfirm?.setText(returnData.data?.completion.toString() + "%")
                    binding?.name?.setText(returnData.data?.name)
                    binding?.image?.setText(returnData.data?.name!![0].toString())

                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })

    }

    private fun getC2CUserAccount(){
        C2CApiServiceHelper.getC2CUserAccount(mContext, object : NormalCallback<HttpRequestResultDataList<UserCoinAccount?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultDataList<UserCoinAccount?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })

    }
}