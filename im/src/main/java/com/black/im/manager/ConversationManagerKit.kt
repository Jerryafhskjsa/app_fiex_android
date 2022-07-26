package com.black.im.manager

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.black.im.model.ConversationInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.provider.ConversationProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.MessageInfoUtil.TIMMessage2MessageInfo
import com.black.im.util.SharedPreferenceUtils.getListData
import com.black.im.util.SharedPreferenceUtils.putListData
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.group.TIMGroupDetailInfoResult
import com.tencent.imsdk.ext.message.TIMMessageLocator
import com.tencent.imsdk.friendship.TIMFriend
import java.util.*
import kotlin.collections.ArrayList

class ConversationManagerKit private constructor() : TIMRefreshListener, MessageRevokedManager.MessageRevokeHandler {
    private var mProvider: ConversationProvider? = null
    private val mUnreadWatchers: MutableList<MessageUnreadWatcher>? = ArrayList()
    private var mConversationPreferences: SharedPreferences
    private var mTopLinkedList: LinkedList<ConversationInfo> = LinkedList()
    private var mUnreadTotal = 0

    init {
        TUIKitLog.i(TAG, "init")
        mConversationPreferences = appContext.getSharedPreferences(TIMManager.getInstance().loginUser + SP_NAME, Context.MODE_PRIVATE)
        mTopLinkedList = getListData(mConversationPreferences, TOP_LIST, ConversationInfo::class.java)
        MessageRevokedManager.instance.addHandler(this)
    }

    /**
     * 加载会话信息
     *
     * @param callBack
     */
    fun loadConversation(callBack: IUIKitCallBack?) {
        TUIKitLog.i(TAG, "loadConversation callBack:$callBack")
        mUnreadTotal = 0
        //mProvider初始化值为null,用户注销时会销毁，登录完成进入需再次实例化
        if (mProvider == null) {
            mProvider = ConversationProvider()
        }
        val TIMConversations = TIMManager.getInstance().conversationList
        val infos = ArrayList<ConversationInfo?>()
        for (i in TIMConversations.indices) {
            val conversation = TIMConversations[i]
            //将imsdk TIMConversation转换为UIKit ConversationInfo
            val conversationInfo = TIMConversation2ConversationInfo(conversation)
            if (conversationInfo != null) {
                mUnreadTotal = mUnreadTotal + conversationInfo.unRead
                conversationInfo.type = ConversationInfo.TYPE_COMMON //
                infos.add(conversationInfo)
            }
        }
        //排序，imsdk加载处理的已按时间排序，但应用层有置顶会话操作，所有需根据置顶标识再次排序（置顶可考虑做到imsdk同步到服务器？）
        mProvider?.setDataSource(sortConversations(infos))
        putListData(mConversationPreferences!!, TOP_LIST, mTopLinkedList!!)
        //更新消息未读总数
        updateUnreadTotal(mUnreadTotal)
        callBack?.onSuccess(mProvider)
    }

    /**
     * 数据刷新通知回调（如未读计数，会话列表等）
     */
    override fun onRefresh() {}

