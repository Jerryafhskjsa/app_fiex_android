package com.black.base.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.black.base.R
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.getMaxBitmap
import com.black.lib.permission.PermissionHelper
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.PictureUtils
import com.black.util.Size
import java.io.File
import java.io.IOException
import java.util.*

open class ImageSelectorHelper(private val activity: Activity) {
    companion object {
        private lateinit var CAMERA: String
        private lateinit var DICTIONARY: String
        fun getImageAndSave(path: String?, savePath: String?): Bitmap? {
            if (TextUtils.isEmpty(path)) {
                return null
            }
            val degree = CommonUtil.getExifOrientation(path)
            try {
                val source = ImageUtil.getBitmapByPath(path)
                if (source != null && savePath != null) {
                    val tmp = getMaxBitmap(source)
                    if (tmp != source) {
                        source.recycle()
                    }
                    ImageUtil.saveImage(File(savePath), tmp, 100)
                    tmp?.recycle()
                }
                var photoBmp = PictureUtils.getSmallBitmap(path, 500, 500)
                if (degree == 90 || degree == 180 || degree == 270) { //Roate preview icon according to exif orientation
                    val matrix = Matrix()
                    matrix.postRotate(degree.toFloat())
                    val realBitmap = Bitmap.createBitmap(photoBmp, 0, 0, photoBmp.width, photoBmp.height, matrix, true)
                    photoBmp.recycle()
                    photoBmp = realBitmap
                }
                return photoBmp
            } catch (e: IOException) {
            }
            return null
        }
    }

    private val selectTypes: MutableList<String> = ArrayList(2)
    private val imageSelectors: MutableList<ImageSelector>
    private val onImageGetListenerList: MutableList<OnImageGetListener> = ArrayList()
    private var cropSize: Size? = null

    init {
        imageSelectors = ArrayList()
        DICTIONARY = activity.getString(R.string.select_picture)
        CAMERA = activity.getString(R.string.take_picture)
        run {
            selectTypes.add(DICTIONARY)
            selectTypes.add(CAMERA)
        }
    }

    @JvmOverloads
    fun showSelectPhoto(activity: Activity, permissionHelper: PermissionHelper, cropSize: Size? = null) {
        this.cropSize = cropSize
        permissionHelper.requestCameraPermissions(Runnable {
            permissionHelper.requestStoragePermissions(Runnable {
                DeepControllerWindow(activity, "", null, selectTypes, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        if (CAMERA == item) {
                            //启动摄像机拍照
                            val imageUri: Uri?
                            val openCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val imageFile = File(CommonUtil.getCatchFilePath(activity))
                            if (!imageFile.exists()) {
                                val imageFileCreate = imageFile.mkdirs()
                            }
                            val file = File(imageFile, ConstData.TEMP_IMG_NAME)
                            // 判断版本大于等于7.0
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                imageUri = FileProvider.getUriForFile(activity, "com.fbsex.exchange.fileProvider", file)
                                // 给目标应用一个临时授权
                                openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } else {
                                imageUri = Uri.fromFile(file)
                            }
                            //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                            openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            activity.startActivityForResult(openCameraIntent, ConstData.TAKE_PICTURE)
                        } else if (DICTIONARY == item) { //打开文件列表选择
                            val openAlbumIntent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            activity.startActivityForResult(openAlbumIntent, ConstData.CHOOSE_PICTURE)
                        }
                    }

                }).show()
            })
        })
    }

    fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            var path: String? = null
            when (requestCode) {
                ConstData.TAKE_PICTURE -> {
                    val file = File(CommonUtil.getCatchFilePath(activity), ConstData.TEMP_IMG_NAME)
                    if (cropSize != null) {
                        var imageUri: Uri? = null
                        // 判断版本大于等于7.0
                        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(activity!!, "com.fbsex.exchange.fileProvider", file)
                        } else {
                            Uri.fromFile(file)
                        }
                        cropImage(imageUri, cropSize!!.width, cropSize!!.height, ConstData.CROP_PICTURE)
                        cropSize = null
                        return
                    }
                    val uri = Uri.parse(file.absolutePath)
                    path = uri.path
                    PictureUtils.galleryAddPic(file.absolutePath, activity)
                }
                ConstData.CHOOSE_PICTURE -> {
                    val uri = data?.data
                    if (cropSize != null) {
                        cropImage(uri, cropSize!!.width, cropSize!!.height, ConstData.CROP_PICTURE)
                        cropSize = null
                        return
                    }
                    val sdkVersion = Integer.valueOf(Build.VERSION.SDK)
                    path = if (sdkVersion >= 19) {
                        PictureUtils.getPath_above19(activity, uri)
                    } else {
                        PictureUtils.getFilePath_below19(activity, uri)
                    }
                }
                ConstData.CROP_PICTURE -> path = cropImagePath
            }
            for (imageSelector in imageSelectors) {
                imageSelector.onIamgePathGot(path)
            }
            for (onImageGetListener in onImageGetListenerList) {
                onImageGetListener.onImageGet(path)
            }
        }
    }

    private val cropImagePath: String
        get() = CommonUtil.getCatchFilePath(activity) + "/crop.jpg"

    //截取图片
    private fun cropImage(uri: Uri?, outputX: Int, outputY: Int, requestCode: Int) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        val file = File(cropImagePath)
        val outUri: Uri?
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // "com.fbsex.exchange.fileProvider"即是在清单文件中配置的authorities
            outUri = FileProvider.getUriForFile(activity, "com.fbsex.exchange.fileProvider", file)
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            outUri = Uri.fromFile(file)
        }
        val resInfoList: List<*> = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resInfoList.isEmpty()) {
            return
        }
        val resInfoIterator = resInfoList.iterator()
        while (resInfoIterator.hasNext()) {
            val resolveInfo = resInfoIterator.next() as ResolveInfo
            val packageName = resolveInfo.activityInfo.packageName
            activity.grantUriPermission(packageName, outUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", outputX)
        intent.putExtra("outputY", outputY)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)
        activity.startActivityForResult(intent, requestCode)
    }

    fun clear() {
        for (imageSelector in imageSelectors) {
            imageSelector.setImageSelectorHelper(null)
        }
        imageSelectors.clear()
        onImageGetListenerList.clear()
    }

    fun addImageSelector(imageSelector: ImageSelector?) {
        if (imageSelector != null) {
            imageSelectors.add(imageSelector)
            imageSelector.setImageSelectorHelper(this)
        }
    }

    fun removeImageSelector(imageSelector: ImageSelector?) {
        if (imageSelector != null) {
            imageSelectors.remove(imageSelector)
            imageSelector.setImageSelectorHelper(null)
        }
    }

    fun addOnImageGetListener(onImageGetListener: OnImageGetListener?) {
        if (onImageGetListener != null && !onImageGetListenerList.contains(onImageGetListener)) {
            onImageGetListenerList.add(onImageGetListener)
        }
    }

    interface OnImageGetListener {
        fun onImageGet(path: String?)
    }
}