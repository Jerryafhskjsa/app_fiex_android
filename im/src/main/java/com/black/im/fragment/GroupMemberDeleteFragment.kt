package com.black.im.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.im.R
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants
import com.black.im.widget.GroupMemberDeleteLayout

class GroupMemberDeleteFragment : BaseFragment() {
    private var mMemberDelLayout: GroupMemberDeleteLayout? = null
    private var mBaseView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBaseView = inflater.inflate(R.layout.group_fragment_del_members, container, false)
        mMemberDelLayout = mBaseView?.findViewById(R.id.group_member_del_layout)
        init()
        return mBaseView
    }

    private fun init() {
        mMemberDelLayout?.setDataSource(arguments.getSerializable(TUIKitConstants.Group.GROUP_INFO) as GroupInfo)
        mMemberDelLayout?.titleBar?.setOnLeftClickListener(View.OnClickListener { backward() })
    }
}