package com.black.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.black.base.util.SwipeLayoutManager

class SwipeLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
    companion object {
        const val TAG = "SwipeLayout"
    }

    private lateinit var contentView //item内容区域的view
            : View
    private lateinit var deleteView //delete区域的view
            : View
    private var contentHeight ///item内容区域的高度
            = 0
    private var contentWidth //item内容区域的宽
            = 0
    private var deleteHeight //delete区域的高度
            = 0
    private var deleteWidth //delete区域的宽度
            = 0
    private var mViewDragHelper //可以对viewgroup中的子view进行拖拽
            : ViewDragHelper
    private var downX = 0f
    private var downY = 0f
    private var density = 0f

    internal enum class SwipeState {
        Open, Close
    }

    private var currentState = SwipeState.Close //当前默认是关闭状态

    private val callback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        /**
         * 用于判断是否捕获当前child的触摸事件
         * @param child 当前触摸的子View
         * @param pointerId
         * @return true:捕获并解析  false:不处理
         */
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === contentView || child === deleteView
        }

        /**
         * 获取view水平方向的拖拽范围,但是目前不能限制边界,返回的值目前用在手指抬起的时候
         * view缓慢移动的动画时间的计算; 最好不要返回0
         * @param child
         * @return
         */
        override fun getViewHorizontalDragRange(child: View): Int {
            return deleteWidth
        }

        /**
         * 控制child在水平方向的移动
         * @param child 当前触摸的子View
         * @param left 当前child的即将移动到的位置,left=chile.getLeft()+dx
         * @param dx 本次child水平方向移动的距离
         * @return 表示你真正想让child的left变成的值
         */
        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int { //限定边界
            var left = left
            if (child === contentView) {
                if (left > 0) {
                    left = 0
                }
                //当deleteView完全滑出来时，contentView不能再滑动
                if (left < -deleteWidth) {
                    left = -deleteWidth
                }
            } else if (child === deleteView) {
                if (left > contentWidth) {
                    left = contentWidth
                }
                if (left < contentWidth - deleteWidth) {
                    left = contentWidth - deleteWidth
                }
            }
            return left
        }

        /**
         * 当child的位置改变的时候执行,一般用来做其他子View跟随该view移动
         * @param changedView 当前位置改变的child
         * @param left child当前最新的left
         * @param top child当前最新的top
         * @param dx 本次水平移动的距离
         * @param dy 本次垂直移动的距离
         */
        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            if (changedView === contentView) { //contentView移动的时候，deleteview伴随移动
                deleteView.layout(deleteView.left + dx, deleteView.top + dy,
                        deleteView.right + dx, deleteView.bottom + dy)
            } else if (changedView === deleteView) { //deleteview移动的时候，contentview伴随移动
                contentView.layout(contentView.left + dx, contentView.top + dy,
                        contentView.right + dx, contentView.bottom + dy)
            }
            /**
             * 问题2:不能同时打开多个条目，只能打开一个条目
             */
