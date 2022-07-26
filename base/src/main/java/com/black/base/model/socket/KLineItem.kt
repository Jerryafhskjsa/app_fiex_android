package com.black.base.model.socket

import android.os.Parcel
import android.os.Parcelable

class KLineItem : Parcelable {
    var t //时间
            : Long? = null
    var c //收盘价
            = 0.0
    var o //开盘价
            = 0.0
    var h //最高价
            = 0.0
    var l //最低价
            = 0.0
    var a //交易量
            = 0.0
    var v //交易量 amount
            = 0.0

    constructor()
    constructor(source: Parcel) {
        t = source.readLong()
        c = source.readDouble()
        o = source.readDouble()
        h = source.readDouble()
        l = source.readDouble()
        a = source.readDouble()
        v = source.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(t!!)
        dest.writeDouble(c)
        dest.writeDouble(o)
        dest.writeDouble(h)
        dest.writeDouble(l)
        dest.writeDouble(a)
        dest.writeDouble(v)
    }

    companion object {
        // 实例化静态内部对象CREATOR实现接口Parcelable.Creator
        @JvmField
        val CREATOR: Parcelable.Creator<KLineItem?> = object : Parcelable.Creator<KLineItem?> {
            override fun createFromParcel(source: Parcel): KLineItem? {
                return KLineItem(source)
            }

            override fun newArray(size: Int): Array<KLineItem?> {
                return arrayOfNulls(0)
            }
        }
    }
}
