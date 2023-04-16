package com.black.frying.contract.biz.model

import com.black.base.api.FutureApiService
import com.black.base.api.FutureSuspendApiService
import com.black.base.api.PairApiService
import com.black.base.api.PairSuspendApiService
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.future.SymbolBean
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
        // TODO: 币本位修改  host method
        return ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureSuspendApiService::class.java)
            ?.getSymbolList()
    }

}