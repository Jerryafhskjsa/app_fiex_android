package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_APPLY1])
class C2CMcApply: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ViewFirstC2cBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.view_first_c2c)
        binding?.bar?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
    }

    override fun getTitleText(): String? {
        return  "商家详情"
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.bar){
            binding?.bar?.isChecked = binding?.bar?.isChecked == false
        }
        if (id == R.id.btn_submit){
            val extras = Bundle()
            var boolean = binding?.bar?.isChecked
            extras.putBoolean("isVisibility",boolean?: false)
            BlackRouter.getInstance().build(RouterConstData.C2C_APPLY2).with(extras).go(this)
        }
    }
}