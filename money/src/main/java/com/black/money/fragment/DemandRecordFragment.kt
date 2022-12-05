package com.black.money.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.filter.CoinFilter
import com.black.base.model.filter.DemandRecordStatus
import com.black.base.model.money.Demand
import com.black.base.model.money.DemandConfig
import com.black.base.model.money.DemandLock
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.DemandLockAdapter
import com.black.money.adpter.DemandLockAdapter.OnDemandChangeOutListener
import com.black.money.databinding.FragmentDemandRecordBinding
import com.black.net.HttpRequestResult
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

class DemandRecordFragment : BaseFragment(), OnDemandChangeOutListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var demand: Demand? = null
    private var demandCoinFilter: CoinFilter? = null
    private var demandRecordStatus: DemandRecordStatus? = null

    private var binding: FragmentDemandRecordBinding? = null

    private var adapter: DemandLockAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_demand_record, container, false)

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = DemandLockAdapter(mContext!!, BR.listItemDemandLockModel, null)
        adapter?.setOnDemandChangeOutListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.emptyView?.root?.setBackgroundColor(SkinCompatResources.getColor(mContext, R.color.B1))
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        refreshDemand()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshDemandData()
        getDemandLockRecord(true)
    }

    override fun onRefresh() {
        currentPage = 1
        refreshDemandData()
        getDemandLockRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getDemandLockRecord(true)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onDemandChangeOut(demandLock: DemandLock) {
        mContext?.let {
            val displayMetrics = resources.displayMetrics
            val density = displayMetrics.density
            val confirmDialog = ConfirmDialog(it,
                    String.format("取出%s", if (demandLock.coinType == null) nullAmount else demandLock.coinType),
                    String.format("亲，该笔理财%s日后年化率将提升至%s%%，确认取出吗？",
                            if (demandLock.remainingDay == null) nullAmount else demandLock.remainingDay,
                            if (demandLock.nextRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demandLock.nextRate!! * 100, 2)),
                    object : OnConfirmCallback {
                        override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                            MoneyApiServiceHelper.postDemandChangeOut(mContext, demandLock.id, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                                override fun callback(returnData: HttpRequestResultString?) {
                                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                        FryingUtil.showToast(mContext, "取出成功")
                                        confirmDialog.dismiss()
                                        refreshDemandData()
                                        adapter?.removeItem(demandLock)
                                        adapter?.notifyDataSetChanged()
                                    } else {
                                        FryingUtil.showToast(mContext, if (returnData?.msg == null) "取出失败" else returnData.msg)
                                    }
                                }
                            })
                        }

                    })
            confirmDialog.setTitleGravity(Gravity.LEFT or Gravity.CENTER_VERTICAL)
            val titleView = confirmDialog.titleView
            //        titleView.setCompoundDrawablePadding((int) (density * 8));
//        int iconRes = demandLock == null ? 0 : FryingUtil.getCoinTypeIconRes(demandLock.coinType);
//        Drawable iconDrawable = iconRes == 0 ? null : SkinCompatResources.getDrawable(mContext, iconRes);
//        CommonUtil.setTextViewCompoundDrawable(titleView, iconDrawable, 0);
            confirmDialog.setConfirmText("确认取出")
            val messageView = confirmDialog.messageView
            messageView.setTextColor(SkinCompatResources.getColor(mContext, R.color.T5))
            messageView.gravity = Gravity.LEFT
            messageView.setBackgroundColor(SkinCompatResources.getColor(mContext, R.color.T5_ALPHA10))
            messageView.setPadding((density * 10).toInt(), (density * 5).toInt(), (density * 10).toInt(), (density * 5).toInt())
            confirmDialog.show()
        }
    }

    fun setDemand(demand: Demand?) {
        if (this.demand == null) {
            this.demand = demand
            if (binding == null) {
                return
            }
            refreshDemand()
        }
    }

    private fun refreshDemand() {
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (demand == null || demand?.coinType == null) nullAmount else demand?.coinType)
        binding?.totalAmount?.text = if (demand == null || demand?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.lockAmount, 8, 0, 2)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (demand == null || demand?.coinType == null) nullAmount else demand?.coinType)
        binding?.totalReward?.text = if (demand == null || demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.totalInterestAmount, 8, 0, 2)
    }

    private fun refreshDemandData() {
        if (demandCoinFilter == null) {
            return
        }
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, demandCoinFilter?.code)) {
                                demand = temp
                                refreshDemand()
                                break
                            }
                        }
                    }
                }
            }
        })
    }

    fun setFilters(demandCoinFilter: CoinFilter?, demandRecordStatus: DemandRecordStatus?) {
        if (this.demandCoinFilter == null || this.demandCoinFilter != demandCoinFilter) {
            this.demandCoinFilter = demandCoinFilter
            refreshDemandData()
            this.demandRecordStatus = demandRecordStatus
            currentPage = 1
            getDemandLockRecord(true)
        } else if (this.demandRecordStatus == null || this.demandRecordStatus != demandRecordStatus) {
            this.demandRecordStatus = demandRecordStatus
            currentPage = 1
            getDemandLockRecord(true)
        }
    }

    private fun getDemandLockRecord(isShowLoading: Boolean) {
        if (demandCoinFilter == null || demandRecordStatus == null) {
            return
        }
        MoneyApiServiceHelper.getDemandLockRecord(mContext, demandCoinFilter?.code, demandRecordStatus?.code, currentPage, 10, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<DemandLock?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                showData(null)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<DemandLock?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    showData(returnData.data?.data)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showData(data: ArrayList<DemandLock?>?) {
        if (adapter != null) {
            if (TextUtils.equals(demandRecordStatus?.code, DemandRecordStatus.INTO.code)) {
                adapter?.setStatus(1)
            } else {
                adapter?.setStatus(2)
            }
            if (currentPage == 1) {
                adapter?.data = data
            } else {
                adapter?.addAll(data)
            }
            adapter?.notifyDataSetChanged()
            binding?.refreshLayout?.setRefreshing(false)
            binding?.refreshLayout?.setLoading(false)
        }
    }

    fun changeOutALl() {
        if (adapter?.count == 0) {
            FryingUtil.showToast(mContext, "无可取出的记录")
        } else {
            mContext?.let {
                val confirmDialog = ConfirmDialog(it,
                        String.format("取出%s", if (demand == null || demand?.coinType == null) nullAmount else demand?.coinType),
                        "你是否确定要提取当前所有资产？",
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                MoneyApiServiceHelper.postDemandChangeOutBatch(mContext, if (demand == null) null else demand?.coinType, true, null, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                                    override fun callback(returnData: HttpRequestResultString?) {
                                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            FryingUtil.showToast(mContext, "取出成功")
                                            confirmDialog.dismiss()
                                            refreshDemandData()
                                            onRefresh()
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "取出失败" else returnData.msg)
                                        }
                                    }
                                })
                            }

                        })
                confirmDialog.setTitleGravity(Gravity.LEFT)
                confirmDialog.setConfirmText("确认取出")
                val messageView = confirmDialog.messageView
                messageView.setTextColor(SkinCompatResources.getColor(mContext, R.color.T5))
                messageView.gravity = Gravity.LEFT
                messageView.setBackgroundColor(SkinCompatResources.getColor(mContext, R.color.T5_ALPHA10))
                val displayMetrics = resources.displayMetrics
                val density = displayMetrics.density
                messageView.setPadding((density * 10).toInt(), (density * 5).toInt(), (density * 10).toInt(), (density * 5).toInt())
                confirmDialog.show()
            }
        }
    }
}