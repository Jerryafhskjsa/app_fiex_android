package com.black.clutter.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.CommonApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.clutter.Forum
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.BR
import com.black.clutter.R
import com.black.clutter.adapter.ForumAdapter
import com.black.clutter.databinding.ActivityForumBinding
import com.black.clutter.view.ForumShareWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

//社区
@Route(value = [RouterConstData.FORUM])
class ForumActivity : BaseActionBarActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnItemClickListener {
    private var binding: ActivityForumBinding? = null

    private var adapter: ForumAdapter? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forum)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = ForumAdapter(this, BR.listItemForumModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        //解决数据加载不完的问题
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        getForumList(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return resources.getString(R.string.forum)
    }

    override fun onClick(v: View) {}

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val forum = adapter?.getItem(position)
        forum?.let {
            ForumShareWindow(this, forum).show()
        }
    }

    override fun onRefresh() {
        currentPage = 1
        getForumList(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage++
            getForumList(true)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    private fun getForumList(isShowLoading: Boolean) {
        CommonApiServiceHelper.getForumList(this, isShowLoading, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<Forum?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<Forum?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.totalCount ?: 0
                    if (currentPage == 1) {
                        adapter?.data = (returnData.data?.list)
                    } else {
                        adapter?.addAll(returnData.data?.list)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                }
            }
        })
    }
}