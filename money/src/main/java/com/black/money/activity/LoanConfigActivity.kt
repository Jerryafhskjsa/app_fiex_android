package com.black.money.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.money.LoanConfig
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.LoanConfigAdapter
import com.black.money.databinding.ActivityLoanConfigBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.LOAN_CONFIG], beforePath = RouterConstData.LOGIN)
class LoanConfigActivity : BaseActionBarActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener {
    private var binding: ActivityLoanConfigBinding? = null
    private var adapter: LoanConfigAdapter? = null
    private var loanConfigList: ArrayList<LoanConfig?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_config)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val displayMetrics = resources.displayMetrics
        decoration.setDividerHeight((displayMetrics.density * 8).toInt())
        decoration.setDrawable(SkinCompatResources.getDrawable(mContext, R.drawable.bg_divider_default))
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = LoanConfigAdapter(this, BR.listItemLoanConfigModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.btnLoan?.setOnClickListener(this)
        loanConfig
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "抵押借贷"
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        findViewById<View>(R.id.btn_record).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_record) {
            BlackRouter.getInstance().build(RouterConstData.LOAN_RECORD).go(this)
        } else if (id == R.id.btn_loan) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.LOAN_CONFIG_LIST, loanConfigList)
            BlackRouter.getInstance().build(RouterConstData.LOAN_CREATE).with(bundle).go(this)
        }
    }

    override fun onRefresh() {
        loanConfig
    }

    private val loanConfig: Unit
        get() {
            checkClickEnable()
            MoneyApiServiceHelper.getLoanConfig(this, object : NormalCallback<HttpRequestResultDataList<LoanConfig?>?>() {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    binding?.refreshLayout?.setRefreshing(false)
                }

                override fun callback(returnData: HttpRequestResultDataList<LoanConfig?>?) {
                    binding?.refreshLayout?.setRefreshing(false)
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        loanConfigList = returnData.data
                        adapter?.data = returnData.data
                        adapter?.notifyDataSetChanged()
                        checkClickEnable()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private fun checkClickEnable() {
        binding?.btnLoan?.isEnabled = loanConfigList != null
    }
}