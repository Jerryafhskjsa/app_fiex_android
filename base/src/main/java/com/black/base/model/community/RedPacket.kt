package com.black.base.model.community

import android.os.Parcel
import android.os.Parcelable

class RedPacket : Parcelable {
    var id: String? = null
    var sendName: String? = null
    var sendAvatar: String? = null
    var title: String? = null
    var packetId: String? = null
    var status // 0 初始值 1 已抢完 2 已过期
            : Int? = null

    constructor(`in`: Parcel) {
        id = `in`.readString()
        sendName = `in`.readString()
        sendAvatar = `in`.readString()
        title = `in`.readString()
        packetId = `in`.readString()
        status = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(sendName)
        dest.writeString(sendAvatar)
        dest.writeString(title)
        dest.writeString(packetId)
        dest.writeInt(status!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val NEW = 0
        const val IS_OPEN = 1
        const val IS_OVER = 2
        const val IS_OVER_TIME = 3
        @JvmField
        val CREATOR: Parcelable.Creator<RedPacket?> = object : Parcelable.Creator<RedPacket?> {
            override fun createFromParcel(`in`: Parcel): RedPacket? {
                return RedPacket(`in`)
            }

            override fun newArray(size: Int): Array<RedPacket?> {
                return arrayOfNulls(size)
            }
        }
    }
}