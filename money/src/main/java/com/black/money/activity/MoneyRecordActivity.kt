package com.black.money.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.filter.FilterEntity
import com.black.base.lib.filter.FilterResult
import com.black.base.lib.filter.FilterWindow
import com.black.base.model.HttpRequestResultData
import com.black.base.model.MenuEntity
import com.black.base.model.NormalCallback
import com.black.base.model.filter.CoinFilter
import com.black.base.model.filter.DemandRecordStatus
import com.black.base.model.filter.RegularRecordStatus
import com.black.base.model.money.Demand
import com.black.base.model.money.DemandConfig
import com.black.base.model.money.Regular
import com.black.base.model.money.RegularConfig
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.view.MenuChoosePopup
import com.black.base.view.MenuChoosePopup.OnMenuChooseListener
import com.black.money.R
import com.black.money.databinding.ActivityMoneyRecordBinding
import com.black.money.fragment.DemandRecordFragment
import com.black.money.fragment.RegularRecordFragment
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.MONEY_RECORD], beforePath = RouterConstData.LOGIN)
class MoneyRecordActivity : BaseActionBarActivity(), View.OnClickListener, OnMenuChooseListener {
    companion object {
        private const val MENU_FILTER = "1"
        private const val MENU_CHANGE_OUT_ALL = "2"
        private const val MENU_REWORD = "3"
    }

    private var binding: ActivityMoneyRecordBinding? = null

    private var currentTab = 0
    private var fManager: FragmentManager? = null
    private var demandRecordFragment: DemandRecordFragment? = null
    private var regularRecordFragment: RegularRecordFragment? = null

    var demand: Demand? = null
        private set
    private var demandCoins: ArrayList<String?>? = null
    var demandCoinFilter: CoinFilter? = null
        private set
    var demandRecordStatus: DemandRecordStatus? = null
        private set

