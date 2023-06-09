package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.black.base.R
import com.black.base.model.CanTransferCoin
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketUtil
import com.black.base.util.StatusBarUtil
import com.black.base.util.UrlConfig
import com.black.base.widget.SpanTextView
import com.black.router.BlackRouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.transfer_window.view.btn_action
import kotlinx.android.synthetic.main.transfer_window.view.btn_cancel
import kotlinx.android.synthetic.main.transfer_window.view.coin_edit
import kotlinx.android.synthetic.main.transfer_window.view.five
import kotlinx.android.synthetic.main.transfer_window.view.four
import kotlinx.android.synthetic.main.transfer_window.view.one
import kotlinx.android.synthetic.main.transfer_window.view.three
import kotlinx.android.synthetic.main.transfer_window.view.two
import skin.support.content.res.SkinCompatResources
import java.util.*

//划转钱包类型选择弹窗
class ChooseCoinControllerWindow<T>(private val activity: Activity, title: String?, data: List<T>?, private val onReturnListener: OnReturnListener<T>?) : View.OnClickListener, AdapterView.OnItemClickListener {
    private val COLOR_DEFAULT: Int = SkinCompatResources.getColor(activity, R.color.T1)
    private val COLOR_SELECT: Int = SkinCompatResources.getColor(activity, R.color.C1)
    private val COLOR_BG: Int = SkinCompatResources.getColor(activity, R.color.B2)
    private var density: Float
    private val popupWindow: PopupWindow
    //private val titleView: TextView
    private val listView: ListView
    private val adapter: ChooseCoinListAdapter
    private val instance:ChooseCoinControllerWindow<T>
    private var view: View? = null

    init {
        val dm = activity.resources.displayMetrics
        density = dm.density
        val inflater = LayoutInflater.from(activity)
        val contentView = inflater.inflate(R.layout.transfer_window, null)
        val height: Int = activity.resources.displayMetrics.heightPixels
        popupWindow = PopupWindow(
            contentView,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        //popupWindow.isClippingEnabled = false
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
        /*titleView = contentView.findViewById(R.id.title)
        if (TextUtils.isEmpty(title)) {
            titleView.visibility = View.GONE
        } else {
            titleView.visibility = View.VISIBLE
            titleView.text = title
        }

         */
        instance = this
        listView = contentView.findViewById(R.id.list_view)
        view = contentView
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.B2)
        listView.divider = drawable
        listView.dividerHeight = 60
        val backgroundDrawable = ColorDrawable()
        backgroundDrawable.color = SkinCompatResources.getColor(activity, R.color.B2)
        listView.background = backgroundDrawable
        adapter = ChooseCoinListAdapter(activity, data)
        listView.adapter = adapter
        listView.onItemClickListener = this
        //重设listView的高度
       // val params = listView.layoutParams
        /*val dataCount = data?.size ?: 0
        if (dataCount >= 5) {
            params.height = (60 * 5 * density).toInt()
        } else {
            params.height = (60 * dataCount * density).toInt()
        }

         */
        //listView.layoutParams = params
        contentView.btn_cancel.setOnClickListener(this)
        contentView?.one?.setOnClickListener(this)
        contentView?.two?.setOnClickListener(this)
        contentView?.three?.setOnClickListener(this)
        contentView?.four?.setOnClickListener(this)
        contentView?.five?.setOnClickListener(this)
        contentView?.btn_action?.setOnClickListener(this)
        contentView.coin_edit.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                onReturnListener?.onSearch(this, v.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
        contentView.coin_edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                onReturnListener?.onSearch(instance, s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        refreshDearPairs()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.btn_cancel -> {
                dismiss()
            }

            R.id.one -> {
                onReturnListener?.onSearch(instance, view?.findViewById<SpanTextView>(R.id.one)?.text.toString())
            }

            R.id.two -> {
                onReturnListener?.onSearch(instance, view?.findViewById<SpanTextView>(R.id.two)?.text.toString())
            }

            R.id.three -> {
                onReturnListener?.onSearch(instance, view?.findViewById<SpanTextView>(R.id.three)?.text.toString())
            }

            R.id.four -> {
                onReturnListener?.onSearch(instance, view?.findViewById<SpanTextView>(R.id.four)?.text.toString())
            }

            R.id.five -> {
                onReturnListener?.onSearch(instance, view?.findViewById<SpanTextView>(R.id.five)?.text.toString())
            }

            R.id.btn_action -> {
                CookieUtil.clearCoinSearchHistory(activity)
                onReturnListener?.onSearch(instance, view?.coin_edit?.text.toString())
                refreshDearPairs()
            }
        }
    }

    private fun refreshDearPairs() {
        var searchHistory = CookieUtil.getCoinSearchHistory(activity)
        searchHistory = searchHistory ?: ArrayList()
        if (searchHistory.size == 0){
            view?.one?.visibility = View.GONE
            view?.two?.visibility = View.GONE
            view?.three?.visibility = View.GONE
            view?.four?.visibility = View.GONE
            view?.five?.visibility = View.GONE
        }
        for (i in searchHistory.indices) {
            if (i == searchHistory.size - 1){
                view?.one?.setText(searchHistory[i])
                view?.one?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 2){
                view?.two?.setText(searchHistory[i])
                view?.two?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 3){
                view?.three?.setText(searchHistory[i])
                view?.three?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 4){
                view?.four?.setText(searchHistory[i])
                view?.four?.visibility = View.VISIBLE
            }
            if (i == searchHistory.size - 5){
                view?.five?.setText(searchHistory[i])
                view?.five?.visibility = View.VISIBLE
            }
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

    fun getAdapter():ChooseCoinListAdapter{
        return adapter
    }

    interface OnReturnListener<T> {
        fun onReturn(window: ChooseCoinControllerWindow<T>, item: T)
        fun onSearch(window: ChooseCoinControllerWindow<T>,searchKey: String?)
    }

    inner class ChooseCoinListAdapter(protected var context: Context, data: List<T>?) : BaseAdapter() {
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
            var coinFullNameText = view?.findViewById<View>(R.id.coin_name_des) as TextView?
            var coinlogoImg = view?.findViewById<View>(R.id.img_coin) as ImageView?
            var coinAmountText = view?.findViewById<View>(R.id.coin_count) as TextView?
            coinNameText?.text = item.coin
            coinFullNameText?.text = item.coinDes
            coinAmountText?.text = item.amount
            if(item?.coinIconUrl != null){
                if (coinlogoImg != null) {
                    Glide.with(context)
                        .load(Uri.parse(UrlConfig.getCoinIconUrl(context,item?.coinIconUrl)))
                        .apply(RequestOptions().error(R.drawable.icon_coin_default))
                        .into(coinlogoImg)
                }
            }
            return view
        }

        init {
            this.data = data ?: ArrayList()
        }
    }
}