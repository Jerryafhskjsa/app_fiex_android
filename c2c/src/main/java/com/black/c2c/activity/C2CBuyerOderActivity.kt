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
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_BUYER])
class C2CBuyerOderActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityC2cBuyerOderBinding? = null
    private var sellerName: String? = null
    private var payChain: String? = null
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buyer_oder)
        payChain = intent.getStringExtra(ConstData.C2C_ORDER)
        binding?.add?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.send?.setOnClickListener(this)
        binding?.phone?.setOnClickListener(this)
        checkClickable()
    }
    override fun getTitleText(): String? {
        return sellerName
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm) {
            val extras = Bundle()
            extras.putString(ConstData.C2C_ORDER, payChain)
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY_FOR).with(extras).go(mContext)
        }
        }
    private fun checkClickable(){

    }
}