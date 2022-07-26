package com.black.user.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.lib.share.ShareAdapter
import com.black.base.lib.share.ShareWindow
import com.black.base.model.user.UserInfo
import com.black.base.util.FryingUtil
import com.black.user.R
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import java.util.*

class AdvanceShareNewWindow : ShareWindow {
    private val userInfo: UserInfo
    private var shareLink: String
    private var data: ArrayList<String?>?
    private val shareImageViews: MutableList<ShareImageView>? = ArrayList()
    private var selectedIndex = -1
    private var rootView: View? = null
    private var picLayout: LinearLayout? = null

    constructor(activity: BaseActionBarActivity, userInfo: UserInfo, inviteUrl: String, data: ArrayList<String?>?) : super(activity) {
        this.userInfo = userInfo
        this.data = data
        shareLink = inviteUrl + userInfo.inviteCode
    }

    constructor(activity: BaseActivity, userInfo: UserInfo, inviteUrl: String, data: ArrayList<String?>?) : super(activity) {
        this.userInfo = userInfo
        this.data = data
        shareLink = inviteUrl + userInfo.inviteCode
    }

    override fun initShareContent() {
        initShareImages()
    }

    override fun getShareContent(): View? {
        val shareContent = inflater.inflate(R.layout.view_share_advance, null)
        rootView = shareContent.findViewById(R.id.root_view)
        picLayout = shareContent.findViewById(R.id.pic_layout)
        initShareImages()
        return shareContent
    }

    override fun getShareResult(): ShareAdapter {
        return object : ImageShare() {
            override fun getShareBitmap(): Bitmap? {
                val shareImageView = CommonUtil.getItemFromList(shareImageViews, selectedIndex)
                return if (shareImageView == null) {
                    null
                } else {
                    shareImageView.bitmap!!
                }
            }
        }
    }

    private fun initShareImages() {
        if (data != null) {
            val loadingDialog = FryingUtil.getLoadDialog(activity, "")
            loadingDialog.show()
            initShareImages(0, Runnable {
                activity.runOnUiThread {
                    loadingDialog.dismiss()
                    if (shareImageViews != null && shareImageViews.isNotEmpty()) {
                        selectImageView(shareImageViews[0])
                    }
                }
            })
        }
    }

    private fun initShareImages(index: Int, end: Runnable) {
        val base64ImageData = CommonUtil.getItemFromList(data, index)
        if (base64ImageData != null) {
            insertShareImages(base64ImageData, Runnable { initShareImages(index + 1, end) })
        } else {
            end.run()
        }
    }

    private fun insertShareImages(base64ImageData: String, next: Runnable?) {
        val base64Image = getBase64Image(base64ImageData)
        if (base64Image != null) {
            activity.runOnUiThread {
                val shareImageView = createShareImageView(createShareImage(base64Image))
                shareImageViews!!.add(shareImageView)
                picLayout!!.addView(shareImageView.view)
                next?.run()
            }
        } else {
            next?.run()
        }
    }

    private fun getBase64Image(base64ImageData: String?): Bitmap? {
        return if (base64ImageData == null || base64ImageData.isEmpty()) {
            null
        } else try {
            val dataArr = base64ImageData.split(",").toTypedArray()
            val imageData = CommonUtil.getItemFromArray(dataArr, 1)
            if (imageData != null) {
                ImageUtil.base64ToBitmap(imageData)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun createShareImage(resBitmap: Bitmap?): Bitmap? {
        if (resBitmap == null) {
            return null
        }
        val mWidth = (375 * density).toInt()
        val mHeight = (667 * density).toInt()
        val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        canvas.drawBitmap(resBitmap, null, Rect(0, 0, mWidth, mHeight), paint)
        resBitmap.recycle()
        return bitmap
    }

    private fun selectImageView(thisView: ShareImageView) {
        selectedIndex = -1
        for (i in shareImageViews!!.indices) {
            val shareImageView = shareImageViews[i]
            if (shareImageView == thisView) {
                shareImageView.select()
                selectedIndex = i
            } else {
                shareImageView.unSelect()
            }
        }
        //滚动到位置
        val width = rootView!!.width - rootView!!.paddingLeft - rootView!!.paddingRight
        val scrollX = rootView!!.scrollX
        val childView = picLayout!!.getChildAt(selectedIndex)
        if (childView.right > width + scrollX) { //右边边距是否被遮挡
            rootView!!.scrollBy(childView.right - width - scrollX, 0)
        } else if (scrollX > childView.left) { //左边边距是否被遮挡
            rootView!!.scrollBy(childView.left - scrollX, 0)
        }
    }

    private fun createShareImageView(bitmap: Bitmap?): ShareImageView {
        val dm = activity.resources.displayMetrics
        val bitmapWidth = bitmap!!.width
        val bitmapHeight = bitmap.height
        val height = (445 * dm.density).toInt()
        val width = bitmapWidth * height / bitmapHeight
        val view = inflater.inflate(R.layout.view_share_image, null)
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        imageView.setImageBitmap(bitmap)
        var params = imageView.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
        } else {
            params.height = height
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        imageView.layoutParams = params
        val maskView = view.findViewById<View>(R.id.mask)
        val checkView = view.findViewById<View>(R.id.check)
        params = checkView.layoutParams
        if (params == null) {
            params = ViewGroup.LayoutParams(width, height)
        } else {
            params.height = height
            params.width = width
        }
        checkView.layoutParams = params
        val shareImageView = ShareImageView()
        shareImageView.view = view
        shareImageView.imageView = imageView
        shareImageView.checkView = checkView
        shareImageView.maskView = maskView
        shareImageView.bitmap = bitmap
        view.setOnClickListener { selectImageView(shareImageView) }
        shareImageView.maskView!!.invalidate()
        shareImageView.checkView!!.invalidate()
        shareImageView.view!!.invalidate()
        return shareImageView
    }

    internal inner class ShareImageView {
        var bitmap: Bitmap? = null
        var view: View? = null
        var imageView: ImageView? = null
        var checkView: View? = null
        var maskView: View? = null
        fun select() {
            maskView!!.visibility = View.GONE
            checkView!!.visibility = View.VISIBLE
        }

        fun unSelect() {
            maskView!!.visibility = View.VISIBLE
            checkView!!.visibility = View.GONE
        }
    }
}