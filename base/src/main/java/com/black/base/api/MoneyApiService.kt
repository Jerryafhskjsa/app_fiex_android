package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.money.*
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

interface MoneyApiService {
    //发售列表
    @GET(UrlConfig.Money.URL_PROMOTIONS_LIST)
    fun getPromotionsList(): Observable<HttpRequestResultData<PromotionsConfig?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_PROMOTIONS_ADD)
    fun rushPromotions(@Field("val") rsa: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_RECORD)
    fun getPromotionsRecord(@Query("pageNum") page: Int, @Query("pageSize") pageSize: Int): Observable<HttpRequestResultData<PagingData<PromotionsRecord?>?>?>?

    //发售列表
    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY)
    fun getPromotionsBuy(@Query("language") language: Int, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<PromotionsBuy?>?>?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_DETAIL)
    fun getPromotionsBuyDetail(@Query("purchaseId") purchaseId: String?): Observable<HttpRequestResultData<PromotionsBuyDetail?>?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_USER_INFO)
    fun getPromotionsBuyUserInfo(@Query("purchaseId") purchaseId: String?): Observable<HttpRequestResultData<PromotionsBuyUserInfo?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_PROMOTIONS_BUY_CREATE)
    fun promotionsBuyCreate(@Field("amount") amount: String?, @Field("coinType") coinType: String?, @Field("purchaseId") purchaseId: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_RECORD)
    fun getPromotionsBuyRecord(@Query("purchaseId") purchaseId: String?, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<PromotionsBuyRecord?>?>?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_FIVE)
    fun getPromotionsBuyFive(@Query("language") language: Int, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<PromotionsBuyFive?>?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_PROMOTIONS_BUY_FIVE_CREATE)
    fun promotionsBuyFiveCreate(@Field("amount") amount: String?, @Field("coinType") coinType: String?, @Field("purchaseId") purchaseId: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_FIVE_DETAIL)
    fun getPromotionsBuyFiveDetail(@Query("purchaseId") purchaseId: String?, @Query("type") type: Int): Observable<HttpRequestResultData<PromotionsBuyFiveDetail?>?>?

    @GET(UrlConfig.Money.URL_PROMOTIONS_BUY_FIVE_RECORD)
    fun getPromotionsBuyFiveRecord(@Query("purchaseId") purchaseId: String?): Observable<HttpRequestResultDataList<PromotionsBuyFiveRecord?>?>?

    @GET(UrlConfig.Money.URL_DEMAND_CONFIG)
    fun getDemandConfig(): Observable<HttpRequestResultData<DemandConfig?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_DEMAND_CHANGE_IN)
    fun postDemandChangeIn(@Field("amount") amount: String?, @Field("coinType") coinType: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_DEMAND_REWARD_RECORD)
    fun getDemandRewardRecord(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<DemandRecord?>?>?>?

    @GET(UrlConfig.Money.URL_DEMAND_LOCK_RECORD)
    fun getDemandLockRecord(@Query("coinType") coinType: String?, @Query("type") type: String?, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<DemandLock?>?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_DEMAND_CHANGE_OUT)
    fun postDemandChangeOut(@Field("unLockId") lockId: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_DEMAND_CHANGE_OUT_BATCH)
    fun postDemandChangeOutBatch(@Field("coinType") coinType: String?, @Field("all") all: Boolean, @Field("lockIds") lockIds: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_REGULAR_CONFIG)
    fun getRegularConfig(): Observable<HttpRequestResultData<RegularConfig?>?>?

    @GET(UrlConfig.Money.URL_REGULAR_LOCK_RECORD)
    fun getRegularLockRecord(@Query("pledgeId") regularId: String?, @Query("coinType") coinType: String?, @Query("type") type: String?, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<RegularLock?>?>?>?

    @GET(UrlConfig.Money.URL_REGULAR_LOCK_HISTORY)
    fun getRegularLockHistory(@Query("coinType") coinType: String?, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<RegularLock?>?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_REGULAR_CHANGE_IN)
    fun postRegularChangeIn(@Field("amount") amount: String?, @Field("pledgeId") regularId: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_REGULAR_CHANGE_OUT)
    fun postRegularChangeOut(@Field("lockId") regularLockId: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_LOAN_CONFIG)
    fun getLoanConfig(): Observable<HttpRequestResultDataList<LoanConfig?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_LOAN_CREATE)
    fun createLoan(@Field("mortgageCoinType") mortgageCoinType: String?, @Field("borrowCoinType") loanCoinType: String?, @Field("mortgageAmount") mortgageAmount: String?, @Field("borrowAmount") loanAmount: String?, @Field("numberDays") days: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_LOAN_RECORD)
    fun getLoanRecord(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<LoanRecord?>?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_LOAN_ADD_DEPOSIT)
    fun addLoanDeposit(@Field("id") loanId: String?, @Field("mortgageAmount") mortgageAmount: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_LOAN_BACK)
    fun backLoan(@Field("id") loanId: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_MONEY_HOME)
    fun getMoneyHomeConfig(): Observable<HttpRequestResultData<MoneyHomeConfig?>?>?

    @GET(UrlConfig.Money.URL_LOAN_ADD_DEPOSIT_RECORD)
    fun getLoanAddDepositRecord(@Query("id") loanId: String?, @Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<LoanAddDepositRecord?>?>?>?

    @GET(UrlConfig.Money.URL_LOAN_RECORD_DETAIL)
    fun getLoanRecordDetail(@Query("id") loanRecordId: String?): Observable<HttpRequestResultData<LoanRecordDetail?>?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_CONFIG)
    fun getCloudPowerConfig(): Observable<HttpRequestResultDataList<CloudPowerProject?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Money.URL_CLOUD_POWER_BUY)
    fun buyCloudPower(@Field("miningId") cloudPowerId: String?, @Field("amount") amount: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_HOLD_RECORD)
    fun getCloudPowerHoldRecord(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultDataList<CloudPowerHoldRecord?>?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_BUY_RECORD)
    fun getCloudPowerBuyRecord(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<CloudPowerBuyRecord?>?>?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_REWARD_RECORD)
    fun getCloudPowerRewardRecord(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<CloudPowerRewardRecord?>?>?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_BTC_INCOME)
    fun getCloudPowerBtcIncome(): Observable<HttpRequestResultData<Double?>?>?

    @GET(UrlConfig.Money.URL_CLOUD_POWER_SUMMARY)
    fun getCloudPowerPersonHold(): Observable<HttpRequestResultData<CloudPowerPersonHold?>?>?
}