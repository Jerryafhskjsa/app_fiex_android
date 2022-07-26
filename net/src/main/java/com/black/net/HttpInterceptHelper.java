package com.black.net;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public interface HttpInterceptHelper {
    void intercept(OkHttpClient.Builder builder, Response response) throws IOException;
}