    /**
     * 部分会话刷新（包括多终端已读上报同步）
     *
     * @param conversations 需要刷新的会话列表
     */
    override fun onRefreshConversation(conversations: List<TIMConversation>) {
        TUIKitLog.i(TAG, "onRefreshConversation conversations:$conversations")
        if (mProvider == null) {
            return
        }
        val infos = ArrayList<ConversationInfo>()
        for (i in conversations.indices) {
            val conversation = conversations[i]
            TUIKitLog.i(TAG, "onRefreshConversation TIMConversation $conversation")
            val conversationInfo = TIMConversation2ConversationInfo(conversation)
            if (conversation.type == TIMConversationType.System) {
                val message = conversation.lastMsg
                if (message.elementCount > 0) {
                    val ele = message.getElement(0)
                    val eleType = ele.type
                    if (eleType == TIMElemType.GroupSystem) {
                        val groupSysEle = ele as TIMGroupSystemElem
                        if (groupSysEle.subtype == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_INVITED_TO_GROUP_TYPE) {
                            var group = conversation.groupName
                            if (TextUtils.isEmpty(group)) {
                                group = groupSysEle.groupId
                            }
                            toastLongMessage("您已经被邀请进群【$group】，请到我的群聊里面查看！")
                        }
                    }
                }
            }
            if (conversationInfo != null) {
                infos.add(conversationInfo)
            }
        }
        if (infos.size == 0) {
            return
        }
        val dataSource: MutableList<ConversationInfo?> = mProvider?.getDataSource() ?: ArrayList()
        val exists = ArrayList<Any?>()
        for (j in infos.indices) {
            val update = infos[j]
            var exist = false
            for (i in dataSource?.indices) {
                val cacheInfo = dataSource[i]
                //单个会话刷新时找到老的会话数据，替换
                if (cacheInfo?.id == update.id) {
                    dataSource.removeAt(i)
                    dataSource.add(i, update)
                    exists.add(update)
                    //infos.remove(j);
//需同步更新未读计数
                    mUnreadTotal = mUnreadTotal - (cacheInfo?.unRead ?: 0) + update.unRead
                    TUIKitLog.i(TAG, "onRefreshConversation after mUnreadTotal = $mUnreadTotal")
                    exist = true
                    break
                }
            }
            if (!exist) {
                mUnreadTotal += update.unRead
                TUIKitLog.i(TAG, "onRefreshConversation exist = $exist, mUnreadTotal = $mUnreadTotal")
            }
        }
        updateUnreadTotal(mUnreadTotal)
        infos.removeAll(exists)
        if (infos.size > 0) {
            dataSource.addAll(infos)
        }
        mProvider?.setDataSource(sortConversations(dataSource))
        putListData(mConversationPreferences!!, TOP_LIST, mTopLinkedList!!)
    }

