package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.socket.TradeOrder
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

interface TradeApiService {
    @FormUrlEncoded
    @POST(UrlConfig.Trade.URL_CREATE_TRADE_ORDER_NEW)
    fun createTradeOrder(@Field("pair") pair: String?, @Field("direction") direction: String?, @Field("totalAmount") totalAmount: String?, @Field("price") price: String?, @Field("orderLeverType") levelType: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Trade.URL_URL_CANCEL_TRADE_ORDER_NEW)
    fun cancelTradeOrder(@Field("orderNo") orderId: String?, @Field("pair") pair: String?, @Field("direction") direction: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Trade.URL_TRADE_ORDERS_RECORD)
    fun getTradeOrderRecord(@Query("pair") pair: String?, @Query("ended") ended: Boolean, @Query("page") page: Int, @Query("size") size: Int, @Query("asc") asc: Boolean, @Query("startTime") startTime: String?, @Query("endTime") endTime: String?, @Query("leverType") leverType: String?): Observable<HttpRequestResultData<PagingData<TradeOrder?>?>?>?
}
