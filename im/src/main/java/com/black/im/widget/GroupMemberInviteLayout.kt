package com.black.im.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.black.im.R
import com.black.im.fragment.BaseFragment
import com.black.im.interfaces.IGroupMemberLayout
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.model.ContactItemBean
import com.black.im.model.group.GroupInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.ToastUtil.toastLongMessage
import java.util.*

class GroupMemberInviteLayout : LinearLayout, IGroupMemberLayout {
    companion object {
        private val TAG = GroupMemberInviteLayout::class.java.simpleName
    }

    override var titleBar: TitleBarLayout? = null
        private set
    private var mContactListView: ContactListView? = null
    private val mInviteMembers: MutableList<String?> = ArrayList()
    private var mParentLayout: Any? = null
    private var mGroupInfo: GroupInfo? = null

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
        View.inflate(context, R.layout.group_member_invite_layout, this)
        titleBar = findViewById(R.id.group_invite_title_bar)
        titleBar?.setTitle("确定", ITitleBarLayout.POSITION.RIGHT)
        titleBar?.setTitle("添加成员", ITitleBarLayout.POSITION.MIDDLE)
        titleBar?.rightTitle?.setTextColor(Color.BLUE)
        titleBar?.getRightIcon()?.visibility = View.GONE
        titleBar?.setOnRightClickListener(OnClickListener {
            val provider = GroupInfoProvider()
            provider.loadGroupInfo(mGroupInfo!!)
            provider.inviteGroupMembers(mInviteMembers, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    if (data is String) {
                        toastLongMessage(data.toString())
                    } else {
                        toastLongMessage("邀请成员成功")
                    }
                    mInviteMembers.clear()
                    finish()
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage("邀请成员失败:$errCode=$errMsg")
                }
            })
        })
        mContactListView = findViewById(R.id.group_invite_member_list)
        mContactListView?.loadDataSource(ContactListView.DataSource.FRIEND_LIST)
        mContactListView?.setOnSelectChangeListener(object : ContactListView.OnSelectChangedListener {
            override fun onSelectChanged(contact: ContactItemBean?, selected: Boolean) {
                if (selected) {
                    mInviteMembers.add(contact?.id)
                } else {
                    mInviteMembers.remove(contact?.id)
                }
                if (mInviteMembers.size > 0) {
                    titleBar?.setTitle("确定（" + mInviteMembers.size + "）", ITitleBarLayout.POSITION.RIGHT)
                } else {
                    titleBar?.setTitle("确定", ITitleBarLayout.POSITION.RIGHT)
                }
            }
        })
    }

    override fun setDataSource(dataSource: GroupInfo?) {
        mGroupInfo = dataSource
        if (mContactListView != null) {
            mContactListView?.setGroupInfo(mGroupInfo)
        }
    }

    override fun setParentLayout(parent: Any?) {
        mParentLayout = parent
    }

    private fun finish() {
        if (mParentLayout is Activity) {
            (mParentLayout as Activity).finish()
        } else if (mParentLayout is BaseFragment) {
            (mParentLayout as BaseFragment).backward()
        }
    }
}