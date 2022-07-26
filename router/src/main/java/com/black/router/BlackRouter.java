package com.black.router;

import android.content.Context;
import android.content.pm.PackageManager;

import com.black.router.annotation.Config;
import com.black.router.annotation.RouteConfigHelper;
import com.black.util.CommonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlackRouter {
    private final static String TAG = "BlackRouter";
    protected static String webViewPath = null;
    public final static Map<String, Config> configMap = new HashMap<>();
    private static BlackRouter router = null;
    // 所有 moudle
    private static List<String> mAllModuleClassName;

    static {
//        initConfig();
    }

    private BlackRouter() {
    }

    public void init(Context context) {
        // 获取 com.drotuer.assist 包名下的所有类名信息
        try {
            mAllModuleClassName = ClassUtils.getFileNameByPackageName(context, "com.black.router.generated");
        } catch (PackageManager.NameNotFoundException e) {
            CommonUtil.printError(context, e);
        } catch (IOException e) {
            CommonUtil.printError(context, e);
        }

        for (String className : mAllModuleClassName) {
            initConfig(className);
        }
    }

    private void initConfig(String className) {
        try {
            Class<?> generatedClass = Class.forName(className);
            Object object = generatedClass.newInstance();
            if (object instanceof RouteConfigHelper) {
                ((RouteConfigHelper) object).initRouteConfig(configMap);
            }
        } catch (Exception e) {
        }
    }

    public void setWebViewPath(String webViewPath) {
        BlackRouter.webViewPath = webViewPath;
    }

    public static BlackRouter getInstance() {
        if (router == null) {
            router = new BlackRouter();
        }
        return router;
    }

    protected static Config getRouteClass(String uri) {
        String urlNoParams = BlackRouterUtil.getUrlNoParams(uri);
        Config clz = configMap.get(urlNoParams);
        if (clz == null) {
//            initConfig();
        }
        return clz;
    }

    public BlackRouterImpl build(String uri) {
        BlackRouterImpl blackRouter = new BlackRouterImpl();
        RouteDestination destination = null;
        destination = new RouteDestination(uri, getRouteClass(uri));
        blackRouter.destination = destination;
        return blackRouter;
    }

    public BlackRouterImpl build(RouteDestination destination) {
        BlackRouterImpl blackRouter = new BlackRouterImpl();
        blackRouter.destination = destination;
        return blackRouter;
    }
}
