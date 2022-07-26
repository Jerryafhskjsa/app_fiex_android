package com.black.im.util

import android.net.Uri
import android.text.TextUtils
import com.black.im.R
import com.black.im.model.MessageTyping
import com.black.im.model.chat.MessageInfo
import com.black.im.util.DateTimeUtil.formatSeconds
import com.black.im.util.FileUtil.getPathFromUri
import com.black.im.util.FileUtil.getUriFromPath
import com.black.im.util.ImageUtil.getImageSize
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.tencent.imsdk.*
import skin.support.content.res.SkinCompatResources
import java.io.File
import java.nio.charset.Charset
import java.util.*

object MessageInfoUtil {
    const val GROUP_CREATE = "group_create"
    const val GROUP_DELETE = "group_delete"
    private val TAG = MessageInfoUtil::class.java.simpleName
    /**
     * 创建一条文本消息
     *
     * @param message 消息内容
     * @return
     */
    fun buildTextMessage(message: String?): MessageInfo {
        val info = MessageInfo()
        val TIMMsg = TIMMessage()
        val ele = TIMTextElem()
        ele.text = message
        TIMMsg.addElement(ele)
        info.extra = message
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.isSelf = true
        info.tIMMessage = TIMMsg
        info.fromUser = TIMManager.getInstance().loginUser
        info.setMsgType(MessageInfo.MSG_TYPE_TEXT)
        return info
    }

    /**
     * 创建一条自定义表情的消息
     *
     * @param groupId  自定义表情所在的表情组id
     * @param faceName 表情的名称
     * @return
     */
    fun buildCustomFaceMessage(groupId: Int, faceName: String?): MessageInfo {
        val info = MessageInfo()
        val TIMMsg = TIMMessage()
        val ele = TIMFaceElem()
        ele.index = groupId
        ele.data = faceName?.toByteArray()
        TIMMsg.addElement(ele)
        info.extra = "[自定义表情]"
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.isSelf = true
        info.tIMMessage = TIMMsg
        info.fromUser = TIMManager.getInstance().loginUser
        info.setMsgType(MessageInfo.MSG_TYPE_CUSTOM_FACE)
        return info
    }

    /**
     * 创建一条图片消息
     *
     * @param uri        图片URI
     * @param compressed 是否压缩
     * @return
     */
    fun buildImageMessage(uri: Uri?, compressed: Boolean): MessageInfo {
        val info = MessageInfo()
        val ele = TIMImageElem()
        info.dataUri = uri
        val size = getImageSize(uri)
        val path = getPathFromUri(uri!!)
        ele.path = path
        info.dataPath = path
        info.imgWidth = size[0]
        info.imgHeight = size[1]
        val TIMMsg = TIMMessage()
        TIMMsg.sender = TIMManager.getInstance().loginUser
        TIMMsg.setTimestamp(System.currentTimeMillis())
        if (!compressed) {
            ele.level = 0
        }
        TIMMsg.addElement(ele)
        info.isSelf = true
        info.tIMMessage = TIMMsg
        info.extra = "[图片]"
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.fromUser = TIMManager.getInstance().loginUser
        info.setMsgType(MessageInfo.MSG_TYPE_IMAGE)
        return info
    }

    /**
     * 创建一条视频消息
     *
     * @param imgPath   视频缩略图路径
     * @param videoPath 视频路径
     * @param width     视频的宽
     * @param height    视频的高
     * @param duration  视频的时长
     * @return
     */
    fun buildVideoMessage(imgPath: String?, videoPath: String?, width: Int, height: Int, duration: Long): MessageInfo {
        val info = MessageInfo()
        val TIMMsg = TIMMessage()
        val ele = TIMVideoElem()
        val video = TIMVideo()
        video.duaration = duration / 1000
        video.type = "mp4"
        val snapshot = TIMSnapshot()
        snapshot.width = width.toLong()
        snapshot.height = height.toLong()
        ele.setSnapshot(snapshot)
        ele.setVideo(video)
        ele.snapshotPath = imgPath
        ele.videoPath = videoPath
        TIMMsg.addElement(ele)
        val videoUri = Uri.fromFile(File(videoPath))
        info.isSelf = true
        info.imgWidth = width
        info.imgHeight = height
        info.dataPath = imgPath
        info.dataUri = videoUri
        info.tIMMessage = TIMMsg
        info.extra = "[视频]"
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.fromUser = TIMManager.getInstance().loginUser
        info.setMsgType(MessageInfo.MSG_TYPE_VIDEO)
        return info
    }

