package com.black.base.api

import android.content.Context
import com.black.base.api.CommonApiServiceHelper.geetestInit
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.filter.GeeTestResult
import com.black.base.model.money.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.FryingUtil.showToast
import com.black.base.util.GeeTestCallback
import com.black.base.util.GeeTestHelper.GeeTestApi1Callback
import com.black.base.util.GeeTestHelper.GeeTestApi2Callback
import com.black.base.util.GeeTestInterface
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.RSAUtil
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject

object MoneyApiServiceHelper {
    //发售配置
    fun getPromotionsList(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PromotionsConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsList()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //抢购币 {foundationId:"",amount:"",geetest:""}

    fun rushPromotionsNew(context: Context?, promotionId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("foundationId", promotionId)
        jsonObject.addProperty("amount", amount)
        //jsonObject.addProperty("geetest", null)
        val list = JsonArray()
        val time = System.currentTimeMillis()
        list.add(time)
        jsonObject.add("list", list)
        val rsaParam = "$jsonObject#$time"
        val rsa = RSAUtil.encryptDataByPublicKey(rsaParam)
        ApiManager.build(context, UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
            ?.rushPromotions(rsa)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))

    }
    fun rushPromotions(context: Context?, promotionId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        if (context is GeeTestInterface) {
            (context as GeeTestInterface).startVerify(object : GeeTestCallback {
                override fun onApi1(api1Callback: GeeTestApi1Callback?) {
                    geetestInit(context, object : NormalCallback<HttpRequestResultData<JsonObject?>?>(context) {
                        override fun error(type: Int, error: Any?) {
                            verifyNext(null)
                        }

                        override fun callback(returnData: HttpRequestResultData<JsonObject?>?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                // TODO 设置返回api1数据，即使为null也要设置，SDK内部已处理
//                            JSONObject jsonObject = returnData.data == null ? null : returnData.data.toJson();
                                var jsonObject: JSONObject? = null
                                try {
                                    jsonObject = JSONObject(returnData.data.toString())
                                } catch (e: JSONException) {
                                }
                                verifyNext(jsonObject)
                            } else {
                                showToast(context, if (returnData == null) "null" else returnData.msg)
                                verifyNext(null)
                            }
                        }

                        private fun verifyNext(jsonObject: JSONObject?) {
                            api1Callback?.callback(jsonObject)
                        }
                    })
                }

                override fun onApi2(result: String?, api2Callback: GeeTestApi2Callback?) {
                    api2Callback?.dismiss()
                    val addCallback: Callback<HttpRequestResultString?> = object : Callback<HttpRequestResultString?>() {
                        override fun error(type: Int, error: Any) {
                            callback.error(type, error)
                            //                            api2Callback.callback(false);
                        }

                        override fun callback(returnData: HttpRequestResultString?) {
                            //                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                                api2Callback.callback(true);
//                            } else {
//                                api2Callback.callback(false);
//                            }
                            callback.callback(returnData)
                        }
                    }
                    val geeTestResult = Gson().fromJson(result, GeeTestResult::class.java)
                    val jsonObject = JsonObject()
                    jsonObject.addProperty("foundationId", promotionId)
                    jsonObject.addProperty("amount", amount)
                    jsonObject.addProperty("geetest", geeTestResult.toJsonString())
                    val list = JsonArray()
                    val time = System.currentTimeMillis()
                    list.add(time)
                    jsonObject.add("list", list)
                    val rsaParam = "$jsonObject#$time"
                    val rsa = RSAUtil.encryptDataByPublicKey(rsaParam)
                    ApiManager.build(context, UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                            ?.rushPromotions(rsa)
                            ?.compose(RxJavaHelper.observeOnMainThread())
                            ?.subscribe(HttpCallbackSimple(context, true, addCallback))
                }
            })
        } else {
            throw RuntimeException(context.javaClass.simpleName + "  must implements GeeTestInterface")
        }
    }

