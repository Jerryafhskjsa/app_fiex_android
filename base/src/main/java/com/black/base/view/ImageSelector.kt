package com.black.base.view

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.black.base.R
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.getMaxBitmap
import com.black.base.util.RouterConstData
import com.black.lib.permission.PermissionHelper
import com.black.router.BlackRouter
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.PictureUtils
import java.io.File
import java.io.IOException
import java.util.*

class ImageSelector(private val activity: Activity, permissionHelper: PermissionHelper, parent: ViewGroup?, private val imageFileName: String) {
    companion object {
        private lateinit var CAMERA: String
        private lateinit var DICTIONARY: String
    }

    private val selectTypes: MutableList<String> = ArrayList(2)
    private var imageSelectorHelper: ImageSelectorHelper? = null
    var path: String? = null
        private set
    private var sourcePath: String? = null
    private val imageLayout: View
    private val photoImageView: ImageView
    private val photoImageDeleteView: ImageView

    init {
        DICTIONARY = activity.getString(R.string.select_picture)
        CAMERA = activity.getString(R.string.take_picture)
        run {
            selectTypes.add(DICTIONARY)
            selectTypes.add(CAMERA)
        }
        imageLayout = View.inflate(activity, R.layout.view_image_select, parent)
        imageLayout.setOnClickListener {
            if (path == null) {
                if (imageSelectorHelper != null) {
                    imageSelectorHelper?.showSelectPhoto(activity, permissionHelper)
                }
            } else {
                showBigImage()
            }
        }
        photoImageView = imageLayout.findViewById(R.id.real_name_img)
        photoImageDeleteView = imageLayout.findViewById(R.id.real_name_img_delete)
        photoImageDeleteView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (path != null) {
                    removePhotoImageItem()
                }
            }
        })
        show(null)
    }

    fun setImageSelectorHelper(imageSelectorHelper: ImageSelectorHelper?) {
        this.imageSelectorHelper = imageSelectorHelper
    }

    private fun showBigImage() {
        if (!TextUtils.isEmpty(sourcePath)) {
            val bundle = Bundle()
            bundle.putString(ConstData.IMAGE_LOCAL_PATH, sourcePath)
            BlackRouter.getInstance().build(RouterConstData.SHOW_BIG_IMAGE).with(bundle).go(activity)
        }
    }

    //刷新照片选择视图
    fun show(path: String?) {
        val bitmap = getImage(path)
        if (bitmap == null) {
            photoImageView.setImageResource(R.drawable.icon_real_name_img_default)
            photoImageDeleteView.visibility = View.GONE
        } else {
            photoImageView.setImageBitmap(bitmap)
            photoImageDeleteView.visibility = View.VISIBLE
        }
    }

    fun onIamgePathGot(path: String?) {
        sourcePath = path
        show(path)
    }

    private val currentImagePath: String
        private get() = CommonUtil.getCatchFilePath(activity) + File.separator + imageFileName

    private fun getImage(path: String?): Bitmap? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        try {
            this.path = currentImagePath
            //            String savePath = FryingUtil.getCatchFilePath(mContext) + File.separator + ConstData.TEMP_IMG_NAME_02;
//            ImageUtil.saveImage(FryingUtil.getCatchFilePath(mContext), getCurrentImageFileName(), photoBmp);
            val degree = CommonUtil.getExifOrientation(path)
            val source = ImageUtil.getBitmapByPath(path)
            if (source != null && this.path != null) {
                var tmp = getMaxBitmap(source)
                tmp?.let {
                    if (it != source) {
                        source.recycle()
                    }
                    if (degree == 90 || degree == 180 || degree == 270) {
                        //Roate preview icon according to exif orientation
                        val matrix = Matrix()
                        matrix.postRotate(degree.toFloat())
                        val realBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                        it.recycle()
                        tmp = realBitmap
                    }
                    ImageUtil.saveImage(File(this.path), tmp, 100)
                    tmp?.recycle()
                }
            }
            //            FileUtil.copyFile(savePath, path);
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
            CommonUtil.printError(activity, e)
        }
        return null
    }

    //删除照片选择视图
    private fun removePhotoImageItem() {
        path = null
        sourcePath = null
        show(null)
    }
}