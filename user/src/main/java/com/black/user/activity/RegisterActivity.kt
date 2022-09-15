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
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.UrlConfig
import com.black.base.view.CountryChooseWindow
import com.black.base.view.CountryChooseWindow.OnCountryChooseListener
import com.black.base.widget.SpanTextView
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityRegisterBinding
import com.black.util.RSAUtil
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.REGISTER])
class RegisterActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }

    private var type = ConstData.AUTHENTICATE_TYPE_PHONE
    private var binding: ActivityRegisterBinding? = null

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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding?.countryCode?.tag = "86"
        binding?.countryCode?.setOnClickListener(this)
        binding?.phoneAccount?.addTextChangedListener(watcher)
        binding?.phoneCode?.addTextChangedListener(watcher)
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.password?.addTextChangedListener(watcher)
        binding?.phoneInviteCode?.addTextChangedListener(watcher)

        binding?.phoneBar?.setOnClickListener(this)
        binding?.emailBar?.setOnClickListener(this)
        binding?.imgCountryCode?.setOnClickListener(this)

        binding?.mailAccount?.addTextChangedListener(watcher)
        binding?.root?.findViewById<SpanTextView>(R.id.text_action_bar_right)?.text = getString(R.string.login_title)
        binding?.root?.findViewById<SpanTextView>(R.id.text_action_bar_right)?.setOnClickListener(this)

        binding?.registerAgreementCheck?.setOnCheckedChangeListener { _, _ -> checkClickable() }
        binding?.registerAgreement?.setOnClickListener(this)
        binding?.btnRegister?.setOnClickListener(this)

        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "86"
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object : OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode?.tag = thisCountry?.code
                binding?.countryCode?.setText("+" + thisCountry?.code)
            }
        })

        initChooseWindowData()
        checkClickable()
    }

    override fun getTitleText(): String? {
        return getString(R.string.register_title)
    }


    override fun needGeeTest(): Boolean {
        return true
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        //不需要打开需要登录的目标
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.action_bar_extras) {
            BlackRouter.getInstance().build(RouterConstData.LOGIN)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(this) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }
        } else if (i == R.id.country_code) {
            //切换国家区号
            chooseCountryCode()
        } else if (i == R.id.get_phone_code) {
            //获取验证码
            if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
                getPhoneVerifyCode()
            } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
                getMailVerifyCode()
            }
        } else if (i == R.id.btn_register) {
            //完成注册
            registerAccount()
        } else if (i == R.id.register_agreement) {
            //打开注册协议
            val bundle = Bundle()
            bundle.putString(ConstData.TITLE, getString(R.string.user_protocol))
            bundle.putString(ConstData.URL, UrlConfig.getUrlProcote(this))
            BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(this)
        } else if (i == R.id.text_action_bar_right) {
            BlackRouter.getInstance().build(RouterConstData.LOGIN)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(this) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }
        }else if(i == R.id.email_bar){
            type = ConstData.AUTHENTICATE_TYPE_MAIL
            changeLoinType(type)
        }else if(i == R.id.phone_bar){
            type = ConstData.AUTHENTICATE_TYPE_PHONE
            changeLoinType(type)
        }
    }

    override fun getStatusBarColor(): Int {
        return SkinCompatResources.getColor(this, R.color.black)
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

    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(this, false, object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    chooseWindow!!.setCountryList(returnData.data)
                }
            }
        })
    }

    private fun chooseCountryCode() {
        chooseWindow!!.show(thisCountry)
    }

    private fun checkClickable() {
        when (type) {
            ConstData.AUTHENTICATE_TYPE_PHONE -> binding?.btnRegister?.isEnabled = !(!(binding?.registerAgreementCheck?.isChecked
                    ?: false)
                    || TextUtils.isEmpty(binding?.phoneAccount?.text.toString().trim { it <= ' ' })
                    || TextUtils.isEmpty(binding?.phoneCode?.text.toString().trim { it <= ' ' }))
            ConstData.AUTHENTICATE_TYPE_MAIL -> binding?.btnRegister?.isEnabled = !(!(binding?.registerAgreementCheck?.isChecked
                    ?: false)
                    || TextUtils.isEmpty(binding?.mailAccount?.text.toString().trim { it <= ' ' })
                    || TextUtils.isEmpty(binding?.phoneCode?.text.toString().trim { it <= ' ' }))
            else -> {
            }
        }
    }

    private fun changeLoinType(loginType:Int?){
        when(loginType){
            ConstData.AUTHENTICATE_TYPE_PHONE ->{
                binding?.mailBarB?.visibility = View.GONE
                binding?.phoneBarB?.visibility = View.VISIBLE
                binding?.mailAccount?.visibility = View.GONE
                binding?.relPhone?.visibility = View.VISIBLE
                binding?.loginType?.text = getString(R.string.phone_number)
            }
            ConstData.AUTHENTICATE_TYPE_MAIL ->{
                binding?.mailBarB?.visibility = View.VISIBLE
                binding?.phoneBarB?.visibility = View.GONE
                binding?.mailAccount?.visibility = View.VISIBLE
                binding?.relPhone?.visibility = View.GONE
                binding?.loginType?.text = getString(R.string.email)
            }
        }
    }

    //获取手机验证码
    private fun getPhoneVerifyCode() {
        if (getPhoneCodeLocked) {
            return
        }
        val telCountryCode = if (binding?.countryCode?.tag == null) null else binding?.countryCode?.tag.toString()
        if (TextUtils.isEmpty(telCountryCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_choose_country))
            return
        }
        val userName = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
            return
        }
        UserApiServiceHelper.getVerifyCode(this, userName, telCountryCode, true, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
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
        val userName = binding?.mailAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
            return
        }
        UserApiServiceHelper.getVerifyCode(this, userName, null, true, object : NormalCallback<HttpRequestResultString?>() {
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

    //注册
    private fun registerAccount() {
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
            registerPhoneAccount()
        } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
            registerMailAccount()
        }
    }

    //手机号注册
    private fun registerPhoneAccount() {
        val telCountryCode = if (binding?.countryCode?.tag == null) null else binding?.countryCode?.tag.toString()
        if (TextUtils.isEmpty(telCountryCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_choose_country))
            return
        }
        val userName = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
            return
        }
        val verifyCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(verifyCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_phone_code))
            return
        }
        //        String captcha = phoneImageCodeEditText.getText().toString().trim()
