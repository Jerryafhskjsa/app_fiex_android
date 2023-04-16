package com.black.net.okhttp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OKWebSocketFactory {
    static Map<String, OkWebSocket> _cacheOkWebSocket = new ConcurrentHashMap<>();


    //todo 网络监听重新连接逻辑
    public static synchronized OkWebSocket getOkWebSocket(String url) {
        OkWebSocket okWebSocket = _cacheOkWebSocket.get(url);
        if (okWebSocket != null) {
            boolean canUse = okWebSocket.canUse();
            if (!canUse) {
                okWebSocket.ensureOk();
            }
            return okWebSocket;
        } else {
            okWebSocket = OkWebSocket.createOkWebSocket(url);
        }
        return okWebSocket;
    }
}
