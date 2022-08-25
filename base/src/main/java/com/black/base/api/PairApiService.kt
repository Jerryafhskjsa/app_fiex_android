package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.clutter.HomeSymbolList
import com.black.base.model.clutter.HomeTickers
import com.black.base.model.clutter.HomeTickersKline
import com.black.base.model.socket.CoinOrder
import com.black.base.model.socket.PairStatus
import com.black.base.model.trade.TradeSet
import com.black.base.model.trade.TradeSetFiex
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

interface PairApiService {

    @GET(UrlConfig.Config.URL_HOME_CONFIG_LIST)
    fun getHomeSymbolList():Observable<HttpRequestResultDataList<HomeSymbolList?>?>?

    @GET(UrlConfig.Config.URL_HOME_TICKERS)
    fun getHomeTickersList():Observable<HttpRequestResultDataList<HomeTickers?>?>?

    @GET(UrlConfig.Config.URL_HOME_KLine)
    fun getHomeKLine():Observable<HttpRequestResultDataList<HomeTickersKline?>?>?

    @GET(UrlConfig.Config.URL_SET_LIST)
    fun getTradeSetsFiex(): Observable<HttpRequestResultDataList<TradeSetFiex?>?>?


    @GET(UrlConfig.Config.URL_ORDERED_PAIRS)
    fun getOrderedPairs(@Query("pair") pair: String?): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Config.URL_DEAR_PAIRS)
    fun getDearPairs(): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Config.URL_PAIR_SET)
    fun getTradeSets(): Observable<HttpRequestResultDataList<TradeSet?>?>?

    @GET(UrlConfig.Config.URL_HOT_PAIRS)
    fun getHotPair(): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Config.URL_PAIRS_DEEPS)
    fun getTradePairInfo(@Query("pair") pair: String?): Observable<HttpRequestResultDataList<PairStatus?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Config.URL_PAIR_COLLECT)
    fun pairCollect(@Field("pair") pair: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Config.URL_PAIR_COLLECT_CANCEL)
    fun pairCollectCancel(@Field("pair") pair: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Config.URL_SEARCH_COLLECT_PAIRS)
    fun getCollectPairs(): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Config.URL_COIN_ORDERS_LIST)
    fun getCoinOrders(): Observable<HttpRequestResultData<CoinOrder?>?>?
}