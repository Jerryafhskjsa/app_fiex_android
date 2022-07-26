package com.black.im.widget

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.black.im.R
import com.black.im.fragment.BaseInputFragment
import com.black.im.interfaces.IInputLayout
import com.black.im.model.InputMoreActionUnit
import com.black.im.util.TUIKitLog
import com.black.im.widget.ChatLayoutUI.SendCheckListener
import java.util.*

abstract class InputLayoutUI : LinearLayout, IInputLayout {
    companion object {
        @JvmStatic
        protected val CAPTURE = 1
        @JvmStatic
        protected val AUDIO_RECORD = 2
        @JvmStatic
        protected val VIDEO_RECORD = 3
        @JvmStatic
        protected val SEND_PHOTO = 4
        @JvmStatic
        protected val SEND_FILE = 5
        private val TAG = InputLayoutUI::class.java.simpleName
    }

    protected var mAudioInputSwitchButton: ImageView? = null
    protected var sendCheckListener: SendCheckListener? = null
        private set
    /**
     * 语音/文字切换输入控件
     */
    protected var mAudioInputDisable = false
    /**
     * 表情按钮
     */
    protected var mEmojiInputButton: ImageView? = null
    protected var mEmojiInputDisable = false
    /**
     * 更多按钮
     */
    protected var mMoreInputButton: ImageView? = null
    protected var mMoreInputEvent: Any? = null
    protected var mMoreInputDisable = false
    /**
     * 消息发送按钮
     */
    protected var mSendTextButton: Button? = null
    /**
     * 语音长按按钮
     */
    protected var mSendAudioButton: Button? = null
    /**
     * 文本输入框
     */
    override var inputText: EditText? = null
        protected set
    protected var mActivity: Activity? = null
    protected var mInputMoreLayout: View? = null
    //    protected ShortcutArea mShortcutArea;
    protected var mInputMoreView: View? = null
    protected var mInputMoreActionList: MutableList<InputMoreActionUnit> = ArrayList()
    protected var mInputMoreCustomActionList: MutableList<InputMoreActionUnit> = ArrayList()
    private var mPermissionDialog: AlertDialog? = null
    private var mSendPhotoDisable = false
    private var mCaptureDisable = false
    private var mVideoRecordDisable = false
    private var mSendFileDisable = false
    private var mSendRedPacketDisable = false

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
        mActivity = context as Activity
        View.inflate(mActivity, R.layout.chat_input_layout, this)
        //        mShortcutArea = findViewById(R.id.shortcut_area);
        mInputMoreView = findViewById(R.id.more_groups)
        mSendAudioButton = findViewById(R.id.chat_voice_input)
        mAudioInputSwitchButton = findViewById(R.id.voice_input_switch)
        mEmojiInputButton = findViewById(R.id.face_btn)
        mMoreInputButton = findViewById(R.id.more_btn)
        mSendTextButton = findViewById(R.id.send_btn)
        inputText = findViewById(R.id.chat_message_input)
        // 子类实现所有的事件处理
        init()
    }

    open fun setSendCheckListener(sendCheckListener: SendCheckListener?) {
        this.sendCheckListener = sendCheckListener
    }

    protected fun assembleActions() {
        mInputMoreActionList.clear()
        var action = InputMoreActionUnit()
        if (!mCaptureDisable) {
            action = InputMoreActionUnit()
            action.iconResId = R.drawable.ic_more_camera
            action.titleId = R.string.im_photo
            action.onClickListener = OnClickListener { startCapture() }
            mInputMoreActionList.add(action)
        }
        if (!mSendPhotoDisable) {
            action = InputMoreActionUnit()
            action.iconResId = R.drawable.ic_more_picture
            action.titleId = R.string.im_pic
            action.onClickListener = OnClickListener { startSendPhoto() }
            mInputMoreActionList.add(action)
        }
        if (!mVideoRecordDisable) {
            action = InputMoreActionUnit()
            action.iconResId = R.drawable.ic_more_video
            action.titleId = R.string.im_video
            action.onClickListener = OnClickListener { startVideoRecord() }
            mInputMoreActionList.add(action)
        }
        if (!mSendFileDisable) {
            action = InputMoreActionUnit()
            action.iconResId = R.drawable.ic_more_file
            action.titleId = R.string.im_file
            action.onClickListener = OnClickListener { startSendFile() }
            mInputMoreActionList.add(action)
        }
        if (!mSendRedPacketDisable) {
            action = InputMoreActionUnit()
            action.iconResId = R.drawable.ic_more_red_packet
            action.titleId = R.string.im_red_packet
            action.onClickListener = OnClickListener { startSendRedPacket() }
            mInputMoreActionList.add(action)
        }
        mInputMoreActionList.addAll(mInputMoreCustomActionList)
    }

    protected fun checkPermission(context: Context?, permission: String): Boolean {
        TUIKitLog.i(TAG, "checkPermission permission:" + permission + "|sdk:" + Build.VERSION.SDK_INT)
        var flag = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = ActivityCompat.checkSelfPermission(context!!, permission)
            if (PackageManager.PERMISSION_GRANTED != result) {
                showPermissionDialog()
                flag = false
            }
        }
        return flag
    }

    protected fun checkPermission(type: Int): Boolean {
        if (!checkPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return false
        }
        if (!checkPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return false
        }
        if (type == SEND_FILE || type == SEND_PHOTO) {
            return true
        } else if (type == CAPTURE) {
            return checkPermission(mActivity, Manifest.permission.CAMERA)
        } else if (type == AUDIO_RECORD) {
            return checkPermission(mActivity, Manifest.permission.RECORD_AUDIO)
        } else if (type == VIDEO_RECORD) {
            return (checkPermission(mActivity, Manifest.permission.CAMERA)
                    && checkPermission(mActivity, Manifest.permission.RECORD_AUDIO))
        }
        return true
    }

    private fun showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = AlertDialog.Builder(mActivity)
                    .setMessage("使用该功能，需要开启权限，鉴于您禁用相关权限，请手动设置开启权限")
                    .setPositiveButton("设置") { dialog, which ->
                        cancelPermissionDialog()
                        val packageURI = Uri.parse("package:" + mActivity?.packageName)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                        mActivity?.startActivity(intent)
                    }
                    .setNegativeButton("取消") { _, which ->
                        //关闭页面或者做其他操作
                        cancelPermissionDialog()
                    }
                    .create()
        }
        mPermissionDialog?.show()
    }

    private fun cancelPermissionDialog() {
        mPermissionDialog?.cancel()
    }

    protected abstract fun init()
    protected abstract fun startSendPhoto()
    protected abstract fun startCapture()
    protected abstract fun startVideoRecord()
    protected abstract fun startSendFile()
    protected abstract fun startSendRedPacket()
    override fun disableAudioInput(disable: Boolean) {
        mAudioInputDisable = disable
        if (disable) {
            mAudioInputSwitchButton?.visibility = View.GONE
        } else {
            mAudioInputSwitchButton?.visibility = View.VISIBLE
        }
    }

    override fun disableEmojiInput(disable: Boolean) {
        mEmojiInputDisable = disable
        if (disable) {
            mEmojiInputButton?.visibility = View.GONE
        } else {
            mEmojiInputButton?.visibility = View.VISIBLE
        }
    }

    override fun disableMoreInput(disable: Boolean) {
        mMoreInputDisable = disable
        if (disable) {
            mMoreInputButton?.visibility = View.GONE
            mSendTextButton?.visibility = View.VISIBLE
        } else {
            mMoreInputButton?.visibility = View.VISIBLE
            mSendTextButton?.visibility = View.GONE
        }
    }

    override fun replaceMoreInput(fragment: BaseInputFragment?) {
        mMoreInputEvent = fragment
    }

    override fun replaceMoreInput(listener: OnClickListener?) {
        mMoreInputEvent = listener
    }

    override fun disableSendPhotoAction(disable: Boolean) {
        mSendPhotoDisable = disable
    }

    override fun disableCaptureAction(disable: Boolean) {
        mCaptureDisable = disable
    }

    override fun disableVideoRecordAction(disable: Boolean) {
        mVideoRecordDisable = disable
    }

    override fun disableSendFileAction(disable: Boolean) {
        mSendFileDisable = disable
    }

    override fun disableSendRedPacketAction(disable: Boolean) {
        mSendRedPacketDisable = disable
    }

    override fun addAction(action: InputMoreActionUnit) {
        mInputMoreCustomActionList.add(action)
    }

    protected fun showMoreInputButton(visibility: Int) {
        if (mMoreInputDisable) {
            return
        }
        mMoreInputButton?.visibility = visibility
    }

    protected fun showSendTextButton(visibility: Int) {
        if (mMoreInputDisable) {
            mSendTextButton?.visibility = View.VISIBLE
        } else {
            mSendTextButton?.visibility = visibility
        }
    }

    protected fun showEmojiInputButton(visibility: Int) {
        if (mEmojiInputDisable) {
            return
        }
        mEmojiInputButton?.visibility = visibility
    }
}