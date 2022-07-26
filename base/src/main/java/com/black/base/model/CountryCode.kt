package com.black.base.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import net.sourceforge.pinyin4j.PinyinHelper
import java.util.*

class CountryCode : BaseAdapterItem, Parcelable {
    var id: String? = null
    var en: String? = null
    var zh: String? = null
    var locale: String? = null
    var code: String? = null
    var status: String? = null

    constructor()
    constructor(`in`: Parcel) {
        id = `in`.readString()
        en = `in`.readString()
        zh = `in`.readString()
        locale = `in`.readString()
        code = `in`.readString()
        status = `in`.readString()
    }

    val sortLetter: Char?
        get() {
            if (zh == null || zh!!.trim { it <= ' ' }.isEmpty()) {
                return null
            }
            val cStrHY = PinyinHelper.toHanyuPinyinStringArray(zh!![0])
            return if (cStrHY == null || cStrHY.isEmpty()) {
                null
            } else cStrHY[0].trim { it <= ' ' }.toUpperCase(Locale.getDefault())[0]
        }

    override fun getType(): Int {
        return COUNTRY_CODE
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CountryCode) {
            TextUtils.equals(code, other.code)
        } else false
    }

    val display: String
        get() = (if (zh == null) "" else zh) + "  " + if (en == null) "" else en

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(en)
        dest.writeString(zh)
        dest.writeString(locale)
        dest.writeString(code)
        dest.writeString(status)
    }

    companion object {
        var COMPARATOR_CHOOSE_COUNTRY: Comparator<CountryCode?> = Comparator<CountryCode?> { o1, o2 ->
            if (o1 == null || o2 == null) {
                return@Comparator 0
            }
            val c1 = o1.sortLetter
            val c2 = o2.sortLetter
            if (c1 == null || c2 == null) 0 else c1.compareTo(c2)
        }
        @JvmField
        val CREATOR: Parcelable.Creator<CountryCode?> = object : Parcelable.Creator<CountryCode?> {
            override fun createFromParcel(`in`: Parcel): CountryCode? {
                return CountryCode(`in`)
            }

            override fun newArray(size: Int): Array<CountryCode?> {
                return arrayOfNulls(size)
            }
        }
    }
}