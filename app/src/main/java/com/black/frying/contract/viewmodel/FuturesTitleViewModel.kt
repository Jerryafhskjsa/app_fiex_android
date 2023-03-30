package com.black.frying.contract.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.frying.FryingApplication
import com.black.frying.contract.biz.model.FuturesRepository
import com.black.frying.contract.viewmodel.dto.FuturesCoinInfoDTo
import com.black.frying.contract.viewmodel.model.FuturesCoinPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

const val TAG = "FuturesTitleViewModel"

class FuturesTitleViewModel : ViewModel() {


    val coinInfo = MutableLiveData<FuturesCoinInfoDTo>()

    val context = FryingApplication.instance()

    var isCollect = false
    var coinNameInfo: FuturesCoinPair? = null
    var priceSincePercent: String = "--"
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
}