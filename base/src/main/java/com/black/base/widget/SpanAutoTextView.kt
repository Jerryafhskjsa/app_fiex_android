package com.black.base.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import me.grantland.widget.AutofitTextView
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable
import skin.support.widget.SkinCompatTextHelper

class SpanAutoTextView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AutofitTextView(context, attrs, defStyleAttr), SkinCompatSupportable {
    //    private SpanTextViewHelper spanTextViewHelper;
    private val mTextHelper: SkinCompatTextHelper?
    private val mBackgroundTintHelper: SkinCompatBackgroundHelper?

    init {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr)
        mTextHelper = SkinCompatTextHelper.create(this)
        mTextHelper.loadFromAttributes(attrs, defStyleAttr)
        includeFontPadding = false
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        mBackgroundTintHelper?.onSetBackgroundResource(resId)
    }

    override fun setTextAppearance(resId: Int) {
        setTextAppearance(context, resId)
    }

    override fun setTextAppearance(context: Context, resId: Int) {
        super.setTextAppearance(context, resId)
        mTextHelper?.onSetTextAppearance(context, resId)
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes start: Int, @DrawableRes top: Int, @DrawableRes end: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
        mTextHelper?.onSetCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes left: Int, @DrawableRes top: Int, @DrawableRes right: Int, @DrawableRes bottom: Int) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        mTextHelper?.onSetCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
    }

    override fun applySkin() {
        mBackgroundTintHelper?.applySkin()
        mTextHelper?.applySkin()
    }

    override fun setText(text: CharSequence, type: BufferType) {
        //        if (!(text instanceof Spannable)) {
//            text = getSpanTextViewHelper() == null ? text : getSpanTextViewHelper().getSpanString(text);
//        }
        super.setText(text, type)
    }
    //    public SpanTextViewHelper getSpanTextViewHelper() {

    //        if (spanTextViewHelper == null) {
//            spanTextViewHelper = SpanTextViewHelper.buildHelper(this);
//        }
//        return spanTextViewHelper;
//    }

}