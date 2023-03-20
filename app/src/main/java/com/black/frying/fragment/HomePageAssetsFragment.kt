package com.black.frying.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.Money
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.TigerWallet
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLever
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.base.viewmodel.BaseViewModel
import com.black.frying.fragment.assets.AssetsContractFragment
import com.black.frying.fragment.assets.AssetsFinanceFragment
import com.black.frying.fragment.assets.AssetsSpotFragment
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.black.wallet.fragment.WalletLeverFragment
import com.black.wallet.viewmodel.WalletViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import kotlin.math.abs
import com.fbsex.exchange.databinding.FragmentHomePageAssetsBinding
import com.black.frying.fragment.assets.AssetsWalletFragment

class HomePageAssetsFragment : BaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener,
    WalletViewModel.OnWalletModelListener, AssetsSpotFragment.EventResponseListener,
    AssetsContractFragment.ContractEventResponseListener,
    AssetsFinanceFragment.FinanceEventResponseListener,
    AssetsWalletFragment.WalletEventResponseListener{
    companion object {
        private const val TYPE_CNY = "CNY"
        private const val TYPE_BTC = "BTC"
        private val TAB_TITLES = arrayOfNulls<String>(5) //标题
        private var TAB_NORMAL: String? = null
        private var TAB_CONTRACT: String? = null
        private var TAB_FINANCE: String? = null
        private var TAB_WALLET: String? = null
    }
    private var bgB2 = 0
    private var bgDefault: Int? = null
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var rate = C2CApiServiceHelper.coinUsdtPrice?.usdt
    private var colorDefault = 0
    private var colorT1: Int = 0

    private var appBarLayout: AppBarLayout? = null

    private var otherMoneyType = TYPE_CNY
    private var typeList: MutableList<String>? = null
    var layout: FrameLayout? = null
    var binding:FragmentHomePageAssetsBinding? = null

    private var viewModel: WalletViewModel? = null

    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var normalFragment: AssetsSpotFragment? = null
    private var walletFragment: AssetsWalletFragment? = null
    private var contractFragment: AssetsContractFragment? = null
    private var assetsContractFragment: EmptyFragment? = null
    private var assetsFinanceFragment: EmptyFragment? = null
    private var assetsWalletFragment: EmptyFragment? = null
//    private var assetsContractFragment: AssetsContractFragment? = null
//    private var assetsFinanceFragment: AssetsFinanceFragment? = null
//    private var assetsWalletFragment: AssetsWalletFragment? = null

    private var leverFragment: WalletLeverFragment? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        bgB2 = SkinCompatResources.getColor(mContext, R.color.B2)
        bgDefault = R.drawable.bg_assets_header
        btnBackDefault = SkinCompatResources.getDrawable(mContext, R.drawable.btn_back_white)
        btnBackNormal = SkinCompatResources.getDrawable(mContext, R.drawable.btn_back)
        colorDefault = SkinCompatResources.getColor(mContext, R.color.white)
        colorT1 = SkinCompatResources.getColor(mContext, R.color.T1)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home_page_assets,container,false)
        layout = binding?.root as FrameLayout

        StatusBarUtil.addStatusBarPadding(layout)
        appBarLayout = binding?.root?.findViewById(R.id.app_bar_layout)
        appBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
            if (abs(verticalOffset) >= appBar.totalScrollRange) {
                binding?.root?.setBackgroundColor(bgB2)
            } else {
                binding?.root?.setBackgroundResource(bgDefault!!)
            }
        })

        viewModel = WalletViewModel(mContext!!,this)
        binding?.btnWalletEye?.isChecked = true
        binding?.btnWalletEye?.setOnCheckedChangeListener(this)
        binding?.linExchange?.setOnClickListener(this)
        binding?.linWithdraw?.setOnClickListener(this)
        binding?.linTransfer?.setOnClickListener(this)

        typeList = ArrayList()
        typeList!!.add(TYPE_CNY)
        typeList!!.add(TYPE_BTC)

        getString(R.string.wallet_account).also {
            TAB_WALLET = it
            TAB_TITLES[0] = TAB_WALLET
        }
        getString(R.string.spot_account).also {
            TAB_NORMAL = it
            TAB_TITLES[1] = TAB_NORMAL
        }
        getString(R.string.contract_account).also {
            TAB_CONTRACT = it
            TAB_TITLES[2] = TAB_CONTRACT
        }

        getString(R.string.finance_account).also {
            TAB_FINANCE = it
            TAB_TITLES[3] = TAB_FINANCE
        }
        getString(R.string.capital_account).also {
            TAB_FINANCE = it
            TAB_TITLES[4] = TAB_FINANCE
        }


        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE

        initFragmentList()

        binding?.viewPager?.adapter = object : FragmentStatePagerAdapter(mContext!!.supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

            }
        }
        binding?.tabLayout?.setupWithViewPager(binding?.viewPager, true)

        for (i in TAB_TITLES.indices) {
            try {
                val tab: TabLayout.Tab? = binding?.tabLayout?.getTabAt(i)
                if (tab != null) {
                    tab.setCustomView(R.layout.view_tab_normal)
                    if (tab.customView != null) {
                        val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                        textView.text = TAB_TITLES[i]
                    }
                }
            } catch (throwable: Throwable) {
                FryingUtil.printError(throwable)
            }
        }
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lin_exchange -> {
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN_RECHARGE)
                    .go(this)
            }
            R.id.lin_withdraw -> {
                val extras = Bundle()
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN_WITHDRAW)
                    .go(this)
            }
            R.id.lin_transfer -> {
                BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)
            }
            /*R.id.money_cny -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.price_unit), otherMoneyType, typeList, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        otherMoneyType = item
                        doTypeChange()
                    }

                }).show()
            }*/
           }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.CHOOSE_COIN_RECHARGE -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        val bundle = Bundle()
                        bundle.putParcelable(ConstData.WALLET, chooseWallet)
                        BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(this) { _, error ->
                            if (error != null) {
                                CommonUtil.printError(mContext, error)
                            }
                        }
                    }
                }
                ConstData.CHOOSE_COIN_WITHDRAW -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                            val bundle = Bundle()
                            bundle.putParcelable(ConstData.WALLET, chooseWallet)
                            BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this){ _, error ->
                                if (error != null) {
                                    CommonUtil.printError(mContext, error)
                                }
                            }
                    }
                }
                ConstData.LEVER_PAIR_CHOOSE -> {
                    val pair = data?.getStringExtra(ConstData.PAIR)
                    if (pair != null) {
                        FryingUtil.checkAndAgreeLeverProtocol(mContext!!, Runnable {
                            val bundle = Bundle()
                            bundle.putString(ConstData.PAIR, pair)
                            BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER).with(bundle).go(this)
                        })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.getAllWallet(true)
    }

    override fun getViewModel(): BaseViewModel<*>? {
        return viewModel
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        refreshMoneyDisplay()
        normalFragment?.setVisibility(isChecked)
        walletFragment?.setVisibility(isChecked)
        contractFragment?.setVisibility(isChecked)
        leverFragment?.setVisibility(isChecked)
    }

    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(AssetsWalletFragment().also {
            val bundle = Bundle()
//            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            it.arguments = bundle
            walletFragment = it
            walletFragment?.setEventListener(this)
        })
        fragmentList?.add(AssetsSpotFragment().also {
            val bundle = Bundle()
//            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            normalFragment = it
            normalFragment?.setEventListener(this)
        })
        fragmentList?.add(AssetsContractFragment().also {
            val bundle = Bundle()
//            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            contractFragment = it
            contractFragment?.setEventListener(this)
        })
        fragmentList?.add(EmptyFragment().also {
            val bundle = Bundle()
//            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(EmptyFragment().also {
            val bundle = Bundle()
//            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })

    }
    fun setMoney(total: Money?){
        binding?.money?.tag = total
        refreshMoneyDisplay()
    }
    fun setMoney2(total2: Money?){
        binding?.moneyCny?.tag = total2
        refreshMoneyDisplay()
    }

    private fun refreshMoneyDisplay() {
        mContext?.runOnUiThread {
            if (binding?.btnWalletEye?.isChecked != true) {
                binding?.money?.setText("****")
                binding?.moneyCny?.setText("****")
            }
            else{
                val total = binding?.money?.tag as Money?
                var usdt = "$nullAmount "
                var usd = String.format("≈ %s USD", nullAmount)
                var cny = String.format("≈ %S CNY", nullAmount)
                val exChange = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
                val rates: Double? = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd
                if (total != null && exChange == 0) {
                    usdt = NumberUtil.formatNumberDynamicScaleNoGroup(total.total, 8, 2, 2) + " "
                    cny = String.format("≈ %S CNY", NumberUtil.formatNumberDynamicScaleNoGroup(total.total!! * (total.rate!!), 8, 2, 2))
                    binding?.money?.setText(usdt)
                    binding?.moneyCny?.setText(cny)
                }
                if (total != null && exChange == 1) {
                    usdt = NumberUtil.formatNumberDynamicScaleNoGroup(total.total, 8, 2, 2) + " "
                    usd = String.format("≈ %S USD", NumberUtil.formatNumberDynamicScaleNoGroup(total.total!! * rates!!, 8, 2, 2))
                    binding?.money?.setText(usdt)
                    binding?.moneyCny?.setText(usd)
                }
                if (exChange == 0){
                    binding?.money?.setText(usdt)
                    binding?.moneyCny?.setText(cny)
                }
                if (exChange == 1){
                    binding?.money?.setText(usdt)
                    binding?.moneyCny?.setText(usd)
                }  }
        }
    }

    private fun doTypeChange() {
        val value = binding?.money?.tag
        if (value is Number) {
            refreshTotalMoney(value.toDouble())
        } else {
            refreshTotalMoney(null)
        }
    }

    private fun refreshTotalMoney(total: Double?) {
        binding?.money?.tag = total
        refreshMoneyDisplay()
        if (TYPE_BTC == otherMoneyType) {
            viewModel!!.computeTotalBTC(total)
        } else {
            viewModel!!.computeTotalCNY(total)
        }
    }

    override fun onUserBalanceChanged(userBalance: UserBalance?) {
            viewModel?.updateBalance(userBalance)
           viewModel?.updateTigerBalance(userBalance)
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
        viewModel!!.getAllWallet(false)
    }

    override fun onGetWallet(observable: Observable<Int>?, isShowLoading: Boolean) {
        observable!!.subscribe(HttpCallbackSimple(mContext, isShowLoading, object : Callback<Int>() {
            override fun error(type: Int, message: Any) {
                FryingUtil.showToast(mContext, message.toString())
            }

            override fun callback(result: Int) {
            }
        }))
    }

    override fun onWallet(observable: Observable<ArrayList<Wallet?>?>?, isShowLoading: Boolean) {
        mContext?.runOnUiThread {
            normalFragment?.run {
                observable?.subscribe {
                    if(normalFragment!!.isSearch() == true){
                        setData(viewModel?.filterWallet())
                    }else{
                        setData(it)
                    }
                }
            }
            assetsContractFragment?.run {
                observable?.subscribe {
//                    setData(it)
                }
            }
            assetsFinanceFragment?.run {
                observable?.subscribe {
//                    setData(it)
                }
            }
            assetsWalletFragment?.run {
                observable?.subscribe {
//                                setData(it)
                }
            }
        }
    }

    override fun onContractWallet(
        observable: Observable<ArrayList<TigerWallet?>?>?,
        isShowLoading: Boolean
    ) {
        contractFragment?.run {
            observable?.subscribe {
                if(contractFragment!!.isSearch() == true){
                    setData(viewModel?.filterTigerWallet())
                }else{
                    setData(it)
                }
            }
        }
    }

    override fun onWalletLever(observable: Observable<ArrayList<WalletLever?>?>?, isShowLoading: Boolean) {
        mContext?.runOnUiThread {
            leverFragment?.run {
                observable?.subscribe {
                    setData(it)
                }
            }
        }
    }

    override fun onWalletTotal(observable: Observable<Money?>?) {
        mContext?.runOnUiThread {
            observable?.subscribe {
                setMoney(it)
            }

            normalFragment?.run {
                observable?.subscribe {
                    setTotal(it)
                }
            }
            walletFragment?.run {
                observable?.subscribe {
                    setTotal(it)
                }
            }
        }
    }

    override fun onContractWalletTotal(observable: Observable<Money?>?) {
        mContext?.runOnUiThread {
            observable?.subscribe {
                setMoney2(it)
            }
            contractFragment?.run {
                observable?.subscribe {
                    setTotal(it)
                }
            }
            walletFragment?.run {
                observable?.subscribe {
                    setTotal2(it)
                }
            }
        }
    }


    override fun onWalletLeverTotal(observable: Observable<Money?>?) {
        mContext?.runOnUiThread {
            leverFragment?.run {
                observable?.subscribe {
                    setTotal(it)
                }
            }
        }
    }

    override fun onTotalMoney(observable: Observable<Double?>) {
        observable.subscribe { total -> refreshTotalMoney(total) }.run { }
    }

    override fun onTotalCNY(observable: Observable<Double?>) {
        observable.subscribe { cny: Double? ->
            val showCny = "≈" + if (cny == null) getString(R.string.number_default) else NumberUtil.formatNumberDynamicScaleNoGroup(cny, 8, 2, 2)
            binding?.moneyCny?.tag = showCny + "" + otherMoneyType
            refreshMoneyDisplay()
        }.run { }
    }

    override fun onTotalBTC(observable: Observable<Double?>) {
        observable.subscribe { btcMoney: Double? ->
            val showBtcMoney: String? = if (btcMoney == null) {
                "≈" + getString(R.string.number_default)
            } else {
                "≈" + NumberUtil.formatNumberDynamicScaleNoGroup(btcMoney, 8, 5, 5)
            }
            binding?.moneyCny?.tag = showBtcMoney + "" + otherMoneyType
            refreshMoneyDisplay()
        }.run { }
    }

    override fun getWalletCoinFilter(): Boolean? {
        return viewModel!!.getWalletCoinFilter()
    }

    override fun setWalletCoinFilter(checked: Boolean) {
        viewModel!!.setWalletCoinFilter(checked)
        normalFragment?.setWalletCoinFilter(checked)
        contractFragment?.setWalletCoinFilter(checked)
        leverFragment?.setWalletCoinFilter(checked)
    }

    override fun search(searchKey: String, walletType: Int) {
        viewModel!!.search(searchKey)
//        normalFragment?.setSearchKey(searchKey)
        mContext?.hideSoftKeyboard()
    }

    override fun getAllWallet(isShowLoading: Boolean) {
        viewModel!!.getAllWallet(isShowLoading)
    }

    override fun getContractAllWallet(isShowLoading: Boolean) {
        viewModel!!.getAllWallet(isShowLoading)
    }

    override fun getFinanceAllWallet(isShowLoading: Boolean) {
        viewModel!!.getAllWallet(isShowLoading)
    }

    override fun getAssetAllWallet(isShowLoading: Boolean) {
        viewModel!!.getAllWallet(isShowLoading)
    }












}