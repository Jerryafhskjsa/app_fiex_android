package com.black.base.util

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.util.SparseArray
import com.black.base.R
import com.black.base.api.*
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.SuccessObserver
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.util.FryingUtil.printError
import com.black.base.util.SharePreferencesUtil.getTextValue
import com.black.base.util.SharePreferencesUtil.setTextValue
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
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object SocketDataContainer {
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }
    private const val DATA_CACHE_OVER_TIME = 20 * 60 * 1000.toLong() //20分钟
    private const val C2C_PRICE = 1
    private const val TRADE_SET = 2
    private const val TRADE_PAIR = 3
    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long>()
    private val c2CPrice: C2CPrice? = null

    private val pairDataList: ArrayList<PairStatus?> = ArrayList()

    //所有现货交易对
    private val allPairStatusMap: MutableMap<String, PairStatus> = HashMap()
    //所有杠杆交易对
    private val allLeverPairMap: MutableMap<String, PairStatus> = HashMap()
    private val allPairStatusParentMap: MutableMap<String, List<PairStatus?>> = HashMap()

    private val pairDataSource: MutableMap<String, PairStatusNew> = HashMap()
    //自选交易对数据
    private val dearPairMap: MutableMap<String, Boolean?> = HashMap()
    private val pairObservers = ArrayList<Observer<ArrayList<PairStatus?>?>>()
    //委托
    private val orderDataList = ArrayList<QuotationOrderNew?>()
    private val orderObservers = ArrayList<Observer<Pair<String?, TradeOrderPairList?>>>()

    /***fiex***/
    private val orderDepthDataList = ArrayList<QuotationOrderNew?>()
    private val currentPairDeal = PairDeal()
    private val currentPairDealObservers = ArrayList<Observer<PairDeal?>?>()
    private val pairQuotationObservers = ArrayList<Observer<PairQuotation?>?>()
    /***fiex***/
    //成交
    const val DEAL_MAX_SIZE = 20
    private val dealList = ArrayList<QuotationDealNew>()
    private val dealObservers = ArrayList<Observer<Pair<String?, ArrayList<TradeOrder?>?>>>()
    //K线数据
