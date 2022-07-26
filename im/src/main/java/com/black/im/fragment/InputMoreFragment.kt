package com.black.im.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.im.R
import com.black.im.model.InputMoreActionUnit
import com.black.im.util.IUIKitCallBack
import com.black.im.widget.InputMoreLayout
import java.util.*

class InputMoreFragment : BaseInputFragment() {
    companion object {
        const val REQUEST_CODE_FILE = 1011
        const val REQUEST_CODE_PHOTO = 1012
    }

    private var mBaseView: View? = null
    private var mInputMoreList: List<InputMoreActionUnit> = ArrayList()
    private var mCallback: IUIKitCallBack? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBaseView = inflater.inflate(R.layout.chat_inputmore_fragment, container, false)
        val layout: InputMoreLayout? = mBaseView?.findViewById(R.id.input_extra_area)
        layout?.init(mInputMoreList)
        return mBaseView
    }

    fun setActions(actions: List<InputMoreActionUnit>) {
        mInputMoreList = actions
    }

    fun setCallback(callback: IUIKitCallBack?) {
        mCallback = callback
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_FILE
                || requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode != -1) {
                return
            }
            val uri = data?.data //得到uri，后面就是将uri转化成file的过程。
            if (mCallback != null) {
                mCallback?.onSuccess(uri)
            }
        }
    }
}