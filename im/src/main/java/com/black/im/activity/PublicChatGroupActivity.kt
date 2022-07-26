package com.black.im.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.community.ChatRoomEnable
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.im.R
import com.black.im.fragment.ChatFragment
import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.util.IMConstData
import com.black.im.widget.ChatLayout
import com.black.im.widget.ChatLayoutUI.MessageClickCheckListener
import com.black.im.widget.ChatLayoutUI.SendCheckListener
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.tencent.imsdk.TIMConversationType
import com.tencent.imsdk.TIMGroupManager
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.ext.group.TIMGroupDetailInfoResult
import skin.support.content.res.SkinCompatResources
import java.util.*

//公共聊天室
@Route(value = [RouterConstData.PUBLIC_CHAT_GROUP])
class PublicChatGroupActivity : BaseActionBarActivity(), View.OnClickListener {
    private var chatRoomEnable: ChatRoomEnable? = null
    private var isEnable = false
    private var chatRoomEnableMessage: String? = null
    private var groupId: String? = null
    private var groupName: String? = null
    private var titleView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupName = intent.getStringExtra(ConstData.IM_GROUP_NAME);
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID);
        chatRoomEnable = intent.getParcelableExtra(ConstData.IM_CHAT_ROOM_ENABLE)
        isEnable = chatRoomEnable == null || true == chatRoomEnable?.enable
        chatRoomEnableMessage = if (chatRoomEnable == null) null else chatRoomEnable?.message

        setContentView(R.layout.activity_public_chat_group)
        if (TextUtils.isEmpty(groupId)) {
            finish()
            return
        }
        initChatView()
        val groupIdList = ArrayList<String?>()
        groupIdList.add(groupId)
        TIMGroupManager.getInstance().getGroupInfo(groupIdList, object : TIMValueCallBack<List<TIMGroupDetailInfoResult?>?> {
            override fun onError(i: Int, s: String) {}
            override fun onSuccess(timGroupDetailInfoResults: List<TIMGroupDetailInfoResult?>?) {
                val infoResult = CommonUtil.getItemFromList(timGroupDetailInfoResults, 0)
                if (infoResult != null) {
                    groupName = infoResult.groupName
                    titleView?.text = if (groupName == null) "" else groupName
                }
            }
        })
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back_text
    }

    override fun initActionBarView(view: View) {
        groupName = intent.getStringExtra(ConstData.IM_GROUP_NAME)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)
        titleView = view.findViewById(R.id.action_bar_title)
        titleView?.text = if (groupName == null) "" else groupName
        titleView?.setOnClickListener(this)
        val extrasView = view.findViewById<TextView>(R.id.action_bar_extras)
        extrasView.setOnClickListener(this)
        CommonUtil.setTextViewCompoundDrawable(extrasView, SkinCompatResources.getDrawable(this, R.drawable.icon_mine_info), 0)
        extrasView.visibility = View.GONE
        extrasView.isEnabled = false
    }

    public override fun onPause() {
        super.onPause()
        //        AudioPlayer.getInstance().stopPlay();
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //        if (chatFragment != null) {
//            getSupportFragmentManager().beginTransaction().remove(chatFragment).commitAllowingStateLoss();
//        }
    }

    override fun onResume() {
        super.onResume()
    }

    var chatFragment: ChatFragment? = null
    private fun initChatView() {
        val chatInfo = ChatInfo()
        chatInfo.type = TIMConversationType.Group
        chatInfo.id = groupId
        chatInfo.chatName = groupName
        chatFragment = ChatFragment()
        val bundle = Bundle()
        bundle.putSerializable(IMConstData.CHAT_INFO, chatInfo)
        chatFragment?.arguments = bundle
        chatFragment?.setOnChatLayoutListener(object : ChatFragment.OnChatLayoutListener {
            override fun onInitChatLayout(chatLayout: ChatLayout?) {
                chatLayout?.titleBar?.visibility = View.GONE
                chatLayout?.messageLayout?.setLeftNameVisibility(View.VISIBLE)
                chatLayout?.messageLayout?.setLeftIconVisibility(View.VISIBLE)
                chatLayout?.messageLayout?.setRightIconVisibility(View.VISIBLE)
                val displayMetrics = resources.displayMetrics
                chatLayout?.messageLayout?.setAvatarRadius((displayMetrics.density * 20).toInt())
                chatLayout?.messageLayout?.setLeftNameDefault("FBsexer")
                chatLayout?.messageLayout?.setRightNameDefault("FBsexer")
                chatLayout?.inputLayout?.disableSendFileAction(true)
                chatLayout?.sendCheckListener = object : SendCheckListener {
                    override fun beforeMessageSendCheck(msg: MessageInfo?): Boolean {
                        if (!isEnable) {
                            FryingUtil.showToast(mContext, if (chatRoomEnableMessage == null) "null" else chatRoomEnableMessage)
                        }
                        return isEnable
                    }
                }
                chatLayout?.setMessageClickCheckListener(object : MessageClickCheckListener {
                    override fun onMessageClickCheck(msg: MessageInfo?): Boolean {
                        if (!isEnable) {
                            FryingUtil.showToast(mContext, if (chatRoomEnableMessage == null) "null" else chatRoomEnableMessage)
                        }
                        return isEnable
                    }
                })
            }

        })
        supportFragmentManager.beginTransaction().replace(R.id.chat_container, chatFragment!!).commitAllowingStateLoss()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.action_bar_extras) {
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, groupId)
            bundle.putString(ConstData.IM_GROUP_NAME, groupName)
            BlackRouter.getInstance().build(RouterConstData.IM_CHAT_ROOM_INFO).with(bundle).go(this)
        }
    }
}