package com.black.base.model.filter

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class RegularRecordStatus : Parcelable {
    var code: String?
    var text: String?

    constructor(code: String?, text: String) {
        this.code = code
        this.text = text
    }

    constructor(`in`: Parcel) {
        code = `in`.readString()
        text = `in`.readString()
    }

    override fun toString(): String {
        return text ?: ""
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(code)
        dest.writeString(text)
    }

    companion object {
        const val KEY = "RegularRecordStatus"
        lateinit var ALL: RegularRecordStatus
        lateinit var INTO: RegularRecordStatus
        lateinit var OUT: RegularRecordStatus
        lateinit var BREAK: RegularRecordStatus
        @JvmField
        val CREATOR: Parcelable.Creator<RegularRecordStatus?> = object : Parcelable.Creator<RegularRecordStatus?> {
            override fun createFromParcel(`in`: Parcel): RegularRecordStatus? {
                return RegularRecordStatus(`in`)
            }

            override fun newArray(size: Int): Array<RegularRecordStatus?> {
                return arrayOfNulls(size)
            }
        }

        fun init(context: Context) {
            ALL = RegularRecordStatus(null, context.getString(R.string.total))
            INTO = RegularRecordStatus("1", context.getString(R.string.demand_status_into))
            OUT = RegularRecordStatus("5", context.getString(R.string.demand_status_out))
            BREAK = RegularRecordStatus("4", context.getString(R.string.regular_status_break))
        }

        fun getDefaultFilterEntity(selectItem: RegularRecordStatus?): FilterEntity<RegularRecordStatus> {
            val entity: FilterEntity<RegularRecordStatus> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<RegularRecordStatus>()
            //        data.add(ALL);
            data.add(INTO)
            data.add(OUT)
            data.add(BREAK)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }
}
