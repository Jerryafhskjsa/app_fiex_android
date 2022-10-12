package com.black.frying.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.black.base.fragment.BaseFragment
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentEmptyBinding

class EmptyFragment : BaseFragment() {
    var layout: FrameLayout? = null
    var binding: FragmentEmptyBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_empty, container, false)
        layout = binding?.root as FrameLayout
        return layout
    }
}