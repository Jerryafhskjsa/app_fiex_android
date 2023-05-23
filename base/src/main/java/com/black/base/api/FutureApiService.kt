package com.black.base.api


import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.PagingData
import com.black.base.model.clutter.Kline
import com.black.base.model.future.*
import com.black.base.model.socket.PairDeal
import com.black.base.model.socket.PairQuotation
import com.black.base.model.wallet.CostBill
import com.black.base.model.wallet.FlowBill

import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*


interface FutureApiService {
    /**
     * 获取K线
     */
    @GET(UrlConfig.Future.URL_KLINE)
    fun getHistoryKline(@Query("symbol") symbol: String?,
                        @Query("interval") interval: String?,
                        @Query("limit") limit: Int?): Observable<HttpRequestResultDataList<Kline?>?>?

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
    fun getSymbolMarkPrice(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<MarkPriceBean?>?>?

    /**
     * 获取单个交易对指数价格
     */
    @GET(UrlConfig.Future.URL_SYMBOL_INDEX_PRICE)
    fun getSymbolIndexPrice(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<IndexPriceBean?>?>?

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
            Observable<HttpRequestResultBean<ArrayList<PairDeal>?>?>?

    /**
     * 获取行情数据
     */
    @GET(UrlConfig.Future.URL_DEAL_LIST)
    fun getAggTicker(@Query("symbol") symbol: String?):
            Observable<HttpRequestResultBean<PairQuotation?>?>?

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
    @POST(UrlConfig.Future.URL_OPEN_ACCOUNT)
    fun openAccount(): Observable<HttpRequestResultBean<String?>?>?

    /**
     * 获取用户持仓
     */
    @GET(UrlConfig.Future.URL_POSITION_LIST)
    fun getPositionList(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<ArrayList<PositionBean?>?>?>?

    /**
     * 获取止盈止损列表
     */
    @GET(UrlConfig.Future.URL_PROFIT_LIST)
    fun getProfitList(
        @Query("symbol") symbol: String?,
        @Query("state") state: String?
    ): Observable<HttpRequestResultBean<PagingData<ProfitsBean?>?>?>?

    /**
     * 获取计划委托列表
     */
    @GET(UrlConfig.Future.URL_PLAN_LIST)
    fun getPlanList(
        @Query("symbol") symbol: String?,
        @Query("state") state: String?
    ): Observable<HttpRequestResultBean<PagingData<PlansBean?>?>?>?

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
     * 获取所有交易对杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_LEVERAGE_BRACKET_LIST)
    fun getLeverageBracketList(): Observable<HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?>?

    /**
     * 获取listenKey、时间有效为8小时
     */
    @GET(UrlConfig.Future.URL_LISTEN_KEY)
    fun getListenKey(): Observable<HttpRequestResultBean<String>?>?


    /**
     * 获取单个交易对杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_LEVERAGE_BRACKET_DETAIL)
    fun getLeverageBracketDetail(@Query("symbol") symbol: String?): Observable<HttpRequestResultBean<LeverageBracketBean?>?>?

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
     * 获取用户限价委托
     */
    @GET(UrlConfig.Future.ULR_ORDER_LIST)
    fun getOrderList(
        @Query("symbol") symbol: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("state") state: String?,
    ): Observable<HttpRequestResultBean<OrderBean>>

    /**
     * 获取历史订单
     */
    @GET(UrlConfig.Future.URL_LIST_HISTORY)
    fun getListHistory(
        @Query("symbol") symbol: String?,
        @Query("forceClose") forceClose: Boolean?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): Observable<HttpRequestResultBean<OrderBean>>

    /**
     * 获取资金费用
     */
    @GET(UrlConfig.Future.URL_FUNDING_RATE_LIST)
    fun getFoundingRateList(
        @Query("symbol") symbol: String?,
        @Query("forceClose") forceClose: Boolean?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): Observable<HttpRequestResultBean<PagingData<FlowBill?>?>?>

    /**
     * 获取资金流水
     */
    @GET(UrlConfig.Future.URL_BALANCE_BILLS)
    fun getBalancesBills(
        @Query("coinType") coinType: String?,
        @Query("direction") direction: String?,
        @Query("limit") limit: Int?,
        @Query("type") type: String?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): Observable<HttpRequestResultBean<PagingData<CostBill?>?>?>


    /**
     * 下单接口
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE_PLAN)
    fun planOrderCreate(
        @Field("orderSide") orderSide: String?,
        @Field("symbol") symbol: String?,
        @Field("price") price: Double?,
        @Field("timeInForce") timeInForce: String?,
        @Field("orderType") orderType: String?,
        @Field("positionSide") positionSide: String?,
        @Field("origQty") origQty: Int?,
        @Field("triggerProfitPrice") triggerProfitPrice: Number?,
        @Field("triggerStopPrice") triggerStopPrice: Number?,
        @Field("stopPrice") stopPrice: Double?,
        @Field("triggerPriceType") triggerPriceType: String?,
        @Field("entrustType") entrustType: String?,
    ): Observable<HttpRequestResultBean<String>?>?

    /**
     * 创建止盈止损
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE_PROFIT)
    fun profitOrderCreate(
        @Field("symbol") symbol: String?,
        @Field("origQty") origQty: Int?,
        @Field("positionSide") positionSide: String?,
        @Field("triggerProfitPrice") triggerProfitPrice: Number?,
        @Field("triggerStopPrice") triggerStopPrice: Number?,
        @Field("triggerPriceType") triggerPriceType: String?,
    ): Observable<HttpRequestResultBean<String>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE)
    fun orderCreate(
        @Field("orderSide") orderSide: String?,
        @Field("symbol") symbol: String?,
        @Field("price") price: Double?,
        @Field("timeInForce") timeInForce: String?,
        @Field("orderType") orderType: String?,
        @Field("positionSide") positionSide: String?,
        @Field("origQty") origQty: Int?,
        @Field("triggerProfitPrice") triggerProfitPrice: Number?,
        @Field("triggerStopPrice") triggerStopPrice: Number?,
        @Field("reduceOnly") reduceOnly: Boolean?,
    ): Observable<HttpRequestResultBean<String>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE)
    fun orderCreate2(
        @Field("orderSide") orderSide: String?,
        @Field("symbol") symbol: String?,
        @Field("timeInForce") timeInForce: String?,
        @Field("orderType") orderType: String?,
        @Field("positionSide") positionSide: String?,
        @Field("origQty") origQty: Int?,
        @Field("sourceType") sourceType: String?,
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

    /**
     * 调整杠杆倍数
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_ADJUST_LEVERAGE)
    fun adjustLeverage(
        @Field("symbol") symbol: String?,
        @Field("positionSide") positionSide: String?,
        @Field("leverage") leverage: Int?,
    ): Observable<HttpRequestResultBean<String>?>?

    /**
     * 调整杠杆方向
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CHANGE_TYPE)
    fun changeType(
        @Field("symbol") symbol: String?,
        @Field("positionSide") positionSide: String?,
        @Field("positionType") positionType: String?,
    ): Observable<HttpRequestResultBean<String>?>?

    /**
     * 一键全部平仓
     */
    @POST(UrlConfig.Future.URL_CLOSE_ALL)
    fun closeAll(): Observable<HttpRequestResultBean<String>?>?

    /**
     * 撤销所有限价委托和市价委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL)
    fun cancelAll(@Field("symbol") symbol: String?): Observable<HttpRequestResultBean<String>?>?

    /**
     * 撤销所有止盈止损
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL_PROFIT_STOP)
    fun cancelAllProfitStop(@Field("symbol") symbol: String?): Observable<HttpRequestResultBean<String>?>?

    /**
     * 根据id撤销止盈止损
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_PROFIT_STOP_BY_ID)
    fun cancelProfitStopById(@Field("profitId") profitId: String?): Observable<HttpRequestResultBean<String>?>?

    /**
     * 撤销所有计划委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL_PLAN)
    fun cancelALlPlan(@Field("symbol") symbol: String?): Observable<HttpRequestResultBean<String>?>?

    /**
     * 根据id撤销计划委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_PLAN_BY_ID)
    fun cancelPlanById(@Field("entrustId") profitId: String?): Observable<HttpRequestResultBean<String>?>?

    /**
     * 根据id撤销限价委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_ORDER_CANCEL)
    fun cancelOrderId(@Field("orderId") orderId: String?): Observable<HttpRequestResultBean<String>?>?

}