package com.black.base.lib.refreshlayout.defaultview

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import com.black.lib.refresh.LoadView
import com.black.lib.refresh.RefreshHolder
import com.black.lib.refresh.RefreshView

class RefreshHolderDefault(context: Context?) : RefreshHolder(context!!) {
    private var refreshView: RefreshView? = null
    private var loadView: LoadView? = null
    override fun getRefreshView(): RefreshView {
        return if (refreshView != null) {
            refreshView!!
        } else {
            DefaultRefreshView(context).also { refreshView = it }
        }
    }

    override fun getLoadView(): LoadView {
        return if (loadView != null) {
            loadView!!
        } else {
            DefaultLoadView(context).also { loadView = it }
        }
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        if (refreshView is DefaultRefreshView) {
            refreshView?.setBackgroundColor(color)
        }
    }

    override fun setSecondFloorView(secondFloorView: View?) {
        if (refreshView is DefaultRefreshView) {
            secondFloorView?.let {
                (refreshView as DefaultRefreshView).setSecondFloorView(secondFloorView)
            }
        } else {
            Log.d("QRefreshLayout", "no DefaultRefreshView, please set secondFloorView by yourself")
        }
    }

    override fun setColorSchemeColors(colors: IntArray) {
        if (refreshView is DefaultRefreshView) {
            (refreshView as DefaultRefreshView).setColorSchemeColors(*colors)
        }
    }
}