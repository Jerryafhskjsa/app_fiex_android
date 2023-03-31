package com.black.frying.contract.viewmodel

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.*
import com.black.base.model.QuotationSet
import com.black.base.model.socket.PairQuotation
import com.black.base.model.socket.PairStatus
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.base.util.UrlConfig
import com.black.base.view.PairStatusPopupWindow
import com.black.frying.FryingApplication
import com.black.frying.contract.biz.model.FuturesRepository
import com.black.frying.contract.biz.okwebsocket.SincePriceMessageHandler
import com.black.frying.contract.biz.okwebsocket.getMarketOkWebSocket
import com.black.frying.contract.biz.okwebsocket.sendCommandSymbol
import com.black.frying.contract.biz.okwebsocket.sendCommandUnSymbol
import com.black.frying.contract.viewmodel.dto.FuturesCoinInfoDTo
import com.black.frying.contract.viewmodel.model.FuturesCoinPair
import com.black.net.okhttp.OKWebSocketFactory
import com.black.net.okhttp.OKWebSocketFactory.getOkWebSocket
import com.black.net.okhttp.OkWebSocketHelper
import com.black.router.BlackRouter
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

const val TAG = "FuturesTitleViewModel"

class FuturesTitleViewModel : ViewModel() {


    val coinInfo = MutableLiveData<FuturesCoinInfoDTo>()

    val context = FryingApplication.instance()

    var isCollect = false
    var coinNameInfo: FuturesCoinPair? = null
    var priceSincePercent: BigDecimal = BigDecimal.ZERO

    var okWebSocketHelper: OkWebSocketHelper? = null

    var lifecycleOwner: LifecycleOwner? = null

    init {
        val okWebSocket = getMarketOkWebSocket()
        okWebSocketHelper = OkWebSocketHelper(okWebSocket)
        okWebSocketHelper?.start()
        okWebSocketHelper?.setImessageHandler(object : SincePriceMessageHandler() {
            override fun consumeMessage(pairQuotation: PairQuotation) {
                val price = NumberUtil.toBigDecimal(pairQuotation.r).multiply(BigDecimal.valueOf(100))
//                val value = NumberUtil.formatNumber(price,2).toString()
                priceSincePercent = price
                updateCoinInfo()
            }
        })
        okWebSocketHelper?.sendCommandSymbol()
    }

    fun loadCoinInfo() {
        viewModelScope.launch {
            val coinNameInfoTask = async(Dispatchers.IO) {
                return@async FuturesCoinPair.load()
            }
            coinNameInfo = coinNameInfoTask.await()
            isCollect = FuturesRepository.isCollectCoin(coinNameInfo?.source() ?: "")
            updateCoinInfo()
        }
    }

    private fun updateCoinInfo() {
        coinInfo.postValue(
            FuturesCoinInfoDTo(
                coinName = (coinNameInfo?.display() ?: "--").uppercase(Locale.ROOT),
                priceSincePercent = priceSincePercent,
                isCollect = isCollect
            )
        )
    }


    override fun onCleared() {
        super.onCleared()
        lifecycleOwner = null
    }

    fun autoCollectCoin() {
        val coin = coinNameInfo?.source() ?: ""
        if (coin.isEmpty()) {
            return
        }
        isCollect = !isCollect
        FuturesRepository.collectionCoin(coin, isCollect)
        updateCoinInfo()
    }

    fun goToKlineDetail(act: Activity) {
        val currentPair = CookieUtil.getCurrentFutureUPair(context)
        if (!TextUtils.isEmpty(currentPair)) {
            val bundle = Bundle()
            bundle.putString(ConstData.PAIR, currentPair)
            BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle)
                .go(act)
        }
    }

    fun goToSelectOtherCoin(act: Activity) {
        val setData = ArrayList<QuotationSet?>(3)
        val optionalUbaseSet = QuotationSet()
        optionalUbaseSet.coinType = context.getString(R.string.usdt)
        optionalUbaseSet.name = context.getString(R.string.usdt_base)
        setData.add(optionalUbaseSet)
        val optionalCoinBaseSet = QuotationSet()
        optionalCoinBaseSet.coinType = context.getString(R.string.usd)
        optionalCoinBaseSet.name = context.getString(R.string.coin_base)
        setData.add(optionalCoinBaseSet)
        PairStatusPopupWindow.getInstance(
            act,
            PairStatusPopupWindow.TYPE_FUTURE_ALL,
            setData
        ).show(object : PairStatusPopupWindow.OnPairStatusSelectListener {
            override fun onPairStatusSelected(pairStatus: PairStatus?) {
                if (pairStatus == null) {
                    return
                }
                CookieUtil.setCurrentFutureUPair(context, pairStatus.pair)
                loadCoinInfo()
            }
        })
    }

    fun onPause() {
//        okWebSocketHelper?.pause()
        okWebSocketHelper?.sendCommandUnSymbol()
    }

    fun onResume() {
//        okWebSocketHelper?.resume()
        okWebSocketHelper?.sendCommandSymbol()
    }

}