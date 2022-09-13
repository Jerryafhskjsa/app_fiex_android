package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import com.black.base.activity.BaseActionBarActivity
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.user.User
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.RSAUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Observer
import skin.support.app.SkinAppCompatViewInflater
import java.util.*
import kotlin.collections.ArrayList

object WalletApiServiceHelper {
    private val coinInfoCache = ArrayList<CoinInfo?>()
    private val walletCache: ArrayList<Wallet?> = ArrayList()
    private val walletLeverCache: ArrayList<WalletLever?> = ArrayList()


    private const val COIN_INFO = 1
    private const val WALLET = 2
    private const val DATA_CACHE_OVER_TIME = 20 * 60 * 1000 //20分钟
            .toLong()
    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long>()
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }

    fun getLastGetTime(type: Int): Long {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }
    /***fiex***/
    //查询支持的账户划转类型
    fun getSupportAccount(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<String?>?>) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getSupportAccount()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    //查询可支持划转的币种
    fun getSupportTransferCoin(context: Context?,formAccount:String,toAccount:String, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<CanTransferCoin?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getSupportCoin(formAccount,toAccount)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    //划转
    fun doTransfer(context: Context?,transferData:AssetTransfer, isShowLoading: Boolean, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.doTransfer(transferData)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
    /***fiex***/


    //获取普通资产
    fun getWalletList(context: Context?, isShowLoading: Boolean, callback: Callback<ArrayList<Wallet?>?>?) {
        if (context == null || callback == null) {
            return
        }
        //        getWalletList(context, "3", isShowLoading, callback);
        getWalletAll(context, "3", isShowLoading, callback, null, callback)
    }

    //获取杠杆资产
    fun getWalletLeverList(context: Context?, isShowLoading: Boolean, callback: Callback<ArrayList<WalletLever?>?>?) {
        if (context == null || callback == null) {
            return
        }
        getWalletAll(context, "4", isShowLoading, null, callback, callback)
    }

    //按类型获取普通资产  type 3 现货 4 杠杆 不传全部
    fun getWalletList(context: Context?, type: String?, isShowLoading: Boolean, callback: Callback<ArrayList<Wallet?>?>?) { //        if (context == null || callback == null) {
//            return;
//        }
//        ApiManager.build(context).getService(WalletApiService.class)
//                .getWallet(type)
//                .compose(RxJavaHelper.observeOnMainThread())
//                .subscribe(new HttpCallbackSimple<>(context, isShowLoading, new Callback<HttpRequestResultData<WalletConfig>?>() {
//                    @Override
//                    public void error(int type, Object error) {
//                        callback.error(type, error);
//                    }
//
//                    @Override
//                    public void callback(HttpRequestResultData<WalletConfig> returnData) {
//                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                            ArrayList<Wallet> normalWalletList = returnData.data == null ? null : returnData.data.userCoinAccountVO;
//                            callback.callback(normalWalletList);
//                        } else {
//                            callback.error(ERROR_NORMAL, returnData == null ? null : returnData.message);
//                        }
//                    }
//                }));
    }

    //按类型全部资产
    fun getWalletAll(context: Context?, isShowLoading: Boolean, walletCallback: Callback<HttpRequestResultData<WalletConfig?>?>?) {
        if (context == null || walletCallback == null) {
            return
        }
        synchronized(walletCache) {
            synchronized(walletLeverCache) {
                val resultData = HttpRequestResultData<WalletConfig?>()
                resultData.code = HttpRequestResult.SUCCESS
                val walletConfig = WalletConfig()
                walletConfig.userCoinAccountVO = gson.fromJson(gson.toJson(walletCache),
                        object : TypeToken<ArrayList<Wallet?>?>() {}.type)
                walletConfig.userCoinAccountLeverVO = gson.fromJson(gson.toJson(walletLeverCache),
                        object : TypeToken<ArrayList<WalletLever?>?>() {}.type)
                resultData.data = walletConfig
                walletCallback.callback(resultData)
            }
        }
        getWalletList(context, null)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, walletCallback))
    }

    //按类型全部资产
    fun getWalletAll(context: Context?, observer: Observer<HttpRequestResultData<WalletConfig?>?>?) {
        if (context == null || observer == null) {
            return
        }
        synchronized(walletCache) {
            synchronized(walletLeverCache) {
                val resultData = HttpRequestResultData<WalletConfig?>()
                resultData.code = HttpRequestResult.SUCCESS
                val walletConfig = WalletConfig()
                walletConfig.userCoinAccountVO = gson.fromJson(gson.toJson(walletCache),
                        object : TypeToken<ArrayList<Wallet?>?>() {}.type)
                walletConfig.userCoinAccountLeverVO = gson.fromJson(gson.toJson(walletLeverCache),
                        object : TypeToken<ArrayList<WalletLever?>?>() {}.type)
                resultData.data = walletConfig
                observer.onNext(resultData)
            }
        }
        getWalletList(context, null)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(observer)
    }

    //按类型获取普通资产  type 3 现货 4 杠杆 不传全部
    fun getWalletAll(context: Context?, type: String?, isShowLoading: Boolean, walletCallback: Callback<ArrayList<Wallet?>?>?, walletLeverCallback: Callback<ArrayList<WalletLever?>?>?, errorCallback: Callback<*>?) {
        if (context == null) {
            return
        }
        val callback = Runnable {
            synchronized(walletCache) {
                walletCallback?.callback(if (walletCache == null) null else gson.fromJson<ArrayList<Wallet?>?>(gson.toJson(walletCache),
                        object : TypeToken<ArrayList<Wallet?>?>() {}.type))
            }
            synchronized(walletLeverCache) {
                walletLeverCallback?.callback(if (walletLeverCache == null) null else gson.fromJson<ArrayList<WalletLever?>?>(gson.toJson(walletLeverCache),
                        object : TypeToken<ArrayList<WalletLever?>?>() {}.type))
            }
        }
        callback.run()
        getWalletList(context, type)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, object : Callback<HttpRequestResultData<WalletConfig?>?>() {
                    override fun error(type: Int, error: Any) {
                        errorCallback?.error(type, error)
                    }
                    override fun callback(returnData: HttpRequestResultData<WalletConfig?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            callback.run()
                        } else {
                            errorCallback?.error(ConstData.ERROR_NORMAL, returnData?.message)
                        }
                    }
                }))
    }

    //获取普通资产
    private fun getWalletList(context: Context?, type: String?): Observable<HttpRequestResultData<WalletConfig?>?>? {
        return if (context == null) {
            Observable.empty()
        } else ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWallet(type)
                ?.flatMap { returnData: HttpRequestResultData<WalletConfig?>? ->
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        if (type == null || "3".equals(type, ignoreCase = true)) {
                            val wallets: ArrayList<Wallet?>? = if (returnData.data == null) ArrayList() else returnData.data!!.userCoinAccountVO
                            synchronized(walletCache) {
                                val list: ArrayList<Wallet?>? = if (wallets == null) ArrayList() else gson.fromJson(gson.toJson(wallets),
                                        object : TypeToken<ArrayList<Wallet?>?>() {}.type)
                                list?.let {
                                    walletCache.clear()
                                    walletCache.addAll(list)
                                }
                            }
                        }
                        if (type == null || "4".equals(type, ignoreCase = true)) {
                            val walletLevers: ArrayList<WalletLever?>? = if (returnData.data == null) ArrayList() else returnData.data!!.userCoinAccountLeverVO
                            synchronized(walletLeverCache) {
                                val list: ArrayList<WalletLever?>? = if (walletLevers == null) ArrayList() else gson.fromJson(gson.toJson(walletLevers),
                                        object : TypeToken<ArrayList<WalletLever?>?>() {}.type)
                                list?.let {
                                    walletLeverCache.clear()
                                    walletLeverCache.addAll(list)
                                }
                            }
                        }
                    }
                    Observable.just(returnData)
                }
    }

    //get冲币地址
    fun getExchangeAddress(context: Context?, coinType: String?, callback: Callback<HttpRequestResultData<WalletAddress?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getExchangeAddress(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //现货资产提币 资产币信息
    fun getWithdrawInfo(context: Context?, coinType: String?, chainType: String?,
                        callback: Callback<HttpRequestResultDataList<WalletWithdrawInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWithdrawInfo(coinType, chainType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //现货资产提币 资产币信息
    fun getWithdrawInfo(context: Context?, coinType: String?, callback: Callback<HttpRequestResultDataList<WalletWithdrawInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWithdrawInfo(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //现货资产提币s 申请
    fun createWithdraw(context: Context?, coinType: String?, withdrawFee: String?, txTo: String?, amount: String?, memo: String?,
                       password: String?, phoneCode: String?, emailCode: String?, googleCode: String?,
                       chainType: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("coinType", coinType)
        jsonObject.addProperty("withdrawFee", withdrawFee)
        jsonObject.addProperty("txTo", txTo)
        jsonObject.addProperty("amount", amount)
        jsonObject.addProperty("memo", memo)
        jsonObject.addProperty("password", password)
        jsonObject.addProperty("phoneCode", phoneCode)
        jsonObject.addProperty("emailCode", emailCode)
        jsonObject.addProperty("googleCode", googleCode)
        //        jsonObject.addProperty("moneyPassword", moneyPassword);
        jsonObject.addProperty("chainType", chainType)
        val rsaParam = jsonObject.toString() + "#" + System.currentTimeMillis()
        val rsa = RSAUtil.encryptDataByPublicKey(rsaParam)
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.createWithdraw(rsa)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //现货资产提币记录
    fun getWithdrawRecord(context: Context?, coinType: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<FinancialRecord?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWithdrawRecord(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //现货资产冲币记录
    fun getRechargeRecord(context: Context?, isShowLoading: Boolean, coinType: String?,
                          callback: Callback<HttpRequestResultDataList<FinancialRecord?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getRechargeRecord(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //现货资产提币 撤销
    fun cancelWithdraw(context: Context?, id: String?,
                       callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.cancelWithdraw(id)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getCoinInfos(context: Context?, callback: Callback<HttpRequestResultDataList<CoinInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getCoins(null)
                ?.flatMap { resultConfig: HttpRequestResultData<CoinInfoConfig?>? ->
                    val resultCoinInfos = HttpRequestResultDataList<CoinInfo?>()
                    resultCoinInfos.code = resultConfig?.code
                    resultCoinInfos.message = resultConfig?.message
                    resultCoinInfos.msg = resultConfig?.msg
                    resultCoinInfos.data = if (resultConfig?.data == null) null else resultConfig.data!!.configs
                    Observable.just(resultCoinInfos)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    private fun getCoinInfoFromCache(coinType: String?, callback: Callback<CoinInfo?>?) {
        if (coinType == null || callback == null) {
            return
        }
        var coinInfo: CoinInfo? = null
        if (coinInfoCache.isNotEmpty()) {
            for (coinInfo1 in coinInfoCache) {
                if (TextUtils.equals(coinType, coinInfo1?.coinType)) {
                    coinInfo = coinInfo1
                    break
                }
            }
        }
        callback.callback(coinInfo)
    }

    private fun getCoinInfoFromCache(coinType: String?): CoinInfo? {
        if (coinType == null) {
            return null
        }
        var coinInfo: CoinInfo? = null
        if (coinInfoCache.isNotEmpty()) {
            for (coinInfo1 in coinInfoCache) {
                if (TextUtils.equals(coinType, coinInfo1?.coinType)) {
                    coinInfo = coinInfo1
                    break
                }
            }
        }
        return coinInfo
    }

    fun getCoinInfo(context: Context?, coinType: String?): Observable<CoinInfo?>? {
        if (context == null) {
            return Observable.empty()
        }
        val lastGetTime = getLastGetTime(COIN_INFO)
        return if (coinInfoCache.isEmpty() || lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            getCoinInfoConfigAndCache(context)
                    ?.flatMap { Observable.just(getCoinInfoFromCache(coinType)) }
                    ?.compose(RxJavaHelper.observeOnMainThread())
        } else {
            Observable.just(getCoinInfoFromCache(coinType))
        }
    }

    fun getCoinInfo(context: Context?, coinType: String?, callback: Callback<CoinInfo?>?) {
        if (context == null || callback == null) {
            return
        }
        val lastGetTime = getLastGetTime(COIN_INFO)
        if (coinInfoCache.isEmpty() || lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            getCoinInfoConfigAndCache(context, object : Callback<ArrayList<CoinInfo?>?>() {
                override fun error(type: Int, error: Any) {
                    callback.error(type, error)
                }

                override fun callback(returnData: ArrayList<CoinInfo?>?) {
                    setLastGetTime(COIN_INFO, System.currentTimeMillis())
                    getCoinInfoFromCache(coinType, callback)
                }
            })
        } else {
            getCoinInfoFromCache(coinType, callback)
        }
    }

    fun getCoinInfoList(context: Context?, callback: Callback<ArrayList<CoinInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        val lastGetTime = getLastGetTime(COIN_INFO)
        if (coinInfoCache.isEmpty() || lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            getCoinInfoConfigAndCache(context, object : Callback<ArrayList<CoinInfo?>?>() {
                override fun error(type: Int, error: Any) {
                    callback.error(type, error)
                }

                override fun callback(returnData: ArrayList<CoinInfo?>?) {
                    callback.callback(returnData)
                }
            })
        } else {
            callback.callback(coinInfoCache)
        }
    }

    fun getCoinInfoConfigAndCache(context: Context?): Observable<ArrayList<CoinInfo?>?>? {
        return if (context == null) {
            Observable.empty()
        } else ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getCoins(null)
                ?.flatMap { resultConfig: HttpRequestResultData<CoinInfoConfig?>? ->
                    synchronized(coinInfoCache) {
                        coinInfoCache.clear()
                        if (resultConfig?.data != null && resultConfig.data!!.configs != null) {
                            coinInfoCache.addAll(resultConfig.data!!.configs!!)
                        }
                    }
                    setLastGetTime(COIN_INFO, System.currentTimeMillis())
                    Observable.just(coinInfoCache)
                }
    }

    fun getCoinInfoConfigAndCache(context: Context?, callback: Callback<ArrayList<CoinInfo?>?>?) {
        if (context == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getCoins(null)
                ?.flatMap { resultConfig: HttpRequestResultData<CoinInfoConfig?>? ->
                    synchronized(coinInfoCache) {
                        coinInfoCache.clear()
                        if (resultConfig?.data != null && resultConfig.data!!.configs != null) {
                            coinInfoCache.addAll(resultConfig.data!!.configs!!)
                        }
                    }
                    setLastGetTime(COIN_INFO, System.currentTimeMillis())
                    Observable.just(coinInfoCache)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //综合账单类型配置
    fun getWalletBillType(context: Context?, callback: Callback<HttpRequestResultDataList<WalletBillType?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWalletBillType()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //综合账单列表
    fun getWalletBill(context: Context?, isShowLoading: Boolean, page: Int,
                      pageSize: Int, billType: String?, coinType: String?, from: String?, to: String?,
                      callback: Callback<HttpRequestResultData<PagingData<WalletBill?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWalletBill(page, pageSize, billType, coinType, from, to)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //综合账单列表
    fun getWalletBillFiex(context: Context?, isShowLoading: Boolean, coinType: String?,
                      callback: Callback<HttpRequestResultData<PagingData<WalletBill?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getWalletBillFiex(coinType)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //币下链列表
    fun getLianListInCoin(context: Context?, isShowLoading: Boolean, coinType: String?, chainType: String?, callback: Callback<HttpRequestResultDataList<LianInCoinModel?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getChainAddress(coinType, chainType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //综合账单列表
    fun getFinanceList(context: Context?, isShowLoading: Boolean, page: Int,
                       pageSize: Int, coinType: String?, txType: String?, startTime: String?, endTime: String?,
                       callback: Callback<HttpRequestResultData<FinancialRecordModel?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getFinanceList(page, pageSize, coinType, txType, startTime, endTime)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //地址列表
    fun getWalletAddressList(context: Context?, page: Int, pageSize: Int, coinType: String?, callback: Callback<HttpRequestResultDataList<WalletWithdrawAddress?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWalletAddressList(page, pageSize, coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //添加地址
    fun addWalletAddress(context: Context?, coinType: String?, name: String?, address: String?, memo: String?, verifyCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.addWalletAddress(coinType, name, address, memo, verifyCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //删除地址
    fun deleteWalletAddress(context: Context?, id: String?, verifyCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.deleteWalletAddress(id, verifyCode)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun clearCache() {
        lastGetTimeMap.clear()
        coinInfoCache.clear()
    }

    //现货资产提币、冲币记录 type 0 充币 1 提币
    fun getWalletRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, type: Int, coinType: String?, callback: Callback<HttpRequestResultData<PagingData<FinancialRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(WalletApiService::class.java)
                ?.getWalletRecord(page, pageSize, type, coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
}