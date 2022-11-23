package com.black.base.api


import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.future.*

import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface FutureApiService {

    /**
     * 获取深度
     */
    @GET(UrlConfig.Future.URL_DEPTH)
    fun getDepth(
        @Query("symbol") symbol: String?,
        @Query("level") level: Int?
    ): Observable<HttpRequestResultBean<DepthBean?>?>?

    /**
     * 获取深度
     */
    @GET(UrlConfig.Future.URL_SYMBOL_LIST)
    fun getSymbolList(): Observable<HttpRequestResultBean<ArrayList<SymbolBean>?>?>?

    /**
     * 获取标记价格
     */
    @GET(UrlConfig.Future.URL_MARK_PRICE)
    fun getMarkPrice(): Observable<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>?

    /**
     * 获取资金费率
     */
    @GET(UrlConfig.Future.ULR_FUNDING_RATE)
    fun getFundingRate(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<FundingRateBean?>?>?

    /**
     * 获取实时成交
     */
    @GET(UrlConfig.Future.URL_DEAL_LIST)
    fun getDealList(@Query("symbol") symbol: String?, @Query("num") num: Int?):
            Observable<HttpRequestResultBean<ArrayList<DealBean>?>?>?

    /**
     * 获取币种列表
     */
    @GET(UrlConfig.Future.URL_COIN_LIST)
    fun getCoinList(): Observable<HttpRequestResultBean<ArrayList<String>?>?>?

    /**
     * 获取用户账户信息
     */
    @GET(UrlConfig.Future.URL_ACCOUNT_INFO)
    fun getAccountInfo(): Observable<HttpRequestResultBean<AccountInfoBean?>?>?


    /**
     * 用户登录
     */
    @GET(UrlConfig.Future.URL_LOGIN)
    fun login(): Observable<HttpRequestResultBean<String?>?>?
}