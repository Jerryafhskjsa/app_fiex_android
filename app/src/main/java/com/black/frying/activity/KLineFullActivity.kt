package com.black.frying.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.PairApiServiceHelper
import com.black.base.model.QuotationSet
import com.black.base.model.socket.KLineItem
import com.black.base.model.socket.PairStatus
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.StyleChangeUtil
import com.black.base.view.PairStatusPopupWindow
import com.black.base.view.PairStatusPopupWindow.OnPairStatusSelectListener
import com.black.base.widget.AnalyticChart
import com.black.base.widget.AnalyticChart.TimeStep
import com.black.frying.viewmodel.KLineFullViewModel
import com.black.frying.viewmodel.KLineFullViewModel.OnKLineFullListener
import com.black.router.annotation.Route
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityKLineFullBinding
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.K_LINE_FULL])
class KLineFullActivity : BaseActivity(), View.OnClickListener, OnKLineFullListener {
    private var binding: ActivityKLineFullBinding? = null
    private var viewModel: KLineFullViewModel? = null
    private var colorT7 = 0
    private var colorT5 = 0
    private var colorT3 = 0
    private var kLinePage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        colorT7 = SkinCompatResources.getColor(this, R.color.T7)
        colorT5 = SkinCompatResources.getColor(this, R.color.T5)
        colorT3 = SkinCompatResources.getColor(this, R.color.T3)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_k_line_full)
        viewModel = KLineFullViewModel(this, intent.getStringExtra(ConstData.PAIR), this)
        binding!!.coinTypeLayout.setOnClickListener(this)
        binding!!.btnClose.setOnClickListener(this)
        binding!!.analyticChart.setShowCount(96)
        binding!!.analyticChart.setType(AnalyticChart.BOLL or AnalyticChart.KDJ)
        binding!!.analyticChart.setAnalyticChartHelper(object : AnalyticChart.AnalyticChartHelper {
            override fun onLoadMore(page: Int) {
                var endTime = System.currentTimeMillis() - (binding?.analyticChart?.getTimeStep()?.value?.times(1000*
                        100
                ) ?: 0) * (page)
                var startTime = endTime - (binding?.analyticChart?.getTimeStep()?.value?.times(1000*100) ?: 0)
                startTime = Math.max(startTime, 1567296000)
                endTime = Math.max(endTime, 1567296000)
                if(page > 1){
                    binding?.analyticChart?.setLoadingMore(true)
                }
                viewModel?.getKLineDataFiex(binding?.analyticChart?.getTimeStepRequestStr(),page,startTime,endTime)
            }

        })
        selectKTab(binding!!.analyticChart.getTimeStep())
        binding!!.loadingLayout.visibility = View.GONE
        initQuotaControlViews()
        refreshQuotaControlViews(binding!!.analyticChart.getType())
        initTimeStepTab()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.coin_type_layout -> {
                mContext?.let {
                    PairApiServiceHelper.getTradeSetsLocal(it, true, object : Callback<ArrayList<QuotationSet?>?>() {
                        override fun callback(returnData: ArrayList<QuotationSet?>?) {
                            if (returnData != null) {
                                val dataType = PairStatus.NORMAL_DATA
                                PairStatusPopupWindow.getInstance(this@KLineFullActivity, PairStatusPopupWindow.TYPE_TRANSACTION or dataType, returnData)
                                    .show(object : OnPairStatusSelectListener {
                                        override fun onPairStatusSelected(pairStatus: PairStatus?) {
                                            if (pairStatus == null) {
                                                return
                                            }
                                            //交易对切换
                                            if (!TextUtils.equals(viewModel?.getCurrentPair(), pairStatus.pair)) { //清空价格，数量
                                                onPairStatusChanged(pairStatus)
                                            }
                                        }
                                    })
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                        }
                    })
                }
            }
            R.id.btn_close -> finish()
        }
        onQuotaControlViewClick(v)
    }

    override fun onResume() {
        super.onResume()
        listenKLineData()
        viewModel!!.getTradePairInfo()
        var endTime = System.currentTimeMillis() - (binding?.analyticChart?.getTimeStep()?.value?.times(1000*
                100
        ) ?: 0) * (kLinePage)
        var startTime = endTime - (binding?.analyticChart?.getTimeStep()?.value?.times(1000*100) ?: 0)
        startTime = startTime.coerceAtLeast(1567296000)
        endTime = endTime.coerceAtLeast(1567296000)
        viewModel!!.getKLineDataFiex(binding?.analyticChart?.getTimeStepRequestStr(),kLinePage,startTime,endTime)
    }

    public override fun getViewModel(): KLineFullViewModel {
        return viewModel!!
    }

    private fun onQuotaControlViewClick(view: View) {
        var isQuotaControlViewClick = false
        when (view.id) {
            R.id.ma -> {
                isQuotaControlViewClick = true
                binding!!.ma.isChecked = true
                binding!!.boll.isChecked = false
                binding!!.mainHidden.isChecked = true
            }
            R.id.boll -> {
                isQuotaControlViewClick = true
                binding!!.ma.isChecked = false
                binding!!.boll.isChecked = true
                binding!!.mainHidden.isChecked = true
            }
            R.id.main_hidden -> {
                isQuotaControlViewClick = true
                if (binding!!.mainHidden.isChecked) {
                    binding!!.ma.isChecked = false
                    binding!!.boll.isChecked = false
                    binding!!.mainHidden.isChecked = false
                } else {
                    return
                }
            }
            R.id.macd -> {
                isQuotaControlViewClick = true
                binding!!.macd.isChecked = true
                binding!!.kdj.isChecked = false
                binding!!.rsi.isChecked = false
                binding!!.wr.isChecked = false
                binding!!.subHidden.isChecked = true
            }
            R.id.kdj -> {
                isQuotaControlViewClick = true
                binding!!.macd.isChecked = false
                binding!!.kdj.isChecked = true
                binding!!.rsi.isChecked = false
                binding!!.wr.isChecked = false
                binding!!.subHidden.isChecked = true
            }
            R.id.rsi -> {
                isQuotaControlViewClick = true
                binding!!.macd.isChecked = false
                binding!!.kdj.isChecked = false
                binding!!.rsi.isChecked = true
                binding!!.wr.isChecked = false
                binding!!.subHidden.isChecked = true
            }
            R.id.wr -> {
                isQuotaControlViewClick = true
                binding!!.macd.isChecked = false
                binding!!.kdj.isChecked = false
                binding!!.rsi.isChecked = false
                binding!!.wr.isChecked = true
                binding!!.subHidden.isChecked = true
            }
            R.id.sub_hidden -> {
                isQuotaControlViewClick = true
                if (binding!!.subHidden.isChecked) {
                    binding!!.macd.isChecked = false
                    binding!!.kdj.isChecked = false
                    binding!!.rsi.isChecked = false
                    binding!!.wr.isChecked = false
                    binding!!.subHidden.isChecked = false
                } else {
                    return
                }
            }
        }
        if (isQuotaControlViewClick) {
            binding!!.analyticChart.setType(quotaSelectedType)
        }
    }

    private fun initQuotaControlViews() {
        binding!!.ma.setOnClickListener(this)
        binding!!.boll.setOnClickListener(this)
        binding!!.mainHidden.setOnClickListener(this)
        binding!!.macd.setOnClickListener(this)
        binding!!.kdj.setOnClickListener(this)
        binding!!.rsi.setOnClickListener(this)
        binding!!.wr.setOnClickListener(this)
        binding!!.subHidden.setOnClickListener(this)
    }

    private fun refreshQuotaControlViews(type: Int) {
        binding!!.ma.isChecked = type and AnalyticChart.MA == AnalyticChart.MA
        binding!!.boll.isChecked = type and AnalyticChart.BOLL == AnalyticChart.BOLL
        binding!!.mainHidden.isChecked = type and AnalyticChart.MAIN_HIDDEN != AnalyticChart.MAIN_HIDDEN
        binding!!.macd.isChecked = type and AnalyticChart.MACD == AnalyticChart.MACD
        binding!!.kdj.isChecked = type and AnalyticChart.KDJ == AnalyticChart.KDJ
        binding!!.rsi.isChecked = type and AnalyticChart.RSI == AnalyticChart.RSI
        binding!!.wr.isChecked = type and AnalyticChart.WR == AnalyticChart.WR
        binding!!.subHidden.isChecked = type and AnalyticChart.SUB_HIDDEN != AnalyticChart.SUB_HIDDEN
    }

    private val quotaSelectedType: Int
        get() {
            var type = 0
            if (binding!!.ma.isChecked) {
                type = type or AnalyticChart.MA
            }
            if (binding!!.boll.isChecked) {
                type = type or AnalyticChart.BOLL
            }
            if (!binding!!.mainHidden.isChecked) {
                type = type or AnalyticChart.MAIN_HIDDEN
            }
            if (binding!!.macd.isChecked) {
                type = type or AnalyticChart.MACD
            }
            if (binding!!.kdj.isChecked) {
                type = type or AnalyticChart.KDJ
            }
            if (binding!!.rsi.isChecked) {
                type = type or AnalyticChart.RSI
            }
            if (binding!!.wr.isChecked) {
                type = type or AnalyticChart.WR
            }
            if (!binding!!.subHidden.isChecked) {
                type = type or AnalyticChart.SUB_HIDDEN
            }
            return type
        }

    private fun initTimeStepTab() {
        val pubSteps = arrayOf(TimeStep.NONE, TimeStep.MIN_1, TimeStep.MIN_5, TimeStep.MIN_15, TimeStep.MIN_30, TimeStep.HOUR_1, TimeStep.HOUR_4, TimeStep.DAY_1, TimeStep.WEEK_1)
        val timeStepTab: TabLayout = findViewById(R.id.k_time_step_tab)
        timeStepTab.setSelectedTabIndicatorHeight(0)
        timeStepTab.tabMode = TabLayout.MODE_FIXED
        for (i in pubSteps.indices) {
            val timeStep = pubSteps[i]
            val tab = timeStepTab.newTab().setText(timeStep.toString())
            tab.tag = timeStep
            tab.setCustomView(R.layout.view_k_time_step_tab)
            val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
            textView.text = timeStep.toString()
            timeStepTab.addTab(tab)
        }
        timeStepTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as TimeStep?
                selectKTab(timeStep)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val tab = timeStepTab.getTabAt(3)
        tab?.select()
    }

    private fun onPairStatusChanged(pairStatus: PairStatus) {
        viewModel!!.changePairStatus(pairStatus)
        selectKTab(binding!!.analyticChart.getTimeStep())
        listenKLineData()
    }

    private fun selectKTab(timeStep: TimeStep?) {
        if (timeStep == null) {
            return
        }
        if (binding?.analyticChart?.getTimeStep() != timeStep) {
            binding?.analyticChart?.setTimeStep(timeStep)
            listenKLineData()
            var endTime = System.currentTimeMillis() - (binding?.analyticChart?.getTimeStep()?.value?.times(
                1000*100
            ) ?: 0) * kLinePage
            var startTime = endTime - (binding?.analyticChart?.getTimeStep()?.value?.times(1000*100) ?: 0)
            startTime = Math.max(startTime, 1567296000)
            endTime = Math.max(endTime, 1567296000)
            viewModel!!.getKLineDataFiex(binding?.analyticChart?.getTimeStepRequestStr(),kLinePage,startTime,endTime)
        }
    }

    private fun listenKLineData() {
        binding?.loadingLayout?.visibility = View.VISIBLE
        binding?.analyticChart?.let { viewModel!!.listenKLineData(it!!) }
    }

    private fun refreshKLineChart(kLineItems: ArrayList<KLineItem?>) {
        try {
            binding!!.analyticChart.setData(kLineItems)
            binding!!.analyticChart.invalidate()
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    override fun onPairStatusDataChanged(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            finish()
            return
        }
        //刷新交易对信息
        binding!!.price.setText(pairStatus.currentPriceFormat)
        binding!!.priceCny.setText(pairStatus.currentPriceCNYFormat)
        binding!!.percentage.setText(pairStatus.priceChangeSinceTodayDisplay)
        var stylechange = StyleChangeUtil.getStyleChangeSetting(mContext)?.styleCode
        if (pairStatus.priceChangeSinceToday != null && pairStatus.priceChangeSinceToday == 0.0) {
            binding!!.price.setTextColor(colorT3)
            binding!!.priceCny.setTextColor(colorT3)
        } else if (pairStatus.priceChangeSinceToday!! > 0 && stylechange == 0) {
            binding!!.price.setTextColor(colorT7)
            binding!!.priceCny.setTextColor(colorT7)
        } else {
            binding!!.price.setTextColor(colorT5)
            binding!!.priceCny.setTextColor(colorT5)
        }
        binding!!.high.setText(pairStatus.maxPriceFormat)
        binding!!.low.setText(pairStatus.minPriceFormat)
        binding!!.volumeH24.setText(pairStatus.totalAmountFromat)
        binding!!.analyticChart.setCurrentPrice(pairStatus.currentPrice)
    }

    override fun onKLineDataAll(items: ArrayList<KLineItem?>) {
        binding?.loadingLayout?.visibility = View.GONE
        if(items.isNotEmpty()){
            refreshKLineChart(items)
        }
    }

    override fun onKLineDataAdd(item: KLineItem) {
        if(item != null){
            binding?.analyticChart?.addData(item)
            binding?.analyticChart?.postInvalidate()
        }
    }

    override fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>) {
        binding?.analyticChart?.setLoadingMore(false)
        if(items.isNotEmpty()){
            binding?.analyticChart?.addDataList(kLinePage, items)
            binding?.analyticChart?.postInvalidate()
        }
    }

    override fun onKLineLoadingMore() {
        binding?.analyticChart?.setLoadingMore(false)
    }

    override fun onPairChanged(pair: String?) {
        binding!!.coinType.setText(pair?.replace("_", "/") ?: "NULL")
    }

    override fun onPairStatusPrecision(precision: Int) {
        binding!!.analyticChart.setPrecision(precision)
    }
}