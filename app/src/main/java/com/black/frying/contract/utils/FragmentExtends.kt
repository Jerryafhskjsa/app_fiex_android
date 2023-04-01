package com.black.frying.contract.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.black.frying.contract.FuturesTransactionInfoFragment

fun Fragment.replaceTransactionFragment(
    @IdRes container: Int,
    transactionInfoFragment: FuturesTransactionInfoFragment
) {
    val beginTransaction = childFragmentManager.beginTransaction()
    beginTransaction.replace(
        container,
        transactionInfoFragment,
        FuturesTransactionInfoFragment.TAG
    )
    beginTransaction.commitAllowingStateLoss()
}