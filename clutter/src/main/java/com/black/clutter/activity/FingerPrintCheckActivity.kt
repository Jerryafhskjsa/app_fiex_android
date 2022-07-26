package com.black.clutter.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.FryingSingleToast
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityFingerPrintCheckBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.FingerPrintCallBack

@Route(value = [RouterConstData.FINGER_PRINT_CHECK])
class FingerPrintCheckActivity : BaseActionBarActivity(), View.OnClickListener {
    private var unBack = false
    private var isSetting = false
    private var nextAction: String? = null
    private var manager: FingerprintManagerCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextAction = intent.getStringExtra(ConstData.NEXT_ACTION)
        val binding: ActivityFingerPrintCheckBinding = DataBindingUtil.setContentView(this, R.layout.activity_finger_print_check)
        binding.userName.setText(CookieUtil.getUserName(mContext))
        binding.forgetFingerPrint.setOnClickListener(this)
        if (isSetting) {
            binding.forgetFingerPrint.visibility = View.GONE
        } else {
            binding.forgetFingerPrint.visibility = View.VISIBLE
        }
        manager = FingerprintManagerCompat.from(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        unBack = intent.getBooleanExtra(ConstData.CHECK_UN_BACK, false)
        isSetting = intent.getBooleanExtra("FINGER_PRINT_SETTING", false)
        return if (unBack && !isSetting) {
            0
        } else {
            R.layout.action_bar_left_back
        }
    }

    override fun initActionBarView(view: View) {}
    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        //不需要打开需要登录的目标
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        //验证token和指纹
        if (isSetting || (!TextUtils.isEmpty(CookieUtil.getToken(mContext))
                        && ConstData.ACCOUNT_PROTECT_FINGER == CookieUtil.getAccountProtectType(mContext) && ConstData.ACCOUNT_PROTECT_GESTURE == CommonUtil.getFingerPrintStatus(mContext))) {
            manager!!.authenticate(null, 0, null, FingerPrintCallBack(object : Callback<Int?>() {
                override fun error(type: Int, error: Any?) {
                    when (type) {
                        FingerPrintCallBack.CHECK_FAILED -> FryingUtil.showToast(mContext, getString(R.string.alert_finger_password_check_failed), FryingSingleToast.ERROR)
                        FingerPrintCallBack.ERROR_OVER_MAX -> if (isSetting) {
                            cancelCheck()
                        } else {
                            clearAndLogin()
                        }
                        FingerPrintCallBack.ERROR_OTHER -> FryingUtil.showToast(mContext, getString(R.string.alert_finger_password_check_failed), FryingSingleToast.ERROR)
                    }
                }

                override fun callback(returnData: Int?) {
                    if (returnData != null && returnData == FingerPrintCallBack.SUCCESS) {
                        onFingerPrintChecked()
                    }
                }
            }), null)
        } else {
            //            FryingUtil.showToast(mContext, getString(R.string.alert_finger_password_invalid));
            finish()
        }
    }

    //指纹验证成功
    private fun onFingerPrintChecked() {
        //指纹验证成功
        if (!TextUtils.isEmpty(nextAction)) {
            BlackRouter.getInstance().build(nextAction)
                    .go(this) { routeResult, _ ->
                        if (routeResult) {
                            finish()
                        }
                    }
        } else {
            val intent = Intent()
            intent.putExtra(ConstData.FINGER_PRINT_CHECK_RESULT, true)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun clearAndLogin() {
        if (unBack) {
            FryingUtil.clearAllUserInfo(this)
            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .go(this) { routeResult, error ->
                        if (routeResult) {
                            finish()
                        }
                    }
        } else {
            CookieUtil.setAccountProtectType(this, ConstData.ACCOUNT_PROTECT_NONE)
            val intent = Intent()
            intent.putExtra(ConstData.FINGER_PRINT_CHECK_RESULT, false)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.forget_gesture_password) {
            clearAndLogin()
        }
    }

    override fun onBackClick(view: View?) {
        cancelCheck()
    }

    override fun onBackPressed() {
        if (unBack) {
        } else {
            cancelCheck()
        }
    }

    private fun cancelCheck() {
        CookieUtil.setAccountProtectType(this, ConstData.ACCOUNT_PROTECT_NONE)
        val intent = Intent()
        intent.putExtra(ConstData.FINGER_PRINT_CHECK_RESULT, false)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}