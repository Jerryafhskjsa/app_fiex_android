package com.black.frying.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.wallet.WalletBill
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.c2c.activity.C2CNewActivity
import com.black.frying.adapter.ThirdAdapters
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletBillAdapter
import com.fbsex.exchange.databinding.ActivityThreePaymentBinding
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.THREEPAYMENT])
class ThirdPayment: BaseActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener{
    private var binding: ActivityThreePaymentBinding? = null
    private var adapter: ThirdAdapters? = null
    private var hasMore = false
    private var list: ArrayList<payOrder?>? = null
    private var list1: MutableList<String>? = null
    private var list2: MutableList<String>? = null
    private var list3: MutableList<String>? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_three_payment)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.currentCoin?.setOnClickListener(this)
        binding?.currentChain?.setOnClickListener(this)
        binding?.extractAddress?.setOnClickListener(this)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = ThirdAdapters(this, com.fbsex.exchange.BR.listItemEntrustRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.recyclerView?.layoutManager = layoutManager

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
    }

    override fun onResume() {
        super.onResume()
        getList()
        getUrl()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.rechange)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btn_confirm -> {
                if (binding?.phoneCode?.text == null)
                    FryingUtil.showToast(mContext ,"Please put amount")
                else {
                    val bundle = Bundle()
                    val amount = binding?.phoneCode?.text.toString()
                    val country = binding?.country?.text.toString()
                    val current = binding?.currentCoin?.text.toString()
                    bundle.putString(ConstData.TITLE, amount)
                    bundle.putString(ConstData.BIRTH, country)
                    bundle.putString(ConstData.WALLET, current)
                    BlackRouter.getInstance().build(RouterConstData.CHOOSEPAYMENT).with(bundle)
                        .go(mContext)
                }
            }

            R.id.current_coin -> {
                list1?.add("ZAR")
                DeepControllerWindow(mContext as Activity, null, "ZAR" , list1, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()

                            binding?.currentCoin?.setText(item)

                    }

                }).show()
            }

            R.id.current_chain -> {
                list2?.add("USDT")
                DeepControllerWindow(mContext as Activity, null, "USDT" , list2, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()

                        binding?.currentChain?.setText(item)

                    }

                }).show()
            }

            R.id.extract_address -> {
                list3?.add("Sounth African online banking")
                DeepControllerWindow(mContext as Activity, null, "Sounth African online banking" , list3, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()

                        binding?.extractAddress?.setText(item)

                    }

                }).show()
            }

        }
        }
    override fun onRefresh() {
        currentPage = 1

    }

    override fun onLoad() {
        if (total > adapter?.count!! || hasMore) {
            currentPage += 1

        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!! || hasMore
    }


    private fun getUrl(){
        WalletApiServiceHelper.getDepositOrderCodeList(mContext, object : NormalCallback<HttpRequestResultData<Deposit<OrderCode?>?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<Deposit<OrderCode?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {

                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun getList(){
        WalletApiServiceHelper.getDepositOrderList(mContext, 1,10,object : NormalCallback<HttpRequestResultData<PagingData<payOrder?>?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<payOrder?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val list = returnData.data?.items
                    total = returnData.data?.total ?: 0
                    if (currentPage == 1) {
                        adapter?.data = (list)
                    } else {
                        adapter?.addAll(list)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}