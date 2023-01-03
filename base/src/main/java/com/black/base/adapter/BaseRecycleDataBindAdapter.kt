package com.black.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.R
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.view.AutoLoadRecyclerView
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author databind专用adapter
 * create by zxh at 2018/6/6
 */
abstract class BaseRecycleDataBindAdapter<T, K : ViewDataBinding?>(protected var context: Context, private val variableId: Int, data: List<T>?) : RecyclerView.Adapter<BaseViewHolder<K>>() {
    protected var recyclerView: RecyclerView? = null
    var data: MutableList<T>? = null
        set(value) {
            field = value?.let { ArrayList(it) } ?: ArrayList()
        }
    protected var nullAmount: String
    protected var colorWin = 0
    protected var colorLost = 0
    private var onItemClickListener: OnItemClickListener? = null

    init {
        this.data = data?.let { ArrayList(it) } ?: ArrayList()
        nullAmount = getString(R.string.number_default)
        init()
    }

    private fun init() {
        resetSkinResources()
    }

    open fun resetSkinResources() {
        colorWin = SkinCompatResources.getColor(context, R.color.T7)
        colorLost = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onFailedToRecycleView(holder: BaseViewHolder<K>): Boolean {
        recyclerView = null
        return super.onFailedToRecycleView(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
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

    fun addItem(index: Int, item: T?) {
        if (data == null) {
            data = ArrayList()
        }
        if (item != null) {
            data?.add(index, item)
        }
    }

    fun addAll(data: List<T>?) {
        if (this.data == null) {
            this.data = ArrayList()
        }
        if (data != null && data.isNotEmpty()) {
            this.data?.addAll(data)
        }
    }

    fun addAllAtFirst(data: List<T>?) {
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

    fun getItem(position: Int): T {
        return data!![position]
    }

    val count: Int
        get() = data?.size ?: 0

    fun getString(resId: Int): String {
        return context.getString(resId)
    }

    fun getString(resId: Int, vararg objects: Any?): String {
        return context.getString(resId, *objects)
    }

    fun setRecycleManager(recyclerView: RecyclerView, direction: Int) {
        val layoutManager = GridLayoutManager(context, 1, direction, false)
        recyclerView.layoutManager = layoutManager
        if (recyclerView is AutoLoadRecyclerView) {
            recyclerView.adapter = this
        } else {
            recyclerView.adapter = this
        }
    }

    fun setRecycleManager(recyclerView: RecyclerView, direction: Int, spanCount: Int) {
        val layoutManager = GridLayoutManager(context, spanCount, direction, false)
        recyclerView.layoutManager = layoutManager
        if (recyclerView is AutoLoadRecyclerView) {
            recyclerView.adapter = this
        } else {
            recyclerView.adapter = this
        }
    }

    fun setRecycleLinManager(recyclerView: RecyclerView, direction: Int) {
        val layoutManager = LinearLayoutManager(context, direction, false)
        recyclerView.layoutManager = layoutManager
        if (recyclerView is AutoLoadRecyclerView) {
            recyclerView.adapter = this
        } else {
            recyclerView.adapter = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<K> {
        //获取DataBing相当于获取View
        val itemBing: K = DataBindingUtil.inflate<K>(LayoutInflater.from(parent.context), getResourceId(), parent, false)
        //初始化ViewHolder存放View
        val holder = BaseViewHolder<K>(itemBing?.root)
        holder.setDataBing(itemBing)
        return holder
    }

    override fun onBindViewHolder(holder: BaseViewHolder<K>, position: Int) { //获取数据
        val item = data!![position]
        //赋值
        holder.dataBing?.setVariable(variableId, item)
        //刷新界面
        holder.dataBing?.executePendingBindings()
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(recyclerView, holder.itemView, position, item) }
        }
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data?.size ?: 0
    }

    open fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    protected abstract fun getResourceId(): Int
}