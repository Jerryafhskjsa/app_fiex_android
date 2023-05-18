package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.QuotationSet
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

interface PairSuspendApiService {

    @GET(UrlConfig.Config.URL_HOME_CONFIG_LIST)
    suspend fun getHomeSymbolList(): HttpRequestResultDataList<HomeSymbolList?>?

    @GET(UrlConfig.Config.URL_PAIR_SYMBOL_CONFIG)
    suspend fun getPairSymbol(): HttpRequestResultData<HomeSymbolList?>?

    @GET(UrlConfig.Config.URL_HOME_TICKERS)
    suspend fun getHomeTickersList(): HttpRequestResultDataList<HomeTickers?>?

    /**
     * 单个交易对行情
     */
    @GET(UrlConfig.Config.URL_SYMBOL_TICKER)
    suspend fun getSymbolTicker(@Query("symbol") symbol: String?): HttpRequestResultData<HomeTickers?>?

    @GET(UrlConfig.Config.URL_HOME_KLine)
    suspend fun getHomeKLine(): HttpRequestResultDataList<HomeTickersKline?>?

    @GET(UrlConfig.Config.URL_SET_LIST)
    suspend fun getTradeSetsFiex(): HttpRequestResultDataList<QuotationSet?>?

    @GET(UrlConfig.Config.URL_ORDERED_PAIRS)
    suspend fun getOrderedPairs(@Query("pair") pair: String?): HttpRequestResultDataList<String?>?

    @GET(UrlConfig.Config.URL_DEAR_PAIRS)
    suspend fun getDearPairs(): HttpRequestResultDataList<String?>?

    @GET(UrlConfig.Config.URL_PAIR_SET)
    suspend fun getTradeSets(): HttpRequestResultDataList<TradeSet?>?

    @GET(UrlConfig.Config.URL_HOT_PAIRS)
    suspend fun getHotPair(): HttpRequestResultDataList<String?>?

    @GET(UrlConfig.Config.URL_PAIRS_DEEPS)
    suspend fun getTradePairInfo(@Query("pair") pair: String?): HttpRequestResultDataList<PairStatus?>?

    @FormUrlEncoded
    @POST(UrlConfig.Config.URL_PAIR_COLLECT)
    suspend fun pairCollect(@Field("pair") pair: String?): HttpRequestResultString?

    @FormUrlEncoded
    @POST(UrlConfig.Config.URL_PAIR_COLLECT_CANCEL)
    suspend fun pairCollectCancel(@Field("pair") pair: String?): HttpRequestResultString?

    @GET(UrlConfig.Config.URL_SEARCH_COLLECT_PAIRS)
    suspend fun getCollectPairs(): HttpRequestResultDataList<String?>?

    @GET(UrlConfig.Config.URL_COIN_ORDERS_LIST)
    suspend fun getCoinOrders(): HttpRequestResultData<CoinOrder?>?
}