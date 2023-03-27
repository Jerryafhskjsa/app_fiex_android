package com.black.base.lib.verify

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.os.CountDownTimer
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.base.R
import com.black.base.api.UserApiServiceHelper
import com.black.base.databinding.*
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.observe.ObservablePopupWindow
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil

abstract class VerifyWindowObservable(protected var activity: Activity, protected var type: Int, private var target: Target, protected var alwaysNoToken: Boolean) : ObservablePopupWindow<Target>(), View.OnClickListener {
    companion object {
        fun getVerifyWindowSingle(activity: Activity, type: Int, target: Target): VerifyWindowObservable {
            return getVerifyWindowSingle(activity, type, false, target)
        }

        fun getVerifyWindowSingle(activity: Activity, type: Int, alwaysNoToken: Boolean, target: Target): VerifyWindowObservable {
            return VerifyWindowObservableSingle(activity, type, target, alwaysNoToken)
        }

        fun getVerifyWindowMultiple(activity: Activity, type: Int, target: Target): VerifyWindowObservable {
            return getVerifyWindowMultiple(activity, type, false, target)
        }

        fun getVerifyWindowMultiple(activity: Activity, type: Int, alwaysNoToken: Boolean, target: Target): VerifyWindowObservable {
            return VerifyWindowObservableMultiple(activity, type, target, alwaysNoToken)
        }
    }

    protected var inflater: LayoutInflater? = null
    private var popupWindow: PopupWindow? = null

    private val mHandler = Handler()
    protected var binding: ViewVerifyWindowNewBinding? = null

    private var dismissType = 0

    private var phoneBinding: ViewVerifyPhoneLayoutBinding? = null
    private var mailBinding: ViewVerifyMailLayoutBinding? = null
    private var googleBinding: ViewVerifyGoogleLayoutBinding? = null
    private var passwordBinding: ViewVerifyPasswordLayoutBinding? = null
    private var moneyPasswordBinding: ViewVerifyMoneyPasswordLayoutBinding? = null

