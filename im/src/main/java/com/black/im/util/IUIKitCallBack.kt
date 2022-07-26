package com.black.im.util

/**
 * UIKit回调的通用接口类
 */
interface IUIKitCallBack {
    fun onSuccess(data: Any?)
    fun onError(module: String?, errCode: Int, errMsg: String?)
}
