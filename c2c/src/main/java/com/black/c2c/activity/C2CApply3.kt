package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewThirdC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_APPLY3])
class C2CApply3: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ViewThirdC2cBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.view_third_c2c)
        binding?.bar?.isChecked = intent?.getBooleanExtra("isVisibility", false)!!
        binding?.bar?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
    }

    override fun getTitleText(): String? {
        return  "商家详情"
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.bar){
            binding?.bar?.isChecked = binding?.bar?.isChecked == false
        }
        if (id == R.id.btn_submit){
            if (binding?.bar?.isChecked == true) {
                BlackRouter.getInstance().build(RouterConstData.C2C_MSG1).go(this)
            }
            else{
                FryingUtil.showToast(mContext,"请先阅读《广告发布协议》")
            }
        }
    }
}