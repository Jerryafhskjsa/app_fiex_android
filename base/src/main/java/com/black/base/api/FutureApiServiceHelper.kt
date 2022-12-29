package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.future.*
import com.black.base.model.socket.PairDeal
import com.black.base.model.socket.PairQuotation
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.util.Callback

object FutureApiServiceHelper {

    /**
     * 获取深度列表
     */
    fun getDepthData(
        context: Context?,
        symbol: String?,
        level: Int,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<DepthBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getDepth(symbol, level)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取交易对列表
     */
    fun getSymbolList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<SymbolBean>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getSymbolList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取标记价格
     */
    fun getMarkPrice(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getMarkPrice()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取单个交易对标记价格
     */
    fun getSymbolMarkPrice(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<MarkPriceBean?>?>?
    ) {
        if (context == null || callback == null || symbol==null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getSymbolMarkPrice(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取单个交易对标记价格
     */
    fun getSymbolIndexPrice(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<IndexPriceBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getSymbolIndexPrice(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取资金费率
     */
    fun getFundingRate(
        symbol: String?,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<FundingRateBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getFundingRate(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取u本位限价委托订单列表
     */
    fun getOrderList(
        page: Int,
        size: Int,
        symbol: String?,
        state: String?,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getOrderList(symbol, page, size, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    /**
     * 获取币本位限价委托订单列表
     */
    fun getCoinOrderList(
        page: Int,
        size: Int,
        symbol: String?,
        state: String?,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getOrderList(symbol, page, size, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取U本位历史订单列表
     */
    fun getHistoryList(
        symbol: String?,
        direction: String?,
        limit: Int?,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getListHistory(symbol, direction , limit )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取币本位历史订单列表
     */

    fun getCoinHistoryList(
        symbol: String?,
        direction: String?,
        limit: Int?,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getListHistory(symbol, direction , limit )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取u本位的资金费用
     */

    fun getFoundingRateList(
        symbol: String?,
        direction: String?,
        limit: Int?,
        context: Context?,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getFoundingRateList(symbol, direction , limit )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, callback))
    }

    /**
     * 获取币本位的资金费用
     */

    fun getCoinFoundingRateList(
        direction: String?,
        limit: Int?,
        context: Context?,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getFoundingRateList(null, direction , limit )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, callback))
    }

    /**
     * 获取资金流水
     */

    fun getBalanceBills(
        coin: String?,
        direction: String?,
        limit: Int?,
        type: String?,
        context: Context?,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getBalancesBills(coin, direction , limit ,type )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, callback))
    }

    /**
     * 获取合约交易对聚合行情数据
     */
    fun getAggTicker(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<PairQuotation?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getAggTicker(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取实时成交
     */
    fun getDealList(
        context: Context?,
        symbol: String?,
        num: Int,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<PairDeal>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getDealList(symbol, num)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取合约币种列表
     */
    fun getCoinList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<String>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getCoinList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取用户账户信息
     */
    fun getAccountInfo(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<AccountInfoBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getAccountInfo()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 用户登录
     */
    fun getFutureToken(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.login()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 用户开通合约
     */
    fun openAccount(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.openAccount()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取用户持仓
     * symbol=null就是获取全部
     */
    fun getPositionList(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getPositionList(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取止盈止损列表
     * state=null获取全部
     */
    fun getProfitList(
        context: Context?,
        symbol: String?,
        state: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<PagingData<ProfitsBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getProfitList(symbol, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取U计划委托列表
     * state=null获取全部
     */
    fun getPlanList(
        context: Context?,
        symbol: String?,
        state: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<PagingData<PlansBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getPlanList(symbol, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    /**
     * 获取币计划委托列表
     * state=null获取全部
     */
    fun getCoinPlanList(
        context: Context?,
        symbol: String?,
        state: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<PagingData<PlansBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_D)
            .getService(FutureApiService::class.java)
            ?.getPlanList(symbol, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    /**
     * 获取行情列表
     */
    fun getTickers(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<List<TickerBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getTickers()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取单个交易对行情列表
     */
    fun getSymbolTickers(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<TickerBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getSymbolTickers(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取交易对杠杆分层
     */
    fun getLeverageBracketList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getLeverageBracketList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取交易对杠杆分层
     */
    fun getLeverageBracketDetail(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<LeverageBracketBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getLeverageBracketDetail(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取用户单币种资金
     */
    fun getBalanceDetail(
        context: Context?,
        coin: String,
        underlyingType: String,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<BalanceDetailBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getBalanceDetail(coin, underlyingType)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取adl信息
     */
    fun getPositionAdl(
        context: Context?, isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<ADLBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getPositionAdl()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 订阅合约user相关接口
     */
    fun getListenKey(
        context: Context?, isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getListenKey()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    /**
     * 获取用户阶梯费率
     */
    fun getUserStepRate(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<UserStepRate>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getUserStepRate()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    /**
     * 获取用户资产列表
     */
    fun getBalanceList(
        context: Context?, isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<BalanceDetailBean>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getBalanceList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    /**
     * 下单接口
     * 买卖方向->orderSide:BUY;SELL
     * 订单类型->orderType:LIMIT；MARKET
     * 数量（张）->origQty
     * 只减仓->reduceOnly (true,false)
     * 有效方式->timeInForce:GTC;IOC;FOK;GTX
     * 仓位方向：LONG;SHORT
     * 止盈价->triggerProfitPrice(number)
     * 止损价->triggerStopPrice(number)
     * 仓位方向->positionSide:LONG,SHORT
     *
     */
    fun createOrder(
        context: Context?,
        orderSide: String,
        orderType: String,
        symbol: String?,
        positionSide: String?,
        price: Double?,
        timeInForce: String?,
        origQty: Int,
        triggerProfitPrice: Number?,
        triggerStopPrice: Number?,
        reduceOnly: Boolean?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.orderCreate(
                orderSide,
                symbol,
                price,
                timeInForce,
                orderType,
                positionSide,
                origQty,
                triggerProfitPrice,
                triggerStopPrice,
                reduceOnly
            )
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 修改自动追加保证金
     * 买卖方向：BUY;SELL
     * 订单类型：LIMIT；MARKET
     * 有效方式：GTC;IOC;FOK;GTX
     * 仓位方向：LONG;SHORT
     */
    fun autoMargin(
        context: Context?,
        symbol: String?,
        positionSide: String?,
        autoMargin: Boolean?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.autoMargin(symbol, positionSide, autoMargin)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 调整杠杆倍数
     * 仓位方向：LONG(全仓);SHORT(逐仓)
     */
    fun adjustLeverage(
        context: Context?,
        symbol: String?,
        positionSide: String?,
        leverage: Int?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.adjustLeverage(symbol, positionSide, leverage)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 一键全部平仓
     */
    fun closeAll(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.closeAll()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 撤销所有限价委托和市价委托
     */
    fun closeAll(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.cancelAll(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 撤销所有止盈止损
     */
    fun cancelAllProfitStop(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.cancelAllProfitStop(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 根据id撤销止盈止损
     */
    fun cancelProfitStopById(
        context: Context?,
        profitId: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.cancelProfitStopById(profitId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 撤销所有计划委托
     */
    fun cancelALlPlan(
        context: Context?,
        symbol: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.cancelALlPlan(symbol)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 根据id撤销计划委托
     */
    fun cancelPlanById(
        context: Context?,
        entrustId: String?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.cancelPlanById(entrustId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))

    }
}