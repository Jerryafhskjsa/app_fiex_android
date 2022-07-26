package com.black.frying.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.TradeApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.socket.TradeOrder
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityEntrustDetailBinding
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.ENTRUST_DETAIL])
class EntrustDetailActivity : BaseActivity(), View.OnClickListener {
    private var tradeOrder: TradeOrder? = null
    private var amountPrecision = 0
    private var c1 = 0
    private var t5: Int = 0
    private var binding: ActivityEntrustDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_entrust_detail)
        c1 = SkinCompatResources.getColor(this, R.color.C1)
        t5 = SkinCompatResources.getColor(this, R.color.T5)
        tradeOrder = intent.getParcelableExtra(ConstData.TRADE_ORDER)
        amountPrecision = intent.getIntExtra(ConstData.AMOUNT_PRECISION, 4)

        binding!!.btnCancel.setOnClickListener(this)
        refreshViews()
    }

    override fun isStatusBarDark(): Boolean {
        return !CookieUtil.getNightMode(mContext)
    }

    override fun getTitleText(): String? {
        return getString(R.string.trade_detail)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_cancel -> TradeApiServiceHelper.cancelTradeOrder(mContext, tradeOrder!!.id, tradeOrder!!.pair, tradeOrder!!.direction, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        FryingUtil.showToast(mContext, getString(R.string.trade_cancel_success))
                        finish()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }
    }

    private fun refreshViews() {
        if (tradeOrder == null) {
            finish()
            return
        }

        val type = StringBuilder()
        if (TextUtils.equals(tradeOrder!!.direction, "ASK")) {
            binding!!.type.setTextColor(t5)
            type.append(getString(R.string.entrust_type_sale))
        } else if (TextUtils.equals(tradeOrder!!.direction, "BID")) {
            binding!!.type.setTextColor(c1)
            type.append(getString(R.string.entrust_type_buy))
        } else {
            binding!!.type.setTextColor(c1)
        }
        type.append(" ").append(if (tradeOrder!!.pair == null) "" else tradeOrder!!.pair!!.replace("_", "/"))
        binding!!.type.text = type.toString()
        binding!!.date.text = CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", tradeOrder!!.updateTime)
        binding!!.entrustPrice.text = if (tradeOrder!!.price == null || tradeOrder!!.price == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder!!.price)
        binding!!.entrustAmount.text = if (tradeOrder!!.totalAmount == null || tradeOrder!!.totalAmount == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder!!.totalAmount, amountPrecision, amountPrecision)
        val dealTotalAmount: Double = if (tradeOrder!!.dealAvgPrice != null && tradeOrder!!.dealAmount != null) tradeOrder!!.dealAvgPrice!! * tradeOrder!!.dealAmount!! else 0.toDouble()
        binding!!.dealTotalAmount.text = NumberUtil.formatNumberNoGroup(dealTotalAmount)
        binding!!.dealPrice.text = if (tradeOrder!!.dealAvgPrice == null || tradeOrder!!.dealAvgPrice == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder!!.dealAvgPrice)
        binding!!.dealAmount.text = if (tradeOrder!!.dealAmount == null || tradeOrder!!.dealAmount == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder!!.dealAmount, amountPrecision, amountPrecision)
        when (tradeOrder!!.status) {
            0 ->  //新订单，未结束的
                binding!!.btnCancel.visibility = View.VISIBLE
            1 ->  //完全成交
                binding!!.btnCancel.visibility = View.GONE
            else ->  //已撤销或者其他情况
                binding!!.btnCancel.visibility = View.GONE
        }
    }

}