    /**
     * 创建一条音频消息
     *
     * @param recordPath 音频路径
     * @param duration   音频的时长
     * @return
     */
    fun buildAudioMessage(recordPath: String?, duration: Int): MessageInfo {
        val info = MessageInfo()
        info.dataPath = recordPath
        val TIMMsg = TIMMessage()
        TIMMsg.sender = TIMManager.getInstance().loginUser
        TIMMsg.setTimestamp(System.currentTimeMillis() / 1000)
        val ele = TIMSoundElem()
        ele.duration = duration / 1000.toLong()
        ele.path = recordPath
        TIMMsg.addElement(ele)
        info.isSelf = true
        info.tIMMessage = TIMMsg
        info.extra = "[语音]"
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.fromUser = TIMManager.getInstance().loginUser
        info.setMsgType(MessageInfo.MSG_TYPE_AUDIO)
        return info
    }

    /**
     * 创建一条文件消息
     *
     * @param fileUri 文件路径
     * @return
     */
    fun buildFileMessage(fileUri: Uri?): MessageInfo? {
        val filePath = getPathFromUri(fileUri!!)
        val file = File(filePath)
        if (file.exists()) {
            val info = MessageInfo()
            info.dataPath = filePath
            val TIMMsg = TIMMessage()
            val ele = TIMFileElem()
            TIMMsg.sender = TIMManager.getInstance().loginUser
            TIMMsg.setTimestamp(System.currentTimeMillis() / 1000)
            ele.path = filePath
            ele.fileName = file.name
            TIMMsg.addElement(ele)
            info.isSelf = true
            info.tIMMessage = TIMMsg
            info.extra = "[文件]"
            info.msgTime = System.currentTimeMillis() / 1000
            info.element = ele
            info.fromUser = TIMManager.getInstance().loginUser
            info.setMsgType(MessageInfo.MSG_TYPE_FILE)
            return info
        }
        return null
    }

    /**
     * 创建一条自定义消息
     *
     * @param data 自定义消息内容，可以是任何内容
     * @return
     */
    fun buildCustomMessage(data: String): MessageInfo {
        val info = MessageInfo()
        val TIMMsg = TIMMessage()
        val ele = TIMCustomElem()
        ele.data = data.toByteArray()
        TIMMsg.addElement(ele)
        info.isSelf = true
        info.tIMMessage = TIMMsg
        info.msgTime = System.currentTimeMillis() / 1000
        info.element = ele
        info.setMsgType(MessageInfo.MSG_TYPE_CUSTOM)
        info.fromUser = TIMManager.getInstance().loginUser
        return info
    }

    /**
     * 创建一条群消息自定义内容
     *
     * @param action  群消息类型，比如建群等
     * @param message 消息内容
     * @return
     */
    fun buildGroupCustomMessage(action: String, message: String): TIMMessage {
        val TIMMsg = TIMMessage()
        val ele = TIMCustomElem()
        ele.data = action.toByteArray()
        ele.ext = message.toByteArray()
        TIMMsg.addElement(ele)
        return TIMMsg
    }

    /**
     * 把SDK的消息bean列表转化为TUIKit的消息bean列表
     *
     * @param tIMMessages SDK的群消息bean列表
     * @param isGroup     是否是群消息
     * @return
     */
    fun TIMMessages2MessageInfos(tIMMessages: List<TIMMessage?>?, isGroup: Boolean): ArrayList<MessageInfo?>? {
        if (tIMMessages == null) {
            return null
        }
        val messageInfos = ArrayList<MessageInfo?>()
        for (i in tIMMessages.indices) {
            val tIMMessage = tIMMessages[i]
            val info = TIMMessage2MessageInfo(tIMMessage, isGroup)
            if (info != null) {
                messageInfos.addAll(info)
            }
        }
        return messageInfos
    }

    /**
     * 把SDK的消息bean转化为TUIKit的消息bean
     *
     * @param tIMMessage SDK的群消息bean
     * @param isGroup    是否是群消息
     * @return
     */
    fun TIMMessage2MessageInfo(tIMMessage: TIMMessage?, isGroup: Boolean): MutableList<MessageInfo?>? {
        if (tIMMessage == null || tIMMessage.status() == TIMMessageStatus.HasDeleted || tIMMessage.elementCount == 0) {
            return null
        }
        val list: MutableList<MessageInfo?> = ArrayList()
        for (i in 0 until tIMMessage.elementCount) {
            val msgInfo = MessageInfo()
            if (ele2MessageInfo(msgInfo, tIMMessage, tIMMessage.getElement(i), isGroup) != null) {
                if (!msgInfo.isBlankMessage) {
                    list.add(msgInfo)
                }
            }
        }
        return list
    }

