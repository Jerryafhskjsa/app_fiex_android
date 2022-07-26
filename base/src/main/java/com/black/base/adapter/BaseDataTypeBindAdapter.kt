package com.black.base.adapter

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.black.base.model.BaseAdapterItem
import kotlin.math.max

abstract class BaseDataTypeBindAdapter<T : BaseAdapterItem?, DB : ViewDataBinding?>(context: Context, data: MutableList<T>?) : BaseDataBindAdapter<T, DB>(context, data) {
    override fun getItemViewType(position: Int): Int {
        val item: BaseAdapterItem? = getItem(position)
        return item?.getType() ?: BaseAdapterItem.TYPE_DEFAULT
    }

    override fun getViewTypeCount(): Int {
        return max(BaseAdapterItem.getTypeCount(data), 1)
    }
}