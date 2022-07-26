package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.black.im.R
import com.black.im.adapter.ConversationListAdapter
import com.black.im.adapter.IConversationAdapter
import com.black.im.interfaces.IConversationLayout
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.manager.ConversationManagerKit
import com.black.im.model.ConversationInfo
import com.black.im.provider.ConversationProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.ToastUtil.toastLongMessage

class ConversationLayout : RelativeLayout, IConversationLayout {
    override var titleBar: TitleBarLayout? = null
        private set
    override var conversationList: ConversationListLayout? = null
        private set

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * 初始化相关UI元素
     */
    private fun init() {
        View.inflate(context, R.layout.conversation_layout, this)
        titleBar = findViewById(R.id.conversation_title)
        conversationList = findViewById(R.id.conversation_list)
    }

    fun initDefault() {
        titleBar?.setTitle(resources.getString(R.string.conversation_title), ITitleBarLayout.POSITION.MIDDLE)
        titleBar?.leftGroup?.visibility = View.GONE
        titleBar?.setRightIcon(R.drawable.conversation_more)
        val adapter: IConversationAdapter = ConversationListAdapter()
        conversationList?.setAdapter(adapter)
        ConversationManagerKit.instance.loadConversation(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                adapter.setDataProvider(data as ConversationProvider)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage("加载消息失败")
            }
        })
    }

    override fun setParentLayout(parent: Any?) {}

    fun addConversationInfo(position: Int, info: ConversationInfo?) {
        if (info == null) {
            return
        }
        conversationList?.adapter?.addItem(position, info)
    }

    fun removeConversationInfo(position: Int) {
        conversationList?.getAdapter()?.removeItem(position)
    }

    override fun setConversationTop(position: Int, conversation: ConversationInfo?) {
        conversation?.let {
            ConversationManagerKit.instance.setConversationTop(position, conversation)
        }
    }

    override fun deleteConversation(position: Int, conversation: ConversationInfo?) {
        conversation?.let {
            ConversationManagerKit.instance.deleteConversation(position, conversation)
        }
    }
}