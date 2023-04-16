package com.black.frying.contract.viewmodel

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.base.model.QuotationSet
import com.black.base.model.socket.PairStatus
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.base.view.PairStatusPopupWindow
import com.black.frying.FryingApplication
import com.black.frying.contract.biz.model.FuturesRepository
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.router.BlackRouter
import com.fbsex.exchange.R
import kotlinx.coroutines.launch
import java.util.*

const val TAG = "FuturesTitleViewModel"

class FuturesTitleViewModel : ViewModel() {

    var globalStateViewModel :FutureGlobalStateViewModel?=null
    val context = FryingApplication.instance()

    var isCollect = false
    val isCollectLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>()


    init {
        loadCoinInfo()
    }

    fun loadCoinInfo() {
        viewModelScope.launch {
            globalStateViewModel?.let {
                val symbolBean = it.symbolBean
                symbolBean?.let {symbolBeanInfo ->
                    val symbol = symbolBeanInfo.symbol
                    val contains = FuturesRepository.isCollect(symbol)
                    isCollect = contains
                    isCollectLiveData.postValue(contains)
//                    val collectPairs = FuturesRepository.getCollectPairs()
//                    collectPairs?.let {list ->
//                        val contains = list.contains(symbol)
//
//                        isCollect = contains
//                        isCollectLiveData.postValue(contains)
//                    }
                }

            }
        }

    }


    override fun onCleared() {
        super.onCleared()
    }

    fun autoCollectCoin() {
        isCollect = !isCollect
        isCollectLiveData.value = isCollect
        globalStateViewModel?.symbolBean?.let {
            FuturesRepository.collectionCoin(it.symbol,isCollect)
        }
    }

    fun goToKlineDetail(act: Activity) {
        val currentPair = CookieUtil.getCurrentFutureUPair(context)!!.uppercase()
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
    }

    fun onResume() {
//        okWebSocketHelper?.resume()
    }

}