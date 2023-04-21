package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.wallet.*
import com.black.base.net.NormalObserver
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityRechargeBinding
import com.google.zxing.WriterException
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_recharge.*
import skin.support.content.res.SkinCompatResources
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.RECHARGE])
open class RechargeActivity : BaseActivity(), View.OnClickListener {

    private var wallet: Wallet? = null//在这里只使用了币种名称
    private var coinType: String? = null
    private var coinInfo: CoinInfoType? = null
    private var coinChain: String? = null
    private var chainNames: ArrayList<String>? = null

    private var coinInfoReal: CoinInfo? = null
    private var memoNeeded = false

    private var binding: ActivityRechargeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        if (coinType == null){
            coinType = "USDT"
        }
        else {
            coinType = wallet?.coinType
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recharge)

        val actionBarRecord: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        actionBarRecord?.visibility = View.VISIBLE
        actionBarRecord?.setOnClickListener(this)

        binding?.chooseCoinLayout?.setOnClickListener(this)
        binding?.btnSaveQrcode?.setOnClickListener(this)
        binding?.btnCopyAddress?.setOnClickListener(this)
        binding?.btnCopyMemo?.setOnClickListener(this)
        binding?.chooseChainLayout?.setOnClickListener(this)
        binding?.btnSaveQrcode?.setOnClickListener(this)
        when {
            wallet != null -> {
                selectCoin(wallet)
            }
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.rechange) + coinType
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_action_bar_right -> {
                val extras = Bundle()
                extras.putParcelable(ConstData.WALLET, wallet)
                extras.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
                BlackRouter.getInstance().build(RouterConstData.FINANCIAL_RECORD).with(extras)
                    .go(this)
            }
            R.id.btn_copy_address -> {
                if (CommonUtil.copyText(mContext, wallet?.coinWallet)) {
                    FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
                }
            }
            R.id.choose_chain_layout -> {
                showChainChooseDialog()
            }
            R.id.btn_copy_memo -> {
                if (CommonUtil.copyText(mContext, wallet?.memo)) {
                    FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
                }
            }
            R.id.choose_coin_layout -> {
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN)
                    .go(this)
            }
            R.id.btn_save_qrcode -> {
                requestStoragePermissions(Runnable {
                    try {
                        ImageUtil.saveScreenShotFromActivit(this)
                        FryingUtil.showToast(
                            mContext,
                            mContext.getString(com.black.base.R.string.save_success)
                        )
                    } catch (e: Exception) {
                        FryingUtil.showToast(
                            mContext,
                            mContext.getString(com.black.base.R.string.save_failed)
                        )
                    }
                })
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
            }
        }
    }

    private fun showChainChooseDialog() {
        if (coinChain != null && chainNames!!.size > 0) {
            var chooseWalletDialog = ChooseWalletControllerWindow(mContext as Activity,
                getString(R.string.get_chain_name),
                coinChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(
                        window: ChooseWalletControllerWindow<String?>,
                        item: String?
                    ) {
                          binding?.currentChain?.setText(item)
                          coinChain = item
                    }
                })

            chooseWalletDialog.setTipsText(getString(R.string.chain_tips))
            chooseWalletDialog.setTipsTextVisible(true)
            chooseWalletDialog.show()
        }

    }

    private fun selectCoin(wallet: Wallet?) {
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
        binding?.address?.setText(R.string.recharge_not_support)
        binding?.memoText?.setText(R.string.recharge_not_support)
        binding?.btnSaveQrcode?.isEnabled = false
        binding?.btnCopyAddress?.isEnabled = false
        binding?.btnCopyMemo?.isEnabled = false
        if (TextUtils.isEmpty(coinType) && coinInfo != null) {
            if (memoNeeded) {
                binding?.description01?.setText(
                    getString(
                        R.string.recharge_description_01,
                        coinType
                    )
                )
                binding?.description02?.setText(
                    getString(
                        R.string.recharge_description_02,
                        NumberUtil.formatNumber(coinInfoReal?.minWithdrawSingle),
                        coinType
                    )
                )
                binding?.description02?.visibility = View.VISIBLE
            } else {
                binding?.description01?.setText(
                    getString(
                        R.string.recharge_description_03,
                        coinType,
                        coinInfoReal?.blockConfirm.toString(),
                        NumberUtil.formatNumber(coinInfoReal?.minimumDepositAmount),
                        coinType
                    )
                )
                binding?.description02?.setText("")
                binding?.description02?.visibility = View.GONE
            }
        } else {
            binding?.description01?.setText("")
            binding?.description02?.setText("")
        }
    }

    private fun showCoinAddress(wallet: Wallet?) {
        if (memoNeeded) {
            binding?.memoLayout?.visibility = View.VISIBLE
            binding?.memoText?.setText(if (wallet?.memo == null) "" else wallet.memo)
        } else {
            binding?.memoLayout?.visibility = View.GONE
        }
        if (coinInfoReal == null || true != coinInfoReal!!.supportDeposit) {
            binding?.qrcode?.setImageResource(R.drawable.icon_recharge_false)
            binding?.address?.setText(R.string.recharge_not_support)
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
                    qrcodeBitmap = CommonUtil.createQRCode(
                        address,
                        300,
                        0,
                        SkinCompatResources.getColor(mContext, R.color.T14)
                    )
                } catch (e: WriterException) {
                    CommonUtil.printError(applicationContext, e)
                }
                if (qrcodeBitmap != null) {
                    binding?.qrcode?.setImageBitmap(qrcodeBitmap)
                    binding?.address?.setText(address)
                }
            } else {
                binding?.qrcode?.setImageResource(R.drawable.icon_recharge_false)
            }
            if (memoNeeded) {
                binding?.description01?.setText(
                    getString(
                        R.string.recharge_description_01,
                        wallet?.coinType
                    )
                )
//                binding?.description02?.setText(getString(R.string.recharge_description_02, NumberUtil.formatNumber(coinInfo?.minimumDepositAmount), wallet?.coinType))
                binding?.description02?.visibility = View.VISIBLE
            } else {
//                binding?.description01?.setText(getString(R.string.recharge_description_03, wallet?.coinType, coinInfo?.blockConfirm.toString(), NumberUtil.formatNumber(coinInfo?.minimumDepositAmount), wallet?.coinType))
                binding?.description02?.setText("")
                binding?.description02?.visibility = View.GONE
            }
        }
    }

    open fun refreshWallet(wallet: Wallet?, coinInfo: CoinInfoType?) {
        this.wallet = wallet
        coinType = wallet?.coinType
        memoNeeded = coinInfo != null && coinInfo?.config?.get(0)?.coinConfigVO?.memoNeeded == true
        this.coinInfo = coinInfo
        requestRefresh()
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

    private fun setChainNames(info: CoinInfoType?) {
        chainNames = ArrayList()
        var chainConfig = info?.config
        if (chainConfig != null) {
            for (i in chainConfig) {
                i.chain?.let { chainNames?.add(it) }
            }
        }
    }

    private fun getRechargeAddress(coinType: String?): Observable<WalletAddress?>? {
        return if (coinType == null) {
            Observable.just(null)
        } else {
            ApiManager.build(this, UrlConfig.ApiType.URL_PRO)
                .getService(WalletApiService::class.java)
                ?.getExchangeAddress(coinType, coinChain)
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
                refreshWallet(wallet, coinInfo)
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
                            refreshWallet(wallet, coinInfo)
                        }

                        override fun callback(result: WalletAddress?) {
                            wallet?.coinWallet =
                                if (result?.address == null) result?.account else result.address
                            wallet?.memo = result?.memo
                            wallet?.minChainDepositAmt = result?.minChainDepositAmt
                            refreshWallet(wallet, coinInfo)
                        }

                    })

            }
        }
    }
}