package com.black.user.activity

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.CountryChooseWindow
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityForgetPasswordFiexBinding

@Route(value = [RouterConstData.FORGET_PASSWORD_FIEX])
class ForgetPasswordFiexActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    private var binding: ActivityForgetPasswordFiexBinding? = null
    private var type = ConstData.AUTHENTICATE_TYPE_PHONE
    private var account:String? = null

    private var thisCountry: CountryCode? = null
    private var chooseWindow: CountryChooseWindow? = null
    private var mailCaptcha: String? = null
    private var phoneCaptcha: String? = null

    private val mHandler = Handler()

    private var getPhoneCodeLocked = false
    private var getPhoneCodeLockedTime = 0
    private val getPhoneCodeLockTimer = object : Runnable {
        override fun run() {
            getPhoneCodeLockedTime--
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false
                binding?.getVerifyCodePhone?.setText(R.string.get_check_code)
            } else {
                binding?.getVerifyCodePhone?.setText(getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString()))
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
                binding?.getVerifyCodeMail?.setText(R.string.get_check_code)
            } else {
                binding?.getVerifyCodeMail?.setText(getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString()))
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password_fiex)
        application = getApplication() as BaseApplication
        type = intent.getIntExtra(ConstData.TYPE, ConstData.AUTHENTICATE_TYPE_NONE)
        account = intent.getStringExtra(ConstData.ACCOUNT)


        binding?.phoneAccount?.addTextChangedListener(watcher)
        binding?.mailAccount?.addTextChangedListener(watcher)
        binding?.verifyCodePhone?.addTextChangedListener(watcher)
        binding?.verifyCodeMail?.addTextChangedListener(watcher)
        binding?.editGoogleCode?.addTextChangedListener(watcher)
        binding?.phoneBar?.setOnClickListener(this)
        binding?.emailBar?.setOnClickListener(this)
        binding?.imgCountryCode?.setOnClickListener(this)
        binding?.btnNext?.setOnClickListener(this)
        binding?.googleCodePaste?.setOnClickListener(this)
        binding?.getVerifyCodePhone?.setOnClickListener(this)
        binding?.getVerifyCodeMail?.setOnClickListener(this)
        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "86"
            binding?.countryCode?.tag = thisCountry?.code
            binding?.countryCode?.setText("+" + thisCountry?.code)
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object :
            CountryChooseWindow.OnCountryChooseListener {
            override fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode?.tag = thisCountry?.code
                binding?.countryCode?.setText("+" + thisCountry?.code)
            }
        })
        changeLoinType(type)
        initChooseWindowData()
        checkClickable()
        initEditAccount()
    }

    private fun initEditAccount(){
        if(account != null){
            when(type){
                ConstData.AUTHENTICATE_TYPE_PHONE ->{
                    binding?.phoneAccount?.text = Editable.Factory.getInstance().newEditable(account)
                }
                ConstData.AUTHENTICATE_TYPE_MAIL ->{
                    binding?.mailAccount?.text = Editable.Factory.getInstance().newEditable(account)
                }
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

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.btn_reset_password)
    }

    override fun needGeeTest(): Boolean {
        return true
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.email_bar ->{
                type = ConstData.AUTHENTICATE_TYPE_MAIL
                changeLoinType(type)
            }
            R.id.phone_bar ->{
                type = ConstData.AUTHENTICATE_TYPE_PHONE
                changeLoinType(type)
            }
            R.id.img_country_code -> chooseCountryCode()
            R.id.btn_next ->{
                var bundle = Bundle()
                bundle.putInt(ConstData.TYPE, type)

                when(type){
                    ConstData.AUTHENTICATE_TYPE_PHONE ->{
                        bundle.putString(ConstData.COUNTRY_CODE,binding?.countryCode?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.ACCOUNT,binding?.phoneAccount?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.VERIFY_CODE,binding?.verifyCodePhone?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.PHONE_CAPTCHA,phoneCaptcha)
                    }
                    ConstData.AUTHENTICATE_TYPE_MAIL ->{
                        bundle.putString(ConstData.ACCOUNT,binding?.mailAccount?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.GOOGLE_CODE,binding?.editGoogleCode?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.VERIFY_CODE,binding?.verifyCodeMail?.text.toString().trim { it <= ' ' })
                        bundle.putString(ConstData.MAIL_CAPTCHA,mailCaptcha)
                    }
                }
                BlackRouter.getInstance().build(RouterConstData.FORGET_PASSWORD_NEW_PWD).with(bundle).go(mContext)
            }
            R.id.google_code_paste ->{

            }
            R.id.get_verify_code_mail ->{
                getMailVerifyCode()
            }
            R.id.get_verify_code_phone ->{
                getPhoneVerifyCode()
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
        val userName = account
        UserApiServiceHelper.getVerifyCode(this, userName, null, true, object : NormalCallback<HttpRequestResultString?>() {
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

    private fun chooseCountryCode() {
        chooseWindow!!.show(thisCountry)
    }

    private fun checkClickable() {
        when (type) {
            ConstData.AUTHENTICATE_TYPE_PHONE -> binding!!.btnNext.isEnabled = !(TextUtils.isEmpty(binding!!.phoneAccount.text.toString().trim { it <= ' ' })
                    || (TextUtils.isEmpty(binding!!.verifyCodePhone.text.toString().trim { it <= ' ' })))
            ConstData.AUTHENTICATE_TYPE_MAIL -> binding!!.btnNext.isEnabled = !(TextUtils.isEmpty(binding!!.mailAccount.text.toString().trim { it <= ' ' })
                    || (TextUtils.isEmpty(binding!!.verifyCodeMail.text.toString().trim { it <= ' ' }))
                    || (TextUtils.isEmpty(binding!!.editGoogleCode.text.toString().trim { it <= ' ' })))
            else -> binding!!.btnNext.isEnabled = false
        }
    }

    private fun changeLoinType(loginType:Int?){
        when(loginType){
            ConstData.AUTHENTICATE_TYPE_PHONE ->{
                binding?.mailBarB?.visibility = View.GONE
                binding?.phoneBarB?.visibility = View.VISIBLE
                binding?.mailAccount?.visibility = View.GONE
                binding?.loginType?.text = getString(R.string.phone_number)
                binding?.relPhone?.visibility = View.VISIBLE
                binding?.googleCode?.visibility = View.GONE
                binding?.googleVerifyCodeLayout?.visibility = View.GONE
                binding?.verifyCodeMailLayout?.visibility = View.GONE
                binding?.verifyCodePhoneLayout?.visibility = View.VISIBLE
            }
            ConstData.AUTHENTICATE_TYPE_MAIL ->{
                binding?.mailBarB?.visibility = View.VISIBLE
                binding?.phoneBarB?.visibility = View.GONE
                binding?.mailAccount?.visibility = View.VISIBLE
                binding?.loginType?.text = getString(R.string.email)
                binding?.relPhone?.visibility = View.GONE
                binding?.googleCode?.visibility = View.VISIBLE
                binding?.googleVerifyCodeLayout?.visibility = View.VISIBLE
                binding?.verifyCodeMailLayout?.visibility = View.VISIBLE
                binding?.verifyCodePhoneLayout?.visibility = View.GONE
            }
        }
    }
}