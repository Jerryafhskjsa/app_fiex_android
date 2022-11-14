package com.black.user.activity

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
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.UserApiService
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.net.NormalObserver2
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.CountryChooseWindow
import com.black.base.view.CountryChooseWindow.OnCountryChooseListener
import com.black.net.HttpRequestResult
import com.black.net.RequestFunction
import com.black.net.RequestFunction2
import com.black.net.RequestObserveResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityForgetPasswordBinding
import com.black.util.RSAUtil
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

//找回密码
@Route(value = [RouterConstData.FORGET_PASSWORD])
open class ForgetPasswordActivity : BaseActivity(), View.OnClickListener {
    private var type = 0

    private var binding: ActivityForgetPasswordBinding? = null

    private var phoneCaptcha: String? = null
    private var mailCaptcha: String? = null

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

    private var thisCountry: CountryCode? = null
    private var chooseWindow: CountryChooseWindow? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password)
        type = intent.getIntExtra(ConstData.TYPE, ConstData.AUTHENTICATE_TYPE_NONE)
        val account = intent.getStringExtra(ConstData.ACCOUNT)
        if (type != ConstData.AUTHENTICATE_TYPE_PHONE && type != ConstData.AUTHENTICATE_TYPE_MAIL) {
            FryingUtil.showToast(mContext, getString(R.string.alert_phone_or_mail))
            finish()
        }
        binding?.tabPhone?.setOnClickListener(this)
        binding?.tabEmail?.setOnClickListener(this)
        /*****手机找回密码 */
        binding?.countryCode?.isFocusable = false
        binding?.countryCode?.isFocusableInTouchMode = false
        binding?.countryCode?.tag = "86"
        binding?.countryCode?.setOnClickListener(this)
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE && !TextUtils.isEmpty(account)) {
            binding?.phoneAccount?.setText(account)
        }
        binding?.phoneAccount?.addTextChangedListener(watcher)
        binding?.phoneCode?.addTextChangedListener(watcher)
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.phonePassword?.addTextChangedListener(watcher)
        /*****手机找回密码 */
        /*****邮箱找回密码 */
        if (type == ConstData.AUTHENTICATE_TYPE_MAIL && !TextUtils.isEmpty(account)) {
            binding?.mailAccount?.setText(account)
        }
        binding?.mailAccount?.addTextChangedListener(watcher)
        binding?.mailPassword?.addTextChangedListener(watcher)
        binding?.mailCode?.addTextChangedListener(watcher)
        binding?.getMailCode?.setOnClickListener(this)
        /*****邮箱找回密码 */
        binding?.btnConfirm?.setOnClickListener(this)
        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "86"
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object : OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode?.tag = thisCountry!!.code
                binding?.countryCode?.setText("+" + thisCountry?.code)
            }

        })
        initChooseWindowData()
        refreshAccountLayout()
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun needGeeTest(): Boolean {
        return true
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        //不需要打开需要登录的目标
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.tab_phone) {
            //切换类型
            type = when (type) {
                ConstData.AUTHENTICATE_TYPE_PHONE -> {
                    ConstData.AUTHENTICATE_TYPE_MAIL
                }
                ConstData.AUTHENTICATE_TYPE_MAIL -> {
                    ConstData.AUTHENTICATE_TYPE_PHONE
                }
                else -> {
                    ConstData.AUTHENTICATE_TYPE_NONE
                }
            }
            checkClickable()
            refreshAccountLayout()
        } else if (i == R.id.tab_email) {
            //切换类型
            type = when (type) {
                ConstData.AUTHENTICATE_TYPE_PHONE -> {
                    ConstData.AUTHENTICATE_TYPE_MAIL
                }
                ConstData.AUTHENTICATE_TYPE_MAIL -> {
                    ConstData.AUTHENTICATE_TYPE_PHONE
                }
                else -> {
                    ConstData.AUTHENTICATE_TYPE_NONE
                }
            }
            checkClickable()
            refreshAccountLayout()
        } else if (i == R.id.country_code) {
            //切换国家区号
            chooseCountryCode()
        } else if (i == R.id.get_phone_code || i == R.id.get_mail_code) {
            //获取验证码
            if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
                getPhoneVerifyCode()
            } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
                getMailVerifyCode()
            }
        } else if (i == R.id.btn_confirm) {
            //确定
            resetPassword()
        }
    }

    override fun onCountryCodeChoose(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val code = data?.getStringExtra(ConstData.COUNTRY_CODE)
            if (!TextUtils.isEmpty(code)) {
                binding?.countryCode?.tag = code
                binding?.countryCode?.setText("+$code")
            }
        }
    }

    private fun checkClickable() {
        when (type) {
            ConstData.AUTHENTICATE_TYPE_PHONE ->
                binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.phoneAccount?.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding?.phonePassword?.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding?.phoneCode?.text.toString().trim { it <= ' ' }))
            ConstData.AUTHENTICATE_TYPE_MAIL ->
                binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.mailAccount?.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding?.mailPassword?.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding?.mailCode?.text.toString().trim { it <= ' ' }))
            else -> {
            }
        }
    }

    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(this, false, object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    chooseWindow?.setCountryList(returnData.data)
                }
            }
        })
    }

    private fun chooseCountryCode() {
        chooseWindow?.show(thisCountry)
    }

    //切换账号类型
    private fun refreshAccountLayout() {
        when (type) {
            ConstData.AUTHENTICATE_TYPE_PHONE -> {
                binding?.phoneAccountLayout?.visibility = View.VISIBLE
                binding?.mailAccountLayout?.visibility = View.GONE
                binding?.tabPhone?.isChecked = true
                binding?.tabEmail?.isChecked = false
            }
            ConstData.AUTHENTICATE_TYPE_MAIL -> {
                binding?.phoneAccountLayout?.visibility = View.GONE
                binding?.mailAccountLayout?.visibility = View.VISIBLE
                binding?.tabPhone?.isChecked = false
                binding?.tabEmail?.isChecked = true
            }
            else -> {
                binding?.phoneAccountLayout?.visibility = View.GONE
                binding?.mailAccountLayout?.visibility = View.GONE
            }
        }
    }

    //获取手机验证码
    private fun getPhoneVerifyCode() {
        if (getPhoneCodeLocked) {
            return
        }
        phoneCaptcha = null
        val telCountryCode: String? = if (binding?.countryCode?.tag == null) null else binding?.countryCode?.tag.toString()
        if (TextUtils.isEmpty(telCountryCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_choose_country))
            return
        }
        val userName: String = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
            return
        }
        UserApiServiceHelper.getVerifyCode(this, userName, telCountryCode, true, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                    phoneCaptcha = returnData.data
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

    //获取邮箱验证码
    private fun getMailVerifyCode() {
        if (getMailCodeLocked) {
            return
        }
        mailCaptcha = null
        val userName: String = binding?.mailAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
            return
        }
        UserApiServiceHelper.getVerifyCode(this, userName, null, true, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                    mailCaptcha = returnData.data
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

    private fun resetPassword() {
        var userName: String? = null
        var telCountryCode: String? = null
        var verifyCode: String? = null
        var captcha: String? = null
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
            captcha = phoneCaptcha
            //进行手机密码找回
            telCountryCode = if (binding?.countryCode?.tag == null) null else binding?.countryCode?.tag.toString()
            if (TextUtils.isEmpty(telCountryCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_choose_country))
                return
            }
            userName = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
                return
            }
            verifyCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(verifyCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_phone_code))
                return
            }
            val password: String = binding?.phonePassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
                return
            }
            if (password.length < 8) {
                FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
                return
            }
