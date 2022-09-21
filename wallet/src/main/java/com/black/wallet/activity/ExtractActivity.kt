package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.PointLengthFilter
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultString
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletWithdrawAddress
import com.black.base.net.NormalObserver
import com.black.base.util.*
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
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
import java.util.*

@Route(value = [RouterConstData.EXTRACT])
open class ExtractActivity : BaseActivity(), View.OnClickListener {
    protected var walletList: ArrayList<Wallet?>? = null
    protected var wallet: Wallet? = null
    protected var coinType: String? = null
    protected var coinInfo: CoinInfo? = null
    protected var memoNeeded = false

    private var binding: ActivityExtractBinding? = null

    private var resumeOnMineClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        walletList = intent.getParcelableArrayListExtra(ConstData.WALLET_LIST)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        coinType = intent.getStringExtra(ConstData.COIN_TYPE)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_extract)
        binding?.chooseCoinLayout?.setOnClickListener(this)
        val c1 = SkinCompatResources.getColor(this, R.color.C1)
        val l1 = SkinCompatResources.getColor(this, R.color.L1)
        FryingUtil.setEditTextBottomLine(binding?.extractAddress, binding?.addressLine, l1, c1)
        binding?.scanQrcode?.setOnClickListener(this)
        binding?.addressManager?.setOnClickListener(this)
        binding?.extractCount?.filters = arrayOf<InputFilter>(PointLengthFilter(10))
        binding?.extractCount?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val amount = CommonUtil.parseDouble(binding?.extractCount?.text.toString().trim { it <= ' ' })
                binding?.arriveCount?.text = if (amount == null || coinInfo?.withdrawFee == null || amount < coinInfo?.withdrawFee!!) "0.0" else NumberUtil.formatNumberNoGroup(amount - coinInfo?.withdrawFee!!)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.total?.setOnClickListener(this)
        binding?.poundage?.isEnabled = false
        binding?.descriptionUsdt?.visibility = View.GONE
        binding?.btnConfirm?.setOnClickListener(this)
        if (wallet != null) {
            selectCoin(wallet)
        } else if (walletList != null) {
            selectCoin(0)
        } else {
            getAllWallet()
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.extract)
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        toolbar.findViewById<View>(R.id.btn_record).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.btn_record) {
            //点击账户详情
            val extras = Bundle()
            extras.putParcelable(ConstData.WALLET, wallet)
            extras.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
            BlackRouter.getInstance().build(RouterConstData.FINANCIAL_RECORD).with(extras).go(this)
        } else if (i == R.id.choose_coin_layout) {
            val extras = Bundle()
            extras.putParcelableArrayList(ConstData.WALLET_LIST, walletList)
            BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN)
                    .with(extras)
                    .go(this)
        } else if (i == R.id.btn_confirm) {
            val userInfo = CookieUtil.getUserInfo(mContext)
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
                if (!checkAddress(address, coinInfo)) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_address_error))
                    return
                }
                if (userInfo.withDrawStatus == null || TextUtils.equals(userInfo.withDrawStatus, "0")) {
                    FryingUtil.showToast(mContext, getString(R.string.can_not_withdraw))
                    return
                }
