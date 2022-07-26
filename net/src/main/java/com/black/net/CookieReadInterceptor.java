package com.black.net;

import android.content.Context;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.http.Headers;

public class CookieReadInterceptor implements Interceptor {
    private SoftReference<Context> rxAppCompatActivity;
    private static Headers headers;
    private Map<String, List<String>> listMap;
    private Context context;

    public CookieReadInterceptor(Context context) {
        this.context = context;
        this.rxAppCompatActivity = new SoftReference(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (CookieSaveInterceptor.JSESSIONIDCookie != null) {
            builder.addHeader("Cookie", CookieSaveInterceptor.JSESSIONIDCookie);
            builder.addHeader("cookie", CookieSaveInterceptor.JSESSIONIDCookie);
        }
        return chain.proceed(builder.build());
//        List<String> cookieSet = CookieSaveInterceptor.cookieSet;
//
//        Request original = chain.request();
//        Request request = null;
//        Request.Builder builder = original.newBuilder();
//
//        //添加cookie，到请求头中
////        if (!cookieSet.isEmpty()) {
////            builder.addHeader("Cookie", cookieSet.get(0));
////        }
//        if (CookieSaveInterceptor.JSESSIONIDCookie != null) {
//            builder.addHeader("Cookie", CookieSaveInterceptor.JSESSIONIDCookie);
//        }
//        request = builder.build();
//
//
//        //调试用的应用请求，打印，可以输出请求头的参数
//        System.out.println("=====================================================");
//        long t1 = System.nanoTime();
//        String requestHeader = String.format(">>>>>Sending request %s on %s%n%s",
//                request.url(), chain.connection(), request.headers());
//
//        Response response = chain.proceed(request);
//        long t2 = System.nanoTime();
//        String responseHeader = String.format(">>>>>Received response for %s in %.1fms%n%s",
//                response.request().url(), (t2 - t1) / 1e6d, response.headers());
//        System.out.println("=====================================================");
//        System.out.println(responseHeader);
//
//        return response;
    }
}