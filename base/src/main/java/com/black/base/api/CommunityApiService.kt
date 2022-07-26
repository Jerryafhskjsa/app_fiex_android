package com.black.base.api

import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.community.*
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.*

//社交相关api
interface CommunityApiService {
    @GET(UrlConfig.Community.URL_FACTION_LIST)
    fun getFactionList(): Observable<HttpRequestResultDataList<FactionItem?>?>?

    @GET(UrlConfig.Community.URL_FACTION_DETAIL)
    fun getFactionDetail(@Query("leagueId") leagueId: String?): Observable<HttpRequestResultData<FactionItem?>?>?

    @GET(UrlConfig.Community.URL_FACTION_NOTICE)
    fun getFactionNotice(@Query("language") language: Int, @Query("type") type: Int): Observable<HttpRequestResultData<FactionNotice?>?>?

    @GET(UrlConfig.Community.URL_FACTION_MEMBER_LIST)
    fun getFactionMemberList(@Query("leagueId") leagueId: String?): Observable<HttpRequestResultDataList<FactionMember?>?>?

    @GET(UrlConfig.Community.URL_FACTION_USER_INFO)
    fun getFactionUserInfo(@Query("leagueId") leagueId: String?): Observable<HttpRequestResultData<FactionUserInfo?>?>?

    @GET(UrlConfig.Community.URL_FACTION_CONFIG)
    fun getFactionConfig(): Observable<HttpRequestResultData<FactionConfig?>?>?

    @FormUrlEncoded
    @POST(UrlConfig.Community.URL_FACTION_BECOME)
    fun postFactionBecome(@Field("leagueId") leagueId: String?, @Field("amount") amount: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Community.URL_FACTION_KEEP)
    fun postFactionKeep(@Field("leagueId") leagueId: String?, @Field("amount") amount: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Community.URL_FACTION_LOCK)
    fun postFactionLock(@Field("leagueId") leagueId: String?, @Field("amount") amount: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Community.URL_FACTION_UNLOCK)
    fun postFactionUnLock(@Field("leagueId") leagueId: String?): Observable<HttpRequestResultString?>?

    @FormUrlEncoded
    @POST(UrlConfig.Community.URL_RED_PACKET_SEND)
    fun sendRedPacket(@Field("type") leagueId: String?, @Field("coinType") coinType: String?, @Field("amount") amount: String?, @Field("quantity") quantity: Int, @Field("title") title: String?): Observable<HttpRequestResultString?>?

    @GET(UrlConfig.Community.URL_RED_PACKET_DETAIL)
    fun getRedPacketDetail(@Path("id") redPacketId: String?): Observable<HttpRequestResultData<RedPacketDetail?>?>?

    @GET(UrlConfig.Community.URL_RED_PACKET_SUMMARY)
    fun getRedPacketSummary(@Path("id") redPacketId: String?): Observable<HttpRequestResultData<RedPacketPub?>?>?

    @POST(UrlConfig.Community.URL_RED_PACKET_OPEN)
    fun openRedPacket(@Path("id") redPacketId: String?): Observable<HttpRequestResultString?>?
}