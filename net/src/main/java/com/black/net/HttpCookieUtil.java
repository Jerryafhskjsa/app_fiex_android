package com.black.net;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class HttpCookieUtil {
    public final static String HTTP_COOKIE = "http_cookie";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static void saveHttpCookie(Context context, Set<String> value) {
        SharedPreferences prefs = getSharedPreferences(context);
        prefs.edit().putStringSet(HTTP_COOKIE, value).commit();
    }

    public static Set<String> getHttpCookie(Context context, Set<String> defValues) {
        return getSharedPreferences(context).getStringSet(HTTP_COOKIE, defValues);
    }

}
