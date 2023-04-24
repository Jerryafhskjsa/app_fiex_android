package com.black.net.okhttp;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OkWebSocket extends WebSocketListener {

    public static final String TAG = "OkWebSocket";
    private final HandlerThread mReceiveHandlerThread;

    public HandlerThread getReceiveHandlerThread() {
        return mReceiveHandlerThread;
    }

    private final HandlerThread mActionHandlerThread;
    private final Handler mReceiveHandler;

    private final Handler mActionHandler;

    public Handler getActionHandler() {
        return mActionHandler;
    }


    public static final int TIME = 10*1000;
    public static final int OK_MESSAGE_OPEN = 0x00001;
    public static final int OK_MESSAGE_MESSAGE = 0x00002;
    public static final int OK_MESSAGE_CLOSING = 0x00003;
    public static final int OK_MESSAGE_CLOSED = 0x00004;
    public static final int OK_MESSAGE_FAILURE = 0x00005;



    public static final int ACTION_PAUSE = 0x00001;
    public static final int ACTION_RESUME = 0x00002;
    public static final int ACTION_QUITE = 0x00003;

    public static final int ACTION_PING = 0x00004;


    private final List<IOkWebSocketMessage> mObserverList = Collections.synchronizedList(new ArrayList<>());

    private OkHttpClient.Builder createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{(TrustManager) (new X509TrustManager() {
                public void checkClientTrusted(@Nullable X509Certificate[] chain, @Nullable String authType) throws CertificateException {
                }

                public void checkServerTrusted(@Nullable X509Certificate[] chain, @Nullable String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            })};
            SSLContext var10000 = SSLContext.getInstance("SSL");
            SSLContext sslContext = var10000;
            sslContext.init((KeyManager[]) null, trustAllCerts, new SecureRandom());
            SSLSocketFactory var6 = sslContext.getSocketFactory();
            SSLSocketFactory sslSocketFactory = var6;
            TrustManager var10002 = trustAllCerts[0];
            if (trustAllCerts[0] == null) {
                throw new NullPointerException("null cannot be cast to non-null type javax.net.ssl.X509TrustManager");
            } else {
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) var10002);
                builder.hostnameVerifier((hostname, session) -> true);
                return builder;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder;
    }

    public static OkWebSocket createOkWebSocket(String url) {
        return new OkWebSocket(url);
    }

    private final String mTargetUrl;
    private WebSocket mWebSocket;

    public boolean sendMsg(String msg){
        d("sendMsg() called with: webSocket = [" + mWebSocket + "], msg = [" + msg + "]");
        if(mWebSocket!=null){
            return mWebSocket.send(msg);
        }
        return false;
    }

    public void addObserver(IOkWebSocketMessage observer) {
        if (!mObserverList.contains(observer)) {
            mObserverList.add(observer);
        }
    }

    public void removeObserver(IOkWebSocketMessage observer) {
        mObserverList.remove(observer);
    }


    private OkWebSocket(String url) {
        mTargetUrl = url;

        mReceiveHandlerThread = new HandlerThread(mTargetUrl);
        mReceiveHandlerThread.start();
        final Handler.Callback receiveCallback = msg -> {
            switch (msg.what) {
                case OK_MESSAGE_OPEN:
                    notifyObserverOnOpen();
                    break;
                case OK_MESSAGE_MESSAGE:
                    notifyObserverOnMessage((String) msg.obj);
                    break;
                case OK_MESSAGE_CLOSING:
                    final Bundle closing = msg.getData();
                    notifyObserverOnClosing(closing.getInt("code"), closing.getString("reason"));
                    break;
                case OK_MESSAGE_CLOSED:
                    final Bundle closed = msg.getData();
                    notifyObserverOnClosed(closed.getInt("code"), closed.getString("reason"));
                    break;
                case OK_MESSAGE_FAILURE:
                    notifyObserverOnError((Throwable) msg.obj);
                    break;
            }
            return false;
        };
        mReceiveHandler = new Handler(mReceiveHandlerThread.getLooper(), receiveCallback);

        mActionHandlerThread = new HandlerThread(mTargetUrl);
        mActionHandlerThread.start();

        Handler.Callback actionCallback = msg -> {
            switch (msg.what) {
                case ACTION_PAUSE:
                    performActionPause();
                    return true;
                case ACTION_RESUME:
                    performActionResume();
                    return true;
                case ACTION_QUITE:
                    performActionQuite();
                    return true;
                case ACTION_PING:
                    performActionPing();
                default:
                    break;
            }
            return false;
        };
        mActionHandler = new Handler(mActionHandlerThread.getLooper(), actionCallback);
        connection();
    }

    private void performActionPing() {
        if(mWebSocket!=null){
            mWebSocket.send("ping");
            sendPingMsg();
        }
    }

    private void sendPingMsg() {
        final Message obtain = Message.obtain();
        obtain.what = ACTION_PING;
        mActionHandler.sendMessageDelayed(obtain,TIME);
    }

    private void performActionQuite() {
        mWebSocket.cancel();
        mReceiveHandlerThread.quitSafely();
        mActionHandlerThread.quitSafely();
    }

    private void performActionResume() {
        // TODO: 2023/3/30
    }

    private void performActionPause() {
        // TODO: 2023/3/30
    }

    public void connection() {
        OkHttpClient okHttpClient = createOkHttpClient()
//                .pingInterval(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        Request request = new Request.Builder()
                .url(mTargetUrl)
                .build();
        mWebSocket = okHttpClient.newWebSocket(request, this);
    }


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        d("onOpen() called with: webSocket = [" + webSocket + "], response = [" + response + "]");
        super.onOpen(webSocket, response);
        mReceiveHandler.sendEmptyMessage(OK_MESSAGE_OPEN);
        sendPingMsg();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        d("onMessage() called with: webSocket = [" + webSocket + "], text = [" + text + "]");

        super.onMessage(webSocket, text);
        if(TextUtils.equals("pong",text)){
            return;
        }
        final Message obtain = Message.obtain();
        obtain.what = OK_MESSAGE_MESSAGE;
        obtain.obj = text;
