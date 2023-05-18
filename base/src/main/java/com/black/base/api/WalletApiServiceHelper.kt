package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import com.black.base.activity.BaseActionBarActivity
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.user.User
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import skin.support.app.SkinAppCompatViewInflater
import java.util.*
import kotlin.collections.ArrayList

object WalletApiServiceHelper {
    //所有币种配置信息
    private val coinInfoCache = ArrayList<CoinInfoType?>()
    private val walletCache: ArrayList<Wallet?> = ArrayList()
    private val walletLeverCache: ArrayList<WalletLever?> = ArrayList()

    var userBalanceWrapperCache:UserBalanceWarpper = UserBalanceWarpper()

    private const val COIN_INFO = 1
    private const val WALLET = 2
    private const val DATA_CACHE_OVER_TIME = 0.5 * 60 * 1000 //20分钟
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
    //获取用户24小时提现额度
    fun getUserWithdrawQuota(context: Context,coin:String,callback:Callback<HttpRequestResultString?>){
        ApiManager.build(context!!,true,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getUserWithdrawQuota(coin)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context,callback))
    }
    //获取普通资产
    fun getUserBalanceReal(context: Context?, isShowLoading: Boolean, callback: Callback<UserBalanceWarpper?>?, errorCallback: Callback<Any?>?) {
        if (context == null || callback == null) {
            return
        }
        getUserBalance(context, false, callback, errorCallback)
    }

    private fun getUserBalance(context: Context, isShowLoading: Boolean, userBalance: Callback<UserBalanceWarpper?>?, errorCallback: Callback<*>?){
        if (context == null) {
            return
        }
        var callback = Runnable {
            synchronized(userBalanceWrapperCache) {
                userBalance?.callback(if (userBalanceWrapperCache == null) null else gson.fromJson<UserBalanceWarpper?>(
                    gson.toJson(userBalanceWrapperCache),
                    object : TypeToken<UserBalanceWarpper?>() {}.type))
            }
        }
        callback.run()
        getUserBalance(context)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, object : Callback<HttpRequestResultData<UserBalanceWarpper?>?>() {
                override fun error(type: Int, error: Any) {
                    errorCallback?.error(type, error)
                }
                override fun callback(returnData: HttpRequestResultData<UserBalanceWarpper?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        callback.run()
                    } else {
                        errorCallback?.error(ConstData.ERROR_NORMAL, returnData?.message)
                    }
                }
            }))
    }


    fun getUserBalance(context: Context?): Observable<HttpRequestResultData<UserBalanceWarpper?>?>?{
        return if(context == null){
            Observable.empty()
        }else ApiManager.build(context, false, UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getUserBalance()
            ?.flatMap { returnData: HttpRequestResultData<UserBalanceWarpper?>? ->
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val balance: UserBalanceWarpper? =
                        if (returnData?.data == null) UserBalanceWarpper() else returnData?.data!!
                    synchronized(userBalanceWrapperCache) {
                        val balanceWrapper: UserBalanceWarpper? =
                            if (balance == null) UserBalanceWarpper() else gson.fromJson(gson.toJson(balance),
                                object : TypeToken<UserBalanceWarpper?>() {}.type
                            )
                        balanceWrapper?.let {
                            userBalanceWrapperCache = balanceWrapper
                        }
                    }
                }
                Observable.just(returnData)
            }
    }


    //查询支持的账户划转类型
    fun getSupportAccount(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<AssetTransferTypeList?>?>) {
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
//        getWalletList(context, null)
//                ?.compose(RxJavaHelper.observeOnMainThread())
//                ?.subscribe(HttpCallbackSimple(context, isShowLoading, walletCallback))
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
//        getWalletList(context, null)
//                ?.compose(RxJavaHelper.observeOnMainThread())
//                ?.subscribe(observer)
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
//        getWalletList(context, type)
//                ?.compose(RxJavaHelper.observeOnMainThread())
//                ?.subscribe(HttpCallbackSimple(context, isShowLoading, object : Callback<HttpRequestResultData<WalletConfig?>?>() {
//                    override fun error(type: Int, error: Any) {
//                        errorCallback?.error(type, error)
//                    }
//                    override fun callback(returnData: HttpRequestResultData<WalletConfig?>?) {
//                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                            callback.run()
//                        } else {
//                            errorCallback?.error(ConstData.ERROR_NORMAL, returnData?.message)
//                        }
//                    }
//                }))
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

    //现货资产提币 撤销
    fun cancelWithdraw(context: Context?, id: String?,
                       callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.cancelWithdraw(id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    private fun getCoinInfoFromCache(coinType: String?, callback: Callback<CoinInfoType?>?) {
        if (coinType == null || callback == null) {
            return

        }
        var coinInfo: CoinInfoType? = null
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

    private fun getCoinInfoFromCache(coinType: String?): CoinInfoType? {
        if (coinType == null) {
            return null
        }
        var coinInfo: CoinInfoType? = null
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

    fun getCoinInfo(context: Context?, coinType: String?): Observable<CoinInfoType?>? {
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

    fun getCoinInfo(context: Context?, coinType: String?, callback: Callback<CoinInfoType?>?) {
        if (context == null || callback == null) {
            return
        }
        val lastGetTime = getLastGetTime(COIN_INFO)
        if (coinInfoCache.isEmpty() || lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            getCoinInfoConfigAndCache(context, object : Callback<ArrayList<CoinInfoType?>?>() {
                override fun error(type: Int, error: Any) {
                    callback.error(type, error)
                }

                override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                    setLastGetTime(COIN_INFO, System.currentTimeMillis())
                    getCoinInfoFromCache(coinType, callback)
                }
            })
        } else {
            getCoinInfoFromCache(coinType, callback)
        }
    }

    fun getCoinInfoList(context: Context?, callback: Callback<ArrayList<CoinInfoType?>?>?) {
        if (context == null || callback == null) {
            return
        }
        val lastGetTime = getLastGetTime(COIN_INFO)
        if (coinInfoCache.isEmpty() || lastGetTime == null || System.currentTimeMillis() - lastGetTime > DATA_CACHE_OVER_TIME) {
            getCoinInfoConfigAndCache(context, object : Callback<ArrayList<CoinInfoType?>?>() {
                override fun error(type: Int, error: Any) {
                    callback.error(type, error)
                }

                override fun callback(returnData: ArrayList<CoinInfoType?>?) {
                    callback.callback(returnData)
                }
            })
        } else {
            callback.callback(coinInfoCache)
        }
    }

    private fun getCoinInfoConfigAndCache(context: Context?): Observable<ArrayList<CoinInfoType?>?>? {
        return if (context == null) {
            Observable.empty()
        } else ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
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

    fun getCoinInfoConfigAndCache(context: Context?, callback: Callback<ArrayList<CoinInfoType?>?>?) {
        if (context == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
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
    fun getWalletBillFiex(context: Context?, isShowLoading: Boolean, coinType: String?,direction: String?,id:String?,
                          callback: Callback<HttpRequestResultData<PagingData<WalletBill?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getWalletBillFiex(coinType,direction,id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }



    //提现地址列表
    fun getWalletAddressList(context: Context?,coinType: String?, callback: Callback<HttpRequestResultDataList<WalletWithdrawAddress?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getWalletAddressList(coinType)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //添加地址
    fun addWalletAddress(context: Context?, coinType: String?, name: String?, address: String?, memo: String?, verifyCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.addWalletAddress(coinType, name, address, memo, verifyCode)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //更新地址
    fun updateWalletAddress(context: Context?,id:String?, coinType: String?, name: String?, address: String?, memo: String?, verifyCode: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.updateWalletAddress(id,coinType, name, address, memo, verifyCode)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun deleteWithdrawAddress(context: Context?,id:String?,isShowLoading: Boolean,callback: Callback<HttpRequestResultString?>?){
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.deleteWalletAddress(id)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    //现货资产提币、冲币记录 type 0 充币 1 提币
    fun getWalletRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int,total: Int, type: Int, coinType: String?, callback: Callback<HttpRequestResultData<PagingData<FinancialRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getWalletRecord(page, pageSize, total, type, coinType)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun clearCache() {
        lastGetTimeMap.clear()
        coinInfoCache.clear()
    }


    /*fun getDepositCreate(context: Context?, payVO: PayVO, callback: Callback<HttpRequestResultData<payOrder?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getDepositCreate(payVO)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getDepositConfirm(context: Context?, orderId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getDepositConfirm(orderId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getDepositOrderCodeList(context: Context?, callback: Callback<HttpRequestResultData<Deposit<OrderCode?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getDepositCodeList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getDepositOrderList(context: Context?,isShowLoading: Boolean, orderType:String?, page: Int?,size : Int?, callback: Callback<HttpRequestResultData<PagingData<payOrder?>?>?>) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getDepositList(orderType,page,size)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }*/
}