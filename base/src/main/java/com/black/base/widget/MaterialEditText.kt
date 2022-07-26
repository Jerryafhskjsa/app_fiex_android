package com.black.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.InputType
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.black.base.R
import com.black.base.widget.Density.dp2px
import com.black.lib.typeface.TypefaceTextPaintHelper
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatHelper
import skin.support.widget.SkinCompatSupportable
import skin.support.widget.SkinCompatTextHelper
import java.util.*

open class MaterialEditText : EditText, SkinCompatSupportable {
    private var mTextHelper: SkinCompatTextHelper?
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper?
    private var innerPaddingTop = 0
    private var innerPaddingBottom = 0
    private var innerPaddingLeft = 0
    private var innerPaddingRight = 0
    private var extraPaddingTop = 0
    private var extraPaddingBottom = 0
    private var extraPaddingLeft = 0
    private var extraPaddingRight = 0
    var labTextColor: ColorStateList? = null
    var labCurrentColor = 0
    var labTextColorRes = -1
    var labTextSize = 0
    var labTextStyle = -1
    var labText: CharSequence? = null
    var deleteIconRes = -1
    var deleteIcon: Drawable? = null
    var deleteIconWidth = 0
    var deleteIconHeight = 0
    var useDelete = false
    var eyeIconRes = -1
    var eyeIcon: Drawable? = null
    var eyeIconWidth = 0
    var eyeIconHeight = 0
    var useEye = false
    private var iconPadding = 0
    var assistButtonTextColor: ColorStateList? = null
    var assistButtonCurrentColor = 0
    var assistButtonTextColorRes = -1
    var assistButtonTextSize = 0
    var assistButtonTextStyle = -1
    private var assistButtonText: CharSequence? = null
    var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    var assistButton: AssistButton? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.MaterialEditTextStyle) : super(context, attrs, defStyleAttr) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init(context, attrs, defStyleAttr, 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper?.onSetBackgroundResource(resId)
        }
    }

    override fun setTextAppearance(resId: Int) {
        setTextAppearance(context, resId)
    }

    override fun setTextAppearance(context: Context, resId: Int) {
        super.setTextAppearance(context, resId)
        if (mTextHelper != null) {
            mTextHelper?.onSetTextAppearance(context, resId)
        }
    }

    open fun getTextColorResId(): Int {
        return mTextHelper?.textColorResId ?: SkinCompatHelper.INVALID_ID
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(@DrawableRes start: Int, @DrawableRes top: Int, @DrawableRes end: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        if (mTextHelper != null) {
            mTextHelper?.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        }
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(@DrawableRes left: Int, @DrawableRes top: Int, @DrawableRes right: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        if (mTextHelper != null) {
            mTextHelper?.onSetCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        }
    }

    override fun applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper?.applySkin()
        }
        if (mTextHelper != null) {
            mTextHelper?.applySkin()
        }
    }

    @SuppressLint("ResourceType")
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val dm = context.resources.displayMetrics
        iconPadding = (5 * dm.density).toInt()
        val paddings = intArrayOf(
                android.R.attr.padding,  // 0
                android.R.attr.paddingLeft,  // 1
                android.R.attr.paddingTop,  // 2
                android.R.attr.paddingRight,  // 3
                android.R.attr.paddingBottom // 4
        )
        val theme = context.theme
        var a = theme.obtainStyledAttributes(attrs, R.styleable.MaterialEditText, defStyleAttr, defStyleRes)
        var appearance: TypedArray? = null
        val ap = a.getResourceId(R.styleable.MaterialEditText_MaterialEditTextStyle, -1)
        a.recycle()
        if (ap != -1) {
            appearance = theme.obtainStyledAttributes(ap, R.styleable.MaterialEditText)
        }
        if (appearance != null) {
            val n = appearance.indexCount
            for (i in 0 until n) {
                val attr = appearance.getIndex(i)
                if (attr == R.styleable.MaterialEditText_labTextColor) {
                    labTextColorRes = appearance.getResourceId(attr, -1)
                } else if (attr == R.styleable.MaterialEditText_labTextSize) {
                    labTextSize = appearance.getDimensionPixelSize(attr, labTextSize)
                } else if (attr == R.styleable.MaterialEditText_labTextStyle) {
                    labTextStyle = appearance.getInt(attr, -1)
                } else if (attr == R.styleable.MaterialEditText_labText) {
                    labText = appearance.getText(attr)
                } else if (attr == R.styleable.MaterialEditText_deleteIcon) {
                    deleteIconRes = appearance.getResourceId(attr, deleteIconRes)
                } else if (attr == R.styleable.MaterialEditText_useDelete) {
                    useDelete = appearance.getBoolean(attr, false)
                } else if (attr == R.styleable.MaterialEditText_eyeIcon) {
                    eyeIconRes = appearance.getResourceId(attr, eyeIconRes)
                } else if (attr == R.styleable.MaterialEditText_useEye) {
                    useEye = appearance.getBoolean(attr, false)
                } else if (attr == R.styleable.MaterialEditText_iconPadding) {
                    iconPadding = appearance.getDimensionPixelSize(attr, iconPadding)
                } else if (attr == R.styleable.MaterialEditText_assistButtonTextColor) {
                    assistButtonTextColorRes = appearance.getResourceId(attr, -1)
                } else if (attr == R.styleable.MaterialEditText_assistButtonTextSize) {
                    assistButtonTextSize = appearance.getDimensionPixelSize(attr, assistButtonTextSize)
                } else if (attr == R.styleable.MaterialEditText_assistButtonTextStyle) {
                    assistButtonTextStyle = appearance.getInt(attr, -1)
                } else if (attr == R.styleable.MaterialEditText_assistButtonText) {
                    assistButtonText = appearance.getText(attr)
                }
            }
            appearance.recycle()
        }
        if (ap != -1) {
            val paddingsTypedArray = theme.obtainStyledAttributes(ap, paddings)
            val padding = paddingsTypedArray.getDimensionPixelSize(0, 0)
            innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding)
            innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding)
            innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding)
            innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding)
            paddingsTypedArray.recycle()
        }
        a = theme.obtainStyledAttributes(attrs, R.styleable.MaterialEditText, defStyleAttr, defStyleRes)
        val n = a.indexCount
        for (i in 0 until n) {
            when (val attr = a.getIndex(i)) {
                R.styleable.MaterialEditText_labTextColor -> {
                    labTextColorRes = a.getResourceId(attr, -1)
                }
                R.styleable.MaterialEditText_labTextSize -> {
                    labTextSize = a.getDimensionPixelSize(attr, labTextSize)
                }
                R.styleable.MaterialEditText_labTextStyle -> {
                    labTextStyle = a.getInt(attr, -1)
                }
                R.styleable.MaterialEditText_labText -> {
                    labText = a.getText(attr)
                }
                R.styleable.MaterialEditText_deleteIcon -> {
                    deleteIconRes = a.getResourceId(attr, deleteIconRes)
                }
                R.styleable.MaterialEditText_useDelete -> {
                    useDelete = a.getBoolean(attr, false)
                }
                R.styleable.MaterialEditText_eyeIcon -> {
                    eyeIconRes = a.getResourceId(attr, eyeIconRes)
                }
                R.styleable.MaterialEditText_useEye -> {
                    useEye = a.getBoolean(attr, false)
                }
                R.styleable.MaterialEditText_iconPadding -> {
                    iconPadding = a.getDimensionPixelSize(attr, iconPadding)
                }
                R.styleable.MaterialEditText_assistButtonTextColor -> {
                    assistButtonTextColorRes = a.getResourceId(attr, -1)
                }
                R.styleable.MaterialEditText_assistButtonTextSize -> {
                    assistButtonTextSize = a.getDimensionPixelSize(attr, assistButtonTextSize)
                }
                R.styleable.MaterialEditText_assistButtonTextStyle -> {
                    assistButtonTextStyle = a.getInt(attr, -1)
                }
                R.styleable.MaterialEditText_assistButtonText -> {
                    assistButtonText = a.getText(attr)
                }
            }
        }
        a.recycle()
        val paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings)
        val padding = paddingsTypedArray.getDimensionPixelSize(0, -1)
        innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, if (padding == -1) innerPaddingLeft else padding)
        innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, if (padding == -1) innerPaddingTop else padding)
        innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, if (padding == -1) innerPaddingRight else padding)
        innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, if (padding == -1) innerPaddingBottom else padding)
        paddingsTypedArray.recycle()
        if (labTextColorRes != -1) {
            labTextColor = SkinCompatResources.getColorStateList(context, labTextColorRes)
        }
        labCurrentColor = if (labTextColor != null) {
            labTextColor?.getColorForState(drawableState, 0) ?: Color.BLACK
        } else {
            Color.BLACK
        }
        labTextStyle = if (labTextStyle == 1) {
            Typeface.BOLD
        } else if (labTextStyle == 2) {
            Typeface.ITALIC
        } else {
            Typeface.NORMAL
        }
        if (deleteIconRes != -1) {
            deleteIcon = SkinCompatResources.getDrawable(context, deleteIconRes)
        }
        if (eyeIconRes != -1) {
            eyeIcon = SkinCompatResources.getDrawable(context, eyeIconRes)
        }
        //密码输入框，才使用眼睛
        useEye = useEye && isPasswordBox
        if (assistButtonTextColorRes != -1) {
            assistButtonTextColor = SkinCompatResources.getColorStateList(context, assistButtonTextColorRes)
        }
        assistButtonCurrentColor = if (assistButtonTextColor != null) {
            assistButtonTextColor?.getColorForState(drawableState, 0) ?: Color.BLACK
        } else {
            Color.BLACK
        }
        setAssistButtonText(assistButtonText)
        includeFontPadding = false
        initPadding()
    }

    private val isPasswordBox: Boolean
        get() {
            val inputType = inputType
            return InputType.TYPE_TEXT_VARIATION_PASSWORD == inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD || InputType.TYPE_NUMBER_VARIATION_PASSWORD == inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD || InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD || InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD == inputType and InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }

    private fun initPadding() {
        extraPaddingTop = labHeight
        extraPaddingBottom = 0
        extraPaddingLeft = 0
        if (useDelete && deleteIcon != null) {
            val width = deleteIcon?.intrinsicWidth ?: 0
            val height = deleteIcon?.intrinsicHeight ?: 0
            deleteIcon?.setBounds(0, 0, width, height)
            deleteIconWidth = iconPadding * 2 + width
            deleteIconHeight = iconPadding * 2 + height
        } else {
            deleteIconWidth = 0
            deleteIconHeight = 0
        }
        if (useEye && eyeIcon != null) {
            val width = eyeIcon?.intrinsicWidth ?: 0
            val height = eyeIcon?.intrinsicHeight ?: 0
            eyeIcon?.setBounds(0, 0, width, height)
            eyeIconWidth = iconPadding * 2 + width
            eyeIconHeight = iconPadding * 2 + height
        } else {
            eyeIconWidth = 0
            eyeIconHeight = 0
        }
        extraPaddingRight = deleteIconWidth + eyeIconWidth + assistButtonWidth
        correctPaddings()
    }

    fun setPaddings(left: Int, top: Int, right: Int, bottom: Int) {
        innerPaddingTop = top
        innerPaddingBottom = bottom
        innerPaddingLeft = left
        innerPaddingRight = right
        correctPaddings()
    }

    /**
     * Set paddings to the correct values
     */
    private fun correctPaddings() {
        val paddingRight = innerPaddingRight + extraPaddingRight
        super.setPadding(innerPaddingLeft + extraPaddingLeft, innerPaddingTop + extraPaddingTop, paddingRight, innerPaddingBottom + extraPaddingBottom)
    }

    private val labHeight: Int
        private get() = if (TextUtils.isEmpty(labText)) {
            0
        } else {
            textPaint.textSize = labTextSize.toFloat()
            (Math.abs(textPaint.ascent()) - textPaint.descent()).toInt()
        }

    private val assistButtonLength: Int
        private get() {
            return if (TextUtils.isEmpty(assistButtonText)) {
                0
            } else {
                textPaint.textSize = assistButtonTextSize.toFloat()
                val helper = TypefaceTextPaintHelper(context, textPaint, assistButtonTextStyle, assistButtonText.toString())
                helper.calculateSize()
                helper.length.toInt()
            }
        }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = drawableState
        if (labTextColor != null) {
            updateTextColors()
        }
        if (deleteIcon != null) {
            deleteIcon?.state = state
        }
        //        if (eyeIcon != null) {
