package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.viewmodel.FuturesTransactionInfoDisplayViewModel
import com.fbsex.exchange.databinding.FragmentLayoutFuturesTransactionInfoDisplayBinding

class FuturesDeepGraphFragment : Fragment() {

    companion object {
        const val TAG = "FuturesDeepGraphFragment"
        fun newInstance() = FuturesDeepGraphFragment()
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

}