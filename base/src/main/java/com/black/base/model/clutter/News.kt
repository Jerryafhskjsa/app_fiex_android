package com.black.base.model.clutter

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem

class News : BaseAdapterItem, Parcelable {
    var id: String? = null
    var title: String? = null
    var createTime: Long? = null
    var content: String? = null
    var hot: Boolean? = null

    constructor()
    constructor(`in`: Parcel) {
        id = `in`.readString()
        title = `in`.readString()
        createTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        content = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(title)
        if (createTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(createTime!!)
        }
        dest.writeString(content)
    } // [{"id":4,"title":"555","hot":false,"content":"5555555555555555555555555555555555","createdTime":1555153464000},

    /*
//    {"id":261,"userName":null,"title":"活动测试","sorting":0,"createdTime":1543804160325,
// "updateTime":1543818257187,"status":1,"tag":null,"readednum":0,"language":1,"categoryId":2,
// "isActivated":true,"content":"<p>啊实打实大苏打</p>"}
     */
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<News?> = object : Parcelable.Creator<News?> {
            override fun createFromParcel(`in`: Parcel): News? {
                return News(`in`)
            }

            override fun newArray(size: Int): Array<News?> {
                return arrayOfNulls(size)
            }
        }
    }
}