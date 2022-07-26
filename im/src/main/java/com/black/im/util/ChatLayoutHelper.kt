package com.black.im.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object ChatLayoutHelper {
    private val TAG = ChatLayoutHelper::class.java.simpleName
    fun isVisibleBottom(recyclerView: RecyclerView): Boolean {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        if (layoutManager == null || recyclerView.adapter == null) {
            return false
        }
        val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()
        return lastPosition >= (recyclerView.adapter?.itemCount ?: 0) - 3
    }
}
