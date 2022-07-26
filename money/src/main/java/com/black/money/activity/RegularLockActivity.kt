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
import com.black.base.model.PagingData
import com.black.base.model.money.Regular
import com.black.base.model.money.RegularConfig
import com.black.base.model.money.RegularLock
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
import com.black.money.adpter.RegularLockAdapter
import com.black.money.adpter.RegularLockAdapter.OnRegularChangeOutListener
import com.black.money.databinding.ActivityRegularLockBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.REGULAR_LOCK], beforePath = RouterConstData.LOGIN)
class RegularLockActivity : BaseActionBarActivity(), View.OnClickListener, ObserveScrollView.ScrollListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnRegularChangeOutListener {
    private var bgC1: Drawable? = null
    private var bgB2: Drawable? = null
    private var bgDefault: Drawable? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1 = 0

    private var regular: Regular? = null

    private var binding: ActivityRegularLockBinding? = null

    private var headView: View? = null
    private var btnBack: ImageButton? = null
    private var headTitleView: TextView? = null

    private var adapter: RegularLockAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        regular = intent.getParcelableExtra(ConstData.REGULAR)
        if (regular == null) {
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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_regular_lock)
        binding?.scrollView?.addScrollListener(this)
        binding?.totalAmountTitle?.setText(String.format("资产总额 (%s) ", if (regular?.coinType == null) nullAmount else regular?.coinType))
        binding?.totalAmount?.setText(if (regular?.sumLockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.sumLockAmount, 15, 0, 12))
        binding?.totalRewardTitle?.setText(String.format("累计收益 (%s) ", if (regular?.coinType == null) nullAmount else regular?.coinType))
        binding?.totalReward?.setText(if (regular?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.totalInterestAmount, 15, 0, 12))

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = RegularLockAdapter(mContext, BR.listItemRegularLockModel, null)
        adapter?.setOnRegularChangeOutListener(this)
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
        headTitleView?.text = "定利宝"
    }

    override fun onClick(v: View) {}
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (binding?.scrollView != null && binding?.topLayout != null && binding?.topLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY!!.toFloat() / binding?.topLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            resetActionBarRes(alpha)
        }
    }

    override fun onResume() {
        super.onResume()
        getRegularLockRecord(true)
    }

    override fun onRefresh() {
        currentPage = 1
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
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val confirmDialog = ConfirmDialog(this,
                String.format("取出%s", if (regularLock.coinType == null) nullAmount else regularLock.coinType),
                String.format("亲，提前退出将扣除本金 %s%% 的违约金", if (regularLock.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regularLock.defaultRate!! * 100, 2)),
                object : OnConfirmCallback {
                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                        MoneyApiServiceHelper.postRegularChangeOut(mContext, regularLock.id, object : NormalCallback<HttpRequestResultString?>() {
                            override fun error(type: Int, error: Any?) {
                                super.error(type, error)
                                if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                                    confirmDialog.dismiss()
                                }
                            }

                            override fun callback(returnData: HttpRequestResultString?) {
                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    FryingUtil.showToast(mContext, "取出成功")
                                    confirmDialog.dismiss()
                                    refreshDemandData()
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
        confirmDialog.setConfirmText("确认取出")
        val messageView = confirmDialog.messageView
        messageView.setTextColor(SkinCompatResources.getColor(this, R.color.T5))
        messageView.gravity = Gravity.LEFT
        messageView.setBackgroundColor(SkinCompatResources.getColor(this, R.color.T5_ALPHA10))
        messageView.setPadding((density * 10).toInt(), (density * 5).toInt(), (density * 10).toInt(), (density * 5).toInt())
        confirmDialog.show()
    }

    private fun resetActionBarRes(alpha: Float) {
        if (alpha < 1) {
            headView?.background = bgDefault
            binding?.rootView?.background = bgDefault
            btnBack?.setImageDrawable(btnBackDefault)
            headTitleView?.setTextColor(colorDefault)
        } else {
            headView?.background = bgB2
            binding?.rootView?.background = bgB2
            btnBack?.setImageDrawable(btnBackNormal)
            headTitleView?.setTextColor(colorT1)
        }
    }

    private fun refreshDemand() {
        binding?.totalAmountTitle?.text = String.format("资产总额 (%s) ", if (regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalAmount?.text = if (regular?.sumLockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.sumLockAmount, 8, 0, 2)
        binding?.totalRewardTitle?.text = String.format("累计收益 (%s) ", if (regular?.coinType == null) nullAmount else regular?.coinType)
        binding?.totalReward?.text = if (regular?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular?.totalInterestAmount, 8, 0, 2)
    }

    private fun refreshDemandData() {
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>() {

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val demands = if (returnData.data == null) null else returnData.data?.coinTypeConf
                    if (demands != null) {
                        for (temp in demands) {
                            if (temp != null && TextUtils.equals(temp.coinType, regular?.coinType)) {
                                regular = temp
                                refreshDemand()
                                break
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getRegularLockRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getRegularLockRecord(this, null, regular?.coinType, null, currentPage, 10, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<RegularLock?>?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<RegularLock?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.list
                    } else {
                        adapter?.addAll(returnData.data?.list)
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