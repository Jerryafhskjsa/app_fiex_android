package com.black.lib

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Process
import com.black.util.CommonUtil
import java.io.IOException

class SoundPlayer(private val context: Context) {
    companion object {
        private const val START = 1
        private const val PLAY = 2
        private const val SEND_SOUND_BUFFER_SIZE = 320
        private const val FREQUENCE = 8000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO
        private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    private val bufferSize: Int
    private var isPlaying = false
    private val playThread: HandlerThread
    private val playHandler: Handler
    private var track: AudioTrack?

    init {
        bufferSize = AudioRecord.getMinBufferSize(FREQUENCE, CHANNEL_CONFIG, AUDIO_ENCODING)
        // 实例AudioTrack
        track = AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCE, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM)
        playThread = HandlerThread("client_sound_play_thread", Process.THREAD_PRIORITY_BACKGROUND)
        playThread.start()
        playHandler = object : Handler(playThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    START -> {
                    }
                    PLAY -> {
                        val nBytes = msg.arg1
                        val pSlice = msg.obj as ByteArray
                        track?.write(pSlice, 0, nBytes)
                    }
                }
            }
        }
    }

    fun startPlay() {
        track?.play()
        isPlaying = true
    }

    fun stopPlay() {
        if (track?.state == AudioTrack.PLAYSTATE_PLAYING) {
            track?.stop()
        }
        isPlaying = false
    }

    fun playRaw(rawId: Int): SoundPlayer {
        val inputStream = context.resources.openRawResource(rawId)
        try {
            var readLength = 0
            do {
                val b = ByteArray(bufferSize)
                readLength = inputStream.read(b, 0, bufferSize)
                if (readLength > 0) {
                    track?.write(b, 0, readLength)
                    //playHandler.obtainMessage(PLAY, readLength, readLength, b).sendToTarget();
                }
            } while (readLength == -1)
        } catch (e: IOException) {
            CommonUtil.printError(context, e)
        }
        return this
    }

    fun release() {
        stopPlay()
        if (track != null) {
            track?.release()
            track = null
        }
        playThread.quit()
        playHandler.removeMessages(START)
        playHandler.removeMessages(PLAY)
    }
}