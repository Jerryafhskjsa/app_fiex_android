package com.black.c2c.activity

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.BtnC2cWaitBuyBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_BUYER_PAY])
class C2CBuyerPayActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: BtnC2cWaitBuyBinding? = null
    private var id: String? = null
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.btn_c2c_wait_buy)
        id = intent.getStringExtra(ConstData.BUY_PRICE)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        countDownTimer
        getPayChoose()
        checkClickable()
    }
    override fun getTitleText(): String? {
        return "等待卖家确认收款并放币"
    }
    override fun onClick(v: View) {
        val id2 = v.id
        if (id2 == R.id.btn_confirm) {
            val bundle = Bundle()
            bundle.putString(ConstData.BUY_PRICE,id)
            BlackRouter.getInstance().build(RouterConstData.C2C_BUY_CONFRIM).go(mContext)
        }
        if (id2 == R.id.btn_cancel) {
            cancelDialog()
        }
    }
    private fun checkClickable(){

    }
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
            FryingUtil.showToast(mContext, "申述已发送")
            dialog.dismiss()
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
                    binding?.id?.setText(id)
                    binding?.coinType?.setText(returnData.data?.coinType)
                    binding?.amount?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                    binding?.price?.setText(returnData.data?.price.toString())
                    binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                    binding?.time?.setText(returnData.data?.createTime)
                    binding?.realName?.setText(returnData.data?.otherSideRealName)
                    binding?.realNameName?.setText(returnData.data?.payEeRealName)
                    binding?.person?.setText(returnData.data?.receiptInfo?.name)
                    binding?.ad?.setText(returnData.data?.receiptInfo?.account)
                    val payMethod = returnData.data?.payMethod
                    if (payMethod == 0){
                        binding?.paymentMethod?.setText(getString(R.string.id_pay))
                        binding?.ma?.visibility = View.VISIBLE
                    }
                    else if (payMethod == 1){
                        binding?.paymentMethod?.setText(getString(R.string.wei_xin))
                        binding?.ma?.visibility = View.VISIBLE
                    }
                    else{
                        binding?.paymentMethod?.setText(getString(R.string.cards))
                        binding?.ma?.visibility = View.GONE
                    }
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}