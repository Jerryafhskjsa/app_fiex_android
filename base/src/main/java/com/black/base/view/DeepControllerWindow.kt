package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.black.base.R
import com.black.base.model.FryingLinesConfig
import com.black.base.model.socket.Deep
import skin.support.content.res.SkinCompatResources
import java.util.*

//深度选择器
class DeepControllerWindow<T>(private val activity: Activity, title: String?, private val selectObject: T?, data: List<T>?, private val onReturnListener: OnReturnListener<T>?) : View.OnClickListener, AdapterView.OnItemClickListener {
    private val COLOR_DEFAULT: Int = SkinCompatResources.getColor(activity, R.color.T1)
    private val COLOR_SELECT: Int = SkinCompatResources.getColor(activity, R.color.T13)
    private val COLOR_BG: Int = SkinCompatResources.getColor(activity, R.color.B2)
    private var density: Float
    private val popupWindow: PopupWindow
    private val titleView: TextView
    private val listView: ListView
    private val adapter: DeepListAdapter

    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = LayoutInflater.from(activity).inflate(R.layout.view_deep_chooser, null)
        popupWindow = PopupWindow(contentView,
                dm.widthPixels,
                WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
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
        adapter = DeepListAdapter(activity, data)
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
        fun onReturn(window: DeepControllerWindow<T>, item: T)
    }

    private inner class DeepListAdapter(protected var context: Context, data: List<T>?) : BaseAdapter() {
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
                view = inflater.inflate(R.layout.list_item_deep_choose, null)
            }
            val item = getItem(position)
            val textView = view?.findViewById<View>(R.id.text) as TextView?
            var text:String? = null
            if(item is String){
                if (item == selectObject) {
                    textView?.setTextColor(COLOR_SELECT)
                } else {
                    textView?.setTextColor(COLOR_DEFAULT)
                }
                text = if(item == "LIMIT"){
                    context.getString(R.string.order_type_limit)
                }else if(item == "MARKET"){
                    context.getString(R.string.order_type_market)
                }else{
                    item.toString()
                }

            }
            if(item is Deep){
                if (item == selectObject) {
                    textView?.setTextColor(COLOR_SELECT)
                } else {
                    textView?.setTextColor(COLOR_DEFAULT)
                }
                text = item.deep.toString()
            }
            if(item is FryingLinesConfig){
                var selected  = selectObject as FryingLinesConfig
                if (item.lineUrl.equals(selected.lineUrl)){
                    textView?.setTextColor(COLOR_SELECT)
                } else {
                    textView?.setTextColor(COLOR_DEFAULT)
                }
                text = item?.lineUrl+"("+item?.speed+"ms)"
            }
            textView?.text = text
            view?.setBackgroundColor(COLOR_BG)
            return view
        }

        init {
            this.data = data ?: ArrayList()
        }
    }
}