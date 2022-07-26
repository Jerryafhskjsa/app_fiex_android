package com.black.user.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityMoneyPasswordBinding
import com.black.util.Callback
import com.black.util.RSAUtil

@Route(value = [RouterConstData.MONEY_PASSWORD], beforePath = RouterConstData.LOGIN)
class MoneyPasswordActivity : BaseActivity(), View.OnClickListener {
    private var moneyPasswordType = ConstData.MONEY_PASSWORD_SET
    private var userInfo: UserInfo? = null

    private var binding: ActivityMoneyPasswordBinding? = null

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
        moneyPasswordType = intent.getIntExtra(ConstData.MONEY_PASSWORD_TYPE, ConstData.MONEY_PASSWORD_SET)
        userInfo = CookieUtil.getUserInfo(this)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_money_password)
        binding?.password?.addTextChangedListener(watcher)
        binding?.passwordAgain?.addTextChangedListener(watcher)
        binding?.phoneCode?.addTextChangedListener(watcher)
        //        binding?.phoneCode?.setAssistButtonCallback(this);
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.googleCode?.addTextChangedListener(watcher)
        binding?.mailCode?.addTextChangedListener(watcher)
        //        binding?.mailCode?.setAssistButtonCallback(this);
        binding?.getMailCode?.setOnClickListener(this)
        binding?.loginPassword?.addTextChangedListener(watcher)
        if (moneyPasswordType == ConstData.MONEY_PASSWORDR_RESET) {
            binding?.password?.setHint(R.string.input_money_password_reset)
            binding?.passwordAgain?.setHint(R.string.input_money_password_reset_again)
            if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
                binding?.googleLayout?.visibility = View.VISIBLE
            } else {
                binding?.googleLayout?.visibility = View.GONE
            }
            binding?.loginPassword?.visibility = View.VISIBLE
            binding?.resetWarning?.visibility = View.VISIBLE
        } else {
            binding?.googleLayout?.visibility = View.GONE
            binding?.loginPassword?.visibility = View.GONE
            binding?.resetWarning?.visibility = View.GONE
        }
        binding?.btnConfirm?.setOnClickListener(this)
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
            binding?.phoneLayout?.visibility = View.VISIBLE
            binding?.mailLayout?.visibility = View.GONE
        } else {
            binding?.mailLayout?.visibility = View.VISIBLE
            binding?.phoneLayout?.visibility = View.GONE
        }
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.money_password)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.get_phone_code -> {
                phoneVerifyCode
            }
            R.id.get_mail_code -> {
                mailVerifyCode
            }
            R.id.btn_confirm -> {
                setMoneyPassword()
            }
        }
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding?.password?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.passwordAgain?.text.toString().trim { it <= ' ' })) {
            binding?.btnConfirm?.isEnabled = false
        } else {
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
            if (binding?.googleLayout?.visibility == View.VISIBLE
                    && binding?.googleCode?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            if (binding?.loginPassword?.visibility == View.VISIBLE
                    && binding?.loginPassword?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            binding?.btnConfirm?.isEnabled = true
        }
    }

    //发送成功后，锁定按钮
    //获取手机验证码
    val phoneVerifyCode: Unit
        get() {
            if (getPhoneCodeLocked) {
                return
            }
            UserApiServiceHelper.getVerifyCode(mContext, userInfo!!.tel, userInfo!!.telCountryCode, object : NormalCallback<HttpRequestResultString?>() {
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
                        FryingUtil.showToast(mContext, returnData?.msg)
                    }
                }
            })
        }
    //锁定发送按钮
    //获取邮箱验证码
    val mailVerifyCode: Unit
        get() {
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

    private fun setMoneyPassword() {
        var password: String? = binding?.password?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.input_money_password))
            return
        }
        val passwordAgain = binding?.passwordAgain?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(passwordAgain)) {
            FryingUtil.showToast(mContext, getString(R.string.input_money_password_again))
            return
        }
        if (!TextUtils.equals(password, passwordAgain)) {
            FryingUtil.showToast(mContext, getString(R.string.password_not_same))
            return
        }
        password = RSAUtil.encryptDataByPublicKey(password)
        val phoneCode = binding?.phoneCode?.text.toString()
        val mailCode = binding?.mailCode?.text.toString()
        val callback: Callback<HttpRequestResultString?> = object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    onSuccess()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                }
            }
        }
        if (moneyPasswordType == ConstData.MONEY_PASSWORDR_RESET) {
            var googleCode: String? = null
            if (binding?.googleLayout?.visibility == View.VISIBLE) {
                googleCode = binding?.googleCode?.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(googleCode)) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                    return
                }
            }
            var loginPassword: String? = binding?.loginPassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(loginPassword)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_password))
                return
            }
            loginPassword = RSAUtil.encryptDataByPublicKey(loginPassword)
            UserApiServiceHelper.resetMoneyPassword(mContext, password, phoneCode, mailCode, googleCode, loginPassword, callback)
        } else {
            UserApiServiceHelper.setMoneyPassword(mContext, password, phoneCode, mailCode, callback)
        }
    }

    private fun onSuccess() {
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    FryingUtil.showToast(mContext, getString(if (moneyPasswordType == ConstData.MONEY_PASSWORDR_RESET) R.string.reset_money_password_success else R.string.set_money_password_success))
                    finish()
                }
            }

            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(mContext, error.toString())
            }
        }, true)
    }
}