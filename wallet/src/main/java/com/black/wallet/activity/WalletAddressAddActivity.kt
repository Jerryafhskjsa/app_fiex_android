package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.CoinInfo
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
    private var coinType: String? = null

    private var binding: ActivityWalletAddressAddBinding? = null

    private val mHandler = Handler()

    private var getPhoneCodeLocked = false
    private var getPhoneCodeLockedTime = 0
    private val getPhoneCodeLockTimer = object : Runnable {
        override fun run() {
            getPhoneCodeLockedTime--
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false
                binding?.getPhoneCode?.setText(R.string.get_check_code)
            } else {
                binding?.getPhoneCode?.setText(getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }

    }

    private var getMailCodeLocked = false
    private var getMailCodeLockedTime = 0
    private val getMailCodeLockTimer = object : Runnable {
        override fun run() {
            getMailCodeLockedTime--
            if (getMailCodeLockedTime <= 0) {
                getMailCodeLocked = false
                binding?.getMailCode?.setText(R.string.get_check_code)
            } else {
                binding?.getMailCode?.setText(getString(R.string.aler_get_code_locked, getMailCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }

    }

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
        coinInfo = intent.getParcelableExtra(ConstData.COIN_INFO)
        coinType = intent.getStringExtra(ConstData.COIN_TYPE)
        if (userInfo == null || coinInfo == null || coinType == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_address_add)
        binding?.scanQrcode?.setOnClickListener(this)
        binding?.phoneCode?.addTextChangedListener(watcher)
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.mailCode?.addTextChangedListener(watcher)
        binding?.getMailCode?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        if (coinInfo == null || !coinInfo!!.memoNeeded) {
            binding?.memoLayout?.visibility = View.GONE
        } else {
            binding?.memoLayout?.visibility = View.VISIBLE
        }
        if (userInfo?.registerFromMail()!!) {
            binding?.phoneLayout?.visibility = View.GONE
            binding?.mailLayout?.visibility = View.VISIBLE
        } else {
            binding?.phoneLayout?.visibility = View.VISIBLE
            binding?.mailLayout?.visibility = View.GONE
        }
        checkClickable()
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
            R.id.get_phone_code -> {
                getPhoneVerifyCode()
            }
            R.id.get_mail_code -> {
                getMailVerifyCode()
            }
            R.id.btn_confirm -> {
                val name = binding!!.remark?.text.toString()
                val address = binding?.extractAddress?.text.toString()
                val memo = binding?.memo?.text.toString()
                val verifyCode: String = if (userInfo!!.registerFromMail()) {
                    binding?.mailCode?.text.toString()
                } else {
                    binding?.phoneCode?.text.toString()
                }
                WalletApiServiceHelper.addWalletAddress(this, coinType, name, address, memo, verifyCode, object : NormalCallback<HttpRequestResultString?>() {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            setResult(Activity.RESULT_OK, null)
                            finish()
                        } else {
                            FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                        }
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data?.extras
            when (requestCode) {
                ConstData.SCANNIN_GREQUEST_CODE -> {
                    val scanResult = bundle?.getString("result")
                    binding?.extractAddress?.setText(scanResult ?: "")
                }
            }
        }
    }

    //获取手机验证码
    private fun getPhoneVerifyCode() {
        if (getPhoneCodeLocked) {
            return
        }
        UserApiServiceHelper.getVerifyCode(mContext, userInfo!!.tel, userInfo?.telCountryCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(com.black.base.R.string.alert_verify_code_success))
                    //发送成功后，锁定按钮
                    if (!getPhoneCodeLocked) {
                        getPhoneCodeLocked = true
                        getPhoneCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                        mHandler.post(getPhoneCodeLockTimer)
                    }
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //获取邮箱验证码
    private fun getMailVerifyCode() {
        if (getMailCodeLocked) {
            return
        }
        UserApiServiceHelper.getVerifyCode(mContext, userInfo!!.email, null, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(com.black.base.R.string.alert_verify_code_success))
                    //锁定发送按钮
                    if (!getMailCodeLocked) {
                        getMailCodeLocked = true
                        getMailCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                        mHandler.post(getMailCodeLockTimer)
                    }
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "" else returnData.msg)
                }
            }
        })
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding!!.extractAddress.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.remark?.text.toString().trim { it <= ' ' })) {
            binding?.btnConfirm?.isEnabled = false
        } else {
            if (coinInfo != null && coinInfo?.memoNeeded!!
                    && binding?.memo?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            if (binding?.phoneLayout?.visibility == View.VISIBLE
                    && binding?.phoneCode?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            if (binding?.mailLayout?.visibility == View.VISIBLE
                    && binding?.mailCode?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            binding?.btnConfirm?.isEnabled = true
        }
    }
}