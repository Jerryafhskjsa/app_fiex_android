package com.black.im.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.im.R
import com.black.im.interfaces.IGroupMemberRouter
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants
import com.black.im.widget.GroupMemberManagerLayout

/**
 * 群成员管理
 */
class GroupMemberManagerFragment : BaseFragment() {
    private var mMemberLayout: GroupMemberManagerLayout? = null
    private var mBaseView: View? = null
    private var mGroupInfo: GroupInfo? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBaseView = inflater.inflate(R.layout.group_fragment_members, container, false)
        mMemberLayout = mBaseView?.findViewById(R.id.group_member_grid_layout)
        init()
        return mBaseView
    }

    private fun init() {
        mGroupInfo = arguments.getSerializable(TUIKitConstants.Group.GROUP_INFO) as GroupInfo
        mMemberLayout?.setDataSource(mGroupInfo)
        mMemberLayout?.titleBar?.setOnLeftClickListener(View.OnClickListener { backward() })
        mMemberLayout?.setRouter(object : IGroupMemberRouter {
            override fun forwardListMember(info: GroupInfo?) {}
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