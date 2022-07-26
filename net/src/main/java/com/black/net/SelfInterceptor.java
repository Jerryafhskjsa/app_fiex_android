package com.black.net;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelfInterceptor implements Interceptor {
    private OkHttpClient.Builder builder;
    private HttpInterceptHelper interceptHelper;

    public SelfInterceptor(OkHttpClient.Builder builder, HttpInterceptHelper interceptHelper) {
        this.builder = builder;
        this.interceptHelper = interceptHelper;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            throw e;
        }
        if (interceptHelper != null) {
            interceptHelper.intercept(builder, response);
        }
        return response;
    }
}