//            if (TextUtils.isDigitsOnly(password)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//                return
//            }
        } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
            captcha = mailCaptcha
            //进行邮箱密码找回
            userName = binding?.mailAccount?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
                return
            }
            val password: String = binding?.mailPassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
                return
            }
            if (password.length < 8) {
                FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
                return
            }
//            if (TextUtils.isDigitsOnly(password)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//                return
//            }
            verifyCode = binding?.mailCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(verifyCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                return
            }
        }
        doResetPassword(userName, telCountryCode, verifyCode, captcha)
    }

    open fun doResetPassword(userName: String?, telCountryCode: String?, verificationCode: String?, captcha: String?) {
        showLoading()
        ApiManager.build(this, true).getService(UserApiService::class.java)
                ?.resetPassword(userName, telCountryCode, verificationCode, captcha)
                ?.materialize()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.flatMap(object : RequestFunction<HttpRequestResultString, RequestObserveResult<HttpRequestResultString?>>() {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    override fun applyResult(returnData: HttpRequestResultString?): Observable<RequestObserveResult<HttpRequestResultString?>> {
                        if (returnData != null) {
                            when (returnData.code) {
                                ConstData.AUTHENTICATE_CODE_MAIL,
                                ConstData.AUTHENTICATE_CODE_PHONE,
                                ConstData.AUTHENTICATE_CODE_GOOGLE,
                                ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE -> {
                                    val prefixAuth = returnData.msg
                                    val target = Target()
                                    if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
                                        //手机找回，邮箱从data中取
                                        target.poneCountyCode = binding?.countryCode?.tag.toString()
                                        target.phone = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
                                        target.mail = returnData.data
                                    } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
                                        //如果邮箱找回，邮箱从msg中分割取，手机号从data中分割取
                                        val mailArr = prefixAuth?.split("#")?.toTypedArray()
                                                ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                        target.mail = if (mailArr.size >= 2) mailArr[1] else null
                                        if (returnData.data != null) {
                                            val phoneArr = returnData.data?.split("#")?.toTypedArray()
                                                    ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                            if (phoneArr.size >= 2) {
                                                target.poneCountyCode = phoneArr[0]
                                                target.phone = phoneArr[1]
                                            }
                                        }
                                    }
                                    var verifyType = VerifyType.NONE
                                    when (returnData.code) {
                                        ConstData.AUTHENTICATE_CODE_MAIL -> verifyType = verifyType or VerifyType.MAIL
                                        ConstData.AUTHENTICATE_CODE_PHONE -> verifyType = verifyType or VerifyType.PHONE
                                        ConstData.AUTHENTICATE_CODE_GOOGLE -> verifyType = verifyType or VerifyType.GOOGLE
                                        ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE -> verifyType = verifyType or VerifyType.PHONE or VerifyType.GOOGLE
                                    }
                                    return verify(verifyType, target, returnData.code!!, prefixAuth)
                                            ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                }
                                else -> {
                                    FryingUtil.showToast(mContext, returnData.msg)
                                    return Observable.empty()
                                }
                            }
                        } else {
                            FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                            return Observable.empty()
                        }
                    }

                })
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : NormalObserver2<HttpRequestResultString?>(this) {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    override fun callback(result: HttpRequestResultString?) {
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            //密码修改成功，使用新密码登录
                            FryingUtil.showToast(mContext, getString(R.string.change_password_success))
                            BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext) { routeResult, _ ->
                                if (routeResult) {
                                    finish()
                                }
                            }
                        } else {
                            FryingUtil.showToast(mContext, if (result == null) getString(R.string.data_error) else result.msg)
                        }
                    }
                })

    }

    //打开验证框并验证
    private fun verify(type: Int, target: Target, errorCode: Int, prefixAuth: String?): Observable<RequestObserveResult<HttpRequestResultString?>>? {
        hideSoftKeyboard()
        val verifyWindow = VerifyWindowObservable.getVerifyWindowMultiple(this, type, true, target)
        return verifyWindow.show()
                .flatMap(object : Function<Target?, ObservableSource<Target>> {
                    @Throws(Exception::class)
                    override fun apply(target: Target): ObservableSource<Target> {
                        if (target == null) {
                            verifyWindow.dismiss()
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_MAIL == errorCode && TextUtils.isEmpty(target.mailCode)) {
                            FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_PHONE == errorCode) {
                            if (TextUtils.isEmpty(target.phoneCode)) {
                                FryingUtil.showToast(mContext, getString(R.string.alert_input_sms_code))
                                return Observable.empty()
                            }
                        }
                        if (ConstData.AUTHENTICATE_CODE_GOOGLE == errorCode) {
                            if (TextUtils.isEmpty(target.googleCode)) {
                                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                                return Observable.empty()
                            }
                        }
                        if (ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE == errorCode) {
                            if (TextUtils.isEmpty(target.phoneCode) || TextUtils.isEmpty(target.googleCode)) {
                                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_and_phone_code))
                                return Observable.empty()
                            }
                        }
                        target.prefixAuth = prefixAuth
                        target.type = type
                        return Observable.just(target)
                    }

                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap { targetNew -> resetPasswordSuffix(verifyWindow, type, targetNew, prefixAuth) }
    }

    //修改密码验证 第二步
    private fun resetPasswordSuffix(verifyWindow: VerifyWindowObservable, verifyType: Int, target: Target, prefixAuth: String?): Observable<RequestObserveResult<HttpRequestResultString?>>? {
        var newPass: String? = null
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
            val password: String = binding?.phonePassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
                return Observable.empty()
            }
            if (password.length < 8) {
                FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
                return Observable.empty()
            }