//            eyeIcon.setState(state);
//        }
    }

    private fun updateTextColors() {
        var inval = false
        val color = labTextColor?.getColorForState(drawableState, 0) ?: Color.BLACK
        if (color != labCurrentColor) {
            labCurrentColor = color
            inval = true
        }
        if (inval) { // Text needs to be redrawn with the new color
            invalidate()
        }
    }

    fun resetRes() {
        if (labTextColorRes != -1) {
            labTextColor = SkinCompatResources.getColorStateList(context, labTextColorRes)
        }
        labCurrentColor = if (labTextColor != null) {
            labTextColor?.getColorForState(drawableState, 0) ?: Color.BLACK
        } else {
            Color.BLACK
        }
        if (deleteIconRes != -1) {
            deleteIcon = SkinCompatResources.getDrawable(context, deleteIconRes)
        }
        if (eyeIconRes != -1) {
            eyeIcon = SkinCompatResources.getDrawable(context, eyeIconRes)
        }
        if (assistButtonTextColorRes != -1) {
            assistButtonTextColor = SkinCompatResources.getColorStateList(context, assistButtonTextColorRes)
        }
        assistButtonCurrentColor = if (assistButtonTextColor != null) {
            assistButtonTextColor?.getColorForState(drawableState, 0) ?: Color.BLACK
        } else {
            Color.BLACK
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val startX = scrollX + innerPaddingLeft
        val endX = scrollX + width - innerPaddingRight
        if (!TextUtils.isEmpty(labText)) {
            textPaint.textSize = labTextSize.toFloat()
            textPaint.color = labCurrentColor
            TypefaceTextPaintHelper(context, textPaint, labTextStyle, labText.toString())
                    .draw(canvas, startX.toFloat(), (innerPaddingTop + scrollY).toFloat(), Gravity.LEFT or Gravity.BOTTOM)
        }
        val assistButtonRect = assistButtonRect
        if (assistButtonRect != null) {
            textPaint.color = assistButton?.getColor() ?: Color.BLACK
            val helper = if (assistButton == null) null else assistButton?.paintHelper
            helper?.draw(canvas, assistButtonRect.left.toFloat(), assistButtonRect.top.toFloat(), Gravity.LEFT or Gravity.TOP)
        }
        if (hasFocus() && useEye && eyeIcon != null) {
            val rect = eyeIconRect
            if (rect != null) {
                canvas.save()
                canvas.translate(rect.left + iconPadding.toFloat(), rect.top + iconPadding.toFloat())
                eyeIcon?.draw(canvas)
                canvas.restore()
            }
        }
        if (hasFocus() && useDelete && deleteIcon != null && !TextUtils.isEmpty(text)) {
            deleteIcon?.state = drawableState
            val rect = deleteIconRect
            if (rect != null) {
                canvas.save()
                canvas.translate(rect.left + iconPadding.toFloat(), rect.top + iconPadding.toFloat())
                deleteIcon?.draw(canvas)
                canvas.restore()
            }
        }
        super.onDraw(canvas)
    }

    var assistButtonClicking = false
    var deleteClicking = false
    var eyeClicking = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (maxLines == 1 && scrollX > 0 && event.action == MotionEvent.ACTION_DOWN && event.x < getPixel(4 * 5) && event.y > height - extraPaddingBottom - innerPaddingBottom && event.y < height - innerPaddingBottom) {
            setSelection(0)
            return false
        }
        if (hasFocus() && (useDelete && deleteIcon != null || useEye && eyeIcon != null)) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (insideAssistButton(event)) {
                        assistButtonClicking = true
                        return true
                    }
                    if (insideDeleteButton(event)) {
                        deleteClicking = true
                        return true
                    }
                    if (insideEyeButton(event)) {
                        eyeClicking = true
                        return true
                    }
                    if (assistButtonClicking && !insideAssistButton(event)) {
                        assistButtonClicking = false
                    }
                    if (deleteClicking && !insideDeleteButton(event)) {
                        deleteClicking = false
                    }
                    if (eyeClicking && !insideEyeButton(event)) {
                        eyeClicking = false
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (assistButtonClicking && !insideAssistButton(event)) {
                        assistButtonClicking = false
                    }
                    if (deleteClicking && !insideDeleteButton(event)) {
                        deleteClicking = false
                    }
                    if (eyeClicking && !insideEyeButton(event)) {
                        eyeClicking = false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (assistButtonClicking) {
                        if (assistButton != null && assistButton?.callback != null) {
                            assistButton?.callback?.onAssistButtonClick(this@MaterialEditText)
                        }
                        assistButtonClicking = false
                    }
                    if (deleteClicking) {
                        if (!TextUtils.isEmpty(text)) {
                            text = null
                        }
                        deleteClicking = false
                    }
                    if (eyeClicking) {
                        toggleEyeControl()
                        eyeClicking = false
                    }
                    deleteClicking = false
                    eyeClicking = false
                }
                MotionEvent.ACTION_CANCEL -> {
                    assistButtonClicking = false
                    deleteClicking = false
                    eyeClicking = false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getPixel(dp: Int): Int {
        return dp2px(context, dp.toFloat())
    }

    private val assistButtonWidth: Int
        private get() = if (assistButton == null) 0 else assistButton?.textLength ?: 0

    private val assistButtonRect: Rect?
        private get() {
            if (assistButton == null) {
                return null
            }
            val startX = scrollX + innerPaddingLeft
            val helper = assistButton?.paintHelper
            val textWidth = helper?.length?.toInt() ?: 0
            val textHeight = helper?.height?.toInt() ?: 0
            val endX = scrollX + width - innerPaddingRight
            val left = (endX - textWidth)
            val top = innerPaddingTop + extraPaddingTop + (height - innerPaddingTop - extraPaddingTop - innerPaddingBottom - extraPaddingBottom) / 2 - textHeight / 2
            return Rect(left, top, left + textWidth, top + textHeight)
        }

    private val deleteIconRect: Rect?
        get() {
            if (!useDelete || deleteIcon == null) {
                return null
            }
            val startX = scrollX + innerPaddingLeft
            val endX = scrollX + width - innerPaddingRight - assistButtonLength
            val left = endX - eyeIconWidth - deleteIconWidth
            val top = innerPaddingTop + extraPaddingTop + (height - innerPaddingTop - extraPaddingTop - innerPaddingBottom - extraPaddingBottom) / 2 - deleteIconHeight / 2
            return Rect(left, top, left + deleteIconWidth, top + deleteIconHeight)
        }

    private val eyeIconRect: Rect?
        private get() {
            if (!useDelete || deleteIcon == null) {
                return null
            }
            val startX = scrollX + innerPaddingLeft
            val endX = scrollX + width - innerPaddingRight - assistButtonLength
            val left = endX - eyeIconWidth
            val top = innerPaddingTop + extraPaddingTop + (height - innerPaddingTop - extraPaddingTop - innerPaddingBottom - extraPaddingBottom) / 2 - eyeIconHeight / 2
            return Rect(left, top, left + eyeIconWidth, top + eyeIconHeight)
        }

    private fun insideAssistButton(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val rectF = RectF(assistButtonRect)
        return rectF.contains(x, y)
    }

    private fun insideDeleteButton(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val rectF = RectF(deleteIconRect)
        return rectF.contains(x, y)
    }

    private fun insideEyeButton(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val rectF = RectF(eyeIconRect)
        return rectF.contains(x, y)
    }

    private fun toggleEyeControl() {
        if (!isPasswordBox) {
            return
        }
        val inputType = inputType
        if (inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) //设置密码可见
        } else {
            setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) //设置密码不可见
        }
        if (useEye && eyeIcon != null && eyeIcon is StateListDrawable) {
            var currentState: IntArray = intArrayOf()
            if (inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            } else {
                currentState = intArrayOf(android.R.attr.state_checked)
            }
            val drawable = eyeIcon as StateListDrawable
            drawable.state = currentState
        }
    }

    fun setAssistButtonText(resId: Int) {
        setAssistButtonText(resources.getString(resId))
    }

    fun setAssistButtonText(assistButtonText: CharSequence?) {
        this.assistButtonText = assistButtonText
        if (assistButton == null) {
            assistButton = AssistButton()
            assistButton?.color = assistButtonTextColor
        }
        assistButton?.setText(assistButtonText)
    }

    fun setAssistButtonCallback(assistButtonCallback: AssistButtonCallback?) {
        if (assistButton != null) {
            assistButton?.callback = assistButtonCallback
        }
    }

    //右侧辅助按钮
    inner class AssistButton {
        var color: ColorStateList? = null
        var callback: AssistButtonCallback? = null
        var currentState = intArrayOf()
        @JvmField
        var text: CharSequence? = null
        var textSize = 0
        var helper: TypefaceTextPaintHelper? = null
        fun getColor(): Int {
            var state = if (callback == null) null else callback?.getAssistButtonState(this@MaterialEditText)
            state = state ?: intArrayOf()
            return if (Arrays.equals(currentState, state) && CommonUtil.equals(color, assistButtonTextColor)) {
                assistButtonCurrentColor
            } else {
                currentState = state
                color = assistButtonTextColor
                var currentColor = assistButtonCurrentColor
                if (color != null) {
                    currentColor = color?.getColorForState(state, currentColor) ?: Color.BLACK
                }
                currentColor.also { assistButtonCurrentColor = it }
            }
        }

        fun setText(text: CharSequence?) {
            assistButtonCurrentColor = getColor()
            initPadding()
            postInvalidate()
        }

        val textLength: Int
            get() {
                return if (TextUtils.isEmpty(assistButtonText)) {
                    0
                } else {
                    val helper = paintHelper
                    helper?.length?.toInt() ?: 0
                }
            }

        val paintHelper: TypefaceTextPaintHelper?
            get() {
                if (TextUtils.equals(text, assistButtonText) && textSize == assistButtonTextSize && helper != null) {
                } else {
                    text = assistButtonText
                    textSize = assistButtonTextSize
                    if (text != null) {
                        textPaint.textSize = textSize.toFloat()
                        helper = TypefaceTextPaintHelper(context, textPaint, assistButtonTextStyle, text.toString())
                        helper?.calculateSize()
                    }
                }
                return helper
            }
    }

    interface AssistButtonCallback {
        fun onAssistButtonClick(view: View?)
        fun getAssistButtonState(view: View?): IntArray?
    }
}