package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletAddress
import com.black.base.net.NormalObserver
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityRechargeBinding
import com.google.zxing.WriterException
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.RECHARGE])
open class RechargeActivity : BaseActivity(), View.OnClickListener {
    private var walletList: ArrayList<Wallet?>? = null
    private var wallet: Wallet? = null
    private var coinType: String? = null
    private var coinInfo: CoinInfo? = null
    private var memoNeeded = false

    private var binding: ActivityRechargeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        walletList = intent.getParcelableArrayListExtra(ConstData.WALLET_LIST)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        coinType = intent.getStringExtra(ConstData.COIN_TYPE)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_recharge)

        binding?.chooseCoinLayout?.setOnClickListener(this)
        binding?.btnSaveQrcode?.setOnClickListener(this)
        binding?.btnCopyAddress?.setOnClickListener(this)
        binding?.btnCopyMemo?.setOnClickListener(this)

        when {
            wallet != null -> {
                selectCoin(wallet)
            }
            walletList != null -> {
                selectCoin(0)
            }
            else -> {
                getAllWallet()
            }
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.rechange)
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
            extras.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
            BlackRouter.getInstance().build(RouterConstData.FINANCIAL_RECORD).with(extras).go(this)
        } else if (i == R.id.btn_save_qrcode) {
        } else if (i == R.id.btn_copy_address) {
            if (CommonUtil.copyText(mContext, wallet?.coinWallet)) {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
            }
        } else if (i == R.id.btn_copy_memo) {
            if (CommonUtil.copyText(mContext, wallet?.memo)) {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
            }
        } else if (i == R.id.choose_coin_layout) {
            val extras = Bundle()
            extras.putParcelableArrayList(ConstData.WALLET_LIST, walletList)
            BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN)
                    .with(extras)
                    .go(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.CHOOSE_COIN -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        //查找出选中的币种，并做相应跳转
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
                            wallet = chooseWallet
                            wallet?.coinWallet = null
                            wallet?.memo = null
                        }
                        coinInfo = null
                        selectCoin(wallet)
                    }
                }
            }
        }
    }

    protected fun showWalletList() {
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

    fun selectCoin(position: Int) {
        selectCoin(CommonUtil.getItemFromList(walletList, position))
    }

    fun selectCoin(wallet: Wallet?) {
        this.wallet = wallet
        coinType = wallet?.coinType
        binding?.currentCoin?.setText(if (coinType == null) "null" else coinType)
        refreshWalletHandleInfo()
    }

    private fun requestRefresh() {
        if (wallet == null || coinInfo == null) {
            clear()
        } else {
            showCoinAddress(wallet)
        }
    }

    private fun clear() {
        val coinType = if (wallet == null) "" else wallet?.coinType
        if (memoNeeded) {
            binding?.memoLayout?.visibility = View.VISIBLE
            binding?.memoText?.setText(if (wallet?.memo == null) "" else wallet?.memo)
        } else {
            binding?.memoLayout?.visibility = View.GONE
        }
        binding?.qrcode?.setImageResource(R.drawable.icon_recharge_false)
        binding?.qrcodeText?.setText(R.string.recharge_not_support)
        binding?.memoText?.setText(R.string.recharge_not_support)
        binding?.btnSaveQrcode?.isEnabled = false
        binding?.btnCopyAddress?.isEnabled = false
        binding?.btnCopyMemo?.isEnabled = false
        if (TextUtils.isEmpty(coinType) && coinInfo != null) {
            if (memoNeeded) {
                binding?.description01?.setText(getString(R.string.recharge_description_01, coinType))
                binding?.description02?.setText(getString(R.string.recharge_description_02, NumberUtil.formatNumber(coinInfo?.minWithdrawSingle), coinType))
                binding?.description02?.visibility = View.VISIBLE
            } else {
                binding?.description01?.setText(getString(R.string.recharge_description_03, coinType, coinInfo?.blockConfirm.toString(), NumberUtil.formatNumber(coinInfo?.minimumDepositAmount), coinType))
                binding?.description02?.setText("")
                binding?.description02?.visibility = View.GONE
            }
        } else {
            binding?.description01?.setText("")
            binding?.description02?.setText("")
        }
        if (TextUtils.equals("USDT", coinType)) {
            binding?.description03?.visibility = View.VISIBLE
        } else {
            binding?.description03?.visibility = View.GONE
        }
    }

    private fun showCoinAddress(wallet: Wallet?) {
        if (memoNeeded) {
            binding?.memoLayout?.visibility = View.VISIBLE
            binding?.memoText?.setText(if (wallet?.memo == null) "" else wallet.memo)
        } else {
            binding?.memoLayout?.visibility = View.GONE
        }
        if (coinInfo == null || true != coinInfo!!.supportDeposit) {
            binding?.qrcode?.setImageResource(R.drawable.icon_recharge_false)
            binding?.qrcodeText?.setText(R.string.recharge_not_support)
            binding?.memoText?.setText(R.string.recharge_not_support)
            binding?.btnSaveQrcode?.isEnabled = false
            binding?.btnCopyAddress?.isEnabled = false
            binding?.btnCopyMemo?.isEnabled = false
        } else {
            binding?.btnSaveQrcode?.isEnabled = true
            binding?.btnCopyAddress?.isEnabled = true
            binding?.btnCopyMemo?.isEnabled = true
            val address = wallet?.coinWallet
            if (address != null && address.trim { it <= ' ' }.isNotEmpty()) {
                var qrcodeBitmap: Bitmap? = null
                try {
                    qrcodeBitmap = CommonUtil.createQRCode(address, 300, 0, SkinCompatResources.getColor(mContext, R.color.C1))
                } catch (e: WriterException) {
                    CommonUtil.printError(applicationContext, e)
                }
                if (qrcodeBitmap != null) {
                    binding?.qrcode?.setImageBitmap(qrcodeBitmap)
                    binding?.qrcodeText?.setText(address)
                }
            } else {
                binding?.qrcode?.setImageResource(R.drawable.icon_recharge_false)
            }
            if (memoNeeded) {
                binding?.description01?.setText(getString(R.string.recharge_description_01, wallet?.coinType))
                binding?.description02?.setText(getString(R.string.recharge_description_02, NumberUtil.formatNumber(coinInfo?.minimumDepositAmount), wallet?.coinType))
                binding?.description02?.visibility = View.VISIBLE
            } else {
                binding?.description01?.setText(getString(R.string.recharge_description_03, wallet?.coinType, coinInfo?.blockConfirm.toString(), NumberUtil.formatNumber(coinInfo?.minimumDepositAmount), wallet?.coinType))
                binding?.description02?.setText("")
                binding?.description02?.visibility = View.GONE
            }
        }
        if (TextUtils.equals("USDT", coinType)) {
            binding?.description03?.visibility = View.VISIBLE
        } else {
            binding?.description03?.visibility = View.GONE
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

    open fun refreshWallet(wallet: Wallet?, coinInfo: CoinInfo?) {
        this.wallet = wallet
        coinType = wallet?.coinType
        memoNeeded = coinInfo != null && coinInfo.memoNeeded
        this.coinInfo = coinInfo
        requestRefresh()
    }

    private fun getCoinInfo(coinType: String?): Observable<CoinInfo?>? {
        return if (coinType == null) {
            Observable.just(null)
        } else {
            WalletApiServiceHelper.getCoinInfo(this, coinType)
                    ?.flatMap { info: CoinInfo? ->
                        this@RechargeActivity.coinInfo = info
                        memoNeeded = info != null && info.memoNeeded
                        Observable.just(info)
                    }
        }
    }

    private fun getRechargeAddress(coinType: String?): Observable<WalletAddress?>? {
        return if (coinType == null) {
            Observable.just(null)
        } else {
            ApiManager.build(this).getService(WalletApiService::class.java)
                    ?.getExchangeAddress(coinType)
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
            if (coinInfo != null && coinInfo != null) {
                refreshWallet(wallet, coinInfo)
            } else {
                //判断充值提现开关是否开启
                showLoading()
                getCoinInfo(coinType)
                        ?.flatMap { info: CoinInfo? ->
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
                                refreshWallet(wallet, coinInfo)
                            }

                            override fun callback(result: WalletAddress?) {
                                wallet?.coinWallet = if (result?.address == null) result?.account else result.address
                                wallet?.memo = result?.memo
                                wallet?.minChainDepositAmt = result?.minChainDepositAmt
                                refreshWallet(wallet, coinInfo)
                            }

                        })

            }
        }
    }
}