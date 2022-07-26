package com.black.im.widget

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import com.black.im.R
import com.black.im.adapter.GroupMemberManagerAdapter
import com.black.im.interfaces.IGroupMemberChangedCallback
import com.black.im.interfaces.IGroupMemberLayout
import com.black.im.interfaces.IGroupMemberRouter
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.util.PopWindowUtil.buildFullScreenDialog

class GroupMemberManagerLayout : LinearLayout, IGroupMemberLayout {
    override var titleBar: TitleBarLayout? = null
        private set
    private var mDialog: AlertDialog? = null
    private var mAdapter: GroupMemberManagerAdapter? = null
    private var mGroupMemberManager: IGroupMemberRouter? = null
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
        View.inflate(context, R.layout.group_member_layout, this)
        titleBar = findViewById(R.id.group_member_title_bar)
        titleBar?.setTitle("管理", ITitleBarLayout.POSITION.RIGHT)
        titleBar?.getRightIcon()?.visibility = View.GONE
        titleBar?.setOnRightClickListener(OnClickListener { buildPopMenu() })
        mAdapter = GroupMemberManagerAdapter()
        mAdapter?.setMemberChangedCallback(object : IGroupMemberChangedCallback {
            override fun onMemberRemoved(memberInfo: GroupMemberInfo?) {
                titleBar?.setTitle("群成员(" + mGroupInfo?.memberDetails?.size + ")", ITitleBarLayout.POSITION.MIDDLE)
            }
        })
        val gridView = findViewById<GridView>(R.id.group_all_members)
        gridView.adapter = mAdapter
    }

    override fun setParentLayout(parent: Any?) {}
    override fun setDataSource(dataSource: GroupInfo?) {
        mGroupInfo = dataSource
        mAdapter?.setDataSource(dataSource)
        if (dataSource != null) {
            titleBar?.setTitle("群成员(" + dataSource.memberDetails?.size + ")", ITitleBarLayout.POSITION.MIDDLE)
        }
    }

    private fun buildPopMenu() {
        if (mGroupInfo == null) {
            return
        }
        if (mDialog == null) {
            mDialog = buildFullScreenDialog((context as Activity))
            val moreActionView = View.inflate(context, R.layout.group_member_pop_menu, null)
            moreActionView.setOnClickListener { mDialog?.dismiss() }
            val addBtn = moreActionView.findViewById<Button>(R.id.add_group_member)
            addBtn.setOnClickListener {
                if (mGroupMemberManager != null) {
                    mGroupMemberManager?.forwardAddMember(mGroupInfo)
                }
                mDialog?.dismiss()
            }
            val deleteBtn = moreActionView.findViewById<Button>(R.id.remove_group_member)
            if (true != mGroupInfo?.isOwner()) {
                deleteBtn.visibility = View.GONE
            }
            deleteBtn.setOnClickListener {
                if (mGroupMemberManager != null) {
                    mGroupMemberManager?.forwardDeleteMember(mGroupInfo)
                }
                mDialog?.dismiss()
            }
            val cancelBtn = moreActionView.findViewById<Button>(R.id.cancel)
            cancelBtn.setOnClickListener { mDialog?.dismiss() }
            mDialog?.setContentView(moreActionView)
        } else {
            mDialog?.show()
        }
    }

    fun setRouter(callBack: IGroupMemberRouter?) {
        mGroupMemberManager = callBack
    }
}