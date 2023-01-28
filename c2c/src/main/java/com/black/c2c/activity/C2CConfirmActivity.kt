package com.black.c2c.activity

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cConfirmBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_CONFRIM])
class C2CConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cConfirmBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_confirm)
        binding?.btnConfirmNew?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
        binding?.wallet?.getPaint()?.setFlags(Paint.FAKE_BOLD_TEXT_FLAG)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm_new) {
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
        }
    }

}