package com.black.net;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonParseException;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.HttpException;

import static com.black.net.HttpRequestResult.ERROR_TOKEN_INVALID_CODE;

public abstract class RxObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {
        beforeRequest();
    }

    @Override
    public void onNext(T value) {
        try {
            afterRequest();
        } catch (Throwable ignored) {
        }
        try {
            onReturnResult(value);
        } catch (Throwable e) {
            onError(e);
        }
    }

    @Override
    final public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof com.jakewharton.retrofit2.adapter.rxjava2.HttpException) {
            com.jakewharton.retrofit2.adapter.rxjava2.HttpException httpException = (com.jakewharton.retrofit2.adapter.rxjava2.HttpException) e;
            int code = httpException.code();
            HttpUrl url = httpException.response().raw().request().url();
            String urlStr = url.url().toString();
            if (code == ERROR_TOKEN_INVALID_CODE) {
                error(httpException.response().raw().request(), HttpRequestResult.ERROR_TOKEN_INVALID);
            } else if (code == HttpRequestResult.ERROR_MISS_MONEY_PASSWORD_CODE) {
                //资金密码错误
                Request request = httpException.response().raw().request();
                error(request, HttpRequestResult.ERROR_MISS_MONEY_PASSWORD);
            } else {
                error(httpException.response().message(), HttpRequestResult.NOTWORK_ERROR);
            }
        } else if (e instanceof HttpException) {
            /** 网络异常，http 请求失败，即 http 状态码不在 [200, 300) 之间, such as: "server internal error". */
            HttpException httpException = (HttpException) e;
            int code = httpException.code();
            if (code == ERROR_TOKEN_INVALID_CODE) {
                //token失效
                error(httpException.response().raw().request(), HttpRequestResult.ERROR_TOKEN_INVALID);
            } else if (code == HttpRequestResult.ERROR_MISS_MONEY_PASSWORD_CODE) {
                //资金密码错误
                Request request = httpException.response().raw().request();
                RequestBody requestBody = request.body();
                error(request, HttpRequestResult.ERROR_MISS_MONEY_PASSWORD);
            } else {
                error(httpException.response().message(), HttpRequestResult.NOTWORK_ERROR);
            }
        } else if (e instanceof BlackHttpException) {
            BlackHttpException interceptException = (BlackHttpException) e;
            HttpUrl url = interceptException.response().request().url();
            String urlStr = url.url().toString();
            int code = interceptException.code();
            if (code == ERROR_TOKEN_INVALID_CODE) {
                //token失效
                error(interceptException.response().request().url(), HttpRequestResult.ERROR_TOKEN_INVALID);
            } else if (code == HttpRequestResult.ERROR_MISS_MONEY_PASSWORD_CODE) {
                //资金密码错误
                error(interceptException, HttpRequestResult.ERROR_MISS_MONEY_PASSWORD);
            } else {
                error(interceptException.response().message(), HttpRequestResult.NOTWORK_ERROR);
            }
        } else if (e instanceof IOException) {
            /** 没有网络 */
            error(null, HttpRequestResult.NOTWORK_ERROR);
        } else if (e instanceof JsonParseException) {
            /** 网络正常，http 请求成功，服务器返回逻辑错误 */
            error(null, HttpRequestResult.JSON_ERROR);
        } else {
            /** 其他未知错误 */
            error(!TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "unknown error", HttpRequestResult.OTHER_ERROR);
        }
        try {
            afterRequest();
        } catch (Throwable err) {
        }
    }

    @Override
    public void onComplete() {
        try {
            afterRequest();
        } catch (Throwable err) {
        }
    }

    public abstract void beforeRequest();

    public abstract void afterRequest();

    public abstract void error(Object returnString, int errorType);

    public abstract void onReturnResult(T result);
}
