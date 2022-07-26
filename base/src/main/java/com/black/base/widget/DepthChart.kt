package com.black.base.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import androidx.customview.widget.ViewDragHelper
import com.black.base.R
import com.black.lib.typeface.TypefaceTextPaintHelper
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatView
import java.util.*
import kotlin.math.abs

class DepthChart : SkinCompatView {
    companion object {
        private const val TAG = "DepthChart"
        private const val WIDTH = 300
        private const val HEIGHT = 300
        private var COLOR_BG = 0
        private var COLOR_BUY = 0
        private var COLOR_BUY_SHADOW = 0
        private var COLOR_SALE = 0
        private var COLOR_SALE_SHADOW = 0
        private var COLOR_NUMBER = 0
        private var POPPER_BORDER_COLOR = 0
        private var POPPER_BG_COLOR = 0
        private var POPPER_TEXT_COLOR = 0
        private const val LONG_CLICK_TIME: Long = 500
        const val STEP_COUNT = 5
        const val DATA_ITEM_COUNT = 200
    }

    private var density = 0f
    //图形尺寸
    private var chartWidth = 0f
    private var chartHeight = 0f
    private var rectSize = 0f
    //    private Paint basePaint = new Paint();
    private val textPaint = Paint()
    private val linePaint = Paint()
    private val shadowPaint = Paint()
    private val numberPaint = Paint()

    private var titleTextHeight = 0f
    private var numberTextHeight = 0f
    private var precision = 15 //精度
    private var amountPrecision = 5 //交易数量精度

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        val dm = resources.displayMetrics
        density = dm.density
        COLOR_BG = SkinCompatResources.getColor(context, R.color.transparent)
        COLOR_BUY = SkinCompatResources.getColor(context, R.color.T7)
        COLOR_BUY_SHADOW = SkinCompatResources.getColor(context, R.color.T7_ALPHA10)
        COLOR_SALE = SkinCompatResources.getColor(context, R.color.T5)
        COLOR_SALE_SHADOW = SkinCompatResources.getColor(context, R.color.T5_ALPHA10)
        COLOR_NUMBER = SkinCompatResources.getColor(context, R.color.T2)
        POPPER_BORDER_COLOR = SkinCompatResources.getColor(context, R.color.T2)
        POPPER_BG_COLOR = SkinCompatResources.getColor(context, R.color.B2)
        POPPER_TEXT_COLOR = SkinCompatResources.getColor(context, R.color.T1)
        rectSize = 10 * density

        linePaint.isAntiAlias = true
        linePaint.color = COLOR_BUY
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2 * density

        shadowPaint.isAntiAlias = true
        shadowPaint.color = COLOR_BUY
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.strokeWidth = 1 * density

        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_14).toFloat()

        numberPaint.isAntiAlias = true
        numberPaint.style = Paint.Style.FILL
        numberPaint.color = COLOR_NUMBER
        numberPaint.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size_10).toFloat()

        titleTextHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent
        numberTextHeight = numberPaint.fontMetrics.descent - numberPaint.fontMetrics.ascent
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
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        refreshSize()
    }

    private fun refreshSize() {
        chartWidth = width - paddingLeft - paddingRight.toFloat()
        chartHeight = height - paddingTop - titleTextHeight - paddingBottom - numberTextHeight
        //        data = getTestsData();
        handleData()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data == null) {
            return
        }
        //        Drawable background = getBackground();
//        if (background != null) {
//            background.draw(canvas);
//        }
        canvas.drawColor(COLOR_BG)
        canvas.save()
        //绘制文字描述
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        textPaint.color = COLOR_BUY
        val textBaseY = titleTextHeight / 2 + (Math.abs(textPaint.ascent()) - textPaint.descent()) / 2
        val buyTitle = context.getString(R.string.buy_02)
        //        float buyTitleWidth = textPaint.measureText(buyTitle);
//        canvas.drawText(buyTitle, chartWidth / 2 - buyTitleWidth, textBaseY, textPaint);
        val buyTitleHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, buyTitle)
        buyTitleHelper.draw(canvas, chartWidth / 2 - 8 * density, 2 * density, Gravity.RIGHT or Gravity.TOP)
        canvas.drawRect(chartWidth / 2 - 24 * density - buyTitleHelper.length, 2 * density + (buyTitleHelper.height - 8 * density) / 2, chartWidth / 2 - 16 * density - buyTitleHelper.length, 2 * density + (buyTitleHelper.height + 8 * density) / 2, textPaint)
        textPaint.color = COLOR_SALE
        val saleTitle = context.getString(R.string.sale_02)
        val saleTitleHelper = TypefaceTextPaintHelper(context, textPaint, Typeface.NORMAL, saleTitle)
        saleTitleHelper.draw(canvas, chartWidth / 2 + 24 * density, 2 * density, Gravity.LEFT or Gravity.TOP)
        canvas.drawRect(chartWidth / 2 + 8 * density, 2 * density + (saleTitleHelper.height - 8 * density) / 2, chartWidth / 2 + 16 * density, 2 * density + (buyTitleHelper.height + 8 * density) / 2, textPaint)
        //        float saleTitleWidth = textPaint.measureText(saleTitle);
