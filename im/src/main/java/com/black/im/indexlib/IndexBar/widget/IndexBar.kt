package com.black.im.indexlib.IndexBar.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.black.im.R
import com.black.im.indexlib.IndexBar.bean.BaseIndexPinyinBean
import com.black.im.indexlib.IndexBar.helper.IIndexBarDataHelper
import com.black.im.indexlib.IndexBar.helper.IndexBarDataHelperImpl
import com.black.im.manager.CustomLinearLayoutManager
import java.util.*

/**
 * 介绍：索引右侧边栏
 */
class IndexBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    //是否需要根据实际的数据来生成索引数据源（例如 只有 A B C 三种tag，那么索引栏就 A B C 三项）
    private var isNeedRealIndex = false
    //索引数据源
    private var mIndexDatas: MutableList<String?>? = null
    //View的宽高
    private var mWidth = 0
    private var mHeight = 0
    //每个index区域的高度
    private var mGapHeight = 0
    private var mPaint: Paint? = null
    //手指按下时的背景色
    private var mPressedBackground = 0
    //以下是帮助类
//汉语->拼音，拼音->tag
    var dataHelper: IIndexBarDataHelper? = null
        private set
    //以下边变量是外部set进来的
    private var mPressedShowTextView: TextView? = null //用于特写显示正在被触摸的index值
    var isSourceDatasAlreadySorted = false  //源数据 已经有序？
        private set
    private var mSourceDatas: MutableList<out BaseIndexPinyinBean?>? = null //Adapter的数据源
    private var mLayoutManager: CustomLinearLayoutManager? = null
    var headerViewCount = 0
        private set
    private var onIndexPressedListener: OnIndexPressedListener? = null

    init {
        var textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics).toInt() //默认的TextSize
        mPressedBackground = Color.BLACK //默认按下是纯黑色
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0)
        val n = typedArray.indexCount
        for (i in 0 until n) {
            val attr = typedArray.getIndex(i)
            //modify 2016 09 07 :如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.IndexBar_indexBarTextSize) {
                textSize = typedArray.getDimensionPixelSize(attr, textSize)
            } else if (attr == R.styleable.IndexBar_indexBarPressBackground) {
                mPressedBackground = typedArray.getColor(attr, mPressedBackground)
            }
        }
        typedArray.recycle()
        initIndexDatas()
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        mPaint?.textSize = textSize.toFloat()
        mPaint?.color = Color.GRAY
        //设置index触摸监听器
        onIndexPressedListener = object : OnIndexPressedListener {
            override fun onIndexPressed(index: Int, text: String?) {
                if (mPressedShowTextView != null) { //显示hintTexView
                    mPressedShowTextView?.visibility = VISIBLE
                    mPressedShowTextView?.text = text
                }
                //滑动Rv
                if (mLayoutManager != null) {
                    val position = getPosByTag(text)
                    if (position != -1) {
                        mLayoutManager?.scrollToPositionWithOffset(position, 0)
                    }
                }
            }

            override fun onMotionEventEnd() { //隐藏hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView?.visibility = GONE
                }
            }
        }
        dataHelper = IndexBarDataHelperImpl()
    }

    /**
     * 设置Headerview的Count
     *
     * @param headerViewCount
     * @return
     */
    fun setHeaderViewCount(headerViewCount: Int): IndexBar {
        this.headerViewCount = headerViewCount
        return this
    }

    /**
     * 源数据 是否已经有序
     *
     * @param sourceDatasAlreadySorted
     * @return
     */
    fun setSourceDatasAlreadySorted(sourceDatasAlreadySorted: Boolean): IndexBar {
        isSourceDatasAlreadySorted = sourceDatasAlreadySorted
        return this
    }

    /**
     * 设置数据源帮助类
     *
     * @param dataHelper
     * @return
     */
    fun setDataHelper(dataHelper: IIndexBarDataHelper?): IndexBar {
        this.dataHelper = dataHelper
        return this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) { //取出宽高的MeasureSpec  Mode 和Size
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        var measureWidth = 0
        var measureHeight = 0 //最终测量出来的宽高
        //得到合适宽度：
        val indexBounds = Rect() //存放每个绘制的index的Rect区域
        var index: String? //每个要绘制的index内容
        mIndexDatas?.let {
            for (i in it.indices) {
                index = mIndexDatas!![i]
                mPaint?.getTextBounds(index, 0, index?.length
                        ?: 0, indexBounds) //测量计算文字所在矩形，可以得到宽高
                measureWidth = Math.max(indexBounds.width(), measureWidth) //循环结束后，得到index的最大宽度
                measureHeight = Math.max(indexBounds.height(), measureHeight) //循环结束后，得到index的最大高度，然后*size
            }
            measureHeight *= mIndexDatas?.size ?: 0
        }
        when (wMode) {
            MeasureSpec.EXACTLY -> measureWidth = wSize
            MeasureSpec.AT_MOST -> measureWidth = Math.min(measureWidth, wSize) //wSize此时是父控件能给子View分配的最大空间
            MeasureSpec.UNSPECIFIED -> {
            }
        }
        when (hMode) {
            MeasureSpec.EXACTLY -> measureHeight = hSize
            MeasureSpec.AT_MOST -> measureHeight = Math.min(measureHeight, hSize) //wSize此时是父控件能给子View分配的最大空间
            MeasureSpec.UNSPECIFIED -> {
            }
        }
        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val t = paddingTop //top的基准点(支持padding)
        var index: String? //每个要绘制的index内容
        mIndexDatas?.let {
            for (i in it.indices) {
                index = it[i]
                mPaint?.let {
                    val fontMetrics = it.fontMetrics //获得画笔的FontMetrics，用来计算baseLine。因为drawText的y坐标，代表的是绘制的文字的baseLine的位置
                    val baseline = ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2).toInt() //计算出在每格index区域，竖直居中的baseLine值
                    canvas.drawText(index, mWidth / 2 - it.measureText(index) / 2, t + mGapHeight * i + baseline.toFloat(), mPaint) //调用drawText，居中显示绘制index
                }

            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setBackgroundColor(mPressedBackground) //手指按下时背景变色
                val y = event.y
                //通过计算判断落点在哪个区域：
                var pressI = ((y - paddingTop) / mGapHeight).toInt()
                //边界处理（在手指move时，有可能已经移出边界，防止越界）
                if (pressI < 0) {
                    pressI = 0
                } else if (pressI >= mIndexDatas?.size ?: 0) {
                    pressI = (mIndexDatas?.size ?: 0) - 1
                }
                //回调监听器
                if (null != onIndexPressedListener && pressI > -1 && pressI < mIndexDatas?.size ?: 0) {
                    onIndexPressedListener?.onIndexPressed(pressI, mIndexDatas!![pressI])
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                var pressI = ((y - paddingTop) / mGapHeight).toInt()
                if (pressI < 0) {
                    pressI = 0
                } else if (pressI >= mIndexDatas?.size ?: 0) {
                    pressI = (mIndexDatas?.size ?: 0) - 1
                }
                if (null != onIndexPressedListener && pressI > -1 && pressI < mIndexDatas?.size ?: 0) {
                    onIndexPressedListener?.onIndexPressed(pressI, mIndexDatas!![pressI])
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                setBackgroundResource(android.R.color.transparent) //手指抬起时背景恢复透明
                //回调监听器
                if (null != onIndexPressedListener) {
                    onIndexPressedListener?.onMotionEventEnd()
                }
            }
            else -> {
                setBackgroundResource(android.R.color.transparent)
                if (null != onIndexPressedListener) {
                    onIndexPressedListener?.onMotionEventEnd()
                }
            }
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        //解决源数据为空 或者size为0的情况,
        if (null == mIndexDatas || true == mIndexDatas?.isEmpty()) {
            return
        }
        computeGapHeight()
    }

    /**
     * 显示当前被按下的index的TextView
     *
     * @return
     */
    fun setPressedShowTextView(mPressedShowTextView: TextView?): IndexBar {
        this.mPressedShowTextView = mPressedShowTextView
        return this
    }

    fun setLayoutManager(mLayoutManager: CustomLinearLayoutManager?): IndexBar {
        this.mLayoutManager = mLayoutManager
        return this
    }

    /**
     * 一定要在设置数据源[.setSourceDatas]之前调用
     *
     * @param needRealIndex
     * @return
     */
    fun setNeedRealIndex(needRealIndex: Boolean): IndexBar {
        isNeedRealIndex = needRealIndex
        initIndexDatas()
        return this
    }

    private fun initIndexDatas() {
        mIndexDatas = if (isNeedRealIndex) {
            ArrayList()
        } else {
            Arrays.asList(*INDEX_STRING)
        }
    }

    fun setSourceDatas(mSourceDatas: MutableList<out BaseIndexPinyinBean?>?): IndexBar {
        this.mSourceDatas = mSourceDatas
        initSourceDatas() //对数据源进行初始化
        return this
    }

    /**
     * 初始化原始数据源，并取出索引数据源
     *
     * @return
     */
    private fun initSourceDatas() { // 解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || true == mSourceDatas?.isEmpty()) {
            return
        }
        if (!isSourceDatasAlreadySorted) { //排序sourceDatas
            dataHelper?.sortSourceDatas(mSourceDatas)
        } else { //汉语->拼音
            dataHelper?.convert(mSourceDatas)
            //拼音->tag
            dataHelper?.fillInexTag(mSourceDatas)
        }
        if (isNeedRealIndex) {
            dataHelper?.getSortedIndexDatas(mSourceDatas, mIndexDatas)
            computeGapHeight()
        }
        //sortData();
    }

    /**
     * 以下情况调用：
     * 1 在数据源改变
     * 2 控件size改变时
     * 计算gapHeight
     */
    private fun computeGapHeight() {
        mGapHeight = (mHeight - paddingTop - paddingBottom) / (mIndexDatas?.size ?: 0)
    }

    /**
     * 对数据源排序
     */
    private fun sortData() {}

    /**
     * 根据传入的pos返回tag
     *
     * @param tag
     * @return
     */
    private fun getPosByTag(tag: String?): Int {
        // 解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || true == mSourceDatas?.isEmpty()) {
            return -1
        }
        if (TextUtils.isEmpty(tag)) {
            return -1
        }
        for (i in mSourceDatas!!.indices) {
            if (tag == mSourceDatas!![i]?.baseIndexTag) {
                return i + headerViewCount
            }
        }
        return -1
    }

    /**
     * 当前被按下的index的监听器
     */
    interface OnIndexPressedListener {
        fun onIndexPressed(index: Int, text: String?) //当某个Index被按下
        fun onMotionEventEnd() //当触摸事件结束（UP CANCEL）
    }

    companion object {
        private val TAG = IndexBar::class.java.simpleName
        //#在最后面（默认的数据源）
        var INDEX_STRING = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z", "#")
    }
}