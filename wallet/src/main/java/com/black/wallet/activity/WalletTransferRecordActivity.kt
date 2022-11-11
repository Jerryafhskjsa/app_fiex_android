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
import com.black.base.model.HttpRequestResultData
import com.black.base.model.PagingData
import com.black.base.model.wallet.SupportAccount
import com.black.base.model.wallet.WalletTransferRecord
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.view.TransferRecordFilterControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletTransferRecordAdapter
import com.black.wallet.databinding.ActivityWalletTransferRecordBinding
import skin.support.content.res.SkinCompatResources
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.WALLET_TRANSFER_RECORD], beforePath = RouterConstData.LOGIN)
class WalletTransferRecordActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener,View.OnClickListener {
    private var pair: String? = null

    private var binding: ActivityWalletTransferRecordBinding? = null

    private var adapter: WalletTransferRecordAdapter? = null
    private var currentPage = 1
    private var pageSize = 10
    private var total = 0
    private var all:Boolean? = true
    private var supportFromAccoutTypeList:ArrayList<SupportAccount?>? = null
    private var supportToAccoutTypeList:ArrayList<SupportAccount?>? = null
    private var fromAccount:SupportAccount? = null
    private var toAccount:SupportAccount? = null
    private var filterDialog:TransferRecordFilterControllerWindow? = null

    companion object{
        var fromAccountType = "SPOT"
        var toAccountType = "CONTRACT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pair = intent.getStringExtra(ConstData.PAIR)
        supportFromAccoutTypeList = intent.getParcelableArrayListExtra(ConstData.ASSET_SUPPORT_SPOT_ACCOUNT_TYPE)
        supportToAccoutTypeList = intent.getParcelableArrayListExtra(ConstData.ASSET_SUPPORT_OTHER_ACCOUNT_TYPE)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_transfer_record)
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
        getRecord(true,all)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.img_action_bar_right ->{
                showChooseDialog()
            }
        }
    }

    private fun showChooseDialog(){
        filterDialog =  TransferRecordFilterControllerWindow(mContext as Activity, getString(R.string.transfer_record_filter), fromAccount,
            toAccount,
            all,
            object : TransferRecordFilterControllerWindow.OnReturnListener{
                override fun onConfirm(
                    dialog: TransferRecordFilterControllerWindow,
                    all: Boolean?,
                    from: SupportAccount?,
                    to: SupportAccount?
                ) {
                    dialog.dismiss()
                    getRecord(true,all)
                }
                override fun onWalletTypeChoose(
                    dialog: TransferRecordFilterControllerWindow,
                    item: SupportAccount?,
                    type: Int?
                ) {
                    if(type == 1){
                        fromAccount = item
                        showWalletChooseDialog(fromAccountType)
                    }else{
                        toAccount = item
                        showWalletChooseDialog(toAccountType)
                    }
                }
                override fun onSelectedAll(dialog: TransferRecordFilterControllerWindow,selected:Boolean?) {
                    all = selected
                }
            })
        filterDialog?.show()
    }

    private fun showWalletChooseDialog(accountType:String){
        var clickAccout:SupportAccount? = null
        var selectDataList:ArrayList<SupportAccount?>? = null
        when(accountType){
            fromAccountType -> {
                clickAccout = fromAccount
                selectDataList = supportFromAccoutTypeList
            }
            toAccountType -> {
                clickAccout = toAccount
                selectDataList = supportToAccoutTypeList
            }
        }
        ChooseWalletControllerWindow(mContext as Activity,
            getString(R.string.select_wallet),
            clickAccout,
            selectDataList,
            object : ChooseWalletControllerWindow.OnReturnListener<SupportAccount?> {
                override fun onReturn(window: ChooseWalletControllerWindow<SupportAccount?>, item: SupportAccount?) {
                    when(accountType){
                        AssetTransferActivity.fromAccountType -> {
                            fromAccount = item
                            fromAccount?.selected = true
                            filterDialog?.getFromView()?.text = fromAccount?.name
                        }
                        AssetTransferActivity.toAccountType -> {
                            toAccount = item
                            toAccount?.selected = true
                            filterDialog?.getToView()?.text = toAccount?.name
                        }
                    }
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
        getRecord(false,all)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getRecord(false,all)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    private fun getRecord(isShowLoading: Boolean,all:Boolean?) {
        if(all == false){
            if(fromAccount == null || toAccount == null){
                FryingUtil.showToast(mContext,getString(R.string.pls_selected_query))
                return
            }
        }
        ApiManager.build(this,UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
                ?.getWalletTransferRecord(null,currentPage, pageSize,
                    if(all == true)fromAccount?.type else null,if(all == true) toAccount?.type else null)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<WalletTransferRecord?>?>?>() {
                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                        showData(null)
                    }
                    override fun callback(returnData: HttpRequestResultData<PagingData<WalletTransferRecord?>?>?) {
                        if (returnData?.code != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            total = returnData.data?.totalCount!!
                            val dataList = returnData.data?.records
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