package com.black.user.activity

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
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityChangePasswordBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.RSAUtil

@Route(value = [RouterConstData.CHANGE_PASSWORD], beforePath = RouterConstData.LOGIN)
class ChangePasswordActivity : BaseActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityChangePasswordBinding? = null

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
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding?.currentPassword?.addTextChangedListener(watcher)
        binding?.newPassword?.addTextChangedListener(watcher)
        binding?.newPasswordAgain?.addTextChangedListener(watcher)
        binding?.phoneCode?.addTextChangedListener(watcher)
        //        binding?.phoneCode?.setAssistButtonCallback(this);
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.mailCode?.addTextChangedListener(watcher)
        //        binding?.mailCode?.setAssistButtonCallback(this);
        binding?.getMailCode?.setOnClickListener(this)
        binding?.googleCode?.addTextChangedListener(watcher)
        binding?.btnConfirm?.setOnClickListener(this)
        var check = false
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
            check = true
//            binding?.phoneLayout?.visibility = View.VISIBLE
        } else {
            binding?.phoneLayout?.visibility = View.GONE
        }
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            check = true
            binding?.googleCode?.visibility = View.VISIBLE
        } else {
            binding?.googleCode?.visibility = View.GONE
        }
        if (!check) {
            binding?.mailLayout?.visibility = View.VISIBLE
        } else {
            binding?.mailLayout?.visibility = View.GONE
        }
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.change_password)
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        //不需要打开需要登录的目标
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_copy_google_code -> {
                CommonUtil.pasteText(mContext, object : Callback<String?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: String?) {
                        binding?.googleCode?.setText(returnData ?: "")
                    }
                })
            }
            R.id.get_phone_code -> {
                phoneVerifyCode
            }
            R.id.get_mail_code -> {
                mailVerifyCode
            }
            R.id.btn_confirm -> {
                changePassword()
            }
        }
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding?.currentPassword?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.newPassword?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.newPasswordAgain?.text.toString().trim { it <= ' ' })) {
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
            if (binding?.googleCode?.visibility == View.VISIBLE
                    && binding?.googleCode?.text.toString().trim { it <= ' ' }.isEmpty()) {
                binding?.btnConfirm?.isEnabled = false
                return
            }
            binding?.btnConfirm?.isEnabled = true
        }
    }//发送成功后，锁定按钮

    //获取手机验证码
    private val phoneVerifyCode: Unit
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
                        FryingUtil.showToast(mContext, if (returnData == null) "" else returnData.msg)
                    }
                }
            })
        }//锁定发送按钮

    //获取邮箱验证码
    private val mailVerifyCode: Unit
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

    private fun changePassword() {
        var password = binding?.currentPassword?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_login_password))
            return
        }
        var newPassword: String? = binding?.newPassword?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
            return
        }
        if (password.length < 8) {
            FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
            return
        }
        val newPasswordAgain = binding?.newPasswordAgain?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_login_password))
            return
        }
        if (!TextUtils.equals(newPassword, newPasswordAgain)) {
            FryingUtil.showToast(mContext, getString(R.string.password_not_same))
            return
        }
        password = RSAUtil.encryptDataByPublicKey(password)
        newPassword = RSAUtil.encryptDataByPublicKey(newPassword)
        val phoneCode = binding?.phoneCode?.text.toString()
        val mailCode = binding?.mailCode?.text.toString()
        val googleCode = binding?.googleCode?.text.toString()
        UserApiServiceHelper.changePassword(mContext, password, newPassword, phoneCode, googleCode, mailCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //密码修改成功，使用新密码登录
                    onGetTokenSuccess()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                }
            }
        })
    }

    private fun onGetTokenSuccess() {

        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    FryingUtil.showToast(mContext, getString(R.string.change_password_success_02))
                      val intent = Intent(this@ChangePasswordActivity, SafeBindActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(mContext, error.toString())
            }
        }, true)

}
}