package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class LoanConfig protected constructor(`in`: Parcel) : Parcelable {
    var coinType: String?
    var borrowCoinTypeList: ArrayList<LoanConfigSub?>?

    init {
        coinType = `in`.readString()
        borrowCoinTypeList = `in`.createTypedArrayList(LoanConfigSub.CREATOR)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        dest.writeTypedList(borrowCoinTypeList)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return coinType ?: ""
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LoanConfig?> = object : Parcelable.Creator<LoanConfig?> {
            override fun createFromParcel(`in`: Parcel): LoanConfig? {
                return LoanConfig(`in`)
            }

            override fun newArray(size: Int): Array<LoanConfig?> {
                return arrayOfNulls(size)
            }
        }
    }
}