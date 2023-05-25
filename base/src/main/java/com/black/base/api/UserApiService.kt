package com.black.base.api

import com.black.base.model.*
import com.black.base.model.user.*
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface UserApiService {
    //get jsessionId
    @FormUrlEncoded
    @POST(UrlConfig.User.URL_TOKEN)
    fun getToken(@Field("telCountryCode") telCountryCode: String?, @Field("username") username: String?, @Field("password") password: String?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.User.URL_TICKET)
    fun getTicket(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.User.URL_USER_LOGIN)
    fun getProToken(): Observable<HttpRequestResultData<ProTokenResult?>?>?

    @POST(UrlConfig.User.OTC_LOGIN)
    fun getOtcToken(): Observable<HttpRequestResultData<LoginVO?>?>?

    @POST(UrlConfig.User.FIC_LOGIN)
    fun getFicToken(): Observable<HttpRequestResultData<LoginVO?>?>?

    @GET(UrlConfig.User.URL_WS_TOKEN)
    fun getWsToken(): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_LOGIN_SUFFIX)
    fun loginSuffix(@Field("prefixAuth") prefixAuth: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?): Observable<HttpRequestResultString?>?

    //fiex 返回{"ucToken","ticket"}
    @FormUrlEncoded
    @POST(UrlConfig.User.URL_LOGIN_SUFFIX)
    fun loginSuffixResultObj(@Field("prefixAuth") prefixAuth: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?): Observable<HttpRequestResultData<SuffixResult?>?>?




    @FormUrlEncoded
    @POST(UrlConfig.User.URL_LOGIN_SUFFIX)
    fun loginSuffixGoogle(@Field(value = "prefixAuth", encoded = false) prefixAuth: String?, @Field(value = "googleCode", encoded = false) googleCode: String?): Observable<HttpRequestResultString?>?

//    @POST(UrlConfig.User.URL_LOGIN_SUFFIX)
//    Observable<HttpRequestResultString> loginSuffixGoogle(@Body HashMap<String, Object> params);

    //    @POST(UrlConfig.User.URL_LOGIN_SUFFIX)
