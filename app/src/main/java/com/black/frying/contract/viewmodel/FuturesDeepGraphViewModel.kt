package com.black.frying.contract.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.base.model.socket.Deep
import com.black.base.model.socket.TradeOrder
import com.black.frying.contract.FuturesDeepGraphFragment

class FuturesDeepGraphViewModel : ViewModel() {
    lateinit var mContext: Context

    val pPrecisionLData:MutableLiveData<Int> = MutableLiveData<Int>(1)

    fun init(context: Context) {
        mContext = context
    }

    fun clickShowMode() {
        val maxIndex = FuturesDeepGraphFragment.ShowMode.values().size - 1
        var ordinal = showMode.value!!.ordinal
        if (ordinal >= maxIndex) {
            ordinal = 0
        } else {
            ordinal += 1
        }
        val nV = Math.min(maxIndex, ordinal)
        showMode.postValue(FuturesDeepGraphFragment.ShowMode.values()[nV])
    }

    val showMode =
        MutableLiveData(FuturesDeepGraphFragment.ShowMode.DEFAULT)

    val sellList: ArrayList<TradeOrder?> = ArrayList()
    val buyList: ArrayList<TradeOrder?> = ArrayList()

    fun getPrecisionDeep(supportingPrecisionList //支持深度
                         : ArrayList<Deep>): Deep? {
        return supportingPrecisionList.find { pPrecisionLData.value==it.precision }
    }

    fun setPrecision(precision: Int) {
        pPrecisionLData.postValue(precision)
    }

}


