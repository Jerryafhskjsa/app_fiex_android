package com.black.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.black.base.R
import com.black.base.adapter.interfaces.OnItemClickListener
import skin.support.content.res.SkinCompatResources
import java.util.*

abstract class BaseRecyclerViewAdapter<T, VH : BaseRecyclerViewAdapter<T, VH>.ViewHolder?>(protected var context: Context, data: MutableList<T>?) : RecyclerView.Adapter<VH>() {
    protected var inflater: LayoutInflater = LayoutInflater.from(context)
    protected var recyclerView: RecyclerView? = null
    var data: MutableList<T>? = null
        set(value) {
            field = value?.let { ArrayList(it) } ?: ArrayList()
        }
    protected var nullAmount: String
    protected var colorWin = 0
    protected var colorLost = 0
    var onItemClickListener: OnItemClickListener? = null

    init {
        this.data = data?.let { ArrayList(it) } ?: ArrayList()
        nullAmount = context.getString(R.string.number_default)
        resetSkinResources()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onFailedToRecycleView(holder: VH): Boolean {
        recyclerView = null
        return super.onFailedToRecycleView(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    fun resetSkinResources() {
        colorWin = SkinCompatResources.getColor(context, R.color.T7)
        colorLost = SkinCompatResources.getColor(context, R.color.T5)
    }

    open fun addItem(item: T?) {
        if (data == null) {
            data = ArrayList()
        }
        if (item != null) {
            data?.add(item)
        }
    }

    open fun addItem(index: Int, item: T?) {
        if (data == null) {
            data = ArrayList()
        }
        if (item != null) {
            data?.add(index, item)
        }
    }

    open fun addAll(data: MutableList<T>?) {
        if (this.data == null) {
            this.data = ArrayList()
        }
        if (data != null && data.isNotEmpty()) {
            this.data?.addAll(data)
        }
    }

    open fun addAllAtFirst(data: MutableList<T>?) {
        if (this.data == null) {
            this.data = ArrayList()
        }
        if (data != null && data.isNotEmpty()) {
            val count = data.size
            for (i in count - 1 downTo 0) {
                this.data?.add(0, data[i])
            }
        }
    }

    open fun sortData(comparator: Comparator<T>?) {
        if (data != null && comparator != null) {
            Collections.sort(data, comparator)
        }
    }

    open fun removeItem(item: T): Boolean {
        return data?.remove(item) ?: false
    }

    open fun clear() {
        data?.clear()
    }

    val count: Int
        get() = data?.size ?: 0

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg objects: Any?): String {
        return context.getString(resId, *objects)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    open fun getItem(position: Int): T {
        return data!![position]
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (holder != null && onItemClickListener != null) {
            holder.listenItemClick(position)
        }
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun listenItemClick(position: Int) {
            itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener?.onItemClick(recyclerView, itemView, position, getItem(position))
                }
            }
        }
    }
}