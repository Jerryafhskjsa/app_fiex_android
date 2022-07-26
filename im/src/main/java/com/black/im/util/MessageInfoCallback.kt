package com.black.im.util

import com.black.im.model.chat.MessageInfo

interface MessageInfoCallback {
    fun onError(code: Int, desc: String?)
    fun onSuccess(fullData: ArrayList<MessageInfo?>, showingData: ArrayList<MessageInfo?>)
}