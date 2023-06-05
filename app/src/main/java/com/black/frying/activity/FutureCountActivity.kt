package com.black.frying.activity

import android.annotation.SuppressLint
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
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ContractMultipleSelectWindow
import com.black.base.view.ContractMultipleSelectWindow2
import com.black.base.view.PairStatusPopupWindow
import com.black.router.annotation.Route
import com.black.wallet.R
import com.fbsex.exchange.databinding.FutureCountActivityBinding
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal


@Route(value = [RouterConstData.FUTURE_COUNT_ACTIVITY])

class FutureCountActivity: BaseActivity(), View.OnClickListener {
    companion object{
        var TAB_ONE: String = "ONE"
        var TAB_TWO: String = "TWO"
        var TAB_THREE: String = "THREE"
        var TAB_FOUR: String = "FOUR"
        var TAB_FIVE: String = "FIVE"
        var TAB_SIX: String = "SIX"
    }
    private var binding: FutureCountActivityBinding? = null
    private var num1: Int = 100
    private var type = TAB_ONE
    private var color1: Int? = null
    private var color2: Int? = null


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
        binding?.btnConfirm?.setOnClickListener(this)
        color1 = SkinCompatResources.getColor(mContext, R.color.T10)
        color2 = SkinCompatResources.getColor(mContext, R.color.T9)
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
                getType()
            }

            com.fbsex.exchange.R.id.shouyi -> {
                binding?.shouyi?.isChecked = true
                binding?.qiangping?.isChecked = false
                binding?.pingcang?.isChecked = false
                binding?.resultShouyi?.visibility = View.VISIBLE
                binding?.price?.visibility = View.GONE
                binding?.xuanzhe?.visibility = View.VISIBLE
                binding?.tishi?.visibility = View.GONE
                getType()
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
                getType()
            }

            com.fbsex.exchange.R.id.duo -> {
                binding?.duo?.isChecked = true
                binding?.kong?.isChecked = false
                getType()

            }

            com.fbsex.exchange.R.id.kong -> {
                binding?.duo?.isChecked = false
                binding?.kong?.isChecked = true
                getType()

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
                            num1 = beishu
                            binding?.xuanzhe?.setText(beishu.toString() + "X")
                        }
                    }).show()

            }

            com.fbsex.exchange.R.id.btn_confirm -> {
                getCount()
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

    private fun getType(){
        if ( binding?.duo?.isChecked == true && binding?.shouyi?.isChecked == true) {
            type = TAB_ONE
        }
        if ( binding?.kong?.isChecked == true && binding?.shouyi?.isChecked == true) {
            type = TAB_TWO
        }
        if ( binding?.duo?.isChecked == true && binding?.qiangping?.isChecked == true) {
            type = TAB_THREE
        }
        if ( binding?.kong?.isChecked == true && binding?.qiangping?.isChecked == true) {
            type = TAB_FOUR
        }
        if ( binding?.duo?.isChecked == true && binding?.pingcang?.isChecked == true) {
            type = TAB_FIVE
        }
        if ( binding?.kong?.isChecked == true && binding?.pingcang?.isChecked == true) {
            type = TAB_SIX
        }
    }
    @SuppressLint("SetTextI18n")
    private fun getCount(){
        if (binding?.one?.text?.isEmpty() == true || binding?.two?.text?.isEmpty() == true || binding?.three?.text?.isEmpty() == true){
            FryingUtil.showToast(mContext , "部分数据未填写，无法计算")
            return
        }
        val one  = binding?.one?.text.toString()
        val two  = binding?.two?.text.toString()
        val three  = binding?.three?.text.toString()
        if (type == TAB_ONE){
            val num2 = BigDecimal(one).divide(BigDecimal(num1)).multiply(BigDecimal(three))
            val num3 = (BigDecimal(two).minus(BigDecimal(one))).multiply(BigDecimal(three))
            val num4 = num3.divide(num2).multiply(BigDecimal(100))
            binding?.second?.text = num2.toString() + "USDT"
            binding?.third?.text = num3.toString() + "USDT"
            binding?.fourth?.text = "$num4%"
            if (num3 > BigDecimal.ZERO){
                binding?.third?.setTextColor(color2!!)
            }else{
                binding?.third?.setTextColor(color1!!)
            }
            if (num4 > BigDecimal.ZERO){
                binding?.third?.setTextColor(color2!!)
            }else{
                binding?.third?.setTextColor(color1!!)
            }
        }
        if (type == TAB_TWO){
            val num2 = BigDecimal(one).divide(BigDecimal(num1)).multiply(BigDecimal(three))
            val num3 = (BigDecimal(one).minus(BigDecimal(two))).multiply(BigDecimal(three))
            val num4 = num3.divide(num2).multiply(BigDecimal(100))
            binding?.second?.text = num2.toString() + "USDT"
            binding?.third?.text = num3.toString() + "USDT"
            binding?.fourth?.text = "$num4%"
            if (num3 > BigDecimal.ZERO){
                binding?.third?.setTextColor(color2!!)
            }else{
                binding?.third?.setTextColor(color1!!)
            }
            if (num4 > BigDecimal.ZERO){
                binding?.fourth?.setTextColor(color2!!)
            }else{
                binding?.fourth?.setTextColor(color1!!)
            }
        }
        /* 全仓时:
        多仓强平价格 = 数量 * 面值 * 开仓均价 / (数量 * 面值 + 开仓均价 * dex)
        空仓强平价格 = 数量 * 面值 * 开仓均价 / (数量 * 面值 - 开仓均价 * dex)

        dex（共享保证金） = 钱包余额 - ∑逐仓仓位保证金 - ∑全仓维持保证金 - ∑委托保证金 + ∑除本仓位其他全仓仓位未实现盈亏

        逐仓时:
        多仓强平价格 = 开仓均价 * 数量 * 面值 / (数量 * 面值 + 开仓均价 * (仓位保证金 - 维持保证金))
        空仓平价格 = 开仓均价 * 数量 * 面值 / (数量 * 面值 + 开仓均价 * (维持保证金 - 仓位保证金))*/
        if (type == TAB_THREE){

        }
        if (type == TAB_FOUR){

        }
        if (type == TAB_FIVE){
            val num = (BigDecimal(three).divide(BigDecimal(two))).plus(BigDecimal(one))
            binding?.first?.text = num.toString() + "USDT"
                   }
        if (type == TAB_SIX){
            val num = (BigDecimal(one)).minus(BigDecimal(three).divide(BigDecimal(two)))
            binding?.first?.text = num.toString() + "USDT"
        }
    }
}