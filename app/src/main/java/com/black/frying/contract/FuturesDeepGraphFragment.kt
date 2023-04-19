package com.black.frying.contract

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.black.base.model.socket.Deep
import com.black.base.model.socket.PairStatus
import com.black.base.model.socket.TradeOrder
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.view.DeepControllerWindow
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.viewmodel.FuturesTransactionInfoDisplayViewModel
import com.black.lib.view.ProgressDrawable
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FuturesLayoutDeepGraphBinding
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import skin.support.content.res.SkinCompatResources

class FuturesDeepGraphFragment : Fragment() {
    enum class ShowMode {
        DEFAULT,
        SELL,
        BUY;

        public fun ifDefault() = this == DEFAULT
    }

    val maxDoubleShowCount = 5

    companion object {
        const val TAG = "FuturesDeepGraphFragment"
        fun newInstance() = FuturesDeepGraphFragment()
    }

    private val viewModel: FuturesTransactionInfoDisplayViewModel by lazy {
        ViewModelProvider(this).get(FuturesTransactionInfoDisplayViewModel::class.java)
    }
    private val globalViewModel: FutureGlobalStateViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        )[FutureGlobalStateViewModel::class.java]
    }

    private val binding: FuturesLayoutDeepGraphBinding by lazy {
        FuturesLayoutDeepGraphBinding.inflate(
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

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalViewModel.printThis()
        setupBindView()

        setupBindData()
    }

    val nullAmount by lazy { context!!.resources.getString(R.string.number_default) }

    private val colorTransparent: Int by lazy {
        SkinCompatResources.getColor(
            context,
            R.color.transparent
        )
    }
    private val colorT7A10: Int by lazy {
        SkinCompatResources.getColor(
            context,
            R.color.T7_ALPHA10
        )
    }
    private val colorT5A10: Int by lazy {
        SkinCompatResources.getColor(
            context,
            R.color.T5_ALPHA10
        )
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        viewModel.init(context!!)
    }

    private fun setupBindView() {
        binding.apply {
            //set rv
            rvUpList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = object : CommonAdapter<TradeOrder>(
                    context,
                    R.layout.futures_layout_deep_graph_item,
                    viewModel.sellList
                ) {
                    override fun convert(
                        holder: ViewHolder?,
                        tradeOrder: TradeOrder,
                        position: Int
                    ) {
                        holder?.getView<ViewGroup>(R.id.rl_bg)?.let {
                            it.background = ProgressDrawable(
                                colorT5A10,
                                colorTransparent,
                                ProgressDrawable.RIGHT
                            )
                            setOrderBg(it,tradeOrder)
                        }

                        holder?.getView<TextView>(R.id.tv_price)?.let {
                            it.text =tradeOrder?.formattedPrice
                            it.setTextColor(it.context.getColor(R.color.T5))
                        }
                        holder?.getView<TextView>(R.id.tv_amount)?.let {
                            it.text =tradeOrder?.exchangeAmountFormat
                        }
                    }
                }
            }

            rvBtmList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = object : CommonAdapter<TradeOrder>(
                    context,
                    R.layout.futures_layout_deep_graph_item,
                    viewModel.buyList
                ) {
                    override fun convert(
                        holder: ViewHolder?,
                        tradeOrder: TradeOrder,
                        position: Int
                    ) {
                        holder?.getView<ViewGroup>(R.id.rl_bg)?.let {
                            it.background = ProgressDrawable(
                                colorT7A10,
                                colorTransparent,
                                ProgressDrawable.RIGHT
                            )

                            setOrderBg(it,tradeOrder)
                        }

                        holder?.getView<TextView>(R.id.tv_price)?.let {
                            it.text =tradeOrder?.formattedPrice
                        }
                        holder?.getView<TextView>(R.id.tv_amount)?.let {
                            it.text =tradeOrder?.exchangeAmountFormat
                        }
                    }
                }
            }

            displayType.setOnClickListener {
                viewModel.clickShowMode()
            }

            deep.setOnClickListener {
                if (!viewModel.getPrecisionList().isNullOrEmpty()) {
                    DeepControllerWindow<Deep>(requireActivity(), null, viewModel.getPrecisionDeep(viewModel.getPrecision()), viewModel.getPrecisionList() as List<Deep>,
                        object : DeepControllerWindow.OnReturnListener<Deep> {
                            override fun onReturn(window: DeepControllerWindow<Deep>, item: Deep) {
                                item.precision?.let { viewModel.setPrecision(it) }

                                refreshDeepView()
                            }
                        }).show()
                }
            }
        }
    }
    private fun refreshDeepView() {
        var deep = viewModel!!.getPrecisionDeep(viewModel!!.getPrecision())
        binding!!.deep.setText(
            getString(
                R.string.point_count,
                deep?.deep ?: ""
            )
        )
    }
    private fun setupBindData() {
        globalViewModel.tradeOrderDepthLiveData.observe(viewLifecycleOwner) {
            val showCount = if (viewModel.showMode.value!!.ifDefault()) {
                maxDoubleShowCount
            } else maxDoubleShowCount * 2
            // trans
            val parseOrderDepthData =
                SocketDataContainer.parseOrderDepthData(context, ConstData.DEPTH_FUTURE_TYPE, it.apply {
                    this.a = a?.takeLast(showCount)?.toTypedArray()
                    this.b = b?.take(showCount)?.toTypedArray()
                })
            parseOrderDepthData?.apply {
                bidOrderList?.let {
                    viewModel.buyList.apply {
                        clear()
                        addAll(it.toList())
                        FryingUtil.computeTradeOrderWeightPercent(this, showCount)
                        formatShowPrice()
                    }
                    binding.rvBtmList.adapter?.notifyDataSetChanged()
                }

                askOrderList?.let {
                    viewModel.sellList.apply {
                        clear()
                        FryingUtil.computeTradeOrderWeightPercent(it, showCount)
                        addAll(it.reversed().toList())
                        formatShowPrice()
                    }
                    binding.rvUpList.adapter?.notifyDataSetChanged()
                }
            }
        }

        viewModel.showMode.observe(viewLifecycleOwner) {
            when (it) {
                ShowMode.DEFAULT -> {
                    binding.apply {
                        rvBtmList.visibility = View.VISIBLE
                        rvUpList.visibility = View.VISIBLE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep
                            )
                        )
                    }
                }
                ShowMode.SELL -> {
                    binding.apply {
                        rvUpList.visibility = View.VISIBLE
                        rvBtmList.visibility = View.GONE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep_03
                            )
                        )
                    }
                }
                ShowMode.BUY -> {
                    binding.apply {
                        rvUpList.visibility = View.GONE
                        rvBtmList.visibility = View.VISIBLE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep_02
                            )
                        )
                    }
                }
                else -> {}
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
    private fun ArrayList<TradeOrder?>.formatShowPrice() {
        forEach {
            it?.formattedPrice =
                if (it == null) nullAmount else NumberUtil.formatNumberNoGroup(
                    it.price,
                    viewModel.currentPairStatus.precision,
                    viewModel.currentPairStatus.precision
                )
            it?.exchangeAmountFormat =
                if (it == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(
                    it.exchangeAmount,
                    10,
                    viewModel.getAmountLength(),
                    viewModel.getAmountLength()
                )
        }
    }

}