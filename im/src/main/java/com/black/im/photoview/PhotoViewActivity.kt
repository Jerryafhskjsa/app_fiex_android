package com.black.im.photoview

import android.app.Activity
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.util.FileUtil.getUriFromPath
import com.black.im.util.TUIKitConstants
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMImage
import java.io.File

class PhotoViewActivity : Activity() {
    private var mPhotoView: PhotoView? = null
    private var mCurrentDisplayMatrix: Matrix? = null
    private var mViewOriginalBtn: TextView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //去除状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_photo_view)
        val uri = getUriFromPath(intent.getStringExtra(TUIKitConstants.IMAGE_DATA))
        val isSelf = intent.getBooleanExtra(TUIKitConstants.SELF_MESSAGE, false)
        mCurrentDisplayMatrix = Matrix()
        mPhotoView = findViewById(R.id.photo_view)
        mPhotoView?.setDisplayMatrix(mCurrentDisplayMatrix)
        mPhotoView?.setOnMatrixChangeListener(MatrixChangeListener())
        mPhotoView?.setOnPhotoTapListener(PhotoTapListener())
        mPhotoView?.setOnSingleFlingListener(SingleFlingListener())
        mViewOriginalBtn = findViewById(R.id.view_original_btn)
        if (isSelf || mCurrentOriginalImage == null) {
            mPhotoView?.setImageURI(uri)
        } else {
            if (mCurrentOriginalImage != null) {
                val path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + mCurrentOriginalImage!!.uuid
                val file = File(path)
                if (file.exists()) mPhotoView?.setImageURI(getUriFromPath(file.path)) else {
                    mPhotoView?.setImageURI(uri)
                    mViewOriginalBtn?.setVisibility(View.VISIBLE)
                    mViewOriginalBtn?.setOnClickListener(View.OnClickListener {
                        if (mCurrentOriginalImage != null) {
                            val imgPath = TUIKitConstants.IMAGE_DOWNLOAD_DIR + mCurrentOriginalImage!!.uuid
                            val imgFile = File(imgPath)
                            if (!imgFile.exists()) {
                                mCurrentOriginalImage!!.getImage(imgPath, object : TIMCallBack {
                                    override fun onError(code: Int, desc: String) {}
                                    override fun onSuccess() {
                                        mPhotoView?.setImageURI(getUriFromPath(imgFile.path))
                                        mViewOriginalBtn?.text = "已完成"
                                        mViewOriginalBtn?.setOnClickListener(null)
                                    }
                                })
                            } else {
                                mPhotoView?.setImageURI(getUriFromPath(imgFile.path))
                            }
                        }
                    })
                }
            }
        }
        findViewById<View>(R.id.photo_view_back).setOnClickListener { finish() }
    }

    private inner class PhotoTapListener : OnPhotoTapListener {
        override fun onPhotoTap(view: ImageView?, x: Float, y: Float) {
            val xPercentage = x * 100f
            val yPercentage = y * 100f
        }
    }

    private inner class MatrixChangeListener : OnMatrixChangedListener {
        override fun onMatrixChanged(rect: RectF?) {}
    }

    private inner class SingleFlingListener : OnSingleFlingListener {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return true
        }
    }

    companion object {
        var mCurrentOriginalImage: TIMImage? = null
    }
}