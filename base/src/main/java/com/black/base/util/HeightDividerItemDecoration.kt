package com.black.base.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class HeightDividerItemDecoration(context: Context, orientation: Int) : DividerItemDecoration(context, orientation) {
    private var dividerHeight = 0
    fun setDividerHeight(dividerHeight: Int) {
        this.dividerHeight = dividerHeight
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dividerHeight
    }
}
