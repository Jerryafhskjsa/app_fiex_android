package com.black.clutter.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.model.FryingExchangeRates
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityExchangeRatesBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.EXCHANGE_RATES])
class ExchangeRatesActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    private var binding: ActivityExchangeRatesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = getApplication() as BaseApplication
        initExchangeRates()
        onExchangeRatesChanged()
    }

    private fun initExchangeRates() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_exchange_rates)
        binding?.exchangeCny?.setOnClickListener(this)
        binding?.exchangeCny?.tag = application!!.getExhcangeRates(FryingExchangeRates.cny)
        binding?.exchangeUsd?.setOnClickListener(this)
        binding?.exchangeUsd?.tag = application!!.getExhcangeRates(FryingExchangeRates.usd)
        binding?.exchangeJpy?.setOnClickListener(this)
        binding?.exchangeJpy?.tag = application!!.getExhcangeRates(FryingExchangeRates.jpy)
        binding?.exchangeKrw?.setOnClickListener(this)
        binding?.exchangeKrw?.tag = application!!.getExhcangeRates(FryingExchangeRates.krw)
        binding?.exchangeVnd?.setOnClickListener(this)
        binding?.exchangeVnd?.tag = application!!.getExhcangeRates(FryingExchangeRates.vnd)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.exchange_rates)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.exchange_cny, R.id.exchange_usd, R.id.exchange_jpy, R.id.exchange_krw, R.id.exchange_vnd -> changeExchangeRates(
                v.tag as FryingExchangeRates
            )
        }
    }

    private fun changeExchangeRates(exchangeRates: FryingExchangeRates) {
        if (exchangeRates.rateCode != 0 && exchangeRates.rateCode != 1) {
            FryingUtil.showToast(this, getString(R.string.please_waiting))
            return
        }
        if (exchangeRates != ExchangeRatesUtil.getExchangeRatesSetting(this)) {
            ExchangeRatesUtil.setExChangeRatesSetting(this, exchangeRates)
            onExchangeRatesChanged()
            BlackRouter.getInstance().build(RouterConstData.START_PAGE)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .go(this)
        }
    }


     private fun onExchangeRatesChanged() {
        var exchangeRates = ExchangeRatesUtil.getExchangeRatesSetting(this)
        refreshExchangeRatesChecked(exchangeRates, binding?.exchangeCny)
        refreshExchangeRatesChecked(exchangeRates, binding?.exchangeUsd)
        refreshExchangeRatesChecked(exchangeRates, binding?.exchangeJpy)
        refreshExchangeRatesChecked(exchangeRates, binding?.exchangeKrw)
        refreshExchangeRatesChecked(exchangeRates, binding?.exchangeVnd)
    }

    private fun refreshExchangeRatesChecked(rate: FryingExchangeRates?, textView: TextView?) {
        if (rate == null || textView == null) {
            return
        }
        var tag = textView.tag
        if(tag is FryingExchangeRates){
            if (rate.rateCode == tag.rateCode) {
                CommonUtil.setTextViewCompoundDrawable(textView, SkinCompatResources.getDrawable(this, R.drawable.icon_language_ok), 2)
            } else {
                CommonUtil.setTextViewCompoundDrawable(textView, null, 2)
            }
        }else{
            CommonUtil.setTextViewCompoundDrawable(textView, null, 2)
        }
    }
}