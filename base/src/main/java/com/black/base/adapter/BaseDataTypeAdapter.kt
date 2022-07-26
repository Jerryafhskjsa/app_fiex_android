package com.black.base.adapter

import android.content.Context
import com.black.base.model.BaseAdapterItem

abstract class BaseDataTypeAdapter<T : BaseAdapterItem?>(context: Context, data: MutableList<T>?) : BaseDataAdapter<T>(context, data) {
    override fun getItemViewType(position: Int): Int {
        val item: BaseAdapterItem? = getItem(position)
        return item?.getType() ?: BaseAdapterItem.TYPE_DEFAULT
    }

    override fun getViewTypeCount(): Int {
        return Math.max(BaseAdapterItem.getTypeCount(data), 1)
    }
}