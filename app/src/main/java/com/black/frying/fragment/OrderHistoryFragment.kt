package com.black.frying.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.Constants
import com.black.base.model.future.OrderBean
import com.black.base.model.future.OrderBeanItem
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.util.Callback
import com.black.wallet.BR
import com.black.wallet.R
import com.black.frying.adapter.ContraLimitTabListAdapter
import com.black.frying.adapter.ContractOrderHistoryAdapter
import com.black.wallet.databinding.FragmentDelegationBinding
import kotlin.collections.ArrayList

class OrderHistoryFragment : BaseFragment(), View.OnClickListener,OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    companion object {
        private var TYPE_U_CONTRACT = ""
        private var TYPE_COIN_CONTRACT = ""
        private var TYPE_ALL = ""
        private const val TYPE_BTC = "BTCUSDT"
        private const val TYPE_ETH = "ETHUSDT"
    }
    private var wallet: Wallet? = null
    private var binding: FragmentDelegationBinding? = null
    private var layout: View? = null
    private var currentPage = 1
    private var total = 0
    private var adapter: ContractOrderHistoryAdapter? = null
    private var otherType = TYPE_U_CONTRACT
    private var typeList: MutableList<String>? = null
    private var dataList: ArrayList<OrderBeanItem>? = null
    private var type = TYPE_ALL
    private var list: MutableList<String>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        wallet = arguments?.getParcelable(ConstData.WALLET)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delegation, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = ContractOrderHistoryAdapter(mContext!!, BR.listTabContractPlan,null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.id?.visibility = View.GONE
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        binding?.contractChoose?.setOnClickListener(this)
        binding?.btnAll?.setOnClickListener(this)
        binding?.timeChoose?.visibility = View.GONE
        TYPE_U_CONTRACT = getString(R.string.usdt_base_contract)
        TYPE_COIN_CONTRACT = getString(R.string.coin_base_contract)
        TYPE_ALL = getString(R.string.all)
        typeList = ArrayList()
        typeList!!.add(TYPE_U_CONTRACT)
        typeList!!.add(TYPE_COIN_CONTRACT)
        otherType = TYPE_U_CONTRACT
        type = TYPE_ALL
        getLimitPricePlanData()
        return layout
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.contract_choose -> {
                DeepControllerWindow(mContext as Activity, null, otherType, typeList, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        otherType = item
                        getLimitPricePlanData()
                        when(item){
                            TYPE_U_CONTRACT -> {
                                binding?.usdM?.setText(R.string.usdt_base_contract)
                            }
                            TYPE_COIN_CONTRACT -> {
                                binding?.usdM?.setText(R.string.coin_base_contract)
                                binding?.all?.setText(R.string.all)
                            }
                        }
                    }

                }).show()
            }
            R.id.btn_all -> {
                if (binding?.usdM?.text == getString(R.string.coin_base_contract)){
                    list = ArrayList()
                    list!!.add(TYPE_ALL)

                }
                else{
                    list = ArrayList()
                    list!!.add(TYPE_ALL)
                    list!!.add(TYPE_BTC)
                    list!!.add(TYPE_ETH)
                }
                DeepControllerWindow(mContext as Activity, null, type, list, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        type = item
                        getLimitPricePlanData()
                        when(item){
                            TYPE_ALL -> {
                                binding?.all?.setText(R.string.all)
                            }
                            TYPE_BTC -> {
                                binding?.all?.setText("BTCUSDT 永续")
                            }
                            TYPE_ETH -> {
                                binding?.all?.setText("ETHUSDT 永续")
                            }
                        }


                    }

                }).show()
            }
        }
    }
    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
        val extras = Bundle()
    }

    override fun onRefresh() {
        currentPage = 1
        getLimitPricePlanData()
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getLimitPricePlanData()
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    private fun show(dataList: ArrayList<OrderBeanItem>?){
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }
    //获取当前委托记录
    private fun getLimitPricePlanData() {
        //U本位
        if(otherType == TYPE_U_CONTRACT) {
            FutureApiServiceHelper.getOrderList(currentPage, 10, if(type == TYPE_ALL) null else if (type == TYPE_BTC) "btc_usdt" else "eth_usdt" , "FILLED", context, true,
                object : Callback<HttpRequestResultBean<OrderBean>>() {
                    override fun error(type: Int, error: Any?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                    }

                    override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                        if (returnData != null) {
                            total = returnData.result!!.total
                            dataList = returnData.result?.items
                            show(dataList)
                        }
                    }
                })
        }
        //币本位
        else{
            FutureApiServiceHelper.getCoinOrderList(1, 10, null, Constants.UNFINISHED, context, false,
                object : Callback<HttpRequestResultBean<OrderBean>>() {
                    override fun error(type: Int, error: Any?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                    }

                    override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                        if (returnData != null) {
                            val orderData = returnData.result
                        }
                    }
                })
        }
    }




}