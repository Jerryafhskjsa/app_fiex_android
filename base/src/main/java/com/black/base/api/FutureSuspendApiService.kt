package com.black.base.api


import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.future.*
import com.black.base.model.socket.PairDeal
import com.black.base.model.socket.PairQuotation
import com.black.base.model.wallet.CostBill
import com.black.base.model.wallet.FlowBill

import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*


interface FutureSuspendApiService {

    /**
     * 获取深度
     */
    @GET(UrlConfig.Future.URL_DEPTH)
    suspend fun getDepth(
        @Query("symbol") symbol: String?,
        @Query("level") level: Int?
    ): HttpRequestResultBean<DepthBean?>?

    /**
     * 获取交易对
     */
    @GET(UrlConfig.Future.URL_SYMBOL_LIST)
    suspend fun getSymbolList(): HttpRequestResultBean<ArrayList<SymbolBean>?>?

    /**
     * 获取所有交易对标记价格
     */
    @GET(UrlConfig.Future.URL_MARK_PRICE)
    suspend fun getMarkPrice(): HttpRequestResultBean<ArrayList<MarkPriceBean>?>?

    /**
     * 获取单个交易对标记价格
     */
    @GET(UrlConfig.Future.URL_SYMBOL_MARK_PRICE)
    suspend fun getSymbolMarkPrice(@Query("symbol") symbol: String?): HttpRequestResultBean<MarkPriceBean?>?

    /**
     * 获取单个交易对指数价格
     */
    @GET(UrlConfig.Future.URL_SYMBOL_INDEX_PRICE)
    suspend fun getSymbolIndexPrice(@Query("symbol") symbol: String?): HttpRequestResultBean<IndexPriceBean?>?

    /**
     * 获取资金费率
     */
    @GET(UrlConfig.Future.ULR_FUNDING_RATE)
    suspend fun getFundingRate(@Query("symbol") symbol: String?): HttpRequestResultBean<FundingRateBean?>?

    /**
     * 获取实时成交
     */
    @GET(UrlConfig.Future.URL_DEAL_LIST)
    suspend fun getDealList(@Query("symbol") symbol: String?, @Query("num") num: Int?):
            HttpRequestResultBean<ArrayList<PairDeal>?>?

    /**
     * 获取行情数据
     */
    @GET(UrlConfig.Future.URL_DEAL_LIST)
    suspend fun getAggTicker(@Query("symbol") symbol: String?):
            HttpRequestResultBean<PairQuotation?>?

    /**
     * 获取币种列表
     */
    @GET(UrlConfig.Future.URL_COIN_LIST)
    suspend fun getCoinList(): HttpRequestResultBean<ArrayList<String>?>?

    /**
     * 获取用户账户信息
     */
    @GET(UrlConfig.Future.URL_ACCOUNT_INFO)
    suspend fun getAccountInfo(): HttpRequestResultBean<AccountInfoBean?>?


    /**
     * 用户登录
     */
    @POST(UrlConfig.Future.URL_LOGIN)
    suspend fun login(): HttpRequestResultBean<String?>?

    /**
     * 开通合约
     */
    @POST(UrlConfig.Future.URL_OPEN_ACCOUNT)
    suspend fun openAccount(): HttpRequestResultBean<String?>?

    /**
     * 获取用户持仓
     */
    @GET(UrlConfig.Future.URL_POSITION_LIST)
    suspend fun getPositionList(@Query("symbol") symbol: String?): HttpRequestResultBean<ArrayList<PositionBean?>?>?

    /**
     * 获取止盈止损列表
     */
    @GET(UrlConfig.Future.URL_PROFIT_LIST)
    suspend fun getProfitList(
        @Query("symbol") symbol: String?,
        @Query("state") state: String?
    ): HttpRequestResultBean<PagingData<ProfitsBean?>?>?

    /**
     * 获取计划委托列表
     */
    @GET(UrlConfig.Future.URL_PLAN_LIST)
    suspend fun getPlanList(
        @Query("symbol") symbol: String?,
        @Query("state") state: String?
    ): HttpRequestResultBean<PagingData<PlansBean?>?>?

    /**
     * 获取行情
     */
    @GET(UrlConfig.Future.URL_TICKERS)
    suspend fun getTickers(): HttpRequestResultBean<List<TickerBean?>?>?

    /**
     * 获取指定交易对行情
     */
    @GET(UrlConfig.Future.URL_SYMBOL_TICKER)
    suspend fun getSymbolTickers(@Query("symbol") symbol: String?): HttpRequestResultBean<TickerBean?>?

    /**
     * 获取adl信息
     */
    @GET(UrlConfig.Future.URL_POSITION_ADL)
    suspend fun getPositionAdl(): HttpRequestResultBean<ArrayList<ADLBean?>?>?

    /**
     * 获取所有交易对杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_LEVERAGE_BRACKET_LIST)
    suspend fun getLeverageBracketList(): HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?

    /**
     * 获取listenKey、时间有效为8小时
     */
    @GET(UrlConfig.Future.URL_LISTEN_KEY)
    suspend fun getListenKey(): HttpRequestResultBean<String>?


