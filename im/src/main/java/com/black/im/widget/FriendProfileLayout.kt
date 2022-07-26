package com.black.im.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.activity.SelectionActivity
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.manager.ConversationManagerKit
import com.black.im.manager.GroupChatManagerKit
import com.black.im.model.ContactItemBean
import com.black.im.model.chat.ChatInfo
import com.black.im.model.group.GroupApplyInfo
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.util.ToastUtil.toastShortMessage
import com.black.lib.widget.CircleImageView
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMFriendshipManager
import com.tencent.imsdk.TIMUserProfile
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.friendship.*
import java.util.*
import kotlin.collections.ArrayList

class FriendProfileLayout : LinearLayout, OnClickListener {
    companion object {
        private val TAG = FriendProfileLayout::class.java.simpleName
    }

    private val CHANGE_REMARK_CODE = 200
    private var mTitleBar: TitleBarLayout? = null
    private var mHeadImageView: CircleImageView? = null
    private var mNickNameView: TextView? = null
    private var mIDView: LineControllerView? = null
    private var mAddWordingView: LineControllerView? = null
    private var mRemarkView: LineControllerView? = null
    private var mAddBlackView: LineControllerView? = null
    private var mChatTopView: LineControllerView? = null
    private var mDeleteView: TextView? = null
    private var mChatView: TextView? = null
    private var mContactInfo: ContactItemBean? = null
    private var mChatInfo: ChatInfo? = null
    private var mPendencyItem: TIMFriendPendencyItem? = null
    private var mListener: OnButtonClickListener? = null
    private var mId: String? = null
    private var mNickname: String? = null
    private val mRemark: String? = null
    private val mAddWords: String? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.contact_friend_profile_layout, this)
        mHeadImageView = findViewById(R.id.avatar)
        mNickNameView = findViewById(R.id.name)
        mIDView = findViewById(R.id.id)
        mAddWordingView = findViewById(R.id.add_wording)
        mAddWordingView?.setCanNav(false)
        mAddWordingView?.setSingleLine(false)
        mRemarkView = findViewById(R.id.remark)
        mRemarkView?.setOnClickListener(this)
        mChatTopView = findViewById(R.id.chat_to_top)
        mAddBlackView = findViewById(R.id.blackList)
        mDeleteView = findViewById(R.id.btnDel)
        mDeleteView?.setOnClickListener(this)
        mChatView = findViewById(R.id.btnChat)
        mChatView?.setOnClickListener(this)
        mTitleBar = findViewById(R.id.friend_titlebar)
        mTitleBar?.setTitle(resources.getString(R.string.profile_detail), ITitleBarLayout.POSITION.MIDDLE)
        mTitleBar?.rightGroup?.visibility = View.GONE
        mTitleBar?.setOnLeftClickListener(OnClickListener { (context as Activity).finish() })
    }

    fun initData(data: Any?) {
        if (data is ChatInfo) {
            mChatInfo = data
            mId = mChatInfo?.id
            mChatTopView?.visibility = View.VISIBLE
            mChatTopView?.isChecked = if (mId == null) false else ConversationManagerKit.instance.isTopConversation(mId!!)
            mChatTopView?.setCheckListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                mId?.let {
                    ConversationManagerKit.instance.setConversationTop(it, isChecked)
                }
            })
            loadUserProfile()
            return
        } else if (data is ContactItemBean) {
            mContactInfo = data
            mId = mContactInfo?.id
            mNickname = mContactInfo?.nickname
            mRemarkView?.visibility = View.VISIBLE
            mRemarkView?.content = mContactInfo?.remark
            mAddBlackView?.isChecked = mContactInfo?.isBlackList ?: false
            mAddBlackView?.setCheckListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    addBlack()
                } else {
                    deleteBlack()
                }
            })
            if (!TextUtils.isEmpty(mContactInfo?.avatarurl)) {
                GlideEngine.loadImage(mHeadImageView, Uri.parse(mContactInfo?.avatarurl))
            }
        } else if (data is TIMFriendPendencyItem) {
            mPendencyItem = data
            mId = mPendencyItem?.identifier
            mNickname = mPendencyItem?.nickname
            mAddWordingView?.visibility = View.VISIBLE
            mAddWordingView?.content = mPendencyItem?.addWording
            mRemarkView?.visibility = View.GONE
            mAddBlackView?.visibility = View.GONE
            mDeleteView?.setText(R.string.refuse)
            mDeleteView?.setOnClickListener { refuse() }
            mChatView?.setText(R.string.accept)
            mChatView?.setOnClickListener { accept() }
        } else if (data is GroupApplyInfo) {
            val item = data.pendencyItem
            mId = item.identifer
            if (TextUtils.isEmpty(mId)) {
                mId = item.fromUser
            }
            mNickname = item.fromUser
            mAddWordingView?.visibility = View.VISIBLE
            mAddWordingView?.content = item.requestMsg
            mRemarkView?.visibility = View.GONE
            mAddBlackView?.visibility = View.GONE
            mDeleteView?.setText(R.string.refuse)
            mDeleteView?.setOnClickListener { refuseApply(data) }
            mChatView?.setText(R.string.accept)
            mChatView?.setOnClickListener { acceptApply(data) }
        }
        if (!TextUtils.isEmpty(mNickname)) {
            mNickNameView?.text = mNickname
        } else {
            mNickNameView?.text = mId
        }
        mIDView?.content = mId
    }

    private fun updateViews(bean: ContactItemBean) {
        mContactInfo = bean
        mChatTopView?.visibility = View.VISIBLE
        val top: Boolean = if (mId == null) false else ConversationManagerKit.instance.isTopConversation(mId!!)
        if (mChatTopView?.isChecked != top) {
            mChatTopView?.isChecked = top
        }
        mChatTopView?.setCheckListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mId?.let {
                ConversationManagerKit.instance.setConversationTop(it, isChecked)
            }
        })
        mId = bean.id
        mNickname = bean.nickname
        if (bean.isFriend) {
            mRemarkView?.visibility = View.VISIBLE
            mRemarkView?.content = bean.remark
            mAddBlackView?.visibility = View.VISIBLE
            mAddBlackView?.isChecked = bean.isBlackList
            mAddBlackView?.setCheckListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    addBlack()
                } else {
                    deleteBlack()
                }
            })
            mDeleteView?.visibility = View.VISIBLE
        } else {
            mRemarkView?.visibility = View.GONE
            mAddBlackView?.visibility = View.GONE
            mDeleteView?.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(mNickname)) {
            mNickNameView?.text = mNickname
        } else {
            mNickNameView?.text = mId
        }
        if (!TextUtils.isEmpty(bean.avatarurl)) {
            GlideEngine.loadImage(mHeadImageView, Uri.parse(bean.avatarurl))
        }
        mIDView?.content = mId
    }

    private fun loadUserProfile() {
        val list = ArrayList<String?>()
        list.add(mId)
        val bean = ContactItemBean()
        bean.isFriend = false
        TIMFriendshipManager.getInstance().getUsersProfile(list, false, object : TIMValueCallBack<List<TIMUserProfile>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "loadUserProfile err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timUserProfiles: List<TIMUserProfile>?) {
                if (timUserProfiles == null || timUserProfiles.size != 1) {
                    return
                }
                val profile = timUserProfiles[0]
                bean.nickname = profile.nickName
                bean.setId(profile.identifier)
                bean.avatarurl = profile.faceUrl
                updateViews(bean)
            }
        })
        TIMFriendshipManager.getInstance().getBlackList(object : TIMValueCallBack<List<TIMFriend>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "getBlackList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timFriends: List<TIMFriend>?) {
                if (timFriends != null && timFriends.isNotEmpty()) {
                    for (friend in timFriends) {
                        if (TextUtils.equals(friend.identifier, mId)) {
                            bean.isBlackList = true
                            updateViews(bean)
                            break
                        }
                    }
                }
            }
        })
        TIMFriendshipManager.getInstance().getFriendList(object : TIMValueCallBack<List<TIMFriend>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "getFriendList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timFriends: List<TIMFriend>?) {
                if (timFriends != null && timFriends.isNotEmpty()) {
                    for (friend in timFriends) {
                        if (TextUtils.equals(friend.identifier, mId)) {
                            bean.isFriend = true
                            bean.remark = friend.remark
                            bean.avatarurl = friend.timUserProfile.faceUrl
                            break
                        }
                    }
                }
                updateViews(bean)
            }
        })
    }

    private fun accept() {
        val response = TIMFriendResponse()
        response.identifier = mId
        response.responseType = TIMFriendResponse.TIM_FRIEND_RESPONSE_AGREE_AND_ADD
        TIMFriendshipManager.getInstance().doResponse(response, object : TIMValueCallBack<TIMFriendResult?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "accept err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timUserProfiles: TIMFriendResult?) {
                TUIKitLog.i(TAG, "accept success")
                mChatView?.setText(R.string.accepted)
                (context as Activity).finish()
            }
        })
    }

    private fun refuse() {
        val response = TIMFriendResponse()
        response.identifier = mId
        response.responseType = TIMFriendResponse.TIM_FRIEND_RESPONSE_REJECT
        TIMFriendshipManager.getInstance().doResponse(response, object : TIMValueCallBack<TIMFriendResult?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "refuse err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timUserProfiles: TIMFriendResult?) {
                TUIKitLog.i(TAG, "refuse success")
                mDeleteView?.setText(R.string.refused)
                (context as Activity).finish()
            }
        })
    }

    private fun acceptApply(item: GroupApplyInfo?) {
        item?.let {
            GroupChatManagerKit.instance.provider.acceptApply(item, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    val intent = Intent()
                    intent.putExtra(TUIKitConstants.Group.MEMBER_APPLY, item)
                    (context as Activity).setResult(Activity.RESULT_OK, intent)
                    (context as Activity).finish()
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage(errMsg)
                }
            })
        }
    }

    private fun refuseApply(item: GroupApplyInfo?) {
        item?.let {
            GroupChatManagerKit.instance.provider.refuseApply(item, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    val intent = Intent()
                    intent.putExtra(TUIKitConstants.Group.MEMBER_APPLY, item)
                    (context as Activity).setResult(Activity.RESULT_OK, intent)
                    (context as Activity).finish()
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage(errMsg)
                }
            })
        }
    }

    private fun delete() {
        val identifiers: MutableList<String?> = ArrayList()
        identifiers.add(mId)
        TIMFriendshipManager.getInstance().deleteFriends(identifiers, TIMDelFriendType.TIM_FRIEND_DEL_BOTH, object : TIMValueCallBack<List<TIMFriendResult?>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "deleteFriends err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timUserProfiles: List<TIMFriendResult?>?) {
                TUIKitLog.i(TAG, "deleteFriends success")
                mId?.let {
                    ConversationManagerKit.instance.deleteConversation(it, false)
                    if (mListener != null) {
                        mListener?.onDeleteFriendClick(mId)
                    }
                    (context as Activity).finish()
                }
            }
        })
    }

    private fun chat() {
        if (mListener != null || mContactInfo != null) {
            mListener?.onStartConversationClick(mContactInfo)
        }
        (context as Activity).finish()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnChat -> {
                chat()
            }
            R.id.btnDel -> {
                delete()
            }
            R.id.remark -> {
                val bundle = Bundle()
                bundle.putString(TUIKitConstants.Selection.TITLE, resources.getString(R.string.profile_remark_edit))
                bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mRemarkView?.content)
                bundle.putInt(TUIKitConstants.Selection.LIMIT, 20)
                SelectionActivity.startTextSelection(appContext, bundle, object : SelectionActivity.OnResultReturnListener {
                    override fun onReturn(res: Any?) {
                        var newText = res
                        mRemarkView?.content = newText.toString()
                        if (TextUtils.isEmpty(newText.toString())) {
                            newText = ""
                        }
                        modifyRemark(newText.toString())
                    }

                })
            }
        }
    }

    private fun modifyRemark(txt: String) {
        val hashMap = HashMap<String, Any>()
        // 修改好友备注
        hashMap[TIMFriend.TIM_FRIEND_PROFILE_TYPE_KEY_REMARK] = txt
        TIMFriendshipManager.getInstance().modifyFriend(mId!!, hashMap, object : TIMCallBack {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "modifyRemark err code = $i, desc = $s")
            }

            override fun onSuccess() {
                mContactInfo?.remark = txt
                TUIKitLog.i(TAG, "modifyRemark success")
            }
        })
    }

    private fun addBlack() {
        val idStringList: Array<String> = mId?.split(",")?.toTypedArray() ?: arrayOf()

        val idList: MutableList<String> = ArrayList()
        for (id in idStringList) {
            idList.add(id)
        }
        TIMFriendshipManager.getInstance().addBlackList(idList, object : TIMValueCallBack<List<TIMFriendResult?>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "addBlackList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timFriendResults: List<TIMFriendResult?>?) {
                TUIKitLog.i(TAG, "addBlackList success")
            }
        })
    }

    private fun deleteBlack() {
        val idStringList = mId?.split(",")?.toTypedArray() ?: arrayOf()
        val idList: MutableList<String> = ArrayList()
        for (id in idStringList) {
            idList.add(id)
        }
        TIMFriendshipManager.getInstance().deleteBlackList(idList, object : TIMValueCallBack<List<TIMFriendResult?>?> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "deleteBlackList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timFriendResults: List<TIMFriendResult?>?) {
                TUIKitLog.i(TAG, "deleteBlackList success")
            }
        })
    }

    fun setOnButtonClickListener(l: OnButtonClickListener?) {
        mListener = l
    }

    interface OnButtonClickListener {
        fun onStartConversationClick(info: ContactItemBean?)
        fun onDeleteFriendClick(id: String?)
    }
}