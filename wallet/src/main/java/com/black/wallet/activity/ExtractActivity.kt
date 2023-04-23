package com.black.wallet.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.PointLengthFilter
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.*
import com.black.base.net.NormalObserver
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.widget.SpanTextView
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.util.RSAUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityExtractBinding
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import skin.support.content.res.SkinCompatResources
import java.math.RoundingMode

@Route(value = [RouterConstData.EXTRACT])
open class ExtractActivity : BaseActivity(), View.OnClickListener {
    protected var wallet: Wallet? = null
    protected var coinType: String? = null
    protected var coinInfo: CoinInfoType? = null
    private var userInfo: UserInfo? = null
    private var coinInfoReal: CoinInfo? = null
    private var coinChain:String? = null
    private var chainNames:ArrayList<String>? = null
    private var address:String? = null
    private var amount: Double? = null
    protected var memoNeeded = false

    private var binding: ActivityExtractBinding? = null

    private var resumeOnMineClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        coinType = wallet?.coinType

        binding = DataBindingUtil.setContentView(this, R.layout.activity_extract)
        val actionBarRecord: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)
        binding?.chooseCoinLayout?.setOnClickListener(this)
        binding?.chooseChainLayout?.setOnClickListener(this)
        binding?.tvTransfer?.setOnClickListener(this)
        val c1 = SkinCompatResources.getColor(this, R.color.C1)
        val l1 = SkinCompatResources.getColor(this, R.color.L1)
        binding?.extractCount?.filters = arrayOf<InputFilter>(PointLengthFilter(10))
        binding?.extractCount?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var amount = CommonUtil.parseDouble(binding?.extractCount?.text.toString().trim { it <= ' ' })
                if (amount == null)
                    amount = 0.0
                binding?.arriveCount?.text = if (amount == null || coinInfoReal?.withdrawFee == null || amount < coinInfoReal?.withdrawFee!!) getString(R.string.actual_receive_amount,"0.0")  else getString(R.string.actual_receive_amount,NumberUtil.formatNumberNoGroup(amount - coinInfoReal?.withdrawFee!! - (coinInfoReal?.withdrawFeeRate!!) * amount))
                binding?.poundage?.setText(getString(R.string.fee_amount,String.format("%s %s", NumberUtil.formatNumberNoGroup(coinInfoReal?.withdrawFee!! + (coinInfoReal?.withdrawFeeRate!!) * amount,4,8), coinType)))

            }
            override fun afterTextChanged(s: Editable) {}
        })
        binding?.total?.setOnClickListener(this)
        binding?.poundage?.isEnabled = false
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.withdrawAddrLayout?.setOnClickListener(this)
        if (wallet != null) {
            selectCoin(wallet)
        }
        /* userInfo = CookieUtil.getUserInfo(mContext)
         if (userInfo == null) {
             return
         }
         if (TextUtils.equals(userInfo?.phoneSecurityStatus, "0") &&  TextUtils.equals(userInfo?.googleSecurityStatus, "0")) {
             FryingUtil.showToast(mContext,  getString(R.string.withdraw_must_bind_phone))
             getDialog()
         }
         if (TextUtils.equals(userInfo?.emailSecurityStatus, "0") && TextUtils.equals(userInfo?.googleSecurityStatus, "0")) {
             FryingUtil.showToast(mContext,  getString(R.string.withdraw_must_bind_mail))
             getDialog()
         }*/
       
    }

    override fun onResume() {
        super.onResume()
        getUserWithdrawQuota()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.extract)
    }

    private fun showChainChooseDialog(){
        if(coinChain != null && chainNames!!.size >0){
            val chooseWalletDialog =  ChooseWalletControllerWindow(mContext as Activity,
                getString(R.string.get_chain_name),
                coinChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(window: ChooseWalletControllerWindow<String?>, item: String?) {
                        binding?.currentChain?.setText(item)
                        coinChain = item
                    }
                })
            chooseWalletDialog.setTipsText(getString(R.string.chain_tips))
            chooseWalletDialog.setTipsTextVisible(true)
            chooseWalletDialog.show()
        }
    }

    private fun getCoinInfo(coinType: String?): Observable<CoinInfoType?>? {
        return if (coinType == null) {
            Observable.just(null)
        } else {
            WalletApiServiceHelper.getCoinInfo(this, coinType)
                ?.flatMap { info: CoinInfoType? ->
                    coinInfo = info
                    coinInfoReal = info?.config?.get(0)?.coinConfigVO
                    memoNeeded = coinInfoReal != null && coinInfoReal?.memoNeeded == true
                    setChainNames(info)
                    coinChain = info?.config?.get(0)?.chain
                    binding?.currentChain?.setText(if (coinType == null) "null" else coinChain)
                    Observable.just(info)
                }
        }
    }

    private fun setChainNames(info:CoinInfoType?){
        chainNames = ArrayList()
        var chainConfig = info?.config
        if (chainConfig != null) {
            for (i in chainConfig){
                i.chain?.let { chainNames?.add(it) }
            }
        }
    }


    override fun onClick(view: View) {
        when(view.id){
            R.id.img_action_bar_right ->{
                //点击账户详情
                val extras = Bundle()
                extras.putParcelable(ConstData.WALLET, wallet)
                extras.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                BlackRouter.getInstance().build(RouterConstData.FINANCIAL_RECORD).with(extras).go(this)
            }
            R.id.choose_coin_layout ->{
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN)
                    .go(this)
            }
            R.id.btn_confirm -> doSubmit()
            R.id.scan_qrcode ->{
                requestCameraPermissions(Runnable {
                    BlackRouter.getInstance().build(RouterConstData.CAPTURE)
                        .withRequestCode(ConstData.SCANNIN_GREQUEST_CODE)
                        .go(mContext)
                })
            }
            R.id.withdraw_addr_layout ->{
                //选择地址
                val extras = Bundle()
                extras.putParcelable(ConstData.COIN_INFO, coinInfoReal)
                extras.putString(ConstData.COIN_CHAIN,coinChain)
                BlackRouter.getInstance().build(RouterConstData.WALLET_ADDRESS_MANAGE)
                    .with(extras)
                    .withRequestCode(ConstData.WALLET_ADDRESS_MANAGE)
                    .go(mContext)
            }
            R.id.total ->binding?.extractCount?.setText(if (wallet?.coinAmount == null) "" else NumberUtil.formatNumberNoGroup(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8))
            R.id.choose_chain_layout ->  showChainChooseDialog()
            R.id.tv_transfer ->{
                BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ConstData.CHOOSE_COIN -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        coinInfo = null
                        selectCoin(chooseWallet)
                    }
                }
                ConstData.WALLET_ADDRESS_MANAGE ->{
                    val address:String? = data?.getStringExtra(ConstData.COIN_ADDRESS)
                    binding?.extractAddress?.setText(address)
                }
            }
        }
    }


    private fun doSubmit(){
            userInfo = CookieUtil.getUserInfo(mContext)
            if (userInfo != null) {
//                if (TextUtils.equals(userInfo.authType, "5") || TextUtils.equals(userInfo.authType, "6")) {
//                    BlackRouter.getInstance().build(RouterConstData.GOOGLE_GET_KEY).go(mContext)
//                } else {
                //验证输入框
                val address = binding?.extractAddress?.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(address)) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_address))
                    return
                }
                if (!checkAddress(address, coinInfoReal)) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_address_error))
                    return
                }
                if (userInfo?.withDrawStatus == null || TextUtils.equals(userInfo?.withDrawStatus, "0")) {
                    FryingUtil.showToast(mContext, getString(R.string.can_not_withdraw))
                    return
                }
