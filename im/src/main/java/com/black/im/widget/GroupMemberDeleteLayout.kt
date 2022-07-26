package com.black.im.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.ListView
import com.black.im.R
import com.black.im.adapter.GroupMemberDeleteAdapter
import com.black.im.interfaces.IGroupMemberLayout
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.ToastUtil.toastLongMessage

class GroupMemberDeleteLayout : LinearLayout, IGroupMemberLayout {
    override var titleBar: TitleBarLayout? = null
        private set
    private var mMembers: ListView? = null
    private var mAdapter: GroupMemberDeleteAdapter? = null
    private var mDelMembers: List<GroupMemberInfo>? = null
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
        View.inflate(context, R.layout.group_member_del_layout, this)
        titleBar = findViewById(R.id.group_member_title_bar)
        titleBar?.setTitle("移除", ITitleBarLayout.POSITION.RIGHT)
        titleBar?.setTitle("移除成员", ITitleBarLayout.POSITION.MIDDLE)
        titleBar?.rightTitle?.setTextColor(Color.BLUE)
        titleBar?.getRightIcon()?.visibility = View.GONE
        titleBar?.setOnRightClickListener(OnClickListener {
            val provider = GroupInfoProvider()
            provider.loadGroupInfo(mGroupInfo!!)
            provider.removeGroupMembers(mDelMembers, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    toastLongMessage("删除成员成功")
                    post {
                        titleBar?.setTitle("移除", ITitleBarLayout.POSITION.RIGHT)
                        mAdapter?.clear()
                        mAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    toastLongMessage("删除成员失败:$errCode=$errMsg")
                }
            })
        })
        mAdapter = GroupMemberDeleteAdapter()
        mAdapter?.setOnSelectChangedListener(object : GroupMemberDeleteAdapter.OnSelectChangedListener {
            override fun onSelectChanged(mDelMembers: List<GroupMemberInfo>?) {
                this@GroupMemberDeleteLayout.mDelMembers = mDelMembers
                if (this@GroupMemberDeleteLayout.mDelMembers?.size ?: 0 > 0) {
                    titleBar?.setTitle("移除（" + (this@GroupMemberDeleteLayout.mDelMembers?.size.toString() + "）"), ITitleBarLayout.POSITION.RIGHT)
                } else {
                    titleBar?.setTitle("移除", ITitleBarLayout.POSITION.RIGHT)
                }
            }

        })
        mMembers = findViewById(R.id.group_del_members)
        mMembers?.adapter = mAdapter
    }

    override fun setParentLayout(parent: Any?) {}
    override fun setDataSource(dataSource: GroupInfo?) {
        mGroupInfo = dataSource
        mAdapter?.setDataSource(dataSource?.memberDetails)
    }
}