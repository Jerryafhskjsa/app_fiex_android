package com.black.base.widget

import android.R
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable
import skin.support.widget.SkinCompatTextHelper

class SpanTextView : TextView, SkinCompatSupportable {
    private var mTextHelper: SkinCompatTextHelper?
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper?
    //    private SpanTextViewHelper spanTextViewHelper;
    private var lastClickTime: Long = 0
    private val MIN_DELAY_TIME = 1000

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.textViewStyle) : super(context, attrs, defStyleAttr) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper?.loadFromAttributes(attrs, defStyleAttr)
        init()
    }

    override fun performClick(): Boolean {
        val currentClickTime = System.currentTimeMillis()
        if (currentClickTime - lastClickTime <= MIN_DELAY_TIME) {
            //防止1秒内多次点击
            return false
        }
        lastClickTime = currentClickTime
        return super.performClick()
    }

    override fun setIncludeFontPadding(includepad: Boolean) {
        super.setIncludeFontPadding(false)
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        //        if (!(text instanceof Spannable)) {
//            text = getSpanTextViewHelper() == null ? text : getSpanTextViewHelper().getSpanString(text);
//        }
        super.setText(text, type)
    }

    private fun init() { //        setIncludeFontPadding(false);
    }

    //    public SpanTextViewHelper getSpanTextViewHelper() {
//        if (spanTextViewHelper == null) {
//            spanTextViewHelper = SpanTextViewHelper.buildHelper(this);
//        }
//        return spanTextViewHelper;
//    }
    fun setText(text: String?) {
        super.setText(text)
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
}