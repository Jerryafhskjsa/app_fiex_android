package com.black.net.okhttp

import android.os.Handler
import android.os.Message
import com.black.net.okhttp.FutureNetWorkReceiver.INetWorkChangeListener
import com.black.net.okhttp.NetWorkChangeHelper.reListener
import java.util.concurrent.ConcurrentHashMap

object OKWebSocketFactory {
    const val WHAT_WIFI = 1
    const val WHAT_MOBILE = 2
    private const val WHAT_REG_NET_LISTEN = 3
    const val T_DELAY_CHANGE = 4 * 500L

    private var _cacheOkWebSocket: MutableMap<String, OkWebSocket> = ConcurrentHashMap()
    val mHandler by lazy {
        Handler(Handler.Callback {
            when (it.what) {
                WHAT_WIFI, WHAT_MOBILE -> {
                    _cacheOkWebSocket.forEach {
                        it.value.ensureOk()
                    }
                }
                WHAT_REG_NET_LISTEN->{
                    initListener()
                }
            }
            return@Callback false
        })
    }

    init {
        mHandler.sendEmptyMessageDelayed(WHAT_REG_NET_LISTEN,T_DELAY_CHANGE)
    }

    //todo 网络监听重新连接逻辑
    @Synchronized
    fun getOkWebSocket(url: String): OkWebSocket {
        var okWebSocket = _cacheOkWebSocket[url]
        if (okWebSocket != null) {
            val canUse = okWebSocket.canUse()
            if (!canUse) {
                okWebSocket.ensureOk()
            }
            return okWebSocket
        } else {
            okWebSocket = OkWebSocket.createOkWebSocket(url).apply {
                _cacheOkWebSocket[url] = this
            }
        }
        return okWebSocket
    }


    private fun initListener() {
        reListener(object : INetWorkChangeListener {
            override fun onNone() {}
            override fun onWifi() {
                mHandler.removeMessages(WHAT_WIFI)
                mHandler.sendEmptyMessageDelayed(WHAT_WIFI, T_DELAY_CHANGE)
            }

            override fun onMobile() {
                mHandler.removeMessages(WHAT_MOBILE)
                mHandler.sendEmptyMessageDelayed(WHAT_MOBILE, T_DELAY_CHANGE)
            }
        })
    }
}