package com.black.im.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.black.im.R
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.util.TUIKitConstants
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.widget.TitleBarLayout

class SelectionActivity : Activity() {
    companion object {
        private var sOnResultReturnListener: OnResultReturnListener? = null
        fun startTextSelection(context: Context, bundle: Bundle, listener: OnResultReturnListener) {
            bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_TEXT)
            startSelection(context, bundle, listener)
        }

        fun startListSelection(context: Context, bundle: Bundle, listener: OnResultReturnListener) {
            bundle.putInt(TUIKitConstants.Selection.TYPE, TUIKitConstants.Selection.TYPE_LIST)
            startSelection(context, bundle, listener)
        }

        private fun startSelection(context: Context, bundle: Bundle, listener: OnResultReturnListener) {
            val intent = Intent(context, SelectionActivity::class.java)
            intent.putExtra(TUIKitConstants.Selection.CONTENT, bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            sOnResultReturnListener = listener
        }
    }

    private var radioGroup: RadioGroup? = null
    private var input: EditText? = null
    private var mSelectionType = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selection_activity)
        val titleBar: TitleBarLayout = findViewById(R.id.edit_title_bar)
        radioGroup = findViewById(R.id.content_list_rg)
        input = findViewById(R.id.edit_content_et)
        val bundle = intent.getBundleExtra(TUIKitConstants.Selection.CONTENT)
        when (bundle.getInt(TUIKitConstants.Selection.TYPE)) {
            TUIKitConstants.Selection.TYPE_TEXT -> {
                radioGroup?.visibility = View.GONE
                val defaultString = bundle.getString(TUIKitConstants.Selection.INIT_CONTENT)
                val limit = bundle.getInt(TUIKitConstants.Selection.LIMIT)
                if (!TextUtils.isEmpty(defaultString)) {
                    input?.setText(defaultString)
                    input?.setSelection(defaultString!!.length)
                }
                if (limit > 0) {
                    input?.filters = arrayOf<InputFilter>(LengthFilter(limit))
                }
            }
            TUIKitConstants.Selection.TYPE_LIST -> {
                input?.visibility = View.GONE
                val list = bundle.getStringArrayList(TUIKitConstants.Selection.LIST)
                if (list == null || list.size == 0) {
                    return
                }
                var i = 0
                while (i < list.size) {
                    val radioButton = RadioButton(this)
                    radioButton.text = list[i]
                    radioButton.id = i
                    radioGroup?.addView(radioButton, i, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                    i++
                }
                val checked = bundle.getInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX)
                radioGroup?.check(checked)
                radioGroup?.invalidate()
            }
            else -> {
                finish()
                return
            }
        }
        mSelectionType = bundle.getInt(TUIKitConstants.Selection.TYPE)
        val title = bundle.getString(TUIKitConstants.Selection.TITLE)
        titleBar.setTitle(title, ITitleBarLayout.POSITION.MIDDLE)
        titleBar.setOnLeftClickListener(View.OnClickListener { finish() })
        titleBar.getRightIcon()?.visibility = View.GONE
        titleBar.rightTitle?.text = resources.getString(R.string.sure)
        titleBar.setOnRightClickListener(View.OnClickListener { echoClick(title) })
    }

    private fun echoClick(title: String?) {
        when (mSelectionType) {
            TUIKitConstants.Selection.TYPE_TEXT -> {
                if (TextUtils.isEmpty(input?.text.toString()) && title == resources.getString(R.string.modify_group_name)) {
                    toastLongMessage("没有输入昵称，请重新填写")
                    return
                }
                if (sOnResultReturnListener != null) {
                    sOnResultReturnListener?.onReturn(input?.text.toString())
                }
            }
            TUIKitConstants.Selection.TYPE_LIST -> if (sOnResultReturnListener != null) {
                sOnResultReturnListener?.onReturn(radioGroup?.checkedRadioButtonId)
            }
        }
        finish()
    }

    override fun onStop() {
        super.onStop()
        sOnResultReturnListener = null
    }

    interface OnResultReturnListener {
        fun onReturn(res: Any?)
    }
}