package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.PayInfo
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cSellBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil

@Route(value = [RouterConstData.C2C_SELL])
class C2CSellActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
    }
    private var c2cList: C2CMainAD? = null
    private var binding: ActivityC2cSellBinding? = null
    private var cointype = "USDT"
    private var payChain: String? = null
    private var rate = C2CApiServiceHelper?.coinUsdtPrice?.usdt
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_sell)
        binding?.accont?.setOnClickListener(this)
        binding?.amount?.setOnClickListener(this)
        binding?.payTime?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.methodsLayout?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.putMoney?.addTextChangedListener(watcher)
        binding?.putAmount?.addTextChangedListener(watcher)
        binding?.btnConfirm?.setText(getString(R.string.sell) + cointype)
        getC2CADData()
        getAllPay()
        checkClickable()
    }
    override fun getTitleText(): String? {
        return getString(R.string.sell) + cointype
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm){
            val two = CommonUtil.parseDouble(binding?.two?.text.toString().trim { it <= ' ' })
            val xianMin = CommonUtil.parseDouble(binding?.xianMin?.text.toString().trim { it <= ' ' })
            val xianMax = CommonUtil.parseDouble(binding?.xianMax?.text.toString().trim { it <= ' ' })
            if (two != null && two >= xianMin!! && two <= xianMax!!){
                if (binding?.methodsLayout?.visibility == View.GONE) {
                    BlackRouter.getInstance().build(RouterConstData.C2C_WAITE1).go(mContext)
                }
                else{
                    getAllPay()
                    choosePayMethodWindowOld()
                }
            }
            else{
                FryingUtil.showToast(mContext, "请输入限额内的金额")
            }
        }
        else if (id == R.id.unit_price){
            getC2CADData()
        }
        else if (id == R.id.methods_layout){
            getAllPay()
            choosePayMethodWindowOld()
        }
        else if (id == R.id.pay_time){
            payTimeDialog()
        }
        else if (id == R.id.name){
            getC2CADData()
            val merchantId = c2cList?.merchantId
            val bundle = Bundle()
            bundle.putInt(ConstData.BIRTH, merchantId!!)
            BlackRouter.getInstance().build(RouterConstData.C2C_SELLER).with(bundle).go(mContext)
        }
        else if (id == R.id.amount){
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.amount?.isChecked = true
            binding?.accont?.isChecked = false
            binding?.money?.visibility = View.VISIBLE
            binding?.amountAll?.visibility = View.GONE
            checkClickable()
        }
        else if (id == R.id.accont){
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.amount?.isChecked = false
            binding?.accont?.isChecked = true
            binding?.money?.visibility = View.GONE
            binding?.amountAll?.visibility = View.VISIBLE
            checkClickable()
        }
    }
    private fun choosePayMethodWindowOld() {
        if (chainNames!!.size > 0) {
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
                        binding?.methodsLayout?.setText(payChain)
                        getC2COrder()
                         }
                })
            chooseWalletDialog.show()
        }
        else {
            choosePayMethodWindowNew()
        }

    }
    private fun choosePayMethodWindowNew() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.sell_dialog, null)
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
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY).go(this)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
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
    private fun checkClickable(){
        binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.putAmount?.text.toString().trim { it <= ' ' })
                && TextUtils.isEmpty(binding?.putMoney?.text.toString().trim { it <= ' ' })
                )
        rate =  CommonUtil.parseDouble(binding?.unitPrice?.text.toString().trim { it <= ' ' })
        binding?.unitPrice?.setText(rate.toString())
        if (binding?.amount?.isChecked == false) {
            var amount =
                CommonUtil.parseDouble(binding?.putAmount?.text.toString().trim { it <= ' ' })
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
        binding?.name?.setText(c2CMainAD?.realName)
        binding?.btnConfirm?.setText(getString(R.string.buy_02) + c2CMainAD?.coinType)
        val paymentTypeList = c2CMainAD?.payMethods
        if (paymentTypeList != null && paymentTypeList== "[0,1,2]") {
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.VISIBLE
            binding?.weiXin?.visibility = View.VISIBLE
        }
        if (paymentTypeList != null && paymentTypeList == "[1]") {
            binding?.cards?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[0]") {
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.GONE
            binding?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[2]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.cards?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList == "[0,2]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[0,1]") {
            binding?.weiXin?.visibility = View.GONE
            binding?.ali?.visibility = View.VISIBLE
            binding?.cards?.visibility = View.VISIBLE
        }
        if (paymentTypeList != null && paymentTypeList== "[1,2]") {
            binding?.weiXin?.visibility = View.VISIBLE
            binding?.ali?.visibility = View.GONE
            binding?.cards?.visibility = View.VISIBLE
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
    private fun getC2COrder(){
        val id = intent.getStringExtra(ConstData.PAIR)
        val num1 = CommonUtil.parseDouble(binding?.two?.text.toString().trim { it <= ' ' })
        val num2 = CommonUtil.parseDouble(binding?.unitPrice?.text.toString().trim { it <= ' ' })
        C2CApiServiceHelper.getC2COrder(
            mContext,
            id,
            num1,
            num2,
            object : NormalCallback<HttpRequestResultData<String?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<String?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        val id = returnData.data
                        val extras = Bundle()
                        extras.putString(ConstData.BUY_PRICE,id)
                        BlackRouter.getInstance().build(RouterConstData.C2C_WAITE1).with(extras).go(mContext)
                    } else {
                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }
    //获得用户收款方式
    private fun getAllPay(){
        C2CApiServiceHelper.getAllPay(mContext, object : NormalCallback<HttpRequestResultDataList<PayInfo?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }


            override fun callback(returnData: HttpRequestResultDataList<PayInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val dataList: ArrayList<PayInfo?>? = returnData.data
                    show(dataList)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun show(dataList: ArrayList<PayInfo?>?) {
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)
        chainNames = ArrayList()
        val num = dataList!!.size - 1
        for (i in 0..num) {
                if (dataList[i]?.type!! == 0 && binding?.ali?.visibility == View.VISIBLE) {
                    chainNames?.add(TAB_IDPAY + "                         " + dataList[i]?.account)
                } else if (dataList[i]?.type!! == 1 && binding?.cards?.visibility == View.VISIBLE) {
                    chainNames?.add(TAB_CARDS + "                         " + dataList[i]?.account)
                } else if (dataList[i]?.type!! == 2 && binding?.weiXin?.visibility == View.VISIBLE) {
                    chainNames?.add(TAB_WEIXIN + "                         " + dataList[i]?.account)
                }
            }
        }

}