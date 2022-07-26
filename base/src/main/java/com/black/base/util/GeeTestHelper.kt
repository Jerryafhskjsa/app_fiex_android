package com.black.base.util

import android.content.Context
import android.text.TextUtils
import com.black.util.CommonUtil
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import org.json.JSONObject

class GeeTestHelper(context: Context?) {
    private val gt3GeetestUtils: GT3GeetestUtils = GT3GeetestUtils(context)
    private val gt3ConfigBean: GT3ConfigBean = GT3ConfigBean()
    private var geeTestCallback: GeeTestCallback? = null
    private val geeTestApi1Callback = GeeTestApi1Callback()
    private val geeTestApi2Callback = GeeTestApi2Callback()
    fun startVerify(geeTestCallback: GeeTestCallback?) {
        if (geeTestCallback != null) {
            this.geeTestCallback = geeTestCallback
            gt3GeetestUtils.startCustomFlow()
        }
    }

    fun release() {
        gt3GeetestUtils.destory()
    }

    inner class GeeTestApi1Callback {
        fun callback(jsonObject: JSONObject?) {
            gt3ConfigBean.api1Json = jsonObject
            // 继续api验证
            gt3GeetestUtils.getGeetest()
        }
    }

    inner class GeeTestApi2Callback {
        fun callback(success: Boolean) {
            if (success) {
                gt3GeetestUtils.showSuccessDialog()
            } else {
                gt3GeetestUtils.showFailedDialog()
            }
        }

        fun dismiss() {
            gt3GeetestUtils.dismissGeetestDialog()
        }
    }

    companion object {
        private const val TAG = "GeeTestHelper"
    }

    init {
        // 配置bean文件，也可在oncreate初始化
        // 设置验证模式，1：bind，2：unbind
        gt3ConfigBean.pattern = 1
        // 设置点击灰色区域是否消失，默认不消失
        gt3ConfigBean.isCanceledOnTouchOutside = false
        // 设置debug模式，TODO 线上版本关闭
        gt3ConfigBean.isDebug = CommonUtil.isApkInDebug(context)
        // 设置语言，如果为null则使用系统默认语言
        gt3ConfigBean.lang = null
        // 设置加载webview超时时间，单位毫秒，默认10000，仅且webview加载静态文件超时，不包括之前的http请求
        gt3ConfigBean.timeout = 30000
        // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
        gt3ConfigBean.webviewTimeout = 30000
        // 设置自定义view
//        gt3ConfigBean.setLoadImageView(new TestLoadingView(this));
// 设置回调监听
        gt3ConfigBean.listener = object : GT3Listener() {
            /**
             * api1结果回调
             * @param result
             */
            override fun onApi1Result(result: String) {}

            /**
             * 验证码加载完成
             * @param duration 加载时间和版本等信息，为json格式
             */
            override fun onDialogReady(duration: String) {}

            /**
             * 验证结果
             * @param result
             */
            override fun onDialogResult(result: String) { // 开启api2逻辑
                if (geeTestCallback != null) {
                    geeTestCallback!!.onApi2(result, geeTestApi2Callback)
                }
            }

            /**
             * api2回调
             * @param result
             */
            override fun onApi2Result(result: String) { //                GeeTestResult geeTestResult = gson.fromJson(result, GeeTestResult.class);
//                getPhoneVerifyCode(geeTestResult);
                if (!TextUtils.isEmpty(result)) {
                    try {
                        val jsonObject = JSONObject(result)
                        val status = jsonObject.getString("status")
                        if ("success" == status) {
                            gt3GeetestUtils.showSuccessDialog()
                        } else {
                            gt3GeetestUtils.showFailedDialog()
                        }
                    } catch (e: Exception) {
                        gt3GeetestUtils.showFailedDialog()
                    }
                } else {
                    gt3GeetestUtils.showFailedDialog()
                }
            }

            /**
             * 统计信息，参考接入文档
             * @param result
             */
            override fun onStatistics(result: String) {}

            /**
             * 验证码被关闭
             * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
             */
            override fun onClosed(num: Int) {}

            /**
             * 验证成功回调
             * @param result
             */
            override fun onSuccess(result: String) {}

            /**
             * 验证失败回调
             * @param errorBean 版本号，错误码，错误描述等信息
             */
            override fun onFailed(errorBean: GT3ErrorBean) {}

            /**
             * api1回调
             */
            override fun onButtonClick() {
                if (geeTestCallback != null) {
                    geeTestCallback!!.onApi1(geeTestApi1Callback)
                }
            }
        }
        gt3GeetestUtils.init(gt3ConfigBean)
    }
}