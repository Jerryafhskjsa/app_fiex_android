package com.black.im.util

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.text.TextUtils

class AudioPlayer private constructor() {
    companion object {
        private val TAG = AudioPlayer::class.java.simpleName
        val instance = AudioPlayer()
        private val CURRENT_RECORD_FILE = TUIKitConstants.RECORD_DIR + "auto_"
        private const val MAGIC_NUMBER = 500
        private const val MIN_RECORD_DURATION = 1000
    }

    private var mRecordCallback: Callback? = null
    private var mPlayCallback: Callback? = null
    var path: String? = null
        private set
    private var mPlayer: MediaPlayer? = null
    private var mRecorder: MediaRecorder? = null
    private val mHandler: Handler = Handler()

    fun startRecord(callback: Callback?) {
        mRecordCallback = callback
        try {
            path = CURRENT_RECORD_FILE + System.currentTimeMillis() + ".m4a"
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            // 使用mp4容器并且后缀改为.m4a，来兼容小程序的播放
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mRecorder!!.setOutputFile(path)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mRecorder!!.prepare()
            mRecorder!!.start()
            // 最大录制时间之后需要停止录制
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed({
                stopInternalRecord()
                onRecordCompleted(true)
                mRecordCallback = null
                ToastUtil.toastShortMessage("已达到最大语音长度")
            }, (TUIKit.configs.generalConfig?.audioRecordMaxTime ?: 0) * 1000.toLong())
        } catch (e: Exception) {
            TUIKitLog.w(TAG, "startRecord failed", e)
            stopInternalRecord()
            onRecordCompleted(false)
        }
    }

    fun stopRecord() {
        stopInternalRecord()
        onRecordCompleted(true)
        mRecordCallback = null
    }

    private fun stopInternalRecord() {
        mHandler.removeCallbacksAndMessages(null)
        if (mRecorder == null) {
            return
        }
        mRecorder!!.release()
        mRecorder = null
    }

    fun startPlay(filePath: String?, callback: Callback?) {
        path = filePath
        mPlayCallback = callback
        try {
            mPlayer = MediaPlayer()
            mPlayer!!.setDataSource(filePath)
            mPlayer!!.setOnCompletionListener {
                stopInternalPlay()
                onPlayCompleted(true)
            }
            mPlayer!!.prepare()
            mPlayer!!.start()
        } catch (e: Exception) {
            TUIKitLog.w(TAG, "startPlay failed", e)
            ToastUtil.toastLongMessage("语音文件已损坏或不存在")
            stopInternalPlay()
            onPlayCompleted(false)
        }
    }

    fun stopPlay() {
        stopInternalPlay()
        onPlayCompleted(false)
        mPlayCallback = null
    }

    private fun stopInternalPlay() {
        if (mPlayer == null) {
            return
        }
        mPlayer!!.release()
        mPlayer = null
    }

    val isPlaying: Boolean
        get() = mPlayer != null && mPlayer!!.isPlaying

    private fun onPlayCompleted(success: Boolean) {
        if (mPlayCallback != null) {
            mPlayCallback!!.onCompletion(success)
        }
        mPlayer = null
    }

    private fun onRecordCompleted(success: Boolean) {
        if (mRecordCallback != null) {
            mRecordCallback!!.onCompletion(success)
        }
        mRecorder = null
    }// 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果

    // 通过初始化播放器的方式来获取真实的音频长度
    val duration: Int
        get() {
            if (TextUtils.isEmpty(path)) {
                return 0
            }
            var duration = 0
            // 通过初始化播放器的方式来获取真实的音频长度
            try {
                val mp = MediaPlayer()
                mp.setDataSource(path)
                mp.prepare()
                duration = mp.duration
                // 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果
                duration = if (duration < MIN_RECORD_DURATION) {
                    0
                } else {
                    duration + MAGIC_NUMBER
                }
            } catch (e: Exception) {
                TUIKitLog.w(TAG, "getDuration failed", e)
            }
            if (duration < 0) {
                duration = 0
            }
            return duration
        }

    interface Callback {
        fun onCompletion(success: Boolean?)
    }
}