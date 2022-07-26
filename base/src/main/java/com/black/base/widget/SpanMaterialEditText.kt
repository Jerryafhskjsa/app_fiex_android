package com.black.base.widget

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.annotation.RequiresApi

class SpanMaterialEditText : MaterialEditText {
    //    private SpanTextViewHelper spanTextViewHelper;
    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s is Spannable) { //                    removeSpans((Spannable) s);
//                    editChange((Spannable) s);
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }
//    private void removeSpans(Spannable sp) {
//        CustomerTypefaceSpan[] customerTypefaceSpans = sp.getSpans(0, sp.length(), CustomerTypefaceSpan.class);
//        for (int i = 0; i < customerTypefaceSpans.length; i++) {
//            sp.removeSpan(customerTypefaceSpans[i]);
//        }
//    }
//
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