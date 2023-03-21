package com.black.net;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManagerImpl {
    private static final String TAG = "ApiManagerImpl";
    public static final int DEFAULT_TIME_OUT = 30;//超时时间5s
    public static final int DEFAULT_READ_TIME_OUT = 30;//读取时间
    public static final int DEFAULT_WRITE_TIME_OUT = 30;//读取时间
    private static final Map<String, SoftReference<ApiManagerImpl>> managerCache = Collections.synchronizedMap(new HashMap<String, SoftReference<ApiManagerImpl>>());// 软引用
    private static String globalCookie;
    public String JSESSIONIDCookie = null;

    public OkHttpClient.Builder builder;
    private Retrofit mRetrofit;
    private ApiCookieHelper apiCookieHelper;
    private HttpInterceptHelper interceptHelper;

    public static void clearCache() {
        synchronized (managerCache) {
            managerCache.clear();
        }
    }

    public synchronized static ApiManagerImpl getInstance(Context context, String cachePath, String url, String deviceId, String lang, String ucToken, ApiCookieHelper apiCookieHelper, HttpInterceptHelper interceptHelper) throws Throwable {
        String key = getKey(url, deviceId, lang, ucToken);
        SoftReference<ApiManagerImpl> apiManagerRef = managerCache.get(key);
        ApiManagerImpl apiManager = apiManagerRef == null ? null : apiManagerRef.get();
//        Log.d(TAG, "url = " + url);
        if (apiManager == null) {
            apiManager = new ApiManagerImpl(context, cachePath, url, deviceId, lang, ucToken, apiCookieHelper, interceptHelper);
            managerCache.put(key, new SoftReference<>(apiManager));
        }
//        Log.d(TAG, "apiManager = " + apiManager);
        return apiManager;
    }

    private ApiManagerImpl(Context context, String cachePath, String url, String deviceId, String lang, String ucToken, ApiCookieHelper apiCookieHelper, HttpInterceptHelper interceptHelper) throws Throwable {
        this.apiCookieHelper = apiCookieHelper;
        this.interceptHelper = interceptHelper;
        //OkHttpClient配置
//        builder = new OkHttpClient.Builder();
        builder = getOkHttpBuilder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS);
//        Cache cache = new Cache(new File(Environment.getExternalStorageDirectory() + "/fbsex/cache"), 1024 * 1024 * 10);
        Cache cache = new Cache(new File(cachePath), 1024 * 1024 * 10);
        builder.cache(cache);
        addInterceptor(context, builder, deviceId, lang, ucToken);
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private OkHttpClient.Builder getOkHttpBuilder() throws Throwable {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            return new OkHttpClient.Builder();
//        }else{
//            return getUnsafeOkHttpClient();
//        }
        return getUnsafeOkHttpClient();
    }

    private final OkHttpClient.Builder getUnsafeOkHttpClient() throws Throwable {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{(TrustManager)(new X509TrustManager() {
                public void checkClientTrusted(@Nullable X509Certificate[] chain, @Nullable String authType) throws CertificateException {
                }

                public void checkServerTrusted(@Nullable X509Certificate[] chain, @Nullable String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            })};
            SSLContext var10000 = SSLContext.getInstance("SSL");
            SSLContext sslContext = var10000;
            sslContext.init((KeyManager[])null, trustAllCerts, new SecureRandom());
            SSLSocketFactory var6 = sslContext.getSocketFactory();
            SSLSocketFactory sslSocketFactory = var6;
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            TrustManager var10002 = trustAllCerts[0];
            if (trustAllCerts[0] == null) {
                throw new NullPointerException("null cannot be cast to non-null type javax.net.ssl.X509TrustManager");
            } else {
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)var10002);
                builder.hostnameVerifier((hostname, session) -> true);
                return builder;
            }
        } catch (Exception var5) {
            throw (Throwable)(new RuntimeException((Throwable)var5));
        }
    }

    private static String getKey(String url, String deviceId, String lang, String ucToken) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("url:").append(url).append("deviceId:").append(deviceId).append("lang:").append(lang).append("token").append(ucToken);
