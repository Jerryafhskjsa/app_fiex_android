package com.black.c2c.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import kotlinx.android.synthetic.main.activity_c2c_buy.*

@Route(value = [RouterConstData.C2C_BUY])
class C2CBuyActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
        private var TAB_PAYPAID: String? = "PayPai"
    }
    private var binding: ActivityC2cBuyBinding? = null
    private var cointype = "USDT"
    private var payChain: String? = null
    private var chainNames: MutableList<String?>? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_buy)
        binding?.accont?.setOnClickListener(this)
        binding?.accont?.setOnClickListener(this)
        binding?.amount?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.putMoney?.addTextChangedListener(watcher)
        binding?.putAmount?.addTextChangedListener(watcher)
        binding?.btnConfirm?.setText(getString(R.string.buy_02) + cointype)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)
        payChain = TAB_CARDS
        chainNames = ArrayList()
        chainNames?.add(TAB_CARDS)
        chainNames?.add(TAB_IDPAY)
        chainNames?.add(TAB_WEIXIN)
        chainNames?.add(TAB_PAYPAID)
        checkClickable()
    }
    override fun getTitleText(): String? {
        return getString(R.string.buy_02) + cointype
    }
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_confirm){
            choosePayMethodWindow()
        }
        else if (id == R.id.unit_price){
        }
        else if (id == R.id.amount){
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.amount?.isChecked = true
            binding?.accont?.isChecked = false
            binding?.money?.visibility = View.VISIBLE
            binding?.amountAll?.visibility = View.GONE
            checkClickable()
        }
        else if (id == R.id.accont){
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.amount?.isChecked = false
            binding?.accont?.isChecked = true
            binding?.money?.visibility = View.GONE
            binding?.amountAll?.visibility = View.VISIBLE
            checkClickable()
        }
    }
    private fun choosePayMethodWindow() {
        if (payChain != null && chainNames!!.size > 0) {
            val chooseWalletDialog = ChooseWalletControllerWindow(this as Activity,
                getString(R.string.choose_pay),
                payChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(
                        window: ChooseWalletControllerWindow<String?>,
                        item: String?
                    ) {
                        payChain = item
                        val extras = Bundle()
                        extras.putString(ConstData.COIN_TYPE,cointype)
                        extras.putString(ConstData.C2C_ORDER, payChain)
                        extras.putString(ConstData.C2C_ORDER, payChain)
                        BlackRouter.getInstance().build(RouterConstData.C2C_ORDERS).with(extras).go(mContext)
                    }
                })
            chooseWalletDialog.show()
        }

    }
    private fun checkClickable(){
        if ( binding?.amount?.isChecked == false){
            var amount = binding?.putAmount?.text
            binding?.one?.text = amount
            binding?.two?.setText(amount)
        }
        if ( binding?.amount?.isChecked == true){
            var money = binding?.putMoney?.text
            binding?.one?.text = money
            binding?.two?.text = money

        }

    }
}