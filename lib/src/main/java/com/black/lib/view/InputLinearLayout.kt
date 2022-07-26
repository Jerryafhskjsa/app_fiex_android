package com.black.lib.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import skin.support.widget.SkinCompatLinearLayout
import java.util.*

class InputLinearLayout : SkinCompatLinearLayout {
    companion object {
        private fun getEditText(viewGroup: ViewGroup): List<EditText> {
            val list: MutableList<EditText> = ArrayList()
            val childrenCount = viewGroup.childCount
            for (i in 0 until childrenCount) {
                val child = viewGroup.getChildAt(i)
                if (child is EditText) {
                    list.add(child)
                } else if (child is ViewGroup) {
                    val childList = getEditText(child)
                    if (childList != null) {
                        list.addAll(childList)
                    }
                }
            }
            return list
        }
    }

    private var editTextList: List<EditText>? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public InputLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
//    }
    private fun init() {
        editTextList = getEditText(this)
        if (editTextList != null && !editTextList!!.isEmpty()) {
            for (editText in editTextList!!) {
                editText.onFocusChangeListener = MyOnFocusChangeListener(editText.onFocusChangeListener)
            }
        }
        controlFocusChange(false)
    }

    override fun addView(child: View) {
        super.addView(child)
        init()
    }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        init()
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        super.addView(child, params)
        init()
    }

    override fun addView(child: View, width: Int, height: Int) {
        super.addView(child, width, height)
        init()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        init()
    }

    private fun controlFocusChange(b: Boolean) {
        isSelected = if (b) {
            true
        } else {
            hasEditTextFocus()
        }
    }

    private fun hasEditTextFocus(): Boolean {
        if (editTextList != null && !editTextList!!.isEmpty()) {
            for (editText in editTextList!!) {
                if (editText.isFocused) {
                    return true
                }
            }
        }
        return false
    }

    internal inner class MyOnFocusChangeListener(private val editListener: OnFocusChangeListener?) : OnFocusChangeListener {
        override fun onFocusChange(view: View, b: Boolean) {
            controlFocusChange(b)
            editListener?.onFocusChange(view, b)
        }

    }
}