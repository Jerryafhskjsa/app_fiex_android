package com.black.c2c.view

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CSeller
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.c2c.R
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.BigDecimal
import java.math.RoundingMode

class C2CCreateWidget(private val context: Context, private val type: String, private val c2CSeller: C2CMainAD?, supportCoin: C2CSupportCoin?) : CompoundButton.OnCheckedChangeListener {
    private val imageLoader: ImageLoader = ImageLoader(context)
    private val precision: Int = supportCoin?.precision ?: 0
    private val minAmount: Double = supportCoin?.minOrderAmount ?: 0.0
    private val alertDialog: Dialog?
    private val contentView: View = LayoutInflater.from(context).inflate(if (C2COrder.ORDER_BUY == type) R.layout.dialog_c2c_buy_create else R.layout.dialog_c2c_sell_create, null)
    private var iconView: ImageView? = null
    private var inputCNYLayout: View? = null
    private var inputAmountLayout: View? = null
    private var editView: EditText? = null

    init {
        alertDialog = Dialog(context, R.style.AlertDialog)
        val window = alertDialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = context.resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        alertDialog.setCancelable(false)
        initViews()
        selectDefault()
    }

    private fun initViews() {
        val isBuy = C2COrder.ORDER_BUY == type
        val coinType = c2CSeller?.coinType
        iconView = contentView.findViewById(R.id.icon)
        FryingUtil.setCoinIcon(context, iconView, imageLoader, coinType)
        val titleView = contentView.findViewById<TextView>(R.id.title)
        titleView.text = String.format("%s%s", if (isBuy) "购买" else "出售", coinType)
        val coinTypeView = contentView.findViewById<TextView>(R.id.coin_type)
        coinTypeView.text = coinType
        val moneyRadio = contentView.findViewById<RadioButton>(R.id.type_money)
        moneyRadio.setOnCheckedChangeListener(this)
        val amountRadio = contentView.findViewById<RadioButton>(R.id.type_amount)
        amountRadio.setOnCheckedChangeListener(this)
        val priceView = contentView.findViewById<TextView>(R.id.price)
        priceView.text = context.getString(R.string.c2c_price, NumberUtil.formatNumberNoGroup(c2CSeller?.currentPrice))
        inputCNYLayout = contentView.findViewById(R.id.cny_layout)
        inputAmountLayout = contentView.findViewById(R.id.amount_layout)
        editView = contentView.findViewById(R.id.edit_text)
        val cnyEditView = contentView.findViewById<EditText>(R.id.cny_edit_text)
        val price: Double? = 0.0
        val amountTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //计算CNY
                if (editView?.isFocused == true) {
                    val amount = CommonUtil.parseDouble(editView?.text.toString())
                    if (amount != null) {
                        cnyEditView.setText(NumberUtil.formatNumberNoGroup(amount * price!!, RoundingMode.FLOOR, 0, precision))
                    } else {
                        cnyEditView.setText("")
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }
        val cnyTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { //计算数量
                if (cnyEditView.isFocused) {
                    val cny = CommonUtil.parseDouble(cnyEditView.text.toString())
                    if (cny != null) {
                        editView?.setText(NumberUtil.formatNumberNoGroup(if (price == 0.0) 0 else cny / price!!, RoundingMode.FLOOR, 0, precision))
                    } else {
                        editView?.setText("")
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }
        editView?.filters = arrayOf(NumberFilter(), PointLengthFilter(precision))
        //        amountTextWatcher.setJoinTextWatchers(cnyTextWatcher);
        editView?.addTextChangedListener(amountTextWatcher)
        editView?.hint = String.format("最小%s数量%s", if (C2COrder.ORDER_BUY == type) "购买" else "出售", NumberUtil.formatNumberNoGroup(minAmount, precision))
        cnyEditView.filters = arrayOf(NumberFilter(), PointLengthFilter(precision))
        //        cnyTextWatcher.setJoinTextWatchers(amountTextWatcher);
        cnyEditView.addTextChangedListener(cnyTextWatcher)
        cnyEditView.hint = String.format("最小%s金额%s", if (C2COrder.ORDER_BUY == type) "购买" else "出售", NumberUtil.formatNumberNoGroup(minAmount * price!!, precision))
        val btnCancel = contentView.findViewById<View>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            if (onC2CHandlerListener != null) {
                onC2CHandlerListener!!.onCancel(this@C2CCreateWidget)
            }
        }
        val btnConfirm = contentView.findViewById<View>(R.id.btn_buy_confirm)
        btnConfirm.setOnClickListener {
            if (onC2CHandlerListener != null) {
                onC2CHandlerListener!!.onConfirm(this@C2CCreateWidget)
            }
        }
        moneyRadio.isChecked = true
    }

    private fun selectDefault() {
        selectType("CNY")
    }

    private fun selectType(inputType: String) {
        if ("CNY" == inputType) {
            inputCNYLayout!!.visibility = View.VISIBLE
            inputAmountLayout!!.visibility = View.GONE
        } else {
            inputCNYLayout!!.visibility = View.GONE
            inputAmountLayout!!.visibility = View.VISIBLE
        }
    }

    val amount: Double?
        get() = CommonUtil.parseDouble(editView!!.text.toString())

    fun show() {
        if (alertDialog != null && !alertDialog.isShowing) {
            alertDialog.show()
        }
    }

    fun dismiss() {
        if (alertDialog != null && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, b: Boolean) {
        if (compoundButton.id == R.id.type_money) {
            selectType(if (b) "CNY" else "AMOUNT")
        } else {
            selectType(if (b) "AMOUNT" else "CNY")
        }
    }

    private var onC2CHandlerListener: OnC2CHandlerListener? = null

    fun setOnC2CHandlerListener(onC2CHandlerListener: OnC2CHandlerListener?) {
        this.onC2CHandlerListener = onC2CHandlerListener
    }

    interface OnC2CHandlerListener {
        fun onCancel(widget: C2CCreateWidget?)
        fun onConfirm(widget: C2CCreateWidget?)
    }
}