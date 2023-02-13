package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.future.FundingRateBean
import com.black.base.model.future.LeverageBracketBean
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.widget.SpanCheckedTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cOrderBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.RoundingMode

@Route(value = [RouterConstData.C2C_ORDERS])
class C2COrdersActivity: BaseActionBarActivity(), View.OnClickListener{
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
        //private var TAB_PAYPAID: String? = "PayPai"
    }
    private var userInfo: UserInfo? = null
    private var chainNames: MutableList<String?>? = null
    private var binding: ActivityC2cOrderBinding? = null
    private var c2cList: C2CMainAD? = null
    private var payChain: String? = null
    private var cointype: String? = null
    private val mHandler = Handler()
    private var payFor: String? = null

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
                finish()
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
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_order)
        binding?.pay?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        c2cList = intent.getParcelableExtra(ConstData.C2C_LIST)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
       TAB_WEIXIN = getString(R.string.wei_xin)
        chainNames = ArrayList()
        chainNames?.add(TAB_CARDS)
        chainNames?.add(TAB_IDPAY)
        chainNames?.add(TAB_WEIXIN)
        //chainNames?.add(TAB_PAYPAID)
        if (payChain == getString(R.string.wei_xin)){
            binding?.cardsColor?.visibility = View.GONE
            binding?.aliColor?.visibility =View.GONE
            binding?.weiXinColor?.visibility = View.VISIBLE
        }
        if (payChain == getString(R.string.cards)){
            binding?.cardsColor?.visibility = View.VISIBLE
            binding?.aliColor?.visibility =View.GONE
            binding?.weiXinColor?.visibility = View.GONE
        }
        if(payChain == getString(R.string.id_pay)){
            binding?.cardsColor?.visibility = View.GONE
            binding?.aliColor?.visibility =View.VISIBLE
            binding?.weiXinColor?.visibility = View.GONE
        }
      binding?.payName?.setText(payChain)
        countDownTimer
        getC2COrder()
    }


    override fun getTitleText(): String? {
        return getString(R.string.order_generated)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.pay){
            payChain = binding?.payName?.text?.toString()
            choosePayMethodWindow()
        }
        else if (id == R.id.btn_confirm){
            cancelDialog()
        }
        else if (id == R.id.btn_cancel) {
            payChain = binding?.payName?.text?.toString()
            val extras = Bundle()
            extras.putString(ConstData.COIN_TYPE,cointype)
            extras.putString(ConstData.C2C_ORDERS, payChain)
            extras.putLong(ConstData.C2C_TIME,TotalTime)
            BlackRouter.getInstance().build(RouterConstData.C2C_BUYER).with(extras).go(mContext)

        }
    }
    private fun checkClickable(){

    }
    private fun choosePayMethodWindow() {
        if (payChain != null && chainNames!!.size > 0) {
            val chooseWalletDialog = ChooseWalletControllerWindow(mContext as Activity,
                getString(R.string.choose_pay),
                payChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(
                        window: ChooseWalletControllerWindow<String?>,
                        item: String?
                    ) {
                        payChain = item
                        if (payChain == getString(R.string.wei_xin)){
                            binding?.cardsColor?.visibility = View.GONE
                            binding?.aliColor?.visibility =View.GONE
                            binding?.weiXinColor?.visibility = View.VISIBLE
                        }
                        if (payChain == getString(R.string.cards)){
                            binding?.cardsColor?.visibility = View.VISIBLE
                            binding?.aliColor?.visibility =View.GONE
                            binding?.weiXinColor?.visibility = View.GONE
                        }
                        if (payChain == getString(R.string.id_pay)){
                            binding?.cardsColor?.visibility = View.GONE
                            binding?.aliColor?.visibility =View.VISIBLE
                            binding?.weiXinColor?.visibility = View.GONE
                        }
                        binding?.payName?.setText(payChain)
                    }
                })
            chooseWalletDialog.show()
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
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            dialog.dismiss()
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.range).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked = dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked == false
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            if(dialog.findViewById<SpanCheckedTextView>(R.id.range).isChecked){
                getC2cCancel()
            }
            else{
                FryingUtil.showToast(mContext,"请先确认是否付款给卖方")
            }
        }
    }
    //下单获取信息
    private fun getC2COrder() {
        val id = intent.getStringExtra(ConstData.COIN_TYPE)
        val price = intent.getDoubleExtra(ConstData.BIRTH,0.0)
        val amount = intent.getDoubleExtra(ConstData.BUY_PRICE,0.0)
        C2CApiServiceHelper.getC2COrder(mContext, id,amount,price,  object : NormalCallback<HttpRequestResultData<String?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val id = returnData.data
                    binding?.adId?.setText(id)
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
                    val intent = Intent(this@C2COrdersActivity, C2CNewActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}