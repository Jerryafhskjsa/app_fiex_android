package com.black.base.model.community

import android.os.Parcel
import android.os.Parcelable

//聊天室检查结果
class ChatRoomEnable : Parcelable {
    var enable: Boolean? = null
    var message: String? = null

    constructor()
    constructor(`in`: Parcel) {
        enable = `in`.readByte().toInt() != 0
        message = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (enable!!) 1 else 0).toByte())
        dest.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ChatRoomEnable?> = object : Parcelable.Creator<ChatRoomEnable?> {
            override fun createFromParcel(`in`: Parcel): ChatRoomEnable? {
                return ChatRoomEnable(`in`)
            }

            override fun newArray(size: Int): Array<ChatRoomEnable?> {
                return arrayOfNulls(size)
            }
        }
    }
}