package com.black.base.lib.verify

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.black.base.R
import com.black.base.api.UserApiServiceHelper.getVerifyCode
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.showToast
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil

abstract class VerifyWindow protected constructor(protected var activity: Activity, type: Int, checkType: Int, alwaysNoToken: Boolean, target: Target, onReturnListener: OnReturnListener?) : View.OnClickListener, PopupWindow.OnDismissListener {
    protected var inflater: LayoutInflater
    private val popupWindow: PopupWindow?
    private val mHandler = Handler()
    var type = 0
        protected set
    private val target: Target
    private val onReturnListener: OnReturnListener?

    protected var alwaysNoToken: Boolean

    protected var titleView: TextView

    private var phoneLayout: View? = null
    private var phoneCodeEditText: EditText? = null
    private var btnGetPhoneCode: TextView? = null
    protected var phoneCaptcha: String? = null

    private var mailLayout: View? = null
    private var mailCodeEditText: EditText? = null
    private var btnGetMailCode: TextView? = null
    protected var mailCaptcha: String? = null

    private var googleLayout: View? = null
    private var googleCodeEditText: EditText? = null
    private var btnCopyGoogleCode: View? = null

    private var passwordLayout: View? = null
    private var passwordEditText: EditText? = null

    private var moneyPasswordLayout: View? = null
    private var moneyPasswordEditText: EditText? = null

    private var dismissType = 0
    private val btnCommit: View

