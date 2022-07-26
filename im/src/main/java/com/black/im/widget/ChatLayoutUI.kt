package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.interfaces.IChatLayout
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo

abstract class ChatLayoutUI : LinearLayout, IChatLayout {
    protected var mGroupApplyLayout: NoticeLayout? = null
    protected var mRecordingGroup: View? = null
    protected var mRecordingIcon: ImageView? = null
    protected var mRecordingTips: TextView? = null
    final override var titleBar: TitleBarLayout? = null
        private set
    final override var messageLayout: MessageLayout? = null
        private set
    final override var inputLayout: InputLayout? = null
        private set
    final override var noticeLayout: NoticeLayout? = null
        private set
    var sendCheckListener: SendCheckListener? = null
    var messageClickCheckListener: MessageClickCheckListener? = null
        get
        private set

    constructor(context: Context?) : super(context) {
        initViews()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViews()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViews()
    }

    private fun initViews() {
        View.inflate(context, R.layout.chat_layout, this)
        titleBar = findViewById(R.id.chat_title_bar)
        messageLayout = findViewById(R.id.chat_message_layout)
        inputLayout = findViewById(R.id.chat_input_layout)
        inputLayout?.setSendCheckListener(sendCheckListener)
        mRecordingGroup = findViewById(R.id.voice_recording_view)
        mRecordingIcon = findViewById(R.id.recording_icon)
        mRecordingTips = findViewById(R.id.recording_tips)
        mGroupApplyLayout = findViewById(R.id.chat_group_apply_layout)
        noticeLayout = findViewById(R.id.chat_notice_layout)
        init()
    }

    protected fun init() {}

    override fun setChatInfo(chatInfo: ChatInfo?) {
        if (chatInfo == null) {
            return
        }
        val chatTitle = chatInfo.chatName
        titleBar?.setTitle(chatTitle, ITitleBarLayout.POSITION.MIDDLE)
    }

    override fun exitChat() {}
    override fun initDefault() {}
    override fun loadMessages() {}
    override fun sendMessage(msg: MessageInfo?, retry: Boolean) {}

    override fun setParentLayout(parent: Any?) {}

    fun setMessageClickCheckListener(messageClickCheckListener: MessageClickCheckListener?) {
        this.messageClickCheckListener = messageClickCheckListener
        if (inputLayout != null) {
            inputLayout?.setSendCheckListener(sendCheckListener)
        }
    }

    interface SendCheckListener {
        /**
         * 发送消息之前检查，返回true则进行发送
         *
         * @param msg 需要发送的消息体
         * @return 返回true发送消息
         */
        fun beforeMessageSendCheck(msg: MessageInfo?): Boolean
    }

    interface MessageClickCheckListener {
        /**
         * 点击消息之前检查，返回true则进行点击
         *
         * @param msg 需要点击的消息体
         * @return 返回true点击消息
         */
        fun onMessageClickCheck(msg: MessageInfo?): Boolean
    }
}