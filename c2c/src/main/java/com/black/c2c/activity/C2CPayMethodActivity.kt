package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R

import com.black.c2c.databinding.ActivityPayMethodBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY])
class C2CPayMethodActivity : BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityPayMethodBinding? = null
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
            BlackRouter.getInstance().build(RouterConstData.C2C_WAITE1).go(this)
        }
    }
}