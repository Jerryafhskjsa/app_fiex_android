package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ChooseWalletControllerWindow
import com.black.base.view.DeepControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMainBinding
import com.black.c2c.databinding.ActivityC2cOldBinding
import com.black.c2c.fragment.C2CCustomerFragment
import com.black.c2c.fragment.C2COneKeyFragment
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import kotlin.collections.ArrayList

@Route(value = [RouterConstData.C2C_QIULK])
class C2CQiulkActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private const val TAB_ONE_KEY = 1
        private const val TAB_CUSTOMER = 2
        private val TAB_TITLES = arrayOfNulls<String>(6)
        private var TAB_CARDS: String? = null
        private var TAB_IDPAY: String? = null
        private var TAB_WEIXIN: String? = null
        private var TAB_PAYPAID: String? = "PayPai"
        private var TAB_ETH: String? = null
        private var TAB_DOGE: String? = null
        private val TAB_SELF = "自选区"
        private val TAB_QUCILK = "快捷区"

    }

    private var binding: ActivityC2cOldBinding? = null
    private var currentTab = 0
    private var type = 0
    private  var otherType = false
    private  var otherType2 = false
    private  var otherType3 = false
    private var tab = TAB_QUCILK
    private var payChain: String? = null
    private var fManager: FragmentManager? = null
    private var typeList: MutableList<String>? = null
    private var chainNames: MutableList<String?>? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var c2COneKeyFragment: C2COneKeyFragment? = null
    private var c2CCustomerFragment: C2CCustomerFragment? = null
    private var supportCoins: ArrayList<C2CSupportCoin?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_old)
        binding?.c2cOneKey?.setOnClickListener(this)
        binding?.c2cCustomer?.setOnClickListener(this)
        binding?.one?.setOnClickListener(this)
        binding?.two?.setOnClickListener(this)
        binding?.three?.setOnClickListener(this)
        binding?.four?.setOnClickListener(this)
        binding?.five?.setOnClickListener(this)
        binding?.six?.setOnClickListener(this)
        binding?.sven?.setOnClickListener(this)
        binding?.first?.setOnClickListener(this)
        binding?.second?.setOnClickListener(this)
        binding?.third?.setOnClickListener(this)
        binding?.fourth?.setOnClickListener(this)
        binding?.fifth?.setOnClickListener(this)
        binding?.sixth?.setOnClickListener(this)
        binding?.areaChoose?.setOnClickListener(this)
        binding?.bills?.setOnClickListener(this)
        binding?.rate?.setOnClickListener(this)
        binding?.settings?.setOnClickListener(this)
        binding?.person?.setOnClickListener(this)
        binding?.idPayLayout?.setOnClickListener(this)
        binding?.btnConfirm?.setOnClickListener(this)
        binding?.quilkPoint?.setOnClickListener(this)
        binding?.accont?.setOnClickListener(this)
        binding?.amount?.setOnClickListener(this)
        binding?.btnConfirmSale?.setOnClickListener(this)
        binding?.quilkPointSale?.setOnClickListener(this)
        binding?.adviceNull?.setOnClickListener(this)
        binding?.btnConfirmSale?.setOnClickListener(this)
        TAB_CARDS = getString(R.string.cards)
        TAB_IDPAY = getString(R.string.id_pay)
        TAB_WEIXIN = getString(R.string.wei_xin)
        payChain = TAB_CARDS
        fManager = supportFragmentManager
        typeList = ArrayList()
        typeList!!.add(TAB_SELF)
        typeList!!.add(TAB_QUCILK)
        chainNames = ArrayList()
        chainNames?.add(TAB_CARDS)
        chainNames?.add(TAB_IDPAY)
        chainNames?.add(TAB_WEIXIN)
        chainNames?.add(TAB_PAYPAID)

    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.c2c_one_key) {
            type = 0
            refresh(type)
            // selectTab(TAB_ONE_KEY)
        } else if (id == R.id.c2c_customer) {
            type = 1
            refresh(type)
            // selectTab(TAB_CUSTOMER)
        }
       else if (id == R.id.area_choose){
            DeepControllerWindow(mContext as Activity, null, tab , typeList, object : DeepControllerWindow.OnReturnListener<String> {
                override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                    window.dismiss()
                    tab = item
                    when(item){
                        TAB_SELF -> {
                            BlackRouter.getInstance().build(RouterConstData.C2C_NEW).go(mContext)
                        }
                    }
                }

            }).show()
        }
        else if (id == R.id.rate){

        }
        else if (id == R.id.bills){

        }
        else if (id == R.id.person){
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }
        else if (id == R.id.btn_confirm){
            BlackRouter.getInstance().build(RouterConstData.C2C_BUY).go(this)
        }
        else if (id == R.id.btn_confirm_sale){
            BlackRouter.getInstance().build(RouterConstData.C2C_SELL).go(this)
        }
        else if (id == R.id.id_pay_layout){
            choosePayMethodWindow()
        }
        else if (id == R.id.settings){
            settingsDialog()
        }

        else if (id == R.id.one){
            binding?.one?.isChecked = true
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
        }
        else if (id == R.id.two){
            binding?.one?.isChecked = false
            binding?.two?.isChecked = true
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
        }
        else if (id == R.id.three){
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = true
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
        }
        else if (id == R.id.four){
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = true
            binding?.five?.isChecked = false
            binding?.six?.isChecked = false
        }
        else if (id == R.id.five){
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = true
            binding?.six?.isChecked = false
        }
        else if (id == R.id.six){
            binding?.one?.isChecked = false
            binding?.two?.isChecked = false
            binding?.three?.isChecked = false
            binding?.four?.isChecked = false
            binding?.five?.isChecked = false
            binding?.six?.isChecked = true
        }
        else if (id == R.id.sven){
        }
        else if (id == R.id.first){
            binding?.putMoney?.setText("100")
        }
        else if (id == R.id.second){
            binding?.putMoney?.setText("500")
        }
        else if (id == R.id.third){
            binding?.putMoney?.setText("1000")
        }
        else if (id == R.id.fourth){
            binding?.putMoney?.setText("2000")
        }
        else if (id == R.id.fifth){
            binding?.putMoney?.setText("5000")
        }
        else if (id == R.id.sixth){
            binding?.putMoney?.setText("10000")
        }
        else if (id == R.id.amount){
            binding?.moneyAmount?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.VISIBLE
            binding?.barB?.visibility = View.GONE
            binding?.amount?.isChecked = true
            binding?.accont?.isChecked = false
            binding?.money?.visibility = View.VISIBLE
            binding?.moneyAccount?.visibility = View.GONE
        }
        else if (id == R.id.accont){
            binding?.moneyAmount?.visibility = View.GONE
            binding?.barB?.visibility = View.VISIBLE
            binding?.barA?.visibility = View.GONE
            binding?.amount?.isChecked = false
            binding?.accont?.isChecked = true
            binding?.money?.visibility = View.GONE
            binding?.moneyAccount?.visibility = View.VISIBLE
        }
        else if (id == R.id.quilk_point){
            quilkDialog()
        }
        else if (id == R.id.quilk_point_sale){
            quilkSaleDialog()
        }
    }
    private fun refresh(type :Int){
        if (type == 0){
            binding?.moneyAmount?.visibility = View.VISIBLE
            binding?.c2cOneKey?.isChecked = false
            binding?.c2cCustomer?.isChecked = true
            binding?.quilkPointSale?.visibility = View.GONE
            binding?.btnConfirmSale?.visibility = View.GONE
            binding?.btnConfirm?.visibility = View.VISIBLE
            binding?.quilkPoint?.visibility = View.VISIBLE
        }
        else{
            binding?.moneyAmount?.visibility = View.GONE
            binding?.c2cOneKey?.isChecked = true
            binding?.c2cCustomer?.isChecked = false
            binding?.quilkPoint?.visibility = View.GONE
            binding?.btnConfirm?.visibility = View.GONE
            binding?.btnConfirmSale?.visibility = View.VISIBLE
            binding?.quilkPointSale?.visibility = View.VISIBLE
        }
    }
    private fun settingsDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.bills_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.TOP
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 80
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
    }
    private fun choosePayMethodWindow() {
        if (payChain != null && chainNames!!.size > 0) {
            val chooseWalletDialog = ChooseWalletControllerWindow(mContext as Activity,
                getString(R.string.choose_pay),
                payChain,
                chainNames,
                object : ChooseWalletControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(
                        window: ChooseWalletControllerWindow<String?>,
                        item: String?
                    ) {
                        binding?.idPayId?.setText(item)
                        payChain = item
                    }
                })
            chooseWalletDialog.show()
        }

    }
    private fun quilkDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.quilk_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener  {
                v -> dialog.dismiss()
        }
    }
    private fun quilkSaleDialog() {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.quilk_sale_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener  {
            v -> dialog.dismiss()
        }
    }
}