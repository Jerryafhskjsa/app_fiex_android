package com.black.c2c.activity

import android.app.Activity
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
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.model.c2c.PayInfo
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.widget.SpanCheckedTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cOrderBinding
import com.black.c2c.util.C2CHandleCheckHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

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
    private var id2: String? = null
    private var item: String? = null
    private var payFor: Int? = null
    private var c2CHandleCheckHelper: C2CHandleCheckHelper? = null
    private var totalTime : Long = 15*60*1000 //总时长 15min
    private var countDownTimer: CountDownTimer? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_order)
        payChain = intent.getStringExtra(ConstData.USER_YES)
        id2 = intent.getStringExtra(ConstData.COIN_TYPE)
        item = intent.getStringExtra(ConstData.PAIR)
        binding?.pay?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.seller?.setOnClickListener(this)
        c2cList = intent.getParcelableExtra(ConstData.C2C_LIST)
        binding?.adId?.setText(id2)
        binding?.payName?.setText(payChain)
        c2CHandleCheckHelper = C2CHandleCheckHelper(mContext, BaseActionBarActivity(),fryingHelper)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)
        chainNames = ArrayList()
        if (item == "0") {
            chainNames?.add(TAB_CARDS)
        }
        else if (item == "1") {
            chainNames?.add(TAB_IDPAY)
        }
        else if (item == "2") {
            chainNames?.add(TAB_WEIXIN)
        }
        else if (item == "01") {
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_CARDS)
        }
        else if (item == "12") {
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_WEIXIN)
        }
        else if (item == "02") {
            chainNames?.add(TAB_WEIXIN)
            chainNames?.add(TAB_CARDS)
        }
        else {
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_CARDS)
            chainNames?.add(TAB_WEIXIN)
        }
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
        getC2COIV2(id2)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.order_generated)
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.pay){
            choosePayMethodWindow()
        }
        else if (id == R.id.btn_confirm){
            cancelDialog()
        }
        else if (id == R.id.seller){
            val extras = Bundle()
            payChain = binding?.payName?.text.toString()
            extras.putString(ConstData.BUY_PRICE, id2)
            extras.putString(ConstData.USER_YES, payChain)
            BlackRouter.getInstance().build(RouterConstData.C2C_BUYER).with(extras).go(mContext)
        }
        else if (id == R.id.btn_cancel) {
            val extras = Bundle()
            payChain = binding?.payName?.text.toString()
            extras.putString(ConstData.BUY_PRICE, id2)
            extras.putString(ConstData.USER_YES, payChain)
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY_FOR).with(extras).go(mContext)
            finish()
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
                getC2cCancel(id2)
            }
            else{
                FryingUtil.showToast(mContext,getString(R.string.queren_f))
            }
        }
    }


    //撤单
    private fun getC2cCancel(id: String?){

        C2CApiServiceHelper.getC2COrderCancel(mContext, id,  object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
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

    //订单详情
    fun getC2COIV2(id: String?){
        C2CApiServiceHelper.getC2CDetails(
            mContext,
            id,
            object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.coinType?.setText(returnData.data?.coinType)
                        binding?.account?.setText(returnData.data?.amount.toString() + returnData.data?.coinType)
                        binding?.price?.setText(returnData.data?.price.toString())
                        binding?.total?.setText((returnData.data?.amount!! * returnData.data?.price!!).toString())
                        val time = TimeUtil.getTime(returnData.data?.createTime)
                        binding?.createTime?.setText(time)
                        binding?.name?.setText(returnData.data?.otherSideRealName)
                        binding?.accountTotal?.setText(returnData.data?.otherSideCompletedOrders30Days.toString())
                        val time1 = returnData.data?.validTime?.time
                        val calendar: Calendar = Calendar.getInstance()
                        val time2 = calendar.time.time
                        totalTime = time1!!.minus(time2)
                        countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
                        override fun onFinish(){
                            binding?.time?.setText("00:00")
                            FryingUtil.showToast(mContext,getString(R.string.cancel1))
                        }
                            override fun onTick(millisUntilFinished: Long) {
                                val minute = millisUntilFinished / 1000 / 60 % 60
                                val second = millisUntilFinished / 1000 % 60
                                binding?.time?.setText("$minute:$second")
                            }
                        }.start()
                    } else {

                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }
}