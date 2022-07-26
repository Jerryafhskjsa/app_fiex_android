package com.black.im.manager

import com.black.im.model.chat.ChatInfo

class C2CChatManagerKit private constructor() : ChatManagerKit() {
    companion object {
        private val TAG = C2CChatManagerKit::class.java.simpleName
        private var mKit: C2CChatManagerKit? = null
        val instance: C2CChatManagerKit?
            get() {
                if (mKit == null) {
                    mKit = C2CChatManagerKit()
                }
                return mKit
            }
    }

    private var mCurrentChatInfo: ChatInfo? = null

    init {
        super.init()
    }

    override fun destroyChat() {
        super.destroyChat()
        mCurrentChatInfo = null
        mIsMore = true
    }

    override fun getCurrentChatInfo(): ChatInfo? {
        return mCurrentChatInfo
    }

    override fun setCurrentChatInfo(info: ChatInfo?) {
        super.setCurrentChatInfo(info)
        mCurrentChatInfo = info
    }

    override fun isGroup(): Boolean {
        return false
    }
}