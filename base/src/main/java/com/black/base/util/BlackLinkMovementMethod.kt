package com.black.base.util

import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView

class BlackLinkMovementMethod(private val listener: LinkClickListener?) : LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (links.size != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (links[0] is URLSpan) {
                        val url = links[0] as URLSpan
                        if (listener != null && listener.onLinkClick(widget.context, url.url)) {
                            return true
                        } else {
                            links[0].onClick(widget)
                        }
                    }
                } else if (action == MotionEvent.ACTION_DOWN) { //                    Selection.setSelection(buffer,
//                            buffer.getSpanStart(links[0]),
//                            buffer.getSpanEnd(links[0]));
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }

    interface LinkClickListener {
        /**
         * true  表示要自己处理  false 使用系统默认
         *
         * @param mURL
         */
        fun onLinkClick(context: Context?, mURL: String?): Boolean
    }

}