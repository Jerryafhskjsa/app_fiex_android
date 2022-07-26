package com.black.im.video

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.*
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.PictureCallback
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.ImageView
import com.black.im.util.FileUtil
import com.black.im.util.ScreenUtil
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.video.listener.ErrorListener
import com.black.im.video.state.PreviewState
import com.black.im.video.util.AngleUtil.getSensorAngle
import com.black.im.video.util.CameraParamUtil
import com.black.im.video.util.CheckPermission
import com.black.im.video.util.DeviceUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraInterface private constructor() : Camera.PreviewCallback {
    var handlerTime = 0
    private var mCamera: Camera? = null
    private var mParams: Camera.Parameters? = null
    private var isPreviewing = false
    private var SELECTED_CAMERA = -1
    private var CAMERA_POST_POSITION = -1
    private var CAMERA_FRONT_POSITION = -1
    private var mHolder: SurfaceHolder? = null
    private var screenProp = -1.0f
    private var isRecorder = false
    private var mediaRecorder: MediaRecorder? = null
    private var videoFileName: String? = null
    private val saveVideoPath = TUIKitConstants.MEDIA_DIR
    private var videoFileAbsPath: String? = null
    private var videoFirstFrame: Bitmap? = null
    private var errorLisenter: ErrorListener? = null
    private var mSwitchView: ImageView? = null
    private var mFlashLamp: ImageView? = null
    private var preview_width = 0
    private var preview_height = 0
    private var angle = 0
    private var cameraAngle = 90 //摄像头角度   默认为90度
    private var rotation = 0
    private var firstframeData: ByteArray? = null
    private var nowScaleRate = 0
    private var recordScleRate = 0
    //视频质量
    private var mediaQuality = JCameraView.MEDIA_QUALITY_MIDDLE
    private var sm: SensorManager? = null
    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.type) {
                return
            }
            val values = event.values
            angle = getSensorAngle(values[0], values[1])
            rotationAnimation()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    /**
     * 拍照
     */
    private var nowAngle = 0

    companion object {
        const val TYPE_RECORDER = 0x090
        const val TYPE_CAPTURE = 0x091
        private val TAG = CameraInterface::class.java.simpleName
        @Volatile
        private var mCameraInterface: CameraInterface? = null

        fun destroyCameraInterface() {
            if (mCameraInterface != null) {
                mCameraInterface = null
            }
        }

        //获取CameraInterface单例
        @get:Synchronized
        val instance: CameraInterface
            get() {
                if (mCameraInterface == null) synchronized(CameraInterface::class.java) { if (mCameraInterface == null) mCameraInterface = CameraInterface() }
                return mCameraInterface!!
            }

        private fun calculateTapArea(x: Float, y: Float, coefficient: Float, context: Context): Rect {
            val focusAreaSize = 300f
            val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient).toInt()
            val centerX = (x / ScreenUtil.getScreenWidth(context) * 2000 - 1000).toInt()
            val centerY = (y / ScreenUtil.getScreenHeight(context) * 2000 - 1000).toInt()
            val left = clamp(centerX - areaSize / 2, -1000, 1000)
            val top = clamp(centerY - areaSize / 2, -1000, 1000)
            val rectF = RectF(left.toFloat(), top.toFloat(), (left + areaSize).toFloat(), (top + areaSize).toFloat())
            return Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom))
        }

        private fun clamp(x: Int, min: Int, max: Int): Int {
            if (x > max) {
                return max
            }
            return if (x < min) {
                min
            } else x
        }
    }

    init {
        findAvailableCameras()
        SELECTED_CAMERA = CAMERA_POST_POSITION
    }

    fun setSwitchView(mSwitchView: ImageView?, mFlashLamp: ImageView?) {
        this.mSwitchView = mSwitchView
        this.mFlashLamp = mFlashLamp
        if (mSwitchView != null) {
            cameraAngle = CameraParamUtil.instance.getCameraDisplayOrientation(mSwitchView.context,
                    SELECTED_CAMERA)
        }
    }

    //切换摄像头icon跟随手机角度进行旋转
    @SuppressLint("ObjectAnimatorBinding")
    private fun rotationAnimation() {
        if (mSwitchView == null) {
            return
        }
        if (rotation != angle) {
            var startRotaion = 0
            var endRotation = 0
            when (rotation) {
                0 -> {
                    startRotaion = 0
                    when (angle) {
                        90 -> endRotation = -90
                        270 -> endRotation = 90
                    }
                }
                90 -> {
                    startRotaion = -90
                    when (angle) {
                        0 -> endRotation = 0
                        180 -> endRotation = -180
                    }
                }
                180 -> {
                    startRotaion = 180
                    when (angle) {
                        90 -> endRotation = 270
                        270 -> endRotation = 90
                    }
                }
                270 -> {
                    startRotaion = 90
                    when (angle) {
                        0 -> endRotation = 0
                        180 -> endRotation = 180
                    }
                }
            }
            val animC = ObjectAnimator.ofFloat(mSwitchView, "rotation", startRotaion.toFloat(), endRotation.toFloat())
            val animF = ObjectAnimator.ofFloat(mFlashLamp, "rotation", startRotaion.toFloat(), endRotation.toFloat())
            val set = AnimatorSet()
            set.playTogether(animC, animF)
            set.duration = 500
            set.start()
            rotation = angle
        }
    }

    fun setZoom(zoom: Float, type: Int) {
        if (mCamera == null) {
            return
        }
        if (mParams == null) {
            mParams = mCamera?.parameters
        }
        if (true != mParams?.isZoomSupported || true != mParams?.isSmoothZoomSupported) {
            return
        }
        when (type) {
            TYPE_RECORDER -> {
                //如果不是录制视频中，上滑不会缩放
                if (!isRecorder) {
                    return
                }
                if (zoom >= 0) { //每移动50个像素缩放一个级别
                    val scaleRate = (zoom / 40).toInt()
                    if (scaleRate <= mParams?.maxZoom ?: 0 && scaleRate >= nowScaleRate && recordScleRate != scaleRate) {
                        mParams?.zoom = scaleRate
                        mCamera?.parameters = mParams
                        recordScleRate = scaleRate
                    }
                }
            }
            TYPE_CAPTURE -> {
                if (isRecorder) {
                    return
                }
                //每移动50个像素缩放一个级别
                val scaleRate = (zoom / 50).toInt()
                if (scaleRate < mParams?.maxZoom ?: 0) {
                    nowScaleRate += scaleRate
                    if (nowScaleRate < 0) {
                        nowScaleRate = 0
                    } else if (nowScaleRate > mParams?.maxZoom ?: 0) {
                        nowScaleRate = mParams?.maxZoom ?: 0
                    }
                    mParams?.zoom = nowScaleRate
                    mCamera?.parameters = mParams
                }
                TUIKitLog.i(TAG, "setZoom = $nowScaleRate")
            }
        }
    }

    fun setMediaQuality(quality: Int) {
        mediaQuality = quality
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        firstframeData = data
    }

    fun setFlashMode(flashMode: String?) {
        if (mCamera == null) return
        val params = mCamera?.parameters
        params?.flashMode = flashMode
        mCamera?.parameters = params
    }

    /**
     * open Camera
     */
    fun doOpenCamera(callback: CameraOpenOverCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!CheckPermission.isCameraUseable(SELECTED_CAMERA) && errorLisenter != null) {
                errorLisenter?.onError()
                return
            }
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA)
        }
        callback.cameraHasOpened()
    }

    private fun setFlashModel() {
        mParams = mCamera?.parameters
        mParams?.flashMode = Camera.Parameters.FLASH_MODE_TORCH //设置camera参数为Torch模式
        mCamera?.parameters = mParams
    }

    @Synchronized
    private fun openCamera(id: Int) {
        try {
            mCamera = Camera.open(id)
        } catch (var3: Exception) {
            if (errorLisenter != null) {
                errorLisenter?.onError()
            }
        }
        if (Build.VERSION.SDK_INT > 17 && mCamera != null) {
            try {
                mCamera?.enableShutterSound(false)
            } catch (e: Exception) {
                TUIKitLog.e(TAG, "enable shutter sound faild")
            }
        }
    }

    @Synchronized
    fun switchCamera(holder: SurfaceHolder?, screenProp: Float) {
        SELECTED_CAMERA = if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            CAMERA_FRONT_POSITION
        } else {
            CAMERA_POST_POSITION
        }
        doDestroyCamera()
        TUIKitLog.i(TAG, "open start")
        openCamera(SELECTED_CAMERA)
        //        mCamera = Camera.open();
        if (Build.VERSION.SDK_INT > 17 && mCamera != null) {
            try {
                mCamera?.enableShutterSound(false)
            } catch (e: Exception) {
            }
        }
        TUIKitLog.i(TAG, "open end")
        doStartPreview(holder, screenProp)
    }

    /**
     * doStartPreview
     */
    fun doStartPreview(holder: SurfaceHolder?, screenProp: Float) {
        if (isPreviewing) {
            TUIKitLog.i(TAG, "doStartPreview isPreviewing")
        }
        if (this.screenProp < 0) {
            this.screenProp = screenProp
        }
        if (holder == null) {
            return
        }
        mHolder = holder
        if (mCamera != null) {
            try {
                mParams = mCamera?.parameters
                val previewSize: Camera.Size = CameraParamUtil.instance.getPreviewSize(mParams?.supportedPreviewSizes!!, 1000, screenProp)
                val pictureSize: Camera.Size = CameraParamUtil.instance.getPictureSize(mParams?.supportedPictureSizes!!, 1200, screenProp)
                mParams?.setPreviewSize(previewSize.width, previewSize.height)
                preview_width = previewSize.width
                preview_height = previewSize.height
                mParams?.setPictureSize(pictureSize.width, pictureSize.height)
                if (CameraParamUtil.instance.isSupportedFocusMode(
                                mParams?.supportedFocusModes!!,
                                Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mParams?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                }
                if (CameraParamUtil.instance.isSupportedPictureFormats(mParams?.supportedPictureFormats!!,
                                ImageFormat.JPEG)) {
                    mParams?.pictureFormat = ImageFormat.JPEG
                    mParams?.jpegQuality = 100
                }
                mCamera?.parameters = mParams
                mParams = mCamera?.parameters
                mCamera?.setPreviewDisplay(holder) //SurfaceView
                mCamera?.setDisplayOrientation(cameraAngle) //浏览角度
                mCamera?.setPreviewCallback(this) //每一帧回调
                mCamera?.startPreview() //启动浏览
                isPreviewing = true
                TUIKitLog.i(TAG, "=== Start Preview ===")
            } catch (e: IOException) {
            }
        }
    }

    /**
     * 停止预览
     */
    fun doStopPreview() {
        if (null != mCamera) {
            try {
                mCamera?.setPreviewCallback(null)
                mCamera?.stopPreview()
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera?.setPreviewDisplay(null)
                isPreviewing = false
                TUIKitLog.i(TAG, "=== Stop Preview ===")
            } catch (e: IOException) {
            }
        }
    }

    /**
     * 销毁Camera
     */
    fun doDestroyCamera() {
        errorLisenter = null
        if (null != mCamera) {
            try {
                mCamera?.setPreviewCallback(null)
                mSwitchView = null
                mFlashLamp = null
                mCamera?.stopPreview()
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera?.setPreviewDisplay(null)
                mHolder = null
                isPreviewing = false
                mCamera?.release()
                mCamera = null
                //                destroyCameraInterface();
                TUIKitLog.i(TAG, "=== Destroy Camera ===")
            } catch (e: IOException) {
            }
        } else {
            TUIKitLog.i(TAG, "=== Camera  Null===")
        }
    }

    fun takePicture(callback: TakePictureCallback?) {
        if (mCamera == null) {
            return
        }
        when (cameraAngle) {
            90 -> nowAngle = Math.abs(angle + cameraAngle) % 360
            270 -> nowAngle = Math.abs(cameraAngle - angle)
        }
        //
        TUIKitLog.i(TAG, "$angle = $cameraAngle = $nowAngle")
        mCamera?.takePicture(null, null, PictureCallback { data, camera ->
            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val matrix = Matrix()
            if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                matrix.setRotate(nowAngle.toFloat())
            } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                matrix.setRotate(360 - nowAngle.toFloat())
                matrix.postScale(-1f, 1f)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (callback != null) {
                if (nowAngle == 90 || nowAngle == 270) {
                    callback.captureResult(bitmap, true)
                } else {
                    callback.captureResult(bitmap, false)
                }
            }
        })
    }

    //启动录像
    fun startRecord(surface: Surface?, screenProp: Float, callback: ErrorCallback?) {
        mCamera?.setPreviewCallback(null)
        val nowAngle = (angle + 90) % 360
        //获取第一帧图片
        val parameters = mCamera?.parameters
        val width = parameters?.previewSize?.width ?: 0
        val height = parameters?.previewSize?.height ?: 0
        val yuv = YuvImage(firstframeData, parameters?.previewFormat!!, width, height, null)
        val out = ByteArrayOutputStream()
        yuv.compressToJpeg(Rect(0, 0, width, height), 50, out)
        val bytes = out.toByteArray()
        videoFirstFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val matrix = Matrix()
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            matrix.setRotate(nowAngle.toFloat())
        } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            matrix.setRotate(270f)
        }
        videoFirstFrame = Bitmap.createBitmap(videoFirstFrame!!, 0, 0, videoFirstFrame!!.width, videoFirstFrame!!.height, matrix, true)
        if (isRecorder) {
            return
        }
        if (mCamera == null) {
            openCamera(SELECTED_CAMERA)
        }
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }
        if (mParams == null) {
            mParams = mCamera?.parameters
        }
        val focusModes = mParams?.supportedFocusModes
        if (true == focusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        }
        mCamera?.parameters = mParams
        mCamera?.unlock()
        mediaRecorder?.reset()
        mediaRecorder?.setCamera(mCamera)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val videoSize: Camera.Size = if (mParams?.supportedVideoSizes == null) {
            CameraParamUtil.instance.getPreviewSize(mParams?.supportedPreviewSizes!!, 600, screenProp)
        } else {
            CameraParamUtil.instance.getPreviewSize(mParams?.supportedVideoSizes!!, 600, screenProp)
        }
        TUIKitLog.e(TAG, "setVideoSize    width = " + videoSize.width + "height = " + videoSize.height)
        if (videoSize.width == videoSize.height) {
            mediaRecorder?.setVideoSize(preview_width, preview_height)
        } else {
            mediaRecorder?.setVideoSize(videoSize.width, videoSize.height)
        }
        //        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
