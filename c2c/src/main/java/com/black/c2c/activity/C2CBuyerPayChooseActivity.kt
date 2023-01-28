package com.black.c2c.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActiivtyPayForSellerBinding
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY_FOR])
class C2CBuyerPayChooseActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActiivtyPayForSellerBinding? = null
    private var sellerName: String? = null
    private var payFor: String? = null
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.actiivty_pay_for_seller)
        payFor = intent.getStringExtra(ConstData.C2C_ORDER)
        if (payFor == "银行卡"){
            binding?.cards?.visibility = View.VISIBLE
            binding?.idPay?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
        }
        if (payFor == "微信"){
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.idPay?.visibility = View.GONE
            binding?.cards?.visibility = View.GONE
        }
        else{
            binding?.idPay?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
        }
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)
        binding?.num2?.setOnClickListener(this)
        binding?.num3?.setOnClickListener(this)
        binding?.num4?.setOnClickListener(this)
        binding?.num5?.setOnClickListener(this)
        binding?.num6?.setOnClickListener(this)
        binding?.num7?.setOnClickListener(this)
        binding?.num8?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)

        checkClickable()
    }
    override fun getTitleText(): String? {
        return "请向商家付款"
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm) {
            BlackRouter.getInstance().build(RouterConstData.C2C_BUYER_PAY).go(mContext)
        }
    }
    private fun checkClickable(){

    }
}