package com.black.lib.widget

import android.content.res.ColorStateList
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

class InputController @JvmOverloads constructor(private val editText: EditText, private val layout: ViewGroup? = null, private val labelTextView: TextView? = null, private val assistButton: TextView? = null) {
    constructor(editText: EditText, labelTextView: TextView?, layout: ViewGroup?) : this(editText, layout, labelTextView, null) {}
    constructor(labelTextView: TextView?, editText: EditText) : this(editText, null, labelTextView, null) {}
    constructor(editText: EditText, assistButton: TextView?) : this(editText, null, null, assistButton) {}
    constructor(editText: EditText, labelTextView: TextView?, assistButton: TextView?) : this(editText, null, labelTextView, assistButton) {}

    init {
        editText.onFocusChangeListener = MyOnFocusChangeListener(editText.onFocusChangeListener)
    }

    private fun controlFocusChange(isFocused: Boolean) {
        if (layout != null) {
            layout.isSelected = isFocused
        }
        if (labelTextView != null) {
            labelTextView.isSelected = isFocused
        }
        if (assistButton != null) {
            assistButton.isSelected = isFocused
        }
    }

    fun setLabelTextColor(color: Int) {
        labelTextView?.setTextColor(color)
    }

    fun setLabelTextColor(color: ColorStateList?) {
        labelTextView?.setTextColor(color)
    }

    fun setLabelText(text: CharSequence?) {
        if (labelTextView != null) {
            labelTextView.text = text
        }
    }

    fun setLabelTextSize(textSize: Float) {
        if (labelTextView != null) {
            labelTextView.textSize = textSize
        }
    }

    fun setAssistButtonColor(color: Int) {
        assistButton?.setTextColor(color)
    }

    fun setAssistButtonTextColor(color: ColorStateList?) {
        assistButton?.setTextColor(color)
    }

    fun setAssistButtonText(text: CharSequence?) {
        if (assistButton != null) {
            assistButton.text = text
        }
    }

    fun setAssistButtonTextSize(textSize: Float) {
        if (assistButton != null) {
            assistButton.textSize = textSize
        }
    }

    fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        editText.onFocusChangeListener = MyOnFocusChangeListener(listener!!)
    }

    internal inner class MyOnFocusChangeListener constructor(private val editListener: OnFocusChangeListener) : OnFocusChangeListener {
        override fun onFocusChange(view: View, b: Boolean) {
            controlFocusChange(b)
            editListener.onFocusChange(view, b)
        }

    }
}