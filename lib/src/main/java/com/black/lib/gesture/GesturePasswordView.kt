package com.black.lib.gesture

import android.content.Context
import android.gesture.GesturePoint
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class GesturePasswordView : View {
    companion object {
        private const val DEFAULT_COLOR = Color.GREEN
        private const val CHECKED_COLOR = Color.BLUE
        private const val WIDTH = 300
        private const val HEIGHT = 300
        private const val LINE_WEIGHT = 5
        val DEFAULT_PASSWORD_BOOK = arrayOf<Any>(1, 2, 3, 4, 5, 6, 7, 8, 9)
        private var POINT_SIZE = 10
        private var AREA_SIZE = 30
    }

    private var density = 0f
    private var lineWeight = 0f
    private var linePaint: Paint? = null
    private val pointPaint: Paint? = null
    private var passwordBook = DEFAULT_PASSWORD_BOOK
    private var pointList: MutableList<GesturePasswordPoint?>? = ArrayList(9)
    private var checkedList: MutableList<GesturePasswordPoint>? = ArrayList()
    private var lastCheckedPoint: GesturePasswordPoint? = null
    private var eventPoint: Point? = null
    private var callBack: GetPasswordCallBack? = null
    private var defaultPoint: PointDisplay? = null
    private var checkedPoint: PointDisplay? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
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
        initPoints(width, height)
    }

    fun setPasswordBook(passwordBook: Array<Any>?) {
        if (passwordBook == null || passwordBook.size != 9) {
            throw RuntimeException("The password list is incorrect in length!")
        }
        this.passwordBook = passwordBook
    }

    fun setGetPasswordCallBack(callBack: GetPasswordCallBack?) {
        this.callBack = callBack
    }

    //获取密码
    val password: List<Any>?
        get() {
            if (checkedList == null || checkedList!!.isEmpty()) {
                return null
            }
            val password: MutableList<Any> = ArrayList()
            for (point in checkedList!!) {
                password.add(point.value)
            }
            return password
        }

    //清空
    fun clear() {
        if (checkedList == null) {
            checkedList = ArrayList()
        }
        for (point in checkedList!!) {
            point.setChecked(false)
        }
        checkedList?.clear()
        lastCheckedPoint = null
        eventPoint = null
        invalidate()
        if (callBack != null) {
            callBack?.onClear()
        }
    }

    fun check(position: Int, isChecked: Boolean) {
        if (pointList == null || pointList!!.isEmpty()) {
            return
        }
        val pointSize = pointList!!.size
        if (position > -1 && position < pointSize) {
            val point = pointList!![position]
            if (point != null) {
                point.setChecked(isChecked)
                lastCheckedPoint = point
                checkedList?.add(point)
                postInvalidate()
            }
        }
    }

    private fun init() {
        val dm = resources.displayMetrics
        density = dm.density
        lineWeight = LINE_WEIGHT * density
        linePaint = Paint()
        linePaint?.style = Paint.Style.STROKE
        linePaint?.strokeWidth = LINE_WEIGHT * density
        linePaint?.color = CHECKED_COLOR
    }

    //初始化九宫格点位
    private fun initPoints(width: Int, height: Int) {
        val size = Math.min(width, height)
        //设置点尺寸
        POINT_SIZE = size / 20
        AREA_SIZE = (POINT_SIZE * 2.4).toInt()
        val scale = size / 3
        val top = (height - size) / 2 + scale / 2
        val left = (width - size) / 2 + scale / 2
        if (pointList == null || pointList?.size != 9) {
            pointList = ArrayList(9)
            for (i in 0..8) {
                pointList!!.add(null)
            }
        }
        setPoint(0, left, top, passwordBook[0])
        setPoint(1, left + scale, top, passwordBook[1])
        setPoint(2, left + scale * 2, top, passwordBook[2])
        setPoint(3, left, top + scale, passwordBook[3])
        setPoint(4, left + scale, top + scale, passwordBook[4])
        setPoint(5, left + scale * 2, top + scale, passwordBook[5])
        setPoint(6, left, top + scale * 2, passwordBook[6])
        setPoint(7, left + scale, top + scale * 2, passwordBook[7])
        setPoint(8, left + scale * 2, top + scale * 2, passwordBook[8])
        invalidate()
    }

    fun setStyle(lineColor: Int, lineWeight: Float, defaultPoint: PointDisplay, checkedPoint: PointDisplay) {
        this.defaultPoint = defaultPoint
        this.checkedPoint = checkedPoint
        if (pointList != null && pointList!!.isNotEmpty()) {
            for (passwordPoint in pointList!!) {
                if (passwordPoint != null) {
                    passwordPoint.defaultPointDisplay = defaultPoint.clone()
                    passwordPoint.defaultPointDisplay?.init(passwordPoint.x, passwordPoint.y)
                    passwordPoint.checkedPointDisplay = checkedPoint.clone()
                    passwordPoint.checkedPointDisplay?.init(passwordPoint.x, passwordPoint.y)
                }
            }
        }
        this.lineWeight = lineWeight
        linePaint?.strokeWidth = lineWeight
        linePaint?.color = lineColor
    }

    private fun setPoint(index: Int, x: Int, y: Int, value: Any) {
        var point = pointList!![index]
        val isChecked = point != null && point.isChecked()
        point = GesturePasswordPoint(x.toFloat(), y.toFloat(), value)
        point.defaultPointDisplay = if (defaultPoint == null) GesturePasswordPointDefault(context, DEFAULT_COLOR) else defaultPoint?.clone()
        point.defaultPointDisplay?.init(x.toFloat(), y.toFloat())
        point.checkedPointDisplay = if (checkedPoint == null) GesturePasswordPointChecked(context, DEFAULT_COLOR, CHECKED_COLOR) else checkedPoint?.clone()
        point.checkedPointDisplay?.init(x.toFloat(), y.toFloat())
        point.setChecked(isChecked)
        pointList!![index] = point
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        //画出点位
        for (point in pointList!!) {
            point?.draw(canvas)
        }
        if (checkedList != null && checkedList!!.size > 1) { //画出连接线
            val path = Path()
            path.moveTo(checkedList!![0].x, checkedList!![0].y)
            for (i in 1 until checkedList!!.size) {
                path.lineTo(checkedList!![i].x, checkedList!![i].y)
            }
            if (lineWeight > 0) {
                canvas.drawPath(path, linePaint)
            }
        }
        //画出最后点和手势之间的线
        if (lastCheckedPoint != null && eventPoint != null) {
            val path = Path()
            path.moveTo(lastCheckedPoint!!.x, lastCheckedPoint!!.y)
            path.lineTo(eventPoint!!.x.toFloat(), eventPoint!!.y.toFloat())
            if (lineWeight > 0) {
                canvas.drawPath(path, linePaint)
            }
        }
        canvas.restore()
    }

    //获取正被按压的点位
    private fun getPressedPoint(event: MotionEvent): GesturePasswordPoint? {
        val x = event.x
        val y = event.y
        pointList?.let {
            for (point in pointList!!) {
                if (point != null) {
                    if (Math.sqrt(Math.pow(x - point.x.toDouble(), 2.0) + Math.pow(y - point.y.toDouble(), 2.0)) <= AREA_SIZE) {
                        return point
                    }
                }
            }
        }
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        eventPoint = Point(event.x.toInt(), event.y.toInt())
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val downPoint = getPressedPoint(event)
                if (downPoint != null) {
                    downPoint.setChecked(true)
                    lastCheckedPoint = downPoint
                    checkedList?.add(downPoint)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val movePoint = getPressedPoint(event)
                checkedList?.let {
                    if (movePoint != null && movePoint != lastCheckedPoint && !checkedList!!.contains(movePoint)) {
                        movePoint.setChecked(true)
                        lastCheckedPoint = movePoint
                        checkedList?.add(movePoint)
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (callBack != null) {
                    callBack?.getPassword(password)
                }
                clear()
            }
            else -> {
            }
        }
        return true
    }

    interface GetPasswordCallBack {
        fun getPassword(password: List<Any>?)
        fun onClear()
        fun onPointChecked(position: Int, isChecked: Boolean, value: Any)
    }

    protected inner class GesturePasswordPoint(x: Float, y: Float, val value: Any) : GesturePoint(x, y, System.currentTimeMillis()) {
        private var isChecked = false
        var defaultPointDisplay: PointDisplay? = null
        var checkedPointDisplay: PointDisplay? = null
        fun isChecked(): Boolean {
            return isChecked
        }

        fun setChecked(checked: Boolean) {
            isChecked = checked
            if (callBack != null && pointList != null) {
                callBack?.onPointChecked(pointList!!.indexOf(this), checked, value)
            }
        }

        override fun toString(): String {
            return value.toString()
        }

        fun draw(canvas: Canvas?) {
            //绘制边缘
            if (isChecked) {
                if (checkedPointDisplay != null) {
                    checkedPointDisplay?.draw(canvas!!, x, y)
                } else {
                    throw RuntimeException("checkedPointDisplay is null")
                }
            } else {
                if (defaultPointDisplay != null) {
                    defaultPointDisplay?.draw(canvas!!, x, y)
                } else {
                    throw RuntimeException("defaultPointDisplay is null")
                }
            }
        }

    }
}
