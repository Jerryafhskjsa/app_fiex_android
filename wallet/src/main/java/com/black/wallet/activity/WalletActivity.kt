package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.Money
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.TigerWallet
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLever
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotAccountBinding
import com.black.wallet.fragment.WalletLeverFragment
import com.black.wallet.fragment.WalletNormalFragment
import com.black.wallet.viewmodel.WalletViewModel
import com.black.wallet.viewmodel.WalletViewModel.OnWalletModelListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import kotlin.math.abs

//现货账户
@Route(value = [RouterConstData.WALLET], beforePath = RouterConstData.LOGIN)
class WalletActivity : BaseActionBarActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnWalletModelListener {
    companion object {
        private const val TYPE_CNY = "CNY"
        private const val TYPE_BTC = "BTC"
        private val TAB_TITLES = arrayOfNulls<String>(4) //标题
        private var TAB_NORMAL: String? = null
        private var TAB_CONTRACT: String? = null
        private var TAB_FINANCE: String? = null
        private var TAB_WALLET: String? = null
    }

    private var bgB2 = 0
    private var bgDefault: Int = 0
    private var btnBackDefault: Drawable? = null
    private var btnBackNormal: Drawable? = null
    private var colorDefault = 0
    private var colorT1: Int = 0

    private var appBarLayout: AppBarLayout? = null
    private var actionBarTitle: TextView? = null

