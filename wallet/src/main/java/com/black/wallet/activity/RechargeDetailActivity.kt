package com.black.wallet.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.wallet.FinancialRecord
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityRechargeDetailBinding

@Route(value = [RouterConstData.RECHARGE_DETAIL])
class RechargeDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    private var record: FinancialRecord? = null
    private var binding: ActivityRechargeDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        record = intent.getParcelableExtra("record")
        if (record == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recharge_detail)
        binding?.txid?.setOnClickListener(this)
        binding?.coinType?.setText(if (record!!.txCoin == null) "" else record!!.txCoin)
        binding?.amount?.setText(String.format("%s%s", "+", NumberUtil.formatNumberNoGroup(record!!.txAmount)))
        binding?.confirmAmount?.setText(if (record!!.confirmations == null) nullAmount else record!!.confirmations)
        binding?.status?.setText(record!!.getStatusText(this))
        binding?.address?.setText(if (record!!.txToWallet == null) "" else record!!.txToWallet)
        if (TextUtils.isEmpty(record!!.memo)) {
            binding?.memoLayout?.visibility = View.GONE
        } else {
            binding?.memoLayout?.visibility = View.VISIBLE
            binding?.memo?.setText(record!!.memo)
        }
        binding?.txid?.setText(if (record!!.txNetworkId == null) "" else record!!.txNetworkId)
        binding?.time?.setText(record!!.createdTime)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back
    }

    override fun initActionBarView(view: View) {
        val titleView = view.findViewById<TextView>(R.id.action_bar_title)
        titleView.setText(R.string.recharge_detail)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.txid) {
            if (record != null && !TextUtils.isEmpty(record!!.explorerLink)) {
                val intent = Intent()
                intent.data = Uri.parse(record!!.explorerLink) //Url 就是你要打开的网址
                intent.action = Intent.ACTION_VIEW
                this.startActivity(intent) //启动浏览器
            }
        }
    }
}