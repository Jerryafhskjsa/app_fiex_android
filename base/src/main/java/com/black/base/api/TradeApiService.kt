package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderResult
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

interface TradeApiService {
    @FormUrlEncoded
    @POST(UrlConfig.Trade.URL_CREATE_TRADE_ORDER_NEW)
    fun createTradeOrder(@Field("symbol") symbol: String?, @Field("direction") direction: String?, @Field("totalAmount") totalAmount: String?, @Field("price") price: String?, @Field("tradeType") tradeType: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Trade.URL_URL_CANCEL_TRADE_ORDER_NEW)
    fun cancelTradeOrder(@Field("orderNo") orderId: String?, @Field("pair") pair: String?, @Field("direction") direction: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Trade.URL_URL_CANCEL_TRADE_ORDER_NEW)
    fun cancelTradeOrderFiex(@Field("orderId") orderId: String?): Observable<HttpRequestResultString?>?


    @GET(UrlConfig.Trade.URL_TRADE_ORDERS_RECORD)
    fun getTradeOrderRecord(@Query("pair") pair: String?, @Query("ended") ended: Boolean, @Query("page") page: Int, @Query("size") size: Int, @Query("asc") asc: Boolean, @Query("startTime") startTime: String?, @Query("endTime") endTime: String?, @Query("leverType") leverType: String?): Observable<HttpRequestResultData<PagingData<TradeOrder?>?>?>?

    /**
     * state 1：新建订单;未成交; 2：部分成交；3：全部成交；4：已撤销；5：下单失败；6：已过期; 9:未完成；10：历史订单
     */
    @GET(UrlConfig.Trade.URL_TRADE_ORDERS_RECORD)
    fun getTradeOrderRecordFiex(@Query("symbol") symbol: String?, @Query("state") state: Int, @Query("startTime") startTime: String?, @Query("endTime") endTime: String?): Observable<HttpRequestResultData<TradeOrderResult?>?>?

    @GET(UrlConfig.Trade.URL_TRADE_ORDERS_DEPTH)
    fun getTradeOrderDepth(@Query("level") level: Int?, @Query("symbol") symbol: String?): Observable<HttpRequestResultData<TradeOrderDepth?>?>?

}
