package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cBuyBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import kotlinx.android.synthetic.main.activity_c2c_buy.*

@Route(value = [RouterConstData.C2C_BUY])
class C2CBuyActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
       // private var TAB_PAYPAID: String? = "PayPai"
    }
    private var binding: ActivityC2cBuyBinding? = null
    private var cointype = "USDT"
    private var payChain: String? = null
    private var rate = C2CApiServiceHelper?.coinUsdtPrice?.usdt
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
        binding?.payTime?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.unitPrice?.setOnClickListener(this)
        binding?.seller?.setOnClickListener(this)
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
        //chainNames?.add(TAB_PAYPAID)

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
        else if (id == R.id.seller){
            BlackRouter.getInstance().build(RouterConstData.C2C_SELLER).go(mContext)
        }
        else if (id == R.id.pay_time){
            payTimeDialog()
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
                        var num1 = binding?.putAmount?.text
                        var num2 = binding?.putMoney?.text
                        val extras = Bundle()
                        extras.putString(ConstData.COIN_TYPE,cointype)
                        extras.putString(ConstData.BUY_PRICE,rate.toString())
                        extras.putString(ConstData.COIN_TYPE,cointype)
                        extras.putString(ConstData.C2C_ORDERS, payChain)
                        BlackRouter.getInstance().build(RouterConstData.C2C_ORDERS).with(extras).go(mContext)
                    }
                })
            chooseWalletDialog.show()
        }

    }
    private fun payTimeDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.pay_time_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 1f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 380
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }

    }
    private fun checkClickable() {
        binding?.btnConfirm?.isEnabled = !(TextUtils.isEmpty(binding?.putAmount?.text.toString().trim { it <= ' ' })
                && TextUtils.isEmpty(binding?.putMoney?.text.toString().trim { it <= ' ' }))
        if (cointype == "usdt"){
            rate = C2CApiServiceHelper?.coinUsdtPrice?.usdt
        }
        if (cointype == "eth"){
            rate = C2CApiServiceHelper?.coinUsdtPrice?.eth
        }
        if (cointype == "btc"){
            rate = C2CApiServiceHelper?.coinUsdtPrice?.btc
        }
        binding?.unitPrice?.setText(rate.toString())
        if (binding?.amount?.isChecked == false) {
             var amount = CommonUtil.parseDouble(binding?.putAmount?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount * rate!!,
                        4,
                        4
                    )
                )
            } else {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
            }

        }
        if (binding?.amount?.isChecked == true) {
            var amount =
                CommonUtil.parseDouble(binding?.putMoney?.text.toString().trim { it <= ' ' })
            if (amount != null) {
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount,
                        4,
                        4
                    )
                )
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        amount / rate!!,
                        4,
                        4
                    )
                )
            } else {
                binding?.one?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
                binding?.two?.setText(
                    NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    )
                )
            }

        }
    }
}