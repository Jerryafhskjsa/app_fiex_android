package com.black.c2c.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.Money
import com.black.base.model.user.UserInfo
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivitySellerMsgBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route

@Route(value = [RouterConstData.C2C_SELLER])
class C2CSellerMsgActivity: BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivitySellerMsgBinding? = null
    private val userInfo = UserInfo()
    private val money = Money()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_seller_msg)
        binding?.black?.setOnClickListener(this)
        binding?.msg?.setOnClickListener(this)
        binding?.ad?.setOnClickListener(this)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        // adapter = ContractAdapter(mContext!!, BR.listItemSpotAccountModel, walletList)
        //binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        //解决数据加载不完的问题
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        binding?.recyclerView?.isFocusable = false
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.black){
            finish()
        }
        if (id == R.id.msg){
            binding?.recyclerView?.visibility = View.GONE
            binding?.msgBar?.visibility = View.VISIBLE
            binding!!.ad.isChecked = false
            binding?.barA?.visibility = View.GONE
            binding?.barB?.visibility = View.VISIBLE
            binding!!.msg.isChecked = true
        }
        if (id == R.id.ad){
            binding?.recyclerView?.visibility = View.VISIBLE
            binding?.msgBar?.visibility = View.GONE
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding!!.msg.isChecked = false
            binding!!.ad.isChecked = true
        }
    }
}