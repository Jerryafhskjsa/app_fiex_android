package com.black.frying.contract

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.ContractRecordTabBean
import com.black.base.util.ConstData
import com.black.base.util.StyleChangeUtil
import com.black.frying.FryingApplication
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.utils.TransRecordFragmentPagerAdapter
import com.black.frying.contract.utils.replaceTransactionFragment
import com.black.frying.contract.viewmodel.FuturesTitleViewModel
import com.black.frying.contract.viewmodel.FuturesViewModel
import com.black.frying.contract.viewmodel.model.display
import com.black.frying.fragment.ContractPlanTabFragment
import com.black.frying.fragment.ContractPositionTabFragment
import com.black.frying.fragment.ContractProfitTabFragment
import com.black.lib.refresh.QRefreshLayout
import com.black.net.okhttp.NetWorkChangeHelper
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.futures_layout_title_bar.*
import java.math.BigDecimal

class FuturesFragment : Fragment() {

    companion object {
        fun newInstance() = FuturesFragment()
    }

    private val viewModel: FuturesViewModel by lazy { ViewModelProvider(this)[FuturesViewModel::class.java] }
    private val globalViewModel: FutureGlobalStateViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        )[FutureGlobalStateViewModel::class.java]
    }
    private val futuresTitleViewModel: FuturesTitleViewModel by lazy {
        ViewModelProvider(this)[FuturesTitleViewModel::class.java].apply {
            globalStateViewModel = globalViewModel
        }
    }
    private val transactionInfoFragment = FuturesTransactionInfoFragment.newInstance()

    private var mTransRecordAdapter: TransRecordFragmentPagerAdapter? = null
    private val _binding: FragmentLayoutFuturesBinding by lazy {
        FragmentLayoutFuturesBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return _binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NetWorkChangeHelper.re(FryingApplication.instance())
        // TODO: Use the ViewModel
        futuresTitleViewModel.loadCoinInfo()

    }

    override fun onDestroy() {
        super.onDestroy()
        NetWorkChangeHelper.unre(FryingApplication.instance())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildFuturesTitleBar()
        replaceTransactionFragment(
            R.id.futuresTransactionInfoLayout,
            transactionInfoFragment,
            FuturesTransactionInfoFragment.TAG
        )
        buildTransRecordView()
        buildRefreshAction()
        globalViewModel.printThis()
    }

    private fun buildRefreshAction() {
        _binding.refreshLayout.apply {
            setRefreshHolder(RefreshHolderFrying(requireActivity()))
            setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
                override fun onRefresh() {
                    globalViewModel.refresh()
                    postDelayed(
                        { setRefreshing(false) },
                        300
                    )
                }
            })
            //notify item refresh
        }
    }

    private fun buildTransRecordView() {
        val futuresTabLayout = _binding.futuresTabLayout
        val positionsTab = futuresTabLayout.newTab().apply { text = "tab1" }
        val profitLossTab = futuresTabLayout.newTab().apply { text = "tab2" }
        val openOrderTab = futuresTabLayout.newTab().apply { text = "tab3" }


        val listOfFragment = mutableListOf<Fragment>()
        val list = mutableListOf<ContractRecordTabBean>()
        val positionsTabBean = ContractRecordTabBean()
        val profitLossTabBean = ContractRecordTabBean()
        val openOrderTabBean = ContractRecordTabBean()
        positionsTabBean.amount = 0
        positionsTabBean.name =
            getString(R.string.contract_record_tab1, positionsTabBean.amount.toString())
        positionsTabBean.type = ConstData.CONTRACT_REC_HOLD_AMOUNT
        profitLossTabBean.amount = 0
        profitLossTabBean.name =
            getString(R.string.contract_record_tab2, profitLossTabBean.amount.toString())
        profitLossTabBean.type = ConstData.CONTRACT_REC_WITH_LIMIE
        openOrderTabBean.amount = 0
        openOrderTabBean.name =
            getString(R.string.contract_record_tab3, openOrderTabBean.amount.toString())
        openOrderTabBean.type = ConstData.CONTRACT_REC_CURRENT
        list.add(positionsTabBean)
        list.add(profitLossTabBean)
        list.add(openOrderTabBean)
        listOfFragment.add(FuturePositionTabFragment.newInstance(positionsTabBean))
        listOfFragment.add(ContractProfitTabFragment.newInstance(profitLossTabBean))
        listOfFragment.add(ContractPlanTabFragment.newInstance(openOrderTabBean))
        val pagerAdapter = TransRecordFragmentPagerAdapter(listOfFragment, childFragmentManager)
        mTransRecordAdapter = pagerAdapter

        _binding.futuresRecordViewPager.apply {
            adapter = mTransRecordAdapter
        }
        futuresTabLayout.setupWithViewPager(_binding.futuresRecordViewPager)

        futuresTabLayout.removeAllTabs()
        futuresTabLayout.addTab(positionsTab)
        futuresTabLayout.addTab(profitLossTab)
        futuresTabLayout.addTab(openOrderTab)

        _binding.futuresAppbarView.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appbar: AppBarLayout?, verticalOffset: Int) {
                _binding.refreshLayout.isEnabled = verticalOffset >= 0
            }

        })
    }


    override fun onPause() {
        super.onPause()
        futuresTitleViewModel.onPause()
    }

    override fun onResume() {
        super.onResume()
        futuresTitleViewModel.onResume()
    }

    private fun buildFuturesTitleBar() {
        val futureTitleBar = _binding.futuresTitleBarRoot
        futureTitleBar.apply {
            futuresCollectCoin.setOnClickListener {
                futuresTitleViewModel.autoCollectCoin()
            }
            futuresTitleBar.setOnClickListener {
                // TODO: 选择币种
                futuresTitleViewModel.goToSelectOtherCoin(requireActivity())
            }
            futuresCoinCharts.setOnClickListener {
                //todo k线详情
                futuresTitleViewModel.goToKlineDetail(requireActivity())
            }
            futuresTransactionMore.setOnClickListener {
                //todo 更多设置
//                viewModel.startConnect()
            }
        }
        globalViewModel.pairQuotationLiveData.observe(viewLifecycleOwner) { pairQuotation ->
            pairQuotation?.let { info ->
                updatePriceSince(info.pricePercent(), futureTitleBar.futuresTitleBarPriceSince)
            }
        }
        globalViewModel.symbolBeanLiveData.observe(viewLifecycleOwner) { bean ->
            futureTitleBar.let { root ->
                root.futuresTitleBarTitle.text = bean?.display() ?: "--"
            }
        }
        futuresTitleViewModel.isCollectLiveData.observe(viewLifecycleOwner){isCollect ->
            futureTitleBar.futuresCollectCoin.setImageResource(if (isCollect) R.drawable.btn_collect_dis else R.drawable.btn_collect_default)
        }
    }

    private fun updatePriceSince(since: BigDecimal, sincePriceTv: TextView) {
        val background: Drawable?
        val color: Int
        val ctx = sincePriceTv.context
        val styleChange = StyleChangeUtil.getStyleChangeSetting(ctx)?.styleCode
        if (styleChange == 1) {
            if (since > BigDecimal.ZERO) {//涨
                background = ctx.getDrawable(R.drawable.trans_raise_bg_corner)
                color = ctx.getColor(R.color.T10)
            } else if (since < BigDecimal.ZERO) {
                background = ctx.getDrawable(R.drawable.trans_fall_bg_corner)
                color = ctx.getColor(R.color.T9)
            } else {
                background = ctx.getDrawable(R.drawable.trans_default_bg_corner)
                color = ctx.getColor(R.color.B3)
            }

        } else {
            if (since < BigDecimal.ZERO) {//跌
                background = ctx?.getDrawable(R.drawable.trans_raise_bg_corner)
                color = ctx.getColor(R.color.T10)
            } else if (since > BigDecimal.ZERO) {
                background = ctx?.getDrawable(R.drawable.trans_fall_bg_corner)
                color = ctx.getColor(R.color.T9)
            } else {
                background = ctx?.getDrawable(R.drawable.trans_default_bg_corner)
                color = ctx.getColor(R.color.B3)
            }
        }
        val result = NumberUtil.formatNumber2(since) + "%"
        sincePriceTv.text = result
        sincePriceTv.background = background
        sincePriceTv.setTextColor(color)
    }

}