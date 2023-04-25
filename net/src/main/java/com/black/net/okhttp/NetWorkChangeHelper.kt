package com.black.net.okhttp

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager

object NetWorkChangeHelper {
    private var netWorkReceiver: FutureNetWorkReceiver? = null

    fun reListener(lis: FutureNetWorkReceiver.INetWorkChangeListener){
        netWorkReceiver?.addListener(lis)
    }

    fun re(context: Context) {
        netWorkReceiver = FutureNetWorkReceiver();
        val filter = IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netWorkReceiver, filter);
    }

    fun unre(context: Context) {
        netWorkReceiver?.apply {
            context.unregisterReceiver(this)
            clearListener()
        }
        netWorkReceiver = null
    }

}