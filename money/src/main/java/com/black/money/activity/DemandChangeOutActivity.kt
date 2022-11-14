package com.black.money.activity

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.Demand
import com.black.base.model.money.DemandConfig
import com.black.base.model.money.DemandLock
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.util.RouterConstData
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.base.widget.ObserveScrollView
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.DemandLockAdapter
import com.black.money.adpter.DemandLockAdapter.OnDemandChangeOutListener
import com.black.money.databinding.ActivityDemandChangeOutBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

//聚宝盆转出列表
@Route(value = [RouterConstData.DEMAND_CHANGE_OUT])
class DemandChangeOutActivity : BaseActionBarActivity(), View.OnClickListener, ObserveScrollView.ScrollListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnDemandChangeOutListener {
    private var bgC1: Drawable? = null
    private var bgB2: Drawable? = null
    private var bgDefault: Drawable? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1 = 0

    private var demand: Demand? = null

    private var binding: ActivityDemandChangeOutBinding? = null

    private var headView: View? = null
    private var btnBack: ImageButton? = null
    private var headTitleView: TextView? = null
    private var headExtrasView: TextView? = null

    private var adapter: DemandLockAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demand = intent.getParcelableExtra(ConstData.DEMAND)
        if (demand == null) {
            finish()
            return
        }

        bgC1 = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        bgB2 = ColorDrawable(SkinCompatResources.getColor(this, R.color.B2))
        bgDefault = ColorDrawable(SkinCompatResources.getColor(this, R.color.C1))
        btnBackDefault = SkinCompatResources.getDrawable(this, R.drawable.btn_back_white)
        btnBackNormal = SkinCompatResources.getDrawable(this, R.drawable.btn_back)
        colorDefault = SkinCompatResources.getColor(this, R.color.T4)
        colorT1 = SkinCompatResources.getColor(this, R.color.T1)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_demand_change_out)

        binding?.scrollView?.addScrollListener(this)
        binding?.totalAmountTitle?.setText(String.format("资产总额 (%s) ", if (demand?.coinType == null) nullAmount else demand?.coinType))
        binding?.totalAmount?.setText(if (demand?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.lockAmount, 15, 0, 12))
        binding?.totalRewardTitle?.setText(String.format("累计收益 (%s) ", if (demand?.coinType == null) nullAmount else demand?.coinType))
        binding?.totalReward?.setText(if (demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.totalInterestAmount, 15, 0, 12))

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = DemandLockAdapter(mContext, BR.listItemDemandLockModel, null)
        adapter?.setOnDemandChangeOutListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.emptyView?.root?.setBackgroundColor(SkinCompatResources.getColor(this, R.color.B1))
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        resetActionBarRes(0f)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back_text
    }

    override fun initActionBarView(view: View) {
        headView = view
        btnBack = view.findViewById(R.id.action_bar_back)
        headTitleView = view.findViewById(R.id.action_bar_title)
        headTitleView?.text = "聚宝盆"
        view.findViewById<TextView>(R.id.action_bar_extras).also { headExtrasView = it }.setOnClickListener(this)
        headExtrasView?.text = "全部取出"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.action_bar_extras) {
            if (adapter?.count == 0) {
                FryingUtil.showToast(mContext, "无可取出的记录")
            } else {
                val confirmDialog = ConfirmDialog(this,
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
                messageView.setTextColor(SkinCompatResources.getColor(this, R.color.T5))
                messageView.gravity = Gravity.LEFT
                messageView.setBackgroundColor(SkinCompatResources.getColor(this, R.color.T5_ALPHA10))
                val displayMetrics = resources.displayMetrics
                val density = displayMetrics.density
                messageView.setPadding((density * 10).toInt(), (density * 5).toInt(), (density * 10).toInt(), (density * 5).toInt())
                confirmDialog.show()
            }
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (binding?.scrollView != null && binding?.topLayout != null && binding?.topLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY!!.toFloat() / binding?.topLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            resetActionBarRes(alpha)
        }
    }

    override fun onResume() {
        super.onResume()
        getDemandLockRecord(true)
    }

    override fun onRefresh() {
        currentPage = 1
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
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val confirmDialog = ConfirmDialog(this,
                String.format("取出%s", if (demandLock.coinType == null) nullAmount else demandLock.coinType),
                String.format("亲，该笔理财%s日后年化率将提升至%s%%，确认取出吗？", if (demandLock.remainingDay == null) nullAmount else demandLock.remainingDay, if (demandLock.nextRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demandLock.nextRate!! * 100, 2)),
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
//        Drawable iconDrawable = iconRes == 0 ? null : SkinCompatResources.getDrawable(this, iconRes);
//        CommonUtil.setTextViewCompoundDrawable(titleView, iconDrawable, 0);
        confirmDialog.setConfirmText("确认取出")
        val messageView = confirmDialog.messageView
        messageView.setTextColor(SkinCompatResources.getColor(this, R.color.T5))
        messageView.gravity = Gravity.LEFT
        messageView.setBackgroundColor(SkinCompatResources.getColor(this, R.color.T5_ALPHA10))
        messageView.setPadding((density * 10).toInt(), (density * 5).toInt(), (density * 10).toInt(), (density * 5).toInt())
        confirmDialog.show()
    }

    val coinIcon: Unit
        get() {}

    private fun resetActionBarRes(alpha: Float) {
        if (alpha < 1) {
            headView?.background = bgDefault
            binding?.rootView?.background = bgDefault
            btnBack?.setImageDrawable(btnBackDefault)
            headTitleView?.setTextColor(colorDefault)
            headExtrasView?.setTextColor(colorDefault)
        } else {
            headView?.background = bgB2
            binding?.rootView?.background = bgB2
            btnBack?.setImageDrawable(btnBackNormal)
            headTitleView?.setTextColor(colorT1)
            headExtrasView?.setTextColor(colorT1)
        }
    }

    private fun refreshDemand() {
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (demand?.coinType == null) nullAmount else demand?.coinType)
        binding?.totalAmount?.text = if (demand?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.lockAmount, 8, 0, 2)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (demand?.coinType == null) nullAmount else demand?.coinType)
        binding?.totalReward?.text = if (demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand?.totalInterestAmount, 8, 0, 2)
    }

    private fun refreshDemandData() {
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>(mContext!!) {

            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, demand?.coinType)) {
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

    private fun getDemandLockRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getDemandLockRecord(this, demand?.coinType, "1", currentPage, 10, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<DemandLock?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<DemandLock?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.data
                    } else {
                        adapter?.addAll(returnData.data?.data)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    adapter?.data = null
                    adapter?.notifyDataSetChanged()
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }
}