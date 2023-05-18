package com.black.net.okhttp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OkWebSocketHelper implements OkWebSocket.IOkWebSocketMessage {
    public static final String TAG = "OkWebSocketHelper";
    public static final Boolean DEBUG = false;

    private final OkWebSocket mOkWebSocket;

    private volatile boolean running = true;

    public OkWebSocket getOkWebSocket() {
        return mOkWebSocket;
    }

    private IMessageLifeCycle mIMessageLifeCycle = null;

    private IMessageHandler mImessageHandler = null;

    private List<IMessageHandler> mMessageHandlers = Collections.synchronizedList(new ArrayList<>());

    public void setImessageHandler(IMessageHandler mImessageHandler) {
        this.mImessageHandler = mImessageHandler;
    }

    public void setIMessageLifeCycle(IMessageLifeCycle mIMessageLifeCycle) {
        this.mIMessageLifeCycle = mIMessageLifeCycle;
    }

    public OkWebSocketHelper(OkWebSocket okWebSocket) {
        mOkWebSocket = okWebSocket;
    }

    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
    }

    public void stop() {
        mOkWebSocket.removeObserver(this);
    }

    public void start() {
        mOkWebSocket.addObserver(this);
    }

    private void d(String message) {
        if(DEBUG)
        Log.d(TAG, message);
    }

    public void testSend() {

    }

    @Override
    public void onOpen() {
        d("onOpen() called");
        if (!running) {
            return;
        }
        if (mIMessageLifeCycle != null) {
            mIMessageLifeCycle.onOpen();
        }
    }

    @Override
    public void onMessage(String msg) {
        d("onMessage() called with: msg = [" + msg + "]");
        if (!running) {
            return;
        }
        if (mIMessageLifeCycle != null) {
            mIMessageLifeCycle.onMessage(msg);
        }
        try {
            final JSONObject json = new JSONObject(msg);
            final String channel = json.optString("channel");
            final Object data = json.opt("data");
            if (mImessageHandler != null ) {
                final boolean observer = mImessageHandler.observerMessage(channel, data);
                if (observer) {
                    mImessageHandler.processingMessage(data);
                }
            }
            notifyMessageHandler(channel,data);
        } catch (JSONException e) {
            d("onMessage  =====> error message");
            e.printStackTrace();
        }
    }

    @Override
    public void onClosing(int code, String reason) {
        d("onClosing() called with: code = [" + code + "], reason = [" + reason + "]");
        if (!running) {
            return;
        }
        if (mIMessageLifeCycle != null) {
            mIMessageLifeCycle.onClosing(code, reason);
        }
    }

    @Override
    public void onClosed(int code, String reason) {
        d("onClosed() called with: code = [" + code + "], reason = [" + reason + "]");
        if (!running) {
            return;
        }
        if (mIMessageLifeCycle != null) {
            mIMessageLifeCycle.onClosed(code, reason);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        d("onError() called with: throwable = [" + throwable + "]");
        if (!running) {
            return;
        }
        if (mIMessageLifeCycle != null) {
            mIMessageLifeCycle.onError(throwable);
        }
    }

    public OkWebSocketHelper addMessageHandler(IMessageHandler messageHandler) {
        mMessageHandlers.add(messageHandler);
        return this;
    }

    private void notifyMessageHandler(String channel, Object data) {
        final int size = mMessageHandlers.size();
        for (int i = size - 1; i >= 0; i--) {
            final IMessageHandler messageHandler = mMessageHandlers.get(i);
            if (messageHandler.observerMessage(channel, data)) {
                messageHandler.processingMessage(data);
            }
        }
    }

    public interface IMessageHandler {
        /**
         * data  is  jsonObject  or  jsonArray
         * 可用 泛型 替代
         */
        boolean observerMessage(String channel, Object data);

        void processingMessage(Object t);
    }

    public interface IMessageLifeCycle {
        void onOpen();

        void onMessage(String msg);

        void onClosing(int code, String reason);

        void onClosed(int code, String reason);

        void onError(Throwable throwable);

    }
}