//        if (TextUtils.isEmpty(captcha)) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_input_captcha))
//            return
//        }
        var password = binding?.password?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_password))
            return
        }
        if (password.length < 8) {
            FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
            return
        }
        //        if (TextUtils.isDigitsOnly(password)) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//            return
//        }
//        if (password.contains(" ")) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_password_has_blank))
//            return
//        }
        password = RSAUtil.encryptDataByPublicKey(password)
        val inviteCode = binding?.phoneInviteCode?.text.toString().trim { it <= ' ' }
        UserApiServiceHelper.register(this, userName, password, telCountryCode, verifyCode, null, inviteCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_registrer_success))
                    onRegisterSuccess(userName)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //邮箱注册
    private fun registerMailAccount() {
        val userName = binding?.mailAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(userName)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
            return
        }
        val verifyCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(verifyCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
            return
        }
        //        String captcha = mailImageCodeEditText.getText().toString().trim()
//        if (TextUtils.isEmpty(captcha)) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_input_captcha))
//            return
//        }
        var password = binding?.password?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(password)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_password))
            return
        }
        if (password.length < 8) {
            FryingUtil.showToast(mContext, getString(R.string.alert_password_too_short))
            return
        }
        //        if (TextUtils.isDigitsOnly(password)) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_password_all_number))
//            return
//        }
//        if (password.contains(" ")) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_password_has_blank))
//            return
//        }
        password = RSAUtil.encryptDataByPublicKey(password)
        val inviteCode = binding?.phoneInviteCode?.text.toString().trim { it <= ' ' }
        UserApiServiceHelper.register(this, userName, password, null, verifyCode, null, inviteCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_registrer_success))
                    onRegisterSuccess(userName)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun onRegisterSuccess(userName: String) {
        val intent = Intent()
        intent.putExtra(ConstData.ACCOUNT, userName)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


}
