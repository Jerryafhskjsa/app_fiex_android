package com.black.base.lib.filter

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.black.base.R
import java.util.*

//筛选适配器
class FilterWindow(private var activity: Activity, data: List<FilterEntity<*>>) : View.OnClickListener, PopupWindow.OnDismissListener {
    companion object {
        private const val TAG = "FilterWindow"
    }

    private val popupWindow: PopupWindow?
    private val filterRowList: MutableList<FilterRow<*>>
    private val btnCommit: View
    private var onFilterSelectListener: OnFilterSelectListener? = null

    init {
        val dm = activity.resources.displayMetrics
        val contentView = LayoutInflater.from(activity).inflate(R.layout.view_filter_window, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_bottom_in_out
        popupWindow.setOnDismissListener(this)
        val workSpaceLayout = contentView.findViewById<LinearLayout>(R.id.work_space)
        filterRowList = ArrayList(data.size)
        for (i in data.indices) {
            val rowData = data[i]
            val filterRow: FilterRow<*> = FilterRow(activity, rowData)
            filterRowList.add(filterRow)
            workSpaceLayout.addView(filterRow.contentView)
        }
        btnCommit = contentView.findViewById(R.id.btn_commit)
        btnCommit.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_commit) {
            if (onFilterSelectListener != null) {
                onFilterSelectListener?.onFilterSelect(this, selectResult)
            }
            dismiss()
        }
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    fun show(onFilterSelectListener: OnFilterSelectListener?) {
        this.onFilterSelectListener = onFilterSelectListener
        popupWindow?.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
    }

    fun dismiss() {
        if (isShowing) {
            popupWindow?.dismiss()
        }
    }

    private val selectResult: List<FilterResult<*>>
        get() {
            val result: MutableList<FilterResult<*>> = ArrayList()
            for (i in filterRowList.indices) {
                result.add(filterRowList[i].getSelectItem())
            }
            return result
        }

    fun setOnFilterSelectListener(onFilterSelectListener: OnFilterSelectListener?) {
        this.onFilterSelectListener = onFilterSelectListener
    }

    interface OnFilterSelectListener {
        fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>)
    }

}