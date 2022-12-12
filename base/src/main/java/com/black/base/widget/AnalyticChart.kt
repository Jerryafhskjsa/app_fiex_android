package com.black.base.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import com.black.base.R
import com.black.base.model.socket.KLineChartItem
import com.black.base.model.socket.KLineItem
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.printError
import com.black.base.util.KLineUtil.addKLIneItemsToFront
import com.black.base.util.KLineUtil.calculate
import com.black.base.util.KLineUtil.getAllNode
import com.black.base.util.KLineUtil.max
import com.black.base.util.KLineUtil.min
import com.black.base.util.KLineUtil.updateNode
import com.black.lib.typeface.TypefaceTextPaintHelper
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatView
import java.util.*
import kotlin.math.abs


class AnalyticChart : SkinCompatView {
    companion object {
        private const val TAG = "AnalyticChart"
        private const val WIDTH = 300
        private const val HEIGHT = 300
        //主图模式
        const val MAIN_HIDDEN = 0x1
        const val MA = 0x2
        const val BOLL = 0x4
        //副图模式
        const val SUB_HIDDEN = 0x8
        const val MACD = 0x10
        const val KDJ = 0x20
        const val RSI = 0x40
        const val WR = 0x80

        //滚动时刷新间隔 ms
        private const val FLING_REFRESH_TIME: Long = 30

        private const val SHOW_COUNT_MIN = 24
        private const val ITEM_COUNT_MAX = 1000
        private var SHOW_COUNT_MAX = 240

        //边缘空白
        private var ROUND_BLANK = ConstData.ROUND_BLANK
        //上下文字区域高度
        private var TOP_TEXT_HEIGHT = ConstData.TOP_TEXT_HEIGHT
        private var BOTTOM_TEXT_HEIGHT = ConstData.BOTTOM_TEXT_HEIGHT
        //文字间距
        private var TEXT_SPACING = ConstData.TEXT_SPACING
        //弹出窗口文字边距
        private var POPPER_TEXT_PADDING = ConstData.POPPER_TEXT_PADDING
        //弹出窗口文字边距
        private var POPPER_POINT_INNER_RADIUS = ConstData.POPPER_POINT_INNER_RADIUS
        private var POPPER_POINT_OUTER_RADIUS = ConstData.POPPER_POINT_OUTER_RADIUS

        //右边空白区域
        private var RIGHT_BLANK_WIDTH_MAX = 0f
        private var currentRightBlankWidth = RIGHT_BLANK_WIDTH_MAX

        private var NONE_COLOR = Color.argb(0x00, 0x66, 0xae, 0xe6)
        private var START_COLOR = Color.argb(0xab, 0xc2, 0xd1, 0xf9)
        private var NONE_STEP_COLOR = 0
        private var SELECT_BG_COLOR = 0
        private var BASE_LINE_COLOR: Int = 0
        private var COORDINATE_TEXT_COLOR: Int = 0
        private var CURRENT_PRICE_LINE_COLOR: Int = 0
        private var CURRENT_PRICE_BG_COLOR: Int = 0
        private var CURRENT_PRICE_TEXT_COLOR: Int = 0
        private var POPPER_BORDER_COLOR = 0
        private var POPPER_BG_COLOR: Int = 0
        private var POPPER_TITLE_COLOR: Int = 0
        private var POPPER_TEXT_COLOR: Int = 0
        private var POPPER_PRICE_COLOR: Int = 0
        private var POPPER_LINE_COLOR: Int = 0
        private var POPPER_POINTER_INNER_COLOR: Int = 0
        //POPPER_POINTER_OUTER_COLOR;
        private var MAXMIN_COLOR = 0
        private var MA5_COLOR = 0
        private var MA10_COLOR = 0
        private var MA30_COLOR = 0
        private var BOLL_COLOR = 0
        private var UB_COLOR = 0
        private var LB_COLOR = 0
        private var MA_OTHER01_COLOR = 0
        private var MA_OTHER02_COLOR: Int = 0
        private var MA_OTHER03_COLOR: Int = 0
        private var VOL_COLOR = 0
        private var VOL_MA5_COLOR: Int = 0
        private var VOL_MA10_COLOR: Int = 0
        private var WIN_COLOR = 0
        private var LOSE_COLOR: Int = 0
        private var MACD_COLOR = 0
        private var DIF_COLOR: Int = 0
        private var DEA_COLOR: Int = 0
        private var K_COLOR: Int = 0
        private var D_COLOR: Int = 0
        private var J_COLOR: Int = 0
        private var RSI_COLOR: Int = 0
        private var WR_COLOR: Int = 0

        //副图MACD柱宽度
        private var MACD_COLUMN_WIDTH = ConstData.MACD_COLUMN_WIDTH
    }

    private var type = MA or SUB_HIDDEN

    private var density = 0f
    private var mPaddingLeft = 0f
    private var mPaddingTop: Float = 0f
    private var mPaddingRight: Float = 0f
    private var mPaddingBottom: Float = 0f
    //图形尺寸
    private var chartWidth = 0f
    private var chartHeight = 0f
    //图形区域
    private var chartTop = 0f
    private var chartBottom = 0f
    private var chartLeft = 0f
    private var chartRight = 0f
    //图形分格尺寸
    private var cellWidth = 0f
    private var cellHeight = 0f
    //每一项占用宽度
    private var itemWidth = 0f
    //弹出窗口尺寸
    private var popperWidth = 0f
    private var popperHeight = 0f
    //主图占用格数,交易量占用格数，附图占用格数
    private var mainCount = 0
    private var businessCount = 0
    private var subCount = 0
    //各区域高度
    private var mainGraphHeight = 0f
    private var businessHeight = 0f
    private var subHeight = 0f
    //副图MACD 0 值对应Y
    private var macdZeroY = 0f
    //主图Y轴价格坐标,二级数组 [数值，纵坐标]
    private var yValueCoos: Array<DoubleArray?>? = null
    //价格精度
    private var precision = 6
    //主图X轴价格坐标,二级数组 [时间，横坐标起点]
    private var xValueCoos: Array<DoubleArray?>? = null

    private var showCount = SHOW_COUNT_MIN + SHOW_COUNT_MIN //显示数量

    private val maxValue = 0
    private var minValue: Int = 0
    private val maxTime: Long = 0
    private var minTime: kotlin.Long = 0

    private val isHideSub = false //是否隐藏副图
    private var currentKlinePage = 0
    private var isLoadingMore = false

    //正在显示的节点
    private var showingKLineChartItemList: MutableList<KLineChartItem?>? = null
    //正在显示的时间区域
    private var showingMinTime: Long = 0
    private var showingMaxTime: Long = 0
    private var timeStep = TimeStep.MIN_15
    //主图最值
    private var showingMinValue = 0.0
    private var showingMaxValue = 0.0
    private var showingKMinValue = -1.0
    private var showingKMaxValue = -1.0
    private var __Length = 100f
    private var kMaxIndex = 0
    private var kMinIndex: Int = 0
    private var kMaxY = 0.0
    private var kMinY: kotlin.Double = 0.0
    //交易量最大值
    private var showingMaxVol = 0.0
    //副图最值
    private var showingSubMinValue = 0.0
    private var showingSubMaxValue = 0.0

    //数字格式化
    private val dateFormat = "yyyy/MM/dd HH:mm"
    private val dateFormatX = "MM/dd HH:mm"

    //显示数量
    private var showFirstIndex = 0
    private var showLastIndex = 0
    //滚动
    private var mScroller: Scroller? = null
    private var mVelocityTracker: VelocityTracker? = null

    private var mTouchSlop = 0
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mOverscrollDistance = 0
    private var mOverflingDistance = 0

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mActivePointerId = 0

    private val INVALID_POINTER = -1

    private var lastMotionX1 = -1f
    private var lastMotionY1 = -1f
    private var lastMotionX2 = -1f
    private var lastMotionY2 = -1f

    private var isLongClick = false
    //选中的节点index
    private var selectedIndex = -1

    private val INVALID_Y = -10000f
    private var currentPrice //当前价格
            = 0.0
    private var currentPriceY = INVALID_Y //当前价格坐标
            .toDouble()
    private var currentPricePopRect //当前价格右边箭头区域
            : RectF? = null

    private val basePaint = Paint()
    private val selectPaint = Paint()
    private val textPaint = Paint()
    private var textHeight = 0f
    private val popPointerPaint = Paint()
    private val popTextPaint = Paint()
    private var popTextHeight = 0f
    private val popPriceTextPaint = Paint()
    private var popPriceTextHeight = 0f
    private val linePaint = Paint()
    private val hatchPaint = Paint()
    private val graphPaint = Paint()
    private val currentPriceLinePaint = Paint()

    private val LONG_CLICK_TIME: Long = 500
    private var longClickCheckListener: LongClickCheckListener? = null

    private var logoDrawable: Drawable? = null
    private var numberDefault: String? = null
    private var stepNoneLightDrawable: AnimatedVectorDrawable? = null

    internal inner class LongClickCheckListener(private val ev: MotionEvent) : Runnable {
        override fun run() {
            isLongClick = true
            onLongClick(ev)
        }

    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        init()
    }

    private fun init() {
        //        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        numberDefault = context.getString(R.string.number_default)
        //设置 TimeStep Text
        TimeStep.NONE.text = context.getString(R.string.min_none)
        TimeStep.MIN_1.text = context.getString(R.string.min_1)
        TimeStep.MIN_5.text = context.getString(R.string.min_5)
        TimeStep.MIN_15.text = context.getString(R.string.min_15)
        TimeStep.MIN_30.text = context.getString(R.string.min_30)
        TimeStep.HOUR_1.text = context.getString(R.string.hour_1)
        TimeStep.HOUR_4.text = context.getString(R.string.hour_4)
        TimeStep.DAY_1.text = context.getString(R.string.day_1)
        TimeStep.WEEK_1.text = context.getString(R.string.week_1)
        TimeStep.MONTH_1.text = context.getString(R.string.mon_1)
        TimeStep.MORE.text = context.getString(R.string.more)
        logoDrawable = SkinCompatResources.getDrawable(context, R.drawable.icon_k_line_logo)
//        logoDrawable = null
        stepNoneLightDrawable = context.getDrawable(R.drawable.icon_analytic_chart_none_price_anim) as AnimatedVectorDrawable
        val dm = resources.displayMetrics
        density = dm.density
        ROUND_BLANK = ConstData.ROUND_BLANK * density
        TOP_TEXT_HEIGHT = ConstData.TOP_TEXT_HEIGHT * density
        BOTTOM_TEXT_HEIGHT = ConstData.BOTTOM_TEXT_HEIGHT * density
        TEXT_SPACING = ConstData.TEXT_SPACING * density
        MACD_COLUMN_WIDTH = ConstData.MACD_COLUMN_WIDTH * density
        POPPER_TEXT_PADDING = ConstData.POPPER_TEXT_PADDING * density
        POPPER_POINT_INNER_RADIUS = ConstData.POPPER_POINT_INNER_RADIUS * density
        POPPER_POINT_OUTER_RADIUS = ConstData.POPPER_POINT_OUTER_RADIUS * density
        //        NONE_STEP_COLOR = SkinCompatResources.getColor(getContext(), R.color.T7);
//        START_COLOR = CommonUtil.setColorAlpha(NONE_STEP_COLOR, 0.6f);
//        NONE_COLOR = CommonUtil.setColorAlpha(NONE_STEP_COLOR, 0);
        NONE_STEP_COLOR = -0xbf9317
        START_COLOR = CommonUtil.setColorAlpha(SkinCompatResources.getColor(context, R.color.C8), 0.8f)
        NONE_COLOR = CommonUtil.setColorAlpha(SkinCompatResources.getColor(context, R.color.C7), 0.6f)
        SELECT_BG_COLOR = SkinCompatResources.getColor(context, R.color.C2_ALPHA30)
        BASE_LINE_COLOR = SkinCompatResources.getColor(context, R.color.L1_ALPHA60)
        COORDINATE_TEXT_COLOR = SkinCompatResources.getColor(context, R.color.T2)
        CURRENT_PRICE_LINE_COLOR = SkinCompatResources.getColor(context, R.color.C1)
        CURRENT_PRICE_BG_COLOR = SkinCompatResources.getColor(context, R.color.C1)
        CURRENT_PRICE_TEXT_COLOR = -0x70607
        POPPER_BORDER_COLOR = SkinCompatResources.getColor(context, R.color.C3)
        POPPER_BG_COLOR = SkinCompatResources.getColor(context, R.color.C3)
        POPPER_TITLE_COLOR = SkinCompatResources.getColor(context, R.color.T4)
        POPPER_TEXT_COLOR = SkinCompatResources.getColor(context, R.color.T4)
        POPPER_PRICE_COLOR = SkinCompatResources.getColor(context, R.color.T4)
        POPPER_LINE_COLOR = SkinCompatResources.getColor(context, R.color.C3)
        POPPER_POINTER_INNER_COLOR = SkinCompatResources.getColor(context, R.color.T1)
        //        POPPER_POINTER_OUTER_COLOR = 0x66364365;
        MAXMIN_COLOR = SkinCompatResources.getColor(context, R.color.T2)
        WIN_COLOR =  SkinCompatResources.getColor(context, R.color.T17)
        LOSE_COLOR = SkinCompatResources.getColor(context, R.color.T16)
        MA5_COLOR = SkinCompatResources.getColor(context, R.color.T18)
        MA10_COLOR = SkinCompatResources.getColor(context, R.color.T19)
        MA30_COLOR = SkinCompatResources.getColor(context, R.color.T20)
        MA_OTHER01_COLOR = -0x9c6c5
        MA_OTHER02_COLOR = -0x8f2ff9
        MA_OTHER03_COLOR = -0x90df02
        BOLL_COLOR = MA5_COLOR
        UB_COLOR = MA10_COLOR
        LB_COLOR = MA30_COLOR
        VOL_COLOR = MA_OTHER03_COLOR
        VOL_MA5_COLOR = MA5_COLOR
        VOL_MA10_COLOR = MA10_COLOR
        MACD_COLOR = MA_OTHER03_COLOR
        DIF_COLOR = MA5_COLOR
        DEA_COLOR = MA10_COLOR
        K_COLOR = MA5_COLOR
        D_COLOR = MA10_COLOR
        J_COLOR = MA30_COLOR
        RSI_COLOR = MA5_COLOR
        WR_COLOR = MA5_COLOR
        __Length = 30 * density
        mPaddingLeft = paddingLeft + ROUND_BLANK
        mPaddingTop = paddingTop + ROUND_BLANK
        mPaddingRight = paddingRight + ROUND_BLANK
        mPaddingBottom = paddingBottom + ROUND_BLANK
        mScroller = Scroller(context, DecelerateInterpolator())
        isFocusable = true
        setWillNotDraw(false)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mOverscrollDistance = configuration.scaledOverscrollDistance
        mOverflingDistance = configuration.scaledOverflingDistance

        basePaint.isAntiAlias = true
        basePaint.style = Paint.Style.FILL
        basePaint.strokeWidth = 0.5f * density

        selectPaint.isAntiAlias = true
        selectPaint.style = Paint.Style.FILL
        selectPaint.strokeWidth = 2f

        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 3f
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_10).toFloat()

        textHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent
        popPointerPaint.isAntiAlias = true
        popPointerPaint.style = Paint.Style.FILL
        popTextPaint.isAntiAlias = true
        popTextPaint.style = Paint.Style.FILL
        popTextPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_10).toFloat()

        popTextHeight = popTextPaint.fontMetrics.descent - popTextPaint.fontMetrics.ascent
        popPriceTextPaint.isAntiAlias = true
        popPriceTextPaint.style = Paint.Style.FILL
        popPriceTextPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_10).toFloat()

        popPriceTextHeight = popPriceTextPaint.fontMetrics.descent - popPriceTextPaint.fontMetrics.ascent
        hatchPaint.isAntiAlias = true
        hatchPaint.style = Paint.Style.FILL_AND_STROKE

        graphPaint.isAntiAlias = true
        graphPaint.color = START_COLOR
        graphPaint.style = Paint.Style.STROKE
        graphPaint.strokeWidth = 1f

        currentPriceLinePaint.isAntiAlias = true
        currentPriceLinePaint.strokeWidth = density * 1
        currentPriceLinePaint.pathEffect = DashPathEffect(floatArrayOf(4 * density, 4 * density), 0f)
        currentPriceLinePaint.color = CURRENT_PRICE_LINE_COLOR
        currentPriceLinePaint.style = Paint.Style.FILL
    }

    //  添加测试数据
    private fun resetTestValues() {
        data = if (data == null) Data() else data
        refreshShowData(true)
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    fun setLoadingMore(loading:Boolean){
        this.isLoadingMore = loading
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            (paddingLeft + WIDTH * density + paddingRight).toInt()
        }
        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            (paddingTop + HEIGHT * density + paddingBottom).toInt()
        }
        Log.d("ttt--->height",height.toString())
        setMeasuredDimension(width, height+200)
    }

    var dispatchPointerId = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //横向滑动控件处理，纵向滑动，父窗口处理
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                dispatchPointerId = ev.getPointerId(0)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> if (parentTouch) {
                parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(ev)
    }

    var parentTouch = false
    var mineTouch = false

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        mVelocityTracker?.addMovement(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                parentTouch = false
                mineTouch = false
                mActivePointerId = ev.getPointerId(0)
                if (isLongClick) {
                } else {
                    if (true != mScroller?.isFinished) {
                        mScroller?.abortAnimation()
                    }
                    mLastMotionX = ev.x
                    mLastMotionY = ev.y
                    longClickCheckListener = LongClickCheckListener(ev)
                    postDelayed(longClickCheckListener, LONG_CLICK_TIME)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mLastMotionX = ev.getX(index)
                mLastMotionY = ev.getY(index)
                mActivePointerId = ev.getPointerId(index)
                unCheckLongClick()
            }
            MotionEvent.ACTION_MOVE -> if (!parentTouch) {
                val count = ev.pointerCount
                if (count == 1) {
                    val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                    val x = ev.getX(activePointerIndex)
                    val deltaX = mLastMotionX - x
                    val y = ev.getY(activePointerIndex)
                    val deltaY = mLastMotionY - y
                    //                    if (Math.abs(deltaX) > 3 * density && isLongClick) {
//                        unCheckLongClick();
//                    }
                    if (isLongClick) {
                        val clickTime = ev.eventTime - ev.downTime
                        if (clickTime >= 200) { //点击事件
                            onLongClick(ev)
                        }
                    } else {
                        if (!mineTouch && !parentTouch) {
                            if (Math.abs(deltaY) > Math.abs(deltaX) * 1.5) {
                                parentTouch = true
                            } else {
                                mineTouch = true
                            }
                        }
                        if (!parentTouch) {
                            if (Math.abs(deltaX) >= itemWidth / 2) {
                                unCheckLongClick()
                                imitateScroll(deltaX)
                                mLastMotionX = x
                            } else { //没有移动
                            }
                        }
                    }
                    //                    final int oldX = getScrollX();
////                    final int oldY = getScrollY();
////
////                    final int rangeX = getScrollRangeX();
////                    final int rangeY = getScrollRangeY();
////                    final int overscrollMode = getOverScrollMode();
////                    final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
////                            (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS
////                                    && (rangeX > 0 || rangeY > 0));
////                    if (overScrollBy(deltaX, deltaY, getScrollX(), getScrollY(), rangeX, rangeY, mOverscrollDistance, 0, true)) {
////                        mVelocityTracker.clear();
////                    }
////                    onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
                } else if (count >= 2) {
                    unCheckLongClick()
                    scaleOnTouchModel(ev)
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val clickTime = ev.eventTime - ev.downTime
                if (clickTime < 200) { //点击事件
                    unCheckLongClick()
                    if (isClickCurrentPricePop(ev)) {
                        scrollToEnd()
                    }
                }
                if (isLongClick) {
                } else {
                    unCheckLongClick()
                    onTouchPointerUp(ev)
                    val velocityTracker = mVelocityTracker!!
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocityX = velocityTracker.getXVelocity(mActivePointerId).toInt()
                    if (Math.abs(initialVelocityX) / 2 > mMinimumVelocity) {
                        fling(-initialVelocityX)
                    }
                    mActivePointerId = INVALID_POINTER
                    recycleVelocityTracker()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                unCheckLongClick()
                onTouchPointerUp(ev)
                onSecondaryPointerUp(ev)
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId))
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId))
            }
            MotionEvent.ACTION_CANCEL -> {
                unCheckLongClick()
                mActivePointerId = INVALID_POINTER
                recycleVelocityTracker()
            }
        }
        return true
    }

    private fun unCheckLongClick() {
        if (longClickCheckListener != null) {
            removeCallbacks(longClickCheckListener)
            longClickCheckListener = null
            isLongClick = false
            selectedIndex = -1
            postInvalidate()
        }
    }

    private fun onLongClick(ev: MotionEvent) {
        val selectX = ev.x
        val index = ((selectX - mPaddingLeft) * showCount / chartWidth).toInt()
        if (index > -1 && index < (showingKLineChartItemList?.size ?: 0)) {
            selectedIndex = index
            postInvalidate()
        }
    }

    //滚动到最后
    private fun scrollToEnd() {
        val scrollRange: Float = (SHOW_COUNT_MAX - showLastIndex - 1) * itemWidth + RIGHT_BLANK_WIDTH_MAX - currentRightBlankWidth
        val maxRange = getMaxRange()
        if (maxRange != 0f) {
            mScroller?.startScroll(0, 0, scrollRange.toInt(), 0, 500)
            flingX = 0f
            onFling()
        }
    }

    //判断是否点击在当前价格的弹窗内
    private fun isClickCurrentPricePop(ev: MotionEvent): Boolean {
        if (currentPricePopRect != null) {
            val clickX = ev.x
            val clickY = ev.y
            return currentPricePopRect!!.left <= clickX && currentPricePopRect!!.right >= clickX && currentPricePopRect!!.top <= clickY && currentPricePopRect!!.bottom >= clickY
        }
        return false
    }


    private fun getMyScrollX(): Float {
        return -(showFirstIndex * itemWidth)
    }

    private fun getLeftScrollRange(): Float {
        return showFirstIndex * itemWidth
    }

    private fun getRightScrollRange(): Float {
        return (SHOW_COUNT_MAX - showLastIndex - 1) * itemWidth
    }

    private fun getMaxRange(): Float {
        return (SHOW_COUNT_MAX - showCount) * itemWidth
    }

    //模拟滚动界面 dx < 0 向右滑； dx > 0 向左滑；
    private fun imitateScroll(dx: Float) {
        var scrollCount = (abs(dx) / itemWidth).toInt()
        val exd = abs(dx) % itemWidth
        scrollCount = if (exd >= itemWidth / 2) scrollCount + 1 else scrollCount
        if (dx < 0) {
            if (showFirstIndex > 0) {
                if (currentRightBlankWidth <= 0) { //已滚动到最后，改变显示数据位置
                    var scrollToFirstIndex = showFirstIndex - scrollCount
                    scrollToFirstIndex = if (scrollToFirstIndex < 0) 0 else scrollToFirstIndex
                    val realScrollCount = showFirstIndex - scrollToFirstIndex
                    showFirstIndex = scrollToFirstIndex
                    showLastIndex -= realScrollCount
                }
                currentRightBlankWidth -= scrollCount * itemWidth
                currentRightBlankWidth = if (currentRightBlankWidth < 0) 0f else currentRightBlankWidth
                currentRightBlankWidth = if (currentRightBlankWidth > RIGHT_BLANK_WIDTH_MAX) RIGHT_BLANK_WIDTH_MAX else currentRightBlankWidth
                refreshShowData(false)
                postInvalidate()
            } else { //通知加载更多
                if (analyticChartHelper != null && lastPage > 0) {
                    if(!isLoadingMore){
                        currentKlinePage = lastPage + 1
                        analyticChartHelper?.onLoadMore(currentKlinePage)
                    }
                }
            }
        } else if (dx > 0) {
            if (showLastIndex < SHOW_COUNT_MAX - 1) { //最后一条没有被显示
                var scrollToLastIndex = showLastIndex + scrollCount
                scrollToLastIndex = if (scrollToLastIndex >= SHOW_COUNT_MAX - 1) SHOW_COUNT_MAX - 1 else scrollToLastIndex
                val realScrollCount = scrollToLastIndex - showLastIndex
                showFirstIndex += realScrollCount
                showLastIndex = scrollToLastIndex
                val blankCount = scrollCount - realScrollCount
                if (blankCount > 0) { //滑出之后还有空余，需要加上右边空白一起滑动
                    currentRightBlankWidth += blankCount * itemWidth
                    currentRightBlankWidth = if (currentRightBlankWidth < 0) 0f else currentRightBlankWidth
                    currentRightBlankWidth = if (currentRightBlankWidth > RIGHT_BLANK_WIDTH_MAX) RIGHT_BLANK_WIDTH_MAX else currentRightBlankWidth
                }
                refreshShowData(false)
                postInvalidate()
            } else if (currentRightBlankWidth < RIGHT_BLANK_WIDTH_MAX) {
                currentRightBlankWidth += scrollCount * itemWidth
                currentRightBlankWidth = if (currentRightBlankWidth < 0) 0f else currentRightBlankWidth
                currentRightBlankWidth = if (currentRightBlankWidth > RIGHT_BLANK_WIDTH_MAX) RIGHT_BLANK_WIDTH_MAX else currentRightBlankWidth
                refreshShowData(false)
                postInvalidate()
            }
        }
    }

    var flingX = 0f

    private fun fling(velocityX: Int) {
        val rightScrollRange = getRightScrollRange()
        mScroller?.fling((-rightScrollRange).toInt(), 0, velocityX, 0, (-getMaxRange()).toInt(), 0, 0, 0)
        //        mScroller.fling(getScrollX(), 0, velocityX, 0, 0, getWidth(), 0, 0);
        flingX = -rightScrollRange
        onFling()
    }

    private fun onFling() {
        if (true == mScroller?.computeScrollOffset()) {
            val currX = mScroller?.currX ?: 0
            val dx = currX - flingX
            if (Math.abs(dx) >= itemWidth / 2) {
                flingX = currX.toFloat()
                imitateScroll(dx + density * 4)
            }
            postDelayed({ onFling() }, FLING_REFRESH_TIME)
        } else {
            val currX = mScroller?.currX ?: 0
            val dx = currX - flingX
            if (Math.abs(dx) >= itemWidth / 2) {
                flingX = currX.toFloat()
                imitateScroll(dx + density * 4)
            }
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr
                MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
            if (mVelocityTracker != null) {
                mVelocityTracker?.clear()
            }
        }
    }

    private fun scaleOnTouchModel(ev: MotionEvent) {
        if (lastMotionX1 == -1f && lastMotionY1 == -1f && lastMotionX2 == -1f && lastMotionY2 == -1f) {
            lastMotionX1 = ev.getX(0)
            lastMotionY1 = ev.getY(0)
            lastMotionX2 = ev.getX(1)
            lastMotionY2 = ev.getY(1)
        } else {
            val x1 = ev.getX(0)
            val y1 = ev.getY(0)
            val x2 = ev.getX(1)
            val y2 = ev.getY(1)
            val distance = Math.sqrt(Math.pow(x2 - x1.toDouble(), 2.0) + Math.pow(y2 - y1.toDouble(), 2.0))
            val distanceLast = Math.sqrt(Math.pow(lastMotionX2 - lastMotionX1.toDouble(), 2.0) + Math.pow(lastMotionY2 - lastMotionY1.toDouble(), 2.0))
            val dx = distance - distanceLast
            if (Math.abs(dx) >= itemWidth) {
                onScale(dx)
                lastMotionX1 = x1
                lastMotionY1 = y1
                lastMotionX2 = x2
                lastMotionY2 = y2
            }
        }
    }

    private fun onTouchPointerUp(ev: MotionEvent) {
        val count = ev.pointerCount
        if (count < 2) {
            lastMotionY2 = -1f
            lastMotionX2 = lastMotionY2
            lastMotionY1 = lastMotionX2
            lastMotionX1 = lastMotionY1
        }
    }

    fun setShowCount(showCount: Int) {
        this.showCount = showCount
        this.showCount = Math.max(this.showCount, SHOW_COUNT_MIN)
        this.showCount = Math.min(this.showCount, SHOW_COUNT_MAX)
        refreshShowData(true)
    }

    //缩放 dx < 0 缩小，dx > 0 放大
    private fun onScale(dx: Double) {
        var scaleCount = (Math.abs(dx) / itemWidth).toInt() //缩放数量
        if (dx < 0) {
            var newShowCount = showCount + scaleCount
            newShowCount = if (newShowCount > SHOW_COUNT_MAX) SHOW_COUNT_MAX else newShowCount
            scaleCount = newShowCount - showCount
            showCount = newShowCount
            if (showLastIndex < SHOW_COUNT_MAX - 1) {
                showLastIndex += scaleCount
                showLastIndex = if (showLastIndex >= SHOW_COUNT_MAX - 1) SHOW_COUNT_MAX - 1 else showLastIndex
            }
            showFirstIndex = showLastIndex - showCount + 1
            refreshShowData(true)
            postInvalidate()
        } else if (dx > 0) {
            var newShowCount = showCount - scaleCount
            newShowCount = if (newShowCount < SHOW_COUNT_MIN) SHOW_COUNT_MIN else newShowCount
            scaleCount = showCount - newShowCount
            showCount = newShowCount
            showFirstIndex += scaleCount
            refreshShowData(true)
            postInvalidate()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshSize()
    }

    var ishow = false

    override fun onDraw(canvas: Canvas) { //        super.onDraw(canvas);
//Log.e(TAG, "onDraw:===========");
        canvas.drawColor(SkinCompatResources.getColor(context, R.color.B2))
        canvas.save()
        drawBaseChart(canvas)
        //		canvas.scale(scaleX, scaleY);
        canvas.translate(mPaddingLeft, mPaddingTop + TOP_TEXT_HEIGHT)
        try { //显示选中节点背景
            showSelectedNodeBg(canvas)
            //显示主图曲线图
            showMainGraph(canvas)
            //显示交易量
            canvas.translate(0f, mainGraphHeight + textHeight)
            showVolume(canvas)
            //显示副图
            canvas.translate(0f, businessHeight + textHeight)
            showSubGraph(canvas)
            canvas.restore()
            canvas.save()
            canvas.translate(mPaddingLeft, mPaddingTop + TOP_TEXT_HEIGHT)
            //显示文字
            showTexts(canvas)
            //显示弹出窗口
            showPopper(canvas)
            //测试
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "123197胜多负少13674443")
//                    .draw(canvas, 200, 20, 0);
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "CENTER123197胜多负少13674443")
//                    .draw(canvas, 200, 100, Gravity.CENTER);
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "LEFT  CENTER_VERTICAL 123197胜多负少13674443")
//                    .draw(canvas, 200, 200, Gravity.LEFT | Gravity.CENTER_VERTICAL);
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "RIGHT CENTER_VERTICAL 123197胜多负少13674443")
//                    .draw(canvas, 200, 300, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "RIGHT TOP 123197胜多负少13674443")
//                    .draw(canvas, 200, 400, Gravity.RIGHT | Gravity.TOP);
//            new TypefaceTextPaintHelper(getContext(), textPaint, Typeface.NORMAL, "RIGHT BOTTOM 123197胜多负少13674443")
//                    .draw(canvas, 200, 500, Gravity.RIGHT | Gravity.BOTTOM);
        } catch (e: Exception) {
            printError(e)
        }
        canvas.restore()
    }

    private fun showSelectedNodeBg(canvas: Canvas) {
        if (selectedIndex != -1 && selectedIndex < showCount) {
            val item = showingKLineChartItemList!![selectedIndex]
            item?.let {
                val centerX = item.x
                selectPaint.style = Paint.Style.FILL
                selectPaint.color = SELECT_BG_COLOR
                canvas.drawRect(centerX - itemWidth / 2, 0f, centerX + itemWidth / 2, chartHeight, selectPaint)
            }
        }
    }

    //绘制基础图表线条
    private fun drawBaseChart(canvas: Canvas) {
        basePaint.color = BASE_LINE_COLOR
        if (logoDrawable != null) {
            canvas.save()
            val left = (mPaddingLeft + 10 * density).toInt()
            val top = (mainGraphHeight + mPaddingTop + TOP_TEXT_HEIGHT - 10 * density - 20 * density).toInt()
            val right = (left + 85 * density).toInt()
            val bottom = (top + 30 * density).toInt()
            logoDrawable?.setBounds(left, top, right, bottom)
            logoDrawable?.draw(canvas)
            canvas.restore()
        }
        //横向6条线
        var top = chartTop
        for (i in 0..5) {
            canvas.drawLine(chartLeft, top, chartRight, top, basePaint)
            top += cellHeight
        }
        //竖向3条线
        var left = chartLeft
        for (i in 0..2) {
            left += cellWidth
            canvas.drawLine(left, 0f, left, chartBottom, basePaint)
        }
    }

    private fun formatPrice(price: Double?): String {
        return NumberUtil.formatNumberNoGroupHardScale(price, precision)
    }

    //显示文字
    private fun showTexts(canvas: Canvas) {
        if (showingKLineChartItemList == null || showingKLineChartItemList!!.isEmpty()) {
            return
        }
        canvas.save()
        var selectedKLineChartItem: KLineChartItem? = null
        if (selectedIndex != -1) {
            selectedKLineChartItem = CommonUtil.getItemFromList(showingKLineChartItemList, selectedIndex)
        }
        //显示主图文字
        var offset = 0f
        if (timeStep !== TimeStep.NONE) {
            if (type and MA == MA) {
                textPaint.color = MA5_COLOR
                val ma5v = selectedKLineChartItem?.MA5 ?: data?.MA5
                val ma5 = "MA5:" + if (ma5v == 0.0) context.getString(R.string.number_default) else formatPrice(ma5v)
                val ma5Helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, ma5)
                ma5Helper.draw(canvas, offset, -textHeight / 2, Gravity.CENTER_VERTICAL)
                offset += ma5Helper.length + TEXT_SPACING
                //            canvas.drawText(ma5, 0, getShowTextCenterVerticalY(-textHeight / 2, textPaint), textPaint);
//            offset += textPaint.measureText(ma5) + TEXT_SPACING;
                textPaint.color = MA10_COLOR
                val ma10v = selectedKLineChartItem?.MA10 ?: data?.MA10
                val ma10 = "MA10:" + if (ma10v == 0.0) context.getString(R.string.number_default) else formatPrice(ma10v)
                val ma10Helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, ma10)
                ma10Helper.draw(canvas, offset, -textHeight / 2, Gravity.CENTER_VERTICAL)
                //            float m10y = getShowTextCenterVerticalY(-textHeight / 2, textPaint);
//            offset += textPaint.measureText(ma10) + TEXT_SPACING;
                offset += ma10Helper.length + TEXT_SPACING
                textPaint.color = MA30_COLOR
                val ma30v = selectedKLineChartItem?.MA30 ?: data?.MA30
                val ma30 = "MA30:" + if (ma30v == 0.0) context.getString(R.string.number_default) else formatPrice(ma30v)
                val ma30Helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, ma30)
                ma30Helper.draw(canvas, offset, -textHeight / 2, Gravity.CENTER_VERTICAL)
                //            canvas.drawText(ma30, offset, getShowTextCenterVerticalY(-textHeight / 2, textPaint), textPaint);
            } else if (type and BOLL == BOLL) {
                textPaint.color = BOLL_COLOR
                val bollv = selectedKLineChartItem?.BOLL ?: data?.BOLL
                val boll = "BOLL:" + formatPrice(bollv)
                canvas.drawText(boll, offset, 0f, textPaint)
                offset += textPaint.measureText(boll) + TEXT_SPACING
                textPaint.color = UB_COLOR
                val ubv = selectedKLineChartItem?.UB ?: data?.UB
                val ub = "UB:" + formatPrice(ubv)
                canvas.drawText(ub, offset, 0f, textPaint)
                offset += textPaint.measureText(ub) + TEXT_SPACING
                textPaint.color = LB_COLOR
                val lbv = selectedKLineChartItem?.LB ?: data?.LB
                val lb = "LB:" + formatPrice(lbv)
                canvas.drawText(lb, offset, 0f, textPaint)
            }
        }
        canvas.restore()
        //显示交易量文字
        canvas.save()
        run {
            canvas.translate(0f, TOP_TEXT_HEIGHT + mainGraphHeight)
            offset = 0f
            textPaint.color = VOL_COLOR
            val volv = selectedKLineChartItem?.VOL ?: data?.VOL
            val vol = "VOL:" + if (volv == 0.0) context.getString(R.string.number_default) else NumberUtil.formatNumberNoGroup(volv, 5, 5)
            //            canvas.drawText(vol, offset, 0, textPaint);
//            offset += textPaint.measureText(vol) + TEXT_SPACING;
            val volHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, vol)
            volHelper.draw(canvas, offset, 0f, 0)
            if (timeStep !== TimeStep.NONE) {
                offset += volHelper.length + TEXT_SPACING
                textPaint.color = VOL_MA5_COLOR
                val volma5v = selectedKLineChartItem?.VOLMA5 ?: data?.VOLMA5
                val volma5 = "MA5:" + if (volma5v == 0.0) context.getString(R.string.number_default) else NumberUtil.formatNumberNoGroup(volma5v, 5, 5)
                //            canvas.drawText(volma5, offset, 0, textPaint);
//            offset += textPaint.measureText(volma5) + TEXT_SPACING;
                val volma5Helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, volma5)
                volma5Helper.draw(canvas, offset, 0f, 0)
                offset += volma5Helper.length + TEXT_SPACING
                textPaint.color = VOL_MA10_COLOR
                val volma10v = selectedKLineChartItem?.VOLMA10 ?: data?.VOLMA10
                val volma10 = "MA10:" + if (volma10v == 0.0) context.getString(R.string.number_default) else NumberUtil.formatNumberNoGroup(volma10v, 5, 5)
                //            canvas.drawText(volma10, offset, 0, textPaint);
                val volma10Helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, volma10)
                volma10Helper.draw(canvas, offset, 0f, 0)
            }
            //交易量最大值
            textPaint.color = COORDINATE_TEXT_COLOR
            if (showingMaxVol != 0.0) {
                val volmax = NumberUtil.formatNumberNoGroup(showingMaxVol, 5, 5)
                //                canvas.drawText(volmax, chartWidth - textPaint.measureText(volmax), 0, textPaint);
                val volmaxHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, volmax)
                volmaxHelper.draw(canvas, chartWidth, 0f, Gravity.RIGHT)
            }
        }
        canvas.restore()
        canvas.save()
        //显示副图文字
        canvas.translate(0f, TOP_TEXT_HEIGHT + mainGraphHeight + businessHeight + textHeight)
        offset = 0f
        if (type and MACD == MACD) {
            textPaint.color = MACD_COLOR
            val macdTitle = "MACD(12,26,9)"
            val macdTitleHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, macdTitle)
            macdTitleHelper.draw(canvas, offset, 0f, 0)
            offset += macdTitleHelper.length + TEXT_SPACING
            val macdv = selectedKLineChartItem?.MACD ?: data?.MACD
            val macd = "MACD:" + formatPrice(macdv)
            val macdHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, macd)
            macdHelper.draw(canvas, offset, 0f, 0)
            offset += macdHelper.length + TEXT_SPACING
            textPaint.color = DIF_COLOR
            val difv = selectedKLineChartItem?.DIF ?: data?.DIF
            val dif = "DIF:" + formatPrice(difv)
            val difHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, dif)
            difHelper.draw(canvas, offset, 0f, 0)
            offset += difHelper.length + TEXT_SPACING
            textPaint.color = DEA_COLOR
            val deav = selectedKLineChartItem?.DEA ?: data?.DEA
            val dea = "DEA:" + formatPrice(deav)
            val deaHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, dea)
            deaHelper.draw(canvas, offset, 0f, 0)
        } else if (type and KDJ == KDJ) {
            textPaint.color = K_COLOR
            val kdjTitle = "KDJ(14,1,3)"
            val kdjTitleHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, kdjTitle)
            kdjTitleHelper.draw(canvas, offset, 0f, 0)
            offset += kdjTitleHelper.length + TEXT_SPACING
            val kv = selectedKLineChartItem?.K ?: data?.K
            val k = "K:" + formatPrice(kv)
            val kHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, k)
            kHelper.draw(canvas, offset, 0f, 0)
            offset += kHelper.length + TEXT_SPACING
            textPaint.color = D_COLOR
            val dv = selectedKLineChartItem?.D ?: data?.D
            val d = "D:" + formatPrice(dv)
            val dHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, d)
            dHelper.draw(canvas, offset, 0f, 0)
            offset += dHelper.length + TEXT_SPACING
            textPaint.color = J_COLOR
            val jv = selectedKLineChartItem?.J ?: data?.J
            val j = "J:" + formatPrice(jv)
            val jHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, j)
            jHelper.draw(canvas, offset, 0f, 0)
        } else if (type and RSI == RSI) {
            textPaint.color = RSI_COLOR
            val rsiv = selectedKLineChartItem?.RSI ?: data?.RSI
            val rsi = "RSI(14):" + formatPrice(rsiv)
            val rsiHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, rsi)
            rsiHelper.draw(canvas, offset, 0f, 0)
        } else if (type and WR == WR) {
            textPaint.color = WR_COLOR
            val wrv = selectedKLineChartItem?.WR ?: data?.WR
            val wr = "WR(14):" + formatPrice(wrv)
            val wrHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, wr)
            wrHelper.draw(canvas, offset, 0f, 0)
        }
        canvas.restore()
        //显示Y轴价格坐标
        if (yValueCoos != null) {
            canvas.save()
            textPaint.color = COORDINATE_TEXT_COLOR
            for (i in yValueCoos!!.indices) {
                if (yValueCoos!![i] != null && yValueCoos!![i]!!.get(0) != 0.0) {
                    val value = NumberUtil.formatNumberNoGroupHardScale(yValueCoos!![i]!![0], precision)
                    val helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, value)
                    helper.draw(canvas, chartWidth, yValueCoos!![i]!![1].toFloat() + textHeight, Gravity.RIGHT)
                    //                    canvas.drawText(value, chartWidth - textPaint.measureText(value), (float) yValueCoos[i][1] + textHeight, textPaint);
                }
            }
            if (showingMinValue != 0.0) {
                val value = NumberUtil.formatNumberNoGroupHardScale(if (showingMinValue < 0) 0.00001 else showingMinValue, precision)
                val helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, value)
                helper.draw(canvas, chartWidth, mainGraphHeight, Gravity.RIGHT)
                //                canvas.drawText(value, chartWidth - textPaint.measureText(value), mainGraphHeight, textPaint);
            }
            canvas.restore()
        }
        //显示X轴时间轴
        if (xValueCoos != null) {
            canvas.save()
            canvas.translate(0f, chartHeight)
            textPaint.color = COORDINATE_TEXT_COLOR
            for (i in xValueCoos!!.indices) {
                if (xValueCoos!![i] != null && xValueCoos!![i]!![0] != 0.0) {
                    val value = CommonUtil.formatTimestamp(dateFormatX, xValueCoos!![i]!![0].toLong() * 1000)
                    val helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, value)
                    if (i == 0) {
                        //第一个靠左
                        helper.draw(canvas, cellWidth * i, textHeight / 2, Gravity.CENTER_VERTICAL)
                        //                        drawTextCenterVertical(canvas, value, textPaint, cellWidth * i, textHeight / 2, Gravity.LEFT);
                    } else if (i == xValueCoos!!.size - 1) {
                        //最后一个靠右
                        helper.draw(canvas, cellWidth * i, textHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
                        //                        drawTextCenterVertical(canvas, value, textPaint, cellWidth * i, textHeight / 2, Gravity.RIGHT);
                    } else {
                        helper.draw(canvas, cellWidth * i, textHeight / 2, Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
                        //                        drawTextCenterVertical(canvas, value, textPaint, cellWidth * i, textHeight / 2, Gravity.CENTER);
                    }
                }
            }
            canvas.restore()
        }
        //显示当前价格
        run {
            val last = CommonUtil.getItemFromArray(data?.kLineChartItems, showLastIndex)
            var isShowRight = false
            if (currentPriceY != INVALID_Y.toDouble()) {
                val centerY = currentPriceY.toFloat()
                val value = NumberUtil.formatNumberNoGroupHardScale(currentPrice, precision)
                textPaint.color = CURRENT_PRICE_TEXT_COLOR
                val helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, value)
                helper.calculateSize()
                val paddingLeft = 5 * density
                val paddingRight = 5 * density
                val paddingTop = 5 * density
                val paddingBottom = 5 * density
                if (last != null && chartWidth - last.x > helper.length + paddingLeft + paddingRight) {
                    //显示在右边
                    //计算背景位置
                    val left = chartWidth - paddingLeft - paddingRight - helper.length
                    val right = chartWidth
                    val top = centerY - helper.height / 2 - paddingTop
                    val bottom = centerY + helper.height / 2 + paddingBottom
                    val textBgPath = Path()
                    textBgPath.moveTo(left, top)
                    textBgPath.lineTo(right, top)
                    textBgPath.lineTo(right, bottom)
                    textBgPath.lineTo(left, bottom)
                    textBgPath.close()
                    currentPriceLinePaint.color = CURRENT_PRICE_LINE_COLOR
                    canvas.drawLine(last.x, centerY, left, centerY, currentPriceLinePaint)
                    currentPriceLinePaint.color = CURRENT_PRICE_BG_COLOR
                    canvas.drawPath(textBgPath, currentPriceLinePaint)
                    helper.draw(canvas, (left + right) / 2, (top + bottom) / 2, Gravity.CENTER)
                    currentPricePopRect = null
                    isShowRight = true
                } else {
                    //计算背景位置
                    val left = (chartWidth - paddingLeft - paddingRight - cellWidth * 0.8).toFloat()
                    val right = left + paddingLeft + paddingRight + helper.length
                    val top = centerY - helper.height / 2 - paddingTop
                    val bottom = centerY + helper.height / 2 + paddingBottom
                    val textBgPath = Path()
                    textBgPath.moveTo(left, top)
                    textBgPath.lineTo(right, top)
                    textBgPath.lineTo(right + helper.height, centerY)
                    textBgPath.lineTo(right, bottom)
                    textBgPath.lineTo(left, bottom)
                    textBgPath.close()
                    currentPriceLinePaint.color = CURRENT_PRICE_LINE_COLOR
                    canvas.drawLine(0f, centerY, chartWidth, centerY, currentPriceLinePaint)
                    currentPriceLinePaint.color = CURRENT_PRICE_BG_COLOR
                    canvas.drawPath(textBgPath, currentPriceLinePaint)
                    helper.draw(canvas, (left + right) / 2, (top + bottom) / 2, Gravity.CENTER)
                    currentPricePopRect = RectF(left + mPaddingLeft, top + mPaddingTop + TOP_TEXT_HEIGHT, mPaddingLeft + right + helper.height, bottom + mPaddingTop + TOP_TEXT_HEIGHT)
                    isShowRight = false
                }
            }
            //绘制最后价格的亮点
            if (isShowRight && timeStep === TimeStep.NONE && last != null && last.outY != INVALID_Y && showLastIndex >= SHOW_COUNT_MAX - 1) { //正在显示最后一个节点
                if (stepNoneLightDrawable != null && stepNoneLightDrawable!!.isVisible) {
                    val rect = Rect((last.x - 6 * density).toInt(), (last.outY - 6 * density).toInt(), (last.x + 6 * density).toInt(), (last.outY + 6 * density).toInt())
                    stepNoneLightDrawable!!.bounds = rect
                    stepNoneLightDrawable?.draw(canvas)
                    //Log.e(TAG, "isRunning:" + stepNoneLightDrawable.isRunning());
                    if (!stepNoneLightDrawable!!.isRunning) {
                        stepNoneLightDrawable?.start()
                    }
                }
            }
        }
        //显示最大和最小
        canvas.save()
        if (timeStep !== TimeStep.NONE) {
            if (showingKMaxValue != -1.0) {
                showKMaxMinValue(canvas, showingKMaxValue, kMaxIndex, kMaxY.toFloat())
            }
            if (showingKMinValue != -1.0) {
                showKMaxMinValue(canvas, showingKMinValue, kMinIndex, kMinY.toFloat())
            }
        }
        canvas.restore()
    }

    override fun onSaveInstanceState(): Parcelable? {
        if (stepNoneLightDrawable != null && stepNoneLightDrawable!!.isVisible) {
            stepNoneLightDrawable?.stop()
        }
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (stepNoneLightDrawable != null && stepNoneLightDrawable!!.isVisible) {
            stepNoneLightDrawable?.start()
        }
    }

    private fun showKMaxMinValue(canvas: Canvas, value: Double, index: Int, y: Float) {
        textPaint.color = MAXMIN_COLOR
        linePaint.color = MAXMIN_COLOR
        //        float x = (float) (itemWidth * (index + 0.5));
        val item = showingKLineChartItemList!![index]
        item?.let {
            val x = item.x
            val text = formatPrice(value)
            val textLength = textPaint.measureText(text)
            val helper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, text)
            if (index > showCount / 2) {
                helper.draw(canvas, x - __Length, y, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
                //            float textBaseY = y + (Math.abs(textPaint.ascent()) - textPaint.descent()) / 2;
//            canvas.drawText(text, x - __Length - textLength, textBaseY, textPaint);
                canvas.drawLine(x - __Length, y, x, y, linePaint)
            } else {
                canvas.drawLine(x, y, x + __Length, y, linePaint)
                helper.draw(canvas, x + __Length, y, Gravity.CENTER_VERTICAL)
                //            float textBaseY = y + (Math.abs(textPaint.ascent()) - textPaint.descent()) / 2;
//            canvas.drawText(text, x + __Length, textBaseY, textPaint);
            }
        }
    }

    private fun getShowTextCenterVerticalY(y: Float, paint: Paint): Float {
        return y + (Math.abs(paint.ascent()) - paint.descent()) / 2
    }

    //显示主图
    private fun showMainGraph(canvas: Canvas) {
        if (showingKLineChartItemList == null || showingKLineChartItemList!!.isEmpty()) {
            return
        }
        //        double itemWidth = (double) chartWidth / showCount;
        val halfItemWidth = itemWidth * 0.4
        if (timeStep !== TimeStep.NONE) { //显示影线
            for (i in showingKLineChartItemList!!.indices) {
                val kLineChartItem = showingKLineChartItemList!![i]
                kLineChartItem?.let {
                    val rectF = RectF()
                    rectF.left = (kLineChartItem.x - halfItemWidth).toFloat()
                    rectF.right = (kLineChartItem.x + halfItemWidth).toFloat()
                    if (kLineChartItem.`in` >= kLineChartItem.out) {
                        //亏损
                        linePaint.color = LOSE_COLOR
                        hatchPaint.color = LOSE_COLOR
                        rectF.top = kLineChartItem.inY
                        rectF.bottom = if (kLineChartItem.outY == kLineChartItem.inY) kLineChartItem.outY + 1 else kLineChartItem.outY
                    } else {
                        //盈利
                        linePaint.color = WIN_COLOR
                        hatchPaint.color = WIN_COLOR
                        rectF.top = if (kLineChartItem.outY == kLineChartItem.inY) kLineChartItem.outY + 1 else kLineChartItem.outY
                        rectF.bottom = kLineChartItem.inY
                    }
                    canvas.drawLine(kLineChartItem.x, kLineChartItem.highY, kLineChartItem.x, kLineChartItem.lowY, linePaint)
                    canvas.drawRect(rectF, hatchPaint)
                }
            }
            //显示曲线
            if (type and MA == MA) { //显示MA图像
                val ma5Path = Path()
                val ma10Path = Path()
                val ma30Path = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        checkAddPath(ma5Path, kLineChartItem.x, kLineChartItem.ma5Y, kLineChartItem.ma5Y.toDouble())
                        checkAddPath(ma10Path, kLineChartItem.x, kLineChartItem.ma10Y, kLineChartItem.ma10Y.toDouble())
                        checkAddPath(ma30Path, kLineChartItem.x, kLineChartItem.ma30Y, kLineChartItem.ma30Y.toDouble())
                    }
                }
                linePaint.color = MA5_COLOR
                canvas.drawPath(ma5Path, linePaint)
                linePaint.color = MA10_COLOR
                canvas.drawPath(ma10Path, linePaint)
                linePaint.color = MA30_COLOR
                canvas.drawPath(ma30Path, linePaint)
            } else if (type and BOLL == BOLL) {
                //显示BOLL图像
                val bollPath = Path()
                val ubPath = Path()
                val lbPath = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        checkAddPath(bollPath, kLineChartItem.x, kLineChartItem.bollY, kLineChartItem.BOLL)
                        checkAddPath(ubPath, kLineChartItem.x, kLineChartItem.ubY, kLineChartItem.UB)
                        checkAddPath(lbPath, kLineChartItem.x, kLineChartItem.lbY, kLineChartItem.LB)
                    }
                }
                linePaint.color = BOLL_COLOR
                canvas.drawPath(bollPath, linePaint)
                linePaint.color = UB_COLOR
                canvas.drawPath(ubPath, linePaint)
                linePaint.color = LB_COLOR
                canvas.drawPath(lbPath, linePaint)
            }
        } else {
            //NONESTEP path
            val noneStepPath = Path()
            var first: KLineChartItem? = null
            var last: KLineChartItem? = null
            var minY = height.toFloat()
            for (i in showingKLineChartItemList!!.indices) {
                val kLineChartItem = showingKLineChartItemList!![i]
                kLineChartItem?.let {
                    val result = checkAddPath(noneStepPath, kLineChartItem.x, kLineChartItem.outY, kLineChartItem.out)
                    if (result) {
                        if (first == null) {
                            first = kLineChartItem
                        }
                        last = kLineChartItem
                        minY = kotlin.math.min(minY, kLineChartItem.outY)
                    }
                }
            }
            //绘制曲线
            linePaint.color = NONE_STEP_COLOR
            canvas.drawPath(noneStepPath, linePaint)
            //绘制阴影
            if (first != null && last != null) {
                val shader: Shader = LinearGradient(0f, minY, 0f, mainGraphHeight, intArrayOf(START_COLOR, NONE_COLOR), null, Shader.TileMode.REPEAT)
                graphPaint.shader = shader
                graphPaint.style = Paint.Style.FILL
                noneStepPath.lineTo(last!!.x, mainGraphHeight)
                noneStepPath.lineTo(first!!.x, mainGraphHeight)
                canvas.drawPath(noneStepPath, graphPaint)
            }
        }
    }

    private fun checkAddPath(path: Path, x: Float, y: Float, value: Double): Boolean {
        if (value != 0.0 && y != INVALID_Y) {
            if (path.isEmpty) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            return true
        }
        return false
    }

    //显示交易量
    private fun showVolume(canvas: Canvas) {
        if (showingKLineChartItemList == null || showingKLineChartItemList!!.isEmpty()) {
            return
        }
        canvas.save()
        //        double itemWidth = (double) chartWidth / showCount;
        if (timeStep !== TimeStep.NONE) {
            val halfItemWidth = itemWidth * 0.4
            val ma5Path = Path()
            val ma10Path = Path()
            for (i in showingKLineChartItemList!!.indices) {
                val kLineChartItem = showingKLineChartItemList!![i]
                kLineChartItem?.let {
                    val rectF = RectF()
                    rectF.left = (kLineChartItem.x - halfItemWidth).toFloat()
                    rectF.right = (kLineChartItem.x + halfItemWidth).toFloat()
                    rectF.top = kLineChartItem.volY
                    rectF.bottom = businessHeight
                    if (kLineChartItem.`in` >= kLineChartItem.out) { //亏损
                        hatchPaint.color = LOSE_COLOR
                    } else { //盈利
                        hatchPaint.color = WIN_COLOR
                    }
                    checkAddPath(ma5Path, kLineChartItem.x, kLineChartItem.volma5Y, kLineChartItem.VOLMA5)
                    checkAddPath(ma10Path, kLineChartItem.x, kLineChartItem.volma10Y, kLineChartItem.VOLMA10)
                    canvas.drawRect(rectF, hatchPaint)
                }
            }
            linePaint.color = VOL_MA5_COLOR
            canvas.drawPath(ma5Path, linePaint)
            linePaint.color = VOL_MA10_COLOR
            canvas.drawPath(ma10Path, linePaint)
        } else {
            val halfItemWidth = 1 * density.toDouble()
            for (i in showingKLineChartItemList!!.indices) {
                val kLineChartItem = showingKLineChartItemList!![i]
                kLineChartItem?.let {
                    val rectF = RectF()
                    rectF.left = (kLineChartItem.x - halfItemWidth).toFloat()
                    rectF.right = (kLineChartItem.x + halfItemWidth).toFloat()
                    rectF.top = kLineChartItem.volY
                    rectF.bottom = businessHeight
                    hatchPaint.color = NONE_STEP_COLOR
                    canvas.drawRect(rectF, hatchPaint)
                }
            }
        }
        canvas.restore()
    }

    //显示副图
    private fun showSubGraph(canvas: Canvas) {
        if (showingKLineChartItemList == null || showingKLineChartItemList!!.isEmpty()) {
            return
        }
        when {
            type and MACD == MACD -> {
                val difPath = Path()
                val deaPath = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        val rectF = RectF()
                        rectF.left = kLineChartItem.x - MACD_COLUMN_WIDTH / 2
                        rectF.right = kLineChartItem.x + MACD_COLUMN_WIDTH / 2
                        if (kLineChartItem.MACD > 0) {
                            rectF.top = kLineChartItem.macdY
                            rectF.bottom = macdZeroY
                            hatchPaint.color = WIN_COLOR
                        } else {
                            rectF.top = macdZeroY
                            rectF.bottom = kLineChartItem.macdY
                            hatchPaint.color = LOSE_COLOR
                        }
                        canvas.drawRect(rectF, hatchPaint)
                        checkAddPath(difPath, kLineChartItem.x, kLineChartItem.difY, kLineChartItem.DIF)
                        checkAddPath(deaPath, kLineChartItem.x, kLineChartItem.deaY, kLineChartItem.DEA)
                    }
                }
                linePaint.color = DIF_COLOR
                canvas.drawPath(difPath, linePaint)
                linePaint.color = DEA_COLOR
                canvas.drawPath(deaPath, linePaint)
            }
            type and KDJ == KDJ -> {
                val kPath = Path()
                val dPath = Path()
                val jPath = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        checkAddPath(kPath, kLineChartItem.x, kLineChartItem.kY, kLineChartItem.K)
                        checkAddPath(dPath, kLineChartItem.x, kLineChartItem.dY, kLineChartItem.D)
                        checkAddPath(jPath, kLineChartItem.x, kLineChartItem.jY, kLineChartItem.J)
                    }
                }
                linePaint.color = K_COLOR
                canvas.drawPath(kPath, linePaint)
                linePaint.color = D_COLOR
                canvas.drawPath(dPath, linePaint)
                linePaint.color = J_COLOR
                canvas.drawPath(jPath, linePaint)
            }
            type and RSI == RSI -> {
                val rsiPath = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        checkAddPath(rsiPath, kLineChartItem.x, kLineChartItem.rsiY, kLineChartItem.RSI)
                    }
                }
                linePaint.color = RSI_COLOR
                canvas.drawPath(rsiPath, linePaint)
            }
            type and WR == WR -> {
                val wrPath = Path()
                for (i in showingKLineChartItemList!!.indices) {
                    val kLineChartItem = showingKLineChartItemList!![i]
                    kLineChartItem?.let {
                        checkAddPath(wrPath, kLineChartItem.x, kLineChartItem.wrY, kLineChartItem.WR)
                    }
                }
                linePaint.color = WR_COLOR
                canvas.drawPath(wrPath, linePaint)
            }
        }
    }

    //显示弹窗
    private fun showPopper(canvas: Canvas) {
        if (selectedIndex != -1 && selectedIndex < showCount) {
            canvas.save()
            val kLineChartItem = CommonUtil.getItemFromList(showingKLineChartItemList, selectedIndex)
            if (kLineChartItem != null) {
                val y = kLineChartItem.outY
                val x = kLineChartItem.x
                val text = formatPrice(kLineChartItem.out)
                popPriceTextPaint.color = POPPER_PRICE_COLOR
                val priceHelper = TypefaceTextPaintHelper(context, popPriceTextPaint, Typeface.BOLD, text)
                priceHelper.calculateSize()
                val textWidth = priceHelper.length
                val textHeight = priceHelper.height
                //显示选中圆点
//                popPointerPaint.setColor(POPPER_POINTER_OUTER_COLOR);
//                canvas.drawCircle(x, y, POPPER_POINT_OUTER_RADIUS, popPointerPaint);
                popPointerPaint.color = POPPER_POINTER_INNER_COLOR
                if (selectedIndex > showCount / 2) {
                    //点击右边
                    //计算文字背景框位置
                    val top: Float = y - textHeight / 2 - POPPER_TEXT_PADDING
                    val bottom: Float = y + textHeight / 2 + POPPER_TEXT_PADDING
                    val left: Float = chartWidth - textWidth - POPPER_TEXT_PADDING * 2
                    val right = chartWidth
                    val arrowX: Float = left - textHeight / 2 - POPPER_TEXT_PADDING
                    val textBgPath = Path()
                    textBgPath.moveTo(left, top)
                    textBgPath.lineTo(right, top)
                    textBgPath.lineTo(right, bottom)
                    textBgPath.lineTo(left, bottom)
                    textBgPath.lineTo(arrowX, y)
                    textBgPath.close()
                    linePaint.color = POPPER_LINE_COLOR
                    canvas.drawLine(0f, y, left, y, linePaint)
                    canvas.drawCircle(x, y, POPPER_POINT_INNER_RADIUS, popPointerPaint)
                    popPriceTextPaint.color = POPPER_LINE_COLOR
                    canvas.drawPath(textBgPath, popPriceTextPaint)
                    popPriceTextPaint.color = POPPER_PRICE_COLOR
                    priceHelper.draw(canvas, (left + right) / 2, (top + bottom) / 2, Gravity.CENTER)
                    //弹出框显示在左边
                    showPopper(canvas, kLineChartItem, 0f, POPPER_TEXT_PADDING)
                } else {
                    //点击左边，
                    //计算文字背景框位置
                    val top: Float = y - textHeight / 2 - POPPER_TEXT_PADDING
                    val bottom: Float = y + textHeight / 2 + POPPER_TEXT_PADDING
                    val left = 0f
                    val right: Float = left + POPPER_TEXT_PADDING * 2 + textWidth
                    val arrowX: Float = right + textHeight / 2 + POPPER_TEXT_PADDING
                    val textBgPath = Path()
                    textBgPath.moveTo(left, top)
                    textBgPath.lineTo(right, top)
                    textBgPath.lineTo(arrowX, y)
                    textBgPath.lineTo(right, bottom)
                    textBgPath.lineTo(left, bottom)
                    textBgPath.close()
                    linePaint.color = POPPER_LINE_COLOR
                    canvas.drawLine(right, y, chartWidth, y, linePaint)
                    canvas.drawCircle(x, y, POPPER_POINT_INNER_RADIUS, popPointerPaint)
                    popPriceTextPaint.color = POPPER_LINE_COLOR
                    canvas.drawPath(textBgPath, popPriceTextPaint)
                    popPriceTextPaint.color = POPPER_PRICE_COLOR
                    priceHelper.draw(canvas, (left + right) / 2, (top + bottom) / 2, Gravity.CENTER)
                    //                    priceHelper.draw(canvas, POPPER_TEXT_PADDING, y, Gravity.LEFT | Gravity.CENTER_VERTICAL);
                    //点击左边，弹出框显示在右边
                    showPopper(canvas, kLineChartItem, chartWidth - popperWidth, POPPER_TEXT_PADDING)
                }
            }
            canvas.restore()
        }
    }

    //显示弹窗
    private fun showPopper(canvas: Canvas, KLineChartItem: KLineChartItem?, left: Float, top: Float) {
        if (KLineChartItem != null && timeStep !== TimeStep.NONE) {
            canvas.save()
            canvas.translate(left, top)
            var color: Int = WIN_COLOR
            if (KLineChartItem.`in` >= KLineChartItem.out) { //亏损
                color = LOSE_COLOR
            } else { //盈利
                color = WIN_COLOR
            }
            selectPaint.style = Paint.Style.FILL
            selectPaint.color = POPPER_BG_COLOR
            canvas.drawRect(0f, 0f, 0 + popperWidth, 0 + popperHeight, selectPaint)
            selectPaint.style = Paint.Style.STROKE
            selectPaint.color = POPPER_BORDER_COLOR
            canvas.drawRect(0f, 0f, 0 + popperWidth, 0 + popperHeight, selectPaint)
            popTextPaint.color = POPPER_TITLE_COLOR
            var offset: Float = POPPER_TEXT_PADDING
            //显示时间
//            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_time), popTextPaint, 0 + POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, CommonUtil.formatTimestamp(dateFormat, KLineChartItem.time * 1000), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_time))
                    .draw(canvas, 0 + POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, CommonUtil.formatTimestamp(dateFormat, KLineChartItem.time * 1000))
                    .draw(canvas, 0 + popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //显示开盘价
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_open), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, formatPrice(KLineChartItem.in), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_open))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, formatPrice(KLineChartItem.`in`))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //最高价
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_high), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, formatPrice(KLineChartItem.high), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_high))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, formatPrice(KLineChartItem.high))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //最低价
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_low), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, formatPrice(KLineChartItem.low), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_low))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, formatPrice(KLineChartItem.low))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //收盘价
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_close), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, formatPrice(KLineChartItem.out), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_close))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, formatPrice(KLineChartItem.out))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //涨跌额
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_dif), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
            val subValue = KLineChartItem.out - KLineChartItem.`in`
            //            drawTextCenterVertical(canvas, (subValue <= 0 ? "-" : "+") + CommonUtil.formatNumberNoGroup(subValue), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_dif))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = color
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, (if (subValue < 0) "" else "+") + NumberUtil.formatNumberNoGroup(subValue, 5, 5))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //涨跌幅度
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_dif_percent), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
            val subPercent = subValue / KLineChartItem.`in`
            //            drawTextCenterVertical(canvas, (subValue <= 0 ? "-" : "+") + CommonUtil.formatNumberNoGroup(subPercent * 100, 2) + "%", popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_dif_percent))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = color
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, (if (subValue < 0) "" else "+") + NumberUtil.formatNumberNoGroup(subPercent * 100, 2, 2) + "%")
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            //成交量
            popTextPaint.color = POPPER_TEXT_COLOR
            offset += POPPER_TEXT_PADDING * 2 + popTextHeight
            //            drawTextCenterVertical(canvas, getContext().getString(R.string.k_pop_vol), popTextPaint, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT);
