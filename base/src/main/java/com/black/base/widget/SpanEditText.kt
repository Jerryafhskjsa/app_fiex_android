package com.black.base.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.black.base.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatHelper
import skin.support.widget.SkinCompatSupportable
import skin.support.widget.SkinCompatTextHelper

class SpanEditText : EditText, SkinCompatSupportable {
    private var mTextHelper: SkinCompatTextHelper?
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper?
    private var useDelete = false
    private var drawableDel: Drawable? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle) : super(context, attrs, defStyleAttr) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init(context, attrs, defStyleAttr)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init(context, attrs, defStyleAttr)
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

    fun getTextColorResId(): Int {
        return mTextHelper?.textColorResId ?: SkinCompatHelper.INVALID_ID
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes start: Int, @DrawableRes top: Int, @DrawableRes end: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        if (mTextHelper != null) {
            mTextHelper?.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        }
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes left: Int, @DrawableRes top: Int, @DrawableRes right: Int, @DrawableRes bottom: Int) {
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

    //    @Override
//    public void setFilters(InputFilter[] filters) {
//        if (this.filters == null) {
//            this.filters = filters;
//        } else {
//            if (filters == null) {
//                this.filters = filters;
//            } else if (filters.length == 0) {
//
//            } else {
//                InputFilter[] newFilters = new InputFilter[this.filters.length + filters.length];
//                System.arraycopy(this.filters, 0, newFilters, 0, this.filters.length);
//                System.arraycopy(filters, 0, newFilters, this.filters.length, filters.length);
//                this.filters = newFilters;
//            }
//        }
//        super.setFilters(this.filters);
//    }
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.SpanEditText,
                    defStyleAttr,
                    0)
            useDelete = ta.getBoolean(R.styleable.SpanEditText_use_delete, true)
            ta.recycle()
        }
        includeFontPadding = false
        if (useDelete) {
            drawableDel = SkinCompatResources.getDrawable(getContext(), R.drawable.icon_delete)
        }
        //        setFilters(new InputFilter[]{new SpanInputFilter(getContext(), this)});
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s is Spannable) { //                    removeSpans((Spannable) s);
//                    editChange((Spannable) s);
                }
            }

            override fun afterTextChanged(s: Editable) { //                editChange(s);
                if (useDelete) {
                    setDrawable()
                }
            }
        })
        super.setOnFocusChangeListener(mOnFocusChangeListener)
    }

    fun resetRes() {
        if (useDelete) {
            drawableDel = SkinCompatResources.getDrawable(context, R.drawable.icon_delete)
            setDrawable()
        }
    }

    fun setUseDelete(useDelete: Boolean) {
        this.useDelete = useDelete
        postInvalidate()
    }

    private val isShowDeleteStatusChanged: Boolean
        private get() {
            val needShow = length() > 0 && isFocused
            return deleteShowing != needShow
        }

    var deleteShowing = false
    //绘制删除图片
    private fun setDrawable() {
        if (isShowDeleteStatusChanged) {
            if (length() < 1 || !isFocused) {
                deleteShowing = false
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            } else {
                deleteShowing = true
                setCompoundDrawablesWithIntrinsicBounds(null, null, drawableDel, null)
            }
        }
    }

    var realOnFocusChangeListener: OnFocusChangeListener? = null
    var mOnFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
        setDrawable()
        if (realOnFocusChangeListener != null) {
            realOnFocusChangeListener?.onFocusChange(v, hasFocus)
        }
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener) {
        realOnFocusChangeListener = l
        super.setOnFocusChangeListener(mOnFocusChangeListener)
    }

    //当触摸范围在右侧时，触发删除方法，隐藏删除图片
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawableDel != null && event.action == MotionEvent.ACTION_UP) {
            val eventX = event.rawX.toInt()
            val eventY = event.rawY.toInt()
            val x = event.x.toInt()
            val y = event.y.toInt()
            val rect = Rect()
            getGlobalVisibleRect(rect)
            val rect2 = Rect()
            getLocalVisibleRect(rect2)
            rect.left = rect.right - 100
            rect2.left = rect2.right - 100
            if (rect2.contains(x, y) || rect.contains(eventX, eventY)) {
                setText("")
                return true
            }
        }
        return super.onTouchEvent(event)
    } //    private void removeSpans(Spannable sp) {
//        CustomerTypefaceSpan[] customerTypefaceSpans = sp.getSpans(0, sp.length(), CustomerTypefaceSpan.class);
//        for (int i = 0; i < customerTypefaceSpans.length; i++) {
//            sp.removeSpan(customerTypefaceSpans[i]);
//        }
//    }
//    private void editChange(Spannable s) {
//        if (spanTextViewHelper == null) {
//            spanTextViewHelper = getSpanTextViewHelper();
//        }
//        if (spanTextViewHelper == null) {
//            return;
//        }
//        String text = s.toString();
//        String regex = "([\\u4e00-\\u9fa5]+)";
//        Matcher matcher = Pattern.compile(regex).matcher(text);
//        int lastStart = 0;
//        int lastEnd = 0;
//        while (matcher.find()) {
//            int start = matcher.start();
//            int end = matcher.end();
//            if (start > 0 && lastEnd < start) {
//                s.setSpan(spanTextViewHelper.getOtherSpan(), lastEnd, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//            }
//            s.setSpan(spanTextViewHelper.getChineseSpan(), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//            lastStart = start;
//            lastEnd = end;
//        }
//        if (lastEnd < text.length()) {
//            s.setSpan(spanTextViewHelper.getOtherSpan(), lastEnd, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//        }
//    }
//
//    public SpanTextViewHelper getSpanTextViewHelper() {
//        if (spanTextViewHelper == null) {
//            spanTextViewHelper = SpanTextViewHelper.buildHelper(this);
//        }
//        return spanTextViewHelper;
//    }
}
