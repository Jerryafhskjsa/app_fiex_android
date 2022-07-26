package com.black.util;

import android.hardware.fingerprint.FingerprintManager;

import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

//封装指纹识别回调
public class FingerPrintCallBack extends FingerprintManagerCompat.AuthenticationCallback {
    /**
     * 验证成功
     */
    public final static int SUCCESS = 0;
    /**
     * 验证失败
     */
    public final static int CHECK_FAILED = 1;
    /**
     * 失败次数超过上限
     */
    public final static int ERROR_OVER_MAX = 2;
    /**
     * 其他错误
     */
    public final static int ERROR_OTHER = 4;
    private Callback<Integer> callback;

    public FingerPrintCallBack(Callback<Integer> callback) {
        this.callback = callback;
    }

    // 当出现错误的时候回调此函数，比如多次尝试都失败了的时候，errString是错误信息
    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        int errorCode;
        switch (errMsgId) {
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                errorCode = ERROR_OVER_MAX;
                break;
            default:
                errorCode = ERROR_OTHER;
                break;
        }
        if (callback != null) {
            callback.error(errorCode, null);
        }
//            checkMessageView.setText("onAuthenticationError: " + errString);
    }

    // 当指纹验证失败的时候会回调此函数，失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
    @Override
    public void onAuthenticationFailed() {
        if (callback != null) {
            callback.error(CHECK_FAILED, null);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
//            checkMessageView.setText("onAuthenticationHelp: " + helpString);
    }

    // 当验证的指纹成功时会回调此函数，然后不再监听指纹sensor
    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
//            checkMessageView.setText("onAuthenticationSucceeded: " + "验证成功");
        if (callback != null) {
            callback.callback(SUCCESS);
        }
    }
}
