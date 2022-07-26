package com.black.im.widget

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.CompoundButton
import android.widget.GridView
import android.widget.LinearLayout
import com.black.im.R
import com.black.im.activity.SelectionActivity
import com.black.im.adapter.GroupInfoAdapter
import com.black.im.interfaces.IGroupMemberLayout
import com.black.im.interfaces.IGroupMemberRouter
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.model.group.GroupInfo
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.view.GroupInfoPresenter
import com.black.im.view.TUIKitDialog
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMGroupManager
import java.util.*

class GroupInfoLayout : LinearLayout, IGroupMemberLayout, OnClickListener {
    companion object {
        private val TAG = GroupInfoLayout::class.java.simpleName
    }

    override var titleBar: TitleBarLayout? = null
        private set
    private var mMemberView: LineControllerView? = null
    private var mMemberAdapter: GroupInfoAdapter? = null
    private var mMemberPreviewListener: IGroupMemberRouter? = null
    private var mGroupTypeView: LineControllerView? = null
    private var mGroupIDView: LineControllerView? = null
    private var mGroupNameView: LineControllerView? = null
    private var mGroupIcon: LineControllerView? = null
    private var mGroupNotice: LineControllerView? = null
    private var mNickView: LineControllerView? = null
    private var mJoinTypeView: LineControllerView? = null
    private var mTopSwitchView: LineControllerView? = null
    private var mDissolveBtn: Button? = null
    private var mGroupInfo: GroupInfo? = null
    private var mPresenter: GroupInfoPresenter? = null
    private val mJoinTypes = ArrayList<String>()

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
        View.inflate(context, R.layout.group_info_layout, this)
        // 标题
        titleBar = findViewById(R.id.group_info_title_bar)
        titleBar?.rightGroup?.visibility = View.GONE
        titleBar?.setTitle(resources.getString(R.string.group_detail), ITitleBarLayout.POSITION.MIDDLE)
        titleBar?.setOnLeftClickListener(OnClickListener { (context as Activity).finish() })
        // 成员标题
        mMemberView = findViewById(R.id.group_member_bar)
        mMemberView?.setOnClickListener(this)
        mMemberView?.setCanNav(true)
        // 成员列表
        val memberList = findViewById<GridView>(R.id.group_members)
        mMemberAdapter = GroupInfoAdapter()
        memberList.adapter = mMemberAdapter
        // 群类型，只读
        mGroupTypeView = findViewById(R.id.group_type_bar)
        // 群ID，只读
        mGroupIDView = findViewById(R.id.group_account)
        // 群聊名称
        mGroupNameView = findViewById(R.id.group_name)
        mGroupNameView?.setOnClickListener(this)
        mGroupNameView?.setCanNav(true)
        // 群头像
        mGroupIcon = findViewById(R.id.group_icon)
        mGroupIcon?.setOnClickListener(this)
        mGroupIcon?.setCanNav(false)
        // 群公告
        mGroupNotice = findViewById(R.id.group_notice)
        mGroupNotice?.setOnClickListener(this)
        mGroupNotice?.setCanNav(true)
        // 加群方式
        mJoinTypeView = findViewById(R.id.join_type_bar)
        mJoinTypeView?.setOnClickListener(this)
        mJoinTypeView?.setCanNav(true)
        mJoinTypes.addAll(listOf(*resources.getStringArray(R.array.group_join_type)))
        // 群昵称
        mNickView = findViewById(R.id.self_nickname_bar)
        mNickView?.setOnClickListener(this)
        mNickView?.setCanNav(true)
        // 是否置顶
        mTopSwitchView = findViewById(R.id.chat_to_top_switch)
        mTopSwitchView?.setCheckListener(CompoundButton.OnCheckedChangeListener { _, isChecked -> mPresenter?.setTopConversation(isChecked) })
        // 退群
        mDissolveBtn = findViewById(R.id.group_dissolve_button)
        mDissolveBtn?.setOnClickListener(this)
        mPresenter = GroupInfoPresenter(this)
    }

    override fun onClick(v: View) {
        if (mGroupInfo == null) {
            TUIKitLog.e(TAG, "mGroupInfo is NULL")
            return
        }
        if (v.id == R.id.group_member_bar) {
            if (mMemberPreviewListener != null) {
                mMemberPreviewListener?.forwardListMember(mGroupInfo)
            }
        } else if (v.id == R.id.group_name) {
            val bundle = Bundle()
            bundle.putString(TUIKitConstants.Selection.TITLE, resources.getString(R.string.modify_group_name))
            bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mGroupNameView?.content)
            bundle.putInt(TUIKitConstants.Selection.LIMIT, 20)
            SelectionActivity.startTextSelection(context as Activity, bundle, object : SelectionActivity.OnResultReturnListener {
                override fun onReturn(res: Any?) {
                    mPresenter?.modifyGroupName(res.toString())
                    mGroupNameView?.content = res.toString()
                }

            })
        } else if (v.id == R.id.group_icon) {
            mGroupInfo?.id?.let {
                val groupUrl = String.format("https://picsum.photos/id/%d/200/200", Random().nextInt(1000))
                val param = TIMGroupManager.ModifyGroupInfoParam(it)
                param.faceUrl = groupUrl
                TIMGroupManager.getInstance().modifyGroupInfo(param, object : TIMCallBack {
                    override fun onError(code: Int, desc: String) {
                        TUIKitLog.e(TAG, "modify group icon failed, code:$code|desc:$desc")
                        toastLongMessage("修改群头像失败, code = $code, info = $desc")
                    }

                    override fun onSuccess() {
                        toastLongMessage("修改群头像成功")
                    }
                })
            }
        } else if (v.id == R.id.group_notice) {
            val bundle = Bundle()
            bundle.putString(TUIKitConstants.Selection.TITLE, resources.getString(R.string.modify_group_notice))
            bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mGroupNotice?.content)
            bundle.putInt(TUIKitConstants.Selection.LIMIT, 200)
            SelectionActivity.startTextSelection(context as Activity, bundle, object : SelectionActivity.OnResultReturnListener {
                override fun onReturn(res: Any?) {
                    mPresenter?.modifyGroupNotice(res.toString())
                    mGroupNotice?.content = res.toString()
                }

            })
        } else if (v.id == R.id.self_nickname_bar) {
            val bundle = Bundle()
            bundle.putString(TUIKitConstants.Selection.TITLE, resources.getString(R.string.modify_nick_name_in_goup))
            bundle.putString(TUIKitConstants.Selection.INIT_CONTENT, mNickView?.content)
            bundle.putInt(TUIKitConstants.Selection.LIMIT, 20)
            SelectionActivity.startTextSelection(context as Activity, bundle, object : SelectionActivity.OnResultReturnListener {
                override fun onReturn(res: Any?) {
                    mPresenter?.modifyMyGroupNickname(res.toString())
                    mNickView?.content = res.toString()
                }
            })
        } else if (v.id == R.id.join_type_bar) {
            if (mGroupTypeView?.content == "聊天室") {
                toastLongMessage("加入聊天室为自动审批，暂不支持修改")
                return
            }
            val bundle = Bundle()
            bundle.putString(TUIKitConstants.Selection.TITLE, resources.getString(R.string.group_join_type))
            bundle.putStringArrayList(TUIKitConstants.Selection.LIST, mJoinTypes)
            bundle.putInt(TUIKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX, mGroupInfo?.joinType
                    ?: 0)
            SelectionActivity.startListSelection(context as Activity, bundle, object : SelectionActivity.OnResultReturnListener {
                override fun onReturn(res: Any?) {
                    if (res is Int) {
                        mPresenter?.modifyGroupInfo(res, TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE)
                        mJoinTypeView?.content = mJoinTypes[res]
                    }
                }
            })
        } else if (v.id == R.id.group_dissolve_button) {
            if (true == mGroupInfo?.isOwner() && mGroupInfo?.groupType != "Private") {
                TUIKitDialog(context)
                        .builder()
                        .setCancelable(true)
                        .setCancelOutside(true)
                        .setTitle("您确认解散该群?")
                        .setDialogWidth(0.75f)
                        .setPositiveButton("确定", OnClickListener { mPresenter?.deleteGroup() })
                        .setNegativeButton("取消", OnClickListener { })
                        .show()
            } else {
                TUIKitDialog(context)
                        .builder()
                        .setCancelable(true)
                        .setCancelOutside(true)
                        .setTitle("您确认退出该群？")
                        .setDialogWidth(0.75f)
                        .setPositiveButton("确定", OnClickListener { mPresenter?.quitGroup() })
                        .setNegativeButton("取消", OnClickListener { })
                        .show()
            }
        }
    }

    fun setGroupId(groupId: String?) {
        mPresenter?.loadGroupInfo(groupId, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                setGroupInfo(data as GroupInfo?)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {}
        })
    }

    private fun setGroupInfo(info: GroupInfo?) {
        if (info == null) {
            return
        }
        mGroupInfo = info
        mGroupNameView?.content = info.groupName
        mGroupIDView?.content = info.id
        mGroupNotice?.content = info.notice
        mMemberView?.content = info.memberCount.toString() + "人"
        mMemberAdapter?.setDataSource(info)
        mGroupTypeView?.content = convertGroupText(info.groupType ?: "")
        mJoinTypeView?.content = mJoinTypes[info.joinType]
        mNickView?.content = mPresenter?.nickName
        mTopSwitchView?.isChecked = mGroupInfo?.isTopChat ?: false
        mDissolveBtn?.setText(R.string.dissolve)
        if (true == mGroupInfo?.isOwner()) {
            mGroupNotice?.visibility = View.VISIBLE
            mJoinTypeView?.visibility = View.VISIBLE
            if (mGroupInfo?.groupType == "Private") {
                mDissolveBtn?.setText(R.string.exit_group)
            }
        } else {
            mGroupNotice?.visibility = View.GONE
            mJoinTypeView?.visibility = View.GONE
            mDissolveBtn?.setText(R.string.exit_group)
        }
    }

    private fun convertGroupText(groupType: String): String {
        var groupText = ""
        if (TextUtils.isEmpty(groupType)) {
            return groupText
        }
        when {
            TextUtils.equals(groupType, TUIKitConstants.GroupType.TYPE_PRIVATE) -> {
                groupText = "讨论组"
            }
            TextUtils.equals(groupType, TUIKitConstants.GroupType.TYPE_PUBLIC) -> {
                groupText = "公开群"
            }
            TextUtils.equals(groupType, TUIKitConstants.GroupType.TYPE_CHAT_ROOM) -> {
                groupText = "聊天室"
            }
        }
        return groupText
    }

    fun onGroupInfoModified(value: Any, type: Int) {
        when (type) {
            TUIKitConstants.Group.MODIFY_GROUP_NAME -> {
                toastLongMessage(resources.getString(R.string.modify_group_name_success))
                mGroupNameView?.content = value.toString()
            }
            TUIKitConstants.Group.MODIFY_GROUP_NOTICE -> {
                mGroupNotice?.content = value.toString()
                toastLongMessage(resources.getString(R.string.modify_group_notice_success))
            }
            TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE -> mJoinTypeView?.content = mJoinTypes[(value as Int)]
            TUIKitConstants.Group.MODIFY_MEMBER_NAME -> {
                toastLongMessage(resources.getString(R.string.modify_nickname_success))
                mNickView?.content = value.toString()
            }
        }
    }

    fun setRouter(listener: IGroupMemberRouter?) {
        mMemberPreviewListener = listener
        mMemberAdapter?.setManagerCallBack(listener)
    }

    override fun setDataSource(dataSource: GroupInfo?) {}

    override fun setParentLayout(parent: Any?) {}
}