//            drawTextCenterVertical(canvas, CommonUtil.formatNumberNoGroup(KLineChartItem.VOL), popTextPaint, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT);
            popTextPaint.color = POPPER_TITLE_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.NORMAL, context.getString(R.string.k_pop_vol))
                    .draw(canvas, POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.LEFT or Gravity.CENTER_VERTICAL)
            popTextPaint.color = POPPER_TEXT_COLOR
            TypefaceTextPaintHelper(context, popTextPaint, Typeface.BOLD, formatPrice(KLineChartItem.VOL))
                    .draw(canvas, popperWidth - POPPER_TEXT_PADDING, offset + POPPER_TEXT_PADDING + popTextHeight / 2, Gravity.RIGHT or Gravity.CENTER_VERTICAL)
            canvas.restore()
        }
    }

    private fun drawTextCenterVertical(canvas: Canvas, text: String, paint: Paint, x: Float, y: Float, gravity: Int) {
        var y = y
        y = getShowTextCenterVerticalY(y, paint)
        if (Gravity.RIGHT == gravity) {
            val textLength = paint.measureText(text)
            canvas.drawText(text, x - textLength, y, paint)
        } else if (Gravity.CENTER == gravity) {
            val textLength = paint.measureText(text)
            canvas.drawText(text, x - textLength / 2, y, paint)
        } else {
            canvas.drawText(text, x, y, paint)
        }
    }

    private fun refreshSize() {
        chartWidth = width - mPaddingLeft - mPaddingRight
        chartHeight = height - mPaddingTop - mPaddingBottom - TOP_TEXT_HEIGHT - BOTTOM_TEXT_HEIGHT
        chartTop = mPaddingTop + TOP_TEXT_HEIGHT
        chartBottom = chartTop + chartHeight
        chartLeft = mPaddingLeft
        chartRight = chartWidth + chartLeft
        cellWidth = chartWidth / 4
        cellHeight = chartHeight / 5
        RIGHT_BLANK_WIDTH_MAX = cellWidth
        if (data?.kLineChartItems == null || data?.kLineChartItems?.size == 0) {
            currentRightBlankWidth = RIGHT_BLANK_WIDTH_MAX
        }
        if (type and SUB_HIDDEN == SUB_HIDDEN) {
            mainCount = 4
            businessCount = 1
            subCount = 0
        } else {
            mainCount = 3
            businessCount = 1
            subCount = 1
        }
        yValueCoos = arrayOfNulls(mainCount)
        mainGraphHeight = cellHeight * mainCount
        businessHeight = cellHeight * businessCount - textHeight
        subHeight = cellHeight * subCount - textHeight
        popperWidth = (cellWidth * 1.5).toFloat()
        popperHeight = POPPER_TEXT_PADDING * 2 + POPPER_TEXT_PADDING * 16 + popTextHeight * 8
        xValueCoos = arrayOfNulls(5)
        resetTestValues()
    }

    private fun refreshShowData(isAll: Boolean) {
        val start = System.currentTimeMillis()
        clearMaxMin(isAll)
        val size = if (data?.kLineChartItems == null) 0 else data?.kLineChartItems?.size ?: 0
        if (size == 0) {
            showingKLineChartItemList = ArrayList()
        } else {
            if (size < showCount) {
                showingKLineChartItemList = ArrayList(listOf(*data?.kLineChartItems!!))
            } else {
                showingKLineChartItemList = ArrayList(showCount)
                val blankCount = (currentRightBlankWidth / itemWidth).toInt()
                var realShowFirstIndex = showFirstIndex + blankCount
                realShowFirstIndex = kotlin.math.max(0, realShowFirstIndex)
                for (i in realShowFirstIndex..showLastIndex) {
                    val item = CommonUtil.getItemFromArray(data?.kLineChartItems, i)
                    if (item != null) {
                        showingKLineChartItemList?.add(i - realShowFirstIndex, item)
                    } else {
                        break
                    }
                }
            }
            computeShowingMaxMin(showingKLineChartItemList!!, type)
            computeCoordinate(showingKLineChartItemList!!, type)
        }
    }

    private fun clearMaxMin(isAll: Boolean) {
        if (isAll) { //            currentRightBlankWidth = RIGHT_BLANK_WIDTH_MAX;
            isLongClick = false
            selectedIndex = -1
        }
        //        selectedIndex = -1;
        showingMinTime = 0
        showingMaxTime = 0
        showingKMinValue = -1.0
        showingKMaxValue = -1.0
        showingMinValue = 0.0
        showingMaxValue = 0.0
        showingMaxVol = 0.0
        showingSubMinValue = 0.0
        showingSubMaxValue = 0.0
        showingSubMinValue = 0.0
        showingSubMaxValue = 0.0
        if (yValueCoos != null) {
            for (yValueCoo in yValueCoos!!) {
                if (yValueCoo != null && yValueCoo.size == 2) {
                    yValueCoo[0] = 0.0
                    yValueCoo[1] = 0.0
                }
            }
        }
        if (xValueCoos != null) {
            for (xValueCoo in xValueCoos!!) {
                if (xValueCoo != null && xValueCoo.size == 2) {
                    xValueCoo[0] = 0.0
                    xValueCoo[1] = 0.0
                }
            }
        }
    }

    //计算显示的数据
    private fun computeShowingMaxMin(KLineChartItems: List<KLineChartItem?>, type: Int) {
        for (i in KLineChartItems.indices) {
            val kLineChartItem = KLineChartItems[i]
            kLineChartItem?.let {
                showingMinTime = if (showingMinTime == 0L) kLineChartItem.time else min(showingMinTime, kLineChartItem.time)
                showingMaxTime = if (showingMaxTime == 0L) kLineChartItem.time else max(showingMaxTime, kLineChartItem.time)
                if (showingKMinValue == -1.0 || kLineChartItem.KMin != 0.0 && showingKMinValue > kLineChartItem.KMin) {
                    showingKMinValue = kLineChartItem.KMin
                    kMinIndex = i
                }
                if (showingKMaxValue == -1.0 || kLineChartItem.KMax != 0.0 && showingKMaxValue < kLineChartItem.KMax) {
                    showingKMaxValue = kLineChartItem.KMax
                    kMaxIndex = i
                }
                showingMinValue = if (showingMinValue == 0.0) showingKMinValue else min(showingMinValue, showingKMinValue)
                showingMaxValue = if (showingMaxValue == 0.0) showingKMaxValue else max(showingMaxValue, showingKMaxValue)
                if (timeStep === TimeStep.NONE) { //分时计算 kLineItem的out
                    showingMinValue = if (showingMinValue == 0.0) kLineChartItem.out else min(showingMinValue, kLineChartItem.out)
                    showingMaxValue = if (showingMaxValue == 0.0) kLineChartItem.out else max(showingMaxValue, kLineChartItem.out)
                } else {
                    if (type and MA == MA) {
                        showingMinValue = if (showingMinValue == 0.0) kLineChartItem.MAMin else min(showingMinValue, kLineChartItem.MAMin)
                        showingMaxValue = if (showingMaxValue == 0.0) kLineChartItem.MAMax else max(showingMaxValue, kLineChartItem.MAMax)
                    } else if (type and BOLL == BOLL) {
                        showingMinValue = if (showingMinValue == 0.0) kLineChartItem.BOLLMin else min(showingMinValue, kLineChartItem.BOLLMin)
                        showingMaxValue = if (showingMaxValue == 0.0) kLineChartItem.BOLLMax else max(showingMaxValue, kLineChartItem.BOLLMax)
                    }
                }
                if (currentPrice > 0) {
                    showingMinValue = if (showingMinValue == 0.0) currentPrice else min(showingMinValue, currentPrice)
                    showingMaxValue = if (showingMaxValue == 0.0) currentPrice else max(showingMaxValue, currentPrice)
                }
                showingMaxVol = if (showingMaxVol == 0.0) kLineChartItem.VOLMax else max(showingMaxVol, kLineChartItem.VOLMax)
                when {
                    type and MACD == MACD -> {
                        showingSubMinValue = if (showingSubMinValue == 0.0) kLineChartItem.MACDMin else min(showingSubMinValue, kLineChartItem.MACDMin)
                        showingSubMaxValue = if (showingSubMaxValue == 0.0) kLineChartItem.MACDMax else max(showingSubMaxValue, kLineChartItem.MACDMax)
                    }
                    type and KDJ == KDJ -> {
                        showingSubMinValue = if (showingSubMinValue == 0.0) kLineChartItem.KDJMin else min(showingSubMinValue, kLineChartItem.KDJMin)
                        showingSubMaxValue = if (showingSubMaxValue == 0.0) kLineChartItem.KDJMax else max(showingSubMaxValue, kLineChartItem.KDJMax)
                    }
                    type and RSI == RSI -> {
                        showingSubMinValue = if (showingSubMinValue == 0.0) kLineChartItem.RSI else min(showingSubMinValue, kLineChartItem.RSI)
                        showingSubMaxValue = if (showingSubMaxValue == 0.0) kLineChartItem.RSI else max(showingSubMaxValue, kLineChartItem.RSI)
                    }
                    type and WR == WR -> {
                        showingSubMinValue = if (showingSubMinValue == 0.0) kLineChartItem.WR else min(showingSubMinValue, kLineChartItem.WR)
                        showingSubMaxValue = if (showingSubMaxValue == 0.0) kLineChartItem.WR else max(showingSubMaxValue, kLineChartItem.WR)
                    }
                }
            }
        }
//        Log.e(TAG, "showingMinValue:$showingMinValue,showingMaxValue:$showingMaxValue");
    }

    //计算坐标
    private fun computeCoordinate(KLineChartItems: List<KLineChartItem?>, type: Int) {
        val scaleNumber = 1000000
        //        double timeStep = ((double) (showingMaxTime - showingMinTime)) / (showCount - 1);
        val timeStep = getTimeStep().value.toDouble()
        itemWidth = (chartWidth.toDouble() / showCount).toFloat()
        val textValue = (showingMaxValue - showingMinValue) * textHeight * scaleNumber / mainGraphHeight / scaleNumber //文字占用折算价格
        //        double valueRange = showingMaxValue - showingMinValue;
//        double valueWeight = valueRange / mainGraphHeight;
        showingMaxValue += textValue / 2
        showingMinValue -= textValue / 2
        val valueRange = showingMaxValue - showingMinValue
        val valueWeight = valueRange * scaleNumber / mainGraphHeight
        val volWeight = showingMaxVol * scaleNumber / businessHeight
        val subValueRange = showingSubMaxValue - showingSubMinValue
        val subValueWeight = subValueRange * scaleNumber / subHeight
        macdZeroY = (showingSubMaxValue * scaleNumber / subValueWeight).toFloat()
        kMaxY = if (showingKMaxValue == -1.0) 0.0 else ((showingMaxValue - showingKMaxValue) * scaleNumber / valueWeight)
        kMinY = if (showingKMinValue == -1.0) 0.0 else ((showingMaxValue - showingKMinValue) * scaleNumber / valueWeight)
        val count = KLineChartItems.size
        if (count < 1) {
            return
        }
        var lastX = (itemWidth * (count - 1 + 0.5)).toFloat()
        val maxLastX = (chartWidth - currentRightBlankWidth - itemWidth * 0.5).toFloat()
        lastX = if (lastX > maxLastX) maxLastX else lastX
        for (i in KLineChartItems.indices) {
            val item = KLineChartItems[i]
            item?.let {
                ////Log.e(TAG, "index:" + i + ",time:" + CommonUtil.formatTimestamp(dateFormatX, KLineChartItem.time * 1000));
                val index = ((item.time - showingMinTime) / timeStep).toInt()
                item.x = (lastX - itemWidth * (count - 1 - index))
                item.lowY = if (item.low == 0.0) INVALID_Y else ((showingMaxValue - item.low) * scaleNumber / valueWeight).toFloat()
                item.highY = if (item.high == 0.0) INVALID_Y else ((showingMaxValue - item.high) * scaleNumber / valueWeight).toFloat()
                item.inY = if (item.`in` == 0.0) INVALID_Y else ((showingMaxValue - item.`in`) * scaleNumber / valueWeight).toFloat()
                item.outY = if (item.out == 0.0) INVALID_Y else ((showingMaxValue - item.out) * scaleNumber / valueWeight).toFloat()
                if (type and MA == MA) {
                    item.ma5Y = if (item.MA5 == 0.0) INVALID_Y else ((showingMaxValue - item.MA5) * scaleNumber / valueWeight).toFloat()
                    item.ma10Y = if (item.MA10 == 0.0) INVALID_Y else ((showingMaxValue - item.MA10) * scaleNumber / valueWeight).toFloat()
                    item.ma30Y = if (item.MA30 == 0.0) INVALID_Y else ((showingMaxValue - item.MA30) * scaleNumber / valueWeight).toFloat()
                } else if (type and BOLL == BOLL) {
                    item.bollY = if (item.BOLL == 0.0) INVALID_Y else ((showingMaxValue - item.BOLL) * scaleNumber / valueWeight).toFloat()
                    item.ubY = if (item.UB == 0.0) INVALID_Y else ((showingMaxValue - item.UB) * scaleNumber / valueWeight).toFloat()
                    item.lbY = if (item.LB == 0.0) INVALID_Y else ((showingMaxValue - item.LB) * scaleNumber / valueWeight).toFloat()
                }
                item.volY = ((showingMaxVol - item.VOL) * scaleNumber / volWeight).toFloat()
                item.volma5Y = ((showingMaxVol - item.VOLMA5) * scaleNumber / volWeight).toFloat()
                item.volma10Y = ((showingMaxVol - item.VOLMA10) * scaleNumber / volWeight).toFloat()
                when {
                    type and MACD == MACD -> {
                        item.macdY = ((showingSubMaxValue - item.MACD) * scaleNumber / subValueWeight).toFloat()
                        item.difY = if (item.DIF == 0.0) INVALID_Y else ((showingSubMaxValue - item.DIF) * scaleNumber / subValueWeight).toFloat()
                        item.deaY = if (item.DEA == 0.0) INVALID_Y else ((showingSubMaxValue - item.DEA) * scaleNumber / subValueWeight).toFloat()
                    }
                    type and KDJ == KDJ -> {
                        item.kY = if (item.K == 0.0) INVALID_Y else ((showingSubMaxValue - item.K) * scaleNumber / subValueWeight).toFloat()
                        item.dY = if (item.D == 0.0) INVALID_Y else ((showingSubMaxValue - item.D) * scaleNumber / subValueWeight).toFloat()
                        item.jY = if (item.J == 0.0) INVALID_Y else ((showingSubMaxValue - item.J) * scaleNumber / subValueWeight).toFloat()
                    }
                    type and RSI == RSI -> {
                        item.rsiY = if (item.RSI == 0.0) INVALID_Y else ((showingSubMaxValue - item.RSI) * scaleNumber / subValueWeight).toFloat()
                    }
                    type and WR == WR -> {
                        item.wrY = if (item.WR == 0.0) INVALID_Y else ((showingSubMaxValue - item.WR) * scaleNumber / subValueWeight).toFloat()
                    }
                }
            }
        }
        computeCurrentPriceCoordinate()
        //计算显示的Y轴坐标值
        //文字对应的价格区间
        if (yValueCoos != null) {
            for (i in yValueCoos!!.indices) {
                val valueCoo = DoubleArray(2)
                val value: Double = showingMaxValue - valueRange / yValueCoos!!.size * i
                valueCoo[0] = value
                valueCoo[1] = if (value == 0.0) 0.0 else ((showingMaxValue - value) * scaleNumber / valueWeight)
                yValueCoos!![i] = valueCoo
            }
        }
        //计算时间轴横坐标
        if (xValueCoos != null) {
//            int scrollItemCount = (int) (currentRightBlankWidth / itemWidth);
//            long realShowMinTime = (long) (showingMinTime + scrollItemCount * timeStep);
            val realShowMinTime = showingMinTime
            for (i in xValueCoos!!.indices) {
                val xValueCoo = DoubleArray(2)
                xValueCoo[0] = realShowMinTime + timeStep * showCount * i / 4 - (if (i == 0) 0.0 else timeStep)
                xValueCoo[0] = if (xValueCoo[0] <= 0) 0.000001 else xValueCoo[0]
                xValueCoos!![i] = xValueCoo
            }
        }
    }

    fun hideSub() {
        type = MA or SUB_HIDDEN
    }

    fun setType(type: Int) {
        if (this.type != type) {
            this.type = type
            refreshSize()
            refreshShowData(true)
            postInvalidate()
        }
    }

    fun getType(): Int {
        return type
    }

    fun setTimeStep(timeStep: TimeStep) {
        if (this.timeStep !== timeStep) {
            this.timeStep = timeStep
        }
        //        ishow = false;
        if (stepNoneLightDrawable != null) {
            if (timeStep === TimeStep.NONE) {
                stepNoneLightDrawable?.setVisible(true, true)
            } else {
                stepNoneLightDrawable?.setVisible(false, true)
                stepNoneLightDrawable?.stop()
            }
        }
        //        if (lightDrawable != null) {
//            if (timeStep == TimeStep.NONE) {
//                lightDrawable.setVisible(true, true);
//            } else {
//                lightDrawable.setVisible(false, true);
//                lightDrawable.stop();
//            }
//        }
        clearChart()
        lastPage = 0
    }

    fun setPrecision(precision: Int?) {
        if (precision != null && precision > 0) {
            this.precision = precision
            invalidate()
        }
    }

    fun getTimeStep(): TimeStep {
        return timeStep
    }

    fun getCurrentKlinePage():Int{
        return currentKlinePage
    }

    private fun clearChart() {
        data = Data()
        refreshShowData(true)
        postInvalidate()
    }

    fun setCurrentPrice(currentPrice: Double) {
        this.currentPrice = currentPrice
        val oldMin = showingMinValue
        val oldMax = showingMaxValue
        if (currentPrice > 0) {
            showingMinValue = if (showingMinValue == 0.0) currentPrice else min(showingMinValue, currentPrice)
            showingMaxValue = if (showingMaxValue == 0.0) currentPrice else max(showingMaxValue, currentPrice)
        }
        //区间最值有变化，需要重新计算所有节点坐标，否则只用计算当前价格坐标
        if (oldMin != showingMinValue || oldMax != showingMaxValue) {
            if(type==null||showingKLineChartItemList==null){
                return
            }
            computeCoordinate(showingKLineChartItemList!!, type)
        } else {
            computeCurrentPriceCoordinate()
        }
        postInvalidate()
    }

    private fun computeCurrentPriceCoordinate() {
        currentPriceY = if (currentPrice > 0) {
            val scaleNumber = 1000000
            val valueRange = showingMaxValue - showingMinValue
            val valueWeight = valueRange * scaleNumber / mainGraphHeight
            if (currentPrice == 0.0) INVALID_Y.toDouble() else ((showingMaxValue - currentPrice) * scaleNumber / valueWeight)
        } else {
            INVALID_Y.toDouble()
        }
    }

    private fun getAllNode(kLineItems: ArrayList<KLineItem?>?): Array<KLineChartItem?>? {
        var items = kLineItems
        if (items == null) {
            items = ArrayList()
        }
        val size = items.size
        val dataSize = Math.min(size, ITEM_COUNT_MAX)
        SHOW_COUNT_MAX = dataSize
        val start = size - SHOW_COUNT_MAX
        val dataList = ArrayList<KLineChartItem>(dataSize)
        var i = start
        var nodeIndex = 0
        while (i < size) {
            val kLineItem = items[i] ?: continue
            val kLineChartItem = KLineChartItem(kLineItem.t!!, kLineItem.a, kLineItem.h, kLineItem.l, kLineItem.o, kLineItem.c)
            val lastKLineChartItem = CommonUtil.getItemFromList(dataList, nodeIndex - 1)
            if (kLineChartItem.low == 0.0) {
                if (lastKLineChartItem == null) {
                    nodeIndex--
                    i++
                    nodeIndex++
                    continue
                } else {
                    kLineChartItem.low = lastKLineChartItem.out
                    kLineChartItem.high = kLineChartItem.low
                    kLineChartItem.`in` = kLineChartItem.high
                    kLineChartItem.out = kLineChartItem.`in`
                }
            }
            if (lastKLineChartItem != null) { //判断是否有断层
                val count = ((kLineChartItem.time - lastKLineChartItem.time) / timeStep.value).toInt()
                val time = lastKLineChartItem.time
                for (ii in 0 until count - 1) {
                    val insertKLineChartItem = KLineChartItem(time + timeStep.value * (ii + 1), 0.0, lastKLineChartItem.high, lastKLineChartItem.low, lastKLineChartItem.`in`, lastKLineChartItem.out)
                    dataList.add(insertKLineChartItem)
                    insertKLineChartItem.isAddData = true
                    nodeIndex++
                }
            }
            dataList.add(kLineChartItem)
            i++
            nodeIndex++
        }
        val nodeSize = dataList.size
        //Log.e(TAG, "KLineUtil.calculate start ====");
        calculate(dataList)
        //Log.e(TAG, "KLineUtil.calculate end ====");
        return if (nodeSize > dataSize) {
            val subList: MutableList<KLineChartItem> = dataList.subList(nodeSize - dataSize, nodeSize)
            //删除开头手动添加的数据
            while (subList.isNotEmpty() && subList[0].isAddData) {
                subList.removeAt(0)
            }
            subList.toTypedArray()
        } else {
            dataList.toTypedArray()
        }
    }

    fun setData(kLineItems: ArrayList<KLineItem?>?) {
        var items = kLineItems
        if (items == null) {
            items = ArrayList()
        }
        //过滤KLineItem v 为 0 的字段
//        for (int i = kLineItems.size() - 1; i >= 0; i--) {
//            KLineItem kLineItem = kLineItems.get(i);
//            if (kLineItem.a == 0) {
//                kLineItems.remove(i);
//            }
//        }
        val size = items.size
        val dataSize = size.coerceAtMost(ITEM_COUNT_MAX)
        SHOW_COUNT_MAX = dataSize
        var start = size - SHOW_COUNT_MAX
        start = if (start < 0) 0 else start
        if (size == 0) {
            data = Data()
            data?.kLineChartItems = emptyArray()
        } else {
            val data = Data()
            data.kLineChartItems = getAllNode(items)
            this.data = data
            if (dataSize < showCount) {
                showCount = Math.max(showCount, SHOW_COUNT_MIN)
            }
            showLastIndex = dataSize - 1
            showFirstIndex = showLastIndex - showCount + 1
        }
        val lastKLineChartItem = if (data?.kLineChartItems?.size == 0) null else data?.kLineChartItems!![data?.kLineChartItems!!.size - 1]
        data?.setValues(lastKLineChartItem)
        data?.sortNodes()
        refreshShowData(true)
        lastPage = 1
    }

    fun addData(newKLineChartItem: KLineChartItem) {
        val oldSize = if (data?.kLineChartItems == null) 0 else data?.kLineChartItems?.size ?: 0
        var lastKLineChartItem = if (data?.kLineChartItems == null) null else CommonUtil.getItemFromArray(data?.kLineChartItems, data?.kLineChartItems!!.size - 1)
        //Log.e(TAG, "addData oldSize:" + oldSize + ",lastKLineChartItem:" + lastKLineChartItem);
        if (lastKLineChartItem != null) {
            if (lastKLineChartItem.time > newKLineChartItem.time) { //小于最后一个柱子的时间，不处理
                return
            }
            if (newKLineChartItem.time - lastKLineChartItem.time < timeStep.value) { //属于第一个节点
                updateNode(lastKLineChartItem, newKLineChartItem)
            } else { //存在中间空白间隙
                val count = ((newKLineChartItem.time - lastKLineChartItem.time) / timeStep.value).toInt()
                val newKLineChartItems = arrayOfNulls<KLineChartItem>(oldSize + count)
                System.arraycopy(data?.kLineChartItems!!, 0, newKLineChartItems, 0, oldSize)
                val time = lastKLineChartItem.time + timeStep.value
                for (i in 0 until count - 1) {
                    val nodeIndex = oldSize + i
                    val insertKLineChartItem = KLineChartItem(time + timeStep.value * i, 0.0, lastKLineChartItem.high, lastKLineChartItem.low, lastKLineChartItem.`in`, lastKLineChartItem.out)
                    newKLineChartItems[nodeIndex] = insertKLineChartItem
                }
                val nodeIndex = oldSize + count - 1
                newKLineChartItems[nodeIndex] = newKLineChartItem
                data?.kLineChartItems = newKLineChartItems
            }
        } else {
            val array = arrayOfNulls<KLineChartItem>(1)
            array[0] = newKLineChartItem
            data?.kLineChartItems = array
        }
        run {
            val size = data?.kLineChartItems?.size ?: 0
            //计算增加的节点数据
            calculate(data?.kLineChartItems!!)
            val dataSize = Math.min(size, ITEM_COUNT_MAX)
            if (dataSize < showCount) {
                showCount = Math.max(showCount, SHOW_COUNT_MIN)
            }
            if (oldSize == 0 || showLastIndex >= oldSize - 1) { //旧的数组为空，显示到最后;目前显示到了最后，继续显示到最后
                showLastIndex = dataSize - 1
                showFirstIndex = showLastIndex - showCount + 1
            } else { //否则从当最开始的地方显示
                val addSize = size - oldSize //增加的数量
                if (addSize > 0) {
                }
            }
            if (size > ITEM_COUNT_MAX) {
                val newKLineChartItems = arrayOfNulls<KLineChartItem>(ITEM_COUNT_MAX)
                System.arraycopy(data?.kLineChartItems!!, size - ITEM_COUNT_MAX, newKLineChartItems, 0, ITEM_COUNT_MAX)
                data?.kLineChartItems = newKLineChartItems
            }
        }
        //Log.e(TAG, "addData data.KLineChartItems.length:" + data.KLineChartItems.length);
        SHOW_COUNT_MAX = kotlin.math.min(data?.kLineChartItems?.size ?: 0, ITEM_COUNT_MAX)
        lastKLineChartItem = if (data?.kLineChartItems?.size == 0) null else data?.kLineChartItems!![data?.kLineChartItems!!.size - 1]
        data?.setValues(lastKLineChartItem)
        data?.sortNodes()
        refreshShowData(false)
    }

    fun addData(kLineItem: KLineItem) {
//        if (kLineItem.a == 0) {
//            return;
//        }
        val kLineChartItem = KLineChartItem(kLineItem.t!!, kLineItem.a, kLineItem.h, kLineItem.l, kLineItem.o, kLineItem.c)
        addData(kLineChartItem)
    }

    var lastPage = 0
    var moreDataCache: Array<ArrayList<KLineItem?>?>? = null

    //添加节点数组
    fun addDataList(page: Int, kLineItems: ArrayList<KLineItem?>?) {
        if (page <= lastPage) {
            return
        }
        if (page - lastPage > 1) { //加载的数据跟原有数据存在断层，缓存当前数据，等待中间数据返回
            if (moreDataCache == null) {
                moreDataCache = arrayOfNulls<ArrayList<KLineItem?>?>(page + 1)
            }
            val oldCacheSize = moreDataCache?.size ?: 0
            if (page >= oldCacheSize) {
                val tmp: Array<ArrayList<KLineItem?>?> = arrayOfNulls<ArrayList<KLineItem?>?>(page + 1)
                System.arraycopy(moreDataCache!!, 0, tmp, 0, oldCacheSize)
                moreDataCache = tmp
            }
            moreDataCache!![page] = kLineItems
            return
        }
        //返回的数据刚好是后一页数据，添加数据
        var addedData: Array<KLineChartItem?>? = getAllNode(kLineItems, timeStep)
        lastPage = page
        if (moreDataCache != null) {
            for (i in lastPage + 1 until moreDataCache!!.size) {
                val cacheData = moreDataCache!![i] ?: break
                val nextPageData = getAllNode(cacheData, timeStep)
                addedData = addKLIneItemsToFront(addedData!!, nextPageData, timeStep)
                moreDataCache!![i] = null
                lastPage = i
            }
        }
        val addedSize = addedData?.size ?: 0
        if (addedSize == 0) {
            return
        }
        val oldSize = if (data?.kLineChartItems == null) 0 else data?.kLineChartItems?.size ?: 0
        data?.kLineChartItems = addKLIneItemsToFront(data?.kLineChartItems!!, addedData, timeStep)
        run {
            val size = data?.kLineChartItems?.size ?: 0
            //计算增加的节点数据
            calculate(data?.kLineChartItems!!)
            val dataSize = kotlin.math.min(size, ITEM_COUNT_MAX)
            if (dataSize < showCount) {
                showCount = Math.max(showCount, SHOW_COUNT_MIN)
            }
            showLastIndex = showLastIndex + size - oldSize
            showFirstIndex = showLastIndex - showCount + 1
            if (size > ITEM_COUNT_MAX) {
                val newKLineChartItems = arrayOfNulls<KLineChartItem>(ITEM_COUNT_MAX)
                System.arraycopy(data?.kLineChartItems!!, size - ITEM_COUNT_MAX, newKLineChartItems, 0, ITEM_COUNT_MAX)
                data?.kLineChartItems = newKLineChartItems
            }
        }
        //Log.e(TAG, "addData data.KLineChartItems.length:" + data.KLineChartItems.length);
        SHOW_COUNT_MAX = kotlin.math.min(data?.kLineChartItems?.size ?: 0, ITEM_COUNT_MAX)
        val lastKLineChartItem = if (data?.kLineChartItems?.size == 0) null else data?.kLineChartItems!![data?.kLineChartItems!!.size - 1]
        data?.setValues(lastKLineChartItem)
        data?.sortNodes()
        refreshShowData(false)
    }

    private var data: Data? = Data()

    private class Data {
        var MA5 = 0.0
        var MA10 = 0.0
        var MA30 = 0.0
        var BOLL = 0.0
        var UB = 0.0
        var LB = 0.0
        var VOL = 0.0
        var VOLMA5 = 0.0
        var VOLMA10 = 0.0
        var MACD = 0.0
        var DIF = 0.0
        var DEA = 0.0
        var K = 0.0
        var D = 0.0
        var J = 0.0
        var RSI = 0.0
        var WR = 0.0
        var kLineChartItems: Array<KLineChartItem?>? = null
        fun setValues(kLineChartItem: KLineChartItem?) {
            if (kLineChartItem == null) {
                MA30 = 0.0
                MA10 = MA30
                MA5 = MA10
                LB = 0.0
                UB = LB
                BOLL = UB
                VOLMA10 = 0.0
                VOLMA5 = VOLMA10
                VOL = VOLMA5
                DEA = 0.0
                DIF = DEA
                MACD = DIF
                J = 0.0
                D = J
                K = D
                RSI = 0.0
                WR = 0.0
            } else {
                MA5 = kLineChartItem.MA5
                MA10 = kLineChartItem.MA10
                MA30 = kLineChartItem.MA30
                BOLL = kLineChartItem.BOLL
                UB = kLineChartItem.UB
                LB = kLineChartItem.LB
                VOL = kLineChartItem.VOL
                VOLMA5 = kLineChartItem.VOLMA5
                VOLMA10 = kLineChartItem.VOLMA10
                MACD = kLineChartItem.MACD
                DIF = kLineChartItem.DIF
                DEA = kLineChartItem.DEA
                K = kLineChartItem.K
                D = kLineChartItem.D
                J = kLineChartItem.J
                RSI = kLineChartItem.RSI
                WR = kLineChartItem.WR
            }
        }

        //对数组进行排序，筛选重复数据
        fun sortNodes() { //            KLineChartItem[] newNodes = new KLineChartItem[300];
//            long timeStep = (maxTime - minTime) / 300;
//            for (KLineChartItem node : KLineChartItems) {
//                if (node != null) {
//                    int index = (int) ((node.time - minTime) / timeStep);
//                    if (index > -1 && index < 300) {
//                        newNodes[index] = node;
//                    }
//                }
//            }
//            KLineChartItems = newNodes;
            Arrays.sort(kLineChartItems, comparator)
        }

        companion object {
            private val comparator = Comparator<KLineChartItem?> { o1, o2 ->
                if (o1?.time == null || o2?.time == null) 0 else (o1.time - o2.time).toInt()
            }
        }
    }

    private var analyticChartHelper: AnalyticChartHelper? = null

    fun setAnalyticChartHelper(analyticChartHelper: AnalyticChartHelper?) {
        this.analyticChartHelper = analyticChartHelper
    }

    interface AnalyticChartHelper {
        fun onLoadMore(page: Int)
    }

    fun getTimeStepRequestStr():String?{
        var str:String? = "15m"
        when(timeStep.text.toString()){
            context.getString(R.string.min_1) -> str = "1m"
            context.getString(R.string.min_15) -> str = "15m"
            context.getString(R.string.min_30) -> str = "30m"
            context.getString(R.string.hour_1) -> str = "1h"
            context.getString(R.string.hour_4) -> str = "4h"
            context.getString(R.string.day_1) -> str = "1d"
            context.getString(R.string.week_1) -> str = "1w"
        }
        return str
    }

    class TimeStep constructor(internal var text: String, val apiText: String, val value: Long) {

        override fun toString(): String {
            return text
        }


        companion object {
            val NONE = TimeStep("", "1", 60)
            val MIN_1 = TimeStep("", "1", 60)
            val MIN_5 = TimeStep("", "5", 5 * 60)
            val MIN_15 = TimeStep("", "15", 15 * 60)
            val MIN_30 = TimeStep("", "30", 30 * 60)
            val HOUR_1 = TimeStep("", "60", 60 * 60)
            val HOUR_4 = TimeStep("", "240", 4 * 60 * 60)
            val DAY_1 = TimeStep("", "1D", 24 * 60 * 60)
            val WEEK_1 = TimeStep("", "1W", 7 * 24 * 60 * 60)
            val MONTH_1 = TimeStep("", "1M", 30 * 24 * 60 * 60)
            val MORE = TimeStep("", "", 0)
        }

    }

}