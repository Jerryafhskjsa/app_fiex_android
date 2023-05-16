package com.black.frying.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.ContractMultiChooseBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PositionBean
import com.black.base.util.FryingUtil
import com.black.base.view.ContractMultipleSelectWindow
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabPositionBinding
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal

class ContractPositionTabAdapter(context: Context, data: MutableList<PositionBean?>) :
    BaseDataTypeBindAdapter<PositionBean?, ListItemContractTabPositionBinding>(context, data) {
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null
    private var color: Int? = null
    private var gray: Int? = null
    private var colorGray: Int? = null
    private var amount: Double? = null
    private var amount2: Double? = null
    private var buyMultiChooseBean: ContractMultiChooseBean? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, R.color.T3)
        bgWin = SkinCompatResources.getColor(context, R.color.T10)
        bgLose = SkinCompatResources.getColor(context, R.color.T9)
        color = SkinCompatResources.getColor(context, R.color.T13)
        gray = SkinCompatResources.getColor(context, R.color.black)
        colorGray = SkinCompatResources.getColor(context, R.color.gray)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.list_item_contract_tab_position
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabPositionBinding>?) {
        val positionData = getItem(position)

        Log.d("ttt----->item", positionData.toString())
        val unit = positionData?.symbol!!.split("_")[1].toString().uppercase()

        val viewHolder = holder?.dataBing
        var sideDes: String? = null
        var sideBgColor: Int? = null
        var sideBlackColor: Int? = null
        var bondDes: String? = null
        var positionType: String? = null
        var amonut: Double? = 0.0
        val autoMergeBond: Boolean? = positionData.autoMargin
        buyMultiChooseBean = ContractMultiChooseBean()
        buyMultiChooseBean?.maxMultiple = 100
        buyMultiChooseBean?.defaultMultiple = positionData.leverage
        buyMultiChooseBean?.symbol = "btc_usdt"
        amonut = positionData.price?.toDouble()?:0.0
        when (positionData.positionSide) {
            //做多
            "LONG" -> {
                sideDes = getString(R.string.contract_see_up)
                sideBgColor = context.getColor(R.color.T17)
                sideBlackColor = context.getColor(R.color.T22)
                buyMultiChooseBean?.orientation = "BUY"
            }
            //做空
            "SHORT" -> {
                sideDes = getString(R.string.contract_see_down)
                sideBgColor = context.getColor(R.color.T16)
                sideBlackColor = context.getColor(R.color.T21)
                buyMultiChooseBean?.orientation = "SELL"
            }
        }
        when (positionData.positionType) {
            //逐仓
            "ISOLATED" -> {
                bondDes = positionData.isolatedMargin
                buyMultiChooseBean?.type = 0
                positionType = getString(R.string.contract_fiexble_position)
            }
            //全仓
            "CROSSED" -> {
                bondDes = positionData.isolatedMargin
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
        viewHolder?.positionDes?.text = positionData.symbol.toString().uppercase()
        viewHolder?.cangwei?.text = positionType
        viewHolder?.beishu?.text = positionData.leverage.toString() + "X"
        //方向
        viewHolder?.positionSide?.text = sideDes
        viewHolder?.positionSide?.setTextColor(sideBgColor!!)
        viewHolder?.positionSide?.setBackgroundColor(sideBlackColor!!)
        //已实现盈亏
        viewHolder?.alreadyCloseProfit?.text = positionData.realizedProfit
        //当前盈亏
        viewHolder?.profits?.text = positionData.unRealizedProfit
        //当前盈亏百分比
        viewHolder?.profitsPercent?.text = positionData.profitRate
        //持仓均价
        viewHolder?.entryPrice?.text = positionData.entryPrice
        //强平价格>0
        //持仓数量
        viewHolder?.positionAmount?.text = positionData.price
        //可平数量
        viewHolder?.availableCloseAmount?.text = positionData.price
        //仓位保证金
        //viewHolder?.bondAmount?.text = positionData.isolatedMargin

        if(positionData.forceStopPrice != null){
            if (BigDecimal(positionData.forceStopPrice).compareTo(BigDecimal.ZERO) == 1) {
                viewHolder?.forceClosePrice?.text = positionData.forceStopPrice
            } else {
                viewHolder?.forceClosePrice?.text = "--"
            }
        }
        //标记价格
        viewHolder?.flagPrice?.text = positionData.flagPrice
        //保证金
        viewHolder?.bondAmount?.text = bondDes
        //是否自动追加保证金开关
        viewHolder?.autoAddBond?.isChecked = autoMergeBond!!
        viewHolder?.autoAddBond?.setOnCheckedChangeListener { _, isChecked ->
            FutureApiServiceHelper.autoMargin(
                context,
                positionData.symbol,
                positionData.positionSide,
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
        when (positionData.adl) {
            0 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_0)
            }
            1 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_0)
            }
            2 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_2)
            }
            3 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_3)
            }
            4 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_4)
            }
            5 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_5)
            }
        }
        viewHolder?.btnBond?.setOnClickListener {
            v ->
            ContractMultipleSelectWindow(context as Activity,
                getString(R.string.contract_adjust),
                buyMultiChooseBean,
                positionData.leverage.toString(),
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
            dialog.findViewById<TextView>(R.id.positionDes).text = positionData.symbol.toString().uppercase()
            dialog.findViewById<TextView>(R.id.cangwei).text = positionType
            dialog.findViewById<TextView>(R.id.beishu).text = positionData.leverage.toString() + "X"
            dialog.findViewById<TextView>(R.id.positionSide).text = sideDes
            dialog.findViewById<TextView>(R.id.keping).text = positionData.price + unit
            dialog.findViewById<TextView>(R.id.positionSide).setTextColor(sideBgColor!!)
            dialog.findViewById<TextView>(R.id.positionSide).setBackgroundColor(sideBlackColor!!)
            dialog.findViewById<TextView>(R.id.junjia).text = positionData.entryPrice
            dialog.findViewById<TextView>(R.id.zuixin).text = positionData.flagPrice
            dialog.findViewById<TextView>(R.id.price).text = positionData.price

            dialog.findViewById<TextView>(R.id.qiangping2).text =  if (BigDecimal(positionData.forceStopPrice).compareTo(BigDecimal.ZERO) == 1)
            {
                positionData.forceStopPrice
            } else {
                 "--"
            }
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
                    FutureApiServiceHelper.createOrderProfit(
                        context,
                        positionData.symbol,
                        positionData.positionSize!!.toInt(),
                        positionData.positionSide,
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

        }
        viewHolder?.btnClosePosition?.setOnClickListener {
            val createRunnable = Runnable {
                FutureApiServiceHelper.createOrder2(context,
                    "SELL",
                    "MARKET",
                    positionData.symbol,
                    positionData.positionSide,
                    "IOC",
                    positionData.positionSize!!.toInt(),
                     "1001",
                    true,
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                FryingUtil.showToast(context, "平仓成功")

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

        }

    }
    /*fun set(price:String?)
    {
    }*/
    }