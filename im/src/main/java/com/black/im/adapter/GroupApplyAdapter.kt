package com.black.im.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.manager.GroupChatManagerKit
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKit.appContext
import com.black.im.util.ToastUtil.toastLongMessage

class GroupApplyAdapter : BaseAdapter() {
    private var mGroupMembers: MutableList<GroupApplyInfo> = ArrayList()
    private var mProvider: GroupInfoProvider? = null
    private var mOnItemClickListener: OnItemClickListener? = null
    override fun getCount(): Int {
        return mGroupMembers.size
    }

    val unHandledSize: Int
        get() {
            var total = 0
            for (i in mGroupMembers) {
                if (i.status == GroupApplyInfo.UNHANDLED) {
                    total++
                }
            }
            return total
        }

    override fun getItem(i: Int): GroupApplyInfo {
        return mGroupMembers[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
        var view1 = view
        val holder: MyViewHolder
        val info = getItem(i)
        if (view1 == null) {
            view1 = LayoutInflater.from(appContext).inflate(R.layout.group_member_apply_adpater, viewGroup, false)
            view1.setOnClickListener {
                if (mOnItemClickListener != null && info.status == GroupApplyInfo.UNHANDLED) {
                    mOnItemClickListener?.onItemClick(info)
                }
            }
            holder = MyViewHolder()
            holder.memberIcon = view1.findViewById(R.id.group_apply_member_icon)
            holder.memberName = view1.findViewById(R.id.group_apply_member_name)
            holder.reason = view1.findViewById(R.id.group_apply_reason)
            holder.accept = view1.findViewById(R.id.group_apply_accept)
            holder.refuse = view1.findViewById(R.id.group_apply_refuse)
            view1.tag = holder
        } else {
            holder = view1.tag as MyViewHolder
        }
        holder.memberName?.text = info.pendencyItem.fromUser
        holder.reason?.text = info.pendencyItem.requestMsg
        when (info.status) {
            GroupApplyInfo.UNHANDLED -> {
                holder.accept?.visibility = View.VISIBLE
                holder.accept?.setText(R.string.accept)
                holder.accept?.background = appContext.resources.getDrawable(R.color.bg_positive_btn)
                holder.accept?.setOnClickListener { acceptApply(i, info) }
                holder.refuse?.visibility = View.VISIBLE
                holder.refuse?.setText(R.string.refuse)
                holder.refuse?.background = appContext.resources.getDrawable(R.color.bg_negative_btn)
                holder.refuse?.setOnClickListener { refuseApply(i, info) }
            }
            GroupApplyInfo.APPLIED -> {
                holder.accept?.visibility = View.VISIBLE
                holder.accept?.isClickable = false
                holder.accept?.setText(R.string.accepted)
                holder.accept?.background = appContext.resources.getDrawable(R.drawable.gray_btn_bg)
                holder.refuse?.visibility = View.GONE
            }
            GroupApplyInfo.REFUSED -> {
                holder.refuse?.visibility = View.VISIBLE
                holder.refuse?.isClickable = false
                holder.refuse?.setText(R.string.refused)
                holder.refuse?.background = appContext.resources.getDrawable(R.drawable.gray_btn_bg)
                holder.accept?.visibility = View.GONE
            }
        }
        return view1
    }

    fun setOnItemClickListener(l: OnItemClickListener?) {
        mOnItemClickListener = l
    }

    fun updateItemData(info: GroupApplyInfo) {
        for (i in mGroupMembers) {
            if (TextUtils.equals(i.pendencyItem.fromUser, info.pendencyItem.fromUser)) {
                i.status = info.status
                notifyDataSetChanged()
                break
            }
        }
    }

    fun setDataSource(info: GroupInfo?) {
        mProvider = GroupChatManagerKit.instance.provider
        mGroupMembers = mProvider?.applyList ?: ArrayList()
    }

    fun acceptApply(position: Int, item: GroupApplyInfo?) {
        mProvider?.acceptApply(item!!, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                notifyDataSetChanged()
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage(errMsg)
            }
        })
    }

    fun refuseApply(position: Int, item: GroupApplyInfo?) {
        mProvider?.refuseApply(item!!, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                notifyDataSetChanged()
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage(errMsg)
            }
        })
    }

    interface OnItemClickListener {
        fun onItemClick(info: GroupApplyInfo?)
    }

    private inner class MyViewHolder {
        var memberIcon: ImageView? = null
        var memberName: TextView? = null
        var reason: TextView? = null
        var accept: Button? = null
        var refuse: Button? = null
    }
}