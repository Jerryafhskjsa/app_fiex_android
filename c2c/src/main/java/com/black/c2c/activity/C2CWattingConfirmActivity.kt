package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cSellerConfirmBinding
import com.black.c2c.databinding.ActivityC2cSellerWaitBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_WAITE2])
class C2CWattingConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cSellerConfirmBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_seller_confirm)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.aboveBar?.setOnClickListener(this)
        binding?.bottomBar?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)
        binding?.num2?.setOnClickListener(this)
        binding?.num3?.setOnClickListener(this)
        /*binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_mail)?.setOnClickListener{}
        binding?.root?.findViewById<SpanTextView>(R.id.img_action_bar_phone)?.setOnClickListener{}*/
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.waite_confirm)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_cancel){
            BlackRouter.getInstance().build(RouterConstData.C2C_ALI).go(this)
        }
        if (id == R.id.bottom_bar){
            binding?.above?.visibility = View.VISIBLE
            binding?.bottom?.visibility = View.GONE
        }
        if (id == R.id.above_bar){
            binding?.above?.visibility = View.GONE
            binding?.bottom?.visibility = View.VISIBLE
        }
        if (id == R.id.num1){
        }
        if (id == R.id.num2){
        }
        if (id == R.id.num3){
        }
        if (id == R.id.bottom_bar){
        }
    }
}