    fun isTyping(tIMMessage: TIMMessage): Boolean { // 如果有任意一个element是正在输入，则认为这条消息是正在输入。除非测试，正常不可能发这种消息。
        for (i in 0 until tIMMessage.elementCount) {
            if (tIMMessage.getElement(i).type == TIMElemType.Custom) {
                val customElem = tIMMessage.getElement(i) as TIMCustomElem
                if (isTyping(customElem.data)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isTyping(data: ByteArray): Boolean {
        try {
            val str = String(data, Charset.forName("UTF-8"))
            val typing = Gson().fromJson(str, MessageTyping::class.java)
            return typing != null && typing.userAction == MessageTyping.TYPE_TYPING && TextUtils.equals(typing.actionParam, MessageTyping.EDIT_START)
        } catch (e: Exception) {
            TUIKitLog.e(TAG, "parse json error")
        }
        return false
    }

    private fun ele2MessageInfo(msgInfo: MessageInfo?, tIMMessage: TIMMessage?, ele: TIMElem?, isGroup: Boolean): MessageInfo? {
        if (msgInfo == null || tIMMessage == null || tIMMessage.status() == TIMMessageStatus.HasDeleted || tIMMessage.elementCount == 0 || ele == null || ele.type == TIMElemType.Invalid) {
            TUIKitLog.e(TAG, "ele2MessageInfo parameters error")
            return null
        }
        val sender = tIMMessage.sender
        msgInfo.tIMMessage = tIMMessage
        msgInfo.element = ele
        msgInfo.isGroup = isGroup
        msgInfo.id = tIMMessage.msgId
        msgInfo.uniqueId = tIMMessage.msgUniqueId
        msgInfo.isPeerRead = tIMMessage.isPeerReaded
        msgInfo.fromUser = sender
        if (isGroup) {
            val memberInfo = tIMMessage.senderGroupMemberProfile
            if (memberInfo != null && !TextUtils.isEmpty(memberInfo.nameCard)) {
                msgInfo.groupNameCard = memberInfo.nameCard
            } else { //                msgInfo.setGroupNameCard(sender);
            }
        }
        msgInfo.msgTime = tIMMessage.timestamp()
        msgInfo.isSelf = sender == TIMManager.getInstance().loginUser
        val type = ele.type
        if (type == TIMElemType.Custom) {
            val customElem = ele as TIMCustomElem
            val data = String(customElem.data)
            if (data == GROUP_CREATE) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_CREATE)
                val message = wrapperColor(if (TextUtils.isEmpty(msgInfo.groupNameCard)) msgInfo.fromUser else msgInfo.groupNameCard) + "创建群组"
                msgInfo.extra = message
            } else if (data == GROUP_DELETE) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_DELETE)
                msgInfo.extra = String(customElem.ext)
            } else {
                if (isTyping(customElem.data)) { // 忽略正在输入，它不能作为真正的消息展示
                    return null
                }
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_CUSTOM)
                msgInfo.extra = "[自定义消息]"
            }
        } else if (type == TIMElemType.GroupTips) {
            val groupTips = ele as TIMGroupTipsElem
            val tipsType = groupTips.tipsType
            var user: String? = ""
            if (groupTips.changedGroupMemberInfo.isNotEmpty()) {
                val ids: Array<Any> = groupTips.changedGroupMemberInfo.keys.toTypedArray()
                for (i in ids.indices) {
                    val userId = ids[i].toString()
                    val userName = CustomMessageUtil.getUserName(msgInfo, userId)
                    user += userName
                    if (i != 0) user = "，$user"
                    if (i == 2 && ids.size > 3) {
                        user += "等"
                        break
                    }
                }
            } else {
                user = groupTips.opUserInfo.identifier
                val userId = groupTips.opUserInfo.identifier
                user = CustomMessageUtil.getUserName(msgInfo, userId)
            }
            var message = wrapperColor(user)
            if (tipsType == TIMGroupTipsType.Join) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_JOIN)
                message = message + "加入群组"
            }
            if (tipsType == TIMGroupTipsType.Quit) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_QUITE)
                message = message + "退出群组"
            }
            if (tipsType == TIMGroupTipsType.Kick) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_KICK)
                message = message + "被踢出群组"
            }
            if (tipsType == TIMGroupTipsType.SetAdmin) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                message = message + "被设置管理员"
            }
            if (tipsType == TIMGroupTipsType.CancelAdmin) {
                msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                message = message + "被取消管理员"
            }
            if (tipsType == TIMGroupTipsType.ModifyGroupInfo) {
                val modifyList = groupTips.groupInfoList
                for (i in modifyList.indices) {
                    val modifyInfo = modifyList[i]
                    val modifyType = modifyInfo.type
                    if (modifyType == TIMGroupTipsGroupInfoType.ModifyName) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME)
                        message = message + "修改群名称为\"" + modifyInfo.content + "\""
                    } else if (modifyType == TIMGroupTipsGroupInfoType.ModifyNotification) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        message = message + "修改群公告为\"" + modifyInfo.content + "\""
                    } else if (modifyType == TIMGroupTipsGroupInfoType.ModifyOwner) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        val userId = modifyInfo.content
                        val userName = CustomMessageUtil.getUserName(msgInfo, userId)
                        message = message + "转让群主给\"" + userName + "\""
                    } else if (modifyType == TIMGroupTipsGroupInfoType.ModifyFaceUrl) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        message = message + "修改了群头像"
                    } else if (modifyType == TIMGroupTipsGroupInfoType.ModifyIntroduction) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        message = message + "修改群介绍为\"" + modifyInfo.content + "\""
                    }
                    if (i < modifyList.size - 1) {
                        message = "$message、"
                    }
                }
            }
            if (tipsType == TIMGroupTipsType.ModifyMemberInfo) {
                val modifyList = groupTips.memberInfoList
                if (modifyList.size > 0) {
                    val shutupTime = modifyList[0].shutupTime
                    if (shutupTime > 0) {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        message = message + "被禁言\"" + formatSeconds(shutupTime) + "\""
                    } else {
                        msgInfo.setMsgType(MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)
                        message = message + "被取消禁言"
                    }
                }
            }
            if (TextUtils.isEmpty(message)) {
                return null
            }
            msgInfo.extra = message
        } else {
            when (type) {
                TIMElemType.Text -> {
                    val txtEle = ele as TIMTextElem
                    msgInfo.extra = txtEle.text
                }
                TIMElemType.Face -> {
                    val txtEle = ele as TIMFaceElem
                    if (txtEle.index < 1 || txtEle.data == null) {
                        TUIKitLog.e("MessageInfoUtil", "txtEle data is null or index<1")
                        return null
                    }
                    msgInfo.extra = "[自定义表情]"
                }
                TIMElemType.Sound -> {
                    val soundElemEle = ele as TIMSoundElem
                    if (msgInfo.isSelf) {
                        msgInfo.dataPath = soundElemEle.path
                    } else {
                        val path = TUIKitConstants.RECORD_DOWNLOAD_DIR + soundElemEle.uuid
                        val file = File(path)
                        if (!file.exists()) {
                            soundElemEle.getSoundToFile(path, object : TIMCallBack {
                                override fun onError(code: Int, desc: String) {
                                    TUIKitLog.e("MessageInfoUtil getSoundToFile", "$code:$desc")
                                }

                                override fun onSuccess() {
                                    msgInfo.dataPath = path
                                }
                            })
                        } else {
                            msgInfo.dataPath = path
                        }
                    }
                    msgInfo.extra = "[语音]"
                }
                TIMElemType.Image -> {
                    val imageEle = ele as TIMImageElem
                    val localPath = imageEle.path
                    if (msgInfo.isSelf && !TextUtils.isEmpty(localPath)) {
                        msgInfo.dataPath = localPath
                        val size = getImageSize(localPath)
                        msgInfo.imgWidth = size[0]
                        msgInfo.imgHeight = size[1]
                    } else {
                        val imgs: List<TIMImage> = imageEle.imageList
                        for (i in imgs.indices) {
                            val img = imgs[i]
                            if (img.type == TIMImageType.Thumb) {
                                val path = TUIKitConstants.IMAGE_DOWNLOAD_DIR + img.uuid
                                msgInfo.imgWidth = img.width.toInt()
                                msgInfo.imgHeight = img.height.toInt()
                                val file = File(path)
                                if (file.exists()) {
                                    msgInfo.dataPath = path
                                }
                            }
                        }
                    }
                    msgInfo.extra = "[图片]"
                }
                TIMElemType.Video -> {
                    val videoEle = ele as TIMVideoElem
                    if (msgInfo.isSelf && !TextUtils.isEmpty(videoEle.snapshotPath)) {
                        val size = getImageSize(videoEle.snapshotPath)
                        msgInfo.imgWidth = size[0]
                        msgInfo.imgHeight = size[1]
                        msgInfo.dataPath = videoEle.snapshotPath
                        msgInfo.dataUri = getUriFromPath(videoEle.videoPath)
                    } else {
                        val video = videoEle.videoInfo
                        val videoPath = TUIKitConstants.VIDEO_DOWNLOAD_DIR + video.uuid
                        val uri = Uri.parse(videoPath)
                        msgInfo.dataUri = uri
                        val snapshot = videoEle.snapshotInfo
                        msgInfo.imgWidth = snapshot.width.toInt()
                        msgInfo.imgHeight = snapshot.height.toInt()
                        val snapPath = TUIKitConstants.IMAGE_DOWNLOAD_DIR + snapshot.uuid
                        //判断快照是否存在,不存在自动下载
                        if (File(snapPath).exists()) {
                            msgInfo.dataPath = snapPath
                        }
                    }
                    msgInfo.extra = "[视频]"
                }
                TIMElemType.File -> {
                    val fileElem = ele as TIMFileElem
                    var filename = fileElem.uuid
                    if (TextUtils.isEmpty(filename)) {
                        filename = System.currentTimeMillis().toString() + fileElem.fileName
                    }
                    val path = TUIKitConstants.FILE_DOWNLOAD_DIR + filename
                    if (!msgInfo.isSelf) {
                        val file = File(path)
                        if (!file.exists()) {
                            msgInfo.status = MessageInfo.MSG_STATUS_UN_DOWNLOAD
                        } else {
                            msgInfo.status = MessageInfo.MSG_STATUS_DOWNLOADED
                        }
                        msgInfo.dataPath = path
                    } else {
                        if (TextUtils.isEmpty(fileElem.path)) {
                            fileElem.getToFile(path, object : TIMCallBack {
                                override fun onError(code: Int, desc: String) {
                                    TUIKitLog.e("MessageInfoUtil getToFile", "$code:$desc")
                                }

                                override fun onSuccess() {
                                    msgInfo.dataPath = path
                                }
                            })
                        } else {
                            msgInfo.status = MessageInfo.MSG_STATUS_SEND_SUCCESS
                            msgInfo.dataPath = fileElem.path
                        }
                    }
                    msgInfo.extra = "[文件]"
                }
            }
            msgInfo.setMsgType(TIMElemType2MessageInfoType(type))
        }
        if (tIMMessage.status() == TIMMessageStatus.HasRevoked) {
            msgInfo.status = MessageInfo.MSG_STATUS_REVOKE
            msgInfo.setMsgType(MessageInfo.MSG_STATUS_REVOKE)
            if (msgInfo.isSelf) {
                msgInfo.extra = "您撤回了一条消息"
            } else if (msgInfo.isGroup) {
                val message = wrapperColor(msgInfo.fromUser)
                msgInfo.extra = message + "撤回了一条消息"
            } else {
                msgInfo.extra = "对方撤回了一条消息"
            }
        } else {
            if (msgInfo.isSelf) {
                if (tIMMessage.status() == TIMMessageStatus.SendFail) {
                    msgInfo.status = MessageInfo.MSG_STATUS_SEND_FAIL
                } else if (tIMMessage.status() == TIMMessageStatus.SendSucc) {
                    msgInfo.status = MessageInfo.MSG_STATUS_SEND_SUCCESS
                } else if (tIMMessage.status() == TIMMessageStatus.Sending) {
                    msgInfo.status = MessageInfo.MSG_STATUS_SENDING
                }
            }
        }
        return msgInfo
    }

    private fun wrapperColor(raw: String?): String {
        val c2Code = CommonUtil.toHexEncodingColor(SkinCompatResources.getColor(TUIKit.appContext, R.color.C1))
        return "\"<font color=\"#$c2Code\">$raw</font>\""
    }

    private fun TIMElemType2MessageInfoType(type: TIMElemType): Int {
        when (type) {
            TIMElemType.Text -> return MessageInfo.MSG_TYPE_TEXT
            TIMElemType.Image -> return MessageInfo.MSG_TYPE_IMAGE
            TIMElemType.Sound -> return MessageInfo.MSG_TYPE_AUDIO
            TIMElemType.Video -> return MessageInfo.MSG_TYPE_VIDEO
            TIMElemType.File -> return MessageInfo.MSG_TYPE_FILE
            TIMElemType.Location -> return MessageInfo.MSG_TYPE_LOCATION
            TIMElemType.Face -> return MessageInfo.MSG_TYPE_CUSTOM_FACE
            TIMElemType.GroupTips -> return MessageInfo.MSG_TYPE_TIPS
        }
        return -1
    }
}