    /**
     * TIMConversation转换为ConversationInfo
     *
     * @param conversation
     * @return
     */
    private fun TIMConversation2ConversationInfo(conversation: TIMConversation?): ConversationInfo? {
        if (conversation == null) {
            return null
        }
        TUIKitLog.i(TAG, "loadConversation conversation peer " + conversation.peer + ", groupName " + conversation.groupName)
        val message = conversation.lastMsg ?: return null
        val info = ConversationInfo()
        val type = conversation.type
        if (type == TIMConversationType.System) {
            if (message.elementCount > 0) {
                val ele = message.getElement(0)
                val eleType = ele.type
                if (eleType == TIMElemType.GroupSystem) {
                    val groupSysEle = ele as TIMGroupSystemElem
                    groupSystMsgHandle(groupSysEle)
                }
            }
            return null
        }
        val isGroup = type == TIMConversationType.Group
        info.lastMessageTime = message.timestamp()
        val list: List<MessageInfo?>? = TIMMessage2MessageInfo(message, isGroup)
        if (list != null && list.size > 0) {
            info.lastMessage = list[list.size - 1]
        }
        if (isGroup) {
            val groupDetailInfo = TIMGroupManager.getInstance().queryGroupInfo(conversation.peer)
            if (groupDetailInfo == null) {
                if (TextUtils.isEmpty(conversation.groupName)) {
                    info.title = conversation.peer
                } else {
                    info.title = conversation.groupName
                }
                val ids = ArrayList<String>()
                ids.add(conversation.peer)
                TIMGroupManager.getInstance().getGroupInfo(ids, object : TIMValueCallBack<List<TIMGroupDetailInfoResult?>?> {
                    override fun onError(code: Int, desc: String) {
                        TUIKitLog.e(TAG, "getGroupInfo failed! code: $code desc: $desc")
                    }

                    override fun onSuccess(timGroupDetailInfoResults: List<TIMGroupDetailInfoResult?>?) {
                        if (timGroupDetailInfoResults == null || timGroupDetailInfoResults.size != 1) {
                            TUIKitLog.i(TAG, "No GroupInfo")
                            return
                        }
                        val result = timGroupDetailInfoResults[0]
                        if (result != null) {
                            if (TextUtils.isEmpty(result.groupName)) {
                                info.title = result.groupId
                            } else {
                                info.title = result.groupName
                            }
                        }
                    }
                })
            } else {
                info.iconUrl = groupDetailInfo.faceUrl
                if (TextUtils.isEmpty(groupDetailInfo.groupName)) {
                    info.title = groupDetailInfo.groupId
                } else {
                    info.title = groupDetailInfo.groupName
                }
            }
        } else {
            var title = conversation.peer
            var faceUrl: String? = null
            val ids = ArrayList<String>()
            ids.add(conversation.peer)
            val profile = TIMFriendshipManager.getInstance().queryUserProfile(conversation.peer)
            if (profile == null) {
                TIMFriendshipManager.getInstance().getUsersProfile(ids, false, object : TIMValueCallBack<List<TIMUserProfile?>?> {
                    override fun onError(code: Int, desc: String) {
                        TUIKitLog.e(TAG, "getUsersProfile failed! code: $code desc: $desc")
                    }

                    override fun onSuccess(timUserProfiles: List<TIMUserProfile?>?) {
                        if (timUserProfiles == null || timUserProfiles.size != 1) {
                            TUIKitLog.i(TAG, "No TIMUserProfile")
                            return
                        }
                        val profile = timUserProfiles[0]
                        var faceUrl: String? = null
                        if (profile != null && !TextUtils.isEmpty(profile.faceUrl)) {
                            faceUrl = profile.faceUrl
                        }
                        var title = conversation.peer
                        if (profile != null && !TextUtils.isEmpty(profile.nickName)) {
                            title = profile.nickName
                        }
                        info.title = title
                        info.iconUrl = faceUrl
                        mProvider?.updateAdapter()
                    }
                })
            } else {
                if (!TextUtils.isEmpty(profile.nickName)) {
                    title = profile.nickName
                }
                if (!TextUtils.isEmpty(profile.faceUrl)) {
                    faceUrl = profile.faceUrl
                }
                info.title = title
                info.iconUrl = faceUrl
            }
            val friend = TIMFriendshipManager.getInstance().queryFriend(conversation.peer)
            if (friend == null) {
                TIMFriendshipManager.getInstance().getFriendList(object : TIMValueCallBack<List<TIMFriend>?> {
                    override fun onError(code: Int, desc: String) {
                        TUIKitLog.e(TAG, "getFriendList failed! code: $code desc: $desc")
                    }

                    override fun onSuccess(timFriends: List<TIMFriend>?) {
                        if (timFriends == null || timFriends.size == 0) {
                            TUIKitLog.i(TAG, "No Friends")
                            return
                        }
                        for (friend in timFriends) {
                            if (!TextUtils.equals(conversation.peer, friend.identifier)) {
                                continue
                            }
                            if (TextUtils.isEmpty(friend.remark)) {
                                continue
                            }
                            info.title = friend.remark
                            mProvider?.updateAdapter()
                            return
                        }
                        TUIKitLog.i(TAG, conversation.peer + " is not my friend")
                    }
                })
            } else {
                if (!TextUtils.isEmpty(friend.remark)) {
                    title = friend.remark
                    info.title = title
                }
            }
        }
        info.id = conversation.peer
        info.isGroup = conversation.type == TIMConversationType.Group
        if (conversation.unreadMessageNum > 0) {
            info.unRead = conversation.unreadMessageNum.toInt()
        }
        TUIKitLog.i(TAG, "onRefreshConversation ext.getUnreadMessageNum() " + conversation.unreadMessageNum)
        return info
    }

