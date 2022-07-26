package com.black.im.video.proxy

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import java.io.IOException

class SystemMediaPlayerWrapper : IPlayer {
    private val mMediaPlayer: MediaPlayer = MediaPlayer()
    override fun setOnPreparedListener(l: IPlayer.OnPreparedListener?) {
        mMediaPlayer.setOnPreparedListener { l!!.onPrepared(this@SystemMediaPlayerWrapper) }
    }

    override fun setOnErrorListener(l: IPlayer.OnErrorListener?) {
        mMediaPlayer.setOnErrorListener { mp, what, extra -> l!!.onError(this@SystemMediaPlayerWrapper, what, extra) }
    }

    override fun setOnCompletionListener(l: IPlayer.OnCompletionListener?) {
        mMediaPlayer.setOnCompletionListener { l!!.onCompletion(this@SystemMediaPlayerWrapper) }
    }

    override fun setOnVideoSizeChangedListener(l: IPlayer.OnVideoSizeChangedListener?) {
        mMediaPlayer.setOnVideoSizeChangedListener { mp, width, height -> l!!.onVideoSizeChanged(this@SystemMediaPlayerWrapper, width, height) }
    }

    override fun setOnInfoListener(l: IPlayer.OnInfoListener?) {
        mMediaPlayer.setOnInfoListener { mp, what, extra ->
            l!!.onInfo(this@SystemMediaPlayerWrapper, what, extra)
            false
        }
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        mMediaPlayer.setDisplay(sh)
    }

    override fun setSurface(sh: Surface?) {
        mMediaPlayer.setSurface(sh)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context?, uri: Uri?) {
        mMediaPlayer.setDataSource(context, uri)
    }

    override fun prepareAsync() {
        mMediaPlayer.prepareAsync()
    }

    override fun release() {
        mMediaPlayer.release()
    }

    override fun start() {
        mMediaPlayer.start()
    }

    override fun stop() {
        mMediaPlayer.stop()
    }

    override fun pause() {
        mMediaPlayer.pause()
    }

    override val isPlaying: Boolean
        get() = mMediaPlayer.isPlaying

    override val videoWidth: Int
        get() = mMediaPlayer.videoWidth

    override val videoHeight: Int
        get() = mMediaPlayer.videoHeight

}