package com.black.im.adapter.holders

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.black.base.util.ImageLoader
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.IMHelper.getUserUID
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import com.black.im.widget.UserIconView
import com.tencent.imsdk.TIMFriendshipManager
import com.tencent.imsdk.TIMManager
import com.tencent.imsdk.TIMUserProfile
import skin.support.content.res.SkinCompatResources
import java.util.*

abstract class MessageContentHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageEmptyHolder(itemView, properties) {
    private val imageLoader: ImageLoader = ImageLoader(itemView.context)
    private val leftUserIconLayout: View
    var leftUserIcon: UserIconView
    private val rightUserIconLayout: View
    var rightUserIcon: UserIconView
    private val usernameLayout: View
    private val leftFirstLetterText: TextView
    var usernameText: TextView
    var msgContentLinear: LinearLayout
    var sendingProgress: ProgressBar
    var statusImage: ImageView
    var isReadText: TextView
    var unreadAudioText: TextView

    init {
        rootView = itemView
        leftUserIconLayout = itemView.findViewById(R.id.left_user_icon_layout)
        leftUserIcon = itemView.findViewById(R.id.left_user_icon_view)
        rightUserIconLayout = itemView.findViewById(R.id.right_user_icon_layout)
        rightUserIcon = itemView.findViewById(R.id.right_user_icon_view)
        usernameLayout = itemView.findViewById(R.id.user_name_layout)
        leftFirstLetterText = itemView.findViewById(R.id.left_first_letter)
        usernameText = itemView.findViewById(R.id.user_name_tv)
        msgContentLinear = itemView.findViewById(R.id.msg_content_ll)
        statusImage = itemView.findViewById(R.id.message_status_iv)
        sendingProgress = itemView.findViewById(R.id.message_sending_pb)
        isReadText = itemView.findViewById(R.id.is_read_tv)
        unreadAudioText = itemView.findViewById(R.id.audio_unread)
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        super.layoutViews(msg, position)
        //// 头像设置
        if (true == msg?.isSelf) {
            leftUserIconLayout.visibility = View.GONE
            leftUserIcon.visibility = View.GONE
            rightUserIconLayout.visibility = View.VISIBLE
            if (properties?.getRightIconVisibility() == View.GONE) {
                rightUserIcon.visibility = View.GONE
            } else {
                rightUserIcon.visibility = View.VISIBLE
            }
        } else {
            leftUserIconLayout.visibility = View.VISIBLE
            if (properties?.getLeftIconVisibility() == View.GONE) {
                leftUserIcon.visibility = View.GONE
            } else {
                leftUserIcon.visibility = View.VISIBLE
            }
            rightUserIconLayout.visibility = View.GONE
            rightUserIcon.visibility = View.GONE
        }
        //        if (properties.getAvatar() != 0) {
//            leftUserIcon.setDefaultImageResId(properties.getAvatar());
//            rightUserIcon.setDefaultImageResId(properties.getAvatar());
//        } else {
//            leftUserIcon.setDefaultImageResId(R.drawable.chat_left);
//            rightUserIcon.setDefaultImageResId(R.drawable.chat_right);
//        }
        if (properties?.getAvatarRadius() != 0) {
            leftUserIcon.setRadius(properties?.getAvatarRadius() ?: 0)
            rightUserIcon.setRadius(properties?.getAvatarRadius() ?: 0)
        } else {
            leftUserIcon.setRadius(5)
            rightUserIcon.setRadius(5)
        }
        if (properties?.getAvatarSize() != null && properties?.getAvatarSize()?.size == 2) {
            var params = leftUserIcon.layoutParams
            params.width = properties?.getAvatarSize()!![0]
            params.height = properties?.getAvatarSize()!![1]
            leftUserIcon.layoutParams = params
            params = rightUserIcon.layoutParams
            params.width = properties?.getAvatarSize()!![0]
            params.height = properties?.getAvatarSize()!![1]
            rightUserIcon.layoutParams = params
        }
        //// 用户昵称设置
        if (true == msg?.isSelf) {
            // 默认不显示自己的昵称
            if (properties?.getRightNameVisibility() == View.GONE) {
                usernameText.visibility = View.GONE
                usernameLayout.visibility = View.GONE
            } else {
                properties?.getRightNameVisibility()?.let {
                    usernameText.visibility = it
                    usernameLayout.visibility = it
                }
            }
            if (usernameText.visibility == View.GONE) {
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgContentLinear.getLayoutParams();
//                params.topMargin = 6;
//                msgContentLinear.setLayoutParams(params);
            }
        } else {
            if (properties?.getLeftNameVisibility() == View.GONE) {
                if (true == msg?.isGroup) {
                    // 群聊默认显示对方的昵称
                    usernameText.visibility = View.VISIBLE
                    usernameLayout.visibility = View.VISIBLE
                } else {
                    // 单聊默认不显示对方昵称
                    usernameText.visibility = View.GONE
                    usernameLayout.visibility = View.GONE
                }
            } else {
                properties?.getLeftNameVisibility()?.let {
                    usernameText.visibility = it
                    usernameLayout.visibility = it
                }
            }
            if (usernameText.visibility == View.GONE) { //                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) msgContentLinear.getLayoutParams();
//                params.topMargin = 17;
//                msgContentLinear.setLayoutParams(params);
            }
        }
        if (properties?.getNameFontColor() != 0) {
            usernameText.setTextColor(properties?.getNameFontColor()!!)
        }
        if (properties?.getNameFontSize() != 0) {
            usernameText.textSize = properties?.getNameFontSize()!!.toFloat()
        }
        // 聊天界面设置头像和昵称
        val nameHard: String?
        if (true == msg?.isSelf) {
            nameHard = properties?.getRightNameHard()
            leftFirstLetterText.visibility = View.GONE
        } else {
            nameHard = properties?.getLeftNameHard()
            if (true == properties?.isUseLeftFirstLetter()) {
                leftFirstLetterText.visibility = View.VISIBLE
            } else {
                leftFirstLetterText.visibility = View.GONE
            }
        }
        val userId = msg?.fromUser
        if (!TextUtils.isEmpty(nameHard)) {
            usernameText.text = nameHard
        } else {
            val nameDefault: String? = if (true == msg?.isSelf) {
                properties?.getRightNameDefault()
            } else {
                properties?.getLeftNameDefault()
            }
            val profile = TIMFriendshipManager.getInstance().queryUserProfile(msg?.fromUser)
            if (profile == null) {
                usernameText.text = if (TextUtils.isEmpty(nameDefault)) msg?.fromUser else nameDefault
            } else {
                if (TextUtils.isEmpty(msg?.groupNameCard)) {
                    usernameText.text = if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(nameDefault)) nameDefault else msg?.fromUser
                } else {
                    usernameText.text = msg?.groupNameCard
                }
                if (true != msg?.isSelf) {
                    if (!TextUtils.isEmpty(profile.faceUrl)) {
                        val urllist: MutableList<String> = ArrayList()
                        urllist.add(profile.faceUrl)
                        leftUserIcon.setIconUrls(urllist)
                        //                    urllist.clear();
                    } else {
                        leftUserIcon.setIconUrls(null)
                    }
                }
            }
        }
        usernameText.text = String.format("%s(UID:%s)", usernameText.text.toString(), getUserUID(msg?.fromUser))
        val memberChatProfile = properties?.getMemberChatProfile(msg?.fromUser)
        if (memberChatProfile != null) {
            leftUserIcon.setDefaultImageUrl(memberChatProfile.defaultAvatarUrl)
            rightUserIcon.setDefaultImageUrl(memberChatProfile.defaultAvatarUrl)
            usernameText.text = String.format("%s(UID:%s)", memberChatProfile.hardUserName, getUserUID(msg?.fromUser))
        }
        if (leftFirstLetterText.visibility == View.VISIBLE) {
            val userName = usernameText.text.toString()
            val firstLetter = if (TextUtils.isEmpty(userName)) null else userName[0].toString()
            leftFirstLetterText.text = if (TextUtils.isEmpty(firstLetter)) "" else firstLetter
        }
        val selfInfo = TIMFriendshipManager.getInstance().queryUserProfile(TIMManager.getInstance().loginUser)
        if (selfInfo != null && true == msg?.isSelf) {
            if (!TextUtils.isEmpty(selfInfo.faceUrl)) {
                val urllist: MutableList<String> = ArrayList()
                urllist.add(selfInfo.faceUrl)
                rightUserIcon.setIconUrls(urllist)
                //                urllist.clear();
            }
        }
        val userName = usernameText.text.toString()
        leftUserIcon.setDefaultImageResId(R.drawable.default_user_icon)
        val bitmap = properties?.getLeftDefaultNameAvatar()
        if (bitmap != null) {
            leftUserIcon.setDefaultImageBitmap(bitmap)
        }
        leftUserIcon.invokeInformation(msg)
        rightUserIcon.setDefaultImageResId(R.drawable.default_user_icon)
        //        rightUserIcon.setDefaultImageBitmap(IMAvatarUtil.getCacheIcon(userName));
        rightUserIcon.invokeInformation(msg)
        if (msg?.status == MessageInfo.MSG_STATUS_SENDING || msg?.status == MessageInfo.MSG_STATUS_DOWNLOADING) {
            sendingProgress.visibility = View.VISIBLE
        } else {
            sendingProgress.visibility = View.GONE
        }
        //// 聊天气泡设置
        if (true == msg?.isSelf) {
            if (properties?.getRightBubble() != null && properties?.getRightBubble()?.constantState != null) {
                msgContentFrame.background = properties?.getRightBubble()?.constantState?.newDrawable()
            } else { ///msgContentFrame.setBackgroundResource(R.drawable.chat_bubble_myself);
//换肤
                val drawable = SkinCompatResources.getDrawable(appContext, R.drawable.bg_chat_message_right)
                msgContentFrame.background = drawable
            }
        } else {
            if (properties?.getLeftBubble() != null && properties?.getLeftBubble()?.constantState != null) {
                msgContentFrame.background = properties?.getLeftBubble()?.constantState?.newDrawable()
                msgContentFrame.layoutParams = msgContentFrame.layoutParams
            } else { //msgContentFrame.setBackgroundResource(R.drawable.chat_other_bg);
//换肤
                val drawable = SkinCompatResources.getDrawable(appContext, R.drawable.bg_chat_message_left)
                msgContentFrame.background = drawable
            }
        }
        //// 聊天气泡的点击事件处理
        if (onItemClickListener != null) {
            msgContentFrame.setOnLongClickListener { v ->
                onItemClickListener?.onMessageLongClick(v, position, msg)
                true
            }
            leftUserIcon.setOnClickListener { view -> onItemClickListener?.onUserIconClick(view, position, msg) }
            rightUserIcon.setOnClickListener { view -> onItemClickListener?.onUserIconClick(view, position, msg) }
        }
        //// 发送状态的设置
        if (msg?.status == MessageInfo.MSG_STATUS_SEND_FAIL) {
            statusImage.visibility = View.VISIBLE
            msgContentFrame.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener?.onMessageLongClick(msgContentFrame, position, msg)
                }
            }
        } else {
            msgContentFrame.setOnClickListener(null)
            statusImage.visibility = View.GONE
        }
        //// 左右边的消息需要调整一下内容的位置
        if (true == msg?.isSelf) {
            msgContentLinear.removeView(msgContentFrame)
            msgContentLinear.addView(msgContentFrame)
        } else {
            msgContentLinear.removeView(msgContentFrame)
            msgContentLinear.addView(msgContentFrame, 0)
        }
        //// 对方已读标识的设置
        if (true == msg?.isSelf) {
            if (msg.isGroup) {
                isReadText.visibility = View.GONE
            } else {
                isReadText.visibility = View.VISIBLE
                val params = isReadText.layoutParams as LinearLayout.LayoutParams
                params.gravity = Gravity.CENTER_VERTICAL
                isReadText.layoutParams = params
                if (msg.isPeerRead) {
                    isReadText.setText(R.string.has_read)
                } else {
                    isReadText.setText(R.string.unread)
                }
            }
        } else {
            isReadText.visibility = View.GONE
        }
        isReadText.visibility = View.GONE
        //// 音频已读
        unreadAudioText.visibility = View.GONE
        //// 由子类设置指定消息类型的views
        layoutVariableViews(msg, position)
    }

    private fun showTIMUserProfile(msg: MessageInfo, profile: TIMUserProfile?) {
        if (profile == null) {
            usernameText.text = msg.fromUser
        } else {
            if (TextUtils.isEmpty(msg.groupNameCard)) {
                usernameText.text = if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else msg.fromUser
            } else {
                usernameText.text = msg.groupNameCard
            }
            if (!TextUtils.isEmpty(profile.faceUrl) && !msg.isSelf) {
                val urllist: MutableList<String> = ArrayList()
                urllist.add(profile.faceUrl)
                leftUserIcon.setIconUrls(urllist)
                urllist.clear()
            }
        }
    }

    abstract fun layoutVariableViews(msg: MessageInfo?, position: Int)
}
