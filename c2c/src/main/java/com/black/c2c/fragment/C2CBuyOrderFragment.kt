package com.black.c2c.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CMessage
import com.black.base.model.c2c.SellerAD
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CMessageAdapter
import com.black.c2c.adapter.C2CSellerBuyAdapter
import com.black.c2c.databinding.FragmentBuyerOderBinding
import com.black.c2c.databinding.FragmentC2cCustomerBuyItemBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import skin.support.content.res.SkinCompatResources
import java.util.Collections.addAll


class C2CBuyOrderFragment : BaseFragment(),View.OnClickListener
{
    private var binding: FragmentBuyerOderBinding? = null
    private var layout: View? = null
    private var adapter: C2CMessageAdapter? = null
    private var id2: String? = null
    private var dataList: ArrayList<C2CMainAD?>? = ArrayList()
    private var sellList: ArrayList<C2CMainAD?>? = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_buyer_oder, container, false)
        layout = binding?.root
        id2 = arguments?.getString(ConstData.COIN_TYPE).toString()
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.send?.setOnClickListener(this)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = C2CMessageAdapter(mContext!!, BR.listItemC2CSallerBuyModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.refreshLayout?.isFocusable = false
        binding?.refreshLayout?.isNestedScrollingEnabled = false
        binding?.refreshLayout?.setRefreshing(true)
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        //getC2cList()

        return layout
    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id ==R.id.send){
            getC2CTime()
            getC2CPull()
        }
    }

   /* private fun getC2CImage(){
        C2CApiServiceHelper.getC2CImage(mContext, id2 , object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    adapter
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
*/
   /* private fun getC2cText(){
        val content = binding?.putMessage?.text.toString()
        C2CApiServiceHelper.getC2CText(mContext, id2 ,content, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
*/


    private fun getC2CPull(){
        C2CApiServiceHelper.getC2CPull(mContext, id2 ,null, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun getC2CTime(){
        C2CApiServiceHelper.getC2CTime(mContext , object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

}