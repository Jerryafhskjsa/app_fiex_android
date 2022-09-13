package com.black.wallet.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivityAssetCoinDetailBinding

@Route(value = [RouterConstData.WALLET_COIN_DETAIL])
class WalletCoinDetailActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityAssetCoinDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_asset_coin_detail)
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