//    Observable<HttpRequestResultString> loginSuffixGoogle(@Body HashMap<String, Object> params);
    @FormUrlEncoded
    @POST(UrlConfig.User.URL_SEND_VERIFY_CODE)
    fun sendVerifyCode(@Field("username") userName: String?, @Field("telCountryCode") telCountryCode: String?, @Field("verifyCode") verifyCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_SEND_VERIFY_CODE)
    fun sendVerifyCodeGeeTest(@Field("username") userName: String?, @Field("telCountryCode") telCountryCode: String?, @Field(value = "geetest", encoded = false) geetest: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_SEND_VERIFY_CODE_02)
    fun sendVerifyCode02(@Field("username") userName: String?, @Field("telCountryCode") telCountryCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_REGISTER)
    fun register(@Field("username") userName: String?, @Field("password") password: String?, @Field("telCountryCode") telCountryCode: String?, @Field("verifyCode") verifyCode: String?, @Field("captcha") captcha: String?, @Field("inviteCode") inviteCode: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_USER_INFO)
    fun getUserInfo(): Observable<HttpRequestResultData<UserInfo?>?>?

    //邮箱用户只用传 username
    @FormUrlEncoded
    @POST(UrlConfig.User.URL_RESET_PASSWORD)
    fun resetPassword(@Field("username") userName: String?, @Field("telCountryCode") telCountryCode: String?, @Field("verifyCode") verifyCode: String?, @Field("captcha") captcha: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_RESET_PASSWORD_SUFFIX)
    fun resetPasswordSuffix(@Field("newPassword") newPassword: String?, @Field("prefixAuth") prefixAuth: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?, @Field("captchaPhone") captchaPhone: String?, @Field("captchaEmail") captchaEmail: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_GOOGLE_CODE)
    fun getGoogleCode(): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_ENABLE_SECURITY)
    fun enableSecurity(@Field("telCountryCode") telCountryCode: String?, @Field("phone") phone: String?, @Field("phoneCode") phoneCode: String?,@Field("newPhone") newPhone: String?, @Field("newPhoneCode") newPhoneCode: String?,  @Field("email") email: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?, @Field("password") password: String?, @Field("action") action: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_PHONE_SECURITY)
    fun phoneSecurity(@Field("telCountryCode") telCountryCode: String?, @Field("phone") phone: String?, @Field("phoneCode") phoneCode: String?, @Field("newPhoneCode") newPhoneCode: String?,  @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_IDENTITY_BIND)
    fun bindIdentity(@Field("idType") idType: Int, @Field("realName") realName: String?, @Field("idNo") idNo: String?, @Field("idNoImg") idNoImg: String?, @Field("country") countryId: String?,@Field("birthday") birthday: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_IDENTITY_BIND_AI)
    fun bindIdentityAI(@Field("idNo") idNo: String?, @Field("score") score: String?, @Field("idNoImg") idNoImg: String?, @Field("realName") realName: String?, @Field("country") countryId: String?): Observable<HttpRequestResultString?>?

    @Multipart
    @POST(UrlConfig.User.URL_UPLOAD)
    fun upload(@Part("description") description: RequestBody?, @Part file: MultipartBody.Part?): Observable<HttpRequestResultString?>?

    @Multipart
    @POST(UrlConfig.User.URL_UPLOAD_PUBLIC)
    fun uploadPublic(@Part("description") description: RequestBody?, @Part file: MultipartBody.Part?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_RECOMMEND_COUNT)
    fun getRecommendCount(): Observable<HttpRequestResultData<Int?>?>?

    @GET(UrlConfig.User.URL_RECOMMEND_DETAIL)
    fun getRecommendDetail(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("level") level: String?, @Query("start") from: String?, @Query("end") to: String?): Observable<HttpRequestResultData<PagingData<RecommendPeopleDetail?>?>?>?

    @POST(UrlConfig.User.URL_LOGOUT)
    fun logout(): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_CHANGE_PASSWORD)
    fun changePassword(@Field("password") password: String?, @Field("newPassword") newPassword: String?, @Field("phoneCode") phoneCode: String?, @Field("googleCode") googleCode: String?, @Field("emailCode") emailCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_MONEY_PASSWORD)
    fun setMoneyPassword(@Field("moneyPassword") password: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_MONEY_PASSWORD_REMOVE)
    fun removeMoneyPassword(@Field("moneyPassword") moneyPassword: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_MONEY_PASSWORD_RESET)
    fun resetMoneyPassword(@Field("moneyPassword") moneyPassword: String?, @Field("phoneCode") phoneCode: String?, @Field("emailCode") emailCode: String?, @Field("googleCode") googleCode: String?, @Field("password") password: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_USER_INFO_UPDATE)
    fun postUserInfoModify(@Field("avatar") avatarUrl: String?, @Field("nickName") nickName: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_PUSH_SWITCH_LIST)
    fun getPushSwitchList(): Observable<HttpRequestResultData<PushSwitch?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_PUSH_SWITCH_CHANGE)
    fun modifyPushSwitch(@Field("orderSwitch") tradeSwitch: String?, @Field("investSwitch") rechargeSwitch: String?, @Field("withdrawSwitch") extractSwitch: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_CHECK_CHAT_ENABLE)
    fun checkChatEnable(@Query("coinType") coinType: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_CHECK_MAIN_CHAT_ENABLE)
    fun checkMainChatEnable(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.User.URL_SUPPORT)
    fun getSupportUrl(): Observable<HttpRequestResultData<String?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.User.URL_AGREE_LEVER_PROTOCOL)
    fun agreeLeverProtocol(@Field("aBoolean") agree: Boolean?): Observable<HttpRequestResultString?>?
}