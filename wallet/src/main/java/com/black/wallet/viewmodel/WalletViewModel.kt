package com.black.wallet.viewmodel

import android.content.Context
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.Money
import com.black.base.model.SuccessObserver
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.socket.PairStatus
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletConfig
import com.black.base.model.wallet.WalletLever
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RxJavaHelper
import com.black.base.util.SocketDataContainer
import com.black.base.viewmodel.BaseViewModel
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.wallet.util.WalletComparator
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.functions.Function
import java.util.*
import kotlin.collections.ArrayList

class WalletViewModel(context: Context) : BaseViewModel<Any>(context) {
    companion object {
        const val WALLET_NORMAL = 1
        const val WALLET_LEVER = 2
    }

    private var onWalletModelListener: OnWalletModelListener? = null
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()
    private var userLeverObserver: Observer<String?>? = createUserLeverObserver()

    private var walletList: ArrayList<Wallet?>? = null
    private var walletLeverList: ArrayList<WalletLever?>? = null
    private var searchKey: String? = null
    private var walletCoinFilter: Boolean? = false

    private val comparator = WalletComparator(WalletComparator.NORMAL, WalletComparator.NORMAL, WalletComparator.NORMAL, WalletComparator.NORMAL)

    constructor(context: Context, onWalletModelListener: OnWalletModelListener) : this(context) {
        this.onWalletModelListener = onWalletModelListener
        this.walletCoinFilter = CookieUtil.getWalletCoinFilter(context)
    }

