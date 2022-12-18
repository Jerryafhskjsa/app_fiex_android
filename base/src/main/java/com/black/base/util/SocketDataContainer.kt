package com.black.base.util

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import com.black.base.R
import com.black.base.api.*
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.SuccessObserver
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.future.IndexPriceBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.FryingUtil.printError
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object SocketDataContainer {
    private var TAG = SocketDataContainer::class.java.simpleName
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }


    //合约所有u本位交易对
    private val pairFutureUDataList: ArrayList<PairStatus?> = ArrayList()

    //合约所有币本位交易对
    private val pairFutureCoinDataList: ArrayList<PairStatus?> = ArrayList()

    //合约所有交易对
    private val allFuturePairStatusList: ArrayList<PairStatus?> = ArrayList()

    //合约所有交易对Map缓存
    private val allFuturePairStatusMap: MutableMap<String, PairStatus> = HashMap()

    //合约所有交易对分组Map
    private val allFuturePairStatusParentMap: MutableMap<String, List<PairStatus?>> = HashMap()

    //合约所有行情交易对socket更新数据缓存
    private val pairFutureDataSource: MutableMap<String, PairStatusNew> = HashMap()

    //合约自选交易对缓存数据
    private val dearFuturePairMap: MutableMap<String, Boolean?> = HashMap()

    //所有合约交易对信息观察者
    private val pairFutureObservers = ArrayList<Observer<ArrayList<PairStatus?>?>>()

    //所有现货交易对
    private val pairDataList: ArrayList<PairStatus?> = ArrayList()

    //所有现货交易对Map缓存
    private val allPairStatusMap: MutableMap<String, PairStatus> = HashMap()

    //所有现货交易对分组Map
    private val allPairStatusParentMap: MutableMap<String, List<PairStatus?>> = HashMap()

    //所有现货行情交易对socket更新数据缓存
    private val pairDataSource: MutableMap<String, PairStatusNew> = HashMap()


    //现货自选交易对缓存数据
    private val dearPairMap: MutableMap<String, Boolean?> = HashMap()

    //所有现货行情交易对信息观察者
    private val pairObservers = ArrayList<Observer<ArrayList<PairStatus?>?>>()


    //现货委托深度挂单列表
    private val depthDataList = ArrayList<QuotationOrderNew?>()
    private val spotDepthObservers = ArrayList<Observer<Pair<String?, TradeOrderPairList?>>>()

    /***fiex***/
    //合约委托深度挂单列表
    private val futureDepthDataList = ArrayList<QuotationOrderNew?>()
    private val futureDepthObservers = ArrayList<Observer<Pair<String?, TradeOrderPairList?>>>()

    //现货交易对成交
    private val currentPairDealObservers = ArrayList<Observer<PairDeal?>?>()

    //标记价格
    private val markPriceObservers = ArrayList<Observer<MarkPriceBean?>?>()

    //标记价格
    private val indexPriceObservers = ArrayList<Observer<IndexPriceBean?>?>()

    //现货交易对行情
    private val pairQuotationObservers = ArrayList<Observer<PairQuotation?>?>()

    //现货用户余额变更
    private val userBalanceObservers = ArrayList<Observer<UserBalance?>>()

    //现货用户挂单变更
    private val userOrderObservers = ArrayList<Observer<TradeOrderFiex?>>()

    /***fiex***/
    //成交
    const val DEAL_MAX_SIZE = 20
    private val dealList = ArrayList<QuotationDealNew?>()
    private val dealObservers = ArrayList<Observer<Pair<String?, ArrayList<TradeOrder?>?>>>()

    private val kLineObservers = ArrayList<Observer<KLineItemListPair?>>()
    private val kLineAddObservers = ArrayList<Observer<KLineItemPair?>>()
    private val kLineAddMoreObservers = ArrayList<Observer<KLineItemListPair?>>()

    //用户信息有修改
    private val userInfoObservers = ArrayList<Observer<String?>>()

    private var handlerThread: HandlerThread? = null
    private var pairHandler: Handler? = null

    //创建几个线程，让所有操作在这几个线程中进行
    private fun init() {
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        pairHandler = Handler(handlerThread?.looper)
    }


    //添加用户余额观察者
    fun subscribeUserBalanceObservable(observer: Observer<UserBalance?>?) {
        if (observer == null) {
            return
        }
        synchronized(userBalanceObservers) {
            if (!userBalanceObservers.contains(observer)) {
                userBalanceObservers.add(observer)
            }
        }
    }

    //移除用户余额观察者
    fun removeUserBalanceObservable(observer: Observer<UserBalance?>?) {
        if (observer == null) {
            return
        }
        synchronized(userBalanceObservers) { userBalanceObservers.remove(observer) }
    }

    //添加用户Order观察者
    fun subscribeUserOrderObservable(observer: Observer<TradeOrderFiex?>?) {
        if (observer == null) {
            return
        }
        synchronized(userOrderObservers) {
            if (!userOrderObservers.contains(observer)) {
                userOrderObservers.add(observer)
            }
        }
    }

    //移除用户Order观察者
    fun removeUserOrderObservable(observer: Observer<TradeOrderFiex?>?) {
        if (observer == null) {
            return
        }
        synchronized(userOrderObservers) { userOrderObservers.remove(observer) }
    }

    //添加当前交易对deal观察者
    fun subscribePairDealObservable(observer: Observer<PairDeal?>?) {
        if (observer == null) {
            return
        }
        synchronized(currentPairDealObservers) {
            if (!currentPairDealObservers.contains(observer)) {
                currentPairDealObservers.add(observer)
            }
        }
    }


    //移除当前交易对deal观察者
    fun removePairDealObservable(observer: Observer<PairDeal?>?) {
        if (observer == null) {
            return
        }
        synchronized(currentPairDealObservers) { currentPairDealObservers.remove(observer) }
    }

    //添加当前交易对24小时行情观察者
    fun subscribePairQuotationObservable(observer: Observer<PairQuotation?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairQuotationObservers) {
            if (!pairQuotationObservers.contains(observer)) {
                pairQuotationObservers.add(observer)
            }
        }
    }

    //移除当前交易对24小时行情观察者
    fun removePairQuotationObservable(observer: Observer<PairQuotation?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairQuotationObservers) { pairQuotationObservers.remove(observer) }
    }

    //添加交易对观察者
    fun subscribePairObservable(observer: Observer<ArrayList<PairStatus?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairObservers) {
            if (!pairObservers.contains(observer)) {
                pairObservers.add(observer)
            }
        }
    }

    //移除交易对观察者
    fun removePairObservable(observer: Observer<ArrayList<PairStatus?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairObservers) { pairObservers.remove(observer) }
    }

    //添加现货深度委托观察者
    fun subscribeOrderObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(spotDepthObservers) {
            if (!spotDepthObservers.contains(observer)) {
                spotDepthObservers.add(observer)
            }
        }
    }

    //移除现货深度委托观察者
    fun removeOrderObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(spotDepthObservers) { spotDepthObservers.remove(observer) }
    }

    //添加合约深度委托观察者
    fun subscribeFutureDepthObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(futureDepthObservers) {
            if (!futureDepthObservers.contains(observer)) {
                futureDepthObservers.add(observer)
            }
        }
    }

    //移除合约深度委托观察者
    fun removeFutureDepthObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(futureDepthObservers) { futureDepthObservers.remove(observer) }
    }

    //添加成交观察者
    fun subscribeDealObservable(observer: Observer<Pair<String?, ArrayList<TradeOrder?>?>>?) {
        if (observer == null) {
            return
        }
        synchronized(dealObservers) {
            if (!dealObservers.contains(observer)) {
                dealObservers.add(observer)
            }
        }
    }

    //移除成交观察者
    fun removeDealObservable(observer: Observer<Pair<String?, ArrayList<TradeOrder?>?>>?) {
        if (observer == null) {
            return
        }
        synchronized(dealObservers) { dealObservers.remove(observer) }
    }

    //添加K线观察者
    fun subscribeKLineObservable(observer: Observer<KLineItemListPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineObservers) {
            if (!kLineObservers.contains(observer)) {
                kLineObservers.add(observer)
            }
        }
    }

    //移除K线观察者
    fun removeKLineObservable(observer: Observer<KLineItemListPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineObservers) { kLineObservers.remove(observer) }
    }

    //添加K线新增观察者
    fun subscribeKLineAddObservable(observer: Observer<KLineItemPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineAddObservers) {
            if (!kLineAddObservers.contains(observer)) {
                kLineAddObservers.add(observer)
            }
        }
    }

    //移除K线新增观察者
    fun removeKLineAddObservable(observer: Observer<KLineItemPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineAddObservers) { kLineAddObservers.remove(observer) }
    }

    //添加K线加载更多观察者
    fun subscribeKLineAddMoreObservable(observer: Observer<KLineItemListPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineAddMoreObservers) {
            if (!kLineAddMoreObservers.contains(observer)) {
                kLineAddMoreObservers.add(observer)
            }
        }
    }

    //移除K线加载更多观察者
    fun removeKLineAddMoreObservable(observer: Observer<KLineItemListPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineAddMoreObservers) { kLineAddMoreObservers.remove(observer) }
    }

    //添加用户信息观察者
    fun subscribeUserInfoObservable(observer: Observer<String?>?) {
        if (observer == null) {
            return
        }
        synchronized(userInfoObservers) {
            if (!userInfoObservers.contains(observer)) {
                userInfoObservers.add(observer)
            }
        }
    }

    //移除用户信息观察者
    fun removeUserInfoObservable(observer: Observer<String?>?) {
        if (observer == null) {
            return
        }
        synchronized(userInfoObservers) { userInfoObservers.remove(observer) }
    }


    //添加合约交易对行情观察者
    fun subscribeFuturePairObservable(observer: Observer<ArrayList<PairStatus?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairFutureObservers) {
            if (!pairFutureObservers.contains(observer)) {
                pairFutureObservers.add(observer)
            }
        }
    }

    //移除合约交易对行情观察者
    fun removeFuturePairObservable(observer: Observer<ArrayList<PairStatus?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(pairFutureObservers) { pairFutureObservers.remove(observer) }
    }


    /**
     * 计算币价格CNY
     *
     * @return
     */
    fun computeCoinPriceCNY(pairStatus: PairStatus?, c2CPrice: C2CPrice?): Double? {
        if (pairStatus == null || c2CPrice == null) {
            return null
        }
        if ("DC" == pairStatus.setName) {
            return pairStatus.currentPrice
        }
        val usdtPrice = getCoinPairUsdtPrice(pairStatus.currentPrice, pairStatus)
        return usdtPrice?.let { computeUSDTPriceCNY(it, c2CPrice) }
    }

    /**
     * 计算总资产CNY
     *
     * @param totalAmount usdt钱包
     * @param price       C2C价格
     * @return
     */
    fun computeTotalMoneyCNY(totalAmount: Double?, price: C2CPrice?): Double? {
        return if (totalAmount == null || price == null || price.sell == null) null else totalAmount * price.sell!!
    }

    private fun getCoinPairUsdtPrice(sourcePrice: Double?, coinPairStatus: PairStatus?): Double? {
        if (coinPairStatus == null || sourcePrice == null) {
            return null
        }
        return if (TextUtils.equals("USDT", coinPairStatus.setName)) {
            sourcePrice
        } else {
            synchronized(allPairStatusParentMap) {
                val parents = allPairStatusParentMap[coinPairStatus.pair]
                var parent: PairStatus? = null
                if (parents != null) {
                    for (pairStatus in parents) {
                        if (coinPairStatus.setName + "_USDT" == pairStatus?.pair) {
                            parent = pairStatus
                            break
                        }
                    }
                }
                return if (parent != null) {
                    getCoinPairUsdtPrice(coinPairStatus.currentPrice * parent.currentPrice, parent)
                } else {
                    null
                }
            }
        }
    }

    private fun computeTotalMoneyCNY(totalAmount: Number?, price: C2CPrice?): Double? {
        return if (totalAmount == null || price == null || price.sell == null) null else totalAmount.toDouble() * price.sell!!
    }

    /**
     * 折算USDT CNY 价格
     *
     * @param usdtPrice
     * @param price
     * @return
     */
    private fun computeUSDTPriceCNY(usdtPrice: Double?, price: C2CPrice?): Double? {
        return if (price?.sell == null || usdtPrice == null) null else usdtPrice * price.sell!!
    }

    //缓存所有现货交易对数据
    private fun refreshAllPairStatus(allPairStatus: ArrayList<PairStatus?>?) {
        synchronized(allPairStatusParentMap) {
            synchronized(allPairStatusMap!!) {
                if (allPairStatus == null || allPairStatus.isEmpty()) {
                    allPairStatusParentMap.clear()
                    return
                }
                //计算交易对币分组
                for (pairStatus in allPairStatus) {
                    val setName = pairStatus?.setName
                    val parents: MutableList<PairStatus?> = ArrayList()
                    for (parent in allPairStatus) {
                        if (TextUtils.equals(parent?.name, setName)) {
                            parents.add(parent)
                        }
                    }
                    pairStatus?.pair?.let {
                        allPairStatusParentMap[it] = parents
                    }
                }
            }
        }
    }

    //缓存所有合约交易对数据
    private fun refreshAllFuturePairStatus(allPairStatus: ArrayList<PairStatus?>?) {
        Log.d("iiiiii", "refreshAllFuturePairStatus")
        synchronized(allFuturePairStatusParentMap!!) {
            if (allPairStatus == null || allPairStatus.isEmpty()) {
                allFuturePairStatusParentMap.clear()
                return
            }
            //计算交易对币分组
            for (pairStatus in allPairStatus) {
                val setName = pairStatus?.setName
                val parents: MutableList<PairStatus?> = ArrayList()
                for (parent in allPairStatus) {
                    if (TextUtils.equals(parent?.name, setName)) {
                        parents.add(parent)
                    }
                }
                pairStatus?.pair?.let {
                    allFuturePairStatusParentMap[it] = parents
                }
            }
            Log.d(
                "iiiiii",
                "allFuturePairStatusParentMap.size = " + allFuturePairStatusParentMap.size
            )
        }
    }


    /**
     * 计算coinType对应的cny价格,并赋值到交易对数据bean
     *
     * @param context
     */
    private fun computePairStatusCNY(context: Context?) {
        if (context == null) {
            return
        }
        C2CApiServiceHelper.getC2CPrice(context, object : Callback<C2CPrice?>() {
            override fun error(type: Int, error: Any) {
                onGetC2CPriceComplete(null)
            }

            override fun callback(returnData: C2CPrice?) {
//                Log.d(TAG,"computePairStatusCNY->C2CPrice"+returnData?.buy)
                onGetC2CPriceComplete(returnData)
            }

            private fun onGetC2CPriceComplete(price: C2CPrice?) {
                Observable.create<String> { e ->
                    e.onNext(reallyUpdatePairStatusData(price))
                    e.onComplete()
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(object : SuccessObserver<String?>() {
                        override fun onSuccess(value: String?) {
                            synchronized(pairObservers) {
                                for (observer in pairObservers) {
                                    var updateData: ArrayList<PairStatus?> = gson.fromJson(
                                        value,
                                        object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                                    )
                                    if (updateData.isNotEmpty()) {
                                        Log.d(TAG, "send update dataSize = " + updateData.size)
                                        observer.onNext(updateData)
                                    }
                                }
                            }
                        }
                    })
            }
        })
    }

    /**
     * 计算合约coinType对应的cny价格,并赋值到交易对数据bean
     *
     * @param context
     */
    private fun computeFuturePairStatusCNY(context: Context?) {
        if (context == null) {
            return
        }
        C2CApiServiceHelper.getC2CPrice(context, object : Callback<C2CPrice?>() {
            override fun error(type: Int, error: Any) {
                onGetC2CPriceComplete(null)
            }

            override fun callback(returnData: C2CPrice?) {
//                Log.d(TAG,"computePairStatusCNY->C2CPrice"+returnData?.buy)
                onGetC2CPriceComplete(returnData)
            }

            private fun onGetC2CPriceComplete(price: C2CPrice?) {
                Observable.create<String> { e ->
                    e.onNext(reallyUpdateFuturePairStatusData(price))
                    e.onComplete()
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(object : SuccessObserver<String?>() {
                        override fun onSuccess(value: String?) {
                            Log.d(
                                TAG,
                                "onSuccess = pairFutureObservers.size = " + pairFutureObservers.size
                            )
                            synchronized(pairFutureObservers) {
                                for (observer in pairFutureObservers) {
                                    var updateData: ArrayList<PairStatus?> = gson.fromJson(
                                        value,
                                        object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                                    )
                                    if (updateData.isNotEmpty()) {
                                        Log.d(
                                            TAG,
                                            "send future update dataSize = " + updateData.size
                                        )
                                        observer.onNext(updateData)
                                    }
                                }
                            }
                        }
                    })
            }
        })
    }

    /**
     * 更新合约行情
     */
    private fun reallyUpdateFuturePairStatusData(price: C2CPrice?): String {
        Log.d("iiiiii", "reallyUpdateFuturePairStatusData")
        val result = ArrayList<PairStatus>()
        synchronized(pairFutureDataSource) {
            synchronized(dearFuturePairMap) {
                synchronized(allFuturePairStatusList!!) {
                    Log.d(
                        "iiiiiii",
                        "allFuturePairStatusList.size = " + allFuturePairStatusList.size
                    )
                    if (allFuturePairStatusList.isEmpty()) {
                        return gson.toJson(result)
                    }
                    refreshAllFuturePairStatus(allFuturePairStatusList)
                    for (pairStatus in allFuturePairStatusList) {
                        if (pairStatus == null) {
                            continue
                        }
                        val oldPairCompareKey = pairStatus.compareString
                        val dataSource = pairFutureDataSource[pairStatus.pair]
                        dataSource?.copyValues(pairStatus)
                        val isDear = dearFuturePairMap[pairStatus.pair]
                        pairStatus.is_dear = isDear ?: false
                        if (price != null) { //计算折合CNY
                            pairStatus.currentPriceCNY = computeCoinPriceCNY(pairStatus, price)
                        }
                        val newPairCompareKey = pairStatus.compareString
                        Log.d("iiiiii", "oldPairCompareKey = " + oldPairCompareKey)
                        Log.d("iiiiii", "newPairCompareKey = " + oldPairCompareKey)
                        if (!TextUtils.equals(oldPairCompareKey, newPairCompareKey)) {
                            Log.d(TAG, "updateFuturePairStatusData1,addChange")
                            result.add(pairStatus)
                        }
                    }
                }
            }
        }
        return gson.toJson(result)
    }

    /**
     * 更新现货行情
     */
    private fun reallyUpdatePairStatusData(price: C2CPrice?): String {
        val result = ArrayList<PairStatus>()
        synchronized(pairDataSource) {
            synchronized(dearPairMap) {
                synchronized(pairDataList!!) {
                    if (pairDataList.isEmpty()) {
                        return gson.toJson(result)
                    }
                    refreshAllPairStatus(pairDataList)
                    for (pairStatus in pairDataList) {
                        if (pairStatus == null) {
                            continue
                        }
                        val oldPairCompareKey = pairStatus.compareString
                        val dataSource = pairDataSource[pairStatus.pair]
                        dataSource?.copyValues(pairStatus)
                        val isDear = dearPairMap[pairStatus.pair]
                        pairStatus.is_dear = isDear ?: false
                        if (price != null) { //计算折合CNY
                            pairStatus.currentPriceCNY = computeCoinPriceCNY(pairStatus, price)
                        }
                        val newPairCompareKey = pairStatus.compareString
                        if (!TextUtils.equals(oldPairCompareKey, newPairCompareKey)) {
                            Log.d(TAG, "updatePairStatusData1,addChange")
                            result.add(pairStatus)
                        }
                    }
                }
            }
        }
        return gson.toJson(result)
    }


    /**
     * 初始化合约u本位交易对列表
     */
    fun initAllFutureSymbolList(context: Context?, callback: Callback<ArrayList<PairStatus>?>?) {
        if (context == null) {
            return
        }
        val allFutureSymbolListCallback: Callback<ArrayList<PairStatus>?>? =
            object : Callback<ArrayList<PairStatus>?>() {
                override fun error(type: Int, error: Any) {

                }

                override fun callback(returnData: ArrayList<PairStatus>?) {
                    Log.d("iiiiii", "initAllFutureSymbolList,allSymbolSize = " + returnData?.size)
                    callback?.callback(returnData)
                }
            }
        val uSymbolListCallback: Callback<ArrayList<PairStatus>?> =
            object : Callback<ArrayList<PairStatus>?>() {
                override fun error(type: Int, error: Any) {
                }

                override fun callback(returnData: ArrayList<PairStatus>?) {
                    Log.d("iiiiii", "initAllFutureSymbolList,usdtSymbolSize = " + returnData?.size)
                    FutureApiServiceHelperWrapper.getFuturesSymbolListLocal(
                        context,
                        ConstData.PairStatusType.FUTURE_ALL,
                        false,
                        allFutureSymbolListCallback
                    )
                }
            }
        val coinSymbolListCallback: Callback<ArrayList<PairStatus>?>? =
            object : Callback<ArrayList<PairStatus>?>() {
                override fun error(type: Int, error: Any) {
                }

                override fun callback(returnData: java.util.ArrayList<PairStatus>?) {
                    Log.d("iiiiii", "initAllFutureSymbolList,coinSymbolSize = " + returnData?.size)
                    FutureApiServiceHelperWrapper.getFuturesSymbolListLocal(
                        context,
                        ConstData.PairStatusType.FUTURE_U,
                        false,
                        uSymbolListCallback
                    )
                }
            }
        FutureApiServiceHelperWrapper.getFuturesSymbolListLocal(
            context,
            ConstData.PairStatusType.FUTURE_COIN,
            false,
            coinSymbolListCallback
        )
    }

    /**
     * 初始化现货交易对列表
     */
    fun initAllPairStatusData(context: Context?) {
        if (context == null) {
            return
        }
        val observable = PairApiServiceHelper.getFullPairStatusObservable(context)
        observable?.subscribeOn(Schedulers.io())?.map { pairStatuses ->
            synchronized(pairDataList!!) {
                pairDataList.clear()
                pairDataList.addAll(pairStatuses)
                Collections.sort(pairDataList, PairStatus.COMPARATOR)
                synchronized(allPairStatusMap!!) {
                    allPairStatusMap.clear()
                    for (pairStatus in pairStatuses) {
                        pairStatus.pair?.let {
                            allPairStatusMap[it] = pairStatus
                        }
                    }
                }
            }
            ""
        }?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : SuccessObserver<String?>() {
                override fun onSuccess(s: String?) {
                    computePairStatusCNY(context)
                }
            })
    }

    /**
     * 初始化合约交易对列表
     * 缓存socketDataContainer里边合约交易对相关的数据
     */
    fun cacheFuturePairStatusData(context: Context?) {
        if (context == null) {
            return
        }
        val observable = FutureApiServiceHelperWrapper.getFutureTickersLocal(
            context,
            ConstData.PairStatusType.FUTURE_ALL
        )
        observable?.subscribeOn(Schedulers.io())?.map { pairStatuses ->
            Log.d("iiiii", "initAllFuturePairStatusData,pairStatuses = " + pairStatuses.size)
            synchronized(allFuturePairStatusList!!) {
                allFuturePairStatusList.clear()
                allFuturePairStatusList.addAll(pairStatuses)
                Collections.sort(allFuturePairStatusList, PairStatus.COMPARATOR)
                synchronized(allFuturePairStatusMap!!) {
                    allFuturePairStatusMap.clear()
                    for (pairStatus in pairStatuses) {
                        pairStatus?.pair?.let {
                            allFuturePairStatusMap[it] = pairStatus
                        }
                    }
                }
            }
            ""
        }?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : SuccessObserver<String?>() {
                override fun onSuccess(s: String?) {
                    computePairStatusCNY(context)
                }
            })
    }

    //更新现有交易对信息
    fun updatePairStatusData(
        context: Context?,
        handler: Handler?,
        dataSource: PairStatusNew?,
        isRemoveAll: Boolean
    ) {
        if (context == null) {
            return
        }
        CommonUtil.postHandleTask(handler) {
            Observable.create { emitter: ObservableEmitter<String?> ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
//                    val data = gson.fromJson<PairStatusNew>(dataSource.toString(), object : TypeToken<PairStatusNew?>() {}.type)
                    val data: PairStatusNew? = dataSource
                    synchronized(pairDataSource) {
                        if (isRemoveAll) {
                            pairDataSource.clear()
                        }
                        val pair = data?.s
                        pair?.let {
                            val oldItem = pairDataSource[pair]
                            if (oldItem == null) {
                                pairDataSource[pair] = data
                            } else {
                                PairStatusNew.copyValues(oldItem, data)
                            }
                        }
                    }
                    emitter.onNext("")
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        computePairStatusCNY(context)
                    }
                })
        }
    }

    //更新现有交易对信息
    fun updateFuturePairStatusData(
        context: Context?,
        handler: Handler?,
        dataSource: PairStatusNew?,
        isRemoveAll: Boolean
    ) {
        if (context == null) {
            return
        }
        CommonUtil.postHandleTask(handler) {
            Observable.create { emitter: ObservableEmitter<String?> ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
                    val data: PairStatusNew? = dataSource
                    synchronized(pairFutureDataSource) {
                        if (isRemoveAll) {
                            pairFutureDataSource.clear()
                        }
                        val pair = data?.s
                        pair?.let {
                            val oldItem = pairFutureDataSource[pair]
                            if (oldItem == null) {
                                pairFutureDataSource[pair] = data//socket返回的数据保存到map中
                            } else {
                                PairStatusNew.copyValues(oldItem, data)//已有该交易对，则更新数据
                            }
                        }
                    }
                    Log.d("iiiiiii", "pairFutureDataSource.size = " + pairFutureDataSource.size)
                    emitter.onNext("")
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        computeFuturePairStatusCNY(context)
                    }
                })
        }
    }

    /**
     *  添加合约markPrice观察者
     */

    fun subscribeMarkPriceObservable(observer: Observer<MarkPriceBean?>?) {
        if (observer == null) {
            return
        }
        synchronized(markPriceObservers) {
            if (!markPriceObservers.contains(observer)) {
                markPriceObservers.add(observer)
            }
        }
    }

    /**
     *  添加合约markPrice观察者
     */

    fun subscribeIndexPriceObservable(observer: Observer<IndexPriceBean?>?) {
        if (observer == null) {
            return
        }
        synchronized(indexPriceObservers) {
            if (!indexPriceObservers.contains(observer)) {
                indexPriceObservers.add(observer)
            }
        }
    }

    /**
     *  移除合约markPrice观察者
     */
    fun removeMarkPriceObservable(observer: Observer<MarkPriceBean?>?) {
        if (observer == null) {
            return
        }
        synchronized(markPriceObservers) { markPriceObservers.remove(observer) }
    }

    /**
     *  移除合约indexPrice观察者
     */
    fun removeIndexPriceObservable(observer: Observer<IndexPriceBean?>?) {
        if (observer == null) {
            return
        }
        synchronized(indexPriceObservers) { indexPriceObservers.remove(observer) }
    }

    /**
     * 更新交易对是否添加自选
     *
     * @param context
     * @param handler
     * @param dearPairs
     */
    fun updateDearPairs(
        context: Context?,
        handler: Handler?,
        dearPairs: Map<String, Boolean?>?,
        ifCache: Boolean
    ) {
        if (context == null) {
            return
        }
        CommonUtil.postHandleTask(handler) {
            Observable.create { emitter: ObservableEmitter<String?> ->
                if (dearPairs == null || dearPairs.isEmpty()) {
                    emitter.onComplete()
                } else {
                    synchronized(pairDataList!!) {
                        synchronized(allPairStatusMap!!) {
                            synchronized(pairDataSource) {
                                dearPairMap.clear()
                                for (pair in dearPairs.keys) {
                                    val isDear = dearPairs[pair]
                                    val pairStatus = allPairStatusMap[pair]
                                    if (pairStatus != null) {
                                        pairStatus.is_dear = isDear ?: false
                                        //每次点击添加/删除自选更新到本地
                                        if (ifCache) {
                                            updateFavoriteToCache(pair, pairStatus.is_dear)
                                        }
                                    }
                                }
                            }
                            emitter.onNext("")
                        }
                    }
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        computePairStatusCNY(context)
                    }
                })
        }
    }

    /**
     * 更新自选到本地
     *
     * @param pair
     * @param isDear
     */
    private fun updateFavoriteToCache(pair: String, isDear: Boolean) {
//        updateDearPairMap()
        if (isDear) {
            dearPairMap[pair] = isDear
        } else {
            dearPairMap.remove(pair)
        }
        val pairsJson = Gson().toJson(dearPairMap)
        SharedPreferenceUtils.putData(ConstData.DEAR_PAIR_SP, pairsJson)
    }

    private fun updateDearPairMap() {
        dearPairMap.clear()
        val pairs1 = cachePair ?: HashMap()
        for (pairsKey in pairs1.keys) {
            dearPairMap[pairsKey] = pairs1[pairsKey]
        }
    }

    val cachePair: Map<String, Boolean>?
        get() {
            val hasCachePairs = SharedPreferenceUtils.getData(ConstData.DEAR_PAIR_SP, "") as String
            val hashMap: Map<String, Boolean> = HashMap()
            return Gson().fromJson(hasCachePairs, hashMap.javaClass)
        }


    //主动拉取所有u本位交易对信息，直接返回，不适用观察者模式
    fun getAllFuturePairStatus(
        context: Context?,
        type: ConstData.PairStatusType?,
        callback: Callback<ArrayList<PairStatus?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        var futureList: ArrayList<PairStatus?>? = null
        when (type) {
            ConstData.PairStatusType.FUTURE_ALL -> {
                if (futureList == null) {
                    futureList = ArrayList()
                }
                futureList?.addAll(pairFutureUDataList)
                futureList?.addAll(pairFutureCoinDataList)
            }
            ConstData.PairStatusType.FUTURE_U -> futureList = pairFutureUDataList
            ConstData.PairStatusType.FUTURE_COIN -> futureList = pairFutureCoinDataList
        }
        if (futureList != null) {
            synchronized(futureList!!) {
                val pairListString = gson.toJson(futureList)
                callback.callback(
                    gson.fromJson<ArrayList<PairStatus?>>(
                        pairListString,
                        object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                    )
                )
            }
        }
    }

    //主动拉取所有合约交易对信息，直接返回
    fun getAllFuturePairStatus(
        context: Context?,
        type: String?
    ): Observable<ArrayList<PairStatus?>>? {
        if (context == null) {
            return null
        }
        var futureList: ArrayList<PairStatus?>? = null
        when (type) {
            context.getString(R.string.all_future_coin) -> {
                if (futureList == null) {
                    futureList = ArrayList()
                }
                futureList?.addAll(pairFutureUDataList)
                futureList?.addAll(pairFutureCoinDataList)
            }
            context.getString(R.string.usdt_base) -> futureList = pairFutureUDataList
            context.getString(R.string.coin_base) -> futureList = pairFutureCoinDataList
        }
        synchronized(futureList!!) {
            val pairListString = gson.toJson(futureList)
            return Observable.just(
                gson.fromJson(
                    pairListString,
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            )
        }
    }

    //主动拉取所有交易对信息，直接返回，不适用观察者模式
    fun getAllPairStatus(context: Context?, callback: Callback<ArrayList<PairStatus?>?>?) {
        if (context == null || callback == null) {
            return
        }
        synchronized(pairDataList!!) {
            val pairListString = gson.toJson(pairDataList)
            callback.callback(
                gson.fromJson<ArrayList<PairStatus?>>(
                    pairListString,
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            )
        }
    }

    //主动拉取所有交易对信息，直接返回
    fun getAllPairStatus(context: Context?): Observable<ArrayList<PairStatus?>>? {
        if (context == null) {
            return null
        }
        synchronized(pairDataList) {
            val pairListString = gson.toJson(pairDataList)
            return Observable.just(
                gson.fromJson(
                    pairListString,
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            )
        }
    }

    //主动拉取单个交易对信息，直接返回，不适用观察者模式
    fun getSinceOrderPairs(
        context: Context?,
        type: Int,
        maxSize: Int,
        callback: Callback<ArrayList<PairStatus?>?>?
    ) {
        var size = maxSize
        if (context == null || callback == null) {
            return
        }
        size = if (size < 1) 1 else size
        synchronized(pairDataList) {
            val pairStatuses = ArrayList(pairDataList)
            Collections.sort(
                pairStatuses,
                if (type == 1) PairStatus.COMPARATOR_SINCE_UP else PairStatus.COMPARATOR_SINCE_DOWN
            )
            val result: MutableList<PairStatus?> = ArrayList()
            for (i in pairStatuses.indices) {
                val pairStatus = pairStatuses[i]
                if (pairStatus?.isHighRisk == null || !pairStatus.isHighRisk!!) {
                    result.add(pairStatus)
                }
                if (result.size >= size) {
                    break
                }
            }
            //            List<PairStatus> result = pairStatuses.subList(0, Math.min(pairStatuses.size(), maxSize));
            callback.callback(
                gson.fromJson<ArrayList<PairStatus?>>(
                    gson.toJson(result),
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            )
        }
    }

    fun getSinceOrderPairs(
        context: Context?,
        type: Int,
        maxSize: Int
    ): Observable<ArrayList<PairStatus?>>? {
        var size = maxSize
        if (context == null) {
            return null
        }
        size = if (size < 1) 1 else size
        synchronized(pairDataList) {
            val pairStatuses = ArrayList(pairDataList)
            Collections.sort(
                pairStatuses,
                if (type == 1) PairStatus.COMPARATOR_SINCE_UP else PairStatus.COMPARATOR_SINCE_DOWN
            )
            val result: MutableList<PairStatus?> = ArrayList()
            for (i in pairStatuses.indices) {
                val pairStatus = pairStatuses[i]
                if (pairStatus?.isHighRisk == null || !pairStatus.isHighRisk!!) {
                    result.add(pairStatus)
                }
                if (result.size >= size) {
                    break
                }
            }
            return Observable.just(
                gson.fromJson(
                    gson.toJson(result),
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            )
        }
    }

    /**
     * 根据交易对名字获取交易对的数据
     * 采用回调的方式
     */
    fun getPairStatus(
        context: Context?,
        pairStatusType: ConstData.PairStatusType?,
        pair: String?,
        callback: Callback<PairStatus?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        var pairStatusMap: MutableMap<String, PairStatus>? = null
        when (pairStatusType) {
            ConstData.PairStatusType.SPOT -> {
                pairStatusMap = allPairStatusMap
            }
            ConstData.PairStatusType.FUTURE_U -> {
                pairStatusMap = allFuturePairStatusMap
            }
        }
        if (pairStatusMap != null) {
            synchronized(pairStatusMap) {
                val pairStatus = pairStatusMap[pair]
                if (pairStatus == null) {
                    callback.callback(null)
                } else {
                    callback.callback(
                        gson.fromJson(
                            gson.toJson(pairStatus),
                            PairStatus::class.java
                        )
                    )
                }
            }
        }
    }

    /**
     * 根据交易对名字获取交易对的数据
     * 异步直接返回
     */
    fun getPairStatusSync(
        context: Context?,
        pairStatusType: ConstData.PairStatusType?,
        pair: String?
    ): PairStatus? {
        if (context == null) {
            return null
        }
        var pairStatusMap: MutableMap<String, PairStatus>? = null
        when (pairStatusType) {
            ConstData.PairStatusType.SPOT -> {
                pairStatusMap = allPairStatusMap
            }
            ConstData.PairStatusType.FUTURE_ALL -> {
                pairStatusMap = allFuturePairStatusMap
            }
        }
        if (pairStatusMap != null) {
            synchronized(pairStatusMap) {
                val pairStatus = pairStatusMap[pair]
                return if (pairStatus == null) {
                    null
                } else {
                    gson.fromJson(gson.toJson(pairStatus), PairStatus::class.java)
                }
            }
        }
        return null
    }

    /**
     * 根据交易对名字获取交易对的数据
     * Observable方式
     */
    fun getPairStatusObservable(
        context: Context?,
        pairStatusType: ConstData.PairStatusType?,
        pair: String?
    ): Observable<PairStatus>? {
        if (context == null) {
            return null
        }
        var pairStatusMap: MutableMap<String, PairStatus>? = null
        when (pairStatusType) {
            ConstData.PairStatusType.SPOT -> {
                pairStatusMap = allPairStatusMap
            }
            ConstData.PairStatusType.FUTURE_U -> {
                pairStatusMap = allFuturePairStatusMap
            }
        }
        if (pairStatusMap != null) {
            synchronized(pairStatusMap) {
                val pairStatus = pairStatusMap[pair]
                return if (pairStatus == null) {
                    null
                } else {
                    Observable.just(gson.fromJson(gson.toJson(pairStatus), PairStatus::class.java))
                }
            }
        }
        return Observable.empty()
    }

    private fun getPairCoinName(pair: String?): String? {
        var coinName: String? = null
        if (pair != null) {
            val arr = pair.split("_").toTypedArray()
            if (arr.size > 1) {
                coinName = arr[0]
            }
        }
        return coinName
    }

    private fun getPairSetName(pair: String?): String? {
        var setName: String? = null
        if (pair != null) {
            val arr = pair.split("_").toTypedArray()
            if (arr.size > 1) {
                setName = arr[1]
            }
        }
        return setName
    }

    /**
     * 获取所有交易对名列表
     */
    fun getAllPair(
        context: Context?,
        pairStatusType: ConstData.PairStatusType?
    ): ArrayList<String>? {
        if (context == null) {
            return null
        }
        var pairStatusMap: MutableMap<String, PairStatus>? = null
        when (pairStatusType) {
            ConstData.PairStatusType.SPOT -> {
                pairStatusMap = allPairStatusMap
            }
            ConstData.PairStatusType.FUTURE_U -> {
                pairStatusMap = allFuturePairStatusMap
            }
        }
        if (pairStatusMap != null) {
            synchronized(pairStatusMap) {
                return if (pairStatusMap == null || pairStatusMap.isEmpty()) {
                    null
                } else {
                    ArrayList(pairStatusMap.keys)
                }
            }
        }
        return null
    }

    /**
     * 获取现货的行情数据
     * set(自选，usdt，eth)
     */
    fun getPairsWithSet(
        context: Context?,
        setName: String?,
        callback: Callback<ArrayList<PairStatus?>?>?
    ) {
        if (context == null || callback == null || setName == null) {
            return
        }
        PairApiServiceHelper.getHomeTickersLocal(context)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(
                HttpCallbackSimple(
                    context,
                    false,
                    object : Callback<ArrayList<PairStatus?>?>() {
                        override fun error(type: Int, error: Any) {
                            callback?.error(type, error)
                        }

                        override fun callback(returnData: ArrayList<PairStatus?>?) {
                            if (returnData != null) {
                                val result = ArrayList<PairStatus?>()
                                for (i in returnData.indices) {
                                    val pairStatus = returnData[i]
                                    if (context.getString(R.string.pair_collect) == setName) {
                                        var dearPair = dearPairMap[pairStatus?.pair]
                                        if (dearPair != null) {
                                            pairStatus?.is_dear = true
                                            result.add(pairStatus)
                                        }
                                    } else {
                                        if (TextUtils.equals(pairStatus?.setName, setName)) {
                                            result.add(pairStatus)
                                        }
                                    }
                                }
                                callback.callback(
                                    gson.fromJson(
                                        gson.toJson(result),
                                        object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                                    )
                                )
                            } else {
                                callback?.error(
                                    ConstData.ERROR_NORMAL,
                                    context.getString(R.string.error_data)
                                )
                            }
                        }
                    })
            )
    }

    /**
     * 获取合约的行情数据
     * pairType(自选，u本位，币本位,全部)
     */
    fun getFuturesPairsWithSet(
        context: Context?,
        pairType: ConstData.PairStatusType?,
        callback: Callback<ArrayList<PairStatus?>?>?
    ) {
        if (context == null || callback == null || pairType == null) {
            return
        }
        //自选数据等后边补充逻辑
        if (ConstData.PairStatusType.FUTURE_DEAR == pairType) {

        }
        FutureApiServiceHelperWrapper.getFutureTickersLocal(context, pairType)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(
                HttpCallbackSimple(
                    context,
                    false,
                    object : Callback<ArrayList<PairStatus?>?>() {
                        override fun error(type: Int, error: Any) {
                            callback?.error(type, error)
                        }

                        override fun callback(returnData: ArrayList<PairStatus?>?) {
                            if (returnData != null) {
                                val result = ArrayList<PairStatus?>()
                                for (i in returnData.indices) {
                                    val pairStatus = returnData[i]
                                    result.add(pairStatus)
                                }
                                callback.callback(
                                    gson.fromJson(
                                        gson.toJson(result),
                                        object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                                    )
                                )
                            } else {
                                callback?.error(
                                    ConstData.ERROR_NORMAL,
                                    context.getString(R.string.error_data)
                                )
                            }
                        }
                    })
            )
    }


    fun getPairStatusListByKey(context: Context?, key: String?): ArrayList<PairStatus?>? {
        if (context == null) {
            return null
        }
        synchronized(pairDataList) {
            return if (key == null || key.trim { it <= ' ' }.isEmpty()) {
                gson.fromJson<ArrayList<PairStatus?>>(
                    gson.toJson(pairDataList),
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            } else {
                val realKey = key.uppercase(Locale.getDefault())
                val result = ArrayList<PairStatus?>()
                for (pairStatus in pairDataList) {
                    val pair = pairStatus?.pair?.uppercase(Locale.getDefault())
                    if (pair != null && pair.contains(realKey)) {
                        result.add(pairStatus)
                    }
                }
                gson.fromJson<ArrayList<PairStatus?>>(
                    gson.toJson(result),
                    object : TypeToken<ArrayList<PairStatus?>?>() {}.type
                )
            }
        }
    }

    /**
     * 更新现货买卖单深度数据
     */
    fun updateQuotationOrderNewDataFiex(
        context: Context?,
        depthType: Int?,
        handler: Handler?,
        currentPair: String?,
        tradeOrderDepth: TradeOrderDepth?,
        isRemoveAll: Boolean
    ) {
        var observer: ArrayList<Observer<Pair<String?, TradeOrderPairList?>>>? = null
        var dataList: ArrayList<QuotationOrderNew?>? = null
        CommonUtil.postHandleTask(handler) {
            Observable.create<String>(ObservableOnSubscribe { emitter ->
                if (tradeOrderDepth == null) {
                    emitter.onComplete()
                } else {
                    when (depthType) {
                        ConstData.DEPTH_SPOT_TYPE -> {
                            observer = spotDepthObservers
                            dataList = depthDataList
                        }
                        ConstData.DEPTH_FUTURE_TYPE -> {
                            observer = futureDepthObservers
                            dataList = futureDepthDataList
                        }
                    }
                    var tradeOrderPairList =
                        parseOrderDepthData(context, depthType, tradeOrderDepth)
                    var orderDepthData = tradeOrderPairList?.let { parseOrderDepthToList(it) }
                    synchronized(dataList!!) {
                        if (isRemoveAll) {
                            dataList!!.clear()
                        }
                    }
                    val count = orderDepthData?.size
                    for (i in 0 until count!!) {
                        val quotationOrderNew = orderDepthData?.get(i)
                        if (quotationOrderNew != null && (quotationOrderNew.a < 0 || quotationOrderNew.v < 0)) { //存在错误数据，重新订阅
                            sendSocketCommandBroadcast(context, SocketUtil.COMMAND_PAIR_CHANGED)
                            emitter.onComplete()
                            return@ObservableOnSubscribe
                        }
                        if (quotationOrderNew != null && quotationOrderNew.v > 0) { //添加反向节点
                            val quotationOrderNewReverse = QuotationOrderNew()
                            quotationOrderNewReverse.a = 0.0
                            quotationOrderNewReverse.v = 0.0
                            quotationOrderNewReverse.pair = quotationOrderNew.pair
                            quotationOrderNewReverse.p = quotationOrderNew.p
                            quotationOrderNewReverse.d = if ("BID".equals(
                                    quotationOrderNew.d,
                                    ignoreCase = true
                                )
                            ) "ASK" else "BID"
                            orderDepthData?.add(quotationOrderNewReverse)
                        }
                    }
                    var mergeData: Array<java.util.ArrayList<QuotationOrderNew?>?>?
                    synchronized(dataList!!) {
                        mergeData = mergeQuotationOrder2(dataList!!, orderDepthData)
                    }
                    val askOrderNews: ArrayList<QuotationOrderNew?> =
                        if (mergeData == null || mergeData!![0] == null) ArrayList() else mergeData!![0]!!
                    val bidOrderNews: ArrayList<QuotationOrderNew?> =
                        if (mergeData == null || mergeData!![1] == null) ArrayList() else mergeData!![1]!!
                    synchronized(dataList!!) {
                        dataList!!.clear()
                        dataList!!.addAll(askOrderNews)
                        dataList!!.addAll(bidOrderNews)
                    }
                    //组装返回数据
                    val askOrders = ArrayList<TradeOrder?>(askOrderNews.size)
                    val bidOrders = ArrayList<TradeOrder?>(bidOrderNews.size)
                    for (i in askOrderNews.indices) {
                        askOrderNews[i]?.toTradeOrder()?.let {
                            askOrders.add(it)
                        }
                    }
                    for (i in bidOrderNews.indices) {
                        bidOrderNews[i]?.toTradeOrder()?.let {
                            bidOrders.add(it)
                        }
                    }
                    val result = TradeOrderPairList()
                    result.bidOrderList = bidOrders
                    result.askOrderList = askOrders
                    emitter.onNext(gson.toJson(result))
                }
            }).subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        synchronized(observer!!) {
                            for (observer in observer!!) {
                                observer.onNext(
                                    Pair(
                                        currentPair,
                                        gson.fromJson(
                                            s,
                                            object : TypeToken<TradeOrderPairList?>() {}.type
                                        )
                                    )
                                )
                            }
                        }
                    }
                })
        }
    }


    /**
     * 请求得到的数据转换成TradeOrderPairList
     * type 0现货 1 u本位合约 2币本位合约
     */
    fun parseOrderDepthData(
        context: Context?,
        type: Int?,
        depth: TradeOrderDepth
    ): TradeOrderPairList? {
        var pairListData = TradeOrderPairList()
        var askOrderList = ArrayList<TradeOrder?>()
        var bidOrderList = ArrayList<TradeOrder?>()
        var result = depth
        var pair = result?.s
        var bidArray = result?.b
        var askArray = result?.a
        var contractSize: String? = null
        var currentPairObj = CookieUtil.getCurrentFutureUPairObjrInfo(context!!)
        if (currentPairObj != null) {
            contractSize = currentPairObj.contractSize
        }
        if (bidArray != null) {
            for (bidItem in bidArray) {
                var tradeOrder = TradeOrder()
                //价格
                var price = bidItem?.get(0)?.toDouble()
                //张数/数量
                var count = bidItem?.get(1)?.toDouble()
                tradeOrder.price = price
                tradeOrder.priceString = bidItem?.get(0)
                tradeOrder.orderType = "BID"
                if (type == ConstData.DEPTH_SPOT_TYPE) {
                    tradeOrder.exchangeAmount = count!!
                }
                if (type == ConstData.DEPTH_FUTURE_TYPE) {
                    //计算出每个订单的USDT数量
                    var quantity = BigDecimal(count!!).multiply(BigDecimal(contractSize.toString()))
                    tradeOrder.exchangeAmount = quantity?.toDouble()!!
                }
                tradeOrder.direction = "BID"
                tradeOrder.pair = pair
                bidOrderList.add(tradeOrder)
            }
            pairListData!!.bidOrderList = bidOrderList
        }
        if (askArray != null) {
            for (askItem in askArray) {
                var tradeOrder = TradeOrder()
                //价格
                var price = askItem?.get(0)?.toDouble()
                //张数/数量
                var count = askItem?.get(1)?.toDouble()
                tradeOrder.price = price
                tradeOrder.priceString = askItem?.get(0)
                tradeOrder.orderType = "ASK"
                if (type == ConstData.DEPTH_SPOT_TYPE) {
                    tradeOrder.exchangeAmount = count!!
                }
                if (type == ConstData.DEPTH_FUTURE_TYPE) {
                    //计算出每个订单的USDT数量
                    var quantity = BigDecimal(count!!).multiply(BigDecimal(contractSize.toString()))
                    tradeOrder.exchangeAmount = quantity?.toDouble()!!
                }
                tradeOrder.direction = "ASK"
                tradeOrder.pair = pair
                askOrderList.add(tradeOrder)
            }
            pairListData!!.askOrderList = askOrderList
        }
        return pairListData
    }

    fun parseOrderDepthToList(tradeOrderPairList: TradeOrderPairList): ArrayList<QuotationOrderNew?> {
        var newQuotationOrderList = ArrayList<QuotationOrderNew?>()
        var bidList = tradeOrderPairList.bidOrderList
        var askList = tradeOrderPairList.askOrderList
        if (bidList != null) {
            for (bid in bidList) {
                var newQuotationOrder = QuotationOrderNew()
                newQuotationOrder.pair = bid?.pair
                newQuotationOrder.p = bid?.priceString
                newQuotationOrder.a = bid?.exchangeAmount!!
                newQuotationOrder.d = bid?.direction
                newQuotationOrder.v = (bid.price?.times(bid.exchangeAmount)) as Double
                newQuotationOrderList.add(newQuotationOrder)
            }
        }
        if (askList != null) {
            for (ask in askList) {
                var newQuotationOrder = QuotationOrderNew()
                newQuotationOrder.pair = ask?.pair
                newQuotationOrder.p = ask?.priceString
                newQuotationOrder.a = ask?.exchangeAmount!!
                newQuotationOrder.d = ask?.direction
                newQuotationOrder.v = (ask.price?.times(ask.exchangeAmount)) as Double
                newQuotationOrderList.add(newQuotationOrder)
            }
        }
        return newQuotationOrderList
    }


    //发送数据更新通知
    fun sendSocketCommandBroadcast(context: Context?, type: Int) {
        if (context == null) {
            return
        }
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(context.packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        context.sendBroadcast(intent)
    }

    //按pair和价格合并 Ask Bid
    @Throws(Exception::class)
    fun mergeQuotationOrder(data: List<QuotationOrderNew?>?): Array<ArrayList<QuotationOrderNew>?>? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val result: MutableMap<String, QuotationOrderNew> = HashMap()
        for (quotationDealNew in data) {
            quotationDealNew?.let {
                val key = quotationDealNew.key
                var dealNew = result[key]
                if (dealNew == null) {
                    dealNew = QuotationOrderNew()
                    dealNew.pair = quotationDealNew.pair
                    dealNew.p = quotationDealNew.p
                    dealNew.d = quotationDealNew.d
                    result[key] = dealNew
                }
                dealNew.a += quotationDealNew.a
                dealNew.v += quotationDealNew.v
            }
        }
        val returnData: Array<ArrayList<QuotationOrderNew>?> =
            arrayOfNulls<ArrayList<QuotationOrderNew>?>(2)
        val newList = ArrayList(result.values)
        val askList = ArrayList<QuotationOrderNew>()
        val bidList = ArrayList<QuotationOrderNew>()
        for (dealNew in newList) {
            if (dealNew.a >= 0.0001 || dealNew.a <= -0.0001) {
                if ("ASK".equals(dealNew.d, ignoreCase = true)) {
                    askList.add(dealNew)
                } else if ("BID".equals(dealNew.d, ignoreCase = true)) {
                    bidList.add(dealNew)
                }
            }
        }
        returnData[0] = askList
        returnData[1] = bidList
        return returnData
    }

    //按pair和价格合并 Ask Bid，直接替换对应的交易量  交易额
    @Throws(Exception::class)
    fun mergeQuotationOrder2(
        oldData: List<QuotationOrderNew?>,
        newData: List<QuotationOrderNew?>?
    ): Array<ArrayList<QuotationOrderNew?>?>? {
        if (newData == null || newData.isEmpty()) {
            return null
        }
        val result: MutableMap<String, QuotationOrderNew?> = HashMap()
        for (quotationDealNew in oldData) {
            val key = quotationDealNew?.key
            key?.let {
                result[key] = quotationDealNew
            }
        }
        //直接替换对应key的数据
        for (quotationDealNew in newData) {
            val key = quotationDealNew?.key
            key?.let {
                result[key] = quotationDealNew
            }
        }
        val returnData: Array<ArrayList<QuotationOrderNew?>?> = arrayOfNulls(2)
        val newList = ArrayList(result.values)
        val askList = ArrayList<QuotationOrderNew?>()
        val bidList = ArrayList<QuotationOrderNew?>()
        for (dealNew in newList) {
            dealNew?.let {
                if (dealNew.a >= 0.0001 || dealNew.v >= 0.0001) {
                    if ("ASK".equals(dealNew.d, ignoreCase = true)) {
                        askList.add(dealNew)
                    } else if ("BID".equals(dealNew.d, ignoreCase = true)) {
                        bidList.add(dealNew)
                    }
                }
            }
        }
        returnData[0] = askList
        returnData[1] = bidList
        return returnData
    }

    //主动拉去挂单数据，直接回调
    fun getOrderList(context: Context?, depthType: Int?, callback: Callback<TradeOrderPairList?>?) {
        if (context == null || callback == null) {
            return
        }
        var dataList: ArrayList<QuotationOrderNew?>? = null
        when (depthType) {
            ConstData.DEPTH_SPOT_TYPE -> {
                dataList = depthDataList
            }
            ConstData.DEPTH_FUTURE_TYPE -> {
                dataList = futureDepthDataList
            }
        }
        synchronized(dataList!!) {
            try {
                val mergeData = mergeQuotationOrder(dataList)
                val askOrderNews =
                    if (mergeData == null || mergeData[0] == null) ArrayList() else mergeData[0]!!
                val bidOrderNews =
                    if (mergeData == null || mergeData[1] == null) ArrayList() else mergeData[1]!!
                //组装返回数据
                val askOrders = ArrayList<TradeOrder?>(askOrderNews.size)
                val bidOrders = ArrayList<TradeOrder?>(bidOrderNews.size)
                for (i in askOrderNews.indices) {
                    askOrders.add(askOrderNews[i].toTradeOrder())
                }
                for (i in bidOrderNews.indices) {
                    bidOrders.add(bidOrderNews[i].toTradeOrder())
                }
                val result = TradeOrderPairList()
                result.bidOrderList = bidOrders
                result.askOrderList = askOrders
                callback.callback(result)
            } catch (e: Exception) {
                printError(e)
            }
        }
    }

    //主动拉取挂单数据，直接回调
    fun getOrderListFiex(
        context: Context?,
        depthDataList: ArrayList<QuotationOrderNew?>?,
        callback: Callback<TradeOrderPairList?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        try {
            val mergeData = mergeQuotationOrder(depthDataList)
            val askOrderNews =
                if (mergeData == null || mergeData[0] == null) ArrayList() else mergeData[0]!!
            val bidOrderNews =
                if (mergeData == null || mergeData[1] == null) ArrayList() else mergeData[1]!!
            //组装返回数据
            val askOrders = ArrayList<TradeOrder?>(askOrderNews.size)
            val bidOrders = ArrayList<TradeOrder?>(bidOrderNews.size)
            for (i in askOrderNews.indices) {
                askOrders.add(askOrderNews[i].toTradeOrder())
            }
            for (i in bidOrderNews.indices) {
                bidOrders.add(bidOrderNews[i].toTradeOrder())
            }
            val result = TradeOrderPairList()
            result.bidOrderList = bidOrders
            result.askOrderList = askOrders
            callback.callback(result)
        } catch (e: Exception) {
            printError(e)
        }
    }

    fun updateQuotationDealNewData(
        handler: Handler?,
        currentPair: String?,
        dataSource: ArrayList<QuotationDealNew?>?,
        removeAll: Boolean
    ) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<String> { emitter ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
                    val result = ArrayList<TradeOrder>()
                    synchronized(dealList) {
                        if (removeAll) {
                            dealList.clear()
                        }
                        dealList.addAll(dataSource)
                        //移除错误交易对的数据(这个容错可能不需要)
                        for (i in dealList.indices.reversed()) {
                            val dealNew = dealList[i]
                            if (!TextUtils.equals(currentPair, dealNew?.pair)) {
                                dealList.removeAt(i)
                            }
                        }
                        if (dealList.size > DEAL_MAX_SIZE) {
                            //超过上限之后，移除后面的是数据
                            Collections.sort(dealList, QuotationDealNew.COMPARATOR)
                            CommonUtil.removeListItem(dealList, DEAL_MAX_SIZE, dealList.size - 1)
                        }
                        for (dealNew in dealList) {
                            dealNew?.toTradeOrder()?.let { result.add(it) }
                        }
                    }
                    emitter.onNext(gson.toJson(result))
                }
            }
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onError(t: Throwable) {
                        printError(t)
                    }

                    override fun onSuccess(s: String?) {
                        synchronized(dealObservers) {
                            for (observer in dealObservers) {
                                observer.onNext(
                                    Pair(
                                        currentPair,
                                        gson.fromJson(
                                            s,
                                            object : TypeToken<ArrayList<TradeOrder?>?>() {}.type
                                        )
                                    )
                                )
                            }
                        }
                    }
                })
        }
    }

    //主动拉取成交数据，直接回调
    fun getAllQuotationDeal(
        context: Context?,
        pair: String?,
        callback: Callback<ArrayList<TradeOrder?>?>?
    ) {
        if (context == null || callback == null || TextUtils.isEmpty(pair)) {
            return
        }
        synchronized(dealList) {
            try {
                Collections.sort(dealList, QuotationDealNew.COMPARATOR)
                val result = ArrayList<TradeOrder?>()
                var count = 0
                for (i in dealList.indices.reversed()) {
                    val dealNew = dealList[i]
                    if (TextUtils.equals(pair, dealNew?.pair)) {
                        result.add(dealNew?.toTradeOrder())
                        count++
                        if (count >= DEAL_MAX_SIZE) {
                            break
                        }
                    }
                }
                callback.callback(result)
            } catch (e: Exception) {
                printError(e)
            }
        }
    }

    /**
     * 当前交易对的成交价
     */
    fun getCurrentPairDeal(handler: Handler?, data: PairDeal?) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<PairDeal> { emitter ->
                if (data != null) {
                    emitter.onNext(data)
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<PairDeal>() {
                    override fun onSuccess(pairDeal: PairDeal) {
                        synchronized(currentPairDealObservers) {
                            for (observer in currentPairDealObservers) {
                                observer?.onNext(pairDeal)
                            }
                        }
                    }
                })
        }
    }

    /**
     * 当前交易对的24小时行情
     */
    fun getCurrentPairQuotation(handler: Handler?, data: PairQuotation?) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<PairQuotation> { emitter ->
                if (data != null) {
                    emitter.onNext(data)
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<PairQuotation>() {
                    override fun onSuccess(pairQuo: PairQuotation) {
                        synchronized(pairQuotationObservers) {
                            for (observer in pairQuotationObservers) {
                                observer?.onNext(pairQuo)
                            }
                        }
                    }
                })
        }
    }


    /**
     * 更新MarkPrice
     */
    fun updateMarkPrice(handler: Handler?, data: MarkPriceBean?) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<MarkPriceBean> { emitter ->
                if (data != null) {
                    emitter.onNext(data)
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<MarkPriceBean>() {
                    override fun onSuccess(markPrice: MarkPriceBean) {
                        synchronized(markPriceObservers) {
                            for (observer in markPriceObservers) {
                                observer?.onNext(markPrice)
                            }
                        }
                    }
                })
        }
    }

    /**
     * 更新IndexPrice
     */
    fun updateIndexPrice(handler: Handler?, data: IndexPriceBean?) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<IndexPriceBean> { emitter ->
                if (data != null) {
                    emitter.onNext(data)
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<IndexPriceBean>() {
                    override fun onSuccess(indexPriceBean: IndexPriceBean) {
                        synchronized(indexPriceObservers) {
                            for (observer in indexPriceObservers) {
                                observer?.onNext(indexPriceBean)
                            }
                        }
                    }
                })
        }
    }


    fun addKLineData(
        currentPair: String?,
        handler: Handler?,
        kLineId: String?,
        kLineItem: KLineItem?
    ) {
        if (currentPair == null || kLineItem == null) {
            return
        }
        CommonUtil.postHandleTask(handler) {
            Observable.create<String> { emitter -> emitter.onNext(gson.toJson(kLineItem)) }
                .subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        synchronized(kLineAddObservers) {
                            for (observer in kLineAddObservers) {
                                observer.onNext(
                                    KLineItemPair(
                                        currentPair,
                                        kLineId,
                                        gson.fromJson(s, object : TypeToken<KLineItem?>() {}.type)
                                    )
                                )
                            }
                        }
                    }
                })
        }
    }


    fun onUserBalanceChangedFiex(balance: UserBalance?) {
        synchronized(userBalanceObservers) {
            for (observer in userBalanceObservers) {
                if (balance != null) {
                    observer.onNext(balance)
                }
            }
        }
    }

    fun onUserOrderChangedFiex(tradeorder: TradeOrderFiex?) {
        synchronized(userOrderObservers) {
            for (observer in userOrderObservers) {
                if (tradeorder != null) {
                    observer.onNext(tradeorder)
                }
            }
        }
    }

    private val leverDetailCache: MutableMap<String, WalletLeverDetail?> = HashMap()


    fun getWalletLeverDetail(
        context: Context?,
        pair: String?,
        force: Boolean
    ): Observable<WalletLeverDetail>? {
        if (context == null || pair == null) {
            return null
        }
        synchronized(leverDetailCache) {
            val leverDetail = leverDetailCache[pair]
            if (!force && leverDetail != null) {
                return Observable.just(leverDetail)
            }
        }
        return ApiManager.build(context).getService(WalletApiService::class.java)
            ?.getWalletLeverDetail(pair)
            ?.flatMap { returnData: HttpRequestResultData<WalletLeverDetail?>? ->
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    if (returnData.data != null && !TextUtils.isEmpty(returnData.data?.pair)) {
                        synchronized(leverDetailCache) {
                            leverDetailCache.clear()
                            returnData.data?.pair?.let {
                                leverDetailCache.put(it, returnData.data)
                            }
                        }
                    }
                    Observable.just(returnData.data)
                } else {
                    Observable.error(RuntimeException(if (returnData?.msg == null) "null" else returnData.msg))
                }
            }
    }

    //获取杠杆详情和总量
    fun getWalletLeverDetailTotal(
        context: Context?,
        pair: String?,
        force: Boolean
    ): Observable<WalletLeverDetail>? {
        val observable = getWalletLeverDetail(context, pair, force) ?: return null
        return observable.flatMap { leverDetail: WalletLeverDetail? ->
            leverDetail?.let {
                computeWalletLeverDetailTotal(
                    context,
                    it
                )
            }
        }
    }

    //计算杠杆资产详情总量
    fun computeWalletLeverDetailTotal(
        context: Context?,
        leverDetail: WalletLeverDetail
    ): Observable<WalletLeverDetail>? {
        return C2CApiServiceHelper.getC2CPrice(context)
            ?.materialize()
            ?.flatMap(Function<Notification<C2CPrice?>, ObservableSource<WalletLeverDetail>> { notify ->
                if (notify.isOnNext) {
                    val c2CPrice = notify.value
                    val coinType = getPairCoinName(leverDetail.pair)
                    val set = getPairSetName(leverDetail.pair)
                    if (set != null && coinType != null) {
                        if (set.equals("USDT", ignoreCase = true)) {
                            val pairStatus = getPairStatusSync(
                                context,
                                ConstData.PairStatusType.SPOT,
                                leverDetail.pair
                            )
                            if (pairStatus != null) {
                                val totalCoinCNY =
                                    computeTotalMoneyCNY(leverDetail.estimatedTotalAmount, c2CPrice)
                                val totalSetCNY = computeTotalMoneyCNY(
                                    leverDetail.afterEstimatedTotalAmount,
                                    c2CPrice
                                )
                                leverDetail.totalCNY =
                                    if (totalCoinCNY == null && totalSetCNY == null) null else
                                        (0.0 + (totalCoinCNY ?: 0.0) + (totalSetCNY ?: 0.0))
                                val totalCoinBorrowCNY =
                                    if (leverDetail.coinBorrow == null) null else computeTotalMoneyCNY(
                                        leverDetail.coinBorrow!!.multiply(BigDecimal(pairStatus.currentPrice)),
                                        c2CPrice
                                    )
                                val totalSetBorrowCNY =
                                    computeTotalMoneyCNY(leverDetail.afterCoinBorrow, c2CPrice)
                                val totalCoinInterestCNY =
                                    if (leverDetail.coinInterest == null) null else computeTotalMoneyCNY(
                                        leverDetail.coinInterest!!.multiply(BigDecimal(pairStatus.currentPrice)),
                                        c2CPrice
                                    )
                                val totalSetInterestCNY =
                                    computeTotalMoneyCNY(leverDetail.afterCoinInterest, c2CPrice)
                                leverDetail.totalDebtCNY =
                                    if (totalCoinBorrowCNY == null && totalSetBorrowCNY == null && totalCoinInterestCNY == null && totalSetInterestCNY == null) null else
                                        (0.0 + (totalCoinBorrowCNY ?: 0.0) + (totalSetBorrowCNY
                                            ?: 0.0) + (totalCoinInterestCNY
                                            ?: 0.0) + (totalSetInterestCNY ?: 0.0))
                                leverDetail.netAssetsCNY =
                                    if (leverDetail.totalCNY == null || leverDetail.totalDebtCNY == null) null else leverDetail.totalCNY!! - leverDetail.totalDebtCNY!!
                            }
                        } else {
                            val totalCoinCNY =
                                computeTotalMoneyCNY(leverDetail.estimatedTotalAmount, c2CPrice)
                            val totalSetCNY = computeTotalMoneyCNY(
                                leverDetail.afterEstimatedTotalAmount,
                                c2CPrice
                            )
                            var totalCoinBorrowCNY: Double? = null
                            var totalSetBorrowCNY: Double? = null
                            var totalCoinInterestCNY: Double? = null
                            var totalSetInterestCNY: Double? = null
                            val coinPairStatus = getPairStatusSync(
                                context,
                                ConstData.PairStatusType.SPOT,
                                leverDetail.pair
                            )
                            if (coinPairStatus != null) {
                                val pairCny = computeCoinPriceCNY(coinPairStatus, c2CPrice)
                                //                                        totalCoinCNY = leverDetail.totalAmount == null || pairCny == null ? null : leverDetail.totalAmount * pairCny;
                                totalCoinBorrowCNY =
                                    if (leverDetail.coinBorrow == null || pairCny == null) null else leverDetail.coinBorrow!!.multiply(
                                        BigDecimal(pairCny)
                                    ).toDouble()
                                totalCoinInterestCNY =
                                    if (leverDetail.coinInterest == null || pairCny == null) null else leverDetail.coinInterest!!.multiply(
                                        BigDecimal(pairCny)
                                    ).toDouble()
                            }
                            val setPairStatus = getPairStatusSync(
                                context,
                                ConstData.PairStatusType.SPOT,
                                set + "_USDT"
                            )
                            if (setPairStatus != null) { //                                        totalSetCNY = leverDetail.afterTotalAmount == null ? null : SocketDataContainer.computeTotalMoneyCNY(leverDetail.afterTotalAmount * setPairStatus.currentPrice, c2CPrice);
                                totalSetBorrowCNY =
                                    if (leverDetail.afterCoinBorrow == null) null else computeTotalMoneyCNY(
                                        leverDetail.afterCoinBorrow!!.multiply(
                                            BigDecimal(
                                                setPairStatus.currentPrice
                                            )
                                        ),
                                        c2CPrice
                                    )
                                totalSetInterestCNY =
                                    if (leverDetail.afterCoinInterest == null) null else computeTotalMoneyCNY(
                                        leverDetail.afterCoinInterest!!.multiply(
                                            BigDecimal(
                                                setPairStatus.currentPrice
                                            )
                                        ),
                                        c2CPrice
                                    )
                            }
                            leverDetail.totalCNY =
                                if (totalCoinCNY == null && totalSetCNY == null) null else
                                    (0.0 + (totalCoinCNY ?: 0.0) + (totalSetCNY ?: 0.0))
                            leverDetail.totalDebtCNY =
                                if (totalCoinBorrowCNY == null && totalSetBorrowCNY == null && totalCoinInterestCNY == null && totalSetInterestCNY == null) null else
                                    (0.0 + (totalCoinBorrowCNY
                                        ?: 0.0) + (totalSetBorrowCNY
                                        ?: 0.0) + (totalCoinInterestCNY
                                        ?: 0.0) + (totalSetInterestCNY ?: 0.0))
                            leverDetail.netAssetsCNY =
                                if (leverDetail.totalCNY == null || leverDetail.totalDebtCNY == null) null else leverDetail.totalCNY!! - leverDetail.totalDebtCNY!!
                        }
                    }
                    return@Function Observable.just(leverDetail)
                }
                if (notify.isOnError) {
                    Observable.just(leverDetail)
                } else Observable.empty()
            })
    }

    fun clearAll() {
        synchronized(pairDataList) { pairDataList.clear() }
        synchronized(allPairStatusMap) { allPairStatusMap.clear() }
        synchronized(pairDataSource) { pairDataSource.clear() }
        synchronized(dearPairMap) { dearPairMap.clear() }
        synchronized(depthDataList) { depthDataList.clear() }
        synchronized(dealList) { dealList.clear() }
        synchronized(leverDetailCache) { leverDetailCache.clear() }
    }


}