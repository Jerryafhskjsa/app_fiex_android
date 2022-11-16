package com.black.base.api

import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.clutter.Kline
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface FutureApiService {

    /**
     * 获取深度
     */
    @GET(UrlConfig.Future.URL_DEPTH)
    fun getDepth(@Query("symbol") symbol: String?, @Query("level") interval: Int?): Observable<HttpRequestResultDataList<Kline?>?>?

}