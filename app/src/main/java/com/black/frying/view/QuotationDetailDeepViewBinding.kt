package com.black.frying.view

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.black.base.model.socket.TradeOrder
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.widget.DepthChart
import com.black.base.widget.SpanTextView
import com.black.base.widget.deeepView.DepthBuySellData
import com.black.frying.activity.QuotationDetailActivity
import com.black.lib.view.ProgressDrawable
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentQuotationDetailDepthBinding
import com.google.gson.reflect.TypeToken
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class QuotationDetailDeepViewBinding(
    private val context: Context,
    private val binding: FragmentQuotationDetailDepthBinding,
    private val maxCount: Int
) {
    private var contentLayoutInited = false

    private var colorT7 = 0
    private var colorT5 = 0
    private var colorT3 = 0
    private var colorT2 = 0
    private var colorTransparent = 0
    private var color9 = 0
    private var color10 = 0

    private var padding: Float? = 0f
    private var nullAmount: String? = null

    init {
        colorTransparent = SkinCompatResources.getColor(context, R.color.transparent)
        colorT7 = SkinCompatResources.getColor(context, R.color.T7)
        colorT5 = SkinCompatResources.getColor(context, R.color.T5)
        colorT3 = SkinCompatResources.getColor(context, R.color.T3)
        colorT2 = SkinCompatResources.getColor(context, R.color.T2)
        color9 = SkinCompatResources.getColor(context, R.color.T7_ALPHA10)
        color10 = SkinCompatResources.getColor(context, R.color.T5_ALPHA10)

        padding = context.resources?.getDimension(R.dimen.default_padding)
        nullAmount = context.resources?.getString(R.string.number_default)
    }

    fun initList() {
        if (!contentLayoutInited) { //初始化deepContentLayout
            binding.orderContentLayout?.removeAllViews()
            for (i in 0 until maxCount) {
                val itemView = createDeepContentLayoutItem()
                itemView.visibility = View.GONE
                binding.orderContentLayout?.addView(itemView)
            }
            contentLayoutInited = true
        }
    }

    private fun createDeepContentLayoutItem(): View {
        val viewHolder = QuotationDetailActivity.OrderViewHolder()
        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.orientation = LinearLayout.VERTICAL
        val fistLine = LinearLayout(context)
        fistLine.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        fistLine.orientation = LinearLayout.HORIZONTAL
        fistLine.gravity = Gravity.CENTER_VERTICAL
        val buyLayout = LinearLayout(context)
        buyLayout.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.toFloat())
        buyLayout.orientation = LinearLayout.HORIZONTAL
        buyLayout.gravity = Gravity.CENTER_VERTICAL
        buyLayout.setPadding(0, padding!!.toInt(), (padding!! / 2).toInt(), padding!!.toInt())
        val buyIndexView = createTextView(1, colorT3, 13f)
        buyLayout.addView(buyIndexView)
        viewHolder.buyIndexView = buyIndexView
        val buyAmountView = createTextView(4, colorT2, 13f)
        buyAmountView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
        buyLayout.addView(buyAmountView)
        viewHolder.buyAmountView = buyAmountView
        val buyPriceView = createTextView(4, colorT7, 13f)
        buyPriceView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
        buyPriceView.gravity = Gravity.RIGHT
        buyLayout.addView(buyPriceView)
        viewHolder.buyPriceView = buyPriceView
        buyLayout.background = ProgressDrawable(color9, colorTransparent, ProgressDrawable.RIGHT)
        viewHolder.buyLayout = buyLayout
        fistLine.addView(buyLayout)
        val saleLayout = LinearLayout(context)
        saleLayout.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.toFloat())
        saleLayout.orientation = LinearLayout.HORIZONTAL
        saleLayout.gravity = Gravity.CENTER_VERTICAL
        saleLayout.setPadding((padding!! / 2).toInt(), padding!!.toInt(), 0, padding!!.toInt())
        val salePriceView = createTextView(4, colorT5, 13f)
        salePriceView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
        saleLayout.addView(salePriceView)
        viewHolder.salePriceView = salePriceView
        val saleAmountView = createTextView(4, colorT2, 13f)
        saleAmountView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
        saleAmountView.gravity = Gravity.RIGHT
        saleLayout.addView(saleAmountView)
        viewHolder.saleAmountView = saleAmountView
        val saleIndexView = createTextView(1, colorT3, 13f)
        saleIndexView.gravity = Gravity.RIGHT
        saleLayout.addView(saleIndexView)
        viewHolder.saleIndexView = saleIndexView
        saleLayout.background = ProgressDrawable(color10, colorTransparent)
        viewHolder.saleLayout = saleLayout
        fistLine.addView(saleLayout)
        linearLayout.addView(fistLine)
        linearLayout.tag = viewHolder
        return linearLayout
    }

    private fun createTextView(weight: Int, textColor: Int, textSize: Float): TextView {
        val textView = SpanTextView(context)
        val textViewParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight.toFloat())
        textView.layoutParams = textViewParams
        textView.maxLines = 1
        textView.setTextColor(textColor)
        textView.textSize = textSize
        return textView
    }

    //刷新深度图
    private fun refreshDepthChart(
        currentPrice: Double,
        bidOrders: List<TradeOrder?>,
        askOrders: List<TradeOrder?>
    ) {
        //组装深度图数据
//        val data = DepthChart.Data()
//        data.middlePrice = currentPrice
//        val buyItems: MutableList<DepthChart.Item> = ArrayList(bidOrders.size)
//        for (bidOrder in bidOrders) {
//            buyItems.add(DepthChart.Item(bidOrder!!.price ?: 0.0, bidOrder.exchangeAmount))
//        }
//        val saleItems: MutableList<DepthChart.Item> = ArrayList(askOrders.size)
//        for (askOrder in askOrders) {
//            saleItems.add(DepthChart.Item(askOrder!!.price ?: 0.0, askOrder.exchangeAmount))
//        }
//        data.buyItems = buyItems
//        data.saleItems = saleItems
//        binding.depthChart.setData(data)
        var buyList: ArrayList<DepthBuySellData>? = ArrayList()
        var sellList: ArrayList<DepthBuySellData>? = ArrayList()
        for (bidOrder in bidOrders) {
            sellList?.add(DepthBuySellData(bidOrder!!.price.toString() ?: "0.0", bidOrder.exchangeAmount.toString()))
        }
        for (askOrder in askOrders) {
            buyList?.add(DepthBuySellData(askOrder!!.price.toString() ?: "0.0", askOrder.exchangeAmount.toString()))
        }
        if (buyList?.isNotEmpty()!! && sellList?.isEmpty()!!) {
            sellList.add(0, DepthBuySellData("0", "0"))
        } else if (buyList.isEmpty() && sellList?.isNotEmpty()!!) {
            buyList.add(0, DepthBuySellData("0", "0"))
        }
        binding.depthChart.setData(buyList, sellList, CookieUtil.getCurrentPair(context), 6, 4)
    }

    //刷新当前交易数据
    fun refreshQuotationOrders(
        currentPrice: Double,
        bid: List<TradeOrder?>?,
        ask: List<TradeOrder?>?
    ) {
        var bidOrders: List<TradeOrder?> = bid ?: ArrayList()
        var askOrders: List<TradeOrder?> = ask ?: ArrayList()
        //显示列表
        val bidCount = bidOrders.size
        val askCount = askOrders.size
        val size = min(max(bidCount, askCount), maxCount)
        bidOrders = bidOrders.subList(0, min(size, bidCount))
        askOrders = askOrders.subList(0, min(size, askCount))
        FryingUtil.computeTradeOrderWeightPercent(bidOrders, askOrders)
        //重绘深度图
        refreshDepthChart(currentPrice, bidOrders, askOrders)
        val oldSize = binding.orderContentLayout.childCount
        if (oldSize >= size) {
            for (i in 0 until size) {
                val itemView = binding.orderContentLayout?.getChildAt(i)
                showOrderItem(
                    itemView!!,
                    CommonUtil.getItemFromList(bidOrders, i),
                    CommonUtil.getItemFromList(askOrders, i),
                    i
                )
                itemView.visibility = View.VISIBLE
            }
            for (i in oldSize - 1 downTo size) {
                val itemView = binding.orderContentLayout?.getChildAt(i)
                itemView!!.visibility = View.GONE
            }
        } else {
            for (i in 0 until size) {
                val itemView = binding.orderContentLayout?.getChildAt(i)
                showOrderItem(
                    itemView!!,
                    CommonUtil.getItemFromList(bidOrders, i),
                    CommonUtil.getItemFromList(askOrders, i),
                    i
                )
                itemView.visibility = View.VISIBLE
            }
        }
    }

    private fun showOrderItem(
        itemView: View,
        bidOrder: TradeOrder?,
        askOrder: TradeOrder?,
        position: Int
    ) {
        val viewHolder = itemView.tag as QuotationDetailActivity.OrderViewHolder
        if (bidOrder != null) {
//            viewHolder.buyAmountView!!.text = CommonUtil.formatNumberNoGroup(bidOrder.exchangeAmount, amountLength, amountLength)
            viewHolder.buyAmountView!!.text = bidOrder.exchangeAmountFormat
            viewHolder.buyPriceView!!.text = bidOrder.formattedPrice
            viewHolder.buyIndexView!!.text = (position + 1).toString()
            setOrderBg(viewHolder.buyLayout, bidOrder)
        } else {
            viewHolder.buyAmountView!!.text = ""
            viewHolder.buyPriceView!!.text = ""
            viewHolder.buyIndexView!!.text = ""
            setOrderBg(viewHolder.buyLayout, null)
        }
        if (askOrder != null) {
//            viewHolder.saleAmountView!!.text = CommonUtil.formatNumberNoGroup(askOrder.exchangeAmount, amountLength, amountLength)
            viewHolder.saleAmountView!!.text = askOrder.exchangeAmountFormat
            viewHolder.salePriceView!!.text = askOrder.formattedPrice
            viewHolder.saleIndexView!!.text = (position + 1).toString()
            setOrderBg(viewHolder.saleLayout, askOrder)
        } else {
            viewHolder.saleAmountView!!.text = ""
            viewHolder.salePriceView!!.text = ""
            viewHolder.saleIndexView!!.text = ""
            setOrderBg(viewHolder.saleLayout, null)
        }
    }

    private fun setOrderBg(layout: View?, tradeOrder: TradeOrder?) {
        if (layout == null) {
            return
        }
        val background = layout.background
        if (background is ProgressDrawable) {
            val progress: Double = tradeOrder?.weightPercent ?: 0.toDouble()
            background.setProgress(progress)
        }
    }

    fun refreshTitles(pair: String?) {
        var coinType: String? = null
        pair!!.run {
            val arr: Array<String> = pair.split("_").toTypedArray()
            if (arr.size > 1) {
                coinType = arr[0]
            }
        }
        coinType = coinType
            ?: nullAmount
        binding.orderAmountBuyTitle.setText(context.getString(R.string.k_buy_amount, coinType))
        binding.orderPriceTitle.setText(
            context.getString(R.string.price).toString() + "(" + coinType + ")"
        )
        binding.orderAmountSaleTitle.setText(context.getString(R.string.k_sale_amount, coinType))
    }

    fun show() {
        binding.root.visibility = View.VISIBLE
    }

    fun hide() {
        binding.root.visibility = View.GONE
    }

    fun setPrecision(precision: Int) {
        binding.depthChart.setPrecision(precision)
        binding.depthChart.invalidate()
    }

    fun setAmountPrecision(amountPrecision: Int) {
        binding.depthChart.setAmountPrecision(amountPrecision)
        binding.depthChart.invalidate()
    }
}