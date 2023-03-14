package com.black.c2c.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cResultOneBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_WATTING])
class WattingApplyActivity: BaseActionBarActivity(){
    private var binding: ActivityC2cResultOneBinding? = null
    private var result = ConstData.USER_WATTING
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_result_one)
        /*binding?.appBarLayout?.findViewById<SpanTextView>(R.id.action_bar_back)?.setOnClickListener{
            v ->
            val intent = Intent(this, C2CNewActivity::class.java)
            startActivity(intent)
            finish()
        }*/
        getResult()
    }
    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return  if (result == ConstData.USER_WATTING)"审核中" else "审核结果"
    }
    private fun getResult(){

    }
}