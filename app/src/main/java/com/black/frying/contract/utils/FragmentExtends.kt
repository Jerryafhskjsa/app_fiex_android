package com.black.frying.contract.utils

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

fun Fragment.replaceTransactionFragment(
    @IdRes container: Int,
    transactionInfoFragment: Fragment,
    tag:String
) {
    val beginTransaction = childFragmentManager.beginTransaction()
    beginTransaction.replace(
        container,
        transactionInfoFragment,
        tag
    )
    beginTransaction.commitAllowingStateLoss()
}