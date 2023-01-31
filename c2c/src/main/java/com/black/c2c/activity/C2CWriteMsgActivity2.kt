package com.black.c2c.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cIdCardsOneBinding
import com.black.c2c.databinding.ActivitySellerChooseBinding
import com.black.c2c.databinding.ViewFirstC2cBinding
import com.black.c2c.databinding.ViewSecondC2cBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.PictureUtils
import java.io.File
import java.io.IOException
import java.util.ArrayList

@Route(value = [RouterConstData.C2C_MSG2])
class C2CWriteMsgActivity2: BaseActionBarActivity(), View.OnClickListener{
    companion object {
        private var CAMERA: String? = null
        private var DICTIONARY: String? = null
    }
    private val selectTypes: MutableList<String?> = ArrayList(2)
    private var binding: ActivityC2cIdCardsOneBinding? = null
    private val photoImageList: MutableList<PhotoImageItem> = ArrayList()
    internal inner class PhotoImageItem {
        var path: String? = null
        var parent: ViewGroup? = null
        var imageLayout: View? = null
        var photoImageView: ImageView? = null
        var photoImageDeleteView: ImageView? = null
        fun show(parent: ViewGroup?) {
            parent?.removeAllViews()
            parent?.addView(imageLayout)
            this.parent = parent
            if (path == null) {
                photoImageDeleteView?.visibility = View.GONE
            } else {
                photoImageDeleteView?.visibility = View.VISIBLE
                photoImageView?.isEnabled = false
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DICTIONARY = getString(R.string.select_picture)
        CAMERA = getString(R.string.take_picture)
        run {
            selectTypes.add(DICTIONARY)
            selectTypes.add(CAMERA)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_id_cards_one)
        binding?.appBarLayout?.findViewById<ImageButton>(R.id.img_action_bar_right)?.visibility = View.VISIBLE
        binding?.bar?.setOnClickListener(this)
        binding?.btnSubmit?.setOnClickListener(this)
        binding?.one?.setOnClickListener(this)
        binding?.two?.setOnClickListener(this)
        binding?.three?.setOnClickListener(this)
        binding?.four?.setOnClickListener(this)
    }

    override fun getTitleText(): String? {
        return  "身份认证"
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.bar){
            binding?.bar?.isChecked = binding?.bar?.isChecked == false
        }
        if (id == R.id.btn_submit){
            if (binding?.bar?.isChecked == true) {
                BlackRouter.getInstance().build(RouterConstData.C2C_WATTING).go(this)
            }
            else{
                FryingUtil.showToast(mContext,"请先阅读《广告发布协议》")
            }
        }
        if (id == R.id.one){
            val item = PhotoImageItem()
            val imageLayout = binding?.one
            item.imageLayout = imageLayout
            showSelectPhoto(item)
        }
        if (id == R.id.two){
            val item = PhotoImageItem()
            val imageLayout = binding?.two
            item.imageLayout = imageLayout
            showSelectPhoto(item)
        }
        if (id == R.id.three){
            val item = PhotoImageItem()
            val imageLayout = binding?.three
            item.imageLayout = imageLayout
            showSelectPhoto(item)
        }
        if (id == R.id.four){
            val item = PhotoImageItem()
            val imageLayout = binding?.four
            item.imageLayout = imageLayout
            showSelectPhoto(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            var path: String? = null
            when (requestCode) {
                ConstData.TAKE_PICTURE -> {
                    val file = File(CommonUtil.getCatchFilePath(mContext), ConstData.TEMP_IMG_NAME)
                    val uri = Uri.parse(file.absolutePath)
                    path = uri.path
                    PictureUtils.galleryAddPic(file.absolutePath, this)
                }
                ConstData.CHOOSE_PICTURE -> {
                    val uri = data?.data
                    val sdkVersion = Integer.valueOf(Build.VERSION.SDK)
                    path = if (sdkVersion >= 19) {
                        PictureUtils.getPath_above19(mContext, uri)
                    } else {
                        PictureUtils.getFilePath_below19(mContext, uri)
                    }
                }
                ConstData.CROP_PICTURE -> {
                }
            }
            path?.let { getImage(it) }
        }
    }



    private var thisItem: PhotoImageItem? = null
    private fun showSelectPhoto(photoImageItem: PhotoImageItem) {
        thisItem = photoImageItem
        DeepControllerWindow(mContext as Activity, "", null, selectTypes, object : DeepControllerWindow.OnReturnListener<String?> {
            override fun onReturn(window: DeepControllerWindow<String?>, item: String?) {
                window.dismiss()
                if (CAMERA == item) { //启动摄像机拍照
                    val imageUri: Uri?
                    val openCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val imageFile = File(CommonUtil.getCatchFilePath(mContext))
                    if (!imageFile.exists()) {
                        val imageFileCreate = imageFile.mkdirs()
                    }
                    val file = File(imageFile, ConstData.TEMP_IMG_NAME)
                    // 判断版本大于等于7.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        imageUri = FileProvider.getUriForFile(mContext!!, "com.fbsex.exchange.fileProvider", file)
                        // 给目标应用一个临时授权
                        openCameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } else {
                        imageUri = Uri.fromFile(file)
                    }
                    //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(openCameraIntent, ConstData.TAKE_PICTURE)
                } else if (DICTIONARY == item) { //打开文件列表选择
                    val openAlbumIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(openAlbumIntent, ConstData.CHOOSE_PICTURE)
                }
            }

        }).show()
    }

    private val currentImageFileName: String?
        get() {
            when (photoImageList.indexOf(thisItem)) {
                0 -> return ConstData.TEMP_IMG_NAME_01
                1 -> return ConstData.TEMP_IMG_NAME_02
                2 -> return ConstData.TEMP_IMG_NAME_03
                3 -> return ConstData.TEMP_IMG_NAME_04
            }
            return null
        }
    private val currentImagePath: String?
        get() {
            val fileName = currentImageFileName
            return if (fileName == null) null else CommonUtil.getCatchFilePath(mContext) + File.separator + fileName
        }
    private fun getImage(path: String) {
        val degree = CommonUtil.getExifOrientation(path)
        try {
            val savePath = currentImagePath
            val saveFileName = currentImageFileName
            //            String savePath = FryingUtil.getCatchFilePath(mContext) + File.separator + ConstData.TEMP_IMG_NAME_02;
//            ImageUtil.saveImage(FryingUtil.getCatchFilePath(mContext), getCurrentImageFileName(), photoBmp);
            val source = ImageUtil.getBitmapByPath(path)
            if (source != null && savePath != null) {
                val tmp = FryingUtil.getMaxBitmap(source)
                if (tmp != source) {
                    source.recycle()
                }
                ImageUtil.saveImage(File(savePath), tmp, 100)
                tmp?.recycle()
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
            if (thisItem != null) {
                thisItem?.path = savePath
                thisItem?.photoImageView?.setImageBitmap(photoBmp)
                thisItem?.parent?.tag = savePath
                thisItem?.show(thisItem?.parent)
            }
            //            ImageButton imageButton = null;
//            switch (selectTag) {
//                case SELECT_FRONT:
//                    imageButton = binding?.realNameLayout01?;
//                    break;
//                case SELECT_BACK:
//                    imageButton = binding?.realNameLayout02?;
//                    break;
//                case SELECT_PERSON:
//                    imageButton = binding?.realNameLayout03?;
//                    break;
//            }
//            if (imageButton != null && savePath != null) {
//                imageButton.setImageBitmap(photoBmp);
//                imageButton.setTag(savePath);
//            } else if (imageButton != null) {
//                imageButton.setImageBitmap(null);
//                imageButton.setTag(null);
//            }
        } catch (e: IOException) {
            CommonUtil.printError(applicationContext, e)
        }
    }
}