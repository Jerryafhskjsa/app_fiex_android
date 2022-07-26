package com.black.im.provider

import android.text.TextUtils
import com.black.im.adapter.MessageListAdapter
import com.black.im.interfaces.IChatProvider
import com.black.im.model.chat.MessageInfo
import com.black.im.widget.MessageLayout
import com.tencent.imsdk.ext.message.TIMMessageLocator
import com.tencent.imsdk.ext.message.TIMMessageReceipt
import java.util.*

class ChatProvider : IChatProvider {
    private val mDataSource: ArrayList<MessageInfo?> = ArrayList()
    private val mDataSourceShowing: ArrayList<MessageInfo?> = ArrayList()
    private var mAdapter: MessageListAdapter? = null
    private var mTypingListener: TypingListener? = null

    override fun getDataSource(): MutableList<MessageInfo?>? {
        return mDataSource
    }

    override fun getDataSourceShowing(): MutableList<MessageInfo?> {
        return mDataSourceShowing
    }

    override fun addMessageList(msgs: MutableList<MessageInfo?>?, front: Boolean): Boolean {
        return if (msgs == null) false else if (front) {
            mDataSource.addAll(0, msgs)
            //            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_FRONT, msgs.size());
        } else {
            mDataSource.addAll(msgs)
            //            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_BACK, msgs.size());
        }
    }

