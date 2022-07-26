package com.black.base.lib

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IntDef
import com.black.base.R
import com.black.base.util.StatusBarUtil.getStatusBarHeight
import com.black.base.view.FryingToast
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources
import java.lang.reflect.Field
import kotlin.math.max

//实现toast快速消失，并且只存在当前toast，当前的toast需要显示，旧的toast立即关闭
//并提供关闭接口，用户可自主关闭toast
object FryingSingleToast {
    const val NORMAL = 0
    const val ERROR = 1
    private var toast: Toast? = null
    private var fryingToast: FryingToast? = null
    fun show(context: Context, msgId: Int) {
        show(context, context.getString(msgId))
    }

    @JvmOverloads
    fun show(context: Context, msg: String?, @Type type: Int = NORMAL) {
        if (toast != null) {
            hide()
        }
        CommonUtil.checkActivityAndRun(context) {
            toast = createToast(context, msg, type)
            toast!!.show()
        }
        //        if (fryingToast != null) {
//            hide();
//        }
//        fryingToast = createFryingToast(context, msg, type);
//        fryingToast.show();
//        try {
//            Field field = toast.getClass().getDeclaredField("mTN");
//            field.setAccessible(true);
//            Object obj = field.get(toast);
//            Method method = obj.getClass().getDeclaredMethod("show", null);
//            method.invoke(obj, null);
//        } catch (Exception e) {
//            e.printError();
//        }
    }

    fun hide() {
        if (toast != null) {
            toast!!.cancel()
//            try {
//                Field field = toast.getClass().getDeclaredField("mTN");
//                field.setAccessible(true);
//                Object obj = field.get(toast);
//                Method method = obj.getClass().getDeclaredMethod("hide", null);
//                method.invoke(obj, null);
//            } catch (Exception e) {
//                e.printError();
//            }
            toast = null
        }
        if (fryingToast != null) {
            fryingToast!!.hide()
            fryingToast = null
        }
    }

    private fun createToast(context: Context, msg: String?, type: Int): Toast {
        val inflater = LayoutInflater.from(context) //调用Activity的getLayoutInflater()
        val view = inflater.inflate(R.layout.toast_style, null) //加載layout下的布局
        setToastViewImmerse(view)
        val text = view.findViewById<TextView>(R.id.toast_message)
        if (type == ERROR) {
            val drawable = ColorDrawable()
            drawable.color = SkinCompatResources.getColor(context, R.color.top_message_error)
            view.background = drawable
        }
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        val vlp = LinearLayout.LayoutParams(outMetrics.widthPixels,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        vlp.setMargins(0, 0, 0, 0)
        text.layoutParams = vlp
        text.minHeight = max(getActionBarHeight(context).toFloat(), 31 * outMetrics.density).toInt()
        text.text = msg //toast内容
        val toast = Toast(context)
        toast.setGravity(Gravity.TOP, 0, 0) //setGravity用来设置Toast显示的位置，相当于xml中的android:gravity或android:layout_gravity
        toast.duration = Toast.LENGTH_SHORT //setDuration方法：设置持续时间，以毫秒为单位。该方法是设置补间动画时间长度的主要方法
        toast.view = view //添加视图文件
        initTN(toast)
        return toast
    }

    private fun setToastViewImmerse(view: View) {
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val headerPaddingLeft = view.paddingLeft
        val headerPaddingTop = view.paddingTop + getStatusBarHeight(view.context)
        val headerPaddingRight = view.paddingRight
        val headerPaddingBottom = view.paddingBottom
        view.setPadding(headerPaddingLeft, headerPaddingTop, headerPaddingRight, headerPaddingBottom)
    }

    private fun createFryingToast(context: Context, msg: String, type: Int): FryingToast {
        return FryingToast(context, msg, 2000)
    }

    private fun getActionBarHeight(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.textAppearanceLarge, typedValue, true)
        val attribute = intArrayOf(android.R.attr.actionBarSize)
        val array = context.obtainStyledAttributes(typedValue.resourceId, attribute)
        val height = array.getDimensionPixelSize(0 /* index */, -1 /* default size */)
        array.recycle()
        return height
    }

    /**
     * 通过反射获得mTN下的show和hide方法
     */
    private fun initTN(toast: Toast) {
        val clazz = Toast::class.java
        try {
            val mTN = clazz.getDeclaredField("mTN")
            mTN.isAccessible = true
            val mObj = mTN[toast]
            // 取消掉各个系统的默认toast弹出动画 modify by yangsq 2014-01-04
            val field: Field? = mObj.javaClass.getDeclaredField("mParams")
            if (field != null) {
                field.isAccessible = true
                val mParams = field[mObj]
                if (mParams is WindowManager.LayoutParams) {
//                    Log.e("singleToast", "windowAnimations：" + params.windowAnimations);
//                    params.windowAnimations = R.style.anim_top_in_out;
                    mParams.windowAnimations = -1
                    mParams.width = WindowManager.LayoutParams.MATCH_PARENT //设置Toast宽度为屏幕宽度
                    mParams.height = WindowManager.LayoutParams.WRAP_CONTENT //设置高度
                }
            }
        } catch (e: IllegalAccessException) {
        } catch (e: IllegalArgumentException) {
        } catch (e: NoSuchFieldException) {
        }
    }

    @IntDef(NORMAL, ERROR)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Type
}