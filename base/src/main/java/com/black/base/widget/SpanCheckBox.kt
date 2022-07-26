package com.black.base.widget

import android.content.Context
import android.util.AttributeSet
import skin.support.widget.SkinCompatCheckBox

class SpanCheckBox @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SkinCompatCheckBox(context, attrs, defStyleAttr) {
    override fun setTextAppearance(resId: Int) {
        setTextAppearance(context, resId)
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
    //    private SpanTextViewHelper spanTextViewHelper;
    init {
        includeFontPadding = false
    }
}