package com.black.c2c.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyConfirmBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_BUY_CONFRIM])
class C2CBuyConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cBuyConfirmBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buy_confirm)
        binding?.btnConfirmNew?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.wallet?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
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

}