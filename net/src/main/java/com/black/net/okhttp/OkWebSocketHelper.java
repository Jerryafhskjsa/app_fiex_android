package com.black.net.okhttp;

import android.util.Log;

public class OkWebSocketHelper implements OkWebSocket.IOkWebSocketMessage {
    public static final String TAG = "OkWebSocketHelper";
    private final OkWebSocket mOkWebSocket;

    private boolean running = false;
    public OkWebSocket getOkWebSocket() {
        return mOkWebSocket;
    }

    public OkWebSocketHelper(OkWebSocket okWebSocket) {
        mOkWebSocket = okWebSocket;

    }

    public void pause(){
        running = false;
    }
    public void resume(){
        running = true;
    }
    public void stop(){
        mOkWebSocket.removeObserver(this);
    }
    public void start() {
        mOkWebSocket.addObserver(this);
    }

    private void d(String message) {
        Log.d(TAG, message);
    }

    public void testSend() {

    }

    @Override
    public void onOpen() {
        d("onOpen() called");
    }

    @Override
    public void onMessage(String msg) {
        d("onMessage() called with: msg = [" + msg + "]");
    }

    @Override
    public void onClosing(int code, String reason) {
        d("onClosing() called with: code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onClosed(int code, String reason) {
        d("onClosed() called with: code = [" + code + "], reason = [" + reason + "]");
    }

    @Override
    public void onError(Throwable throwable) {
        d("onError() called with: throwable = [" + throwable + "]");
    }
}
