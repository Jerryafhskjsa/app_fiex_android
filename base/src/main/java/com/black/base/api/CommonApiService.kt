package com.black.base.api

import com.black.base.model.*
import com.black.base.model.clutter.*
import com.black.base.model.socket.PairDescription
import com.black.base.model.socket.PairStatus
import com.black.base.util.UrlConfig
import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CommonApiService {

    @GET(UrlConfig.Config.URL_NETWORK_LINES)
    fun getNetworkLines():Observable<HttpRequestResultDataList<FryingLinesConfig?>?>?

    @GET(UrlConfig.Config.RUL_LINE_SPEED)
    fun getLinesSpeed():Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Config.URL_KLINE_HISTORY)
    fun getHistoryKline(@Query("symbol") symbol: String?,
                        @Query("interval") interval: String?,
                        @Query("limit") limit: Int?,
                        @Query("startTime") startTime:Long,
                        @Query("endTime") endTime:Long): Observable<HttpRequestResultDataList<Kline?>?>?

    @GET(UrlConfig.Config.URL_UPDATE)
    fun checkUpdate(@Query("platform") platform: String?): Observable<HttpRequestResultData<Update?>?>?

    @GET(UrlConfig.Config.URL_COUNTRY_CODE_LIST)
    fun getCountryCodeList(): Observable<HttpRequestResultDataList<CountryCode?>?>?

    @GET(UrlConfig.Config.URL_NOTICE)
    fun getNoticeList(@Path("language") lang: String?, @Path("pageNum") pageNum: Int, @Path("pageSize") pageSize: Int): Observable<HttpRequestResultData<NoticeData?>?>?

    //type=1 首页， type=2 邀请  type=3 理财
    @GET(UrlConfig.Config.URL_BANNER_LIST)
    fun getHomePageMainBannerList(@Query("language") lang: String?, @Query("level") level: String?, @Query("type") type: String?): Observable<HttpRequestResultDataList<Banner?>?>?

//    Observable<HttpRequestResultDataList<Banner>> getHomePageMainBannerList(@Query("language") String lang, @Query("level") String level, @Query("type") String type);

    //    Observable<HttpRequestResultDataList<Banner>> getHomePageMainBannerList(@Query("language") String lang, @Query("level") String level, @Query("type") String type);
    @GET(UrlConfig.Config.URL_NEWS)
    fun getNewsList(): Observable<HttpRequestResultDataList<News?>?>?

    @GET(UrlConfig.Config.URL_FORUM_LIST)
    fun getForumList(@Query("page") page: Int, @Query("size") pageSize: Int): Observable<HttpRequestResultData<PagingData<Forum?>?>?>?

    @GET(UrlConfig.Config.URL_NOTICE_HOME)
    fun getNoticeHome(@Path("language") languageKey: String?, @Query("per_page") pageSize: Int, @Query("page") page: Int): Observable<NoticeHome?>?

    @GET(UrlConfig.Config.URL_PAIR_DESCRIPTION)
    fun getPairDescription(@Query("currency") coinName: String?, @Query("language") lang: String?): Observable<HttpRequestResultData<PairDescription?>?>?

    @GET(UrlConfig.Config.URL_GLOBAL_AD)
    fun getGlobalAd(@Query("language") language: String?, @Query("noticeName") noticeName: String?): Observable<HttpRequestResultData<GlobalAd?>?>?

    @GET(UrlConfig.Config.URL_USDT_CNY_PRICE)
    fun getUsdtCnyPrice(): Observable<HttpRequestResultData<CoinUsdtPrice?>?>?

    @GET(UrlConfig.Config.URL_GEETEST_INIT)
    fun geetestInit(): Observable<HttpRequestResultData<JsonObject?>?>?

    @GET(UrlConfig.Config.URL_MY_POSTER)
    fun getMyPosterList(): Observable<HttpRequestResultDataList<String?>?>?

    @GET(UrlConfig.Config.URL_INVITE_URL)
    fun getInviteUrl(): Observable<HttpRequestResultString?>?
}
