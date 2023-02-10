package com.black.base.api

import android.content.Context
import android.util.SparseArray
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.c2c.*
import com.black.base.model.clutter.CoinUsdtPrice
import com.black.base.model.user.PaymentMethod
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.CookieUtil
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.functions.Function
import java.math.BigDecimal

object C2CApiServiceHelper {
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }
    private const val DATA_CACHE_OVER_TIME = 20 * 60 * 1000 //20分钟
        .toLong()
    private const val C2C_PRICE = 1
    private const val TRADE_SET = 2
    private const val TRADE_PAIR = 3

    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long?>()
    private var c2CPrice: C2CPrice? = null

    var coinUsdtPrice: CoinUsdtPrice? = null

    private fun getLastGetTime(type: Int): Long? {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }

    fun getC2CPrice(context: Context?, callback: Callback<C2CPrice?>?) {
        getC2CPrice(context, false, callback)
    }

    fun getC2CPrice(context: Context?): Observable<C2CPrice?>? {
        if (context == null) {
            return Observable.empty()
        }
        if (c2CPrice == null) {
            c2CPrice = C2CPrice()
            val cell = CookieUtil.getC2CCellPrice(context)
            val buy = CookieUtil.getC2CBuyPrice(context)
            if (cell != 0f) {
                c2CPrice!!.sell = java.lang.Double.valueOf(cell.toDouble())
            } else {
                c2CPrice!!.sell = 6.5
            }
            if (buy != 0f) {
                c2CPrice!!.buy = java.lang.Double.valueOf(buy.toDouble())
            } else {
                c2CPrice!!.buy = 6.5
            }
        }
        val lastGetTime = getLastGetTime(C2C_PRICE)
        return if (lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) { //通过接口获取
            ApiManager.build(context, false, UrlConfig.ApiType.URL_PRO)
                .getService(CommonApiService::class.java)
                ?.getUsdtCnyPrice()
                ?.flatMap(Function<HttpRequestResultData<CoinUsdtPrice?>?, ObservableSource<C2CPrice?>> { returnData: HttpRequestResultData<CoinUsdtPrice?>? ->
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                        coinUsdtPrice = returnData.data
                        val price = CommonUtil.parseDouble(coinUsdtPrice!!.usdt)
                        if (price != null) {
                            if (c2CPrice == null) {
                                c2CPrice = C2CPrice()
                            }
                            c2CPrice!!.buy = price
                            c2CPrice!!.sell = price
                            CookieUtil.setC2CCellPrice(context, c2CPrice!!.sell!!.toFloat())
                            CookieUtil.setC2CBuyPrice(context, c2CPrice!!.buy!!.toFloat())
                            setLastGetTime(C2C_PRICE, System.currentTimeMillis())
                        }
                    }
                    Observable.just(c2CPrice)
                })
        } else {
            Observable.just(c2CPrice)
        }
    }

    fun getC2CPrice(context: Context?, forceUpdate: Boolean, callback: Callback<C2CPrice?>?) {
        if (context == null || callback == null) {
            return
        }
        if (c2CPrice == null) {
            c2CPrice = C2CPrice()
            val cell = CookieUtil.getC2CCellPrice(context)
            val buy = CookieUtil.getC2CBuyPrice(context)
            if (cell != null && cell != 0f) {
                c2CPrice!!.sell = java.lang.Double.valueOf(cell.toDouble())
            } else {
                c2CPrice!!.sell = 6.5
            }
            if (buy != null && buy != 0f) {
                c2CPrice!!.buy = java.lang.Double.valueOf(buy.toDouble())
            } else {
                c2CPrice!!.buy = 6.5
            }
        }
        callback.callback(c2CPrice)
        val lastGetTime = getLastGetTime(C2C_PRICE)
        if (lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            //通过接口获取
            CommonApiServiceHelper.getUsdtCnyPrice(
                context,
                object : Callback<HttpRequestResultData<CoinUsdtPrice?>?>() {
                    override fun error(type: Int, error: Any) {
                        callback.callback(c2CPrice)
                    }

                    override fun callback(returnData: HttpRequestResultData<CoinUsdtPrice?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                            coinUsdtPrice = returnData.data
                            val price = CommonUtil.parseDouble(coinUsdtPrice!!.usdt)
                            if (price != null) {
                                if (c2CPrice == null) {
                                    c2CPrice = C2CPrice()
                                }
                                c2CPrice!!.buy = price
                                c2CPrice!!.sell = price
                                CookieUtil.setC2CCellPrice(context, c2CPrice!!.sell!!.toFloat())
                                CookieUtil.setC2CBuyPrice(context, c2CPrice!!.buy!!.toFloat())
                                setLastGetTime(C2C_PRICE, System.currentTimeMillis())
                            }
                        }
                        callback.callback(c2CPrice)
                    }
                })
            //            ApiManager.build(context).getService(C2CApiService.class)
//                    .getC2CPrice("BID", 1, 10)
//                    .subscribeOn(Schedulers.io())
//                    .map(new Function<HttpRequestResultData<PagingData<C2CSeller>>, C2CPrice>() {
//                        @Override
//                        public C2CPrice apply(HttpRequestResultData<PagingData<C2CSeller>> returnData) throws Exception {
//                            //计算商户平均价格
//                            C2CPrice c2CPrice = null;
//                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS
//                                    && returnData.data.list != null && !returnData.data.list.isEmpty()) {
//                                Double totalPrice = 0d;
//                                for (C2CSeller c2CSeller : returnData.data.list) {
//                                    totalPrice += c2CSeller.price;
//                                }
//                                Double averagePrice = totalPrice / returnData.data.list.size();
//                                if (averagePrice > 0) {
//                                    c2CPrice = new C2CPrice();
//                                    c2CPrice.buy = averagePrice;
//                                    c2CPrice.sell = averagePrice;
//                                }
//                                c2CPrice = new C2CPrice();
//                                c2CPrice.buy =
//                                        returnData.data.list.get(0).price;
//                                c2CPrice.sell =
//                                        returnData.data.list.get(0).price;
//
//                                setLastGetTime(C2C_PRICE, System.currentTimeMillis());
//                            }
//                            return c2CPrice;
//                        }
//                    }).observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new ApiObserverCallback<C2CPrice>() {
//
//                        @Override
//                        public void onSuccess(C2CPrice o) {
//                            c2CPrice = o != null ? o : c2CPrice;
//                            callback.callback(c2CPrice);
//                        }
//
//                        @Override
//                        public void onFailure(Throwable t) {
//                            callback.callback(c2CPrice);
//                        }
//                    });
        }
    }

    fun getC2CSellerList(
        context: Context?,
        isShowLoading: Boolean,
        coinType: String?,
        type: String?,
        pageNum: Int,
        pageSize: Int,
        callback: Callback<HttpRequestResultData<PagingData<C2CSeller?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getC2CMerchant(coinType, type, pageNum, pageSize)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun createC2COrderBuy(
        context: Context?,
        coinType: String?,
        type: String?,
        amount: String?,
        merchantId: String?,
        isOneKey: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.createOrderBuy(coinType, type, amount, merchantId, isOneKey)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun createC2COrderSell(
        context: Context?,
        coinType: String?,
        type: String?,
        amount: String?,
        merchantId: String?,
        isOneKey: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.createOrderSell(coinType, type, amount, merchantId, isOneKey)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getC2COrderList(
        context: Context?,
        isShowLoading: Boolean,
        type: String?,
        status: String?,
        pageNum: Int,
        pageSize: Int,
        callback: Callback<HttpRequestResultData<PagingData<C2COrder?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getOrderList(type, status, pageNum, pageSize)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getC2COrderDetailList(
        context: Context?,
        isSilent: Boolean,
        orderId: String?,
        lastTime: Long,
        direction: Int,
        pageNum: Int,
        pageSize: Int,
        callback: Callback<HttpRequestResultDataList<C2COrderDetailItem?>?>?
    ) {
        /**
         * orderId
         * lastTime（时间戳long类型） 拉取历史消息传最上面那条时间戳，刷新新消息传最下面那条时间戳
         * direction 滑动类型： 0-拉取历史消息，1-刷新新的消息
         * pageNum  默认1
         * pageSize 默认10
         */
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getOrderDetailList(orderId, lastTime.toString(), direction.toString())
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, !isSilent, callback))
    }

    fun createC2COrderDetail(
        context: Context?,
        orderId: String?,
        message: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.createOrderDetail(orderId, message)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getC2COrderDetail(
        context: Context?,
        orderId: String?,
        callback: Callback<HttpRequestResultData<C2CDetail?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getOrderDetail(orderId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun releaseCoin(
        context: Context?,
        orderId: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.releaseCoin(orderId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun confirmPaid(
        context: Context?,
        orderId: String?,
        payment: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.confirmPaid(orderId, payment)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun cancelOrder(
        context: Context?,
        orderId: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.cancelOrder(orderId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun isAgree(context: Context?, callback: Callback<HttpRequestResultData<C2CAgreement?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.isAgree()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun agree(context: Context?, userId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.agree()
            ?.compose(RxJavaHelper.observeOnMainThread<HttpRequestResultString?>())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getPaymentMethodAll(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<PaymentMethod?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getPaymentMethodAll(null)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getPaymentMethodActive(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<PaymentMethod?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getPaymentMethodAll("1")
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun deletePaymentMethod(
        context: Context?,
        id: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.deletePaymentMethod(id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun addPaymentMethod(
        context: Context?,
        userName: String?,
        type: String?,
        account: String?,
        bankName: String?,
        branchBankName: String?,
        qrcodeUrl: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.addPaymentMethod(userName, type, account, bankName, branchBankName, qrcodeUrl)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun updatePaymentMethod(
        context: Context?,
        id: String?,
        status: Int,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.updatePaymentMethod(id, status)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }


    fun getCoinTypeList(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<C2CSupportCoin?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getCoinTypeList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

//C2C支持的币种
    fun getCoinTypeList2(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<C2CSupportCoin?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getC2CSupportCoin()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getC2CSellerFastList(
        context: Context?,
        isShowLoading: Boolean,
        coinType: String?,
        type: String?,
        pageNum: Int,
        pageSize: Int,
        callback: Callback<HttpRequestResultDataList<C2CSeller?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(C2CApiService::class.java)
            ?.getC2CMerchantFast(coinType, type, pageNum, pageSize)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //新增广告
    fun getC2CADConfig(
        context: Context?,
        coinType: String,
        currencyCoin: String,
        direction: String,
        payMethods: String,
        priceParam: BigDecimal,
        priceType: Int,
        singleLimitMax: BigDecimal,
        singleLimitMin: BigDecimal,
        totalAmount: BigDecimal,
        completedOrders: Int?,
        completion: BigDecimal?,
        registeredDays: Int?,
        remark: String? ,
        soldOutTime: Int?,
        callback: Callback<HttpRequestResultDataList<C2CNewAD?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CCreateAD(coinType, currencyCoin, direction, payMethods, priceParam, priceType, singleLimitMax, singleLimitMin, totalAmount, completedOrders, completion, registeredDays, remark, soldOutTime)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //删除广告
    fun getDeleteAD(
    context: Context?,
    id: String,
    callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CADDelete(id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //指数价格
    fun getC2CIndexPrice(
        context: Context?,
        currencyCoin: String?,
        callback: Callback<HttpRequestResultDataList<C2CIndexPrice?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CIndexPrice(currencyCoin)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    //我的订单
    fun getC2COL(
        context: Context?,
        isShowLoading: Boolean,
        direction: String?,
        status: Int?,
        callback: Callback<HttpRequestResultData<C2CADData<C2CBills?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2COL(direction,status)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    //首页广告
    fun getC2CADList(
        context: Context?,
        isShowLoading: Boolean,
        coinType: String?,
        direction: String?,
        gteAmount:Double?,
        payMethod: String?,
        callback: Callback<HttpRequestResultData<C2CADData<C2CMainAD?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CADList(coinType,direction,gteAmount,payMethod)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    //广告详情
    fun getC2CADID(
        context: Context?,
        id: String?,
        callback: Callback<HttpRequestResultData<C2CMainAD?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CADInfo(id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    //快速下单配置
    fun getC2CQuickOrder(
        context: Context?,
        coinType: String?,
        currencyCoin: String?,
        callback: Callback<HttpRequestResultData<OrderConfig?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CQuickConfig(coinType,currencyCoin)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    //快速下单广告查询
    fun getC2CQuickSearch(
        context: Context?,
        gteAmount: Double?,
        gteCurrencyCoinAmount: Double?,
        coinType: String?,
        direction: String?,
        payMethod: String?,
        callback: Callback<HttpRequestResultData<C2CMainAD?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CQuickPublish(gteAmount, gteCurrencyCoinAmount,coinType, direction, payMethod)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
    //下单
    fun getC2COrder(
        context: Context?,
        advertisingId: String?,
        amount: Double?,
        price: Double?,
        callback: Callback<HttpRequestResultData<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_API).getService(C2CApiService::class.java)
            ?.getC2CCreateV2(advertisingId, amount, price)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
}