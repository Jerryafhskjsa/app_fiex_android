package com.black.im.model

import android.R
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

/**
 * This utility class can show a edit menu easily
 */
class PopupList(private val mContext: Context?) {
    companion object {
        const val DEFAULT_NORMAL_TEXT_COLOR = Color.WHITE
        const val DEFAULT_PRESSED_TEXT_COLOR = Color.WHITE
        const val DEFAULT_TEXT_SIZE_DP = 14f
        const val DEFAULT_TEXT_PADDING_LEFT_DP = 10.0f
        const val DEFAULT_TEXT_PADDING_TOP_DP = 5.0f
        const val DEFAULT_TEXT_PADDING_RIGHT_DP = 10.0f
        const val DEFAULT_TEXT_PADDING_BOTTOM_DP = 5.0f
        const val DEFAULT_NORMAL_BACKGROUND_COLOR = -0x34000000
        const val DEFAULT_PRESSED_BACKGROUND_COLOR = -0x18888889
        const val DEFAULT_BACKGROUND_RADIUS_DP = 8
        const val DEFAULT_DIVIDER_COLOR = -0x65000001
        const val DEFAULT_DIVIDER_WIDTH_DP = 0.5f
        const val DEFAULT_DIVIDER_HEIGHT_DP = 40.0f
    }

    private var mPopupWindow: PopupWindow? = null
    private var mAnchorView: View? = null
    private val mAdapterView: View? = null
    private var mContextView: View? = null
    var indicatorView: View?
    private var mPopupItemList: List<String>? = null
    private var mPopupListListener: PopupListListener? = null
    private var mContextPosition = 0
    private var mOffsetX = 0f
    private var mOffsetY = 0f
    private var mLeftItemBackground: StateListDrawable? = null
    private var mRightItemBackground: StateListDrawable? = null
    private var mCornerItemBackground: StateListDrawable? = null
    private var mTextColorStateList: ColorStateList? = null
    private var mCornerBackground: GradientDrawable? = null
    private var mIndicatorWidth = 0
    private var mIndicatorHeight = 0
    private var mPopupWindowWidth = 0
    private var mPopupWindowHeight = 0
    private var mNormalTextColor: Int
    private var mPressedTextColor: Int
    var textSize: Float
    var textPaddingLeft: Int
    var textPaddingTop: Int
    var textPaddingRight: Int
    var textPaddingBottom: Int
    private var mNormalBackgroundColor: Int
    private var mPressedBackgroundColor: Int
    private var mBackgroundCornerRadius: Int
    var dividerColor: Int
    var dividerWidth: Int
    var dividerHeight: Int

    init {
        mNormalTextColor = DEFAULT_NORMAL_TEXT_COLOR
        mPressedTextColor = DEFAULT_PRESSED_TEXT_COLOR
        textSize = dp2px(DEFAULT_TEXT_SIZE_DP).toFloat()
        textPaddingLeft = dp2px(DEFAULT_TEXT_PADDING_LEFT_DP)
        textPaddingTop = dp2px(DEFAULT_TEXT_PADDING_TOP_DP)
        textPaddingRight = dp2px(DEFAULT_TEXT_PADDING_RIGHT_DP)
        textPaddingBottom = dp2px(DEFAULT_TEXT_PADDING_BOTTOM_DP)
        mNormalBackgroundColor = DEFAULT_NORMAL_BACKGROUND_COLOR
        mPressedBackgroundColor = DEFAULT_PRESSED_BACKGROUND_COLOR
        mBackgroundCornerRadius = dp2px(DEFAULT_BACKGROUND_RADIUS_DP.toFloat())
        dividerColor = DEFAULT_DIVIDER_COLOR
        dividerWidth = dp2px(DEFAULT_DIVIDER_WIDTH_DP)
        dividerHeight = dp2px(DEFAULT_DIVIDER_HEIGHT_DP)
        indicatorView = getDefaultIndicatorView(mContext)
        refreshBackgroundOrRadiusStateList()
        refreshTextColorStateList(mPressedTextColor, mNormalTextColor)
    }

