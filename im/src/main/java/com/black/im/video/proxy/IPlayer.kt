package com.black.im.video.proxy

import android.content.Context
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import java.io.IOException

interface IPlayer {
    fun setOnPreparedListener(l: OnPreparedListener?)
    fun setOnErrorListener(l: OnErrorListener?)
    fun setOnCompletionListener(l: OnCompletionListener?)
    fun setOnVideoSizeChangedListener(l: OnVideoSizeChangedListener?)
    fun setOnInfoListener(l: OnInfoListener?)
    fun setDisplay(sh: SurfaceHolder?)
    fun setSurface(sh: Surface?)
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    fun setDataSource(context: Context?, uri: Uri?)

    fun prepareAsync()
    fun release()
    fun start()
    fun stop()
    fun pause()
    val isPlaying: Boolean
    val videoWidth: Int
    val videoHeight: Int

    interface OnPreparedListener {
        fun onPrepared(mp: IPlayer?)
    }

    interface OnErrorListener {
        fun onError(mp: IPlayer?, what: Int, extra: Int): Boolean
    }

    interface OnCompletionListener {
        fun onCompletion(mp: IPlayer?)
    }

    interface OnVideoSizeChangedListener {
        fun onVideoSizeChanged(mp: IPlayer?, width: Int, height: Int)
    }

    interface OnInfoListener {
        fun onInfo(mp: IPlayer?, what: Int, extra: Int)
    }
}