package com.black.frying.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.*
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.base.view.DeepControllerWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityThreePaymentBinding
import kotlinx.android.synthetic.main.activity_three_payment.*


@Route(value = [RouterConstData.THREEPAYMENT])
class ThirdPayment: BaseActivity(), View.OnClickListener{
    private var binding: ActivityThreePaymentBinding? = null
    private var type = "buy"
    private var bank: String? = null
    private var desc: String? = null
    private var list: ArrayList<payOrder?>? = null
    private var coinCode: OrderCoin? = null
    private var userBalanceList: ArrayList<UserBalance?>? = null
    private var orderCode: ArrayList<OrderCode>? = null
    private var payCode: ArrayList<OrderCode>? = null
    private var dataList: ArrayList<Deposit?>? = null
    private var list1: MutableList<String>? = null
    private var list2: MutableList<String>? = null
    private var list3: MutableList<String>? = null
    private var list4: MutableList<String>? = null
    private var list5: MutableList<String>? = null
    private var i: Int = 0
    private var payChain1 = "-"
    private var payChain2 = "-"
    private var payChain3 = "-"
    private var payChain4 = "-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_three_payment)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.chooseCoinLayout?.setOnClickListener(this)
        binding?.chooseChainLayout?.setOnClickListener(this)
        binding?.chooseLayout?.setOnClickListener(this)
        binding?.country?.setOnClickListener(this)
        binding?.sell?.setOnClickListener(this)
        binding?.buy?.setOnClickListener(this)
        binding?.transferAmount?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)
        binding?.withdrawAddrLayout?.setOnClickListener(this)
        val actionBarRecord: ImageButton? = binding?.root?.findViewById(com.black.wallet.R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)
        type = if (binding?.buy?.isChecked == true) "buy" else "sell"
        list1 = ArrayList()
        list2 = ArrayList()
        list2?.add("USDT")
        list3 = ArrayList()
        list4 = ArrayList()
        list5 = ArrayList()
        getUrl()
    }

    override fun onResume() {
        super.onResume()
        getUserBalance(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.buy_sell)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btn_confirm -> {
                val name = binding?.phoneCode?.text?.trim{it <= ' '}.toString()
                if (TextUtils.isEmpty(name))
                    FryingUtil.showToast(mContext ,"Please put amount")
                else {
                    getDepositCreate("B" , null, null ,name)
                }
            }

            R.id.btn_cancel -> {
                val account = binding?.account?.text?.trim{it <= ' '}.toString()
                val name = binding?.name?.text?.trim{it <= ' '}.toString()
                val amount = binding?.phoneCode?.text?.trim {  it <= ' ' }.toString()
                if (TextUtils.isEmpty(account) || TextUtils.isEmpty(name) || TextUtils.isEmpty(amount))
                    FryingUtil.showToast(mContext ,"Please put amount or account or name")
                else {
                    getDepositCreate("S" ,account , name ,amount)
                }
            }

            R.id.country -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.country_area), payChain4 , list5, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        payChain4 = item
                        for (h in list5!!.indices){
                            if (payChain4 == list5!![h])
                                i = h
                        }
                        showData()
                        binding?.country?.setText(item)

                    }

                }).show()
            }

            R.id.choose_chain_layout -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.will_receive), payChain1 , list1, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        //payChain1 = item
                        // binding?.currentCoin?.setText(item)

                    }

                }).show()
            }

            R.id.choose_coin_layout -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.rechange), payChain1 , list1, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        //payChain1 = item
                        //binding?.currentChain?.setText(item)

                    }

                }).show()
            }

            R.id.withdraw_addr_layout -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.payment_methods), payChain2 , list3, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        payChain2 = item
                        binding?.extractAddress?.setText(item)

                    }

                }).show()
            }

            R.id.choose_layout -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.choose_rade), payChain3 , list4, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        payChain3 = item
                        binding?.chooseAddress?.setText(item)

                    }

                }).show()
            }

            R.id.img_action_bar_right -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.WALLET_HANDLE_TYPE, if (type == "buy") 0 else 1)
                BlackRouter.getInstance().build(RouterConstData.CHOOSEPAYMENT).with(bundle).go(mContext)
            }

            R.id.buy -> {
                type = "buy"
                refresh(type)

            }

            R.id.sell -> {
                type = "sell"
                refresh(type)
            }

            R.id.transfer_amount -> {
                BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
            }

        }
        }

    private fun refresh(type: String){
        when(type){
            "buy" -> {
                binding?.sell?.isChecked = false
                binding?.buy?.isChecked = true
                binding?.barA?.visibility = View.VISIBLE
                binding?.barB?.visibility = View.GONE
                binding?.wantBuy?.visibility = View.VISIBLE
                binding?.wantSell?.visibility = View.GONE
                binding?.willBuy?.visibility = View.VISIBLE
                binding?.willSell?.visibility = View.GONE
                binding?.totalAmount?.visibility = View.GONE
                binding?.btnConfirm?.visibility = View.VISIBLE
                binding?.btnCancel?.visibility = View.GONE
                binding?.user?.visibility = View.GONE
                binding?.currentCoin?.setText(dataList!![i]?.coinCode?.ccyNo!![0])
                binding?.currentChain?.setText("USDT")
                binding?.chooseChainLayout?.isEnabled = false
                binding?.chooseCoinLayout?.isEnabled = true
            }
            "sell" -> {
                binding?.buy?.isChecked = false
                binding?.sell?.isChecked = true
                binding?.barB?.visibility = View.VISIBLE
                binding?.barA?.visibility = View.GONE
                binding?.wantSell?.visibility = View.VISIBLE
                binding?.wantBuy?.visibility = View.GONE
                binding?.willSell?.visibility = View.VISIBLE
                binding?.willBuy?.visibility = View.GONE
                binding?.totalAmount?.visibility = View.VISIBLE
                binding?.btnConfirm?.visibility = View.GONE
                binding?.btnCancel?.visibility = View.VISIBLE
                binding?.user?.visibility = View.VISIBLE
                binding?.currentCoin?.setText("USDT")
                binding?.currentChain?.setText(dataList!![i]?.coinCode?.ccyNo!![0])
                binding?.chooseChainLayout?.isEnabled = true
                binding?.chooseCoinLayout?.isEnabled = false
            }
        }
    }
    private fun getUrl(){
        WalletApiServiceHelper.getDepositOrderCodeList(mContext, object : NormalCallback<HttpRequestResultData<PagingData<Deposit?>?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<Deposit?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    dataList = returnData.data?.list
                    showData()
                    refresh(type)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }


    private fun showData(){
        list5?.clear()
        list4?.clear()
        list3?.clear()
        list1?.clear()
        orderCode = dataList!![i]?.bankCode
        coinCode = dataList!![i]?.coinCode
        payCode = dataList!![i]?.payCode
        for (h in dataList!!.indices) {
            list5?.add(dataList!![h]?.country!!)
        }
        for (h in orderCode?.indices!!) {
            list4?.add(orderCode!![h].desc!!)
        }
        for (h in payCode?.indices!!) {
            list3?.add(payCode!![h].desc!!)
        }
            list1?.add(coinCode?.ccyNo!![0])
        payChain1 = list1!![0]
        payChain2 = list3!![0]
        payChain3 = list4!![0]
        payChain4 = list5!![i]
        binding?.extractAddress?.setText(payChain2)
        binding?.country?.setText(payChain4)
        binding?.chooseAddress?.setText(payChain3)
    }

    private fun getDepositCreate(type: String?,account: String? , name: String? , amount: String?) {
        val payVO = PayVO()
        payVO.accName = name
        payVO.accNo = account
        payVO.orderType = type
        payVO.orderAmount = amount
        bank = binding?.chooseAddress?.text?.trim { it <= ' ' }.toString()
        for (h in orderCode?.indices!!) {
            if (bank == orderCode!![h].desc) {
                payVO.bankCode = orderCode!![h].code
            }
        }
        desc = binding?.extractAddress?.text?.trim { it <= ' ' }.toString()
        for (h in payCode?.indices!!) {
            if (desc == payCode!![h].desc) {
                payVO.busiCode = payCode!![h].code
            }
        }
        if (type == "B") {
            payVO.ccyNo = dataList!![i]?.coinCode?.ccyNo!![0]
            payVO.coin = "USDT" }
        else{
            payVO.ccyNo = "USDT"
            payVO.coin = dataList!![i]?.coinCode?.ccyNo!![0]
        }
            WalletApiServiceHelper.getDepositCreate(
                mContext,
                payVO,
                object : NormalCallback<HttpRequestResultData<payOrder?>?>(mContext) {
                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                    }

                    override fun callback(returnData: HttpRequestResultData<payOrder?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            val bundle = Bundle()
                            val order = returnData.data
                            bundle.putString(ConstData.BIRTH, bank)
                            bundle.putString(ConstData.TITLE, type)
                            bundle.putParcelable(ConstData.WALLET, order)
                            BlackRouter.getInstance().build(RouterConstData.PAYMENTDETAILS)
                                .with(bundle).go(mContext)
                            finish()
                        } else {
                            FryingUtil.showToast(
                                mContext,
                                if (returnData == null) "null" else returnData.msg
                            )
                        }
                    }
                })
    }

    //获得现货usdt资产
    private fun getUserBalance(isShowLoading: Boolean ){
        WalletApiServiceHelper.getUserBalance(this)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : Callback<HttpRequestResultData<UserBalanceWarpper?>?>() {
                override fun error(type: Int, error: Any) {
                }
                override fun callback(returnData: HttpRequestResultData<UserBalanceWarpper?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            userBalanceList = returnData.data?.spotBalance
                        for (h in userBalanceList?.indices!!) {
                            val amount = userBalanceList!![h]
                            if("USDT" == amount?.coin) {
                                binding?.moneyAccount?.setText(String.format(" ≈ %S ", NumberUtil.formatNumberDynamicScaleNoGroup(amount.availableBalance?.toDouble(), 8, 2, 2) ) + "USDT")
                            }
                        }
                    }
                }
            }))
    }

}