package com.black.im.manager

import android.text.TextUtils
import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.provider.ChatProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.MessageInfoCallback
import com.black.im.util.MessageInfoUtil.TIMMessage2MessageInfo
import com.black.im.util.MessageInfoUtil.TIMMessages2MessageInfos
import com.black.im.util.MessageInfoUtil.isTyping
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.util.CommonUtil
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.message.TIMMessageLocator
import com.tencent.imsdk.ext.message.TIMMessageReceipt
import java.util.*
import kotlin.collections.ArrayList

abstract class ChatManagerKit : TIMMessageListener, MessageRevokedManager.MessageRevokeHandler {
    companion object {
        const val MSG_PAGE_COUNT = 20
        protected const val REVOKE_TIME_OUT = 6223
        private val TAG = ChatManagerKit::class.java.simpleName
    }

    var currentProvider: ChatProvider? = null
        protected set
    protected var mCurrentConversation: TIMConversation? = null
    protected var mIsMore = false
    private var mIsLoading = false
    protected fun init() {
        destroyChat()
        TIMManager.getInstance().addMessageListener(this)
        MessageRevokedManager.instance.addHandler(this)
    }

    open fun destroyChat() {
        mCurrentConversation = null
        currentProvider = null
    }

    abstract fun getCurrentChatInfo(): ChatInfo?

    open fun setCurrentChatInfo(info: ChatInfo?) {
        if (info == null) {
            return
        }
        mCurrentConversation = TIMManager.getInstance().getConversation(info.type, info.id)
        currentProvider = ChatProvider()
        mIsMore = true
        mIsLoading = false
    }

    fun onReadReport(receiptList: List<TIMMessageReceipt>) {
        TUIKitLog.i(TAG, "onReadReport:$receiptList")
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (receiptList.size == 0) {
            return
        }
        var max = receiptList[0]
        for (msg in receiptList) {
            if (!TextUtils.equals(msg.conversation.peer, mCurrentConversation?.peer)
                    || msg.conversation.type == TIMConversationType.Group) {
                continue
            }
            if (max.timestamp < msg.timestamp) {
                max = msg
            }
        }
        currentProvider?.updateReadMessage(max)
    }

    override fun onNewMessages(msgs: List<TIMMessage>): Boolean {
        if (null != msgs && msgs.size > 0) {
            for (msg in msgs) {
                val conversation = msg.conversation
                val type = conversation.type
                if (type == TIMConversationType.C2C) {
                    if (isTyping(msg)) {
                        notifyTyping()
                    } else {
                        onReceiveMessage(conversation, msg)
                    }
                    TUIKitLog.i(TAG, "onNewMessages() C2C msg = $msg")
                } else if (type == TIMConversationType.Group) {
                    onReceiveMessage(conversation, msg)
                    TUIKitLog.i(TAG, "onNewMessages() Group msg = $msg")
                } else if (type == TIMConversationType.System) {
                    onReceiveSystemMessage(msg)
                    TUIKitLog.i(TAG, "onReceiveSystemMessage() msg = $msg")
                }
            }
        }
        return false
    }

    private fun notifyTyping() {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        currentProvider?.notifyTyping()
    }

    // GroupChatManager会重写该方法
    protected open fun onReceiveSystemMessage(msg: TIMMessage) {
        val ele = msg.getElement(0)
        val eleType = ele.type
        // 用户资料修改通知，不需要在聊天界面展示，可以通过 TIMUserConfig 中的 setFriendshipListener 处理
        if (eleType == TIMElemType.ProfileTips) {
            TUIKitLog.i(TAG, "onReceiveSystemMessage eleType is ProfileTips, ignore")
        }
        if (eleType == TIMElemType.SNSTips) {
            TUIKitLog.i(TAG, "onReceiveSystemMessage eleType is SNSTips")
            val m = ele as TIMSNSSystemElem
            if (m.requestAddFriendUserList.size > 0) {
                toastLongMessage("好友申请通过")
            }
            if (m.delFriendAddPendencyList.size > 0) {
                toastLongMessage("好友申请被拒绝")
            }
        }
    }

