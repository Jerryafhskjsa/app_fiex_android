package com.black.base.activity

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.black.base.R
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.getLoadDialog
import com.black.base.util.FryingUtil.showToast
import com.black.base.util.ImageLoader
import com.black.base.util.RouterConstData
import com.black.base.view.LoadingDialog
import com.black.lib.photoview.PhotoView
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.PictureUtils

/**
 * download and show original image
 */
@Route(value = [RouterConstData.SHOW_BIG_IMAGE])
class ShowBigImageActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "ShowBigImage"
    }

    private val pd: ProgressDialog? = null
    private var image: PhotoView? = null
    private val localFilePath: String? = null
    private var bitmap: Bitmap? = null
    private val isDownloaded = false
    private var imageLocalPath: String? = null
    private var url: String? = null
    private var imageLoader: ImageLoader? = null
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_show_big_image)
        super.onCreate(savedInstanceState)
        imageLoader = ImageLoader(this)
        imageLocalPath = intent.getStringExtra(ConstData.IMAGE_LOCAL_PATH)
        url = intent.getStringExtra(ConstData.URL)
        image = findViewById<View>(R.id.image) as PhotoView
        loadingDialog = getLoadDialog(this, null)
//		ProgressBar loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
//		Uri uri = getIntent().getParcelableExtra("uri");
//		localFilePath = getIntent().getExtras().getString("localUrl");
//		String msgId = getIntent().getExtras().getString("messageId");
//		EMLog.d(TAG, "show big msgId:" + msgId );
//		//show the image if it exist in local path
//		if (uri != null && new File(uri.getPath()).exists()) {
//			EMLog.d(TAG, "showbigimage file exists. directly show it");
//			DisplayMetrics metrics = new DisplayMetrics();
//			getWindowManager().getDefaultDisplay().getMetrics(metrics);
//			// int screenWidth = metrics.widthPixels;
//			// int screenHeight =metrics.heightPixels;
//			bitmap = EaseImageCache.getInstance().get(uri.getPath());
//			if (bitmap == null) {
//				EaseLoadLocalBigImgTask task = new EaseLoadLocalBigImgTask(this, uri.getPath(), image, loadLocalPb, ImageUtils.SCALE_IMAGE_WIDTH,
//						ImageUtils.SCALE_IMAGE_HEIGHT);
//				if (android.os.Build.VERSION.SDK_INT > 10) {
//					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//				} else {
//					task.execute();
//				}
//			} else {
//				image.setImageBitmap(bitmap);
//			}
//		} else if(msgId != null) {
//		    downloadImage(msgId);
//		}else {
//			image.setImageResource(default_res);
//		}
        findViewById<View>(R.id.btn_save_img).setOnClickListener(this)
        showImage()
        image!!.setOnClickListener { finish() }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        val headTitleView = view.findViewById<TextView>(R.id.action_bar_title)
        headTitleView.text = "图片查看"
    }

    private fun showImage() {
        if (TextUtils.isEmpty(imageLocalPath) && TextUtils.isEmpty(url)) { //url为空，提示并退出
            showToast(this, "图片路径为空，无法显示图片！")
            return
        }
        if (!TextUtils.isEmpty(imageLocalPath)) {
            val bitmap = getImage(imageLocalPath)
            if (bitmap != null) {
                image!!.setImageBitmap(bitmap)
                return
            }
        }
        if (!TextUtils.isEmpty(url)) {
            val imageFile = imageLoader!!.getFile(url)
            if (imageFile != null) {
                val bitmap = imageLoader!!.onDecodeFile(imageFile, ImageLoader.MAX_WIDTH, ImageLoader.MAX_HEIGHT)
                if (bitmap != null) {
                    image!!.setImageBitmap(bitmap)
                } else {
                    downloadImage()
                }
            } else {
                downloadImage()
            }
        }
    }

    private fun getImage(path: String?): Bitmap? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        val degree = CommonUtil.getExifOrientation(path)
        try {
            var photoBmp = PictureUtils.getSmallBitmap(path, ImageLoader.MAX_WIDTH, ImageLoader.MAX_HEIGHT)
            if (degree == 90 || degree == 180 || degree == 270) { //Roate preview icon according to exif orientation
                val matrix = Matrix()
                matrix.postRotate(degree.toFloat())
                val realBitmap = Bitmap.createBitmap(photoBmp, 0, 0, photoBmp.width, photoBmp.height, matrix, true)
                photoBmp.recycle()
                photoBmp = realBitmap
            }
            return photoBmp
        } catch (e: Exception) {
            CommonUtil.printError(this, e)
        }
        return null
    }

    private fun downloadImage() {
        loadingDialog!!.show()
        Thread(Runnable {
            bitmap = imageLoader!!.getBitmap(url, ImageLoader.MAX_WIDTH, ImageLoader.MAX_HEIGHT)
            CommonUtil.checkActivityAndRunOnUI(mContext) {
                if (bitmap != null) {
                    image!!.setImageBitmap(bitmap)
                } else {
                    showToast(mContext, "查看失败，请重试！")
                }
            }
            runOnUiThread { loadingDialog!!.dismiss() }
        }).start()
    }

    override fun onBackPressed() {
        if (isDownloaded) setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_save_img) {
            if (bitmap != null) {
                requestStoragePermissions(Runnable {
                    try {
                        ImageUtil.saveImageToSysGallery(mContext, saveFileName, bitmap)
                        showToast(mContext, mContext.getString(R.string.save_success))
                    } catch (e: Exception) {
                        showToast(mContext, mContext.getString(R.string.save_failed))
                    }
                })
            }
        }
    }

    private val saveFileName: String
        get() {
            val now = System.currentTimeMillis()
            return CommonUtil.formatTimestamp("yyyyMMddHHmmss", now) + (Math.random() * 10000).toInt() + ".png"
        }
}