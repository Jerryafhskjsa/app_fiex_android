package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cWeixinBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_WEIXIN])
class C2CWeiXinActivity: BaseActionBarActivity(), View.OnClickListener  {
    private var binding: ActivityC2cWeixinBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_weixin)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.photo?.setOnClickListener(this)
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
        else if (id == R.id.photo){

        }
    }
}