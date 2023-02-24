package com.black.c2c.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.OtcReceiptModel
import com.black.base.model.c2c.ReceiptInfo
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanCheckedTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActiivtyPayForSellerBinding
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY_FOR])
class C2CBuyerPayChooseActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActiivtyPayForSellerBinding? = null
    private var sellerName: String? = null
    private val mHandler = Handler()
    private var payFor: String? = null
    private var id: String? = null
    private var receiptInfo: OtcReceiptModel? = null
    private var TotalTime : Long = 15*60*1000 //总时长 15min
    var countDownTimer = object : CountDownTimer(TotalTime,1000){//1000ms运行一次onTick里面的方法
    override fun onFinish(){
    }

        override fun onTick(millisUntilFinished: Long) {
            if (TotalTime >= 0){
                var minute=millisUntilFinished/1000/60%60
                var second=millisUntilFinished/1000%60
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
        binding = DataBindingUtil.setContentView(this, R.layout.actiivty_pay_for_seller)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.idPayMa?.setOnClickListener(this)
        binding?.weiXinMa?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)
        binding?.num2?.setOnClickListener(this)
        binding?.num3?.setOnClickListener(this)
        binding?.num4?.setOnClickListener(this)
        binding?.num5?.setOnClickListener(this)
        binding?.num6?.setOnClickListener(this)
        binding?.num7?.setOnClickListener(this)
        binding?.num8?.setOnClickListener(this)
        binding?.num1?.setOnClickListener(this)
        countDownTimer
        getC2CGP()
        getPayChoose()
        checkClickable()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm) {
            confirmDialog()

        }
        if (id == R.id.btn_cancel) {
            cancelDialog()
        }
        if (id == R.id.wei_xin_ma) {
            twoMaDialog()

        }
        if (id == R.id.id_pay_ma) {
            twoMaDialog()
        }
    }
    private fun checkClickable(){

    }
    private fun twoMaDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.two_ma_dialog, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.dimAmount = 0.2f
            //设置背景昏暗度
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_submit).setOnClickListener { v ->
            FryingUtil.showToast(mContext,"保存成功")
            dialog.dismiss()
        }
    }
    private fun cancelDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.cancel_dialog, null)
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
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            if(dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked == true){
                getC2cCancel()
            }
            else{
                FryingUtil.showToast(mContext,"请先确认是否付款给卖方")
            }
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.range).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked = dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked == false
        }
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun confirmDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.cancel_dialog_two ,null)
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
            getC2cConfirm()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun getPayChoose() {
        C2CApiServiceHelper.getC2CDetails(mContext, id,  object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    receiptInfo = returnData.data?.receiptInfo
                    binding?.name?.setText(receiptInfo?.name)
                    binding?.name2?.setText(receiptInfo?.account)
                    binding?.name3?.setText(receiptInfo?.depositBank)
                    binding?.name4?.setText(receiptInfo?.subbranch)
                    binding?.name5?.setText(receiptInfo?.name)
                    binding?.name6?.setText(receiptInfo?.account)
                    binding?.name7?.setText(receiptInfo?.name)
                    binding?.name8?.setText(receiptInfo?.account)
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //获取首付款方式
    private fun getC2CGP() {
        val id = intent.getStringExtra(ConstData.COIN_TYPE)
        C2CApiServiceHelper.getC2CGP(mContext, id,  object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    payFor = returnData.data
                    if (payFor == "2"){
                        binding?.cards?.visibility = View.VISIBLE
                        binding?.idPay?.visibility = View.GONE
                        binding?.weiXin?.visibility = View.GONE
                    }
                    if (payFor == "1"){
                        binding?.weiXin?.visibility = View.VISIBLE
                        binding?.idPay?.visibility = View.GONE
                        binding?.cards?.visibility = View.GONE
                    }
                    else{
                        binding?.idPay?.visibility = View.VISIBLE
                        binding?.cards?.visibility = View.GONE
                        binding?.weiXin?.visibility = View.GONE
                    }
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //撤单
    private fun getC2cCancel(){
        val id = intent.getStringExtra(ConstData.COIN_TYPE)
        C2CApiServiceHelper.getC2COrderCancel(mContext, id,  object : NormalCallback<HttpRequestResultData<String?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext,"取消订单成功")
                    val intent = Intent(this@C2CBuyerPayChooseActivity, C2CNewActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //确认付款
    private fun getC2cConfirm(){
        val id = intent.getStringExtra(ConstData.COIN_TYPE)
        C2CApiServiceHelper.getConfirmPay(mContext, id,null,null,null,  object : NormalCallback<HttpRequestResultData<String?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    BlackRouter.getInstance().build(RouterConstData.C2C_BUYER_PAY).go(mContext)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}