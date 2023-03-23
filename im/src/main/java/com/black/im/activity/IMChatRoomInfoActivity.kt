package com.black.im.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.lib.FryingSingleToast
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.im.BR
import com.black.im.R
import com.black.im.adapter.GroupInfoMemberSimpleAdapter
import com.black.im.databinding.ActivityChatRoomInfoBinding
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.group.TIMGroupSelfInfo

//聊天室信息
@Route(value = [RouterConstData.IM_CHAT_ROOM_INFO])
class IMChatRoomInfoActivity : BaseActionBarActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, OnItemClickListener {
    private var groupId: String? = null
    private var groupName: String? = null

    private var binding: ActivityChatRoomInfoBinding? = null

    private var titleView: TextView? = null
    private var provider: GroupInfoProvider? = null
    private var imageLoader: ImageLoader? = null
    private var adminAdapter: GroupInfoMemberSimpleAdapter? = null
    private var normalAdapter: GroupInfoMemberSimpleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupName = intent.getStringExtra(ConstData.IM_GROUP_NAME)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)
        provider = GroupInfoProvider()
        imageLoader = ImageLoader(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room_info)

        binding?.groupLayout?.setOnClickListener(this)
        binding?.groupName?.text = if (groupName == null) "" else groupName
        binding?.groupNoticeLayout?.setOnClickListener(this)

        val dm = resources.displayMetrics
        val decoration = SpacesItemDecoration((dm.density * 10).toInt())
        binding?.groupAdminLayout?.setOnClickListener(this)
        binding?.groupAdminLayout?.isEnabled = false
        val groupAdminLayoutManager = GridLayoutManager(this, 5)
        groupAdminLayoutManager.orientation = RecyclerView.VERTICAL
        groupAdminLayoutManager.isSmoothScrollbarEnabled = true
        binding?.adminRecyclerView?.layoutManager = groupAdminLayoutManager
        binding?.adminRecyclerView?.addItemDecoration(decoration)
        adminAdapter = GroupInfoMemberSimpleAdapter(this, BR.listItemGroupInfoMemberSimpleModel, null)
        adminAdapter?.setOnItemClickListener(this)
        binding?.adminRecyclerView?.adapter = adminAdapter
        binding?.adminRecyclerView?.isNestedScrollingEnabled = false
        binding?.adminRecyclerView?.setHasFixedSize(true)
        binding?.adminRecyclerView?.isFocusable = false

        binding?.groupMemberLayout?.setOnClickListener(this)
        binding?.groupMemberLayout?.isEnabled = false
        val groupMemberLayoutManager = GridLayoutManager(this, 5)
        groupMemberLayoutManager.orientation = RecyclerView.VERTICAL
        groupMemberLayoutManager.isSmoothScrollbarEnabled = true
        binding?.memberRecyclerView?.layoutManager = groupMemberLayoutManager
        binding?.memberRecyclerView?.addItemDecoration(decoration)
        normalAdapter = GroupInfoMemberSimpleAdapter(this, BR.listItemGroupInfoMemberSimpleModel, null)
        normalAdapter?.setOnItemClickListener(this)
        binding?.memberRecyclerView?.adapter = normalAdapter
        binding?.memberRecyclerView?.isNestedScrollingEnabled = false
        binding?.memberRecyclerView?.setHasFixedSize(true)
        binding?.memberRecyclerView?.isFocusable = false

        binding?.groupNickNameLayout?.setOnClickListener(this)
        binding?.groupNickNameLayout?.isEnabled = false
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.btnExit?.setOnClickListener(this)
        binding?.btnExit?.visibility = View.GONE
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
        titleView = view.findViewById(R.id.action_bar_title)
        titleView?.text = "群资料"
    }

    override fun onResume() {
        super.onResume()
        loadGroupInfo()
        selfInfo
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.group_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.IM_GROUP_ID, groupId)
                BlackRouter.getInstance().build(RouterConstData.IM_MODIFY_GROUP).with(bundle).go(this)
            }
            R.id.group_notice_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.IM_GROUP_ID, groupId)
                BlackRouter.getInstance().build(RouterConstData.IM_MODIFY_GROUP_NOTICE).with(bundle).go(this)
            }
            R.id.group_admin_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.IM_GROUP_ID, groupId)
                bundle.putString(ConstData.TITLE, "群管理员")
                val memberList = adminAdapter?.data
                bundle.putString(ConstData.IM_GROUP_MEMBER_LIST, if (memberList == null) null else gson.toJson(memberList))
                BlackRouter.getInstance().build(RouterConstData.IM_CHAT_ROOM_MEMBER_LIST).with(bundle).go(this)
            }
            R.id.group_member_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.IM_GROUP_ID, groupId)
                bundle.putString(ConstData.TITLE, "群成员")
                val memberList = normalAdapter?.data
                bundle.putString(ConstData.IM_GROUP_MEMBER_LIST, if (memberList == null) null else gson.toJson(memberList))
                BlackRouter.getInstance().build(RouterConstData.IM_CHAT_ROOM_MEMBER_LIST).with(bundle).go(this)
            }
            R.id.group_nick_name_layout -> {
                val bundle = Bundle()
                bundle.putString(ConstData.IM_GROUP_ID, groupId)
                BlackRouter.getInstance().build(RouterConstData.IM_MODIFY_NAME_CARD).with(bundle).go(this)
            }
            R.id.btn_exit -> {
                ConfirmDialog(this, "提示", "确定退出群聊吗？", object : OnConfirmCallback {
                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                        confirmDialog.dismiss()
                        TIMGroupManager.getInstance().quitGroup(groupId!!, object : TIMCallBack {
                            override fun onError(i: Int, s: String) {
                                FryingUtil.showToast(mContext, s, FryingSingleToast.ERROR)
                            }

                            override fun onSuccess() {
                                binding?.groupNickNameLayout?.isEnabled = false
                                binding?.btnExit?.visibility = View.GONE
                                selfInfo
                            }
                        })
                    }

                }).show()
            }
            //            TIMGrouManager.ModifyGroupInfoParam infoParam = new TIMGroupManager.ModifyGroupInfoParam(groupId);
