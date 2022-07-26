package com.black.im.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.model.InputMoreActionUnit

class ActionsGridViewAdapter(private val context: Context, private val baseActions: List<InputMoreActionUnit>) : BaseAdapter() {
    override fun getCount(): Int {
        return baseActions.size
    }

    override fun getItem(position: Int): Any {
        return baseActions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemLayout = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.chat_input_layout_actoin, parent, false)
        val action = baseActions[position]
        if (action.iconResId > 0) (itemLayout.findViewById<View>(R.id.imageView) as ImageView).setImageResource(action.iconResId)
        if (action.titleId > 0) (itemLayout.findViewById<View>(R.id.textView) as TextView).text = context.getString(action.titleId)
        return itemLayout
    }

}