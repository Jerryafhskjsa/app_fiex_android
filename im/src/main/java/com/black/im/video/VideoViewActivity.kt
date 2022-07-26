package com.black.im.video

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.black.im.R
import com.black.im.util.ImageUtil
import com.black.im.util.ScreenUtil
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.video.proxy.IPlayer

class VideoViewActivity : Activity() {
    companion object {
        private val TAG = VideoViewActivity::class.java.simpleName
    }

    private var mVideoView: UIKitVideoView? = null
    private var videoWidth = 0
    private var videoHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        TUIKitLog.i(TAG, "onCreate start")
        super.onCreate(savedInstanceState)
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //去除状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_video_view)
        mVideoView = findViewById(R.id.video_play_view)
        val imagePath = intent.getStringExtra(TUIKitConstants.CAMERA_IMAGE_PATH)
        val videoUri = intent.getParcelableExtra<Uri>(TUIKitConstants.CAMERA_VIDEO_PATH)
        val firstFrame = ImageUtil.getBitmapFormPath(imagePath)
        if (firstFrame != null) {
            videoWidth = firstFrame.width
            videoHeight = firstFrame.height
            updateVideoView()
        }
        mVideoView?.setVideoURI(videoUri)
        mVideoView?.setOnPreparedListener(object : IPlayer.OnPreparedListener {
            override fun onPrepared(mediaPlayer: IPlayer?) {
                mVideoView?.start()
            }
        })
        mVideoView?.setOnClickListener(View.OnClickListener {
            if (true == mVideoView?.isPlaying) {
                mVideoView?.pause()
            } else {
                mVideoView?.start()
            }
        })
        findViewById<View>(R.id.video_view_back).setOnClickListener {
            mVideoView?.stop()
            finish()
        }
        TUIKitLog.i(TAG, "onCreate end")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        TUIKitLog.i(TAG, "onConfigurationChanged start")
        super.onConfigurationChanged(newConfig)
        updateVideoView()
        TUIKitLog.i(TAG, "onConfigurationChanged end")
    }

    private fun updateVideoView() {
        TUIKitLog.i(TAG, "updateVideoView videoWidth: $videoWidth videoHeight: $videoHeight")
        if (videoWidth <= 0 && videoHeight <= 0) {
            return
        }
        var isLandscape = true
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false
        }
        val deviceWidth: Int
        val deviceHeight: Int
        if (isLandscape) {
            deviceWidth = Math.max(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this))
            deviceHeight = Math.min(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this))
        } else {
            deviceWidth = Math.min(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this))
            deviceHeight = Math.max(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this))
        }
        val scaledSize = ScreenUtil.scaledSize(deviceWidth, deviceHeight, videoWidth, videoHeight)
        TUIKitLog.i(TAG, "scaled width: " + scaledSize[0] + " height: " + scaledSize[1])
        val params = mVideoView!!.layoutParams
        params.width = scaledSize[0]
        params.height = scaledSize[1]
        mVideoView!!.layoutParams = params
    }

    override fun onStop() {
        TUIKitLog.i(TAG, "onStop")
        super.onStop()
        if (mVideoView != null) {
            mVideoView!!.stop()
        }
    }
}