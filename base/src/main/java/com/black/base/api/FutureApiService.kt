package com.black.base.api


import com.black.base.model.HttpRequestResultData

import com.black.base.model.future.DepthBean
import com.black.base.util.UrlConfig
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface FutureApiService {

    /**
     * 获取深度
     */
    @GET(UrlConfig.Future.URL_DEPTH)
    fun getDepth(@Query("symbol") symbol: String?,
                 @Query("level") level: Int?): Observable<HttpRequestResultData<DepthBean>?>?

}