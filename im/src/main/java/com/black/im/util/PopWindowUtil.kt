package com.black.im.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.black.im.R

object PopWindowUtil {
    fun buildCustomDialog(activity: Activity): AlertDialog? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed) return null
        }
        val builder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.Theme_Transparent))
        builder.setTitle("")
        builder.setCancelable(true)
        val dialog = builder.create()
        //dialog.getWindow().setDimAmount(0);
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    fun buildFullScreenDialog(activity: Activity): AlertDialog? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed) return null
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("")
        builder.setCancelable(true)
        val dialog = builder.create()
        dialog.window.setDimAmount(0f)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        dialog.window.setBackgroundDrawable(null)
        return dialog
    }

    fun buildEditorDialog(context: Context?, title: String?, cancel: String?, sure: String?, autoDismiss: Boolean, listener: EnsureListener?): AlertDialog? {
        if (context is Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (context.isDestroyed) return null
            }
        }
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
        val baseView = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null)
        (baseView.findViewById<View>(R.id.dialog_title) as TextView).text = title
        (baseView.findViewById<View>(R.id.dialog_cancel_btn) as TextView).text = cancel
        (baseView.findViewById<View>(R.id.dialog_sure_btn) as TextView).text = sure
        val editText = baseView.findViewById<EditText>(R.id.dialog_editor)
        baseView.findViewById<View>(R.id.dialog_sure_btn).setOnClickListener {
            SoftKeyBoardUtil.hideKeyBoard(editText)
            listener?.sure(editText.text.toString())
            dialog.dismiss()
            if (autoDismiss) dialog.dismiss()
        }
        baseView.findViewById<View>(R.id.dialog_cancel_btn).setOnClickListener {
            SoftKeyBoardUtil.hideKeyBoard(editText)
            listener?.cancel()
            dialog.dismiss()
        }
        val lp = dialog.window.attributes
        lp.width = ScreenUtil.getPxByDp(320) //定义宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT //定义高度
        dialog.window.attributes = lp
        dialog.setContentView(baseView)
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        return dialog
    }

    fun buildEnsureDialog(context: Context?, title: String?, content: String?, cancel: String?, sure: String?, listener: EnsureListener?): AlertDialog? {
        if (context is Activity) {
            if (context.isDestroyed) return null
        }
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
        val baseView = LayoutInflater.from(context).inflate(R.layout.layout_ensure_dialog, null)
        (baseView.findViewById<View>(R.id.dialog_title) as TextView).text = title
        (baseView.findViewById<View>(R.id.dialog_cancel_btn) as TextView).text = cancel
        (baseView.findViewById<View>(R.id.dialog_sure_btn) as TextView).text = sure
        if (!TextUtils.isEmpty(content)) (baseView.findViewById<View>(R.id.dialog_content) as TextView).text = content else baseView.findViewById<View>(R.id.dialog_content).visibility = View.GONE
        baseView.findViewById<View>(R.id.dialog_sure_btn).setOnClickListener {
            listener?.sure(null)
            dialog.dismiss()
        }
        baseView.findViewById<View>(R.id.dialog_cancel_btn).setOnClickListener {
            listener?.cancel()
            dialog.dismiss()
        }
        val lp = dialog.window.attributes
        lp.width = ScreenUtil.getPxByDp(320) //定义宽度
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT //定义高度
        dialog.window.attributes = lp
        dialog.setContentView(baseView)
        dialog.window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        return dialog
    }

    fun popupWindow(windowView: View?, parent: View?, x: Int, y: Int): PopupWindow {
        val popup = PopupWindow(windowView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        //        int[] position = calculatePopWindowPos(windowView, parent, x, y);
        popup.isOutsideTouchable = true
        popup.isFocusable = true
        popup.setBackgroundDrawable(ColorDrawable(-0x51111200))
        popup.showAtLocation(windowView, Gravity.CENTER or Gravity.TOP, x, y)
        return popup
    }

    private fun calculatePopWindowPos(windowView: View, parentView: View, x: Float, y: Float): IntArray {
        val windowPos = IntArray(2)
        val anchorLoc = IntArray(2)
        anchorLoc[0] = parentView.width
        anchorLoc[1] = parentView.height
        val addHeight = 0 // ScreenUtil.getPxByDp(60) + ScreenUtil.getStatusBarHeight();
        windowView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        // 计算contentView的高宽
        val windowHeight = ScreenUtil.getPxByDp(150)
        val windowWidth = windowView.measuredWidth
        // 判断需要向上弹出还是向下弹出显示
        if (anchorLoc[1] - y < windowHeight) {
            windowPos[1] = y.toInt() + addHeight - windowHeight
        } else {
            windowPos[1] = y.toInt() + addHeight
        }
        if (anchorLoc[0] - x < windowWidth) {
            windowPos[0] = x.toInt() - windowWidth
        } else {
            windowPos[0] = x.toInt()
        }
        return windowPos
    }

    interface EnsureListener {
        fun sure(obj: Any?)
        fun cancel()
    }
}