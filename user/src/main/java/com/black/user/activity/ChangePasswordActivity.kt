package com.black.user.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
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
    private var countDownTimer: CountDownTimer? = null
    private var totalTime: Long = 60 * 1000


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
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.googleCode?.addTextChangedListener(watcher)
        binding?.googleCodeCopy?.setOnClickListener(this)
        binding?.mailCode?.addTextChangedListener(watcher)
        binding?.getMailCode?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
            binding?.phoneLayout?.visibility = View.VISIBLE
            binding?.phoneAccount?.visibility = View.VISIBLE
            binding?.phoneCode?.visibility = View.VISIBLE
            binding?.getPhoneCode?.visibility = View.VISIBLE
        } else {
            binding?.phoneLayout?.visibility = View.GONE
            binding?.phoneAccount?.visibility = View.GONE
            binding?.mailCode?.visibility = View.GONE
            binding?.getMailCode?.visibility = View.GONE
        }
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            binding?.googleCodeLayout?.visibility = View.VISIBLE
            binding?.googleAccount?.visibility = View.VISIBLE
            binding?.googleCode?.visibility = View.VISIBLE
            binding?.googleCodeCopy?.visibility = View.VISIBLE
        } else {
            binding?.googleCodeLayout?.visibility = View.GONE
            binding?.googleAccount?.visibility = View.GONE
            binding?.googleCode?.visibility = View.GONE
            binding?.googleCodeCopy?.visibility = View.GONE
        }
        if (TextUtils.equals("1", userInfo!!.emailSecurityStatus)) {
            binding?.mailLayout?.visibility = View.VISIBLE
            binding?.mailAccount?.visibility = View.VISIBLE
            binding?.mailCode?.visibility = View.VISIBLE
            binding?.getMailCode?.visibility = View.VISIBLE
        } else {
            binding?.mailLayout?.visibility = View.GONE
            binding?.mailAccount?.visibility = View.GONE
            binding?.mailCode?.visibility = View.GONE
            binding?.getMailCode?.visibility = View.GONE
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
            R.id.google_code_copy -> {
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
            countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
            override fun onFinish(){
                binding?.getPhoneCode?.isEnabled = true
                binding?.getPhoneCode?.setText(getString(R.string.send_code))
            }
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000 % 60
                    binding?.getPhoneCode?.isEnabled = false
                    binding?.getPhoneCode?.setText("$second")
                }
            }.start()
            UserApiServiceHelper.getSendVerifyCode(mContext, userInfo!!.tel, userInfo!!.telCountryCode, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(com.black.base.R.string.alert_verify_code_success))
                        //发送成功后，锁定按钮
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "" else returnData.msg)
                    }
                }
            })
        }//锁定发送按钮

    //获取邮箱验证码
    private val mailVerifyCode: Unit
        get() {
            countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
            override fun onFinish(){
                binding?.getMailCode?.isEnabled = true
                binding?.getMailCode?.setText(getString(R.string.send_code))
            }
                override fun onTick(millisUntilFinished: Long) {
                    val second = millisUntilFinished / 1000 % 60
                    binding?.getMailCode?.isEnabled = false
                    binding?.getMailCode?.setText("$second")
                }
            }.start()
            UserApiServiceHelper.getSendVerifyCode(mContext, userInfo!!.email, null, object : NormalCallback<HttpRequestResultString?>(mContext) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(com.black.base.R.string.alert_verify_code_success))
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
        var phoneCode: String? = null
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
            phoneCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(phoneCode)) {
                FryingUtil.showToast(mContext, getString(R.string.input_phone_code_veifily))
                return
            }
        }
        var mailCode: String? = null
        if (TextUtils.equals("1", userInfo!!.emailSecurityStatus)) {
          mailCode = binding?.mailCode?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(mailCode)) {
            FryingUtil.showToast(mContext, getString(R.string.input_mail_code))
            return
        }
        }
        var googleCode: String? = null
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            googleCode = binding?.googleCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.input_google_code))
                return
            }
        }
        password = RSAUtil.encryptDataByPublicKey(password)
        newPassword = RSAUtil.encryptDataByPublicKey(newPassword)

        UserApiServiceHelper.changePassword(mContext, password, newPassword, phoneCode, googleCode, mailCode, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
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
                      val intent = Intent(this@ChangePasswordActivity, LoginActivity::class.java)
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