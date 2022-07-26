package com.black.base.lib.decode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.provider.MediaStore
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import com.black.base.R
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.camera.CameraManager
import com.black.lib.decoding.InactivityTimer
import com.black.lib.view.ViewfinderView
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.PictureUtils
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.IOException
import java.util.*

/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
@Route(value = [RouterConstData.CAPTURE])
class MipcaActivityCapture : BaseActionBarActivity(), SurfaceHolder.Callback, View.OnClickListener {
    companion object {
        private const val BEEP_VOLUME = 0.10f
        private const val VIBRATE_DURATION = 200L
    }

    private var title: String? = null
    private var handler: CaptureActivityHandler? = null
    var viewfinderView: ViewfinderView? = null
        private set
    private var hasSurface = false
    private var decodeFormats: Vector<BarcodeFormat?>? = null
    private var characterSet: String? = null
    private var inactivityTimer: InactivityTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep = false
    private var vibrate = false
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        title = intent.getStringExtra(ConstData.TITLE)
        initActionBar()
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(application)
        viewfinderView = findViewById(R.id.viewfinder_view)
        viewfinderView?.setScanText(getString(R.string.scan_text))
        hasSurface = false
        inactivityTimer = InactivityTimer(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_capture
    }

    override fun initActionBarView(view: View) {
        val headTitleView = view.findViewById<TextView>(R.id.action_bar_title)
        headTitleView.setText(R.string.scan_qrcode)
        val pictureView = view.findViewById<TextView>(R.id.action_bar_extras)
        pictureView.setText(R.string.scan_picture)
        pictureView.setOnClickListener(this)
    }

    override fun onBackClick(view: View?) {
        super.onBackClick(view)
        inactivityTimer?.shutdown()
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onResume() {
        super.onResume()
        val surfaceView = findViewById<SurfaceView>(R.id.preview_view)
        val surfaceHolder = surfaceView.holder
        if (hasSurface) {
            initCamera(surfaceHolder)
        } else {
            surfaceHolder.addCallback(this)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
        decodeFormats = null
        characterSet = null
        playBeep = true
        val audioService = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false
        }
        initBeepSound()
        vibrate = true
    }

    override fun onPause() {
        super.onPause()
        if (handler != null) {
            handler?.quitSynchronously()
            handler = null
        }
        CameraManager.get()?.closeDriver()
    }

    override fun onDestroy() {
        inactivityTimer?.shutdown()
        super.onDestroy()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.action_bar_extras) {
            requestStoragePermissions(Runnable {
                //打开文件列表选择
                val openAlbumIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(openAlbumIntent, ConstData.CHOOSE_PICTURE)
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            var path: String? = null
            when (requestCode) {
                ConstData.CHOOSE_PICTURE -> {
                    val uri = data?.data
                    val sdkVersion = Integer.valueOf(Build.VERSION.SDK)
                    if (sdkVersion >= 19) {
                        path = uri?.path
                        path = PictureUtils.getPath_above19(mContext, uri)
                    } else {
                        path = PictureUtils.getFilePath_below19(mContext, uri)
                    }
                }
            }
            path?.let { getImage(it) }
        }
    }

    private fun getImage(path: String) {
        val degree = CommonUtil.getExifOrientation(path)
        try {
            var photoBmp = PictureUtils.getSmallBitmap(path, 500, 500)
            if (degree == 90 || degree == 180 || degree == 270) { //Roate preview icon according to exif orientation
                val matrix = Matrix()
                matrix.postRotate(degree.toFloat())
                val newBmp = Bitmap.createBitmap(photoBmp, 0, 0, photoBmp.width, photoBmp.height, matrix, true)
                photoBmp.recycle()
                photoBmp = newBmp
            }
            //解析选择的图片
// 开始对图像资源解码
            var rawResult: Result? = null
            try {
                val multiFormatReader = MultiFormatReader()
                // 解码的参数
                val hints = Hashtable<DecodeHintType, Any?>(
                        2)
                // 可以解析的编码类型
                var decodeFormats = Vector<BarcodeFormat?>()
                if (decodeFormats == null || decodeFormats.isEmpty()) {
                    decodeFormats = Vector()
                    // 这里设置可扫描的类型，我这里选择了都支持
                    decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS)
                    decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
                    decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
                }
                hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
                // 设置继续的字符编码格式为UTF8
// hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
// 设置解析配置参数
                multiFormatReader.setHints(hints)
                rawResult = multiFormatReader
                        .decodeWithState(BinaryBitmap(HybridBinarizer(
                                BitmapLuminanceSource(photoBmp))))
            } catch (e: NotFoundException) {
                FryingUtil.printError(mContext, e)
            }
            if (rawResult != null) {
                handleDecode(rawResult, photoBmp)
            } else {
                FryingUtil.showToast(this, getString(R.string.scan_picture_failed))
            }
        } catch (e: Exception) {
            FryingUtil.printError(mContext, e)
        }
    }

    /**
     * ����ɨ����
     *
     * @param result
     * @param barcode
     */
    fun handleDecode(result: Result, barcode: Bitmap?) {
        inactivityTimer?.onActivity()
        playBeepSoundAndVibrate()
        val resultString = result.text
        if (resultString == "") {
            FryingUtil.showToast(this@MipcaActivityCapture, "Scan failed!")
        } else {
            val resultIntent = Intent()
            val bundle = Bundle()
            bundle.putString("result", resultString)
            //			bundle.putParcelable("bitmap", barcode);
            resultIntent.putExtras(bundle)
            setResult(Activity.RESULT_OK, resultIntent)
        }
        if (barcode != null && !barcode.isRecycled) {
            barcode.recycle()
        }
        finish()
        overridePendingTransition(0, 0)
    }

    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get()?.openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            FryingUtil.printError(mContext, ioe)
            return
        } catch (e: RuntimeException) {
            FryingUtil.printError(mContext, e)
            return
        }
        if (handler == null) {
            handler = CaptureActivityHandler(this, decodeFormats,
                    characterSet)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
    }

    fun getHandler(): Handler? {
        return handler
    }

    fun drawViewfinder() {
        viewfinderView?.drawViewfinder()
    }

    private fun initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setOnCompletionListener(beepListener)
            val file = resources.openRawResourceFd(
                    R.raw.beep)
            try {
                mediaPlayer?.setDataSource(file.fileDescriptor,
                        file.startOffset, file.length)
                file.close()
                mediaPlayer?.setVolume(BEEP_VOLUME, BEEP_VOLUME)
                mediaPlayer?.prepare()
            } catch (e: IOException) {
                mediaPlayer = null
            }
        }
    }

    private fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer?.start()
        }
        if (vibrate) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private val beepListener = MediaPlayer.OnCompletionListener { mediaPlayer -> mediaPlayer.seekTo(0) }
}