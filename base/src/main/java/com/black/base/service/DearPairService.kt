package com.black.base.service

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.api.PairApiService
import com.black.base.api.PairApiServiceHelper
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil.showToast
import com.black.base.util.RxJavaHelper
import com.black.base.util.SocketDataContainer.cachePair
import com.black.base.util.SocketDataContainer.updateDearPairs
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.util.CallbackObject
import io.reactivex.Observable
import java.util.*

object DearPairService {
    const val ACTION_UPDATE = 1
    const val ACTION_REPLACE = 2
    var dearPairMap: MutableMap<String, Boolean?> = HashMap()
    var hasGotAll = false
    /**
     * 添加自选
     *
     * @param context
     * @param handler
     * @param pair
     * @param callBack
     */
    fun insertDearPair(context: Context?, handler: Handler?, pair: String?, callBack: CallbackObject<Boolean>?) {
        if (context == null || handler == null || pair == null) {
            return
        }
        val userInfo = CookieUtil.getUserInfo(context)
        if (userInfo == null) { //未登录情况
            dearPairMap[pair] = true
            callBack?.callback(true)
            val updatedPairs = HashMap<String, Boolean?>()
            updatedPairs[pair] = true
            updateDearPairs(context, handler, updatedPairs, true)
            return
        }
        //登录情况同步接口
        PairApiServiceHelper.pairCollect(context, pair, object : NormalCallback<HttpRequestResultString?>(context) {

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    dearPairMap[pair] = true
                    callBack?.callback(true)
                    val updatedPairs = HashMap<String, Boolean?>()
                    updatedPairs[pair] = true
                    updateDearPairs(context, handler, updatedPairs, false)
                } else {
                    showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    /**
     * 添加自选
     *
     * @param context
     * @param handler
     * @param pair
     */
    fun insertDearPair(context: Context, handler: Handler?, pair: String): Observable<HttpRequestResultString?>? {
        val userInfo = CookieUtil.getUserInfo(context)
        if (userInfo == null) { //未登录情况
            dearPairMap[pair] = true
            val updatedPairs = HashMap<String, Boolean?>()
            updatedPairs[pair] = true
            updateDearPairs(context, handler, updatedPairs, true)
            val result = HttpRequestResultString()
            result.code = HttpRequestResult.SUCCESS
            return Observable.just(result)
        }
        //登录情况同步接口
        return ApiManager.build(context!!,UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
                ?.pairCollect(pair)
                ?.flatMap { result: HttpRequestResultString? ->
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        dearPairMap[pair] = true
                        val updatedPairs = HashMap<String, Boolean?>()
                        updatedPairs[pair] = true
                        updateDearPairs(context, handler, updatedPairs, false)
                    }
                    Observable.just(result)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 移除自选
     *
     * @param context
     * @param handler
     * @param pair
     * @param callBack
     */
    fun removeDearPair(context: Context?, handler: Handler?, pair: String?, callBack: CallbackObject<Boolean>?) {
        if (context == null || handler == null || pair == null) {
            return
        }
        val userInfo = CookieUtil.getUserInfo(context)
        if (userInfo == null) {
            //未登录情况
            dearPairMap[pair] = false
            callBack?.callback(true)
            val updatedPairs = HashMap<String, Boolean?>()
            updatedPairs[pair] = false
            updateDearPairs(context, handler, updatedPairs, true)
            return
        }
        //已登录同步接口
        PairApiServiceHelper.pairCollectCancel(context, pair, object : NormalCallback<HttpRequestResultString?>(context) {

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    dearPairMap[pair] = false
                    callBack?.callback(true)
                    val updatedPairs = HashMap<String, Boolean?>()
                    updatedPairs[pair] = false
                    updateDearPairs(context, handler, updatedPairs, false)
                } else {
                    showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    /**
     * 移除自选
     *
     * @param context
     * @param handler
     * @param pair
     */
    fun removeDearPair(context: Context, handler: Handler?, pair: String): Observable<HttpRequestResultString?>? {
        val userInfo = CookieUtil.getUserInfo(context)
        if (userInfo == null) { //未登录情况
            dearPairMap[pair] = false
            val updatedPairs = HashMap<String, Boolean?>()
            updatedPairs[pair] = false
            updateDearPairs(context, handler, updatedPairs, true)
            val result = HttpRequestResultString()
            result.code = HttpRequestResult.SUCCESS
            return Observable.just(result)
        }
        //已登录同步接口
        return ApiManager.build(context!!,UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
                ?.pairCollectCancel(pair)
                ?.flatMap { result: HttpRequestResultString? ->
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        dearPairMap[pair] = false
                        val updatedPairs = HashMap<String, Boolean?>()
                        updatedPairs[pair] = false
                        updateDearPairs(context, handler, updatedPairs, false)
                    }
                    Observable.just(result)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 服务器拉自选列表
     *
     * @param context
     * @param handler
     * @param callback
     */
    fun getDearPairList(context: Context?, handler: Handler?, callback: CallbackObject<ArrayList<String?>?>?) {
        if (context == null) {
            return
        }
        PairApiServiceHelper.getCollectPairs(context, object : NormalCallback<HttpRequestResultDataList<String?>?>(context) {
            override fun error(type: Int, error: Any?) {
                callback?.callback(null)
                //                super.error(type, error);
            }

            override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    synchronized(dearPairMap){
                        hasGotAll = true
                        val tempMap: MutableMap<String, Boolean> = HashMap()
                        returnData.data?.let { it ->
                            for (i in it.indices) {
                                it[i]?.let {
                                    tempMap[it] = true
                                }
                            }
                        }
                        dearPairMap.clear()
                        dearPairMap.putAll(tempMap)
                        updateDearPairs(context, handler, dearPairMap, true)
                    }
                } else { //FryingUtil.showToast(context, returnData == null ? "null" : returnData.msg);
                }
                callback?.callback(returnData?.data)
            }
        })
    }

    fun isDearPair(context: Context?, handler: Handler?, pair: String?, callback: CallbackObject<Boolean?>?) {
        if (context == null || pair == null || callback == null || handler == null) {
            return
        }
        //未登录取本地数据进行判断
        if (CookieUtil.getUserInfo(context) == null) {
            var cachePairs: Map<String, Boolean>? = cachePair
            if (cachePairs == null) {
                cachePairs = HashMap(1)
            }
            val isDear = cachePairs[pair]
            callback.callback(isDear ?: false)
            return
        }
        if (hasGotAll) {
            val isDear = dearPairMap[pair]
            callback.callback(isDear ?: false)
        } else {
            getDearPairList(context, handler, object : CallbackObject<ArrayList<String?>?>() {
                override fun callback(returnData: ArrayList<String?>?) {
                    val isDear = dearPairMap[pair]
                    callback.callback(isDear ?: false)
                }
            })
        }
    }

    fun isDearPair(context: Context?, handler: Handler?, pair: String?): Observable<Boolean>? {
        if (context == null || pair == null) {
            return Observable.error(RuntimeException())
        }
        //未登录取本地数据进行判断
        if (CookieUtil.getUserInfo(context) == null) {
            var cachePairs: Map<String, Boolean>? = cachePair
            if (cachePairs == null) {
                cachePairs = HashMap(1)
            }
            val isDear = cachePairs[pair]
            return Observable.just(isDear ?: false)
        }
        return if (hasGotAll) {
            val isDear = dearPairMap[pair]
            Observable.just(isDear ?: false)
        } else {
            ApiManager.build(context,UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
                    ?.getCollectPairs()
                    ?.flatMap { result: HttpRequestResultDataList<String?>? ->
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            hasGotAll = true
                            val tempMap: MutableMap<String, Boolean?> = HashMap()
                            result.data?.let { list ->
                                for (i in list.indices) {
                                    list[i]?.let {
                                        tempMap[it] = true
                                    }
                                }
                            }
                            dearPairMap.clear()
                            dearPairMap.putAll(tempMap)
                            updateDearPairs(context, handler, dearPairMap, false)
                        }
                        val isDear = dearPairMap[pair]
                        Observable.just(isDear ?: false)
                    }
        }
    }
}