//判断开和关的状态
            if (contentView.left == 0 && currentState != SwipeState.Close) {
                currentState = SwipeState.Close //更改为关闭状态
                //回调接口关闭的方法
                if (listener != null) {
                    listener?.onClose(tag)
                }
                //说明当前的SwipeLayout已经关闭，需要让Manager清空一下
                if (!SwipeLayoutManager.instance.isShouldSwipe(this@SwipeLayout)) { //                    SwipeLayoutManager.instance.closeCurrentLayout();
//                    SwipeLayoutManager.instance.clearCurrentLayout();
                }
            } else if (contentView.left == -deleteWidth && currentState != SwipeState.Open) {
                currentState = SwipeState.Open
                //回调接口打开的方法
                if (listener != null) {
                    listener?.onOpen(tag)
                }
                //当前的Swipelayout已经打开，需要让Manager记录一下下
                SwipeLayoutManager.instance.setSwipeLayout(this@SwipeLayout)
            }
        }

        /**
         * 手指抬起的执行该方法
         * @param releasedChild 当前抬起的view
         * @param xvel x方向的移动速度有 正：向右移动
         * @param yvel 方向的移动速度
         */
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            if (contentView.left < -deleteWidth / 2) { //滑动超过一半，打开
                open()
            } else { //滑动小于一半，关闭
                close()
            }
        }
    }

    init {
        mViewDragHelper = ViewDragHelper.create(this, callback)
        val dm = resources.displayMetrics
        density = dm.density
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        contentView = getChildAt(0)
        deleteView = getChildAt(1)
    }

    /**
     * 这个方法会在onMeasure执行完后执行，可以在该方法中获取给控件自己和子控件的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        contentHeight = contentView.measuredHeight
        contentWidth = contentView.measuredWidth
        deleteHeight = deleteView.measuredHeight
        deleteWidth = deleteView.measuredWidth
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        contentHeight = contentView.measuredHeight
        contentWidth = contentView.measuredWidth
        deleteHeight = deleteView.measuredHeight
        deleteWidth = deleteView.measuredWidth
        if (currentState == SwipeState.Open) {
            contentView.layout(contentView.left, contentView.top, contentWidth, contentHeight)
            deleteView.layout(deleteView.left, deleteView.top, deleteView.left + deleteWidth, deleteHeight)
            mViewDragHelper?.smoothSlideViewTo(contentView!!, -deleteWidth, contentView.top)
            ViewCompat.postInvalidateOnAnimation(this@SwipeLayout) //刷新
        } else {
            contentView.layout(0, 0, contentWidth, contentHeight)
            deleteView.layout(contentView.right, 0, contentView.right + deleteWidth, deleteHeight)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean { // 让ViewDragHelper帮我们判断是否应该拦截
        var result = mViewDragHelper?.shouldInterceptTouchEvent(ev)
        //如果当前有打开的，则需要直接拦截，交给onTouch处理
        if (!SwipeLayoutManager.instance.isShouldSwipe(this)) { //先关闭已经打开的layout
            SwipeLayoutManager.instance.closeCurrentLayout()
            SwipeLayoutManager.instance.clearCurrentLayout()
            result = true
        }
        return result
    }

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mActivePointerId = 0
    override fun onTouchEvent(event: MotionEvent): Boolean { //如果当前有打开的，就不能再打开新的，要先关闭已经打开的,才能打开新的，则下面的逻辑不能执行
        if (!SwipeLayoutManager.instance.isShouldSwipe(this)) {
            requestDisallowInterceptTouchEvent(true) //listview不能滑动
            SwipeLayoutManager.instance.closeCurrentLayout()
            SwipeLayoutManager.instance.clearCurrentLayout()
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                mActivePointerId = event.getPointerId(0)
                mLastMotionX = event.x
                mLastMotionY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = event.x
                val moveY = event.y
                //获取x和y方向移动的距离
                val dx = moveX - downX
                val dy = moveY - downY
                /**
                 * 问题1： 水平滚动的时候，让listview不能上下滑动
                 */
                if (Math.abs(dx) > Math.abs(dy)) { //表示移动是偏向于水平方向，那么应该SwipeLayout应该处理，请求listview不要拦截
//就是SwipeLayout能滑动，listview不能滑动
                    requestDisallowInterceptTouchEvent(true) //请求父类不要拦截
                }
                //更新downX，downY
                downX = moveX
                downY = moveY
            }
            MotionEvent.ACTION_UP -> {
                val clickTime = event.eventTime - event.downTime
                val activePointerIndex = event.findPointerIndex(mActivePointerId)
                val x = event.getX(activePointerIndex)
                val deltaX = mLastMotionX - x
                val y = event.getY(activePointerIndex)
                val deltaY = mLastMotionY - y
                val moveDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY.toDouble())
                if (clickTime <= 200 && moveDistance < 50 * density) {
                    onItemClick(event)
                }
            }
        }
        mViewDragHelper?.processTouchEvent(event)
        return true
    }

    private fun onItemClick(ev: MotionEvent) {
        if (listener != null) {
            listener?.onClick(ev)
        }
    }

    /**
     * 打开的方法
     */
    fun open() {
        mViewDragHelper?.smoothSlideViewTo(contentView!!, -deleteWidth, contentView.top)
        ViewCompat.postInvalidateOnAnimation(this@SwipeLayout) //刷新
    }

    /**
     * 关闭的方法
     */
    fun close() {
        currentState = SwipeState.Close
        mViewDragHelper?.smoothSlideViewTo(contentView!!, 0, contentView.top)
        ViewCompat.postInvalidateOnAnimation(this@SwipeLayout)
    }

    override fun computeScroll() {
        super.computeScroll()
        //如果动画还没结束
        if (mViewDragHelper?.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private var listener: OnSwipeStateChangeListener? = null
    fun setOnSwipeStateChangeListener(listener: OnSwipeStateChangeListener?) {
        this.listener = listener
    }

    //把打开或关闭的状态暴露给外界
    interface OnSwipeStateChangeListener {
        fun onOpen(tag: Any?)
        fun onClose(tag: Any?)
        fun onClick(event: MotionEvent?)
    }
}