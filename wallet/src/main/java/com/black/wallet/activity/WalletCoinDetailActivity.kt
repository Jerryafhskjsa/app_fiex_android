package com.black.wallet.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.model.wallet.WalletBill
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityAssetCoinDetailBinding

@Route(value = [RouterConstData.WALLET_COIN_DETAIL])
class WalletCoinDetailActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityAssetCoinDetailBinding? = null
    private var walletBill: WalletBill? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_coin_detail)
        walletBill = intent.getParcelableExtra(ConstData.WALLET_BILL)
        binding?.dateDes?.setText(if (walletBill?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", walletBill?.createdTime!!))
        binding?.typeDes?.setText(walletBill?.getType(mContext))
        binding?.remarkDes?.setText("XT3S/" + walletBill?.coin)
        if (walletBill == null && walletBill?.availableChange?.toDouble() !=0.0) {
            binding?.chinese?.setText(
                NumberUtil.formatNumberNoGroup(
                    walletBill?.availableChange?.toDouble(),
                    2,
                    8
                )
            )

        }
        if (walletBill == null && walletBill?.frozeChange?.toDouble() != 0.0) {
            binding?.chinese?.setText(
                NumberUtil.formatNumberNoGroup(
                    walletBill?.frozeChange?.toDouble(),
                    2,
                    8
                )
            )
        }
        binding?.chinese?.setText(if (walletBill?.availableChange == null) nullAmount else NumberUtil.formatNumberNoGroup(walletBill?.availableChange?.toDouble(), 2, 8))
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.bill_detail)
    }

    override fun onClick(v: View) {
        val i = v.id
    }
}