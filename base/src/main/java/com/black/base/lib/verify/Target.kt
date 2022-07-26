package com.black.base.lib.verify

import com.black.base.model.user.UserInfo

class Target {
    var type = 0
    var prefixAuth: String? = null
    var title: String? = null
    var poneCountyCode: String? = null
    var phone: String? = null
    var phoneCode: String? = null
    var phoneCaptcha: String? = null
    var mail: String? = null
    var mailCode: String? = null
    var mailCaptcha: String? = null
    var googleCode: String? = null
    var password: String? = null
    var moneyPassword: String? = null

    fun clone(): Target {
        val target = Target()
        target.title = title
        target.poneCountyCode = poneCountyCode
        target.phone = phone
        //            target.phoneCode = phoneCode;
        target.mail = mail
        //            target.mailCode = mailCode;
//            target.googleCode = googleCode;
//            target.password = password;
        return target
    }

    companion object {
        fun buildFromUserInfo(info: UserInfo?): Target {
            val target = Target()
            if (info != null) {
                target.poneCountyCode = info.telCountryCode
                target.phone = info.tel
                target.mail = info.email
            }
            return target
        }
    }
}