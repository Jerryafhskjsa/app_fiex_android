package com.black.base.api


import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.future.*

import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.POST
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
     * 获取交易对
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
    @POST(UrlConfig.Future.URL_LOGIN)
    fun login(): Observable<HttpRequestResultBean<String?>?>?

    /**
     * 开通合约
     */
    @GET(UrlConfig.Future.URL_OPEN_ACCOUNT)
    fun openAccount(): Observable<HttpRequestResultBean<String?>?>?

    /**
     * 获取用户持仓
     */
    @GET(UrlConfig.Future.URL_POSITION_LIST)
    fun getPositionList(): Observable<HttpRequestResultBean<ArrayList<PositionBean?>?>?>?

    /**
     * 获取行情
     */
    @GET(UrlConfig.Future.URL_TICKERS)
    fun getTickers(): Observable<HttpRequestResultBean<List<TickerBean?>?>?>?


    /**
     * 获取adl信息
     */
    @GET(UrlConfig.Future.URL_POSITION_ADL)
    fun getPositionAdl(): Observable<HttpRequestResultBean<ArrayList<ADLBean?>?>?>?

    /**
     * 获取杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_leverage_bracket_LIST)
    fun getLeverageBracketList(): Observable<HttpRequestResultBean<ArrayList<LeverageBracketResp?>?>?>?

    /**
     * 获取用户单币种资金
     */
    @GET(UrlConfig.Future.ULR_BALANCE_DETAIL)
    fun getBalanceDetail(@Query("coin") coin: String?,
                         @Query("underlyingType") underlyingType: String?)
                        :Observable<HttpRequestResultBean<BalanceDetailBean?>?>?

}