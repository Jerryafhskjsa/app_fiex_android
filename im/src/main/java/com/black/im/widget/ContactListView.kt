package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.black.im.R
import com.black.im.adapter.ContactAdapter
import com.black.im.indexlib.IndexBar.widget.IndexBar
import com.black.im.indexlib.suspension.SuspensionDecoration
import com.black.im.manager.CustomLinearLayoutManager
import com.black.im.model.ContactItemBean
import com.black.im.model.group.GroupInfo
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastShortMessage
import com.tencent.imsdk.TIMFriendshipManager
import com.tencent.imsdk.TIMGroupManager
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo
import com.tencent.imsdk.friendship.TIMFriend

class ContactListView : LinearLayout {
    companion object {
        private val TAG = ContactListView::class.java.simpleName
        private const val INDEX_STRING_TOP = "↑"
    }

    private var mRv: RecyclerView? = null
    var adapter: ContactAdapter? = null
        private set
    private var mManager: CustomLinearLayoutManager? = null
    private var mData: MutableList<ContactItemBean> = ArrayList()
    private var mDecoration: SuspensionDecoration? = null
    private var mContactCountTv: TextView? = null
    private var mGroupInfo: GroupInfo? = null
    /**
     * 右侧边栏导航区域
     */
    private var mIndexBar: IndexBar? = null
    /**
     * 显示指示器DialogText
     */
    private var mTvSideBarHint: TextView? = null

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
        View.inflate(context, R.layout.contact_list, this)
        mRv = findViewById(R.id.contact_member_list)
        mManager = CustomLinearLayoutManager(context)
        mRv?.layoutManager = mManager
        adapter = ContactAdapter(mData)
        mRv?.adapter = adapter
        mRv?.addItemDecoration(SuspensionDecoration(context, mData).also { mDecoration = it })
        //如果add两个，那么按照先后顺序，依次渲染。
//使用indexBar
        mTvSideBarHint = findViewById(R.id.contact_tvSideBarHint)
        mIndexBar = findViewById(R.id.contact_indexBar)
        //indexbar初始化
        mIndexBar?.setPressedShowTextView(mTvSideBarHint)?.setNeedRealIndex(true)?.setLayoutManager(mManager)
        mContactCountTv = findViewById(R.id.contact_count)
        mContactCountTv?.text = String.format(resources.getString(R.string.contact_count), 0)
    }

    fun setDataSource(data: MutableList<ContactItemBean>) {
        mData = data
        adapter?.setDataSource(mData)
        mIndexBar?.setSourceDatas(mData)?.invalidate()
        mDecoration?.setDatas(mData)
        mContactCountTv?.text = String.format(resources.getString(R.string.contact_count), mData.size)
        // 根据内容动态设置右侧导航栏的高度
        val params = mIndexBar?.layoutParams
        if (mData.size * 50 < mIndexBar?.measuredHeight ?: 0) {
            // 若动态设置的侧边栏高度大于之前的旧值，则不改变侧边栏高度
            params?.height = mData.size * 50
        }
        mIndexBar?.layoutParams = params
    }

    fun setSingleSelectMode(mode: Boolean) {
        adapter?.setSingleSelectMode(mode)
    }

    fun setOnSelectChangeListener(selectChangeListener: OnSelectChangedListener?) {
        adapter?.setOnSelectChangedListener(selectChangeListener)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        adapter?.setOnItemClickListener(listener)
    }

    fun loadDataSource(dataSource: Int) {
        when (dataSource) {
            DataSource.FRIEND_LIST -> loadFriendListData(false)
            DataSource.BLACK_LIST -> loadBlackListData()
            DataSource.GROUP_LIST -> loadGroupListData()
            DataSource.CONTACT_LIST -> loadFriendListData(true)
        }
    }

    fun setGroupInfo(groupInfo: GroupInfo?) {
        mGroupInfo = groupInfo
    }

    private fun updateStatus(beanList: List<ContactItemBean?>) {
        if (mGroupInfo == null) {
            return
        }
        val list = mGroupInfo?.memberDetails ?: ArrayList()
        var needFresh = false
        if (list.size > 0) {
            for (info in list) {
                for (bean in beanList) {
                    if (info.account == bean?.id) {
                        bean?.isSelected = true
                        bean?.isEnable = false
                        needFresh = true
                    }
                }
            }
        }
        if (needFresh) {
            adapter?.notifyDataSetChanged()
        }
    }

    private fun loadFriendListData(loopMore: Boolean) {
        TIMFriendshipManager.getInstance().getFriendList(object : TIMValueCallBack<List<TIMFriend?>> {
            override fun onError(code: Int, desc: String) {
                TUIKitLog.e(TAG, "getFriendList err code = $code")
            }

            override fun onSuccess(timFriends: List<TIMFriend?>) {
                TUIKitLog.i(TAG, "getFriendList success result = " + timFriends.size)
                if (timFriends.isEmpty()) {
                    TUIKitLog.i(TAG, "getFriendList success but no data")
                }
                mData.clear()
                if (loopMore) {
                    mData.add(ContactItemBean(resources.getString(R.string.new_friend))
                            .setTop(true).setBaseIndexTag(ContactItemBean.INDEX_STRING_TOP) as ContactItemBean)
                    mData.add(ContactItemBean(resources.getString(R.string.group)).setTop(true).setBaseIndexTag(ContactItemBean.INDEX_STRING_TOP) as ContactItemBean)
                    mData.add(ContactItemBean(resources.getString(R.string.blacklist)).setTop(true).setBaseIndexTag(ContactItemBean.INDEX_STRING_TOP) as ContactItemBean)
                }
                for (timFriend in timFriends) {
                    val info = ContactItemBean()
                    info.covertTIMFriend(timFriend)
                    mData.add(info)
                }
                updateStatus(mData)
                setDataSource(mData)
            }
        })
    }

    private fun loadBlackListData() {
        TIMFriendshipManager.getInstance().getBlackList(object : TIMValueCallBack<List<TIMFriend?>> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "getBlackList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(timFriends: List<TIMFriend?>) {
                TUIKitLog.i(TAG, "getFriendGroups success")
                if (timFriends.isEmpty()) {
                    TUIKitLog.i(TAG, "getFriendGroups success but no data")
                }
                mData.clear()
                for (timFriend in timFriends) {
                    val info = ContactItemBean()
                    info.covertTIMFriend(timFriend).isBlackList = true
                    mData.add(info)
                }
                setDataSource(mData)
            }
        })
    }

    private fun loadGroupListData() {
        TIMGroupManager.getInstance().getGroupList(object : TIMValueCallBack<List<TIMGroupBaseInfo?>> {
            override fun onError(i: Int, s: String) {
                TUIKitLog.e(TAG, "getGroupList err code = $i, desc = $s")
                toastShortMessage("Error code = $i, desc = $s")
            }

            override fun onSuccess(infos: List<TIMGroupBaseInfo?>) {
                TUIKitLog.i(TAG, "getFriendGroups success")
                if (infos.isEmpty()) {
                    TUIKitLog.i(TAG, "getFriendGroups success but no data")
                }
                mData.clear()
                for (info in infos) {
                    val bean = ContactItemBean()
                    mData.add(bean.covertTIMGroupBaseInfo(info))
                }
                setDataSource(mData)
            }
        })
    }

    val groupData: List<ContactItemBean?>
        get() = mData

    interface OnSelectChangedListener {
        fun onSelectChanged(contact: ContactItemBean?, selected: Boolean)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, contact: ContactItemBean?)
    }

    object DataSource {
        const val UNKNOWN = -1
        const val FRIEND_LIST = 1
        const val BLACK_LIST = 2
        const val GROUP_LIST = 3
        const val CONTACT_LIST = 4
    }
}