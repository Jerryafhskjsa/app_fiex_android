package com.black.router;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.black.router.annotation.Config;

public class BlackRouterImpl {
    public final static String TAG = "BlackRouterImpl";
    RouteDestination destination;

    BlackRouterImpl() {

    }

    public BlackRouterImpl withSingle(BlackRouterSingle single) {
        destination.single = single;
        return this;
    }

    public BlackRouterImpl with(Bundle bundle) {
        destination.extras = bundle;
        return this;
    }

    public BlackRouterImpl withRequestCode(int requestCode) {
        destination.requestCode = requestCode;
        return this;
    }

    public BlackRouterImpl addFlags(int flags) {
        destination.flags = destination.flags | flags;
        return this;
    }

    public void goFinal(Context context) {
        goFinal(context, null);
    }

    public void goFinal(Context context, RouteCallback callback) {
        destination.isFinal = true;
        destination.beforePath = null;
        go(context, callback);
    }

    public void goFinal(androidx.fragment.app.Fragment fragment, RouteCallback callback) {
        destination.isFinal = true;
        destination.beforePath = null;
        go(fragment, callback);
    }

    public void go(Context context) {
        go(context, null);
    }

    public void go(Context context, RouteCallback callback) {
        boolean result = true;
        Throwable error = null;
        try {
            if (isBrowse() && destination.aClass == null) {
                destination.addUriParams();
                goBrowse(context);
            } else {
                checkDestination(destination);
                destination.addUriParams();
                if (BlackRouterHelper.class.isAssignableFrom(destination.aClass)) {
                    BlackRouterHelper blackRouterHelper = (BlackRouterHelper) destination.aClass.newInstance();
                    blackRouterHelper.bindRouteDestination(destination).goContext(context);
                } else {
                    goRoute(context);
                }
            }
        } catch (Throwable throwable) {
            result = false;
            error = throwable;
        } finally {
            if (callback != null) {
                callback.onResult(result, error);
            }
        }
    }

    public void checkDestination(RouteDestination destination) {
        if (destination == null || destination.aClass == null) {
            throw new RuntimeException("no router config");
        }
    }

    public boolean isBrowse() {
        return destination.uri != null && (destination.uri.toLowerCase().startsWith("http://") || destination.uri.toLowerCase().startsWith("https://"));
    }

