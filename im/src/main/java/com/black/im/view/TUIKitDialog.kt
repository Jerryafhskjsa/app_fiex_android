package com.black.im.view

import android.app.Dialog
import android.content.Context
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.black.im.R

class TUIKitDialog(private val mContext: Context) {
    private var dialog: Dialog? = null
    private var mBackgroundLayout: LinearLayout? = null
    private var mMainLayout: LinearLayout? = null
    /***
     * 获取title
     * @return
     */
    var txt_title: TextView? = null
        private set
    /**
     * 获取确定按钮
     *
     * @return
     */
    var btn_neg: Button? = null
        private set
    /**
     * 获取取消按钮
     *
     * @return
     */
    var btn_pos: Button? = null
        private set
    private var mLineImg: ImageView? = null
    private val mDisplay: Display
    /**
     * 是否显示title
     */
    private var showTitle = false
    /***
     * 是否显示确定按钮
     */
    private var showPosBtn = false
    /**
     * 是否显示取消按钮
     */
    private var showNegBtn = false
    /**
     * dialog  宽度
     */
    private var dialogWidth = 0.7f

    init {
        val windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplay = windowManager.defaultDisplay
    }


    fun builder(): TUIKitDialog {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_dialog, null)
        mBackgroundLayout = view.findViewById<View>(R.id.ll_background) as LinearLayout
        mMainLayout = view.findViewById<View>(R.id.ll_alert) as LinearLayout
        mMainLayout!!.setVerticalGravity(View.GONE)
        txt_title = view.findViewById<View>(R.id.tv_title) as TextView
        txt_title!!.visibility = View.GONE
        btn_neg = view.findViewById<View>(R.id.btn_neg) as Button
        btn_neg!!.visibility = View.GONE
        btn_pos = view.findViewById<View>(R.id.btn_pos) as Button
        btn_pos!!.visibility = View.GONE
        mLineImg = view.findViewById<View>(R.id.img_line) as ImageView
        mLineImg!!.visibility = View.GONE
        dialog = Dialog(mContext, R.style.AlertDialogStyle)
        dialog!!.setContentView(view)
        mBackgroundLayout!!.layoutParams = FrameLayout.LayoutParams((mDisplay.width * dialogWidth).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        return this
    }

    fun setTitle(title: String): TUIKitDialog {
        showTitle = true
        txt_title!!.text = title
        return this
    }

    /***
     * 是否点击返回能够取消
     * @param cancel
     * @return
     */
    fun setCancelable(cancel: Boolean): TUIKitDialog {
        dialog!!.setCancelable(cancel)
        return this
    }

    /**
     * 设置是否可以取消
     *
     * @param isCancelOutside
     * @return
     */
    fun setCancelOutside(isCancelOutside: Boolean): TUIKitDialog {
        dialog!!.setCanceledOnTouchOutside(isCancelOutside)
        return this
    }

    /**
     * 设置确定
     *
     * @param text
     * @param listener
     * @return
     */
    fun setPositiveButton(text: String?,
                          listener: View.OnClickListener): TUIKitDialog {
        showPosBtn = true
        btn_pos!!.text = text
        btn_pos!!.setOnClickListener { v ->
            listener.onClick(v)
            dialog!!.dismiss()
        }
        return this
    }

    fun setPositiveButton(listener: View.OnClickListener): TUIKitDialog {
        setPositiveButton("确定", listener)
        return this
    }

    /***
     * 设置取消
     * @param text
     * @param listener
     * @return
     */
    fun setNegativeButton(text: String?,
                          listener: View.OnClickListener): TUIKitDialog {
        showNegBtn = true
        btn_neg!!.text = text
        btn_neg!!.setOnClickListener { v ->
            listener.onClick(v)
            dialog!!.dismiss()
        }
        return this
    }

    fun setNegativeButton(
            listener: View.OnClickListener): TUIKitDialog {
        setNegativeButton("取消", listener)
        return this
    }

    private fun setLayout() {
        if (!showTitle) {
            txt_title!!.visibility = View.GONE
        }
        if (showTitle) {
            txt_title!!.visibility = View.VISIBLE
        }
        if (!showPosBtn && !showNegBtn) {
            btn_pos!!.visibility = View.GONE
            btn_pos!!.setOnClickListener { dialog!!.dismiss() }
        }
        if (showPosBtn && showNegBtn) {
            btn_pos!!.visibility = View.VISIBLE
            btn_neg!!.visibility = View.VISIBLE
            mLineImg!!.visibility = View.VISIBLE
        }
        if (showPosBtn && !showNegBtn) {
            btn_pos!!.visibility = View.VISIBLE
        }
        if (!showPosBtn && showNegBtn) {
            btn_neg!!.visibility = View.VISIBLE
        }
    }

    fun show() {
        setLayout()
        dialog!!.show()
    }

    fun dismiss() {
        dialog!!.dismiss()
    }

    /**
     * 设置dialog  宽度
     *
     * @param dialogWidth
     * @return
     */
    fun setDialogWidth(dialogWidth: Float): TUIKitDialog {
        if (mBackgroundLayout != null) {
            mBackgroundLayout!!.layoutParams = FrameLayout.LayoutParams((mDisplay.width * dialogWidth).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        this.dialogWidth = dialogWidth
        return this
    }

    /***
     * 获取用于添加自定义控件的ll
     * @return
     */
    fun getlLayout_alert_ll(): LinearLayout? {
        return mMainLayout
    }

    /***
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     * @param dpValue
     * @return
     */
    fun dp2px(dpValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(pxValue: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    fun px2sp(pxValue: Float): Int {
        val fontScale = mContext.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    fun sp2px(spValue: Float): Int {
        val fontScale = mContext.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }
}