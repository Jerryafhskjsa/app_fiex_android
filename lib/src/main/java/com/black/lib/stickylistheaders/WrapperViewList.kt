package com.black.lib.stickylistheaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import com.black.util.CommonUtil
import java.lang.reflect.Field
import java.util.*

internal class WrapperViewList(context: Context?) : ListView(context) {
    internal interface LifeCycleListener {
        fun onDispatchDrawOccurred(canvas: Canvas)
    }

    private var mLifeCycleListener: LifeCycleListener? = null
    private var mFooterViews: MutableList<View>? = null
    private var mTopClippingLength = 0
    private var mSelectorRect = Rect() // for if reflection fails
    private var mSelectorPositionField: Field? = null
    private var mClippingToPadding = true
    private var mBlockLayoutChildren = false

    init {
        // Use reflection to be able to change the size/position of the list
        // selector so it does not come under/over the header
        try {
            val selectorRectField = AbsListView::class.java.getDeclaredField("mSelectorRect")
            selectorRectField.isAccessible = true
            mSelectorRect = selectorRectField[this] as Rect
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mSelectorPositionField = AbsListView::class.java.getDeclaredField("mSelectorPosition")
                mSelectorPositionField?.isAccessible = true
            }
        } catch (e: NoSuchFieldException) {
            CommonUtil.printError(getContext(), e)
        } catch (e: IllegalArgumentException) {
            CommonUtil.printError(getContext(), e)
        } catch (e: IllegalAccessException) {
            CommonUtil.printError(getContext(), e)
        }
    }

    override fun performItemClick(view: View?, position: Int, id: Long): Boolean {
        var view1: View? = view
        if (view1 is WrapperView) {
            view1 = view1.item
        }
        return super.performItemClick(view1, position, id)
    }

    private fun positionSelectorRect() {
        if (!mSelectorRect.isEmpty) {
            val selectorPosition = selectorPosition
            if (selectorPosition >= 0) {
                val firstVisibleItem = fixedFirstVisibleItem
                val v = getChildAt(selectorPosition - firstVisibleItem)
                if (v is WrapperView) {
                    mSelectorRect.top = v.top + v.mItemTop
                }
            }
        }
    }

    // not all supported andorid
    // version have this variable
    private val selectorPosition: Int
        get() {
            if (mSelectorPositionField == null) {
                // not all supported andorid
                // version have this variable
                for (i in 0 until childCount) {
                    if (getChildAt(i).bottom == mSelectorRect.bottom) {
                        return i + fixedFirstVisibleItem
                    }
                }
            } else {
                try {
                    return mSelectorPositionField?.getInt(this) ?: 0
                } catch (e: IllegalArgumentException) {
                    CommonUtil.printError(context, e)
                } catch (e: IllegalAccessException) {
                    CommonUtil.printError(context, e)
                }
            }
            return -1
        }

    override fun dispatchDraw(canvas: Canvas) {
        positionSelectorRect()
        if (mTopClippingLength != 0) {
            canvas.save()
            val clipping = canvas.clipBounds
            clipping.top = mTopClippingLength
            canvas.clipRect(clipping)
            super.dispatchDraw(canvas)
            canvas.restore()
        } else {
            super.dispatchDraw(canvas)
        }
        mLifeCycleListener!!.onDispatchDrawOccurred(canvas)
    }

    fun setLifeCycleListener(lifeCycleListener: LifeCycleListener?) {
        mLifeCycleListener = lifeCycleListener
    }

    override fun addFooterView(v: View) {
        super.addFooterView(v)
        addInternalFooterView(v)
    }

    override fun addFooterView(v: View, data: Any, isSelectable: Boolean) {
        super.addFooterView(v, data, isSelectable)
        addInternalFooterView(v)
    }

    private fun addInternalFooterView(v: View) {
        if (mFooterViews == null) {
            mFooterViews = ArrayList()
        }
        mFooterViews!!.add(v)
    }

    override fun removeFooterView(v: View): Boolean {
        if (super.removeFooterView(v)) {
            mFooterViews!!.remove(v)
            return true
        }
        return false
    }

    fun containsFooterView(v: View?): Boolean {
        return if (mFooterViews == null) {
            false
        } else mFooterViews!!.contains(v)
    }

    fun setTopClippingLength(topClipping: Int) {
        mTopClippingLength = topClipping
    }

    // first getFirstVisiblePosition() reports items
// outside the view sometimes on old versions of android
    // work around to fix bug with firstVisibleItem being to high
// because list view does not take clipToPadding=false into account
// on old versions of android
    val fixedFirstVisibleItem: Int
        get() {
            var firstVisibleItem = firstVisiblePosition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                return firstVisibleItem
            }
            // first getFirstVisiblePosition() reports items
            // outside the view sometimes on old versions of android
            for (i in 0 until childCount) {
                if (getChildAt(i).bottom >= 0) {
                    firstVisibleItem += i
                    break
                }
            }
            // work around to fix bug with firstVisibleItem being to high
            // because list view does not take clipToPadding=false into account
            // on old versions of android
            if (!mClippingToPadding && paddingTop > 0 && firstVisibleItem > 0) {
                if (getChildAt(0).top > 0) {
                    firstVisibleItem -= 1
                }
            }
            return firstVisibleItem
        }

    override fun setClipToPadding(clipToPadding: Boolean) {
        mClippingToPadding = clipToPadding
        super.setClipToPadding(clipToPadding)
    }

    fun setBlockLayoutChildren(block: Boolean) {
        mBlockLayoutChildren = block
    }

    override fun layoutChildren() {
        if (!mBlockLayoutChildren) {
            super.layoutChildren()
        }
    }
}