//            //修改群公告
//            infoParam.setNotification("这里是群公告");
//            //修改群简介
//            infoParam.setIntroduction("这里是群简介");
//            TIMGroupManager.getInstance().modifyGroupInfo(infoParam, new TIMCallBack() {
//                @Override
//                public void onError(int i, String s) {
//
//                }
//
//                @Override
//                public void onSuccess() {
//                    FryingUtil.showToast(mContext, "修改成功");
//                }
//            });
        }
        //            TIMGrouManager.ModifyGroupInfoParam infoParam = new TIMGroupManager.ModifyGroupInfoParam(groupId);
//            //修改群公告
//            infoParam.setNotification("这里是群公告");
//            //修改群简介
//            infoParam.setIntroduction("这里是群简介");
//            TIMGroupManager.getInstance().modifyGroupInfo(infoParam, new TIMCallBack() {
//                @Override
//                public void onError(int i, String s) {
//
//                }
//
//                @Override
//                public void onSuccess() {
//                    FryingUtil.showToast(mContext, "修改成功");
//                }
//            });
    }

    override fun onRefresh() {
        loadGroupInfo()
        selfInfo
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val id = recyclerView?.id
        if (id == R.id.admin_recycler_view) {
            val groupMemberInfo = adminAdapter?.getItem(position)
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, groupId)
            bundle.putString(ConstData.IM_USER_ID, groupMemberInfo?.account)
            BlackRouter.getInstance().build(RouterConstData.IM_GROUP_MEMBER).with(bundle).go(this)
        } else if (id == R.id.member_recycler_view) {
            val groupMemberInfo = normalAdapter?.getItem(position)
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, groupId)
            bundle.putString(ConstData.IM_USER_ID, groupMemberInfo?.account)
            BlackRouter.getInstance().build(RouterConstData.IM_GROUP_MEMBER).with(bundle).go(this)
        }
    }

    private fun loadGroupInfo() {
        provider?.loadGroupInfo(groupId, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (data is GroupInfo) {
                    showGroupInfo(data)
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                binding?.refreshLayout?.setRefreshing(false)
            }
        })
    }

    private val selfInfo: Unit
        get() {
            TIMGroupManager.getInstance().getSelfInfo(groupId!!, object : TIMValueCallBack<TIMGroupSelfInfo?> {
                override fun onError(i: Int, s: String) {
                    showSelfInfo(null)
                }

                override fun onSuccess(timGroupSelfInfo: TIMGroupSelfInfo?) {
                    showSelfInfo(timGroupSelfInfo)
                }
            })
        }

    private fun showSelfInfo(selfInfo: TIMGroupSelfInfo?) {
        if (selfInfo == null) {
            binding?.groupNickName?.text = "未加入"
            binding?.groupNickNameLayout?.isEnabled = false
            binding?.btnExit?.visibility = View.GONE
        } else {
            binding?.groupNickName?.text = if (TextUtils.isEmpty(selfInfo.nameCard)) getString(R.string.not_filled_in) else selfInfo.nameCard
            binding?.groupNickNameLayout?.isEnabled = true
            if (selfInfo.role == TIMGroupMemberRoleType.ROLE_TYPE_ADMIN || selfInfo.role == TIMGroupMemberRoleType.ROLE_TYPE_NORMAL) {
                binding?.btnExit?.visibility = View.VISIBLE
            } else {
                binding?.btnExit?.visibility = View.GONE
            }
        }
    }

    private fun showGroupInfo(groupInfo: GroupInfo) {
        binding?.groupName?.text = if (groupInfo.groupName == null) "" else groupInfo.groupName
        binding?.groupIntroduction?.text = if (TextUtils.isEmpty(groupInfo.introduction)) getString(R.string.not_filled_in) else groupInfo.introduction
        binding?.groupNotice?.text = if (TextUtils.isEmpty(groupInfo.notice)) getString(R.string.not_filled_in) else groupInfo.notice
        val ownerInfo = groupInfo.ownerInfo
        val ownerTIMInfo = ownerInfo?.detail
        if (ownerTIMInfo != null) {
            val nameDefault = "FBSexer"
            val profile = TIMFriendshipManager.getInstance().queryUserProfile(ownerTIMInfo.user)
            if (profile == null) {
                binding?.ownerName?.text = nameDefault
            } else {
                if (TextUtils.isEmpty(ownerTIMInfo.nameCard)) {
                    binding?.ownerName?.text = if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(nameDefault)) nameDefault else ownerTIMInfo.user
                } else {
                    binding?.ownerName?.text = ownerTIMInfo.nameCard
                }
                if (!TextUtils.isEmpty(profile.faceUrl) && binding?.ownerAvatar != null) {
                    imageLoader?.loadImage(binding?.ownerAvatar!!, profile.faceUrl)
                }
            }
        }
        val adminMembers: MutableList<GroupMemberInfo>? = groupInfo.adminMemberDetails
        val adminCount = adminMembers?.size ?: 0
        binding?.groupAdminCount?.text = String.format("共%s人", NumberUtil.formatNumberNoGroup(adminCount))
        binding?.groupAdminLayout?.isEnabled = adminCount > 0
        if (adminMembers != null && adminMembers.size > 10) {
            adminAdapter?.data = adminMembers.subList(0, 10)
        } else {
            adminAdapter?.data = adminMembers
        }
        adminAdapter?.notifyDataSetChanged()
        val normalMembers: MutableList<GroupMemberInfo>? = groupInfo.normalMemberDetails
        val normalCount = normalMembers?.size ?: 0
        binding?.groupMemberCount?.text = String.format("共%s人", NumberUtil.formatNumberNoGroup(normalCount))
        binding?.groupMemberLayout?.isEnabled = normalCount > 0
        if (normalMembers != null && normalMembers.size > 10) {
            normalAdapter?.data = normalMembers.subList(0, 10)
        } else {
            normalAdapter?.data = normalMembers
        }
        normalAdapter?.notifyDataSetChanged()
    }
}