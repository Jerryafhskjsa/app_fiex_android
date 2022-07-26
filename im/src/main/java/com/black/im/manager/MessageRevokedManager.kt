package com.black.im.manager

import com.tencent.imsdk.ext.message.TIMMessageLocator
import com.tencent.imsdk.ext.message.TIMMessageRevokedListener
import java.util.*

class MessageRevokedManager private constructor() : TIMMessageRevokedListener {
    private val mHandlers: MutableList<MessageRevokeHandler> = ArrayList()
    override fun onMessageRevoked(locator: TIMMessageLocator) {
        for (i in mHandlers.indices) {
            mHandlers[i].handleInvoke(locator)
        }
    }

    fun addHandler(handler: MessageRevokeHandler) {
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler)
        }
    }

    fun removeHandler(handler: MessageRevokeHandler?) {
        mHandlers.remove(handler)
    }

    interface MessageRevokeHandler {
        fun handleInvoke(locator: TIMMessageLocator)
    }

    companion object {
        val instance = MessageRevokedManager()
    }
}