//                    if (!TextUtils.equals(userInfo.moneyPasswordStatus, "1")) {
//                        FryingUtil.showToast(mContext, getString(R.string.withdraw_must_money_password))
//                        return
//                    }
                if (coinInfoReal?.supportWithdraw == null || !coinInfoReal?.supportWithdraw!!) {
                    FryingUtil.showToast(mContext, getString(R.string.can_not_withdraw_02))
                    return
                }
                if (memoNeeded) {
                    val memo = binding?.memo?.text.toString().trim { it <= ' ' }
                    if (!checkMemo(memo, coinInfoReal)) {
                        FryingUtil.showToast(mContext, getString(R.string.extract_target_error))
                        return
                    }
                }
                val count = CommonUtil.parseBigDecimal(binding?.extractCount?.text.toString().trim { it <= ' ' })
                if (count == null) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_count))
                    return
                }
                if (coinInfoReal?.minWithdrawSingle != null && count.toDouble() < coinInfoReal?.minWithdrawSingle!!) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_too_small))
                    return
                }
                if (coinInfoReal?.maxWithdrawSingle != null && count.toDouble() > coinInfoReal?.maxWithdrawSingle!!) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_too_large))
                    return
                }
                if (wallet?.coinAmount != null && count > wallet?.coinAmount!!) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_not_enough))
                    return
                }
                //authType=用户提现所需验证：1-手机+邮箱  2-手机+google 3-邮箱+google
                // 4-手机+google+邮箱 5-必须完成邮箱或google其中一项  6-必须完成手机或google其中一项
                //用户提现所需验证：4-手机+google 5-邮箱+google 6-手机+邮箱  7-手机+google+邮箱 1,2,3-必须完成邮箱或google其中一项  -必须完成手机或google其中一项
                val authType = userInfo?.authType
                var type = VerifyType.NONE
                if (TextUtils.equals("6", authType)) {
                    type = VerifyType.PHONE or VerifyType.MAIL
                } else if (TextUtils.equals("4", authType)) {
                    type = VerifyType.PHONE or VerifyType.GOOGLE
                } else if (TextUtils.equals("5", authType)) {
                    type = VerifyType.MAIL or VerifyType.GOOGLE
                } else if (TextUtils.equals("7", authType)) {
                    type = VerifyType.PHONE or VerifyType.MAIL or VerifyType.GOOGLE
                } else if (TextUtils.equals("1", authType)){
                    type = VerifyType.MAIL or VerifyType.PASSWORD
                }
                else if (TextUtils.equals("2", authType)){
                    type = VerifyType.PHONE or VerifyType.PASSWORD
                }
                else if (TextUtils.equals("3", authType)){
                    type = VerifyType.GOOGLE or VerifyType.PASSWORD
                }
                //                    type |= VerifyWindow.MONEY_PASSWORD;
                val target = Target.buildFromUserInfo(userInfo)
                val verifyWindow = VerifyWindowObservable.getVerifyWindowMultiple(this, type, target)
                verifyWindow.show()
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap { returnTarget: Target? ->
                        if (returnTarget == null) {
                            verifyWindow.dismiss()
                            Observable.empty()
                        } else {
                            if (checkVerify(authType!!, returnTarget)) {
                                createWithdrawCoinNew(verifyWindow, returnTarget)
                                Observable.just(1)
                            } else {
                                Observable.just(0)
                            }
                        }
                    }
                    .subscribe()
