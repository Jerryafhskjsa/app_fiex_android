package com.black.wallet.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import com.black.base.R
import com.black.base.model.wallet.SupportAccount
import com.black.base.util.StatusBarUtil
import com.black.base.view.ChooseWalletControllerWindow
import skin.support.content.res.SkinCompatResources
import java.util.ArrayList

@SuppressLint("MissingInflatedId")
class TransferPopupWindow<T>(private val activity: Activity, title: String?, private val selectObject: T, data: List<T>?, private val onReturnListener: ChooseWalletControllerWindow.OnReturnListener<T>?) : View.OnClickListener, AdapterView.OnItemClickListener {
    private val popupWindow: PopupWindow
    private val listView: ListView
    private var density: Float
    private val adapter: ChooseWalletListAdapter

    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        val inflater = LayoutInflater.from(activity)
        val contentView = inflater.inflate(R.layout.view_pair_status_popup_window, null)
        val height: Int = activity.resources.displayMetrics.heightPixels
        popupWindow = PopupWindow(
            contentView,
            WindowManager.LayoutParams.MATCH_PARENT,
            height + StatusBarUtil.getStatusBarHeight(activity) + StatusBarUtil.getStatusBarHeight(
                activity
            ) + StatusBarUtil.getStatusBarHeight(activity)
        )
        popupWindow.isClippingEnabled = false
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_pairs_status_popup_window
        popupWindow.setOnDismissListener {
            val lp = activity.window.attributes
            lp.alpha = 1f
            activity.window.attributes = lp
        }
        StatusBarUtil.fitPopupWindowOverStatusBar(popupWindow, true)
        listView = contentView.findViewById(com.black.wallet.R.id.list_view)
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1_ALPHA60)
        listView.divider = drawable
        listView.dividerHeight = 0
        val backgroundDrawable = ColorDrawable()
        backgroundDrawable.color = SkinCompatResources.getColor(activity, R.color.B2)
        listView.background = backgroundDrawable
        adapter = ChooseWalletListAdapter(activity, data)
        listView.adapter = adapter
        listView.onItemClickListener = this
        //重设listView的高度
        val params = listView.layoutParams
        val dataCount = data?.size ?: 0
        if (dataCount >= 5) {
            params.height = (60 * 5 * density).toInt()
        } else {
            params.height = (60 * dataCount * density).toInt()
        }
        listView.layoutParams = params
        contentView.findViewById<View>(R.id.btn_cancel).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) { //点击之后回调选择结果
    }

    fun show() {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        popupWindow.showAtLocation(activity.window.decorView, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    interface OnReturnListener<T> {
        fun onReturn(window: ChooseWalletControllerWindow<T>, item: T)
    }

    private inner class ChooseWalletListAdapter(protected var context: Context, data: List<T>?) : BaseAdapter() {
        protected var inflater: LayoutInflater = LayoutInflater.from(context)
        private var data: List<T>

        fun setData(data: List<T>?) {
            this.data = data ?: ArrayList()
        }

        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): T {
            return data[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view = convertView
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_wallet_choose, null)
            }
            val item = getItem(position)
            var textView = view?.findViewById<View>(R.id.text) as TextView?
            val select = view?.findViewById<View>(R.id.img_select) as ImageView?
            if (item == selectObject) {
                select?.visibility = View.VISIBLE
            } else {
                select?.visibility = View.GONE
            }
            if(item is SupportAccount){
                textView?.text = item.name
            }
            if(item is String){
                textView?.text = item
            }
            return view
        }

        init {
            this.data = data ?: ArrayList()
        }
    }
}