    /**
     * Popup a window when anchorView is clicked and held.
     * That method will call [View.setOnTouchListener] and
     * [View.setOnLongClickListener](or
 * [AbsListView.setOnItemLongClickListener]
 * if anchorView is a instance of AbsListView), so you can only use
     * [PopupList.showPopupListWindow]
     * if you called those method before.
     *
     * @param anchorView        the view on which to pin the popup window
     * @param popupItemList     the list of the popup menu
     * @param popupListListener the Listener
     */
    fun show(anchorView: View, popupItemList: List<String>?, popupListListener: PopupListListener?) {
        mAnchorView = anchorView
        mPopupItemList = popupItemList
        mPopupListListener = popupListListener
        mPopupWindow = null
        mOffsetX = anchorView.x + anchorView.width / 2
        mOffsetY = anchorView.y
        if (mPopupListListener != null && !mPopupListListener!!.showPopupList(anchorView, anchorView, 0)) {
            return
        }
        mContextView = anchorView
        mContextPosition = 0
        showPopupListWindow(mOffsetX, mOffsetY)
    }

    /**
     * show a popup window in a bubble style.
     *
     * @param anchorView        the view on which to pin the popup window
     * @param contextPosition   context position
     * @param rawX              the original raw X coordinate
     * @param rawY              the original raw Y coordinate
     * @param popupItemList     the list of the popup menu
     * @param popupListListener the Listener
     */
    fun showPopupListWindow(anchorView: View?, contextPosition: Int, rawX: Float, rawY: Float,
                            popupItemList: List<String>?, popupListListener: PopupListListener?) {
        mAnchorView = anchorView
        mContextPosition = contextPosition
        mPopupItemList = popupItemList
        mPopupListListener = popupListListener
        mPopupWindow = null
        mContextView = anchorView
        if (mPopupListListener != null
                && !mPopupListListener!!.showPopupList(mContextView, mContextView, contextPosition)) {
            return
        }
        val location = IntArray(2)
        mAnchorView!!.getLocationOnScreen(location)
        showPopupListWindow(rawX - location[0], rawY - location[1])
    }