//                    if (!TextUtils.equals(userInfo.moneyPasswordStatus, "1")) {
//                        FryingUtil.showToast(mContext, getString(R.string.withdraw_must_money_password))
//                        return
//                    }
                if (coinInfo?.supportWithdraw == null || !coinInfo?.supportWithdraw!!) {
                    FryingUtil.showToast(mContext, getString(R.string.can_not_withdraw_02))
                    return
                }
                if (memoNeeded) {
                    val memo = binding?.memo?.text.toString().trim { it <= ' ' }
                    if (!checkMemo(memo, coinInfo)) {
                        FryingUtil.showToast(mContext, getString(R.string.extract_target_error))
                        return
                    }
                }
                val count = CommonUtil.parseBigDecimal(binding?.extractCount?.text.toString().trim { it <= ' ' })
                if (count == null) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_count))
                    return
                }
                if (coinInfo?.minWithdrawSingle != null && count.toDouble() < coinInfo?.minWithdrawSingle!!) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_withdraw_too_small))
                    return
                }
                if (coinInfo?.maxWithdrawSingle != null && count.toDouble() > coinInfo?.maxWithdrawSingle!!) {
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
                val authType = userInfo.authType ?: ""
                var type = VerifyType.NONE
                if (TextUtils.equals("6", authType)) {
                    type = VerifyType.PHONE or VerifyType.MAIL
                } else if (TextUtils.equals("4", authType)) {
                    type = VerifyType.PHONE or VerifyType.GOOGLE
                } else if (TextUtils.equals("5", authType)) {
                    type = VerifyType.MAIL or VerifyType.GOOGLE
                } else if (TextUtils.equals("7", authType)) {
                    type = VerifyType.PHONE or VerifyType.MAIL or VerifyType.GOOGLE
                } else {
                    FryingUtil.showToast(mContext, "为了您的安全，请在绑定邮箱、手机或者Google验证后进行提现")
                    return
                }
                if (type == VerifyType.NONE) {
                    FryingUtil.showToast(mContext, getString(R.string.withdraw_error_level_low))
                    return
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
                                if (checkVerify(authType, returnTarget)) {
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
        } else if (i == R.id.scan_qrcode) {
            requestCameraPermissions(Runnable {
                BlackRouter.getInstance().build(RouterConstData.CAPTURE)
                        .withRequestCode(ConstData.SCANNIN_GREQUEST_CODE)
                        .go(mContext)
            })
        } else if (i == R.id.address_manager) {
            //选择地址
            val extras = Bundle()
            extras.putString(ConstData.COIN_TYPE, if (wallet == null) null else wallet?.coinType)
            extras.putParcelable(ConstData.COIN_INFO, coinInfo)
            BlackRouter.getInstance().build(RouterConstData.WALLET_ADDRESS_MANAGE)
                    .with(extras)
                    .withRequestCode(ConstData.WALLET_ADDRESS_MANAGE)
                    .go(mContext)
        } else if (i == R.id.total) {
            binding?.extractCount?.setText(if (wallet?.coinAmount == null) "" else NumberUtil.formatNumberNoGroup(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8))
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data?.extras
            when (requestCode) {
                ConstData.CHOOSE_COIN -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) { //查找出选中的币种，并做相应跳转
                        if (walletList != null) {
                            for (wallet in walletList!!) {
                                if (TextUtils.equals(wallet?.coinType, chooseWallet.coinType)) {
                                    this.wallet = wallet
                                    this.wallet?.coinWallet = null
                                    this.wallet?.memo = null
                                    break
                                }
                            }
                        } else {
                            this.wallet = chooseWallet
                            this.wallet?.coinWallet = null
                            this.wallet?.memo = null
                        }
                        coinInfo = null
                        selectCoin(wallet)
                    }
                }
                ConstData.SCANNIN_GREQUEST_CODE -> {
                    val scanResult = bundle?.getString("result")
                    binding?.extractAddress?.setText(scanResult ?: "")
                    refreshAddress(null, scanResult, null)
                }
                ConstData.WALLET_ADDRESS_MANAGE -> {
                    val selectAddress: WalletWithdrawAddress? = bundle?.getParcelable(ConstData.WALLET_WITHDRAW_ADDRESS)
                    refreshAddress(selectAddress, null, null)
                }
            }
        }
    }

    private fun showWalletList() {
        if (wallet == null) {
            if (TextUtils.isEmpty(coinType)) {
                selectCoin(0)
            } else {
                if (walletList != null && walletList!!.isNotEmpty()) {
                    for (wallet in walletList!!) {
                        if (TextUtils.equals(coinType, wallet?.coinType)) {
                            selectCoin(wallet)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun getAllWallet() {
        WalletApiServiceHelper.getWalletList(mContext, true, object : Callback<ArrayList<Wallet?>?>() {
            override fun callback(result: ArrayList<Wallet?>?) {
                walletList = result
                showWalletList()
            }

            override fun error(type: Int, message: Any) {
                FryingUtil.showToast(mContext, message.toString())
            }
        })
    }

    private fun selectCoin(position: Int) {
        selectCoin(CommonUtil.getItemFromList(walletList, position))
    }

    private fun selectCoin(wallet: Wallet?) {
        this.wallet = wallet
        coinType = wallet?.coinType
        binding?.currentCoin?.text = if (coinType == null) "null" else coinType
        refreshWalletHandleInfo()
    }

    fun refreshWallet(wallet: Wallet?, coinInfo: CoinInfo?) {
        this.wallet = wallet
        this.coinType = wallet?.coinType
        this.memoNeeded = coinInfo != null && coinInfo.memoNeeded
        this.coinInfo = coinInfo
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
        binding?.scanQrcode?.isEnabled = false
        binding?.addressManager?.isEnabled = false
        binding?.btnConfirm?.isEnabled = false
        if ("EOS".equals(coinType, ignoreCase = true) || "XRP".equals(coinType, ignoreCase = true)
                || "PASC".equals(coinType, ignoreCase = true)) {
            binding?.memo?.visibility = View.VISIBLE
            binding?.memo?.hint = getString(R.string.memo_hint)
        } else {
            binding?.memo?.visibility = View.GONE
            if (memoNeeded) {
                binding?.memo?.visibility = View.VISIBLE
                binding?.memo?.hint = getString(R.string.memo_hint)
            }
        }
        if ("USDT".equals(coinType, ignoreCase = true)) {
            binding?.descriptionUsdt?.visibility = View.VISIBLE
        } else {
            binding?.descriptionUsdt?.visibility = View.GONE
        }
    }

    private fun refreshAddress(withdrawAddress: WalletWithdrawAddress?, scanAddress: String?, copyAddress: String?) {
        if (withdrawAddress != null) {
            binding?.extractAddress?.setText(if (withdrawAddress.coinWallet == null) "" else withdrawAddress.coinWallet)
            if (coinInfo != null && coinInfo?.memoNeeded!!) {
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
        if (coinInfo?.supportWithdraw != null && coinInfo?.supportWithdraw!!) {
            binding?.extractAddress?.setText("")
            binding?.memo?.setText("")
            binding?.poundage?.setText("")
            binding?.extractCount?.setText("")
            binding?.extractAddress?.isEnabled = true
            binding?.memo?.isEnabled = true
            binding?.extractCount?.isEnabled = true
            binding?.arriveCount?.isEnabled = true
            binding?.scanQrcode?.isEnabled = true
            binding?.addressManager?.isEnabled = true
            binding?.btnConfirm?.isEnabled = true
            binding?.available?.text = String.format(getString(R.string.extract_useable), NumberUtil.formatNumberNoGroup(wallet?.coinAmount, RoundingMode.FLOOR, 0, 8), coinType)
            binding?.extractCount?.hint = getString(R.string.extract_single,
                    NumberUtil.formatNumberNoGroup(coinInfo?.minWithdrawSingle, 2, 15),
                    NumberUtil.formatNumberNoGroup(coinInfo?.maxWithdrawSingle, 2, 15))
            binding?.poundage?.setText(String.format("%s %s", NumberUtil.formatNumberNoGroup(coinInfo?.withdrawFee), coinType))
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
            binding?.scanQrcode?.isEnabled = false
            binding?.addressManager?.isEnabled = false
            binding?.btnConfirm?.isEnabled = false
        }
        if ("EOS".equals(coinType, ignoreCase = true) || "XRP".equals(coinType, ignoreCase = true)
                || "PASC".equals(coinType, ignoreCase = true)) {
            binding?.memo?.visibility = View.VISIBLE
            binding?.memo?.hint = getString(R.string.memo_hint)
        } else {
            binding?.memo?.visibility = View.GONE
            if (memoNeeded) {
                binding?.memo?.visibility = View.VISIBLE
                binding?.memo?.hint = getString(R.string.memo_hint)
            }
        }
        if ("USDT".equals(coinType, ignoreCase = true)) {
            binding?.descriptionUsdt?.visibility = View.VISIBLE
        } else {
            binding?.descriptionUsdt?.visibility = View.GONE
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
        jsonObject.addProperty("coinType", coinType)
        jsonObject.addProperty("withdrawFee", coinInfo?.withdrawFee.toString())
        jsonObject.addProperty("txTo", binding?.extractAddress?.text.toString().trim { it <= ' ' })
        jsonObject.addProperty("amount", binding?.extractCount?.text.toString().trim { it <= ' ' })
        jsonObject.addProperty("memo", memoPost)
        jsonObject.addProperty("password", if (target != null && !TextUtils.isEmpty(target.password)) target.password else "")
        jsonObject.addProperty("phoneCode", if (target == null) "" else target.phoneCode)
        jsonObject.addProperty("emailCode", if (target == null) "" else target.mailCode)
        jsonObject.addProperty("googleCode", if (target == null) "" else target.googleCode)
//        jsonObject.addProperty("moneyPassword", moneyPassword);
        //        jsonObject.addProperty("moneyPassword", moneyPassword);
        jsonObject.addProperty("chainType", null as String?)
        val rsaParam = jsonObject.toString() + "#" + System.currentTimeMillis()
        val rsa = RSAUtil.encryptDataByPublicKey(rsaParam)
        showLoading()
        ApiManager.build(this).getService(WalletApiService::class.java)
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
                            FryingUtil.showToast(mContext, getString(R.string.withdraw_success))
                            finish()
                        } else {
                            FryingUtil.showToast(mContext, if (result == null) "null" else result.msg)
                        }
                    }

                })
    }

//    private fun getCoinInfo(coinType: String?): Observable<CoinInfo?>? {
//        return if (coinType == null) {
//            Observable.just(null)
//        } else {
//            WalletApiServiceHelper.getCoinInfo(this, coinType)
//                    ?.flatMap { info: CoinInfo? ->
//                        this.coinInfo = info
//                        memoNeeded = info != null && info.memoNeeded
//                        Observable.just(info)
//                    }
//        }
//    }

    private fun refreshWalletHandleInfo() {
        if (wallet != null) {
            coinType = wallet?.coinType
            //数据不为空刷新 否则重新请求
            if (coinInfo != null) {
                refreshWallet(wallet, coinInfo)
            } else {
                //判断充值提现开关是否开启
                showLoading()
//                getCoinInfo(coinType)
//                        ?.compose(RxJavaHelper.observeOnMainThread())
//                        ?.subscribe(object : NormalObserver<CoinInfo?>(this) {
//                            override fun afterRequest() {
//                                super.afterRequest()
//                                hideLoading()
//                            }
//
//                            override fun error(type: Int, error: Any?) {
//                                super.error(type, error)
//                                refreshWallet(wallet, coinInfo)
//                            }
//
//                            override fun callback(result: CoinInfo?) {
//                                refreshWallet(wallet, coinInfo)
//                            }
//
//                        })
            }
        }
    }
}