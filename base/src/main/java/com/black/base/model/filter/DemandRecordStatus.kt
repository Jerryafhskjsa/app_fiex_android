package com.black.base.model.filter

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import com.black.base.lib.filter.FilterEntity
import java.util.*

class DemandRecordStatus : Parcelable {
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
        const val KEY = "DemandRecordStatus"
        lateinit var ALL: DemandRecordStatus
        lateinit var INTO: DemandRecordStatus
        lateinit var OUT: DemandRecordStatus
        @JvmField
        val CREATOR: Parcelable.Creator<DemandRecordStatus?>? = object : Parcelable.Creator<DemandRecordStatus?> {
            override fun createFromParcel(`in`: Parcel): DemandRecordStatus? {
                return DemandRecordStatus(`in`)
            }

            override fun newArray(size: Int): Array<DemandRecordStatus?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun init(context: Context) {
            ALL = DemandRecordStatus(null, context.getString(R.string.total))
            INTO = DemandRecordStatus("1", context.getString(R.string.demand_status_into))
            OUT = DemandRecordStatus("2", context.getString(R.string.demand_status_out))
        }

        fun getDefaultFilterEntity(selectItem: DemandRecordStatus?): FilterEntity<DemandRecordStatus> {
            val entity: FilterEntity<DemandRecordStatus> = FilterEntity()
            entity.key = KEY
            val data = ArrayList<DemandRecordStatus>()
            //        data.add(ALL);
            data.add(INTO)
            data.add(OUT)
            entity.data = data
            entity.selectItem = selectItem
            return entity
        }
    }
}