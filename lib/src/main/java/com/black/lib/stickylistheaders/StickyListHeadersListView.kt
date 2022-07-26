package com.black.lib.stickylistheaders

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewConfiguration
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import com.black.lib.R
import com.black.lib.stickylistheaders.WrapperViewList.LifeCycleListener
import kotlin.math.min

/**
 * Even though this is a FrameLayout subclass we still consider it a ListView.
 * This is because of 2 reasons:
 * 1. It acts like as ListView.
 * 2. It used to be a ListView subclass and refactoring the name would cause compatibility errors.
 *
 * @author Emil Sjölander
 */
open class StickyListHeadersListView @TargetApi(Build.VERSION_CODES.HONEYCOMB) constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : FrameLayout(context, attrs, defStyle) {
    interface OnHeaderClickListener {
        fun onHeaderClick(l: StickyListHeadersListView?, header: View?,
                          itemPosition: Int, headerId: Long, currentlySticky: Boolean)
    }

    /**
     * Notifies the listener when the sticky headers top offset has changed.
     */
    interface OnStickyHeaderOffsetChangedListener {
        /**
         * @param l      The view parent
         * @param header The currently sticky header being offset.
         * This header is not guaranteed to have it's measurements set.
         * It is however guaranteed that this view has been measured,
         * therefor you should user getMeasured* methods instead of
         * get* methods for determining the view's size.
         * @param offset The amount the sticky header is offset by towards to top of the screen.
         */
        fun onStickyHeaderOffsetChanged(l: StickyListHeadersListView?, header: View?, offset: Int)
    }

    /**
     * Notifies the listener when the sticky header has been updated
     */
    interface OnStickyHeaderChangedListener {
        /**
         * @param l             The view parent
         * @param header        The new sticky header view.
         * @param itemPosition  The position of the item within the adapter's data set of
         * the item whose header is now sticky.
         * @param headerId      The id of the new sticky header.
         */
        fun onStickyHeaderChanged(l: StickyListHeadersListView?, header: View?,
                                  itemPosition: Int, headerId: Long)
    }

    /* --- Children --- */
    private val mList: WrapperViewList?
    private var mHeader: View? = null
    /* --- Header state --- */
    private var mHeaderId: Long? = null
    // used to not have to call getHeaderId() all the time
    private var mHeaderPosition: Int? = null
    private var mHeaderOffset: Int? = null
    /* --- Delegates --- */
    private var mOnScrollListenerDelegate: AbsListView.OnScrollListener? = null
    private var mAdapter: AdapterWrapper? = null
    /* --- Settings --- */
    private var mAreHeadersSticky = true
    private var mClippingToPadding = true
    private var mIsDrawingListUnderStickyHeader = true
    private var mStickyHeaderTopOffset = 0
    private var mPaddingLeft = 0
    private var mPaddingTop = 0
    private var mPaddingRight = 0
    private var mPaddingBottom = 0
    /* --- Touch handling --- */
    private var mDownY = 0f
    private var mHeaderOwnsTouch = false
    private val mTouchSlop: Float = ViewConfiguration.get(getContext()).scaledTouchSlop.toFloat()
    /* --- Other --- */
    private var mOnHeaderClickListener: OnHeaderClickListener? = null
    private var mOnStickyHeaderOffsetChangedListener: OnStickyHeaderOffsetChangedListener? = null
    private var mOnStickyHeaderChangedListener: OnStickyHeaderChangedListener? = null
    private var mDataSetObserver: AdapterWrapperDataSetObserver? = null
    private var mDivider: Drawable
    private var mDividerHeight: Int

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, R.attr.stickyListHeadersListViewStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureHeader(mHeader)
    }

    private fun ensureHeaderHasCorrectLayoutParams(header: View?) {
        var lp = header?.layoutParams
        if (lp == null) {
            lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            header?.layoutParams = lp
        } else if (lp.height == LayoutParams.MATCH_PARENT || lp.width == LayoutParams.WRAP_CONTENT) {
            lp.height = LayoutParams.WRAP_CONTENT
            lp.width = LayoutParams.MATCH_PARENT
            header?.layoutParams = lp
        }
    }

    private fun measureHeader(header: View?) {
        if (header != null) {
            val width = measuredWidth - mPaddingLeft - mPaddingRight
            val parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    width, MeasureSpec.EXACTLY)
            val parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED)
            measureChild(header, parentWidthMeasureSpec,
                    parentHeightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mList?.layout(0, 0, mList.measuredWidth, height)
        if (mHeader != null) {
            val lp = mHeader?.layoutParams as MarginLayoutParams
            val headerTop = lp.topMargin
            mHeader?.layout(mPaddingLeft, headerTop, mHeader!!.measuredWidth + mPaddingLeft, headerTop + mHeader!!.measuredHeight)
        }
    }

    override fun dispatchDraw(canvas: Canvas) { // Only draw the list here.
// The header should be drawn right after the lists children are drawn.
// This is done so that the header is above the list items
// but below the list decorators (scroll bars etc).
        if (mList?.visibility == View.VISIBLE || mList?.animation != null) {
            drawChild(canvas, mList, 0)
        }
    }

    // Reset values tied the header. also remove header form layout
