package com.black.frying.fragment.assets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
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
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.Money
import com.black.base.model.future.AccountInfoBean
import com.black.base.model.wallet.TigerWallet
import com.black.base.util.ConstData
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.NumberUtil
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.ContractAdapter
import com.black.wallet.databinding.FragmentContractNormalBinding
import com.black.wallet.viewmodel.WalletViewModel

class AssetsContractFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    private var walletList: ArrayList<TigerWallet?>? = null
    private var isVisibility: Boolean = false
    private var searchKey: String? = null
    private var doSearch = true
    private var type: Int = 0
    private var binding: FragmentContractNormalBinding? = null
    private var layout: View? = null
    private var rate = C2CApiServiceHelper.coinUsdtPrice?.usdt
    private var adapter: ContractAdapter? = null
    private var eventListener:ContractEventResponseListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun setEventListener(listener: ContractEventResponseListener){
        this.eventListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
//        walletList = arguments?.getParcelableArrayList(ConstData.WALLET_LIST)
        isVisibility = if (arguments?.getBoolean("isVisibility", false) == null) false else arguments?.getBoolean("isVisibility", false)!!
        searchKey = arguments?.getString("searchKey")

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contract_normal, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.uBtn?.setOnClickListener(this)
        binding?.coinBtn?.setOnClickListener(this)
        binding?.exchange?.setOnClickListener(this)
        binding?.transaction?.setOnClickListener(this)
        binding?.bill?.setOnClickListener(this)
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = ContractAdapter(mContext!!, BR.listItemSpotAccountModel, walletList)
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
        binding?.refreshLayout?.setRefreshing(true)
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                eventListener?.getContractAllWallet(false)
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        binding?.coinSearch?.setText(searchKey)
        binding?.coinSearch?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    eventListener?.search(v!!.text.toString(), WalletViewModel.WALLET_CONTRACT)
                    return true
                }
                return false
            }
        })
        binding?.coinSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    eventListener?.search(s.toString(), WalletViewModel.WALLET_CONTRACT)
            }

            override fun afterTextChanged(s: Editable) {}
        })
                binding?.btnWalletFilter?.setOnCheckedChangeListener { _, isChecked ->
                eventListener?.setWalletCoinFilter(isChecked)
                eventListener?.search(binding?.coinSearch?.text.toString(), WalletViewModel.WALLET_CONTRACT)
                doSearch = isChecked
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        doSearch = (if (eventListener?.getContractWalletCoinFilter() == null) true else eventListener?.getContractWalletCoinFilter()!!)
        binding?.btnWalletFilter?.isChecked = true
    }
    fun isSearch():Boolean{
        return true
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val wallet = adapter?.getItem(position)
        //点击账户详情
        val extras = Bundle()
        extras.putParcelable(ConstData.WALLET, wallet)
        BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras).go(this)
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.u_btn -> {
                type = 0
                refresh(type)
                binding?.refreshLayout?.setRefreshing(true)
            }
            R.id.coin_btn -> {
                type = 1
                refresh(type)
                binding?.refreshLayout?.setRefreshing(true)
            }
            R.id.exchange -> { BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(this)}
            R.id.transaction -> {
                BlackRouter.getInstance().build(RouterConstData.TRANSACTION)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(mContext)}
            R.id.bill -> {
                FutureApiServiceHelper.getAccountInfo(
                    context,
                    object : Callback<HttpRequestResultBean<AccountInfoBean?>?>() {
                        override fun error(type: Int, error: Any?) {
                        }

                        override fun callback(result: HttpRequestResultBean<AccountInfoBean?>?) {
                            if (result != null && result.returnCode == HttpRequestResult.SUCCESS) {
                                BlackRouter.getInstance().build(RouterConstData.CONTRACT_BILL_ACTIVITY)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .go(mContext)
                            }
                            else {
                                FryingUtil.showToast(mContext, getString(R.string.info_futrues))
                            }
                        }

                    })
               }
        }
    }
