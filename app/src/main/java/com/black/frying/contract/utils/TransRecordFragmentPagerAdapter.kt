package com.black.frying.contract.utils

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.model.ContractRecordTabBean
import com.black.frying.fragment.ContractPlanTabFragment
import com.black.frying.fragment.ContractPositionTabFragment
import com.black.frying.fragment.ContractProfitTabFragment
import com.black.frying.fragment.EmptyFragment

class TransRecordFragmentPagerAdapter(private val listOfFragment :MutableList<Fragment>, fm: FragmentManager)  : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int {
        return listOfFragment.size
    }
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItem(position: Int): Fragment {
       return when (position) {
            0 -> listOfFragment.get(0)
            1 -> listOfFragment.get(1)
            2 -> listOfFragment.get(2)
            else -> EmptyFragment()
        }
    }
}