// This is called in response to the data set or the adapter changing
    private fun clearHeader() {
        if (mHeader != null) {
            removeView(mHeader)
            mHeader = null
            mHeaderId = null
            mHeaderPosition = null
            mHeaderOffset = null
            // reset the top clipping length
            mList?.setTopClippingLength(0)
            updateHeaderVisibilities()
        }
    }

    private fun updateOrClearHeader(firstVisiblePosition: Int) {
        val adapterCount = mAdapter?.count ?: 0
        if (adapterCount == 0 || !mAreHeadersSticky) {
            return
        }
        val headerViewCount = mList?.headerViewsCount ?: 0
        var headerPosition = firstVisiblePosition - headerViewCount
        if (mList != null && mList.childCount > 0) {
            val firstItem = mList.getChildAt(0)
            if (firstItem.bottom < stickyHeaderTop()) {
                headerPosition++
            }
        }
        // It is not a mistake to call getFirstVisiblePosition() here.
        // Most of the time getFixedFirstVisibleItem() should be called
        // but that does not work great together with getChildAt()
        val doesListHaveChildren = (mList?.childCount ?: 0) != 0
        val isFirstViewBelowTop = (doesListHaveChildren
                && (mList?.firstVisiblePosition ?: 0) == 0 &&
                (mList?.getChildAt(0)?.top ?: 0) >= stickyHeaderTop())
        val isHeaderPositionOutsideAdapterRange = (headerPosition > adapterCount - 1 || headerPosition < 0)
        if (!doesListHaveChildren || isHeaderPositionOutsideAdapterRange || isFirstViewBelowTop) {
            clearHeader()
            return
        }
        updateHeader(headerPosition)
    }

    private fun updateHeader(headerPosition: Int) { // check if there is a new header should be sticky
        if (mHeaderPosition == null || mHeaderPosition != headerPosition) {
            mHeaderPosition = headerPosition
            val headerId = mAdapter?.getHeaderId(headerPosition)
            if (mHeaderId == null || mHeaderId != headerId) {
                mHeaderId = headerId
                val header = mAdapter?.getHeaderView(mHeaderPosition!!, mHeader, this)
                if (mHeader !== header) {
                    if (header == null) {
                        throw NullPointerException("header may not be null")
                    }
                    swapHeader(header)
                }
                ensureHeaderHasCorrectLayoutParams(mHeader)
                measureHeader(mHeader)
                if (mOnStickyHeaderChangedListener != null) {
                    mOnStickyHeaderChangedListener?.onStickyHeaderChanged(this, mHeader, headerPosition, mHeaderId!!)
                }
                // Reset mHeaderOffset to null ensuring
// that it will be set on the header and
// not skipped for performance reasons.
                mHeaderOffset = null
            }
        }
        var headerOffset = stickyHeaderTop()
        // Calculate new header offset
// Skip looking at the first view. it never matters because it always
// results in a headerOffset = 0
        for (i in 0 until (mList?.childCount ?: 0)) {
            val child = mList!!.getChildAt(i)
            val doesChildHaveHeader = child is WrapperView && child.hasHeader()
            val isChildFooter = mList.containsFooterView(child)
            if (child.top >= stickyHeaderTop() && (doesChildHaveHeader || isChildFooter)) {
                headerOffset = min(child.top - (mHeader?.measuredHeight ?: 0), headerOffset)
                break
            }
        }
        setHeaderOffet(headerOffset)
        if (!mIsDrawingListUnderStickyHeader) {
            mList?.setTopClippingLength((mHeader?.measuredHeight ?: 0)
                    + (mHeaderOffset ?: 0))
        }
        updateHeaderVisibilities()
    }

    private fun swapHeader(newHeader: View) {
        if (mHeader != null) {
            removeView(mHeader)
        }
        mHeader = newHeader
        addView(mHeader)
        if (mOnHeaderClickListener != null) {
            mHeader?.setOnClickListener {
                mOnHeaderClickListener?.onHeaderClick(
                        this@StickyListHeadersListView, mHeader,
                        mHeaderPosition ?: 0, mHeaderId!!, true)
            }
        }
        mHeader?.isClickable = true
    }

    // hides the headers in the list under the sticky header.
