package com.black.im.adapter.holders

import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.AudioPlayer
import com.black.im.util.ScreenUtil.getPxByDp
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.widget.MessageLayoutUI
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMSoundElem
import java.io.File

class MessageAudioHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView, properties) {
    companion object {
        private val AUDIO_MIN_WIDTH = getPxByDp(60)
        private val AUDIO_MAX_WIDTH = getPxByDp(250)
        private const val UNREAD = 0
        private const val READ = 1
    }

    private var audioTimeText: TextView? = null
    private var audioPlayImage: ImageView? = null
    private var audioContentView: LinearLayout? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_audio
    }

    override fun initVariableViews() {
        audioTimeText = rootView.findViewById(R.id.audio_time_tv)
        audioPlayImage = rootView.findViewById(R.id.audio_play_iv)
        audioContentView = rootView.findViewById(R.id.audio_content_ll)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_VERTICAL)
        if (true == msg?.isSelf) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            params.rightMargin = 25
            audioPlayImage?.setImageResource(R.drawable.ic_voice_msg_right_playing_3)
            audioContentView?.removeView(audioPlayImage)
            audioContentView?.addView(audioPlayImage)
            unreadAudioText.visibility = View.GONE
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            params.leftMargin = 25
            // TODO 图标不对
            audioPlayImage?.setImageResource(R.drawable.ic_voice_msg_left_playing_3)
            audioContentView?.removeView(audioPlayImage)
            audioContentView?.addView(audioPlayImage, 0)
            if (msg?.customInt == UNREAD) {
                val unreadParams = isReadText.layoutParams as LinearLayout.LayoutParams
                unreadParams.gravity = Gravity.CENTER_VERTICAL
                unreadParams.leftMargin = 10
                unreadAudioText.visibility = View.VISIBLE
                unreadAudioText.layoutParams = unreadParams
            } else {
                unreadAudioText.visibility = View.GONE
            }
        }
        audioContentView?.layoutParams = params
        val elem = msg?.element as? TIMSoundElem ?: return
        var duration = elem.duration.toInt()
        if (duration == 0) {
            duration = 1
        }
        if (TextUtils.isEmpty(msg.dataPath)) {
            getSound(msg, elem)
        }
        val audioParams = msgContentFrame.layoutParams
        audioParams.width = AUDIO_MIN_WIDTH + getPxByDp(duration * 6)
        if (audioParams.width > AUDIO_MAX_WIDTH) {
            audioParams.width = AUDIO_MAX_WIDTH
        }
        msgContentFrame.layoutParams = audioParams
        audioTimeText?.text = "$duration''"
        msgContentFrame.setOnClickListener(View.OnClickListener {
            if (AudioPlayer.instance.isPlaying) {
                AudioPlayer.instance.stopPlay()
                return@OnClickListener
            }
            if (TextUtils.isEmpty(msg.dataPath)) {
                toastLongMessage("语音文件还未下载完成")
                return@OnClickListener
            }
            audioPlayImage?.setImageResource(if (msg.isSelf) R.drawable.play_voice_message_right else R.drawable.play_voice_message_left)
            val animationDrawable = audioPlayImage?.drawable as AnimationDrawable
            animationDrawable.start()
            msg.customInt = READ
            unreadAudioText.visibility = View.GONE
            AudioPlayer.instance.startPlay(msg.dataPath, object : AudioPlayer.Callback {
                override fun onCompletion(success: Boolean?) {
                    audioPlayImage?.post {
                        animationDrawable.stop()
                        audioPlayImage?.setImageResource(if (msg.isSelf) R.drawable.ic_voice_msg_right_playing_3 else R.drawable.ic_voice_msg_left_playing_3)
                    }
                }
            })
        })
    }

    private fun getSound(msgInfo: MessageInfo?, soundElemEle: TIMSoundElem) {
        val path = TUIKitConstants.RECORD_DOWNLOAD_DIR + soundElemEle.uuid
        val file = File(path)
        if (!file.exists()) {
            soundElemEle.getSoundToFile(path, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e("getSoundToFile failed code = ", "$code, info = $desc")
                }

                override fun onSuccess() {
                    msgInfo?.dataPath = path
                }
            })
        } else {
            msgInfo?.dataPath = path
        }
    }
}