    private void goBrowse(Context context) {
        if (TextUtils.isEmpty(BlackRouter.webViewPath)) {
            Uri uri = Uri.parse(destination.uri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(context.getPackageName());
            if (destination.extras != null) {
                intent.putExtras(destination.extras);
            }
            context.startActivity(intent);
        } else {
            Config config = BlackRouter.getRouteClass(BlackRouter.webViewPath);
            RouteDestination webViewDestination = new RouteDestination(BlackRouter.webViewPath, config);
            webViewDestination.extras = destination.extras;
            goRealRoute(context, webViewDestination);
        }
    }

    //目标页面是Activity
    private void goRouteActivity(Context context, RouteDestination destination) {
        if (context instanceof Activity) {
            //Activity 打开 Activity
            Intent intent = new Intent(context, destination.aClass);
            intent.setPackage(context.getPackageName());
            if (destination.extras != null) {
                intent.putExtras(destination.extras);
            }
            intent.addFlags(destination.flags);
            ActivityCompat.startActivityForResult((Activity) context, intent, destination.requestCode, null);
        } else if (context instanceof Service) {
            Intent intent = new Intent(context, destination.aClass);
            intent.setPackage(context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (destination.extras != null) {
                intent.putExtras(destination.extras);
            }
            intent.addFlags(destination.flags);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                context.startActivity(intent, null);
            } else {
                context.startActivity(intent);
            }
        }
    }

    //目标页面是Service
    private void goRouteService(Context context, RouteDestination destination) {
        Intent intent = new Intent(context, destination.aClass);
        if (destination.extras != null) {
            intent.putExtras(destination.extras);
        }
        intent.addFlags(destination.flags);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    //目标页面是androidx.fragment.app.Fragment
    private void goRouteFragmentV4(Context context, RouteDestination destination) {
        if (TextUtils.isEmpty(destination.fragmentParentPath)) {
            throw new RuntimeException("miss fragment parent");
        }
        Config beforeConfig = BlackRouter.getRouteClass(destination.fragmentParentPath);
        RouteDestination fragmentParentDestination = new RouteDestination(destination.fragmentParentPath, beforeConfig);
        checkDestination(fragmentParentDestination);
        fragmentParentDestination.extras = destination.extras;
        fragmentParentDestination.requestCode = destination.requestCode;
        fragmentParentDestination.flags = destination.flags;
        fragmentParentDestination.fragmentIndex = destination.fragmentIndex;
        if (context.getClass().isAssignableFrom(fragmentParentDestination.aClass)) {
            //本页面内打开 Fragment
            if (context instanceof FragmentRouteHelper) {
                ((FragmentRouteHelper) context).openFragment(destination.aClass, destination.fragmentIndex, destination.extras);
            } else {
                throw new RuntimeException(context.getClass().getSimpleName() + " must implement FragmentRouteHelper");
            }
        } else {
            if (Activity.class.isAssignableFrom(fragmentParentDestination.aClass)) {
                //跳转到相应Activity 再打开 Fragment
                destination.fragmentParentPath = "";
                fragmentParentDestination.putExtras("routeUri", destination.uri);
                goRouteActivity(context, fragmentParentDestination);
            } else {
                throw new RuntimeException("fragment parent class must extend Activity");
            }
        }
    }

    public void goRealRoute(Context context, RouteDestination destination) {
        if (Activity.class.isAssignableFrom(destination.aClass)) {
            //目标页面是Activity
            goRouteActivity(context, destination);
        } else if (Service.class.isAssignableFrom(destination.aClass)) {
            //目标页面是Service
            goRouteService(context, destination);
        } else if (android.app.Fragment.class.isAssignableFrom(destination.aClass)) {
        } else if (androidx.fragment.app.Fragment.class.isAssignableFrom(destination.aClass)) {
            goRouteFragmentV4(context, destination);
        }
    }

    public void goRoute(Context context) {
        if (!TextUtils.isEmpty(destination.beforePath)) {
            //需要跳转中介页面,目前表示有前置条件
            if (context instanceof RouteCheckHelper) {
                ((RouteCheckHelper) context).routeCheck(destination.uri, destination.beforePath, destination.requestCode, destination.flags, destination.extras);
            } else {
                throw new RuntimeException(context.getClass().getSimpleName() + " must implement RouteCheckHelper");
            }
//            Config beforeConfig = BlackRouter.getRouteClass(destination.beforePath);
//            RouteDestination beforeDestination = new RouteDestination(destination.beforePath, beforeConfig);
//            destination.beforePath = "";
//            beforeDestination.putExtras("routeUri", destination);
//            checkDestination(beforeDestination);
//            goRealRoute(context, beforeDestination);
        } else {
            goRealRoute(context, destination);
        }
    }

    //目标页面是Activity
    private void goRouteActivity(androidx.fragment.app.Fragment fragment, RouteDestination destination) {
        //Fragment 打开 Activity
        Context context = fragment.getActivity();
        if (context == null) {
            throw new RuntimeException("fragment parent is null");
        }
        Intent intent = new Intent(context, destination.aClass);
        intent.setPackage(context.getPackageName());
        if (destination.extras != null) {
            intent.putExtras(destination.extras);
        }
        intent.addFlags(destination.flags);
        fragment.startActivityForResult(intent, destination.requestCode, null);
    }

    //目标页面是Service
    private void goRouteService(androidx.fragment.app.Fragment fragment, RouteDestination destination) {
        Context context = fragment.getContext();
        if (context == null) {
            throw new RuntimeException("fragment parent is null");
        }
        Intent intent = new Intent(context, destination.aClass);
        if (destination.extras != null) {
            intent.putExtras(destination.extras);
        }
        intent.addFlags(destination.flags);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    //目标页面是androidx.fragment.app.Fragment
    private void goRouteFragmentV4(androidx.fragment.app.Fragment fragment, RouteDestination destination) {
        if (TextUtils.isEmpty(destination.fragmentParentPath)) {
            throw new RuntimeException("miss fragment parent");
        }
        Config beforeConfig = BlackRouter.getRouteClass(destination.fragmentParentPath);
        RouteDestination fragmentParentDestination = new RouteDestination(destination.fragmentParentPath, beforeConfig);
        checkDestination(fragmentParentDestination);
        if (fragment.getClass().isAssignableFrom(fragmentParentDestination.aClass)) {
            //本页面内打开 Fragment
            if (fragment instanceof FragmentRouteHelper) {
                ((FragmentRouteHelper) fragment).openFragment(fragmentParentDestination.aClass, fragmentParentDestination.fragmentIndex, fragmentParentDestination.extras);
            } else {
                throw new RuntimeException(fragment.getClass().getSimpleName() + " must implement FragmentRouteHelper");
            }
        } else {
            if (Activity.class.isAssignableFrom(fragmentParentDestination.aClass)) {
                //跳转到相应Activity 再打开 Fragment
                destination.fragmentParentPath = "";
                fragmentParentDestination.putExtras("routeUri", destination);
                goRouteActivity(fragment, fragmentParentDestination);
            } else {
                throw new RuntimeException("fragment parent class must extend Activity");
            }
        }
    }

    public void goRealRoute(androidx.fragment.app.Fragment fragment, RouteDestination destination) {
        if (Activity.class.isAssignableFrom(destination.aClass)) {
            //目标页面是Activity
            goRouteActivity(fragment, destination);
        } else if (Service.class.isAssignableFrom(destination.aClass)) {
            //目标页面是Service
            goRouteService(fragment, destination);
        } else if (android.app.Fragment.class.isAssignableFrom(destination.aClass)) {
        } else if (androidx.fragment.app.Fragment.class.isAssignableFrom(destination.aClass)) {
            goRouteFragmentV4(fragment, destination);
        }
    }

    public void go(androidx.fragment.app.Fragment fragment) {
        go(fragment, null);
    }

    public void go(androidx.fragment.app.Fragment fragment, RouteCallback callback) {
        boolean result = true;
        Throwable error = null;
        try {
            if (BlackRouterHelper.class.isAssignableFrom(destination.aClass)) {
                BlackRouterHelper blackRouterHelper = (BlackRouterHelper) destination.aClass.newInstance();
                blackRouterHelper.bindRouteDestination(destination).goFragment(fragment);
            } else {
                if (!TextUtils.isEmpty(destination.beforePath)) {
                    //需要跳转中介页面
                    if (fragment instanceof RouteCheckHelper) {
                        ((RouteCheckHelper) fragment).routeCheck(destination.uri, destination.beforePath, destination.requestCode, destination.flags, destination.extras);
                    } else {
                        throw new RuntimeException(fragment.getClass().getSimpleName() + " must implement RouteCheckHelper");
                    }
                } else {
                    goRealRoute(fragment, destination);
                }
            }
        } catch (Throwable throwable) {
            result = false;
            error = throwable;
        } finally {
            if (callback != null) {
                callback.onResult(result, error);
            }
        }
    }
}
