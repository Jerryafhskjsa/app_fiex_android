package com.black.lib.stickylistheaders

import android.content.Context
import android.database.DataSetObserver
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Checkable
import android.widget.ListAdapter
import java.util.*

/**
 * A [ListAdapter] which wraps a [StickyListHeadersAdapter] and
 * automatically handles wrapping the result of
 * [StickyListHeadersAdapter.getView]
 * and
 * [StickyListHeadersAdapter.getHeaderView]
 * appropriately.
 *
 * @author Jake Wharton (jakewharton@gmail.com)
 */
internal open class AdapterWrapper(private val mContext: Context, var mDelegate: StickyListHeadersAdapter?) : BaseAdapter(), StickyListHeadersAdapter {
    internal interface OnHeaderClickListener {
        fun onHeaderClick(header: View?, itemPosition: Int, headerId: Long)
    }

    private val mHeaderCache: MutableList<View> = LinkedList()
    private var mDivider: Drawable? = null
    private var mDividerHeight = 0
    private var mOnHeaderClickListener: OnHeaderClickListener? = null
    private val mDataSetObserver: DataSetObserver = object : DataSetObserver() {
        override fun onInvalidated() {
            mHeaderCache.clear()
            super@AdapterWrapper.notifyDataSetInvalidated()
        }

        override fun onChanged() {
            super@AdapterWrapper.notifyDataSetChanged()
        }
    }

    init {
        mDelegate?.registerDataSetObserver(mDataSetObserver)
    }

    fun setDivider(divider: Drawable?, dividerHeight: Int) {
        mDivider = divider
        mDividerHeight = dividerHeight
        notifyDataSetChanged()
    }

    override fun areAllItemsEnabled(): Boolean {
        return mDelegate?.areAllItemsEnabled() ?: false
    }

    override fun isEnabled(position: Int): Boolean {
        return mDelegate?.isEnabled(position) ?: false
    }

    override fun getCount(): Int {
        return mDelegate?.count ?: 0
    }

    override fun getItem(position: Int): Any? {
        return mDelegate?.getItem(position)
    }

    override fun getItemId(position: Int): Long {
        return mDelegate?.getItemId(position) ?: 0
    }

    override fun hasStableIds(): Boolean {
        return mDelegate?.hasStableIds() ?: false
    }

    override fun getItemViewType(position: Int): Int {
        return mDelegate?.getItemViewType(position) ?: 0
    }

    override fun getViewTypeCount(): Int {
        return mDelegate?.viewTypeCount ?: 0
    }

    override fun isEmpty(): Boolean {
        return mDelegate?.isEmpty ?: true
    }

    /**
     * Will recycle header from [WrapperView] if it exists
     */
    private fun recycleHeaderIfExists(wv: WrapperView) {
        val header = wv.header
        if (header != null) { // reset the headers visibility when adding it to the cache
            header.visibility = View.VISIBLE
            mHeaderCache.add(header)
        }
    }

    /**
     * Get a header view. This optionally pulls a header from the supplied
     * [WrapperView] and will also recycle the divider if it exists.
     */
    private fun configureHeader(wv: WrapperView, position: Int): View {
        var header = if (wv.header == null) popHeader() else wv.header
        header = mDelegate?.getHeaderView(position, header, wv)
        if (header == null) {
            throw NullPointerException("Header view must not be null.")
        }
        //if the header isn't clickable, the listselector will be drawn on top of the header
        header.isClickable = true
        header.setOnClickListener(View.OnClickListener { v ->
            if (mOnHeaderClickListener != null) {
                val headerId = mDelegate?.getHeaderId(position) ?: 0
                mOnHeaderClickListener?.onHeaderClick(v, position, headerId)
            }
        })
        return header
    }

    private fun popHeader(): View? {
        return if (mHeaderCache.size > 0) {
            mHeaderCache.removeAt(0)
        } else null
    }

    /** Returns `true` if the previous position has the same header ID.  */
    private fun previousPositionHasSameHeader(position: Int): Boolean {
        return (position != 0
                && mDelegate?.getHeaderId(position) == mDelegate?.getHeaderId(position - 1))
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): WrapperView {
        var wv = if (convertView == null) WrapperView(mContext) else (convertView as WrapperView)
        val item = mDelegate?.getView(position, wv.item, parent)
        var header: View? = null
        if (previousPositionHasSameHeader(position)) {
            recycleHeaderIfExists(wv)
        } else {
            header = configureHeader(wv, position)
        }
        if (item is Checkable && wv !is CheckableWrapperView) { // Need to create Checkable subclass of WrapperView for ListView to work correctly
            wv = CheckableWrapperView(mContext)
        } else if (item !is Checkable && wv is CheckableWrapperView) {
            wv = WrapperView(mContext)
        }
        wv.update(item, header, mDivider, mDividerHeight)
        return wv
    }

    fun setOnHeaderClickListener(onHeaderClickListener: OnHeaderClickListener?) {
        mOnHeaderClickListener = onHeaderClickListener
    }

    override fun equals(other: Any?): Boolean {
        return mDelegate == other
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return (mDelegate as BaseAdapter).getDropDownView(position, convertView, parent)
    }

    override fun hashCode(): Int {
        return mDelegate?.hashCode() ?: 0
    }

    override fun notifyDataSetChanged() {
        (mDelegate as BaseAdapter).notifyDataSetChanged()
    }

    override fun notifyDataSetInvalidated() {
        (mDelegate as BaseAdapter).notifyDataSetInvalidated()
    }

    override fun toString(): String {
        return mDelegate?.toString() ?: ""
    }

    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        return mDelegate?.getHeaderView(position, convertView, parent)
    }

    override fun getHeaderId(position: Int): Long {
        return mDelegate?.getHeaderId(position) ?: 0
    }
}