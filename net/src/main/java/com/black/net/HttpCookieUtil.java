package com.black.net;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class HttpCookieUtil {
    public final static String HTTP_COOKIE = "http_cookie";
    public final static String JSESSION_ID = "jsession_id";
    public final static String UC_TOKEN = "uc_token";
    public final static String TICKET = "ticket";
    public final static String TRADE_TOKEN = "trade_token";
    public final static String PRO_TOKEN = "pro_token";

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

    public static void saveUcToken(Context context, String jsessionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(UC_TOKEN, jsessionId);
        editor.commit();
    }
    public static void saveTicket(Context context, String jsessionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TICKET, jsessionId);
        editor.commit();
    }
    public static void saveTradeToken(Context context, String jsessionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(TRADE_TOKEN, jsessionId);
        editor.commit();
    }
    public static void saveProToken(Context context, String jsessionId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PRO_TOKEN, jsessionId);
        editor.commit();
    }

    public static String getJsessionId(Context context) {
        return getSharedPreferences(context).getString(JSESSION_ID, null);
    }

    public static String getUcToken(Context context) {
        return getSharedPreferences(context).getString(UC_TOKEN, null);
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

    public static void deleteUcToken(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(UC_TOKEN).apply();
    }

    public static void deleteTicket(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(TICKET).apply();
    }

    public static void deleteTradeToken(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(TRADE_TOKEN).apply();
    }

    public static void deleteProToken(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(PRO_TOKEN).apply();
    }

    public static void deleteSessionId(Context context){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(JSESSION_ID).apply();
    }

    public static void deleteCookies(Context context){
        deleteUcToken(context);
        deleteTicket(context);
        deleteTradeToken(context);
        deleteProToken(context);
        deleteSessionId(context);
    }


}
