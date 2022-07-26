package com.black.im.widget

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.black.im.R
import com.black.im.adapter.MessageListAdapter
import com.black.im.interfaces.IChatLayout
import com.black.im.interfaces.IChatProvider
import com.black.im.manager.ChatManagerKit
import com.black.im.model.chat.MessageInfo
import com.black.im.provider.ChatProvider
import com.black.im.util.AudioPlayer
import com.black.im.util.BackgroundTasks
import com.black.im.util.IUIKitCallBack
import com.black.im.util.NetWorkUtils
import com.black.im.util.ToastUtil.toastLongMessage
import com.tencent.imsdk.TIMTextElem

abstract class AbsChatLayout : ChatLayoutUI, IChatLayout {
    protected var mAdapter: MessageListAdapter? = null
    private var mVolumeAnim: AnimationDrawable? = null
    private var mTypingRunnable: Runnable? = null
    private val mTypingListener: ChatProvider.TypingListener = object : ChatProvider.TypingListener {
        override fun onTyping() {
            val oldTitle: String = titleBar?.middleTitle?.text.toString()
            titleBar?.middleTitle?.setText(R.string.typing)
            if (mTypingRunnable == null) {
                mTypingRunnable = Runnable { titleBar?.middleTitle?.text = oldTitle }
            }
            titleBar?.middleTitle?.removeCallbacks(mTypingRunnable)
            titleBar?.middleTitle?.postDelayed(mTypingRunnable, 3000)
        }
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private fun initListener() {
        messageLayout?.setPopActionClickListener(object : MessageLayout.OnPopActionClickListener {
            override fun onCopyClick(position: Int, msg: MessageInfo?) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard == null || msg == null) {
                    return
                }
                if (msg.element is TIMTextElem) {
                    val textElem = msg.element as TIMTextElem
                    val clip = ClipData.newPlainText("message", textElem.text)
                    clipboard.primaryClip = clip
                }
            }

            override fun onSendMessageClick(msg: MessageInfo?, retry: Boolean) {
                sendMessage(msg, retry)
            }

            override fun onDeleteMessageClick(position: Int, msg: MessageInfo?) {
                deleteMessage(position, msg)
            }

            override fun onRevokeMessageClick(position: Int, msg: MessageInfo?) {
                revokeMessage(position, msg)
            }
        })
        messageLayout?.setLoadMoreMessageHandler(object : MessageLayout.OnLoadMoreHandler {
            override fun loadMore() {
                loadMessages()
            }
        })
        messageLayout?.setEmptySpaceClickListener(object : MessageLayout.OnEmptySpaceClickListener {
            override fun onClick() {
                inputLayout?.hideSoftInput()
            }
        })
        /**
         * 设置消息列表空白处点击处理
         */
        messageLayout?.addOnItemTouchListener(object : OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_UP) {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child == null) {
                        inputLayout?.hideSoftInput()
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
                        if (touchChild == null) inputLayout?.hideSoftInput()
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        inputLayout?.setChatInputHandler(object : InputLayout.ChatInputHandler {
            override fun onInputAreaClick() {
                post { scrollToEnd() }
            }

            override fun onRecordStatusChanged(status: Int) {
                when (status) {
                    InputLayout.ChatInputHandler.RECORD_START -> startRecording()
                    InputLayout.ChatInputHandler.RECORD_STOP -> stopRecording()
                    InputLayout.ChatInputHandler.RECORD_CANCEL -> cancelRecording()
                    InputLayout.ChatInputHandler.RECORD_TOO_SHORT, InputLayout.ChatInputHandler.RECORD_FAILED -> stopAbnormally(status)
                    else -> {
                    }
                }
            }

            private fun startRecording() {
                post {
                    AudioPlayer.instance.stopPlay()
                    mRecordingGroup?.visibility = View.VISIBLE
                    mRecordingIcon?.setImageResource(R.drawable.recording_volume)
                    mVolumeAnim = mRecordingIcon?.drawable as AnimationDrawable?
                    mVolumeAnim?.start()
                    mRecordingTips?.setTextColor(Color.WHITE)
                    mRecordingTips?.text = "手指上滑，取消发送"
                }
            }

            private fun stopRecording() {
                postDelayed({
                    mVolumeAnim?.stop()
                    mRecordingGroup?.visibility = View.GONE
                }, 500)
            }

            private fun stopAbnormally(status: Int) {
                post {
                    mVolumeAnim?.stop()
                    mRecordingIcon?.setImageResource(R.drawable.ic_volume_dialog_length_short)
                    mRecordingTips?.setTextColor(Color.WHITE)
                    if (status == InputLayout.ChatInputHandler.RECORD_TOO_SHORT) {
                        mRecordingTips?.text = "说话时间太短"
                    } else {
                        mRecordingTips?.text = "录音失败"
                    }
                }
                postDelayed({ mRecordingGroup?.visibility = View.GONE }, 1000)
            }

            private fun cancelRecording() {
                post {
                    mRecordingIcon?.setImageResource(R.drawable.ic_volume_dialog_cancel)
                    mRecordingTips?.text = "松开手指，取消发送"
                }
            }
        })
    }