    protected var phoneCaptcha: String? = null
    private var getPhoneCodeLocked = false
    private var countDownTimer: CountDownTimer? = null
    private var getPhoneCodeLockedTime = 60
    private val getPhoneCodeLockTimer: Runnable = object : Runnable {

        override fun run() {
            getPhoneCodeLockedTime--
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false
                phoneBinding?.getPhoneCode?.setText(R.string.get_check_code)
            } else {
                phoneBinding?.getPhoneCode?.setText(activity.getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong());
            }
        }
    }

    protected var mailCaptcha: String? = null
    private var getMailCodeLocked = false
    private var getMailCodeLockedTime = 60
    private val getMailCodeLockTimer: Runnable = object : Runnable {

        override fun run() {
            getMailCodeLockedTime--;
            if (getMailCodeLockedTime <= 0) {
                getMailCodeLocked = false
                mailBinding?.getMailCode?.setText(R.string.get_check_code)
            } else {
                mailBinding?.getMailCode?.setText(activity.getString(R.string.aler_get_code_locked, getMailCodeLockedTime.toString()))
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong());
            }
        }
    }

    init {
        inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_window_new, null, false)
        popupWindow = PopupWindow(binding?.root, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow?.isFocusable = true
        popupWindow?.setBackgroundDrawable(PaintDrawable())
        popupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow?.animationStyle = R.style.anim_bottom_in_out
        popupWindow?.setOnDismissListener(this)
        this.placeWorkSpace(binding?.workSpace)
        binding?.btnCommit?.setOnClickListener(this)
    }

    //获取手机验证码
    open fun getPhoneVerifyCode() {
        phoneCaptcha = null
        val TotalTime:Long = 60*1000
        countDownTimer = object : CountDownTimer(TotalTime,1000){//1000ms运行一次onTick里面的方法
        @SuppressLint("ResourceAsColor")
        override fun onFinish(){
            phoneBinding?.getPhoneCode?.isEnabled = true
            phoneBinding?.getPhoneCode?.setTextColor(R.color.T9)
            phoneBinding?.getPhoneCode?.setText(R.string.get_check_code)
        }

            @SuppressLint("ResourceAsColor")
            override fun onTick(millisUntilFinished: Long) {
                if (TotalTime > 0){
                    val second=millisUntilFinished/1000%60
                    phoneBinding?.getPhoneCode?.isEnabled = false
                    phoneBinding?.getPhoneCode?.setTextColor(R.color.C2)
                    phoneBinding?.getPhoneCode?.setText("$second" + "秒后可重发")}
                if (TotalTime <= 0){
                    phoneBinding?.getPhoneCode?.setTextColor(R.color.T9)
                }
            }
        }.start()
        UserApiServiceHelper.getVerifyCodeOld(activity, target.phone, target.poneCountyCode, true, object : NormalCallback<HttpRequestResultString?>(activity) {
            override fun callback(returnData: HttpRequestResultString?) {

                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    phoneCaptcha = returnData.data
                } else {
                    FryingUtil.showToast(activity, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    //获取邮箱验证码
    open fun getMailVerifyCode() {
        val TotalTime:Long = 60*1000
        countDownTimer = object : CountDownTimer(TotalTime,1000){//1000ms运行一次onTick里面的方法
        @SuppressLint("ResourceAsColor")
        override fun onFinish(){
            mailBinding?.getMailCode?.isEnabled = true
            mailBinding?.getMailCode?.setTextColor(R.color.T13)
            mailBinding?.getMailCode?.setText(R.string.get_check_code)
        }

            @SuppressLint("ResourceAsColor")
            override fun onTick(millisUntilFinished: Long) {
                if (TotalTime > 0){
                    val second=millisUntilFinished/1000%60
                    mailBinding?.getMailCode?.isEnabled = false
                    mailBinding?.getMailCode?.setTextColor(R.color.C2)
                    mailBinding?.getMailCode?.setText("$second" + "秒后可重发")}

                if (TotalTime <= 0)
                {
                    mailBinding?.getMailCode?.setTextColor(R.color.T13)
                }
            }
        }.start()
        mailCaptcha = null
        UserApiServiceHelper.getVerifyCodeOld(activity, target.mail, null, true, object : NormalCallback<HttpRequestResultString?>(activity) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(activity, activity.getString(R.string.alert_verify_code_success))
                    mailCaptcha = returnData.data
                    FryingUtil.showToast(activity, activity.getString(R.string.alert_verify_code_success))
                    //锁定发送按钮
                } else {
                    FryingUtil.showToast(activity, if (returnData == null) "" else returnData.msg)
                }
            }
        })
    }

    protected open fun getPhoneLayout(): ViewVerifyPhoneLayoutBinding? {
        phoneBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_phone_layout, null, false)
        phoneBinding?.getPhoneCode?.setOnClickListener(this)
        return phoneBinding
    }

    protected open fun getMailLayout(): ViewVerifyMailLayoutBinding? {
        mailBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_mail_layout, null, false)
        mailBinding?.getMailCode?.setOnClickListener(this)
        return mailBinding
    }

    protected open fun getGoogleLayout(): ViewVerifyGoogleLayoutBinding? {
        googleBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_google_layout, null, false)
        googleBinding?.btnCopyGoogleCode?.setOnClickListener(this)
        return googleBinding
    }

    protected open fun getPasswordLayout(): ViewVerifyPasswordLayoutBinding? {
        passwordBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_password_layout, null, false)
        return passwordBinding
    }

    protected open fun getMoneyPasswordLayout(): ViewVerifyMoneyPasswordLayoutBinding? {
        moneyPasswordBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_money_password_layout, null, false)
        return moneyPasswordBinding
    }

    override fun onDismiss() {
        super.onDismiss()
        mHandler.removeCallbacks(getPhoneCodeLockTimer)
        mHandler.removeCallbacks(getMailCodeLockTimer)
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    open fun isShowing(): Boolean {
        return popupWindow != null && popupWindow!!.isShowing
    }

    open fun show(): VerifyWindowObservable {
        popupWindow!!.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        return this
    }

    open fun dismiss() {
        dismissType = 1
        if (isShowing()) {
            popupWindow!!.dismiss()
        }
    }

    override fun onClick(v: View?) {
        val i = v!!.id
        when (i) {
            R.id.get_phone_code -> {
                //发送手机验证码
                getPhoneVerifyCode()

            }
            R.id.get_mail_code -> {
                //发送邮箱验证码
                getMailVerifyCode()
            }
            R.id.btn_copy_google_code -> {
                CommonUtil.pasteText(activity, object : Callback<String?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: String?) {
                        googleBinding?.googleCode?.setText(returnData ?: "")
                    }
                })
            }
            R.id.btn_commit -> {
                //确认，回调
                commit()
            }
        }
    }

    private fun commit() {
        if (emitter != null) {
            emitter?.onNext(getResult())
        }
    }

    open fun getGoogleCode(): String? {
        return if (googleBinding == null) null else googleBinding?.googleCode?.text.toString().trim { it <= ' ' }
    }

    open fun getPhoneCode(): String? {
        return if (phoneBinding == null) null else phoneBinding?.phoneCode?.text.toString().trim { it <= ' ' }
    }

    open fun getMailCode(): String? {
        return if (mailBinding == null) null else mailBinding?.mailCode?.text.toString().trim { it <= ' ' }
    }

    open fun getPassword(): String? {
        return if (passwordBinding == null) null else passwordBinding?.password?.getText().toString().trim { it <= ' ' }
    }

    open fun getMoneyPassword(): String? {
        return if (moneyPasswordBinding == null) null else moneyPasswordBinding?.moneyPassword?.getText().toString().trim { it <= ' ' }
    }

    //排列工作空间
    protected abstract fun placeWorkSpace(workSpaceLayout: LinearLayout?)

    protected abstract fun getResult(): Target

    protected abstract fun getReturnType(): Int
}