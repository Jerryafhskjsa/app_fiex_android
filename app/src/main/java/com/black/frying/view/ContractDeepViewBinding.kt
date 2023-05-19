package com.black.frying.view

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.black.base.model.socket.Deep
import com.black.base.model.socket.TradeOrder
import com.black.base.util.FryingUtil
import com.black.base.view.DeepControllerWindow
import com.black.frying.viewmodel.ContractViewModel
import com.black.frying.viewmodel.TransactionViewModel
import com.black.lib.view.ProgressDrawable
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractHeader1Binding
import com.fbsex.exchange.databinding.FragmentHomePageTransactionHeader1Binding
import skin.support.content.res.SkinCompatResources
import java.util.*

class ContractDeepViewBinding(private val context: Activity, private val viewModel: ContractViewModel, private val binding: FragmentHomePageContractHeader1Binding) : View.OnClickListener {
    companion object {
        private const val STYLE_NORMAL = 0
        private const val STYLE_BID_ALL = 1
        private const val STYLE_ASK_ALL = 2
    }

    private var colorTransparent: Int
    private var colorT7A10: Int
    private var colorT5A10: Int
    private var currentStyle = STYLE_NORMAL
    private val nullAmount: String
    private var onTransactionDeepListener: OnTransactionDeepListener? = null
    fun init() {
        binding.handicapSaleLayout01.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout01.setOnClickListener(this)
        binding.handicapSaleLayout02.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout02.setOnClickListener(this)
        binding.handicapSaleLayout03.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout03.setOnClickListener(this)
        binding.handicapSaleLayout04.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout04.setOnClickListener(this)
        binding.handicapSaleLayout05.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout05.setOnClickListener(this)
        binding.handicapSaleLayout06.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout06.setOnClickListener(this)
        binding.handicapSaleLayout07.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout07.setOnClickListener(this)
        binding.handicapSaleLayout08.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout08.setOnClickListener(this)
        binding.handicapSaleLayout09.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout09.setOnClickListener(this)
        binding.handicapSaleLayout10.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout10.setOnClickListener(this)
        binding.handicapSaleLayout11.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout11.setOnClickListener(this)
        binding.handicapSaleLayout12.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout12.setOnClickListener(this)
        binding.handicapSaleLayout13.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout13.setOnClickListener(this)
        binding.handicapSaleLayout14.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout14.setOnClickListener(this)
        binding.handicapSaleLayout15.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout15.setOnClickListener(this)
        binding.handicapSaleLayout16.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout16.setOnClickListener(this)
        binding.handicapSaleLayout17.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout17.setOnClickListener(this)
        binding.handicapSaleLayout18.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout18.setOnClickListener(this)
        binding.handicapSaleLayout19.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout19.setOnClickListener(this)
        binding.handicapSaleLayout20.background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapSaleLayout20.setOnClickListener(this)
        binding.handicapBuyLayout01.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout01.setOnClickListener(this)
        binding.handicapBuyLayout02.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout02.setOnClickListener(this)
        binding.handicapBuyLayout03.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout03.setOnClickListener(this)
        binding.handicapBuyLayout04.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout04.setOnClickListener(this)
        binding.handicapBuyLayout05.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout05.setOnClickListener(this)
        binding.handicapBuyLayout06.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout06.setOnClickListener(this)
        binding.handicapBuyLayout07.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout07.setOnClickListener(this)
        binding.handicapBuyLayout08.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout08.setOnClickListener(this)
        binding.handicapBuyLayout09.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout09.setOnClickListener(this)
        binding.handicapBuyLayout10.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout10.setOnClickListener(this)
        binding.handicapBuyLayout11.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout11.setOnClickListener(this)
        binding.handicapBuyLayout12.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout12.setOnClickListener(this)
        binding.handicapBuyLayout13.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout13.setOnClickListener(this)
        binding.handicapBuyLayout14.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout14.setOnClickListener(this)
        binding.handicapBuyLayout15.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout15.setOnClickListener(this)
        binding.handicapBuyLayout16.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout16.setOnClickListener(this)
        binding.handicapBuyLayout17.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout17.setOnClickListener(this)
        binding.handicapBuyLayout18.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout18.setOnClickListener(this)
        binding.handicapBuyLayout19.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout19.setOnClickListener(this)
        binding.handicapBuyLayout20.background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
        binding.handicapBuyLayout20.setOnClickListener(this)
        binding.deep.setOnClickListener(this)
        binding.displayType.setOnClickListener(this)
        binding.linDeepPostion.setOnClickListener(this)
        binding.linDeepDepth.setOnClickListener(this)
        showCurrentStyle()
        showStyleLayout()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.deep,R.id.lin_deep_postion ->  //弹出深度选择器
                if (viewModel.getPrecisionList() != null && viewModel.getPrecisionList()!!.isNotEmpty()) {
                    DeepControllerWindow<Deep>(context, "深度选择", viewModel.getPrecisionDeep(viewModel.getPrecision()), viewModel.getPrecisionList() as List<Deep>,
                            object : DeepControllerWindow.OnReturnListener<Deep> {
                                override fun onReturn(window: DeepControllerWindow<Deep>, item: Deep) {
                                    item.precision?.let { viewModel.setPrecision(it) }
                                    if (onTransactionDeepListener != null) {
                                        onTransactionDeepListener!!.onDeepChanged(item)
                                    }
                                }
                            }).show()
                }
            R.id.display_type -> {
                currentStyle = (currentStyle + 1) % 3
                showCurrentStyle()
                showStyleLayout()
                viewModel.getAllDepthOrderFiex()
            }
            R.id.handicap_sale_layout_01, R.id.handicap_sale_layout_02, R.id.handicap_sale_layout_03, R.id.handicap_sale_layout_04, R.id.handicap_sale_layout_05, R.id.handicap_sale_layout_06, R.id.handicap_sale_layout_07, R.id.handicap_sale_layout_08, R.id.handicap_sale_layout_09, R.id.handicap_sale_layout_10, R.id.handicap_buy_layout_01, R.id.handicap_buy_layout_02, R.id.handicap_buy_layout_03, R.id.handicap_buy_layout_04, R.id.handicap_buy_layout_05, R.id.handicap_buy_layout_06, R.id.handicap_buy_layout_07, R.id.handicap_buy_layout_08, R.id.handicap_buy_layout_09, R.id.handicap_buy_layout_10 -> {
                val tag = v.tag
                if (tag is TradeOrder) {
                    if (onTransactionDeepListener != null) {
                        onTransactionDeepListener!!.onTradeOrderFastClick(tag)
                    }
                }
            }
        }
    }

    fun setOnTransactionDeepListener(onTransactionDeepListener: OnTransactionDeepListener?) {
        this.onTransactionDeepListener = onTransactionDeepListener
    }

    fun doResetSkinResources() {
        colorT7A10 = SkinCompatResources.getColor(context, R.color.T7_ALPHA10)
        colorT5A10 = SkinCompatResources.getColor(context, R.color.T5_ALPHA10)
        colorTransparent = SkinCompatResources.getColor(context, R.color.transparent)
        resetBuyProgressDrawables()
        resetSaleProgressDrawables()
    }

    private fun resetBuyProgressDrawables() {
        resetProgressDrawable(binding.handicapBuyLayout01, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout02, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout03, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout04, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout05, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout06, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout07, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout08, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout09, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout10, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout11, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout12, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout13, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout14, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout15, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout16, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout17, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout18, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout19, colorT7A10, colorTransparent)
        resetProgressDrawable(binding.handicapBuyLayout20, colorT7A10, colorTransparent)
    }

    private fun resetSaleProgressDrawables() {
        resetProgressDrawable(binding.handicapSaleLayout01, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout02, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout03, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout04, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout05, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout06, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout07, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout08, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout09, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout10, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout11, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout12, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout13, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout14, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout15, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout16, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout17, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout18, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout19, colorT5A10, colorTransparent)
        resetProgressDrawable(binding.handicapSaleLayout20, colorT5A10, colorTransparent)
    }

    private fun resetProgressDrawable(view: View?, progressColor: Int, bgColor: Int) {
        if (view == null) {
            return
        }
        val drawable = view.background
        if (drawable is ProgressDrawable) {
            drawable.setColor(progressColor, bgColor)
        }
    }

    private fun showCurrentStyle() {
        when (currentStyle) {
            STYLE_BID_ALL -> binding.displayType.setImageDrawable(SkinCompatResources.getDrawable(context, R.drawable.icon_trade_deep_02))
            STYLE_ASK_ALL -> binding.displayType.setImageDrawable(SkinCompatResources.getDrawable(context, R.drawable.icon_trade_deep_03))
            else -> binding.displayType.setImageDrawable(SkinCompatResources.getDrawable(context, R.drawable.icon_trade_deep))
        }
    }

    private fun showStyleLayout() {
        val defaultPadding = context.resources.getDimensionPixelSize(R.dimen.default_padding)
        when (currentStyle) {
            STYLE_BID_ALL -> {
                viewModel.bidMax = 20
                viewModel.askMax = 0
                binding.currentPriceLayout.setPadding(0, 0, 0, defaultPadding)
                binding.handicapBuyLayout01.visibility = View.VISIBLE
                binding.handicapBuyLayout02.visibility = View.VISIBLE
                binding.handicapBuyLayout03.visibility = View.VISIBLE
                binding.handicapBuyLayout04.visibility = View.VISIBLE
                binding.handicapBuyLayout05.visibility = View.VISIBLE
                binding.handicapBuyLayout06.visibility = View.VISIBLE
                binding.handicapBuyLayout07.visibility = View.VISIBLE
                binding.handicapBuyLayout08.visibility = View.VISIBLE
                binding.handicapBuyLayout09.visibility = View.VISIBLE
                binding.handicapBuyLayout10.visibility = View.VISIBLE
                binding.handicapBuyLayout11.visibility = View.VISIBLE
                binding.handicapBuyLayout12.visibility = View.VISIBLE
                binding.handicapBuyLayout13.visibility = View.VISIBLE
                binding.handicapBuyLayout14.visibility = View.VISIBLE
                binding.handicapBuyLayout15.visibility = View.VISIBLE
                binding.handicapBuyLayout16.visibility = View.VISIBLE
                binding.handicapBuyLayout17.visibility = View.VISIBLE
                binding.handicapBuyLayout18.visibility = View.VISIBLE
                binding.handicapBuyLayout19.visibility = View.VISIBLE
                binding.handicapBuyLayout20.visibility = View.VISIBLE
                binding.handicapSaleLayout01.visibility = View.GONE
                binding.handicapSaleLayout02.visibility = View.GONE
                binding.handicapSaleLayout03.visibility = View.GONE
                binding.handicapSaleLayout04.visibility = View.GONE
                binding.handicapSaleLayout05.visibility = View.GONE
                binding.handicapSaleLayout06.visibility = View.GONE
                binding.handicapSaleLayout07.visibility = View.GONE
                binding.handicapSaleLayout08.visibility = View.GONE
                binding.handicapSaleLayout09.visibility = View.GONE
                binding.handicapSaleLayout10.visibility = View.GONE
                binding.handicapSaleLayout11.visibility = View.GONE
                binding.handicapSaleLayout12.visibility = View.GONE
                binding.handicapSaleLayout13.visibility = View.GONE
                binding.handicapSaleLayout14.visibility = View.GONE
                binding.handicapSaleLayout15.visibility = View.GONE
                binding.handicapSaleLayout16.visibility = View.GONE
                binding.handicapSaleLayout17.visibility = View.GONE
                binding.handicapSaleLayout18.visibility = View.GONE
                binding.handicapSaleLayout19.visibility = View.GONE
                binding.handicapSaleLayout20.visibility = View.GONE
            }
            STYLE_ASK_ALL -> {
                viewModel.bidMax = 0
                viewModel.askMax = 20
                binding.currentPriceLayout.setPadding(0, defaultPadding, 0, 0)
                binding.handicapBuyLayout01.visibility = View.GONE
                binding.handicapBuyLayout02.visibility = View.GONE
                binding.handicapBuyLayout03.visibility = View.GONE
                binding.handicapBuyLayout04.visibility = View.GONE
                binding.handicapBuyLayout05.visibility = View.GONE
                binding.handicapBuyLayout06.visibility = View.GONE
                binding.handicapBuyLayout07.visibility = View.GONE
                binding.handicapBuyLayout08.visibility = View.GONE
                binding.handicapBuyLayout09.visibility = View.GONE
                binding.handicapBuyLayout10.visibility = View.GONE
                binding.handicapBuyLayout11.visibility = View.GONE
                binding.handicapBuyLayout12.visibility = View.GONE
                binding.handicapBuyLayout13.visibility = View.GONE
                binding.handicapBuyLayout14.visibility = View.GONE
                binding.handicapBuyLayout15.visibility = View.GONE
                binding.handicapBuyLayout16.visibility = View.GONE
                binding.handicapBuyLayout17.visibility = View.GONE
                binding.handicapBuyLayout18.visibility = View.GONE
                binding.handicapBuyLayout19.visibility = View.GONE
                binding.handicapBuyLayout20.visibility = View.GONE
                binding.handicapSaleLayout01.visibility = View.VISIBLE
                binding.handicapSaleLayout02.visibility = View.VISIBLE
                binding.handicapSaleLayout03.visibility = View.VISIBLE
                binding.handicapSaleLayout04.visibility = View.VISIBLE
                binding.handicapSaleLayout05.visibility = View.VISIBLE
                binding.handicapSaleLayout06.visibility = View.VISIBLE
                binding.handicapSaleLayout07.visibility = View.VISIBLE
                binding.handicapSaleLayout08.visibility = View.VISIBLE
                binding.handicapSaleLayout09.visibility = View.VISIBLE
                binding.handicapSaleLayout10.visibility = View.VISIBLE
                binding.handicapSaleLayout11.visibility = View.VISIBLE
                binding.handicapSaleLayout12.visibility = View.VISIBLE
                binding.handicapSaleLayout13.visibility = View.VISIBLE
                binding.handicapSaleLayout14.visibility = View.VISIBLE
                binding.handicapSaleLayout15.visibility = View.VISIBLE
                binding.handicapSaleLayout16.visibility = View.VISIBLE
                binding.handicapSaleLayout17.visibility = View.VISIBLE
                binding.handicapSaleLayout18.visibility = View.VISIBLE
                binding.handicapSaleLayout19.visibility = View.VISIBLE
                binding.handicapSaleLayout20.visibility = View.VISIBLE
            }
            else -> {
                viewModel.bidMax = 10
                viewModel.askMax = 10
                binding.currentPriceLayout.setPadding(0, defaultPadding, 0, defaultPadding)
                binding.handicapBuyLayout01.visibility = View.VISIBLE
                binding.handicapBuyLayout02.visibility = View.VISIBLE
                binding.handicapBuyLayout03.visibility = View.VISIBLE
                binding.handicapBuyLayout04.visibility = View.VISIBLE
                binding.handicapBuyLayout05.visibility = View.VISIBLE
                binding.handicapBuyLayout06.visibility = View.VISIBLE
                binding.handicapBuyLayout07.visibility = View.VISIBLE
                binding.handicapBuyLayout08.visibility = View.VISIBLE
                binding.handicapBuyLayout09.visibility = View.VISIBLE
                binding.handicapBuyLayout10.visibility = View.VISIBLE
                binding.handicapBuyLayout11.visibility = View.GONE
                binding.handicapBuyLayout12.visibility = View.GONE
                binding.handicapBuyLayout13.visibility = View.GONE
                binding.handicapBuyLayout14.visibility = View.GONE
                binding.handicapBuyLayout15.visibility = View.GONE
                binding.handicapBuyLayout16.visibility = View.GONE
                binding.handicapBuyLayout17.visibility = View.GONE
                binding.handicapBuyLayout18.visibility = View.GONE
                binding.handicapBuyLayout19.visibility = View.GONE
                binding.handicapBuyLayout20.visibility = View.GONE
                binding.handicapSaleLayout01.visibility = View.VISIBLE
                binding.handicapSaleLayout02.visibility = View.VISIBLE
                binding.handicapSaleLayout03.visibility = View.VISIBLE
                binding.handicapSaleLayout04.visibility = View.VISIBLE
                binding.handicapSaleLayout05.visibility = View.VISIBLE
                binding.handicapSaleLayout06.visibility = View.VISIBLE
                binding.handicapSaleLayout07.visibility = View.VISIBLE
                binding.handicapSaleLayout08.visibility = View.VISIBLE
                binding.handicapSaleLayout09.visibility = View.VISIBLE
                binding.handicapSaleLayout10.visibility = View.VISIBLE
                binding.handicapSaleLayout11.visibility = View.GONE
                binding.handicapSaleLayout12.visibility = View.GONE
                binding.handicapSaleLayout13.visibility = View.GONE
                binding.handicapSaleLayout14.visibility = View.GONE
                binding.handicapSaleLayout15.visibility = View.GONE
                binding.handicapSaleLayout16.visibility = View.GONE
                binding.handicapSaleLayout17.visibility = View.GONE
                binding.handicapSaleLayout18.visibility = View.GONE
                binding.handicapSaleLayout19.visibility = View.GONE
                binding.handicapSaleLayout20.visibility = View.GONE
            }
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

    //显示卖出订单
    fun showASKTradeOrders(pair: String?, orderData: List<TradeOrder?>?) {
        var data = orderData
        if (!TextUtils.equals(pair, viewModel.getCurrentPair())) { //如果刷新时交易对信息发生了改变，停止刷新界面
            return
        }
        if (data == null || data.isEmpty()) {
            data = ArrayList()
        }
        FryingUtil.computeTradeOrderWeightPercent(data, viewModel.askMax)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 0), binding.handicapSaleLayout01, binding.priceSale01, binding.countSale01)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 1), binding.handicapSaleLayout02, binding.priceSale02, binding.countSale02)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 2), binding.handicapSaleLayout03, binding.priceSale03, binding.countSale03)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 3), binding.handicapSaleLayout04, binding.priceSale04, binding.countSale04)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 4), binding.handicapSaleLayout05, binding.priceSale05, binding.countSale05)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 5), binding.handicapSaleLayout06, binding.priceSale06, binding.countSale06)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 6), binding.handicapSaleLayout07, binding.priceSale07, binding.countSale07)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 7), binding.handicapSaleLayout08, binding.priceSale08, binding.countSale08)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 8), binding.handicapSaleLayout09, binding.priceSale09, binding.countSale09)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 9), binding.handicapSaleLayout10, binding.priceSale10, binding.countSale10)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 10), binding.handicapSaleLayout11, binding.priceSale11, binding.countSale11)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 11), binding.handicapSaleLayout12, binding.priceSale12, binding.countSale12)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 12), binding.handicapSaleLayout13, binding.priceSale13, binding.countSale13)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 13), binding.handicapSaleLayout14, binding.priceSale14, binding.countSale14)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 14), binding.handicapSaleLayout15, binding.priceSale15, binding.countSale15)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 15), binding.handicapSaleLayout16, binding.priceSale16, binding.countSale16)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 16), binding.handicapSaleLayout17, binding.priceSale17, binding.countSale17)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 17), binding.handicapSaleLayout18, binding.priceSale18, binding.countSale18)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 18), binding.handicapSaleLayout19, binding.priceSale19, binding.countSale19)
        showAskOrder(pair,CommonUtil.getItemFromList(data, 19), binding.handicapSaleLayout20, binding.priceSale20, binding.countSale20)
    }

    private fun showAskOrder(pair: String?,tradeOrder: TradeOrder?, layout: View, priceView: TextView, countView: TextView) {
        setOrderBg(layout, tradeOrder)
        layout.tag = tradeOrder
        priceView.text = if (tradeOrder == null) nullAmount else tradeOrder.formattedPrice
        countView.text = if (tradeOrder == null) nullAmount else  NumberUtil.formatNumberDynamicScaleNoGroup(tradeOrder.exchangeAmount, 10, viewModel.getAmountLength(), viewModel.getAmountLength())
    }

    //清空显示卖出订单
    fun clearASKTradeOrders() {
        showASKTradeOrders(viewModel.getCurrentPair(), null)
        setOrderBg(binding.handicapSaleLayout01, null)
        setOrderBg(binding.handicapSaleLayout02, null)
        setOrderBg(binding.handicapSaleLayout03, null)
        setOrderBg(binding.handicapSaleLayout04, null)
        setOrderBg(binding.handicapSaleLayout05, null)
        setOrderBg(binding.handicapSaleLayout06, null)
        setOrderBg(binding.handicapSaleLayout07, null)
        setOrderBg(binding.handicapSaleLayout08, null)
        setOrderBg(binding.handicapSaleLayout09, null)
        setOrderBg(binding.handicapSaleLayout10, null)
        setOrderBg(binding.handicapSaleLayout11, null)
        setOrderBg(binding.handicapSaleLayout12, null)
        setOrderBg(binding.handicapSaleLayout13, null)
        setOrderBg(binding.handicapSaleLayout14, null)
        setOrderBg(binding.handicapSaleLayout15, null)
        setOrderBg(binding.handicapSaleLayout16, null)
        setOrderBg(binding.handicapSaleLayout17, null)
        setOrderBg(binding.handicapSaleLayout18, null)
        setOrderBg(binding.handicapSaleLayout19, null)
        setOrderBg(binding.handicapSaleLayout20, null)
    }

    //显示买入订单
    fun showBIDTradeOrders(pair: String?, orderData: List<TradeOrder?>?) {
        var data = orderData
        if (!TextUtils.equals(pair, viewModel.getCurrentPair())) { //如果刷新时交易对信息发生了改变，停止刷新界面
            return
        }
        if (data == null || data.isEmpty()) {
            data = ArrayList()
        }
        FryingUtil.computeTradeOrderWeightPercent(data, viewModel.bidMax)
        showBidOrder(CommonUtil.getItemFromList(data, 0), binding.handicapBuyLayout01, binding.priceBuy01, binding.countBuy01)
        showBidOrder(CommonUtil.getItemFromList(data, 1), binding.handicapBuyLayout02, binding.priceBuy02, binding.countBuy02)
        showBidOrder(CommonUtil.getItemFromList(data, 2), binding.handicapBuyLayout03, binding.priceBuy03, binding.countBuy03)
        showBidOrder(CommonUtil.getItemFromList(data, 3), binding.handicapBuyLayout04, binding.priceBuy04, binding.countBuy04)
        showBidOrder(CommonUtil.getItemFromList(data, 4), binding.handicapBuyLayout05, binding.priceBuy05, binding.countBuy05)
        showBidOrder(CommonUtil.getItemFromList(data, 5), binding.handicapBuyLayout06, binding.priceBuy06, binding.countBuy06)
        showBidOrder(CommonUtil.getItemFromList(data, 6), binding.handicapBuyLayout07, binding.priceBuy07, binding.countBuy07)
        showBidOrder(CommonUtil.getItemFromList(data, 7), binding.handicapBuyLayout08, binding.priceBuy08, binding.countBuy08)
        showBidOrder(CommonUtil.getItemFromList(data, 8), binding.handicapBuyLayout09, binding.priceBuy09, binding.countBuy09)
        showBidOrder(CommonUtil.getItemFromList(data, 9), binding.handicapBuyLayout10, binding.priceBuy10, binding.countBuy10)
        showBidOrder(CommonUtil.getItemFromList(data, 10), binding.handicapBuyLayout11, binding.priceBuy11, binding.countBuy11)
        showBidOrder(CommonUtil.getItemFromList(data, 11), binding.handicapBuyLayout12, binding.priceBuy12, binding.countBuy12)
        showBidOrder(CommonUtil.getItemFromList(data, 12), binding.handicapBuyLayout13, binding.priceBuy13, binding.countBuy13)
        showBidOrder(CommonUtil.getItemFromList(data, 13), binding.handicapBuyLayout14, binding.priceBuy14, binding.countBuy14)
        showBidOrder(CommonUtil.getItemFromList(data, 14), binding.handicapBuyLayout15, binding.priceBuy15, binding.countBuy15)
        showBidOrder(CommonUtil.getItemFromList(data, 15), binding.handicapBuyLayout16, binding.priceBuy16, binding.countBuy16)
        showBidOrder(CommonUtil.getItemFromList(data, 16), binding.handicapBuyLayout17, binding.priceBuy17, binding.countBuy17)
        showBidOrder(CommonUtil.getItemFromList(data, 17), binding.handicapBuyLayout18, binding.priceBuy18, binding.countBuy18)
        showBidOrder(CommonUtil.getItemFromList(data, 18), binding.handicapBuyLayout19, binding.priceBuy19, binding.countBuy19)
        showBidOrder(CommonUtil.getItemFromList(data, 19), binding.handicapBuyLayout20, binding.priceBuy20, binding.countBuy20)
    }

    private fun showBidOrder(tradeOrder: TradeOrder?, layout: View, priceView: TextView, countView: TextView) {
        setOrderBg(layout, tradeOrder)
        layout.tag = tradeOrder
        priceView.text = if (tradeOrder == null) nullAmount else tradeOrder.formattedPrice
        countView.text = if (tradeOrder == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(tradeOrder.exchangeAmount, 10, viewModel.getAmountLength(), viewModel.getAmountLength())
    }

    //清空显示买入订单
    fun clearBIDTradeOrders() {
        showBIDTradeOrders(viewModel.getCurrentPair(), null)
        setOrderBg(binding.handicapBuyLayout01, null)
        setOrderBg(binding.handicapBuyLayout02, null)
        setOrderBg(binding.handicapBuyLayout03, null)
        setOrderBg(binding.handicapBuyLayout04, null)
        setOrderBg(binding.handicapBuyLayout05, null)
        setOrderBg(binding.handicapBuyLayout06, null)
        setOrderBg(binding.handicapBuyLayout07, null)
        setOrderBg(binding.handicapBuyLayout08, null)
        setOrderBg(binding.handicapBuyLayout09, null)
        setOrderBg(binding.handicapBuyLayout10, null)
        setOrderBg(binding.handicapBuyLayout11, null)
        setOrderBg(binding.handicapBuyLayout12, null)
        setOrderBg(binding.handicapBuyLayout13, null)
        setOrderBg(binding.handicapBuyLayout14, null)
        setOrderBg(binding.handicapBuyLayout15, null)
        setOrderBg(binding.handicapBuyLayout16, null)
        setOrderBg(binding.handicapBuyLayout17, null)
        setOrderBg(binding.handicapBuyLayout18, null)
        setOrderBg(binding.handicapBuyLayout19, null)
        setOrderBg(binding.handicapBuyLayout20, null)
    }

    interface OnTransactionDeepListener {
        fun onTradeOrderFastClick(tradeOrder: TradeOrder)
        fun onDeepChanged(deep: Deep)
    }

    init {
        colorT7A10 = SkinCompatResources.getColor(context, R.color.T7_ALPHA10)
        colorT5A10 = SkinCompatResources.getColor(context, R.color.T5_ALPHA10)
        colorTransparent = SkinCompatResources.getColor(context, R.color.transparent)
        nullAmount = context.resources.getString(R.string.number_default)
    }
}