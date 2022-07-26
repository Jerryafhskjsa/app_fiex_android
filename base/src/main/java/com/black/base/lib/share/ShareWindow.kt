package com.black.base.lib.share

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.black.base.R
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.share.ShareParams
import com.black.base.share.SharePlatform
import com.black.base.util.FryingUtil.showToast
import com.black.lib.permission.PermissionHelper
import com.black.util.CommonUtil
import com.black.util.ImageUtil

abstract class ShareWindow : View.OnClickListener, PopupWindow.OnDismissListener {
    companion object {
        private const val TAG = "ShareWindow"
        const val TELEGRAN = "telegran"
        const val DOWNLOAD = "download"
        protected const val SHARE_TO_WECHAT = 1
        protected const val SHARE_TO_WECHAT_MOMENTS = 2
        protected const val SHARE_TO_TELEGRAN = 3
        protected const val SHARE_TO_DOWNLOAD = 4
        protected const val SHARE_IMAGE = 0x10
        protected const val SHARE_LINK = 0x20
    }

    protected var activity: Activity
    protected var permissionHelper: PermissionHelper
    protected var inflater: LayoutInflater
    protected var density = 1f
    private var popupWindow: PopupWindow?

    constructor(activity: BaseActionBarActivity) {
        this.activity = activity
        permissionHelper = activity
        inflater = LayoutInflater.from(activity)
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = inflater.inflate(R.layout.view_share_window, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow?.isFocusable = true
        popupWindow?.setBackgroundDrawable(PaintDrawable())
        popupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow?.animationStyle = R.style.anim_bottom_in_out
        popupWindow?.setOnDismissListener(this)
        val contentLayout = contentView.findViewById<LinearLayout>(R.id.share_content_layout)
        val shareContent = getShareContent()
        if (shareContent != null) {
            contentLayout.removeAllViews()
            contentLayout.addView(shareContent)
        }
        contentView.findViewById<View>(R.id.share_to_wechat).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_wechat_moments).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_telegran).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_download).setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_share_cancel).setOnClickListener(this)
    }

    constructor(activity: BaseActivity) {
        this.activity = activity
        permissionHelper = activity
        inflater = LayoutInflater.from(activity)
        val dm = activity.resources.displayMetrics
        density = dm.density
        val contentView = inflater.inflate(R.layout.view_share_window, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow?.isFocusable = true
        popupWindow?.setBackgroundDrawable(PaintDrawable())
        popupWindow?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow?.animationStyle = R.style.anim_bottom_in_out
        popupWindow?.setOnDismissListener(this)
        val contentLayout = contentView.findViewById<LinearLayout>(R.id.share_content_layout)
        val shareContent = getShareContent()
        if (shareContent != null) {
            contentLayout.removeAllViews()
            contentLayout.addView(shareContent)
        }
        contentView.findViewById<View>(R.id.share_to_wechat).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_wechat_moments).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_telegran).setOnClickListener(this)
        contentView.findViewById<View>(R.id.share_to_download).setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_share_cancel).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.share_to_wechat) {
            share(SharePlatform.PLATFORM_WECHAT)
        } else if (i == R.id.share_to_wechat_moments) {
            share(SharePlatform.PLATFORM_WECHAT_MOMENTS)
        } else if (i == R.id.share_to_telegran) {
            share(TELEGRAN)
        } else if (i == R.id.share_to_download) {
            share(DOWNLOAD)
        } else if (i == R.id.btn_share_cancel) {
            dismiss()
        }
    }

    protected abstract fun initShareContent()

    protected abstract fun getShareContent(): View?

    protected abstract fun getShareResult(): ShareAdapter?

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    val isShowing: Boolean
        get() = popupWindow != null && popupWindow!!.isShowing

    fun show() {
        popupWindow?.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        initShareContent()
    }

    fun dismiss() {
        if (isShowing) {
            popupWindow?.dismiss()
        }
    }

    private fun share(platformName: String) {
        val shareAdapter = getShareResult()
        if (shareAdapter != null) {
            shareAdapter.share(platformName)
        } else {
            showToast(activity, activity.getString(R.string.choose_share_content))
        }
    }

    protected abstract inner class ImageShare : ShareAdapter {
        override val shareContentType: Int
            get() = SHARE_IMAGE

        override fun share(shareType: String?) {
            val cacheBitmap = getShareBitmap()
            if (cacheBitmap != null) {
                if (SharePlatform.PLATFORM_WECHAT.equals(shareType, ignoreCase = true) || SharePlatform.PLATFORM_WECHAT_MOMENTS.equals(shareType, ignoreCase = true)) {
                    try {
                        val sharePlatform = SharePlatform.getPlatform(activity, shareType)
                        val shareParams = ShareParams()
                        shareParams.shareType = SharePlatform.SHARE_IMAGE
                        shareParams.image = cacheBitmap
                        sharePlatform.share(shareParams, null)
                    } catch (e: Exception) {
                    }
                } else if (TELEGRAN.equals(shareType, ignoreCase = true)) {
                } else if (DOWNLOAD.equals(shareType, ignoreCase = true)) {
                    permissionHelper.requestStoragePermissions(Runnable {
                        try { //                                ImageUtil.saveImage(CommonUtil.getCatchFileSavePath(activity), getSaveFileName(), cacheBitmap);
                            ImageUtil.saveImageToSysGallery(activity, saveFileName, cacheBitmap)
                            showToast(activity, activity.getString(R.string.save_success))
                        } catch (e: Exception) {
                            showToast(activity, activity.getString(R.string.save_failed))
                        }
                    })
                }
            } else {
                showToast(activity, activity.getString(R.string.save_failed))
            }
        }

        abstract fun getShareBitmap(): Bitmap?
        private val saveFileName: String
            get() {
                val now = System.currentTimeMillis()
                return CommonUtil.formatTimestamp("yyyyMMddHHmmss", now) + (Math.random() * 10000).toInt() + ".png"
            }
    }

    abstract inner class LinkShare : ShareAdapter {
        override val shareContentType: Int
            get() = SHARE_LINK

        override fun share(shareType: String?) {
            if (SharePlatform.PLATFORM_WECHAT.equals(shareType, ignoreCase = true) || SharePlatform.PLATFORM_WECHAT_MOMENTS.equals(shareType, ignoreCase = true)) {
                try {
                    val sharePlatform = SharePlatform.getPlatform(activity, shareType)
                    val shareParams = ShareParams()
                    shareParams.shareType = SharePlatform.SHARE_WEBPAGE
                    shareParams.title = getShareTitle()
                    shareParams.text = getShareText()
                    shareParams.image = getShareIcon()
                    shareParams.url = getShareLink()
                    sharePlatform.share(shareParams, null)
                    //                    Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
//                    Platform.ShareParams sp = new Platform.ShareParams();
//                    sp.setShareType(Platform.SHARE_WEBPAGE);
//                    sp.setTitle(getShareTitle());
//                    sp.setText(getShareText());
//                    sp.setImageData(getShareIcon());
//                    sp.setUrl(getShareLink());
//                    wechat.setPlatformActionListener(ShareWindow.this);
//                    wechat.share(sp);
                } catch (e: Exception) {
                }
            } else if (TELEGRAN.equals(shareType, ignoreCase = true)) {
            } else if (DOWNLOAD.equals(shareType, ignoreCase = true)) {
                showToast(activity, activity.getString(R.string.only_save_image))
            }
        }

        abstract fun getShareTitle(): String?

        abstract fun getShareText(): String?

        abstract fun getShareIcon(): Bitmap?

        abstract fun getShareLink(): String?
    }
}
