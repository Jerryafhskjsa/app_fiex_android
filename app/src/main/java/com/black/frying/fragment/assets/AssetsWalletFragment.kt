package com.black.frying.fragment.assets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.FryingExchangeRates.Companion.cny
import com.black.base.model.Money
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.frying.fragment.HomePageAssetsFragment
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentAssetsWalletBinding
import kotlinx.android.synthetic.main.fragment_home_page_mine.*
import java.math.RoundingMode

class AssetsWalletFragment : BaseFragment(),  View.OnClickListener {
    private var layout: View? = null
    private var isVisibility: Boolean = true
    private var binding: FragmentAssetsWalletBinding? = null
    private var eventListener:WalletEventResponseListener? = null
    private var wallet: Wallet? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var normalFragment: AssetsSpotFragment? = null
    private var financeFragment: AssetsFinanceFragment? = null
    private var contractFragment: AssetsContractFragment? = null
    fun setEventListener(listener: WalletEventResponseListener){
        this.eventListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        super.onCreate(savedInstanceState)
        if (layout != null) {
            return layout
        }
        //isVisibility = if (arguments?.getBoolean("isVisibility", false) == null) false else arguments?.getBoolean("isVisibility", false)!!
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_assets_wallet, container, false)
        layout = binding?.root
        binding?.recharge?.setOnClickListener(this)
        binding?.extract?.setOnClickListener(this)
        binding?.spot?.setOnClickListener(this)
        binding?.future?.setOnClickListener(this)
        binding?.capital?.setOnClickListener(this)
        binding?.exchange?.setOnClickListener(this)
        binding?.transaction?.setOnClickListener(this)
        binding?.xianshi?.setOnCheckedChangeListener {_, isChecked ->
            eventListener?.setWalletziCanFilter(isChecked)
            isVisibility = isChecked
        }
        return layout
    }



    override fun onClick(v: View) {
        when(v.id){
            R.id.recharge -> {
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN_RECHARGE)
                    .go(this)
            }


            R.id.extract -> {
                val extras = Bundle()
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                    .withRequestCode(ConstData.CHOOSE_COIN_WITHDRAW)
                    .go(this)
            }
            R.id.transaction -> {
                BlackRouter.getInstance().build(RouterConstData.TRANSACTION)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(mContext)}
            R.id.spot -> {}
            R.id.future -> {}
            R.id.capital -> {}
            R.id.financial -> {}
            R.id.exchange -> { BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)}

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

    fun setData(data: ArrayList<Wallet?>?) {
        for (h in data?.indices!!) {
            if (data[h]?.coinType == "USDT")
            {
                wallet = data[h]
            }
        }
    }

    fun setTotal(total: Money?) {
        binding?.moneyTotal?.tag = total
        refreshMoneyDisplay()
    }
    fun setTotal2(total2: Money?) {
        binding?.futureUsdt?.tag = total2
        refreshMoneyDisplay()
    }

    private fun refreshMoneyDisplay() {
        mContext?.runOnUiThread {
            if (!isVisibility) {
                binding?.moneyTotal?.text = "****"
                binding?.moneyTotalcny?.text = "****"
                binding?.spotUsdt?.text = "****"
                binding?.futureUsdt?.text = "****"
                binding?.financialUsdt?.text = "****"
                binding?.capitalUsdt?.text = "****"
                binding?.spotCny?.text = "****"
                binding?.futureCny?.text = "****"
                binding?.financialCny?.text = "****"
                binding?.capitalCny?.text = "****"
                binding?.spotBili?.text = "****"
                binding?.futureBili?.text = "****"
                binding?.capitalBili?.text = "****"
            } else {
                val total: Money? = binding?.moneyTotal?.tag as Money?
                val total2: Money? = binding?.futureUsdt?.tag as Money?
                val exChange = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
                val rates: Double? = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd
                val spotBili = total?.cny!! / (total.cny!! + total2?.tigerUsdt!!) * 100
                val futureBili = total2.tigerUsdt!! / (total.cny!! + total2.tigerUsdt!!) * 100
                binding?.spotBili?.setText(NumberUtil.formatNumberDynamicScaleNoGroup(
                    spotBili,
                    8,
                    2,
                    2
                ) + "%")
                binding?.futureBili?.setText(NumberUtil.formatNumberDynamicScaleNoGroup(
                    futureBili,
                    8,
                    2,
                    2
                ) + "%")
                if (exChange == 0) {
                    val cny = total?.cny!! * (total.rate!!)
                    binding?.spotUsdt?.setText(
                        if (total.usdt == null) "0.00 USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.cny,
                            8,
                            2,
                            2
                        ) + "USDT"
                    )
                    binding?.futureUsdt?.setText(
                        if (total2?.tigerUsdt == null) "0.00 USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigerUsdt,
                            8,
                            2,
                            2
                        ) + "USDT"
                    )
                    binding?.financialUsdt?.setText("0.00 USDT")
                    binding?.capitalUsdt?.setText("0.00 USDT")
                    binding?.spotCny?.setText(
                        if (total.usdt == null) "≈ ￥0.00" else "≈ ￥" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.futureCny?.setText(
                        if (total2 == null) "≈ ￥0.00" else "≈ ￥" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            (total2.tigerUsdt!! * total.rate!!),
                            8,
                            2,
                            2
                        )
                    )
                    binding?.financialCny?.setText("≈ ￥0.00")
                    binding?.capitalCny?.setText("≈ ￥0.00")
                    binding?.moneyTotalcny?.setText(
                        if (total.total == null && total2?.tigercny == null) "≈ ￥0.0" else if (total.total == null && total2?.tigercny != null) "≈ ￥" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigercny,
                            8,
                            2,
                            2
                        )  else if (total.total != null && total2?.tigercny == null) "≈ ￥" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny,
                            8,
                            2,
                            2
                        )  else "≈ ￥" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny + total2?.tigerUsdt!! * (total.rate!!),
                            8,
                            2,
                            2
                        )
                    )
                    binding?.moneyTotal?.setText(
                        if (total.total == null && total2?.tigerUsdt == null) "0.0 USDT" else if (total.total == null && total2?.tigerUsdt != null) NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigerUsdt,
                            8,
                            2,
                            2
                        ) + "USDT" else if (total.total != null && total2?.tigerUsdt == null) NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.cny,
                            8,
                            2,
                            2
                        ) + "USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.cny!! + total2?.tigerUsdt!!,
                            8,
                            2,
                            2
                        )
                    )
                }
                else{
                    val cny = total?.cny!! * rates!!
                    binding?.spotUsdt?.setText(
                        if (total.total == null) "0.00 USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.usdt,
                            8,
                            2,
                            2
                        ) + "USDT"
                    )
                    binding?.futureUsdt?.setText(
                        if (total2?.tigerUsdt == null) "0.00 USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigerUsdt,
                            8,
                            2,
                            2
                        ) + "USDT"
                    )
                    binding?.financialUsdt?.setText("0.00 USDT")
                    binding?.capitalUsdt?.setText("0.00 USDT")
                    binding?.spotCny?.setText(
                        if (total.total == null) "≈ $0.00" else "≈ $" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.futureCny?.setText(
                        if (total2 == null) "≈ $0.00" else "≈ $" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            (total2.tigerUsdt!! * rates),
                            8,
                            2,
                            2
                        )
                    )
                    binding?.financialCny?.setText("≈ $0.00")
                    binding?.capitalCny?.setText("≈ $0.00")
                    binding?.moneyTotalcny?.setText(
                        if (total.total == null && total2?.tigercny == null) "≈ $0.0" else if (total.total == null && total2?.tigercny != null) "≈ $" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigerUsdt!! * rates,
                            8,
                            2,
                            2
                        )  else if (total.total != null && total2?.tigercny == null) "≈ $" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny,
                            8,
                            2,
                            2
                        )  else "≈ $" + NumberUtil.formatNumberDynamicScaleNoGroup(
                            cny + total2?.tigerUsdt!! * rates,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.moneyTotal?.setText(
                        if (total.total == null && total2?.tigerUsdt == null) "0.0 USDT" else if (total.total == null && total2?.tigerUsdt != null) NumberUtil.formatNumberDynamicScaleNoGroup(
                            total2.tigerUsdt,
                            8,
                            2,
                            2
                        ) + "USDT" else if (total.total != null && total2?.tigerUsdt == null) NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.cny,
                            8,
                            2,
                            2
                        ) + "USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.cny!! + total2?.tigerUsdt!!,
                            8,
                            2,
                            2
                        )
                    )
                }
            }
        }
    }

    fun setVisibility(isChecked: Boolean) {
        isVisibility = isChecked
        binding?.xianshi?.isChecked = isChecked
        refreshMoneyDisplay()
    }


    interface WalletEventResponseListener {
        fun getAssetAllWallet(isShowLoading: Boolean) {
        }

        fun getAssetWalletCoinFilter(): Boolean? {
            return false
        }

        fun setAssetWalletCoinFilter(checked: Boolean) {
        }

        fun setWalletziCanFilter(checked: Boolean) {
        }
        fun assetWalletSearch(searchKey: String, walletType: Int) {
        }
    }

}