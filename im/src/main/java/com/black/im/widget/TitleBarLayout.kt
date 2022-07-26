package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.util.ScreenUtil.getPxByDp

class TitleBarLayout : LinearLayout, ITitleBarLayout {
    override var leftGroup: LinearLayout? = null
        private set
    override var rightGroup: LinearLayout? = null
        private set
    override var leftTitle: TextView? = null
        private set
    override var middleTitle: TextView? = null
        private set
    override var rightTitle: TextView? = null
        private set
    private var mLeftIcon: ImageView? = null
    private var mRightIcon: ImageView? = null
    private var mTitleLayout: RelativeLayout? = null

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
        View.inflate(context, R.layout.title_bar_layout, this)
        mTitleLayout = findViewById(R.id.page_title_layout)
        leftGroup = findViewById(R.id.page_title_left_group)
        rightGroup = findViewById(R.id.page_title_right_group)
        leftTitle = findViewById(R.id.page_title_left_text)
        rightTitle = findViewById(R.id.page_title_right_text)
        middleTitle = findViewById(R.id.page_title)
        mLeftIcon = findViewById(R.id.page_title_left_icon)
        mRightIcon = findViewById(R.id.page_title_right_icon)
        val params = mTitleLayout?.layoutParams as LayoutParams
        params.height = getPxByDp(45)
        mTitleLayout?.layoutParams = params
        setBackgroundColor(resources.getColor(R.color.main_bg_color))
    }

    override fun setOnLeftClickListener(listener: OnClickListener?) {
        leftGroup?.setOnClickListener(listener)
    }

    override fun setOnRightClickListener(listener: OnClickListener?) {
        rightGroup?.setOnClickListener(listener)
    }

    override fun setTitle(title: String?, position: ITitleBarLayout.POSITION?) {
        when (position) {
            ITitleBarLayout.POSITION.LEFT -> leftTitle?.text = title
            ITitleBarLayout.POSITION.RIGHT -> rightTitle?.text = title
            ITitleBarLayout.POSITION.MIDDLE -> middleTitle?.text = title
        }
    }

    override fun getLeftIcon(): ImageView? {
        return mLeftIcon
    }

    override fun setLeftIcon(resId: Int) {
        mLeftIcon?.setImageResource(resId)
    }

    override fun getRightIcon(): ImageView? {
        return mRightIcon
    }

    override fun setRightIcon(resId: Int) {
        mRightIcon?.setImageResource(resId)
    }

}