    protected open fun onReceiveMessage(conversation: TIMConversation?, msg: TIMMessage?) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (conversation == null || conversation.peer == null) {
            return
        }
        addMessage(conversation, msg)
    }

    protected abstract fun isGroup(): Boolean

    protected open fun addMessage(conversation: TIMConversation, msg: TIMMessage?) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        val list = TIMMessage2MessageInfo(msg, isGroup())
        if (list != null && list.isNotEmpty() && mCurrentConversation?.peer == conversation.peer) {
            currentProvider?.addMessageInfoList(list)
            for (msgInfo in list) {
                msgInfo?.isRead = true
                addGroupMessage(msgInfo)
            }
            mCurrentConversation?.setReadMessage(msg, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "addMessage() setReadMessage failed, code = $code, desc = $desc")
                }

                override fun onSuccess() {
                    TUIKitLog.d(TAG, "addMessage() setReadMessage success")
                }
            })
        }
    }

    protected open fun addGroupMessage(msgInfo: MessageInfo?) { // GroupChatManagerKit会重写该方法
    }

    fun deleteMessage(position: Int, messageInfo: MessageInfo) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (messageInfo.remove()) {
            currentProvider?.remove(position)
        }
    }

    fun revokeMessage(position: Int, messageInfo: MessageInfo) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        messageInfo.tIMMessage?.let {
            mCurrentConversation?.revokeMessage(it, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    if (code == REVOKE_TIME_OUT) {
                        toastLongMessage("消息发送已超过2分钟")
                    } else {
                        toastLongMessage("撤回失败:$code=$desc")
                    }
                }

                override fun onSuccess() {
                    if (!safetyCall()) {
                        TUIKitLog.w(TAG, "unSafetyCall")
                        return
                    }
                    currentProvider?.updateMessageRevoked(messageInfo.id)
                    ConversationManagerKit.instance.loadConversation(null)
                }
            })
        }
    }

    fun sendMessage(message: MessageInfo?, retry: Boolean, callBack: IUIKitCallBack?) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (message == null || message.status == MessageInfo.MSG_STATUS_SENDING) {
            return
        }
        message.isSelf = true
        message.isRead = true
        assembleGroupMessage(message)
        //消息先展示，通过状态来确认发送是否成功
        if (message.getMsgType() < MessageInfo.MSG_TYPE_TIPS) {
            message.status = MessageInfo.MSG_STATUS_SENDING
            if (retry) {
                currentProvider?.resendMessageInfo(message)
            } else {
                currentProvider?.addMessageInfo(message)
            }
        }
        TUIKitLog.i(TAG, "sendMessage:" + message.tIMMessage)
        message.tIMMessage?.let {
            mCurrentConversation?.sendMessage(it, object : TIMValueCallBack<TIMMessage> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.i(TAG, "sendMessage fail:$code=$desc")
                    if (!safetyCall()) {
                        TUIKitLog.w(TAG, "unSafetyCall")
                        return
                    }
                    callBack?.onError(TAG, code, desc)
                    message.status = MessageInfo.MSG_STATUS_SEND_FAIL
                    currentProvider?.updateMessageInfo(message)
                }

                override fun onSuccess(timMessage: TIMMessage) {
                    TUIKitLog.i(TAG, "sendMessage onSuccess")
                    if (!safetyCall()) {
                        TUIKitLog.w(TAG, "unSafetyCall")
                        return
                    }
                    callBack?.onSuccess(currentProvider)
                    message.status = MessageInfo.MSG_STATUS_SEND_SUCCESS
                    message.id = timMessage.msgId
                    currentProvider?.updateMessageInfo(message)
                }
            })
        }
    }

    protected open fun assembleGroupMessage(message: MessageInfo?) { // GroupChatManager会重写该方法
    }

    fun loadLocalChatMessages(lastMessage: MessageInfo?, callBack: IUIKitCallBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (mIsLoading) {
            return
        }
        mIsLoading = true
        if (!mIsMore) {
            currentProvider?.addMessageInfo(null)
            callBack.onSuccess(null)
            mIsLoading = false
            return
        }
        var lastTIMMsg: TIMMessage? = null
        if (lastMessage == null) {
            currentProvider?.clear()
        } else {
            lastTIMMsg = lastMessage.tIMMessage
        }
        val unread = mCurrentConversation?.unreadMessageNum?.toInt() ?: 0
        mCurrentConversation?.getLocalMessage(if (unread > MSG_PAGE_COUNT) unread else MSG_PAGE_COUNT
                , lastTIMMsg, object : TIMValueCallBack<List<TIMMessage?>> {
            override fun onError(code: Int, desc: String) {
                mIsLoading = false
                callBack.onError(TAG, code, desc)
                TUIKitLog.e(TAG, "loadChatMessages() getMessage failed, code = $code, desc = $desc")
            }

            override fun onSuccess(timMessages: List<TIMMessage?>) {
                mIsLoading = false
                if (!safetyCall()) {
                    TUIKitLog.w(TAG, "unSafetyCall")
                    return
                }
                if (unread > 0) {
                    mCurrentConversation?.setReadMessage(null, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            TUIKitLog.e(TAG, "loadChatMessages() setReadMessage failed, code = $code, desc = $desc")
                        }

                        override fun onSuccess() {
                            TUIKitLog.d(TAG, "loadChatMessages() setReadMessage success")
                        }
                    })
                }
                if (timMessages.size < MSG_PAGE_COUNT) {
                    mIsMore = false
                }
                val messages = ArrayList(timMessages)
                Collections.reverse(messages)
                val msgInfos: MutableList<MessageInfo?> = TIMMessages2MessageInfos(messages, isGroup())
                        ?: ArrayList()
                currentProvider?.addMessageList(msgInfos, true)
                for (i in msgInfos.indices) {
                    val info = msgInfos[i]
                    if (MessageInfo.MSG_STATUS_SENDING == info?.status) {
                        sendMessage(info, true, null)
                    }
                }
                callBack.onSuccess(currentProvider)
            }
        })
    }

    fun loadLocalChatMessagesUntilFull(lastMessage: MessageInfo?, callBack: IUIKitCallBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (mIsLoading) {
            return
        }
        mIsLoading = true
        if (!mIsMore) {
            currentProvider?.addMessageInfo(null)
            callBack.onSuccess(null)
            mIsLoading = false
            return
        }
        loadLocalChatMessagesUntilFull(null, null, lastMessage, object : MessageInfoCallback {
            override fun onError(code: Int, desc: String?) {
                mIsLoading = false
                callBack.onError(TAG, code, desc)
                TUIKitLog.e(TAG, "loadChatMessages() getMessage failed, code = $code, desc = $desc")
            }

            override fun onSuccess(fullData: ArrayList<MessageInfo?>, showingData: ArrayList<MessageInfo?>) {
                mIsLoading = false
                currentProvider?.addMessageList(fullData, true)
                currentProvider?.addMessageListShowing(showingData, true)
                callBack.onSuccess(currentProvider)
            }
        })
    }

    private fun loadLocalChatMessagesUntilFull(lastMessages: ArrayList<MessageInfo?>?, lastMessagesShowing: ArrayList<MessageInfo?>?, lastMessage: MessageInfo?, callback: MessageInfoCallback) {
        val count = lastMessagesShowing?.size ?: 0
        if (count >= MSG_PAGE_COUNT) {
            callback.onSuccess(lastMessages!!, lastMessagesShowing!!)
            return
        }
        var lastTIMMsg: TIMMessage? = null
        if (lastMessage != null) {
            lastTIMMsg = lastMessage.tIMMessage
        }
        val unread = mCurrentConversation?.unreadMessageNum?.toInt() ?: 0
        mCurrentConversation?.getLocalMessage(if (unread > MSG_PAGE_COUNT) unread else MSG_PAGE_COUNT
                , lastTIMMsg, object : TIMValueCallBack<List<TIMMessage?>> {
            override fun onError(code: Int, desc: String) {
                mIsLoading = false
                if (lastMessages != null) {
                    callback.onSuccess(lastMessages, lastMessagesShowing!!)
                } else {
                    callback.onError(code, desc)
                }
            }

            override fun onSuccess(timMessages: List<TIMMessage?>) {
                if (!safetyCall()) {
                    TUIKitLog.w(TAG, "unSafetyCall")
                    return
                }
                if (unread > 0) {
                    mCurrentConversation?.setReadMessage(null, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            TUIKitLog.e(TAG, "loadChatMessages() setReadMessage failed, code = $code, desc = $desc")
                        }

                        override fun onSuccess() {
                            TUIKitLog.d(TAG, "loadChatMessages() setReadMessage success")
                        }
                    })
                }
                if (timMessages.size < MSG_PAGE_COUNT) {
                    mIsMore = false
                }
                val messages = ArrayList(timMessages)
                Collections.reverse(messages)
                val msgInfos: ArrayList<MessageInfo?> = TIMMessages2MessageInfos(messages, isGroup())
                        ?: ArrayList()
                for (i in msgInfos.indices) {
                    val info = msgInfos[i]
                    if (info?.status == MessageInfo.MSG_STATUS_SENDING) {
                        sendMessage(info, true, null)
                    }
                }
                val realMsgInfos = getRealMessages(msgInfos) ?: ArrayList()
                if (lastMessages != null) {
                    msgInfos.addAll(lastMessages)
                }
                if (lastMessagesShowing != null) {
                    realMsgInfos?.addAll(lastMessagesShowing)
                }
                val realCount = realMsgInfos?.size ?: 0
                if (realCount >= MSG_PAGE_COUNT || !mIsMore) {
                    callback.onSuccess(msgInfos, realMsgInfos)
                } else {
                    loadLocalChatMessagesUntilFull(msgInfos, realMsgInfos, CommonUtil.getItemFromList(msgInfos, 0), callback)
                }
            }
        })
    }

    private fun getRealMessages(messages: ArrayList<MessageInfo?>?): ArrayList<MessageInfo?>? {
        if (messages == null) {
            return null
        }
        val realMessages = ArrayList<MessageInfo?>()
        for (i in messages.indices) {
            val info = messages[i]
            if (info?.isBlankMessage != true) {
                realMessages.add(info)
            }
        }
        return realMessages
    }

    fun loadChatMessages(lastMessage: MessageInfo?, callBack: IUIKitCallBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (mIsLoading) {
            return
        }
        mIsLoading = true
        if (!mIsMore) {
            currentProvider?.addMessageInfo(null)
            callBack.onSuccess(null)
            mIsLoading = false
            return
        }
        var lastTIMMsg: TIMMessage? = null
        if (lastMessage == null) {
            currentProvider?.clear()
        } else {
            lastTIMMsg = lastMessage.tIMMessage
        }
        val unread = mCurrentConversation?.unreadMessageNum?.toInt() ?: 0
        mCurrentConversation?.getMessage(if (unread > MSG_PAGE_COUNT) unread else MSG_PAGE_COUNT
                , lastTIMMsg, object : TIMValueCallBack<List<TIMMessage?>> {
            override fun onError(code: Int, desc: String) {
                mIsLoading = false
                val msgInfos: ArrayList<MessageInfo?> = ArrayList()
                currentProvider?.addMessageList(msgInfos, true)
                callBack.onError(TAG, code, desc)
                TUIKitLog.e(TAG, "loadChatMessages() getMessage failed, code = $code, desc = $desc")
            }

            override fun onSuccess(timMessages: List<TIMMessage?>) {
                mIsLoading = false
                if (!safetyCall()) {
                    TUIKitLog.w(TAG, "unSafetyCall")
                    return
                }
                if (unread > 0) {
                    mCurrentConversation?.setReadMessage(null, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            TUIKitLog.e(TAG, "loadChatMessages() setReadMessage failed, code = $code, desc = $desc")
                        }

                        override fun onSuccess() {
                            TUIKitLog.d(TAG, "loadChatMessages() setReadMessage success")
                        }
                    })
                }
                if (timMessages.size < MSG_PAGE_COUNT) {
                    mIsMore = false
                }
                val messages = ArrayList(timMessages)
                Collections.reverse(messages)
                val msgInfos: MutableList<MessageInfo?> = TIMMessages2MessageInfos(messages, isGroup())
                        ?: ArrayList()
                currentProvider?.addMessageList(msgInfos!!, true)
                for (i in msgInfos.indices) {
                    val info = msgInfos[i]
                    if (info?.status == MessageInfo.MSG_STATUS_SENDING) {
                        sendMessage(info, true, null)
                    }
                }
                callBack.onSuccess(currentProvider)
            }
        })
    }

    fun loadChatMessagesUtilFull(lastMessage: MessageInfo?, callBack: IUIKitCallBack) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (mIsLoading) {
            return
        }
        mIsLoading = true
        if (!mIsMore) {
            currentProvider?.addMessageInfo(null)
            callBack.onSuccess(null)
            mIsLoading = false
            return
        }
        loadChatMessagesUtilFull(null, null, lastMessage, object : MessageInfoCallback {
            override fun onError(code: Int, desc: String?) {
                mIsLoading = false
                callBack.onError(TAG, code, desc)
                TUIKitLog.e(TAG, "loadChatMessages() getMessage failed, code = $code, desc = $desc")
            }

            override fun onSuccess(fullData: ArrayList<MessageInfo?>, showingData: ArrayList<MessageInfo?>) {
                mIsLoading = false
                currentProvider?.addMessageList(fullData, true)
                currentProvider?.addMessageListShowing(showingData, true)
                callBack.onSuccess(currentProvider)
            }
        })
    }

    private fun loadChatMessagesUtilFull(lastMessages: ArrayList<MessageInfo?>?, lastMessagesShowing: ArrayList<MessageInfo?>?, lastMessage: MessageInfo?, callback: MessageInfoCallback) {
        val count = lastMessagesShowing?.size ?: 0
        if (count >= MSG_PAGE_COUNT) {
            callback.onSuccess(lastMessages!!, lastMessagesShowing!!)
            return
        }
        var lastTIMMsg: TIMMessage? = null
        if (lastMessage != null) {
            lastTIMMsg = lastMessage.tIMMessage
        }
        val unread = mCurrentConversation?.unreadMessageNum?.toInt() ?: 0
        mCurrentConversation?.getMessage(if (unread > MSG_PAGE_COUNT) unread else MSG_PAGE_COUNT
                , lastTIMMsg, object : TIMValueCallBack<List<TIMMessage?>> {
            override fun onError(code: Int, desc: String) {
                mIsLoading = false
                if (lastMessages != null) {
                    callback.onSuccess(lastMessages, lastMessagesShowing!!)
                } else {
                    callback.onError(code, desc)
                }
            }

            override fun onSuccess(timMessages: List<TIMMessage?>) {
                if (!safetyCall()) {
                    TUIKitLog.w(TAG, "unSafetyCall")
                    return
                }
                if (unread > 0) {
                    mCurrentConversation?.setReadMessage(null, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            TUIKitLog.e(TAG, "loadChatMessages() setReadMessage failed, code = $code, desc = $desc")
                        }

                        override fun onSuccess() {
                            TUIKitLog.d(TAG, "loadChatMessages() setReadMessage success")
                        }
                    })
                }
                if (timMessages.size < MSG_PAGE_COUNT) {
                    mIsMore = false
                }
                val messages = ArrayList(timMessages)
                Collections.reverse(messages)
                val msgInfos: ArrayList<MessageInfo?> = TIMMessages2MessageInfos(messages, isGroup())
                        ?: ArrayList()
                for (i in msgInfos?.indices) {
                    val info = msgInfos[i]
                    if (info?.status == MessageInfo.MSG_STATUS_SENDING) {
                        sendMessage(info, true, null)
                    }
                }
                val realMsgInfos = getRealMessages(msgInfos) ?: ArrayList()
                if (lastMessages != null) {
                    msgInfos.addAll(lastMessages)
                }
                if (lastMessagesShowing != null) {
                    realMsgInfos?.addAll(lastMessagesShowing)
                }
                val realCount = realMsgInfos?.size ?: 0
                if (realCount >= MSG_PAGE_COUNT || !mIsMore) {
                    callback.onSuccess(msgInfos, realMsgInfos)
                } else {
                    loadChatMessagesUtilFull(msgInfos, realMsgInfos, CommonUtil.getItemFromList(msgInfos, 0), callback)
                }
            }
        })
    }

    override fun handleInvoke(locator: TIMMessageLocator) {
        if (!safetyCall()) {
            TUIKitLog.w(TAG, "unSafetyCall")
            return
        }
        if (locator.conversationId == getCurrentChatInfo()?.id) {
            TUIKitLog.i(TAG, "handleInvoke locator = $locator")
            currentProvider?.updateMessageRevoked(locator)
        }
    }

    protected fun safetyCall(): Boolean {
        return !(mCurrentConversation == null || currentProvider == null || getCurrentChatInfo() == null)
    }

}