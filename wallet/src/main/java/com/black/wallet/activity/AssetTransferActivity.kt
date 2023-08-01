package com.black.wallet.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.*
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
import com.black.base.model.wallet.CoinInfoType
import com.black.base.model.wallet.SupportAccount
import com.black.base.model.wallet.Wallet
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.ChooseCoinControllerWindow
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.widget.SpanTextView
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityAssetTransferBinding
import com.black.wallet.view.ZhangHuSelector
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

/**
 * 资产划转页面 对应WalletTransferActivity
 */
@Route(value = [RouterConstData.ASSET_TRANSFER])
class AssetTransferActivity : BaseActionBarActivity(), View.OnClickListener{
    companion object{
        var fromAccountType = "SPOT"
        var toAccountType = "CONTRACT"
    }
    private var binding: ActivityAssetTransferBinding? = null

    private var supportAccountData:ArrayList<SupportAccount?>? = null
    private var supportFromAccountData:ArrayList<SupportAccount?>? = null
    private var supportToAccountData:ArrayList<SupportAccount?>? = null
    private var supportCoinData:ArrayList<CanTransferCoin?>? = null
    private var showSupportCoin:Boolean? = false
    private var type : Boolean? = false
    private var configCoinInfoList:ArrayList<CoinInfoType?>? = null

    private var fromAccount:SupportAccount? = null
    private var toAccount:SupportAccount? = null
    private var selectedCoin:CanTransferCoin? = null

    private var coinCount:BigDecimal? = null
    private var userBalanceList:ArrayList<UserBalance?>? = null
    private var balance: String? = null
    private var tigerBalanceList:ArrayList<UserBalance?>? = null
    private var userBalance:UserBalance? = null
    private var kLineQuotaSelector: ZhangHuSelector? = null

