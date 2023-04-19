package com.black.c2c.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.c2c.C2CMessage
import com.black.base.model.c2c.C2COrderDetails
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CMessageAdapter
import com.black.c2c.databinding.ActivityC2cBuyerOderBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.PictureUtils
import skin.support.content.res.SkinCompatResources
import java.io.File
import java.io.IOException
import java.util.*

@Route(value = [RouterConstData.C2C_BUYER])
class C2CBuyerOderActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var CAMERA: String? = null
        private var DICTIONARY: String? = null
    }
    private val selectTypes: MutableList<String?> = ArrayList(2)
    private var binding: ActivityC2cBuyerOderBinding? = null
    private var id2: String? = null
    private var totalTime: Long = 15*60*1000 //总时长 15min
    private var countDownTimer: CountDownTimer? = null
    private var paychain: String? = null
    private val photoImageList: MutableList<PhotoImageItem> = ArrayList()
    private var adapter: C2CMessageAdapter? = null
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

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DICTIONARY = getString(R.string.select_picture)
        CAMERA = getString(R.string.take_picture)
        run {
            selectTypes.add(DICTIONARY)
            selectTypes.add(CAMERA)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buyer_oder)
        id2 = intent.getStringExtra(ConstData.BUY_PRICE)
        paychain = intent.getStringExtra(ConstData.USER_YES)
        binding?.add?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.send?.setOnClickListener(this)
        binding?.phone?.setOnClickListener(this)
        binding?.putMessage?.addTextChangedListener(watcher)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.send?.setOnClickListener(this)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = C2CMessageAdapter(mContext, BR.listItemC2CSallerBuyModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.refreshLayout?.isFocusable = false
        binding?.refreshLayout?.isNestedScrollingEnabled = false
        binding?.refreshLayout?.setRefreshing(true)
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext))
        binding?.refreshLayout?.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        getC2COIV2(id2)
        getC2cList()
        checkClickable()
    }
    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
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
            path?.let { getImage(it)
                submitRealNameAuthenticate()}
        }
    }
    private val currentImageFileName: String?
        get() {
            when (photoImageList.indexOf(thisItem)) {
                0 -> return ConstData.TEMP_IMG_NAME_01
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

        } catch (e: IOException) {
            CommonUtil.printError(applicationContext, e)
        }
    }

    private var thisItem: PhotoImageItem? = null
    private fun showSelectPhoto(photoImageItem: PhotoImageItem) {
        thisItem = photoImageItem
        DeepControllerWindow(
            mContext as Activity,
            "",
            null,
            selectTypes,
            object : DeepControllerWindow.OnReturnListener<String?> {
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
                            imageUri = FileProvider.getUriForFile(
                                mContext,
                                "com.fbsex.exchange.fileProvider",
                                file
                            )
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
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(openAlbumIntent, ConstData.CHOOSE_PICTURE)
                    }
                }

            }).show()
    }

    private fun submitRealNameAuthenticate() {
        val pathList: MutableList<String?> = ArrayList(1)
        for (item in photoImageList) {
            if (item.path != null) {
                pathList.add(item.path)
                getC2cImage(item.path!!)
            }
        }

        val needUploadCount = pathList.size
        upLoadCount = 0
        val imgUrlList: MutableList<String> = ArrayList(1)
        upLoadPhoto(pathList, object : Callback<String?>() {
            override fun callback(result: String?) {
                upLoadCount++
                result?.also {
                    imgUrlList.add(result)
                }
                if (upLoadCount >= needUploadCount) {
                    val imageStrings = StringBuilder()
                    for (imUrl in imgUrlList) {
                        if (imageStrings.isEmpty()) {
                            imageStrings.append(imgUrlList)
                        } else {
                            imageStrings.append(",").append(imgUrlList)
                        }
                    }
                }
            }

            override fun error(type: Int, error: Any) {}
        })
    }

        var upLoadCount = 0
        private fun upLoadPhoto(pathList: List<String?>, callBack: Callback<String?>?) {
            for (path in pathList) { //先上传图片，根据返回的路径，进行验证
                val fileParams: MutableMap<String, File> = HashMap()
                fileParams["file"] = File(path)
                UserApiServiceHelper.upload(mContext, "file", File(path), object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //上传成功
                            callBack?.callback(returnData.data)
                        } else {
                            FryingUtil.showToast(mContext, returnData?.msg)
                        }
                    }
                })
            }
        }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm) {
            val extras = Bundle()
            extras.putString(ConstData.BUY_PRICE, id2)
            extras.putString(ConstData.USER_YES,paychain)
            BlackRouter.getInstance().build(RouterConstData.C2C_PAY_FOR).with(extras).go(mContext)
        }

        if (id == R.id.send){
            getC2cText()
        }
        if (id == R.id.add){
            val item = PhotoImageItem()
            val imageLayout = findViewById<View>(R.id.add)
            item.imageLayout = imageLayout
            photoImageList.add(item)
            showSelectPhoto(item)
        }
    }
    private fun checkClickable(){

    }
    //回复图片
    private fun getC2cImage(path: String){
            val fileParams: MutableMap<String, File> = HashMap()
            fileParams["file"] = File(path)
        C2CApiServiceHelper.getC2CImage(mContext, id2 ,"file", File(path), object : NormalCallback<HttpRequestResultData<C2CMessage?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CMessage?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    getC2cList()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })

    }

    //回复文本
    private fun getC2cText(){
        val text = binding?.putMessage?.text?.toString()
        C2CApiServiceHelper.getC2CText(mContext, id2 ,text, object : NormalCallback<HttpRequestResultData<C2CMessage?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CMessage?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    binding?.putMessage?.setText("")
                    getC2cList()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //回复列表
    private fun getC2cList(){
        adapter?.clear()
        C2CApiServiceHelper.getC2CList(mContext, id2 , object : NormalCallback<HttpRequestResultDataList<C2CMessage?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultDataList<C2CMessage?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val data = returnData.data
                    if (data != null) {
                        adapter?.addAll(data)
                        adapter?.notifyDataSetChanged()
                    }
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

  /*  //拉去更新
    private fun getC2cPull(){
        C2CApiServiceHelper.getC2CPull(mContext, id2 ,null, object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //实时更新
    private fun getC2cTime(){
        C2CApiServiceHelper.getC2CTime(mContext , object : NormalCallback<HttpRequestResultString?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
*/
    //订单详情
    fun getC2COIV2(id: String?){
        C2CApiServiceHelper.getC2CDetails(
            mContext,
            id,
            object : NormalCallback<HttpRequestResultData<C2COrderDetails?>?>(mContext) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultData<C2COrderDetails?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding?.seller?.text = returnData.data?.otherSideRealName
                        binding?.sellerFirst?.text = returnData.data?.otherSideRealName!![0].toString()
                        val time1 = returnData.data?.validTime?.time
                        val calendar: Calendar = Calendar.getInstance()
                        val time2 = calendar.time.time
                        totalTime = time1!!.minus(time2)
                        if (time2 <= 0)
                        {
                            binding?.btnConfirm?.visibility = View.GONE
                        }
                        countDownTimer = object : CountDownTimer(totalTime,1000){//1000ms运行一次onTick里面的方法
                        override fun onFinish(){
                            FryingUtil.showToast(mContext, getString(R.string.cancel1))
                            binding?.btnConfirm?.visibility = View.GONE
                            binding?.time?.setText("0:00")
                        }
                            override fun onTick(millisUntilFinished: Long) {
                                val minute = millisUntilFinished / 1000 / 60 % 60
                                val second = millisUntilFinished / 1000 % 60
                                binding?.time?.setText("$minute:$second")
                            }
                        }.start()
                    } else {

                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }
}