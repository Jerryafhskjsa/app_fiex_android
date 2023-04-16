package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.base.model.future.Constants
import com.black.frying.contract.biz.view.FuturesMultipleSettingDialog
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.utils.replaceTransactionFragment
import com.black.frying.contract.viewmodel.FuturesTransactionInfoViewModel
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesTransactionInfoBinding
import java.math.RoundingMode

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
    private val globalViewModel: FutureGlobalStateViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        )[FutureGlobalStateViewModel::class.java]
    }

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
        globalViewModel.isolatedPositionBeanLiveData.observe(viewLifecycleOwner) { bean ->
            val positionSide = bean.positionSide ?: Constants.ISOLATED
            val leverage = bean.leverage ?: 10
            binding.futuresMultipleSettingView.setMuchText(formatLeverage(positionSide, leverage))
        }
        globalViewModel.crossedPositionBeanLiveData.observe(viewLifecycleOwner) { bean ->
            val positionSide = bean.positionSide ?: Constants.ISOLATED
            val leverage = bean.leverage ?: 10
            binding.futuresMultipleSettingView.setLessText(formatLeverage(positionSide, leverage))

        }
        globalViewModel.balanceBeanLiveData.observe(viewLifecycleOwner){bean ->
            binding.futuresAccountAndRate.getAccountTotalTv().text = bean.walletBalance
        }
        globalViewModel.fundRateBeanLiveData.observe(viewLifecycleOwner) { bean ->
            if (bean == null) {
                binding.futuresAccountAndRate.getCoinRateTv().text = "--/--"
            } else {
                val rate = NumberUtil.formatNumberNoGroup(
                    bean.r.toFloat().times(100),
                    RoundingMode.FLOOR,
                    4,
                    4
                ) + "%"
                binding.futuresAccountAndRate.getCoinRateTv().text = "$rate/${bean.t}"
            }
        }
        binding.futuresMultipleSettingView.apply {
            //开仓
            getMuchBtn().setOnClickListener {
                // TODO:  设置倍数
//                val settingDialog = FuturesMultipleSettingDialog(context)
//                settingDialog.setCancelable(true)
//                settingDialog.show()
            }
            //平仓
            getLessBtn().setOnClickListener {
                // TODO:  设置倍数
//                val settingDialog = FuturesMultipleSettingDialog(context)
//                settingDialog.setCancelable(true)
//                settingDialog.show()
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

    private fun formatLeverage(positionSide: String, leverage: Int): String {
        val des: String
        val typeDes: String = if (positionSide == Constants.ISOLATED) {
            getString(R.string.contract_fiexble_position)
        } else {
            getString(R.string.contract_all_position)
        }
        val multiDes: String = "" + leverage + "X"

        des = "$typeDes $multiDes"
        return des
    }

}