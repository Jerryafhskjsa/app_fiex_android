package com.black.base.adapter.interfaces

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnItemClickListener {
    fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?)
}
