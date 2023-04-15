package com.black.base.api

import android.content.Context
import android.text.TextUtils
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.base.net.HttpCallbackSimple
import com.black.base.net.NetObserver
import com.black.base.util.ConstData
import com.black.base.util.RxJavaHelper
import com.black.net.ApiManagerImpl
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.*
import okhttp3.internal.http.HttpMethod
import okio.Buffer
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit

object RetryRequestHelper {
    fun testRequestBody(context: Context?) {
        var requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "moneyPassword=" + 1231456)
        requestBody = FormBody.Builder()
                .add("moneyPassword", "1231456")
                .add("bbb", "bbb")
                .build()
        val file = File(CommonUtil.getCatchFilePath(context) + File.separator + ConstData.TEMP_IMG_NAME_01)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        requestBody = MultipartBody.Builder()
                .addFormDataPart("moneyPassword", "1231456")
                .addFormDataPart("user", "aaa")
                .addPart(body)
                .build()
    }

    fun test(context: Context?) {
        val url = "https://fad34sd32g541.fbsex.co/api/countryCodeList"
        val request = Request.Builder().method("GET", null).url(url).build()
        requestQueryAddMoneyPassword(null, request, "123412551", false,
                HttpCallbackSimple(context, true,
                        object : Callback<HttpRequestResultDataList<CountryCode?>?>() {
                            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {}
                            override fun error(type: Int, error: Any) {}
                        }))
    }

    fun getRequestBodyParam(requestBody: RequestBody?): String {
        if (requestBody == null) {
            return ""
        }
        val buffer = Buffer()
        try {
            requestBody.writeTo(buffer)
        } catch (e: IOException) {
        }
        var charset = Charset.forName("UTF-8")
        val contentType = requestBody.contentType()
        if (contentType != null) {
            charset = contentType.charset(Charset.forName("UTF-8"))
        }
        return buffer.readString(charset)
    }

    fun <T> requestQueryAddMoneyPassword(builder: OkHttpClient.Builder?, request: Request, moneyPassword: String?, timeMill: Boolean?, netObserver: NetObserver<T>) { //拿到所有Query的Key
        val newParams: MutableMap<String, String> = HashMap()
        if (moneyPassword != null) {
            newParams["moneyPassword"] = moneyPassword
        }
        if (timeMill != null) {
            newParams["timeMill"] = timeMill.toString()
        }
        val newRequest = newRequestAddParams(request, newParams)
        requestQuery(builder, newRequest, netObserver)
    }

    fun newRequestAddParams(request: Request, newParams: Map<String, String>?): Request {
        if (newParams == null || newParams.isEmpty()) {
            return request
        }
        val newRequest: Request
        if (HttpMethod.requiresRequestBody(request.method())) { //添加body
            var requestBody = request.body()
            requestBody = if (requestBody is FormBody) {
                val formBody = requestBody
                val builder = FormBody.Builder()
                val paramSize = formBody.size()
                for (i in 0 until paramSize) {
                    builder.add(formBody.name(i), formBody.value(i))
                }
                for (entry in newParams.entries) {
                    if (entry != null) {
                        val key = entry.key
                        val value = entry.value
                        if (key != null && value != null) {
                            builder.add(key, value)
                        }
                    }
                }
                builder.build()
            } else if (requestBody is MultipartBody) {
                val multipartBody = requestBody
                val builder = MultipartBody.Builder(multipartBody.boundary())
                        .setType(multipartBody.type())
                val parts = multipartBody.parts()
                if (parts != null && !parts.isEmpty()) {
                    for (part in parts) {
                        builder.addPart(part)
                    }
                }
                for (entry in newParams.entries) {
                    if (entry != null) {
                        val key = entry.key
                        val value = entry.value
                        if (key != null && value != null) {
                            builder.addFormDataPart(key, value)
                        }
                    }
                }
                builder.build()
            } else {
                val sb = StringBuilder()
                for (entry in newParams.entries) {
                    if (entry != null) {
                        val key = entry.key
                        val value = entry.value
                        if (key != null && value != null) {
                            if (sb.length == 0) {
                                sb.append("&")
                            }
                            sb.append(key).append("=").append(value)
                        }
                    }
                }
                if (requestBody != null) {
                    val requestBodyStirng = getRequestBodyParam(requestBody)
                    if (TextUtils.isEmpty(requestBodyStirng)) {
                        RequestBody.create(requestBody.contentType(), sb.toString())
                    } else {
                        RequestBody.create(requestBody.contentType(), "$requestBodyStirng&$sb")
                    }
                } else {
                    RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), sb.toString())
                }
            }
            newRequest = request.newBuilder()
                    .method(request.method(), requestBody)
                    .build()
        } else {
            val builder = request.url().newBuilder()
            for (entry in newParams.entries) {
                if (entry != null) {
                    val key = entry.key
                    val value = entry.value
                    if (key != null && value != null) {
                        builder.addQueryParameter(key, value)
                    }
                }
            }
            newRequest = request
                    .newBuilder()
                    .url(builder.build())
                    .build()
        }
        return newRequest
    }

    fun <T> requestQuery(builder: OkHttpClient.Builder?, request: Request, netObserver: NetObserver<T>) {
        createResendObservable(builder, request, netObserver)
                .compose(RxJavaHelper.observeOnMainThread())
                .subscribe(netObserver)
    }

    private fun <T> createResendObservable(builder: OkHttpClient.Builder?, request: Request, netObserver: NetObserver<T>): Observable<T> {
        var builder = builder
        if (builder == null) {
            builder = OkHttpClient.Builder()
            builder.connectTimeout(ApiManagerImpl.DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
            builder.readTimeout(ApiManagerImpl.DEFAULT_READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
            builder.writeTimeout(ApiManagerImpl.DEFAULT_WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
            builder.cache(Cache(File(ConstData.CACHE_PATH), 1024 * 1024 * 10))
        }
        val call = builder.build().newCall(request)
        return CallObservable(call)
                .flatMap { response ->
                    val type = netObserver.genericType
                    val gson = Gson()
                    val adapter = gson.getAdapter(TypeToken.get(type)) as TypeAdapter<T>
                    val value = response.body()
                    val jsonReader = gson.newJsonReader(response.body()?.charStream())
                    val data: T? = value.use { _ ->
                        adapter.read(jsonReader)
                    }
                    Observable.just(data)
                }
    }

    internal class CallObservable(private val originalCall: Call) : Observable<Response?>() {
        override fun subscribeActual(observer: Observer<in Response?>) {
            // Since Call is a one-shot type, clone it for each new observer.
            val call = originalCall.clone()
            observer.onSubscribe(CallDisposable(call))
            var terminated = false
            try {
                val response = call.execute()
                if (!call.isCanceled) {
                    observer.onNext(response)
                }
                if (!call.isCanceled) {
                    terminated = true
                    observer.onComplete()
                }
            } catch (t: Throwable) {
                Exceptions.throwIfFatal(t)
                if (terminated) {
                    RxJavaPlugins.onError(t)
                } else if (!call.isCanceled) {
                    try {
                        observer.onError(t)
                    } catch (inner: Throwable) {
                        Exceptions.throwIfFatal(inner)
                        RxJavaPlugins.onError(CompositeException(t, inner))
                    }
                }
            }
        }

        private class CallDisposable internal constructor(private val call: Call) : Disposable {
            override fun dispose() {
                call.cancel()
            }

            override fun isDisposed(): Boolean {
                return call.isCanceled
            }

        }

    }
}