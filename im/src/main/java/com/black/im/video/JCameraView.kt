package com.black.im.video

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import com.black.im.R
import com.black.im.util.FileUtil
import com.black.im.util.ScreenUtil
import com.black.im.util.TUIKit
import com.black.im.util.TUIKitLog
import com.black.im.video.listener.*
import com.black.im.video.state.CameraMachine
import com.black.im.video.view.CameraView
import java.io.IOException

class JCameraView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(mContext, attrs, defStyleAttr), CameraInterface.CameraOpenOverCallback, SurfaceHolder.Callback, CameraView {
    companion object {
        //拍照浏览时候的类型
        const val TYPE_PICTURE = 0x001
        const val TYPE_VIDEO = 0x002
        const val TYPE_SHORT = 0x003
        const val TYPE_DEFAULT = 0x004
        //录制视频比特率
        const val MEDIA_QUALITY_HIGH = 20 * 100000
        const val MEDIA_QUALITY_MIDDLE = 16 * 100000
        const val MEDIA_QUALITY_LOW = 12 * 100000
        const val MEDIA_QUALITY_POOR = 8 * 100000
        const val MEDIA_QUALITY_FUNNY = 4 * 100000
        const val MEDIA_QUALITY_DESPAIR = 2 * 100000
        const val MEDIA_QUALITY_SORRY = 1 * 80000
        const val BUTTON_STATE_ONLY_CAPTURE = 0x101 //只能拍照
        const val BUTTON_STATE_ONLY_RECORDER = 0x102 //只能录像
        const val BUTTON_STATE_BOTH = 0x103 //两者都可以
        private val TAG = JCameraView::class.java.simpleName
        //闪关灯状态
        private const val TYPE_FLASH_AUTO = 0x021
        private const val TYPE_FLASH_ON = 0x022
        private const val TYPE_FLASH_OFF = 0x023
    }

    //Camera状态机
    private var machine: CameraMachine? = null
    private val type_flash = TYPE_FLASH_OFF
    //回调监听
    private var jCameraLisenter: JCameraListener? = null
    private var leftClickListener: ClickListener? = null
    private var rightClickListener: ClickListener? = null
    private var mVideoView: VideoView? = null
    private var mPhoto: ImageView? = null
    private var mSwitchCamera: ImageView? = null
    private var mCaptureLayout: CaptureLayout? = null
    private var mFoucsView: FoucsView? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var layout_width = 0
    private var screenProp = 0f
    private var captureBitmap //捕获的图片
            : Bitmap? = null
    private var firstFrame //第一帧图片
            : Bitmap? = null
    private var videoUrl //视频URL
            : String? = null
    //切换摄像头按钮的参数
    private var iconSize = 0 //图标大小
    private var iconMargin = 0 //右上边距
    private var iconSrc = 0 //图标资源
    private var iconLeft = 0 //左图标
    private var iconRight = 0 //右图标
    private var duration = 0 //录制时间
    private var recordTime: Long = 0
    //缩放梯度
    private var zoomGradient = 0
    private var firstTouch = true
    private var firstTouchLength = 0f
    private var errorLisenter: ErrorListener? = null

