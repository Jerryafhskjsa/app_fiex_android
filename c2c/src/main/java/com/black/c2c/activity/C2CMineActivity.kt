package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.Money
import com.black.base.model.user.UserInfo
import com.black.base.util.RouterConstData
import com.black.base.util.UrlConfig
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMineBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_MINE])
class C2CMineActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityC2cMineBinding? = null
    private val userInfo = UserInfo()
    private val money = WalletApiServiceHelper.userBalanceWrapperCache.spotBalance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_mine)
        binding?.linTransfer?.setOnClickListener(this)
        binding?.id?.setText(userInfo.id)
        //binding?.c2cAvailable?.setText(money.total.toString())
        //binding?.unable?.setText(money.forze.toString())
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.lin_transfer){
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
        }
    }
}