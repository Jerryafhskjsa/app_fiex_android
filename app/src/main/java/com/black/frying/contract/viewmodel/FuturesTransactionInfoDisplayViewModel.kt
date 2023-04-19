package com.black.frying.contract.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.socket.Deep
import com.black.base.model.socket.PairStatus
import com.black.base.model.socket.TradeOrder
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.SocketDataContainer
import com.black.frying.contract.FuturesDeepGraphFragment
import com.black.util.CommonUtil
import com.fbsex.exchange.R

class FuturesTransactionInfoDisplayViewModel : ViewModel() {
    lateinit var mContext: Context
    private var coinType: String? = null
    private var pairSet: String? = null
    fun init(context: Context) {
        mContext = context
        initPairStatus()
    }

    fun clickShowMode() {
        val maxIndex = FuturesDeepGraphFragment.ShowMode.values().size - 1
        var ordinal = showMode.value!!.ordinal
        if (ordinal >= maxIndex) {
            ordinal = 0
        } else {
            ordinal += 1
        }
        val nV = Math.min(maxIndex, ordinal)
        showMode.postValue(FuturesDeepGraphFragment.ShowMode.values()[nV])
    }

    val showMode =
        MutableLiveData<FuturesDeepGraphFragment.ShowMode>(FuturesDeepGraphFragment.ShowMode.DEFAULT)

    val sellList: ArrayList<TradeOrder?> = ArrayList()
    val buyList: ArrayList<TradeOrder?> = ArrayList()
     var currentPairStatus = PairStatus()

    private fun initPairStatus() {
        val onResumeTodo = Runnable {
            val currentPair = getLastPair()
            if (!TextUtils.equals(currentPair, currentPairStatus.pair)) {
                currentPairStatus = PairStatus()
                currentPairStatus.pair = (currentPair)
                initPairCoinSet()
                currentPairStatus.supportingPrecisionList = null
                currentPairStatus.precision = ConstData.DEFAULT_PRECISION
            }
            getCurrentPairStatus(currentPairStatus.pair)
        }
        currentPairStatus.pair = (getLastPair())
        initPairCoinSet()
        currentPairStatus.supportingPrecisionList = null
        currentPairStatus.precision = ConstData.DEFAULT_PRECISION
        if (currentPairStatus.pair == null) {
//            SocketDataContainer.initAllFutureUsdtPairStatusData(context)
        } else {
            onResumeTodo.run()
        }
    }
    private fun initPairCoinSet() {
        currentPairStatus.pair?.run {
            val arr: Array<String>? = currentPairStatus.pair?.split("_")?.toTypedArray()
            if (arr != null && arr.size > 1) {
                coinType = arr[0]
                pairSet = arr[1]
            }
        }
    }

    fun getCurrentPairStatus(pair: String?) {
        currentPairStatus.pair = (pair)
        initPairCoinSet()
        val pairStatus: PairStatus? = SocketDataContainer.getPairStatusSync(
            mContext,
            ConstData.PairStatusType.FUTURE_ALL,
            pair
        )
        if (pairStatus != null) {
            currentPairStatus = pairStatus
            initPairCoinSet()
        } else {
//            SocketDataContainer.initAllFutureUsdtPairStatusData(context)
        }
        resetPairStatus(pairStatus)
    }

    private fun resetPairStatus(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            return
        }
        currentPairStatus.currentPrice = (pairStatus.currentPrice)
        currentPairStatus.setCurrentPriceCNY(
            pairStatus.currentPriceCNY,
            mContext.getString(R.string.number_default)
        )
        currentPairStatus.maxPrice = (pairStatus.maxPrice)
        currentPairStatus.minPrice = (pairStatus.minPrice)
        currentPairStatus.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
        currentPairStatus.totalAmount = (pairStatus.totalAmount)
        if (pairStatus.pair != null) {
            currentPairStatus.pair = (pairStatus.pair)
        }
    }
    private fun getLastPair(): String? {
        var pair = CookieUtil.getCurrentFutureUPair(mContext)
        if (pair == null) {
            val allPair = SocketDataContainer.getAllPair(mContext, ConstData.PairStatusType.FUTURE_U)
            if (allPair != null) {
                pair = CommonUtil.getItemFromList(allPair, 0)
            }
        }
        return pair
    }


    fun getPrecisionDeep(precision: Int?): Deep? {
        var deepList = currentPairStatus.supportingPrecisionList
        if (deepList != null) {
            for (deep in deepList) {
                if (precision == deep.precision) {
                    return deep
                }
            }
        }
        return null
    }
    fun getPrecisionList(): ArrayList<Deep>? {
        return currentPairStatus.supportingPrecisionList
    }

    fun getAmountLength(): Int {
        return currentPairStatus.amountPrecision ?: 4
    }
    fun setPrecision(precision: Int) {
        currentPairStatus.precision = precision
    }

    fun getPrecision(): Int? {
        return currentPairStatus.precision
    }
}


