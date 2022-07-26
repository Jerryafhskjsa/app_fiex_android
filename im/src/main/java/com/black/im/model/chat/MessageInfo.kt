package com.black.im.model.chat

import android.net.Uri
import com.black.base.model.community.RedPacket
import com.black.base.model.community.RedPacketGot
import com.black.im.util.CustomMessageUtil.getCustomMessageData
import com.black.im.util.CustomMessageUtil.getSubType
import com.tencent.imsdk.TIMElem
import com.tencent.imsdk.TIMMessage
import com.tencent.imsdk.ext.message.TIMMessageLocator
import java.util.*

class MessageInfo {
    companion object {
        const val MSG_TYPE_MIME = 0x1
        /**
         * 文本类型消息
         */
        const val MSG_TYPE_TEXT = 0x00
        /**
         * 图片类型消息
         */
        const val MSG_TYPE_IMAGE = 0x20
        /**
         * 语音类型消息
         */
        const val MSG_TYPE_AUDIO = 0x30
        /**
         * 视频类型消息
         */
        const val MSG_TYPE_VIDEO = 0x40
        /**
         * 文件类型消息
         */
        const val MSG_TYPE_FILE = 0x50
        /**
         * 位置类型消息
         */
        const val MSG_TYPE_LOCATION = 0x60
        /**
         * 自定义图片类型消息
         */
        const val MSG_TYPE_CUSTOM_FACE = 0x70
        /**
         * 自定义消息
         */
        const val MSG_TYPE_CUSTOM = 0x80
        /**
         * 自定义消息--红包
         */
        const val MSG_TYPE_CUSTOM_RED_PACKET = 0x81
        /**
         * 自定义消息--红包领取
         */
        const val MSG_TYPE_CUSTOM_RED_PACKET_GOT = 0x82
        const val MSG_TYPE_CUSTOM_UNKNOWN = -1
        /**
         * 提示类信息
         */
        const val MSG_TYPE_TIPS = 0x100
        /**
         * 群创建提示消息
         */
        const val MSG_TYPE_GROUP_CREATE = 0x101
        /**
         * 群创建提示消息
         */
        const val MSG_TYPE_GROUP_DELETE = 0x102
        /**
         * 群成员加入提示消息
         */
        const val MSG_TYPE_GROUP_JOIN = 0x103
        /**
         * 群成员退群提示消息
         */
        const val MSG_TYPE_GROUP_QUITE = 0x104
        /**
         * 群成员被踢出群提示消息
         */
        const val MSG_TYPE_GROUP_KICK = 0x105
        /**
         * 群名称修改提示消息
         */
        const val MSG_TYPE_GROUP_MODIFY_NAME = 0x106
        /**
         * 群通知更新提示消息
         */
        const val MSG_TYPE_GROUP_MODIFY_NOTICE = 0x107
        /**
         * 消息未读状态
         */
        const val MSG_STATUS_READ = 0x111
        /**
         * 消息删除状态
         */
        const val MSG_STATUS_DELETE = 0x112
        /**
         * 消息撤回状态
         */
        const val MSG_STATUS_REVOKE = 0x113
        /**
         * 消息正常状态
         */
        const val MSG_STATUS_NORMAL = 0
        /**
         * 消息发送中状态
         */
        const val MSG_STATUS_SENDING = 1
        /**
         * 消息发送成功状态
         */
        const val MSG_STATUS_SEND_SUCCESS = 2
        /**
         * 消息发送失败状态
         */
        const val MSG_STATUS_SEND_FAIL = 3
        /**
         * 消息内容下载中状态
         */
        const val MSG_STATUS_DOWNLOADING = 4
        /**
         * 消息内容未下载状态
         */
        const val MSG_STATUS_UN_DOWNLOAD = 5
        /**
         * 消息内容已下载状态
         */
        const val MSG_STATUS_DOWNLOADED = 6
    }
    /**
     * 获取消息唯一标识
     *
     * @return
     */
    /**
     * 设置消息唯一标识
     *
     * @param id
     */
    var id = UUID.randomUUID().toString()
    var uniqueId: Long = 0
    /**
     * 获取消息发送方 ID
     *
     * @return
     */
    /**
     * 设置消息发送方 ID
     *
     * @param fromUser
     */
    var fromUser: String? = null
    /**
     * 获取群名片
     *
     * @return
     */
    /**
     * 设置群名片
     *
     * @param groupNameCard
     */
    var groupNameCard: String? = null
    private var msgType = 0
    /**
     * 获取消息发送状态
     *
     * @return
     */
    /**
     * 设置消息发送状态
     *
     * @param status
     */
    var status = MSG_STATUS_NORMAL
    /**
     * 获取消息是否为登录用户发送
     *
     * @return
     */
    /**
     * 设置消息是否是登录用户发送
     *
     * @param self
     */
    var isSelf = false
    /**
     * 获取消息是否已读
     *
     * @return
     */
    /**
     * 设置消息已读
     *
     * @param read
     */
    var isRead = false
    /**
     * 获取消息是否为群消息
     *
     * @return
     */
    /**
     * 设置消息是否为群消息
     *
     * @param group
     */
    var isGroup = false
    /**
     * 获取多媒体消息的数据源
     *
     * @return
     */
    /**
     * 设置多媒体消息的数据源
     *
     * @param dataUri
     */
    var dataUri: Uri? = null
    /**
     * 获取多媒体消息的保存路径
     *
     * @return
     */
    /**
     * 设置多媒体消息的保存路径
     *
     * @param dataPath
     */
    var dataPath: String? = null
    /**
     * 非文字消息在会话列表时展示的文字说明，比如照片在会话列表展示为“[图片]”
     *
     * @return
     */
    /**
     * 设置非文字消息在会话列表时展示的文字说明，比如照片在会话列表展示为“[图片]”
     *
     * @param extra
     */
    var extra: Any? = null
    /**
     * 获取消息发送时间，单位是秒
     *
     * @return
     */
    /**
     * 设置消息发送时间，单位是秒
     *
     * @param msgTime
     */
    var msgTime: Long = 0
    /**
     * 获取图片或者视频缩略图的图片宽
     *
     * @return
     */
    /**
     * 设置图片或者视频缩略图的图片宽
     *
     * @param imgWidth
     */
    var imgWidth = 0
    /**
     * 获取图片或者视频缩略图的图片高
     *
     * @return
     */
    /**
     * 设置图片或者视频缩略图的图片高
     *
     * @param imgHeight
     */
    var imgHeight = 0
    var isPeerRead = false
    /**
     * 获取SDK的消息bean
     *
     * @return
     */
    /**
     * 设置SDK的消息bean
     *
     * @param TIMMessage
     */
    var tIMMessage: TIMMessage? = null
    var element: TIMElem? = null
        set(element) {
            field = element
            customMessageData = getCustomMessageData(element)
        }
    //自定义消息内容
    var customMessageData: CustomMessageData? = null
        private set
    var redPacket: RedPacket? = null
    var redPacketGot: RedPacketGot? = null

    /**
     * 获取消息类型
     *
     * @return
     */
    fun getMsgType(): Int {
        return msgType
    }

    /**
     * 设置消息类型
     *
     * @param msgType
     */
    fun setMsgType(msgType: Int) {
        this.msgType = msgType
        if (msgType == MSG_TYPE_CUSTOM) { //自定义消息，再取出自定义消息子类型
            this.msgType = getSubType(this)
        }
    }

    var customInt: Int
        get() = if (tIMMessage == null) {
            0
        } else tIMMessage!!.customInt
        set(value) {
            if (tIMMessage == null) {
                return
            }
            tIMMessage!!.customInt = value
        }

    fun checkEquals(locator: TIMMessageLocator): Boolean {
        return if (tIMMessage == null) {
            false
        } else tIMMessage!!.checkEquals(locator)
    }

    fun remove(): Boolean {
        return if (tIMMessage == null) {
            false
        } else tIMMessage!!.remove()
    }

    val isBlankMessage: Boolean
        get() = msgType == MSG_TYPE_CUSTOM_UNKNOWN
    //                || msgType == MSG_TYPE_CUSTOM_RED_PACKET_GOT
    //                || msgType == MSG_TYPE_CUSTOM_RED_PACKET
}