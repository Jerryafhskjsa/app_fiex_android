package com.black.lib.view

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.black.lib.camera.CameraManager
import com.google.zxing.ResultPoint
import java.util.*

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
class ViewfinderView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        private const val TAG = "log"
        /**
         * 刷新界面的时间
         */
        private const val ANIMATION_DELAY = 10L
        private const val OPAQUE = 0xFF
        /**
         * 四个绿色边角对应的宽度
         */
        private const val CORNER_WIDTH = 10
        /**
         * 扫描框中的中间线的宽度
         */
        private const val MIDDLE_LINE_WIDTH = 6
        /**
         * 扫描框中的中间线的与扫描框左右的间隙
         */
        private const val MIDDLE_LINE_PADDING = 5
        /**
         * 中间那条线每次刷新移动的距离
         */
        private const val SPEEN_DISTANCE = 5
        /**
         * 手机的屏幕密度
         */
        private var density: Float = 1.0.toFloat()
        /**
         * 字体大小
         */
        private const val TEXT_SIZE = 15
        /**
         * 字体距离扫描框下面的距离
         */
        private const val TEXT_PADDING_TOP = 30
    }

    /**
     * 四个绿色边角对应的长度
     */
    private val ScreenRate: Int
    /**
     * 画笔对象的引用
     */
    private val paint: Paint
    /**
     * 中间滑动线的最顶端位置
     */
    private var slideTop = 0
    /**
     * 中间滑动线的最底端位置
     */
    private var slideBottom = 0
    /**
     * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
     */
    private var resultBitmap: Bitmap? = null
    private var maskColor: Int
    private var resultColor: Int
    private var resultPointColor: Int
    private var possibleResultPoints: MutableCollection<ResultPoint>
    private var lastPossibleResultPoints: Collection<ResultPoint>? = null
    var isFirst = false
    private var scanText: String? = null

    init {
        density = context.resources.displayMetrics.density
        //将像素转换成dp
        ScreenRate = (20 * density).toInt()
        paint = Paint()
        val resources = resources
        maskColor = Color.alpha(-0x50000000)
        resultColor = Color.alpha(-0x50000000)
        resultPointColor = Color.alpha(-0x3f0042df)
        possibleResultPoints = HashSet(5)
    }

    fun setMaskColors(maskColor: Int, resultColor: Int, resultPointColor: Int) {
        this.maskColor = maskColor
        this.resultColor = resultColor
        this.resultPointColor = resultPointColor
    }

    fun setScanText(scanText: String?) {
        this.scanText = scanText
    }

    public override fun onDraw(canvas: Canvas) {
        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        val frame = CameraManager.get()?.framingRect ?: return
        //初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true
            slideTop = frame.top
            slideBottom = frame.bottom
        }
        //获取屏幕的宽和高
        val width = canvas.width
        val height = canvas.height
        paint.color = if (resultBitmap != null) resultColor else maskColor
        //画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
//扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), frame.bottom + 1.toFloat(), paint)
        canvas.drawRect(frame.right + 1.toFloat(), frame.top.toFloat(), width.toFloat(), frame.bottom + 1.toFloat(),
                paint)
        canvas.drawRect(0f, frame.bottom + 1.toFloat(), width.toFloat(), height.toFloat(), paint)
        if (resultBitmap != null) { // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = OPAQUE
            canvas.drawBitmap(resultBitmap, frame.left.toFloat(), frame.top.toFloat(), paint)
        } else { //画扫描框边上的角，总共8个部分
            paint.color = Color.GREEN
            canvas.drawRect(frame.left.toFloat(), frame.top.toFloat(), frame.left + ScreenRate.toFloat(),
                    frame.top + CORNER_WIDTH.toFloat(), paint)
            canvas.drawRect(frame.left.toFloat(), frame.top.toFloat(), frame.left + CORNER_WIDTH.toFloat(), (frame.top
                    + ScreenRate).toFloat(), paint)
            canvas.drawRect(frame.right - ScreenRate.toFloat(), frame.top.toFloat(), frame.right.toFloat(),
                    frame.top + CORNER_WIDTH.toFloat(), paint)
            canvas.drawRect(frame.right - CORNER_WIDTH.toFloat(), frame.top.toFloat(), frame.right.toFloat(), (frame.top
                    + ScreenRate).toFloat(), paint)
            canvas.drawRect(frame.left.toFloat(), frame.bottom - CORNER_WIDTH.toFloat(), (frame.left
                    + ScreenRate).toFloat(), frame.bottom.toFloat(), paint)
            canvas.drawRect(frame.left.toFloat(), frame.bottom - ScreenRate.toFloat(),
                    frame.left + CORNER_WIDTH.toFloat(), frame.bottom.toFloat(), paint)
            canvas.drawRect(frame.right - ScreenRate.toFloat(), frame.bottom - CORNER_WIDTH.toFloat(),
                    frame.right.toFloat(), frame.bottom.toFloat(), paint)
            canvas.drawRect(frame.right - CORNER_WIDTH.toFloat(), frame.bottom - ScreenRate.toFloat(),
                    frame.right.toFloat(), frame.bottom.toFloat(), paint)
            //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE
            if (slideTop >= frame.bottom) {
                slideTop = frame.top
            }
            canvas.drawRect(frame.left + MIDDLE_LINE_PADDING.toFloat(), slideTop - MIDDLE_LINE_WIDTH / 2.toFloat(), frame.right - MIDDLE_LINE_PADDING.toFloat(), slideTop + MIDDLE_LINE_WIDTH / 2.toFloat(), paint)
            //画扫描框下面的字
            paint.color = Color.WHITE
            paint.textSize = TEXT_SIZE * density
            paint.alpha = 0x40
            paint.typeface = Typeface.create("System", Typeface.BOLD)
            canvas.drawText(if (TextUtils.isEmpty(scanText)) "Align the QR code within the frame to scan" else scanText, frame.left.toFloat(), frame.bottom + TEXT_PADDING_TOP.toFloat() * density, paint)
            val currentPossible: Collection<ResultPoint> = possibleResultPoints
            val currentLast = lastPossibleResultPoints
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null
            } else {
                possibleResultPoints = HashSet(5)
                lastPossibleResultPoints = currentPossible
                paint.alpha = OPAQUE
                paint.color = resultPointColor
                for (point in currentPossible) {
                    canvas.drawCircle(frame.left + point.x, frame.top
                            + point.y, 6.0f, paint)
                }
            }
            if (currentLast != null) {
                paint.alpha = OPAQUE / 2
                paint.color = resultPointColor
                for (point in currentLast) {
                    canvas.drawCircle(frame.left + point.x, frame.top
                            + point.y, 3.0f, paint)
                }
            }
            //只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom)
        }
    }

    fun drawViewfinder() {
        resultBitmap = null
        invalidate()
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    fun drawResultBitmap(barcode: Bitmap?) {
        resultBitmap = barcode
        invalidate()
    }

    fun addPossibleResultPoint(point: ResultPoint) {
        possibleResultPoints.add(point)
    }
}
