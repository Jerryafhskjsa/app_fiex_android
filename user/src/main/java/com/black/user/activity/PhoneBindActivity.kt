package com.black.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.black.base.model.NormalCallback
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.CountryChooseWindow
import com.black.base.view.CountryChooseWindow.OnCountryChooseListener
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityPhoneBindBinding
import com.black.util.Callback
import com.black.util.CommonUtil

//手机验证绑定
@Route(value = [RouterConstData.PHONE_BIND], beforePath = RouterConstData.LOGIN)
class PhoneBindActivity : BaseActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityPhoneBindBinding? = null
    private var totalTime : Long = 60*1000
    private var countDownTimer: CountDownTimer? = null
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
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_bind)
        binding?.countryCode?.isFocusable = false
        binding?.countryCode?.isFocusableInTouchMode = false
        binding?.countryCode?.tag = "86"
        binding?.countryCode?.setOnClickListener(this)
        binding?.phoneAccount?.addTextChangedListener(watcher)
        binding?.phoneCode?.addTextChangedListener(watcher)
        binding?.getPhoneCode?.setOnClickListener(this)
        binding?.phoneCodeVieify?.addTextChangedListener(watcher)
        binding?.getPhoneCodeVerify?.setOnClickListener(this)
        binding?.mailCode?.addTextChangedListener(watcher)
        binding?.getMailCode?.setOnClickListener(this)
        binding?.googleCode?.addTextChangedListener(watcher)
        if (userInfo!!.tel == null){
            binding?.phoneLayout?.visibility = View.GONE
            binding?.phoneOld?.visibility = View.GONE
        }
        if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
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
        if (TextUtils.equals("1", userInfo!!.emailSecurityStatus)) {
            binding?.mailCode?.visibility = View.VISIBLE
            binding?.mailLayout?.visibility = View.VISIBLE
            binding?.getMailCode?.visibility = View.VISIBLE
            binding?.mailAccount?.visibility =View.VISIBLE
        } else {
            binding?.mailCode?.visibility = View.GONE
            binding?.mailLayout?.visibility = View.GONE
            binding?.getMailCode?.visibility = View.GONE
            binding?.mailAccount?.visibility =View.GONE
        }
        binding?.btnSubmit?.setOnClickListener(this)
        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "61"
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object : OnCountryChooseListener {
            @SuppressLint("SetTextI18n")
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

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.open_phone_bind)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.country_code -> {
                //切换国家区号
                chooseCountryCode()
            }
            R.id.get_phone_code -> {
                phoneVerifyCode
            }
            R.id.get_phone_code_verify -> {
                newPhoneVerifyCode
            }
            R.id.get_mail_code -> {
                mailVerifyCode
            }
            R.id.btn_copy_google_code -> {
                CommonUtil.pasteText(mContext, object : Callback<String?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: String?) {
                        binding?.googleCode?.setText(returnData ?: "")
                    }
                })
            }
            R.id.btn_submit -> {
                bindPhone()
            }
        }
    }

    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(this, false, object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>(mContext) {
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

    private fun checkClickable() {
        if (TextUtils.isEmpty(binding?.phoneAccount?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.phoneCode?.text.toString().trim { it <= ' ' })
                || TextUtils.isEmpty(binding?.phoneCodeVieify?.text.toString().trim { it <= ' ' })) {
            binding?.btnSubmit?.isEnabled = false
        }

       else {
            if (binding?.googleCode?.visibility == View.VISIBLE
                    && TextUtils.isEmpty(binding?.googleCode?.text.toString().trim { it <= ' ' }))
                {
                    binding?.btnSubmit?.isEnabled = false
            }
            if (binding?.mailCode?.visibility == View.VISIBLE
                && TextUtils.isEmpty(binding?.mailCode?.text.toString().trim { it <= ' ' }))
            {
                binding?.btnSubmit?.isEnabled = false
            }

        binding?.btnSubmit?.isEnabled = true
    }
       }

    //获取新手机验证码
    private val newPhoneVerifyCode: Unit
        get() {
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
            countDownTimer = object : CountDownTimer(totalTime,1000) {
                //1000ms运行一次onTick里面的方法
                override fun onFinish() {
                    binding?.getPhoneCodeVerify?.isEnabled = true
                    binding?.getPhoneCodeVerify?.setText(R.string.sent)
                }

                override fun onTick(millisUntilFinished: Long) {
                    val minute = millisUntilFinished / 1000 / 60 % 60
                    val second = millisUntilFinished / 1000 % 60
                    binding?.getPhoneCodeVerify?.isEnabled = false
                    binding?.getPhoneCodeVerify?.setText("$second")
                }
            }.start()
            UserApiServiceHelper.getVerifyCodeOld(this, userName, telCountryCode,true, object : NormalCallback<HttpRequestResultString?>(mContext) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        //锁定发送按钮
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }

    //获取当前手机验证码
    private val phoneVerifyCode: Unit
        get() {
            countDownTimer = object : CountDownTimer(totalTime,1000) {
                //1000ms运行一次onTick里面的方法
                override fun onFinish() {
                    binding?.getPhoneCode?.isEnabled = true
                    binding?.getPhoneCode?.setText(R.string.sent)
                }

                override fun onTick(millisUntilFinished: Long) {
                    val minute = millisUntilFinished / 1000 / 60 % 60
                    val second = millisUntilFinished / 1000 % 60
                    binding?.getPhoneCode?.setText("$second")
                    binding?.getPhoneCode?.isEnabled = false
                }
            }.start()
            UserApiServiceHelper.getVerifyCodeOld(this, userInfo!!.tel , userInfo!!.telCountryCode ,true, object : NormalCallback<HttpRequestResultString?>(mContext) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_success))
                        //锁定发送按钮
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.alert_verify_code_failed))
                    }
                }
            })
        }
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
            UserApiServiceHelper.getVerifyCodeOld(mContext, userInfo!!.email, null,true, object : NormalCallback<HttpRequestResultString?>(mContext) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(com.black.base.R.string.alert_verify_code_success))
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "" else returnData.msg)
                    }
                }
            })
        }

    //绑定手机验证
    private fun bindPhone() {
        val telCountryCode = if (binding?.countryCode?.tag == null) null else binding?.countryCode?.tag.toString()
        if (TextUtils.isEmpty(telCountryCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_choose_country))
            return
        }
        val newPhone = binding?.phoneAccount?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(newPhone)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
            return
        }
        val phoneCode = binding?.phoneCode?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(phoneCode)) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_phone_code))
            return
        }
        val newPhoneCode = binding?.phoneCodeVieify?.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(newPhoneCode)){
            FryingUtil.showToast(mContext, getString(R.string.input_phone_code_veifily))
            return
        }
        var mailCode: String? = null
        if (TextUtils.equals("1", userInfo!!.emailSecurityStatus)) {
            mailCode = binding?.mailCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(mailCode)) {
                FryingUtil.showToast(mContext, getString(R.string.input_mail_code))
                return
            }
        }
        //        String password = passwordEditText.getText().toString().trim();
//        if (TextUtils.isEmpty(password)) {
//            FryingUtil.showToast(mContext, getString(R.string.alert_input_password));
//            return;
//        }
            var googleCode: String? = null
            if (TextUtils.equals("1", userInfo!!.googleSecurityStatus)) {
               googleCode = binding?.googleCode?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(googleCode)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                return

        }
            }
        UserApiServiceHelper.phoneSecurity(mContext, telCountryCode, newPhone , phoneCode , newPhoneCode,  mailCode, googleCode, object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
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
                    FryingUtil.showToast(mContext, getString(R.string.alert_bind_success))
                    val intent = Intent(this@PhoneBindActivity, SafeCenterActivity::class.java)
                    startActivity(intent)
                    finish()
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