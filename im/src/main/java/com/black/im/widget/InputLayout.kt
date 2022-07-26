package com.black.im.widget

import android.annotation.SuppressLint
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import com.black.im.R
import com.black.im.fragment.BaseInputFragment
import com.black.im.fragment.FaceFragment
import com.black.im.fragment.InputMoreFragment
import com.black.im.manager.FaceManager.handlerEmojiText
import com.black.im.manager.FaceManager.isFaceChar
import com.black.im.model.chat.MessageInfo
import com.black.im.model.face.Emoji
import com.black.im.util.AudioPlayer
import com.black.im.util.IUIKitCallBack
import com.black.im.util.MessageInfoUtil.buildAudioMessage
import com.black.im.util.MessageInfoUtil.buildCustomFaceMessage
import com.black.im.util.MessageInfoUtil.buildCustomMessage
import com.black.im.util.MessageInfoUtil.buildFileMessage
import com.black.im.util.MessageInfoUtil.buildImageMessage
import com.black.im.util.MessageInfoUtil.buildTextMessage
import com.black.im.util.MessageInfoUtil.buildVideoMessage
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.video.CameraActivity
import com.black.im.video.JCameraView
import java.io.File


/**
 * 聊天界面，底部发送图片、拍照、摄像、文件面板
 */
class InputLayout : InputLayoutUI, View.OnClickListener, TextWatcher {
    companion object {
        private val TAG = InputLayout::class.java.simpleName
        private const val STATE_NONE_INPUT = -1
        private const val STATE_SOFT_INPUT = 0
        private const val STATE_VOICE_INPUT = 1
        private const val STATE_FACE_INPUT = 2
        private const val STATE_ACTION_INPUT = 3
    }

