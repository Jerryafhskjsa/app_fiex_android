package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.model.c2c.OrderConfig
import com.black.base.model.c2c.PayInfo
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.view.DeepControllerWindow
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cOldBinding
import com.black.c2c.fragment.C2CCustomerFragment
import com.black.c2c.fragment.C2COneKeyFragment
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.C2C_QIULK])
class C2CQiulkActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
        private var TAB_SELF: String? = null
        private var TAB_QUCILK: String? = null

    }

    private var binding: ActivityC2cOldBinding? = null
    private var dataList:OrderConfig? = null
    private var c2cList: C2CMainAD? = null
    private var type = 0
    private var currencyCoin = "CNY"
    private var rate = C2CApiServiceHelper.coinUsdtPrice?.usdt
    private var tab = TAB_QUCILK
    private var ciontype = "USDT"
    private var payChain: String? = null
    private var fManager: FragmentManager? = null
    private var typeList: MutableList<String>? = null
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
        if (rate == null)
        {
            binding?.refreshLayout?.setRefreshing(false)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_old)
        binding?.c2cOneKey?.setOnClickListener(this)
        binding?.c2cCustomer?.setOnClickListener(this)
        binding?.one?.setOnClickListener(this)
        binding?.two?.setOnClickListener(this)
        binding?.three?.setOnClickListener(this)
        binding?.four?.setOnClickListener(this)
        binding?.five?.setOnClickListener(this)
        binding?.six?.setOnClickListener(this)
        binding?.sven?.setOnClickListener(this)
        binding?.first?.setOnClickListener(this)
        binding?.second?.setOnClickListener(this)
        binding?.third?.setOnClickListener(this)
        binding?.fourth?.setOnClickListener(this)
        binding?.fifth?.setOnClickListener(this)
        binding?.sixth?.setOnClickListener(this)
        binding?.areaChoose?.setOnClickListener(this)
        binding?.bills?.setOnClickListener(this)
        binding?.rate?.setOnClickListener(this)
        binding?.settings?.setOnClickListener(this)
        binding?.person?.setOnClickListener(this)
        binding?.idPayLayout?.setOnClickListener(this)
        binding?.weiXinLayout?.setOnClickListener(this)
        binding?.cardsLayout?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.quilkPoint?.setOnClickListener(this)
        binding?.accont?.setOnClickListener(this)
        binding?.amount?.setOnClickListener(this)
        binding?.btnConfirmSale?.setOnClickListener(this)
        binding?.quilkPointSale?.setOnClickListener(this)
        binding?.adviceNull?.setOnClickListener(this)
        binding?.btnConfirmSale?.setOnClickListener(this)
        binding?.putMoney?.addTextChangedListener(watcher)
        binding?.moneyAccount?.addTextChangedListener(watcher)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)
        TAB_QUCILK = getString(R.string.shortcut_area)
        TAB_SELF = getString(R.string.self_selection_area)
        payChain = TAB_CARDS
        fManager = supportFragmentManager
        typeList = ArrayList()
        typeList?.add(TAB_SELF!!)
        typeList?.add(TAB_QUCILK!!)
        chainNames?.clear()
        chainNames = ArrayList()
        chainNames?.add(TAB_CARDS)
        chainNames?.add(TAB_IDPAY)
        chainNames?.add(TAB_WEIXIN)
        // chainNames?.add(TAB_PAYPAID)
        checkClickable()
        getC2CADData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }


    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.c2c_one_key) {
            type = 0
            refresh(type)
            checkClickable()
            // selectTab(TAB_ONE_KEY)
        } else if (id == R.id.c2c_customer) {
            type = 1
            refresh(type)
            checkClickable()
            // selectTab(TAB_CUSTOMER)
        }
        else if (id == R.id.area_choose){
            DeepControllerWindow(mContext as Activity, null, tab , typeList, object : DeepControllerWindow.OnReturnListener<String> {
                override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                    window.dismiss()
                    tab = item
                    when(item){
                        TAB_SELF -> {
                            BlackRouter.getInstance().build(RouterConstData.C2C_NEW).go(mContext)
                        }
                    }
                }

            }).show()
        }
        else if (id == R.id.bills){
            BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
        }
        else if (id == R.id.person){
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }
        else if (id == R.id.btn_confirm){
            getC2CQuickSearch()
        }
        else if (id == R.id.btn_confirm_sale){
            getC2CQuickSearch()
        }
        else if (id == R.id.id_pay_layout){
            choosePayMethodWindow()
        }
        else if (id == R.id.cards_layout){
            choosePayMethodWindow()
        }
        else if (id == R.id.wei_xin_layout){
            choosePayMethodWindow()
        }
        else if (id == R.id.settings){
            settingsDialog()
        }

        else if (id == R.id.one){
            binding?.advice?.visibility = View.VISIBLE
            binding?.adviceNull?.visibility = View.GONE
            ciontype = binding?.one?.text.toString()
            rate = C2CApiServiceHelper.coinUsdtPrice?.usdt
            binding?.one?.isChecked = true
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
            checkClickable()
        }
        else if (id == R.id.two){
            binding?.advice?.visibility = View.VISIBLE
            binding?.adviceNull?.visibility = View.GONE
            ciontype = binding?.two?.text.toString()
            binding?.one?.isChecked = false
            binding?.two?.isChecked = true
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
            rate = C2CApiServiceHelper.coinUsdtPrice?.btc
            checkClickable()
        }
        else if (id == R.id.three){
            binding?.advice?.visibility = View.GONE
            binding?.adviceNull?.visibility = View.VISIBLE
            ciontype = binding?.three?.text.toString()
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = true
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
            checkClickable()
        }
        else if (id == R.id.four){
            binding?.advice?.visibility = View.GONE
            binding?.adviceNull?.visibility = View.VISIBLE
            ciontype = binding?.four?.text.toString()
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = true
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
            checkClickable()
        }
        else if (id == R.id.five){
            binding?.advice?.visibility = View.GONE
            binding?.adviceNull?.visibility = View.VISIBLE
            ciontype = binding?.five?.text.toString()
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = true
            binding?.six?.isChecked = false
            checkClickable()
        }
        else if (id == R.id.six){
            binding?.advice?.visibility = View.VISIBLE
            binding?.adviceNull?.visibility = View.GONE
            ciontype = binding?.six?.text.toString()
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = true
            rate = C2CApiServiceHelper.coinUsdtPrice?.eth
            checkClickable()
        }
        else if (id == R.id.sven){
        }
        else if (id == R.id.first){
            binding?.putMoney?.setText("100")
        }
        else if (id == R.id.second){
            binding?.putMoney?.setText("500")
        }
        else if (id == R.id.third){
            binding?.putMoney?.setText("1000")
        }
        else if (id == R.id.fourth){
            binding?.putMoney?.setText("2000")
        }
        else if (id == R.id.fifth){
            binding?.putMoney?.setText("5000")
        }
        else if (id == R.id.sixth){
            binding?.putMoney?.setText("10000")
        }
        else if (id == R.id.amount){
            binding?.change?.setText(getString(R.string.number_of_transactions))
            binding?.moneyAmount?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.amount?.isChecked = true
            binding?.accont?.isChecked = false
            binding?.money?.visibility = View.VISIBLE
            binding?.moneyAccount1?.visibility = View.GONE
            binding?.moneyAccount?.text = null
            checkClickable()
        }
        else if (id == R.id.accont){
            binding?.change?.setText(getString(R.string.total_price))
            binding?.moneyAmount?.visibility = View.GONE
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.amount?.isChecked = false
            binding?.accont?.isChecked = true
            binding?.money?.visibility = View.GONE
            binding?.putMoney?.text = null
            binding?.moneyAccount1?.visibility = View.VISIBLE
            checkClickable()
        }
        else if (id == R.id.quilk_point){
            quilkDialog()
        }
        else if (id == R.id.quilk_point_sale){
            quilkSaleDialog()
        }
    }

    private fun checkClickable() {
        if (binding?.three?.isChecked == true || binding?.four?.isChecked == true || binding?.five?.isChecked == true){
            binding?.advice?.visibility = View.GONE
            binding?.adviceNull?.visibility = View.VISIBLE
        }
        else{
            binding?.advice?.visibility = View.VISIBLE
            binding?.adviceNull?.visibility = View.GONE
        }
        binding?.rate2?.setText(rate.toString() + "CNY/" + ciontype)
        binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.moneyAccount?.text.toString().trim { it <= ' ' })
                && TextUtils.isEmpty(binding?.putMoney?.text.toString().trim { it <= ' ' })||binding?.three?.isChecked == true || binding?.four?.isChecked == true || binding?.five?.isChecked == true)
        binding?.btnConfirmSale?.isEnabled = !(TextUtils.isEmpty(binding?.moneyAccount?.text.toString().trim { it <= ' ' })
                && TextUtils.isEmpty(binding?.putMoney?.text.toString().trim { it <= ' ' })||binding?.three?.isChecked == true || binding?.four?.isChecked == true || binding?.five?.isChecked == true)
        if (binding?.amount?.isChecked == false) {
            val amount = CommonUtil.parseDouble(binding?.moneyAccount?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.changeTwo?.setText( "￥" +
                        NumberUtil.formatNumberNoGroup(
                            amount * rate!!,
                            4,
                            4
                        )
                )
            }
            else {
                binding?.changeTwo?.setText( "￥" )
            }

        }
        if (binding?.amount?.isChecked == true) {
            val amount =
                CommonUtil.parseDouble(binding?.putMoney?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.changeTwo?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount / rate!!,
                        4,
                        4
                    )
                )
            }
            else
            {
                binding?.changeTwo?.setText(
                    "-"
                )
            }

        }
    }
    private fun refresh(type :Int){
        if (type == 0){
            binding?.moneyAmount?.visibility = View.VISIBLE
            binding?.c2cOneKey?.isChecked = false
            binding?.c2cCustomer?.isChecked = true
            binding?.quilkPointSale?.visibility = View.GONE
            binding?.btnConfirmSale?.visibility = View.GONE
            binding?.btnConfirm?.visibility = View.VISIBLE
            binding?.quilkPoint?.visibility = View.VISIBLE
            chainNames?.clear()
            chainNames = ArrayList()
            chainNames?.add(TAB_CARDS)
            chainNames?.add(TAB_IDPAY)
            chainNames?.add(TAB_WEIXIN)
        }
        else{
            binding?.moneyAmount?.visibility = View.GONE
            binding?.c2cOneKey?.isChecked = true
            binding?.c2cCustomer?.isChecked = false
            binding?.quilkPoint?.visibility = View.GONE
            binding?.btnConfirm?.visibility = View.GONE
            binding?.btnConfirmSale?.visibility = View.VISIBLE
            binding?.quilkPointSale?.visibility = View.VISIBLE
            chainNames?.clear()
            getAllPay()
        }
    }
    private fun settingsDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.bills_dialog, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.TOP
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 80
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.findViewById<SpanTextView>(R.id.merchant).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_APPLY1).go(this)
            dialog.dismiss()
        }
        dialog.findViewById<SpanTextView>(R.id.settings).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY).go(this)
            dialog.dismiss()
        }
        dialog.findViewById<SpanTextView>(R.id.btn_bills).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
            dialog.dismiss()
        }
        dialog.show()
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
                        when(payChain) {
                            TAB_CARDS -> {
                                binding?.cardsLayout?.visibility = View.VISIBLE
                                binding?.weiXinLayout?.visibility = View.GONE
                                binding?.idPayLayout?.visibility = View.GONE
                                binding?.cards?.setText(payChain)
                            }
                            TAB_IDPAY -> {
                                binding?.idPayLayout?.visibility = View.VISIBLE
                                binding?.weiXinLayout?.visibility = View.GONE
                                binding?.cardsLayout?.visibility = View.GONE
                                binding?.ali?.setText(payChain)
                            }
                             TAB_WEIXIN -> {
                                binding?.weiXinLayout?.visibility = View.VISIBLE
                                binding?.cardsLayout?.visibility = View.GONE
                                binding?.idPayLayout?.visibility = View.GONE
                                binding?.weiXin?.setText(payChain)
                            }
                        }

                    }
                })
            chooseWalletDialog.show()
        }

    }
    private fun quilkDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.quilk_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener  {
                v -> dialog.dismiss()
        }
    }
    private fun quilkSaleDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.quilk_sale_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener  {
                v -> dialog.dismiss()
        }
    }
    //快速下单配置
    private fun getC2CADData() {
        C2CApiServiceHelper.getC2CQuickOrder(mContext, ciontype,currencyCoin,  object : NormalCallback<HttpRequestResultData<OrderConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<OrderConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    dataList = returnData.data
                    showData(dataList)
                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
    //快速下单查询
    private fun getC2CQuickSearch() {
        getC2CADData()
        val gteAmount = if (binding?.amount?.isChecked == true)CommonUtil.parseDouble(binding?.changeTwo?.text.toString().trim { it <= ' ' })
        else CommonUtil.parseDouble(binding?.moneyAccount?.text.toString().trim { it <= ' ' })
        val gteCurrencyCoinAmount = dataList?.coinAmountMax?.toDouble()
        val direction = if (binding?.c2cCustomer?.isChecked == true) "B" else "S"
        val coinType = if (binding?.one?.isChecked == true) "USDT" else "BTC"
        val payMethod = if (binding?.cardsLayout?.visibility == View.VISIBLE) "[0]" else if (binding?.weiXinLayout?.visibility == View.VISIBLE)"[2]" else "[1]"
        C2CApiServiceHelper.getC2CQuickSearch(mContext,gteAmount,gteCurrencyCoinAmount, coinType ,direction,payMethod,  object : NormalCallback<HttpRequestResultData<C2CMainAD?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CMainAD?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    c2cList = returnData.data
                    getOrder(c2cList?.id , gteAmount , c2cList?.currentPrice)

                } else {

                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun getOrder(id: String? , amount: Double? ,price: Double?) {
        C2CApiServiceHelper.getC2COrder(
            mContext,
            id,
            amount,
            price,
            object : NormalCallback<HttpRequestResultData<String?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<String?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        val id2 = returnData.data
                        val item = if (binding?.ali?.visibility == View.VISIBLE)"0" else "" +
                                if (binding?.cards?.visibility == View.VISIBLE) "1" else "" +
                                        if (binding?.weiXin?.visibility == View.VISIBLE) "2" else ""
                        val extras = Bundle()
                        extras.putString(ConstData.PAIR, item)
                        extras.putParcelable(ConstData.C2C_LIST,c2cList)
                        extras.putString(ConstData.USER_YES ,payChain)
                        extras.putString(ConstData.COIN_TYPE,id2)
                        BlackRouter.getInstance().build(RouterConstData.C2C_ORDERS).with(extras).go(mContext)
                    } else {
                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                        finish()
                    }
                }
            })
    }
    private fun showData(dataList:OrderConfig?){
        binding?.putMoney?.setHint(dataList?.currencyCoinAmountMin?.toString() + "起")
        binding?.moneyAccount?.setHint("最大可售" + dataList?.coinAmountMax?.toString())
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
            if (dataList[i]?.type!! == 1 && binding?.ali?.visibility == View.VISIBLE) {
                TAB_IDPAY = TAB_IDPAY + "                         " + dataList[i]?.account
                chainNames?.add(TAB_IDPAY)
            } else if (dataList[i]?.type!! == 0 && binding?.cards?.visibility == View.VISIBLE) {
                TAB_CARDS = TAB_CARDS + "                         " + dataList[i]?.account
                chainNames?.add(TAB_CARDS + "                         " + dataList[i]?.account)
            } else if (dataList[i]?.type!! == 2 && binding?.weiXin?.visibility == View.VISIBLE) {
                TAB_WEIXIN = TAB_WEIXIN + "                         " + dataList[i]?.account
                chainNames?.add(TAB_WEIXIN + "                         " + dataList[i]?.account)
            }
        }
    }
}