package com.black.lib.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import com.black.lib.util.ViewUtil.setChildrenChecked
import skin.support.widget.SkinCompatLinearLayout

class CheckedLinearLayout : SkinCompatLinearLayout, Checkable {
    private var mChecked = false

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public CheckedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
    override fun setChecked(checked: Boolean) {
        if (mChecked != checked) {
            mChecked = checked
            refreshDrawableState()
        }
        val childCount = childCount
        for (i in 0 until childCount) {
            setChildrenChecked(getChildAt(i), checked)
        }
    }

    override fun toggle() {
        isChecked = !mChecked
    }

    override fun isChecked(): Boolean {
        return mChecked
    }
}