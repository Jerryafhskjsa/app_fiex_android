package com.black.frying.fragment.assets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.Money
import com.black.base.model.user.UserBalance
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentAssetsWalletBinding
import java.math.RoundingMode

class AssetsWalletFragment : BaseFragment(),  View.OnClickListener {
    private var layout: View? = null
    private var isVisibility: Boolean = false
    private var binding: FragmentAssetsWalletBinding? = null
    private var eventListener:WalletEventResponseListener? = null


    fun setEventListener(listener: WalletEventResponseListener){
        this.eventListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        super.onCreate(savedInstanceState)
        if (layout != null) {
            return layout
        }
        isVisibility = if (arguments?.getBoolean("isVisibility", false) == null) false else arguments?.getBoolean("isVisibility", false)!!
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_assets_wallet, container, false)
        layout = binding?.root
        binding?.recharge?.setOnClickListener(this)
        binding?.extract?.setOnClickListener(this)
        binding?.transaction?.setOnClickListener(this)
        binding?.refreshLayout?.isFocusable = false
        binding?.refreshLayout?.isNestedScrollingEnabled = false
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                eventListener?.getAssetAllWallet(false)
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })
        return layout
    }



    override fun onClick(v: View) {
      when(v.id){
          R.id.recharge -> {
              val bundle = Bundle()
              bundle.putInt("USDT", ConstData.TAB_EXCHANGE)
              BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(this)
          }

          R.id.extract -> {
              val bundle = Bundle()
              bundle.putInt("USDT", ConstData.TAB_WITHDRAW)
              BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this)
          }
          /*R.id.transaction -> {
              BlackRouter.getInstance().build(RouterConstData.TRANSACTION).go(this)
          }
           */
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
            } else {
                val total: Money? = binding?.moneyTotal?.tag as Money?
                val total2: Money? = binding?.futureUsdt?.tag as Money?
                binding?.spotUsdt?.setText(if (total?.usdt == null) "0.0USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt, 8, 2, 2) + "USDT")
                binding?.futureUsdt?.setText(if (total2?.tigerUsdt == null) "0.0USDT" else NumberUtil.formatNumberDynamicScaleNoGroup(total2.tigerUsdt, 8, 2, 2) + "USDT")
                binding?.financialUsdt?.setText("0.0USDT")
                binding?.capitalUsdt?.setText("0.0USDT")
                binding?.moneyTotalcny?.setText(if(total?.cny == null && total2?.tigercny == null) "≈ 0.0CNY" else if( total?.cny == null && total2?.tigercny != null) "≈" + NumberUtil.formatNumberDynamicScaleNoGroup(total2.tigercny, 8, 2, 2)  + "CNY" else if(total?.cny != null && total2?.tigercny == null) "≈" + NumberUtil.formatNumberDynamicScaleNoGroup(total.cny, 8, 2, 2) + "CNY" else "≈" + NumberUtil.formatNumberDynamicScaleNoGroup(total?.cny!! + total2?.tigercny!!, 8, 2, 2) + "CNY")
                binding?.moneyTotal?.setText(if (total?.usdt == null && total2?.tigerUsdt == null) "0.0USDT" else if(total?.usdt == null && total2?.tigerUsdt != null) NumberUtil.formatNumberDynamicScaleNoGroup(total2.tigerUsdt , 8, 2, 2) + "USDT" else if(total?.usdt != null && total2?.tigerUsdt == null) NumberUtil.formatNumberDynamicScaleNoGroup(total.usdt, 8, 2, 2) + "USDT"  else NumberUtil.formatNumberDynamicScaleNoGroup(total?.usdt!! + total2?.tigerUsdt!! , 8, 2, 2) )        }
        }
    }

    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
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

        fun assetWalletSearch(searchKey: String, walletType: Int) {
        }
    }

}