package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.biz.view.FuturesMultipleSettingDialog
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.utils.replaceTransactionFragment
import com.black.frying.contract.viewmodel.FuturesTransactionInfoViewModel
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesTransactionInfoBinding

class FuturesTransactionInfoFragment : Fragment() {
    val binding: FragmentLayoutFuturesTransactionInfoBinding by lazy {
        FragmentLayoutFuturesTransactionInfoBinding.inflate(
            layoutInflater
        )
    }


    companion object {
        const val TAG = "FuturesTransactionInfoFragment"
        fun newInstance() = FuturesTransactionInfoFragment()
    }

    private val viewModel: FuturesTransactionInfoViewModel by lazy {
        ViewModelProvider(this).get(
            FuturesTransactionInfoViewModel::class.java
        )
    }
    private val globalViewModel: FutureGlobalStateViewModel by lazy { ViewModelProvider(requireActivity())[FutureGlobalStateViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceTransactionFragment(
            R.id.futureCreateOrderLayout,
            FuturesOrderCreateFragment.newInstance(),
            FuturesOrderCreateFragment.TAG
        )
        replaceTransactionFragment(
            R.id.futureDisplayDeepGraph,
            FuturesDeepGraphFragment.newInstance(),
            FuturesDeepGraphFragment.TAG
        )
        binding.futuresAccountAndRate.let {
            it.getAccountTotalProfitTitle().setOnClickListener {
//                viewModel.testApi()
//                viewModel.testAPiSuspend()
                globalViewModel.printThis()

            }
        }
        binding.futuresMultipleSettingView.apply {
            getMuchBtn().setOnClickListener {
                val settingDialog = FuturesMultipleSettingDialog(context)
                settingDialog.setCancelable(true)
                settingDialog.show()
            }
        }

        viewModel.userBalanceDto.observe(
            requireActivity()
        ) { userBalanceDto ->
            val value = NumberUtil.toBigDecimal(userBalanceDto.walletBalance)
            binding.futuresAccountAndRate.getAccountTotalTv().text = value.toString()
            binding.futuresAccountAndRate.getCoinRateTv().text = userBalanceDto.r
        }

    }

}