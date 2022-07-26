package com.black.im.video.util

import android.os.Build

object DeviceUtil {
    private val huaweiRongyao = arrayOf(
            "hwH60",  //荣耀6
            "hwPE",  //荣耀6 plus
            "hwH30",  //3c
            "hwHol",  //3c畅玩版
            "hwG750",  //3x
            "hw7D",  //x1
            "hwChe2")

    val deviceInfo: String
        get() = "手机型号：" + Build.DEVICE +
                "\n系统版本：" + Build.VERSION.RELEASE +
                "\nSDK版本：" + Build.VERSION.SDK_INT

    val deviceModel: String
        get() = Build.DEVICE

    val isHuaWeiRongyao: Boolean
        get() {
            val length = huaweiRongyao.size
            for (i in 0 until length) {
                if (huaweiRongyao[i] == deviceModel) {
                    return true
                }
            }
            return false
        }
}