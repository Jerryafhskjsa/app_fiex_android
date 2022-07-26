package com.black.im.adapter.holders

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.manager.FaceManager.getCustomBitmap
import com.black.im.manager.FaceManager.getEmoji
import com.black.im.model.chat.MessageInfo
import com.black.im.photoview.PhotoViewActivity
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.video.VideoViewActivity
import com.black.im.widget.MessageLayoutUI
import com.tencent.imsdk.*
import java.io.File
import java.util.*

class MessageImageHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView, properties) {
    companion object {
        private const val DEFAULT_MAX_SIZE = 360
        private const val DEFAULT_RADIUS = 5
    }

    private val downloadEles: MutableList<String> = ArrayList()
    private var contentImage: ImageView? = null
    private var videoPlayBtn: ImageView? = null
    private var videoDurationText: TextView? = null
    private var mClicking = false
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_image
    }

    override fun initVariableViews() {
        contentImage = rootView.findViewById(R.id.content_image_iv)
        videoPlayBtn = rootView.findViewById(R.id.video_play_btn)
        videoDurationText = rootView.findViewById(R.id.video_duration_tv)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        msgContentFrame.background = null
        when (msg?.getMsgType()) {
            MessageInfo.MSG_TYPE_CUSTOM_FACE, MessageInfo.MSG_TYPE_CUSTOM_FACE + 1 -> performCustomFace(msg, position)
            MessageInfo.MSG_TYPE_IMAGE, MessageInfo.MSG_TYPE_IMAGE + 1 -> performImage(msg, position)
            MessageInfo.MSG_TYPE_VIDEO, MessageInfo.MSG_TYPE_VIDEO + 1 -> performVideo(msg, position)
        }
    }

    private fun performCustomFace(msg: MessageInfo?, position: Int) {
        videoPlayBtn?.visibility = View.GONE
        videoDurationText?.visibility = View.GONE
        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        contentImage?.layoutParams = params
        val elem = msg?.element as? TIMFaceElem ?: return
        var filter = String(elem.data)
        if (!filter.contains("@2x")) {
            filter += "@2x"
        }
        var bitmap = getCustomBitmap(elem.index, filter)
        if (bitmap == null) {
            // 自定义表情没有找到，用emoji再试一次
            bitmap = getEmoji(String(elem.data))
            if (bitmap == null) {
                // TODO 临时找的一个图片用来表明自定义表情加载失败
                contentImage?.setImageDrawable(rootView.context.resources.getDrawable(R.drawable.face_delete))
            } else {
                contentImage?.setImageBitmap(bitmap)
            }
        } else {
            contentImage?.setImageBitmap(bitmap)
        }
    }

    private fun getImageParams(params: ViewGroup.LayoutParams?, msg: MessageInfo): ViewGroup.LayoutParams? {
        if (msg.imgWidth == 0 || msg.imgHeight == 0) {
            return params
        }
        if (msg.imgWidth > msg.imgHeight) {
            params?.width = DEFAULT_MAX_SIZE
            params?.height = DEFAULT_MAX_SIZE * msg.imgHeight / msg.imgWidth
        } else {
            params?.width = DEFAULT_MAX_SIZE * msg.imgWidth / msg.imgHeight
            params?.height = DEFAULT_MAX_SIZE
        }
        return params
    }

    private fun resetParentLayout() {
        val parent = contentImage?.parent?.parent
        if (parent is FrameLayout) {
            parent.setPadding(17, 0, 13, 0)
        }
    }

    private fun performImage(msg: MessageInfo, position: Int) {
        contentImage?.layoutParams = getImageParams(contentImage?.layoutParams, msg)
        resetParentLayout()
        videoPlayBtn?.visibility = View.GONE
        videoDurationText?.visibility = View.GONE
        val elem = msg.element as? TIMImageElem ?: return
        val imgs: List<TIMImage> = elem.imageList
        if (!TextUtils.isEmpty(msg.dataPath)) {
            GlideEngine.loadCornerImage(contentImage, msg.dataPath, null, DEFAULT_RADIUS.toFloat())
        } else {
            for (i in imgs.indices) {
                val img = imgs[i]
                if (img.type == TIMImageType.Thumb) {
                    var check = true
                    synchronized(downloadEles) {
                        if (downloadEles.contains(img.uuid)) {
                            check = false
                        } else {
                            downloadEles.add(img.uuid)
                        }
                    }
                    if (!check) {
                        break
                    }
                    val path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + img.uuid
                    img.getImage(path, object : TIMCallBack {
                        override fun onError(code: Int, desc: String) {
                            downloadEles.remove(img.uuid)
                            TUIKitLog.e("MessageListAdapter img getImage", "$code:$desc")
                        }

                        override fun onSuccess() {
                            downloadEles.remove(img.uuid)
                            msg.dataPath = path
                            GlideEngine.loadCornerImage(contentImage, msg.dataPath, null, DEFAULT_RADIUS.toFloat())
                        }
                    })
                    break
                }
            }
        }
        contentImage?.setOnClickListener {
            for (i in imgs.indices) {
                val img = imgs[i]
                if (img.type == TIMImageType.Original) {
                    PhotoViewActivity.mCurrentOriginalImage = img
                    break
                }
            }
            val intent = Intent(appContext, PhotoViewActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(TUIKitConstants.IMAGE_DATA, msg.dataPath)
            intent.putExtra(TUIKitConstants.SELF_MESSAGE, msg.isSelf)
            appContext.startActivity(intent)
        }
        contentImage?.setOnLongClickListener { view ->
            if (onItemClickListener != null) {
                onItemClickListener?.onMessageLongClick(view, position, msg)
            }
            true
        }
    }

    private fun performVideo(msg: MessageInfo, position: Int) {
        contentImage?.layoutParams = getImageParams(contentImage?.layoutParams, msg)
        resetParentLayout()
        videoPlayBtn?.visibility = View.VISIBLE
        videoDurationText?.visibility = View.VISIBLE
        val elem = msg.element as? TIMVideoElem ?: return
        val video = elem.videoInfo
        if (!TextUtils.isEmpty(msg.dataPath)) {
            GlideEngine.loadCornerImage(contentImage, msg.dataPath, null, DEFAULT_RADIUS.toFloat())
        } else {
            val shotInfo = elem.snapshotInfo
            synchronized(downloadEles) {
                if (!downloadEles.contains(shotInfo.uuid)) {
                    downloadEles.add(shotInfo.uuid)
                }
            }
            val path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + elem.snapshotInfo.uuid
            elem.snapshotInfo.getImage(path, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    downloadEles.remove(shotInfo.uuid)
                    TUIKitLog.e("MessageListAdapter video getImage", "$code:$desc")
                }

                override fun onSuccess() {
                    downloadEles.remove(shotInfo.uuid)
                    msg.dataPath = path
                    GlideEngine.loadCornerImage(contentImage, msg.dataPath, null, DEFAULT_RADIUS.toFloat())
                }
            })
        }
        var durations = "00:" + video.duaration
        if (video.duaration < 10) {
            durations = "00:0" + video.duaration
        }
        videoDurationText?.text = durations
        val videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + video.uuid
        val videoFile = File(videoPath)
        //以下代码为zanhanding修改，用于fix视频消息发送失败后不显示红色感叹号的问题
        if (msg.status == MessageInfo.MSG_STATUS_SEND_SUCCESS) { //若发送成功，则不显示红色感叹号和发送中动画
            statusImage.visibility = View.GONE
            sendingProgress.visibility = View.GONE
        } else if (videoFile.exists() && msg.status == MessageInfo.MSG_STATUS_SENDING) { //若存在正在发送中的视频文件（消息），则显示发送中动画（隐藏红色感叹号）
            statusImage.visibility = View.GONE
            sendingProgress.visibility = View.VISIBLE
        } else if (msg.status == MessageInfo.MSG_STATUS_SEND_FAIL) { //若发送失败，则显示红色感叹号（不显示发送中动画）
            statusImage.visibility = View.VISIBLE
            sendingProgress.visibility = View.GONE
        }
        //以上代码为zanhanding修改，用于fix视频消息发送失败后不显示红色感叹号的问题
        msgContentFrame.setOnClickListener(View.OnClickListener {
            if (mClicking) {
                return@OnClickListener
            }
            sendingProgress.visibility = View.VISIBLE
            mClicking = true
            //以下代码为zanhanding修改，用于fix点击发送失败视频后无法播放，并且红色感叹号消失的问题
            val videoFile = File(videoPath)
            if (videoFile.exists()) { //若存在本地文件则优先获取本地文件
                mAdapter?.notifyItemChanged(position)
                mClicking = false
                play(msg)
                // 有可能播放的Activity还没有显示，这里延迟200ms，拦截压力测试的快速点击
                Handler(Looper.getMainLooper()).postDelayed({ mClicking = false }, 200)
            } else {
                getVideo(video, videoPath, msg, true, position)
            }
            //以上代码为zanhanding修改，用于fix点击发送失败视频后无法播放，并且红色感叹号消失的问题
        })
    }

    private fun getVideo(video: TIMVideo, videoPath: String, msg: MessageInfo, autoPlay: Boolean, position: Int) {
        video.getVideo(videoPath, object : TIMCallBack {
            override fun onError(code: Int, desc: String) {
                toastLongMessage("下载视频失败:$code=$desc")
                msg.status = MessageInfo.MSG_STATUS_DOWNLOADED
                sendingProgress.visibility = View.GONE
                statusImage.visibility = View.VISIBLE
                mAdapter?.notifyItemChanged(position)
                mClicking = false
            }

            override fun onSuccess() {
                mAdapter?.notifyItemChanged(position)
                if (autoPlay) {
                    play(msg)
                }
                // 有可能播放的Activity还没有显示，这里延迟200ms，拦截压力测试的快速点击
                Handler(Looper.getMainLooper()).postDelayed({ mClicking = false }, 200)
            }
        })
    }

    private fun play(msg: MessageInfo) {
        statusImage.visibility = View.GONE
        sendingProgress.visibility = View.GONE
        val intent = Intent(appContext, VideoViewActivity::class.java)
        intent.putExtra(TUIKitConstants.CAMERA_IMAGE_PATH, msg.dataPath)
        intent.putExtra(TUIKitConstants.CAMERA_VIDEO_PATH, msg.dataUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }
}