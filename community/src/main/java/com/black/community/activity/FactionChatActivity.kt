package com.black.community.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.MemberChatProfile
import com.black.base.model.SuccessObserver
import com.black.base.model.community.FactionItem
import com.black.base.model.community.FactionMember
import com.black.base.model.community.FactionUserInfo
import com.black.base.util.*
import com.black.community.R
import com.black.community.databinding.ActivityFactionChatBinding
import com.black.im.fragment.ChatFragment
import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.util.AudioPlayer
import com.black.im.util.IMConstData
import com.black.im.widget.ChatLayout
import com.black.im.widget.ChatLayoutUI.MessageClickCheckListener
import com.black.im.widget.ChatLayoutUI.SendCheckListener
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.tencent.imsdk.TIMConversationType
import io.reactivex.Observer
import org.json.JSONObject
import java.util.*

//笑傲江湖聊天界面
@Route(value = [RouterConstData.FACTION_CHAT])
class FactionChatActivity : BaseActionBarActivity() {
    private var factionItem: FactionItem? = null
    private var factionId: Long = 0
    private var factionUserInfo: FactionUserInfo? = null
    private var groupId: String? = null
    private var groupName: String? = null

    private var binding: ActivityFactionChatBinding? = null

    //门派变更
    private var factionUpdateObserver: Observer<JSONObject?>? = createFactionUpdateObserver()

    private fun createFactionUpdateObserver(): Observer<JSONObject?> {
        return object : SuccessObserver<JSONObject?>() {
            override fun onSuccess(value: JSONObject?) {
                val jsonId = value?.optLong("id", -1) ?: -1
                if (jsonId != -1L && factionId == jsonId) {
                    getFactionUserInfo()
                }
            }
        }
    }

    //掌门变更
    private var ownerUpdateObserver: Observer<Long?>? = createOwnerUpdateObserver()

    private fun createOwnerUpdateObserver(): Observer<Long?> {
        return object : SuccessObserver<Long?>() {
            override fun onSuccess(value: Long?) {
                if (value != null && factionId == value) {
                    getFactionUserInfo()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factionId = intent.getLongExtra(ConstData.FACTION_ID, -1)
        factionUserInfo = intent.getParcelableExtra(ConstData.FACTION_USER_INFO)
        factionItem = intent.getParcelableExtra(ConstData.FACTION_ITEM)
        if (factionId == -1L) {
            finish()
            return
        }
        if (TextUtils.isEmpty(groupId)) {
            finish()
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_faction_chat)
        initChatView()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        groupName = intent.getStringExtra(ConstData.IM_GROUP_NAME)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)
        val titleView = view.findViewById<TextView>(R.id.action_bar_title)
        titleView.text = if (groupName == null) "" else groupName
    }

    public override fun onPause() {
        super.onPause()
        AudioPlayer.instance.stopPlay()
    }

    override fun onResume() {
        super.onResume()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_OPEN)
        if (factionUpdateObserver == null) {
            factionUpdateObserver = createFactionUpdateObserver()
        }
        SocketDataContainer.subscribeFactionUpdateObservable(factionUpdateObserver)
        if (ownerUpdateObserver == null) {
            ownerUpdateObserver = createOwnerUpdateObserver()
        }
        SocketDataContainer.subscribeFactionOwnerObservable(ownerUpdateObserver)
        refreshFactionUserInfo()
        factionMemberList
    }

    override fun onStop() {
        super.onStop()
        sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_FACTION_CLOSE)
        if (factionUpdateObserver != null) {
            SocketDataContainer.removeFactionUpdateObservable(factionUpdateObserver)
        }
        if (ownerUpdateObserver != null) {
            SocketDataContainer.removeFactionOwnerObservable(ownerUpdateObserver)
        }
    }

    var chatLayout: ChatLayout? = null
    private fun initChatView() {
        val chatInfo = ChatInfo()
        chatInfo.type = TIMConversationType.Group
        chatInfo.id = groupId
        chatInfo.chatName = groupName
        val chatFragment = ChatFragment()
        val bundle = Bundle()
        bundle.putSerializable(IMConstData.CHAT_INFO, chatInfo)
        chatFragment.arguments = bundle
        chatFragment.setOnChatLayoutListener(object :ChatFragment.OnChatLayoutListener{
            override fun onInitChatLayout(chatLayout: ChatLayout?) {
                this@FactionChatActivity.chatLayout = chatLayout
                chatLayout?.titleBar?.visibility = View.GONE
                chatLayout?.messageLayout?.setLeftNameVisibility(View.VISIBLE)
                chatLayout?.messageLayout?.setLeftIconVisibility(View.VISIBLE)
                chatLayout?.messageLayout?.setRightIconVisibility( View.VISIBLE)
                val displayMetrics = resources.displayMetrics
                chatLayout?.messageLayout?.setAvatarRadius((displayMetrics.density * 20).toInt())
                chatLayout?.messageLayout?.setLeftNameDefault("FBsexer")
                chatLayout?.messageLayout?.setRightNameDefault("FBsexer")
                chatLayout?.inputLayout?.disableSendFileAction(true)
                chatLayout?.sendCheckListener = object :SendCheckListener {
                    override fun beforeMessageSendCheck(msg: MessageInfo?): Boolean {
                        val isJoin = isJoin
                        if (!isJoin) {
                            FryingUtil.showToast(mContext, "请先加入门派", FryingSingleToast.ERROR)
                        }
                       return isJoin
                    }
                }
                chatLayout?.setMessageClickCheckListener(object :MessageClickCheckListener{
                    override fun onMessageClickCheck(msg: MessageInfo?): Boolean {
                        val isJoin = isJoin
                        if (!isJoin) {
                            FryingUtil.showToast(mContext, "请先加入门派", FryingSingleToast.ERROR)
                        }
                       return  isJoin
                    }

                })
                refreshMemberChatDefaultProfile()
            }

        })
        supportFragmentManager.beginTransaction().replace(R.id.chat_container, chatFragment).commitAllowingStateLoss()
    }

