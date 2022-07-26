package com.black.base.share

import android.content.Context
import android.graphics.Bitmap
import com.black.base.util.ConstData
import com.black.util.ImageUtil
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WechatPlatform private constructor(private val mContext: Context) : SharePlatform() {
    companion object {
        const val WECHAT_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession //会话
        private const val THUMB_SIZE = 150
        private var wechatPlatform: WechatPlatform? = null
        fun getInstance(context: Context): WechatPlatform {
            if (wechatPlatform == null) {
                wechatPlatform = WechatPlatform(context)
            }
            return wechatPlatform!!
        }
    }

    private var mWXApi: IWXAPI? = null
    private val isRegisted: Boolean
    private val isInstalled: Boolean

    init {
        if (mWXApi == null) {
            mWXApi = WXAPIFactory.createWXAPI(mContext, ConstData.WECHAT_APP_ID, true)
        }
        isRegisted = mWXApi?.registerApp(ConstData.WECHAT_APP_ID) ?: false
        isInstalled = mWXApi?.isWXAppInstalled ?: false
    }

    override fun share(shareParams: ShareParams?, listener: ShareResultListener?) {
        if (!isInstalled) { //未安装微信
            listener?.onError(NO_WECHAT, NullPointerException("no wechat"))
            return
        }
        if (!isRegisted) { //注册到微信失败
            listener?.onError(WECHAT_REG_FAILED, NullPointerException("register failed"))
            return
        }
        if (shareParams != null) {
            when (shareParams.shareType) {
                SHARE_IMAGE -> shareImage(shareParams, listener)
                SHARE_WEBPAGE -> shareWebPage(shareParams, listener)
            }
        }
    }

    /*
     * 分享图片
     */
    private fun shareImage(shareParams: ShareParams, listener: ShareResultListener?) {
        var bitmap = shareParams.image
        if (bitmap == null) {
            val bitmapPath = shareParams.imagePath
            bitmap = if (bitmapPath == null) null else ImageUtil.getBitmapByPath(bitmapPath)
        }
        if (bitmap == null) {
            listener?.onError(NO_IMAGE, NullPointerException("bitmap is null"))
            return
        }
        val imgObj = WXImageObject(bitmap)
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true)
        //        bitmap.recycle();
        msg.thumbData = ImageUtil.getBitmapBytes(thumbBitmap) //设置缩略图
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("imgshareappdata")
        req.message = msg
        req.scene = WECHAT_SHARE_TYPE_TALK
        mWXApi?.sendReq(req)
    }

    /*
     * 分享链接
     */
    private fun shareWebPage(shareParams: ShareParams, listener: ShareResultListener?) {
        val webpage = WXWebpageObject()
        webpage.webpageUrl = shareParams.url
        val msg = WXMediaMessage(webpage)
        msg.title = shareParams.title
        msg.description = shareParams.text
        var bitmap = shareParams.image
        if (bitmap == null) {
            val bitmapPath = shareParams.imagePath
            bitmap = if (bitmapPath == null) null else ImageUtil.getBitmapByPath(bitmapPath)
        }
        if (bitmap != null) {
            val thumbBitmap = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true)
            //            bitmap.recycle();
            msg.thumbData = ImageUtil.getBitmapBytes(thumbBitmap) //设置缩略图
        }
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("webpage")
        req.message = msg
        req.scene = WECHAT_SHARE_TYPE_TALK
        mWXApi?.sendReq(req)
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }
}