    private var mFaceFragment: FaceFragment? = null
    private var mChatInputHandler: ChatInputHandler? = null
    private var mMessageHandler: MessageHandler? = null
    private var mFragmentManager: FragmentManager? = null
    private var mInputMoreFragment: InputMoreFragment? = null
    private var mSendEnable = false
    private var mAudioCancel = false
    private var mCurrentState = 0
    private var mLastMsgLineCount = 0
    private var mStartRecordY = 0f
    private var mInputContent: String? = null
    private var inputHelper: InputSendHelper? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun init() {
        mAudioInputSwitchButton?.setOnClickListener(this)
        mEmojiInputButton?.setOnClickListener(this)
        mMoreInputButton?.setOnClickListener(this)
        mSendTextButton?.setOnClickListener(this)
        inputText?.addTextChangedListener(this)
        inputText?.setOnTouchListener(OnTouchListener { view, motionEvent ->
            showSoftInput()
            false
        })
        inputText?.setOnKeyListener(OnKeyListener { view, i, keyEvent -> false })
        inputText?.setOnEditorActionListener(OnEditorActionListener { textView, i, keyEvent -> false })
        mSendAudioButton?.setOnTouchListener(OnTouchListener { view, motionEvent ->
            TUIKitLog.i(TAG, "mSendAudioButton onTouch action:" + motionEvent.action)
            if (!checkPermission(AUDIO_RECORD)) {
                TUIKitLog.i(TAG, "audio record checkPermission failed")
                return@OnTouchListener false
            }
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    mAudioCancel = true
                    mStartRecordY = motionEvent.y
                    if (mChatInputHandler != null) {
                        mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_START)
                    }
                    mSendAudioButton?.text = "松开结束"
                    AudioPlayer.instance.startRecord(object : AudioPlayer.Callback {
                        override fun onCompletion(success: Boolean?) {
                            recordComplete(success!!)
                        }
                    })
                }
                MotionEvent.ACTION_MOVE -> {
                    if (motionEvent.y - mStartRecordY < -100) {
                        mAudioCancel = true
                        if (mChatInputHandler != null) {
                            mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_CANCEL)
                        }
                    } else {
                        if (mAudioCancel) {
                            if (mChatInputHandler != null) {
                                mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_START)
                            }
                        }
                        mAudioCancel = false
                    }
                    mSendAudioButton?.text = "松开结束"
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mAudioCancel = motionEvent.y - mStartRecordY < -100
                    if (mChatInputHandler != null) {
                        mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_STOP)
                    }
                    AudioPlayer.instance.stopRecord()
                    mSendAudioButton?.text = "按住说话"
                }
                else -> {
                }
            }
            false
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        inputText?.removeTextChangedListener(this)
    }

    override fun startSendPhoto() {
        if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
            return
        }
        TUIKitLog.i(TAG, "startSendPhoto")
        if (!checkPermission(SEND_PHOTO)) {
            TUIKitLog.i(TAG, "startSendPhoto checkPermission failed")
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        mInputMoreFragment?.setCallback(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                TUIKitLog.i(TAG, "onSuccess: $data")
                val info = buildImageMessage(data as Uri?, true)
                if (mMessageHandler != null) {
                    mMessageHandler?.sendMessage(info)
                    hideSoftInput()
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.i(TAG, "errCode: $errCode")
                toastLongMessage(errMsg)
            }
        })
        mInputMoreFragment?.startActivityForResult(intent, InputMoreFragment.REQUEST_CODE_PHOTO)
    }

    override fun startCapture() {
        if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
            return
        }
        TUIKitLog.i(TAG, "startCapture")
        if (!checkPermission(CAPTURE)) {
            TUIKitLog.i(TAG, "startCapture checkPermission failed")
            return
        }
        val captureIntent = Intent(context, CameraActivity::class.java)
        captureIntent.putExtra(TUIKitConstants.CAMERA_TYPE, JCameraView.BUTTON_STATE_ONLY_CAPTURE)
        CameraActivity.mCallBack = object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                val contentUri = Uri.fromFile(File(data.toString()))
                val msg = buildImageMessage(contentUri, true)
                if (mMessageHandler != null) {
                    mMessageHandler?.sendMessage(msg)
                    hideSoftInput()
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {}
        }
        context.startActivity(captureIntent)
    }

    override fun startVideoRecord() {
        if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
            return
        }
        TUIKitLog.i(TAG, "startVideoRecord")
        if (!checkPermission(VIDEO_RECORD)) {
            TUIKitLog.i(TAG, "startVideoRecord checkPermission failed")
            return
        }
        val captureIntent = Intent(context, CameraActivity::class.java)
        captureIntent.putExtra(TUIKitConstants.CAMERA_TYPE, JCameraView.BUTTON_STATE_ONLY_RECORDER)
        CameraActivity.mCallBack = object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                val videoData = data as Intent?
                val imgPath = videoData?.getStringExtra(TUIKitConstants.CAMERA_IMAGE_PATH)
                val videoPath = videoData?.getStringExtra(TUIKitConstants.CAMERA_VIDEO_PATH)
                val imgWidth = videoData?.getIntExtra(TUIKitConstants.IMAGE_WIDTH, 0) ?: 0
                val imgHeight = videoData?.getIntExtra(TUIKitConstants.IMAGE_HEIGHT, 0) ?: 0
                val duration = videoData?.getLongExtra(TUIKitConstants.VIDEO_TIME, 0) ?: 0
                val msg = buildVideoMessage(imgPath, videoPath, imgWidth, imgHeight, duration)
                if (mMessageHandler != null) {
                    mMessageHandler?.sendMessage(msg)
                    hideSoftInput()
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {}
        }
        context.startActivity(captureIntent)
    }

    override fun startSendFile() {
        if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
            return
        }
        TUIKitLog.i(TAG, "startSendFile")
        if (!checkPermission(SEND_FILE)) {
            TUIKitLog.i(TAG, "startSendFile checkPermission failed")
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        mInputMoreFragment?.setCallback(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                val info = buildFileMessage(data as Uri?)
                if (mMessageHandler != null) {
                    mMessageHandler?.sendMessage(info)
                    hideSoftInput()
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage(errMsg)
            }
        })
        mInputMoreFragment?.startActivityForResult(intent, InputMoreFragment.REQUEST_CODE_FILE)
    }

    override fun startSendRedPacket() {
        if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
            return
        }
        if (inputHelper != null) {
            inputHelper?.sendRedPacket()
        }
    }

    fun setChatInputHandler(handler: ChatInputHandler?) {
        mChatInputHandler = handler
    }

    fun setInputHelper(inputHelper: InputSendHelper?) {
        this.inputHelper = inputHelper
    }

    fun setMessageHandler(handler: MessageHandler?) {
        mMessageHandler = handler
    }

    override fun onClick(view: View) {
        TUIKitLog.i(TAG, "onClick id:" + view.id
                + "|voice_input_switch:" + R.id.voice_input_switch
                + "|face_btn:" + R.id.face_btn
                + "|more_btn:" + R.id.more_btn
                + "|send_btn:" + R.id.send_btn
                + "|mCurrentState:" + mCurrentState
                + "|mSendEnable:" + mSendEnable
                + "|mMoreInputEvent:" + mMoreInputEvent)
        if (view.id == R.id.voice_input_switch) {
            if (mCurrentState == STATE_FACE_INPUT || mCurrentState == STATE_ACTION_INPUT) {
                mCurrentState = STATE_VOICE_INPUT
                mInputMoreView?.visibility = View.GONE
                mEmojiInputButton?.setImageResource(R.drawable.action_face_selector)
            } else if (mCurrentState == STATE_SOFT_INPUT) {
                mCurrentState = STATE_VOICE_INPUT
            } else {
                mCurrentState = STATE_SOFT_INPUT
            }
            if (mCurrentState == STATE_VOICE_INPUT) {
                mAudioInputSwitchButton?.setImageResource(R.drawable.action_textinput_selector)
                mSendAudioButton?.visibility = View.VISIBLE
                inputText?.visibility = View.GONE
                hideSoftInput()
            } else {
                mAudioInputSwitchButton?.setImageResource(R.drawable.action_audio_selector)
                mSendAudioButton?.visibility = View.GONE
                inputText?.visibility = View.VISIBLE
                showSoftInput()
            }
        } else if (view.id == R.id.face_btn) {
            if (mCurrentState == STATE_VOICE_INPUT) {
                mCurrentState = STATE_NONE_INPUT
                mAudioInputSwitchButton?.setImageResource(R.drawable.action_audio_selector)
                mSendAudioButton?.visibility = View.GONE
                inputText?.visibility = View.VISIBLE
            }
            if (mCurrentState == STATE_FACE_INPUT) {
                mCurrentState = STATE_NONE_INPUT
                mInputMoreView?.visibility = View.GONE
                mEmojiInputButton?.setImageResource(R.drawable.action_face_selector)
                inputText?.visibility = View.VISIBLE
            } else {
                mCurrentState = STATE_FACE_INPUT
                mEmojiInputButton?.setImageResource(R.drawable.action_textinput_selector)
                showFaceViewGroup()
            }
        } else if (view.id == R.id.more_btn) { //若点击右边的“+”号按钮
            hideSoftInput()
            if (mMoreInputEvent is OnClickListener) {
                (mMoreInputEvent as OnClickListener).onClick(view)
            } else if (mMoreInputEvent is BaseInputFragment) {
                showCustomInputMoreFragment()
            } else {
                if (mCurrentState == STATE_ACTION_INPUT) {
                    mCurrentState = STATE_NONE_INPUT
                    //以下是zanhanding添加的代码，用于fix有时需要两次点击加号按钮才能呼出富文本选择布局的问题
//判断富文本选择布局是否已经被呼出，并反转相应的状态
                    if (mInputMoreView?.visibility == View.VISIBLE) {
                        mInputMoreView?.visibility = View.GONE
                    } else {
                        mInputMoreView?.visibility = View.VISIBLE
                    }
                    //以上是zanhanding添加的代码，用于fix有时需要两次点击加号按钮才能呼出富文本选择布局的问题
                } else {
                    showInputMoreLayout() //显示“更多”消息发送布局
                    mCurrentState = STATE_ACTION_INPUT
                    mAudioInputSwitchButton?.setImageResource(R.drawable.action_audio_selector)
                    mEmojiInputButton?.setImageResource(R.drawable.action_face_selector)
                    mSendAudioButton?.visibility = View.GONE
                    inputText?.visibility = View.VISIBLE
                }
            }
        } else if (view.id == R.id.send_btn) {
            if (sendCheckListener != null && true != sendCheckListener?.beforeMessageSendCheck(null)) {
                return
            }
            if (mSendEnable) {
                if (mMessageHandler != null) {
                    mMessageHandler?.sendMessage(buildTextMessage(inputText?.text.toString().trim()))
                }
                inputText?.setText("")
            }
        }
    }

    private fun showSoftInput() {
        TUIKitLog.i(TAG, "showSoftInput")
        hideInputMoreLayout()
        mAudioInputSwitchButton?.setImageResource(R.drawable.action_audio_selector)
        mEmojiInputButton?.setImageResource(R.drawable.ic_input_face_normal)
        inputText?.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputText, 0)
        if (mChatInputHandler != null) {
            postDelayed({ mChatInputHandler?.onInputAreaClick() }, 200)
        }
    }

    fun hideSoftInput() {
        TUIKitLog.i(TAG, "hideSoftInput")
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputText?.windowToken, 0)
        inputText?.clearFocus()
        mInputMoreView?.visibility = View.GONE
    }

    private fun showFaceViewGroup() {
        TUIKitLog.i(TAG, "showFaceViewGroup")
        if (mFragmentManager == null) {
            mFragmentManager = mActivity?.fragmentManager
        }
        if (mFaceFragment == null) {
            mFaceFragment = FaceFragment()
        }
        hideSoftInput()
        mInputMoreView?.visibility = View.VISIBLE
        inputText?.requestFocus()
        mFaceFragment?.setListener(object : FaceFragment.OnEmojiClickListener {
            override fun onEmojiDelete() {
                inputText?.let {
                    val index: Int = it.selectionStart ?: 0
                    val editable: Editable = it.text
                    var isFace = false
                    if (index <= 0) {
                        return
                    }
                    if (editable[index - 1] == ']') {
                        for (i in index - 2 downTo 0) {
                            if (editable[i] == '[') {
                                val faceChar = editable.subSequence(i, index).toString()
                                if (isFaceChar(faceChar)) {
                                    editable.delete(i, index)
                                    isFace = true
                                }
                                break
                            }
                        }
                    }
                    if (!isFace) {
                        editable.delete(index - 1, index)
                    }
                }
            }

            override fun onEmojiClick(emoji: Emoji?) {
                inputText?.let {
                    val index: Int = it.selectionStart
                    val editable: Editable = it.text
                    editable.insert(index, emoji?.filter)
                    handlerEmojiText(it, editable.toString())
                }
            }

            override fun onCustomFaceClick(groupIndex: Int, emoji: Emoji?) {
                mMessageHandler?.sendMessage(buildCustomFaceMessage(groupIndex, emoji?.filter))
            }
        })
        mFragmentManager?.beginTransaction()?.replace(R.id.more_groups, mFaceFragment)?.commitAllowingStateLoss()
        if (mChatInputHandler != null) {
            postDelayed({ mChatInputHandler?.onInputAreaClick() }, 100)
        }
    }

    private fun showCustomInputMoreFragment() {
        TUIKitLog.i(TAG, "showCustomInputMoreFragment")
        if (mFragmentManager == null) {
            mFragmentManager = mActivity?.fragmentManager
        }
        val fragment = mMoreInputEvent as BaseInputFragment
        hideSoftInput()
        mInputMoreView?.visibility = View.VISIBLE
        mFragmentManager?.beginTransaction()?.replace(R.id.more_groups, fragment)?.commitAllowingStateLoss()
        if (mChatInputHandler != null) {
            postDelayed({ mChatInputHandler?.onInputAreaClick() }, 100)
        }
    }

    private fun showInputMoreLayout() {
        TUIKitLog.i(TAG, "showInputMoreLayout")
        if (mFragmentManager == null) {
            mFragmentManager = mActivity?.fragmentManager
        }
        if (mInputMoreFragment == null) {
            mInputMoreFragment = InputMoreFragment()
        }
        assembleActions()
        mInputMoreFragment?.setActions(mInputMoreActionList)
        hideSoftInput()
        mInputMoreView?.visibility = View.VISIBLE
        mFragmentManager?.beginTransaction()?.replace(R.id.more_groups, mInputMoreFragment)?.commitAllowingStateLoss()
        if (mChatInputHandler != null) {
            postDelayed({ mChatInputHandler?.onInputAreaClick() }, 100)
        }
    }

    private fun hideInputMoreLayout() {
        mInputMoreView?.visibility = View.GONE
    }

    private fun recordComplete(success: Boolean) {
        val duration: Int = AudioPlayer.instance.duration
        TUIKitLog.i(TAG, "recordComplete duration:$duration")
        if (mChatInputHandler != null) {
            if (!success || duration == 0) {
                mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_FAILED)
                return
            }
            if (mAudioCancel) {
                mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_CANCEL)
                return
            }
            if (duration < 1000) {
                mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_TOO_SHORT)
                return
            }
            mChatInputHandler?.onRecordStatusChanged(ChatInputHandler.RECORD_STOP)
        }
        if (mMessageHandler != null && success) {
            mMessageHandler?.sendMessage(buildAudioMessage(AudioPlayer.instance.path, duration))
        }
    }

    fun sendCustomMessage(customMessage: String?) {
        if (mMessageHandler != null && !TextUtils.isEmpty(customMessage)) {
            mMessageHandler?.sendMessage(buildCustomMessage(customMessage!!))
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        mInputContent = s.toString()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        if (TextUtils.isEmpty(s.toString().trim { it <= ' ' })) {
            mSendEnable = false
            showSendTextButton(View.GONE)
            showMoreInputButton(View.VISIBLE)
        } else {
            mSendEnable = true
            showSendTextButton(View.VISIBLE)
            showMoreInputButton(View.GONE)
            if (mLastMsgLineCount != inputText?.lineCount) {
                mLastMsgLineCount = inputText?.getLineCount() ?: 0
                if (mChatInputHandler != null) {
                    mChatInputHandler?.onInputAreaClick()
                }
            }
            if (!TextUtils.equals(mInputContent, inputText?.text.toString())) {
                handlerEmojiText(inputText, inputText?.text.toString())
            }
        }
    }

    fun setInputHint(inputHint: String?) {
        inputText?.hint = inputHint ?: ""
    }

    interface MessageHandler {
        fun sendMessage(msg: MessageInfo?)
    }

    interface ChatInputHandler {
        fun onInputAreaClick()
        fun onRecordStatusChanged(status: Int)

        companion object {
            const val RECORD_START = 1
            const val RECORD_STOP = 2
            const val RECORD_CANCEL = 3
            const val RECORD_TOO_SHORT = 4
            const val RECORD_FAILED = 5
        }
    }

    interface InputSendHelper {
        fun sendRedPacket()
    }
}