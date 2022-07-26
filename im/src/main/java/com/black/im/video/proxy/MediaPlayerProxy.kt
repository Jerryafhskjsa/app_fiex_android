package com.black.im.video.proxy

import android.content.Context
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import com.black.im.util.TUIKitLog
import java.io.IOException

class MediaPlayerProxy : IPlayer {
    private var mMediaPlayer: IPlayer? = null
    override fun setOnPreparedListener(l: IPlayer.OnPreparedListener?) {
        mMediaPlayer?.setOnPreparedListener(l)
    }

    override fun setOnErrorListener(l: IPlayer.OnErrorListener?) {
        mMediaPlayer?.setOnErrorListener(l)
    }

    override fun setOnCompletionListener(l: IPlayer.OnCompletionListener?) {
        mMediaPlayer?.setOnCompletionListener(l)
    }

    override fun setOnVideoSizeChangedListener(l: IPlayer.OnVideoSizeChangedListener?) {
        mMediaPlayer?.setOnVideoSizeChangedListener(l)
    }

    override fun setOnInfoListener(l: IPlayer.OnInfoListener?) {
        mMediaPlayer?.setOnInfoListener(l)
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        mMediaPlayer?.setDisplay(sh)
    }

    override fun setSurface(sh: Surface?) {
        mMediaPlayer?.setSurface(sh)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context?, uri: Uri?) {
        mMediaPlayer?.setDataSource(context, uri)
    }

    override fun prepareAsync() {
        mMediaPlayer?.prepareAsync()
    }

    override fun release() {
        mMediaPlayer?.release()
    }

    override fun start() {
        mMediaPlayer?.start()
    }

    override fun stop() {
        mMediaPlayer?.stop()
    }

    override fun pause() {
        mMediaPlayer?.pause()
    }

    override val isPlaying: Boolean
        get() = mMediaPlayer?.isPlaying ?: false

    override val videoWidth: Int
        get() = mMediaPlayer?.videoWidth ?: 0

    override val videoHeight: Int
        get() = mMediaPlayer?.videoHeight ?: 0

    companion object {
        private val TAG = MediaPlayerProxy::class.java.simpleName
    }

    init {
        mMediaPlayer = try {
            Class.forName("tv.danmaku.ijk.media.player.IjkMediaPlayer").newInstance()
            IjkMediaPlayerWrapper()
        } catch (e: Exception) {
            SystemMediaPlayerWrapper()
        }
        TUIKitLog.i(TAG, "use mMediaPlayer: $mMediaPlayer")
    }
}