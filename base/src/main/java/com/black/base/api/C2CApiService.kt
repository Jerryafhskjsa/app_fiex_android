package com.black.base.api

import com.black.base.model.*
import com.black.base.model.c2c.*
import com.black.base.model.user.PaymentMethod
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*
import java.math.BigDecimal

interface C2CApiService {
    @GET(UrlConfig.C2C.URL_C2C_MERCHANT)
    fun getC2CMerchant(@Query("coinType") coinType: String?, @Query("direction") direction: String?, @Query("pageNum") pageNum: Int, @Query("pageSize") pageSize: Int): Observable<HttpRequestResultData<PagingData<C2CSeller?>?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_CREATE_ORDER_BUY)
    fun createOrderBuy(@Field("coinType") coinType: String?, @Field("direction") direction: String?, @Field("amount") amount: String?, @Field("merchantId") merchantId: String?, @Field("isOneKey") isOneKey: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_CREATE_ORDER_SELL)
    fun createOrderSell(@Field("coinType") coinType: String?, @Field("direction") direction: String?, @Field("amount") amount: String?, @Field("merchantId") merchantId: String?, @Field("isOneKey") isOneKey: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_ORDER_LIST)
    fun getOrderList(@Query("direction") direction: String?, @Query("status") status: String?, @Query("page") pageNum: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<C2COrder?>?>?>?

    @GET(UrlConfig.C2C.URL_C2C_ORDER_DETAIL_LIST)
    fun getOrderDetailList(@Query("id") orderId: String?, @Query("lastTime") lastTime: String?, @Query("direction") direction: String?): Observable<HttpRequestResultDataList<C2COrderDetailItem?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_ORDER_DETAIL_CREATE)
    fun createOrderDetail(@Field("id") orderId: String?, @Field("content") note: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_ORDER_DETAIL)
    fun getOrderDetail(@Query("id") id: String?): Observable<HttpRequestResultData<C2CDetail?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_ORDER_CONFIRM)
    fun confirmPaid(@Field("id") id: String?, @Field("paymentId") payment: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_ORDER_CANCEL)
    fun cancelOrder(@Field("id") id: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_ORDER_RELEASE)
    fun releaseCoin(@Field("id") id: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_IS_AGREE)
    fun isAgree(): Observable<HttpRequestResultData<C2CAgreement?>?>?

    @POST(UrlConfig.C2C.URL_C2C_AGREE)
    fun agree(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_COIN_TYPE)
    fun getCoinTypeList(): Observable<HttpRequestResultDataList<C2CSupportCoin?>?>?

    @GET(UrlConfig.C2C.URL_C2C_PAYMENT_METHOD_ALL)
    fun getPaymentMethodAll(@Query("isActive") isActive: String?): Observable<HttpRequestResultDataList<PaymentMethod?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_PAYMENT_METHOD_DELETE)
    fun deletePaymentMethod(@Field("id") id: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_PAYMENT_METHOD_ADD)
    fun addPaymentMethod(@Field("payeeName") userName: String?, @Field("type") type: String?, @Field("account") account: String?, @Field("bankName") bankName: String?, @Field("branchBankName") branchBankName: String?, @Field("url") qrcodeUrl: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.C2C.URL_C2C_PAYMENT_METHOD_UPDATE)
    fun updatePaymentMethod(@Field("id") id: String?, @Field("status") status: Int): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_MERCHANT_FAST)
    fun getC2CMerchantFast(@Query("coinType") coinType: String?, @Query("direction") direction: String?, @Query("pageNum") pageNum: Int, @Query("pageSize") pageSize: Int): Observable<HttpRequestResultDataList<C2CSeller?>?>?

    //OTC广告
    @GET(UrlConfig.C2C.URL_C2C_CONFIG)
    fun getC2CConfig(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_CONFIG_V2)
    fun getC2CConfigV2(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_CREATE)
    fun getC2CCreateAD(@Query("coinType") coinType: String, @Query("currencyCoin") currencyCoin: String, @Query("direction") direction: String, @Query("payMethods") payMethods: String, @Query("priceParam") priceParam: BigDecimal, @Query("priceType") priceType: Int, @Query("singleLimitMax") singleLimitMax: BigDecimal, @Query("singleLimitMin") singleLimitMin: BigDecimal, @Query("totalAmount") totalAmount: BigDecimal, completedOrders: Int?, @Query("completion") completion: BigDecimal?, @Query("registeredDays") registeredDays: Int?, @Query("remark") remark: String? , @Query("soldOutTime") soldOutTime: Int?,): Observable<HttpRequestResultDataList<C2CNewAD?>?>?

    @GET(UrlConfig.C2C.URL_C2C_CURRENT_PRICE)
    fun getC2CPriceCurrent(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_AD_DELETE)
    fun getC2CADDelete(@Query("id") id: String): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_INDEX_PRICE)
    fun getC2CIndexPrice(@Query("currencyCoin") currencyCoin: String?): Observable<HttpRequestResultDataList<C2CIndexPrice?>?>?

    @GET(UrlConfig.C2C.URL_C2C_AD_INFO)
    fun getC2CADInfo(@Query("id") id: String?): Observable<HttpRequestResultData<C2CMainAD?>?>?

    @GET(UrlConfig.C2C.URL_C2C_AD_LIST)
    fun getC2CADList(@Query("coinType") coinType: String?,@Query("direction") direction: String?,@Query("gteSingleLimitMin") gteSingleLimitMin:Double?,@Query("payMethod") payMethod: String?): Observable<HttpRequestResultData<C2CADData<C2CMainAD?>?>?>?

    @GET(UrlConfig.C2C.URL_C2C_AD_MERCHANT_PAGE)
    fun getC2CADMerchantPage(@Query("merchantId")merchantId: Int?): Observable<HttpRequestResultData<C2CMainAD?>?>?

    @GET(UrlConfig.C2C.URL_C2C_MY_LIST)
    fun getC2CMyList(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_PRICE)
    fun getC2CPrc(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_PUBLISH)
    fun getC2CPublish(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_QUICK_PUBLISH)
    fun getC2CQuickPublish(@Query("gteAmount") gteAmount: Double?,@Query("gteCurrencyCoinAmount") gteCurrencyCoinAmount: Double?,@Query("coinType") coinType: String?,@Query("direction") direction: String?,@Query("payMethod")payMethod: String?): Observable<HttpRequestResultData<C2CMainAD?>?>?

    @GET(UrlConfig.C2C.URL_C2C_QUICK_CONFIG)
    fun getC2CQuickConfig(@Query("coinType") coinType: String?,@Query("currencyCoin") currencyCoin: String?): Observable<HttpRequestResultData<OrderConfig?>?>?

    @POST(UrlConfig.C2C.URL_C2C_SOLD_OUT)
    fun getC2CSoldOut(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_SUPPORT_COIN)
    fun getC2CSupportCoin(): Observable<HttpRequestResultDataList<C2CSupportCoin?>?>?

    @POST(UrlConfig.C2C.URL_C2C_AD_UPDATE)
    fun getC2CADUpdate(): Observable<HttpRequestResultString?>?

    //OTC用户相关接口
    @GET(UrlConfig.C2C.URL_C2C_ACCOUNT)
    fun getC2CAccount(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_APPLY_PAYEE)
    fun getC2CApplyPayee(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_DELETE)
    fun getC2CDelete(@Query("receiptId") receiptId: Int?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_PAYEE)
    fun getC2CPayee(): Observable<HttpRequestResultData<C2CSellerMsg?>?>?

    @GET(UrlConfig.C2C.URL_C2C_RECEIPT)
    fun getC2CReceipt(): Observable<HttpRequestResultDataList<PayInfo?>?>?

    @GET(UrlConfig.C2C.URL_C2C_USERINFO)
    fun getC2CUserInfo(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_OPEN)
    fun getC2COpen(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_REFUSE_PAYEE)
    fun getC2CRefusePayee(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_SET_RECEIPT)
    fun getC2CSetReceipt(@Body OtcReceiptModel:OtcReceiptModel?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_UNBIND_PAYEE)
    fun getC2CUnbindPayee(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_UPDATE)
    fun getC2CUpdate(): Observable<HttpRequestResultString?>?
    //OTC聊天
    @POST(UrlConfig.C2C.URL_C2C_CREATE_IMG)
    fun getC2CImage(@Query("id") id: String?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_CRATE_TXT)
    fun getC2CText(@Query("id") id: String?,@Query("content") content: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_REPLY_LIST)
    fun getC2CList(@Query("id") id: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_REPLY_PULL)
    fun getC2CPull(@Query("id") id: String?,@Query("replyId") replyId: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_REAL_TIME)
    fun getC2CTime(): Observable<HttpRequestResultString?>?

    //OTC订单
    @POST(UrlConfig.C2C.URL_C2C_CANCEL)
    fun getC2CCancel(@Query("id") id: String?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_CONFIRM_PAY)
    fun getC2CConfirmPay(@Query("id") id: String?,@Query("payEeId") payEeId: Int?,@Query("payMethod") payMethod: Int?, @Query("receiptId") receiptId: Int?):  Observable<HttpRequestResultData<String?>?>?

    @POST(UrlConfig.C2C.URL_C2C_CP)
    fun getC2CCP(@Query("id") id: String?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_CREATE_V2)
    fun getC2CCreateV2(@Query("advertisingId") advertisingId: String?,@Query("amount") amount: Double?,@Query("price") price: Double?): Observable<HttpRequestResultData<String?>?>?

    @GET(UrlConfig.C2C.URL_C2C_GP)
    fun getC2CGP(@Query("id") id: String?): Observable<HttpRequestResultDataList<PayInfo?>?>?

    @GET(UrlConfig.C2C.URL_C2C_ORDER_INFO)
    fun getC2COrderInfo(@Query("id") id: String?): Observable<HttpRequestResultData<C2COrderDetails?>?>?

    @POST(UrlConfig.C2C.URL_C2C_OI_V2)
    fun getC2COIV2(@Query("id") id: String?): Observable<HttpRequestResultData<C2COrderDetails?>?>?

    @GET(UrlConfig.C2C.URL_C2C_OL)
    fun getC2COL(@Query("coinType") coinType: String?, @Query("currencyCoinType") currencyCoin: String?,  @Query("direction") direction: String?,  @Query("gteAmount") gteAmount: Double?,  @Query("gteCurrencyCoinAmount") gteCurrencyCoinAmount: Double?,  @Query("page") page: Int?, @Query("size") size: Int?, @Query("status") status: Int?): Observable<HttpRequestResultData<C2CADData<C2CBills?>?>?>?

    @GET(UrlConfig.C2C.URL_C2C_OO)
    fun getC2COO(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_UFC)
    fun getC2CUFC(): Observable<HttpRequestResultString?>?

    //OTC商家相关
    @POST(UrlConfig.C2C.URL_C2C_ADD_P)
    fun getC2CAddP(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_MCA)
    fun getC2CMca(@Body otcMerchantDTO: OtcMerchantDTO?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_MCN)
    fun getC2CMcn(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_GM)
    fun getC2CGm(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_GPL)
    fun getC2CGpl(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_MCP)
    fun getC2CMcp(@Query("merchantId")merchantId: Int?): Observable<HttpRequestResultData<C2CSMSG?>?>?

    @POST(UrlConfig.C2C.URL_C2C_MP)
    fun getC2CMp(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_QVF)
    fun getC2CQvf(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_MUP)
    fun getC2CMup(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_C2C_UMC)
    fun getC2CUmc(): Observable<HttpRequestResultString?>?

    //申述、登陆、谷歌验证
    @POST(UrlConfig.C2C.URL_CA)
    fun getC2CCa(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_VFC)
    fun getC2CVfc(@Query("googleCode") googleCode: String?): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_CU)
    fun getC2CCu(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_LOGIN)
    fun getOtcToken(): Observable<HttpRequestResultData<LoginVO?>?>?

    @POST(UrlConfig.C2C.URL_C2C_ALLEGE)
    fun getC2CAllege(): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.C2C.URL_C2C_ALLEGE_INFO)
    fun getC2CAllegeInfo(): Observable<HttpRequestResultString?>?

    @POST(UrlConfig.C2C.URL_LOGIN)
    fun getC2CLogin(): Observable<HttpRequestResultData<ProTokenResult?>?>?
}
