package com.black.im.video

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.black.im.util.ScreenUtil
import com.black.im.util.TUIKitLog
import com.black.im.video.proxy.IPlayer
import com.black.im.video.proxy.MediaPlayerProxy

class UIKitVideoView : TextureView {
    companion object {
        private val TAG = UIKitVideoView::class.java.simpleName
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
        private const val STATE_STOPPED = 6
    }

    private var mCurrentState = STATE_IDLE
    private var mContext: Context? = null
    private var mSurface: Surface? = null
    private var mMediaPlayer: MediaPlayerProxy? = null
    private var mUri: Uri? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mVideoRotationDegree = 0
    private var mOutOnPreparedListener: IPlayer.OnPreparedListener? = null
    private var mOutOnErrorListener: IPlayer.OnErrorListener? = null
    private var mOutOnCompletionListener: IPlayer.OnCompletionListener? = null
    private val mOnPreparedListener: IPlayer.OnPreparedListener = object : IPlayer.OnPreparedListener {
        override fun onPrepared(mp: IPlayer?) {
            mCurrentState = STATE_PREPARED
            mVideoHeight = mp!!.videoHeight
            mVideoWidth = mp.videoWidth
            TUIKitLog.i(TAG, "onPrepared mVideoWidth: " + mVideoWidth
                    + " mVideoHeight: " + mVideoHeight
                    + " mVideoRotationDegree: " + mVideoRotationDegree)
            if (mOutOnPreparedListener != null) {
                mOutOnPreparedListener!!.onPrepared(mp)
            }
        }
    }
    private val mOnErrorListener: IPlayer.OnErrorListener = object : IPlayer.OnErrorListener {
        override fun onError(mp: IPlayer?, what: Int, extra: Int): Boolean {
            TUIKitLog.w(TAG, "onError: what/extra: $what/$extra")
            mCurrentState = STATE_ERROR
            stop_l()
            if (mOutOnErrorListener != null) {
                mOutOnErrorListener!!.onError(mp, what, extra)
            }
            return true
        }
    }
    private val mOnInfoListener: IPlayer.OnInfoListener = object : IPlayer.OnInfoListener {
        override fun onInfo(mp: IPlayer?, what: Int, extra: Int) {
            TUIKitLog.w(TAG, "onInfo: what/extra: $what/$extra")
            if (what == 10001) { // IJK: MEDIA_INFO_VIDEO_ROTATION_CHANGED
// 有些视频拍摄的时候有角度，需要做旋转，默认ijk是不会做的，这里自己实现
                mVideoRotationDegree = extra
                rotation = mVideoRotationDegree.toFloat()
                requestLayout()
            }
        }
    }
    private val mOnCompletionListener: IPlayer.OnCompletionListener = object : IPlayer.OnCompletionListener {
        override fun onCompletion(mp: IPlayer?) {
            TUIKitLog.i(TAG, "onCompletion")
            mCurrentState = STATE_PLAYBACK_COMPLETED
            if (mOutOnCompletionListener != null) {
                mOutOnCompletionListener!!.onCompletion(mp)
            }
        }
    }
    private val mOnVideoSizeChangedListener: IPlayer.OnVideoSizeChangedListener = object : IPlayer.OnVideoSizeChangedListener {
        override fun onVideoSizeChanged(mp: IPlayer?, width: Int, height: Int) { // TUIKitLog.i(TAG, "onVideoSizeChanged width: " + width + " height: " + height);
        }
    }
    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            TUIKitLog.i(TAG, "onSurfaceTextureAvailable")
            mSurface = Surface(surface)
            openVideo()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            TUIKitLog.i(TAG, "onSurfaceTextureSizeChanged")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            TUIKitLog.i(TAG, "onSurfaceTextureDestroyed")
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) { // TUIKitLog.i(TAG,"onSurfaceTextureUpdated");
        }
    }

    constructor(context: Context) : super(context) {
        initVideoView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initVideoView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initVideoView(context)
    }

    private fun initVideoView(context: Context) {
        TUIKitLog.i(TAG, "initVideoView")
        mContext = context
        surfaceTextureListener = mSurfaceTextureListener
        mCurrentState = STATE_IDLE
    }

    fun setOnPreparedListener(l: IPlayer.OnPreparedListener?) {
        mOutOnPreparedListener = l
    }

    fun setOnErrorListener(l: IPlayer.OnErrorListener?) {
        mOutOnErrorListener = l
    }

    fun setOnCompletionListener(l: IPlayer.OnCompletionListener?) {
        mOutOnCompletionListener = l
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) { //  TUIKitLog.i(TAG, "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
//        + MeasureSpec.toString(heightMeasureSpec) + ")"
//        + " mVideoWidth: " + mVideoWidth
//        + " mVideoHeight: " + mVideoHeight);
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) { // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) { //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) { //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) { // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) { // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) { // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) { // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else { // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) { // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) { // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else { // no size yet, just adopt the given spec sizes
        }
        TUIKitLog.i(TAG, "onMeasure width: $width height: $height rotation degree: $mVideoRotationDegree")
        setMeasuredDimension(width, height)
        if ((mVideoRotationDegree + 180) % 180 != 0) { // 画面旋转之后需要缩放，而且旋转之后宽高的计算都要换为高宽。
            val size = ScreenUtil.scaledSize(widthSpecSize, heightSpecSize, height, width)
            TUIKitLog.i(TAG, "onMeasure scaled width: " + size[0] + " height: " + size[1])
            scaleX = size[0] / height.toFloat()
            scaleY = size[1] / width.toFloat()
        }
    }

    fun setVideoURI(uri: Uri?) {
        mUri = uri
        openVideo()
    }

    private fun openVideo() {
        TUIKitLog.i(TAG, "openVideo: mUri: " + mUri!!.path + " mSurface: " + mSurface)
        if (mSurface == null) {
            return
        }
        stop_l()
        try {
            mMediaPlayer = MediaPlayerProxy()
            mMediaPlayer!!.setOnPreparedListener(mOnPreparedListener)
            mMediaPlayer!!.setOnCompletionListener(mOnCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mOnErrorListener)
            mMediaPlayer!!.setOnInfoListener(mOnInfoListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
            mMediaPlayer!!.setSurface(mSurface)
            mMediaPlayer!!.setDataSource(context, mUri)
            mMediaPlayer!!.prepareAsync()
            mCurrentState = STATE_PREPARING
        } catch (ex: Exception) {
            TUIKitLog.w(TAG, ex.message)
            mCurrentState = STATE_ERROR
        }
    }

    fun start(): Boolean {
        TUIKitLog.i(TAG, "start mCurrentState:$mCurrentState")
        if (mMediaPlayer != null) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
        }
        return true
    }

    fun stop(): Boolean {
        TUIKitLog.i(TAG, "stop mCurrentState:$mCurrentState")
        stop_l()
        return true
    }

    fun pause(): Boolean {
        TUIKitLog.i(TAG, "pause mCurrentState:$mCurrentState")
        if (mMediaPlayer != null) {
            mMediaPlayer!!.pause()
            mCurrentState = STATE_PAUSED
        }
        return true
    }

    fun stop_l() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
        }
    }

    val isPlaying: Boolean
        get() = if (mMediaPlayer != null) {
            mMediaPlayer!!.isPlaying
        } else false

    override fun setBackgroundDrawable(background: Drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && background != null) {
            super.setBackgroundDrawable(background)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }
}