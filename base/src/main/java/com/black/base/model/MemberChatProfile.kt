package com.black.base.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

class MemberChatProfile : Parcelable {
    var hardUserName: String? = null
    var defaultAvatarUrl: String? = null

    constructor()
    constructor(`in`: Parcel) {
        hardUserName = `in`.readString()
        defaultAvatarUrl = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(hardUserName)
        dest.writeString(defaultAvatarUrl)
    }

    override fun equals(other: Any?): Boolean {
        if (other is MemberChatProfile) {
            return TextUtils.equals(hardUserName, other.hardUserName) && TextUtils.equals(defaultAvatarUrl, other.defaultAvatarUrl)
        }
        return false
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MemberChatProfile?> = object : Parcelable.Creator<MemberChatProfile?> {
            override fun createFromParcel(`in`: Parcel): MemberChatProfile? {
                return MemberChatProfile(`in`)
            }

            override fun newArray(size: Int): Array<MemberChatProfile?> {
                return arrayOfNulls(size)
            }
        }
    }
}