    override fun addMessageListShowing(msgs: MutableList<MessageInfo?>?, front: Boolean): Boolean {
        if (msgs == null) {
            return false
        }
        val flag: Boolean
        if (front) {
            flag = mDataSourceShowing.addAll(0, msgs)
            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_FRONT, msgs.size)
        } else {
            flag = mDataSourceShowing.addAll(msgs)
            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_BACK, msgs.size)
        }
        return flag
    }

    private fun checkExist(msg: MessageInfo?): Boolean {
        if (msg != null) {
            val msgId = msg.id
            for (i in mDataSource.indices.reversed()) {
                if (mDataSource[i]!!.id == msgId && mDataSource[i]!!.uniqueId == msg.uniqueId && TextUtils.equals(mDataSource[i]!!.extra.toString(), msg.extra.toString())) {
                    return true
                }
            }
        }
        return false
    }

    override fun deleteMessageList(messages: MutableList<MessageInfo?>?): Boolean {
        if (messages == null) {
            return false
        }
        for (i in mDataSource.indices) {
            for (j in messages.indices) {
                if (mDataSource[i]!!.id == messages[j]?.id) {
                    mDataSource.removeAt(i)
                    updateAdapter(MessageLayout.DATA_CHANGE_TYPE_DELETE, i)
                    break
                }
            }
        }
        return false
    }

    override fun updateMessageList(messages: MutableList<MessageInfo?>?): Boolean {
        return false
    }

    fun addMessageInfoList(msg: MutableList<MessageInfo?>?): Boolean {
        if (msg == null || msg.isEmpty()) {
            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_LOAD, 0)
            return true
        }
        val list: MutableList<MessageInfo?> = ArrayList()
        for (info in msg) {
            if (checkExist(info) || info?.isBlankMessage == true) {
                continue
            }
            list.add(info)
        }
        val flag = mDataSourceShowing.addAll(list)
        mDataSource.addAll(list)
        updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_BACK, list.size)
        return flag
    }

    fun addMessageInfo(msg: MessageInfo?): Boolean {
        if (msg == null) {
            updateAdapter(MessageLayout.DATA_CHANGE_TYPE_LOAD, 0)
            return true
        }
        if (checkExist(msg)) {
            return true
        }
        val flag = mDataSourceShowing.add(msg)
        mDataSource.add(msg)
        updateAdapter(MessageLayout.DATA_CHANGE_TYPE_ADD_BACK, 1)
        return flag
    }

    fun deleteMessageInfo(msg: MessageInfo): Boolean {
        for (i in mDataSource.indices) {
            if (mDataSource[i]!!.id == msg.id) {
                mDataSource.removeAt(i)
                updateAdapter(MessageLayout.DATA_CHANGE_TYPE_DELETE, -1)
                return true
            }
        }
        return false
    }

    fun resendMessageInfo(message: MessageInfo): Boolean {
        var found = false
        for (i in mDataSourceShowing.indices) {
            if (mDataSourceShowing[i]!!.id == message.id) {
                val removeMessage = mDataSourceShowing.removeAt(i)
                mDataSource.remove(removeMessage)
                found = true
                break
            }
        }
        return if (!found) {
            false
        } else addMessageInfo(message)
    }

    fun updateMessageInfo(message: MessageInfo): Boolean {
        for (i in mDataSourceShowing.indices) {
            if (mDataSourceShowing[i]!!.id == message.id) {
                mDataSourceShowing.removeAt(i)
                mDataSourceShowing.add(i, message)
                for (j in mDataSource.indices) {
                    if (mDataSource[j]!!.id == message.id) {
                        mDataSource.removeAt(j)
                        mDataSource.add(j, message)
                        break
                    }
                }
                updateAdapter(MessageLayout.DATA_CHANGE_TYPE_UPDATE, i)
                return true
            }
        }
        return false
    }

    fun updateMessageRevoked(locator: TIMMessageLocator?): Boolean {
        for (i in mDataSourceShowing.indices) {
            val messageInfo = mDataSourceShowing[i]
            // 一条包含多条元素的消息，撤回时，会把所有元素都撤回，所以下面的判断即使满足条件也不能return
            if (messageInfo!!.checkEquals(locator!!)) {
                messageInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE)
                messageInfo.status = MessageInfo.MSG_STATUS_REVOKE
                updateAdapter(MessageLayout.DATA_CHANGE_TYPE_UPDATE, i)
            }
        }
        return false
    }

    fun updateMessageRevoked(msgId: String): Boolean {
        for (i in mDataSourceShowing.indices) {
            val messageInfo = mDataSourceShowing[i]
            // 一条包含多条元素的消息，撤回时，会把所有元素都撤回，所以下面的判断即使满足条件也不能return
            if (messageInfo!!.id == msgId) {
                messageInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE)
                messageInfo.status = MessageInfo.MSG_STATUS_REVOKE
                updateAdapter(MessageLayout.DATA_CHANGE_TYPE_UPDATE, i)
            }
        }
        return false
    }

    fun updateReadMessage(max: TIMMessageReceipt) {
        for (i in mDataSourceShowing.indices) {
            val messageInfo = mDataSourceShowing[i]
            if (messageInfo!!.msgTime > max.timestamp) {
                messageInfo.isPeerRead = false
            } else {
                messageInfo.isPeerRead = true
                updateAdapter(MessageLayout.DATA_CHANGE_TYPE_UPDATE, i)
            }
        }
    }

    fun notifyTyping() {
        if (mTypingListener != null) {
            mTypingListener!!.onTyping()
        }
    }

    fun setTypingListener(l: TypingListener?) {
        mTypingListener = l
    }

    fun remove(index: Int) {
        val messageInfo = mDataSourceShowing.removeAt(index)
        mDataSource.remove(messageInfo)
        updateAdapter(MessageLayout.DATA_CHANGE_TYPE_DELETE, index)
    }

    fun clear() {
        mDataSource.clear()
        mDataSourceShowing.clear()
        updateAdapter(MessageLayout.DATA_CHANGE_TYPE_LOAD, 0)
    }

    private fun updateAdapter(type: Int, data: Int) {
        if (mAdapter != null) {
            mAdapter!!.notifyDataSourceChanged(type, data)
        }
    }

    override fun setAdapter(adapter: MessageListAdapter?) {
        mAdapter = adapter
    }

    interface TypingListener {
        fun onTyping()
    }
}