//    private static final Map<String, ArrayList<KLineItem>> kLineDataSet = new HashMap<>();
//    private static final Map<String, String> kLineDataSet = new HashMap<>();
    private val kLineObservers = ArrayList<Observer<KLineItemListPair?>>()
    private val kLineAddObservers = ArrayList<Observer<KLineItemPair?>>()
    private val kLineAddMoreObservers = ArrayList<Observer<KLineItemListPair?>>()
    //用户信息有修改
    private val userInfoObservers = ArrayList<Observer<String?>>()
    //用户杠杆余额有修改
    private val userLeverObservers = ArrayList<Observer<String?>>()
    //用户杠杆详情有修改
    private val userLeverDetailObservers = ArrayList<Observer<WalletLeverDetail?>>()

    //热门币种变更
    private val hotPairObservers = ArrayList<Observer<ArrayList<String?>?>>()

    private var handlerThread: HandlerThread? = null
    private var pairHandler: Handler? = null
    //创建几个线程，让所有操作在这几个线程中进行
    private fun init() {
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        pairHandler = Handler(handlerThread?.looper)
    }

    fun getLastGetTime(type: Int): Long {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }

    //添加当前交易对deal观察者
    fun subscribePairDealObservable(observer: Observer<PairDeal?>?){
        if(observer == null){
            return
        }
        synchronized(currentPairDealObservers){
            if(!currentPairDealObservers.contains(observer)){
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
    fun subscribePairQuotationObservable(observer: Observer<PairQuotation?>?){
        if(observer == null){
            return
        }
        synchronized(pairQuotationObservers){
            if(!pairQuotationObservers.contains(observer)){
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

    //添加委托观察者
    fun subscribeOrderObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(orderObservers) {
            if (!orderObservers.contains(observer)) {
                orderObservers.add(observer)
            }
        }
    }

    //移除委托观察者
    fun removeOrderObservable(observer: Observer<Pair<String?, TradeOrderPairList?>>?) {
        if (observer == null) {
            return
        }
        synchronized(orderObservers) { orderObservers.remove(observer) }
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

    //添加K线观察者
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

    //移除K线观察者
    fun removeKLineAddObservable(observer: Observer<KLineItemPair?>?) {
        if (observer == null) {
            return
        }
        synchronized(kLineAddObservers) { kLineAddObservers.remove(observer) }
    }

    //添加K线观察者
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

    //移除K线观察者
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

    //添加用户杠杆余额观察者
    fun subscribeUserLeverObservable(observer: Observer<String?>?) {
        if (observer == null) {
            return
        }
        synchronized(userLeverObservers) {
            if (!userLeverObservers.contains(observer)) {
                userLeverObservers.add(observer)
            }
        }
    }

    //移除用户杠杆余额观察者
    fun removeUserLeverObservable(observer: Observer<String?>?) {
        if (observer == null) {
            return
        }
        synchronized(userLeverObservers) { userLeverObservers.remove(observer) }
    }

    //添加用户杠杆余额观察者
    fun subscribeUserLeverDetailObservable(observer: Observer<WalletLeverDetail?>?) {
        if (observer == null) {
            return
        }
        synchronized(userLeverDetailObservers) {
            if (!userLeverDetailObservers.contains(observer)) {
                userLeverDetailObservers.add(observer)
            }
        }
    }

    //移除用户杠杆余额观察者
    fun removeUserLeverDetailObservable(observer: Observer<WalletLeverDetail?>?) {
        if (observer == null) {
            return
        }
        synchronized(userLeverDetailObservers) { userLeverDetailObservers.remove(observer) }
    }

    //添加热门币种变更观察者
    fun subscribeHotPairObservable(observer: Observer<ArrayList<String?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(hotPairObservers) {
            if (!hotPairObservers.contains(observer)) {
                hotPairObservers.add(observer)
            }
        }
    }

    //移除热门币种变更观察者
    fun removeHotPairObservable(observer: Observer<ArrayList<String?>?>?) {
        if (observer == null) {
            return
        }
        synchronized(hotPairObservers) { hotPairObservers.remove(observer) }
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

    private fun computeTotalMoneyCNY(totalAmount: Number?, price: C2CPrice?): Double? {
        return if (totalAmount == null || price == null || price.sell == null) null else totalAmount.toDouble() * price.sell!!
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

    /**
     * 折算USDT CNY 价格
     *
     * @param usdtPrice
     * @param price
     * @return
     */
    fun computeUSDTPriceCNY(usdtPrice: Double?, price: C2CPrice?): Double? {
        return if (price?.sell == null || usdtPrice == null) null else usdtPrice * price.sell!!
    }

    /**
     * 折算USDT CNY 价格
     *
     * @param usdtPrice
     * @return
     */
    fun computeUSDTPriceCNY(usdtPrice: Double): Double? {
        return if (c2CPrice?.sell == null) {
            null
        } else usdtPrice * c2CPrice.sell!!
    }

    //缓存所有交易对数据
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
                onGetC2CPriceComplete(returnData)
            }

            private fun onGetC2CPriceComplete(price: C2CPrice?) {
                Observable.create<String> { e ->
                    e.onNext(updatePairStatusData(price))
                    e.onComplete()
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(object : SuccessObserver<String?>() {
                            override fun onSuccess(value: String?) {
                                synchronized(pairObservers) {
                                    for (observer in pairObservers) {
                                        observer.onNext(gson.fromJson(value, object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
                                    }
                                }
                            }
                        })
            }
        })
    }

    private fun updatePairStatusData(price: C2CPrice?): String {
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
                            result.add(pairStatus)
                        }
                    }
                }
            }
        }
        return gson.toJson(result)
    }

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
                    synchronized(allLeverPairMap!!) {
                        allPairStatusMap.clear()
                        allLeverPairMap.clear()
                        for (pairStatus in pairStatuses) {
                            pairStatus.pair?.let {
                                allPairStatusMap[it] = pairStatus
                                if (pairStatus.isLever) {
                                    allLeverPairMap!![it] = pairStatus
                                }
                            }
                        }
                    }
                }
            }
            ""
        }?.observeOn(AndroidSchedulers.mainThread())?.subscribe(object : SuccessObserver<String?>() {
            override fun onSuccess(s: String?) {
                computePairStatusCNY(context)
            }
        })
    }

    //更新现有交易对信息
    fun updatePairStatusData(context: Context?, handler: Handler?, dataSource: JSONArray?, isRemoveAll: Boolean) {
        if (context == null) {
            return
        }
        CommonUtil.postHandleTask(handler) {
            Observable.create { emitter: ObservableEmitter<String?> ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
                    val data = gson.fromJson<ArrayList<PairStatusNew>>(dataSource.toString(), object : TypeToken<ArrayList<PairStatusNew?>?>() {}.type)
                    synchronized(pairDataSource) {
                        if (isRemoveAll) {
                            pairDataSource.clear()
                        }
                        for (dataItem in data) {
                            val pair = dataItem.pair
                            pair?.let {
                                val oldItem = pairDataSource[pair]
                                if (oldItem == null) {
                                    pairDataSource[pair] = dataItem
                                } else {
                                    PairStatusNew.copyValues(oldItem, dataItem)
                                }
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

    /**
     * 查询自选交易对
     *
     * @param context
     */
    fun refreshDearPairs(context: Context?) {
        if (context == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.getDearPairs()
                ?.subscribeOn(Schedulers.io())
                ?.map { returnData: HttpRequestResultDataList<String?>? ->
                    synchronized(dearPairMap) {
                        //未登录时获取本地自选or
                        if (CookieUtil.getUserInfo(context) != null) {
                            dearPairMap.clear()
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                                for (pair in returnData.data!!) {
                                    pair?.let {
                                        dearPairMap[pair] = true
                                    }
                                }
                            }
                        } else {
                            updateDearPairMap()
                        }
                    }
                    ""
                }?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(value: String?) {
                        computePairStatusCNY(context)
                    }
                })
    }

    /**
     * 更新交易对是否添加已自选
     *
     * @param context
     * @param handler
     * @param dearPairs
     */
    fun updateDearPairs(context: Context?, handler: Handler?, dearPairs: Map<String, Boolean?>?, ifCache: Boolean) {
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
                            //                        computePairStatusCNY(context);
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
        updateDearPairMap()
        if (isDear) {
            dearPairMap[pair] = isDear
        } else {
            dearPairMap.remove(pair)
        }
        val pairsJson = Gson().toJson(dearPairMap)
        setTextValue(Commend.DEAR_PAIR_SP, pairsJson)
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
            val hasCachePairs = getTextValue(Commend.DEAR_PAIR_SP)
            val hashMap: Map<String, Boolean> = HashMap()
            return Gson().fromJson(hasCachePairs, hashMap.javaClass)
        }

    //主动拉取所有交易对信息，直接返回，不适用观察者模式
    fun getAllPairStatus(context: Context?, callback: Callback<ArrayList<PairStatus?>?>?) {
        if (context == null || callback == null) {
            return
        }
        synchronized(pairDataList!!) {
            val pairListString = gson.toJson(pairDataList)
            callback.callback(gson.fromJson<ArrayList<PairStatus?>>(pairListString, object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
        }
    }

    //主动拉取所有交易对信息，直接返回
    fun getAllPairStatus(context: Context?): Observable<ArrayList<PairStatus?>>? {
        if (context == null) {
            return null
        }
        synchronized(pairDataList) {
            val pairListString = gson.toJson(pairDataList)
            return Observable.just(gson.fromJson(pairListString, object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
        }
    }

    //主动拉取单个交易对信息，直接返回，不适用观察者模式
    fun getSinceOrderPairs(context: Context?, type: Int, maxSize: Int, callback: Callback<ArrayList<PairStatus?>?>?) {
        var size = maxSize
        if (context == null || callback == null) {
            return
        }
        size = if (size < 1) 1 else size
        synchronized(pairDataList) {
            val pairStatuses = ArrayList(pairDataList)
            Collections.sort(pairStatuses, if (type == 1) PairStatus.COMPARATOR_SINCE_UP else PairStatus.COMPARATOR_SINCE_DOWN)
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
            callback.callback(gson.fromJson<ArrayList<PairStatus?>>(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
        }
    }

    fun getSinceOrderPairs(context: Context?, type: Int, maxSize: Int): Observable<ArrayList<PairStatus?>>? {
        var size = maxSize
        if (context == null) {
            return null
        }
        size = if (size < 1) 1 else size
        synchronized(pairDataList) {
            val pairStatuses = ArrayList(pairDataList)
            Collections.sort(pairStatuses, if (type == 1) PairStatus.COMPARATOR_SINCE_UP else PairStatus.COMPARATOR_SINCE_DOWN)
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
            return Observable.just(gson.fromJson(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
        }
    }


    fun getHomeTickerTypePairs(context: Context?, type: Int,initPairStatus:ArrayList<PairStatus?>?): Observable<ArrayList<PairStatus?>>? {
            if (context == null) {
                return null
            }
            val result: MutableList<PairStatus?> = ArrayList()
            when(type){
                ConstData.HOME_TAB_HOT ->{
                    for (i in initPairStatus?.indices!!) {
                        val pairStatus = initPairStatus[i]
                        if (pairStatus?.hot != null && pairStatus.hot!!) {
                            result.add(pairStatus)
                        }
                    }
                }

                ConstData.HOME_TAB_RAISE_BAND ->{
                    Collections.sort(initPairStatus,PairStatus.COMPARATOR_SINCE_UP)
                    if (initPairStatus != null) {
                        result.addAll(initPairStatus)
                    }
                }
                ConstData.HOME_TAB_FAIL_BAND ->{
                    Collections.sort(initPairStatus,PairStatus.COMPARATOR_SINCE_DOWN)
                    if (initPairStatus != null) {
                        result.addAll(initPairStatus)
                    }
                }
                ConstData.HOME_TAB_VOLUME_BAND ->{
                    Collections.sort(initPairStatus,PairStatus.COMPARATOR_VOLUME_24)
                    if (initPairStatus != null) {
                        result.addAll(initPairStatus)
                    }
                }
            }
            return Observable.just(gson.fromJson(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
    }

    fun getPairStatus(context: Context?, pair: String?, callback: Callback<PairStatus?>?) {
        if (context == null || callback == null) {
            return
        }
        synchronized(allPairStatusMap) {
            val pairStatus = allPairStatusMap[pair]
            if (pairStatus == null) {
                callback.callback(null)
            } else {
                callback.callback(gson.fromJson(gson.toJson(pairStatus), PairStatus::class.java))
            }
        }
    }

    fun getPairStatusSync(context: Context?, pair: String?): PairStatus? {
        if (context == null) {
            return null
        }
        synchronized(allPairStatusMap) {
            val pairStatus = allPairStatusMap[pair]
            return if (pairStatus == null) {
                null
            } else {
                gson.fromJson(gson.toJson(pairStatus), PairStatus::class.java)
            }
        }
    }

    fun getPairStatusObservable(context: Context?, pair: String?): Observable<PairStatus>? {
        if (context == null) {
            return null
        }
        synchronized(allPairStatusMap) {
            val pairStatus = allPairStatusMap[pair]
            return if (pairStatus == null) {
                null
            } else {
                Observable.just(gson.fromJson(gson.toJson(pairStatus), PairStatus::class.java))
            }
        }
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

    fun getAllPair(context: Context?): ArrayList<String>? {
        if (context == null) {
            return null
        }
        synchronized(allPairStatusMap) {
            return if (allPairStatusMap == null || allPairStatusMap.isEmpty()) {
                null
            } else {
                ArrayList(allPairStatusMap.keys)
            }
        }
    }

    fun getAllLeverPair(context: Context?): ArrayList<String?>? {
        if (context == null) {
            return null
        }
        synchronized(allLeverPairMap) {
            return if (allLeverPairMap == null || allLeverPairMap.isEmpty()) {
                null
            } else {
                ArrayList(allLeverPairMap.keys)
            }
        }
    }

    fun getAllLeverPairStatus(context: Context?, callback: Callback<ArrayList<PairStatus?>?>?) {
        if (context == null || callback == null) {
            return
        }
        synchronized(allLeverPairMap) {
            if (allLeverPairMap == null || allLeverPairMap.isEmpty()) {
                callback.callback(null)
            } else {
                val result = ArrayList(allLeverPairMap.values)
                callback.callback(gson.fromJson<ArrayList<PairStatus?>>(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
            }
        }
    }

    fun getPairsWithSet(context: Context?, setName: String?, callback: Callback<ArrayList<PairStatus?>?>?) {
        if (context == null || callback == null || setName == null) {
            return
        }
        var symbolList = PairApiServiceHelper.getHomePagePairData()
        if (symbolList != null) {
            synchronized(symbolList) {
                val result = ArrayList<PairStatus?>()
                for (i in symbolList.indices) {
                    val pairStatus = symbolList[i]
                    if (context.getString(R.string.pair_collect) == setName) {
                        if (true == pairStatus?.is_dear) {
                            result.add(pairStatus)
                        }
                    } else {
                        if (TextUtils.equals(pairStatus?.setName, setName)) {
                            result.add(pairStatus)
                        }
                    }
                }
                callback.callback(gson.fromJson(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type))
            }
        }
    }

    fun getPairStatusListByKey(context: Context?, key: String?): ArrayList<PairStatus?>? {
        if (context == null) {
            return null
        }
        synchronized(pairDataList) {
            return if (key == null || key.trim { it <= ' ' }.isEmpty()) {
                gson.fromJson<ArrayList<PairStatus?>>(gson.toJson(pairDataList), object : TypeToken<ArrayList<PairStatus?>?>() {}.type)
            } else {
                val realKey = key.uppercase(Locale.getDefault())
                val result = ArrayList<PairStatus?>()
                for (pairStatus in pairDataList) {
                    val pair = pairStatus?.pair?.uppercase(Locale.getDefault())
                    if (pair != null && pair.contains(realKey)) {
                        result.add(pairStatus)
                    }
                }
                gson.fromJson<ArrayList<PairStatus?>>(gson.toJson(result), object : TypeToken<ArrayList<PairStatus?>?>() {}.type)
            }
        }
    }

    fun updateQuotationOrderNewData(context: Context?, handler: Handler?, currentPair: String?, dataSource: JSONArray?, isRemoveAll: Boolean) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<String>(ObservableOnSubscribe { emitter ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
                    val data: ArrayList<QuotationOrderNew?> = gson.fromJson<ArrayList<QuotationOrderNew?>>(dataSource.toString(), object : TypeToken<ArrayList<QuotationOrderNew?>?>() {}.type)
                    synchronized(orderDataList) {
                        if (isRemoveAll) {
                            orderDataList.clear()
                        }
                    }
                    //                            ArrayList<QuotationOrderNew>[] mergeData = mergeQuotationOrder(data);
                    val count = data.size
                    for (i in 0 until count) {
                        val quotationOrderNew = data[i]
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
                            quotationOrderNewReverse.d = if ("BID".equals(quotationOrderNew.d, ignoreCase = true)) "ASK" else "BID"
                            data.add(quotationOrderNewReverse)
                        }
                    }
                    var mergeData: Array<java.util.ArrayList<QuotationOrderNew?>?>?
                    synchronized(orderDataList) { mergeData = mergeQuotationOrder2(orderDataList, data) }
                    val askOrderNews: ArrayList<QuotationOrderNew?> = if (mergeData == null || mergeData!![0] == null) ArrayList() else mergeData!![0]!!
                    val bidOrderNews: ArrayList<QuotationOrderNew?> = if (mergeData == null || mergeData!![1] == null) ArrayList() else mergeData!![1]!!
                    synchronized(orderDataList) {
                        orderDataList.clear()
                        orderDataList.addAll(askOrderNews)
                        orderDataList.addAll(bidOrderNews)
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
                    //排序
                    //                    Collections.sort(bidOrders, TradeOrder.COMPARATOR_DOWN);
                    //                    Collections.sort(askOrders, TradeOrder.COMPARATOR_UP);
                    val result = TradeOrderPairList()
                    result.bidOrderList = bidOrders
                    result.askOrderList = askOrders
                    //                    int size = Math.max(bidOrders.size(), askOrders.size());
                    //                    for (int i = 0; i < size; i++) {
                    //                        TradeOrderPair tradeOrderPair = new TradeOrderPair();
                    //                        tradeOrderPair.order = i;
                    //                        tradeOrderPair.bidOrder = CommonUtil.getItemFromList(bidOrders, i);
                    //                        tradeOrderPair.askOrder = CommonUtil.getItemFromList(askOrders, i);
                    //                        result.add(tradeOrderPair);
                    //                    }
                    emitter.onNext(gson.toJson(result))
                }
            }).subscribeOn(Schedulers.trampoline())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(object : SuccessObserver<String?>() {
                        override fun onSuccess(s: String?) {
                            synchronized(orderObservers) {
                                for (observer in orderObservers) {
                                    observer.onNext(Pair(currentPair, gson.fromJson(s, object : TypeToken<TradeOrderPairList?>() {}.type)))
                                }
                            }
                        }
                    })
        }
    }

    fun updateQuotationOrderNewDataFiex(context: Context?, handler: Handler?, currentPair: String?, tradeOrderDepth: TradeOrderDepth?, isRemoveAll: Boolean) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<String>(ObservableOnSubscribe { emitter ->
                if (tradeOrderDepth == null) {
                    emitter.onComplete()
                } else {
                    var tradeOrderPairList  = parseOrderDepthData(tradeOrderDepth)
                    var orderDepthData = tradeOrderPairList?.let { parseOrderDepthToList(it) }
                    synchronized(orderDataList) {
                        if (isRemoveAll) {
                            orderDataList.clear()
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
                            quotationOrderNewReverse.d = if ("BID".equals(quotationOrderNew.d, ignoreCase = true)) "ASK" else "BID"
                            orderDepthData?.add(quotationOrderNewReverse)
                        }
                    }
                    var mergeData: Array<java.util.ArrayList<QuotationOrderNew?>?>?
                    synchronized(orderDataList) { mergeData = mergeQuotationOrder2(orderDataList, orderDepthData) }
                    val askOrderNews: ArrayList<QuotationOrderNew?> = if (mergeData == null || mergeData!![0] == null) ArrayList() else mergeData!![0]!!
                    val bidOrderNews: ArrayList<QuotationOrderNew?> = if (mergeData == null || mergeData!![1] == null) ArrayList() else mergeData!![1]!!
                    synchronized(orderDataList) {
                        orderDataList.clear()
                        orderDataList.addAll(askOrderNews)
                        orderDataList.addAll(bidOrderNews)
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
                    //排序
                    //                    Collections.sort(bidOrders, TradeOrder.COMPARATOR_DOWN);
                    //                    Collections.sort(askOrders, TradeOrder.COMPARATOR_UP);
                    val result = TradeOrderPairList()
                    result.bidOrderList = bidOrders
                    result.askOrderList = askOrders
                    //                    int size = Math.max(bidOrders.size(), askOrders.size());
                    //                    for (int i = 0; i < size; i++) {
                    //                        TradeOrderPair tradeOrderPair = new TradeOrderPair();
                    //                        tradeOrderPair.order = i;
                    //                        tradeOrderPair.bidOrder = CommonUtil.getItemFromList(bidOrders, i);
                    //                        tradeOrderPair.askOrder = CommonUtil.getItemFromList(askOrders, i);
                    //                        result.add(tradeOrderPair);
                    //                    }
                    emitter.onNext(gson.toJson(result))
                }
            }).subscribeOn(Schedulers.trampoline())
                .observeOn(Schedulers.trampoline())
                .subscribe(object : SuccessObserver<String?>() {
                    override fun onSuccess(s: String?) {
                        synchronized(orderObservers) {
                            for (observer in orderObservers) {
                                observer.onNext(Pair(currentPair, gson.fromJson(s, object : TypeToken<TradeOrderPairList?>() {}.type)))
                            }
                        }
                    }
                })
        }
    }


    /**
     * 请求得到的数据转换成TradeOrderPairList
     */
    fun parseOrderDepthData(depth: TradeOrderDepth):TradeOrderPairList?{
        var pairListData = TradeOrderPairList()
        var askOrderList = ArrayList<TradeOrder?>()
        var bidOrderList = ArrayList<TradeOrder?>()
        var result = depth
        var pair = result?.s
        var bidArray = result?.b
        var askArray = result?.a
        if (bidArray != null) {
            for(bidItem in bidArray){
                var tradeOrder = TradeOrder()
                tradeOrder.price = bidItem[0]
                tradeOrder.priceString = bidItem[0].toString()
                tradeOrder.orderType = "BID"
                tradeOrder.exchangeAmount = bidItem[1]!!
                tradeOrder.direction = "BID"
                tradeOrder.pair = pair
                bidOrderList.add(tradeOrder)
            }
            pairListData!!.bidOrderList = bidOrderList
        }
        if (askArray != null) {
            for(askItem in askArray){
                var tradeOrder = TradeOrder()
                tradeOrder.price = askItem[0]
                tradeOrder.priceString = askItem[0].toString()
                tradeOrder.orderType = "ASK"
                tradeOrder.exchangeAmount = askItem[1]!!
                tradeOrder.direction = "ASK"
                tradeOrder.pair = pair
                askOrderList.add(tradeOrder)
            }
            pairListData!!.askOrderList = askOrderList
        }
        return pairListData
    }

    fun parseOrderDepthToList(tradeOrderPairList: TradeOrderPairList):ArrayList<QuotationOrderNew?>{
        var newQuotationOrderList = ArrayList<QuotationOrderNew?>()
        var bidList = tradeOrderPairList.bidOrderList
        var askList = tradeOrderPairList.askOrderList
        if (bidList != null) {
            for(bid in bidList){
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
            for(ask in askList){
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
        val returnData: Array<ArrayList<QuotationOrderNew>?> = arrayOfNulls<ArrayList<QuotationOrderNew>?>(2)
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
    fun mergeQuotationOrder2(oldData: List<QuotationOrderNew?>, newData: List<QuotationOrderNew?>?): Array<ArrayList<QuotationOrderNew?>?>? {
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
    fun getOrderList(context: Context?, callback: Callback<TradeOrderPairList?>?) {
        if (context == null || callback == null) {
            return
        }
        synchronized(orderDataList) {
            try {
                val mergeData = mergeQuotationOrder(orderDataList)
                val askOrderNews = if (mergeData == null || mergeData[0] == null) ArrayList() else mergeData[0]!!
                val bidOrderNews = if (mergeData == null || mergeData[1] == null) ArrayList() else mergeData[1]!!
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

    //主动拉去挂单数据，直接回调
    fun getOrderListFiex(context: Context?, depthDataList:ArrayList<QuotationOrderNew?>?, callback: Callback<TradeOrderPairList?>?) {
        if (context == null || callback == null) {
            return
        }
            try {
                val mergeData = mergeQuotationOrder(depthDataList)
                val askOrderNews = if (mergeData == null || mergeData[0] == null) ArrayList() else mergeData[0]!!
                val bidOrderNews = if (mergeData == null || mergeData[1] == null) ArrayList() else mergeData[1]!!
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

    fun updateQuotationDealNewData(context: Context?, handler: Handler?, currentPair: String?, dataSource: JSONArray?, removeAll: Boolean) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<String> { emitter ->
                if (dataSource == null) {
                    emitter.onComplete()
                } else {
                    val data = gson.fromJson<ArrayList<QuotationDealNew>>(dataSource.toString(), object : TypeToken<ArrayList<QuotationDealNew?>?>() {}.type)
                    val result = ArrayList<TradeOrder>()
                    synchronized(dealList) {
                        if (removeAll) {
                            dealList.clear()
                        }
                        dealList.addAll(data)
                        //移除错误交易对的数据
                        for (i in dealList.indices.reversed()) {
                            val dealNew = dealList[i]
                            if (!TextUtils.equals(currentPair, dealNew.pair)) {
                                dealList.removeAt(i)
                            }
                        }
                        if (dealList.size > DEAL_MAX_SIZE) {
                            //超过上限之后，移除后面的是数据
                            Collections.sort(dealList, QuotationDealNew.COMPARATOR)
                            CommonUtil.removeListItem(dealList, DEAL_MAX_SIZE, dealList.size - 1)
                        }
                        for (dealNew in dealList) {
                            result.add(dealNew.toTradeOrder())
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
                                    observer.onNext(Pair(currentPair, gson.fromJson(s, object : TypeToken<ArrayList<TradeOrder?>?>() {}.type)))
                                }
                            }
                        }
                    })
        }
    }

    //主动拉取成交数据，直接回调
    fun getAllQuotationDeal(context: Context?, pair: String?, callback: Callback<ArrayList<TradeOrder?>?>?) {
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
                    if (TextUtils.equals(pair, dealNew.pair)) {
                        result.add(dealNew.toTradeOrder())
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

    fun saveKLineDataAll(currentPair: String?, handler: Handler?, data: JSONObject) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<Pair<String?, String?>> { emitter ->
                //Log.e("KLineDataAll", "data get start");
                val kLineId = data.optString("no")
                //                        ArrayList<KLineItem> list = listData == null ? new ArrayList<KLineItem>() :
                //                                (ArrayList<KLineItem>) gson.fromJson(listData.toString(),
                //                                        new TypeToken<ArrayList<KLineItem>>() {
                //                                        }.getType());
                //                        if (kLineId != null && list != null) {
                //                            kLineDataSet.put(kLineId, list);
                //                        }
                if (kLineId != null) {
                    var listData = data.optJSONArray("list")
                    listData = listData ?: JSONArray()
                    emitter.onNext(Pair(kLineId, listData.toString()))
                    //Log.e("KLineDataAll", "data get end");
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(object : SuccessObserver<Pair<String?, String?>>() {
                        override fun onSuccess(pair: Pair<String?, String?>) {
                            synchronized(kLineObservers) {
                                for (observer in kLineObservers) {
                                    //Log.e("KLineDataAll", "data parse start");
                                    observer.onNext(KLineItemListPair(currentPair, pair.first, gson.fromJson(pair.second, object : TypeToken<ArrayList<KLineItem?>?>() {}.type)))
                                    //Log.e("KLineDataAll", "data parse get");
                                }
                            }
                        }
                    })
        }
    }

    fun addKLineData(currentPair: String?, handler: Handler?, kLineId: String?, kLineItem: KLineItem?) {
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
                                    observer.onNext(KLineItemPair(currentPair, kLineId, gson.fromJson(s, object : TypeToken<KLineItem?>() {}.type)))
                                }
                            }
                        }
                    })
        }
    }

    fun addKLineDataList(currentPair: String?, handler: Handler?, data: JSONObject) {
        CommonUtil.postHandleTask(handler) {
            Observable.create<Pair<String?, String?>> { emitter ->
                val kLineId = data.optString("no")
                if (kLineId != null) {
                    var listData = data.optJSONArray("list")
                    listData = listData ?: JSONArray()
                    emitter.onNext(Pair(kLineId, listData.toString()))
                } else {
                    emitter.onComplete()
                }
            }.subscribeOn(Schedulers.trampoline())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(object : SuccessObserver<Pair<String?, String?>>() {
                        override fun onSuccess(pair: Pair<String?, String?>) {
                            synchronized(kLineAddMoreObservers) {
                                for (observer in kLineAddMoreObservers) {
                                    observer.onNext(KLineItemListPair(currentPair, pair.first, gson.fromJson(pair.second, object : TypeToken<ArrayList<KLineItem?>?>() {}.type)))
                                }
                            }
                        }
                    })
        }
    }

    fun removeKLineData(kLineId: String?) { //        synchronized (kLineDataSet) {
//            kLineDataSet.remove(kLineId);
//        }
    }

    fun clearKLineData() { //        synchronized (kLineDataSet) {
//            kLineDataSet.clear();
//        }
    }

    fun onUserInfoChanged() {
        synchronized(userInfoObservers) {
            for (observer in userInfoObservers) {
                observer.onNext("")
            }
        }
    }

    fun onUserLeverChanged() {
        synchronized(userLeverObservers) {
            for (observer in userLeverObservers) {
                observer.onNext("")
            }
        }
    }

    fun onUserOrderChanged() {
        synchronized(userInfoObservers) {
            for (observer in userInfoObservers) {
                observer.onNext("")
            }
        }
    }

    //笑傲江湖门派成员变更
    fun onFactionMemberUpdate(factionId: Long) {
    }

    //笑傲江湖门派变更
    fun onFactionUpdate(factionId: JSONObject) {
    }

    //笑傲江湖门派成员变更
    fun onFactionOwnerUpdate(factionId: Long) {
    }

    private val leverDetailCache: MutableMap<String, WalletLeverDetail?> = HashMap()
    //杠杆资产详情变化
    fun onWalletLeverDetailUpdate(json: JSONObject?, handler: Handler?) {
        CommonUtil.postHandleTask(handler) {
            Observable.create { emitter: ObservableEmitter<WalletLeverDetail?> ->
                if (json == null) {
                    emitter.onComplete()
                } else {
                    synchronized(leverDetailCache) {
                        var leverDetail: WalletLeverDetail? = null
                        try {
                            leverDetail = gson.fromJson<WalletLeverDetail>(json.toString(), object : TypeToken<WalletLeverDetail?>() {}.type)
                        } catch (e: Exception) {
                        }
                        if (leverDetail != null && !TextUtils.isEmpty(leverDetail.pair)) {
                            leverDetailCache[leverDetail.pair!!] = leverDetail
                            emitter.onNext(leverDetail)
                        } else {
                            emitter.onComplete()
                        }
                    }
                }
            }.subscribeOn(Schedulers.trampoline())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(object : SuccessObserver<WalletLeverDetail?>() {
                        override fun onSuccess(detail: WalletLeverDetail?) {
                            if (detail == null) {
                                return
                            }
                            synchronized(userLeverDetailObservers) {
                                for (observer in userLeverDetailObservers) {
                                    observer.onNext(detail)
                                }
                            }
                        }
                    })
        }
    }

    fun updateWalletLeverDetail(context: Context?, handler: Handler?, pair: String?) {
        CommonUtil.postHandleTask(handler) {
            getWalletLeverDetail(context, pair, true)
                    ?.subscribeOn(Schedulers.trampoline())
                    ?.observeOn(Schedulers.trampoline())
                    ?.subscribe(object : SuccessObserver<WalletLeverDetail?>() {
                        override fun onSuccess(detail: WalletLeverDetail?) {
                            detail ?: return
                            synchronized(userLeverDetailObservers) {
                                for (observer in userLeverDetailObservers) {
                                    observer.onNext(detail)
                                }
                            }
                        }
                    })
        }
    }

    fun getWalletLeverDetail(context: Context?, pair: String?, force: Boolean): Observable<WalletLeverDetail>? {
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
    fun getWalletLeverDetailTotal(context: Context?, pair: String?, force: Boolean): Observable<WalletLeverDetail>? {
        val observable = getWalletLeverDetail(context, pair, force) ?: return null
        return observable.flatMap { leverDetail: WalletLeverDetail? -> leverDetail?.let { computeWalletLeverDetailTotal(context, it) } }
    }

    //计算杠杆资产详情总量
    fun computeWalletLeverDetailTotal(context: Context?, leverDetail: WalletLeverDetail): Observable<WalletLeverDetail>? {
        return C2CApiServiceHelper.getC2CPrice(context)
                ?.materialize()
                ?.flatMap(Function<Notification<C2CPrice?>, ObservableSource<WalletLeverDetail>> { notify ->
                    if (notify.isOnNext) {
                        val c2CPrice = notify.value
                        val coinType = getPairCoinName(leverDetail.pair)
                        val set = getPairSetName(leverDetail.pair)
                        if (set != null && coinType != null) {
                            if (set.equals("USDT", ignoreCase = true)) {
                                val pairStatus = getPairStatusSync(context, leverDetail.pair)
                                if (pairStatus != null) {
                                    val totalCoinCNY = computeTotalMoneyCNY(leverDetail.estimatedTotalAmount, c2CPrice)
                                    val totalSetCNY = computeTotalMoneyCNY(leverDetail.afterEstimatedTotalAmount, c2CPrice)
                                    leverDetail.totalCNY = if (totalCoinCNY == null && totalSetCNY == null) null else
                                        (0.0 + (totalCoinCNY ?: 0.0) + (totalSetCNY ?: 0.0))
                                    val totalCoinBorrowCNY = if (leverDetail.coinBorrow == null) null else computeTotalMoneyCNY(leverDetail.coinBorrow!!.multiply(BigDecimal(pairStatus.currentPrice)), c2CPrice)
                                    val totalSetBorrowCNY = computeTotalMoneyCNY(leverDetail.afterCoinBorrow, c2CPrice)
                                    val totalCoinInterestCNY = if (leverDetail.coinInterest == null) null else computeTotalMoneyCNY(leverDetail.coinInterest!!.multiply(BigDecimal(pairStatus.currentPrice)), c2CPrice)
                                    val totalSetInterestCNY = computeTotalMoneyCNY(leverDetail.afterCoinInterest, c2CPrice)
                                    leverDetail.totalDebtCNY = if (totalCoinBorrowCNY == null && totalSetBorrowCNY == null && totalCoinInterestCNY == null && totalSetInterestCNY == null) null else
                                        (0.0 + (totalCoinBorrowCNY ?: 0.0) + (totalSetBorrowCNY
                                                ?: 0.0) + (totalCoinInterestCNY
                                                ?: 0.0) + (totalSetInterestCNY ?: 0.0))
                                    leverDetail.netAssetsCNY = if (leverDetail.totalCNY == null || leverDetail.totalDebtCNY == null) null else leverDetail.totalCNY!! - leverDetail.totalDebtCNY!!
                                }
                            } else {
                                val totalCoinCNY = computeTotalMoneyCNY(leverDetail.estimatedTotalAmount, c2CPrice)
                                val totalSetCNY = computeTotalMoneyCNY(leverDetail.afterEstimatedTotalAmount, c2CPrice)
                                var totalCoinBorrowCNY: Double? = null
                                var totalSetBorrowCNY: Double? = null
                                var totalCoinInterestCNY: Double? = null
                                var totalSetInterestCNY: Double? = null
                                val coinPairStatus = getPairStatusSync(context, leverDetail.pair)
                                if (coinPairStatus != null) {
                                    val pairCny = computeCoinPriceCNY(coinPairStatus, c2CPrice)
                                    //                                        totalCoinCNY = leverDetail.totalAmount == null || pairCny == null ? null : leverDetail.totalAmount * pairCny;
                                    totalCoinBorrowCNY = if (leverDetail.coinBorrow == null || pairCny == null) null else leverDetail.coinBorrow!!.multiply(BigDecimal(pairCny)).toDouble()
                                    totalCoinInterestCNY = if (leverDetail.coinInterest == null || pairCny == null) null else leverDetail.coinInterest!!.multiply(BigDecimal(pairCny)).toDouble()
                                }
                                val setPairStatus = getPairStatusSync(context, set + "_USDT")
                                if (setPairStatus != null) { //                                        totalSetCNY = leverDetail.afterTotalAmount == null ? null : SocketDataContainer.computeTotalMoneyCNY(leverDetail.afterTotalAmount * setPairStatus.currentPrice, c2CPrice);
                                    totalSetBorrowCNY = if (leverDetail.afterCoinBorrow == null) null else computeTotalMoneyCNY(leverDetail.afterCoinBorrow!!.multiply(BigDecimal(setPairStatus.currentPrice)), c2CPrice)
                                    totalSetInterestCNY = if (leverDetail.afterCoinInterest == null) null else computeTotalMoneyCNY(leverDetail.afterCoinInterest!!.multiply(BigDecimal(setPairStatus.currentPrice)), c2CPrice)
                                }
                                leverDetail.totalCNY = if (totalCoinCNY == null && totalSetCNY == null) null else
                                    (0.0 + (totalCoinCNY ?: 0.0) + (totalSetCNY ?: 0.0))
                                leverDetail.totalDebtCNY = if (totalCoinBorrowCNY == null && totalSetBorrowCNY == null && totalCoinInterestCNY == null && totalSetInterestCNY == null) null else
                                    (0.0 + (totalCoinBorrowCNY
                                            ?: 0.0) + (totalSetBorrowCNY
                                            ?: 0.0) + (totalCoinInterestCNY
                                            ?: 0.0) + (totalSetInterestCNY ?: 0.0))
                                leverDetail.netAssetsCNY = if (leverDetail.totalCNY == null || leverDetail.totalDebtCNY == null) null else leverDetail.totalCNY!! - leverDetail.totalDebtCNY!!
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
        synchronized(pairObservers) {}
        synchronized(pairDataList) { pairDataList.clear() }
        synchronized(allLeverPairMap) { allLeverPairMap.clear() }
        synchronized(orderDataList) { orderDataList.clear() }
        synchronized(dealList) { dealList.clear() }
        //        synchronized (kLineDataSet) {
//            kLineDataSet.clear();
//        }
        synchronized(leverDetailCache) { leverDetailCache.clear() }
    }

    fun onCoinInfoUpdate(context: Context?) {
        WalletApiServiceHelper.getCoinInfoConfigAndCache(context, null)
    }

    fun onCoinPriceUpdate(context: Context?) {
        C2CApiServiceHelper.getC2CPrice(context, true, object : Callback<C2CPrice?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: C2CPrice?) {}
        })
    }

    fun onPairUpdate(context: Context?) {
        initAllPairStatusData(context)
    }

    fun onC2cCoinTypeUpdate(context: Context?) {}
    fun onHotPairUpdate(context: Context?) {
        PairApiServiceHelper.getHotPairAndCache(context, object : Callback<HttpRequestResultDataList<String?>?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    synchronized(hotPairObservers) {
                        for (observer in hotPairObservers) {
                            observer.onNext(returnData.data!!)
                        }
                    }
                }
            }
        })
    }
}