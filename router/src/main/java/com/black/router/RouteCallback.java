package com.black.router;

public interface RouteCallback {
    void onResult(boolean routeResult, Throwable error);
}
