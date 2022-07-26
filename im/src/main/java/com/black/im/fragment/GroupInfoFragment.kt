package com.black.im.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.im.R
import com.black.im.interfaces.IGroupMemberRouter
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants
import com.black.im.widget.GroupInfoLayout

class GroupInfoFragment : BaseFragment() {
    private var mBaseView: View? = null
    private var mGroupInfoLayout: GroupInfoLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBaseView = inflater.inflate(R.layout.group_info_fragment, container, false)
        initView()
        return mBaseView
    }

    private fun initView() {
        mGroupInfoLayout = mBaseView!!.findViewById(R.id.group_info_layout)
        mGroupInfoLayout?.setGroupId(arguments.getString(TUIKitConstants.Group.GROUP_ID))
        mGroupInfoLayout?.setRouter(object : IGroupMemberRouter {
            override fun forwardListMember(info: GroupInfo?) {
                val fragment = GroupMemberManagerFragment()
                val bundle = Bundle()
                bundle.putSerializable(TUIKitConstants.Group.GROUP_INFO, info)
                fragment.arguments = bundle
                forward(fragment, false)
            }

            override fun forwardAddMember(info: GroupInfo?) {
                val fragment = GroupMemberInviteFragment()
                val bundle = Bundle()
                bundle.putSerializable(TUIKitConstants.Group.GROUP_INFO, info)
                fragment.arguments = bundle
                forward(fragment, false)
            }

            override fun forwardDeleteMember(info: GroupInfo?) {
                val fragment = GroupMemberDeleteFragment()
                val bundle = Bundle()
                bundle.putSerializable(TUIKitConstants.Group.GROUP_INFO, info)
                fragment.arguments = bundle
                forward(fragment, false)
            }
        })
    }
}