package com.black.im.util

import android.widget.Toast

/**
 * UI通用方法类
 */
object ToastUtil {
    private var mToast: Toast? = null
    fun toastLongMessage(message: String?) {
        BackgroundTasks.instance?.runOnUiThread(Runnable {
            if (mToast != null) {
                mToast!!.cancel()
                mToast = null
            }
            mToast = Toast.makeText(TUIKit.appContext, message,
                    Toast.LENGTH_LONG)
            mToast?.show()
        })
    }

    fun toastShortMessage(message: String?) {
        BackgroundTasks.instance?.runOnUiThread(Runnable {
            if (mToast != null) {
                mToast!!.cancel()
                mToast = null
            }
            mToast = Toast.makeText(TUIKit.appContext, message,
                    Toast.LENGTH_SHORT)
            mToast?.show()
        })
    }
}