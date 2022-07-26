package com.black.money.activity

import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.PagingData
import com.black.base.model.money.PromotionsBuy
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsBuyListAdapter
import com.black.money.databinding.ActivityPromotionsBuyBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.PROMOTIONS_BUY], beforePath = RouterConstData.LOGIN)
class PromotionsBuyActivity : BaseActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, OnItemClickListener, PromotionsBuyListAdapter.OnPromotionsBuyListener, OnLoadListener, OnLoadMoreCheckListener {
    private var binding: ActivityPromotionsBuyBinding? = null

    private var adapter: PromotionsBuyListAdapter? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_buy)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = PromotionsBuyListAdapter(this, BR.listItemPromotionsBuyModel, null)
        adapter?.setOnPromotionsBuyListener(this)
        adapter?.setOnItemClickListener(this)
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
        getPromotionsBuy(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.request_buy_coin)
    }

    override fun onClick(view: View) {
    }

    override fun onRefresh() {
        currentPage = 1
        getPromotionsBuy(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage++
            getPromotionsBuy(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val promotions = adapter?.getItem(position)
        if (promotions is PromotionsBuy) {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.PROMOTIONS_BUY, promotions)
            BlackRouter.getInstance().build(RouterConstData.PROMOTIONS_BUY_DETAIL).with(bundle).go(this)
        }
    }

    override fun onResume() {
        super.onResume()
        checkListAddTimer()
    }

    override fun onStop() {
        super.onStop()
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
    }

    private fun getPromotionsBuy(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getPromotionsBuyList(this, 1, currentPage, 8, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<PromotionsBuy?>?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<PromotionsBuy?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = if (returnData.data == null) 0 else returnData.data!!.total
                    showPromotionsBuy(if (returnData.data == null) null else returnData.data?.data)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showPromotionsBuy(list: ArrayList<PromotionsBuy?>?) {
        if (list != null && list.isNotEmpty()) {
            val thisTime = System.currentTimeMillis()
            for (promotionsBuy in list) {
                promotionsBuy?.systemTime = thisTime
            }
        }
        adapter?.setLoseTime(0)
        if (currentPage == 1) {
            adapter?.data = (list)
        } else {
            adapter?.addAll(list)
        }
        adapter?.notifyDataSetChanged()
        listRefreshTime = SystemClock.elapsedRealtime()
        checkListAddTimer()
    }

    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null

    override fun onBuy(promotions: PromotionsBuy?) {
        promotions?.let {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.PROMOTIONS_BUY, promotions)
            BlackRouter.getInstance().build(RouterConstData.PROMOTIONS_BUY_DETAIL).with(bundle).go(this)
        }
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (refreshCountdown()) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun checkListAddTimer() { //查询是否存在募集中的项目，如果存在，需要开启定时器，刷新倒计时
        checkStatusChanged()
        val hasNew = checkNeedTimer()
        if (hasNew) {
            timerCommand = TimerCommand()
            handler.post(timerCommand)
        }
    }

    //刷新倒计时
    private fun checkNeedTimer(): Boolean {
        var hasNew = false
        val list = adapter?.data
        if (list != null) {
            for (i in list.indices) {
                val promotions = list[i]
                if (promotions is PromotionsBuy) {
                    val statusCode = promotions.statusCode
                    if (statusCode == 2 || statusCode == 3) {
                        continue
                    }
                    if (statusCode == 0) {
                        //未开始判断开始时间和当前时间
                        val startTime = promotions.startTime ?: 0
                        val thisTime = (promotions.systemTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (startTime > thisTime) {
                            hasNew = true
                            break
                        }
                    }
                    if (statusCode == 1) {
                        //未开始判断开始时间和当前时间
                        val endTime = promotions.endTime ?: 0
                        val thisTime = (promotions.systemTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (endTime > thisTime) {
                            hasNew = true
                            break
                        }
                    }
                }
            }
        }
        return hasNew
    }

    //刷新倒计时
    private fun checkStatusChanged(): List<Int> {
        val list = adapter?.data
        val updatePositions: MutableList<Int> = ArrayList()
        if (list != null) {
            for (i in list.indices) {
                val promotions = list[i]
                if (promotions is PromotionsBuy) {
                    var statusCode = promotions.statusCode
                    if (statusCode == 2 || statusCode == 3) {
                        continue
                    }
                    updatePositions.add(i)
                    if (statusCode == 0) {
                        //未开始判断开始时间和当前时间
                        val startTime = promotions.startTime ?: 0
                        val thisTime = (promotions.systemTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (startTime <= thisTime) {
                            promotions.status = 1
                            statusCode = 1
                        }
                    }
                    if (statusCode == 1) {
                        //未开始判断开始时间和当前时间
                        val endTime = promotions.endTime ?: 0
                        val thisTime = (promotions.systemTime ?: 0) + (adapter?.getLoseTime()
                                ?: 0)
                        if (endTime <= thisTime) {
                            promotions.status = 2
                        }
                    }
                }
            }
        }
        return updatePositions
    }

    //刷新倒计时
    private fun refreshCountdown(): Boolean {
        val loseTime = SystemClock.elapsedRealtime() - listRefreshTime
        adapter?.setLoseTime(loseTime)
        val updatePositions = checkStatusChanged()
        if (updatePositions.isNotEmpty()) {
            for (i in updatePositions) {
                adapter?.notifyItemChanged(i, PromotionsBuyListAdapter.STATUS)
            }
        }
        val check = checkNeedTimer()
        if (!check) {
            if (timerCommand != null) {
                handler.removeCallbacks(timerCommand)
                timerCommand = null
            }
        }
        return check
    }
}
