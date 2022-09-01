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
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.Money
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.NumberUtil
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletAdapter
import com.black.wallet.databinding.FragmentWalletNormalBinding
import com.black.wallet.viewmodel.WalletViewModel

class AssetsSpotFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    private var walletList: ArrayList<Wallet?>? = null
    private var isVisibility: Boolean = false
    private var searchKey: String? = null
    private var doSearch = true

    private var binding: FragmentWalletNormalBinding? = null
    private var layout: View? = null

    private var adapter: WalletAdapter? = null
    private var eventListener:EventResponseListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun setEventListener(listener: EventResponseListener){
        this.eventListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
//        walletList = arguments?.getParcelableArrayList(ConstData.WALLET_LIST)
        isVisibility = if (arguments?.getBoolean("isVisibility", false) == null) false else arguments?.getBoolean("isVisibility", false)!!
        searchKey = arguments?.getString("searchKey")

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_normal, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
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
                eventListener?.getAllWallet(false)
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        binding?.coinSearch?.setText(searchKey)
        binding?.coinSearch?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    eventListener?.search(v!!.text.toString(), WalletViewModel.WALLET_NORMAL)
                    return true
                }
                return false
            }
        })
        binding?.coinSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (doSearch)
                    eventListener?.search(s.toString(), WalletViewModel.WALLET_NORMAL)
                    doSearch = true
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding?.btnWalletFilter?.setOnCheckedChangeListener { _, isChecked ->
            if (doSearch) {
                eventListener?.setWalletCoinFilter(isChecked)
                eventListener?.search(binding?.coinSearch?.text.toString(), WalletViewModel.WALLET_NORMAL)
            }
            doSearch = true
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        doSearch = false
//        binding?.btnWalletFilter?.isChecked = (if (walletActivity?.getWalletCoinFilter() == null) false else walletActivity?.getWalletCoinFilter()!!)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val wallet = adapter?.getItem(position)
        //点击账户详情
        val extras = Bundle()
        extras.putParcelable(ConstData.WALLET, wallet)
        BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras).go(this)
    }

    override fun onClick(v: View?) {
    }

    fun setData(data: ArrayList<Wallet?>?) {
        binding?.refreshLayout?.setRefreshing(false)
        adapter?.data = data
        adapter?.notifyDataSetChanged()
    }

    fun setTotal(total: Money?) {
        binding?.moneyTotal?.tag = total
        refreshMoneyDisplay()
    }

    private fun refreshMoneyDisplay() {
        mContext?.runOnUiThread {
            if (!isVisibility) {
                binding?.moneyTotal?.text = "****"
            } else {
                val total: Money? = binding?.moneyTotal?.tag as Money?
                var usdt = "$nullAmount "
                var cny = String.format("≈%S CNY", nullAmount)
                if (total != null) {
                    usdt = NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt, 8, 2, 2) + " "
                    cny = String.format("≈%S CNY", NumberUtil.formatNumberDynamicScaleNoGroup(total.cny, 8, 2, 2))
                }
                val holeAmountString = usdt + cny
                val holdSpan = SpannableStringBuilder(holeAmountString)
                holdSpan.setSpan(AbsoluteSizeSpan(14, true), usdt.length, holeAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding?.moneyTotal?.setText(holdSpan)
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
    }

    interface EventResponseListener {
        fun getAllWallet(isShowLoading: Boolean) {
        }

        fun getWalletCoinFilter(): Boolean? {
            return false
        }

        fun setWalletCoinFilter(checked: Boolean) {
        }

        fun search(searchKey: String, walletType: Int) {
        }
    }

}