// Makes sure the other ones are showing
    private fun updateHeaderVisibilities() {
        val top = stickyHeaderTop()
        val childCount = mList?.childCount ?: 0
        for (i in 0 until childCount) { // ensure child is a wrapper view
            val child = mList?.getChildAt(i) as? WrapperView ?: continue
            // ensure wrapper view child has a header
            val wrapperViewChild = child
            if (!wrapperViewChild.hasHeader()) {
                continue
            }
            // update header views visibility
            val childHeader = wrapperViewChild.header
            if (wrapperViewChild.top < top) {
                if (childHeader?.visibility != View.INVISIBLE) {
                    childHeader?.visibility = View.INVISIBLE
                }
            } else {
                if (childHeader?.visibility != View.VISIBLE) {
                    childHeader?.visibility = View.VISIBLE
                }
            }
        }
    }

    // Wrapper around setting the header offset in different ways depending on
// the API version
    @SuppressLint("NewApi")
    private fun setHeaderOffet(offset: Int) {
        if (mHeaderOffset == null || mHeaderOffset != offset) {
            mHeaderOffset = offset
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mHeader?.translationY = mHeaderOffset?.toFloat() ?: 0f
            } else {
                val params = mHeader?.layoutParams as MarginLayoutParams
                params.topMargin = mHeaderOffset!!
                mHeader?.layoutParams = params
            }
            if (mOnStickyHeaderOffsetChangedListener != null) {
                mOnStickyHeaderOffsetChangedListener?.onStickyHeaderOffsetChanged(this, mHeader, -mHeaderOffset!!)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN) {
            mDownY = ev.y
            mHeaderOwnsTouch = mHeader != null && mDownY <= (mHeader?.height ?: 0) + mHeaderOffset!!
        }
        val handled: Boolean
        if (mHeaderOwnsTouch) {
            if (mHeader != null && Math.abs(mDownY - ev.y) <= mTouchSlop) {
                handled = mHeader?.dispatchTouchEvent(ev) ?: false
            } else {
                if (mHeader != null) {
                    val cancelEvent = MotionEvent.obtain(ev)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL
                    mHeader?.dispatchTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                }
                val downEvent = MotionEvent.obtain(ev.downTime, ev.eventTime, ev.action, ev.x, mDownY, ev.metaState)
                downEvent.action = MotionEvent.ACTION_DOWN
                handled = mList?.dispatchTouchEvent(downEvent) ?: false
                downEvent.recycle()
                mHeaderOwnsTouch = false
            }
        } else {
            handled = mList?.dispatchTouchEvent(ev) ?: false
        }
        return handled
    }

    private inner class AdapterWrapperDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            clearHeader()
        }

        override fun onInvalidated() {
            clearHeader()
        }
    }

    private inner class WrapperListScrollListener : AbsListView.OnScrollListener {
        override fun onScroll(view: AbsListView, firstVisibleItem: Int,
                              visibleItemCount: Int, totalItemCount: Int) {
            if (mOnScrollListenerDelegate != null) {
                mOnScrollListenerDelegate?.onScroll(view, firstVisibleItem,
                        visibleItemCount, totalItemCount)
            }
            mList?.fixedFirstVisibleItem?.let {
                updateOrClearHeader(it)
            }
        }

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            if (mOnScrollListenerDelegate != null) {
                mOnScrollListenerDelegate?.onScrollStateChanged(view,
                        scrollState)
            }
        }
    }

    private inner class WrapperViewListLifeCycleListener : LifeCycleListener {
        override fun onDispatchDrawOccurred(canvas: Canvas) { // onScroll is not called often at all before froyo
// therefor we need to update the header here as well.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                mList?.fixedFirstVisibleItem?.let {
                    updateOrClearHeader(it)
                }
            }
            if (mHeader != null) {
                if (mClippingToPadding) {
                    canvas.save()
                    canvas.clipRect(0, mPaddingTop, right, bottom)
                    drawChild(canvas, mHeader, 0)
                    canvas.restore()
                } else {
                    drawChild(canvas, mHeader, 0)
                }
            }
        }
    }

    private inner class AdapterWrapperHeaderClickHandler : AdapterWrapper.OnHeaderClickListener {
        override fun onHeaderClick(header: View?, itemPosition: Int, headerId: Long) {
            mOnHeaderClickListener?.onHeaderClick(
                    this@StickyListHeadersListView, header, itemPosition,
                    headerId, false)
        }
    }

    private fun isStartOfSection(position: Int): Boolean {
        return position == 0 || mAdapter?.getHeaderId(position) != mAdapter?.getHeaderId(position - 1)
    }

    fun getHeaderOverlap(position: Int): Int {
        val isStartOfSection = isStartOfSection(Math.max(0, position - headerViewsCount))
        if (!isStartOfSection) {
            val header = mAdapter?.getHeaderView(position, null, mList)
                    ?: throw NullPointerException("header may not be null")
            ensureHeaderHasCorrectLayoutParams(header)
            measureHeader(header)
            return header.measuredHeight
        }
        return 0
    }

    private fun stickyHeaderTop(): Int {
        return mStickyHeaderTopOffset + if (mClippingToPadding) mPaddingTop else 0
    }

    fun areHeadersSticky(): Boolean {
        return mAreHeadersSticky
    }

    /**
     * Use areHeadersSticky() method instead
     */// invalidating the list will trigger dispatchDraw()/* ---------- StickyListHeaders specific API ---------- */
    @get:Deprecated("")
    var areHeadersSticky: Boolean
        get() = areHeadersSticky()
        set(areHeadersSticky) {
            mAreHeadersSticky = areHeadersSticky
            if (!areHeadersSticky) {
                clearHeader()
            } else {
                mList?.fixedFirstVisibleItem?.let {
                    updateOrClearHeader(it)
                }
            }
            // invalidating the list will trigger dispatchDraw()
            mList?.invalidate()
        }

    /**
     *
     * @param stickyHeaderTopOffset
     * The offset of the sticky header fom the top of the view
     */
    var stickyHeaderTopOffset: Int
        get() = mStickyHeaderTopOffset
        set(stickyHeaderTopOffset) {
            mStickyHeaderTopOffset = stickyHeaderTopOffset
            mList?.fixedFirstVisibleItem?.let {
                updateOrClearHeader(it)
            }
        }

    // reset the top clipping length
    var isDrawingListUnderStickyHeader: Boolean
        get() = mIsDrawingListUnderStickyHeader
        set(drawingListUnderStickyHeader) {
            mIsDrawingListUnderStickyHeader = drawingListUnderStickyHeader
            // reset the top clipping length
            mList?.setTopClippingLength(0)
        }

    fun setOnHeaderClickListener(listener: OnHeaderClickListener?) {
        mOnHeaderClickListener = listener
        if (mAdapter != null) {
            if (mOnHeaderClickListener != null) {
                mAdapter?.setOnHeaderClickListener(AdapterWrapperHeaderClickHandler())
                if (mHeader != null) {
                    mHeader?.setOnClickListener {
                        mOnHeaderClickListener?.onHeaderClick(
                                this@StickyListHeadersListView, mHeader,
                                mHeaderPosition!!, mHeaderId!!, true)
                    }
                }
            } else {
                mAdapter?.setOnHeaderClickListener(null)
            }
        }
    }

    fun setOnStickyHeaderOffsetChangedListener(listener: OnStickyHeaderOffsetChangedListener?) {
        mOnStickyHeaderOffsetChangedListener = listener
    }

    fun setOnStickyHeaderChangedListener(listener: OnStickyHeaderChangedListener?) {
        mOnStickyHeaderChangedListener = listener
    }

    fun getListChildAt(index: Int): View? {
        return mList?.getChildAt(index)
    }

    val listChildCount: Int
        get() = mList?.childCount ?: 0

    /**
     * Use the method with extreme caution!! Changing any values on the
     * underlying ListView might break everything.
     *
     * @return the ListView backing this view.
     */
    val wrappedList: ListView?
        get() = mList

    private fun requireSdkVersion(versionCode: Int): Boolean {
        return Build.VERSION.SDK_INT >= versionCode
    }

    var divider: Drawable
        get() = mDivider
        set(divider) {
            mDivider = divider
            if (mAdapter != null) {
                mAdapter?.setDivider(mDivider, mDividerHeight)
            }
        }

    var dividerHeight: Int
        get() = mDividerHeight
        set(dividerHeight) {
            mDividerHeight = dividerHeight
            if (mAdapter != null) {
                mAdapter?.setDivider(mDivider, mDividerHeight)
            }
        }

    fun setOnScrollListener(onScrollListener: AbsListView.OnScrollListener?) {
        mOnScrollListenerDelegate = onScrollListener
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        if (l != null) {
            mList?.setOnTouchListener { v, event -> l.onTouch(this@StickyListHeadersListView, event) }
        } else {
            mList?.setOnTouchListener(null)
        }
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        mList?.onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: AdapterView.OnItemLongClickListener?) {
        mList?.onItemLongClickListener = listener
    }

    fun addHeaderView(v: View?, data: Any?, isSelectable: Boolean) {
        mList?.addHeaderView(v, data, isSelectable)
    }

    fun addHeaderView(v: View?) {
        mList?.addHeaderView(v)
    }

    fun removeHeaderView(v: View?) {
        mList?.removeHeaderView(v)
    }

    val headerViewsCount: Int
        get() = mList?.headerViewsCount ?: 0

    fun addFooterView(v: View, data: Any, isSelectable: Boolean) {
        mList?.addFooterView(v, data, isSelectable)
    }

    fun addFooterView(v: View) {
        mList?.addFooterView(v)
    }

    fun removeFooterView(v: View) {
        mList?.removeFooterView(v)
    }

    val footerViewsCount: Int
        get() = mList?.footerViewsCount ?: 0

    var emptyView: View?
        get() = mList?.emptyView
        set(v) {
            mList?.emptyView = v
        }

    override fun isVerticalScrollBarEnabled(): Boolean {
        return mList?.isVerticalScrollBarEnabled ?: false
    }

    override fun isHorizontalScrollBarEnabled(): Boolean {
        return mList?.isHorizontalScrollBarEnabled ?: false
    }

    override fun setVerticalScrollBarEnabled(verticalScrollBarEnabled: Boolean) {
        mList?.isVerticalScrollBarEnabled = verticalScrollBarEnabled
    }

    override fun setHorizontalScrollBarEnabled(horizontalScrollBarEnabled: Boolean) {
        mList?.isHorizontalScrollBarEnabled = horizontalScrollBarEnabled
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    override fun getOverScrollMode(): Int {
        return if (requireSdkVersion(Build.VERSION_CODES.GINGERBREAD)) {
            mList?.overScrollMode ?: 0
        } else 0
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    override fun setOverScrollMode(mode: Int) {
        if (requireSdkVersion(Build.VERSION_CODES.GINGERBREAD)) {
            if (mList != null) {
                mList.overScrollMode = mode
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    fun smoothScrollBy(distance: Int, duration: Int) {
        if (requireSdkVersion(Build.VERSION_CODES.FROYO)) {
            mList?.smoothScrollBy(distance, duration)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun smoothScrollByOffset(offset: Int) {
        if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
            mList?.smoothScrollByOffset(offset)
        }
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.FROYO)
    fun smoothScrollToPosition(position: Int) {
        if (requireSdkVersion(Build.VERSION_CODES.FROYO)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mList?.smoothScrollToPosition(position)
            } else {
                var offset = if (mAdapter == null) 0 else getHeaderOverlap(position)
                offset -= if (mClippingToPadding) 0 else mPaddingTop
                mList?.smoothScrollToPositionFromTop(position, offset)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    fun smoothScrollToPosition(position: Int, boundPosition: Int) {
        if (requireSdkVersion(Build.VERSION_CODES.FROYO)) {
            mList?.smoothScrollToPosition(position, boundPosition)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun smoothScrollToPositionFromTop(position: Int, offset: Int) {
        var offset = offset
        if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
            offset += if (mAdapter == null) 0 else getHeaderOverlap(position)
            offset -= if (mClippingToPadding) 0 else mPaddingTop
            mList?.smoothScrollToPositionFromTop(position, offset)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun smoothScrollToPositionFromTop(position: Int, offset: Int,
                                      duration: Int) {
        var offset = offset
        if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
            offset += if (mAdapter == null) 0 else getHeaderOverlap(position)
            offset -= if (mClippingToPadding) 0 else mPaddingTop
            mList?.smoothScrollToPositionFromTop(position, offset, duration)
        }
    }

    fun setSelection(position: Int) {
        setSelectionFromTop(position, 0)
    }

    fun setSelectionAfterHeaderView() {
        mList?.setSelectionAfterHeaderView()
    }

    fun setSelectionFromTop(position: Int, y: Int) {
        var y = y
        y += if (mAdapter == null) 0 else getHeaderOverlap(position)
        y -= if (mClippingToPadding) 0 else mPaddingTop
        mList?.setSelectionFromTop(position, y)
    }

    fun setSelector(sel: Drawable?) {
        mList?.selector = sel
    }

    fun setSelector(resID: Int) {
        mList?.setSelector(resID)
    }

    val firstVisiblePosition: Int
        get() = mList?.firstVisiblePosition ?: 0

    val lastVisiblePosition: Int
        get() = mList?.lastVisiblePosition ?: 0

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun setChoiceMode(choiceMode: Int) {
        mList?.choiceMode = choiceMode
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun setItemChecked(position: Int, value: Boolean) {
        mList?.setItemChecked(position, value)
    }

    @get:TargetApi(Build.VERSION_CODES.HONEYCOMB)
    val checkedItemCount: Int
        get() = if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
            mList?.checkedItemCount ?: 0
        } else 0

    @get:TargetApi(Build.VERSION_CODES.FROYO)
    val checkedItemIds: LongArray?
        get() = if (requireSdkVersion(Build.VERSION_CODES.FROYO)) {
            mList?.checkedItemIds
        } else null

    @get:TargetApi(Build.VERSION_CODES.HONEYCOMB)
    val checkedItemPosition: Int
        get() = mList?.checkedItemPosition ?: 0

    @get:TargetApi(Build.VERSION_CODES.HONEYCOMB)
    val checkedItemPositions: SparseBooleanArray
        get() = mList?.checkedItemPositions ?: SparseBooleanArray()

    val count: Int
        get() = mList?.count ?: 0

    fun getItemAtPosition(position: Int): Any? {
        return mList?.getItemAtPosition(position)
    }

    fun getItemIdAtPosition(position: Int): Long {
        return mList?.getItemIdAtPosition(position) ?: 0
    }

    override fun setOnCreateContextMenuListener(l: OnCreateContextMenuListener) {
        mList?.setOnCreateContextMenuListener(l)
    }

    override fun showContextMenu(): Boolean {
        return mList?.showContextMenu() ?: false
    }

    fun invalidateViews() {
        mList?.invalidateViews()
    }

    override fun setClipToPadding(clipToPadding: Boolean) {
        if (mList != null) {
            mList.clipToPadding = clipToPadding
        }
        mClippingToPadding = clipToPadding
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mPaddingLeft = left
        mPaddingTop = top
        mPaddingRight = right
        mPaddingBottom = bottom
        mList?.setPadding(left, top, right, bottom)
        super.setPadding(0, 0, 0, 0)
        requestLayout()
    }

    /*
     * Overrides an @hide method in View
     */
    protected fun recomputePadding() {
        setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
    }

    override fun getPaddingLeft(): Int {
        return mPaddingLeft
    }

    override fun getPaddingTop(): Int {
        return mPaddingTop
    }

    override fun getPaddingRight(): Int {
        return mPaddingRight
    }

    override fun getPaddingBottom(): Int {
        return mPaddingBottom
    }

    fun setFastScrollEnabled(fastScrollEnabled: Boolean) {
        mList?.isFastScrollEnabled = fastScrollEnabled
    }

    /**
     * @return true if the fast scroller will always show. False on pre-Honeycomb devices.
     * @see AbsListView.isFastScrollAlwaysVisible
     */
    @get:TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @set:TargetApi(Build.VERSION_CODES.HONEYCOMB)
    var isFastScrollAlwaysVisible: Boolean
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            false
        } else mList?.isFastScrollAlwaysVisible ?: false
        set(alwaysVisible) {
            if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
                mList?.isFastScrollAlwaysVisible = alwaysVisible
            }
        }

    override fun setScrollBarStyle(style: Int) {
        mList?.scrollBarStyle = style
    }

    override fun getScrollBarStyle(): Int {
        return mList?.scrollBarStyle ?: 0
    }

    fun getPositionForView(view: View?): Int {
        return mList?.getPositionForView(view) ?: -1
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun setMultiChoiceModeListener(listener: MultiChoiceModeListener?) {
        if (requireSdkVersion(Build.VERSION_CODES.HONEYCOMB)) {
            mList?.setMultiChoiceModeListener(listener)
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        check(!(superState !== BaseSavedState.EMPTY_STATE)) { "Handling non empty state of parent class is not implemented" }
        return mList?.onSaveInstanceState()
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE)
        mList?.onRestoreInstanceState(state)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun canScrollVertically(direction: Int): Boolean {
        return mList?.canScrollVertically(direction) ?: false
    }

    fun setTranscriptMode(mode: Int) {
        mList?.transcriptMode = mode
    }

    fun setBlockLayoutChildren(blockLayoutChildren: Boolean) {
        mList?.setBlockLayoutChildren(blockLayoutChildren)
    }

    var isStackFromBottom: Boolean
        get() = mList?.isStackFromBottom ?: false
        set(stackFromBottom) {
            mList?.isStackFromBottom = stackFromBottom
        }

    init {
        // Initialize the wrapped list
        mList = WrapperViewList(context)
        // null out divider, dividers are handled by adapter so they look good with headers
        mDivider = mList.divider
        mDividerHeight = mList.dividerHeight
        mList.divider = null
        mList.dividerHeight = 0
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StickyListHeadersListView, defStyle, 0)
            try { // -- View attributes --
                val padding = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_padding, 0)
                mPaddingLeft = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingLeft, padding)
                mPaddingTop = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingTop, padding)
                mPaddingRight = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingRight, padding)
                mPaddingBottom = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_paddingBottom, padding)
                setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
                // Set clip to padding on the list and reset value to default on
// wrapper
                mClippingToPadding = a.getBoolean(R.styleable.StickyListHeadersListView_android_clipToPadding, true)
                super.setClipToPadding(true)
                mList.clipToPadding = mClippingToPadding
                // scrollbars
                val scrollBars = a.getInt(R.styleable.StickyListHeadersListView_android_scrollbars, 0x00000200)
                mList.isVerticalScrollBarEnabled = scrollBars and 0x00000200 != 0
                mList.isHorizontalScrollBarEnabled = scrollBars and 0x00000100 != 0
                // overscroll
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    mList.overScrollMode = a.getInt(R.styleable.StickyListHeadersListView_android_overScrollMode, 0)
                }
                // -- ListView attributes --
                mList.setFadingEdgeLength(a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_fadingEdgeLength,
                        mList.verticalFadingEdgeLength))
                val fadingEdge = a.getInt(R.styleable.StickyListHeadersListView_android_requiresFadingEdge, 0)
                if (fadingEdge == 0x00001000) {
                    mList.isVerticalFadingEdgeEnabled = false
                    mList.isHorizontalFadingEdgeEnabled = true
                } else if (fadingEdge == 0x00002000) {
                    mList.isVerticalFadingEdgeEnabled = true
                    mList.isHorizontalFadingEdgeEnabled = false
                } else {
                    mList.isVerticalFadingEdgeEnabled = false
                    mList.isHorizontalFadingEdgeEnabled = false
                }
                mList.cacheColorHint = a
                        .getColor(R.styleable.StickyListHeadersListView_android_cacheColorHint, mList.cacheColorHint)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mList.choiceMode = a.getInt(R.styleable.StickyListHeadersListView_android_choiceMode,
                            mList.choiceMode)
                }
                mList.setDrawSelectorOnTop(a.getBoolean(R.styleable.StickyListHeadersListView_android_drawSelectorOnTop, false))
                mList.isFastScrollEnabled = a.getBoolean(R.styleable.StickyListHeadersListView_android_fastScrollEnabled,
                        mList.isFastScrollEnabled)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mList.isFastScrollAlwaysVisible = a.getBoolean(
                            R.styleable.StickyListHeadersListView_android_fastScrollAlwaysVisible,
                            mList.isFastScrollAlwaysVisible)
                }
                mList.scrollBarStyle = a.getInt(R.styleable.StickyListHeadersListView_android_scrollbarStyle, 0)
                if (a.hasValue(R.styleable.StickyListHeadersListView_android_listSelector)) {
                    mList.selector = a.getDrawable(R.styleable.StickyListHeadersListView_android_listSelector)
                }
                mList.isScrollingCacheEnabled = a.getBoolean(R.styleable.StickyListHeadersListView_android_scrollingCache,
                        mList.isScrollingCacheEnabled)
                if (a.hasValue(R.styleable.StickyListHeadersListView_android_divider)) {
                    mDivider = a.getDrawable(R.styleable.StickyListHeadersListView_android_divider)
                }
                mList.isStackFromBottom = a.getBoolean(R.styleable.StickyListHeadersListView_android_stackFromBottom, false)
                mDividerHeight = a.getDimensionPixelSize(R.styleable.StickyListHeadersListView_android_dividerHeight,
                        mDividerHeight)
                mList.transcriptMode = a.getInt(R.styleable.StickyListHeadersListView_android_transcriptMode,
                        ListView.TRANSCRIPT_MODE_DISABLED)
                // -- StickyListHeaders attributes --
                mAreHeadersSticky = a.getBoolean(R.styleable.StickyListHeadersListView_hasStickyHeaders, true)
                mIsDrawingListUnderStickyHeader = a.getBoolean(
                        R.styleable.StickyListHeadersListView_isDrawingListUnderStickyHeader,
                        true)
            } finally {
                a.recycle()
            }
        }
        // attach some listeners to the wrapped list
        mList.setLifeCycleListener(WrapperViewListLifeCycleListener())
        mList.setOnScrollListener(WrapperListScrollListener())
        addView(mList)
    }

    /* ---------- ListView delegate methods ---------- */
    open fun setAdapter(adapter: StickyListHeadersAdapter?) {
        if (adapter == null) {
            if (mAdapter is SectionIndexerAdapterWrapper) {
                (mAdapter as SectionIndexerAdapterWrapper).mSectionIndexerDelegate = null
            }
            if (mAdapter != null) {
                mAdapter?.mDelegate = null
            }
            mList!!.adapter = null
            clearHeader()
            return
        }
        if (mAdapter != null) {
            mAdapter?.unregisterDataSetObserver(mDataSetObserver)
        }

        mAdapter = if (adapter is SectionIndexer) {
            SectionIndexerAdapterWrapper(context, adapter)
        } else {
            AdapterWrapper(context, adapter)
        }
        mDataSetObserver = AdapterWrapperDataSetObserver()
        mAdapter?.registerDataSetObserver(mDataSetObserver)

        if (mOnHeaderClickListener != null) {
            mAdapter?.setOnHeaderClickListener(AdapterWrapperHeaderClickHandler())
        } else {
            mAdapter?.setOnHeaderClickListener(null)
        }

        mAdapter?.setDivider(mDivider, mDividerHeight)

        mList!!.adapter = mAdapter
        clearHeader()
    }

    open fun getAdapter(): StickyListHeadersAdapter? {
        return if (mAdapter == null) null else mAdapter?.mDelegate
    }
}
