package com.black.frying.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.model.ContractMultiChooseBean
import com.black.base.model.QuotationSet
import com.black.base.model.payOrder
import com.black.base.model.socket.PairStatus
import com.black.base.util.RouterConstData
import com.black.base.view.ContractMultipleSelectWindow
import com.black.base.view.ContractMultipleSelectWindow2
import com.black.base.view.PairStatusPopupWindow
import com.black.router.annotation.Route
import com.black.wallet.R
import com.fbsex.exchange.databinding.FutureCountActivityBinding


@Route(value = [RouterConstData.FUTURE_COUNT_ACTIVITY])

class FutureCountActivity: BaseActivity(), View.OnClickListener {
    private var binding: FutureCountActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.future_count_activity)
        binding?.shouyi?.setOnClickListener(this)
        binding?.pingcang?.setOnClickListener(this)
        binding?.qiangping?.setOnClickListener(this)
        binding?.duo?.setOnClickListener(this)
        binding?.kong?.setOnClickListener(this)
        binding?.positionDes?.setOnClickListener(this)
        binding?.xuanzhe?.setOnClickListener(this)
    }


    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "计算器"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View) {
        when (view.id) {
            com.fbsex.exchange.R.id.pingcang -> {
                binding?.shouyi?.isChecked = false
                binding?.qiangping?.isChecked = false
                binding?.pingcang?.isChecked = true
                binding?.resultShouyi?.visibility = View.GONE
                binding?.price?.visibility = View.VISIBLE
                binding?.xuanzhe?.visibility = View.GONE
                binding?.four?.hint = "平仓价格"
                binding?.tishi?.visibility = View.GONE
            }

            com.fbsex.exchange.R.id.shouyi -> {
                binding?.shouyi?.isChecked = true
                binding?.qiangping?.isChecked = false
                binding?.pingcang?.isChecked = false
                binding?.resultShouyi?.visibility = View.VISIBLE
                binding?.price?.visibility = View.GONE
                binding?.xuanzhe?.visibility = View.VISIBLE
                binding?.tishi?.visibility = View.GONE
            }

            com.fbsex.exchange.R.id.qiangping -> {
                binding?.shouyi?.isChecked = false
                binding?.qiangping?.isChecked = true
                binding?.pingcang?.isChecked = false
                binding?.resultShouyi?.visibility = View.GONE
                binding?.price?.visibility = View.VISIBLE
                binding?.xuanzhe?.visibility = View.VISIBLE
                binding?.four?.hint = "强平价格"
                binding?.tishi?.visibility = View.VISIBLE
            }

            com.fbsex.exchange.R.id.duo -> {
                binding?.duo?.isChecked = true
                binding?.kong?.isChecked = false

            }

            com.fbsex.exchange.R.id.kong -> {
                binding?.duo?.isChecked = false
                binding?.kong?.isChecked = true

            }

            com.fbsex.exchange.R.id.xuanzhe -> {
                val num: String = binding?.xuanzhe?.text.toString().filter{it != 'X' }
                ContractMultipleSelectWindow2(mContext as Activity,
                    getString(com.fbsex.exchange.R.string.contract_adjust),
                    num.toInt(),
                    object : ContractMultipleSelectWindow2.OnReturnListener {
                        override fun onReturn(
                            beishu: Int
                        ) {
                            binding?.xuanzhe?.setText(beishu.toString() + "X")
                        }
                    }).show()

            }

            com.fbsex.exchange.R.id.positionDes -> mContext.let {
                    val setData = ArrayList<QuotationSet?>(3)
                    val optionalUbaseSet = QuotationSet()
                    optionalUbaseSet.coinType = getString(com.fbsex.exchange.R.string.usdt)
                    optionalUbaseSet.name = getString(com.fbsex.exchange.R.string.usdt_base)
                    setData.add(optionalUbaseSet)
                    val optionalCoinBaseSet = QuotationSet()
                    optionalCoinBaseSet.coinType = getString(com.fbsex.exchange.R.string.usd)
                    optionalCoinBaseSet.name = getString(com.fbsex.exchange.R.string.coin_base)
                    setData.add(optionalCoinBaseSet)
                    PairStatusPopupWindow.getInstance(
                        mContext as Activity,
                        PairStatusPopupWindow.TYPE_FUTURE_ALL,
                        setData
                    )
                        .show(object : PairStatusPopupWindow.OnPairStatusSelectListener {
                            override fun onPairStatusSelected(pairStatus: PairStatus?) {
                                if (pairStatus == null) {
                                    return
                                }
                                //交易对切换
                                binding?.positionDes?.setText(pairStatus.pair?.uppercase())
                            }
                        })


                }
            }


    }
}