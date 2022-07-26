package com.black.user.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.adapter.interfaces.OnSwipeItemClickListener
import com.black.base.model.user.User
import com.black.user.R
import com.black.user.databinding.ListItemAccountBinding
import skin.support.content.res.SkinCompatVectorResources

class AccountAdapter(context: Context, variableId: Int, data: ArrayList<User?>?) : BaseRecycleDataBindAdapter<User?, ListItemAccountBinding>(context, variableId, data) {
    private var onAccountItemClickListener: OnSwipeItemClickListener? = null
    override fun onBindViewHolder(holder: BaseViewHolder<ListItemAccountBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val viewHolder: ListItemAccountBinding? = holder.dataBing
        val user = getItem(position)
        viewHolder?.index?.setText((position + 1).toString())
        viewHolder?.name?.setText(user?.userName)
        viewHolder?.uid?.setText(String.format("UID:%s", user?.uid))
        if (true == user?.isCurrentUser) {
            val drawable = SkinCompatVectorResources.getDrawableCompat(context, R.drawable.icon_account_dot_check)
            if (drawable != null) {
                viewHolder?.iconIndex?.setImageDrawable(drawable)
            }
            //            holder.indexIconView.setImageResource(R.drawable.icon_account_dot_check);
            viewHolder?.check?.visibility = View.VISIBLE
        } else {
            val drawable = SkinCompatVectorResources.getDrawableCompat(context, R.drawable.icon_account_dot_default)
            if (drawable != null) {
                viewHolder?.iconIndex?.setImageDrawable(drawable)
            }
            //            holder.indexIconView.setImageResource(R.drawable.icon_account_dot_default);
            viewHolder?.check?.visibility = View.GONE
        }
        viewHolder?.contentLayout?.setOnClickListener {
            if (onAccountItemClickListener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    onAccountItemClickListener?.onItemClick(recyclerView, it, position, user)
                }
            }
        }
        viewHolder?.itemDelete?.setOnClickListener {
            if (onAccountItemClickListener != null) {
                onAccountItemClickListener?.deleteClick(position)
            }
        }
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_account
    }

    fun setOnAccountItemClickListener(onItemClickListener: OnSwipeItemClickListener?) {
        super.setOnItemClickListener(onItemClickListener)
        this.onAccountItemClickListener = onItemClickListener
    }

}