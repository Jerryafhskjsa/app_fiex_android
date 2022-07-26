package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.view.*
import android.widget.*
import com.black.base.R
import skin.support.content.res.SkinCompatResources
import java.util.*

//深度选择器
class ListDialogWindow<T>(private val activity: Activity, title: String?, selectObject: String, data: List<T>, private val onReturnListener: OnReturnListener<T>?) : View.OnClickListener, AdapterView.OnItemClickListener {
    private val COLOR_DEFAULT: Int
    private val COLOR_SELECT: Int
    private val COLOR_BG: Int
    private val COLOR_UNENABLE: Int
    var density: Float
    private val popupWindow: PopupWindow
    private val titleView: TextView
    private val listView: ListView
    private val adapter: DeepListAdapter<T>
    private val selectObject: String

    init {
        this.selectObject = selectObject
        COLOR_DEFAULT = SkinCompatResources.getColor(activity, R.color.C5)
        COLOR_SELECT = SkinCompatResources.getColor(activity, R.color.C2)
        COLOR_BG = SkinCompatResources.getColor(activity, R.color.B2)
        COLOR_UNENABLE = SkinCompatResources.getColor(activity, R.color.C8)
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = LayoutInflater.from(activity).inflate(R.layout.view_deep_chooser, null)
        popupWindow = PopupWindow(contentView,
                dm.widthPixels,
                WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = PopupWindow.INPUT_METHOD_NEEDED
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener {
            val lp = activity.window.attributes
            lp.alpha = 1f
            activity.window.attributes = lp
        }
        titleView = contentView.findViewById(R.id.title)
        titleView.text = title ?: ""
        listView = contentView.findViewById(R.id.list_view)
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.C6)
        listView.divider = drawable
        listView.dividerHeight = 1
        val backgroundDrawable = ColorDrawable()
        backgroundDrawable.color = SkinCompatResources.getColor(activity, R.color.B2)
        listView.background = backgroundDrawable
        adapter = DeepListAdapter(activity, data)
        listView.adapter = adapter
        listView.onItemClickListener = this
        //重设listView的高度
        val params = listView.layoutParams
        val dataCount = data.size
        if (dataCount >= 5) {
            params.height = (40 * 5 * density).toInt()
        } else {
            params.height = (40 * dataCount * density).toInt()
        }
        listView.layoutParams = params
    }

    override fun onClick(v: View) {
        val i = v.id
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        //点击之后回调选择结果
        if (onReturnListener != null) {
            val item: T = adapter.getItem(position)!!
            if (item is ListDialogModel) {
                if (!item.enableClick()) {
                    return
                }
            }
            onReturnListener.onReturn(this, item)
        }
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
        fun onReturn(window: ListDialogWindow<T>?, item: T)
    }

    private inner class DeepListAdapter<T>(protected var context: Context, data: List<T>?) : BaseAdapter() {
        protected var inflater: LayoutInflater
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
                view = inflater.inflate(R.layout.list_item_deep_choose, null)
            }
            val item = getItem(position)
            val textView = view?.findViewById<View>(R.id.text) as TextView?
            if (item is ListDialogModel) {
                val enableClick = (item as ListDialogModel).enableClick()
                textView?.setTextColor(if (!enableClick) COLOR_UNENABLE else if ((item as ListDialogModel).getShowText() == selectObject) COLOR_SELECT else COLOR_DEFAULT)
            } else {
                textView?.setTextColor(if (item == selectObject) COLOR_SELECT else COLOR_DEFAULT)
            }
            if (item is ListDialogModel) {
                textView?.text = (item as ListDialogModel).getShowText()
            } else {
                textView?.text = item.toString()
            }
            view?.setBackgroundColor(COLOR_BG)
            return view
        }

        init {
            inflater = LayoutInflater.from(context)
            this.data = data ?: ArrayList()
        }
    }
}