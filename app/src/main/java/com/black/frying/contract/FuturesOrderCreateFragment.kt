package com.black.frying.contract

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.black.base.view.DeepControllerWindow
import com.black.frying.contract.biz.view.OnInputChange
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel.Companion.ORDER_TYPE_LIMIT
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel.Companion.ORDER_TYPE_MARKET
import com.black.util.NumberUtils
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesOrderCreateBinding
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar

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
            it.btnSell.setOnClickListener {
                viewModel.changeOrderType(false)
            }
            it.futurePriceType.setOnClickListener {
                viewModel.performClickChangePriceType(requireActivity())
            }
            it.withLinCbWithLimitType.setOnClickListener {
                //show spanner  change
                _performSelectTimeInforce()
            }
            it.contractWithLimit.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.performClickShowLimitInput(isChecked)
            }
            it.btnActionBuy.setOnClickListener {
                viewModel.closePosition()
            }
            it.btnActionSell.setOnClickListener {
                viewModel.openPosition()
            }
            it.futurePriceInput.onInputChange= object : OnInputChange{
                override fun onAdd(price: String) {
                }
                override fun onSub(price: String) {
                }

                override fun onInput(price: String) {
                    viewModel.calculateFuturesInfo(price)
                }

            }
            it.futurePriceAmountInput.onInputChange = object :OnInputChange{
                override fun onAdd(number: String) {

                }

                override fun onSub(number: String) {
                }

                override fun onInput(number: String) {
                    viewModel.performInputNumber(number)
                }
            }
            it.futureNumberSeekbar.onSeekChangeListener = object :OnSeekChangeListener{
                override fun onSeeking(seekParams: SeekParams?) {
                    seekParams?.let {params ->
                        Log.d(TAG, "onSeeking() called ${params.progress}")
                        viewModel.performInputNumberPercent(params.progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: TickSeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: TickSeekBar?) {
                }

            }
        }

        globalViewModel.pricePrecision.observe(viewLifecycleOwner) { pricePrecision ->
            binding?.futurePriceInput?.precision = pricePrecision
        }
        globalViewModel.amountPrecision.observe(viewLifecycleOwner) { pricePrecision ->
            binding?.futurePriceAmountInput?.precision = pricePrecision
            binding?.futurePriceAmountInput?.setHint(context!!.getString(R.string.amount))
        }
        viewModel.buyOrSell.observe(viewLifecycleOwner) { isBuy ->
            binding?.btnBuy?.isChecked = isBuy
            binding?.btnSell?.isChecked = !isBuy
        }

        viewModel.orderType.observe(viewLifecycleOwner) { priceType ->
            val isLimit = priceType == ORDER_TYPE_LIMIT
            val isMarket = priceType == ORDER_TYPE_MARKET
            if(isLimit){
                binding?.futurePriceInput?.setEnable(true)
                val currentPrice = globalViewModel.pairQuotationLiveData.value
                binding?.futurePriceInput?.setText(currentPrice?.c?:"")
            }
            if (isMarket){
                binding?.futurePriceInput?.setText("")
                binding?.futurePriceInput?.setEnable(false)
            }
            binding?.futurePriceType?.changeDisplay(
                if (isLimit) {
                    getString(R.string.order_type_limit)
                } else {
                    if (isMarket) {
                        getString(R.string.order_type_market)
                    } else {
                        priceType
                    }
                }
            )
        }

        viewModel.showLimitPrice.observe(viewLifecycleOwner) { show ->
            binding?.contractWithLimit?.isChecked = show
            if (show) {
                binding?.linStopValue?.visibility = View.VISIBLE
            } else {
                binding?.linStopValue?.visibility = View.GONE
            }
        }
        viewModel.timeInForce.observe(viewLifecycleOwner) { timeInForce ->
            binding?.withLinCbWithLimitType?.text = timeInForce
        }
        viewModel.futureAmountPercent.observe(viewLifecycleOwner){percent ->
            binding?.futurePriceAmountInput?.setText("$percent%");
        }
        viewModel.futureAvailableOpenData.observe(viewLifecycleOwner){availableOpenInfo ->

            val pricePrecision = globalViewModel.pricePrecision.value?:0
            binding?.useable?.text =  NumberUtils.formatRoundDown(availableOpenInfo?.longMaxOpen,0,pricePrecision)
            binding?.useableUnit?.text = "USDT"
            binding?.marginAmount?.text = NumberUtils.formatRoundDown(availableOpenInfo?.longMargin,0,pricePrecision)
            binding?.marginUnit?.text = "USDT"


            binding?.sellUseable?.text = NumberUtils.formatRoundDown(availableOpenInfo?.shortMaxOpen,0,pricePrecision)
            binding?.sellUseableUnit?.text = "USDT"
            binding?.sellBond?.text = NumberUtils.formatRoundDown(availableOpenInfo?.shortMargin,0,pricePrecision)
            binding?.sellBondUnit?.text = "USDT"
        }
    }

    private fun _performSelectTimeInforce() {
        val timeInforce = viewModel.timeInForce.value
        DeepControllerWindow(requireActivity(),
            getString(R.string.select_order_type),
            timeInforce,
            viewModel.getTimeInForceList(),
            object : DeepControllerWindow.OnReturnListener<String?> {
                override fun onReturn(
                    window: DeepControllerWindow<String?>,
                    item: String?
                ) {
//                    refreshTimeInForceType(item)
//                    currentTimeInForceType = item
//                    viewModel?.setCurrentTimeInForceType(item)
                    item?.let {
                        viewModel.selectTimeInForce(item)
                    }
                }
            }).show()
    }


}