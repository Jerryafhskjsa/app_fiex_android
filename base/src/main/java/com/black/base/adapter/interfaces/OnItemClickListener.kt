package com.black.base.adapter.interfaces

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.black.base.model.wallet.Wallet

interface OnItemClickListener {
    fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any? )
}
