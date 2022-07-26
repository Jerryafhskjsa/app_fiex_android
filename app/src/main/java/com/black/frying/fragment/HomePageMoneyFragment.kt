package com.black.frying.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.money.*
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.widget.ObserveScrollView
import com.black.frying.adapter.HomeMoneyCloudAdapter
import com.black.frying.adapter.HomeMoneyDemandAdapter
import com.black.frying.adapter.HomeMoneyLoanAdapter
import com.black.frying.adapter.HomeMoneyRegularAdapter
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageMoneyBinding
import com.google.gson.reflect.TypeToken
import skin.support.content.res.SkinCompatResources
import java.util.*

class HomePageMoneyFragment : BaseFragment(), View.OnClickListener, ObserveScrollView.ScrollListener, OnItemClickListener, CompoundButton.OnCheckedChangeListener {
    private var binding: FragmentHomePageMoneyBinding? = null

    private var btnNoticeImgDefault: Drawable? = null
    private var btnNoticeImgScroll: Drawable? = null

    private var demandAdapter: HomeMoneyDemandAdapter? = null
    private var demandConfig: DemandConfig? = null

    private var regularAdapter: HomeMoneyRegularAdapter? = null
    private var regularConfig: RegularConfig? = null

    private var loanAdapter: HomeMoneyLoanAdapter? = null
    private var loanConfigList: ArrayList<LoanConfig?>? = null

