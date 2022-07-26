package com.black.im.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.interfaces.IGroupMemberChangedCallback
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.BackgroundTasks
import com.black.im.util.IUIKitCallBack
import com.black.im.util.PopWindowUtil.popupWindow
import com.black.im.util.ScreenUtil.getPxByDp
import com.black.im.util.TUIKit.appContext
import com.black.im.util.ToastUtil.toastLongMessage
import java.util.*

class GroupMemberManagerAdapter : BaseAdapter() {
    private var mCallback: IGroupMemberChangedCallback? = null
    private var mGroupInfo: GroupInfo? = null
    private var mGroupMembers: MutableList<GroupMemberInfo>? = ArrayList()
    fun setMemberChangedCallback(callback: IGroupMemberChangedCallback?) {
        mCallback = callback
    }

    override fun getCount(): Int {
        return mGroupMembers?.size ?: 0
    }

    override fun getItem(i: Int): GroupMemberInfo {
        return mGroupMembers!![i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
        var view1 = view
        val holder: MyViewHolder
        if (view1 == null) {
            view1 = LayoutInflater.from(appContext).inflate(R.layout.group_member_adpater, viewGroup, false)
            holder = MyViewHolder()
            holder.memberIcon = view1.findViewById(R.id.group_member_icon)
            holder.memberName = view1.findViewById(R.id.group_member_name)
            view1.tag = holder
        } else {
            holder = view1.tag as MyViewHolder
        }
        val info = getItem(i)
        if (!TextUtils.isEmpty(info.iconUrl)) GlideEngine.loadImage(holder.memberIcon, info.iconUrl, null)
        holder.memberName?.text = info.account
        view1?.setOnLongClickListener(OnLongClickListener { v ->
            if (true != mGroupInfo?.isOwner()) return@OnLongClickListener false
            val delete = TextView(viewGroup.context)
            delete.setText(R.string.group_remove_member)
            val padding = getPxByDp(10)
            delete.setPadding(padding, padding, padding, padding)
            delete.setBackgroundResource(R.drawable.text_border)
            val location = IntArray(2)
            v.getLocationInWindow(location)
            val window = popupWindow(delete, viewGroup, location[0], location[1] + v.measuredHeight / 3)
            delete.setOnClickListener {
                val dels: MutableList<GroupMemberInfo> = ArrayList()
                dels.add(info)
                val provider = GroupInfoProvider()
                provider.loadGroupInfo(mGroupInfo!!)
                provider.removeGroupMembers(dels, object : IUIKitCallBack {
                    override fun onSuccess(data: Any?) {
                        mGroupMembers?.remove(info)
                        notifyDataSetChanged()
                        if (mCallback != null) {
                            mCallback?.onMemberRemoved(info)
                        }
                    }

                    override fun onError(module: String?, errCode: Int, errMsg: String?) {
                        toastLongMessage("移除成员失败:errCode=$errCode")
                    }
                })
                window.dismiss()
            }
            false
        })
        return view1
    }

    fun setDataSource(groupInfo: GroupInfo?) {
        if (groupInfo != null) {
            mGroupInfo = groupInfo
            mGroupMembers = groupInfo.memberDetails
            BackgroundTasks.instance?.runOnUiThread(Runnable { notifyDataSetChanged() })
        }
    }

    private inner class MyViewHolder {
        var memberIcon: ImageView? = null
        var memberName: TextView? = null
    }
}