package com.black.wallet.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.WalletApiService
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.manager.ApiManager
import com.black.base.model.AssetTransfer
import com.black.base.model.CanTransferCoin
import com.black.base.model.HttpRequestResultData
import com.black.base.model.PagingData
import com.black.base.model.wallet.WalletTransferRecord
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.base.view.ChooseCoinControllerWindow
import com.black.base.view.TransferRecordFilterControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletTransferRecordAdapter
import com.black.wallet.databinding.ActivityWalletTransferRecordBinding
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.WALLET_TRANSFER_RECORD], beforePath = RouterConstData.LOGIN)
class WalletTransferRecordActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener,View.OnClickListener {
    private var pair: String? = null

    private var binding: ActivityWalletTransferRecordBinding? = null

    private var adapter: WalletTransferRecordAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var all = true
    private var formWallet:AssetTransfer? = AssetTransfer()
    private var toWallet:AssetTransfer? = AssetTransfer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pair = intent.getStringExtra(ConstData.PAIR)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_transfer_record)

        binding?.type?.setText(pair?.replace("_", "/") ?: "")
        var selectIcon: ImageButton? = binding?.root?.findViewById(R.id.img_action_bar_right)
        selectIcon?.setImageDrawable(getDrawable(R.drawable.icon_selected_type))
        selectIcon?.visibility = View.VISIBLE
        selectIcon?.setOnClickListener(this)

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = WalletTransferRecordAdapter(mContext, BR.listItemWalletTransferRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        getRecord(true)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.img_action_bar_right ->{
                showChooseDialog()
            }
        }
    }

    private fun showChooseDialog(){
        TransferRecordFilterControllerWindow(mContext as Activity, getString(R.string.transfer_record_filter), formWallet,
            toWallet,
            all,
            object : TransferRecordFilterControllerWindow.OnReturnListener{
                override fun onConfirm(
                    window: TransferRecordFilterControllerWindow,
                    all: Boolean?,
                    from: AssetTransfer?,
                    to: AssetTransfer?
                ) {

                }

                override fun onWalletTypeChoose(
                    window: TransferRecordFilterControllerWindow,
                    item: AssetTransfer?,
                    type: Int?
                ) {
                    if(type == 1){
                        formWallet = item
                    }else{
                        toWallet = item
                    }
                    showCoinChooseDialog()
                }
            }).show()
    }

    private fun showCoinChooseDialog(){
        var transformCoin = ArrayList<CanTransferCoin?>()
        var coin1 = CanTransferCoin()
        coin1.coin = "BTC"
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        transformCoin.add(coin1)
        ChooseCoinControllerWindow(mContext as Activity, getString(R.string.select_coin),
            transformCoin,
            object : ChooseCoinControllerWindow.OnReturnListener<CanTransferCoin?> {
                override fun onReturn(window: ChooseCoinControllerWindow<CanTransferCoin?>, item: CanTransferCoin?) {
                    showChooseDialog()
                }
            }).show()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        pair = intent.getStringExtra(ConstData.PAIR)
        return getString(R.string.transfer_record)
    }

    override fun onRefresh() {
        currentPage = 1
        getRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getRecord(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    private fun getRecord(isShowLoading: Boolean) {
        ApiManager.build(this).getService(WalletApiService::class.java)
                ?.getWalletTransferRecord(currentPage, 10, pair)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<WalletTransferRecord?>?>?>() {

                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                        showData(null)
                    }

                    override fun callback(returnData: HttpRequestResultData<PagingData<WalletTransferRecord?>?>?) {
                        if (returnData?.code != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            total = returnData.data?.totalCount!!
                            val dataList = returnData.data?.data
                            showData(dataList)
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }

                }))
    }

    private fun showData(dataList: ArrayList<WalletTransferRecord?>?) {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }
}