package com.black.frying.viewmodel

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.black.base.api.*
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.*
import com.black.base.model.clutter.NoticeHome.NoticeHomeItem
import com.black.base.model.community.ChatRoomEnable
import com.black.base.model.socket.PairStatus
import com.black.base.model.wallet.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.net.HttpRequestResult
import com.black.util.Callback
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function

class MainViewModel(context: Context) : BaseViewModel<Any>(context) {
    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var onMainModelListener: OnMainModelListener? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private var hotPairObserver: Observer<java.util.ArrayList<String?>?>? = createHotPairObserver()
    private var userLeverObserver: Observer<String?>? = createUserLeverObserver()

    private var noticeList: java.util.ArrayList<NoticeHomeItem?>? = null
    private var tickerList: ArrayList<HomeTickers>? = null

    constructor(context: Context, onMainModelListener: OnMainModelListener?) : this(context) {
        this.onMainModelListener = onMainModelListener
    }

    override fun onResume() {
        super.onResume()
        initHandler()
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        if (userLeverObserver == null) {
            userLeverObserver = createUserLeverObserver()
        }
        SocketDataContainer.subscribeUserLeverObservable(userLeverObserver)
        if (hotPairObserver == null) {
            hotPairObserver = createHotPairObserver()
        }
        SocketDataContainer.subscribeHotPairObservable(hotPairObserver)
        val bundle = Bundle()
        bundle.putString(SocketUtil.WS_TYPE, SocketUtil.WS_USER)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ADD_SOCKET_LISTENER,bundle)
        val bundle1 = Bundle()
        bundle1.putString(SocketUtil.WS_TYPE, SocketUtil.WS_TICKETS)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ADD_SOCKET_LISTENER,bundle1)
        val bundle2 = Bundle()
        bundle2.putString(SocketUtil.WS_TYPE, SocketUtil.WS_SUBSTATUS)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ADD_SOCKET_LISTENER,bundle2)

//        getHotPairs()
        getSymbolList()
