package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.black.base.model.socket.TradeOrder
import com.black.base.util.FryingUtil
import com.black.frying.contract.viewmodel.FuturesTransactionInfoDisplayViewModel
import com.black.lib.view.ProgressDrawable
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.databinding.FragmentLayoutFuturesTransactionInfoDisplayBinding
import java.util.ArrayList

class FuturesTransactionInfoDisplayFragment : Fragment(),
    FuturesTransactionInfoDisplayViewModel.OnContractModelListener {

    companion object {
        const val TAG = "FuturesTransactionInfoDisplayFragment"
        fun newInstance() = FuturesTransactionInfoDisplayFragment()
    }

    private lateinit var viewModel: FuturesTransactionInfoDisplayViewModel

    private val binding: FragmentLayoutFuturesTransactionInfoDisplayBinding by lazy {
        FragmentLayoutFuturesTransactionInfoDisplayBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FuturesTransactionInfoDisplayViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onTradeOrder(
        pair: String?,
        bidOrderList: List<TradeOrder?>?,
        askOrderList: List<TradeOrder?>?
    ) {
        CommonUtil.checkActivityAndRunOnUI(viewModel.context) {
           showBIDTradeOrders(pair, bidOrderList)
           showASKTradeOrders(pair, askOrderList)
        }
    }


    fun showBIDTradeOrders(pair: String?, orderData: List<TradeOrder?>?) {
       /* var data = orderData
        if (!TextUtils.equals(pair, viewModel.currentPair)) { //如果刷新时交易对信息发生了改变，停止刷新界面
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
    }

    private fun showAskOrder(pair: String?, tradeOrder: TradeOrder?, layout: View, priceView: TextView, countView: TextView) {
        setOrderBg(layout, tradeOrder)
        layout.tag = tradeOrder
        priceView.text = if (tradeOrder == null) nullAmount else tradeOrder.formattedPrice
        countView.text = if (tradeOrder == null) nullAmount else  NumberUtil.formatNumberDynamicScaleNoGroup(tradeOrder.exchangeAmount, 10, viewModel.getAmountLength(), viewModel.getAmountLength())
    }

    private fun setOrderBg(layout: View?, tradeOrder: TradeOrder?) {
        if (layout == null) {
            return
        }
        val background = layout.background
        if (background is ProgressDrawable) {
            val progress: Double = tradeOrder?.weightPercent ?: 0.toDouble()
            background.setProgress(progress)
        }*/
    }
    fun showASKTradeOrders(pair: String?, orderData: List<TradeOrder?>?) {

    }
}