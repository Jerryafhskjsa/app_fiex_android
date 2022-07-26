package com.black.base.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.black.base.R
import com.black.base.adapter.BaseDataTypeAdapter
import com.black.base.api.CommonApiServiceHelper.getCountryCodeList
import com.black.base.model.CountryCode
import com.black.base.model.HttpRequestResultDataList
import com.black.util.Callback
import skin.support.content.res.SkinCompatResources

class CountryChooseWindow(private val activity: Activity, private val type: Int, private var defaultCode: CountryCode?, private var onCountryChooseListener: OnCountryChooseListener?) : AdapterView.OnItemClickListener, PopupWindow.OnDismissListener, View.OnClickListener {
    companion object {
        private const val TAG = "CountryChooseWindow"
        const val TYPE_01 = 1
        const val TYPE_02 = 2
    }

    private val popupWindow: PopupWindow?
    private var inputEdit: EditText? = null
    private var btnCancel: View? = null
    private var listView: ListView? = null
    private val dataSource: MutableList<CountryCode?> = ArrayList()
    private var adapter: BaseDataTypeAdapter<CountryCode?>? = null

    constructor(activity: Activity, defaultCode: CountryCode?, onCountryChooseListener: OnCountryChooseListener?) : this(activity, TYPE_01, defaultCode, onCountryChooseListener)

    init {
        val dm = activity.resources.displayMetrics
        val width = (dm.widthPixels * 0.8).toInt()
        val inflater = LayoutInflater.from(activity)
        val contentView = inflater.inflate(R.layout.view_country_choose, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, (dm.heightPixels * 0.5).toInt())
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_bottom_in_out
        popupWindow.setOnDismissListener(this)
        initViews(contentView)
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    private fun initViews(contentView: View) {
        inputEdit = contentView.findViewById(R.id.input)
        inputEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                search(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })
        listView = contentView.findViewById(R.id.list_view)
        adapter = if (type == TYPE_02) CountryChooseAdapter2(activity, dataSource) else CountryChooseAdapter(activity, dataSource)
        listView?.adapter = adapter
        listView?.onItemClickListener = this
        btnCancel = contentView.findViewById(R.id.btn_cancel)
        btnCancel?.setOnClickListener(this)
    }

    val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    fun show(defaultCode: CountryCode?) {
        show(defaultCode, onCountryChooseListener)
    }

    fun show(defaultCode: CountryCode?, onCountryChooseListener: OnCountryChooseListener?) {
        this.onCountryChooseListener = onCountryChooseListener
        this.defaultCode = defaultCode
        popupWindow?.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        onResume()
    }

    fun dismiss() {
        if (isShowing) {
            popupWindow?.dismiss()
        }
    }

    fun onResume() {
        initCountryList()
        inputEdit?.setText("")
        adapter?.data = dataSource
        adapter?.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        if (onCountryChooseListener != null) {
            onCountryChooseListener?.onCountryChoose(this, adapter?.getItem(position))
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_cancel) {
            dismiss()
        }
    }

    fun setCountryList(data: List<CountryCode?>?) {
        dataSource.clear()
        dataSource.addAll(data ?: ArrayList())
    }

    private fun initCountryList() {
        if (dataSource.isNotEmpty()) {
            return
        }
        getCountryCodeList(activity, true, object : Callback<HttpRequestResultDataList<CountryCode?>?>() {
            override fun error(type: Int, error: Any) {
                dataSource.clear()
                inputEdit?.setText("")
                adapter?.data = null
                adapter?.notifyDataSetChanged()
            }

            override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                dataSource.clear()
                inputEdit?.setText("")
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    dataSource.addAll(returnData.data!!)
                    adapter?.data = dataSource
                    adapter?.notifyDataSetChanged()
                } else {
                    adapter?.data = null
                    adapter?.notifyDataSetChanged()
                }
            }
        })
    }

    private fun search(key: String) {
        val result: MutableList<CountryCode?> = ArrayList()
        if (TextUtils.isEmpty(key)) {
            result.addAll(dataSource)
        } else {
            for (countryCode in dataSource) {
                if (countryCode?.zh != null && true == countryCode.zh?.contains(key) || countryCode?.en != null && true == countryCode.en?.contains(key)) {
                    result.add(countryCode)
                }
            }
        }
        adapter?.data = result
        adapter?.notifyDataSetChanged()
    }

    internal inner class CountryChooseAdapter(context: Context, data: MutableList<CountryCode?>?) : BaseDataTypeAdapter<CountryCode?>(context, data) {
        private val bgDefault = Color.TRANSPARENT
        private val bgSelect = Color.parseColor("#F9F9F9")
        private var textDefault = Color.parseColor("#B1BACA")
        private var textSelect = Color.parseColor("#406CE9")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view = convertView
            val countryCode = getItem(position)
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_choose_country_code_popup, null)
            }
            var bgColor = bgDefault
            var textColor = textDefault
            if (defaultCode != null && true == defaultCode?.equals(countryCode)) {
                bgColor = bgSelect
                textColor = textSelect
            }
            val codeView = view?.findViewById<TextView>(R.id.code)
            codeView?.text = if (countryCode?.code == null) "NULL" else countryCode.code
            codeView?.setTextColor(textColor)
            val nameView = view?.findViewById<TextView>(R.id.display)
            nameView?.text = countryCode?.display
            nameView?.setTextColor(textColor)
            view?.setBackgroundColor(bgColor)
            return view
        }

        init {
            textDefault = SkinCompatResources.getColor(context, R.color.T2)
            textSelect = SkinCompatResources.getColor(context, R.color.C1)
        }
    }

    internal inner class CountryChooseAdapter2(context: Context, data: MutableList<CountryCode?>?) : BaseDataTypeAdapter<CountryCode?>(context, data) {
        private val bgDefault = Color.TRANSPARENT
        private val bgSelect = Color.parseColor("#F9F9F9")
        private var textDefault = Color.parseColor("#B1BACA")
        private var textSelect = Color.parseColor("#406CE9")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view = convertView
            val countryCode = getItem(position)
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_choose_country_code_popup2, null)
            }
            var bgColor = bgDefault
            var textColor = textDefault
            val checkIconView = view?.findViewById<ImageView>(R.id.icon_check)
            if (defaultCode != null && true == defaultCode?.equals(countryCode)) {
                bgColor = bgSelect
                textColor = textSelect
                checkIconView?.visibility = View.VISIBLE
            } else {
                checkIconView?.visibility = View.GONE
            }
            val nameView = view?.findViewById<TextView>(R.id.display)
            nameView?.text = countryCode?.display
            nameView?.setTextColor(textColor)
            view?.setBackgroundColor(bgColor)
            return view
        }

        init {
            textDefault = SkinCompatResources.getColor(context, R.color.T2)
            textSelect = SkinCompatResources.getColor(context, R.color.C1)
        }
    }

    interface OnCountryChooseListener {
        fun onCountryChoose(chooseWindow: CountryChooseWindow, countryCode: CountryCode?)
    }
}
