package com.black.base.lib.verify

import android.app.Activity
import android.widget.LinearLayout
import com.black.base.R

internal open class VerifyWindowMultiple : VerifyWindow {
    private var contentLayout: LinearLayout? = null

    constructor(activity: Activity?, type: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?) : super(activity!!, type, alwaysNoToken, target!!, onReturnListener) {}
    constructor(activity: Activity?, type: Int, checkType: Int, alwaysNoToken: Boolean, target: Target?, onReturnListener: OnReturnListener?) : super(activity!!, type, checkType, alwaysNoToken, target!!, onReturnListener) {}

    override fun placeWorkSpace(workSpaceLayout: LinearLayout?) {
        val innerView = inflater.inflate(R.layout.view_verify_multiple, null)
        contentLayout = innerView.findViewById(R.id.content_layout)
        contentLayout?.removeAllViews()
        if (type and VerifyType.PHONE == VerifyType.PHONE) {
            contentLayout?.addView(getPhoneLayout())
        }
        if (type and VerifyType.MAIL == VerifyType.MAIL) {
            contentLayout?.addView(getMailLayout())
        }
        if (type and VerifyType.GOOGLE == VerifyType.GOOGLE) {
            contentLayout?.addView(getGoogleLayout())
        }
        if (type and VerifyType.MONEY_PASSWORD == VerifyType.MONEY_PASSWORD) {
            contentLayout?.addView(getMoneyPasswordLayout())
        }
        if (type and VerifyType.PASSWORD == VerifyType.PASSWORD) {
            contentLayout?.addView(getPasswordLayout())
        }
        workSpaceLayout?.removeAllViews()
        workSpaceLayout?.addView(innerView)
    }

    override val result: Target
        get() {
            val target = Target()
            target.googleCode = googleCode
            target.phoneCode = phoneCode
            target.phoneCaptcha = phoneCaptcha
            target.mailCode = mailCode
            target.mailCaptcha = mailCaptcha
            target.password = password
            target.moneyPassword = moneyPassword
            return target
        }

    override val returnType: Int
        get() = VerifyType.NONE
}
