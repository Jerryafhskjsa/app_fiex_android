package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.viewmodel.FuturesTransactionInfoViewModel
import com.fbsex.exchange.R

class FuturesTransactionInfoFragment : Fragment() {

    companion object {
        const val TAG = "FuturesTransactionInfoFragment"
        fun newInstance() = FuturesTransactionInfoFragment()
    }

    private lateinit var viewModel: FuturesTransactionInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_futures_transaction_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FuturesTransactionInfoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}