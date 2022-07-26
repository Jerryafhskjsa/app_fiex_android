package com.black.base.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.R
import com.black.base.adapter.MenuChooseAdapter
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.model.MenuEntity
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class MenuChoosePopup(private val activity: Activity, menuEntities: MutableList<MenuEntity?>?, menuEntity: MenuEntity?) : PopupWindow.OnDismissListener, OnItemClickListener {
    private val popupWindow: PopupWindow?
    private val dm: DisplayMetrics
    private val margin: Float
    private val recyclerView: RecyclerView
    private val adapter: MenuChooseAdapter
    private var onMenuChooseListener: OnMenuChooseListener? = null
    private val contentView: View

    init {
        val inflater = LayoutInflater.from(activity)
        contentView = inflater.inflate(R.layout.view_choose_menu, null)
        dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        val drawable = ColorDrawable(SkinCompatResources.getColor(activity, R.color.transparent))
        popupWindow.setBackgroundDrawable(drawable)
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        recyclerView = contentView.findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager
        adapter = MenuChooseAdapter(activity, menuEntities, menuEntity)
        adapter.onItemClickListener = this
        recyclerView.adapter = adapter
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    private val isShowing: Boolean
        private get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View) {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        CommonUtil.measureView(contentView)
        val offsetX = view.width - contentView.measuredWidth
        popupWindow!!.showAsDropDown(view, offsetX, 0, Gravity.LEFT)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        if (onMenuChooseListener != null) {
            onMenuChooseListener!!.onMenuChoose(adapter.getItem(position))
        }
        dismiss()
    }

    fun setOnMenuChooseListener(onMenuChooseListener: OnMenuChooseListener?): MenuChoosePopup {
        this.onMenuChooseListener = onMenuChooseListener
        return this
    }

    interface OnMenuChooseListener {
        fun onMenuChoose(menuEntity: MenuEntity?)
    }
}
