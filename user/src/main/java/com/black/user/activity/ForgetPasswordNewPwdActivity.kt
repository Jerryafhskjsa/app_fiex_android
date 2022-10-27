package com.black.user.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiService
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultString
import com.black.base.net.NormalObserver2
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.net.RequestFunction
import com.black.net.RequestFunction2
import com.black.net.RequestObserveResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityForgetPasswordNewPwdBinding
import com.black.util.RSAUtil
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

@Route(value = [RouterConstData.FORGET_PASSWORD_NEW_PWD])
class ForgetPasswordNewPwdActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityForgetPasswordNewPwdBinding? = null
    private var type:Int? = ConstData.AUTHENTICATE_TYPE_PHONE
    private var account:String? = null
    private var countryCode:String? = null
    private var verifyCode:String? = null
    private var googlgCode:String? = null
    private var phoneCaptcha:String? = null
    private var mailCaptcha:String? = null
    private var newPsw:String? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(ConstData.TYPE, ConstData.AUTHENTICATE_TYPE_NONE)
        account = intent.getStringExtra(ConstData.ACCOUNT)
        countryCode = intent.getStringExtra(ConstData.COUNTRY_CODE)
        verifyCode = intent.getStringExtra(ConstData.VERIFY_CODE)
        googlgCode = intent.getStringExtra(ConstData.GOOGLE_CODE)
        phoneCaptcha = intent.getStringExtra(ConstData.PHONE_CAPTCHA)
        mailCaptcha = intent.getStringExtra(ConstData.MAIL_CAPTCHA)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password_new_pwd)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.newPsw?.addTextChangedListener(watcher)
        initView()
    }


    private fun initView(){
        when(type){
            ConstData.AUTHENTICATE_TYPE_PHONE ->{
                binding?.loginType?.text = getString(R.string.phone_number)
            }
            ConstData.AUTHENTICATE_TYPE_MAIL ->{
                binding?.loginType?.text = getString(R.string.email)
                binding?.countryCode?.visibility = View.GONE
            }
        }
        binding?.tvAccount?.text = account
    }

    private fun checkClickable() {
        binding!!.btnConfirm.isEnabled = !(TextUtils.isEmpty(binding!!.newPsw.text.toString().trim { it <= ' ' }))
    }

    private fun resetPassword() {
        newPsw = binding?.newPsw?.text.toString().trim { it <= ' ' }
        if(TextUtils.isEmpty(newPsw)){
            FryingUtil.showToast(mContext, getString(R.string.alert_input_new_password))
            return
        }
        if (newPsw?.length!! < ConstData.DEFAULT_PSW_LEN) {
            FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
            return
        }
        var captcha: String? = null
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
            captcha = phoneCaptcha
        } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
            captcha = mailCaptcha
        }
        doResetPassword(account, countryCode, verifyCode, captcha)
    }

    open fun doResetPassword(userName: String?, telCountryCode: String?, verificationCode: String?, captcha: String?) {
        showLoading()
        ApiManager.build(this, true,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
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
                                    target.poneCountyCode = countryCode
                                    target.phone = account
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
        var newPass = newPsw
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
        return ApiManager.build(this, true,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
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





    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.new_psw)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_confirm ->{
                resetPassword()
            }
        }
    }

}