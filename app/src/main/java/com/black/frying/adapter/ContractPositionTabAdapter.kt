package com.black.frying.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Editable
import android.text.Layout
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.ContractMultiChooseBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PositionBean
import com.black.base.util.CookieUtil
import com.black.base.util.FryingHelper
import com.black.base.util.FryingUtil
import com.black.base.util.UrlConfig
import com.black.base.view.ContractMultipleSelectWindow
import com.black.base.widget.SpanCheckBox
import com.black.base.widget.SpanMaterialEditText
import com.black.base.widget.SpanTextView
import com.black.frying.service.FutureService
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabPositionBinding
import com.google.zxing.WriterException
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.util.Calendar
import java.util.Objects

class ContractPositionTabAdapter(context: Context, data: MutableList<PositionBean?>) :
    BaseDataTypeBindAdapter<PositionBean?, ListItemContractTabPositionBinding>(context, data) {
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null
    private var color: Int? = null
    private var positionData: PositionBean? = null
    private var gray: Int? = null
    private var url : String = UrlConfig.getHost(context) + "/auth/register/" + CookieUtil.getUserInfo(context)?.inviteCode
    protected var mContext: BaseActionBarActivity? = null
    private var colorGray: Int? = null
    private var amount: Double? = null
    private var amount2: Double? = null
    private var buyMultiChooseBean: ContractMultiChooseBean? = null
    private var sideDes: String? = null
    private var sideDes2: String? = null
    private var sideBgColor: Int? = null
    private var color1: Int? = null
    private var color2: Int? = null
    private var sideBgColor2: Int? = null
    private var sideBlackColor: Int? = null
    private var bondDes: String? = null
    private var positionType: String? = null
    private var amonut: Double = 0.0
    private var click: Boolean = false
    private var click2: Boolean = false

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, R.color.T3)
        bgWin = SkinCompatResources.getColor(context, R.color.T10)
        bgLose = SkinCompatResources.getColor(context, R.color.T9)
        color = SkinCompatResources.getColor(context, R.color.T13)
        gray = SkinCompatResources.getColor(context, R.color.black)
        colorGray = SkinCompatResources.getColor(context, R.color.gray)
        color1 = SkinCompatResources.getColor(context, R.color.T13)
        color2 = SkinCompatResources.getColor(context, R.color.light_gray)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.list_item_contract_tab_position
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabPositionBinding>?) {
        positionData = getItem(position)

        Log.d("ttt----->item", positionData.toString())
        val unit = positionData?.symbol!!.split("_")[1].toString().uppercase()

        val viewHolder = holder?.dataBing
        val autoMergeBond: Boolean? = positionData?.autoMargin
        val num = FutureService.getContractSize(positionData?.symbol)?: BigDecimal(0.0001)
        buyMultiChooseBean = ContractMultiChooseBean()
        buyMultiChooseBean?.maxMultiple = 100
        buyMultiChooseBean?.defaultMultiple = positionData?.leverage
        buyMultiChooseBean?.symbol = positionData?.symbol
        amonut = positionData?.price?.toDouble()?:0.0
        viewHolder?.btnClosePosition?.isChecked = positionData?.availableCloseSize == "0"
       // viewHolder?.btnClosePosition?.isClickable = !(positionData?.availableCloseSize == "0" || positionData?.closeOrderSize == "0")
        Log.d("jhkjhkjh",positionData?.positionSize)
        when (positionData?.positionSide) {
            //做多
            "LONG" -> {
                sideDes = getString(R.string.contract_see_up)
                sideDes2 = "开空"
                sideBgColor = context.getColor(R.color.T17)
                sideBlackColor = context.getColor(R.color.T22)
                sideBgColor2 = context.getColor(R.color.T16)
                buyMultiChooseBean?.orientation = "BUY"
            }
            //做空
            "SHORT" -> {
                sideDes = getString(R.string.contract_see_down)
                sideDes2 = "开多"
                sideBgColor = context.getColor(R.color.T16)
                sideBlackColor = context.getColor(R.color.T21)
                sideBgColor2 = context.getColor(R.color.T17)
                buyMultiChooseBean?.orientation = "SELL"
            }
        }
        when (positionData?.positionType) {
            //逐仓
            "ISOLATED" -> {
                bondDes = positionData?.isolatedMargin
                buyMultiChooseBean?.type = 0
                positionType = getString(R.string.contract_fiexble_position)
            }
            //全仓
            "CROSSED" -> {
                bondDes = positionData?.isolatedMargin
                buyMultiChooseBean?.type = 1
                positionType = getString(R.string.contract_all_position)
            }
        }

        viewHolder?.weishixian?.text =
            String.format(context.getString(R.string.contract_profits), unit)

        viewHolder?.contractHoldAmount?.text =
            String.format(context.getString(R.string.contract_hold_amount), unit)

        viewHolder?.contractHoldAvgPrice?.text =
            String.format(context.getString(R.string.contract_hold_avg_price), unit)

        viewHolder?.contractPositionItemMarkprice?.text =
            String.format(context.getString(R.string.contract_hold_tag_price), unit)

        viewHolder?.contractCanFinishAmount?.text =
            String.format(context.getString(R.string.contract_can_finish_amount), unit)

        viewHolder?.contractForceFinishPirce?.text =
            String.format(context.getString(R.string.contract_force_finish_pirce), unit)

        viewHolder?.contractBond?.text =
            String.format(context.getString(R.string.contract_bond), unit)

        viewHolder?.contractAlreadyProfits?.text =
            String.format(context.getString(R.string.contract_already_profits), unit)

        //仓位描述
        viewHolder?.positionDes?.text = positionData?.symbol.toString().uppercase()
        viewHolder?.cangwei?.text = positionType
        viewHolder?.beishu?.text = positionData?.leverage.toString() + "X"
        //方向
        viewHolder?.positionSide?.text = sideDes
        viewHolder?.positionSide?.setTextColor(sideBgColor!!)
        viewHolder?.positionSide?.setBackgroundColor(sideBlackColor!!)
        //已实现盈亏
        viewHolder?.alreadyCloseProfit?.text = positionData?.realizedProfit
        //当前盈亏
        viewHolder?.profits?.text = positionData?.unRealizedProfit
        //当前盈亏百分比
        viewHolder?.profitsPercent?.text = positionData?.profitRate
        //持仓均价
        viewHolder?.entryPrice?.text = positionData?.entryPrice
        //强平价格>0
        //持仓数量
        viewHolder?.positionAmount?.text = positionData?.positionValue
        //可平数量
        viewHolder?.availableCloseAmount?.text = positionData?.price
        //仓位保证金
        //viewHolder?.bondAmount?.text = positionData.isolatedMargin

        if(positionData?.forceStopPrice != null){
            if (BigDecimal(positionData?.forceStopPrice).compareTo(BigDecimal.ZERO) == 1) {
                viewHolder?.forceClosePrice?.text = positionData?.forceStopPrice
            } else {
                viewHolder?.forceClosePrice?.text = "--"
            }
        }
        //标记价格
        viewHolder?.flagPrice?.text = positionData?.flagPrice
        //保证金
        viewHolder?.bondAmount?.text = bondDes
        //是否自动追加保证金开关
        viewHolder?.autoAddBond?.isChecked = autoMergeBond!!
        viewHolder?.autoAddBond?.setOnCheckedChangeListener { _, isChecked ->
            FutureApiServiceHelper.autoMargin(
                context,
                positionData?.symbol,
                positionData?.positionSide,
                isChecked,
                true,
                object : Callback<HttpRequestResultBean<String>?>() {
                    override fun callback(returnData: HttpRequestResultBean<String>?) {
                        if (returnData != null) {
                            Log.d("iiiiii-->autoMargin", returnData.result.toString())
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d("iiiiii-->autoMargin--error", error.toString())
                    }
                })
        }
        viewHolder?.fenxiang?.setOnClickListener{ v ->
            dialog()
        }
        when (positionData?.adl) {
            0 -> {
                viewHolder?.one!!.setBackgroundColor(color2!!)
                viewHolder.two.setBackgroundColor(color2!!)
                viewHolder.three.setBackgroundColor(color2!!)
                viewHolder.four.setBackgroundColor(color2!!)
                viewHolder.five.setBackgroundColor(color2!!)
            }
            1 -> {
                viewHolder?.one!!.setBackgroundColor(bgLose!!)
                viewHolder.two.setBackgroundColor(color2!!)
                viewHolder.three.setBackgroundColor(color2!!)
                viewHolder.four.setBackgroundColor(color2!!)
                viewHolder.five.setBackgroundColor(color2!!)
            }
            2 -> {
                viewHolder?.one!!.setBackgroundColor(bgLose!!)
                viewHolder.two.setBackgroundColor(bgLose!!)
                viewHolder.three.setBackgroundColor(color2!!)
                viewHolder.four.setBackgroundColor(color2!!)
                viewHolder.five.setBackgroundColor(color2!!)
            }
            3 -> {
                viewHolder?.one!!.setBackgroundColor(bgLose!!)
                viewHolder.two.setBackgroundColor(bgLose!!)
                viewHolder.three.setBackgroundColor(bgLose!!)
                viewHolder.four.setBackgroundColor(color2!!)
                viewHolder.five.setBackgroundColor(color2!!)
            }
            4 -> {
                viewHolder?.one!!.setBackgroundColor(bgLose!!)
                viewHolder.two.setBackgroundColor(bgLose!!)
                viewHolder.three.setBackgroundColor(bgLose!!)
                viewHolder.four.setBackgroundColor(bgLose!!)
                viewHolder.five.setBackgroundColor(color2!!)
            }
            5 -> {
                viewHolder?.one!!.setBackgroundColor(bgLose!!)
                viewHolder.two.setBackgroundColor(bgLose!!)
                viewHolder.three.setBackgroundColor(bgLose!!)
                viewHolder.four.setBackgroundColor(bgLose!!)
                viewHolder.five.setBackgroundColor(bgLose!!)
            }
        }
        viewHolder?.jiancang?.setOnClickListener{
            v ->
                val contentView = LayoutInflater.from(context).inflate(R.layout.current_dialog, null)
                val dialog = Dialog(context, R.style.AlertDialog)
                val window = dialog.window
                if (window != null) {
                    val params = window.attributes
                    //设置背景昏暗度
                    params.dimAmount = 0.2f
                    params.gravity = Gravity.CENTER
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    window.attributes = params
                }
                //设置dialog的宽高为屏幕的宽高
                val display = context.resources.displayMetrics
                val layoutParams =
                    ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.setContentView(contentView, layoutParams)
                dialog.show()
                dialog.findViewById<SpanTextView>(R.id.title).text = "自动减仓"
                dialog.findViewById<SpanTextView>(R.id.btn_bills).text = "这一指标显示你持仓在自动减仓队列中的位置，如果所有指示灯亮起，在发生强平事件后，你持仓的仓位可能被减小"
                dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
                    dialog.dismiss()
                }

        }
        viewHolder?.btnBond?.setOnClickListener {
            v ->
            ContractMultipleSelectWindow(context as Activity,
                getString(R.string.contract_adjust),
                buyMultiChooseBean,
                positionData?.leverage.toString(),
                object : ContractMultipleSelectWindow.OnReturnListener {
                    override fun onReturn(
                        item: ContractMultiChooseBean?
                    ) {
                        buyMultiChooseBean = item
                        viewHolder.beishu.setText(buyMultiChooseBean?.defaultMultiple.toString())
                    }
                }).show()

        }
        viewHolder?.btnWithLimit?.setOnClickListener {
            v ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.profit_loss_dialog, null)
            val dialog = Dialog(context, R.style.AlertDialog)
            val window = dialog.window
            if (window != null) {
                val params = window.attributes
                //设置背景昏暗度
                params.dimAmount = 0.2f
                params.gravity = Gravity.BOTTOM
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                window.attributes = params
            }
            //设置dialog的宽高为屏幕的宽高
            val layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setContentView(contentView, layoutParams)
            dialog.show()
            dialog.findViewById<TextView>(R.id.positionDes).text = positionData?.symbol.toString().uppercase()
            dialog.findViewById<TextView>(R.id.cangwei).text = positionType
            dialog.findViewById<TextView>(R.id.beishu).text = positionData?.leverage.toString() + "X"
            dialog.findViewById<TextView>(R.id.positionSide).text = sideDes
            dialog.findViewById<TextView>(R.id.keping).text = positionData?.price + unit
            dialog.findViewById<TextView>(R.id.positionSide).setTextColor(sideBgColor!!)
            dialog.findViewById<TextView>(R.id.positionSide).setBackgroundColor(sideBlackColor!!)
            dialog.findViewById<TextView>(R.id.junjia).text = positionData?.entryPrice
            dialog.findViewById<TextView>(R.id.zuixin).text = positionData?.flagPrice
            dialog.findViewById<TextView>(R.id.price).text = positionData?.price


            dialog.findViewById<TextView>(R.id.qiangping2).text =  if (BigDecimal(positionData?.forceStopPrice).compareTo(BigDecimal.ZERO) == 1)
            {
                positionData?.forceStopPrice
            } else {
                 "--"
            }
            dialog.findViewById<SpanMaterialEditText>(R.id.one).addTextChangedListener(object :
                TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val count1 = CommonUtil.parseDouble(
                        dialog.findViewById<SpanMaterialEditText>(R.id.one).text.toString()
                            .trim { it <= ' ' })?:0.0
                    var count2 = BigDecimal.ZERO
                    var count3 = "0.00"
                    if (positionData?.positionSide == "LONG"){
                        if (CommonUtil.parseDouble(positionData?.entryPrice)!! > count1){
                            count2 = BigDecimal.ZERO
                            click = false
                        }
                        else {
                            count2 =BigDecimal(count1 - CommonUtil.parseDouble(positionData?.entryPrice)!!).multiply(num).multiply(BigDecimal(positionData?.positionSize))
                            click = true
                        }
                    } else{
                        if (CommonUtil.parseDouble(positionData?.entryPrice)!! < count1){
                            count2 = BigDecimal.ZERO
                            click = false
                        }
                        else {
                            click = true
                            count2 = BigDecimal(CommonUtil.parseDouble(positionData?.entryPrice)!! - count1).multiply(
                                BigDecimal(positionData?.positionSize).multiply(num)
                            )
                        }
                    }
                    count3 =  String.format( "%.2f" , (count2.multiply(BigDecimal(100)) / (BigDecimal(bondDes))).toFloat())
                    dialog.findViewById<SpanTextView>(R.id.yinli).text = String.format("%.2f",count2) +  "USDT"
                    dialog.findViewById<SpanTextView>(R.id.two).text = count3
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
            dialog.findViewById<SpanMaterialEditText>(R.id.three).addTextChangedListener(object :
                TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val count1 = CommonUtil.parseDouble(
                        dialog.findViewById<SpanMaterialEditText>(R.id.three).text.toString()
                            .trim { it <= ' ' })?:0.0
                    var count2 = BigDecimal.ZERO
                    var count3 = "0.00"
                    if (positionData?.positionSide == "LONG"){
                        if (CommonUtil.parseDouble(positionData?.entryPrice)!! < count1){
                            count2 = BigDecimal.ZERO
                            click2 = false
                        }
                        else {
                            click2 = true
                            count2 = BigDecimal(count1 - CommonUtil.parseDouble(positionData?.entryPrice)!!).multiply(num).multiply(BigDecimal(positionData?.positionSize))

                        }
                    } else{
                        if (CommonUtil.parseDouble(positionData?.entryPrice)!! > count1){
                            click2 = false
                            count2 = BigDecimal.ZERO
                        }
                        else {
                            click2 = true
                            count2 = BigDecimal(CommonUtil.parseDouble(positionData?.entryPrice)!! - count1).multiply(
                                BigDecimal(positionData?.positionSize).multiply(num)
                            )
                        }
                    }
                    count3 =  String.format( "%.2f" , (count2.multiply(BigDecimal(100)) / (BigDecimal(bondDes))).toFloat())
                    dialog.findViewById<SpanTextView>(R.id.kuisun).text = String.format("%.2f",count2) +  "USDT"
                    dialog.findViewById<SpanTextView>(R.id.four).text = count3
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
            dialog.findViewById<View>(R.id.xuanzhe).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.xuanzhe2).visibility = View.VISIBLE
                dialog.findViewById<View>(R.id.qiangping).visibility = View.GONE
                if (dialog.findViewById<TextView>(R.id.xuanzhe).text == "限价")
                {
                    dialog.findViewById<TextView>(R.id.xiajia).setTextColor(color!!)
                    dialog.findViewById<TextView>(R.id.shijia).setTextColor(gray!!)
                }
                else{
                    dialog.findViewById<TextView>(R.id.xiajia).setTextColor(gray!!)
                    dialog.findViewById<TextView>(R.id.shijia).setTextColor(color!!)
                }
            }
            dialog.findViewById<View>(R.id.xiajia).setOnClickListener { v ->
                dialog.findViewById<TextView>(R.id.xiajia).setTextColor(color!!)
                dialog.findViewById<TextView>(R.id.shijia).setTextColor(gray!!)
                dialog.findViewById<TextView>(R.id.xuanzhe).text = "限价"
                dialog.findViewById<View>(R.id.xuanzhe2).visibility = View.GONE
                dialog.findViewById<View>(R.id.qiangping).visibility = View.VISIBLE
            }
            dialog.findViewById<View>(R.id.shijia).setOnClickListener { v ->
                dialog.findViewById<TextView>(R.id.xiajia).setTextColor(gray!!)
                dialog.findViewById<TextView>(R.id.shijia).setTextColor(color!!)
                dialog.findViewById<TextView>(R.id.xuanzhe).text = "市价"
                dialog.findViewById<View>(R.id.xuanzhe2).visibility = View.GONE
                dialog.findViewById<View>(R.id.qiangping).visibility = View.VISIBLE
            }
            dialog.findViewById<View>(R.id.first).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.first).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.second).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.third).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.fourth).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.fifth).setBackgroundColor(colorGray!!)
                dialog.findViewById<TextView>(R.id.price).text = String.format("%.2f",0.1 *  amonut) + unit

            }
            dialog.findViewById<View>(R.id.second).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.first).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.second).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.third).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.fourth).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.fifth).setBackgroundColor(colorGray!!)
                dialog.findViewById<TextView>(R.id.price).text = String.format("%.2f",0.25 * amonut) + unit
            }
            dialog.findViewById<View>(R.id.third).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.first).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.second).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.third).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.fourth).setBackgroundColor(colorGray!!)
                dialog.findViewById<View>(R.id.fifth).setBackgroundColor(colorGray!!)
                dialog.findViewById<TextView>(R.id.price).text = String.format("%.2f",0.5 * amonut) + unit
            }
            dialog.findViewById<View>(R.id.fourth).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.first).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.second).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.third).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.fourth).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.fifth).setBackgroundColor(colorGray!!)
                dialog.findViewById<TextView>(R.id.price).text = String.format("%.2f",0.75 * amonut) + unit
            }
            dialog.findViewById<View>(R.id.fifth).setOnClickListener { v ->
                dialog.findViewById<View>(R.id.first).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.second).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.third).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.fourth).setBackgroundColor(color!!)
                dialog.findViewById<View>(R.id.fifth).setBackgroundColor(color!!)
                dialog.findViewById<TextView>(R.id.price).text = String.format("%.2f",amonut) + unit
            }
            dialog.findViewById<SpanCheckBox>(R.id.zhiyin).setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dialog.findViewById<View>(R.id.zhiyin_layout).visibility = View.VISIBLE
                } else {
                    click = true
                    dialog.findViewById<View>(R.id.zhiyin_layout).visibility = View.GONE
                }
            }
            dialog.findViewById<SpanCheckBox>(R.id.zhisun).setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dialog.findViewById<View>(R.id.zhisun_layout).visibility = View.VISIBLE
                } else {
                    click2 = true
                    dialog.findViewById<View>(R.id.zhisun_layout).visibility = View.GONE
                }
            }
            /* dialog.findViewById<TextView>(R.id.one).addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                 val one = CommonUtil.parseDouble(dialog.findViewById<TextView>(R.id.one).text.trim { it < ' ' }.toString())!!.minus(CommonUtil.parseDouble(positionData.entryPrice)?:0.0)
                    val two = one.div(CommonUtil.parseDouble(positionData.entryPrice)?:1.0) * 100
                    if (one >= 0.0) {
                        dialog.findViewById<TextView>(R.id.yinli).text = String.format("%.2f", one)
                        dialog.findViewById<TextView>(R.id.two).text = String.format("%.2f %", two)
                    }
                    }

                override fun afterTextChanged(s: Editable) {
                }


            })
            dialog.findViewById<TextView>(R.id.three).addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val one = (CommonUtil.parseDouble(positionData.entryPrice)?:0.0).minus(CommonUtil.parseDouble(dialog.findViewById<TextView>(R.id.three).text.trim { it < ' ' }.toString())!!)
                    val two = one.div(CommonUtil.parseDouble(positionData.entryPrice)?:1.0) * 100
                    if (one >= 0.0) {
                        dialog.findViewById<TextView>(R.id.yinli).text = String.format("%.2f", one)
                        dialog.findViewById<TextView>(R.id.two).text = String.format("%.2f %", two)
                    }


                }

                override fun afterTextChanged(s: Editable) {
                }


            })*/
                dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
                    if (!click && positionData?.positionSide == "LONG"){
                        FryingUtil.showToast(context,"多仓止盈价不得低于开仓价")
                        return@setOnClickListener
                    }
                    if (!click && positionData?.positionSide == "SHORT"){
                        FryingUtil.showToast(context,"空仓止盈价不得高于开仓价")
                        return@setOnClickListener
                    }
                    if (!click2 && positionData?.positionSide == "LONG"){
                        FryingUtil.showToast(context,"多仓止损价不得高于开仓价")
                        return@setOnClickListener
                    }
                    if (!click2 && positionData?.positionSide == "SHORT"){
                        FryingUtil.showToast(context,"空仓止损价不得低于开仓价")
                        return@setOnClickListener
                    }
                    FutureApiServiceHelper.createOrderProfit(
                        context,
                        positionData?.symbol,
                        positionData?.positionSize!!.toInt(),
                        positionData?.positionSide,
                        CommonUtil.parseDouble(dialog.findViewById<TextView>(R.id.one).text.trim { it < ' ' }.toString()),
                        CommonUtil.parseDouble(dialog.findViewById<TextView>(R.id.three).text.trim { it < ' ' }.toString()),
                        "LATEST_PRICE",
                        object : Callback<HttpRequestResultBean<String>?>() {
                            override fun callback(returnData: HttpRequestResultBean<String>?) {
                                if (returnData != null) {
                                    FryingUtil.showToast(context,"Success")
                                    dialog.dismiss()
                                }
                            }
                            override fun error(type: Int, error: Any?) {
                                FryingUtil.showToast(context, error.toString())
                            }
                        })
            }
            dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
                dialog.dismiss()
            }


        }
        viewHolder?.btnClosePositionFan?.setOnClickListener {
                v ->
            val contentView = LayoutInflater.from(context).inflate(R.layout.fan_xiang_dialog, null)
            val dialog = Dialog(context, R.style.AlertDialog)
            val window = dialog.window
            if (window != null) {
                val params = window.attributes
                //设置背景昏暗度
                params.dimAmount = 0.2f
                params.gravity = Gravity.BOTTOM
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                window.attributes = params
            }
            //设置dialog的宽高为屏幕的宽高
            val layoutParams =
                ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setContentView(contentView, layoutParams)
            dialog.show()
            dialog.findViewById<TextView>(R.id.positionDes).text = positionData?.symbol.toString().uppercase()
            dialog.findViewById<TextView>(R.id.cangwei).text = positionType
            dialog.findViewById<TextView>(R.id.beishu).text = positionData?.leverage.toString() + "X"
            dialog.findViewById<TextView>(R.id.positionSide).text = sideDes
            dialog.findViewById<TextView>(R.id.keping).text = positionData?.price + unit
            dialog.findViewById<TextView>(R.id.two).text = positionData?.price + unit
            dialog.findViewById<TextView>(R.id.one).text = sideDes2
            dialog.findViewById<TextView>(R.id.price).text = positionData?.flagPrice
            dialog.findViewById<TextView>(R.id.positionSide).setTextColor(sideBgColor!!)
            dialog.findViewById<TextView>(R.id.positionSide).setBackgroundColor(sideBlackColor!!)
            dialog.findViewById<TextView>(R.id.one).setTextColor(sideBgColor2!!)
            dialog.findViewById<TextView>(R.id.two).setTextColor(sideBgColor2!!)
            dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                if (viewHolder.btnClosePosition.isChecked) {
                    FryingUtil.showToast(context, "仓位处于平仓状态，不能反向开仓")
                    dialog.dismiss()
                } else {
                    val createRunnable = Runnable {
                        Log.d(
                            "knsdjofdiosj",
                            positionData?.positionSide + positionData?.positionSize
                        )
                        FutureApiServiceHelper.createOrder3(context,
                            if (positionData?.positionSide == "LONG") "SELL" else "BUY",
                            "MARKET",
                            positionData?.symbol,
                            positionData?.positionSide,
                            "IOC",
                            positionData?.positionSize!!.toInt(),
                            4,
                            false,
                            object : Callback<HttpRequestResultBean<String>?>() {
                                override fun callback(returnData: HttpRequestResultBean<String>?) {
                                    if (returnData != null) {
                                        viewHolder.btnClosePosition.isChecked = true
                                        FryingUtil.showToast(context, "反向开仓成功")
                                        /**
                                         * todo 刷新持仓列表
                                         */
                                    }
                                }

                                override fun error(type: Int, error: Any?) {
                                    Log.d("iiiiii-->createFutureOrder--error", error.toString())
                                    FryingUtil.showToast(context, error.toString())
                                }

                            })
                    }
                    createRunnable.run()
                    dialog.dismiss()
                }
            }

        }
        viewHolder?.btnClosePosition?.setOnClickListener {
            if (viewHolder.btnClosePosition.isChecked){
                FryingUtil.showToast(mContext,"当前不能平仓")
            }
            else {
                pingcang()
            }
        }

    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    private fun dialog(){
        val contentView = LayoutInflater.from(context).inflate(R.layout.position_dialog, null)
        val dialog = Dialog(context, com.black.wallet.R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val layoutParams =
            ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        url = UrlConfig.getHost(context) + "/auth/register/" + CookieUtil.getUserInfo(context)?.inviteCode
        val uri = Uri.parse(url)
        //val secret = uri.getQueryParameter("secret") //解析参数
        if (!TextUtils.isEmpty(url)) { //显示密钥，并进行下一步
            var qrcodeBitmap: Bitmap? = null
            try {
                qrcodeBitmap = CommonUtil.createQRCode(url, 40, 0)
            } catch (e: WriterException) {
                CommonUtil.printError(context, e)
            }
            dialog.findViewById<ImageView>(R.id.image).setImageBitmap(qrcodeBitmap)
        }
            dialog.findViewById<SpanTextView>(R.id.first).text = sideDes + positionType
            dialog.findViewById<SpanTextView>(R.id.first).setTextColor(sideBgColor!!)
            dialog.findViewById<SpanTextView>(R.id.second).text = " /" + positionData?.leverage  + "X/ " + positionData?.symbol!!.uppercase() + " 永续"
            dialog.findViewById<SpanTextView>(R.id.fifth).text = positionData?.entryPrice
            dialog.findViewById<SpanTextView>(R.id.sixth).text = positionData?.flagPrice
            dialog.findViewById<SpanTextView>(R.id.fifth).setTextColor(color1!!)
            dialog.findViewById<SpanTextView>(R.id.sixth).setTextColor(color1!!)
        if (positionData?.unRealizedProfit?.toDouble()!! > 0.0){
            dialog.findViewById<SpanTextView>(R.id.third).text =  "+" + positionData?.profitRate
            dialog.findViewById<SpanTextView>(R.id.fourth).text = "(" + "+" + positionData?.unRealizedProfit + positionData?.symbol!!.split("_")[1].toString().uppercase() + ")"
            dialog.findViewById<SpanTextView>(R.id.third).setTextColor(bgWin!!)
            dialog.findViewById<SpanTextView>(R.id.fourth).setTextColor(bgWin!!)
        }
        else{
            dialog.findViewById<SpanTextView>(R.id.third).text =  positionData?.profitRate
            dialog.findViewById<SpanTextView>(R.id.fourth).text = "(" + positionData?.unRealizedProfit + positionData?.symbol!!.split("_")[1].toString().uppercase() + ")"
            dialog.findViewById<SpanTextView>(R.id.third).setTextColor(bgLose!!)
            dialog.findViewById<SpanTextView>(R.id.fourth).setTextColor(bgLose!!)
        }
        dialog.findViewById<SpanCheckBox>(R.id.one).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dialog.findViewById<SpanTextView>(R.id.first).visibility = View.VISIBLE
                dialog.findViewById<SpanTextView>(R.id.second).visibility = View.VISIBLE
            } else {
                dialog.findViewById<SpanTextView>(R.id.first).visibility = View.GONE
                dialog.findViewById<SpanTextView>(R.id.second).visibility = View.GONE
            }
        }
        dialog.findViewById<SpanCheckBox>(R.id.two).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dialog.findViewById<SpanTextView>(R.id.third).visibility = View.VISIBLE
                dialog.findViewById<SpanTextView>(R.id.fourth).visibility = View.VISIBLE
            } else {
                dialog.findViewById<SpanTextView>(R.id.third).visibility = View.GONE
                dialog.findViewById<SpanTextView>(R.id.fourth).visibility = View.GONE
            }
        }
        dialog.findViewById<SpanCheckBox>(R.id.three).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dialog.findViewById<SpanTextView>(R.id.jiage1).visibility = View.VISIBLE
                dialog.findViewById<SpanTextView>(R.id.jiage2).visibility = View.VISIBLE
                dialog.findViewById<SpanTextView>(R.id.fifth).visibility = View.VISIBLE
                dialog.findViewById<SpanTextView>(R.id.sixth).visibility = View.VISIBLE
            } else {
                dialog.findViewById<SpanTextView>(R.id.jiage1).visibility = View.GONE
                dialog.findViewById<SpanTextView>(R.id.jiage2).visibility = View.GONE
                dialog.findViewById<SpanTextView>(R.id.fifth).visibility = View.GONE
                dialog.findViewById<SpanTextView>(R.id.sixth).visibility = View.GONE
            }
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_resume).setOnClickListener { v ->
            dialog.window!!.decorView.isDrawingCacheEnabled = true
            dialog.window!!.decorView.buildDrawingCache()
            val bmp: Bitmap =  dialog.window?.decorView!!.drawingCache
            val contentResolver: ContentResolver = context.contentResolver
            //这里"IMG"+ Calendar.getInstance().time如果没有可能会出现报错
            val uri = Uri.parse(
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    bmp,
                    "IMG" + Calendar.getInstance().time,
                    null
                )
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(context,Intent.createChooser(intent, "分享"),null)
        }

    }
    private fun pingcang(){
            FutureApiServiceHelper.createOrder2(context,
                if (positionData?.positionSide == "LONG") "SELL" else "BUY",
                "LIMIT",//限价“LIMIT”市价“MARKET”
                positionData?.symbol,
                positionData?.positionSide,
                "GTC",//限价“GTC”市价“IOC”
                positionData?.positionSize!!.toInt(),
                positionData?.entryPrice,//限价positionData?.flagPrice市价null
                true,
                object : Callback<HttpRequestResultBean<String>?>() {
                    override fun callback(returnData: HttpRequestResultBean<String>?) {
                        if (returnData != null) {
                            FryingUtil.showToast(context, "平仓成功")
                            /**
                             * todo 刷新持仓列表
                             */
                        }
                        else{
                            FryingUtil.showToast(context, returnData?.msgInfo)
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d("iiiiii-->createFutureOrder--error", error.toString())
                        FryingUtil.showToast(context, error.toString())
                    }

                })


    }


    /*fun set(price:String?)
    {
    }*/
    }
