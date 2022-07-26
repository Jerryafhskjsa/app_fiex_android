package com.black.im.video.proxy

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import com.black.im.util.TUIKitLog
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class IjkMediaPlayerWrapper : IPlayer {
    private var mMediaPlayerClass: Class<*>? = null
    private var mMediaPlayerInstance: Any? = null
    override fun setOnPreparedListener(l: IPlayer.OnPreparedListener?) {
        invokeListener("OnPreparedListener", "setOnPreparedListener", l)
    }

    override fun setOnErrorListener(l: IPlayer.OnErrorListener?) {
        invokeListener("OnErrorListener", "setOnErrorListener", l)
    }

    override fun setOnCompletionListener(l: IPlayer.OnCompletionListener?) {
        invokeListener("OnCompletionListener", "setOnCompletionListener", l)
    }

    override fun setOnVideoSizeChangedListener(l: IPlayer.OnVideoSizeChangedListener?) {
        invokeListener("OnVideoSizeChangedListener", "setOnVideoSizeChangedListener", l)
    }

    override fun setOnInfoListener(l: IPlayer.OnInfoListener?) {
        invokeListener("OnInfoListener", "setOnInfoListener", l)
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        invoke("setDisplay", sh!!)
    }

    override fun setSurface(sh: Surface?) {
        invoke("setSurface", sh!!)
    }

    override fun setDataSource(context: Context?, uri: Uri?) {
        invoke("setDataSource", context!!, uri!!)
    }

    override fun prepareAsync() {
        invoke("prepareAsync")
    }

    override fun release() {
        invoke("release")
    }

    override fun start() {
        invoke("start")
    }

    override fun stop() {
        invoke("stop")
    }

    override fun pause() {
        invoke("pause")
    }

    override val isPlaying: Boolean
        get() = invoke("isPlaying") as Boolean

    override val videoWidth: Int
        get() = invoke("getVideoWidth") as Int

    override val videoHeight: Int
        get() = invoke("getVideoHeight") as Int

    private operator fun invoke(methodName: String, vararg args1: Any): Any? {
        var args: Array<out Any>? = args1
        try {
            var classes: Array<Class<*>?>? = null
            if (args != null && args.isNotEmpty()) {
                classes = arrayOfNulls<Class<*>?>(args.size)
                for (i in args.indices) {
                    classes[i] = args[i].javaClass
                    classes[i]?.let {
                        // setDataSource的参数不能用子类，必须要与方法签名一致
                        if (Context::class.java.isAssignableFrom(it)) {
                            classes[i] = Context::class.java
                        } else if (Uri::class.java.isAssignableFrom(it)) {
                            classes[i] = Uri::class.java
                        }
                    }
                }
            } else {
                args = null
            }
            val methodInstance = if (classes == null) null else mMediaPlayerClass?.getMethod(methodName, *classes)
            return methodInstance?.invoke(mMediaPlayerInstance, if (args == null) null else (args1))
        } catch (e: Exception) {
            TUIKitLog.e(TAG, "invoke failed: " + methodName + " error: " + e.cause)
        }
        return null
    }

    private fun invokeListener(className: String, methodName: String, outerListener: Any?) {
        try {
            val listenerClass = Class.forName("tv.danmaku.ijk.media.player.IMediaPlayer$$className")
            val method = mMediaPlayerClass!!.getMethod(methodName, listenerClass)
            val listenerHandler = ListenerHandler(outerListener!!)
            val listenerInstance = Proxy.newProxyInstance(
                    this.javaClass.classLoader, arrayOf(listenerClass),
                    listenerHandler
            )
            method.invoke(mMediaPlayerInstance, listenerInstance)
        } catch (e: Exception) {
            TUIKitLog.e(TAG, methodName + " failed: " + e.message)
        }
    }

    private inner class ListenerHandler(l: Any) : InvocationHandler {
        private val mListener: Any?
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
            if (mListener == null) {
                return false
            }
            if (mListener is IPlayer.OnInfoListener && TextUtils.equals("onInfo", method.name)) {
                if (args[1] as Int == 10001) {
                    TUIKitLog.i(TAG, "IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED")
                }
                mListener.onInfo(this@IjkMediaPlayerWrapper, args[1] as Int, args[2] as Int)
            } else if (mListener is IPlayer.OnVideoSizeChangedListener && TextUtils.equals("onVideoSizeChanged", method.name)) {
                TUIKitLog.i(TAG, "width: " + args[1] + " height: " + args[2]
                        + " sarNum: " + args[3] + " sarDen: " + args[4])
                mListener.onVideoSizeChanged(this@IjkMediaPlayerWrapper, args[1] as Int, args[2] as Int)
            } else if (mListener is IPlayer.OnCompletionListener && TextUtils.equals("onCompletion", method.name)) {
                mListener.onCompletion(this@IjkMediaPlayerWrapper)
            } else if (mListener is IPlayer.OnErrorListener && TextUtils.equals("onError", method.name)) {
                mListener.onError(this@IjkMediaPlayerWrapper, args[1] as Int, args[2] as Int)
            } else if (mListener is IPlayer.OnPreparedListener && TextUtils.equals("onPrepared", method.name)) {
                mListener.onPrepared(this@IjkMediaPlayerWrapper)
            }
            return false
        }

        init {
            mListener = l
        }
    }

    companion object {
        private val TAG = IjkMediaPlayerWrapper::class.java.simpleName
    }

    init {
        try {
            mMediaPlayerClass = Class.forName("tv.danmaku.ijk.media.player.IjkMediaPlayer")
            mMediaPlayerInstance = mMediaPlayerClass?.newInstance()
        } catch (e: Exception) {
            TUIKitLog.i(TAG, "no IjkMediaPlayer: " + e.message)
        }
    }
}