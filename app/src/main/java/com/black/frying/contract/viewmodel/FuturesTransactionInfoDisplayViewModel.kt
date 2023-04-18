package com.black.frying.contract.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.frying.contract.FuturesDeepGraphFragment

class FuturesTransactionInfoDisplayViewModel : ViewModel() {
    fun clickShowMode() {
        val maxIndex = FuturesDeepGraphFragment.ShowMode.values().size - 1
        var ordinal = showMode.value!!.ordinal
        if(ordinal>=maxIndex){
            ordinal=0
        }else{
            ordinal+=1
        }
        val nV =  Math.min(maxIndex,ordinal)
        showMode.postValue(FuturesDeepGraphFragment.ShowMode.values()[nV])
    }

    val showMode = MutableLiveData<FuturesDeepGraphFragment.ShowMode>(FuturesDeepGraphFragment.ShowMode.DEFAULT)

    val sellList: ArrayList<Array<String?>?> = ArrayList<Array<String?>?>()
    val buyList: ArrayList<Array<String?>?> = ArrayList<Array<String?>?>()


}


