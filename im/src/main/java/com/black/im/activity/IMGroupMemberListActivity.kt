package com.black.im.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.util.SpacesItemDecoration
import com.black.im.BR
import com.black.im.R
import com.black.im.adapter.GroupInfoMemberSimpleAdapter
import com.black.im.databinding.ActivityChatRoomMemberListBinding
import com.black.im.model.group.GroupMemberInfo
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.google.gson.reflect.TypeToken
import java.util.*

//聊天室群成员
@Route(value = [RouterConstData.IM_CHAT_ROOM_MEMBER_LIST])
class IMGroupMemberListActivity : BaseActionBarActivity(), OnItemClickListener {
    private var groupId: String? = null
    private var groupName: String? = null
    private var adapter: GroupInfoMemberSimpleAdapter? = null

    private var binding: ActivityChatRoomMemberListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupName = intent.getStringExtra(ConstData.IM_GROUP_NAME)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_room_member_list)
        val dm = resources.displayMetrics
        val decoration = SpacesItemDecoration((dm.density * 10).toInt())
        val adminLayoutManager = GridLayoutManager(this, 4)
        adminLayoutManager.orientation = RecyclerView.VERTICAL
        adminLayoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = adminLayoutManager
        binding?.recyclerView?.addItemDecoration(decoration)
        val memberListString = intent.getStringExtra(ConstData.IM_GROUP_MEMBER_LIST)
        val memberList = if (TextUtils.isEmpty(memberListString)) null else gson.fromJson<ArrayList<GroupMemberInfo>>(memberListString, object : TypeToken<ArrayList<GroupMemberInfo?>?>() {}.type)
        adapter = GroupInfoMemberSimpleAdapter(this, BR.listItemGroupInfoMemberSimpleModel, memberList)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        val title = intent.getStringExtra(ConstData.TITLE)
        val titleView = view.findViewById<TextView>(R.id.action_bar_title)
        titleView.text = if (TextUtils.isEmpty(title)) "群成员" else title
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val groupMemberInfo = adapter?.getItem(position)
        val bundle = Bundle()
        bundle.putString(ConstData.IM_GROUP_ID, groupId)
        bundle.putString(ConstData.IM_USER_ID, groupMemberInfo?.account)
        BlackRouter.getInstance().build(RouterConstData.IM_GROUP_MEMBER).with(bundle).go(this)
    }
}