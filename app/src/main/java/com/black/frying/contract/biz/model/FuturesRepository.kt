package com.black.frying.contract.biz.model

import com.black.base.api.FutureApiService
import com.black.base.api.FutureSuspendApiService
import com.black.base.api.PairApiService
import com.black.base.api.PairSuspendApiService
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.future.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.SharedPreferenceUtils
import com.black.base.util.UrlConfig
import com.black.frying.FryingApplication

object FuturesRepository {
    suspend fun getCollectPairs(): List<String?>? {
        val context = FryingApplication.instance()
        try {
            val collectPairs = ApiManager.build(context, UrlConfig.ApiType.URl_UC)
                .getService(PairSuspendApiService::class.java)?.getCollectPairs()
            return if (collectPairs?.isCodeOk() == true) {
                collectPairs.data
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    fun isCollect(coin: String): Boolean {
        return SharedPreferenceUtils.getBoolean(coin + "-PERPETUAL", false)
    }

    fun collectionCoin(coin: String, isCollect: Boolean) {
//        val context = FryingApplication.instance()
//        val service = ApiManager.build(context, UrlConfig.ApiType.URl_UC)
//            .getService(PairSuspendApiService::class.java)
//           return  service?.pairCollectCancel(coin)
        SharedPreferenceUtils.putBoolean(coin + "-PERPETUAL", isCollect)
    }

    suspend fun getSymbolList(): HttpRequestResultBean<ArrayList<SymbolBean>?>? {
        val context = FryingApplication.instance()
        try {
            // TODO: 币本位修改  host method
            return ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
                .getService(FutureSuspendApiService::class.java)
                ?.getSymbolList()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    suspend fun getPositionList(coinPair: String): ArrayList<PositionBean?>? {
        val context = FryingApplication.instance()
        try {
            val response = ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
                .getService(FutureSuspendApiService::class.java)
                ?.getPositionList(symbol = coinPair)
            return if (response?.isOk() == true) {
                response.result
            } else {
                null
            }
        }catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }

    suspend fun adjustLeverage(symbol:String,positionSide:String,leverage :Int):Boolean {
       try {
           val context = FryingApplication.instance()
           val response = ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
               .getService(FutureSuspendApiService::class.java)
               ?.adjustLeverage(symbol, positionSide, leverage)
           return response?.isOk() == true
       }catch (e:Exception){
           e.printStackTrace()
           return false
       }
    }

    suspend fun getFundingRate(coinPair:String): FundingRateBean? {

        val context = FryingApplication.instance()
        return try {
            val response = ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
                .getService(FutureSuspendApiService::class.java)
                ?.getFundingRate(symbol = coinPair)
            if (response?.isOk() == true) {
                response.result
            } else {
                null
            }
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    suspend fun getBalanceDetailSuspend(coin:String, underlyingType: String = Constants.U_BASED): BalanceDetailBean? {
        val context = FryingApplication.instance()
        return try {
            val response = ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
                .getService(FutureSuspendApiService::class.java)
                ?.getBalanceDetailSuspend(coin = null,underlyingType = underlyingType)
            if (response?.isOk() == true) {
                response.result
            } else {
                null
            }
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

}