    init {
        //get AttributeSet
        val a = mContext.theme.obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0)
        iconSize = a.getDimensionPixelSize(R.styleable.JCameraView_iconSize, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35f, resources.displayMetrics).toInt())
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_iconMargin, TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15f, resources.displayMetrics).toInt())
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_camera)
        iconLeft = a.getResourceId(R.styleable.JCameraView_iconLeft, 0)
        iconRight = a.getResourceId(R.styleable.JCameraView_iconRight, 0)
        duration = (TUIKit.configs.generalConfig?.videoRecordMaxTime ?: 0) * 1000
        a.recycle()
        initData()
        initView()
    }

    private fun initData() {
        layout_width = ScreenUtil.getScreenWidth(mContext)
        //缩放梯度
        zoomGradient = (layout_width / 16f).toInt()
        TUIKitLog.i(TAG, "zoom = $zoomGradient")
        machine = CameraMachine(context, this, this)
    }

    private fun initView() {
        setWillNotDraw(false)
        val view = LayoutInflater.from(mContext).inflate(R.layout.chat_input_camera_view, this)
        mVideoView = view.findViewById(R.id.video_preview)
        mPhoto = view.findViewById(R.id.image_photo)
        mSwitchCamera = view.findViewById(R.id.image_switch)
        mSwitchCamera?.setImageResource(iconSrc)
        setFlashRes()
        mCaptureLayout = view.findViewById(R.id.capture_layout)
        mCaptureLayout?.setDuration(duration)
        mCaptureLayout?.setIconSrc(iconLeft, iconRight)
        mFoucsView = view.findViewById(R.id.fouce_view)
        mVideoView?.holder?.addCallback(this)
        //切换摄像头
        mSwitchCamera?.setOnClickListener(OnClickListener { machine?.swtich(mVideoView?.holder, screenProp) })
        //拍照 录像
        mCaptureLayout?.setCaptureLisenter(object : CaptureListener {
            override fun takePictures() {
                mSwitchCamera?.visibility = View.INVISIBLE
                machine?.capture()
            }

            override fun recordStart() {
                mSwitchCamera?.visibility = View.INVISIBLE
                machine?.record(mVideoView?.holder?.surface, screenProp)
            }

            override fun recordShort(time: Long) {
                mCaptureLayout?.setTextWithAnimation("录制时间过短")
                mSwitchCamera?.visibility = View.VISIBLE
                postDelayed({ machine?.stopRecord(true, time) }, 1500 - time)
            }

            override fun recordEnd(time: Long) {
                machine?.stopRecord(false, time)
                recordTime = time
            }

            override fun recordZoom(zoom: Float) {
                TUIKitLog.i(TAG, "recordZoom")
                machine?.zoom(zoom, CameraInterface.TYPE_RECORDER)
            }

            override fun recordError() {
                if (errorLisenter != null) {
                    errorLisenter?.AudioPermissionError()
                }
            }
        })
        //确认 取消
        mCaptureLayout?.setTypeLisenter(object : TypeListener {
            override fun cancel() {
                machine?.cancle(mVideoView?.holder, screenProp)
            }

            override fun confirm() {
                machine?.confirm()
            }
        })
        //退出
//        mCaptureLayout.setReturnLisenter(new ReturnListener() {
//            @Override
//            public void onReturn() {
//                if (jCameraLisenter != null) {
//                    jCameraLisenter.quit();
//                }
//            }
//        });
        mCaptureLayout?.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                if (leftClickListener != null) {
                    leftClickListener?.onClick()
                }
            }
        })
        mCaptureLayout?.setRightClickListener(object : ClickListener {
            override fun onClick() {
                if (rightClickListener != null) {
                    rightClickListener?.onClick()
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = mVideoView?.measuredWidth?.toFloat() ?: 0.toFloat()
        val heightSize = mVideoView?.measuredHeight?.toFloat() ?: 0.toFloat()
        if (screenProp == 0f) {
            screenProp = if (widthSize == 0.toFloat()) 0.toFloat() else (heightSize / widthSize)
        }
    }

    override fun cameraHasOpened() {
        CameraInterface.instance.doStartPreview(mVideoView?.holder, screenProp)
    }

    //生命周期onResume
    fun onResume() {
        TUIKitLog.i(TAG, "JCameraView onResume")
        resetState(TYPE_DEFAULT) //重置状态
        CameraInterface.instance.registerSensorManager(mContext)
        machine?.start(mVideoView?.holder, screenProp)
    }

    //生命周期onPause
    fun onPause() {
        TUIKitLog.i(TAG, "JCameraView onPause")
        stopVideo()
        resetState(TYPE_PICTURE)
        CameraInterface.instance.isPreview(false)
        CameraInterface.instance.unregisterSensorManager(mContext)
        CameraInterface.destroyCameraInterface()
    }

    //SurfaceView生命周期
    override fun surfaceCreated(holder: SurfaceHolder) {
        TUIKitLog.i(TAG, "JCameraView SurfaceCreated")
        object : Thread() {
            override fun run() {
                CameraInterface.instance.doOpenCamera(this@JCameraView)
            }
        }.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TUIKitLog.i(TAG, "JCameraView SurfaceDestroyed")
        CameraInterface.instance.doDestroyCamera()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1) { //显示对焦指示器
                    setFocusViewWidthAnimation(event.x, event.y)
                }
                if (event.pointerCount == 2) {
                    TUIKitLog.i(TAG, "ACTION_DOWN = " + 2)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1) {
                    firstTouch = true
                }
                if (event.pointerCount == 2) { //第一个点
                    val point_1_X = event.getX(0)
                    val point_1_Y = event.getY(0)
                    //第二个点
                    val point_2_X = event.getX(1)
                    val point_2_Y = event.getY(1)
                    val result = Math.sqrt(Math.pow(point_1_X - point_2_X.toDouble(), 2.0) + Math.pow(point_1_Y -
                            point_2_Y.toDouble(), 2.0)).toFloat()
                    if (firstTouch) {
                        firstTouchLength = result
                        firstTouch = false
                    }
                    if ((result - firstTouchLength).toInt() / zoomGradient != 0) {
                        firstTouch = true
                        machine?.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE)
                    }
                    //                    TUIKitLog.i(TAG, "result = " + (result - firstTouchLength));
                }
            }
            MotionEvent.ACTION_UP -> firstTouch = true
        }
        return true
    }

    //对焦框指示器动画
    private fun setFocusViewWidthAnimation(x: Float, y: Float) {
        machine?.foucs(x, y, object : CameraInterface.FocusCallback {
            override fun focusSuccess() {
                mFoucsView?.visibility = View.INVISIBLE
            }
        })
    }

    private fun updateVideoViewSize(videoWidth: Float, videoHeight: Float) {
        if (videoWidth > videoHeight) {
            val videoViewParam: LayoutParams
            val height = (videoHeight / videoWidth * width).toInt()
            videoViewParam = LayoutParams(LayoutParams.MATCH_PARENT, height)
            videoViewParam.gravity = Gravity.CENTER
            mVideoView?.layoutParams = videoViewParam
        }
    }

    fun setJCameraLisenter(jCameraLisenter: JCameraListener?) {
        this.jCameraLisenter = jCameraLisenter
    }

    //启动Camera错误回调
    fun setErrorLisenter(errorLisenter: ErrorListener?) {
        this.errorLisenter = errorLisenter
        CameraInterface.instance.setErrorLinsenter(errorLisenter)
    }

    //设置CaptureButton功能（拍照和录像）
    fun setFeatures(state: Int) {
        mCaptureLayout?.setButtonFeatures(state)
    }

    //设置录制质量
    fun setMediaQuality(quality: Int) {
        CameraInterface.instance.setMediaQuality(quality)
    }

    override fun resetState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo() //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl)
                mVideoView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                machine?.start(mVideoView?.holder, screenProp)
            }
            TYPE_PICTURE -> mPhoto?.visibility = View.INVISIBLE
            TYPE_SHORT -> {
            }
            TYPE_DEFAULT -> mVideoView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        mSwitchCamera?.visibility = View.VISIBLE
        //mFlashLamp.setVisibility(VISIBLE);
        mCaptureLayout?.resetCaptureLayout()
    }

    override fun confirmState(type: Int) {
        when (type) {
            TYPE_VIDEO -> {
                stopVideo() //停止播放
                mVideoView?.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                machine?.start(mVideoView?.holder, screenProp)
                if (jCameraLisenter != null) {
                    jCameraLisenter?.recordSuccess(videoUrl, firstFrame, recordTime)
                }
            }
            TYPE_PICTURE -> {
                mPhoto?.visibility = View.INVISIBLE
                if (jCameraLisenter != null) {
                    jCameraLisenter?.captureSuccess(captureBitmap)
                }
            }
            TYPE_SHORT -> {
            }
            TYPE_DEFAULT -> {
            }
        }
        mCaptureLayout?.resetCaptureLayout()
    }

    override fun showPicture(bitmap: Bitmap, isVertical: Boolean) {
        if (isVertical) {
            mPhoto?.scaleType = ImageView.ScaleType.FIT_XY
        } else {
            mPhoto?.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        captureBitmap = bitmap
        mPhoto?.setImageBitmap(bitmap)
        mPhoto?.visibility = View.VISIBLE
        mCaptureLayout?.startAlphaAnimation()
        mCaptureLayout?.startTypeBtnAnimator()
    }

    override fun playVideo(firstFrame: Bitmap, url: String) {
        videoUrl = url
        this@JCameraView.firstFrame = firstFrame
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer()
            } else {
                mMediaPlayer?.reset()
            }
            mMediaPlayer?.setDataSource(url)
            mMediaPlayer?.setSurface(mVideoView?.holder?.surface)
            mMediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer?.setOnVideoSizeChangedListener { _, _, _ ->
                updateVideoViewSize(
                        mMediaPlayer?.videoWidth?.toFloat() ?: 0.toFloat(),
                        mMediaPlayer?.videoHeight?.toFloat() ?: 0.toFloat())
            }
            mMediaPlayer?.setOnPreparedListener { mMediaPlayer?.start() }
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.prepareAsync()
        } catch (e: IOException) {
        }
    }

    override fun stopVideo() {
        if (mMediaPlayer != null && true == mMediaPlayer?.isPlaying) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
    }

    override fun setTip(tip: String) {
        mCaptureLayout?.setTip(tip)
    }

    override fun startPreviewCallback() {
        TUIKitLog.i(TAG, "startPreviewCallback")
        handlerFoucs((mFoucsView?.width ?: 0) / 2.toFloat(),
                (mFoucsView?.height ?: 0) / 2.toFloat())
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun handlerFoucs(x: Float, y: Float): Boolean {
        var x = x
        var y = y
        if (y > mCaptureLayout?.top ?: 0) {
            return false
        }
        mFoucsView?.visibility = View.VISIBLE
        if (x < mFoucsView?.width ?: 0 / 2) {
            x = (mFoucsView?.width ?: 0) / 2.toFloat()
        }
        if (x > layout_width - (mFoucsView?.width ?: 0) / 2) {
            x = layout_width - (mFoucsView?.width ?: 0) / 2.toFloat()
        }
        if (y < mFoucsView?.width ?: 0 / 2) {
            y = (mFoucsView?.width ?: 0) / 2.toFloat()
        }
        if (y > mCaptureLayout?.top ?: 0 - (mFoucsView?.width ?: 0) / 2) {
            y = (mCaptureLayout?.top ?: 0) - (mFoucsView?.width ?: 0) / 2.toFloat()
        }
        mFoucsView?.x = x - (mFoucsView?.width ?: 0) / 2
        mFoucsView?.y = y - (mFoucsView?.height ?: 0) / 2
        val scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1f, 0.6f)
        val scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1f, 0.6f)
        val alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f)
        val animSet = AnimatorSet()
        animSet.play(scaleX).with(scaleY).before(alpha)
        animSet.duration = 400
        animSet.start()
        return true
    }

    fun setLeftClickListener(clickListener: ClickListener?) {
        leftClickListener = clickListener
    }

    fun setRightClickListener(clickListener: ClickListener?) {
        rightClickListener = clickListener
    }

    private fun setFlashRes() {
        when (type_flash) {
            TYPE_FLASH_AUTO -> machine?.flash(Camera.Parameters.FLASH_MODE_AUTO)
            TYPE_FLASH_ON -> machine?.flash(Camera.Parameters.FLASH_MODE_ON)
            TYPE_FLASH_OFF -> machine?.flash(Camera.Parameters.FLASH_MODE_OFF)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}