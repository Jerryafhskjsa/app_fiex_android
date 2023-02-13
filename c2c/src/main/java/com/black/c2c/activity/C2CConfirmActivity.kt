package com.black.c2c.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cConfirmBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_CONFRIM])
class C2CConfirmActivity: BaseActionBarActivity(), View.OnClickListener{
    private var binding: ActivityC2cConfirmBinding? = null
    private var TotalTime : Long = 24*60*60*1000 //总时长 24h
    var countDownTimer = object : CountDownTimer(TotalTime,1000){//1000ms运行一次onTick里面的方法
    override fun onFinish(){
        binding?.btnConfirmNew?.isEnabled = true
    }

        override fun onTick(millisUntilFinished: Long) {
            if (TotalTime >= 0){
                val hour = millisUntilFinished/1000/60/60%24
                var minute=millisUntilFinished/1000/60%60
                var second=millisUntilFinished/1000%60
                binding?.time?.setText("$hour: $minute: $second")
                binding?.btnConfirmNew?.isEnabled = false
            }
            else{
                FryingUtil.showToast(mContext,"订单已取消")
            }
        }
    }.start()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_confirm)
        binding?.btnConfirmNew?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.wallet?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
        binding?.wallet?.getPaint()?.setFlags(Paint.FAKE_BOLD_TEXT_FLAG)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm_new) {
            cancelDialog()
        }
        if (id == R.id.action_bar_back) {
            val intent = Intent(this, C2CNewActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (id == R.id.wallet) {
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }    }
    private fun cancelDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.shen_shu_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            FryingUtil.showToast(mContext, "申述已提交")
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
}