    private var chooseCoinDialog:ChooseCoinControllerWindow<CanTransferCoin?>? = null


    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateComfirmTransferBtn()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_transfer)
       // binding?.relFrom?.setOnClickListener(this)
        binding?.relTo?.setOnClickListener(this)
        binding?.relChoose?.setOnClickListener(this)
        binding?.btnConfirmTransfer?.setOnClickListener(this)
        binding?.imgExchange?.setOnClickListener(this)
        binding?.editAmount?.addTextChangedListener(watcher)
        binding?.tvAll?.setOnClickListener(this)
        var actionBarRecord: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)
        updateComfirmTransferBtn()
        getUserBalance(true)
    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }


    override fun getTitleText(): String {
        return getString(R.string.exchange)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.rel_from -> {
                showWalletChooseDialog(fromAccountType)
                getUserBalance(false)
            }
            R.id.rel_to ->{
                Log.d("yiuyiuyui",toAccount?.name)
                showWalletChooseDialog(toAccountType)
                getUserBalance(false)
            }
            R.id.rel_choose ->{
                showSupportCoin = true
                getUserBalance(false)
                supportTransferCoin
            }
            R.id.img_exchange ->{
                exchange()
                getUserBalance(false)
            }
            R.id.tv_all ->{
                binding?.editAmount?.setText((BigDecimal(userBalance?.availableBalance?:"0") + BigDecimal(userBalance?.profit?:"0")).toString())
            }
            R.id.btn_confirm_transfer -> doTransfer
            R.id.img_action_bar_right ->{
                val bundle = Bundle()
                var pair = selectedCoin?.coin
                bundle.putString(ConstData.PAIR, pair)
                bundle.putParcelableArrayList(ConstData.ASSET_SUPPORT_SPOT_ACCOUNT_TYPE,supportFromAccountData)
                bundle.putParcelableArrayList(ConstData.ASSET_SUPPORT_OTHER_ACCOUNT_TYPE,supportToAccountData)
                BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER_RECORD).with(bundle).go(this)
            }
        }
    }

    private fun exchange(){
        if(supportFromAccountData != null && supportToAccountData != null){
            var tem1 = supportFromAccountData
            var tem2 = supportToAccountData
            supportFromAccountData = tem2
            supportToAccountData = tem1
        }
        if(fromAccount != null && toAccount != null){
            var tem1 = fromAccount
            var tem2 = toAccount
            fromAccount = tem2
            toAccount = tem1
            binding?.tvFromAccount?.text = fromAccount?.name
            binding?.tvToAccount?.text = toAccount?.name
        }
        selectedCoin = null
        userBalance = null
        updateSelectCoinInfo(selectedCoin,userBalance)
        updateComfirmTransferBtn()
    }

    override fun onResume() {
        super.onResume()
        supportAccount
        supportTransferCoin
        allCoinList
        getUserBalance(false)
        kLineQuotaSelector = ZhangHuSelector(this)
        kLineQuotaSelector?.setOnKLineQuotaSelectorListener(object : ZhangHuSelector.OnKLineQuotaSelectorListener {
            override fun onSelect(type: SupportAccount?) {
                binding?.shangla?.visibility = View.GONE
                binding?.imgTo?.visibility = View.VISIBLE
                toAccount = type
                toAccount?.selected = true
                binding?.tvToAccount?.text = toAccount?.name
            }


        })
    }

    private val supportAccount: Unit
        get() {
            WalletApiServiceHelper.getSupportAccount(this, false, object : NormalCallback<HttpRequestResultData<AssetTransferTypeList?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultData<AssetTransferTypeList?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var result = returnData.data
                       initAccountData(result)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                    }
                }
            })
        }

    private fun getUserBalance(isShowLoading: Boolean ){
        WalletApiServiceHelper.getUserBalance(this)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : Callback<HttpRequestResultData<UserBalanceWarpper?>?>() {
                override fun error(type: Int, error: Any) {
                }
                override fun callback(returnData: HttpRequestResultData<UserBalanceWarpper?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {

                        if (binding?.tvFromAccount?.text == getString(R.string.spot_account) && binding?.tvToAccount?.text == getString(R.string.contract_account)){
                            userBalanceList = returnData.data?.spotBalance
                        }
                        else if (binding?.tvFromAccount?.text == getString(R.string.contract_account) && binding?.tvToAccount?.text == getString(R.string.spot_account)){
                            userBalanceList = returnData.data?.tigerBalance
                        }
                        else {
                            userBalanceList = returnData.data?.spotBalance
                        }
                    }
                }
            }))
    }

    private val supportTransferCoin:Unit
        get() {
            val from = fromAccount?.type?.lowercase()
            val to = toAccount?.type?.lowercase()
            if (to != null && from != null) {
                    WalletApiServiceHelper.getSupportTransferCoin(this,  from, to,true, object : NormalCallback<HttpRequestResultDataList<CanTransferCoin?>?>(mContext) {
                        override fun callback(returnData: HttpRequestResultDataList<CanTransferCoin?>?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                val result = returnData.data
                                supportCoinData = result
                                if(supportCoinData != null && supportCoinData?.size!! > 0){
                                    if(showSupportCoin == true){
                                        val result = getNeedCoinInfo()
                                        showCoinChooseDialog(result)
                                        showSupportCoin = false
                                    }
                                }else{
                                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.no_transfer_coin) else returnData.msg)
                                }
                            } else {
                                FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                            }
                        }
                    })
            }
        }

    /**
     * 根据获取到的支持币种，然后去拼需要显示的数据
     * 遍历所有币种跟钱包余额
     */
    private fun getNeedCoinInfo():ArrayList<CanTransferCoin?>?{
        val canTransferCoin:ArrayList<CanTransferCoin?>? = ArrayList()
        for(i in configCoinInfoList?.indices!!){
            val coinInfo = configCoinInfoList!![i]
            for (j in supportCoinData?.indices!!){
                val supportCoinInfo = supportCoinData!![j]
                if(coinInfo?.coinType.equals(supportCoinInfo?.coin)){
                    supportCoinInfo?.coinDes = coinInfo?.config?.get(0)?.coinConfigVO?.coinFullName
                    supportCoinInfo?.coinIconUrl = coinInfo?.config?.get(0)?.coinConfigVO?.logosUrl
                    canTransferCoin?.add(supportCoinInfo)
                    break
                }

            }

        }

        val result:ArrayList<CanTransferCoin?>? = ArrayList()
        for (k in canTransferCoin?.indices!!){
             val canTransferCoin = canTransferCoin[k]
            for (h in userBalanceList?.indices!!){
                val balance = userBalanceList!![h]
                if(canTransferCoin?.coin.equals(balance?.coin)){
                    canTransferCoin?.amount = (BigDecimal(balance?.availableBalance?:"0") ).toString()
                    result?.add(canTransferCoin)
                    break
                }
            }

        }


        return result
    }

    private val allCoinList:Unit
            get() {
                WalletApiServiceHelper.getCoinInfoList(this, object :Callback<ArrayList<CoinInfoType?>?>(){
                    override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                        configCoinInfoList = returnData
                    }
                    override fun error(type: Int, error: Any?) {
                    }
                })
            }


    private val doTransfer:Unit
        get() {
            val inputValue = binding?.editAmount?.text.toString()
            if(TextUtils.isEmpty(inputValue)){
                FryingUtil.showToast(this,getString(R.string.input_amount))
                return
            }
            if(fromAccount == null || toAccount == null){
                FryingUtil.showToast(this,getString(R.string.select_account))
                return
            }
            coinCount = BigDecimal(binding?.editAmount?.text.toString())
            val transferCoin = AssetTransfer()
            transferCoin.amount = coinCount
            transferCoin.coin = selectedCoin?.coin
            transferCoin.fromWalletType = fromAccount?.type?.lowercase()
            transferCoin.toWalletType = toAccount?.type?.lowercase()
            WalletApiServiceHelper.doTransfer(this, transferCoin,true, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        //FryingUtil.showToast(mContext, getString(R.string.transfer_succ))
                        successDialog()
                    } else {
                        //FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                        failedDialog(returnData?.msg)
                    }
                }
            })
        }

    private fun initAccountData(supportAccount:AssetTransferTypeList?){
        val spot = supportAccount?.mainName
        val transName = supportAccount?.transferName
        if(supportFromAccountData == null){
            supportFromAccountData = ArrayList()
        }else{
            supportFromAccountData?.clear()
        }
        if(supportToAccountData == null){
            supportToAccountData = ArrayList()
        }else{
            supportToAccountData?.clear()
        }
        if (supportAccountData == null){
            supportAccountData = ArrayList()
        }else{
            supportAccountData?.clear()
        }
        if (spot != null) {
            for (i in spot){
                var initFromData =  SupportAccount("FINANCIAL",getString(R.string.capital_account),false)
                initFromData.type = i
                when(i){
                    "SPOT" -> initFromData.name = getString(R.string.spot_account)
                }
                supportFromAccountData?.add(initFromData)
                supportAccountData?.add(initFromData)
            }
        }
        if(transName != null){
            for (j in transName){
                var initToData =  SupportAccount("FINANCIAL",getString(R.string.capital_account),false)
                initToData.type = j
                when(j){
                    "CONTRACT" -> initToData.name = getString(R.string.contract_account)
                    "OTC" -> initToData.name = getString(R.string.otc_account)
                }
                supportToAccountData?.add(initToData)
                supportAccountData?.add(initToData)
            }
        }
        if(fromAccount == null){
            fromAccount = supportFromAccountData!![0]
            fromAccount?.selected = true
        }
        if (toAccount == null){
            toAccount = supportToAccountData!![0]
            toAccount?.selected = true
        }
        binding?.tvFromAccount?.text = fromAccount?.name
        binding?.tvToAccount?.text = toAccount?.name
    }

    private fun showWalletChooseDialog(accountType:String){
        var clickAccout:SupportAccount? = null
        var accountData:ArrayList<SupportAccount?>? = null
        when(accountType){
            fromAccountType -> {
                clickAccout = fromAccount
                accountData = supportFromAccountData
            }
            toAccountType -> {
                clickAccout = toAccount
                accountData = supportToAccountData
            }
        }
        binding?.shangla?.visibility = View.VISIBLE
        binding?.imgTo?.visibility = View.GONE
        kLineQuotaSelector!!.show(binding?.tvToAccount,clickAccout, accountData!!)
        /*ChooseWalletControllerWindow(mContext as Activity, getString(R.string.select_wallet),clickAccout,
            accountData,
            object : ChooseWalletControllerWindow.OnReturnListener<SupportAccount?> {
                override fun onReturn(window: ChooseWalletControllerWindow<SupportAccount?>, item: SupportAccount?) {
                    when(accountType){
                        fromAccountType -> {
                            fromAccount = item
                            fromAccount?.selected = true
                            binding?.tvFromAccount?.text = fromAccount?.name
                        }
                        toAccountType -> {
                            toAccount = item
                            toAccount?.selected = true
                            binding?.tvToAccount?.text = toAccount?.name
                        }
                    }
                }
            }).show(
        */
    }

    private fun getSelectedCoinInfo(selectedCoin:CanTransferCoin?):UserBalance?{
        if(userBalanceList != null && userBalanceList?.size!! > 0){
            for (item in userBalanceList!!){
                if(item?.coin.equals(selectedCoin?.coin)){
                    return item
                }
            }
        }
        return null
    }

    private fun search(adapter: ChooseCoinControllerWindow<CanTransferCoin?>.ChooseCoinListAdapter?, data:ArrayList<CanTransferCoin?>?, searchKey: String?) {
        var result: ArrayList<CanTransferCoin?>? = ArrayList()
        if (searchKey == null || searchKey.trim { it <= ' ' }.isEmpty()) {
            result = data
        } else {
            for (coin in data!!) {
                if (coin?.coin != null && coin?.coin!!.uppercase(Locale.getDefault()).trim { it <= ' ' }.contains(
                        searchKey.uppercase(Locale.getDefault())
                    )) {
                    result!!.add(coin)
                }
            }
        }
        if (result != null) {
            Collections.sort(result, CanTransferCoin.COMPARATOR_CHOOSE_COIN)
        }
        adapter?.setData(result)
        adapter?.notifyDataSetChanged()
    }

    private fun updateComfirmTransferBtn(){
        binding?.btnConfirmTransfer?.isEnabled = (
                fromAccount != null && toAccount != null && selectedCoin != null && !TextUtils.isEmpty(binding!!.editAmount.text.toString().trim { it <= ' ' })
                )
    }

    private fun updateSelectCoinInfo(selectedCoin: CanTransferCoin?,userBalance: UserBalance?){
        binding?.tvChooseName?.text = if(selectedCoin == null){
            getString(R.string.coin_choose)
        }else selectedCoin.coin

        binding?.tvName?.text = if(selectedCoin == null){
            "-"
        }else selectedCoin.coin
        val logoView = binding?.imgIcon
        if (logoView != null) {
            Glide.with(mContext)
                .load(Uri.parse(UrlConfig.getCoinIconUrl(mContext,selectedCoin?.coinIconUrl)))
                .apply(RequestOptions().error(com.black.base.R.drawable.icon_coin_default))
                .into(logoView)
        }
        //BigDecimal(userBalance.profit?:"0")
        val maxtCoin = if(userBalance != null)(BigDecimal(userBalance.availableBalance?:"0") ).toString() + " "+userBalance.coin else{
            "0.00"
        }
        binding?.maxTransfer?.text = maxtCoin
        binding?.editAmount?.setText("")
    }

    private fun showCoinChooseDialog(data:ArrayList<CanTransferCoin?>?){
            chooseCoinDialog = ChooseCoinControllerWindow(mContext as Activity, getString(R.string.select_coin),
                data,
                object : ChooseCoinControllerWindow.OnReturnListener<CanTransferCoin?> {
                    override fun onReturn(window: ChooseCoinControllerWindow<CanTransferCoin?>, item: CanTransferCoin?) {
                        CookieUtil.addCoinSearchHistory(mContext, item?.coin)
                        selectedCoin = item
                        userBalance = getSelectedCoinInfo(selectedCoin)
                        updateSelectCoinInfo(selectedCoin,userBalance)
                    }

                    override fun onSearch(window: ChooseCoinControllerWindow<CanTransferCoin?>,searchKey: String?) {
                        var adapter =  window.getAdapter()
                        search(adapter,data,searchKey)
                    }
                })
            chooseCoinDialog?.show()
    }



    private fun successDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.tixian_succcess, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
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
        dialog.findViewById<View>(R.id.amount).visibility = View.GONE
        dialog.findViewById<SpanTextView>(R.id.successed).text = "划转成功，是否立即交易"
        dialog.findViewById<SpanTextView>(R.id.red_up).text = "去交易"
        dialog.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            BlackRouter.getInstance().build(RouterConstData.TRANSACTION).go(mContext)
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.green_up).setOnClickListener { v ->
            finish()
            dialog.dismiss()
        }
    }

    private fun failedDialog(data:String?) {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.tixian_failed, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
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
        dialog.findViewById<View>(R.id.amount).visibility = View.GONE
        dialog.findViewById<SpanTextView>(R.id.failed).text = data
        dialog.findViewById<SpanTextView>(R.id.red_up).text = "去充值"
        dialog.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            BlackRouter.getInstance().build(RouterConstData.TRANSACTION).go(mContext)
            dialog.dismiss()
            /* bundle.putString(ConstData.TITLE, getString(R.string.finance_account))
             bundle.putString(ConstData.URL, UrlConfig.getSupportsUrl(mContext!!))
             BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)

             */
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.green_up).setOnClickListener { v ->
            finish()
            dialog.dismiss()
        }
    }
}