//            if (TextUtils.isDigitsOnly(password)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//                return
//            }
            newPass = password
        } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
            //进行邮箱密码找回
            val userName: String = binding?.mailAccount?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
                return Observable.empty()
            }
//            String verifyCode = mailCheckCodeEditText.getText().toString().trim()
//            if (TextUtils.isEmpty(verifyCode)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
//                return
//            }
//            String captcha = mailImageCodeEditText.getText().toString().trim()
//            if (TextUtils.isEmpty(captcha)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_input_captcha))
//                return
//            }
            val password: String = binding?.mailPassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
                return Observable.empty()
            }
            if (password.length < 8) {
                FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
                return Observable.empty()
            }
//            if (TextUtils.isDigitsOnly(password)) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//                return
//            }
            newPass = password
        }
        var phoneCode: String? = null
        var captchaPhone: String? = null
        if (verifyType and VerifyType.PHONE == VerifyType.PHONE) {
            phoneCode = target.phoneCode
            captchaPhone = target.phoneCaptcha
        }
        var emailCode: String? = null
        var captchaEmail: String? = null
        if (verifyType and VerifyType.MAIL == VerifyType.MAIL) {
            emailCode = target.mailCode
            captchaEmail = target.mailCaptcha
        }
        var googleCode: String? = null
        if (verifyType and VerifyType.GOOGLE == VerifyType.GOOGLE) {
            googleCode = target.googleCode
        }
        newPass = RSAUtil.encryptDataByPublicKey(newPass)
        showLoading()
        return ApiManager.build(this, true).getService(UserApiService::class.java)
                ?.resetPasswordSuffix(newPass, prefixAuth, phoneCode, emailCode, googleCode, captchaPhone, captchaEmail)
                ?.materialize()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.flatMap(object : RequestFunction2<HttpRequestResultString?, HttpRequestResultString?>() {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    @Throws(Exception::class)
                    override fun applyResult(returnData: HttpRequestResultString?): HttpRequestResultString? {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            runOnUiThread { verifyWindow.dismiss() }
                        }
                        return returnData
                    }
                })
    }
}