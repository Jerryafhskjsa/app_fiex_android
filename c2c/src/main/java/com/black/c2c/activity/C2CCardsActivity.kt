package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActicityC2cCardsBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_CARDS])
class C2CCardsActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActicityC2cCardsBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.acticity_c2c_cards)
        binding?.cards?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.cardsCmy?.setOnClickListener(this)
        binding?.otherCmy?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.notUse?.setOnClickListener(this)
        binding?.googleCode?.setOnClickListener(this)
        binding?.googleCodeCopy?.setOnClickListener(this)
        binding?.root?.findViewById<ImageButton>(R.id.img_action_bar_right)?.visibility = View.VISIBLE
        binding?.root?.findViewById<ImageButton>(R.id.img_action_bar_right)?.setOnClickListener{
            v ->
        }
    }

    override fun getTitleText(): String? {
        return super.getString(R.string.pay_add)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_submit){
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
        }
        if (id == R.id.not_use){
        }
        if (id == R.id.google_code_copy){
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
        }
    }
}