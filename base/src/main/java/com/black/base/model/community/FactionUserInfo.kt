package com.black.base.model.community

import android.os.Parcel
import android.os.Parcelable

class FactionUserInfo : Parcelable {
    var leagueId //null 未加⼊入该⻔门派
            : String? = null
    var userName: String? = null
    var type // 0普通 1掌⻔门
            : Int? = null
    var lockAmount //当前锁仓量量
            : Double? = null

    constructor()
    constructor(`in`: Parcel) {
        leagueId = `in`.readString()
        userName = `in`.readString()
        type = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        lockAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(leagueId)
        dest.writeString(userName)
        if (type == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(type!!)
        }
        if (lockAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(lockAmount!!)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    val isOwner: Boolean
        get() = leagueId != null && type != null && type == 1

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FactionUserInfo?> = object : Parcelable.Creator<FactionUserInfo?> {
            override fun createFromParcel(`in`: Parcel): FactionUserInfo? {
                return FactionUserInfo(`in`)
            }

            override fun newArray(size: Int): Array<FactionUserInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}
