package com.black.base.api

import com.black.base.model.*
import com.black.base.model.wallet.*
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*
import java.math.BigDecimal

//钱包相关
interface WalletApiService {
    /***fiex***/
    @GET(UrlConfig.Wallet.URL_GET_SUPPORT_ACCOUNT)
    fun getSupportAccount(): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Wallet.URL_GET_SUPPORT_COIN)
    fun getSupportCoin(@Query("from") fromAccount:String?,@Query("to") toAccount:String?): Observable<HttpRequestResultDataList<CanTransferCoin?>?>?

    @POST(UrlConfig.Wallet.URL_TRANSFER)
    fun doTransfer(@Body bean:AssetTransfer?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Wallet.URL_WALLET_TRANSFER_RECORD)
    fun getWalletTransferRecord(@Query("coin") coin: String?,@Query("page") page: Int?, @Query("size") pageSize: Int?, @Query("fromType") fromType: String?,@Query("toType") toType: String?): Observable<HttpRequestResultData<PagingData<WalletTransferRecord?>?>?>?

    @GET(UrlConfig.Wallet.URL_WALLET_BILL_FIEX)
    fun getWalletBillFiex(@Query("coin") coin: String?): Observable<HttpRequestResultData<PagingData<WalletBill?>?>?>?

    /***fiex***/

    // type 3 现货 4 杠杆 不传全部
    @GET(UrlConfig.Wallet.URL_WALLET)
    fun getWallet(@Query("type") type: String?): Observable<HttpRequestResultData<WalletConfig?>?>?

    @GET(UrlConfig.Wallet.URL_WITHDRAW)
    fun getWithdrawInfo(@Query("coinType") coinType: String?, @Query("chainType") chainType: String?): Observable<HttpRequestResultDataList<WalletWithdrawInfo?>?>?

    @GET(UrlConfig.Wallet.URL_WITHDRAW)
    fun getWithdrawInfo(@Query("coinType") coinType: String?): Observable<HttpRequestResultDataList<WalletWithdrawInfo?>?>?

    @GET(UrlConfig.Wallet.URL_WITHDRAW_QUERY)
    fun getWithdrawRecord(@Query("coinType") coinType: String?): Observable<HttpRequestResultDataList<FinancialRecord?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_WITHDRAW_CREATE)
    fun createWithdraw(@Field("val") rsa: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_WITHDRAW_CANCEL)
    fun cancelWithdraw(@Field("id") id: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Wallet.URL_DEPOSIT)
    fun getRechargeRecord(@Query("coinType") coinType: String?): Observable<HttpRequestResultDataList<FinancialRecord?>?>?

    @GET(UrlConfig.Wallet.URL_RECHARGE_ADDRESS_POST)
    fun getExchangeAddress(@Query("coinType") coinType: String?): Observable<HttpRequestResultData<WalletAddress?>?>?

    @GET(UrlConfig.Wallet.URL_LIAN_IN_COIN)
    fun getChainAddress(@Query("coinType") coinType: String?, @Query("chainType") chainType: String?): Observable<HttpRequestResultDataList<LianInCoinModel?>?>?

    @GET(UrlConfig.Wallet.URL_COINS)
    fun getCoins(@Query("coinTypes") coinType: String?): Observable<HttpRequestResultData<CoinInfoConfig?>?>?

    @GET(UrlConfig.Wallet.URL_WALLET_BILL_TYPE)
    fun getWalletBillType(): Observable<HttpRequestResultDataList<WalletBillType?>?>?

    @GET(UrlConfig.Wallet.URL_WALLET_BILL)
    fun getWalletBill(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("billType") billType: String?, @Query("coinType") coinType: String?, @Query("from") from: String?, @Query("to") to: String?): Observable<HttpRequestResultData<PagingData<WalletBill?>?>?>?

    @GET(UrlConfig.Wallet.URL_FINANCE_LIST)
    fun getFinanceList(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("coinType") coinType: String?, @Query("txType") txType: String?, @Query("startTime") startTime: String?, @Query("endTime") endTime: String?): Observable<HttpRequestResultData<FinancialRecordModel?>?>?

    //    @GET(UrlConfig.Wallet.URL_LIAN_IN_COIN)
//    Observable<HttpRequestResultDataList<LianInCoinModel>> getLianInCoin(@Query("coinType") String coinType);

    //    @GET(UrlConfig.Wallet.URL_LIAN_IN_COIN)
//    Observable<HttpRequestResultDataList<LianInCoinModel>> getLianInCoin(@Query("coinType") String coinType);
    @GET(UrlConfig.Wallet.URL_ADDRESS_LIST)
    fun getWalletAddressList(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("coinType") coinType: String?): Observable<HttpRequestResultDataList<WalletWithdrawAddress?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_ADDRESS_ADD)
    fun addWalletAddress(@Field("coinType") coinType: String?, @Field("name") name: String?, @Field("coinWallet") address: String?, @Field("memo") memo: String?, @Field("verifyCode") verifyCode: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_ADDRESS_DELETE)
    fun deleteWalletAddress(@Path("id") id: String?, @Field("verifyCode") verifyCode: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Wallet.URL_WALLET_RECORD)
    fun getWalletRecord(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("type") type: Int, @Query("coinType") coinType: String?): Observable<HttpRequestResultData<PagingData<FinancialRecord?>?>?>?

    //type 1 币币到杠杆 2 杠杆到币币
    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_WALLET_TRANSFER)
    fun walletTransfer(@Field("coinType") coinType: String?, @Field("amount") amount: String?, @Field("type") type: String?, @Field("pair") pair: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Wallet.URL_WALLET_LEVER_DETAIL)
    fun getWalletLeverDetail(@Query("pair") pair: String?): Observable<HttpRequestResultData<WalletLeverDetail?>?>?

    // type REPAYMENT 还币  BORROW 借币
    @FormUrlEncoded
    @POST(UrlConfig.Wallet.URL_LEVER_BORROW)
    fun walletLeverBorrow(@Field("amount") amount: String?, @Field("coinType") coinType: String?, @Field("pair") pair: String?, @Field("type") type: String?): Observable<HttpRequestResultString?>?

    // type REPAYMENT 还币  BORROW 借币
    @GET(UrlConfig.Wallet.URL_LEVER_BORROW_RECORD)
    fun getLeverBorrowRecord(@Query("page") page: Int, @Query("size") pageSize: Int, @Query("pair") pair: String?, @Query("operationType") type: String?): Observable<HttpRequestResultData<PagingData<LeverBorrowRecord?>?>?>?
}