package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable


class SupportAccount:Parcelable{
    var type:String? = ""
    var name:String? = ""
    var selected:Boolean? = false
    constructor(type:String?,name: String?,selected:Boolean?){
        this.type = type
        this.name = name
        this.selected = selected
    }
    constructor(`in`: Parcel){
        type = `in`.readString()
        name = `in`.readString()
        selected = `in`.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(name)
        parcel.writeValue(selected)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SupportAccount?> = object : Parcelable.Creator<SupportAccount?> {
            override fun createFromParcel(`in`: Parcel): SupportAccount? {
                return SupportAccount(`in`)
            }
            override fun newArray(size: Int): Array<SupportAccount?> {
                return arrayOfNulls(size)
            }
        }
    }


}
