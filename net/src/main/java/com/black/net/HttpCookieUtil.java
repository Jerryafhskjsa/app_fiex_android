package com.black.net;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class HttpCookieUtil {
    public final static String HTTP_COOKIE = "http_cookie";
    public final static String JSESSION_ID = "jsession_id";
    public final static String UC_TOKEN = "uc_token";
    public final static String API_TOKEN = "api_token";
    public final static String TICKET = "ticket";
    public final static String TRADE_TOKEN = "trade_token";
    public final static String PRO_TOKEN = "pro_token";
    public final static String FUTURE_TOKEN = "token";
    public final static String LISTEN_KEY = "listen_key";
    public final static String PRO_TOKEN_EXPIRED_TIME = "pro_token_expired_time";
    public final static String API_TOKEN_EXPIRED_TIME = "api_token_expired_time";
    public final static String WS_TOKEN = "ws_token";

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

    public static void saveJsessionId(Context context, String jsessionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(JSESSION_ID, jsessionId);
        editor.commit();
    }

    public static void saveUcToken(Context context, String ucToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(UC_TOKEN, ucToken);
        editor.commit();
    }

    public static void saveApiToken(Context context, String apiToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(API_TOKEN, apiToken);
        editor.commit();
    }

    public static void saveTicket(Context context, String ticket) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TICKET, ticket);
        editor.commit();
    }

    public static void saveTradeToken(Context context, String tradeToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TRADE_TOKEN, tradeToken);
        editor.commit();
    }

    public static void saveProToken(Context context, String proToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PRO_TOKEN, proToken);
        editor.commit();
    }

    public static void saveFutureToken(Context context, String proToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(FUTURE_TOKEN, proToken);
        editor.commit();
    }

    public static void saveListenKey(Context context, String listenkey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LISTEN_KEY, listenkey);
        editor.commit();
    }

    public static void saveWsToken(Context context, String wsToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(WS_TOKEN, wsToken);
        editor.commit();
    }

    public static void saveTradeTokenExpiredTime(Context context, String proTokenExpiredTime) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PRO_TOKEN_EXPIRED_TIME, proTokenExpiredTime);
        editor.commit();
    }

    public static void saveProTokenExpiredTime(Context context, String proToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PRO_TOKEN_EXPIRED_TIME, proToken);
        editor.commit();
    }

    public static void saveApiTokenExpiredTime(Context context, String apiTokenExpiredTime) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(API_TOKEN_EXPIRED_TIME, apiTokenExpiredTime);
        editor.commit();
    }

    public static String getJsessionId(Context context) {
        return getSharedPreferences(context).getString(JSESSION_ID, null);
    }

    public static String getUcToken(Context context) {
        return getSharedPreferences(context).getString(UC_TOKEN, null);
    }

    public static String getApiToken(Context context) {
        return getSharedPreferences(context).getString(API_TOKEN, null);
    }

    public static String getListenKey(Context context) {
        return getSharedPreferences(context).getString(LISTEN_KEY, null);
    }

    public static String getTicket(Context context) {
        return getSharedPreferences(context).getString(TICKET, null);
    }

    public static String getTradeToken(Context context) {
        return getSharedPreferences(context).getString(TRADE_TOKEN, null);
    }

    public static String getProToken(Context context) {
        return getSharedPreferences(context).getString(PRO_TOKEN, null);
    }

    public static String geFutureToken(Context context) {
        return getSharedPreferences(context).getString(FUTURE_TOKEN, null);
    }

    public static String getWsToken(Context context) {
        return getSharedPreferences(context).getString(WS_TOKEN, null);
    }

    public static void deleteUcToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(UC_TOKEN).commit();
    }

    public static void deleteFutureToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(FUTURE_TOKEN).commit();
    }

    public static void deleteListenKey(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(LISTEN_KEY).commit();
    }

    public static void deleteTicket(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(TICKET).commit();
    }

    public static void deleteTradeToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(TRADE_TOKEN).commit();
    }

    public static void deleteWsToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(WS_TOKEN).commit();
    }

    public static void deleteProToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(PRO_TOKEN).commit();
    }

    public static void deleteApiToken(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(API_TOKEN).commit();
    }

    public static void deleteSessionId(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(JSESSION_ID).commit();
    }

    public static void deleteCookies(Context context) {
        deleteUcToken(context);
        deleteTicket(context);
        deleteTradeToken(context);
        deleteProToken(context);
        deleteApiToken(context);
        deleteWsToken(context);
        deleteSessionId(context);
        deleteFutureToken(context);
    }

}
