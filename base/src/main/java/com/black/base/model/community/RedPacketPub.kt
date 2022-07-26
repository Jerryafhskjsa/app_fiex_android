package com.black.base.model.community

import android.os.Parcel
import android.os.Parcelable

class RedPacketPub : Parcelable {
    var id: String? = null
    var type: String? = null
    var coinType: String? = null
    var userId: String? = null
    var userName: String? = null
    var avatar: String? = null
    var title: String? = null
    var status // 0 初始值 1 已抢完 2 已过期
            : Int? = null
    var userIsGrabbed //
            : Boolean? = null

    constructor()
    constructor(redPacket: RedPacket) {
        id = redPacket.packetId
        title = redPacket.title
        userName = redPacket.sendName
        avatar = redPacket.sendAvatar
    }

    constructor(`in`: Parcel) {
        id = `in`.readString()
        type = `in`.readString()
        coinType = `in`.readString()
        userId = `in`.readString()
        userName = `in`.readString()
        avatar = `in`.readString()
        title = `in`.readString()
        status = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        val tmpUserIsGrabbed = `in`.readByte()
        userIsGrabbed = if (tmpUserIsGrabbed.toInt() == 0) null else tmpUserIsGrabbed.toInt() == 1
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(type)
        dest.writeString(coinType)
        dest.writeString(userId)
        dest.writeString(userName)
        dest.writeString(avatar)
        dest.writeString(title)
        if (status == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(status!!)
        }
        dest.writeByte((if (userIsGrabbed == null) 0 else if (userIsGrabbed!!) 1 else 2).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val NORMAL = "NORMAL"
        const val LUCKY = "LUCKY"
        @JvmField
        val CREATOR: Parcelable.Creator<RedPacketPub?> = object : Parcelable.Creator<RedPacketPub?> {
            override fun createFromParcel(`in`: Parcel): RedPacketPub? {
                return RedPacketPub(`in`)
            }

            override fun newArray(size: Int): Array<RedPacketPub?> {
                return arrayOfNulls(size)
            }
        }
    }
}