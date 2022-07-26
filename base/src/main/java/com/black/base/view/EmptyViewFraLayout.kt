package com.black.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.black.base.R
import skin.support.content.res.SkinCompatResources

/**
 * @author zhangxiaohui
 * create at 2018/10/23
 */
class EmptyViewFraLayout @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(mContext, attrs, defStyleAttr) {
    enum class STATUS {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 空数据
         */
        NO_DATA,
        /**
         * 加载中
         */
        LOADING,
        /**
         * 网络错误
         */
        NETWORK_ERROR,
        /**
         * 系统错误
         */
        SYSTEM_ERROR
    }

    private val tipTextColor = SkinCompatResources.getColor(context, R.color.C5)
    private val noDataIcon = R.drawable.icon_list_empty
    private val loadingIcon = R.drawable.icon_list_empty
    private val netErrorIcon = R.drawable.icon_list_empty
    private val systemErrorIcon = R.drawable.icon_list_empty
    private var noDataText = resources.getString(R.string.list_view_empty_default)
    private var loadingText = resources.getString(R.string.loading)
    private var netErrorText = resources.getString(R.string.error_data)
    private var systemErrorText = resources.getString(R.string.error_data)
    private val frameLayout: FrameLayout
    private var successView: View? = null
    private var loadFailView: TextView? = null
    private var viewHeight = 0
    private var viewWithIcon = true
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val childNum = childCount
        if (childNum > 0 && childNum < 2) {
            successView = getChildAt(0)
            frameLayout.removeAllViews()
            frameLayout.addView(successView)
            frameLayout.addView(loadFailView)
        }
        if (viewHeight == 0) {
            loadFailView!!.setPadding(0, height / 3, 0, 0)
            viewHeight = height
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }

    private fun initFailDataView(textColor: Int, text: String, icon: Int) {
        if (loadFailView == null) {
            loadFailView = TextView(mContext)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            loadFailView!!.layoutParams = layoutParams
            loadFailView!!.gravity = Gravity.CENTER
            loadFailView!!.compoundDrawablePadding = 40
        }
        loadFailView!!.setTextColor(textColor)
        loadFailView!!.text = text
        if (viewWithIcon) {
            loadFailView!!.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0)
        } else {
            loadFailView!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }
    /********############################**对外方法**############################# */
    /**
     * 设置子布局(e.g listView)
     *
     * @param view
     */
    fun setChildView(view: View?) { //        successView = view;
//        frameLayout.removeAllViews();
//        frameLayout.addView(successView);
//        frameLayout.addView(loadFailView);
    }

    /**
     * 显示结果
     *
     * @param status
     */
    fun setViewStatus(status: STATUS?) { //        removeAllViews();
        if (successView != null) {
            successView!!.visibility = View.GONE
        }
        if (loadFailView != null) {
            loadFailView!!.visibility = View.GONE
        }
        when (status) {
            STATUS.SUCCESS -> if (successView != null) { //                    addView(successView);
                successView!!.visibility = View.VISIBLE
            }
            STATUS.NO_DATA -> {
                //                addView(initNoDataView());
                initFailDataView(tipTextColor, noDataText, noDataIcon)
                loadFailView!!.visibility = View.VISIBLE
            }
            STATUS.LOADING -> {
                //                addView(loadingView());
                initFailDataView(tipTextColor, loadingText, loadingIcon)
                loadFailView!!.visibility = View.VISIBLE
            }
            STATUS.NETWORK_ERROR -> {
                //                addView(netErrorView());
                initFailDataView(tipTextColor, netErrorText, netErrorIcon)
                loadFailView!!.visibility = View.VISIBLE
            }
            STATUS.SYSTEM_ERROR -> {
                //                addView(systemErrorView());
                initFailDataView(tipTextColor, systemErrorText, systemErrorIcon)
                loadFailView!!.visibility = View.VISIBLE
            }
            else -> {
            }
        }
    }

    /**
     * 提示是否带图标
     *
     * @param viewWithIcon
     */
    fun setViewWithIcon(viewWithIcon: Boolean) {
        this.viewWithIcon = viewWithIcon
    }

    /**
     * 无数据时文字
     *
     * @param noDataText
     */
    fun setNoDataText(noDataText: String) {
        this.noDataText = noDataText
    }

    /**
     * 加载中文字
     *
     * @param loadingText
     */
    fun setLoadingText(loadingText: String) {
        this.loadingText = loadingText
    }

    /**
     * 网络错误文字
     *
     * @param netErrorText
     */
    fun setNetErrorText(netErrorText: String) {
        this.netErrorText = netErrorText
    }

    /**
     * 系统错误文字
     *
     * @param systemErrorText
     */
    fun setSystemErrorText(systemErrorText: String) {
        this.systemErrorText = systemErrorText
    }

    init {
        val rootView = LayoutInflater.from(mContext).inflate(R.layout.empty_view_fralayout, this, false)
        frameLayout = rootView.findViewById(R.id.emptyViewFraLayout)
        addView(rootView)
        initFailDataView(tipTextColor, noDataText, noDataIcon)
        frameLayout.addView(loadFailView)
        loadFailView!!.visibility = View.GONE
    }
}