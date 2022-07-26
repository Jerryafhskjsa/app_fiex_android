package com.black.user.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.filter.FilterEntity
import com.black.base.lib.filter.FilterResult
import com.black.base.lib.filter.FilterWindow
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.filter.DateFilter
import com.black.base.model.filter.RecommendPeopleFilter
import com.black.base.model.user.RecommendInfo
import com.black.base.model.user.RecommendPeopleDetail
import com.black.base.model.user.UserInfo
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.user.BR
import com.black.user.R
import com.black.user.adapter.RecommendPeopleAdapter
import com.black.user.databinding.ActivityRecommendNewBinding
import com.black.user.view.AdvanceShareNewWindow
import com.black.user.view.RecommendShareWindow
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.RECOMMEND], beforePath = RouterConstData.LOGIN)
class RecommendNewActivity : BaseActivity(), View.OnClickListener, QRefreshLayout.OnLoadMoreCheckListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener {
    private var userInfo: UserInfo? = null

    private var binding: ActivityRecommendNewBinding? = null

    private var adapter: RecommendPeopleAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recommend_new)
        userInfo = CookieUtil.getUserInfo(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = RecommendPeopleAdapter(mContext, BR.listItemRecommendPeopleModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        getRecommendPeopleDetail(true)
        binding?.invitationCode?.setText(if (userInfo!!.inviteCode == null) "null" else userInfo!!.inviteCode)
        binding?.invitationCodeCopy?.setOnClickListener(this)
        binding?.btnShare?.setOnClickListener(this)
        binding?.btnRecommend?.setOnClickListener(this)
        getRecommendCount()
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.invitation_code_copy) {
            if (CommonUtil.copyText(this, userInfo!!.inviteCode)) {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
            }
        } else if (i == R.id.btn_recommend) { //邀请
            getRecommendInfo()
        } else if (i == R.id.btn_share) {
            getPosterConfig()
        }
    }

    private fun getRecommendCount() {
        UserApiServiceHelper.getRecommendCount(this, object : NormalCallback<HttpRequestResultData<Int?>?>() {
            override fun callback(returnData: HttpRequestResultData<Int?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    var count = returnData.data
                    count = count ?: 0
                    binding?.recommendCount?.text = String.format(getString(R.string.recommend_count), NumberUtil.formatNumberNoGroup(count))
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    //        UserApiServiceHelper.getRecommendShareInfo(this, new NormalCallback<HttpRequestResultData<RecommendInfo>>() {
//            @Override
//            public void callback(HttpRequestResultData<RecommendInfo> returnData) {
//                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
//                    new RecommendShareWindow(RecommendNewActivity.this, userInfo, returnData.data).show();
//                } else {
//                    FryingUtil.showToast(mContext, returnData == null ? "null" : returnData.msg);
//                }
//            }
//        });
    private fun getRecommendInfo() {
        CommonApiServiceHelper.getInviteUrl(mContext, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(urlData: HttpRequestResultString?) {
                if (urlData != null && urlData.code == HttpRequestResult.SUCCESS && urlData.data != null && urlData.data!!.isNotEmpty()) {
                    val recommendInfo = RecommendInfo()
                    recommendInfo.title = "FBSEX"
                    recommendInfo.describe = getString(R.string.company)
                    recommendInfo.link = urlData.data + userInfo!!.inviteCode
                    RecommendShareWindow(this@RecommendNewActivity, userInfo, recommendInfo).show()
                } else {
                    FryingUtil.showToast(mContext, getString(R.string.alert_server_error))
                }
            }
        })
    }
    //        UserApiServiceHelper.getRecommendShareInfo(this, new NormalCallback<HttpRequestResultData<RecommendInfo>>() {
    //            @Override
    //            public void callback(HttpRequestResultData<RecommendInfo> returnData) {
    //                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
    //                    new RecommendShareWindow(RecommendNewActivity.this, userInfo, returnData.data).show();
    //                } else {
    //                    FryingUtil.showToast(mContext, returnData == null ? "null" : returnData.msg);
    //                }
    //            }
    //        });
    //        CommonApiServiceHelper.getPosterConfig(this, 1, new NormalCallback<HttpRequestResultDataList<Poster>>() {
//            @Override
//            public void error(int type, Object error) {
//                FryingUtil.showToast(mContext, getString(R.string.alert_server_error));
//            }
//
//            @Override
//            public void callback(HttpRequestResultDataList<Poster> returnData) {
//                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null && !returnData.data.isEmpty()) {
//                    new AdvanceShareWindow((BaseActionBarActivity) mContext, userInfo, returnData.data).show();
//                } else {
//                    FryingUtil.showToast(mContext, getString(R.string.alert_server_error));
//                }
//            }
//        });

    //邀请海报配置
    private fun getPosterConfig() {
        //        CommonApiServiceHelper.getPosterConfig(this, 1, new NormalCallback<HttpRequestResultDataList<Poster>>() {
        //            @Override
        //            public void error(int type, Object error) {
        //                FryingUtil.showToast(mContext, getString(R.string.alert_server_error));
        //            }
        //
        //            @Override
        //            public void callback(HttpRequestResultDataList<Poster> returnData) {
        //                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null && !returnData.data.isEmpty()) {
        //                    new AdvanceShareWindow((BaseActionBarActivity) mContext, userInfo, returnData.data).show();
        //                } else {
        //                    FryingUtil.showToast(mContext, getString(R.string.alert_server_error));
        //                }
        //            }
        //        });
        CommonApiServiceHelper.getMyPosterList(this, object : NormalCallback<HttpRequestResultDataList<String?>?>() {
            override fun error(type: Int, error: Any?) {
                FryingUtil.showToast(mContext, getString(R.string.alert_server_error))
            }

            override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null && returnData.data!!.isNotEmpty()) {
                    CommonApiServiceHelper.getInviteUrl(mContext, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(urlData: HttpRequestResultString?) {
                            if (urlData != null && urlData.code == HttpRequestResult.SUCCESS && urlData.data != null && urlData.data!!.isNotEmpty()) {
                                userInfo?.let {
                                    AdvanceShareNewWindow(this@RecommendNewActivity, it, urlData.data!!, returnData.data).show()
                                }
                            } else {
                                FryingUtil.showToast(mContext, getString(R.string.alert_server_error))
                            }
                        }
                    })
                } else {
                    FryingUtil.showToast(mContext, getString(R.string.alert_server_error))
                }
            }
        })
    }

    private fun onRefreshEnd() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }

    private fun getRecommendPeopleDetail(isShowLoading: Boolean) {
        UserApiServiceHelper.getRecommendPeopleDetail(mContext, isShowLoading, currentPage, 10, peopleFilter.code, dateFilter.startTime, dateFilter.endTime, object : NormalCallback<HttpRequestResultData<PagingData<RecommendPeopleDetail?>?>?>() {
            override fun error(type: Int, error: Any?) {
                onRefreshEnd()
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<RecommendPeopleDetail?>?>?) {
                onRefreshEnd()
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.data
                    } else {
                        adapter?.addAll(returnData.data?.data)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private var peopleFilter = RecommendPeopleFilter.LEVEL_01
    private var dateFilter = DateFilter.ALL
    fun openFilterWindow(activity: BaseActionBarActivity) {
        val data: MutableList<FilterEntity<*>> = ArrayList()
        data.add(RecommendPeopleFilter.getDefaultFilterEntity(peopleFilter))
        data.add(DateFilter.getDefaultFilterEntity(dateFilter))
        FilterWindow(activity, data)
                .show(object : FilterWindow.OnFilterSelectListener {
                    override fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>) {
                        for (filterResult in selectResult) {
                            if (RecommendPeopleFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                peopleFilter = filterResult.data as RecommendPeopleFilter
                            } else if (DateFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                dateFilter = filterResult.data as DateFilter
                            }
                        }
                        currentPage = 1
                        getRecommendPeopleDetail(true)
                    }

                })
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onRefresh() {
        currentPage = 1
        getRecommendPeopleDetail(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getRecommendPeopleDetail(true)
        }
    }
}