    private var cloudAdapter: HomeMoneyCloudAdapter? = null
    private var userInfo: UserInfo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_money, container, false)
        binding?.demandNotice?.setOnClickListener(this)
        btnNoticeImgDefault = SkinCompatResources.getDrawable(mContext, R.drawable.icon_demand_notice)
        btnNoticeImgScroll = SkinCompatResources.getDrawable(mContext, R.drawable.icon_mine_info)
        binding?.scrollView?.addScrollListener(this)
        StatusBarUtil.addStatusBarPadding(binding?.actionBarLayout)

        binding?.btnHomeMoneyDemand?.setOnClickListener(this)
        binding?.btnHomeMoneyRegular?.setOnClickListener(this)
        binding?.btnHomeMoneyLoan?.setOnClickListener(this)
        binding?.btnHomeMoneyPower?.setOnClickListener(this)
        initDemandViews()
        initRegularViews()
        initLoanViews()
        initCloudViews()
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.demand_notice -> {
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, "聚宝盆规则")
                bundle.putString(ConstData.URL, UrlConfig.getUrlDemandRule(mContext))
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(this)
            }
            R.id.btn_home_money_demand, R.id.demand_header -> {
                BlackRouter.getInstance().build(RouterConstData.DEMAND_HOME).go(this)
            }
            R.id.btn_home_money_regular, R.id.regular_header -> {
                BlackRouter.getInstance().build(RouterConstData.REGULAR_HOME).go(this)
            }
            R.id.btn_home_money_loan, R.id.loan_header -> {
                BlackRouter.getInstance().build(RouterConstData.LOAN_CONFIG).go(this)
            }
            R.id.btn_home_money_power, R.id.cloud_header -> BlackRouter.getInstance().build(RouterConstData.CLOUD_POWER_PROJECT).go(this)
        }
    }

    private fun setViewVisibility(view: View?, visible: Int) {
        if (view == null) {
            return
        }
        if (view.visibility != visible) {
            view.visibility = visible
        }
    }

    private fun openMoneyRecords(recordType: Int?) {
        fryingHelper.checkUserAndDoing(Runnable {
            val bundle = Bundle()
            if (recordType != null) {
                bundle.putInt(ConstData.MONEY_RECORD_TYPE, recordType)
            }
            if (demandConfig != null && demandConfig?.coinTypeConf != null) {
                val demandCoins = ArrayList<String>()
                for (demand in demandConfig?.coinTypeConf!!) {
                    demand?.coinType?.let {
                        if (!demandCoins.contains(it)) {
                            demandCoins.add(it)
                        }
                    }
                }
                bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
            }
            if (regularConfig != null && regularConfig?.coinTypeConf != null) {
                val regularCoins = ArrayList<String>()
                for (regular in regularConfig?.coinTypeConf!!) {
                    regular?.coinType?.let {
                        if (!regularCoins.contains(it)) {
                            regularCoins.add(it)
                        }
                    }
                }
                bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
            }
            BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(mFragment)
        }, DEMAND_INDEX)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (binding?.scrollView != null && binding?.actionBarLayout != null && binding?.actionBarLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY!!.toFloat() / binding?.actionBarLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            binding?.actionBarBg?.alpha = alpha
            binding?.actionBarTitle?.alpha = alpha
            resetHeaderButtonImages(alpha)
        }
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        if (recyclerView != null) {
            val id = recyclerView.id
            if (id == R.id.demand_recycler_view) {
                val goRunnable = Runnable {
                    val configDemand = demandAdapter?.getItem(position)
                    val bundle = Bundle()
                    var demand: Demand? = null
                    if (demandConfig != null && demandConfig?.coinTypeConf != null) {
                        val demandCoins = ArrayList<String>()
                        for (temp in demandConfig?.coinTypeConf!!) {
                            temp?.coinType?.let {
                                if (temp.status != null && true == temp.status && !demandCoins.contains(it)) {
                                    demandCoins.add(it)
                                    if (demand == null && TextUtils.equals(configDemand?.coinType, it)) {
                                        demand = temp
                                    }
                                }
                            }
                        }
                        bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
                    }
                    if (demand == null) {
                        FryingUtil.showToast(mContext, "初始化失败，请稍后重试！")
                    } else {
                        bundle.putParcelable(ConstData.DEMAND, demand)
                        BlackRouter.getInstance().build(RouterConstData.DEMAND_DETAIL).with(bundle).go(mFragment)
                    }
                }
                if (demandConfig == null) {
                    getDemandConfig(goRunnable)
                } else {
                    goRunnable.run()
                }
            } else if (id == R.id.regular_recycler_view) {
                BlackRouter.getInstance().build(RouterConstData.REGULAR_HOME).go(this)
            } else if (id == R.id.loan_recycler_view) {
                val goRunnable = Runnable {
                    val loan = loanAdapter?.getItem(position)
                    val bundle = Bundle()
                    bundle.putString(ConstData.COIN_TYPE, loan?.mortgageCoinType)
                    bundle.putParcelableArrayList(ConstData.LOAN_CONFIG_LIST, loanConfigList)
                    BlackRouter.getInstance().build(RouterConstData.LOAN_CREATE).with(bundle).go(mFragment)
                }
                if (loanConfigList == null) {
                    getLoanConfig(goRunnable)
                } else {
                    goRunnable.run()
                }
            } else if (id == R.id.cloud_recycler_view) {
                BlackRouter.getInstance().build(RouterConstData.CLOUD_POWER_PROJECT).go(this)
                //                Runnable goRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        MoneyHomeConfigCloud loan = cloudAdapter.getItem(position);
//                        Bundle bundle = new Bundle();
//                        bundle.putString(ConstData.COIN_TYPE, loan == null ? null : loan.mortgageCoinType);
//                        bundle.putParcelableArrayList(LOAN_CONFIG_LIST, loanConfigList);
//                        BlackRouter.getInstance().build(RouterConstData.LOAN_CREATE).with(bundle).go(mFragment);
//                    }
//                };
//                if (loanConfigList == null) {
//                    getLoanConfig(goRunnable);
//                } else {
//                    goRunnable.run();
//                }
            }
        }
    }

    private fun resetHeaderButtonImages(alpha: Float) {
        if (alpha <= 0) {
            binding?.demandNotice?.setImageDrawable(btnNoticeImgDefault)
        } else {
            binding?.demandNotice?.setImageDrawable(btnNoticeImgScroll)
        }
    }

    override fun doResetSkinResources() {
        super.doResetSkinResources()
        btnNoticeImgDefault = SkinCompatResources.getDrawable(mContext, R.drawable.icon_demand_notice)
        btnNoticeImgScroll = SkinCompatResources.getDrawable(mContext, R.drawable.icon_mine_info)
        if (binding?.scrollView != null && binding?.actionBarLayout != null && binding?.actionBarLayout?.height != 0) {
            var alpha = binding?.scrollView?.scrollY!!.toFloat() / binding?.actionBarLayout?.height!!
            alpha = if (alpha < 0) 0.toFloat() else if (alpha > 1) 1.toFloat() else alpha
            resetHeaderButtonImages(alpha)
        }
        binding?.demandRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        setRecyclerViewDecoration(binding?.demandRecyclerView, decoration)
        demandAdapter?.resetSkinResources()
        demandAdapter?.notifyDataSetChanged()
        binding?.regularRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        setRecyclerViewDecoration(binding?.regularRecyclerView, decoration)
        regularAdapter?.resetSkinResources()
        regularAdapter?.notifyDataSetChanged()
        binding?.loanRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        setRecyclerViewDecoration(binding?.loanRecyclerView, decoration)
        loanAdapter?.resetSkinResources()
        loanAdapter?.notifyDataSetChanged()
        binding?.cloudRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        setRecyclerViewDecoration(binding?.cloudRecyclerView, decoration)
        cloudAdapter?.resetSkinResources()
        cloudAdapter?.notifyDataSetChanged()
    }

    private fun setRecyclerViewDecoration(recyclerView: RecyclerView?, decoration: ItemDecoration?) {
        if (recyclerView != null && decoration != null) {
            val count = recyclerView.itemDecorationCount
            if (count > 0) {
                for (i in count - 1 downTo 0) {
                    recyclerView.removeItemDecorationAt(i)
                }
            }
            recyclerView.addItemDecoration(decoration)
        }
    }

    override fun onResume() {
        super.onResume()
        getUserInfo(object : Callback<UserInfo?>() {
            override fun error(type: Int, error: Any) {
                userInfo = null
            }

            override fun callback(returnData: UserInfo?) {
                userInfo = returnData
            }
        })
        getDemandConfig(null)
        getRegularConfig(null)
        getLoanConfig(null)
        moneyHomeConfig
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    }

    private fun initDemandViews() {
        binding?.demandHeader?.setOnClickListener(this)
        binding?.demandHeader?.visibility = View.GONE
        binding?.demandRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.demandRecyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.demandRecyclerView?.addItemDecoration(decoration)
        demandAdapter = HomeMoneyDemandAdapter(mContext!!, BR.listItemDemandHomeModel, null)
        demandAdapter?.setOnItemClickListener(this)
        binding?.demandRecyclerView?.adapter = demandAdapter
        binding?.demandRecyclerView?.isNestedScrollingEnabled = false
        binding?.demandRecyclerView?.setHasFixedSize(true)
        binding?.demandRecyclerView?.isFocusable = false
    }

    private fun initRegularViews() {
        binding?.regularHeader?.setOnClickListener(this)
        binding?.regularHeader?.visibility = View.GONE
        binding?.regularRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.regularRecyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.regularRecyclerView?.addItemDecoration(decoration)
        regularAdapter = HomeMoneyRegularAdapter(mContext!!, BR.listItemRegularHomeModel, null)
        regularAdapter?.setOnItemClickListener(this)
        binding?.regularRecyclerView?.adapter = regularAdapter
        binding?.regularRecyclerView?.isNestedScrollingEnabled = false
        binding?.regularRecyclerView?.setHasFixedSize(true)
        binding?.regularRecyclerView?.isFocusable = false
    }

    private fun initLoanViews() {
        binding?.loanHeader?.setOnClickListener(this)
        binding?.loanHeader?.visibility = View.GONE
        binding?.loanRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.loanRecyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.loanRecyclerView?.addItemDecoration(decoration)
        loanAdapter = HomeMoneyLoanAdapter(mContext!!, BR.listItemLoanHomeModel, null)
        loanAdapter?.setOnItemClickListener(this)
        binding?.loanRecyclerView?.adapter = loanAdapter
        binding?.loanRecyclerView?.isNestedScrollingEnabled = false
        binding?.loanRecyclerView?.setHasFixedSize(true)
        binding?.loanRecyclerView?.isFocusable = false
    }

    private fun initCloudViews() {
        binding?.cloudHeader?.setOnClickListener(this)
        binding?.cloudHeader?.visibility = View.GONE
        binding?.cloudRecyclerView?.background = SkinCompatResources.getDrawable(mContext, R.drawable.bg_b2_corner3)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.cloudRecyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.cloudRecyclerView?.addItemDecoration(decoration)
        cloudAdapter = HomeMoneyCloudAdapter(mContext!!, BR.listItemCloudPowerHomeModel, null)
        cloudAdapter?.setOnItemClickListener(this)
        binding?.cloudRecyclerView?.adapter = cloudAdapter
        binding?.cloudRecyclerView?.isNestedScrollingEnabled = false
        binding?.cloudRecyclerView?.setHasFixedSize(true)
        binding?.cloudRecyclerView?.isFocusable = false
    }

    private fun getDemandConfig(callback: Runnable?) {
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>() {
            override fun error(type: Int, error: Any) {
                refreshDemandConfig(null, callback)
            }

            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshDemandConfig(returnData.data, callback)
                } else {
                    refreshDemandConfig(null, callback)
                }
            }
        })
    }

    private fun refreshDemandConfig(demandConfig: DemandConfig?, callback: Runnable?) {
        this.demandConfig = demandConfig
        callback?.run()
    }

    private fun getRegularConfig(callback: Runnable?) {
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>() {
            override fun error(type: Int, error: Any) {
                refreshRegularConfig(null, callback)
            }

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshRegularConfig(returnData.data, callback)
                } else {
                    refreshRegularConfig(null, callback)
                }
            }
        })
    }

    private fun refreshRegularConfig(regularConfig: RegularConfig?, callback: Runnable?) {
        this.regularConfig = regularConfig
        callback?.run()
    }

    private fun getLoanConfig(callback: Runnable?) {
        MoneyApiServiceHelper.getLoanConfig(mContext, object : NormalCallback<HttpRequestResultDataList<LoanConfig?>?>() {
            override fun error(type: Int, error: Any) {
                refreshLoanConfig(null, callback)
            }

            override fun callback(returnData: HttpRequestResultDataList<LoanConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshLoanConfig(returnData.data, callback)
                } else {
                    refreshLoanConfig(null, callback)
                }
            }
        })
    }

    private fun refreshLoanConfig(loanConfigList: ArrayList<LoanConfig?>?, callback: Runnable?) {
        this.loanConfigList = loanConfigList
        callback?.run()
    }

    private val moneyHomeConfig: Unit
        get() {
            MoneyApiServiceHelper.getMoneyHomeConfig(mContext, object : NormalCallback<HttpRequestResultData<MoneyHomeConfig?>?>() {
                override fun callback(returnData: HttpRequestResultData<MoneyHomeConfig?>?) {
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showMoneyHomeConfig(returnData.data)
                    }
                }
            })
        }

    private fun showMoneyHomeConfig(moneyHomeConfig: MoneyHomeConfig?) {
        if (moneyHomeConfig != null) {
            val demandJson = moneyHomeConfig["pledgeCurrent"]
            var demands: ArrayList<MoneyHomeConfigDemand?>? = null
            try {
                demands = if (demandJson == null) null else gson.fromJson<ArrayList<MoneyHomeConfigDemand?>>(demandJson, object : TypeToken<ArrayList<MoneyHomeConfigDemand?>?>() {}.type)
            } catch (e: Exception) {
            }
            showMoneyHomeConfigDemand(demands)
            val regularJson = moneyHomeConfig["pledgeHedge"]
            var regulars: ArrayList<MoneyHomeConfigRegular?>? = null
            try {
                regulars = if (regularJson == null) null else gson.fromJson<ArrayList<MoneyHomeConfigRegular?>>(regularJson, object : TypeToken<ArrayList<MoneyHomeConfigRegular?>?>() {}.type)
            } catch (e: Exception) {
            }
            showMoneyHomeConfigRegular(regulars)
            val loanJson = moneyHomeConfig["borrowConf"]
            var loans: ArrayList<MoneyHomeConfigLoan?>? = null
            try {
                loans = if (loanJson == null) null else gson.fromJson<ArrayList<MoneyHomeConfigLoan?>>(loanJson, object : TypeToken<ArrayList<MoneyHomeConfigLoan?>?>() {}.type)
            } catch (e: Exception) {
            }
            showMoneyHomeConfigLoan(loans)
            val cloudJson = moneyHomeConfig["cloudMining"]
            var clouds: ArrayList<MoneyHomeConfigCloud?>? = null
            try {
                clouds = if (demandJson == null) null else gson.fromJson<ArrayList<MoneyHomeConfigCloud?>>(cloudJson, object : TypeToken<ArrayList<MoneyHomeConfigCloud?>?>() {}.type)
            } catch (e: Exception) {
            }
            showMoneyHomeConfigCloud(clouds)
        } else {
            showMoneyHomeConfigDemand(null)
            showMoneyHomeConfigRegular(null)
            showMoneyHomeConfigLoan(null)
            showMoneyHomeConfigCloud(null)
        }
    }

    private fun showMoneyHomeConfigDemand(moneyHomeConfigDemands: ArrayList<MoneyHomeConfigDemand?>?) {
        if (moneyHomeConfigDemands == null || moneyHomeConfigDemands.isEmpty()) {
            binding?.demandHeader?.visibility = View.GONE
        } else {
            binding?.demandHeader?.visibility = View.VISIBLE
        }
        demandAdapter?.data = moneyHomeConfigDemands
        demandAdapter?.notifyDataSetChanged()
    }

    private fun showMoneyHomeConfigRegular(moneyHomeConfigRegulars: ArrayList<MoneyHomeConfigRegular?>?) {
        if (moneyHomeConfigRegulars == null || moneyHomeConfigRegulars.isEmpty()) {
            binding?.regularHeader?.visibility = View.GONE
        } else {
            binding?.regularHeader?.visibility = View.VISIBLE
        }
        regularAdapter?.data = moneyHomeConfigRegulars
        regularAdapter?.notifyDataSetChanged()
    }

    private fun showMoneyHomeConfigLoan(moneyHomeConfigLoans: ArrayList<MoneyHomeConfigLoan?>?) {
        if (moneyHomeConfigLoans == null || moneyHomeConfigLoans.isEmpty()) {
            binding?.loanHeader?.visibility = View.GONE
        } else {
            binding?.loanHeader?.visibility = View.VISIBLE
        }
        loanAdapter?.data = moneyHomeConfigLoans
        loanAdapter?.notifyDataSetChanged()
    }

    private fun showMoneyHomeConfigCloud(moneyHomeConfigClouds: ArrayList<MoneyHomeConfigCloud?>?) {
        if (moneyHomeConfigClouds == null || moneyHomeConfigClouds.isEmpty()) {
            binding?.cloudHeader?.visibility = View.GONE
        } else {
            binding?.cloudHeader?.visibility = View.VISIBLE
        }
        cloudAdapter?.data = moneyHomeConfigClouds
        cloudAdapter?.notifyDataSetChanged()
    }
}