package com.black.frying.contract.biz.model

import com.black.base.api.PairApiService
import com.black.base.manager.ApiManager
import com.black.base.util.RxJavaHelper
import com.black.base.util.SharedPreferenceUtils
import com.black.base.util.UrlConfig
import com.black.frying.FryingApplication

object FuturesRepository {
    fun isCollectCoin(coin: String): Boolean {
        //todo 同步server
        return SharedPreferenceUtils.getData(coin, false) as Boolean
    }

    fun collectionCoin(coin: String, isCollect: Boolean) {
        SharedPreferenceUtils.putBoolean(coin, isCollect)
        val context = FryingApplication.instance()
        if (isCollect) {
            ApiManager.build(context, UrlConfig.ApiType.URl_UC)
                .getService(PairApiService::class.java)?.pairCollect(coin)?.subscribe()
        } else {
            ApiManager.build(context, UrlConfig.ApiType.URl_UC)
                .getService(PairApiService::class.java)?.pairCollectCancel(coin)
                ?.compose(RxJavaHelper.observeOnMainThread())?.subscribe()
        }


    }
}