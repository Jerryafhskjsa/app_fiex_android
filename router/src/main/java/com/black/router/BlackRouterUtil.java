package com.black.router;

import android.util.Log;

public class BlackRouterUtil {
    public static String getUrlNoParams(String url) {
        if (url != null) {
            try {
                int index = url.indexOf("?");
                if (index == -1) {
                    return url;
                } else {
                    return url.substring(0, index);
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }
}
