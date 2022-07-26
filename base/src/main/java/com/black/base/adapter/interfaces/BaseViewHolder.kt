package com.black.base.adapter.interfaces

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BaseViewHolder<T : ViewDataBinding?>(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    var dataBing: T? = null
        private set

    fun setDataBing(dataBing: T?) {
        this.dataBing = dataBing
    }
}
