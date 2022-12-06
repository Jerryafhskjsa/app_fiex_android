package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*
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
        if (context == null || callback == null) {
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
        callback: Callback<HttpRequestResultBean<MarkPriceBean?>?>?
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
     * 获取订单列表
     */
    fun getOrderList(
        page: Int,
        size: Int,
        state: String,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<OrderBean>>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getOrderList(page, size, state)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    /**
     * 获取实时成交
     */
    fun getDealList(
        symbol: String,
        num: Int,
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<DealBean>?>?>?
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
     */
    fun getPositionList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getPositionList()
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
     * 获取用户阶梯费率
     */
    fun getUserStepRate(
        context: Context?, isShowLoading: Boolean,
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
     * 买卖方向：BUY;SELL
     * 订单类型：LIMIT；MARKET
     * 数量（张）
     * 有效方式：GTC;IOC;FOK;GTX
     * 仓位方向：LONG;SHORT
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
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.orderCreate(orderSide, symbol, price, timeInForce, orderType, positionSide, origQty)
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
        autoMargin:Boolean?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<String>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.autoMargin(symbol,positionSide,autoMargin)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
}