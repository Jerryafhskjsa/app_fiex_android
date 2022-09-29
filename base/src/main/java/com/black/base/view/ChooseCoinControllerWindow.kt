package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.media.Image
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.black.base.R
import com.black.base.model.CanTransferCoin
import skin.support.content.res.SkinCompatResources
import java.util.*

//划转钱包类型选择弹窗
class ChooseCoinControllerWindow<T>(private val activity: Activity, title: String?, data: List<T>?, private val onReturnListener: OnReturnListener<T>?) : View.OnClickListener, AdapterView.OnItemClickListener {
    private val COLOR_DEFAULT: Int = SkinCompatResources.getColor(activity, R.color.T1)
    private val COLOR_SELECT: Int = SkinCompatResources.getColor(activity, R.color.C1)
    private val COLOR_BG: Int = SkinCompatResources.getColor(activity, R.color.B2)
    private var density: Float
    private val popupWindow: PopupWindow
    private val titleView: TextView
    private val listView: ListView
    private val adapter: ChooseCoinListAdapter

    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = LayoutInflater.from(activity).inflate(R.layout.view_coin_chooser, null)
        popupWindow = PopupWindow(contentView,
                dm.widthPixels,
                WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
//        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener {
            val lp = activity.window.attributes
            lp.alpha = 1f
            activity.window.attributes = lp
        }
        titleView = contentView.findViewById(R.id.title)
        if (TextUtils.isEmpty(title)) {
            titleView.visibility = View.GONE
        } else {
            titleView.visibility = View.VISIBLE
            titleView.text = title
        }
        listView = contentView.findViewById(R.id.list_view)
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1_ALPHA60)
        listView.divider = drawable
        listView.dividerHeight = 0
        val backgroundDrawable = ColorDrawable()
        backgroundDrawable.color = SkinCompatResources.getColor(activity, R.color.B2)
        listView.background = backgroundDrawable
        adapter = ChooseCoinListAdapter(activity, data)
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

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_cancel) {
            dismiss()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) { //点击之后回调选择结果
        onReturnListener?.onReturn(this, adapter.getItem(position))
        dismiss()
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
        fun onReturn(window: ChooseCoinControllerWindow<T>, item: T)
    }

    private inner class ChooseCoinListAdapter(protected var context: Context, data: List<T>?) : BaseAdapter() {
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
                view = inflater.inflate(R.layout.list_item_coin_choose, null)
            }
            val item = getItem(position) as CanTransferCoin
            var coinNameText = view?.findViewById<View>(R.id.coin_name) as TextView?
            coinNameText?.text = item.coin
            return view
        }

        init {
            this.data = data ?: ArrayList()
        }
    }
}