package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.frying.contract.viewmodel.FuturesOrderCreateViewModel
import com.fbsex.exchange.R

class FuturesOrderCreateFragment : Fragment() {

    companion object {
        const val TAG = "FuturesOrderCreateFragment"
        fun newInstance() = FuturesOrderCreateFragment()
    }

    private lateinit var viewModel: FuturesOrderCreateViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_futures_order_create, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FuturesOrderCreateViewModel::class.java)
        // TODO: Use the ViewModel
    }

}