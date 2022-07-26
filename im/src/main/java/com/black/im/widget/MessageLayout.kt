package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.black.im.action.PopActionClickListener
import com.black.im.action.PopMenuAction
import com.black.im.adapter.MessageListAdapter
import com.black.im.model.PopupList
import com.black.im.model.chat.MessageInfo
import java.util.*

class MessageLayout : MessageLayoutUI {
    companion object {
        val DATA_CHANGE_TYPE_REFRESH = 0
        val DATA_CHANGE_TYPE_LOAD = 1
        val DATA_CHANGE_TYPE_ADD_FRONT = 2
        val DATA_CHANGE_TYPE_ADD_BACK = 3
        val DATA_CHANGE_TYPE_UPDATE = 4
        val DATA_CHANGE_TYPE_DELETE = 5
        val DATA_CHANGE_TYPE_CLEAR = 6
    }

    constructor (context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP) {
            val child = findChildViewUnder(e.x, e.y)
            if (child == null) {
                if (mEmptySpaceClickListener != null) mEmptySpaceClickListener?.onClick()
            } else if (child is ViewGroup) {
                val group = child
                val count = group.childCount
                val x = e.rawX
                val y = e.rawY
                var touchChild: View? = null
                for (i in count - 1 downTo 0) {
                    val innerChild = group.getChildAt(i)
                    val position = IntArray(2)
                    innerChild.getLocationOnScreen(position)
                    if (x >= position[0] && x <= position[0] + innerChild.measuredWidth && y >= position[1] && y <= position[1] + innerChild.measuredHeight) {
                        touchChild = innerChild
                        break
                    }
                }
                if (touchChild == null) {
                    if (mEmptySpaceClickListener != null) {
                        mEmptySpaceClickListener?.onClick()
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    fun showItemPopMenu(index: Int, messageInfo: MessageInfo?, view: View) {
        initPopActions(messageInfo)
        mPopActions?.let {
            if (mPopActions?.size == 0) {
                return
            }
            val popupList = PopupList(context)
            val mItemList: MutableList<String> = ArrayList()
            for (action in it) {
                action?.actionName?.let {
                    mItemList.add(it)
                }
            }
            popupList.show(view, mItemList, object : PopupList.PopupListListener {
                override fun showPopupList(adapterView: View?, contextView: View?, contextPosition: Int): Boolean {
                    return true
                }

                override fun onPopupListClick(contextView: View?, contextPosition: Int, position: Int) {
                    val action = it[position]
                    if (action?.actionClickListener != null) {
                        action.actionClickListener?.onActionClick(index, messageInfo)
                    }
                }
            })
            postDelayed({ popupList.hidePopupListWindow() }, 10000) // 10s后无操作自动消失
        }
    }

    private fun initPopActions(msg: MessageInfo?) {
        if (msg == null) {
            return
        }
        val actions: MutableList<PopMenuAction> = ArrayList()
        var action = PopMenuAction()
        if (msg.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {
            action.actionName = "复制"
            action.actionClickListener = object : PopActionClickListener {
                override fun onActionClick(position: Int, data: Any?) {
                    mOnPopActionClickListener?.onCopyClick(position, data as MessageInfo?)
                }
            }
            actions.add(action)
        }
        action = PopMenuAction()
        action.actionName = "删除"
        action.actionClickListener = object : PopActionClickListener {
            override fun onActionClick(position: Int, data: Any?) {
                mOnPopActionClickListener?.onDeleteMessageClick(position, data as MessageInfo?)
            }
        }
        actions.add(action)
        if (msg.isSelf) {
            action = PopMenuAction()
            action.actionName = "撤回"
            action.actionClickListener = object : PopActionClickListener {
                override fun onActionClick(position: Int, data: Any?) {
                    mOnPopActionClickListener?.onRevokeMessageClick(position, data as MessageInfo?)
                }
            }
            actions.add(action)
            if (msg.status == MessageInfo.MSG_STATUS_SEND_FAIL) {
                action = PopMenuAction()
                action.actionName = "重发"
                action.actionClickListener = object : PopActionClickListener {
                    override fun onActionClick(position: Int, data: Any?) {
                        mOnPopActionClickListener?.onSendMessageClick(msg, true)
                    }
                }
                actions.add(action)
            }
        }
        mPopActions?.clear()
        mPopActions?.addAll(actions)
        mPopActions?.addAll(mMorePopActions)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            if (mHandler != null) {
                val layoutManager = layoutManager as LinearLayoutManager?
                val firstPosition = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                val lastPosition = layoutManager?.findLastCompletelyVisibleItemPosition() ?: 0
                if (firstPosition == 0 && lastPosition - firstPosition + 1 < adapter?.itemCount ?: 0) {
                    if (adapter is MessageListAdapter) {
                        (adapter as MessageListAdapter?)?.showLoading()
                    }
                    mHandler?.loadMore()
                }
            }
        }
    }


    fun scrollToEnd() {
        if (adapter != null) {
            scrollToPosition(adapter!!.itemCount - 1)
        }
    }

    fun getLoadMoreHandler(): OnLoadMoreHandler? {
        return mHandler
    }

    fun setLoadMoreMessageHandler(mHandler: OnLoadMoreHandler?) {
        this.mHandler = mHandler
    }

    fun getEmptySpaceClickListener(): OnEmptySpaceClickListener? {
        return mEmptySpaceClickListener
    }

    fun setEmptySpaceClickListener(mEmptySpaceClickListener: OnEmptySpaceClickListener?) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener
    }

    fun setPopActionClickListener(listener: OnPopActionClickListener) {
        mOnPopActionClickListener = listener
    }

    override fun postSetAdapter(adapter: MessageListAdapter?) {
        mAdapter?.onItemClickListener = object : OnItemClickListener {
            override fun onMessageClick(view: View?, position: Int, messageInfo: MessageInfo?) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener?.onMessageClick(view, position, messageInfo)
                }
            }

            override fun onMessageLongClick(view: View?, position: Int, messageInfo: MessageInfo?) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener?.onMessageLongClick(view, position, messageInfo)
                }
            }

            override fun onUserIconClick(view: View?, position: Int, info: MessageInfo?) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener?.onUserIconClick(view, position, info)
                }
            }
        }
    }

    interface OnLoadMoreHandler {
        fun loadMore()
    }

    interface OnEmptySpaceClickListener {
        fun onClick()
    }

    interface OnItemClickListener {
        fun onMessageClick(view: View?, position: Int, messageInfo: MessageInfo?)
        fun onMessageLongClick(view: View?, position: Int, messageInfo: MessageInfo?)
        fun onUserIconClick(view: View?, position: Int, messageInfo: MessageInfo?)
    }

    interface OnPopActionClickListener {
        fun onCopyClick(position: Int, msg: MessageInfo?)
        fun onSendMessageClick(msg: MessageInfo?, retry: Boolean)
        fun onDeleteMessageClick(position: Int, msg: MessageInfo?)
        fun onRevokeMessageClick(position: Int, msg: MessageInfo?)
    }
}
