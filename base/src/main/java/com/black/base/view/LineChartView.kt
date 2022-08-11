package com.black.base.view
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

/**
 * 化带坐标的折线图
 * 不绘制坐标和折线上的圆点
 */
class LineChartView : View {
    private var minCriterion = 0
    private var hightCriterion = 0
    private var widthCriterion = 0
    private var canvasHeight = 0
    private var canvasWidth = 0
    private var textFont = 0
    private lateinit var xdata: Array<String>
    private lateinit var ydata: IntArray
    private lateinit var linedata: FloatArray
    private var paintColor: Int = Color.BLACK
    private var xCopies = 0
    private var yCopies = 0f

    fun setChartdate(xdata: Array<String>, ydata: IntArray, linedata: FloatArray,paintColor:Int) {
        this.xdata = xdata
        this.ydata = ydata
        this.linedata = linedata
        this.paintColor = paintColor
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("LineChartView","onDraw")
        if (xdata.isNotEmpty() && ydata.isNotEmpty() && linedata.isNotEmpty() && xdata.size >= linedata.size) {
            if (yMaxdata() >= lineMaxdata()) {
                drawAxis(canvas)
            }
        }
    }
    //绘制
    private fun drawAxis(canvas: Canvas) {
        xCopies = xdata.size + 2
        yCopies = (ydata.size + 2).toFloat()
        val daxesPaint: Paint
        val axispointPaint: Paint
        val brokenLinePaint: Paint
        //画布宽度
        canvasWidth = canvas.width
        //画布高度
        canvasHeight = canvas.height
        widthCriterion = canvasWidth / xCopies
        hightCriterion = (canvasHeight / yCopies).toInt()
        minCriterion =
            if (widthCriterion > hightCriterion) hightCriterion / 2 else widthCriterion / 2
        //开始绘制底层背景
        daxesPaint = Paint()
        daxesPaint.color = this.paintColor
        daxesPaint.isAntiAlias = true //去掉锯齿效果
        daxesPaint.strokeWidth = 7.0f
//        drawDaxes(canvas, daxesPaint)

        //开始绘制坐标点
        axispointPaint = daxesPaint
//        drawAxispoint(canvas, axispointPaint)

        //开始绘制折线和线上的点
        brokenLinePaint = axispointPaint
        brokenLinePaint.strokeWidth = 3.0f
        drawbrokenLine(canvas, brokenLinePaint)
    }

    private fun drawDaxes(canvas: Canvas, p: Paint) {
        //开始y绘制坐标系
        canvas.drawLine(
            widthCriterion.toFloat(),
            hightCriterion.toFloat(),
            widthCriterion.toFloat(),
            hightCriterion * (yCopies - 1),
            p
        )
        //绘制y角
        canvas.drawLine(
            (widthCriterion - minCriterion).toFloat(),
            (hightCriterion + minCriterion).toFloat(),
            (widthCriterion + 2).toFloat(),
            hightCriterion.toFloat(),
            p
        )
        canvas.drawLine(
            widthCriterion.toFloat(),
            hightCriterion.toFloat(),
            (widthCriterion + minCriterion - 2).toFloat(),
            (hightCriterion + minCriterion).toFloat(),
            p
        )
        //开始x绘制坐标系
        canvas.drawLine(
            (widthCriterion - 4).toFloat(),
            hightCriterion * (yCopies - 1),
            (widthCriterion * (xCopies - 1)).toFloat(),
            hightCriterion * (yCopies - 1),
            p
        )
        //绘制x角
        canvas.drawLine(
            (widthCriterion * (xCopies - 1) - minCriterion).toFloat(),
            hightCriterion * (yCopies - 1) - minCriterion,
            (widthCriterion * (xCopies - 1)).toFloat(),
            hightCriterion * (yCopies - 1) + 2,
            p
        )
        canvas.drawLine(
            (widthCriterion * (xCopies - 1) - minCriterion).toFloat(),
            hightCriterion * (yCopies - 1) + minCriterion,
            (widthCriterion * (xCopies - 1)).toFloat(),
            hightCriterion * (yCopies - 1) - 2,
            p
        )
    }

    private fun drawAxispoint(canvas: Canvas, p: Paint) {
        textFont = widthCriterion / 5 * 2
        val font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        p.typeface = font
        p.textSize = textFont.toFloat()
        //画x轴数据
        for (i in xdata.indices) {
            val text = xdata[i]
            val stringWidth = p.measureText(text).toInt() //文本长度
            canvas.drawText(
                text,
                ((i + 1) * widthCriterion - stringWidth / 2).toFloat(),
                hightCriterion * (yCopies - 1) + textFont,
                p
            ) // 画文本
        }
        for (i in ydata.indices) {
            val text = ydata[i].toString()
            val stringWidth = p.measureText(text).toInt()
            //文本长度
            if (i == 0) {
            } else {
                canvas.drawText(
                    text,
                    (widthCriterion - textFont - stringWidth).toFloat(),
                    hightCriterion * (yCopies - 1) - i * hightCriterion + stringWidth / 2,
                    p
                ) // 画文本
            }
        }
    }

    private fun drawbrokenLine(canvas: Canvas, p: Paint) {
        val line = (hightCriterion * (yCopies - 1) - hightCriterion * 2) / ydata[ydata.size - 1]
        for (i in linedata.indices) {
            val height = hightCriterion * (yCopies - 1) - line * linedata[i]
            if (i != linedata.size - 1) {
                val elseheight = hightCriterion * (yCopies - 1) - line * linedata[i + 1]
                canvas.drawLine(
                    (widthCriterion * (i + 1)).toFloat(),
                    height,
                    (widthCriterion * (i + 2)).toFloat(),
                    elseheight,
                    p
                )
//                canvas.drawCircle((widthCriterion * (i + 1)).toFloat(), height, 10f, p)
            } else {
//                val endheight = hightCriterion * (yCopies - 1) - line * linedate[linedate.size - 1]
//                canvas.drawCircle((widthCriterion * (i + 1)).toFloat(), endheight, 10f, p)
            }
        }
    }

    private fun yMaxdata(): Float {
        var max = 0f
        for (i in ydata.indices) {
            if (ydata[i] > max) {
                max = ydata[i].toFloat()
            }
        }
        return max
    }

    private fun lineMaxdata(): Float {
        var max = 0f
        for (i in linedata.indices) {
            if (linedata[i] > max) {
                max = linedata[i]
            }
        }
        return max
    }
}