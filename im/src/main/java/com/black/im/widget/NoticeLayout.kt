package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.interfaces.INoticeLayout

class NoticeLayout : RelativeLayout, INoticeLayout {
    override var content: TextView? = null
        private set
    override var contentExtra: TextView? = null
        private set
    private var mAwaysShow = false

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.chat_notice_layout, this)
        content = findViewById(R.id.notice_content)
        contentExtra = findViewById(R.id.notice_content_extra)
    }

    override fun setOnNoticeClickListener(l: OnClickListener?) {
        setOnClickListener(l)
    }

    override fun setVisibility(visibility: Int) {
        if (mAwaysShow) {
            super.setVisibility(View.VISIBLE)
        } else {
            super.setVisibility(visibility)
        }
    }

    override fun alwaysShow(show: Boolean) {
        mAwaysShow = show
        if (mAwaysShow) {
            super.setVisibility(View.VISIBLE)
        }
    }
}