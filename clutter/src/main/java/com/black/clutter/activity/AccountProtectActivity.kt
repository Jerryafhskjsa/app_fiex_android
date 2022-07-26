package com.black.clutter.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.lib.FryingSingleToast
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityAccountProtectBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil

@Route(value = [RouterConstData.ACCOUNT_PROTECT])
class AccountProtectActivity : BaseActivity(), View.OnClickListener {
    private var clickSource: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickSource = intent.extras

        val binding: ActivityAccountProtectBinding = DataBindingUtil.setContentView(this, R.layout.activity_account_protect)
        binding.btnJump.setOnClickListener(this)
        binding.fingerPrint.setOnClickListener(this)
        val fingerPrintStatus = CommonUtil.getFingerPrintStatus(this)
        if (fingerPrintStatus == 1 || fingerPrintStatus == 16 || fingerPrintStatus == 32) {
            binding.fingerPrint.visibility = View.VISIBLE
        } else {
            binding.fingerPrint.visibility = View.GONE
        }
        binding.gesturePassword.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.btn_jump) {
            if (clickSource != null && clickSource!!.getInt(ConstData.OPEN_TYPE) == 1) {
                CookieUtil.setAccountProtectJump(this, true)
            }
            gotoNext(Activity.RESULT_CANCELED)
        } else if (i == R.id.finger_print) { //开启指纹解锁
            val fingerPrintStatus = CommonUtil.getFingerPrintStatus(this)
            if (fingerPrintStatus == 1) {
                var forResult = false
                val bundle = Bundle()
                if (clickSource != null) {
                    forResult = clickSource!!.getBoolean(ConstData.FOR_RESULT)
                    bundle.putAll(clickSource)
                }
                bundle.putBoolean("FINGER_PRINT_SETTING", true)
                BlackRouter.getInstance().build(RouterConstData.FINGER_PRINT_CHECK)
                        .with(bundle)
                        .withRequestCode(ConstData.FINGER_PRINT_SETTING)
                        .go(mContext)
                //                CookieUtil.setGesturePassword(mContext, null);
//                CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_FINGER);
//                gotoNext(RESULT_OK);
            } else {
                FryingUtil.showToast(this, FryingUtil.getFingerPrintStatusText(this, fingerPrintStatus), FryingSingleToast.ERROR)
            }
        } else if (i == R.id.gesture_password) {
            var forResult = false
            val bundle = Bundle()
            if (clickSource != null) {
                forResult = clickSource!!.getBoolean(ConstData.FOR_RESULT)
                bundle.putAll(clickSource)
            }
            BlackRouter.getInstance().build(RouterConstData.GESTURE_PASSWORD_SETTING)
                    .with(bundle)
                    .withRequestCode(ConstData.GESTURE_PASSWORD_SETTING)
                    .go(mContext)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ConstData.GESTURE_PASSWORD_SETTING -> gotoNext(resultCode)
            ConstData.FINGER_PRINT_SETTING -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (true == data?.getBooleanExtra(ConstData.FINGER_PRINT_CHECK_RESULT, false)) {
                        CookieUtil.setGesturePassword(mContext, null)
                        CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_FINGER)
                    }
                }
                gotoNext(resultCode)
            }
        }
    }

    private fun gotoNext(result: Int) {
        var forResult = false
        val bundle = Bundle()
        if (clickSource != null) {
            forResult = clickSource!!.getBoolean(ConstData.FOR_RESULT)
            bundle.putAll(clickSource)
        }
        if (forResult) {
            setResult(result)
        } else {
            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .with(bundle).go(this)
        }
        finish()
    }
}