private fun refresh(type: Int){
    if (0 == type){
        binding!!.coinBtn.isChecked = false
        binding?.barB?.visibility = View.GONE
        binding?.barA?.visibility = View.VISIBLE
        binding!!.coinBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat())
        binding!!.uBtn.isChecked = true
        binding!!.uBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_15).toFloat())
        //binding?.recyclerView?.visibility = View.VISIBLE
    }
    else if (1 == type) {
        binding?.barB?.visibility = View.VISIBLE
        binding?.barA?.visibility = View.GONE
        binding!!.uBtn.isChecked = false
        binding!!.uBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat())
        binding!!.coinBtn.isChecked = true
        binding!!.coinBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_15).toFloat())
        //binding?.recyclerView?.visibility = View.GONE
    }
}
    fun setData(data: ArrayList<TigerWallet?>?) {
        val showData: ArrayList<TigerWallet?>? = data
        if (type == 0) {
                binding?.refreshLayout?.setRefreshing(false)
                adapter?.data = showData
                adapter?.notifyDataSetChanged()
        }
        else{
            binding?.refreshLayout?.setRefreshing(false)
            adapter?.data = showData
            adapter?.notifyDataSetChanged()
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
                binding?.moneyTotalCny?.text = "****"
                binding?.profit?.text = "****"
                binding?.profitCny?.text = "****"
                binding?.margin?.text = "****"
                binding?.marginCny?.text = "****"
                binding?.money?.text = "****"
                binding?.moneyCny?.text = "****"
            } else {
                val total: Money? = binding?.moneyTotal?.tag as Money?
                var usdt = "$nullAmount "
                var cny = String.format("≈ %S CNY", nullAmount)
                val exChange = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
                val rates: Double? = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd
                if (total != null && exChange == 0) {
                    usdt =
                        NumberUtil.formatNumberDynamicScaleNoGroup(total.tigerUsdt, 8, 2, 2) + " "
                    cny = String.format(
                        "≈ %S CNY",
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.tigerUsdt!! * rate!!,
                            8,
                            2,
                            2
                        )
                    )
                    val wallet = total.tigerUsdt!! + total.profit!!
                    val walletCny = rate!! * (total.tigerUsdt!! + total.profit!!)
                    binding?.money?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            wallet,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.moneyCny?.setText(
                        String.format(
                            "≈ %S CNY",
                            NumberUtil.formatNumberDynamicScaleNoGroup(walletCny, 8, 2, 2)
                        )
                    )
                    binding?.moneyTotal?.setText(usdt)
                    binding?.moneyTotalCny?.setText(cny)
                    binding?.profit?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.profit,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.profitCny?.setText(
                        String.format(
                            "≈ %S CNY",
                            NumberUtil.formatNumberDynamicScaleNoGroup(
                                rate!! * (total.profit!!),
                                8,
                                2,
                                2
                            )
                        )
                    )
                    binding?.margin?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.tigerUsdt?.minus(
                                total.crossedMargin!!
                            ), 8, 2, 2
                        )
                    )
                    binding?.marginCny?.setText(
                        String.format(
                            "≈ %S CNY",
                            NumberUtil.formatNumberDynamicScaleNoGroup(
                                (total.tigerUsdt!!.minus(total.crossedMargin!!)) * rate!!,
                                8,
                                2,
                                2
                            )
                        )
                    )
                }
                else{
                    usdt =
                        NumberUtil.formatNumberDynamicScaleNoGroup(total?.tigerUsdt, 8, 2, 2) + " "
                    cny = String.format(
                        "≈ %S USD",
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total?.tigerUsdt!! * rates!!,
                            8,
                            2,
                            2
                        )
                    )
                    val wallet = total.tigerUsdt!! + total.profit!!
                    val walletCny = rates * (total.tigerUsdt!! + total.profit!!)
                    binding?.money?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            wallet,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.moneyCny?.setText(
                        String.format(
                            "≈ %S USD",
                            NumberUtil.formatNumberDynamicScaleNoGroup(walletCny, 8, 2, 2)
                        )
                    )
                    binding?.moneyTotal?.setText(usdt)
                    binding?.moneyTotalCny?.setText(cny)
                    binding?.profit?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.profit,
                            8,
                            2,
                            2
                        )
                    )
                    binding?.profitCny?.setText(
                        String.format(
                            "≈ %S USD",
                            NumberUtil.formatNumberDynamicScaleNoGroup(
                                rates * (total.profit!!),
                                8,
                                2,
                                2
                            )
                        )
                    )
                    binding?.margin?.setText(
                        NumberUtil.formatNumberDynamicScaleNoGroup(
                            total.tigerUsdt?.minus(
                                total.crossedMargin!!
                            ), 8, 2, 2
                        )
                    )
                    binding?.marginCny?.setText(
                        String.format(
                            "≈ %S USD",
                            NumberUtil.formatNumberDynamicScaleNoGroup(
                                (total.tigerUsdt!!.minus(total.crossedMargin!!)) * rates,
                                8,
                                2,
                                2
                            )
                        )
                    )
                }
            }
        }
    }

    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
        refreshMoneyDisplay()
        adapter?.setVisibility(isVisibility)
    }

    fun setWalletCoinFilter(isChecked: Boolean) {
        doSearch = false
        binding?.btnWalletFilter?.isChecked = isChecked
    }

    fun setSearchKey(searchKey: String?) {
        doSearch = false
        binding?.coinSearch?.setText(searchKey)
        binding?.coinSearch?.text?.length?.let { binding?.coinSearch?.setSelection(it) }
    }

    interface ContractEventResponseListener {
        fun getContractAllWallet(isShowLoading: Boolean) {
        }

        fun getContractWalletCoinFilter(): Boolean? {
            return false
        }

        fun setWalletCoinFilter(checked: Boolean) {
        }

        fun search(searchKey: String, walletType: Int) {
        }
    }

}