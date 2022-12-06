package com.black.base.api


import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*

import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*


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
     * 获取所有交易对标记价格
     */
    @GET(UrlConfig.Future.URL_MARK_PRICE)
    fun getMarkPrice(): Observable<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>?

    /**
     * 获取单个交易对标记价格
     */
    @GET(UrlConfig.Future.URL_SYMBOL_MARK_PRICE)
    fun getSymbolMarkPrice( @Query("symbol") symbol: String?): Observable<HttpRequestResultBean<MarkPriceBean?>?>?

    /**
     * 获取单个交易对指数价格
     */
    @GET(UrlConfig.Future.URL_SYMBOL_INDEX_PRICE)
    fun getSymbolIndexPrice( @Query("symbol") symbol: String?): Observable<HttpRequestResultBean<MarkPriceBean?>?>?

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
     * 获取指定交易对行情
     */
    @GET(UrlConfig.Future.URL_SYMBOL_TICKER)
    fun getSymbolTickers(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<TickerBean?>?>?

    /**
     * 获取adl信息
     */
    @GET(UrlConfig.Future.URL_POSITION_ADL)
    fun getPositionAdl(): Observable<HttpRequestResultBean<ArrayList<ADLBean?>?>?>?

    /**
     * 获取杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_leverage_bracket_LIST)
    fun getLeverageBracketList(): Observable<HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?>?

    /**
     * 获取用户单币种资金
     */
    @GET(UrlConfig.Future.ULR_BALANCE_DETAIL)
    fun getBalanceDetail(
        @Query("coin") coin: String?,
        @Query("underlyingType") underlyingType: String?
    )
            : Observable<HttpRequestResultBean<BalanceDetailBean?>?>?

    /**
     * 获取资产列表
     */
    @GET(UrlConfig.Future.ULR_BALANCE_LIST)
    fun getBalanceList(): Observable<HttpRequestResultBean<ArrayList<BalanceDetailBean>?>?>?


    /**
     * 获取用户资金费率
     */
    @GET(UrlConfig.Future.ULR_USER_STEP_RATE)
    fun getUserStepRate(): Observable<HttpRequestResultBean<UserStepRate>>


    /**
     * 获取用户资金费率
     */
    @GET(UrlConfig.Future.ULR_ORDER_LIST)
    fun getOrderList(
        @Query("page") page: Int?,
        @Query("size") size: Int?, @Query("state") state: String?,
    ): Observable<HttpRequestResultBean<OrderBean>>

    /**
     * 下单接口
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE)
    fun orderCreate(
        @Field("orderSide") orderSide: String?,
        @Field("symbol") symbol: String?,
        @Field("price") price: Double?,
        @Field("timeInForce") timeInForce: String?,
        @Field("orderType") orderType: String?,
        @Field("positionSide") positionSide: String?,
        @Field("origQty") origQty: Int?
    ): Observable<HttpRequestResultBean<String>?>?

    /**
     * 修改自动追加保证金
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_AUTO_MARGIN)
    fun autoMargin(
        @Field("symbol") symbol: String?,
        @Field("positionSide") positionSide: String?,
        @Field("autoMargin") autoMargin: Boolean?,
    ): Observable<HttpRequestResultBean<String>?>?
}