//            mediaRecorder.setOrientationHint(270);
//        } else {
//            mediaRecorder.setOrientationHint(nowAngle);
////            mediaRecorder.setOrientationHint(90);
//        }
        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            //手机预览倒立的处理
            if (cameraAngle == 270) { //横屏
                if (nowAngle == 0) {
                    mediaRecorder?.setOrientationHint(180)
                } else if (nowAngle == 270) {
                    mediaRecorder?.setOrientationHint(270)
                } else {
                    mediaRecorder?.setOrientationHint(90)
                }
            } else {
                if (nowAngle == 90) {
                    mediaRecorder?.setOrientationHint(270)
                } else if (nowAngle == 270) {
                    mediaRecorder?.setOrientationHint(90)
                } else {
                    mediaRecorder?.setOrientationHint(nowAngle)
                }
            }
        } else {
            mediaRecorder?.setOrientationHint(nowAngle)
        }
        if (DeviceUtil.isHuaWeiRongyao) {
            mediaRecorder?.setVideoEncodingBitRate(4 * 100000)
        } else {
            mediaRecorder?.setVideoEncodingBitRate(mediaQuality)
        }
        mediaRecorder?.setPreviewDisplay(surface)
        videoFileName = "video_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".mp4"
        videoFileAbsPath = saveVideoPath + File.separator + videoFileName
        mediaRecorder?.setOutputFile(videoFileAbsPath)
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecorder = true
        } catch (e: IllegalStateException) {
            TUIKitLog.i(TAG, "startRecord IllegalStateException")
            if (errorLisenter != null) {
                errorLisenter?.onError()
            }
        } catch (e: IOException) {
            TUIKitLog.i(TAG, "startRecord IOException")
            if (errorLisenter != null) {
                errorLisenter?.onError()
            }
        } catch (e: RuntimeException) {
            TUIKitLog.i(TAG, "startRecord RuntimeException")
        }
    }

    //停止录像
    fun stopRecord(isShort: Boolean, callback: StopRecordCallback) {
        if (!isRecorder) {
            return
        }
        if (mediaRecorder != null) {
            mediaRecorder?.setOnErrorListener(null)
            mediaRecorder?.setOnInfoListener(null)
            mediaRecorder?.setPreviewDisplay(null)
            try {
                mediaRecorder?.stop()
            } catch (e: RuntimeException) {
                mediaRecorder = null
                mediaRecorder = MediaRecorder()
            } finally {
                if (mediaRecorder != null) {
                    mediaRecorder?.reset();//set state to idle
                    mediaRecorder?.release()
                }
                mediaRecorder = null
                isRecorder = false
            }
            if (isShort) {
                if (FileUtil.deleteFile(videoFileAbsPath)) {
                    callback.recordResult(null, null)
                }
                return
            }
            doStopPreview()
            val fileName = saveVideoPath + File.separator + videoFileName
            callback.recordResult(fileName, videoFirstFrame)
        }
    }

    private fun findAvailableCameras() {
        val info = CameraInfo()
        val cameraNum = Camera.getNumberOfCameras()
        for (i in 0 until cameraNum) {
            Camera.getCameraInfo(i, info)
            when (info.facing) {
                CameraInfo.CAMERA_FACING_FRONT -> CAMERA_FRONT_POSITION = info.facing
                CameraInfo.CAMERA_FACING_BACK -> CAMERA_POST_POSITION = info.facing
            }
        }
    }

    fun handleFocus(context: Context, x: Float, y: Float, callback: FocusCallback) {
        if (mCamera == null) {
            return
        }
        val params = mCamera?.parameters
        val focusRect = calculateTapArea(x, y, 1f, context)
        mCamera?.cancelAutoFocus()
        if (params?.maxNumFocusAreas ?: 0 > 0) {
            val focusAreas: MutableList<Camera.Area> = ArrayList()
            focusAreas.add(Camera.Area(focusRect, 800))
            params?.focusAreas = focusAreas
        } else {
            TUIKitLog.i(TAG, "focus areas not supported")
            callback.focusSuccess()
            return
        }
        val currentFocusMode = params?.focusMode
        try {
            params?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            mCamera?.parameters = params
            mCamera?.autoFocus { success, camera ->
                if (success || handlerTime > 10) {
                    val params1 = camera.parameters
                    params1.focusMode = currentFocusMode
                    camera.parameters = params1
                    handlerTime = 0
                    callback.focusSuccess()
                } else {
                    handlerTime++
                    handleFocus(context, x, y, callback)
                }
            }
        } catch (e: Exception) {
            TUIKitLog.e(TAG, "autoFocus failer")
        }
    }

    fun setErrorLinsenter(errorLisenter: ErrorListener?) {
        this.errorLisenter = errorLisenter
    }

    fun registerSensorManager(context: Context) {
        if (sm == null) {
            sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        sm?.registerListener(sensorEventListener, sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterSensorManager(context: Context) {
        if (sm == null) {
            sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
        sm?.unregisterListener(sensorEventListener)
    }

    fun isPreview(res: Boolean) {
        isPreviewing = res
    }

    interface CameraOpenOverCallback {
        fun cameraHasOpened()
    }

    interface StopRecordCallback {
        fun recordResult(url: String?, firstFrame: Bitmap?)
    }

    interface ErrorCallback {
        fun onError()
    }

    interface TakePictureCallback {
        fun captureResult(bitmap: Bitmap?, isVertical: Boolean)
    }

    interface FocusCallback {
        fun focusSuccess()
    }
}