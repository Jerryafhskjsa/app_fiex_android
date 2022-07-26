package com.black.im.video.util

import android.hardware.Camera
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.black.im.util.TUIKitLog

object CheckPermission {
    const val STATE_RECORDING = -1
    const val STATE_NO_PERMISSION = -2
    const val STATE_SUCCESS = 1
    private val TAG = CheckPermission::class.java.simpleName//检测是否可以获取录音结果//6.0以下机型都会返回此状态，故使用时需要判断bulid版本
    //检测是否在录音中
    //检测是否可以进入初始化状态
    /**
     * 用于检测是否具有录音权限
     *
     * @return
     */
    val recordState: Int
        get() {
            val minBuffer = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            var audioRecord: AudioRecord? = AudioRecord(MediaRecorder.AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBuffer * 100)
            val point = ShortArray(minBuffer)
            var readSize = 0
            try {
                audioRecord!!.startRecording() //检测是否可以进入初始化状态
            } catch (e: Exception) {
                if (audioRecord != null) {
                    audioRecord.release()
                    audioRecord = null
                }
                return STATE_NO_PERMISSION
            }
            return if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) { //6.0以下机型都会返回此状态，故使用时需要判断bulid版本
                //检测是否在录音中
                if (audioRecord != null) {
                    audioRecord.stop()
                    audioRecord.release()
                    audioRecord = null
                    TUIKitLog.i(TAG, "录音机被占用")
                }
                STATE_RECORDING
            } else { //检测是否可以获取录音结果
                readSize = audioRecord.read(point, 0, point.size)
                if (readSize <= 0) {
                    if (audioRecord != null) {
                        audioRecord.stop()
                        audioRecord.release()
                        audioRecord = null
                    }
                    TUIKitLog.i(TAG, "录音的结果为空")
                    STATE_NO_PERMISSION
                } else {
                    if (audioRecord != null) {
                        audioRecord.stop()
                        audioRecord.release()
                        audioRecord = null
                    }
                    STATE_SUCCESS
                }
            }
        }

    @Synchronized
    fun isCameraUseable(cameraID: Int): Boolean {
        var canUse = true
        var mCamera: Camera? = null
        try {
            mCamera = Camera.open(cameraID)
            // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
            val mParameters = mCamera.parameters
            mCamera.parameters = mParameters
        } catch (e: Exception) {
            canUse = false
        } finally {
            if (mCamera != null) {
                mCamera.release()
            } else {
                canUse = false
            }
            mCamera = null
        }
        return canUse
    }
}