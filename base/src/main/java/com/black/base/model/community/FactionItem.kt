package com.black.base.model.community

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

//笑傲江湖门派
class FactionItem : Parcelable {
    var id: Long? = null
    var name: String? = null
    var leagueLogo: String? = null
    var ownerAvatar: String? = null
    var memberAvatar: String? = null
    var banner: String? = null
    var createTime: Long? = null
    var nextUnlockStartTime: Long? = null
    var nextUnlockEndTime: Long? = null
    var status // 0 正常不不可解锁 ，1 可解锁
            : Int? = null
    var ownerStatus // 0 ⽆无掌⻔门 ，1有掌⻔门， 2 竞选中，3 续任考虑中
            : Int? = null
    var nextOwnerStartTime //续任考虑开始时间
            : Long? = null
    var nextOwnerEndTime //续任考虑截⽌止时间
            : Long? = null
    var ownerPrice //续任/竞选 价格
            : Double? = null
    var nextOwnerChangeTime //掌⻔门竞选开始时间
            : Long? = null
    var memberCount: Int? = null
    var ownerCreateTime: Long? = null
    var nextOwnerChangeFinishedTime //掌⻔门竞选截⽌止时间
            : Long? = null
    var ownerLastRecordTime: Long? = null
    var ownerPriceStep: Double? = null
    var canLock //是否开启加⼊入
            : Int? = null
    var lastTempOwnerId //掌⻔门竞选⽬目前最⾼高价的userId
            : String? = null
    var thisTime: Long? = null
    var mobileBanner //移动端banner
            : String? = null
    var timRoomId //聊天室ID
            : String? = null

    constructor() {}
    protected constructor(`in`: Parcel) {
        id = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        name = `in`.readString()
        leagueLogo = `in`.readString()
        ownerAvatar = `in`.readString()
        memberAvatar = `in`.readString()
        banner = `in`.readString()
        createTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        nextUnlockStartTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        nextUnlockEndTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        status = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        ownerStatus = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        nextOwnerStartTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        nextOwnerEndTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        ownerPrice = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        nextOwnerChangeTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        memberCount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        ownerCreateTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        nextOwnerChangeFinishedTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        ownerLastRecordTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        ownerPriceStep = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        canLock = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        lastTempOwnerId = `in`.readString()
        thisTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        mobileBanner = `in`.readString()
        timRoomId = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (id == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(id!!)
        }
        dest.writeString(name)
        dest.writeString(leagueLogo)
        dest.writeString(ownerAvatar)
        dest.writeString(memberAvatar)
        dest.writeString(banner)
        if (createTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(createTime!!)
        }
        if (nextUnlockStartTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextUnlockStartTime!!)
        }
        if (nextUnlockEndTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextUnlockEndTime!!)
        }
        if (status == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(status!!)
        }
        if (ownerStatus == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(ownerStatus!!)
        }
        if (nextOwnerStartTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextOwnerStartTime!!)
        }
        if (nextOwnerEndTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextOwnerEndTime!!)
        }
        if (ownerPrice == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(ownerPrice!!)
        }
        if (nextOwnerChangeTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextOwnerChangeTime!!)
        }
        if (memberCount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(memberCount!!)
        }
        if (ownerCreateTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(ownerCreateTime!!)
        }
        if (nextOwnerChangeFinishedTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(nextOwnerChangeFinishedTime!!)
        }
        if (ownerLastRecordTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(ownerLastRecordTime!!)
        }
        if (ownerPriceStep == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(ownerPriceStep!!)
        }
        if (canLock == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(canLock!!)
        }
        dest.writeString(lastTempOwnerId)
        if (thisTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(thisTime!!)
        }
        dest.writeString(mobileBanner)
        dest.writeString(timRoomId)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getStatus(): Int {
        return status ?: -1
    }

    val ownerStatusCode: Int
        get() = ownerStatus ?: -1

    fun getStatusDisplay(context: Context?): String {
        val status = ownerStatusCode
        when (status) {
            0 -> return "即将开始"
            1 -> return "有掌⻔"
            2 -> return "竞选中"
            3 -> return "连任掌门考虑中"
        }
        return ""
    }

    fun isWaiting(thisTime: Long): Boolean {
        val statusCode = ownerStatusCode
        return statusCode == 0 && nextOwnerChangeTime != null && nextOwnerChangeTime!! > thisTime
    }

    fun isChoosing(thisTime: Long): Boolean {
        val statusCode = ownerStatusCode
        return statusCode == 2 && nextOwnerChangeFinishedTime != null && nextOwnerChangeFinishedTime!! > thisTime
    }

    fun isKeepWaiting(thisTime: Long): Boolean {
        val statusCode = ownerStatusCode
        return statusCode == 1 && nextOwnerStartTime != null && nextOwnerStartTime!! > thisTime
    }

    fun isKeepDoing(thisTime: Long): Boolean {
        val statusCode = ownerStatusCode
        return statusCode == 3 && nextOwnerEndTime != null && nextOwnerEndTime!! >= thisTime
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FactionItem?> = object : Parcelable.Creator<FactionItem?> {
            override fun createFromParcel(`in`: Parcel): FactionItem? {
                return FactionItem(`in`)
            }

            override fun newArray(size: Int): Array<FactionItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}
