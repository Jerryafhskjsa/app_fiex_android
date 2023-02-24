package com.black.c2c.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_BUYER])
class C2CBuyerOderActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityC2cBuyerOderBinding? = null
    private var sellerName: String? = "帅"
    private var id: String? = null
    private var payChain: String? = null
    var TotalTime: Long = 15*60*1000 //总时长 15min
    var countDownTimer = object : CountDownTimer(TotalTime,1000){//1000ms运行一次onTick里面的方法
    override fun onFinish(){
    }

        override fun onTick(millisUntilFinished: Long) {
            if (TotalTime >= 0){
                val minute=millisUntilFinished/1000/60%60
                val second=millisUntilFinished/1000%60
                binding?.time?.setText("$minute:$second")}
            else{
                FryingUtil.showToast(mContext,"订单已取消")
            }
        }
    }.start()
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
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.add?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.send?.setOnClickListener(this)
        binding?.phone?.setOnClickListener(this)
        countDownTimer
        checkClickable()
    }
    override fun getTitleText(): String? {
        return  sellerName
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