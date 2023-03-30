package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.viewmodel.FuturesTitleViewModel
import com.black.frying.contract.viewmodel.FuturesViewModel
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesBinding

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
        futuresTitleViewModel.loadCoinInfo()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildFuturesTitleBar()
    }

    private fun buildFuturesTitleBar() {
        val futureTitleBar = _binding.futuresTitleBarRoot
        futureTitleBar.apply {
            futuresCollectCoin.setOnClickListener {
                futuresTitleViewModel.autoCollectCoin()
            }
            futuresTitleBar.setOnClickListener {
                // TODO: 选择币种
            }
            futuresCoinCharts.setOnClickListener {
                //todo k线详情
            }
            futuresTransactionMore.setOnClickListener {
                //todo 更多设置
            }
        }
        futuresTitleViewModel.coinInfo.observe(
            viewLifecycleOwner
        ) { coinInfo ->
            coinInfo?.apply {
                futureTitleBar.let { root ->
                    run {
                        root.futuresTitleBarTitle.text = coinName
                        root.futuresTitleBarPriceSince.text = priceSincePercent
                        root.futuresCollectCoin.setImageResource(if (isCollect) R.drawable.btn_collect_dis else R.drawable.btn_collect_default)
                    }
                }
            }
        }
    }

}