//        final Bundle data = new Bundle();
//        data.putCharSequence();
//        obtain.setData(data);
        mReceiveHandler.sendMessage(obtain);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        d("onMessage() called with: webSocket = [" + webSocket + "], bytes = [" + bytes + "]");
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        d("onClosing() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
        super.onClosing(webSocket, code, reason);
        final Message obtain = Message.obtain();
        obtain.what = OK_MESSAGE_CLOSING;
        final Bundle data = new Bundle();
        data.putInt("code", code);
        data.putCharSequence("reason", reason);
        obtain.setData(data);
        mReceiveHandler.sendMessage(obtain);
    }


    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        d("onClosed() called with: webSocket = [" + webSocket + "], code = [" + code + "], reason = [" + reason + "]");
        final Message obtain = Message.obtain();
        obtain.what = OK_MESSAGE_CLOSED;
        final Bundle data = new Bundle();
        data.putInt("code", code);
        data.putCharSequence("reason", reason);
        obtain.setData(data);
        mReceiveHandler.sendMessage(obtain);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        d("onFailure() called with: webSocket = [" + webSocket + "], t = [" + t + "], response = [" + response + "]");
        super.onFailure(webSocket, t, response);
        final Message obtain = Message.obtain();
        obtain.what = OK_MESSAGE_FAILURE;
        t.printStackTrace();
        obtain.obj = t;
        mReceiveHandler.sendMessage(obtain);
    }

//    public void getStatus(){
//        if (mWebSocket!=null) {
//            mWebSocket.send()
//        }
//    }

    public void disconnect() {
        if (mWebSocket != null) {
//            mWebSocket.close(-1,"user_cancel");
            mWebSocket.cancel();
        }
    }

    public boolean canUse() {
        if (mWebSocket != null) {
            return mWebSocket.send("");
        }
        return false;
    }

    public void ensureOk() {
        if (!canUse()) {
            if (mWebSocket != null) {
                mWebSocket.cancel();
            }
            connection();
        }
    }

    private void d(String msg) {
        Log.d(OkWebSocket.TAG, msg);
    }

    private void notifyObserverOnOpen() {
        final int size = mObserverList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IOkWebSocketMessage observer = mObserverList.get(i);
            observer.onOpen();
        }
    }

    private void notifyObserverOnMessage(String msg) {
        final int size = mObserverList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IOkWebSocketMessage observer = mObserverList.get(i);
            observer.onMessage(msg);
        }
    }

    private void notifyObserverOnClosing(int code, String reason) {
        final int size = mObserverList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IOkWebSocketMessage observer = mObserverList.get(i);
            observer.onClosing(code, reason);
        }
    }

    private void notifyObserverOnClosed(int code, String reason) {
        final int size = mObserverList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IOkWebSocketMessage observer = mObserverList.get(i);
            observer.onClosed(code, reason);
        }
    }

    private void notifyObserverOnError(Throwable throwable) {
        final int size = mObserverList.size();
        for (int i = size - 1; i >= 0; i--) {
            final IOkWebSocketMessage observer = mObserverList.get(i);
            observer.onError(throwable);
        }
    }

    interface IOkWebSocketMessage {
        void onOpen();

        void onMessage(String msg);

        void onClosing(int code, String reason);

        void onClosed(int code, String reason);

        void onError(Throwable throwable);
    }
}
