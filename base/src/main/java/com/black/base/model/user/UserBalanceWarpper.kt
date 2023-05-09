package com.black.base.model.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserBalanceWarpper : Parcelable {
    var spotBalance: ArrayList<UserBalance?>? = null//现货资金
    var tigerBalance: ArrayList<UserBalance?>? = null//合约资金
    var phaseRate: Double? = null
    var profitAmount: Double? = null
}
