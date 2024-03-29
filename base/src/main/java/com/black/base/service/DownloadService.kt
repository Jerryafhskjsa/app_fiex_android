package com.black.base.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {
    @Streaming
    @GET
    fun downLoad(@Url url: String?): Observable<ResponseBody?>?
}