    override fun onResume() {
        super.onResume()
        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)
        if (userLeverObserver == null) {
            userLeverObserver = createUserLeverObserver()
        }
        SocketDataContainer.subscribeUserLeverObservable(userLeverObserver)
    }

    override fun onStop() {
        super.onStop()
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }
        if (userLeverObserver != null) {
            SocketDataContainer.removeUserLeverObservable(userLeverObserver)
        }
    }

    fun doSortName() {
        comparator.nameType = getNextType(comparator.nameType)
        comparator.usableType = WalletComparator.NORMAL
        comparator.frozeType = WalletComparator.NORMAL
        comparator.cnyType = WalletComparator.NORMAL
    }

    fun doSortUsable() {
        comparator.nameType = WalletComparator.NORMAL
        comparator.usableType = getNextType(comparator.usableType)
        comparator.frozeType = WalletComparator.NORMAL
        comparator.cnyType = WalletComparator.NORMAL
    }

    fun doSortFroze() {
        comparator.nameType = WalletComparator.NORMAL
        comparator.usableType = WalletComparator.NORMAL
        comparator.frozeType = getNextType(comparator.frozeType)
        comparator.cnyType = WalletComparator.NORMAL
    }

    fun doSortCny() {
        comparator.nameType = WalletComparator.NORMAL
        comparator.usableType = WalletComparator.NORMAL
        comparator.frozeType = WalletComparator.NORMAL
        comparator.cnyType = getNextType(comparator.cnyType)
    }

    fun getComparator(): WalletComparator {
        return comparator
    }


    fun setWalletCoinFilter(walletCoinFilter: Boolean) {
        CookieUtil.setWalletCoinFilter(context, walletCoinFilter)
        this.walletCoinFilter = walletCoinFilter
    }

    fun getWalletCoinFilter(): Boolean? {
        return walletCoinFilter
    }

    fun getWalletList(): ArrayList<Wallet?>? {
        return walletList
    }

    fun getWalletLeverList(): ArrayList<WalletLever?>? {
        return walletLeverList
    }

    fun getSearchKey(): String? {
        return searchKey
    }

    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onWalletModelListener?.onUserInfoChanged()
            }
        }
    }

    private fun createUserLeverObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onWalletModelListener?.onUserInfoChanged()
            }
        }
    }

    fun getAllWallet(isShowLoading: Boolean) {
        getWalletFromServer2(isShowLoading)
//        onWalletModelListener?.onGetWallet(getWalletFromServer(isShowLoading), isShowLoading)
    }

    private fun getWalletFromServer2(isShowLoading: Boolean) {
        WalletApiServiceHelper.getWalletAll(context, HttpCallbackSimple(context, isShowLoading, object : Callback<HttpRequestResultData<WalletConfig?>?>() {
            override fun callback(returnData: HttpRequestResultData<WalletConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    walletList = returnData.data?.userCoinAccountVO
                    walletLeverList = returnData.data?.userCoinAccountLeverVO
                    C2CApiServiceHelper.getC2CPrice(context!!)
                            ?.materialize()
                            ?.flatMap(Function<Notification<C2CPrice?>, Observable<Int>> { notify ->
                                if (notify.isOnNext) {
                                    val c2CPrice = notify.value
                                    //循环累加
                                    var walletTotal = 0.0
                                    var walletTotalCNY = 0.0
                                    var walletLeverTotal = 0.0
                                    var walletLeverTotalCNY = 0.0
                                    walletList?.run {
                                        for (wallet in walletList!!) {
                                            walletTotal += wallet?.estimatedTotalAmount
                                                    ?: 0.toDouble()
                                            val cny: Double? = SocketDataContainer.computeTotalMoneyCNY(wallet?.estimatedTotalAmount, c2CPrice)
                                            cny?.also {
                                                wallet?.totalAmountCny = it
                                                walletTotalCNY += it
                                            }
                                        }
                                    }
                                    walletLeverList?.run {
                                        for (walletLever in walletLeverList!!) {
                                            walletLeverTotal += walletLever?.estimatedTotalAmount
                                                    ?: 0.toDouble()
                                            val cny: Double? = SocketDataContainer.computeTotalMoneyCNY(walletLever?.estimatedTotalAmount, c2CPrice)
                                            cny?.also {
                                                walletLever?.totalAmountCny = it
                                                walletLeverTotalCNY += it
                                            }
                                            walletLeverTotal += walletLever?.afterEstimatedTotalAmount
                                                    ?: 0.toDouble()
                                            val cny2: Double? = SocketDataContainer.computeTotalMoneyCNY(walletLever?.afterEstimatedTotalAmount, c2CPrice)
                                            cny2?.also {
                                                walletLever?.afterTotalAmountCny = it
                                                walletLeverTotalCNY += it
                                            }
                                        }
                                    }
                                    onWalletModelListener?.onWalletTotal(Observable.just(Money().also {
                                        it.usdt = walletTotal
                                        it.cny = walletTotalCNY
                                    })
                                            .compose(RxJavaHelper.observeOnMainThread()))
                                    onWalletModelListener?.onWalletLeverTotal(Observable.just(Money().also {
                                        it.usdt = walletLeverTotal
                                        it.cny = walletLeverTotalCNY
                                    })
                                            .compose(RxJavaHelper.observeOnMainThread()))
                                    onWalletModelListener?.onTotalMoney(Observable.just(walletTotal + walletLeverTotal)
                                            .compose(RxJavaHelper.observeOnMainThread()))
                                    return@Function Observable.just(2)
                                }
                                if (notify.isOnError) {
                                    return@Function Observable.just(1)
                                }
                                Observable.empty()
                            })
                            ?.flatMap {
                                onWalletModelListener?.onWallet(Observable.just(filterWallet())
                                        .compose(RxJavaHelper.observeOnMainThread()), isShowLoading)
                                onWalletModelListener?.onWalletLever(Observable.just(filterWalletLever())
                                        .compose(RxJavaHelper.observeOnMainThread()), isShowLoading)
                                Observable.just(1)
                            }
                            ?.compose(RxJavaHelper.observeOnMainThread())
                            ?.subscribe()
                } else {
                    FryingUtil.showToast(context, returnData?.message)
                }
            }

            override fun error(type: Int, error: Any?) {
                FryingUtil.showToast(context, error?.toString())
            }

        }))
    }

