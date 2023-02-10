package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.C2CADData
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import kotlinx.android.synthetic.main.activity_c2c_buy.*


@Route(value = [RouterConstData.C2C_BUY])
class C2CBuyActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
       // private var TAB_PAYPAID: String? = "PayPai"
    }
    private var binding: ActivityC2cBuyBinding? = null
    private var c2cList: C2CMainAD? = null
    private var cointype:String? = "USDT"
    private var payChain: String? = null
    private var rate :Double? = 0.0
    private var chainNames: MutableList<String?>? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buy)
        getC2CADData()
        binding?.accont?.setOnClickListener(this)
        binding?.accont?.setOnClickListener(this)
        binding?.amount?.setOnClickListener(this)
        binding?.payTime?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.unitPrice?.setOnClickListener(this)
        binding?.seller?.setOnClickListener(this)
        binding?.putMoney?.addTextChangedListener(watcher)
        binding?.putAmount?.addTextChangedListener(watcher)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)


        checkClickable()
    }
    override fun getTitleText(): String? {
        return getString(R.string.buy_02) + cointype
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm){
            val two = CommonUtil.parseDouble(binding?.two?.text.toString().trim { it <= ' ' })
            val xianMin = CommonUtil.parseDouble(binding?.xianMin?.text.toString().trim { it <= ' ' })
            val xianMax = CommonUtil.parseDouble(binding?.xianMax?.text.toString().trim { it <= ' ' })
            if (two != null && two >= xianMin!! && two <= xianMax!!){
            choosePayMethodWindow()
        }
            else{
                FryingUtil.showToast(mContext, "请输入限额内的金额")
            }
        }
        else if (id == R.id.unit_price){
        }
        else if (id == R.id.seller){
            BlackRouter.getInstance().build(RouterConstData.C2C_SELLER).go(mContext)
        }
        else if (id == R.id.pay_time){
            payTimeDialog()
        }
        else if (id == R.id.amount){
            binding?.putAmount?.text = null
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.amount?.isChecked = true
            binding?.accont?.isChecked = false
            binding?.money?.visibility = View.VISIBLE
            binding?.amountAll?.visibility = View.GONE
            checkClickable()
        }
        else if (id == R.id.accont){
            binding?.putMoney?.text = null
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.amount?.isChecked = false
            binding?.accont?.isChecked = true
            binding?.money?.visibility = View.GONE
            binding?.amountAll?.visibility = View.VISIBLE
            checkClickable()
        }
    }
    private fun choosePayMethodWindow() {
        getC2CADData()
        if (payChain != null && chainNames!!.size > 0) {
            val chooseWalletDialog = ChooseWalletControllerWindow(this as Activity,
                getString(R.string.choose_pay),
                payChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(
                        window: ChooseWalletControllerWindow<String?>,
                        item: String?
                    ) {
                        payChain = item
                        val num1 = CommonUtil.parseDouble(binding?.two?.text.toString().trim { it <= ' ' })
                        val num2 = CommonUtil.parseDouble(binding?.unitPrice?.text.toString().trim { it <= ' ' })
                        val num3 = c2cList?.id
                        val extras = Bundle()
                        extras.putParcelable(ConstData.C2C_LIST,c2cList)
                        extras.putDouble(ConstData.BUY_PRICE,num1!!)
                        extras.putDouble(ConstData.BIRTH,num2!!)
                        extras.putString(ConstData.COIN_TYPE,num3)
                        BlackRouter.getInstance().build(RouterConstData.C2C_ORDERS).with(extras).go(mContext)
                    }
                })
            chooseWalletDialog.show()
        }

    }
    private fun payTimeDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.pay_time_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 1f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 380
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }

    }
    private fun checkClickable() {
            binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.putAmount?.text.toString().trim { it <= ' ' })
                    && TextUtils.isEmpty(binding?.putMoney?.text.toString().trim { it <= ' ' })
                    )
        rate =  CommonUtil.parseDouble(binding?.unitPrice?.text.toString().trim { it <= ' ' })
        binding?.unitPrice?.setText(rate.toString())
        if (binding?.amount?.isChecked == false) {
             val amount = CommonUtil.parseDouble(binding?.putAmount?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount * rate!!,
                        4,
                        4
                    )
                )
            } else {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
            }

        }
        if (binding?.amount?.isChecked == true) {
            var amount =
                CommonUtil.parseDouble(binding?.putMoney?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount,
                        4,
                        4
                    )
                )
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount / rate!!,
                        4,
                        4
                    )
                )
            } else {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
            }

        }
    }
    private fun init(c2CMainAD: C2CMainAD?){
        binding?.totalJie?.setText(c2CMainAD?.completedOrders.toString())
        binding?.unitPrice?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CMainAD?.priceParam, 8, 2, 8)))
        binding?.xianMin?.setText(String.format("%s", NumberUtil.formatNumberNoGroup(c2CMainAD?.singleLimitMin )))
        binding?.xianMax?.setText(String.format("%s", NumberUtil.formatNumberNoGroup(c2CMainAD?.singleLimitMax )))
        binding?.seller?.setText(c2CMainAD?.realName)
        binding?.btnConfirm?.setText(getString(R.string.buy_02) + c2CMainAD?.coinType)
        val paymentTypeList = c2CMainAD?.payMethods
        if (paymentTypeList != null && paymentTypeList== "[1,2,3]") {
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.VISIBLE
            binding?.weiXin?.visibility = View.VISIBLE
            payChain = TAB_IDPAY
            chainNames = ArrayList()
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_CARDS)
            chainNames?.add(TAB_WEIXIN)
        }
        if (paymentTypeList != null && paymentTypeList == "[3]") {
            binding?.cards?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
            payChain = TAB_CARDS
            chainNames = ArrayList()
            chainNames?.add(TAB_CARDS)
        }
        if (paymentTypeList != null && paymentTypeList== "[1]") {
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
            payChain = TAB_IDPAY
            chainNames = ArrayList()
            chainNames?.add(TAB_IDPAY)
        }
        if (paymentTypeList != null && paymentTypeList== "[2]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.cards?.visibility = View.GONE
            payChain = TAB_WEIXIN
            chainNames = ArrayList()
            chainNames?.add(TAB_WEIXIN)
        }
        if (paymentTypeList != null && paymentTypeList == "[1,2]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.GONE
            payChain = TAB_IDPAY
            chainNames = ArrayList()
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_WEIXIN)
        }
        if (paymentTypeList != null && paymentTypeList== "[1,3]") {
            binding?.weiXin?.visibility = View.GONE
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.VISIBLE
            payChain = TAB_CARDS
            chainNames = ArrayList()
            chainNames?.add(TAB_CARDS)
            chainNames?.add(TAB_IDPAY)
        }
        if (paymentTypeList != null && paymentTypeList== "[2,3]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.cards?.visibility = View.VISIBLE
            payChain = TAB_WEIXIN
            chainNames = ArrayList()
            chainNames?.add(TAB_CARDS)
            chainNames?.add(TAB_WEIXIN)
        }
    }

    private fun getC2CADData() {
        val id = intent.getStringExtra(ConstData.PAIR)
        C2CApiServiceHelper.getC2CADID(mContext, id,  object : NormalCallback<HttpRequestResultData<C2CMainAD?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CMainAD?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    c2cList = returnData.data
                    init(c2cList)


                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}