    private var otherMoneyType = TYPE_CNY
    private var typeList: MutableList<String>? = null
    private var binding: ActivitySpotAccountBinding? = null
    private var viewModel: WalletViewModel? = null

    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var normalFragment: WalletNormalFragment? = null
    private var leverFragment: WalletLeverFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bgB2 = SkinCompatResources.getColor(this, R.color.B2)
        bgDefault = SkinCompatResources.getColor(this, R.color.C1)
        btnBackDefault = SkinCompatResources.getDrawable(this, R.drawable.btn_back_white)
        btnBackNormal = SkinCompatResources.getDrawable(this, R.drawable.btn_back)
        colorDefault = SkinCompatResources.getColor(this, R.color.white)
        colorT1 = SkinCompatResources.getColor(this, R.color.T1)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_spot_account)

        appBarLayout = binding?.root?.findViewById(R.id.app_bar_layout)
        actionBarTitle = binding?.root?.findViewById(R.id.action_bar_title)
        appBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBar, verticalOffset ->
            if (abs(verticalOffset) >= appBar.totalScrollRange) {
                binding?.root?.setBackgroundColor(bgB2)
                binding?.toolbar?.setBackgroundColor(bgB2)
                binding?.actionBarBack?.setImageDrawable(btnBackNormal)
                actionBarTitle?.setTextColor(colorT1)
            } else {
                binding?.root?.setBackgroundColor(bgDefault)
                binding?.toolbar?.setBackgroundColor(bgDefault)
                binding?.actionBarBack?.setImageDrawable(btnBackDefault)
                actionBarTitle?.setTextColor(colorDefault)
            }
        })

        viewModel = WalletViewModel(this, this)

        binding?.btnWalletEye?.isChecked = true
        binding?.btnWalletEye?.setOnCheckedChangeListener(this)
        binding?.moneyCny?.setOnClickListener(this)
        binding?.btnExchange?.setOnClickListener(this)
        binding?.btnWithdraw?.setOnClickListener(this)
        binding?.btnDemand?.setOnClickListener(this)

        typeList = ArrayList()
        typeList!!.add(TYPE_CNY)
        typeList!!.add(TYPE_BTC)

        getString(R.string.spot_account).also {
            TAB_NORMAL = it
            TAB_TITLES[0] = TAB_NORMAL
        }
        getString(R.string.contract_account).also {
            TAB_CONTRACT = it
            TAB_TITLES[1] = TAB_CONTRACT
        }
        getString(R.string.finance_account).also {
            TAB_FINANCE = it
            TAB_TITLES[2] = TAB_FINANCE
        }
        getString(R.string.wallet_account).also {
            TAB_WALLET = it
            TAB_TITLES[3] = TAB_WALLET
        }

        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_FIXED

        initFragmentList()

        binding?.viewPager?.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
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

    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.my_wallet)
    }

    public override fun getViewModel(): WalletViewModel {
        return viewModel!!
    }

    override fun onResume() {
        super.onResume()
        viewModel!!.getAllWallet(true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_exchange -> {
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel!!.getWalletList())
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                        .withRequestCode(ConstData.CHOOSE_COIN_RECHARGE)
                        .with(bundle)
                        .go(this)
            }
            R.id.btn_withdraw -> {
                val extras = Bundle()
                extras.putParcelableArrayList(ConstData.WALLET_LIST, viewModel!!.getWalletList())
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                        .withRequestCode(ConstData.CHOOSE_COIN_WITHDRAW)
                        .with(extras)
                        .go(this)
            }
            R.id.btn_demand -> {
                BlackRouter.getInstance().build(RouterConstData.LEVER_PAIR_CHOOSE).withRequestCode(ConstData.LEVER_PAIR_CHOOSE).go(this)
//                BlackRouter.getInstance().build(RouterConstData.C2C_NEW).go(this)
            }
            R.id.money_cny -> {
                DeepControllerWindow(mContext as Activity, getString(R.string.price_unit), otherMoneyType, typeList, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        otherMoneyType = item
                        doTypeChange()
                    }

                }).show()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        refreshMoneyDisplay()
        normalFragment?.setVisibility(isChecked)
        leverFragment?.setVisibility(isChecked)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.CHOOSE_COIN_RECHARGE -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        val bundle = Bundle()
                        bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel!!.getWalletList())
                        bundle.putParcelable(ConstData.WALLET, chooseWallet)
                        bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
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
                        run {
                            val bundle = Bundle()
                            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel!!.getWalletList())
                            bundle.putParcelable(ConstData.WALLET, chooseWallet)
                            bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                            BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this)
                        }
                    }
                }
                ConstData.LEVER_PAIR_CHOOSE -> {
                    val pair = data?.getStringExtra(ConstData.PAIR)
                    if (pair != null) {
                        FryingUtil.checkAndAgreeLeverProtocol(mContext, Runnable {
                            val bundle = Bundle()
                            bundle.putString(ConstData.PAIR, pair)
                            BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER).with(bundle).go(this)
                        })
                    }
                }
            }
        }
    }

    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(WalletNormalFragment().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            normalFragment = it
        })
        fragmentList?.add(WalletNormalFragment().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            normalFragment = it
        })
        fragmentList?.add(WalletNormalFragment().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            normalFragment = it
        })
        fragmentList?.add(WalletNormalFragment().also {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.WALLET_LIST, viewModel?.getWalletList())
            bundle.putBoolean("isVisibility", binding?.btnWalletEye?.isChecked ?: false)
            bundle.putString("searchKey", viewModel?.getSearchKey())
            it.arguments = bundle
            normalFragment = it
        })
    }

    private fun refreshMoneyDisplay() {
        runOnUiThread {
            showNumberTextView(binding?.money!!)
            showNumberTextView(binding?.moneyCny!!)
        }
    }

    private fun showNumberTextView(textView: TextView) {
        if (binding?.btnWalletEye?.isChecked != true) {
            textView.text = "****"
        } else {
            when (val value = textView.tag) {
                null -> {
                    textView.text = nullAmount
                }
                is Number -> {
                    textView.text = NumberUtil.formatNumberDynamicScaleNoGroup(value, 8, 2, 2)
                }
                else -> {
                    textView.text = value.toString()
                }
            }
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
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
        viewModel!!.getAllWallet(false)
    }

    override fun onGetWallet(observable: Observable<Int>?, isShowLoading: Boolean) {
        observable!!.subscribe(HttpCallbackSimple(this, isShowLoading, object : Callback<Int>() {
            override fun error(type: Int, message: Any) {
                FryingUtil.showToast(mContext, message.toString())
            }

            override fun callback(result: Int) {
            }
        }))
    }

    override fun onWallet(observable: Observable<ArrayList<Wallet?>?>?, isShowLoading: Boolean) {
        runOnUiThread {
            normalFragment?.run {
                observable?.subscribe {
                    setData(it)
                }
            }
        }
    }

    override fun onContractWallet(
        observable: Observable<ArrayList<TigerWallet?>?>?,
        isShowLoading: Boolean
    ) {

    }

    override fun onWalletLever(observable: Observable<ArrayList<WalletLever?>?>?, isShowLoading: Boolean) {
        runOnUiThread {
            leverFragment?.run {
                observable?.subscribe {
                    setData(it)
                }
            }
        }
    }

    override fun onWalletTotal(observable: Observable<Money?>?) {
        runOnUiThread {
            normalFragment?.run {
                observable?.subscribe {
                    setTotal(it)
                }
            }
        }
    }

    override fun onContractWalletTotal(observable: Observable<Money?>?) {

    }

    override fun onWalletLeverTotal(observable: Observable<Money?>?) {
        runOnUiThread {
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

    fun getAllWallet(isShowLoading: Boolean) {
        viewModel!!.getAllWallet(isShowLoading)
    }

    fun getWalletCoinFilter(): Boolean? {
        return viewModel!!.getWalletCoinFilter()
    }

    fun setWalletCoinFilter(checked: Boolean) {
        viewModel!!.setWalletCoinFilter(checked)
        normalFragment?.setWalletCoinFilter(checked)
        leverFragment?.setWalletCoinFilter(checked)
    }

    fun search(searchKey: String, walletType: Int) {
        viewModel!!.search(searchKey)
        normalFragment?.setSearchKey(searchKey)
        leverFragment?.setSearchKey(searchKey)
    }
}