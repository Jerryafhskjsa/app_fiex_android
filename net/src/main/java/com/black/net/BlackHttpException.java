package com.black.net;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class BlackHttpException extends IOException {
    private final int code;
    private OkHttpClient.Builder builder;
    private final Response response;

    public BlackHttpException(int code, OkHttpClient.Builder builder, Response response) {
        this.code = code;
        this.builder = builder;
        this.response = response;
    }

    public int code() {
        return code;
    }

    public Response response() {
        return response;
    }

    public OkHttpClient.Builder builder() {
        return builder;
    }
}
