package com.black.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.black.base.R
import skin.support.content.res.SkinCompatResources
import java.util.*

abstract class BaseDataBindAdapter<T, DB : ViewDataBinding?>(protected var context: Context, data: MutableList<T>?) : BaseAdapter() {
    protected var inflater: LayoutInflater = LayoutInflater.from(context)
    var data: MutableList<T>? = null
        set(value) {
            field = value?.let { ArrayList(it) } ?: ArrayList()
        }
    protected var nullAmount: String
    protected var colorWin = 0
    protected var colorLost = 0
    protected var colorDefault = 0

    init {
        this.data = data?.let { ArrayList(it) } ?: ArrayList()
        nullAmount = getString(R.string.number_default)
        init()
    }

    private fun init() {
        resetSkinResources()
    }

    open fun resetSkinResources() {
        colorWin = SkinCompatResources.getColor(context, R.color.T9)
        colorLost = SkinCompatResources.getColor(context, R.color.T10)
        colorDefault = SkinCompatResources.getColor(context, R.color.T3)
    }

    fun addItem(item: T?) {
        if (data == null) {
            data = ArrayList()
        }
        if (item != null) {
            data?.add(item)
        }
    }

    open fun updateItem(index: Int, item: T?){
        if (data == null) {
            data = ArrayList()
        }
        if (item != null) {
            data?.set(index, item)
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

    fun addAllAtFirst(data: MutableList<T>?) {
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

    fun sortData(comparator: Comparator<T>?) {
        if (data != null && comparator != null) {
            Collections.sort(data, comparator)
        }
    }

    fun removeItem(item: T): Boolean {
        return data?.remove(item) ?: false
    }

    fun clear() {
        data?.clear()
    }

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg objects: Any?): String {
        return context.getString(resId, *objects)
    }

    override fun getCount(): Int {
        return data?.size ?: 0
    }

    override fun getItem(position: Int): T {
        return data!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var returnView: View? = convertView
        var viewHolder: ViewHolder<DB>? = null
        if (returnView == null) {
            val dataBing: DB = DataBindingUtil.inflate<DB>(inflater, getItemLayoutId(), parent, false)
            viewHolder = ViewHolder()
            viewHolder.dataBing = dataBing
            returnView = dataBing?.root
            returnView?.tag = viewHolder
        } else {
            viewHolder = returnView.tag as ViewHolder<DB>?
        }
        bindView(position, viewHolder)
        return returnView!!
    }

    protected abstract fun getItemLayoutId(): Int

    protected abstract fun bindView(position: Int, holder: ViewHolder<DB>?)

    protected inner class ViewHolder<VDB : ViewDataBinding?> {
        var dataBing: VDB? = null
    }
}