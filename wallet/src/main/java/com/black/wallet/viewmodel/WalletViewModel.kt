package com.black.wallet.viewmodel

import android.content.Context
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.PairApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.*
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.socket.PairStatus
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
import com.black.base.model.wallet.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.wallet.util.WalletComparator
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.functions.Function
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class WalletViewModel(context: Context) : BaseViewModel<Any>(context) {
    companion object {
        const val WALLET_NORMAL = 1
        const val WALLET_LEVER = 2
        const val WALLET_CONTRACT = 3
        const val CONTRACT_U = 0
        const val  CONTRACT_COIN = 1
    }

    private var onWalletModelListener: OnWalletModelListener? = null
    private var userBalanceObserver: Observer<UserBalance?>? = null
    private var userLeverObserver: Observer<String?>? = null
    private var type = CONTRACT_U
    private var walletList: ArrayList<Wallet?>? = ArrayList()
    private var tigerWalletList: ArrayList<TigerWallet?>? = ArrayList()
    private var walletLeverList: ArrayList<WalletLever?>? = null
    private var searchKey: String? = null
    private var walletCoinFilter: Boolean? = false
    private var isVisibility: Boolean? = true

    private var symbolList:ArrayList<PairStatus?>? = null
    private var coinList:ArrayList<CoinInfo?>? = ArrayList()
    private var spotBalanceList:ArrayList<UserBalance?>?  = null
    private var tigerBalanceList:ArrayList<UserBalance?>?  = null
    private var priceRate: Double? = null
    private var profitAmount: Double? = null



    private val comparator = WalletComparator(WalletComparator.NORMAL, WalletComparator.NORMAL, WalletComparator.NORMAL, WalletComparator.NORMAL)

    constructor(context: Context, onWalletModelListener: OnWalletModelListener) : this(context) {
        this.onWalletModelListener = onWalletModelListener
        this.walletCoinFilter = CookieUtil.getWalletCoinFilter(context)
    }

    init {
        symbolList = PairApiServiceHelper.getSymboleListPairData(context)
        getCoinlistConfig()
    }

    /**
     * 获取币种配置
     */
    private fun getCoinlistConfig(){
        WalletApiServiceHelper.getCoinInfoList(context, object :Callback<ArrayList<CoinInfoType?>?>(){
            override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                if (returnData != null) {
                    for (i in returnData){
                        var config = i?.config
                        //钱包显示币种，多链只取第一个
                        var coinInfo = config?.get(0)?.coinConfigVO
                        coinList?.add(coinInfo)
                    }
                }
            }
            override fun error(type: Int, error: Any?) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (userBalanceObserver == null) {
            userBalanceObserver = createUserBalanceObserver()
        }
        SocketDataContainer.subscribeUserBalanceObservable(userBalanceObserver)
    }

    override fun onStop() {
        super.onStop()
        if (userBalanceObserver != null) {
            SocketDataContainer.removeUserBalanceObservable(userBalanceObserver)
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

    fun setWalletziCanFilter(walletCoinFilter: Boolean) {
        CookieUtil.setWalletziCanFilter(context, walletCoinFilter)
        this.isVisibility = walletCoinFilter
    }
    fun getWalletziCanFilter(): Boolean? {
        return isVisibility
    }

    fun getWalletList(): ArrayList<Wallet?>? {
        return walletList
    }

    fun getSpotBalanceList():ArrayList<UserBalance?>?{
        return spotBalanceList
    }
    fun getTigerBalanceList():ArrayList<UserBalance?>?{
        return tigerBalanceList
    }

    fun getWalletLeverList(): ArrayList<WalletLever?>? {
        return walletLeverList
    }

    fun getSearchKey(): String? {
        return searchKey
    }

    private fun createUserBalanceObserver(): Observer<UserBalance?> {
        return object : SuccessObserver<UserBalance?>() {
            override fun onSuccess(value: UserBalance?) {
                onWalletModelListener?.onUserBalanceChanged(value)
            }
        }
    }

    private fun createUserLeverObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
//                onWalletModelListener?.onUserInfoChanged()
            }
        }
    }

    fun getAllWallet(isShowLoading: Boolean) {
//        getWalletFromServer2(isShowLoading)
        getUserBalance(isShowLoading)
    }


    private fun getUserBalance(isShowLoading: Boolean){
        WalletApiServiceHelper.getUserBalance(context)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, object : Callback<HttpRequestResultData<UserBalanceWarpper?>?>() {
                override fun error(type: Int, error: Any) {
                }
                override fun callback(returnData: HttpRequestResultData<UserBalanceWarpper?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        spotBalanceList = returnData.data?.spotBalance
                        tigerBalanceList = returnData.data?.tigerBalance
                        priceRate = returnData.data?.phaseRate
                        profitAmount = returnData.data?.profitAmount
                        handleBalanceResult(walletList)
                        handleTigerBalanceResult(tigerWalletList)
                    }
                }
            }))
    }

    /**
     * setType 1现货
     */
    private fun getSymbolListSets(setType:Int,symbolList: ArrayList<PairStatus?>?):ArrayList<String>{
        var setListSet = ArrayList<String>()
        var sellCoinSet = ArrayList<String?>()
        var buyCoinSet = ArrayList<String?>()
        if(symbolList != null && symbolList?.size!! > 0){
            for(m in symbolList!!){
                if (m != null) {
                    if(m.setType == setType){
                        if (m != null) {
                            if(!buyCoinSet.contains(m.name)){
                                buyCoinSet.add(m.name)
                            }
                        }
                        if (m != null) {
                            if(!sellCoinSet.contains(m.setName)){
                                sellCoinSet.add(m.setName)
                            }
                        }
                    }
                }
            }
        }
        for (i in buyCoinSet){
            if (i != null) {
                setListSet.add(i)
            }
        }
        for (j in sellCoinSet){
            if (j != null) {
                setListSet.add(j)
            }
        }
        return setListSet
    }

    fun handleBalanceResult(walletList:ArrayList<Wallet?>?){
        if(walletList != null && walletList!!.size > 0){
            walletList!!.clear()
        }
        if(spotBalanceList != null){
            if(coinList != null){
                for(coin in coinList!!){
                    var wallet = Wallet()
                    wallet.coinType = coin?.coinType
                    wallet.coinIconUrl = coin?.logosUrl
                    wallet.coinTypeDes = coin?.coinFullName
                    wallet.coinAmount = BigDecimal(0)
                    wallet.estimatedAvailableAmount = 0.0
                    wallet.estimatedAvailableAmountCny = 0.0
                    wallet.coinFroze = 0.0
                    walletList?.add(wallet)
                }
            }
            for(i in walletList!!.indices){
                for (k in spotBalanceList!!.indices){
                    if(spotBalanceList!![k]?.coin == walletList!![i]?.coinType){
                        walletList!![i]?.totalAmount = spotBalanceList!![k]?.balance?.toDouble()!!
                        walletList!![i]?.coinAmount = BigDecimal(spotBalanceList!![k]?.availableBalance?.toDouble()!!)
                        walletList!![i]?.estimatedAvailableAmount = spotBalanceList!![k]?.estimatedAvailableAmount?.toDouble()!!
                        walletList!![i]?.coinFroze = spotBalanceList!![k]?.freeze?.toDouble()!!
                        walletList!![i]?.estimatedAvailableAmountCny = spotBalanceList!![k]?.estimatedCynAmount?.toDouble()!!
                        walletList!![i]?.coinFroze = spotBalanceList!![k]?.freeze?.toDouble()!!
                        break
                    }
                }
            }
        }
        onWalletModelListener?.onWallet(Observable.just(walletList)
            .compose(RxJavaHelper.observeOnMainThread()), false)

        var walletTotal = 0.0
        var totalAmount = 0.0
        var coinFtoze = 0.0
        var walletTotalCNY = 0.0
        walletList?.run {
            for (wallet in walletList!!) {
                walletTotal += wallet?.estimatedAvailableAmount
                    ?: 0.toDouble()
                coinFtoze += wallet?.coinFroze
                    ?: 0.toDouble()
                walletTotalCNY += wallet?.estimatedAvailableAmountCny
                    ?: 0.toDouble()
                totalAmount += wallet?.totalAmount
                    ?: 0.toDouble()
            }
        }
        onWalletModelListener?.onWalletTotal(Observable.just(Money().also {
            it.phaseRate = priceRate
            it.profitAmount = profitAmount
            it.usdt = walletTotal
            it.cny = walletTotalCNY
            it.forze = coinFtoze
            val price = C2CApiServiceHelper?.coinUsdtPrice?.usdt
            it.rate = price
            it.total = totalAmount
        })
            .compose(RxJavaHelper.observeOnMainThread()))
    }
    fun handleTigerBalanceResult(tigerWalletList:ArrayList<TigerWallet?>?){
        if(tigerWalletList != null && tigerWalletList!!.size > 0){
            tigerWalletList!!.clear()
        }
        if(tigerBalanceList != null){

            if(coinList != null){
                for(coin in coinList!!){
                    if(coin?.coinType == "USDT" || coin?.coinType == "BTC" ||coin?.coinType == "ETH") {
                        var wallet = TigerWallet()
                        wallet.coinType = coin?.coinType
                        wallet.coinIconUrl = coin?.logosUrl
                        wallet.coinTypeDes = coin?.coinFullName
                        wallet.coinAmount = 0.0
                        wallet.estimatedAvailableAmount = 0.0
                        wallet.estimatedAvailableAmountCny = 0.0
                        tigerWalletList?.add(wallet)
                    }
                }
            }
            for(i in tigerWalletList!!.indices){
                for (k in tigerBalanceList!!.indices){
                    if(tigerBalanceList!![k]?.coin == tigerWalletList!![i]?.coinType){
                        tigerWalletList!![i]?.coinAmount =tigerBalanceList!![k]?.availableBalance?.toDouble()!!
                        tigerWalletList!![i]?.totalAmount =tigerBalanceList!![k]?.balance?.toDouble()!!
                        tigerWalletList!![i]?.estimatedTotalAmount = tigerBalanceList!![k]?.estimatedTotalAmount?.toDouble()!!
                        tigerWalletList!![i]?.estimatedAvailableAmountCny = tigerBalanceList!![k]?.estimatedCynAmount?.toDouble()!!
                        tigerWalletList!![i]?.profit = tigerBalanceList!![k]?.profit?.toDouble()!!
                        tigerWalletList!![i]?.crossedMargin = tigerBalanceList!![k]?.crossedMargin?.toDouble()!!
                        break
                    }
                }
            }
        }
        onWalletModelListener?.onContractWallet(Observable.just(tigerWalletList)
            .compose(RxJavaHelper.observeOnMainThread()), false)

        var walletTotal = 0.0
        var walletTotalCNY = 0.0
        var walletBalance = 0.0
        var totalProfit = 0.0
        var totalCrossedMargin = 0.0
        tigerWalletList?.run {
            for (wallet in tigerWalletList) {
                walletTotal += wallet?.estimatedTotalAmount
                    ?: 0.toDouble()
                walletTotalCNY += wallet?.estimatedAvailableAmountCny
                    ?: 0.toDouble()
                walletBalance += wallet?.coinAmount
                    ?: 0.toDouble()
                totalProfit += wallet?.profit
                    ?: 0.toDouble()
                totalCrossedMargin += wallet?.crossedMargin
                    ?: 0.toDouble()
            }
        }
        onWalletModelListener?.onContractWalletTotal(Observable.just(Money().also {
            it.tigerUsdt = walletTotal
            it.tigercny = walletTotalCNY
            it.walletBalance = walletBalance
            it.profit = totalProfit
            it.crossedMargin = totalCrossedMargin
        })
            .compose(RxJavaHelper.observeOnMainThread()))
    }

    fun updateBalance(balance: UserBalance?){
        var updateWalletList = ArrayList<Wallet?>()
        for (j in walletList?.indices!!){
            var newWallet = Wallet()
            var wallet = walletList!![j]
            if(balance?.coin.equals(wallet?.coinType)){
                newWallet?.totalAmount = balance?.balance?.toDouble()!!
                newWallet?.coinAmount = BigDecimal( balance?.availableBalance?.toDouble()!!)
                newWallet?.estimatedAvailableAmount = balance?.estimatedAvailableAmount?.toDouble()!!
                newWallet?.estimatedAvailableAmountCny = balance?.estimatedCynAmount?.toDouble()!!
                newWallet?.coinFroze = balance?.freeze?.toDouble()!!
            }
            updateWalletList.add(newWallet)
        }
        handleBalanceResult(updateWalletList)
    }
    fun updateTigerBalance(balance: UserBalance?){
        var updateTigerWalletList = ArrayList<TigerWallet?>()
        for (j in tigerWalletList?.indices!!){
            val newWallet = TigerWallet()
            val wallet = walletList!![j]
            if(balance?.coin.equals(wallet?.coinType)){
                newWallet.totalAmount = balance?.balance?.toDouble()?:0.0
                newWallet.coinAmount =  balance?.availableBalance?.toDouble()?:0.0
                newWallet.estimatedTotalAmount = balance?.estimatedTotalAmount?.toDouble()?:0.0
                newWallet.estimatedAvailableAmountCny = balance?.estimatedCynAmount?.toDouble()?:0.0
                newWallet.profit = balance?.profit?.toDouble()?:0.0
                newWallet.crossedMargin =  balance?.crossedMargin?.toDouble()?:0.0
            }
            updateTigerWalletList.add(newWallet)
        }
        handleTigerBalanceResult(updateTigerWalletList)
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


    fun filterWallet(): ArrayList<Wallet?>? {
        var showData: ArrayList<Wallet?>? = ArrayList<Wallet?>()
        if (walletList != null && walletList?.isNotEmpty()!!) {
            if (walletCoinFilter!!) {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    for (wallet in walletList!!) {
                        if (wallet?.estimatedAvailableAmountCny != null && wallet.estimatedAvailableAmountCny!! >= 10) {
                            showData?.add(wallet)
                        }
                    }
                } else {
                    for (wallet in walletList!!) {
                        if (wallet?.estimatedAvailableAmountCny != null && wallet.estimatedAvailableAmountCny!! >= 10 && wallet.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
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
    fun filterTigerWallet(): ArrayList<TigerWallet?>? {
        var showData: ArrayList<TigerWallet?>? = ArrayList<TigerWallet?>()
        if (tigerWalletList != null && tigerWalletList?.isNotEmpty()!!) {
            if (walletCoinFilter!!) {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    for (wallet in tigerWalletList!!) {
                        if (wallet?.estimatedAvailableAmountCny != null && wallet.estimatedAvailableAmountCny!! >= 10){
                            showData?.add(wallet)
                        }
                    }
                } else {
                    for (wallet in tigerWalletList!!) {
                        if (wallet?.estimatedAvailableAmountCny != null && wallet.estimatedAvailableAmountCny!! >= 10 && wallet.coinType != null && wallet.coinType!!.toUpperCase(Locale.getDefault()).trim { it <= ' ' }.contains(searchKey!!.toUpperCase(Locale.getDefault()))) {
                            showData?.add(wallet)
                        }
                    }
                }
            } else {
                if (searchKey == null || searchKey!!.trim { it <= ' ' }.isEmpty()) {
                    showData = tigerWalletList
                } else {
                    for (wallet in tigerWalletList!!) {
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
        onWalletModelListener?.onContractWallet(Observable.just(filterTigerWallet()).compose(RxJavaHelper.observeOnMainThread()), false)
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
                    val pairStatus: PairStatus? = SocketDataContainer.getPairStatusSync(context, ConstData.PairStatusType.SPOT,"BTC_USDT")
                    if (pairStatus == null || pairStatus.currentPrice == 0.0) {
                        Observable.just(null)
                    } else {
                        Observable.just(money!! / pairStatus.currentPrice)
                    }
                }
                .compose(RxJavaHelper.observeOnMainThread()))
    }

    interface OnWalletModelListener {
        fun onUserBalanceChanged(userBalance: UserBalance?)
        fun onUserInfoChanged()
        fun onGetWallet(observable: Observable<Int>?, isShowLoading: Boolean)
        fun onWallet(observable: Observable<ArrayList<Wallet?>?>?, isShowLoading: Boolean)
        fun onContractWallet(observable: Observable<ArrayList<TigerWallet?>?>?, isShowLoading: Boolean)
        fun onWalletLever(observable: Observable<ArrayList<WalletLever?>?>?, isShowLoading: Boolean)
        fun onWalletTotal(observable: Observable<Money?>?)
        fun onContractWalletTotal(observable: Observable<Money?>?)
        fun onWalletLeverTotal(observable: Observable<Money?>?)
        fun onTotalMoney(observable: Observable<Double?>)
        fun onTotalCNY(observable: Observable<Double?>)
        fun onTotalBTC(observable: Observable<Double?>)
    }
}