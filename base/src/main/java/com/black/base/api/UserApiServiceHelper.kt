package com.black.base.api

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import com.black.base.R
import com.black.base.api.CommonApiServiceHelper.geetestInit
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.user.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.util.FryingUtil.getLoadDialog
import com.black.base.util.FryingUtil.showToast
import com.black.base.view.LoadingDialog
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.geetest.captcha.GTCaptcha4Client
import com.geetest.captcha.GTCaptcha4Config
import com.google.gson.JsonObject
import com.netease.nis.captcha.Captcha
import com.netease.nis.captcha.CaptchaConfiguration
import com.netease.nis.captcha.CaptchaListener
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File

object UserApiServiceHelper {
    //获取ticket
    fun getTicket(context: Context,callback:Callback<HttpRequestResultString?>){
        ApiManager.build(context,true,UrlConfig.ApiType.URL_PRO).getService(UserApiService::class.java)
            ?.getTicket()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //获取pro-token
    fun getProToken(context: Context,callback:Callback<HttpRequestResultData<ProTokenResult?>?>){
        ApiManager.build(context,true,UrlConfig.ApiType.URL_PRO).getService(UserApiService::class.java)
            ?.getProToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //获取otc_token
    fun getOtcToken(context: Context?,callback:Callback<HttpRequestResultData<LoginVO?>?>){
        ApiManager.build(context!!,true,UrlConfig.ApiType.URL_API).getService(UserApiService::class.java)
            ?.getOtcToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //获取fic_token
    fun getFicToken(context: Context?,callback:Callback<HttpRequestResultData<LoginVO?>?>){
        ApiManager.build(context!!,true,UrlConfig.ApiType.URL_FIC).getService(UserApiService::class.java)
            ?.getFicToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //跳转至理财
    fun backFinancal(context: Context?,callback:Callback<HttpRequestResultData<LoginVO?>?>){
        ApiManager.build3(context!!,true).getService(UserApiService::class.java)
            ?.getFicToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //获取ws-token
    fun getWsToken(context: Context,callback:Callback<HttpRequestResultString?>){
        ApiManager.build(context,true,UrlConfig.ApiType.URL_PRO).getService(UserApiService::class.java)
            ?.getWsToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    //获取futures-token
    fun getFutureToken(context: Context,callback:Callback<HttpRequestResultString?>){
        ApiManager.build(context!!,true,UrlConfig.ApiType.URL_PRO).getService(UserApiService::class.java)
            ?.getWsToken()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }

    fun upload(context: Context?, key: String, file: File, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData(key, file.name, requestFile)
        val description = RequestBody.create(MediaType.parse("multipart/form-data"), key)
        //        RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), key);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("text/plain"), file);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.upload(description, body)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun uploadPublic(context: Context?, key: String, file: File, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData(key, file.name, requestFile)
        val description = RequestBody.create(MediaType.parse("multipart/form-data"), key)
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.uploadPublic(description, body)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getVerifyCode(context: Context?, userName: String?, telCountryCode: String?, callback: Callback<HttpRequestResultString?>?) {
//        getVerifyCode(context, userName, telCountryCode, false, callback)
        getVerifyCodeOld(context,userName,telCountryCode,false,callback)
    }

    /**
     * 网易盾验证
     */
    fun getVerifyCodeOld(context: Context?, userName: String?, telCountryCode: String?, alwaysCaptcha: Boolean, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        val verifyCodeCallBack = VerifyCodeCallBack(callback)
        if (alwaysCaptcha || TextUtils.isEmpty(CookieUtil.getToken(context))) { //判断是否需要做极验，serverUrl 是发送验证码，并且 没有token,验证
            geetestInit(context, object : NormalCallback<HttpRequestResultData<JsonObject?>?>(context) {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultData<JsonObject?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        // TODO 设置返回api1数据，即使为null也要设置，SDK内部已处理
                        var jsonObject: JSONObject? = null
                        try {
                            jsonObject = JSONObject(returnData.data.toString())
                        } catch (e: JSONException) {
                        }
                        if(jsonObject != null) {
                            var type = jsonObject.getString("type")
                            val captchaId = jsonObject.getString("gt")
                            val newCaptcha = jsonObject.getBoolean("new_captcha")
                            if (newCaptcha) {
                                val config = GTCaptcha4Config
                                    .Builder()
                                    .setTimeOut(10000)
                                    .build()
                                val gtCaptcha4Client = GTCaptcha4Client.getClient(context)
                                    .init(captchaId, config)
                                gtCaptcha4Client.addOnSuccessListener(GTCaptcha4Client.OnSuccessListener { b, s ->
                                    if (b) {
                                        verifyCodeCallBack.captcha = s
                                        FryingUtil.showToast(
                                            context,
                                            context.getString(R.string.alert_verify_code_success)
                                        )
                                        sendVerifyCodeGeeTest(
                                            context,
                                            userName,
                                            telCountryCode,
                                            s.toString(),
                                            object :
                                                Callback<HttpRequestResultString?>() {
                                                override fun error(type: Int, error: Any?) {
                                                }

                                                override fun callback(returnData: HttpRequestResultString?) {

                                                }

                                            })
                                    } else {
                                        FryingUtil.showToast(
                                            context,
                                            context.getString(R.string.alert_verify_code_failed)
                                        )
                                    }
                                })
                                gtCaptcha4Client.addOnFailureListener(GTCaptcha4Client.OnFailureListener {
                                    fun onFailure() {

                                    }
                                })
                                    .verifyWithCaptcha()
                            }
                            else
                            {
                                sendVerifyCodeGeeTest(
                                    context,
                                    userName,
                                    telCountryCode,
                                    captchaId,
                                    object :
                                        Callback<HttpRequestResultString?>() {
                                        override fun error(type: Int, error: Any?) {
                                        }

                                        override fun callback(returnData: HttpRequestResultString?) {
                                            if (returnData?.code == HttpRequestResult.SUCCESS){
                                                FryingUtil.showToast(
                                                    context,
                                                    context.getString(R.string.alert_verify_code_success)
                                                )
                                            }
                                            else
                                            {
                                                FryingUtil.showToast(
                                                    context,
                                                   returnData?.msg
                                                )
                                            }

                                        }

                                    })
                            }

                        }
                    } else {
                        showToast(context, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })

        } else {
            ApiManager.build(context).getService(UserApiService::class.java)
                    ?.sendVerifyCode02(userName, telCountryCode)
                    ?.compose(RxJavaHelper.observeOnMainThread())
                    ?.subscribe(HttpCallbackSimple(context, true, verifyCodeCallBack))
        }
    }

    /**
     * 极验
     */
    fun getVerifyCode(context: Context?, userName: String?, telCountryCode: String?, alwaysCaptcha: Boolean, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        getVerifyCodeOld(context,userName,telCountryCode,alwaysCaptcha,callback)
//        val verifyCodeCallBack = VerifyCodeCallBack(callback)
//        if (alwaysCaptcha || TextUtils.isEmpty(CookieUtil.getToken(context))) { //判断是否需要做极验，serverUrl 是发送验证码，并且 没有token,验证
//            if (context is GeeTestInterface) {
//                (context as GeeTestInterface).startVerify(object : GeeTestCallback {
//                    override fun onApi1(api1Callback: GeeTestApi1Callback?) {
//                        geetestInit(context, object : NormalCallback<HttpRequestResultData<JsonObject?>?>(context) {
//                            override fun error(type: Int, error: Any?) {
//                                verifyNext(null)
//                            }
//
//                            override fun callback(returnData: HttpRequestResultData<JsonObject?>?) {
//                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                                    // TODO 设置返回api1数据，即使为null也要设置，SDK内部已处理
//                                    var jsonObject: JSONObject? = null
//                                    try {
//                                        jsonObject = JSONObject(returnData.data.toString())
//                                    } catch (e: JSONException) {
//                                    }
//                                    verifyNext(jsonObject)
//                                } else {
//                                    showToast(context, if (returnData == null) "null" else returnData.msg)
//                                    verifyNext(null)
//                                }
//                            }
//
//                            private fun verifyNext(jsonObject: JSONObject?) {
//                                api1Callback?.callback(jsonObject)
//                            }
//                        })
//                    }
//
//                    override fun onApi2(result: String?, api2Callback: GeeTestApi2Callback?) {
//                        api2Callback?.dismiss()
//                        val geeTestResult = Gson().fromJson(result, GeeTestResult::class.java)
//                        sendVerifyCodeGeeTest(context, userName, telCountryCode, geeTestResult.toJsonString(), object : NormalCallback<HttpRequestResultString?>(context) {
//                            override fun error(type: Int, error: Any?) {
//                                verifyCodeCallBack.error(type, error!!)
//                            }
//
//                            override fun callback(returnData: HttpRequestResultString?) {
//                                verifyCodeCallBack.callback(returnData)
//                            }
//                        })
//                    }
//                })
//            } else {
//                throw RuntimeException(context.javaClass.simpleName + "  must implements GeeTestInterface")
//            }
//        } else {
//            ApiManager.build(context).getService(UserApiService::class.java)
//                    ?.sendVerifyCode02(userName, telCountryCode)
//                    ?.compose(RxJavaHelper.observeOnMainThread())
//                    ?.subscribe(HttpCallbackSimple(context, true, verifyCodeCallBack))
//        }
    }

    fun sendVerifyCodeGeeTest(context: Context?, userName: String?, telCountryCode: String?, geetest: String?, callback: Callback<HttpRequestResultString?>?) {
        val verifyCodeCallBack = VerifyCodeCallBack(callback)
        ApiManager.build(context!!, true,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.sendVerifyCodeGeeTest(userName, telCountryCode, geetest)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, verifyCodeCallBack))
    }


    //绑定身份证
    fun getSendVerifyCode(context: Context?, userName: String?, telCountryCode: String?,callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
            ?.sendVerifyCode02(userName, telCountryCode)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    //绑定身份证
    fun bindIdentity(context: Context?, idType: Int, realName: String?, idNo: String?, idNoImg: String?, countryId: String?, birthday: String?,callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.bindIdentity(idType, realName, idNo, idNoImg, countryId, birthday)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //绑定身份证活体
    fun bindIdentityAI(context: Context?, realName: String?, idNo: String?, countyId: String?, idNoImg: String?, score: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.bindIdentityAI(idNo, score, idNoImg, realName, countyId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

//    fun loginSuffix(context: Context?, prefixAuth: String?, phoneCode: String?, emailCode: String?, googleCode: String?, callback: Callback<HttpRequestResultString?>?) {
//        if (context == null || callback == null) {
//            return
//        }
//        ApiManager.build(context, true).getService(UserApiService::class.java)
//                ?.loginSuffix(prefixAuth, phoneCode, emailCode, googleCode)
//                ?.compose(RxJavaHelper.observeOnMainThread())
//                ?.subscribe(HttpCallbackSimple(context, true, callback))
//    }

    //    public static void loginSuffixGoogle(Context context, String prefixAuth, String googleCode, Callback<HttpRequestResultString?> callback) {
//        if (context == null || callback == null) {
//            return;
//        }
////        try {
////            prefixAuth = URLEncoder.encode(prefixAuth, "utf-8");
////        } catch (UnsupportedEncodingException e) {
////            e.printError();
////        }
//        HashMap<String, String> param = new HashMap<>();
//        param.put("prefixAuth", prefixAuth);
//        param.put("googleCode", googleCode);
//        ApiManager.build(context).getService(UserApiService.class)
//                .loginSuffixGoogle(prefixAuth, googleCode)
//                .compose(RxJavaHelper.observeOnMainThread())
//                .subscribe(new HttpCallbackSimple<>(context, true, callback));
//    }
    fun logout(context: Context?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.logout()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getUserInfo(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<UserInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getUserInfo()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun login(context: Context?, username: String?, password: String?, telCountryCode: String?, callback: Callback<HttpRequestResultData<SuffixResult?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getToken(telCountryCode, username, password)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun register(context: Context?, userName: String?, password: String?, telCountryCode: String?, verifyCode: String?, captcha: String?, inviteCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.register(userName, password, telCountryCode, verifyCode, captcha, inviteCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun resetPassword(context: Context?, userName: String?, telCountryCode: String?, verificationCode: String?, captcha: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(UserApiService::class.java)
                ?.resetPassword(userName, telCountryCode, verificationCode, captcha)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun resetPasswordSuffix(context: Context?, newPass: String?, prefixAuth: String?, phoneCode: String?, emailCode: String?, googleCode: String?, captchaPhone: String?, captchaEmail: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(UserApiService::class.java)
                ?.resetPasswordSuffix(newPass, prefixAuth, phoneCode, emailCode, googleCode, captchaPhone, captchaEmail)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getGoogleKey(context: Context?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getGoogleCode()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun enableSecurity(context: Context?, telCountryCode: String?, phone: String?, phoneCode: String?, newPhone: String?, newPhoneCode: String?, email: String?, emailCode: String?, googleCode: String?, password: String?, action: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.enableSecurity(telCountryCode, phone, phoneCode,newPhone,newPhoneCode, email, emailCode, googleCode, password, action)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun bindGoogle(context: Context?, phoneCode: String?, emailCode: String?, googleCode: String?, password: String?, callback: Callback<HttpRequestResultString?>?) {
        enableSecurity(context, null, null, phoneCode, null, null, null, emailCode, googleCode, password, "2", callback)
    }

    fun bindPhone(context: Context?, telCountryCode: String?, phone: String?, phoneCode: String?,newPhone: String?, newPhoneCode: String?,email: String?, emailCode: String?, googleCode: String?, callback: Callback<HttpRequestResultString?>?) {
        enableSecurity(context, telCountryCode, phone, phoneCode, null, null, email, emailCode, googleCode,null,"0", callback)
    }

    fun bindEmail(context: Context?,  email: String?, emailCode: String? , callback: Callback<HttpRequestResultString?>?) {
        enableSecurity(context, null, null,  null,null, null, email, emailCode, null, null, "4", callback)
    }
    fun bindSafe(context: Context?, phone: String? , phoneCode: String? , email: String?, emailCode: String?, googleCode: String?, callback: Callback<HttpRequestResultString?>?) {
        enableSecurity(context, null, phone, phoneCode,null, null, email, emailCode, googleCode, null, "1", callback)
    }


    fun phoneSecurity(context: Context?, telCountryCode: String?, phone: String?, phoneCode: String?, newPhoneCode: String?,  emailCode: String?, googleCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
            ?.phoneSecurity(telCountryCode, phone, phoneCode,newPhoneCode,emailCode, googleCode)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    /**
     * 查询用户邀请的人数量
     */
    fun getRecommendCount(context: Context?, callback: Callback<HttpRequestResultData<Int?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getRecommendCount()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    /**
     * 邀请的人详细信息
     */
    fun getRecommendPeopleDetail(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, level: String?, startTime: String?, endTime: String?, callback: Callback<HttpRequestResultData<PagingData<RecommendPeopleDetail?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getRecommendDetail(page, pageSize, level, startTime, endTime)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun changePassword(context: Context?, password: String?, newPass: String?, phoneCode: String?, googleCode: String?, emailCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.changePassword(password, newPass, phoneCode, googleCode, emailCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun setMoneyPassword(context: Context?, password: String?, phoneCode: String?, emailCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(UserApiService::class.java)
                ?.setMoneyPassword(password, phoneCode, emailCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun removeMoneyPassword(context: Context?, moneyPassword: String?, phoneCode: String?, emailCode: String?, googleCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(UserApiService::class.java)
                ?.removeMoneyPassword(moneyPassword, phoneCode, emailCode, googleCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun resetMoneyPassword(context: Context?, moneyPassword: String?, phoneCode: String?, emailCode: String?, googleCode: String?, password: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(UserApiService::class.java)
                ?.resetMoneyPassword(moneyPassword, phoneCode, emailCode, googleCode, password)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun modifyUserInfo(context: Context?, avatarUrl: String?, nickName: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.postUserInfoModify(avatarUrl, nickName)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getPushSwitchList(context: Context?, callback: Callback<HttpRequestResultData<PushSwitch?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getPushSwitchList()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun modifyPushSwitch(context: Context?, tradeSwitch: Boolean, rechargeSwitch: Boolean, extractSwitch: Boolean, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(UserApiService::class.java)
                ?.modifyPushSwitch(if (tradeSwitch) "1" else "0", if (rechargeSwitch) "1" else "0", if (extractSwitch) "1" else "0")
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun checkChatEnable(context: Context?, coinType: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(UserApiService::class.java)
                ?.checkChatEnable(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getSupportUrl(context: Context?, callback: Callback<HttpRequestResultData<String?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(UserApiService::class.java)
                ?.getSupportUrl()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    private class VerifyCodeCallBack internal constructor(private val callback: Callback<HttpRequestResultString?>?) : Callback<HttpRequestResultString?>() {
        internal var captcha //极验验证码
                : String? = null

        fun setCaptcha(captcha: String?) {
            this.captcha = captcha
        }

        override fun error(type: Int, error: Any) {
            callback?.error(type, error)
        }

        override fun callback(returnData: HttpRequestResultString?) {
            if (callback != null) {
                if (returnData != null) {
                    returnData.data = captcha
                }
                callback.callback(returnData)
            }
        }

    }
}
