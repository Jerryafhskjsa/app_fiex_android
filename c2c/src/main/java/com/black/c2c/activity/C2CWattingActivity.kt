package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cSellerWaitBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_WAITE1])
class C2CWattingActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cSellerWaitBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_seller_wait)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.num?.setOnClickListener(this)
       /* binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_mail)?.setOnClickListener{}
        binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_phone)?.setOnClickListener{}*/
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.waite)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_cancel){
            BlackRouter.getInstance().build(RouterConstData.C2C_WAITE2).go(this)
        }
        if (id == R.id.num){
            BlackRouter.getInstance().build(RouterConstData.C2C_CARDS).go(this)
        }
    }
}