//        stringBuilder.append("deviceId:").append(deviceId).append("lang:").append(lang);
        return stringBuilder.toString();
    }

    public static String getVersionName(Context context, String defaultValue) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return defaultValue;
        }
    }

    private static String getUserAgent(Context context) {
        /*
         *"ETicket/4.0.3 (Simulator; iOS 12.2; Scale/3.00;fbsexApp)
         *ETicket 工程名称
         *4.0.3版本号
         *Simulater 设备型号
         *iOS 12.1 系统版本号
         *fbsexApp固定值
         */
        StringBuilder sb = new StringBuilder();
        sb.append("FryingNew/").append(getVersionName(context, "1.0.0")).append(" ");
        sb.append("(");
        sb.append(android.os.Build.MODEL).append(";");
        sb.append(android.os.Build.VERSION.RELEASE).append(";");
        sb.append("fbsexApp");
        sb.append(")");
        return sb.toString();
    }

    /**
     * 添加各种拦截器
     */
    private void addInterceptor(Context context, OkHttpClient.Builder builder, String deviceId, String lang, final String ucToken) {
        // 添加日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (Util.isApkInDebug(context)) {
                    Log.e("RetrofitLog", "" + message);
                }
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor headerInterceptor = chain -> {

            String ucToken1 = HttpCookieUtil.getUcToken(context);
            String ticket = HttpCookieUtil.getTicket(context);
            String trade_token = HttpCookieUtil.getTradeToken(context);
            String pro_token = HttpCookieUtil.getProToken(context);
            String otc_token = HttpCookieUtil.getApiToken(context);
            String fic_token = HttpCookieUtil.getFicToken(context);
            String future_token = HttpCookieUtil.geFutureToken(context);
            String ws_token = HttpCookieUtil.getWsToken(context);

            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();
            requestBuilder
                    .header("charset", "UTF-8")
                    //.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                    //.header("Accept", "application/json")
                    .header("User-Agent", getUserAgent(context))
                    .header("ASI-UUID", deviceId)
                    .header("platform", "Android")
                    .header("lang", lang)
                    .header("Client-Type", "Android")
//                        .header("Accept-Language", "Android")
//                        .header("platform", "Android")
//                        .addHeader("cookie", "lang=" + lang)
                    .header("Authorization", ucToken1 == null ? "" : ucToken1)
//                        .header("Authorization-uc", ucToken == null ? "" : ucToken)
//                        .header("Authorization-pro", pro_token == null ? "" : pro_token)
                          .header("Authorization-otc", otc_token == null ? "" : otc_token)
                    .header("Authorization-financial", fic_token == null ? "" : fic_token)
//                        .header("Authorization-ft", "")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Cache-Control", "no-cache");

            JSESSIONIDCookie = HttpCookieUtil.getJsessionId(context);
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(ucToken1)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";uc-token=" + ucToken1 + ";";
                } else {
                    JSESSIONIDCookie += "uc-token=" + ucToken1 + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(ticket)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";ticket=" + ticket + ";";
                } else {
                    JSESSIONIDCookie += "ticket=" + ticket + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(trade_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";trade-token=" + trade_token + ";";
                } else {
                    JSESSIONIDCookie += "trade-token=" + trade_token + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(pro_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";pro-token=" + pro_token + ";";
                } else {
                    JSESSIONIDCookie += "pro-token=" + pro_token + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(otc_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";otc-token=" + otc_token + ";";
                } else {
                    JSESSIONIDCookie += "otc-token=" + otc_token + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(fic_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";fic-token=" + fic_token + ";";
                } else {
                    JSESSIONIDCookie += "fic-token=" + fic_token + ";";
                }
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(future_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";token=" + future_token + ";";
                } else {
                    JSESSIONIDCookie += "token=" + future_token + ";";
                }
                requestBuilder.addHeader("Authorization-ft", future_token);
            }
            if (JSESSIONIDCookie != null && !TextUtils.isEmpty(ws_token)) {
                if (JSESSIONIDCookie.lastIndexOf(";") == -1) {
                    JSESSIONIDCookie += ";ws-token=" + ws_token + ";";
                } else {
                    JSESSIONIDCookie += "ws-token=" + ws_token + ";";
                }
            }
            if (JSESSIONIDCookie != null) {
                requestBuilder.addHeader("Cookie", JSESSIONIDCookie);
//                requestBuilder.addHeader("cookie", JSESSIONIDCookie);
            }
            if (apiCookieHelper != null && apiCookieHelper.useGlobalCookie(original)) {
                if (globalCookie != null) {
                    requestBuilder.addHeader("Cookie", globalCookie);
                    requestBuilder.addHeader("cookie", globalCookie);
                }
            }

            Request request = requestBuilder.method(original.method(), original.body()).build();
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = Charset.forName("UTF-8");
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                }
            }
            Response response = chain.proceed(request);
            List<String> cookies = response.headers("Set-Cookie");
            if (JSESSIONIDCookie == null && !cookies.isEmpty()) {
                Map<String, String> allParam = new HashMap<>();
                for (String key : cookies) {
                    //只取我们需要的那一段cookie
//                        String compareKey = key == null ? null : key.toUpperCase();
//                        if (compareKey != null && compareKey.contains("HTTPONLY") && compareKey.contains("JSESSIONID")) {
//                            JSESSIONIDCookie = key;
//                            break;
//                        }
                    Map<String, String> param = getParam(key);
//                        Log.e("interceptor", "set key：" + key);
//                        Log.e("interceptor", "set param：" + param);
                    if (param != null) {
                        allParam.putAll(param);
                    }
                }
                // Log.e("interceptor", "set allParam：" + allParam);
                String __cdnuid = allParam.get("__cdnuid");
                String AWSALB = allParam.get("AWSALB");
                String __cdnuid_h = allParam.get("__cdnuid_h");
                String __cdnuid_s = allParam.get("__cdnuid_s");
                String JSESSIONID = allParam.get("JSESSIONID");
                String SERVERID = allParam.get("SERVERID");
                if (JSESSIONID != null && JSESSIONID.trim().length() > 0) {
                    String cookie = "";
                    if (AWSALB != null && AWSALB.trim().length() > 0) {
                        cookie += "AWSALB=" + AWSALB + ";";
                    }
                    if (__cdnuid_h != null && __cdnuid_h.trim().length() > 0) {
                        cookie += "__cdnuid_h=" + __cdnuid_h + ";";
                    }
                    if (__cdnuid_s != null && __cdnuid_s.trim().length() > 0) {
                        cookie += "__cdnuid_s=" + __cdnuid_s + ";";
                    }
                    if (__cdnuid != null && __cdnuid.trim().length() > 0) {
                        cookie += "__cdnuid=" + __cdnuid + ";";
                    }
                    if (JSESSIONID != null && JSESSIONID.trim().length() > 0) {
                        cookie += "JSESSIONID=" + JSESSIONID + ";";
                    }
                    if (SERVERID != null && SERVERID.trim().length() > 0) {
                        cookie += "SERVERID=" + SERVERID + ";";
                    }
                    if (cookie.length() > 0 && cookie.charAt(cookie.length() - 1) == ';') {
                        cookie = cookie.substring(0, cookie.length() - 1);
                    }
                    JSESSIONIDCookie = cookie;
                    HttpCookieUtil.saveJsessionId(context, JSESSIONIDCookie);
                }
            }
            // Log.e("interceptor", "JSESSIONIDCookie：" + JSESSIONIDCookie);
            if (apiCookieHelper != null && apiCookieHelper.canSaveGlobalCookie(original)) {
                globalCookie = JSESSIONIDCookie;
            }
//                if (fullUrl.endsWith("gt/init")) {
//                    globalCookie = JSESSIONIDCookie;
//                }
//                response = chain.proceed(request);
            return response;
        };
        //头部参数拦截
        builder.addNetworkInterceptor(headerInterceptor);
        //日志拦截
//        if (Util.isApkInDebug(context)) {
        builder.addInterceptor(loggingInterceptor);
//        }
        builder.addInterceptor(new SelfInterceptor(builder, interceptHelper));

//        builder.addInterceptor(new CookieReadInterceptor(context));
//        builder.addInterceptor(new CookieSaveInterceptor(context));
//        //缓存拦截
//        builder.addInterceptor(new HttpCacheInterceptor());
//        //请求参数拦截
//        builder.addInterceptor(new CommonParamsInterceptor());
    }

    private static Map<String, String> getParam(String paramString) {
        Map<String, String> param = new HashMap<>();
        if (paramString != null) {
            String[] paramArr = paramString.split(";");
            for (String itemString : paramArr) {
                if (itemString != null) {
                    String[] itemArr = itemString.split("=");
                    if (itemArr.length == 2) {
                        String key = itemArr[0];
                        String value = itemArr[1];
                        if (key != null && key.trim().length() > 0 && value != null && value.trim().length() > 0) {
                            param.put(key.trim(), value.trim());
                        }
                    }
                }
            }
        }
        return param;
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    //获取Service实例
    public <T> T create(Class<T> tClass) {
        return mRetrofit.create(tClass);
    }
}