    /**
     * 群系统消息处理，不需要显示信息的
     *
     * @param groupSysEle
     */
    private fun groupSystMsgHandle(groupSysEle: TIMGroupSystemElem) {
        val type = groupSysEle.subtype
        //群组解散或者被踢出群组
        if (type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_KICK_OFF_FROM_GROUP_TYPE
                || type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE) { //imsdk会自动删除持久化的数据，应用层只需删除会话数据源中的即可
            deleteConversation(groupSysEle.groupId, true)
        }
    }

    /**
     * 将某个会话置顶
     *
     * @param index
     * @param conversation
     */
    fun setConversationTop(index: Int, conversation: ConversationInfo) {
        TUIKitLog.i(TAG, "setConversationTop index:$index|conversation:$conversation")
        if (!conversation.isTop) {
            mTopLinkedList?.remove(conversation)
            mTopLinkedList?.addFirst(conversation)
            conversation.isTop = true
        } else {
            conversation.isTop = false
            mTopLinkedList?.remove(conversation)
        }
        mProvider?.setDataSource(sortConversations(mProvider?.getDataSource()))
        putListData(mConversationPreferences!!, TOP_LIST, mTopLinkedList!!)
    }

    /**
     * 会话置顶操作
     *
     * @param id   会话ID
     * @param flag 是否置顶
     */
    fun setConversationTop(id: String, flag: Boolean) {
        TUIKitLog.i(TAG, "setConversationTop id:$id|flag:$flag")
        handleTopData(id, flag)
        mProvider?.setDataSource(sortConversations(mProvider?.getDataSource()))
        putListData(mConversationPreferences!!, TOP_LIST, mTopLinkedList!!)
    }

    private fun isTop(id: String?): Boolean {
        if (mTopLinkedList == null || mTopLinkedList?.size == 0) {
            return false
        }
        for (info in mTopLinkedList!!) {
            if (TextUtils.equals(info.id, id)) {
                return true
            }
        }
        return false
    }

    /**
     * 会话置顶的本地储存操作，目前用SharePreferences来持久化置顶信息
     *
     * @param id
     * @param flag
     */
    private fun handleTopData(id: String?, flag: Boolean) {
        var conversation: ConversationInfo? = null
        val conversationInfos: List<ConversationInfo?> = mProvider?.getDataSource() ?: ArrayList()
        for (i in conversationInfos?.indices) {
            val info = conversationInfos[i]
            if (info?.id == id) {
                conversation = info
                break
            }
        }
        if (conversation == null) {
            return
        }
        if (flag) {
            if (!isTop(conversation.id)) {
                mTopLinkedList?.remove(conversation)
                mTopLinkedList?.addFirst(conversation)
                conversation.isTop = true
            } else {
                return
            }
        } else {
            if (isTop(conversation.id)) {
                conversation.isTop = false
                mTopLinkedList?.remove(conversation)
            } else {
                return
            }
        }
        putListData(mConversationPreferences!!, TOP_LIST, mTopLinkedList!!)
    }

    /**
     * 删除会话，会将本地会话数据从imsdk中删除
     *
     * @param index        在数据源中的索引
     * @param conversation 会话信息
     */
    fun deleteConversation(index: Int, conversation: ConversationInfo) {
        TUIKitLog.i(TAG, "deleteConversation index:$index|conversation:$conversation")
        val status = TIMManager.getInstance().deleteConversation(if (conversation.isGroup) TIMConversationType.Group else TIMConversationType.C2C, conversation.id)
        if (status) {
            handleTopData(conversation.id, false)
            mProvider?.deleteConversation(index)
            updateUnreadTotal(mUnreadTotal - conversation.unRead)
        }
    }

    /**
     * 删除会话，只删除数据源中的会话信息
     *
     * @param id 会话id
     */
    fun deleteConversation(id: String, isGroup: Boolean) {
        TUIKitLog.i(TAG, "deleteConversation id:$id|isGroup:$isGroup")
        handleTopData(id, false)
        val conversationInfos: List<ConversationInfo?> = mProvider?.getDataSource() ?: ArrayList()
        for (i in conversationInfos?.indices) {
            val info = conversationInfos[i]
            if (info?.id == id) {
                updateUnreadTotal(mUnreadTotal - info.unRead)
                break
            }
        }
        if (mProvider != null) {
            mProvider?.deleteConversation(id)
        }
        TIMManager.getInstance().deleteConversation(if (isGroup) TIMConversationType.Group else TIMConversationType.C2C, id)
    }

