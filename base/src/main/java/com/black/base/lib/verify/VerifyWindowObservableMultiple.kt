package com.black.base.lib.verify

import android.app.Activity
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.black.base.R
import com.black.base.databinding.ViewVerifyMultipleBinding
import com.black.base.lib.verify.VerifyType.Companion.GOOGLE
import com.black.base.lib.verify.VerifyType.Companion.MAIL
import com.black.base.lib.verify.VerifyType.Companion.MONEY_PASSWORD
import com.black.base.lib.verify.VerifyType.Companion.NONE
import com.black.base.lib.verify.VerifyType.Companion.PASSWORD
import com.black.base.lib.verify.VerifyType.Companion.PHONE

class VerifyWindowObservableMultiple(activity: Activity, type: Int, target: Target, alwaysNoToken: Boolean) : VerifyWindowObservable(activity, type, target, alwaysNoToken) {
    override fun placeWorkSpace(workSpaceLayout: LinearLayout?) {
        val multipleBinding: ViewVerifyMultipleBinding = DataBindingUtil.inflate(inflater!!, R.layout.view_verify_multiple, null, false)
        multipleBinding.contentLayout?.removeAllViews()
        if (type and PHONE == PHONE) {
            multipleBinding.contentLayout?.addView(getPhoneLayout()?.root)
        }
        if (type and MAIL == MAIL) {
            multipleBinding.contentLayout?.addView(getMailLayout()?.root)
        }
        if (type and GOOGLE == GOOGLE) {
            multipleBinding.contentLayout?.addView(getGoogleLayout()?.root)
        }
        if (type and MONEY_PASSWORD == MONEY_PASSWORD) {
            multipleBinding.contentLayout?.addView(getMoneyPasswordLayout()?.root)
        }
        if (type and PASSWORD == PASSWORD) {
            multipleBinding.contentLayout?.addView(getPasswordLayout()?.root)
        }
        workSpaceLayout?.removeAllViews()
        workSpaceLayout?.addView(multipleBinding.root)
    }

    override fun getResult(): Target {
        val target = Target()
        target.googleCode = getGoogleCode()
        target.phoneCode = getPhoneCode()
        target.phoneCaptcha = phoneCaptcha
        target.mailCode = getMailCode()
        target.mailCaptcha = mailCaptcha
        target.password = getPassword()
        target.moneyPassword = getMoneyPassword()
        return target
    }

    override fun getReturnType(): Int {
        return NONE
    }

}