package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.viewmodel.FuturesTransactionInfoDisplayViewModel
import com.fbsex.exchange.R

class FuturesTransactionInfoDisplayFragment : Fragment() {

    companion object {
        const val TAG = "FuturesTransactionInfoDisplayFragment"
        fun newInstance() = FuturesTransactionInfoDisplayFragment()
    }

    private lateinit var viewModel: FuturesTransactionInfoDisplayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_layout_futures_transaction_info_display,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FuturesTransactionInfoDisplayViewModel::class.java)
        // TODO: Use the ViewModel
    }

}