    private val isJoin: Boolean
        get() = factionUserInfo != null && !TextUtils.isEmpty(factionUserInfo?.leagueId)

    private fun displayFactionUserInfo() {
        if (factionUserInfo == null || TextUtils.isEmpty(factionUserInfo?.leagueId)) {
            binding?.userInfo?.visibility = View.GONE
        } else {
            binding?.userInfo?.visibility = View.VISIBLE
            binding?.userInfo?.text = String.format("名号：%s 本人存币量：%s",
                    if (factionUserInfo?.userName == null) nullAmount else factionUserInfo?.userName,
                    if (factionUserInfo?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(factionUserInfo?.lockAmount, 8, 0, 2))
        }
    }

    private fun refreshFactionUserInfo() {
        if (factionUserInfo == null) {
            getFactionUserInfo()
        } else {
            displayFactionUserInfo()
        }
    }

    private fun getFactionUserInfo() {
        CommunityApiServiceHelper.getFactionUserInfo(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultData<FactionUserInfo?>?>() {
            override fun callback(returnData: HttpRequestResultData<FactionUserInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    factionUserInfo = returnData.data
                    displayFactionUserInfo()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    val factionMemberList: Unit
        get() {
            CommunityApiServiceHelper.getFactionMemberList(this, if (factionId == -1L) "" else NumberUtil.formatNumberNoGroup(factionId), object : NormalCallback<HttpRequestResultDataList<FactionMember?>?>() {
                override fun callback(returnData: HttpRequestResultDataList<FactionMember?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        profileHashMap = getFactionMemberChatDefaultProfile(returnData.data)
                        refreshMemberChatDefaultProfile()
                    }
                }
            })
        }

    private fun refreshMemberChatDefaultProfile() {
        if (chatLayout == null) {
            return
        }
        if (profileHashMap != null && profileHashMap!!.isNotEmpty()) {
            if (true == chatLayout?.messageLayout?.setProfileMap(profileHashMap)) {
                chatLayout?.messageLayout?.refreshAdapter()
            }
        }
    }

    private var profileHashMap: HashMap<String?, MemberChatProfile?>? = null

    private fun getFactionMemberChatDefaultProfile(factionMemberList: ArrayList<FactionMember?>?): HashMap<String?, MemberChatProfile?>? {
        if (factionMemberList != null && factionMemberList.isNotEmpty()) {
            val map = HashMap<String?, MemberChatProfile?>()
            for (member in factionMemberList) {
                val userId = member?.userId
                if (TextUtils.isEmpty(userId)) {
                    continue
                }
                val profile = MemberChatProfile()
                profile.hardUserName = if (member?.userName == null) nullAmount else member.userName
                if (member?.type != null && member.type == 1) {
                    profile.defaultAvatarUrl = if (factionItem == null) null else UrlConfig.getHost(this) + factionItem?.ownerAvatar
                } else {
                    profile.defaultAvatarUrl = if (factionItem == null) null else UrlConfig.getHost(this) + factionItem?.memberAvatar
                }
                map[userId] = profile
            }
            val member = CommonUtil.getItemFromList(factionMemberList, factionMemberList.size - 1)
            if (member != null) {
                val profile = MemberChatProfile()
                profile.hardUserName = if (member.userName == null) nullAmount else member.userName
                if (member.type != null && member.type == 1) {
                    profile.defaultAvatarUrl = if (factionItem == null) null else UrlConfig.getHost(this) + factionItem?.ownerAvatar
                } else {
                    profile.defaultAvatarUrl = if (factionItem == null) null else UrlConfig.getHost(this) + factionItem?.memberAvatar
                }
                map[ConstData.FACTION_MEMBER_PROFILE_DEFAULT] = profile
            }
            return map
            //            Intent data = new Intent();
//            data.putExtra(ConstData.FACTION_MEMBER_PROFILE, map);
        }
        return null
    }
}