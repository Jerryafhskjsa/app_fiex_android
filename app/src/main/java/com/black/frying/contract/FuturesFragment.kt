package com.black.frying.contract

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.whenResumed
import com.black.base.util.StyleChangeUtil
import com.black.frying.contract.viewmodel.FuturesTitleViewModel
import com.black.frying.contract.viewmodel.FuturesViewModel
import com.black.net.okhttp.OkWebSocket
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesBinding
import java.math.BigDecimal

class FuturesFragment : Fragment() {

    companion object {
        fun newInstance() = FuturesFragment()
    }

    private val viewModel: FuturesViewModel by lazy { ViewModelProvider(this)[FuturesViewModel::class.java] }
    private val futuresTitleViewModel: FuturesTitleViewModel by lazy { ViewModelProvider(this)[FuturesTitleViewModel::class.java] }
    private val _binding: FragmentLayoutFuturesBinding by lazy {
        FragmentLayoutFuturesBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return _binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        futuresTitleViewModel.lifecycleOwner = viewLifecycleOwner
        futuresTitleViewModel.loadCoinInfo()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildFuturesTitleBar()
    }

    override fun onPause() {
        super.onPause()
        futuresTitleViewModel.onPause()
    }

    override fun onResume() {
        super.onResume()
        futuresTitleViewModel.onResume()
    }

    private fun buildFuturesTitleBar() {
        val futureTitleBar = _binding.futuresTitleBarRoot
        futureTitleBar.apply {
            futuresCollectCoin.setOnClickListener {
                futuresTitleViewModel.autoCollectCoin()
            }
            futuresTitleBar.setOnClickListener {
                // TODO: 选择币种
                futuresTitleViewModel.goToSelectOtherCoin(requireActivity())
            }
            futuresCoinCharts.setOnClickListener {
                //todo k线详情
                futuresTitleViewModel.goToKlineDetail(requireActivity())
            }
            futuresTransactionMore.setOnClickListener {
                //todo 更多设置
//                viewModel.startConnect()
            }
        }
        futuresTitleViewModel.coinInfo.observe(
            viewLifecycleOwner
        ) { coinInfo ->
            coinInfo?.apply {
                futureTitleBar.let { root ->
                    run {
                        root.futuresTitleBarTitle.text = coinName
                        updatePriceSince(priceSincePercent, root.futuresTitleBarPriceSince)
                        root.futuresCollectCoin.setImageResource(if (isCollect) R.drawable.btn_collect_dis else R.drawable.btn_collect_default)
                    }
                }
            }
        }
    }

    private fun updatePriceSince(since: BigDecimal, sincePriceTv: TextView) {
        val background: Drawable?
        val color: Int
        val ctx = sincePriceTv.context
        val styleChange = StyleChangeUtil.getStyleChangeSetting(ctx)?.styleCode
        if (styleChange == 1) {
            if (since > BigDecimal.ZERO) {//涨
                background = ctx.getDrawable(R.drawable.trans_raise_bg_corner)
                color = ctx.getColor(R.color.T10)
            } else if (since < BigDecimal.ZERO) {
                background = ctx.getDrawable(R.drawable.trans_fall_bg_corner)
                color = ctx.getColor(R.color.T9)
            } else {
                background = ctx.getDrawable(R.drawable.trans_default_bg_corner)
                color = ctx.getColor(R.color.B3)
            }

        } else {
            if (since < BigDecimal.ZERO) {//跌
                background = ctx?.getDrawable(R.drawable.trans_raise_bg_corner)
                color = ctx.getColor(R.color.T10)
            } else if (since > BigDecimal.ZERO) {
                background = ctx?.getDrawable(R.drawable.trans_fall_bg_corner)
                color = ctx.getColor(R.color.T9)
            } else {
                background = ctx?.getDrawable(R.drawable.trans_default_bg_corner)
                color = ctx.getColor(R.color.B3)
            }
        }
        val result = NumberUtil.formatNumber2(since) + "%"
        sincePriceTv.text = result
        sincePriceTv.background = background
        sincePriceTv.setTextColor(color)
    }

}