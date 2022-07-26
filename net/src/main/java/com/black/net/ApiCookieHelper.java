package com.black.net;

import okhttp3.Request;

public interface ApiCookieHelper {
    public boolean canSaveGlobalCookie(Request request);

    public boolean useGlobalCookie(Request request);
}
