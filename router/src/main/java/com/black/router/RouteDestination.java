package com.black.router;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.black.router.annotation.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouteDestination implements Parcelable, Cloneable {
    public String uri;
    public Class aClass;
    public String beforePath;//该字段用于判断是否需要check，调用方需要实现RouteCheckHelper
    public String fragmentParentPath;
    public int fragmentIndex;
    public int requestCode = -1;
    public int flags;

    public Bundle extras;
    public BlackRouterSingle single;

    public boolean isFinal;

    public RouteDestination(String uri, Config config) {
        this.uri = uri;
        this.aClass = config == null ? null : config.clz;
        this.beforePath = config == null ? null : config.beforePath;
        this.fragmentParentPath = config == null ? null : config.fragmentParentPath;
        this.fragmentIndex = config == null ? 0 : config.fragmentIndex;
    }

    protected RouteDestination(Parcel in) {
        uri = in.readString();
        aClass = (Class) in.readSerializable();
        beforePath = in.readString();
        fragmentParentPath = in.readString();
        fragmentIndex = in.readInt();
        extras = in.readBundle();
    }

    public static final Creator<RouteDestination> CREATOR = new Creator<RouteDestination>() {
        @Override
        public RouteDestination createFromParcel(Parcel in) {
            return new RouteDestination(in);
        }

        @Override
        public RouteDestination[] newArray(int size) {
            return new RouteDestination[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeSerializable(aClass);
        dest.writeString(beforePath);
        dest.writeString(fragmentParentPath);
        dest.writeInt(fragmentIndex);
        dest.writeBundle(extras);
    }

    public Bundle getExtras() {
        if (extras == null) {
            extras = new Bundle();
        }
        return extras;
    }

    public void putExtras(String key, String value) {
        getExtras().putString(key, value);
    }

    public void putExtras(String routeUri, RouteDestination destination) {
        getExtras().putParcelable(routeUri, destination);
    }

    public void addUriParams() {
        if (uri != null) {
            try {
                Uri uri = Uri.parse(this.uri);
                Set<String> names = uri.getQueryParameterNames();
                Map<String, String> uriParams = new HashMap<>();
                for (String name : names) {
                    if (!TextUtils.isEmpty(name)) {
                        uriParams.put(name, uri.getQueryParameter(name));
                    }
                }
                for (String key : uriParams.keySet()) {
                    putExtras(key, uriParams.get(key));
                }
            } catch (Exception ignored) {

            }
        }
    }

}