//        canvas.drawRect((float) (chartWidth / 2 + saleTitleWidth / 2 + textBaseY * 0.2), (float) (textBaseY - textBaseY * 0.8), (float) (chartWidth / 2 + saleTitleWidth / 2 + textBaseY * 0.8), (float) (textBaseY - textBaseY * 0.2), textPaint);
//        canvas.drawText(saleTitle, chartWidth / 2 + saleTitleWidth + textBaseY, textBaseY, textPaint);
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2 * density
        canvas.translate(0f, titleTextHeight)
        if (data?.buyItems != null && data?.buyItems!!.size > 1) {
            val buyPath = Path() //买入图像路径
            for (i in data?.buyItems!!.indices) {
                val buyItem = data?.buyItems!![i]
                if (i == 0) {
                    buyPath.moveTo(buyItem.x, buyItem.y)
                } else {
                    buyPath.lineTo(buyItem.x, buyItem.y)
                }
            }
            linePaint.color = COLOR_BUY
            canvas.drawPath(buyPath, linePaint)
            val shader: Shader = LinearGradient(0f, data?.buyItems!![0].y, 0f, chartHeight, intArrayOf(COLOR_BUY_SHADOW, COLOR_BUY_SHADOW), null, Shader.TileMode.REPEAT)
            shadowPaint.color = COLOR_BUY_SHADOW
            //        shadowPaint.setShader(shader);
            shadowPaint.style = Paint.Style.FILL
            buyPath.lineTo(data?.buyItems!![data?.buyItems!!.size - 1].x, chartHeight)
            buyPath.lineTo(data?.buyItems!![0].x, chartHeight)
            canvas.drawPath(buyPath, shadowPaint)
        }
        if (data?.saleItems != null && data?.saleItems!!.size > 1) {
            val salePath = Path() //卖出图像路径
            for (i in data?.saleItems!!.indices) {
                val saleItem = data?.saleItems!![i]
                if (i == 0) {
                    salePath.moveTo(saleItem.x, saleItem.y)
                } else {
                    salePath.lineTo(saleItem.x, saleItem.y)
                }
            }
            linePaint.color = COLOR_SALE
            canvas.drawPath(salePath, linePaint)
            val shader: Shader = LinearGradient(0f, data?.saleItems!![data?.saleItems!!.size - 1].y, 0f, chartHeight, intArrayOf(COLOR_SALE_SHADOW, COLOR_SALE_SHADOW), null, Shader.TileMode.REPEAT)
            shadowPaint.color = COLOR_SALE_SHADOW
            //        shadowPaint.setShader(shader);
            shadowPaint.style = Paint.Style.FILL
            salePath.lineTo(data?.saleItems!![data?.saleItems!!.size - 1].x, chartHeight)
            salePath.lineTo(data?.saleItems!![0].x, chartHeight)
            canvas.drawPath(salePath, shadowPaint)
        }
        canvas.restore()
        canvas.save()
        val numberBaseY = numberTextHeight / 2 + (Math.abs(numberPaint.ascent()) - numberPaint.descent()) / 2
        canvas.translate(paddingLeft.toFloat(), paddingTop + titleTextHeight)
        //纵坐标
        for (i in 0 until STEP_COUNT) {
            val text = NumberUtil.formatNumberNoGroup(maxCount - maxCount * i / STEP_COUNT, amountPrecision, amountPrecision)
            //            float textWidth = numberPaint.measureText(text);
//            canvas.drawText(text, 0, chartHeight * i / STEP_COUNT, numberPaint);
            TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, text)
                    .draw(canvas, chartWidth - 2 * density, chartHeight * i / STEP_COUNT, Gravity.RIGHT)
        }
        //横坐标
        canvas.translate(0f, chartHeight)
        if (minBuyPrice > 0) {
            val minBuyPriceText = NumberUtil.formatNumberNoGroup(minBuyPrice, precision, precision)
            //            float minBuyPriceTextWidth = numberPaint.measureText(minBuyPriceText);
//            canvas.drawText(minBuyPriceText, minBuyPriceTextWidth / 2, numberBaseY, numberPaint);
            TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, minBuyPriceText)
                    .draw(canvas, 2 * density, 0f, Gravity.LEFT or Gravity.TOP)
        }
        val middlePriceText = NumberUtil.formatNumberNoGroup(data?.middlePrice, precision, precision)
        //        float middleTextWidth = numberPaint.measureText(middlePriceText);
