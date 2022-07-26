package com.black.lib.banner.widget.loopviewpager

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

class LoopViewPager : ViewPager {
    companion object {
        private const val DEFAULT_BOUNDARY_CASHING = false
        /**
         * helper function which may be used when implementing FragmentPagerAdapter
         *
         * @param position
         * @param count
         * @return (position-1)%count
         */
        fun toRealPosition(position: Int, count: Int): Int {
            var position1 = position
            position1 -= 1
            if (position1 < 0) {
                position1 += count
            } else {
                position1 %= count
            }
            return position1
        }
    }

    //    OnPageChangeListener mOuterPageChangeListener;
    private var mAdapter: LoopPagerAdapterWrapper? = null
    private var mBoundaryCaching = DEFAULT_BOUNDARY_CASHING
    private var mOnPageChangeListeners: MutableList<OnPageChangeListener?>? = null
    /**
     * If set to true, the boundary views (i.e. first and last) will never be
     * destroyed This may help to prevent "blinking" of some views
     *
     * @param flag
     */
    fun setBoundaryCaching(flag: Boolean) {
        mBoundaryCaching = flag
        if (mAdapter != null) {
            mAdapter?.setBoundaryCaching(flag)
        }
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        mAdapter = if (adapter == null) null else LoopPagerAdapterWrapper(adapter)
        mAdapter?.setBoundaryCaching(mBoundaryCaching)
        super.setAdapter(mAdapter)
        setCurrentItem(0, false)
    }

    override fun getAdapter(): PagerAdapter? {
        return if (mAdapter != null) mAdapter?.realAdapter else mAdapter
    }

    override fun getCurrentItem(): Int {
        return mAdapter?.toRealPosition(super.getCurrentItem()) ?: 0
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        val realItem = mAdapter?.toInnerPosition(item) ?: 0
        super.setCurrentItem(realItem, smoothScroll)
    }

    override fun setCurrentItem(item: Int) {
        if (currentItem != item) {
            setCurrentItem(item, true)
        }
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener) {
        addOnPageChangeListener(listener)
    }

    override fun addOnPageChangeListener(listener: OnPageChangeListener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = ArrayList()
        }
        mOnPageChangeListeners?.add(listener)
    }

    override fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners?.remove(listener)
        }
    }

    override fun clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners?.clear()
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        if (onPageChangeListener != null) {
            super.removeOnPageChangeListener(onPageChangeListener)
        }
        super.addOnPageChangeListener(onPageChangeListener!!)
        clipChildren = false
    }

    private val onPageChangeListener: OnPageChangeListener? = object : OnPageChangeListener {
        private var mPreviousOffset = -1f
        private var mPreviousPosition = -1f
        override fun onPageSelected(position: Int) {
            val realPosition = mAdapter?.toRealPosition(position) ?: 0
            if (mPreviousPosition != realPosition.toFloat()) {
                mPreviousPosition = realPosition.toFloat()
                //                if (mOuterPageChangeListener != null) {
//                    mOuterPageChangeListener.onPageSelected(realPosition);
//                }
                if (mOnPageChangeListeners != null) {
                    for (i in mOnPageChangeListeners!!.indices) {
                        val listener = mOnPageChangeListeners!![i]
                        listener?.onPageSelected(realPosition)
                    }
                }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            var realPosition = position
            if (mAdapter != null) {
                realPosition = mAdapter!!.toRealPosition(position)
                if (positionOffset == 0f && mPreviousOffset == 0f && (position == 0 || position == mAdapter!!.count - 1)) {
                    setCurrentItem(realPosition, false)
                }
            }
            mPreviousOffset = positionOffset
            if (mOnPageChangeListeners != null) {
                for (i in mOnPageChangeListeners!!.indices) {
                    val listener = mOnPageChangeListeners!![i]
                    if (listener != null) {
                        if (realPosition != (mAdapter?.realCount ?: 0) - 1) {
                            listener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels)
                        } else {
                            if (positionOffset > .5) {
                                listener.onPageScrolled(0, 0f, 0)
                            } else {
                                listener.onPageScrolled(realPosition, 0f, 0)
                            }
                        }
                    }
                }
            }
            /*
            if (mOuterPageChangeListener != null) {
                if (realPosition != mAdapter.getRealCount() - 1) {
                    mOuterPageChangeListener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
                } else {
                    if (positionOffset > .5) {
                        mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                    } else {
                        mOuterPageChangeListener.onPageScrolled(realPosition, 0, 0);
                    }
                }
            }*/
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (mAdapter != null) {
                val position = super@LoopViewPager.getCurrentItem()
                val realPosition = mAdapter!!.toRealPosition(position)
                if (state == SCROLL_STATE_IDLE && (position == 0 || position == mAdapter!!.count - 1)) {
                    setCurrentItem(realPosition, false)
                }
            }
            //            if (mOuterPageChangeListener != null) {
//                mOuterPageChangeListener.onPageScrollStateChanged(state);
//            }
            if (mOnPageChangeListeners != null) {
                for (i in mOnPageChangeListeners!!.indices) {
                    val listener = mOnPageChangeListeners!![i]
                    listener?.onPageScrollStateChanged(state)
                }
            }
        }
    }
}