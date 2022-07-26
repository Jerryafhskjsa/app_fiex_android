package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.c2c.*
import com.black.base.model.user.PaymentMethod
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

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
}
