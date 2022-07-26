package com.black.lib.banner

import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

/**
 * A PagerAdapter wrapper responsible for providing a proper page to
 * LoopViewPager
 *
 *
 * This class shouldn't be used directly
 */
class LoopBannerAdapterWrapper(private val realAdapter: PagerAdapter) : PagerAdapter() {
    private var mToDestroy = SparseArray<ToDestroy>()
    private var mBoundaryCaching = false
    fun setBoundaryCaching(flag: Boolean) {
        mBoundaryCaching = flag
    }

    override fun notifyDataSetChanged() {
        mToDestroy = SparseArray()
        super.notifyDataSetChanged()
    }

    fun toRealPosition(position: Int): Int {
        val realCount = realCount
        if (realCount == 0) return 0
        var realPosition = (position - 4) % realCount
        realPosition += 2
        realPosition %= realCount
        if (realPosition < 0) realPosition += realCount
        return realPosition
    }

    fun toInnerPosition(realPosition: Int): Int {
        return realPosition + 2
    }

    private val realFirstPosition: Int
        get() = 2

    private val realLastPosition: Int
        get() = realFirstPosition + realCount - 2

    override fun getCount(): Int {
        return realAdapter.count + 4
    }

    val realCount: Int
        get() = realAdapter.count

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (mBoundaryCaching) {
            val toDestroy = mToDestroy[position]
            if (toDestroy != null) {
                mToDestroy.remove(position)
                return toDestroy.`object`
            }
        }
        val realPosition = if (realAdapter is FragmentPagerAdapter || realAdapter is FragmentStatePagerAdapter) position else toRealPosition(position)
        return realAdapter.instantiateItem(container, realPosition)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val realFirst = realFirstPosition
        val realLast = realLastPosition
        val realPosition = if (realAdapter is FragmentPagerAdapter || realAdapter is FragmentStatePagerAdapter) position else toRealPosition(position)
        if (mBoundaryCaching && (position <= realFirst || position >= realLast)) {
            mToDestroy.put(position, ToDestroy(container, realPosition, `object`))
        } else {
            realAdapter.destroyItem(container, realPosition, `object`)
        }
    }

    /*
     * Delegate rest of methods directly to the inner adapter.
     */
    override fun finishUpdate(container: ViewGroup) {
        realAdapter.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return realAdapter.isViewFromObject(view, `object`)
    }

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) {
        realAdapter.restoreState(bundle, classLoader)
    }

    override fun saveState(): Parcelable? {
        return realAdapter.saveState()
    }

    override fun startUpdate(container: ViewGroup) {
        realAdapter.startUpdate(container)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        realAdapter.setPrimaryItem(container, position, `object`)
    }
    /*
     * End delegation
     */
    /**
     * Container class for caching the boundary views
     */
    internal class ToDestroy(var container: ViewGroup, var position: Int, var `object`: Any)

}