    //抢购记录
    fun getPromotionsRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<PromotionsRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //申购列表
    fun getPromotionsBuyList(context: Context?, language: Int, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<PromotionsBuy?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuy(language, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getPromotionsBuyDetail(context: Context?, purchaseId: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PromotionsBuyDetail?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyDetail(purchaseId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getPromotionsBuyUserInfo(context: Context?, purchaseId: String?, callback: Callback<HttpRequestResultData<PromotionsBuyUserInfo?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyUserInfo(purchaseId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun promotionsBuyCreate(context: Context?, amount: String?, coinType: String?, purchaseId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.promotionsBuyCreate(amount, coinType, purchaseId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //申购记录
    fun getPromotionsBuyRecord(context: Context?, isShowLoading: Boolean, purchaseId: String?, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<PromotionsBuyRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyRecord(purchaseId, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //5折申购列表
    fun getPromotionsBuyFiveList(context: Context?, language: Int, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<PromotionsBuyFive?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyFive(language, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //5折申购项目详情
    fun getPromotionsBuyFiveDetail(context: Context?, purchaseId: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PromotionsBuyFiveDetail?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyFiveDetail(purchaseId, 1)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //5折申购 创建
    fun promotionsBuyFiveCreate(context: Context?, amount: String?, coinType: String?, purchaseId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.promotionsBuyFiveCreate(amount, coinType, purchaseId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //5折申购记录
    fun getPromotionsBuyFiveRecord(context: Context?, isShowLoading: Boolean, purchaseId: String?, callback: Callback<HttpRequestResultDataList<PromotionsBuyFiveRecord?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getPromotionsBuyFiveRecord(purchaseId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆配置 活
    fun getDemandConfig(context: Context?, callback: Callback<HttpRequestResultData<DemandConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getDemandConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //聚宝盆转入 活
    fun postDemandChangeIn(context: Context?, coinType: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.postDemandChangeIn(amount, coinType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //聚宝盆奖励记录 活
    fun getDemandRewardRecord(context: Context?, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<DemandRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getDemandRewardRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆锁仓记录 活
    fun getDemandLockRecord(context: Context?, coinType: String?, type: String?, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<DemandLock?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getDemandLockRecord(coinType, type, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆转出 活
    fun postDemandChangeOut(context: Context?, lockId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.postDemandChangeOut(lockId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //聚宝盆转出 活
    fun postDemandChangeOutBatch(context: Context?, coinType: String?, all: Boolean, lockIds: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.postDemandChangeOutBatch(coinType, all, lockIds)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //聚宝盆配置 定
    fun getRegularConfig(context: Context?, callback: Callback<HttpRequestResultData<RegularConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getRegularConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //聚宝盆锁仓记录 定
    fun getRegularLockRecord(context: Context?, regularId: String?, coinType: String?, type: String?, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<RegularLock?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getRegularLockRecord(regularId, coinType, type, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆锁仓历史记录  定
    fun getRegularLockHistory(context: Context?, coinType: String?, page: Int, pageSize: Int, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<RegularLock?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getRegularLockHistory(coinType, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆转入 活
    fun postRegularChangeIn(context: Context?, amount: String?, regularId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.postRegularChangeIn(amount, regularId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //聚宝盆转出  定 违约
    fun postRegularChangeOut(context: Context?, regularLockId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.postRegularChangeOut(regularLockId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //抵押借贷配置
    fun getLoanConfig(context: Context?, callback: Callback<HttpRequestResultDataList<LoanConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getLoanConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //抵押借贷创建
    fun createLoan(context: Context?, mortgageCoinType: String?, loanCoinType: String?, mortgageAmount: String?, loanAmount: String?, days: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.createLoan(mortgageCoinType, loanCoinType, mortgageAmount, loanAmount, days)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //抵押借贷记录
    fun getLoanRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<LoanRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getLoanRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //抵押借贷追加保证金
    fun addLoanDeposit(context: Context?, loanId: String?, mortgageAmount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.addLoanDeposit(loanId, mortgageAmount)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //抵押借贷还贷
    fun backLoan(context: Context?, loanId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.backLoan(loanId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //聚宝盆首页配置
    fun getMoneyHomeConfig(context: Context?, callback: Callback<HttpRequestResultData<MoneyHomeConfig?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getMoneyHomeConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //抵押借贷追加记录
    fun getLoanAddDepositRecord(context: Context?, isShowLoading: Boolean, loanId: String?, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<LoanAddDepositRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getLoanAddDepositRecord(loanId, page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //聚宝盆首页配置
    fun getLoanRecordDetail(context: Context?, loanRecordId: String?, callback: Callback<HttpRequestResultData<LoanRecordDetail?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getLoanRecordDetail(loanRecordId)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //运算力配置
    fun getCloudPowerConfig(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<CloudPowerProject?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerConfig()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //运算力购买
    fun buyCloudPower(context: Context?, cloudPowerId: String?, amount: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.buyCloudPower(cloudPowerId, amount)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    //运算力持仓记录
    fun getCloudPowerHoldRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, callback: Callback<HttpRequestResultDataList<CloudPowerHoldRecord?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerHoldRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //运算力购买记录
    fun getCloudPowerBuyRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<CloudPowerBuyRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerBuyRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //运算力收益记录
    fun getCloudPowerRewardRecord(context: Context?, isShowLoading: Boolean, page: Int, pageSize: Int, callback: Callback<HttpRequestResultData<PagingData<CloudPowerRewardRecord?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerRewardRecord(page, pageSize)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    //云算力BTC理论收益
    fun getCloudPowerBtcIncome(context: Context?, callback: Callback<HttpRequestResultData<Double?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerBtcIncome()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    //云算力个人收益和持仓
    fun getCloudPowerPersonHold(context: Context?, callback: Callback<HttpRequestResultData<CloudPowerPersonHold?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,UrlConfig.ApiType.URL_PRO).getService(MoneyApiService::class.java)
                ?.getCloudPowerPersonHold()
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }
}