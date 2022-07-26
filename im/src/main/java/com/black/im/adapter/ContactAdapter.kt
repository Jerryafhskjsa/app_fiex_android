package com.black.im.adapter

import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.model.ContactItemBean
import com.black.im.util.TUIKit.appContext
import com.black.im.util.ToastUtil.toastShortMessage
import com.black.im.widget.ContactListView
import com.tencent.imsdk.TIMFriendshipManager
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.friendship.TIMFriendPendencyRequest
import com.tencent.imsdk.friendship.TIMFriendPendencyResponse
import com.tencent.imsdk.friendship.TIMPendencyType

class ContactAdapter(protected var mData: List<ContactItemBean>?) : RecyclerView.Adapter<ContactAdapter.ViewHolder?>() {
    protected var mInflater: LayoutInflater = LayoutInflater.from(appContext)
    private var mOnSelectChangedListener: ContactListView.OnSelectChangedListener? = null
    private var mOnClickListener: ContactListView.OnItemClickListener? = null
    private var mPreSelectedPosition = 0
    private var isSingleSelectMode = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(mInflater.inflate(R.layout.contact_selecable_adapter_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contactBean = getItem(position)
        if (!TextUtils.isEmpty(contactBean?.remark)) {
            holder.tvName.text = contactBean?.remark
        } else if (!TextUtils.isEmpty(contactBean?.nickname)) {
            holder.tvName.text = contactBean?.nickname
        } else {
            holder.tvName.text = contactBean?.id
        }
        if (mOnSelectChangedListener != null) {
            holder.ccSelect.visibility = View.VISIBLE
            holder.ccSelect.isChecked = contactBean?.isSelected ?: false
            holder.ccSelect.setOnCheckedChangeListener { _, isChecked -> mOnSelectChangedListener?.onSelectChanged(getItem(position), isChecked) }
        }
        holder.content.setOnClickListener(View.OnClickListener {
            if (true != contactBean?.isEnable) {
                return@OnClickListener
            }
            holder.ccSelect.isChecked = !holder.ccSelect.isChecked
            contactBean.isSelected = holder.ccSelect.isChecked
            if (mOnClickListener != null) {
                mOnClickListener?.onItemClick(position, contactBean)
            }
            if (isSingleSelectMode && position != mPreSelectedPosition && contactBean.isSelected) { //单选模式的prePos处理
                mData!![mPreSelectedPosition].isSelected = false
                notifyItemChanged(mPreSelectedPosition)
            }
            mPreSelectedPosition = position
        })
        holder.unreadText.visibility = View.GONE
        if (TextUtils.equals(appContext.resources.getString(R.string.new_friend), contactBean?.id)) {
            holder.avatar.setImageResource(R.drawable.group_new_friend)
            val timFriendPendencyRequest = TIMFriendPendencyRequest()
            timFriendPendencyRequest.timPendencyGetType = TIMPendencyType.TIM_PENDENCY_COME_IN
            TIMFriendshipManager.getInstance().getPendencyList(timFriendPendencyRequest, object : TIMValueCallBack<TIMFriendPendencyResponse> {
                override fun onError(i: Int, s: String) {
                    toastShortMessage("Error code = $i, desc = $s")
                }

                override fun onSuccess(timFriendPendencyResponse: TIMFriendPendencyResponse) {
                    if (timFriendPendencyResponse.items != null) {
                        val pendingRequest = timFriendPendencyResponse.items.size
                        if (pendingRequest == 0) {
                            holder.unreadText.visibility = View.GONE
                        } else {
                            holder.unreadText.visibility = View.VISIBLE
                            holder.unreadText.text = "" + pendingRequest
                        }
                    }
                }
            })
        } else if (TextUtils.equals(appContext.resources.getString(R.string.group), contactBean?.id)) {
            holder.avatar.setImageResource(R.drawable.group_common_list)
        } else if (TextUtils.equals(appContext.resources.getString(R.string.blacklist), contactBean?.id)) {
            holder.avatar.setImageResource(R.drawable.group_black_list)
        } else {
            if (TextUtils.isEmpty(contactBean?.avatarurl)) {
                if (true == contactBean?.isGroup) {
                    holder.avatar.setImageResource(R.drawable.conversation_group)
                } else {
                    holder.avatar.setImageResource(R.drawable.ic_personal_member)
                }
            } else {
                GlideEngine.loadImage(holder.avatar, Uri.parse(contactBean?.avatarurl))
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        if (holder != null) {
            GlideEngine.clear(holder.avatar)
            holder.avatar.setImageResource(0)
        }
        super.onViewRecycled(holder!!)
    }

    private fun getItem(position: Int): ContactItemBean? {
        return if (position < mData?.size ?: 0) {
            mData!![position]
        } else null
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    fun setDataSource(datas: List<ContactItemBean>?) {
        mData = datas
        notifyDataSetChanged()
    }

    fun setSingleSelectMode(mode: Boolean) {
        isSingleSelectMode = mode
    }

    fun setOnSelectChangedListener(selectListener: ContactListView.OnSelectChangedListener?) {
        mOnSelectChangedListener = selectListener
    }

    fun setOnItemClickListener(listener: ContactListView.OnItemClickListener?) {
        mOnClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tvCity)
        var unreadText: TextView = itemView.findViewById(R.id.conversation_unread)
        var avatar: ImageView
        var ccSelect: CheckBox
        var content: View

        init {
            unreadText.visibility = View.GONE
            avatar = itemView.findViewById(R.id.ivAvatar)
            ccSelect = itemView.findViewById(R.id.contact_check_box)
            content = itemView.findViewById(R.id.selectable_contact_item)
        }
    }

}