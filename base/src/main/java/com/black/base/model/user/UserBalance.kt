package com.black.base.model.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserBalance : Parcelable {
    var coin: String? = null
    var balance: String? = null
    var freeze:String? = null
    var availableBalance:String? = null
    var estimatedTotalAmount:String? = null
    var estimatedCynAmount:String? = null
    var estimatedAvailableAmount:String? = null
    var estimatedCoinType:String? = null
}
