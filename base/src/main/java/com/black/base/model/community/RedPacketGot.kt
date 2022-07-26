package com.black.base.model.community

import android.os.Parcel
import android.os.Parcelable
import android.text.Spanned

class RedPacketGot : Parcelable {
    var openerId: String? = null
    var ownerId: String? = null
    var text: Spanned? = null

    constructor()
    constructor(`in`: Parcel) {
        openerId = `in`.readString()
        ownerId = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(openerId)
        dest.writeString(ownerId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RedPacketGot?> = object : Parcelable.Creator<RedPacketGot?> {
            override fun createFromParcel(`in`: Parcel): RedPacketGot? {
                return RedPacketGot(`in`)
            }

            override fun newArray(size: Int): Array<RedPacketGot?> {
                return arrayOfNulls(size)
            }
        }
    }
}