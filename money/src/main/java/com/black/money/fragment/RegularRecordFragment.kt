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
import com.black.base.model.PagingData
import com.black.base.model.filter.CoinFilter
import com.black.base.model.filter.RegularRecordStatus
import com.black.base.model.money.Regular
import com.black.base.model.money.RegularConfig
import com.black.base.model.money.RegularLock
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.RegularLockAdapter
import com.black.money.adpter.RegularLockAdapter.OnRegularChangeOutListener
import com.black.money.databinding.FragmentRegularRecordBinding
import com.black.net.HttpRequestResult
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

class RegularRecordFragment : BaseFragment(), OnRegularChangeOutListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var regularId: String? = null
    private var regular: Regular? = null
    private var regularCoinFilter: CoinFilter? = null
    private var regularRecordStatus: RegularRecordStatus? = null

    private var binding: FragmentRegularRecordBinding? = null

    private var adapter: RegularLockAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_regular_record, container, false)

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = RegularLockAdapter(mContext!!, BR.listItemRegularLockModel, null)
        adapter?.setOnRegularChangeOutListener(this)
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
        refreshRegular()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        refreshRegularData()
        getRegularLockRecord(true)
    }

    override fun onRefresh() {
        currentPage = 1
        refreshRegularData()
        getRegularLockRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getRegularLockRecord(true)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onRegularChangeOut(regularLock: RegularLock) {
        mContext?.let {
            val displayMetrics = resources.displayMetrics
            val density = displayMetrics.density
            val confirmDialog = ConfirmDialog(it,
                    String.format("取出%s", if (regularLock.coinType == null) nullAmount else regularLock.coinType),
                    String.format("亲，提前退出将扣除本金 %s%% 的违约金",
                            if (regularLock.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regularLock.defaultRate!! * 100, 2)),
                    object : OnConfirmCallback {
                        override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                            MoneyApiServiceHelper.postRegularChangeOut(mContext, regularLock.id, object : NormalCallback<HttpRequestResultString?>() {
                                override fun error(type: Int, error: Any) {
                                    super.error(type, error)
                                    if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                                        confirmDialog.dismiss()
                                    }
                                }

                                override fun callback(returnData: HttpRequestResultString?) {
                                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                        FryingUtil.showToast(mContext, "取出成功")
                                        confirmDialog.dismiss()
                                        refreshRegularData()
                                        adapter?.removeItem(regularLock)
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
//        int iconRes = regularLock == null ? 0 : FryingUtil.getCoinTypeIconRes(regularLock.coinType);
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

    fun setRegular(regular: Regular?) {
        if (regular?.id != null) {
            regularId = NumberUtil.formatNumberNoGroup(regular.id)
        }
        if (this.regular == null) {
            this.regular = regular
            if (binding == null) {
                return
            }
            refreshRegular()
        }
    }

    private fun refreshRegular() {
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (regular == null || regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalAmount?.text = if (regular == null || regular?.sumLockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.sumLockAmount, 8, 0, 2)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (regular == null || regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalReward?.text = if (regular == null || regular?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.totalInterestAmount, 8, 0, 2)
    }

    private fun refreshRegularData() {
        if (regularCoinFilter == null) {
            return
        }
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>() {

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        val coinType = regularCoinFilter?.code
                        regular = null
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, coinType)) {
                                if (regular == null) {
                                    regular = temp
                                } else {
                                    if (temp.sumLockAmount != null) {
                                        regular?.sumLockAmount = if (regular?.sumLockAmount == null) temp.sumLockAmount else regular?.sumLockAmount!! + temp.sumLockAmount!!
                                    }
                                    if (temp.totalInterestAmount != null) {
                                        regular?.totalInterestAmount = if (regular?.totalInterestAmount == null) temp.totalInterestAmount else regular?.sumLockAmount!! + temp.totalInterestAmount!!
                                    }
                                }
                                regular = temp
                            }
                        }
                        refreshRegular()
                    }
                }
            }
        })
    }

    fun setFilters(regularCoinFilter: CoinFilter?, regularRecordStatus: RegularRecordStatus?) {
        if (this.regularCoinFilter == null || this.regularCoinFilter != regularCoinFilter) {
            this.regularCoinFilter = regularCoinFilter
            refreshRegularData()
            this.regularRecordStatus = regularRecordStatus
            currentPage = 1
            getRegularLockRecord(true)
        } else if (this.regularRecordStatus == null || this.regularRecordStatus != regularRecordStatus) {
            this.regularRecordStatus = regularRecordStatus
            currentPage = 1
            getRegularLockRecord(true)
        }
    }

    private fun getRegularLockRecord(isShowLoading: Boolean) {
        if (regularCoinFilter == null || regularRecordStatus == null) {
            return
        }
        MoneyApiServiceHelper.getRegularLockRecord(mContext, regularId, regularCoinFilter?.code, regularRecordStatus?.code, currentPage, 10, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<RegularLock?>?>?>() {
            override fun error(type: Int, error: Any) {
                super.error(type, error)
                showData(null)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<RegularLock?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    showData(returnData.data?.list)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showData(data: ArrayList<RegularLock?>?) {
        if (adapter != null) {
            if (currentPage == 1) {
                adapter?.data = (data)
            } else {
                adapter?.addAll(data)
            }
            adapter?.notifyDataSetChanged()
            binding?.refreshLayout?.setRefreshing(false)
            binding?.refreshLayout?.setLoading(false)
        }
    }
}