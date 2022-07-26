package com.black.base.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import com.black.base.R
import com.black.base.model.MenuEntity

class MenuChooseAdapter(context: Context, data: MutableList<MenuEntity?>?, private val defaultItem: MenuEntity?) : BaseRecyclerViewAdapter<MenuEntity?, MenuChooseAdapter.ViewHolder>(context, data) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.list_item_choose_menu, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val menuEntity = getItem(position)
        holder.textView.text = menuEntity.toString()
        holder.textView.isChecked = menuEntity!! == defaultItem
    }

    inner class ViewHolder(itemView: View) : BaseRecyclerViewAdapter<MenuEntity?, ViewHolder>.ViewHolder(itemView) {
        var textView: CheckedTextView = itemView.findViewById(R.id.text)
    }

}