//        canvas.drawText(middlePriceText, chartWidth / 2 - middleTextWidth / 2, numberBaseY, numberPaint);
        TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, middlePriceText)
                .draw(canvas, chartWidth / 2, 0f, Gravity.CENTER_HORIZONTAL or Gravity.TOP)
        if (maxSalePrice > 0) {
            val maxSalePriceText = NumberUtil.formatNumberNoGroup(maxSalePrice, precision, precision)
            //            float maxSalePriceTextWidth = numberPaint.measureText(maxSalePriceText);
//            canvas.drawText(maxSalePriceText, chartWidth - maxSalePriceTextWidth, numberBaseY, numberPaint);
            TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, maxSalePriceText)
                    .draw(canvas, chartWidth - 2 * density, 0f, Gravity.RIGHT or Gravity.TOP)
        }
        canvas.restore()
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop + titleTextHeight)
        if (selectedItem != null) {
            //绘制选中的
            val color = if (selectedItem?.type == 1) COLOR_SALE else COLOR_BUY
            linePaint.style = Paint.Style.STROKE
            linePaint.color = color
            linePaint.strokeWidth = 1 * density
            canvas.drawCircle(selectedItem!!.x, selectedItem!!.y, 5 * density, linePaint)
            linePaint.style = Paint.Style.FILL
            canvas.drawCircle(selectedItem!!.x, selectedItem!!.y, 2 * density, linePaint)
            //绘制当前价格和数量
            drawPopCount(canvas)
            canvas.translate(0f, chartHeight)
            drawPopPrice(canvas)
        }
        canvas.restore()
    }

    private fun drawPopCount(canvas: Canvas) {
        if (selectedItem != null) {
            val text = NumberUtil.formatNumberNoGroup(selectedItem?.count, amountPrecision, amountPrecision)
            numberPaint.color = Color.TRANSPARENT
            val countHelper = TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, text)
            countHelper.draw(canvas, chartWidth - 2 * density, selectedItem!!.y, Gravity.RIGHT)
            linePaint.style = Paint.Style.FILL
            linePaint.color = POPPER_BG_COLOR
            val left = chartWidth - 2 * density - countHelper.length - 2 * density
            val top = selectedItem!!.y - countHelper.height - 2 * density
            val right = left + countHelper.length + 4 * density
            val bottom = top + countHelper.height + 4 * density
            canvas.drawRect(left, top, right, bottom, linePaint)
            linePaint.style = Paint.Style.STROKE
            linePaint.color = POPPER_BORDER_COLOR
            linePaint.strokeWidth = 1 * density
            canvas.drawRect(left, top, right, bottom, linePaint)
            numberPaint.color = POPPER_TEXT_COLOR
            countHelper.draw(canvas, chartWidth - 2 * density, selectedItem!!.y, Gravity.RIGHT)
        }
    }

    private fun drawPopPrice(canvas: Canvas) {
        if (selectedItem == null) {
            return
        }
        val middlePriceText = NumberUtil.formatNumberNoGroup(selectedItem?.price, precision, precision)
        numberPaint.color = Color.TRANSPARENT
        val priceHelper = TypefaceTextPaintHelper(context, numberPaint, Typeface.NORMAL, middlePriceText)
        priceHelper.draw(canvas, selectedItem!!.x, 2 * density, Gravity.CENTER_HORIZONTAL or Gravity.TOP)
        linePaint.style = Paint.Style.FILL
        linePaint.color = POPPER_BG_COLOR
        val left = selectedItem!!.x - priceHelper.length / 2 - 2 * density
        val top = 0f
        val right = left + priceHelper.length + 4 * density
        val bottom = top + priceHelper.height + 4 * density
        canvas.drawRect(left, top, right, bottom, linePaint)
        linePaint.style = Paint.Style.STROKE
        linePaint.color = POPPER_BORDER_COLOR
        linePaint.strokeWidth = 1 * density
        canvas.drawRect(left, top, right, bottom, linePaint)
        numberPaint.color = POPPER_TEXT_COLOR
        priceHelper.draw(canvas, selectedItem!!.x, 2 * density, Gravity.CENTER_HORIZONTAL or Gravity.TOP)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean { //横向滑动控件处理，纵向滑动，父窗口处理
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_MOVE -> if (parentTouch) {
                parent.requestDisallowInterceptTouchEvent(false)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }
        return super.dispatchTouchEvent(ev)
    }

    var parentTouch = false
    var mineTouch = false
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mActivePointerId = 0
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                parentTouch = false
                mineTouch = false
                mActivePointerId = ev.getPointerId(0)
                if (isLongClick) {
                } else {
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
                    if (isLongClick) {
                        val clickTime = ev.eventTime - ev.downTime
                        if (clickTime >= 200) { //点击事件
                            onLongClick(ev)
                        }
                    } else {
                        if (!mineTouch && !parentTouch) {
                            if (abs(deltaY) > abs(deltaX) * 1.5) {
                                parentTouch = true
                            } else {
                                mineTouch = true
                            }
                        }
                    }
                } else if (count >= 2) {
                    unCheckLongClick()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val clickTime = ev.eventTime - ev.downTime
                if (clickTime < 200) { //点击事件
                    unCheckLongClick()
                }
                if (isLongClick) {
                } else {
                    unCheckLongClick()
                    mActivePointerId = ViewDragHelper.INVALID_POINTER
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                unCheckLongClick()
                onSecondaryPointerUp(ev)
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId))
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId))
            }
            MotionEvent.ACTION_CANCEL -> {
                unCheckLongClick()
                mActivePointerId = ViewDragHelper.INVALID_POINTER
            }
        }
        return true
    }

    private var isLongClick = false
    private var selectedItem: Item? = null
    private var longClickCheckListener: LongClickCheckListener? = null

    internal inner class LongClickCheckListener internal constructor(private val ev: MotionEvent) : Runnable {
        override fun run() {
            isLongClick = true
            onLongClick(ev)
        }

    }

    private fun unCheckLongClick() {
        if (longClickCheckListener != null) {
            removeCallbacks(longClickCheckListener)
            longClickCheckListener = null
            isLongClick = false
            selectedItem = null
            postInvalidate()
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr
                MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun onLongClick(ev: MotionEvent) {
        val selectX = ev.x
        val xInChart = selectX - paddingLeft
        selectedItem = getSelectedItem(selectX)
        postInvalidate()
    }

    private fun getSelectedItem(actionX: Float): Item? {
        val xInChart = actionX - paddingLeft
        var selectedItem: Item? = null
        if (xInChart >= 0 && chartWidth >= xInChart) {
            val halfChartWidth = chartWidth.toDouble() / 2
            //找出离得最近的一个item
            if (xInChart > halfChartWidth) { //右边
                if (data != null && data?.saleItems != null && data?.saleItems!!.size > 1) {
                    for (i in data?.saleItems!!.indices) {
                        val saleItem = data?.saleItems!![i]
                        if (selectedItem == null) {
                            selectedItem = saleItem
                        } else {
                            if (abs(saleItem.x - xInChart) < abs(selectedItem.x - xInChart)) {
                                selectedItem = saleItem
                            }
                        }
                    }
                }
                if (selectedItem != null) {
                    selectedItem.type = 1
                }
            } else if (xInChart < halfChartWidth) { //左半边
                if (data != null && data?.buyItems != null && data?.buyItems!!.size > 1) {
                    for (i in data?.buyItems!!.indices) {
                        val buyItem = data?.buyItems!![i]
                        if (selectedItem == null) {
                            selectedItem = buyItem
                        } else {
                            if (abs(buyItem.x - xInChart) < abs(selectedItem.x - xInChart)) {
                                selectedItem = buyItem
                            }
                        }
                    }
                }
                if (selectedItem != null) {
                    selectedItem.type = 0
                }
            }
        }
        return selectedItem
    }

    var maxCount = 0.0
    var minBuyPrice = 0.0
    var maxSalePrice = 0.0
    var maxBuyCount = 0.0
    var maxSaleCount = 0.0
    var countSteps = DoubleArray(STEP_COUNT) //数量数字
    var countStepCoordinates = DoubleArray(STEP_COUNT) //数量坐标

    private fun clearMaxMin() {
        maxCount = 0.0
        maxBuyCount = 0.0
        maxSaleCount = 0.0
        for (i in 0 until STEP_COUNT) {
            countSteps[i] = 0.0
            countStepCoordinates[i] = 0.0
        }
    }

    //处理数据，计算数量节点，计算数据坐标
    private fun handleData() {
        if (data == null) {
            return
        }
        val halfChartWidth = chartWidth.toDouble() / 2
        var totalBuyCount = 0.0
        var totalSaleCount = 0.0
        data?.buyItems?.let {
            for (i in it.indices) {
                totalBuyCount += it[i].count
            }
        }
        data?.saleItems?.let {
            for (i in it.indices) {
                totalSaleCount += it[i].count
            }
        }
        maxCount = Math.max(totalBuyCount, totalSaleCount)
        minBuyPrice = 0.0
        if (data?.buyItems != null && data?.buyItems!!.size > 1) {
            val minBuyPrice = data?.buyItems!![0].price
            this.minBuyPrice = minBuyPrice
            val maxBuyPrice = data?.buyItems!![data?.buyItems!!.size - 1].price
            val buyPriceRange = data!!.middlePrice - minBuyPrice
            var buyCountOffset = 0.0
            for (i in data?.buyItems!!.indices) {
                val buyItem = data?.buyItems!![data?.buyItems!!.size - i - 1]
                buyItem.x = (halfChartWidth * (buyItem.price - minBuyPrice) / buyPriceRange).toFloat()
                buyCountOffset += buyItem.count
                buyItem.y = ((maxCount - buyCountOffset) * chartHeight / maxCount).toFloat()
            }
        }
        maxSalePrice = 0.0
        if (data?.saleItems != null && data?.saleItems!!.size > 1) { //            double minSalePrice = data.saleItems.get(0).price;
            val maxSalePrice = data?.saleItems!![data?.saleItems!!.size - 1].price
            this.maxSalePrice = maxSalePrice
            val salePriceRange = maxSalePrice - data!!.middlePrice
            var saleCountOffset = 0.0
            for (i in data?.saleItems!!.indices) {
                val saleItem = data?.saleItems!![i]
                saleItem.x = (halfChartWidth + halfChartWidth * (saleItem.price - data!!.middlePrice) / salePriceRange).toFloat()
                saleCountOffset += saleItem.count
                saleItem.y = ((maxCount - saleCountOffset) * chartHeight / maxCount).toFloat()
                //            Item saleItem = data.salfChartWidth + halfChartWidth * (maxSalePrice - saleItem.price) / salePriceRange);
//            saleItem.y = (float) ((maleItems.get(data.saleItems.size() - 1 - i);
//            saleItem.x = (float) (haxCount - saleCountOffset) * chartHeight / maxCount);
            }
        }
        if (selectedItem != null) {
            selectedItem = getSelectedItem(selectedItem!!.x)
        }
    }

    fun setMiddlePrice(middlePrice: Double) {
        if (middlePrice > 0) {
            if (data == null) {
                data = Data()
            }
            data?.middlePrice = middlePrice
            postInvalidate()
        }
    }

    fun setPrecision(precision: Int) {
        this.precision = precision
    }

    fun setAmountPrecision(amountPrecision: Int) {
        this.amountPrecision = amountPrecision
    }

    private var data: Data? = null
    fun setData(data: Data?) {
        this.data = data
        if (this.data?.buyItems != null) {
            Collections.sort(this.data?.buyItems, Item.PRICE_COMPARATOR)
        }
        if (this.data?.saleItems != null) {
            Collections.sort(this.data?.saleItems, Item.PRICE_COMPARATOR)
        }
        handleData()
        invalidate()
    }

    private val testsData: Data
        private get() {
            val data = Data()
            data.middlePrice = randomPrice
            for (i in 0..199) {
                data.buyItems?.add(Item(data.middlePrice * Math.random(), randomCount))
                data.saleItems?.add(Item(data.middlePrice + data.middlePrice * Math.random(), randomCount * 1.1))
            }
            if (this.data?.buyItems != null) {
                Collections.sort(data.buyItems, Item.PRICE_COMPARATOR)
            }
            if (this.data?.saleItems != null) {
                Collections.sort(data.saleItems, Item.PRICE_COMPARATOR)
            }
            return data
        }

    private val randomPrice: Double
        private get() = Math.random() * 3

    private val randomCount: Double
        private get() = Math.random() * 2

    class Data {
        var middlePrice = 0.0
        //        double buyMinPrice;
//        double saleMaxPrice;
        var buyItems: MutableList<Item>? = ArrayList()
        var saleItems: MutableList<Item>? = ArrayList()
    }

    class Item(var price: Double, var count: Double) {
        var type //0 买入 1 卖
                = 0
        var x = 0f
        var y //坐标
                = 0f

        companion object {
            val PRICE_COMPARATOR: Comparator<Item?> = Comparator { o1, o2 ->
                if (o1 == null || o2 == null) {
                    0
                } else o1.price.compareTo(o2.price)
            }
        }

    }
}