//                }
            } else {
                FryingUtil.showToast(mContext, getString(R.string.login_first))
            }
    }

    private fun successDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.tixian_succcess, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
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
        dialog?.findViewById<SpanTextView>(R.id.amount)?.text = if (amount == null || coinInfoReal?.withdrawFee == null || amount!! < coinInfoReal?.withdrawFee!!) getString(R.string.tixian_amount,"0.0")  else getString(R.string.tixian_amount,NumberUtil.formatNumberNoGroup(amount!! - coinInfoReal?.withdrawFee!! - (coinInfoReal?.withdrawFeeRate!!) * amount!!))
        dialog.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.green_up).setOnClickListener { v ->
            finish()
            dialog.dismiss()
        }
    }

    private fun failedDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.tixian_failed, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
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
        dialog?.findViewById<SpanTextView>(R.id.amount)?.text = if (amount == null || coinInfoReal?.withdrawFee == null || amount!! < coinInfoReal?.withdrawFee!!) getString(R.string.tixian_amount,"0.0")  else getString(R.string.tixian_amount,NumberUtil.formatNumberNoGroup(amount!! - coinInfoReal?.withdrawFee!! - (coinInfoReal?.withdrawFeeRate!!) * amount!!))
        dialog.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            val bundle = Bundle()
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
    private fun getDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.withdraw_dialog, null)
        val dialog = Dialog(mContext, R.style.AlertDialog)
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
            BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER).go(mContext)
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            finish()
            dialog.dismiss()
        }
    }

    private fun selectCoin(wallet: Wallet?) {
        this.wallet = wallet
        coinType = wallet?.coinType
        binding?.currentCoin?.text = if (coinType == null) "USDT" else coinType
        refreshWalletHandleInfo()
    }

    fun refreshWallet(wallet: Wallet?, coinInfo: CoinInfo?) {
        this.wallet = wallet
        this.coinType = wallet?.coinType
        this.memoNeeded = coinInfo != null && coinInfo.memoNeeded
        this.coinInfoReal = coinInfoReal
        requestRefresh()
    }

    private fun requestRefresh() {
        if (wallet != null && coinInfo != null) {
            resetViews()
        } else {
            clear()
        }
    }

    private fun clear() {
        binding?.extractAddress?.setText(R.string.extract_not_support)
        binding?.extractAddress?.isEnabled = false
        binding?.memo?.setText(R.string.extract_not_support)
        binding?.memo?.isEnabled = false
        binding?.poundage?.setText(R.string.extract_not_support)
        binding?.extractCount?.setText(R.string.extract_not_support)
        binding?.arriveCount?.setText(R.string.extract_not_support)
        binding?.extractCount?.isEnabled = false
        binding?.arriveCount?.isEnabled = false
        binding?.btnConfirm?.isEnabled = false
        if ("EOS".equals(coinType, ignoreCase = true) || "XRP".equals(coinType, ignoreCase = true)
                || "PASC".equals(coinType, ignoreCase = true)) {
            binding?.withdrawMemoLayout?.visibility = View.VISIBLE
            binding?.memo?.hint = getString(R.string.memo_hint)
        } else {
            binding?.withdrawMemoLayout?.visibility = View.GONE
            if (memoNeeded) {
                binding?.withdrawMemoLayout?.visibility = View.VISIBLE
                binding?.memo?.hint = getString(R.string.memo_hint)
            }
        }
    }

    private fun refreshAddress(withdrawAddress: WalletWithdrawAddress?, scanAddress: String?, copyAddress: String?) {
        if (withdrawAddress != null) {
            binding?.extractAddress?.setText(if (withdrawAddress.coinWallet == null) "" else withdrawAddress.coinWallet)
            if (coinInfo != null && coinInfoReal?.memoNeeded!!) {
                binding?.memo?.setText(if (withdrawAddress.memo == null) "" else withdrawAddress.memo)
            }
        } else if (scanAddress != null) {
            binding?.extractAddress?.setText(scanAddress)
        } else if (copyAddress != null) {
            binding?.extractAddress?.setText(copyAddress)
        }
        resumeOnMineClick = true
    }

    private fun resetViews() {
        if (coinInfoReal?.supportWithdraw != null && coinInfoReal?.supportWithdraw!!) {
            binding?.extractAddress?.setText("")
            binding?.memo?.setText("")
            binding?.poundage?.setText("")
            binding?.extractCount?.setText("")
            binding?.extractAddress?.isEnabled = true
            binding?.memo?.isEnabled = true
            binding?.extractCount?.isEnabled = true
            binding?.arriveCount?.isEnabled = true
            binding?.btnConfirm?.isEnabled = true
            binding?.available?.text = String.format(getString(R.string.extract_useable), NumberUtil.formatNumberNoGroup(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8), coinType)
            binding?.extractCount?.hint = getString(R.string.extract_single,
                    NumberUtil.formatNumberNoGroup(coinInfoReal?.minWithdrawSingle, 2, 15),
                    NumberUtil.formatNumberNoGroup(coinInfoReal?.maxWithdrawSingle, 2, 15))
            binding?.arriveCount?.text = String.format("0.00 %s", coinType)
        } else {
            binding?.extractAddress?.setText(R.string.extract_not_support)
            binding?.extractAddress?.isEnabled = false
            binding?.memo?.setText(R.string.extract_not_support)
            binding?.memo?.isEnabled = false
            binding?.poundage?.setText(R.string.extract_not_support)
            binding?.extractCount?.setText(R.string.extract_not_support)
            binding?.arriveCount?.setText(R.string.extract_not_support)
            binding?.extractCount?.isEnabled = false
            binding?.arriveCount?.isEnabled = false
            binding?.btnConfirm?.isEnabled = false
        }
        if ("EOS".equals(coinType, ignoreCase = true) || "XRP".equals(coinType, ignoreCase = true)
                || "PASC".equals(coinType, ignoreCase = true)) {
            binding?.withdrawMemoLayout?.visibility = View.VISIBLE
            binding?.memo?.hint = getString(R.string.memo_hint)
        } else {
            binding?.withdrawMemoLayout?.visibility = View.GONE
            if (memoNeeded) {
                binding?.withdrawMemoLayout?.visibility = View.VISIBLE
                binding?.memo?.hint = getString(R.string.memo_hint)
            }
        }
        refreshSubmitButton()
    }

    private fun refreshSubmitButton() {
        val userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo != null) {
            binding?.btnConfirm?.setText(R.string.extract_submit)
            binding?.btnConfirm?.isEnabled = true
        }
    }

    private fun checkMemo(memo: String, info: CoinInfo?): Boolean {
        if (TextUtils.isEmpty(memo)) {
            return true
        }
        if (!TextUtils.isEmpty(info?.memoRegexpExpres)) {
            return memo.matches(Regex(info?.memoRegexpExpres!!))
        }
        when {
            "EOS".equals(coinType, ignoreCase = true) -> {
                return memo.matches(Regex("^[0-9a-zA-Z]{1,50}$"))
            }
            "XRP".equals(coinType, ignoreCase = true) -> {
                return if (!memo.matches(Regex("^[0-9]*$"))) {
                    false
                } else {
                    val memoLong = CommonUtil.parseLong(memo)
                    memoLong != null && memoLong < 0x100000000L
                }
            }
            "PASC".equals(coinType, ignoreCase = true) -> {
                return memo.matches(Regex("^[0-9a-zA-Z]{1,50}$"))
            }
            else -> return true
        }
    }

    //检查输入的提币地址
    private fun checkAddress(address: String?, info: CoinInfo?): Boolean {
        if (address == null) {
            return false
        }
        var result = true
        if (info != null && !TextUtils.isEmpty(info.addrRegexpExpress)) {
            return address.matches(Regex(info.addrRegexpExpress!!))
        }
        if ("ETH".equals(wallet?.coinType, ignoreCase = true) || "ETC".equals(wallet?.coinType, ignoreCase = true)) {
        } else if ("BTC".equals(wallet?.coinType, ignoreCase = true) || "USDT".equals(wallet?.coinType, ignoreCase = true)) {
            result = address.matches(Regex("^(0x|0X){1}[0-9a-fA-F]{40}$"))
            result = address.matches(Regex("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$"))
        } else if ("EOS".equals(wallet?.coinType, ignoreCase = true)) {
            result = address.matches(Regex("^[1-5a-z]{1,12}$"))
        } else if ("LTC".equals(wallet?.coinType, ignoreCase = true)) {
            result = address.matches(Regex("^[LM3][a-km-zA-HJ-NP-Z1-9]{25,34}$"))
        } else if ("XRP".equals(wallet?.coinType, ignoreCase = true)) {
            result = address.matches(Regex("^[r][a-km-zA-HJ-NP-Z1-9]{24,34}$"))
        } else if ("ION".equals(wallet?.coinType, ignoreCase = true)) {
            result = address.matches(Regex("^[i][a-km-zA-HJ-NP-Z1-9]{25,34}$"))
        } else if ("PASC".equals(wallet?.coinType, ignoreCase = true)) {
            result = checkPASCAddress(address)
        }
        return result
    }

    private fun checkPASCAddress(address: String?): Boolean {
        if (address == null || address.trim { it <= ' ' }.isEmpty()) {
            return false
        }
        val addressArr = address.split("-").toTypedArray()
        if (addressArr.size != 2) {
            return false
        }
        val account = CommonUtil.parseLong(addressArr[0])
        if (account == null || account < 0 || account >= 0x100000000L) {
            return false
        }
        val checkSum = CommonUtil.parseInt(addressArr[1])
        if (checkSum == null || checkSum < 10 || checkSum > 98) {
            return false
        }
        val checkSumVerify = account * 101 % 89 + 10
        return checkSum.toLong() == checkSumVerify
    }

    //authType=用户提现所需验证：1-手机+邮箱  2-手机+google 3-邮箱+google
   // 4-手机+google+邮箱 5-必须完成邮箱或google其中一项  6-必须完成手机或google其中一项
    //用户提现所需验证：4-手机+google 5-邮箱+google 6-手机+邮箱  7-手机+google+邮箱 1,2,3-必须完成邮箱或google其中一项  -必须完成手机或google其中一项
    private fun checkVerify(authType: String, target: Target): Boolean {
        if (TextUtils.equals("6", authType)) {
            if (TextUtils.isEmpty(target.phoneCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_sms_code))
                return false
            }
            if (TextUtils.isEmpty(target.mailCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                return false
            }
        } else if (TextUtils.equals("4", authType)) {
            if (TextUtils.isEmpty(target.phoneCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_sms_code))
                return false
            }
            if (TextUtils.isEmpty(target.googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                return false
            }
        } else if (TextUtils.equals("5", authType)) {
            if (TextUtils.isEmpty(target.mailCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                return false
            }
            if (TextUtils.isEmpty(target.googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                return false
            }
        } else if (TextUtils.equals("7", authType)) {
            if (TextUtils.isEmpty(target.phoneCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_sms_code))
                return false
            }
            if (TextUtils.isEmpty(target.mailCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                return false
            }
            if (TextUtils.isEmpty(target.googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                return false
            }
        }
        return true
    }

    private fun createWithdrawCoinNew(verifyWindow: VerifyWindowObservable, target: Target?) {
        var memoPost: String? = null
        if (memoNeeded) {
            val memo = binding?.memo?.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(memo)) {
                if ("EOS".equals(coinType, ignoreCase = true)) {
                    memoPost = memo
                } else if ("XRP".equals(wallet?.coinType, ignoreCase = true)) {
                    val memoLong = CommonUtil.parseLong(memo)
                    if (memoLong != null) {
                        memoPost = NumberUtil.formatNumberNoGroup(memoLong)
                    }
                } else if ("PASC".equals(wallet?.coinType, ignoreCase = true)) {
                    memoPost = memo
                } else if (memoNeeded) {
                    memoPost = memo
                }
            }
        }
        val jsonObject = JsonObject()
        var amount = CommonUtil.parseDouble(binding?.extractCount?.text.toString().trim { it <= ' ' })
        if (amount == null)
            amount = 0.0
        jsonObject.addProperty("coinType", coinType)
        jsonObject.addProperty("withdrawFee", coinInfoReal?.withdrawFee!! + (coinInfoReal?.withdrawFeeRate!!) * amount)
        jsonObject.addProperty("txTo", binding?.extractAddress?.text.toString().trim { it <= ' ' })
        jsonObject.addProperty("amount", binding?.extractCount?.text.toString().trim { it <= ' ' })
        jsonObject.addProperty("chain", binding?.currentChain?.text.toString().trim { it <= ' ' })
        jsonObject.addProperty("memo", memoPost)
        jsonObject.addProperty("password", if (target != null && !TextUtils.isEmpty(target.password)) RSAUtil.encryptDataByPublicKey(target.password) else "")
        jsonObject.addProperty("phoneCode", if (target == null) "" else target.phoneCode)
        jsonObject.addProperty("emailCode", if (target == null) "" else target.mailCode)
        jsonObject.addProperty("googleCode", if (target == null) "" else target.googleCode)
//        jsonObject.addProperty("moneyPassword", moneyPassword);
        //        jsonObject.addProperty("moneyPassword", moneyPassword);
        jsonObject.addProperty("chainType", null as String?)
        val rsaParam = jsonObject.toString() + "#" + System.currentTimeMillis()
        val rsa = RSAUtil.encryptDataByPublicKey(rsaParam)
        showLoading()
        ApiManager.build(this,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
                ?.createWithdraw(rsa)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(object : NormalObserver<HttpRequestResultString?>(this) {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                        if (type == HttpRequestResult.ERROR_MISS_MONEY_PASSWORD) {
                            verifyWindow.dismiss()
                        }
                    }

                    override fun callback(result: HttpRequestResultString?) {
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            verifyWindow.dismiss()
                            successDialog()
                        } else {
                            //FryingUtil.showToast(mContext, if (result == null) "null" else result.msg)
                            failedDialog()
                        }
                    }

                })
    }

    private fun getUserWithdrawQuota(){
        coinType?.let {
            WalletApiServiceHelper.getUserWithdrawQuota(this, it, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.withdrawQuota?.setText(getString(R.string.withdraw_amount_limit,returnData.data))
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                    }
                }
            })
        }
    }

    private fun getRechargeAddress(coinType: String?): Observable<WalletAddress?>? {
        return if (coinType == null) {
            Observable.just(null)
        } else {
            ApiManager.build(this,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
                ?.getExchangeAddress(coinType,coinChain)
                ?.flatMap { addressResult: HttpRequestResultData<WalletAddress?>? ->
                    if (addressResult != null && addressResult.code == HttpRequestResult.SUCCESS) {
                        Observable.just(addressResult.data)
                    } else {
                        Observable.error(RuntimeException(if (addressResult == null) "null" else addressResult.msg))
                    }
                }
        }
    }



    protected open fun refreshWalletHandleInfo() {
        if (wallet != null) {
            coinType = wallet?.coinType
            //数据不为空刷新 否则重新请求
            if (coinInfo != null) {
                refreshWallet(wallet, coinInfoReal)
            } else {
                //判断充值提现开关是否开启
                showLoading()
                getCoinInfo(coinType)
                    ?.flatMap { info: CoinInfoType? ->
                        if (info == null) {
                            Observable.just(null)
                        } else {
                            getRechargeAddress(coinType)
                        }
                    }
                    ?.compose(RxJavaHelper.observeOnMainThread())
                    ?.subscribe(object : NormalObserver<WalletAddress?>(this) {
                        override fun afterRequest() {
                            super.afterRequest()
                            hideLoading()
                        }

                        override fun error(type: Int, error: Any?) {
                            super.error(type, error)
                            refreshWallet(wallet, coinInfoReal)
                        }

                        override fun callback(result: WalletAddress?) {
                            wallet?.coinWallet = if (result?.address == null) result?.account else result.address
                            wallet?.memo = result?.memo
                            wallet?.minChainDepositAmt = result?.minChainDepositAmt
                            refreshWallet(wallet, coinInfoReal)
                        }
                    })
            }
        }
    }
}