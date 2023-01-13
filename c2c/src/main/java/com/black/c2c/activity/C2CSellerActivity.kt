package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY2])
class C2CSellerActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivitySellerChooseBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_choose)
        binding?.cards?.setOnClickListener(this)
        binding?.idPay?.setOnClickListener(this)
        binding?.weiXin?.setOnClickListener(this)
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.get_method)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.id_pay){
            BlackRouter.getInstance().build(RouterConstData.C2C_ALI).go(this)
        }
        if (id == R.id.cards){
            BlackRouter.getInstance().build(RouterConstData.C2C_CARDS).go(this)
        }
        if (id == R.id.wei_xin){
            BlackRouter.getInstance().build(RouterConstData.C2C_WEIXIN).go(this)
        }
    }
}