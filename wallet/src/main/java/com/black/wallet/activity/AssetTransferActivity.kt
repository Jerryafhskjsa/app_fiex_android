package com.black.wallet.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
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
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.wallet.R
import com.black.wallet.databinding.ActivityAssetTransferBinding
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
    private var supportCoinData:ArrayList<CanTransferCoin?>? = null
    private var showSupportCoin:Boolean? = false

    private var fromAccount:SupportAccount? = null
    private var toAccount:SupportAccount? = null
    private var selectedCoin:CanTransferCoin? = null

    private var coinCount:BigDecimal? = null
    private var userBalanceList:ArrayList<UserBalance?>? = null
    private var userBalance:UserBalance? = null

    private var chooseCoinDialog:ChooseCoinControllerWindow<CanTransferCoin?>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_transfer)
        binding?.relFrom?.setOnClickListener(this)
        binding?.relTo?.setOnClickListener(this)
        binding?.relChoose?.setOnClickListener(this)
        binding?.btnConfirmTransfer?.setOnClickListener(this)
        binding?.imgExchange?.setOnClickListener(this)
        binding?.tvAll?.setOnClickListener(this)
        var actionBarRecord: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }


    override fun getTitleText(): String {
        return getString(R.string.asset_transfer)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.rel_from -> {
                showWalletChooseDialog(fromAccountType)
            }
            R.id.rel_to ->{
                showWalletChooseDialog(toAccountType)
            }
            R.id.rel_choose ->{
                showSupportCoin = true
                supportTransferCoin
            }
            R.id.img_exchange ->{
                exchange()
            }
            R.id.tv_all ->{
                binding?.editAmount?.setText(userBalance?.availableBalance)
            }
            R.id.btn_confirm_transfer -> doTransfer
            R.id.img_action_bar_right ->{
                val bundle = Bundle()
                var pair = selectedCoin?.coin
                bundle.putString(ConstData.PAIR, pair)
                bundle.putParcelableArrayList(ConstData.ASSET_SUPPORT_ACCOUNT_TYPE,supportAccountData)
                BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER_RECORD).with(bundle).go(this)
            }
        }
    }

    private fun exchange(){
        if(fromAccount != null && toAccount != null){
            var tem1 = fromAccount
            var tem2 = toAccount
            fromAccount = tem2
            toAccount = tem1
            binding?.tvFromAccount?.text = fromAccount?.name
            binding?.tvToAccount?.text = toAccount?.name
        }
    }

    override fun onResume() {
        super.onResume()
        supportAccount
        supportTransferCoin
        getUserBalance(false)
    }

    private val supportAccount: Unit
        get() {
            WalletApiServiceHelper.getSupportAccount(this, false, object : NormalCallback<HttpRequestResultDataList<String?>?>() {
                override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var result = returnData.data
                        supportAccountData =  initAccountData(result)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                    }
                }
            })
        }

    private fun getUserBalance(isShowLoading: Boolean){
        WalletApiServiceHelper.getUserBalance(this)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : Callback<HttpRequestResultData<UserBalanceWarpper?>?>() {
                override fun error(type: Int, error: Any) {
                }
                override fun callback(returnData: HttpRequestResultData<UserBalanceWarpper?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        userBalanceList = returnData.data?.spotBalance
                    } else {
                    }
                }
            }))
    }

    private val supportTransferCoin:Unit
        get() {
            var from = fromAccount?.type?.lowercase()
            var to = toAccount?.type?.lowercase()
            if (to != null && from != null) {
                    WalletApiServiceHelper.getSupportTransferCoin(this,  from, to,true, object : NormalCallback<HttpRequestResultDataList<CanTransferCoin?>?>() {
                        override fun callback(returnData: HttpRequestResultDataList<CanTransferCoin?>?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                var result = returnData.data
                                supportCoinData = result
                                if(supportCoinData != null && supportCoinData?.size!! > 0){
                                    if(showSupportCoin == true){
                                        showCoinChooseDialog(supportCoinData)
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


    private val doTransfer:Unit
        get() {
            var inputValue = binding?.editAmount?.text.toString()
            if(TextUtils.isEmpty(inputValue)){
                FryingUtil.showToast(this,getString(R.string.input_amount))
                return
            }
            if(fromAccount == null || toAccount == null){
                FryingUtil.showToast(this,getString(R.string.select_account))
                return
            }
            coinCount = BigDecimal(binding?.editAmount?.text.toString())
            var transferCoin = AssetTransfer()
            transferCoin.amount = coinCount
            transferCoin.coin = selectedCoin?.coin
            transferCoin.fromWalletType = fromAccount?.type?.lowercase()
            transferCoin.toWalletType = toAccount?.type?.lowercase()
            WalletApiServiceHelper.doTransfer(this, transferCoin,true, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.transfer_succ))
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                    }
                }
            })
        }

    private fun initAccountData(supportAccount:ArrayList<String?>?):ArrayList<SupportAccount?>?{
        var initSupportAccountData:ArrayList<SupportAccount?>? = ArrayList()
        if(supportAccount != null && supportAccount.size > 0){
            for(i in supportAccount){
                var initData =  SupportAccount("","",false)
                when(i){
                    "SPOT" ->{
                        initData.name = getString(R.string.spot_account)
                    }
                    "CONTRACT" ->{
                        initData.name = getString(R.string.contract_account)
                    }
                }
                initData.type = i
                initData.selected = false
                initSupportAccountData?.add(initData)
            }
        }
        return initSupportAccountData
    }

    private fun showWalletChooseDialog(accountType:String){
        var clickAccout:SupportAccount? = null
        when(accountType){
            fromAccountType -> clickAccout = fromAccount
            toAccountType -> clickAccout = toAccount
        }
        ChooseWalletControllerWindow(mContext as Activity, getString(R.string.select_wallet),clickAccout,
            supportAccountData,
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
            }).show()
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

    private fun showCoinChooseDialog(data:ArrayList<CanTransferCoin?>?){
            chooseCoinDialog = ChooseCoinControllerWindow(mContext as Activity, getString(R.string.select_wallet),
                data,
                object : ChooseCoinControllerWindow.OnReturnListener<CanTransferCoin?> {
                    override fun onReturn(window: ChooseCoinControllerWindow<CanTransferCoin?>, item: CanTransferCoin?) {
                        selectedCoin = item
                        userBalance = getSelectedCoinInfo(selectedCoin)
                        binding?.tvChooseName?.text = selectedCoin?.coin
                        binding?.tvName?.text = selectedCoin?.coin
                        var maxtCoin = userBalance?.availableBalance + " "+userBalance?.coin
                        binding?.maxTransfer?.text = getString(R.string.max_transfer,maxtCoin)
                    }

                    override fun onSearch(window: ChooseCoinControllerWindow<CanTransferCoin?>,searchKey: String?) {
                        var adapter =  window.getAdapter()
                        search(adapter,data,searchKey)
                    }
                })
            chooseCoinDialog?.show()
    }
}