    private fun showPopupListWindow(offsetX: Float, offsetY: Float) {
        if (mContext is Activity && mContext.isFinishing) {
            return
        }
        if (mPopupWindow == null || mPopupListListener is AdapterPopupListListener) {
            val contentView = LinearLayout(mContext)
            contentView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            contentView.orientation = LinearLayout.VERTICAL
            val popupListContainer = LinearLayout(mContext)
            popupListContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            popupListContainer.orientation = LinearLayout.HORIZONTAL
            popupListContainer.setBackgroundDrawable(mCornerBackground)
            contentView.addView(popupListContainer)
            if (indicatorView != null) {
                val layoutParams: LinearLayout.LayoutParams
                layoutParams = if (indicatorView!!.layoutParams == null) {
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                } else {
                    indicatorView!!.layoutParams as LinearLayout.LayoutParams
                }
                layoutParams.gravity = Gravity.CENTER
                indicatorView!!.layoutParams = layoutParams
                val viewParent = indicatorView!!.parent
                if (viewParent is ViewGroup) {
                    viewParent.removeView(indicatorView)
                }
                contentView.addView(indicatorView)
            }
            for (i in mPopupItemList!!.indices) {
                val textView = TextView(mContext)
                val textlayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dividerHeight)
                textlayoutParams.gravity = Gravity.CENTER
                textView.layoutParams = textlayoutParams
                textView.setTextColor(mTextColorStateList)
                textView.gravity = Gravity.CENTER_VERTICAL
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                textView.setPadding(textPaddingLeft, textPaddingTop, textPaddingRight, textPaddingBottom)
                textView.isClickable = true
                textView.setOnClickListener {
                    if (mPopupListListener != null) {
                        mPopupListListener!!.onPopupListClick(mContextView, mContextPosition, i)
                        hidePopupListWindow()
                    }
                }
                if (mPopupListListener is AdapterPopupListListener) {
                    val adapterPopupListListener = mPopupListListener as AdapterPopupListListener
                    textView.text = adapterPopupListListener.formatText(mAdapterView, mContextView, mContextPosition, i, mPopupItemList!![i])
                } else {
                    textView.text = mPopupItemList!![i]
                }
                if (mPopupItemList!!.size > 1 && i == 0) {
                    textView.setBackgroundDrawable(mLeftItemBackground)
                } else if (mPopupItemList!!.size > 1 && i == mPopupItemList!!.size - 1) {
                    textView.setBackgroundDrawable(mRightItemBackground)
                } else if (mPopupItemList!!.size == 1) {
                    textView.setBackgroundDrawable(mCornerItemBackground)
                } else {
                    textView.setBackgroundDrawable(centerItemBackground)
                }
                popupListContainer.addView(textView)
                if (mPopupItemList!!.size > 1 && i != mPopupItemList!!.size - 1) {
                    val divider = View(mContext)
                    val layoutParams = LinearLayout.LayoutParams(dividerWidth, dividerHeight)
                    layoutParams.gravity = Gravity.CENTER
                    divider.layoutParams = layoutParams
                    divider.setBackgroundColor(dividerColor)
                    popupListContainer.addView(divider)
                }
            }
            if (mPopupWindowWidth == 0) {
                mPopupWindowWidth = getViewWidth(popupListContainer)
            }
            if (indicatorView != null && mIndicatorWidth == 0) {
                mIndicatorWidth = if (indicatorView!!.layoutParams.width > 0) {
                    indicatorView!!.layoutParams.width
                } else {
                    getViewWidth(indicatorView!!)
                }
            }
            if (indicatorView != null && mIndicatorHeight == 0) {
                mIndicatorHeight = if (indicatorView!!.layoutParams.height > 0) {
                    indicatorView!!.layoutParams.height
                } else {
                    getViewHeight(indicatorView!!)
                }
            }
            if (mPopupWindowHeight == 0) {
                mPopupWindowHeight = getViewHeight(popupListContainer) + mIndicatorHeight
            }
            mPopupWindow = PopupWindow(contentView, mPopupWindowWidth, mPopupWindowHeight, true)
            mPopupWindow!!.isTouchable = true
            mPopupWindow!!.setBackgroundDrawable(BitmapDrawable())
        }
        val location = IntArray(2)
        mAnchorView!!.getLocationOnScreen(location)
        if (indicatorView != null) {
            val leftTranslationLimit = mIndicatorWidth / 2f + mBackgroundCornerRadius - mPopupWindowWidth / 2f
            val rightTranslationLimit = mPopupWindowWidth / 2f - mIndicatorWidth / 2f - mBackgroundCornerRadius
            val maxWidth = mContext!!.resources.displayMetrics.widthPixels.toFloat()
            if (location[0] + offsetX < mPopupWindowWidth / 2f) {
                indicatorView!!.translationX = Math.max(location[0] + offsetX - mPopupWindowWidth / 2f, leftTranslationLimit)
            } else if (location[0] + offsetX + mPopupWindowWidth / 2f > maxWidth) {
                indicatorView!!.translationX = Math.min(location[0] + offsetX + mPopupWindowWidth / 2f - maxWidth, rightTranslationLimit)
            } else {
                indicatorView!!.translationX = 0f
            }
        }
        if (!mPopupWindow!!.isShowing) {
            val x = (location[0] + offsetX - mPopupWindowWidth / 2f + 0.5f).toInt()
            val y = (location[1] + offsetY - mPopupWindowHeight + 0.5f).toInt()
            mPopupWindow!!.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, x, y)
        }
    }

    private fun refreshBackgroundOrRadiusStateList() { // left
        val leftItemPressedDrawable = GradientDrawable()
        leftItemPressedDrawable.setColor(mPressedBackgroundColor)
        leftItemPressedDrawable.cornerRadii = floatArrayOf(
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(), 0f, 0f, 0f, 0f,
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat())
        val leftItemNormalDrawable = GradientDrawable()
        leftItemNormalDrawable.setColor(Color.TRANSPARENT)
        leftItemNormalDrawable.cornerRadii = floatArrayOf(
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(), 0f, 0f, 0f, 0f,
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat())
        mLeftItemBackground = StateListDrawable()
        mLeftItemBackground!!.addState(intArrayOf(R.attr.state_pressed), leftItemPressedDrawable)
        mLeftItemBackground!!.addState(intArrayOf(), leftItemNormalDrawable)
        // right
        val rightItemPressedDrawable = GradientDrawable()
        rightItemPressedDrawable.setColor(mPressedBackgroundColor)
        rightItemPressedDrawable.cornerRadii = floatArrayOf(0f, 0f,
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(),
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(), 0f, 0f)
        val rightItemNormalDrawable = GradientDrawable()
        rightItemNormalDrawable.setColor(Color.TRANSPARENT)
        rightItemNormalDrawable.cornerRadii = floatArrayOf(0f, 0f,
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(),
                mBackgroundCornerRadius.toFloat(), mBackgroundCornerRadius.toFloat(), 0f, 0f)
        mRightItemBackground = StateListDrawable()
        mRightItemBackground!!.addState(intArrayOf(R.attr.state_pressed), rightItemPressedDrawable)
        mRightItemBackground!!.addState(intArrayOf(), rightItemNormalDrawable)
        // corner
        val cornerItemPressedDrawable = GradientDrawable()
        cornerItemPressedDrawable.setColor(mPressedBackgroundColor)
        cornerItemPressedDrawable.cornerRadius = mBackgroundCornerRadius.toFloat()
        val cornerItemNormalDrawable = GradientDrawable()
        cornerItemNormalDrawable.setColor(Color.TRANSPARENT)
        cornerItemNormalDrawable.cornerRadius = mBackgroundCornerRadius.toFloat()
        mCornerItemBackground = StateListDrawable()
        mCornerItemBackground!!.addState(intArrayOf(R.attr.state_pressed), cornerItemPressedDrawable)
        mCornerItemBackground!!.addState(intArrayOf(), cornerItemNormalDrawable)
        mCornerBackground = GradientDrawable()
        mCornerBackground!!.setColor(mNormalBackgroundColor)
        mCornerBackground!!.cornerRadius = mBackgroundCornerRadius.toFloat()
    }

    private val centerItemBackground: StateListDrawable
        private get() {
            val centerItemBackground = StateListDrawable()
            val centerItemPressedDrawable = GradientDrawable()
            centerItemPressedDrawable.setColor(mPressedBackgroundColor)
            val centerItemNormalDrawable = GradientDrawable()
            centerItemNormalDrawable.setColor(Color.TRANSPARENT)
            centerItemBackground.addState(intArrayOf(R.attr.state_pressed), centerItemPressedDrawable)
            centerItemBackground.addState(intArrayOf(), centerItemNormalDrawable)
            return centerItemBackground
        }

    private fun refreshTextColorStateList(pressedTextColor: Int, normalTextColor: Int) {
        val states = arrayOfNulls<IntArray>(2)
        states[0] = intArrayOf(R.attr.state_pressed)
        states[1] = intArrayOf()
        val colors = intArrayOf(pressedTextColor, normalTextColor)
        mTextColorStateList = ColorStateList(states, colors)
    }

    fun hidePopupListWindow() {
        if (mContext is Activity && mContext.isFinishing) {
            return
        }
        if (mPopupWindow != null && mPopupWindow!!.isShowing) {
            mPopupWindow!!.dismiss()
        }
    }

    fun getDefaultIndicatorView(context: Context?): View {
        return getTriangleIndicatorView(context, dp2px(16f).toFloat(), dp2px(8f).toFloat(), DEFAULT_NORMAL_BACKGROUND_COLOR)
    }

    fun getTriangleIndicatorView(context: Context?, widthPixel: Float, heightPixel: Float,
                                 color: Int): View {
        val indicator = ImageView(context)
        val drawable: Drawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                val path = Path()
                val paint = Paint()
                paint.color = color
                paint.style = Paint.Style.FILL
                path.moveTo(0f, 0f)
                path.lineTo(widthPixel, 0f)
                path.lineTo(widthPixel / 2, heightPixel)
                path.close()
                canvas.drawPath(path, paint)
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: ColorFilter) {}
            override fun getOpacity(): Int {
                return PixelFormat.TRANSLUCENT
            }

            override fun getIntrinsicWidth(): Int {
                return widthPixel.toInt()
            }

            override fun getIntrinsicHeight(): Int {
                return heightPixel.toInt()
            }
        }
        indicator.setImageDrawable(drawable)
        return indicator
    }

    fun setIndicatorSize(widthPixel: Int, heightPixel: Int) {
        mIndicatorWidth = widthPixel
        mIndicatorHeight = heightPixel
        val layoutParams = LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight)
        layoutParams.gravity = Gravity.CENTER
        if (indicatorView != null) {
            indicatorView!!.layoutParams = layoutParams
        }
    }

    var normalTextColor: Int
        get() = mNormalTextColor
        set(normalTextColor) {
            mNormalTextColor = normalTextColor
            refreshTextColorStateList(mPressedTextColor, mNormalTextColor)
        }

    var pressedTextColor: Int
        get() = mPressedTextColor
        set(pressedTextColor) {
            mPressedTextColor = pressedTextColor
            refreshTextColorStateList(mPressedTextColor, mNormalTextColor)
        }

    /**
     * @param left   the left padding in pixels
     * @param top    the top padding in pixels
     * @param right  the right padding in pixels
     * @param bottom the bottom padding in pixels
     */
    fun setTextPadding(left: Int, top: Int, right: Int, bottom: Int) {
        textPaddingLeft = left
        textPaddingTop = top
        textPaddingRight = right
        textPaddingBottom = bottom
    }

    var normalBackgroundColor: Int
        get() = mNormalBackgroundColor
        set(normalBackgroundColor) {
            mNormalBackgroundColor = normalBackgroundColor
            refreshBackgroundOrRadiusStateList()
        }

    var pressedBackgroundColor: Int
        get() = mPressedBackgroundColor
        set(pressedBackgroundColor) {
            mPressedBackgroundColor = pressedBackgroundColor
            refreshBackgroundOrRadiusStateList()
        }

    var backgroundCornerRadius: Int
        get() = mBackgroundCornerRadius
        set(backgroundCornerRadiusPixel) {
            mBackgroundCornerRadius = backgroundCornerRadiusPixel
            refreshBackgroundOrRadiusStateList()
        }

    val resources: Resources
        get() = if (mContext == null) {
            Resources.getSystem()
        } else {
            mContext.resources
        }

    private fun getViewWidth(view: View): Int {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        return view.measuredWidth
    }

    private fun getViewHeight(view: View): Int {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        return view.measuredHeight
    }

    fun dp2px(value: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, resources.displayMetrics).toInt()
    }

    fun sp2px(value: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                value, resources.displayMetrics).toInt()
    }

    interface PopupListListener {
        /**
         * Whether the PopupList should be bound to the special view
         *
         * @param adapterView     The context view(The AbsListView where the click happened or normal view).
         * @param contextView     The view within the AbsListView that was clicked or normal view
         * @param contextPosition The position of the view in the list
         * @return true if the view should show the PopupList, false otherwise
         */
        fun showPopupList(adapterView: View?, contextView: View?, contextPosition: Int): Boolean

        /**
         * The callback to be invoked with an item in this PopupList has
         * been clicked
         *
         * @param contextView     The context view(The AbsListView where the click happened or normal view).
         * @param contextPosition The position of the view in the list
         * @param position        The position of the view in the PopupList
         */
        fun onPopupListClick(contextView: View?, contextPosition: Int, position: Int)
    }

    interface AdapterPopupListListener : PopupListListener {
        fun formatText(adapterView: View?, contextView: View?, contextPosition: Int, position: Int, text: String?): String?
    }


}