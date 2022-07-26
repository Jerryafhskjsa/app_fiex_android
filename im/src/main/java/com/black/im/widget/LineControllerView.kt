package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.black.im.R
import com.black.im.util.ScreenUtil.getPxByDp

/**
 * 设置等页面条状控制或显示信息的控件
 */
class LineControllerView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mName: String? = null
    private var mIsBottom = false
    private var mContent: String? = null
    private var mIsJump = false
    private var mIsSwitch = false
    private var mNameText: TextView? = null
    private var mContentText: TextView? = null
    private var mNavArrowView: ImageView? = null
    private var mSwitchView: Switch? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.line_controller_view, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.LineControllerView, 0, 0)
        try {
            mName = ta.getString(R.styleable.LineControllerView_name)
            mContent = ta.getString(R.styleable.LineControllerView_subject)
            mIsBottom = ta.getBoolean(R.styleable.LineControllerView_isBottom, false)
            mIsJump = ta.getBoolean(R.styleable.LineControllerView_canNav, false)
            mIsSwitch = ta.getBoolean(R.styleable.LineControllerView_isSwitch, false)
            setUpView()
        } finally {
            ta.recycle()
        }
    }

    private fun setUpView() {
        mNameText = findViewById(R.id.name)
        mNameText?.text = mName
        mContentText = findViewById(R.id.content)
        mContentText?.text = mContent
        val bottomLine = findViewById<View>(R.id.bottomLine)
        bottomLine.visibility = if (mIsBottom) View.VISIBLE else View.GONE
        mNavArrowView = findViewById(R.id.rightArrow)
        mNavArrowView?.visibility = if (mIsJump) View.VISIBLE else View.GONE
        val contentLayout = findViewById<RelativeLayout>(R.id.contentText)
        contentLayout.visibility = if (mIsSwitch) View.GONE else View.VISIBLE
        mSwitchView = findViewById(R.id.btnSwitch)
        mSwitchView?.visibility = if (mIsSwitch) View.VISIBLE else View.GONE
    }

    /**
     * 获取内容
     */
    /**
     * 设置文字内容
     *
     * @param content 内容
     */
    var content: String?
        get() = mContentText?.text.toString()
        set(content) {
            mContent = content
            mContentText?.text = content
        }

    fun setSingleLine(singleLine: Boolean) {
        mContentText?.setSingleLine(singleLine)
    }

    /**
     * 设置是否可以跳转
     *
     * @param canNav 是否可以跳转
     */
    fun setCanNav(canNav: Boolean) {
        mIsJump = canNav
        mNavArrowView?.visibility = if (canNav) View.VISIBLE else View.GONE
        if (canNav) {
            val params = mContentText?.layoutParams
            params?.width = getPxByDp(120)
            params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mContentText?.layoutParams = params
            mContentText?.setTextIsSelectable(false)
        } else {
            val params = mContentText?.layoutParams
            params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            params?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mContentText?.layoutParams = params
            mContentText?.setTextIsSelectable(true)
        }
    }

    /**
     * 设置开关状态
     *
     * @param on 开关
     */
    var isChecked: Boolean
        get() = mSwitchView?.isChecked ?: false
        set(on) {
            mSwitchView?.isChecked = on
        }

    /**
     * 设置开关监听
     *
     * @param listener 监听
     */
    fun setCheckListener(listener: CompoundButton.OnCheckedChangeListener?) {
        mSwitchView?.setOnCheckedChangeListener(listener)
    }
}