//    private fun getWalletFromServer(isShowLoading: Boolean): Observable<Int> {
//        return ApiManager.build(context).getService(WalletApiService::class.java)
//                .getWallet(null)
//                .flatMap { returnData: HttpRequestResultData<WalletConfig>? ->
//                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                        walletList = returnData.data.userCoinAccountVO
//                        walletLeverList = returnData.data.userCoinAccountLeverVO
//                        Observable.just(1)
//                    } else {
//                        Observable.error(RuntimeException(returnData?.message))
//                    }
//                }
//                .flatMap {
//                    C2CApiServiceHelper.getC2CPrice(context!!)
//                            .materialize()
//                            .flatMap(Function<Notification<C2CPrice>, Observable<Int>> { notify ->
//                                if (notify.isOnNext) {
//                                    val c2CPrice = notify.value
//                                    //循环累加
//                                    var walletTotal = 0.0
//                                    var walletTotalCNY = 0.0
//                                    var walletLeverTotal = 0.0
//                                    var walletLeverTotalCNY = 0.0
//                                    walletList?.run {
//                                        for (wallet in walletList!!) {
//                                            walletTotal += wallet?.estimatedTotalAmount
//                                                    ?: 0.toDouble()
//                                            val cny: Double? = SocketDataContainer.computeTotalMoneyCNY(wallet?.estimatedTotalAmount, c2CPrice)
//                                            cny?.also {
//                                                wallet?.totalAmountCny = it
//                                                walletTotalCNY += it
//                                            }
//                                        }
//                                    }
//                                    walletLeverList?.run {
//                                        for (walletLever in walletLeverList!!) {
//                                            walletLeverTotal += walletLever?.estimatedTotalAmount
//                                                    ?: 0.toDouble()
//                                            val cny: Double? = SocketDataContainer.computeTotalMoneyCNY(walletLever?.estimatedTotalAmount, c2CPrice)
//                                            cny?.also {
//                                                walletLever?.totalAmountCny = it
//                                                walletLeverTotalCNY += it
//                                            }
//                                            walletLeverTotal += walletLever?.afterEstimatedTotalAmount
//                                                    ?: 0.toDouble()
//                                            val cny2: Double? = SocketDataContainer.computeTotalMoneyCNY(walletLever?.afterEstimatedTotalAmount, c2CPrice)
//                                            cny2?.also {
//                                                walletLever?.afterTotalAmountCny = it
//                                                walletLeverTotalCNY += it
//                                            }
//                                        }
//                                    }
//                                    onWalletModelListener?.onWalletTotal(Observable.just(Money().also {
//                                        it.usdt = walletTotal
//                                        it.cny = walletTotalCNY
//                                    })
//                                            .compose(RxJavaHelper.observeOnMainThread()))
//                                    onWalletModelListener?.onWalletLeverTotal(Observable.just(Money().also {
//                                        it.usdt = walletLeverTotal
//                                        it.cny = walletLeverTotalCNY
//                                    })
//                                            .compose(RxJavaHelper.observeOnMainThread()))
//                                    onWalletModelListener?.onTotalMoney(Observable.just(walletTotal + walletLeverTotal)
//                                            .compose(RxJavaHelper.observeOnMainThread()))
//                                    return@Function Observable.just(2)
//                                }
//                                if (notify.isOnError) {
//                                    return@Function Observable.just(1)
//                                }
//                                Observable.empty()
//                            })
//                }
//                .flatMap {
//                    onWalletModelListener?.onWallet(Observable.just(filterWallet())
//                            .compose(RxJavaHelper.observeOnMainThread()), isShowLoading)
//                    onWalletModelListener?.onWalletLever(Observable.just(filterWalletLever())
//                            .compose(RxJavaHelper.observeOnMainThread()), isShowLoading)
//                    Observable.just(1)
//                }
//                .compose(RxJavaHelper.observeOnMainThread())
//    }

    private fun filterWallet(): ArrayList<Wallet?>? {
        var showData: ArrayList<Wallet?>? = ArrayList<Wallet?>()
        if (walletList != null && walletList?.isNotEmpty()!!) {
            if (walletCoinFilter!!) {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    for (wallet in walletList!!) {
                        if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10) {
                            showData?.add(wallet)
                        }
                    }
                } else {
                    for (wallet in walletList!!) {
                        if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10 && wallet.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                            showData?.add(wallet)
                        }
                    }
                }
            } else {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    showData = walletList
                } else {
                    for (wallet in walletList!!) {
                        if (wallet?.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                            showData?.add(wallet)
                        }
                    }
                }
            }
        }
        return showData
    }

    private fun filterWalletLever(): ArrayList<WalletLever?>? {
        var showData: ArrayList<WalletLever?>? = ArrayList<WalletLever?>()
        if (walletLeverList != null && walletLeverList?.isNotEmpty()!!) {
            if (walletCoinFilter!!) {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    for (wallet in walletLeverList!!) {
                        if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10) {
                            showData?.add(wallet)
                        }
                    }
                } else {
                    for (wallet in walletLeverList!!) {
                        if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10 && wallet.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                            showData?.add(wallet)
                        }
                    }
                }
            } else {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    showData = walletLeverList
                } else {
                    for (wallet in walletLeverList!!) {
                        if ((wallet?.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault())))
                                || (wallet?.afterCoinType != null && wallet.afterCoinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault())))) {
                            showData?.add(wallet)
                        }
                    }
                }
            }
        }
        return showData
    }

    private fun filterWallet(observable: Observable<ArrayList<Wallet?>?>): Observable<ArrayList<Wallet?>?> {
        return observable.flatMap { walletList: ArrayList<Wallet?>? ->
            var showData = ArrayList<Wallet?>()
            if (walletList != null && walletList.isNotEmpty()) {
                if (walletCoinFilter!!) {
                    if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                        for (wallet in walletList) {
                            if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10) {
                                showData.add(wallet)
                            }
                        }
                    } else {
                        for (wallet in walletList) {
                            if (wallet?.totalAmountCny != null && wallet.totalAmountCny!! >= 10 && wallet.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                                showData.add(wallet)
                            }
                        }
                    }
                } else {
                    if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                        showData = walletList
                    } else {
                        for (wallet in walletList) {
                            if (wallet?.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                                showData.add(wallet)
                            }
                        }
                    }
                }
            }
            Observable.just(showData)
        }
    }

    private fun getNextType(currentType: Int): Int {
        return when (currentType) {
            WalletComparator.NORMAL -> WalletComparator.UP
            WalletComparator.UP -> WalletComparator.DOWN
            else -> WalletComparator.NORMAL
        }
    }

    fun search(searchKey: String?) {
        this.searchKey = searchKey
        onWalletModelListener?.onWallet(Observable.just(filterWallet()).compose(RxJavaHelper.observeOnMainThread()), false)
        onWalletModelListener?.onWalletLever(Observable.just(filterWalletLever()).compose(RxJavaHelper.observeOnMainThread()), false)
    }

    fun computeTotalCNY(total: Double?) {
        onWalletModelListener?.onTotalCNY(
                Observable.just(total)
                        .flatMap { money: Double? ->
                            if (money == null) {
                                Observable.just(null)
                            }
                            C2CApiServiceHelper.getC2CPrice(context!!)
                                    ?.materialize()
                                    ?.flatMap(Function<Notification<C2CPrice?>, Observable<Double?>> { notify ->
                                        if (notify.isOnNext) {
                                            val cny: Double? = SocketDataContainer.computeTotalMoneyCNY(money, notify.value)
                                            return@Function Observable.just(cny)
                                        }
                                        if (notify.isOnError) {
                                            return@Function Observable.just(null)
                                        }
                                        Observable.empty()
                                    })
                        }
                        .compose(RxJavaHelper.observeOnMainThread())
        )
    }

    fun computeTotalBTC(total: Double?) {
        onWalletModelListener?.onTotalBTC(
                Observable.just(total)
                        .flatMap { money: Double? ->
                            if (money == null) {
                                Observable.just(null)
                            }
                            if (money == 0.0) {
                                Observable.just(0.0)
                            }
                            val pairStatus: PairStatus? = SocketDataContainer.getPairStatusSync(context, "BTC_USDT")
                            if (pairStatus == null || pairStatus.currentPrice == 0.0) {
                                Observable.just(null)
                            } else {
                                Observable.just(money!! / pairStatus.currentPrice)
                            }
                        }
                        .compose(RxJavaHelper.observeOnMainThread()))
    }

    interface OnWalletModelListener {
        fun onUserInfoChanged()
        fun onGetWallet(observable: Observable<Int>?, isShowLoading: Boolean)
        fun onWallet(observable: Observable<ArrayList<Wallet?>?>?, isShowLoading: Boolean)
        fun onWalletLever(observable: Observable<ArrayList<WalletLever?>?>?, isShowLoading: Boolean)
        fun onWalletTotal(observable: Observable<Money?>?)
        fun onWalletLeverTotal(observable: Observable<Money?>?)
        fun onTotalMoney(observable: Observable<Double?>)
        fun onTotalCNY(observable: Observable<Double?>)
        fun onTotalBTC(observable: Observable<Double?>)
    }
}