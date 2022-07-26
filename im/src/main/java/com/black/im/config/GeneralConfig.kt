package com.black.im.config

import com.tencent.imsdk.TIMLogLevel

/**
 * TUIKit的通用配置，比如可以设置日志打印、音视频录制时长等
 */
class GeneralConfig {
    companion object {
        private val TAG = GeneralConfig::class.java.simpleName
        const val DEFAULT_AUDIO_RECORD_MAX_TIME = 60
        const val DEFAULT_VIDEO_RECORD_MAX_TIME = 15
    }

    /**
     * 获取TUIKit缓存路径
     *
     * @return
     */
    var appCacheDir: String? = null
        private set
    /**
     * 获取录音最大时长
     *
     * @return
     */
    var audioRecordMaxTime = DEFAULT_AUDIO_RECORD_MAX_TIME
        private set
    /**
     * 获取录像最大时长
     *
     * @return
     */
    var videoRecordMaxTime = DEFAULT_VIDEO_RECORD_MAX_TIME
        private set
    /**
     * 获取打印的日志级别
     *
     * @return
     */
    /**
     * 设置打印的日志级别
     *
     * @param logLevel
     */
    var logLevel = TIMLogLevel.DEBUG
    /**
     * 获取是否打印日志
     *
     * @return
     */
    var isLogPrint = true
        private set

    /**
     * 设置是否打印日志
     *
     * @param enableLogPrint
     */
    fun enableLogPrint(enableLogPrint: Boolean) {
        isLogPrint = enableLogPrint
    }

    /**
     * 设置TUIKit缓存路径
     *
     * @param appCacheDir
     * @return
     */
    fun setAppCacheDir(appCacheDir: String?): GeneralConfig {
        this.appCacheDir = appCacheDir
        return this
    }

    /**
     * 录音最大时长
     *
     * @param audioRecordMaxTime
     * @return
     */
    fun setAudioRecordMaxTime(audioRecordMaxTime: Int): GeneralConfig {
        this.audioRecordMaxTime = audioRecordMaxTime
        return this
    }

    /**
     * 摄像最大时长
     *
     * @param videoRecordMaxTime
     * @return
     */
    fun setVideoRecordMaxTime(videoRecordMaxTime: Int): GeneralConfig {
        this.videoRecordMaxTime = videoRecordMaxTime
        return this
    }
}