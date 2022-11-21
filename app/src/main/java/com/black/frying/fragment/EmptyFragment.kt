package com.black.frying.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.black.base.fragment.BaseFragment
import com.black.base.widget.AutoHeightViewPager
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentEmptyBinding

class EmptyFragment : BaseFragment() {
    var params:String? = null
    var layout: FrameLayout? = null
    var binding: FragmentEmptyBinding? = null
    var mViewPager:AutoHeightViewPager? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_empty, container, false)
        layout = binding?.root as FrameLayout
        if(arguments != null){
            val position = arguments!!.getInt(AutoHeightViewPager.POSITION)
            mViewPager?.setViewPosition(layout!!, position)
        }
        return layout
    }

    /**
     * adapter height when viewpager in scrollview
     */
    fun setAutoHeightViewPager(viewPager: AutoHeightViewPager?){
        mViewPager = viewPager
    }

    companion object {
        fun newInstance(params: String?): EmptyFragment {
            val args = Bundle()
            val fragment = EmptyFragment()
            fragment.arguments = args
            fragment.params = params
            return fragment
        }
    }
}