    /**
     * 添加会话
     *
     * @param conversationInfo
     * @return
     */
    fun addConversation(conversationInfo: ConversationInfo?): Boolean {
        val conversationInfos: MutableList<ConversationInfo?> = ArrayList()
        conversationInfos.add(conversationInfo)
        return mProvider?.addConversations(conversationInfos) ?: false
    }

    /**
     * 会话数据排序，添加了置顶标识的处理
     *
     * @param sources
     * @return
     */
    private fun sortConversations(sources: List<ConversationInfo?>?): List<ConversationInfo?> {
        val conversationInfos = ArrayList<ConversationInfo?>()
        val normalConversations: MutableList<ConversationInfo?> = ArrayList()
        val topConversations: MutableList<ConversationInfo> = ArrayList()
        for (i in 0..((sources?.size ?: 0) - 1)) {
            val conversation = sources!![i]
            conversation?.let {
                if (isTop(conversation.id)) {
                    conversation.isTop = true
                    topConversations.add(conversation)
                } else {
                    normalConversations.add(conversation)
                }
            }
        }
        mTopLinkedList?.clear()
        mTopLinkedList?.addAll(topConversations)
        Collections.sort(topConversations) // 置顶会话列表页也需要按照最后一条时间排序，由新到旧，如果旧会话收到新消息，会排序到前面
        conversationInfos.addAll(topConversations)
        Collections.sort(normalConversations) // 正常会话也是按照最后一条消息时间排序，由新到旧
        conversationInfos.addAll(normalConversations)
        return conversationInfos
    }

    /**
     * 更新会话未读计数
     *
     * @param unreadTotal
     */
    fun updateUnreadTotal(unreadTotal: Int) {
        TUIKitLog.i(TAG, "updateUnreadTotal:$unreadTotal")
        mUnreadTotal = unreadTotal
        mUnreadWatchers?.let {
            for (i in mUnreadWatchers?.indices) {
                mUnreadWatchers[i].updateUnread(mUnreadTotal)
            }
        }
    }

    fun isTopConversation(groupId: String): Boolean {
        TUIKitLog.i(TAG, "isTopConversation:$groupId")
        return isTop(groupId)
    }

    /**
     * 消息撤回回调
     *
     * @param locator
     */
    override fun handleInvoke(locator: TIMMessageLocator) {
        TUIKitLog.i(TAG, "handleInvoke:$locator")
        if (mProvider != null) {
            loadConversation(null)
        }
    }

    /**
     * 添加未读计数监听器
     *
     * @param messageUnreadWatcher
     */
    fun addUnreadWatcher(messageUnreadWatcher: MessageUnreadWatcher) {
        TUIKitLog.i(TAG, "addUnreadWatcher:$messageUnreadWatcher")
        if (true == mUnreadWatchers?.contains(messageUnreadWatcher)) {
            mUnreadWatchers.add(messageUnreadWatcher)
        }
    }

    /**
     * 与UI做解绑操作，避免内存泄漏
     */
    fun destroyConversation() {
        TUIKitLog.i(TAG, "destroyConversation")
        if (mProvider != null) {
            mProvider?.attachAdapter(null)
        }
        mUnreadWatchers?.clear()
        mUnreadTotal = 0
    }

    /**
     * 会话未读计数变化监听器
     */
    interface MessageUnreadWatcher {
        fun updateUnread(count: Int)
    }

    companion object {
        private val TAG = ConversationManagerKit::class.java.simpleName
        private const val SP_NAME = "top_conversion_list"
        private const val TOP_LIST = "top_list"
        val instance = ConversationManagerKit()
    }
}