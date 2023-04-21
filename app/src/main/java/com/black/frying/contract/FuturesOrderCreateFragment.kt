package com.black.frying.contract

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.black.base.view.DeepControllerWindow
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel.Companion.ORDER_TYPE_LIMIT
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel.Companion.ORDER_TYPE_MARKET
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesOrderCreateBinding

class FuturesOrderCreateFragment : Fragment() {

    companion object {
        const val TAG = "FuturesOrderCreateFragment"
        fun newInstance() = FuturesOrderCreateFragment()
    }


    private val globalViewModel: FutureGlobalStateViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        ).get(FutureGlobalStateViewModel::class.java)
    }
    var binding: FragmentLayoutFuturesOrderCreateBinding? = null
    private val viewModel: FuturesOrderCreateViewModel by lazy {
        ViewModelProvider(this).get(
            FuturesOrderCreateViewModel::class.java
        ).apply {
            globalStateViewModel = globalViewModel
            start()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLayoutFuturesOrderCreateBinding.inflate(inflater)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        globalViewModel.indexPriceBeanLiveData.observe(viewLifecycleOwner, Observer { bean ->
            bean?.apply {
//                binding?.sampleTest?.text = bean.s+" - "+bean.t+" - "+bean.p
            }
        })

        binding?.let {
            it.btnBuy.setOnClickListener {
                viewModel.changeOrderType(true)
            }
            it.btnSale.setOnClickListener {
                viewModel.changeOrderType(false)
            }
            it.futurePriceType.setOnClickListener {
                viewModel.performClickChangePriceType(requireActivity())
            }
        }
        viewModel.buyOrSell.observe(viewLifecycleOwner) { isBuy ->
            binding?.btnBuy?.isChecked = isBuy
            binding?.btnSale?.isChecked = !isBuy
        }
        viewModel.orderType.observe(viewLifecycleOwner) { priceType ->
            binding?.futurePriceType?.changeDisplay(if(priceType == ORDER_TYPE_LIMIT){
                getString(R.string.order_type_limit)
            }else if(priceType == ORDER_TYPE_MARKET){
                getString(R.string.order_type_market)
            }else{
                priceType
            })
        }
    }


}