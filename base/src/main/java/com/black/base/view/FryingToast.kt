package com.black.base.view

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.black.base.R

class FryingToast(private val mContext: Context, text: CharSequence?, duration: Int) {
    private val mWindowManager //整个Android的窗口机制是基于一个叫做 WindowManager
            : WindowManager
    private val mToastView: View?
    private var mParams: WindowManager.LayoutParams? = null
    private var mHandler: Handler? = null
    private var mShowTime = LENGTH_SHORT //记录Toast的显示长短类型LENGTH_LONG/LENGTH_SHORT
    private var mIsShow //记录当前Toast的内容是否已经在显示
            : Boolean

    /**
     * 设置布局参数
     * 这个设置参数更多是参考源代码中原生Toast的设置参数的类型
     * 在这里我们需要注意的是 mParams.windowAnimations = R.style.anim_view;这个是我们这个仿MIUI的Toast动画实现的基石。
     */
    private fun setParams() {
        mParams = WindowManager.LayoutParams()
        mParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        mParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
        mParams?.format = PixelFormat.TRANSLUCENT
        mParams?.windowAnimations = R.style.anim_top_in_out //设置进入退出动画效果
        mParams?.type = WindowManager.LayoutParams.TYPE_TOAST
        mParams?.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        mParams?.alpha = 1.0f
        mParams?.title = "ToastMiui"
        mParams?.packageName = mContext.packageName
        mParams?.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM //设置显示的位置
        mParams?.y = 50 //设置偏移量
    }

    /**
     * 1、gravity是输入Toast需要显示的位置，例如CENTER_VERTICAL（垂直居中）、CENTER_HORIZONTAL（水平居中）、TOP（顶部）等等。
     * 2、xOffset则是决定Toast在水平方向（x轴）的偏移量，偏移量单位为，大于0向右偏移，小于0向左偏移
     * 3、yOffset决定Toast在垂直方向（y轴）的偏移量，大于0向下偏移，小于0向上偏移，想设大值也没关系，反正Toast不会跑出屏幕。
     */
    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        mParams?.gravity = gravity
        mParams?.x = xOffset
        mParams?.y = yOffset
    }

    fun setText(s: CharSequence?) {
        val tv = mToastView?.findViewById<View>(R.id.toast_message) as TextView
        tv.text = s
    }

    fun setText(resId: Int) {
        val tv = mToastView?.findViewById<View>(R.id.toast_message) as TextView
        tv.setText(resId)
    }

    fun show() {
        if (!mIsShow) { //如果Toast没有显示，则开始加载显示
            mIsShow = true
            mWindowManager.addView(mToastView, mParams)
            if (mHandler == null) {
                mHandler = Handler()
            }
            mHandler?.postDelayed(timerRunnable, mShowTime.toLong())
        }
    }

    fun hide() {
        removeView()
    }

    private val timerRunnable = Runnable { removeView() }

    fun removeView() {
        if (mToastView != null && mToastView.parent != null) {
            mWindowManager.removeView(mToastView)
            mHandler?.removeCallbacks(timerRunnable)
            mIsShow = false
        }
    }

    companion object {
        /**
         * 显示的时间（长）
         */
        const val LENGTH_LONG = 3500
        /**
         * 显示的时间（短）
         */
        const val LENGTH_SHORT = 2000

        /**
         * 逻辑简单粗暴，直接调用构造函数实例化一个MiuiToast对象并返回。
         */
        fun MakeText(context: Context, text: CharSequence?, duration: Int): FryingToast {
            return FryingToast(context, text, duration)
        }

        fun MakeText(context: Context, toastStrId: Int, showTime: Int): FryingToast {
            return FryingToast(context, context.getString(toastStrId), showTime)
        }
    }

    /**
     * 在构造方法中，更多的是对数据的初始化，由于设置布局参数比较多，所以单独抽出一个函数来
     */
    init {
        mShowTime = duration //记录Toast的显示长短类型
        mIsShow = false //记录当前Toast的内容是否已经在显示
        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //通过inflate获取自定义的Toast布局文件
        mToastView = LayoutInflater.from(mContext).inflate(R.layout.toast_style, null)
        val tv = mToastView.findViewById<View>(R.id.toast_message) as TextView
        tv.text = text
        //设置布局参数
        setParams()
    }
}