    override fun initDefault() {
        titleBar?.leftGroup?.visibility = View.VISIBLE
        titleBar?.setOnLeftClickListener(OnClickListener {
            if (context is Activity) {
                (context as Activity).finish()
            }
        })
        inputLayout?.setMessageHandler(object : InputLayout.MessageHandler {
            override fun sendMessage(msg: MessageInfo?) {
                sendMessage(msg, false)
            }

        })
        if (messageLayout?.adapter == null) {
            mAdapter = MessageListAdapter()
            messageLayout?.setAdapter(mAdapter)
        }
        initListener()
    }

    override fun setParentLayout(parentContainer: Any?) {}
    fun scrollToEnd() {
        messageLayout?.scrollToEnd()
    }

    var oldCount = -1
    fun setDataProvider(provider: IChatProvider?) {
        if (provider != null) {
            (provider as ChatProvider).setTypingListener(mTypingListener)
        }
        //        if (mAdapter != null) {
//            mAdapter.setDataSource(provider, getRealMessageInfos(provider));
//        }
        if (mAdapter != null) {
            mAdapter?.setDataSource(provider)
        }
    }

    private fun getRealMessageInfos(provider: IChatProvider?): List<MessageInfo?> {
        val msgInfos: List<MessageInfo?> = provider?.getDataSource()
                ?: return ArrayList()
        val realMsgInfos: MutableList<MessageInfo?> = ArrayList()
        for (i in msgInfos.indices) {
            val messageInfo = msgInfos[i]
            if (true != messageInfo?.isBlankMessage) {
                realMsgInfos.add(messageInfo)
            }
        }
        return realMsgInfos
    }

    private fun getRealMessageCount(provider: IChatProvider): Int {
        var realCount = 0
        val data = provider.getDataSource()
        val count = data?.size ?: 0
        for (i in 0 until count) {
            val messageInfo = data!![i]
            if (true != messageInfo?.isBlankMessage) {
                realCount++
            }
        }
        return realCount
    }

    abstract val chatManager: ChatManagerKit?
    override fun loadMessages() {
        val messageInfos: List<MessageInfo?> = chatManager?.currentProvider?.getDataSource()
                ?: ArrayList()
        loadChatMessages(if (messageInfos.isNotEmpty()) messageInfos[0] else null)
        //        loadChatMessages(mAdapter.getItemCount() > 0 ? mAdapter.getItem(1) : null);
    }

    fun loadChatMessages(lastMessage: MessageInfo?) {
        if (NetWorkUtils.sIMSDKConnected) {
            chatManager?.loadChatMessagesUtilFull(lastMessage, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    if (lastMessage == null && data != null) {
                        setDataProvider(data as ChatProvider?)
                    }
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage(errMsg)
                    if (lastMessage == null) {
                        setDataProvider(null)
                    }
                }
            })
        } else {
            chatManager?.loadLocalChatMessagesUntilFull(lastMessage, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    if (lastMessage == null && data != null) {
                        setDataProvider(data as ChatProvider?)
                    }
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage(errMsg)
                    if (lastMessage == null) {
                        setDataProvider(null)
                    }
                }
            })
        }
    }

    protected fun deleteMessage(position: Int, msg: MessageInfo?) {
        chatManager?.deleteMessage(position, msg!!)
    }

    protected fun revokeMessage(position: Int, msg: MessageInfo?) {
        chatManager?.revokeMessage(position, msg!!)
    }

    override fun sendMessage(msg: MessageInfo?, retry: Boolean) {
        val l = sendCheckListener
        if (l == null || l.beforeMessageSendCheck(msg)) {
            chatManager?.sendMessage(msg, retry, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    BackgroundTasks.instance?.runOnUiThread(Runnable { scrollToEnd() })
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage(errMsg)
                }
            })
        }
    }

    override fun exitChat() {
        titleBar?.middleTitle?.removeCallbacks(mTypingRunnable)
        AudioPlayer.instance.stopRecord()
        AudioPlayer.instance.stopPlay()
        if (chatManager != null) {
            chatManager?.destroyChat()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //        exitChat();
    }
}