    /**
     * 获取单个交易对杠杆分层信息
     */
    @GET(UrlConfig.Future.URL_LEVERAGE_BRACKET_DETAIL)
    suspend fun getLeverageBracketDetail(@Query("symbol") symbol: String?): HttpRequestResultBean<LeverageBracketBean?>?


    @GET(UrlConfig.Future.ULR_BALANCE_DETAIL)
    suspend fun getBalanceDetailSuspend(
        @Query("coin") coin: String?,
        @Query("underlyingType") underlyingType: String?
    )
            : HttpRequestResultBean<BalanceDetailBean?>?

    /**
     * 获取资产列表
     */
    @GET(UrlConfig.Future.ULR_BALANCE_LIST)
    suspend fun getBalanceList(): HttpRequestResultBean<ArrayList<BalanceDetailBean>?>?


    /**
     * 获取用户资金费率
     */
    @GET(UrlConfig.Future.ULR_USER_STEP_RATE)
    suspend fun getUserStepRate(): HttpRequestResultBean<UserStepRate>?


    /**
     * 获取用户限价委托
     */
    @GET(UrlConfig.Future.ULR_ORDER_LIST)
    suspend fun getOrderList(
        @Query("symbol") symbol: String?,
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("state") state: String?,
    ): HttpRequestResultBean<OrderBean>?

    /**
     * 获取历史订单
     */
    @GET(UrlConfig.Future.URL_LIST_HISTORY)
    suspend fun getListHistory(
        @Query("symbol") symbol: String?,
        @Query("forceClose") forceClose: Boolean?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): HttpRequestResultBean<OrderBean>?

    /**
     * 获取资金费用
     */
    @GET(UrlConfig.Future.URL_FUNDING_RATE_LIST)
    suspend fun getFoundingRateList(
        @Query("symbol") symbol: String?,
        @Query("forceClose") forceClose: Boolean?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): HttpRequestResultBean<PagingData<FlowBill?>>?

    /**
     * 获取资金流水
     */
    @GET(UrlConfig.Future.URL_BALANCE_BILLS)
    suspend fun getBalancesBills(
        @Query("coinType") coinType: String?,
        @Query("direction") direction: String?,
        @Query("limit") limit: Int?,
        @Query("type") type: String?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
    ): HttpRequestResultBean<PagingData<CostBill?>?>?


    /**
     * 下单接口
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.ULR_ORDER_CREATE)
    suspend fun orderCreate(
        @Field("orderSide") orderSide: String?,
        @Field("symbol") symbol: String?,
        @Field("price") price: Double?,
        @Field("timeInForce") timeInForce: String?,
        @Field("orderType") orderType: String?,
        @Field("positionSide") positionSide: String?,
        @Field("origQty") origQty: Int?,
        @Field("triggerProfitPrice") triggerProfitPrice: Number?,
        @Field("triggerStopPrice") triggerStopPrice: Number?,
        @Field("reduceOnly") reduceOnly: Boolean?
    ): HttpRequestResultBean<String>?

    /**
     * 修改自动追加保证金
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_AUTO_MARGIN)
    suspend fun autoMargin(
        @Field("symbol") symbol: String?,
        @Field("positionSide") positionSide: String?,
        @Field("autoMargin") autoMargin: Boolean?,
    ): HttpRequestResultBean<String>?

    /**
     * 调整杠杆倍数
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_ADJUST_LEVERAGE)
    suspend fun adjustLeverage(
        @Field("symbol") symbol: String?,
        @Field("positionSide") positionSide: String?,
        @Field("leverage") leverage: Int?,
    ): HttpRequestResultBean<String>?

    /**
     * 一键全部平仓
     */
    @POST(UrlConfig.Future.URL_CLOSE_ALL)
    suspend fun closeAll(): HttpRequestResultBean<String>?

    /**
     * 撤销所有限价委托和市价委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL)
    suspend fun cancelAll(@Field("symbol") symbol: String?): HttpRequestResultBean<String>?

    /**
     * 撤销所有止盈止损
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL_PROFIT_STOP)
    suspend fun cancelAllProfitStop(@Field("symbol") symbol: String?): HttpRequestResultBean<String>?

    /**
     * 根据id撤销止盈止损
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_PROFIT_STOP_BY_ID)
    suspend fun cancelProfitStopById(@Field("profitId") profitId: String?): HttpRequestResultBean<String>?

    /**
     * 撤销所有计划委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_ALL_PLAN)
    suspend fun cancelALlPlan(@Field("symbol") symbol: String?): HttpRequestResultBean<String>?

    /**
     * 根据id撤销计划委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_CANCEL_PLAN_BY_ID)
    suspend fun cancelPlanById(@Field("entrustId") profitId: String?): HttpRequestResultBean<String>?

    /**
     * 根据id撤销限价委托
     */
    @FormUrlEncoded
    @POST(UrlConfig.Future.URL_ORDER_CANCEL)
    suspend fun cancelOrderId(@Field("orderId") orderId: String?): HttpRequestResultBean<String>?

}