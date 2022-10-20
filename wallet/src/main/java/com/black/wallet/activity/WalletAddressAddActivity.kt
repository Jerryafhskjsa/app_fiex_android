package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.WalletWithdrawAddress
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivityWalletAddressAddBinding

@Route(value = [RouterConstData.WALLET_ADDRESS_ADD])
class WalletAddressAddActivity : BaseActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null
    private var coinInfo: CoinInfo? = null
    private var coinAddress: WalletWithdrawAddress? = null
    private var coinType: String? = null
    private var coinChain: String? = null

    private var binding: ActivityWalletAddressAddBinding? = null


    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(this)
        coinChain = intent.getStringExtra(ConstData.COIN_CHAIN)
        coinInfo = intent.getParcelableExtra(ConstData.COIN_INFO)
        coinAddress = intent.getParcelableExtra(ConstData.COIN_ADDRESS)
        coinType = coinInfo?.coinType
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_address_add)
        binding?.extractAddress?.addTextChangedListener(watcher)
        binding?.remark?.addTextChangedListener(watcher)
        binding?.memo?.addTextChangedListener(watcher)
        binding?.scanQrcode?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        if (coinInfo == null || !coinInfo!!.memoNeeded) {
            binding?.memoLayout?.visibility = View.GONE
        } else {
            binding?.memoLayout?.visibility = View.VISIBLE
        }
        checkClickable()
        if(coinAddress != null){
           binding!!.remark?.setText(coinAddress?.name)
           binding?.extractAddress?.setText(coinAddress?.coinWallet)
           binding?.memo?.setText(coinAddress?.memo)
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.add_address, coinType ?: "")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scan_qrcode -> {
                requestCameraPermissions(Runnable {
                    BlackRouter.getInstance().build(RouterConstData.CAPTURE)
                        .withRequestCode(ConstData.SCANNIN_GREQUEST_CODE)
                        .go(mContext)
                })
            }
            R.id.btn_confirm -> {
                val name = binding!!.remark?.text.toString()
                val address = binding?.extractAddress?.text.toString()
                val memo = binding?.memo?.text.toString()
                if(coinAddress != null){
                    var id = coinAddress?.id.toString()
                    WalletApiServiceHelper.updateWalletAddress(
                        this,
                        id,
                        coinType,
                        name,
                        address,
                        memo,
                        null,
                        object : NormalCallback<HttpRequestResultString?>() {
                            override fun callback(returnData: HttpRequestResultString?) {
                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    FryingUtil.showToast(
                                        mContext,
                                       returnData.msg
                                    )
                                    setResult(RESULT_OK, null)
                                    finish()
                                } else {
                                    FryingUtil.showToast(
                                        mContext,
                                        if (returnData == null) getString(R.string.error_data) else returnData.msg
                                    )
                                }
                            }
                        })
                }else{
                    WalletApiServiceHelper.addWalletAddress(
                        this,
                        coinType,
                        name,
                        address,
                        memo,
                        null,
                        object : NormalCallback<HttpRequestResultString?>() {
                            override fun callback(returnData: HttpRequestResultString?) {
                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    setResult(RESULT_OK, null)
                                    finish()
                                } else {
                                    FryingUtil.showToast(
                                        mContext,
                                        if (returnData == null) getString(R.string.error_data) else returnData.msg
                                    )
                                }
                            }
                        })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val bundle = data?.extras
            when (requestCode) {
                ConstData.SCANNIN_GREQUEST_CODE -> {
                    val scanResult = bundle?.getString("result")
                    binding?.extractAddress?.setText(scanResult ?: "")
                }
            }
        }
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding!!.extractAddress.text.toString().trim { it <= ' ' })
            || TextUtils.isEmpty(binding?.remark?.text.toString().trim { it <= ' ' })
        ) {
            binding?.btnConfirm?.isEnabled = false
            return
        }
        if (coinInfo != null && coinInfo?.memoNeeded!!
            && binding?.memo?.text.toString().trim { it <= ' ' }.isEmpty()
        ) {
            binding?.btnConfirm?.isEnabled = false
            return
        }
        binding?.btnConfirm?.isEnabled = true
    }
}