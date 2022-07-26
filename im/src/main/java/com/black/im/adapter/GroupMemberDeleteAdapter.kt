package com.black.im.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.model.group.GroupMemberInfo
import com.black.im.util.BackgroundTasks
import com.black.im.util.TUIKit.appContext
import java.util.*

class GroupMemberDeleteAdapter : BaseAdapter() {
    private var mGroupMembers: List<GroupMemberInfo> = ArrayList()
    private val mDelMembers: MutableList<GroupMemberInfo> = ArrayList()
    private var mSelectCallback: OnSelectChangedListener? = null
    override fun getCount(): Int {
        return mGroupMembers.size
    }

    override fun getItem(i: Int): GroupMemberInfo {
        return mGroupMembers[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
        var view1 = view
        val holder: MyViewHolder
        if (view1 == null) {
            view1 = LayoutInflater.from(appContext).inflate(R.layout.group_member_del_adpater, viewGroup, false)
            holder = MyViewHolder()
            holder.memberIcon = view1.findViewById(R.id.group_member_icon)
            holder.memberName = view1.findViewById(R.id.group_member_name)
            holder.delCheck = view1.findViewById(R.id.group_member_del_check)
            view1.tag = holder
        } else {
            holder = view1.tag as MyViewHolder
        }
        val info = getItem(i)
        if (!TextUtils.isEmpty(info.iconUrl)) GlideEngine.loadImage(holder.memberIcon, info.iconUrl, null)
        holder.memberName?.text = info.account
        holder.delCheck?.isChecked = false
        holder.delCheck?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mDelMembers.add(info)
            } else {
                mDelMembers.remove(info)
            }
            if (mSelectCallback != null) {
                mSelectCallback?.onSelectChanged(mDelMembers)
            }
        }
        return view1
    }

    fun setDataSource(members: List<GroupMemberInfo>?) {
        if (members != null) {
            mGroupMembers = members
            BackgroundTasks.instance?.runOnUiThread(Runnable { notifyDataSetChanged() })
        }
    }

    fun setOnSelectChangedListener(callback: OnSelectChangedListener?) {
        mSelectCallback = callback
    }

    fun clear() {
        mDelMembers.clear()
    }

    interface OnSelectChangedListener {
        fun onSelectChanged(mDelMembers: List<GroupMemberInfo>?)
    }

    private inner class MyViewHolder {
        var memberIcon: ImageView? = null
        var memberName: TextView? = null
        var delCheck: CheckBox? = null
    }
}