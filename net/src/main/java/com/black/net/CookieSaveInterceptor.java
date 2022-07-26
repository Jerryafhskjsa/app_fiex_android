package com.black.net;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CookieSaveInterceptor implements Interceptor {
    private Context context;
    public static List<String> cookieSet = new ArrayList<>();
    public static String JSESSIONIDCookie = null;

    public CookieSaveInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        //响应请求中获取Set-Cookie ，相关list
        List<String> cookies = response.headers("Set-Cookie");
        ResponseBody responseBody = response.body();
//        String json = responseBody.string();//取出响应请求结果后需要重新组装成ResponseBody，返回给拦截器
        MediaType contentType = responseBody.contentType();//数据类型，不重要
        //判读服务器返回请求是否成功，cookies是否有效
        if (cookieSet.isEmpty()) {
            if (!cookies.isEmpty()) {
                for (String key : cookies) {
                    //只取我们需要的那一段cookie
                    if (key.contains("HttpOnly") && key.contains("JSESSIONID")) {
                        cookieSet.add(key);
                    }
                }
            }
        }
        if (JSESSIONIDCookie == null && !cookies.isEmpty()) {
            for (String key : cookies) {
                //只取我们需要的那一段cookie
                if (key.contains("HttpOnly") && key.contains("JSESSIONID")) {
                    JSESSIONIDCookie = key;
                    break;
                }
            }
        }
//        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
//            HashSet<String> cookies = new HashSet<>();
//
//            for (String header : originalResponse.headers("Set-Cookie")) {
//                cookies.add(header);
//            }
//            HttpCookieUtil.saveHttpCookie(context, cookies);
////            SpUtil.putStringSet(MyApplication.getInstance(), Constants.LOGIN_COOKIE, cookies);
//        }
//        ResponseBody body = ResponseBody.create(contentType, json);
//        return response.newBuilder().body(body).build();
        return response;
    }

    /**
     * 判断请求回来的json ，是否是请求成功的，请求成功返回true ，请求失败返回false
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static boolean doHandle(String json) {
        JSONObject jsonO = null;
        try {
            jsonO = new JSONObject(json);
        } catch (JSONException ignored) {
        }
        String rsp = null;
        if (jsonO != null) {
            rsp = jsonO.optString("rsp");
        }
        if ("succ".equals(rsp)) {
            return true;
        }
        return false;
    }
}