//        getHomeTicker()
//        getHomeKline()
        if (noticeList == null) {
            //获取公告信息
            getNoticeInfo()
        }
        getCoinlistConfig()
    }

    override fun onStop() {
        super.onStop()
        val bundle = Bundle()
        bundle.putString(SocketUtil.WS_TYPE, SocketUtil.WS_USER)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,bundle)
        val bundle1 = Bundle()
        bundle1.putString(SocketUtil.WS_TYPE, SocketUtil.WS_TICKETS)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,bundle1)
        val bundle2 = Bundle()
        bundle2.putString(SocketUtil.WS_TYPE, SocketUtil.WS_SUBSTATUS)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,bundle2)

        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
        }
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
        if (hotPairObserver != null) {
            SocketDataContainer.removeHotPairObservable(hotPairObserver)
        }
        if (userLeverObserver != null) {
            SocketDataContainer.removeUserLeverObservable(userLeverObserver)
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
            handlerThread = null
        }
    }

    private fun initHandler() {
        if (socketHandler == null || socketHandler?.looper == null) {
            handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
            handlerThread?.start()
            socketHandler = Handler(handlerThread?.looper)
        }
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?>? {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                onMainModelListener?.onPairStatusDataChanged(
                        Observable.just(value!!)
                                .compose(RxJavaHelper.observeOnMainThread()))
            }
        }
    }

    private fun createUserInfoObserver(): Observer<String?>? {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onMainModelListener?.onUserInfoChanged()
            }
        }
    }

    private fun createUserLeverObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onMainModelListener?.onUserInfoChanged()
            }
        }
    }

    private fun createHotPairObserver(): Observer<java.util.ArrayList<String?>?>? {
        return object : SuccessObserver<java.util.ArrayList<String?>?>() {
            override fun onSuccess(value: java.util.ArrayList<String?>?) {
                onMainModelListener?.onHotPairs(Observable.just(value!!)
                        .flatMap {
                            val result: HttpRequestResultDataList<String?>? = HttpRequestResultDataList()
                            Observable.just(result)
                        }
                        .compose(RxJavaHelper.observeOnMainThread()))
            }
        }
    }

    //获取所有交易对数据
    fun getAllPairStatus() {
        onMainModelListener?.onPairStatusDataChanged(SocketDataContainer.getAllPairStatus(context!!)
                ?.subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
                ?.observeOn(AndroidSchedulers.mainThread()))
    }

    //计算总资产USDT
    fun computeTotalAmount() {
        if (CookieUtil.getUserInfo(context!!) == null) {
            onMainModelListener?.onMoney(null)
        } else {
            val callback = onMainModelListener?.getMoneyCallback()
            callback?.let {
                WalletApiServiceHelper.getWalletAll(context, HttpCallbackSimple(context, false, object : Callback<HttpRequestResultData<WalletConfig?>?>() {
                    override fun callback(returnData: HttpRequestResultData<WalletConfig?>?) {
                        var totalAmount: Double? = null
                        if (returnData?.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                            //循环累加
                            var total = 0.0
                            val normalWalletList: ArrayList<Wallet?>? = returnData.data?.userCoinAccountVO
                            normalWalletList?.run {
                                for (wallet in normalWalletList) {
                                    total += wallet?.estimatedTotalAmount ?: 0.toDouble()
                                }
                            }
                            val leverWalletList: ArrayList<WalletLever?>? = returnData.data?.userCoinAccountLeverVO
                            leverWalletList?.run {
                                for (wallet in leverWalletList) {
                                    total += wallet?.estimatedTotalAmount ?: 0.toDouble()
                                }
                            }
                            totalAmount = total
                        }
                        C2CApiServiceHelper.getC2CPrice(context!!)
                                ?.materialize()
                                ?.flatMap(object : Function<Notification<C2CPrice?>, Observable<Money>> {
                                    override fun apply(notify: Notification<C2CPrice?>): Observable<Money> {
                                        val money = Money()
                                        money.usdt = totalAmount
                                        if (notify.isOnNext) {
                                            val cny = SocketDataContainer.computeTotalMoneyCNY(totalAmount, notify.value)
                                            money.cny = cny
                                            return Observable.just(money)
                                        }
                                        if (notify.isOnError) {
                                            return Observable.just(money)
                                        }
                                        return Observable.empty()
                                    }
                                })
                                ?.compose(RxJavaHelper.observeOnMainThread())
                                ?.subscribe(object : SuccessObserver<Money?>() {
                                    override fun onSuccess(value: Money?) {
                                        callback.callback(value)
                                    }
                                })
                    }

                    override fun error(type: Int, error: Any?) {
                        callback.error(type, error)
                    }

                }))
            }
//            onMainModelListener?.onMoney(ApiManager.build(context!!).getService(WalletApiService::class.java)
//                    .getWallet(null)
//                    .flatMap(object : Function<HttpRequestResultData<WalletConfig?>?, Observable<Double?>> {
//                        override fun apply(returnData: HttpRequestResultData<WalletConfig?>): Observable<Double?> {
//                            if (returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
//                                //循环累加
//                                var total = 0.0
//                                val normalWalletList: ArrayList<Wallet?>? = returnData.data?.userCoinAccountVO
//                                normalWalletList?.run {
//                                    for (wallet in normalWalletList) {
//                                        total += wallet?.estimatedTotalAmount ?: 0.toDouble()
//                                    }
//                                }
//                                val leverWalletList: ArrayList<WalletLever?>? = returnData.data?.userCoinAccountLeverVO
//                                leverWalletList?.run {
//                                    for (wallet in leverWalletList) {
//                                        total += wallet?.estimatedTotalAmount ?: 0.toDouble()
//                                    }
//                                }
//                                return Observable.just(total)
//                            } else {
//                                return Observable.just(null)
//                            }
//                        }
//
//                    })
//                    .flatMap { totalAmount: Double? ->
//                        C2CApiServiceHelper.getC2CPrice(context!!)
//                                .materialize()
//                                .flatMap(object : Function<Notification<C2CPrice>, Observable<Money>> {
//
//                                    override fun apply(notify: Notification<C2CPrice>): Observable<Money> {
//                                        val money = Money()
//                                        money.usdt = totalAmount
//                                        if (notify.isOnNext) {
//                                            val cny = SocketDataContainer.computeTotalMoneyCNY(totalAmount, notify.value)
//                                            money.cny = cny
//                                            return Observable.just(money)
//                                        }
//                                        if (notify.isOnError) {
//                                            return Observable.just(money)
//                                        }
//                                        return Observable.empty()
//                                    }
//                                })
//                    }
//                    .compose(RxJavaHelper.observeOnMainThread()))
        }
    }

    //获取涨跌幅数据
    fun getRiseFallData(type: Int) {
        Log.d("MainViewModel","getRiseFallData->type = "+type)
        Log.d("MainViewModel","getRiseFallData->socketHandler = "+socketHandler)
        Log.d("MainViewModel","getRiseFallData->socketHandler.looper = "+socketHandler?.looper)
        Log.d("MainViewModel","getRiseFallData->onMainModelListener = "+onMainModelListener)
        if (socketHandler == null || socketHandler?.looper == null || onMainModelListener == null) {
            return
        }
        onMainModelListener?.onRiseFallDataChanged(SocketDataContainer.getSinceOrderPairs(context!!, type, 6)
                ?.subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
                ?.observeOn(AndroidSchedulers.mainThread()))
    }



    fun getHomeSybolListData(type: Int) {
        onMainModelListener?.onHomeTabDataChanged(SocketDataContainer.getHomeTickerTypePairs(context!!, type,PairApiServiceHelper.getHomePagePairData())
            ?.subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
            ?.observeOn(AndroidSchedulers.mainThread()))
    }

    //获取公告信息
    fun getNoticeInfo() {
        onMainModelListener?.onNoticeList(ApiManager.build(context!!)
                .getService(CommonApiService::class.java)
//                ?.getNoticeHome(FryingUtil.getLanguageKey(context!!), 6, 1)
            ?.getNoticeHome("zh-tw", 6, 1)
                ?.flatMap { noticeHomeResult ->
                    if (noticeHomeResult.articles != null && noticeHomeResult.articles!!.isNotEmpty()) {
                        noticeList = noticeHomeResult.articles
                    }
                    Observable.just(noticeHomeResult)
                }
                ?.compose(RxJavaHelper.observeOnMainThread()))
    }


    //获取所有bannerlist
    fun getAllBanner() {
        // 1-中，2-日，3-韩，4-英，5-俄
        val context: Context = context ?: return
        val language = LanguageUtil.getLanguageSetting(context)
        onMainModelListener?.onHeadBanner(ApiManager.build(context).getService(CommonApiService::class.java)
                ?.getHomePageMainBannerList(if (language != null && language.languageCode == 4) "4" else "1", "0", "1")
                ?.compose(RxJavaHelper.observeOnMainThread()))
    }

    //获取中间banner
    fun getMiddleBanner() {
        // 1-中，2-日，3-韩，4-英，5-俄
        val context: Context = context ?: return
        val language = LanguageUtil.getLanguageSetting(context)
        onMainModelListener?.onMiddleBanner(ApiManager.build(context).getService(CommonApiService::class.java)
                ?.getHomePageMainBannerList(if (language != null && language.languageCode == 4) "4" else "1", "0", "3")
                ?.compose(RxJavaHelper.observeOnMainThread()))
    }

    fun getHotPairs() {
        onMainModelListener?.onHotPairs(PairApiServiceHelper.getHotPair(context!!))
    }

    /**
     * 获取交易对的详细数据
     */
    fun getHomeTicker(){
        onMainModelListener?.onHomeTickers(PairApiServiceHelper.getHomeTickers((context!!)))
    }

    /**
     * 获取首页最下面的所有币种列表
     */
    fun getSymbolList(){
        onMainModelListener?.onHomeSymbolList(PairApiServiceHelper.getSymbolList((context!!)))
    }

    /**
     * 获取首页相关交易对的k线数据
     */
    fun getHomeKline(){
        onMainModelListener?.onHomeKLine(PairApiServiceHelper.getHomeKline((context!!)))
    }

    /**
     * 获取币种配置
     */
    private fun getCoinlistConfig(){
        WalletApiServiceHelper.getCoinInfoList(context!!, object :Callback<ArrayList<CoinInfoType?>?>(){
            override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                onMainModelListener?.onCoinlistConfig(returnData)
            }
            override fun error(type: Int, error: Any?) {
            }
        })
    }



    fun checkIntoMainChat(): Observable<ChatRoomEnable>? {
        return ApiManager.build(context!!).getService(UserApiService::class.java)
                ?.checkMainChatEnable()
                ?.flatMap { returnData: HttpRequestResultString? ->
                    val chatRoomEnable = ChatRoomEnable()
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        chatRoomEnable.enable = true
                    } else {
                        chatRoomEnable.enable = false
                        chatRoomEnable.message = if (returnData?.msg == null) {
                            "null"
                        } else returnData.msg
                    }
                    Observable.just(chatRoomEnable)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
    }

    interface OnMainModelListener {
        fun onPairStatusDataChanged(observable: Observable<ArrayList<PairStatus?>?>?)
        fun onRiseFallDataChanged(observable: Observable<ArrayList<PairStatus?>?>?)
        fun onHomeTabDataChanged(observable: Observable<ArrayList<PairStatus?>?>?)
        fun onNoticeList(observable: Observable<NoticeHome?>?)
        fun onHeadBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?)
        fun onMiddleBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?)
        fun onHotPairs(observable: Observable<HttpRequestResultDataList<String?>?>?)
        fun onUserInfoChanged()
        fun onMoney(observable: Observable<Money?>?)
        fun getMoneyCallback(): Callback<Money?>?
        fun onHomeSymbolList(observable: Observable<HttpRequestResultDataList<HomeSymbolList?>?>?)
        fun onHomeTickers(observable: Observable<HttpRequestResultDataList<HomeTickers?>?>?)
        fun onHomeKLine(observable: Observable<HttpRequestResultDataList<HomeTickersKline?>?>?)
        fun onCoinlistConfig(coinConfigList: ArrayList<CoinInfoType?>?)
    }
}