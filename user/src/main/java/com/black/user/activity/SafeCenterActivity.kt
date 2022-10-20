package com.black.user.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindow
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
import com.black.user.databinding.ActivitySafeCenterBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.RSAUtil
import skin.support.content.res.SkinCompatResources

//安全中心
@Route(value = [RouterConstData.SAFE_CENTER], beforePath = RouterConstData.LOGIN)
class SafeCenterActivity : BaseActionBarActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivitySafeCenterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_safe_center)
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            //需要重新登录
        }

        binding?.gesturePassword?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.gesturePassword?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.gesturePassword?.setOnCheckedChangeListener(this)

        binding?.fingerPrintPassword?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.fingerPrintPassword?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.fingerPrintPassword?.setOnCheckedChangeListener(this)

        binding?.moneyPassword?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.moneyPassword?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.moneyPassword?.setOnCheckedChangeListener(this)
        binding?.moneyPasswordLayout?.visibility = View.GONE

        binding?.safePhoneLayout?.setOnClickListener(this)
        binding?.safeMailLayout?.setOnClickListener(this)
        binding?.safeGoogleLayout?.setOnClickListener(this)

        binding?.safeGoogleStatus?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.safeGoogleStatus?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.safeGoogleStatus?.setOnCheckedChangeListener(this)

        binding?.changePassword?.setOnClickListener(this)
        binding?.resetMoneyPasswordLayout?.setOnClickListener(this)
        binding?.resetMoneyPasswordLayout?.visibility = View.GONE
        binding?.paymentMethodLayout?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.safe_center)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.safe_phone_layout) {
            BlackRouter.getInstance().build(RouterConstData.PHONE_BIND).go(mContext)
//            if (!TextUtils.equals("phone", userInfo?.registerFrom)) {
//            }
//            if (TextUtils.isEmpty(userInfo?.tel)) {
//                //未绑定
//                BlackRouter.getInstance().build(RouterConstData.PHONE_BIND).go(mContext)
//            } else {
                //                BlackRouter.getInstance().build(RouterConstData.PHONE_SECURITY_STATUS).go(mContext);
//            }
        } else if (i == R.id.safe_mail_layout) {
            if (!TextUtils.equals("email", userInfo?.registerFrom)) {
            }
            if (TextUtils.isEmpty(userInfo?.email)) {
                //未绑定
                BlackRouter.getInstance().build(RouterConstData.EMAIL_BIND).go(mContext)
            } else {
                //                BlackRouter.getInstance().build(RouterConstData.EMAIL_SECURITY_STATUS).go(mContext);
            }
        } else if (i == R.id.safe_google_layout) {
            //            if (!TextUtils.equals(userInfo.googleSecurityStatus, "1")) {
//                BlackRouter.getInstance().build(RouterConstData.GOOGLE_GET_KEY).go(mContext);
//            } else {
////                BlackRouter.getInstance().build(RouterConstData.GOOGLE_SECURITY_STATUS).go(mContext);
//            }
        } else if (i == R.id.safe_bind_layout) {
            BlackRouter.getInstance().build(RouterConstData.SAFE_BIND).go(this)
        }  else if (i == R.id.change_password) {
            BlackRouter.getInstance().build(RouterConstData.CHANGE_PASSWORD).go(this)
        } else if (i == R.id.payment_method_layout) {
            BlackRouter.getInstance().build(RouterConstData.PAYMENT_METHOD_MANAGER).go(this)
        } else if (i == R.id.reset_money_password_layout) {
            val bundle = Bundle()
            bundle.putInt(ConstData.MONEY_PASSWORD_TYPE, ConstData.MONEY_PASSWORDR_RESET)
            BlackRouter.getInstance().build(RouterConstData.MONEY_PASSWORD).with(bundle).go(mContext)
        }
    }

    override fun onResume() {
        super.onResume()
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            return
        }
        if (TextUtils.equals(userInfo?.phoneSecurityStatus, "1")) {
            binding?.safePhoneStatus?.text = if (userInfo?.tel == null) "" else userInfo?.tel?.replace("(?<=\\d{3})\\d(?=\\d{4})".toRegex(), "*")
//            binding?.safePhoneStatusIcon?.visibility = View.GONE
        } else {
            binding?.safePhoneStatus?.setText(R.string.status_closed)
            binding?.safePhoneStatusIcon?.visibility = View.VISIBLE
        }
        if (TextUtils.equals(userInfo?.emailSecurityStatus, "1")) {
            binding?.safeMailStatus?.text = if (userInfo?.email == null) "" else userInfo?.email
            binding?.safeMailStatusIcon?.visibility = View.GONE
        } else {
            binding?.safeMailStatus?.setText(R.string.status_closed)
            binding?.safeMailStatusIcon?.visibility = View.VISIBLE
        }
        binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
        refreshAccountProtect()
        refreshMoneyPasswordViews()
    }

    private var fingerPrintAction = 0 // 1 关闭  2 开启
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.GESTURE_PASSWORD_CHECK -> if (data?.getBooleanExtra(ConstData.GESTURE_PASSWORD_CHECK_RESULT, false)!!) {
                    CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_NONE)
                }
                ConstData.FINGER_PRINT_SETTING -> if (data?.getBooleanExtra(ConstData.FINGER_PRINT_CHECK_RESULT, false)!!) {
                    CookieUtil.setGesturePassword(mContext, null)
                    CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_FINGER)
                }
                ConstData.FINGER_PRINT_CHECK -> if (data?.getBooleanExtra(ConstData.FINGER_PRINT_CHECK_RESULT, false)!!) {
                    CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_NONE)
                }
            }
        }
    }

    private fun refreshAccountProtect() {
        when (CookieUtil.getAccountProtectType(mContext)) {
            ConstData.ACCOUNT_PROTECT_GESTURE -> {
                binding?.gesturePassword?.isChecked = true
                binding?.fingerPrintPassword?.isChecked = false
                //            setItemChecked(gesturePasswordCheckView, true);
//            setItemChecked(fingerPrintCheckView, false);
//            btnCloseProtect.setVisibility(View.VISIBLE);
            }
            ConstData.ACCOUNT_PROTECT_FINGER -> {
                binding?.fingerPrintPassword?.isChecked = true
                binding?.gesturePassword?.isChecked = false
                //            setItemChecked(gesturePasswordCheckView, false);
//            setItemChecked(fingerPrintCheckView, true);
//            btnCloseProtect.setVisibility(View.VISIBLE);
            }
            else -> {
                binding?.gesturePassword?.isChecked = false
                binding?.fingerPrintPassword?.isChecked = false
                //            setItemChecked(gesturePasswordCheckView, false);
//            setItemChecked(fingerPrintCheckView, false);
//            btnCloseProtect.setVisibility(View.GONE);
            }
        }
    }
    /**
     * 如果是手机用户
     * 手机验证通过+邮箱验证未通过+Google验证未通过=安全等级 低
     * 手机验证通过+邮箱验证未通过+Google验证通过=安全等级 低
     * 手机验证通过+邮箱验证通过+Google验证未通过=安全等级 中
     * 手机验证通过+邮箱验证通过+Google验证通过=安全等级 高
     * 规则就是一定要把手机和邮箱都保证起来（Google验证属于手机范畴）
     * 安全级别为低的提示：
     * 您当前的安全级别为低，面临极大的安全风险，请尽快完成其邮箱验证和Google验证
     * 安全级别为中的提示：
     * 您当前的安全级别为中，面临较大的安全风险，请尽快完成其所有验证
     * 安全级别为高的提示：
     * 您当前的安全级别为高，你已完成所有验证，比较安全
     */
    //        if (1 == userInfo.getSecurityLevel()) {