    private var getPhoneCodeLocked: Boolean = false
    private var getPhoneCodeLockedTime: Int = 0
    private val getPhoneCodeLockTimer = object : Runnable {
        override fun run() {
            getPhoneCodeLockedTime--;
            if (getPhoneCodeLockedTime <= 0) {
                getPhoneCodeLocked = false;
                btnGetPhoneCode?.setText(R.string.get_check_code);
            } else {
                btnGetPhoneCode?.text = activity.getString(R.string.aler_get_code_locked, getPhoneCodeLockedTime.toString());
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong());
            }
        }

    }

    private var getMailCodeLocked: Boolean = false
    private var getMailCodeLockedTime: Int = 0
    private val getMailCodeLockTimer = object : Runnable {
        override fun run() {
            getMailCodeLockedTime--;
            if (getMailCodeLockedTime <= 0) {
                getMailCodeLocked = false;
                btnGetMailCode?.setText(R.string.get_check_code);
            } else {
                btnGetMailCode?.text = activity.getString(R.string.aler_get_code_locked, getMailCodeLockedTime.toString());
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong());
            }
        }

    }

    protected constructor(activity: Activity, type: Int, alwaysNoToken: Boolean, target: Target, onReturnListener: OnReturnListener?) : this(activity, type, VerifyType.PHONE or VerifyType.MAIL or VerifyType.GOOGLE or VerifyType.PASSWORD or VerifyType.MONEY_PASSWORD, alwaysNoToken, target, onReturnListener) {}

    init {
        inflater = LayoutInflater.from(activity)
        this.type = type
        this.target = target
        this.onReturnListener = onReturnListener
        this.alwaysNoToken = alwaysNoToken
        val dm = activity.resources.displayMetrics
        val contentView = inflater.inflate(R.layout.view_verify_window_new, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_bottom_in_out
        popupWindow.setOnDismissListener(this)
        titleView = contentView.findViewById(R.id.title)
        val workSpaceLayout = contentView.findViewById<LinearLayout>(R.id.work_space)
        init(workSpaceLayout)
        btnCommit = contentView.findViewById(R.id.btn_commit)
        btnCommit.setOnClickListener(this)

    }

    private fun init(workSpaceLayout: LinearLayout?) {
        placeWorkSpace(workSpaceLayout)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.get_phone_code -> {
                //发送手机验证码
                phoneVerifyCode
            }
            R.id.get_mail_code -> {
                //发送邮箱验证码
                mailVerifyCode
            }
            R.id.btn_copy_google_code -> {
                CommonUtil.pasteText(activity, object : Callback<String?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: String?) {
                        googleCodeEditText?.setText(returnData ?: "")
                    }
                })
            }
            R.id.btn_commit -> {
                //确认，回调
                commit()
            }
        }
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
        onReturnListener?.onDismiss(this, dismissType)
    }

    protected fun getPhoneLayout(): View? {
        phoneLayout = inflater.inflate(R.layout.view_verify_phone_layout, null)
        phoneCodeEditText = phoneLayout?.findViewById(R.id.phone_code)
        btnGetPhoneCode = phoneLayout?.findViewById(R.id.get_phone_code)
        btnGetPhoneCode?.setOnClickListener(this)
        return phoneLayout
    }

    protected fun getMailLayout(): View? {
        mailLayout = inflater.inflate(R.layout.view_verify_mail_layout, null)
        mailCodeEditText = mailLayout?.findViewById(R.id.mail_code)
        btnGetMailCode = mailLayout?.findViewById(R.id.get_mail_code)
        btnGetMailCode?.setOnClickListener(this)
        return mailLayout
    }

    protected fun getGoogleLayout(): View? {
        googleLayout = inflater.inflate(R.layout.view_verify_google_layout, null)
        googleCodeEditText = googleLayout?.findViewById(R.id.google_code)
        btnCopyGoogleCode = googleLayout?.findViewById(R.id.btn_copy_google_code)
        btnCopyGoogleCode?.setOnClickListener(this)
        return googleLayout
    }

    protected fun getPasswordLayout(): View? {
        passwordLayout = inflater.inflate(R.layout.view_verify_password_layout, null)
        passwordEditText = passwordLayout?.findViewById(R.id.password)
        return passwordLayout
    }

    protected fun getMoneyPasswordLayout(): View? {
        moneyPasswordLayout = inflater.inflate(R.layout.view_verify_money_password_layout, null)
        moneyPasswordEditText = moneyPasswordLayout?.findViewById(R.id.money_password)
        return moneyPasswordLayout
    }

    private fun setBtnGetPhoneCode() {
        if (getPhoneCodeLockedTime <= 0) {
            btnGetPhoneCode?.setText(R.string.get_check_code)
        } else {
            btnGetPhoneCode?.text = activity.getString(R.string.aler_get_code_locked, java.lang.String.valueOf(getPhoneCodeLockedTime))
        }
    }

    private fun setBtnGetMailCode() {
        if (getMailCodeLockedTime <= 0) {
            btnGetMailCode?.setText(R.string.get_check_code)
        } else {
            btnGetMailCode?.text = activity.getString(R.string.aler_get_code_locked, java.lang.String.valueOf(getMailCodeLockedTime))
        }
    }

    val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    fun show() {
        popupWindow?.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
    }

    fun dismiss() {
        dismissType = 1
        if (isShowing) {
            popupWindow?.dismiss()
        }
        mHandler.removeCallbacks(getPhoneCodeLockTimer)
        mHandler.removeCallbacks(getMailCodeLockTimer)
    }

    val googleCode: String?
        get() = if (googleCodeEditText == null) null else googleCodeEditText?.text.toString().trim { it <= ' ' }

    val phoneCode: String?
        get() = if (phoneCodeEditText == null) null else phoneCodeEditText?.text.toString().trim { it <= ' ' }

    val mailCode: String?
        get() = if (mailCodeEditText == null) null else mailCodeEditText?.text.toString().trim { it <= ' ' }

    val password: String?
        get() = if (passwordEditText == null) null else passwordEditText?.text.toString().trim { it <= ' ' }

    val moneyPassword: String?
        get() = if (moneyPasswordEditText == null) null else moneyPasswordEditText?.text.toString().trim { it <= ' ' }

    private fun commit() { //        Target target = this.target.clone();
//        if ((type & PHONE) == PHONE) {
//            String phoneCode = phoneCodeEditText.getText().toString().trim();
//            target.phoneCode = phoneCode;
//        }
//        if ((type & MAIL) == MAIL) {
//            String mailCode = mailCodeEditText.getText().toString().trim();
//            target.mailCode = mailCode;
//        }
//        if ((type & GOOGLE) == GOOGLE) {
//            String googleCode = googleCodeEditText.getText().toString().trim();
//            target.googleCode = googleCode;
//        }
//        if ((type & PASSWORD) == PASSWORD) {
//            String password = passwordEditText.getText().toString().trim();
//            target.password = password;
//        }
        onReturnListener?.onReturn(this, result, returnType)
    }//发送成功后，锁定按钮

    //获取手机验证码
    val phoneVerifyCode: Unit
        get() {
            if (getPhoneCodeLocked) {
                return
            }
            phoneCaptcha = null
            getVerifyCode(activity, target.phone, target.poneCountyCode, alwaysNoToken, object : NormalCallback<HttpRequestResultString?>(activity) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showToast(activity, activity.getString(R.string.alert_verify_code_success))
                        phoneCaptcha = returnData.data
                        //发送成功后，锁定按钮
                        if (!getPhoneCodeLocked) {
                            getPhoneCodeLocked = true
                            getPhoneCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getPhoneCodeLockTimer)
                        }
                    } else {
                        showToast(activity, returnData?.msg)
                    }
                }
            })
        }//锁定发送按钮

    //获取邮箱验证码
    val mailVerifyCode: Unit
        get() {
            if (getMailCodeLocked) {
                return
            }
            mailCaptcha = null
            getVerifyCode(activity, target.mail, null, alwaysNoToken, object : NormalCallback<HttpRequestResultString?>(activity) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showToast(activity, activity.getString(R.string.alert_verify_code_success))
                        mailCaptcha = returnData.data
                        //锁定发送按钮
                        if (!getMailCodeLocked) {
                            getMailCodeLocked = true
                            getMailCodeLockedTime = ConstData.GET_CODE_LOCK_TIME
                            mHandler.post(getMailCodeLockTimer)
                        }
                    } else {
                        showToast(activity, if (returnData == null) "" else returnData.msg)
                    }
                }
            })
        }

    //排列工作空间
    protected abstract fun placeWorkSpace(workSpaceLayout: LinearLayout?)

    protected abstract val result: Target?
    protected abstract val returnType: Int

    interface OnReturnListener {
        fun onReturn(window: VerifyWindow, target: Target?, type: Int)
        fun onDismiss(window: VerifyWindow, dismissType: Int)
    }

    companion object {
        private const val TAG = "VerifyWindow"
        fun getVerifyWindowSingle(activity: Activity?, type: Int, target: Target?, onReturnListener: OnReturnListener?): VerifyWindow {
            return getVerifyWindowSingle(activity, type, false, target, onReturnListener)
        }

        fun getVerifyWindowSingle(activity: Activity?, type: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?): VerifyWindow {
            return VerifyWindowSingle(activity, type, alwaysNoToken, target, onReturnListener)
        }

        fun getVerifyWindowMultiple(activity: Activity?, type: Int, target: Target?, onReturnListener: OnReturnListener?): VerifyWindow {
            return getVerifyWindowMultiple(activity, type, false, target, onReturnListener)
        }

        fun getVerifyWindowMultiple(activity: Activity?, type: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?): VerifyWindow {
            return VerifyWindowMultiple(activity, type, alwaysNoToken, target, onReturnListener)
        }
    }
}