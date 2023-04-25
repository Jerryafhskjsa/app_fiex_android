package com.black.frying.fragment.assets

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.Money
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.NumberUtil
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletAdapter
import com.black.wallet.databinding.FragmentCapitalBinding
import com.black.wallet.databinding.FragmentWalletNormalBinding
import com.black.wallet.viewmodel.WalletViewModel

class AssetsFinanceFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    private var walletList: ArrayList<Wallet?>? = null
    private var isVisibility: Boolean = true
    private var searchKey: String? = null
    private var wallet: Wallet? = null
    private var doSearch = true
    private var walletFragment: AssetsWalletFragment? = null
    private var normalFragment: AssetsSpotFragment? = null
    private var contractFragment: AssetsContractFragment? = null
    private var binding: FragmentCapitalBinding? = null
    private var layout: View? = null

    private var adapter: WalletAdapter? = null
    private var eventListener:FinanceEventResponseListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun setEventListener(listener: FinanceEventResponseListener){
        this.eventListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        //walletList = arguments?.getParcelableArrayList(ConstData.WALLET_LIST)
       // isVisibility = if (arguments?.getBoolean("isVisibility", false) == null) false else arguments?.getBoolean("isVisibility", false)!!
        searchKey = arguments?.getString("searchKey")

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_capital, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.exchange?.setOnClickListener(this)
        binding?.withdraw?.setOnClickListener(this)
        binding?.transaction?.setOnClickListener(this)
        binding?.xianshi?.setOnClickListener(this)
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = WalletAdapter(mContext!!, BR.listItemSpotAccountModel, walletList)
        adapter?.setVisibility(isVisibility)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        //解决数据加载不完的问题
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                eventListener?.getFinanceAllWallet(false)
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        binding?.coinSearch?.setText(searchKey)
        binding?.coinSearch?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    eventListener?.financeSearch(v!!.text.toString(), WalletViewModel.WALLET_NORMAL)
                    return true
                }
                return false
            }
        })
        binding?.coinSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (doSearch)
                    eventListener?.financeSearch(s.toString(), WalletViewModel.WALLET_NORMAL)
                doSearch = true
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.btnWalletFilter?.setOnCheckedChangeListener { _, isChecked ->
            if (doSearch) {
                eventListener?.setFinanceWalletCoinFilter(isChecked)
                eventListener?.financeSearch(binding?.coinSearch?.text.toString(), WalletViewModel.WALLET_NORMAL)
            }
            doSearch = true
        }
        binding?.xianshi?.setOnCheckedChangeListener {_, isChecked ->
            eventListener?.setWalletziCanFilter(isChecked)
            isVisibility = isChecked
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        isVisibility = (if (eventListener?.getWalletziCanFilter() == null) true else eventListener?.getWalletziCanFilter()!!)
        doSearch = (if (eventListener?.getFinanceWalletCoinFilter() == null) true else eventListener?.getFinanceWalletCoinFilter()!!)
        binding?.btnWalletFilter?.isChecked = true  }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val wallet = adapter?.getItem(position)
        //点击账户详情
        val extras = Bundle()
        extras.putParcelable(ConstData.WALLET, wallet)
        BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras).go(this)
    }

    override fun onClick(v: View?) {
        when(v?.id)  {
            R.id.withdraw -> {val bundle = Bundle()
                bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                bundle.putParcelable(ConstData.WALLET, wallet)
                BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this)}
            R.id.exchange -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
                bundle.putParcelable(ConstData.WALLET, wallet)
                BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(this)
            }
            R.id.transaction ->{}
        }
    }



    fun setTotal(total: Money?) {
        binding?.moneyTotal?.tag = total
        refreshMoneyDisplay()
    }

    private fun refreshMoneyDisplay() {
        mContext?.runOnUiThread {
            if (!isVisibility) {
                binding?.moneyTotal?.text = "****"
                binding?.moneyTotalcny?.text = "****"
            } else {
                val total: Money? = binding?.moneyTotal?.tag as Money?
                var usdt = "$nullAmount "
                var usd = String.format("≈%s $", nullAmount)
                var cny = String.format("≈%S ￥", nullAmount)
                val exChange = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
                val rates: Double? = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd
                if (total != null && exChange == 0) {
                    usdt = NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt, 8, 2, 2) + " "
                    cny = String.format("≈%S ￥", NumberUtil.formatNumberDynamicScaleNoGroup(total.cny, 8, 2, 2))
                }
                if (total != null && exChange == 1) {
                    usdt = NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt, 8, 2, 2) + " "
                    usd = String.format("≈%S $", NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt!! * rates!!, 8, 2, 2))
                }
                if (exChange == 0){
                val holeAmountString = usdt + cny
                val holdSpan = SpannableStringBuilder(holeAmountString)
                holdSpan.setSpan(AbsoluteSizeSpan(14, true), usdt.length, holeAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding?.moneyTotal?.setText("0.0")
                    binding?.moneyTotalcny?.setText("0.0")
                }
                if (exChange == 1){
                    val holeAmountString = usdt + usd
                    val holdSpan = SpannableStringBuilder(holeAmountString)
                    holdSpan.setSpan(AbsoluteSizeSpan(14, true), usdt.length, holeAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    binding?.moneyTotal?.setText("0.0")
                    binding?.moneyTotalcny?.setText("0.0")
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
        binding?.refreshLayout?.setRefreshing(false)
        adapter?.data = data
        adapter?.notifyDataSetChanged()

    }
    fun setVisibility(isChecked: Boolean) {
        isVisibility = isChecked
        adapter?.setVisibility(isVisibility)
        binding?.xianshi?.isChecked = isChecked
        refreshMoneyDisplay()
    }

    fun setWalletCoinFilter(isChecked: Boolean) {
        doSearch = false
        binding?.btnWalletFilter?.isChecked = isChecked
    }

    fun setSearchKey(searchKey: String?) {
        doSearch = false
        binding?.coinSearch?.setText(searchKey)
    }

    interface FinanceEventResponseListener {
        fun getFinanceAllWallet(isShowLoading: Boolean) {
        }

        fun getFinanceWalletCoinFilter(): Boolean? {
            return false
        }

        fun setFinanceWalletCoinFilter(checked: Boolean) {
        }

        fun setWalletziCanFilter(checked: Boolean) {
        }

        fun getWalletziCanFilter(): Boolean? {
            return false
        }
        fun financeSearch(searchKey: String, walletType: Int) {
        }
    }

}