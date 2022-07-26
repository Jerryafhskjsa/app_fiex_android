package com.black.im.util

import android.R
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager

object ScreenUtil {
    private val TAG = ScreenUtil::class.java.simpleName
    var navigationBarHeight = 0
        get() {
            if (field != 0) return field
            val resources = TUIKit.appContext.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val height = resources.getDimensionPixelSize(resourceId)
            field = height
            return height
        }
        private set

    private var SOFT_INPUT_HEIGHT = 0
    fun checkNavigationBarShow(context: Context, window: Window): Boolean {
        val show: Boolean
        val display = window.windowManager.defaultDisplay
        val point = Point()
        display.getRealSize(point)
        val decorView = window.decorView
        val conf = context.resources.configuration
        show = if (Configuration.ORIENTATION_LANDSCAPE == conf.orientation) {
            val contentView = decorView.findViewById<View>(R.id.content)
            point.x != contentView.width
        } else {
            val rect = Rect()
            decorView.getWindowVisibleDisplayFrame(rect)
            rect.bottom != point.y
        }
        return show
    }

    val screenSize: IntArray
        get() {
            val size = IntArray(2)
            val dm = TUIKit.appContext.resources.displayMetrics
            size[0] = dm.widthPixels
            size[1] = dm.heightPixels
            return size
        }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = TUIKit.appContext.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = TUIKit.appContext.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    fun getScreenHeight(context: Context): Int {
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    fun getPxByDp(dp: Int): Int {
        val scale = TUIKit.appContext.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun getDpi(context: Context): Int {
        var dpi = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, displayMetrics)
            dpi = displayMetrics.heightPixels
        } catch (e: Exception) {
        }
        return dpi
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    fun getBottomStatusHeight(context: Context): Int {
        if (SOFT_INPUT_HEIGHT > 0) return SOFT_INPUT_HEIGHT
        val totalHeight = getDpi(context)
        val contentHeight = getScreenHeight(context)
        SOFT_INPUT_HEIGHT = totalHeight - contentHeight
        return SOFT_INPUT_HEIGHT
    }

    fun scaledSize(containerWidth: Int, containerHeight: Int, realWidth: Int, realHeight: Int): IntArray {
        TUIKitLog.i(TAG, "scaledSize  containerWidth: " + containerWidth + " containerHeight: " + containerHeight
                + " realWidth: " + realWidth + " realHeight: " + realHeight)
        val deviceRate = containerWidth.toFloat() / containerHeight.toFloat()
        val rate = realWidth.toFloat() / realHeight.toFloat()
        var width = 0
        var height = 0
        if (rate < deviceRate) {
            height = containerHeight
            width = (containerHeight * rate).toInt()
        } else {
            width = containerWidth
            height = (containerWidth / rate).toInt()
        }
        return intArrayOf(width, height)
    }
}