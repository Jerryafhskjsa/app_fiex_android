package com.black.money.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.money.PromotionsBuyFive
import com.black.base.model.money.PromotionsBuyFiveRecord
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsBuyFiveRecordAdapter
import com.black.money.databinding.ActivityPromotionsBuyFiveRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import java.util.*

@Route(value = [RouterConstData.PROMOTIONS_BUY_FIVE_RECORD], beforePath = RouterConstData.LOGIN)
class PromotionsBuyFiveRecordActivity : BaseActivity(), QRefreshLayout.OnRefreshListener {
    private var promotionsBuyFive: PromotionsBuyFive? = null
    private var binding: ActivityPromotionsBuyFiveRecordBinding? = null
    private var adapter: PromotionsBuyFiveRecordAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promotionsBuyFive = intent.getParcelableExtra(ConstData.PROMOTIONS_BUY_FIVE)
        if (promotionsBuyFive == null || promotionsBuyFive!!.id == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_buy_five_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = PromotionsBuyFiveRecordAdapter(this, BR.listItemPromotionsBuyFiveRecordModel, promotionsBuyFive!!, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        getPromotionsRecord(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.promotions_buy_record)
    }

    override fun onRefresh() {
        getPromotionsRecord(false)
    }

    private fun getPromotionsRecord(isShowLoading: Boolean) {
        //        showTest();
        MoneyApiServiceHelper.getPromotionsBuyFiveRecord(this, isShowLoading, if (promotionsBuyFive == null) null else NumberUtil.formatNumberNoGroup(promotionsBuyFive!!.id), object : NormalCallback<HttpRequestResultDataList<PromotionsBuyFiveRecord?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultDataList<PromotionsBuyFiveRecord?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    if (returnData.data != null) {
                        val dataList = returnData.data as ArrayList<PromotionsBuyFiveRecord?>
                        adapter?.data = dataList
                    }
                } else {
                    adapter?.data = null
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                adapter?.notifyDataSetChanged()
            }
        })
    }
}