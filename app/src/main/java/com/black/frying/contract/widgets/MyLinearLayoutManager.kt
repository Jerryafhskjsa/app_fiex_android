package com.black.frying.contract.widgets

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyLinearLayoutManager(ctx:Context): LinearLayoutManager(ctx) {

    override fun measureChild(child: View, widthUsed: Int, heightUsed: Int) {
        super.measureChild(child, widthUsed, heightUsed)

    }

}