package com.black.lib.banner.widget.banner

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.lib.R
import com.black.lib.banner.widget.loopviewpager.FixedSpeedScroller
import com.black.lib.banner.widget.loopviewpager.LoopViewPager
import com.black.util.CommonUtil
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class BaseBanner<E, T : BaseBanner<E, T>?> @JvmOverloads constructor(protected var mContext: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(mContext, attrs, defStyle) {
    companion object {
        private val TAG = BaseBanner::class.java.simpleName
    }

    protected var stse: ScheduledExecutorService? = null
    protected var dm: DisplayMetrics
    //ViewPager
    /**
     * ViewPager
     */
    var viewPager: ViewPager?
        protected set
    protected var lp_vp: LayoutParams
    protected var list: List<E>? = ArrayList()
    protected var viewList: List<View> = ArrayList()
    protected var currentPositon = 0
    protected var lastPositon = 0
    protected var innerAdapter: PagerAdapter? = null
    protected var delay: Long
    protected var period: Long
    protected var isAutoScrollEnable: Boolean
    protected var isSmart: Boolean
    protected var isAutoScrolling = false
    protected var scrollSpeed = 450
    protected var transformerClass: Class<out ViewPager.PageTransformer?>? = null
    /**
     * top parent of indicators
     */
    protected var rl_bottom_bar_parent: RelativeLayout
    protected var itemWidth: Int
    protected var itemHeight = 0
    /**
     * container of indicators and title
     */
    protected var ll_bottom_bar: LinearLayout
    protected var isBarShowWhenLast: Boolean
    /**
     * container of indicators
     */
    protected var ll_indicator_container: LinearLayout
    /**
     * title
     */
    protected var tv_title: TextView
    private val mHandler = Handler(Handler.Callback {
        scrollToNextItem(currentPositon)
        false
    })

    init {
        dm = mContext.resources.displayMetrics
        //get custom attr
        val ta = mContext.obtainStyledAttributes(attrs, R.styleable.BaseBanner)
        var scale = ta.getFloat(R.styleable.BaseBanner_bb_scale, -1f)
        val isLoopEnable = ta.getBoolean(R.styleable.BaseBanner_bb_isLoopEnable, true)
        delay = ta.getInt(R.styleable.BaseBanner_bb_delay, 5).toLong()
        period = ta.getInt(R.styleable.BaseBanner_bb_period, 5).toLong()
        isAutoScrollEnable = ta.getBoolean(R.styleable.BaseBanner_bb_isAutoScrollEnable, true)
        isSmart = ta.getBoolean(R.styleable.BaseBanner_bb_isSmart, false)
        val barColor = ta.getColor(R.styleable.BaseBanner_bb_barColor, Color.TRANSPARENT)
        isBarShowWhenLast = ta.getBoolean(R.styleable.BaseBanner_bb_isBarShowWhenLast, true)
        val indicatorGravity = ta.getInt(R.styleable.BaseBanner_bb_indicatorGravity, Gravity.CENTER)
        val barPaddingLeft = ta.getDimension(R.styleable.BaseBanner_bb_barPaddingLeft, dp2px(10f).toFloat())
        val barPaddingTop = ta.getDimension(R.styleable.BaseBanner_bb_barPaddingTop, dp2px(if (indicatorGravity == Gravity.CENTER) 6f else 2.toFloat()).toFloat())
        val barPaddingRight = ta.getDimension(R.styleable.BaseBanner_bb_barPaddingRight, dp2px(10f).toFloat())
        val barPaddingBottom = ta.getDimension(R.styleable.BaseBanner_bb_barPaddingBottom, dp2px(if (indicatorGravity == Gravity.CENTER) 6f else 2.toFloat()).toFloat())
        val textColor = ta.getColor(R.styleable.BaseBanner_bb_textColor, Color.parseColor("#ffffff"))
        val textSize = ta.getDimension(R.styleable.BaseBanner_bb_textSize, sp2px(12.5f))
        val isTitleShow = ta.getBoolean(R.styleable.BaseBanner_bb_isTitleShow, true)
        val isIndicatorShow = ta.getBoolean(R.styleable.BaseBanner_bb_isIndicatorShow, true)
        ta.recycle()
        //get layout_height
        val height = attrs?.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height")
        //create ViewPager
        viewPager = if (isLoopEnable) LoopViewPager(mContext) else ViewPager(mContext)
        itemWidth = dm.widthPixels
        if (scale < 0) { //scale not set in xml
            itemHeight = if (height == ViewGroup.LayoutParams.MATCH_PARENT.toString() + "") {
                //Log.d(TAG, "MATCH_PARENT--->" + height);
                LayoutParams.MATCH_PARENT
            } else if (height == ViewGroup.LayoutParams.WRAP_CONTENT.toString() + "") {
                //Log.d(TAG, "WRAP_CONTENT--->" + height);
                LayoutParams.WRAP_CONTENT
            } else {
                val systemAttrs = intArrayOf(android.R.attr.layout_height)
                val a = mContext.obtainStyledAttributes(attrs, systemAttrs)
                val h = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT)
                a.recycle()
                //Log.d(TAG, "EXACT_NUMBER--->" + h);
                h
            }
        } else {
            if (scale > 1) {
                scale = 1f
            }
            itemHeight = (itemWidth * scale).toInt()
            //Log.d(TAG, "scale--->" + scale);
        }
        lp_vp = LayoutParams(itemWidth, itemHeight)
        viewPager?.clipChildren = false
        lp_vp.addRule(CENTER_IN_PARENT, TRUE)
        addView(viewPager, lp_vp)
        //top parent of indicators
        rl_bottom_bar_parent = RelativeLayout(mContext)
        addView(rl_bottom_bar_parent, lp_vp)
        //container of indicators and title
        ll_bottom_bar = LinearLayout(mContext)
        val lp2 = LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
        lp2.addRule(ALIGN_PARENT_BOTTOM, TRUE)
        rl_bottom_bar_parent.addView(ll_bottom_bar, lp2)
        ll_bottom_bar.setBackgroundColor(barColor)
        ll_bottom_bar.setPadding(barPaddingLeft.toInt(), barPaddingTop.toInt(), barPaddingRight.toInt(), barPaddingBottom.toInt())
        ll_bottom_bar.clipChildren = false
        ll_bottom_bar.clipToPadding = false
        //container of indicators
        ll_indicator_container = LinearLayout(mContext)
        ll_indicator_container.gravity = Gravity.CENTER
        ll_indicator_container.visibility = if (isIndicatorShow) View.VISIBLE else View.INVISIBLE
        ll_indicator_container.clipChildren = false
        ll_indicator_container.clipToPadding = false
        // title
        tv_title = TextView(mContext)
        tv_title.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        tv_title.setSingleLine(true)
        tv_title.setTextColor(textColor)
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv_title.visibility = if (isTitleShow) View.VISIBLE else View.INVISIBLE
        if (indicatorGravity == Gravity.CENTER) {
            ll_bottom_bar.gravity = Gravity.CENTER
            ll_bottom_bar.addView(ll_indicator_container)
        } else {
            if (indicatorGravity == Gravity.RIGHT) {
                ll_bottom_bar.gravity = Gravity.CENTER_VERTICAL
                ll_bottom_bar.addView(tv_title)
                ll_bottom_bar.addView(ll_indicator_container)
                tv_title.setPadding(0, 0, dp2px(7f), 0)
                tv_title.ellipsize = TextUtils.TruncateAt.END
                tv_title.gravity = Gravity.LEFT
            } else if (indicatorGravity == Gravity.LEFT) {
                ll_bottom_bar.gravity = Gravity.CENTER_VERTICAL
                ll_bottom_bar.addView(ll_indicator_container)
                ll_bottom_bar.addView(tv_title)
                tv_title.setPadding(dp2px(7f), 0, 0, 0)
                tv_title.ellipsize = TextUtils.TruncateAt.END
                tv_title.gravity = Gravity.RIGHT
            }
        }
    }

    /**
     * create viewpager item layout
     */
    abstract fun onCreateItemView(position: Int): View

    /**
     * create indicator
     */
    abstract fun onCreateIndicator(): View?

    /**
     * set indicator show status, select or unselect
     */
    abstract fun setCurrentIndicator(position: Int)

    /**
     * Override this method to set title content when vp scroll to the position,
     * also you can set title attr,such as textcolor and etc.
     * if setIndicatorGravity == Gravity.CENTER,do nothing.
     */
    open fun onTitleSlect(tv: TextView?, position: Int) {}

    /**
     * set data source list
     */
    fun setSource(list: List<E>?): T {
        this.list = list
        return this as T
    }

    /**
     * set scroll delay before start scroll,unit second,default 5 seconds
     */
    fun setDelay(delay: Long): T {
        this.delay = delay
        return this as T
    }

    /**
     * set scroll period,unit second,default 5 seconds
     */
    fun setPeriod(period: Long): T {
        this.period = period
        return this as T
    }

    /**
     * set auto scroll enable for LoopViewPager,default true
     */
    fun setAutoScrollEnable(isAutoScrollEnable: Boolean): T {
        this.isAutoScrollEnable = isAutoScrollEnable
        return this as T
    }

    /**
     * set page transformer,only valid for API 3.0 and up since V1.1.0
     */
    fun setTransformerClass(transformerClass: Class<out ViewPager.PageTransformer?>?): T {
        this.transformerClass = transformerClass
        return this as T
    }

    /**
     * set bootom bar color,default transparent
     */
    fun setBarColor(barColor: Int): T {
        ll_bottom_bar.setBackgroundColor(barColor)
        return this as T
    }

    /**
     * set bottom bar show or not when the position is the last,default true
     */
    fun setBarShowWhenLast(isBarShowWhenLast: Boolean): T {
        this.isBarShowWhenLast = isBarShowWhenLast
        return this as T
    }

    /**
     * set bottom bar padding,unit dp
     */
    fun barPadding(left: Float, top: Float, right: Float, bottom: Float): T {
        ll_bottom_bar.setPadding(dp2px(left), dp2px(top), dp2px(right), dp2px(bottom))
        return this as T
    }

    /**
     * set title text color,default "#ffffff"
     */
    fun setTextColor(textColor: Int): T {
        tv_title.setTextColor(textColor)
        return this as T
    }

    /**
     * set title text size,unit sp,default 14sp
     */
    fun setTextSize(textSize: Float): T {
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        return this as T
    }

    /**
     * set title show or not,default true
     */
    fun setTitleShow(isTitleShow: Boolean): T {
        tv_title.visibility = if (isTitleShow) View.VISIBLE else View.INVISIBLE
        return this as T
    }

    /**
     * set indicator show or not,default true
     */
    fun setIndicatorShow(isIndicatorShow: Boolean): T {
        ll_indicator_container.visibility = if (isIndicatorShow) View.VISIBLE else View.INVISIBLE
        return this as T
    }

    /**
     * scroll to next item
     */
    private fun scrollToNextItem(position: Int) {
        var position = position
        position++
        viewPager?.currentItem = position
    }

    /**
     * set viewpager
     */
    private fun setViewPager() { //        viewList = new ArrayList<>(list.size());
//        for(int i = 0; i < list.size(); i++){
//            viewList.add(onCreateItemView(i));
//        }
        innerAdapter = InnerBannerAdapter()
        //innerAdapter.setViewList(viewList);
//        innerAdapter = new SimpleViewPagerAdapter(viewList);
        viewPager?.adapter = innerAdapter
        viewPager?.offscreenPageLimit = list?.size ?: 0
        try {
            if (transformerClass != null) {
                viewPager?.setPageTransformer(true, transformerClass?.newInstance())
                if (isLoopViewPager) {
                    scrollSpeed = 550
                    setScrollSpeed()
                }
            } else {
                if (isLoopViewPager) {
                    scrollSpeed = 450
                    setScrollSpeed()
                }
            }
        } catch (e: Exception) {
            CommonUtil.printError(getContext(), e)
        }
        if (internelPageListener != null) {
            viewPager?.removeOnPageChangeListener(internelPageListener)
        }
        viewPager?.addOnPageChangeListener(internelPageListener!!)
    }

    private val internelPageListener: ViewPager.OnPageChangeListener? = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (onPageChangeListener != null) {
                onPageChangeListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        }

        override fun onPageSelected(position: Int) {
            val size = list?.size ?: 0
            currentPositon = position % size
            setCurrentIndicator(currentPositon)
            onTitleSlect(tv_title, currentPositon)
            ll_bottom_bar.visibility = if (currentPositon == size - 1 && !isBarShowWhenLast) View.GONE else View.VISIBLE
            lastPositon = currentPositon
            if (onPageChangeListener != null) {
                onPageChangeListener?.onPageSelected(position)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (onPageChangeListener != null) {
                onPageChangeListener?.onPageScrollStateChanged(state)
            }
        }
    }

    fun startScroll() {
        checkNotNull(list) { "Data source is empty,you must setSource() before startScroll()" }
        onTitleSlect(tv_title, currentPositon)
        setViewPager()
        //create indicator
        val indicatorViews = onCreateIndicator()
        if (indicatorViews != null) {
            ll_indicator_container.removeAllViews()
            ll_indicator_container.addView(indicatorViews)
        }
        goOnScroll()
    }

    /**
     * for LoopViewPager
     */
    private fun goOnScroll() {
        if (!isValid) {
            return
        }
        if (isAutoScrolling) {
            return
        }
        if (isLoopViewPager && isAutoScrollEnable) {
            pauseScroll()
            stse = Executors.newSingleThreadScheduledExecutor()
            stse?.scheduleAtFixedRate({ mHandler.obtainMessage().sendToTarget() }, delay, period, TimeUnit.SECONDS)
            isAutoScrolling = true
        } else {
            isAutoScrolling = false
        }
    }

    /**
     * for LoopViewPager
     */
    private fun pauseScroll() {
        if (stse != null) {
            stse?.shutdown()
            stse = null
        }
        isAutoScrolling = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> pauseScroll()
            MotionEvent.ACTION_UP -> goOnScroll()
            MotionEvent.ACTION_CANCEL -> goOnScroll()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (isSmart) {
            if (visibility != View.VISIBLE) {
                pauseScroll()
            } else {
                goOnScroll()
            }
        }
    }

    private inner class InnerBannerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return list?.size ?: 0
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflate = onCreateItemView(position)
            inflate.setOnClickListener {
                if (onItemClickL != null) {
                    onItemClickL?.onItemClick(position)
                }
            }
            container.addView(inflate)
            return inflate
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    /**
     * set scroll speed
     */
    private fun setScrollSpeed() {
        try {
            val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
            val interpolator = AccelerateDecelerateInterpolator()
            val myScroller = FixedSpeedScroller(mContext, interpolator, scrollSpeed)
            mScroller[viewPager] = myScroller
        } catch (e: Exception) {
            CommonUtil.printError(getContext(), e)
        }
    }

    protected fun dp2px(dp: Float): Int {
        val scale = mContext.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun sp2px(sp: Float): Float {
        val scale = mContext.resources.displayMetrics.scaledDensity
        return sp * scale
    }

    protected val isLoopViewPager: Boolean
        get() = viewPager is LoopViewPager

    protected val isValid: Boolean
        get() = if (viewPager == null) {
            false
        } else list != null && list?.size != 0

    //listener
    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null

    fun addOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        onPageChangeListener = listener
    }

    private var onItemClickL: OnItemClickL? = null
    fun setOnItemClickL(onItemClickL: OnItemClickL?) {
        this.onItemClickL = onItemClickL
    }

    interface OnItemClickL {
        fun onItemClick(position: Int)
    }
}