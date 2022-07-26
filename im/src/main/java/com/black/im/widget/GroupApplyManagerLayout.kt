package com.black.im.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.ListView
import com.black.im.R
import com.black.im.activity.GroupApplyMemberActivity
import com.black.im.adapter.GroupApplyAdapter
import com.black.im.interfaces.IGroupMemberLayout
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.manager.GroupChatManagerKit
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitConstants

class GroupApplyManagerLayout : LinearLayout, IGroupMemberLayout {
    override var titleBar: TitleBarLayout? = null
        private set
    private var mApplyMemberList: ListView? = null
    private var mAdapter: GroupApplyAdapter? = null

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
        View.inflate(context, R.layout.group_apply_manager_layout, this)
        mApplyMemberList = findViewById(R.id.group_apply_members)
        mAdapter = GroupApplyAdapter()
        mAdapter?.setOnItemClickListener(object : GroupApplyAdapter.OnItemClickListener {
            override fun onItemClick(info: GroupApplyInfo?) {
                val intent = Intent(context, GroupApplyMemberActivity::class.java)
                intent.putExtra(TUIKitConstants.ProfileType.CONTENT, info)
                (context as Activity).startActivityForResult(intent, TUIKitConstants.ActivityRequest.CODE_1)
            }

        })
        mApplyMemberList?.adapter = mAdapter
        titleBar = findViewById(R.id.group_apply_title_bar)
        titleBar?.rightGroup?.visibility = View.GONE
        titleBar?.setTitle(resources.getString(R.string.group_apply_members), ITitleBarLayout.POSITION.MIDDLE)
        titleBar?.setOnLeftClickListener(OnClickListener {
            mAdapter?.let {
                GroupChatManagerKit.instance.onApplied(it.unHandledSize)
            }
            if (context is Activity) {
                (context as Activity).finish()
            }
        })
    }

    override fun setParentLayout(parent: Any?) {}
    override fun setDataSource(dataSource: GroupInfo?) {
        mAdapter?.setDataSource(dataSource)
        mAdapter?.notifyDataSetChanged()
    }

    fun updateItemData(info: GroupApplyInfo?) {
        if (info == null) {
            return
        }
        mAdapter?.updateItemData(info)
    }
}