//            if (TextUtils.equals("email", userInfo.registerFrom)) {
//                notice = "您当前的安全级别为低，面临极大的安全风险，请尽快完成手机验证和Google验证";
//            } else {
//                notice = "您当前的安全级别为低，面临极大的安全风险，请尽快完成邮箱验证和Google验证";
//            }
//        } else if (2 == userInfo.getSecurityLevel()) {
//            notice = "您当前的安全级别为中，面临比较大的安全风险，请尽快完成所有验证";
//        } else if (3 == userInfo.getSecurityLevel()) {
//            notice = "您当前的安全级别为高，你已完成所有验证，比较安全";
//        }

    /**
     * 如果是邮箱用户
     * 邮箱验证通过+手机验证未通过+Google验证未通过=安全等级 低
     * 邮箱验证通过+手机验证未通过+Google验证通过=安全等级 中
     * 邮箱验证通过+手机验证通过+Google验证未通过=安全等级 中
     * 邮箱验证通过+手机验证通过+Google验证通过=安全等级 高
     * 规则就是一定要把手机和邮箱都保证起来（Google验证属于手机范畴）
     * 安全级别为低的提示：
     * 您当前的安全级别为低，面临极大的安全风险，请尽快完成其手机验证和Google验证
     * 安全级别为中的提示：
     * 您当前的安全级别为中，面临比较大的安全风险，请尽快完成所有验证
     * 安全级别为高的提示：
     */
    val safeNotice: String
        get() {
            var notice = ""
            notice = if (TextUtils.equals("email", userInfo?.registerFrom)) {
                /**
                 * 如果是邮箱用户
                 * 邮箱验证通过+手机验证未通过+Google验证未通过=安全等级 低
                 * 邮箱验证通过+手机验证未通过+Google验证通过=安全等级 中
                 * 邮箱验证通过+手机验证通过+Google验证未通过=安全等级 中
                 * 邮箱验证通过+手机验证通过+Google验证通过=安全等级 高
                 * 规则就是一定要把手机和邮箱都保证起来（Google验证属于手机范畴）
                 * 安全级别为低的提示：
                 * 您当前的安全级别为低，面临极大的安全风险，请尽快完成其手机验证和Google验证
                 * 安全级别为中的提示：
                 * 您当前的安全级别为中，面临比较大的安全风险，请尽快完成所有验证
                 * 安全级别为高的提示：
                 */
                if (TextUtils.equals(userInfo?.phoneSecurityStatus, "1") && TextUtils.equals(userInfo?.emailSecurityStatus, "1") && TextUtils.equals(userInfo?.googleSecurityStatus, "1")) {
                    getString(R.string.level_safe)
                } else if (TextUtils.equals(userInfo?.emailSecurityStatus, "1")
                        && (TextUtils.equals(userInfo?.phoneSecurityStatus, "1") || TextUtils.equals(userInfo?.googleSecurityStatus, "1"))) {
                    getString(R.string.level_normal)
                } else {
                    getString(R.string.level_low_phone)
                }
            } else {
                /**
                 * 如果是手机用户
                 * 手机验证通过+邮箱验证未通过+Google验证未通过=安全等级 低
                 * 手机验证通过+邮箱验证未通过+Google验证通过=安全等级 低
                 * 手机验证通过+邮箱验证通过+Google验证未通过=安全等级 中
                 * 手机验证通过+邮箱验证通过+Google验证通过=安全等级 高
                 * 规则就是一定要把手机和邮箱都保证起来（Google验证属于手机范畴）
                 * 安全级别为低的提示：
                 * 您当前的安全级别为低，面临极大的安全风险，请尽快完成其邮箱验证和Google验证
                 * 安全级别为中的提示：
                 * 您当前的安全级别为中，面临较大的安全风险，请尽快完成其所有验证
                 * 安全级别为高的提示：
                 * 您当前的安全级别为高，你已完成所有验证，比较安全
                 */
                if (TextUtils.equals(userInfo?.phoneSecurityStatus, "1") && TextUtils.equals(userInfo?.emailSecurityStatus, "1") && TextUtils.equals(userInfo?.googleSecurityStatus, "1")) {
                    getString(R.string.level_safe)
                } else if (TextUtils.equals(userInfo?.phoneSecurityStatus, "1") && TextUtils.equals(userInfo?.emailSecurityStatus, "1")) {
                    getString(R.string.level_normal)
                } else {
                    getString(R.string.level_low_mail)
                }
            }
            //        if (1 == userInfo.getSecurityLevel()) {
            //            if (TextUtils.equals("email", userInfo.registerFrom)) {
            //                notice = "您当前的安全级别为低，面临极大的安全风险，请尽快完成手机验证和Google验证";
            //            } else {
            //                notice = "您当前的安全级别为低，面临极大的安全风险，请尽快完成邮箱验证和Google验证";
            //            }
            //        } else if (2 == userInfo.getSecurityLevel()) {
            //            notice = "您当前的安全级别为中，面临比较大的安全风险，请尽快完成所有验证";
            //        } else if (3 == userInfo.getSecurityLevel()) {
            //            notice = "您当前的安全级别为高，你已完成所有验证，比较安全";
            //        }
            return notice
        }

    private fun changeSecurityStatus(action: String, target: Target) {
        var password = target.password
        password = RSAUtil.encryptDataByPublicKey(password)
        UserApiServiceHelper.enableSecurity(mContext, target.poneCountyCode, target.phone, target.phoneCode, target.mail, target.mailCode, target.googleCode, password, action, object : NormalCallback<HttpRequestResultString?>() {
            override fun error(type: Int, error: Any?) {
                FryingUtil.showToast(mContext, error.toString())
                binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.alert_close_success))
                    onExecuteSuccess()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
                }
            }
        })
    }

    private fun onExecuteSuccess() {
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                userInfo = result
                binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
            }

            override fun error(type: Int, error: Any) {
                binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
            }
        })
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        val id = compoundButton.id
        if (id == R.id.gesture_password) {
            val protectType = CookieUtil.getAccountProtectType(mContext)
            if (protectType == ConstData.ACCOUNT_PROTECT_GESTURE == isChecked) {
                return
            }
            if (isChecked) {
                //开启手势密码设置
                val bundle = Bundle()
                bundle.putBoolean(ConstData.FOR_RESULT, true)
                BlackRouter.getInstance().build(RouterConstData.GESTURE_PASSWORD_SETTING).with(bundle).go(mContext)
            } else {
                //验证手势密码并关闭手势密码设置
                BlackRouter.getInstance().build(RouterConstData.GESTURE_PASSWORD_CHECK)
                        .withRequestCode(ConstData.GESTURE_PASSWORD_CHECK)
                        .go(this)
            }
        } else if (id == R.id.finger_print_password) {
            val protectType = CookieUtil.getAccountProtectType(mContext)
            if (protectType == ConstData.ACCOUNT_PROTECT_FINGER == isChecked) {
                return
            }
            if (isChecked) {
                //开启指纹解锁
                val fingerPrintStatus = CommonUtil.getFingerPrintStatus(this)
                if (fingerPrintStatus == 1) {
                    fingerPrintAction = 2
                    val bundle = Bundle()
                    bundle.putBoolean("FINGER_PRINT_SETTING", true)
                    BlackRouter.getInstance().build(RouterConstData.FINGER_PRINT_CHECK)
                            .with(bundle)
                            .withRequestCode(ConstData.FINGER_PRINT_SETTING)
                            .go(mContext)
                    //                    CookieUtil.setGesturePassword(mContext, null);
//                    CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_FINGER);
                } else {
                    FryingUtil.showToast(this, FryingUtil.getFingerPrintStatusText(this, fingerPrintStatus), FryingSingleToast.ERROR)
                }
                refreshAccountProtect()
            } else {
                fingerPrintAction = 1
                //验证手势密码并关闭指纹解锁
                BlackRouter.getInstance().build(RouterConstData.FINGER_PRINT_CHECK)
                        .withRequestCode(ConstData.FINGER_PRINT_CHECK)
                        .go(this)
            }
        } else if (id == R.id.safe_google_status) {
            if (TextUtils.equals(userInfo?.googleSecurityStatus, "1") == isChecked) {
                return
            }
            if (!TextUtils.equals(userInfo?.googleSecurityStatus, "1")) {
                BlackRouter.getInstance().build(RouterConstData.GOOGLE_GET_KEY).go(mContext)
            } else {
                //关闭谷歌验证
                var type = VerifyType.NONE
                //                if (TextUtils.equals(userInfo.phoneSecurityStatus, "1")) {
//                    type |= VerifyWindow.PHONE;
//                }
//                if (TextUtils.equals(userInfo.emailSecurityStatus, "1")) {
//                    type |= VerifyWindow.MAIL;
//                }
                if (TextUtils.equals(userInfo?.googleSecurityStatus, "1")) {
                    type = type or VerifyType.GOOGLE
                }
                type = type or VerifyType.PASSWORD
                VerifyWindow.getVerifyWindowMultiple(this, type, Target.buildFromUserInfo(userInfo), object : VerifyWindow.OnReturnListener {
                    override fun onReturn(window: VerifyWindow, target: Target?, type: Int) {
                        //进行验证
                        window.dismiss()
                        if (target == null) {
                            binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
                        } else {
                            changeSecurityStatus("3", target)
                        }
                    }

                    override fun onDismiss(window: VerifyWindow, dismissType: Int) {
                        if (dismissType == 0) {
                            binding?.safeGoogleStatus?.isChecked = TextUtils.equals(userInfo?.googleSecurityStatus, "1")
                        }
                    }
                }).show()
            }
        } else if (id == R.id.money_password) {
            if (TextUtils.equals(userInfo?.moneyPasswordStatus, "1") == isChecked) {
                return
            }
            if (!TextUtils.equals(userInfo?.moneyPasswordStatus, "1")) {
                BlackRouter.getInstance().build(RouterConstData.MONEY_PASSWORD).go(mContext)
            } else {
                //关闭资金密码
                var type = VerifyType.NONE
                if (TextUtils.equals(userInfo?.googleSecurityStatus, "1")) {
                    type = type or VerifyType.GOOGLE
                }
                if (TextUtils.equals(userInfo?.phoneSecurityStatus, "1")) {
                    type = type or VerifyType.PHONE
                } else if (TextUtils.equals(userInfo?.emailSecurityStatus, "1")) {
                    type = type or VerifyType.MAIL
                }
                type = type or VerifyType.MONEY_PASSWORD
                VerifyWindow.getVerifyWindowMultiple(this, type, Target.buildFromUserInfo(userInfo), object : VerifyWindow.OnReturnListener {
                    override fun onReturn(window: VerifyWindow, target: Target?, type: Int) {
                        //进行验证
                        window.dismiss()
                        if (target == null) {
                            refreshMoneyPasswordViews()
                        } else {
                            closeMoneyPassword(target)
                        }
                    }

                    override fun onDismiss(window: VerifyWindow, dismissType: Int) {
                        if (dismissType == 0) {
                            refreshMoneyPasswordViews()
                        }
                    }
                }).show()
            }
        }
    }

    //关闭资金密码
    private fun closeMoneyPassword(target: Target) {
        var moneyPassword = target.moneyPassword
        moneyPassword = RSAUtil.encryptDataByPublicKey(moneyPassword)
        val phoneCode = target.phoneCode
        val mailCode = target.mailCode
        val googleCode = target.googleCode
        UserApiServiceHelper.removeMoneyPassword(mContext, moneyPassword, phoneCode, mailCode, googleCode, object : NormalCallback<HttpRequestResultString?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                refreshMoneyPasswordViews()
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.remove_money_password_success))
                    onCloseMoneyPasswordSuccess()
                    refreshMoneyPasswordViews()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                    refreshMoneyPasswordViews()
                }
            }
        })
    }

    private fun refreshMoneyPasswordViews() {
        if (TextUtils.equals(userInfo?.moneyPasswordStatus, "1")) {
            binding?.moneyPassword?.isChecked = true
            binding?.resetMoneyPasswordLayout?.visibility = View.GONE
        } else {
            binding?.moneyPassword?.isChecked = false
            binding?.resetMoneyPasswordLayout?.visibility = View.GONE
        }
    }

    private fun onCloseMoneyPasswordSuccess() {
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    userInfo = result
                    refreshMoneyPasswordViews()
                }
            }

            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(mContext, error.toString())
            }
        }, true)
    }
}