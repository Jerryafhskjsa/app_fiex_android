package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.Money
import com.black.base.model.user.UserInfo
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivitySellerMsgBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_SELLER])
class C2CSellerMsgActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivitySellerMsgBinding? = null
    private val userInfo = UserInfo()
    private val money = Money()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_msg)
        binding?.linTransfer?.setOnClickListener(this)
        binding?.id?.setText(userInfo.id)
        binding?.c2cAvailable?.setText(money.total.toString())
        binding?.unable?.setText(money.forze.toString())
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.lin_transfer){
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
        }
    }
}