package com.black.base.model.user

import android.text.TextUtils
import com.black.base.model.BaseAdapterItem
import com.black.util.CommonUtil

class UserInfo : BaseAdapterItem() {
    var id: String? = null
    var tel: String? = null
    var telCountryCode: String? = null
    var countryZh: String? = null
    var countryEn: String? = null
    var email: String? = null
    var nickname: String? = null
    var realName: String? = null
    var password: String? = null
    var vip: String? = null
    var payPassword: String? = null
    var idNo //身份证号
            : String? = null
    var lastLoginIp: String? = null
    var lastLoginTime: String? = null
    var inviteBy: String? = null
    var inviteCode: String? = null
    var userRole: String? = null
    //    public String googleAuth;
    var userStatus: String? = null
    var wechatId: String? = null
    var updateTime: String? = null
    var createTime: String? = null
    var registerFrom //注册来源: phone-手机  email-邮箱
            : String? = null
    var username: String? = null
    var securityLevel //安全等级： 1-低  2-中  3-高
            : String? = null
    var phoneSecurityStatus //手机验证状态：0-关闭  1-开启
            : String? = null
    var emailSecurityStatus //邮箱验证状态：0-关闭  1-开启
            : String? = null
    var googleSecurityStatus //google验证状态：0-关闭  1-开启
            : String? = null
    var authType
    //用户提现所需验证：1-手机+邮箱  2-手机+google 3-邮箱+google 4-手机+google+邮箱 5-必须完成邮箱或google其中一项  6-必须完成手机或google其中一项
    //用户提现所需验证：4-手机+google 5-邮箱+google 6-手机+邮箱  7-手机+google+邮箱 1,2,3-必须完成邮箱或google其中一项  -必须完成手机或google其中一项
            : String? = null
    var idNoStatus //0未认证 1已认证 2 审核中 3.未通过
            : String? = null
    var bankAuthStatus //0 未认证 1已认证
            : String? = null
    var withDrawStatus //0不可提现 1可提现
            : String? = null
    var backReason //实名认证失败原因
            : String? = null
    var moneyPasswordStatus //资金密码状态 0 未设置 1 已设置
            : String? = null
    var udeskSignature //客服token
            : String? = null
    var timUserSig //騰訊IM签名
            : String? = null
    var userType: String? = null
    @JvmField
    var isMerchant: String? = null
    @JvmField
    var isMasterNode: String? = null
    var moneyPwdSwitch: String? = null
    var merchant: String? = null
    var masterNode: String? = null
    var headPortrait //头像地址
            : String? = null
    var openLever: Boolean? = null
    fun registerFromMail(): Boolean {
        return "email" == registerFrom
    }

    val displayName: String
        get() = if (!TextUtils.isEmpty(tel)) CommonUtil.secretPhoneNumber(tel) else if (!TextUtils.isEmpty(email)) email!! else realName!!

    val displayUserId: String
        get() = if (id == null) "" else (if (id!!.length > 8) id!!.substring(0, 8) else id!!)

    fun isRealName(): Boolean {
        return TextUtils.equals("1", idNoStatus)
    }

    fun getSecurityLevel(): Int {
        val phoneSecurity = if (phoneSecurityStatus != null && !TextUtils.equals("0", phoneSecurityStatus)) 1 else 0
        val emailSecurity = if (emailSecurityStatus != null && !TextUtils.equals("0", emailSecurityStatus)) 1 else 0
        val googleSecurity = if (googleSecurityStatus != null && !TextUtils.equals("0", googleSecurityStatus)) 1 else 0
        return phoneSecurity + emailSecurity + googleSecurity
    }
}