package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.biz.view.FuturesMultipleSettingDialog
import com.black.frying.contract.utils.replaceTransactionFragment
import com.black.frying.contract.viewmodel.FuturesTransactionInfoViewModel
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

    private lateinit var viewModel: FuturesTransactionInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FuturesTransactionInfoViewModel::class.java)
        // TODO: Use the ViewModel
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
            FuturesTransactionInfoDisplayFragment.newInstance(),
            FuturesTransactionInfoDisplayFragment.TAG
        )
        binding.futuresMultipleSettingView.apply {

            getMuchBtn().setOnClickListener {
                val settingDialog = FuturesMultipleSettingDialog(context)
                settingDialog.setCancelable(true)
                settingDialog.show()
            }
        }

    }

}