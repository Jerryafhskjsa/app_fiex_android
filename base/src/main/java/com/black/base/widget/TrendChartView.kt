package com.black.base.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import com.black.base.R
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatView

//走势图
class TrendChartView : SkinCompatView {
    companion object {
        private const val TAG = "TrendChartView"
        private const val WIDTH = 300
        private const val HEIGHT = 90
        private var NONE_COLOR = Color.argb(0x00, 0x66, 0xae, 0xe6)
        private var START_COLOR = Color.argb(0xab, 0xc2, 0xd1, 0xf9)
        protected fun drawTextCenterVertical(canvas: Canvas, text: String?, paint: Paint, x: Float, y: Float, gravity: Int) {
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

        protected fun getShowTextCenterVerticalY(y: Float, paint: Paint): Float {
            return y + (Math.abs(paint.ascent()) - paint.descent()) / 2
        }
    }

    private var titleColor = Color.rgb(0xcc, 0xcc, 0xcc)
    private var valueColor = Color.rgb(0x44, 0x6C, 0xDB)
    private var dateColor = Color.rgb(0x33, 0x33, 0x33)
    private var dateSelectedColor = Color.rgb(0x44, 0x6C, 0xDB)
    private var lineColor = Color.rgb(0x44, 0x6C, 0xDB)
    private var pointColor = Color.rgb(0x44, 0x6C, 0xDB)
    private var pointShadowColor = Color.argb(0x80, 0x27, 0x4b, 0xae)
    private var title: String? = null
    //颜色 c13 #446CDB
//投影：不透明度：50%     颜色：#274bae
//    #CCCCCC
//#C2D1F9
    private var density = 0f
    private var leftSpace = 0f
    private var rightSpace = 0f
    private var topSpace = 0f
    private var bottomSpace = 0f
    private var bottomTabHeight = 0f
    private var columnCount = 7
    private var data: MutableList<TrendChartItem>? = ArrayList()
    private var positions: Array<ItemPosition>? = null
    private var textPaint = Paint()
    private val linePaint = Paint()
    private val pointPaint = Paint()
    private val graphPaint = Paint()
    var parentTouch = false
    private var dataAverage = false

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val dm = resources.displayMetrics
        density = dm.density
        titleColor = SkinCompatResources.getColor(context, R.color.C5)
        valueColor = SkinCompatResources.getColor(context, R.color.C2)
        dateColor = SkinCompatResources.getColor(context, R.color.C1)
        dateSelectedColor = SkinCompatResources.getColor(context, R.color.C2)
        lineColor = SkinCompatResources.getColor(context, R.color.C2)
        pointColor = SkinCompatResources.getColor(context, R.color.C2)
        pointShadowColor = SkinCompatResources.getColor(context, R.color.C2)
        pointShadowColor = Color.argb(0x80, Color.red(pointShadowColor), Color.green(pointShadowColor), Color.blue(pointShadowColor))
        START_COLOR = SkinCompatResources.getColor(context, R.color.C2)
        START_COLOR = Color.argb(0xab, Color.red(START_COLOR), Color.green(START_COLOR), Color.blue(START_COLOR))
        val bc2 = SkinCompatResources.getColor(context, R.color.B2)
        NONE_COLOR = Color.argb(0x00, Color.red(bc2), Color.green(bc2), Color.blue(bc2))
        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 9 * density
        topSpace = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent + 3 * density
        textPaint.textSize = 6 * density
        bottomTabHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent
        bottomSpace = -0f
        leftSpace = 9 * density
        rightSpace = 9 * density
        linePaint.isAntiAlias = true
        linePaint.color = lineColor
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 2 * density
        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL
        pointPaint.strokeWidth = 2 * density
        graphPaint.isAntiAlias = true
        graphPaint.color = START_COLOR
        graphPaint.style = Paint.Style.STROKE
        graphPaint.strokeWidth = 1f
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
        computePosition()
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

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mActivePointerId = 0
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                parentTouch = false
                mActivePointerId = ev.getPointerId(0)
                try {
                    mLastMotionX = ev.x
                    mLastMotionY = ev.y
                } catch (ignored: Exception) {
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                parentTouch = false
                val index = ev.actionIndex
                try {
                    mLastMotionX = ev.getX(index)
                    mLastMotionY = ev.getY(index)
                } catch (ignored: Exception) {
                }
                mActivePointerId = ev.getPointerId(index)
            }
            MotionEvent.ACTION_MOVE -> if (!parentTouch) {
                val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                var x = 0f
                var y = 0f
                try {
                    x = ev.getX(activePointerIndex)
                    y = ev.getY(activePointerIndex)
                } catch (ignored: Exception) {
                }
                val deltaX = mLastMotionX - x
                val deltaY = mLastMotionY - y
                if (Math.abs(deltaY) > Math.abs(deltaX) * 1.5) {
                    parentTouch = true
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                val clickTime = ev.eventTime - ev.downTime
                val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                var x = 0f
                var y = 0f
                try {
                    x = ev.getX(activePointerIndex)
                    y = ev.getY(activePointerIndex)
                } catch (ignored: Exception) {
                }
                val deltaX = mLastMotionX - x
                val deltaY = mLastMotionY - y
                val moveDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY.toDouble())
                if (clickTime <= 200 && moveDistance < 50 * density) {
                    onItemClick(ev)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val background = background
        background?.draw(canvas)
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        //绘制标题
        textPaint.color = titleColor
        textPaint.textSize = 9 * density
        drawTextCenterVertical(canvas, if (title == null) "" else title, textPaint, paddingLeft.toFloat(), topSpace, Gravity.LEFT)
        if (positions == null || positions?.size == 0) {
            return
        }
        val linePath = Path()
        for (i in positions!!.indices) {
            val itemPosition = positions!![i]
            if (linePath.isEmpty) {
                linePath.moveTo(itemPosition?.x, itemPosition.y)
            } else {
                linePath.lineTo(itemPosition?.x, itemPosition.y)
            }
            if (itemPosition.isSelected) { //绘制圆点阴影
                pointPaint.color = pointShadowColor
                canvas.drawCircle(itemPosition.x, itemPosition.y, (4.5 * density).toFloat(), pointPaint)
                //绘制数量
                textPaint.color = valueColor
                textPaint.textSize = 9 * density
                drawTextCenterVertical(canvas, itemPosition.valueText, textPaint, itemPosition.x, itemPosition.y - 30, Gravity.CENTER)
                //绘制日期
                if (i == 0 || i % 5 == 0) {
                    textPaint.color = dateSelectedColor
                    textPaint.textSize = 6 * density
                    drawTextCenterVertical(canvas, itemPosition.timeText, textPaint, itemPosition.x, height - paddingBottom.toFloat(), Gravity.CENTER)
                }
            } else { //绘制日期
                textPaint.color = dateColor
                textPaint.textSize = 6 * density
                if (i == 0 || i % 5 == 0) {
                    drawTextCenterVertical(canvas, itemPosition.timeText, textPaint, itemPosition.x, height - paddingBottom.toFloat(), Gravity.CENTER)
                }
            }
            //绘制圆点
//            pointPaint.setColor(pointColor);
//            canvas.drawCircle(itemPosition.x, itemPosition.y, (float) (2.5 * density), pointPaint);
        }
        //绘制曲线
        canvas.drawPath(linePath, linePaint)
        //绘制阴影
        val shader: Shader = LinearGradient(0f, paddingTop + topSpace, 0f, height.toFloat(), intArrayOf(START_COLOR, NONE_COLOR), null, Shader.TileMode.REPEAT)
        graphPaint.shader = shader
        graphPaint.style = Paint.Style.FILL
        linePath.lineTo(positions!![positions!!.size - 1]?.x, height.toFloat())
        linePath.lineTo(positions!![0]?.x, height.toFloat())
        canvas.drawPath(linePath, graphPaint)
    }

    fun setColumnCount(columnCount: Int) {
        if (columnCount > 0) {
            this.columnCount = columnCount
        }
    }

    fun setDataAverage(dataAverage: Boolean) {
        this.dataAverage = dataAverage
    }

    fun setTitle(title: String?) {
        this.title = title
        postInvalidate()
    }

    private fun onItemClick(ev: MotionEvent) { //判断点击位置，选中item
        val selectX = ev.x
        val selectY = ev.y
        if (selectX < paddingLeft || selectX > width - paddingRight || selectY < paddingTop + topSpace || selectY > height - paddingBottom - bottomSpace - bottomTabHeight) {
            return
        }
        for (itemPosition in positions!!) {
            val actionPath = itemPosition?.actionPath
            if (actionPath == null) {
                continue
            }
            itemPosition.isSelected = actionPath?.left <= selectX && actionPath.right >= selectX && actionPath.top <= selectY && actionPath.bottom >= selectY
        }
        postInvalidate()
    }

    private fun selectItem(position: Int) {
        if (position < positions?.size ?: 0) {
            for (i in positions!!.indices) {
                val itemPosition = positions!![i]
                itemPosition?.isSelected = i == position
            }
            postInvalidate()
        }
    }

    fun setData(data: MutableList<TrendChartItem>?) {
        this.data = data
        if (this.data == null) {
            this.data = ArrayList()
        }
        computePosition()
    }

    private fun computePosition() {
        if (data == null) {
            data = ArrayList()
        }
        if (data!!.isEmpty()) {
            positions = emptyArray()
            return
        }
        //计算绘制尺寸
        val realWidth = width - paddingLeft - paddingRight - leftSpace - rightSpace
        val realHeight = height - paddingTop - paddingBottom - topSpace - bottomSpace
        val top = paddingTop + topSpace
        val left = paddingLeft.toFloat()
        val right = left + realWidth + leftSpace + rightSpace
        val bottom = top + realHeight
        val curveLeft = left + leftSpace
        val curveWidth = width - paddingLeft - paddingRight - leftSpace - rightSpace
        val curveHeight = realHeight - bottomTabHeight
        //取出有效的数据
        val dataCount = data?.size ?: 0
        var validData: MutableList<TrendChartItem>
        if (dataCount <= columnCount) {
            validData = ArrayList(data)
        } else {
            validData = data?.subList(dataCount - columnCount, dataCount) ?: ArrayList()
        }
        val validCount = validData.size
        //处理无效数据
        for (i in 0 until validCount) {
            val item: TrendChartItem = validData.get(i)
            if (item == null) {
                validData?.set(i, TrendChartItem())
            }
        }
        if (validCount > 1) { //计算最大的值
            var maxValue = 0.0
            var minValue = Double.MAX_VALUE
            for (item in validData) {
                maxValue = Math.max(maxValue, item.value)
                minValue = Math.min(minValue, item.value)
            }
            if (minValue == maxValue) {
                minValue = 0.0
            }
            //计算单位值所占像素
            val valueWeight = if (maxValue == 0.0) curveHeight.toDouble() else curveHeight.toDouble() / 4 / (maxValue - minValue)
            //分配宽度
            val itemWidth = realWidth / (columnCount - 1)
            //从最后的item开始计算位置
            positions = Array(validCount) {
                ItemPosition()
            }
            for (i in validCount - 1 downTo 0) {
                val item = validData.get(i)
                val position = ItemPosition()
                val x = curveLeft + curveWidth - itemWidth * (validCount - 1 - i)
                var y = 0f
                y = if (item.value > 0) {
                    (top + (maxValue - item.value) * valueWeight).toFloat()
                } else {
                    top + curveHeight
                }
                position.x = x
                position.y = y
                position.isSelected = false
                position.valueText = item.valueText
                position.timeText = item.timeText
                if (i == validCount - 1) {
                    position.actionPath = RectF(x - itemWidth / 2, top, x + rightSpace, bottom)
                } else if (i == 0) {
                    position.actionPath = RectF(left, top, x + itemWidth / 2, bottom)
                } else {
                    position.actionPath = RectF(x - itemWidth / 2, top, x + itemWidth / 2, bottom)
                }
                positions!![i] = position
            }
        } else {
            val item: TrendChartItem = validData.get(0)
            if (item == null) {
                positions = emptyArray()
            } else {
                positions = Array(validCount) {
                    ItemPosition()
                }
                val position = ItemPosition()
                position.x = curveLeft + curveWidth
                if (item.value > 0) {
                    position.y = top
                } else {
                    position.y = top + curveHeight
                }
                position.actionPath = RectF(left, top, right, bottom)
                position.isSelected = false
                position.valueText = item.valueText
                position.timeText = item.timeText
                positions!![0] = position
            }
        }
        positions?.let {
            selectItem(it.size - 1)
        }
    }

    internal inner class ItemPosition {
        var x = 0f
        var y = 0f
        var actionPath: RectF? = null
        var isSelected = false
        var valueText: String? = null
        var timeText: String? = null
    }

    class TrendChartItem {
        var time: Long = 0
        var value = 0.0
        val timeText: String
            get() = if (time > 0) {
                CommonUtil.formatTimestamp("MM/dd", time)
            } else {
                "00/00"
            }

        val valueText: String
            get() {
                return if (value > 0) {
                    NumberUtil.formatNumberNoGroup(value * 100, 2, 8) + "%"
                } else {
                    "0.00"
                }
            }
    }
}