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
import com.black.wallet.databinding.ActivityWithdrawDetailBinding
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.WITHDRAW_DETAIL])
class WithdrawDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    private var record: FinancialRecord? = null
    private var binding: ActivityWithdrawDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        record = intent.getParcelableExtra("record")
        if (record == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw_detail)
        binding?.txid?.setOnClickListener(this)
        binding?.txid?.setText(if (record!!.txNetworkId == null) "" else record!!.txNetworkId)
        binding?.coinType?.setText(if (record!!.txCoin == null) "" else record!!.txCoin)
        binding?.amount?.setText(("-") + NumberUtil.formatNumberNoGroup(record!!.txAmount))
        binding?.confirmAmount?.setText(if (record?.txFee == null) nullAmount else NumberUtil.formatNumberNoGroup(record?.txFee))
        binding?.status?.setText(record!!.getStatusText(this))
        if (TextUtils.equals("0", record!!.txStatus)) {
            binding?.status?.setTextColor(SkinCompatResources.getColor(mContext, R.color.C1))
        } else {
            binding?.status?.setTextColor(SkinCompatResources.getColor(mContext, R.color.C3))
        }
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
        titleView.setText(R.string.withdraw_detail)
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