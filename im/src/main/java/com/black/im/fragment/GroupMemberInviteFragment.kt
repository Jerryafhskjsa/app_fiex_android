package com.black.im.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.im.R
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants
import com.black.im.widget.GroupMemberInviteLayout

class GroupMemberInviteFragment : BaseFragment() {
    private var mInviteLayout: GroupMemberInviteLayout? = null
    private var mBaseView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBaseView = inflater.inflate(R.layout.group_fragment_invite_members, container, false)
        mInviteLayout = mBaseView?.findViewById(R.id.group_member_invite_layout)
        mInviteLayout?.setParentLayout(this)
        init()
        return mBaseView
    }

    private fun init() {
        mInviteLayout?.setDataSource(arguments.getSerializable(TUIKitConstants.Group.GROUP_INFO) as GroupInfo)
        mInviteLayout?.titleBar?.setOnLeftClickListener(View.OnClickListener { backward() })
    }
}