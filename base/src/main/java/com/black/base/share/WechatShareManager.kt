package com.black.base.share

import android.content.Context
import android.graphics.Bitmap
import com.black.base.R
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.showToast
import com.black.util.ImageUtil
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WechatShareManager private constructor(private val mContext: Context) {
    companion object {
        private const val THUMB_SIZE = 150
        const val WECHAT_SHARE_WAY_TEXT = 1 //文字
        const val WECHAT_SHARE_WAY_PICTURE = 2 //图片
        const val WECHAT_SHARE_WAY_WEBPAGE = 3 //链接
        const val WECHAT_SHARE_WAY_VIDEO = 4 //视频
        const val WECHAT_SHARE_TYPE_TALK = SendMessageToWX.Req.WXSceneSession //会话
        const val WECHAT_SHARE_TYPE_FRENDS = SendMessageToWX.Req.WXSceneTimeline //朋友圈
        private var mInstance: WechatShareManager? = null
        /**
         * 获取WeixinShareManager实例
         * 非线程安全，请在UI线程中操作
         *
         * @return
         */
        fun getInstance(context: Context): WechatShareManager? {
            if (mInstance == null) {
                mInstance = WechatShareManager(context)
            }
            return mInstance
        }
    }

    private var mShareContentText: ShareContent? = null
    private var mShareContentPicture: ShareContent? = null
    private var mShareContentWebpag: ShareContent? = null
    private var mShareContentVideo: ShareContent? = null
    private var mWXApi: IWXAPI? = null

    init {
        //初始化数据
        //初始化微信分享代码
        initWechatShare(mContext)
    }

    private fun initWechatShare(context: Context) {
        if (mWXApi == null) {
            mWXApi = WXAPIFactory.createWXAPI(context, ConstData.WECHAT_APP_ID, true)
        }
        mWXApi!!.registerApp(ConstData.WECHAT_APP_ID)
    }

    /**
     * 通过微信分享
     *
     * @param shareContent 分享的方式（文本、图片、链接）
     * @param shareType    分享的类型（朋友圈，会话）
     */
    fun shareByWebchat(shareContent: ShareContent, shareType: Int) {
        when (shareContent.shareWay) {
            WECHAT_SHARE_WAY_TEXT -> shareText(shareContent, shareType)
            WECHAT_SHARE_WAY_PICTURE -> sharePicture(shareContent, shareType)
            WECHAT_SHARE_WAY_WEBPAGE -> shareWebPage(shareContent, shareType)
            WECHAT_SHARE_WAY_VIDEO -> shareVideo(shareContent, shareType)
        }
    }

    inner abstract class ShareContent {
        abstract val shareWay: Int
        abstract val content: String?
        abstract val title: String?
        abstract val uRL: String?
        abstract val pictureBitmap: Bitmap?
    }

    /**
     * 设置分享文字的内容
     *
     * @author chengcj1
     */
    inner class ShareContentText(override val content: String?) : ShareContent() {
        override val shareWay: Int
            get() = WECHAT_SHARE_WAY_TEXT

        override val title: String?
            get() = null

        override val uRL: String?
            get() = null

        override val pictureBitmap: Bitmap?
            get() = null

    }

    /*
     * 获取文本分享对象
     */
    fun getShareContentText(content: String?): ShareContent? {
        if (mShareContentText == null) {
            mShareContentText = ShareContentText(content)
        }
        return mShareContentText as ShareContentText?
    }

    /**
     * 设置分享图片的内容
     *
     * @author chengcj1
     */
    inner class ShareContentPicture(override val pictureBitmap: Bitmap?) : ShareContent() {
        override val shareWay: Int
            get() = WECHAT_SHARE_WAY_PICTURE

        override val content: String?
            get() = null

        override val title: String?
            get() = null

        override val uRL: String?
            get() = null

    }

    /*
     * 获取图片分享对象
     */
    fun getShareContentPicture(pictureResource: Bitmap?): ShareContent? {
        if (mShareContentPicture == null) {
            mShareContentPicture = ShareContentPicture(pictureResource)
        }
        return mShareContentPicture as ShareContentPicture?
    }

    /**
     * 设置分享链接的内容
     *
     * @author chengcj1
     */
    inner class ShareContentWebpage(override val title: String, override val content: String, override val uRL: String, override val pictureBitmap: Bitmap) : ShareContent() {
        override val shareWay: Int
            get() = WECHAT_SHARE_WAY_WEBPAGE
    }

    /*
     * 获取网页分享对象
     */
    fun getShareContentWebpag(title: String?, content: String?, url: String?, pictureResource: Bitmap?): ShareContent? {
        if (mShareContentWebpag == null) {
            mShareContentWebpag = ShareContentWebpage(title!!, content!!, url!!, pictureResource!!)
        }
        return mShareContentWebpag as ShareContentWebpage?
    }

    /**
     * 设置分享视频的内容
     *
     * @author chengcj1
     */
    inner class ShareContentVideo(override val uRL: String) : ShareContent() {
        override val shareWay: Int
            get() = WECHAT_SHARE_WAY_VIDEO

        override val content: String?
            get() = null

        override val title: String?
            get() = null

        override val pictureBitmap: Bitmap?
            get() = null

    }

    /*
     * 获取视频分享内容
     */
    fun getShareContentVideo(url: String?): ShareContent? {
        if (mShareContentVideo == null) {
            mShareContentVideo = ShareContentVideo(url!!)
        }
        return mShareContentVideo as ShareContentVideo?
    }

    /*
     * 分享文字
     */
    private fun shareText(shareContent: ShareContent, shareType: Int) {
        val text = shareContent.content
        //初始化一个WXTextObject对象
        val textObj = WXTextObject()
        textObj.text = text
        //用WXTextObject对象初始化一个WXMediaMessage对象
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.description = text
        //构造一个Req
        val req = SendMessageToWX.Req()
        //transaction字段用于唯一标识一个请求
        req.transaction = buildTransaction("textshare")
        req.message = msg
        //发送的目标场景， 可以选择发送到会话 WXSceneSession 或者朋友圈 WXSceneTimeline。 默认发送到会话。
        req.scene = shareType
        mWXApi!!.sendReq(req)
    }

    /*
     * 分享图片
     */
    private fun sharePicture(shareContent: ShareContent, shareType: Int) {
        val bitmap = shareContent.pictureBitmap
        val imgObj = WXImageObject(bitmap)
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true)
        bitmap?.recycle()
        msg.thumbData = ImageUtil.getBitmapBytes(thumbBitmap) //设置缩略图
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("imgshareappdata")
        req.message = msg
        req.scene = shareType
        mWXApi!!.sendReq(req)
    }

    /*
     * 分享链接
     */
    private fun shareWebPage(shareContent: ShareContent, shareType: Int) {
        val webpage = WXWebpageObject()
        webpage.webpageUrl = shareContent.uRL
        val msg = WXMediaMessage(webpage)
        msg.title = shareContent.title
        msg.description = shareContent.content
        val thumb = shareContent.pictureBitmap
        if (thumb == null) {
            showToast(mContext, mContext.getString(R.string.no_image))
        } else {
            msg.thumbData = ImageUtil.getBitmapBytes(thumb)
        }
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("webpage")
        req.message = msg
        req.scene = shareType
        mWXApi!!.sendReq(req)
    }

    /*
     * 分享视频
     */
    private fun shareVideo(shareContent: ShareContent, shareType: Int) {
        val video = WXVideoObject()
        video.videoUrl = shareContent.uRL
        val msg = WXMediaMessage(video)
        msg.title = shareContent.title
        msg.description = shareContent.content
        val thumb = shareContent.pictureBitmap
        //		BitmapFactory.decodeStream(new URL(video.videoUrl).openStream());
        /**
         * 测试过程中会出现这种情况，会有个别手机会出现调不起微信客户端的情况。造成这种情况的原因是微信对缩略图的大小、title、description等参数的大小做了限制，所以有可能是大小超过了默认的范围。
         * 一般情况下缩略图超出比较常见。Title、description都是文本，一般不会超过。
         */
        val thumbBitmap = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true)
        thumb?.recycle()
        msg.thumbData = ImageUtil.getBitmapBytes(thumbBitmap)
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("video")
        req.message = msg
        req.scene = shareType
        mWXApi!!.sendReq(req)
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }
}
