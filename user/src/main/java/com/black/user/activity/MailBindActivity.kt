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
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityMailBindBinding
import com.black.util.Callback
import com.black.util.CommonUtil

//邮箱验证绑定
@Route(value = [RouterConstData.EMAIL_BIND], beforePath = RouterConstData.LOGIN)
class MailBindActivity : BaseActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityMailBindBinding? = null

    private val mHandler = Handler()

 /*   private var getPhoneCodeLocked = false
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
*/
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
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mail_bind)
//        binding?.phoneCode?.addTextChangedListener(watcher)
//        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.mailAccount?.addTextChangedListener(watcher)
        binding?.mailCode?.addTextChangedListener(watcher)
        binding?.getMailCode?.setOnClickListener(this)
//        binding?.googleCode?.addTextChangedListener(watcher)
 /*       if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            binding?.googleCode?.visibility = View.VISIBLE
            binding?.googleCodeLayout?.visibility = View.VISIBLE
            binding?.googleCodeCopy?.visibility = View.VISIBLE
            binding?.googleWindow?.visibility =View.VISIBLE
        } else {
            binding?.googleCode?.visibility = View.GONE
            binding?.googleCodeLayout?.visibility = View.GONE
            binding?.googleCodeCopy?.visibility = View.GONE
            binding?.googleWindow?.visibility =View.GONE
        }
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
            binding?.phoneCode?.visibility = View.VISIBLE
            binding?.phoneLayout?.visibility = View.VISIBLE
            binding?.getPhoneCode?.visibility = View.VISIBLE
            binding?.phoneAccount?.visibility =View.VISIBLE
        } else {
            binding?.phoneCode?.visibility = View.GONE
            binding?.phoneLayout?.visibility = View.GONE
            binding?.getPhoneCode?.visibility = View.GONE
            binding?.phoneAccount?.visibility =View.GONE
        }*/
        binding?.btnSubmit?.setOnClickListener(this)
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.safe_mail_check)
    }

    override fun onClick(v: View) {
        when (v.id) {
           /* R.id.get_phone_code -> {
                phoneVerifyCode
            }*/
            R.id.get_mail_code -> {
                mailVerifyCode
            }
           /* R.id.btn_copy_google_code -> {
                CommonUtil.pasteText(mContext, object : Callback<String?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: String?) {
                        binding?.googleCode?.setText(returnData ?: "")
                    }
                })
            }*/
            R.id.btn_submit -> {
                bindMail()

            }
        }
    }

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding?.mailAccount?.text.toString().trim { it <= ' ' })
       //         || TextUtils.isEmpty(binding?.phoneCode?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.mailCode?.text.toString().trim { it <= ' ' })) {
            binding?.btnSubmit?.isEnabled = false
        } else {
            binding?.btnSubmit?.isEnabled = !(TextUtils.equals("1", userInfo!!.googleSecurityStatus))
                    //&& TextUtils.isEmpty(binding?.googleCode?.text.toString().trim { it <= ' ' }))
        }
    }

    //获取手机验证码
/*    private val phoneVerifyCode: Unit
        get() {
            if (getPhoneCodeLocked) {
                return
            }
            UserApiServiceHelper.getVerifyCode(this, userInfo!!.tel, userInfo!!.telCountryCode, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        //锁定发送按钮
                        if (!getPhoneCodeLocked) {
                            getPhoneCodeLocked = true
                            getPhoneCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getPhoneCodeLockTimer)
                        }
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }
*/
    //获取邮箱验证码
    private val mailVerifyCode: Unit
        get() {
            if (getMailCodeLocked) {
                return
            }
            val userName = binding?.mailAccount?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
                return
            }
            UserApiServiceHelper.getVerifyCode(this, userName, null, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        //锁定发送按钮
                        if (!getMailCodeLocked) {
                            getMailCodeLocked = true
                            getMailCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getMailCodeLockTimer)
                        }
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }

    //绑定邮箱验证
    private fun bindMail() {
     /*   var phoneCode: String? = null
        if (TextUtils.equals("1", userInfo!!.phoneSecurityStatus)) {
         phoneCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(phoneCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_phone_code))
                return
            }
        }*/
        val userName = binding?.mailAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
            return
        }
        val mailCode = binding?.mailCode?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(mailCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
            return
        }
      /*  var googleCode: String? = null
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
            googleCode = binding?.googleCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                return
            }
        }*/
        UserApiServiceHelper.bindEmail(mContext,  userName, mailCode,  object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_bind_success))
                    onBindSuccess()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun onBindSuccess() {
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .go(mContext)
                }
            }

            override fun error(type: Int, error: Any) {
                //回到安全中心界面
                BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .go(mContext)
            }
        })
    }
}