    var regular: Regular? = null
        private set
    private var regularCoins: ArrayList<String?>? = null
    var regularCoinFilter: CoinFilter? = null
        private set
    var regularRecordStatus: RegularRecordStatus? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentTab = intent.getIntExtra(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_DEMAND)
        demandCoins = intent.getStringArrayListExtra(ConstData.DEMAND_COINS)
        demand = intent.getParcelableExtra(ConstData.DEMAND)
        if (demand != null) {
            demandCoinFilter = CoinFilter(demand!!.coinType, demand!!.coinType)
        } else {
            if (demandCoins != null && demandCoins!!.isNotEmpty()) {
                val firstCoin = demandCoins!![0]
                demandCoinFilter = CoinFilter(firstCoin, firstCoin)
            }
        }
        demandRecordStatus = intent.getParcelableExtra(ConstData.DEMAND_STATUS)
        if (demandRecordStatus == null) {
            demandRecordStatus = DemandRecordStatus.INTO
        }
        regularCoins = intent.getStringArrayListExtra(ConstData.REGULAR_COINS)
        regular = intent.getParcelableExtra(ConstData.REGULAR)
        if (regular != null) {
            regularCoinFilter = CoinFilter(regular!!.coinType, regular!!.coinType)
        } else {
            if (regularCoins != null && regularCoins!!.isNotEmpty()) {
                val firstCoin = regularCoins!![0]
                regularCoinFilter = CoinFilter(firstCoin, firstCoin)
            }
        }
        regularRecordStatus = intent.getParcelableExtra(ConstData.REGULAR_STATUS)
        if (regularRecordStatus == null) {
            regularRecordStatus = RegularRecordStatus.INTO
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_money_record)
        binding?.tabDemand?.setOnClickListener(this)
        binding?.tabRegular?.setOnClickListener(this)
        binding?.filterLayout?.setOnClickListener(this)
        fManager = supportFragmentManager
        selectTab(currentTab)
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        demandConfig
        regularConfig
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.tab_demand) {
            selectTab(ConstData.TAB_DEMAND)
        } else if (id == R.id.tab_regular) {
            selectTab(ConstData.TAB_REGULAR)
        } else if (id == R.id.filter_layout) {
            if (currentTab == ConstData.TAB_DEMAND) {
                MenuChoosePopup(this, demandMenuEntityList, null)
                        .setOnMenuChooseListener(this)
                        .show(v)
            } else if (currentTab == ConstData.TAB_REGULAR) {
                openRegularFilterWindow()
            }
        }
    }

    override fun onMenuChoose(menuEntity: MenuEntity?) {
        if (menuEntity?.code == null) {
            return
        }
        when (menuEntity.code) {
            MENU_FILTER -> openDemandFilterWindow()
            MENU_CHANGE_OUT_ALL -> if (demandRecordFragment != null) {
                demandRecordFragment!!.changeOutALl()
            }
            MENU_REWORD -> BlackRouter.getInstance().build(RouterConstData.DEMAND_RECORD).go(this)
        }
    }

    private fun selectTab(tabIndex: Int) {
        if (currentTab != tabIndex) {
            currentTab = tabIndex
            onHeaderTabChanged()
        }
    }

    private fun onHeaderTabChanged() {
        binding?.tabDemand?.isChecked = currentTab == ConstData.TAB_DEMAND
        binding?.tabRegular?.isChecked = currentTab == ConstData.TAB_REGULAR
        if (currentTab == ConstData.TAB_DEMAND) {
            binding?.filterText?.text = ""
//            CommonUtil.setTextViewCompoundDrawable(binding?.filterText, SkinCompatResources.getDrawable(this, R.drawable.icon_money_more), 0)
            CommonUtil.setTextViewCompoundDrawable(binding?.filterText, null, 0)
            binding?.filterText?.setText(R.string.more)
        } else {
            CommonUtil.setTextViewCompoundDrawable(binding?.filterText, null, 0)
            binding?.filterText?.setText(R.string.filter_title)
        }
        refreshFragment()
    }

    //选择对应工作区
    private fun refreshFragment() {
        val transaction = fManager!!.beginTransaction()
        hideFragments(transaction)
        if (currentTab == ConstData.TAB_DEMAND) {
            if (demandRecordFragment == null || !demandRecordFragment!!.isAdded) {
                demandRecordFragment = DemandRecordFragment()
                demandRecordFragment!!.setDemand(demand)
                demandRecordFragment!!.setFilters(demandCoinFilter, demandRecordStatus)
                transaction.add(R.id.work_space, demandRecordFragment!!)
            } else {
                transaction.show(demandRecordFragment!!)
            }
        } else if (currentTab == ConstData.TAB_REGULAR) {
            if (regularRecordFragment == null || !regularRecordFragment!!.isAdded) {
                regularRecordFragment = RegularRecordFragment()
                regularRecordFragment!!.setRegular(regular)
                regularRecordFragment!!.setFilters(regularCoinFilter, regularRecordStatus)
                transaction.add(R.id.work_space, regularRecordFragment!!)
            } else {
                transaction.show(regularRecordFragment!!)
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        if (demandRecordFragment != null) {
            transaction.hide(demandRecordFragment!!)
        }
        if (regularRecordFragment != null) {
            transaction.hide(regularRecordFragment!!)
        }
    }

    private fun openDemandFilterWindow() {
        val showWindow = Runnable {
            val data: MutableList<FilterEntity<*>> = ArrayList()
            data.add(CoinFilter.getDefaultFilterEntity(demandCoins, demandCoinFilter))
            data.add(DemandRecordStatus.getDefaultFilterEntity(demandRecordStatus))
            FilterWindow(mContext as Activity, data)
                    .show(object : FilterWindow.OnFilterSelectListener {
                        override fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>) {
                            for (filterResult in selectResult) {
                                if (CoinFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    demandCoinFilter = filterResult.data as CoinFilter
                                } else if (DemandRecordStatus.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    demandRecordStatus = filterResult.data as DemandRecordStatus
                                }
                            }
                            if (demandRecordFragment != null) {
                                demandRecordFragment!!.setFilters(demandCoinFilter, demandRecordStatus)
                            }
                        }

                    })
        }
        showWindow.run()
    }

    private fun openRegularFilterWindow() {
        val showWindow = Runnable {
            val data: MutableList<FilterEntity<*>> = ArrayList()
            data.add(CoinFilter.getDefaultFilterEntity(regularCoins, regularCoinFilter))
            data.add(RegularRecordStatus.getDefaultFilterEntity(regularRecordStatus))
            FilterWindow(mContext as Activity, data)
                    .show(object : FilterWindow.OnFilterSelectListener {
                        override fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>) {
                            for (filterResult in selectResult) {
                                if (CoinFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    regularCoinFilter = filterResult.data as CoinFilter?
                                } else if (RegularRecordStatus.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    regularRecordStatus = filterResult.data as RegularRecordStatus?
                                }
                            }
                            if (regularRecordFragment != null) {
                                regularRecordFragment!!.setFilters(regularCoinFilter, regularRecordStatus)
                            }
                        }

                    })
        }
        showWindow.run()
    }

    private val demandMenuEntityList: MutableList<MenuEntity?>
        get() {
            val demandMenuEntityList: MutableList<MenuEntity?> = ArrayList()
            demandMenuEntityList.add(MenuEntity(MENU_FILTER, resources.getString(R.string.filter_title)))
            if (demandRecordStatus != null && TextUtils.equals(demandRecordStatus!!.code, DemandRecordStatus.INTO.code)) {
                demandMenuEntityList.add(MenuEntity(MENU_CHANGE_OUT_ALL, "全部转出"))
            }
            demandMenuEntityList.add(MenuEntity(MENU_REWORD, "收益记录"))
            return demandMenuEntityList
        }

    private val demandConfig: Unit
        get() {
            MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    refreshDemandConfig(null)
                }

                override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        refreshDemandConfig(returnData.data)
                    } else {
                        refreshDemandConfig(null)
                    }
                }
            })
        }

    private fun refreshDemandConfig(demandConfig: DemandConfig?) {
        if (demandCoinFilter != null || demandConfig == null || demandConfig.coinTypeConf == null) {
            return
        }
        val demands = demandConfig.coinTypeConf!!
        if (demands.isNotEmpty()) {
            demandCoins = ArrayList()
            for (temp in demands) {
                if (temp?.status != null && true == temp.status && !demandCoins!!.contains(temp.coinType)) {
                    demandCoins!!.add(temp.coinType)
                }
            }
            val demand = demands[0]
            demandCoinFilter = CoinFilter(demand?.coinType, demand?.coinType)
            if (demandRecordFragment != null) {
                demandRecordFragment!!.setFilters(demandCoinFilter, demandRecordStatus)
            }
        }
    }

    private val regularConfig: Unit
        get() {
            MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    refreshRegularConfig(null)
                }

                override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        refreshRegularConfig(returnData.data)
                    } else {
                        refreshRegularConfig(null)
                    }
                }
            })
        }

    private fun refreshRegularConfig(regularConfig: RegularConfig?) {
        if (regularCoinFilter != null || regularConfig == null || regularConfig.coinTypeConf == null) {
            return
        }
        val regulars = regularConfig.coinTypeConf!!
        if (regulars.isNotEmpty()) {
            regularCoins = ArrayList()
            for (temp in regulars) {
                if (temp?.status != null && temp.status != 0 && !regularCoins!!.contains(temp.coinType)) {
                    regularCoins!!.add(temp.coinType)
                }
            }
            val regular = regulars[0]
            regularCoinFilter = CoinFilter(regular?.coinType, regular?.coinType)
            if (regularRecordFragment != null) {
                regularRecordFragment!!.setFilters(regularCoinFilter, regularRecordStatus)
            }
        }
    }
}