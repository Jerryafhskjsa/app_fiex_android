package com.black.c2c.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanCheckedTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActiivtyPayForSellerBinding
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_PAY_FOR])
class C2CBuyerPayChooseActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActiivtyPayForSellerBinding? = null
    private var sellerName: String? = null
    private val mHandler = Handler()
    private var payFor: String? = null

    private var getPhoneCodeLocked = false
    private var getPhoneCodeLockedTime = 0
    private val getPhoneCodeLockTimer = object : Runnable {
        override fun run() {
            getPhoneCodeLockedTime--
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false
            } else {
                binding?.time?.setText(
                    getString(
                        R.string.aler_get_code_locked,
                        getPhoneCodeLockedTime.toString()
                    )
                )
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }
    }
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
        binding?.btnCancel?.setOnClickListener(this)
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
            confirmDialog()

        }
        if (id == R.id.btn_cancel) {
            cancelDialog()
        }
    }
    private fun checkClickable(){

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
            FryingUtil.showToast(mContext,"取消订单成功")
            if(dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked == true){
                val intent = Intent(this, C2CNewActivity::class.java)
                startActivity(intent)
                finish()
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
            BlackRouter.getInstance().build(RouterConstData.C2C_BUYER_PAY).go(mContext)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
}