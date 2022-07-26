package com.black.im.video

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.black.im.R
import com.black.im.util.*
import com.black.im.video.listener.ClickListener
import com.black.im.video.listener.ErrorListener
import com.black.im.video.listener.JCameraListener
import com.black.im.video.util.DeviceUtil

class CameraActivity : Activity() {
    private var jCameraView: JCameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        TUIKitLog.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //去除状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_camera)
        jCameraView = findViewById(R.id.jcameraview)
        //设置视频保存路径
//jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        val state = intent.getIntExtra(TUIKitConstants.CAMERA_TYPE, JCameraView.BUTTON_STATE_BOTH)
        jCameraView?.setFeatures(state)
        if (state == JCameraView.BUTTON_STATE_ONLY_CAPTURE) {
            jCameraView?.setTip("点击拍照")
        } else if (state == JCameraView.BUTTON_STATE_ONLY_RECORDER) {
            jCameraView?.setTip("长按摄像")
        }
        jCameraView?.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE)
        jCameraView?.setErrorLisenter(object : ErrorListener {
            override fun onError() {
                //错误监听
                TUIKitLog.e(TAG, "camera error")
                val intent = Intent()
                setResult(103, intent)
                finish()
            }

            override fun AudioPermissionError() {
                ToastUtil.toastShortMessage("给点录音权限可以?")
            }
        })
        //JCameraView监听
        jCameraView?.setJCameraLisenter(object : JCameraListener {
            override fun captureSuccess(bitmap: Bitmap?) {
                //获取图片bitmap
                bitmap?.let {
                    val path = FileUtil.saveBitmap("JCamera", bitmap)
                    /* Intent intent = new Intent();
                    intent.putExtra(ILiveConstants.CAMERA_IMAGE_PATH, path);
                    setResult(-1, intent);*/if (mCallBack != null) {
                    mCallBack?.onSuccess(path)
                }
                    finish()
                }
            }

            override fun recordSuccess(url: String?, firstFrame: Bitmap?, duration: Long) {
                firstFrame?.let {
                    //获取视频路径
                    val path = FileUtil.saveBitmap("JCamera", firstFrame)
                    val intent = Intent()
                    intent.putExtra(TUIKitConstants.IMAGE_WIDTH, firstFrame.width)
                    intent.putExtra(TUIKitConstants.IMAGE_HEIGHT, firstFrame.height)
                    intent.putExtra(TUIKitConstants.VIDEO_TIME, duration)
                    intent.putExtra(TUIKitConstants.CAMERA_IMAGE_PATH, path)
                    intent.putExtra(TUIKitConstants.CAMERA_VIDEO_PATH, url)
                    firstFrame.width
                    //setResult(-1, intent);
                    if (mCallBack != null) {
                        mCallBack?.onSuccess(intent)
                    }
                    finish()
                }
            }
        })
        jCameraView?.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                finish()
            }

        })
        jCameraView?.setRightClickListener(object : ClickListener {
            override fun onClick() {
                ToastUtil.toastShortMessage("Right")
            }

        })

        //jCameraView.setVisibility(View.GONE);
        TUIKitLog.i(TAG, DeviceUtil.deviceModel)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onStart() {
        super.onStart()
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = option
        }
    }

    override fun onResume() {
        TUIKitLog.i(TAG, "onResume")
        super.onResume()
        jCameraView?.onResume()
    }

    override fun onPause() {
        TUIKitLog.i(TAG, "onPause")
        super.onPause()
        jCameraView?.onPause()
    }

    override fun onDestroy() {
        TUIKitLog.i(TAG, "onDestroy")
        super.onDestroy()
        mCallBack = null
    }

    companion object {
        private val TAG = CameraActivity::class.java.simpleName
        var mCallBack: IUIKitCallBack? = null
    }
}