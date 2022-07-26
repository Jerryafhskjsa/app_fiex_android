package com.black.router;

import android.content.Context;

import androidx.fragment.app.Fragment;

//路由辅助工具
public interface BlackRouterHelper {
    BlackRouterHelper bindRouteDestination(RouteDestination routeDestination);

    void goContext(Context context);

    void goFragment(Fragment fragment);
}
