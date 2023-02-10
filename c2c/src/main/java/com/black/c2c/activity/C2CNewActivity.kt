package com.black.c2c.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.view.*
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.base.widget.SpanCheckedTextView
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMainBinding
import com.black.c2c.fragment.C2CCustomerBuyItemFragment
import com.black.c2c.fragment.C2CCustomerFragment
import com.black.c2c.fragment.C2COneKeyFragment
import com.black.frying.fragment.EmptyC2cFragment
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.google.android.material.tabs.TabLayout
import java.util.*

@Route(value = [RouterConstData.C2C_NEW])
 class C2CNewActivity : BaseActionBarActivity(), View.OnClickListener{
    companion object {
        private const val TAB_ONE_KEY = 1
        private const val TAB_CUSTOMER = 2
        private val TAB_TITLES = arrayOfNulls<String>(6)
        private var TAB_USDT: String? = null
        private var TAB_BTC: String? = null
        private var TAB_BUSD: String? = null
        private var TAB_BNB: String? = null
        private var TAB_ETH: String? = null
        private var TAB_DOGE: String? = null
        private val TAB_SELF = "自选区"
        private val TAB_QUCILK = "快捷区"

    }

    private var binding: ActivityC2cMainBinding? = null
    private var userInfo: UserInfo? = null
    private var currentTab = 0
    private var type = 0
    private  var otherType = false
    private  var otherType2 = false
    private  var otherType3 = false
    private var tab = TAB_SELF
    private var fManager: FragmentManager? = null
    private var typeList: MutableList<String>? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var  c2CCustomerBuyItemFragment: C2CCustomerBuyItemFragment? = null
    private var c2COneKeyFragment: C2COneKeyFragment? = null
    private var c2CCustomerFragment: C2CCustomerFragment? = null
    private var supportCoins: ArrayList<C2CSupportCoin?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_main)
        binding?.c2cOneKey?.setOnClickListener(this)
        binding?.c2cCustomer?.setOnClickListener(this)
        binding?.moneyChoose?.setOnClickListener(this)
        binding?.areaChoose?.setOnClickListener(this)
        binding?.actionBarBack?.setOnClickListener(this)
        binding?.bills?.setOnClickListener(this)
        binding?.rate?.setOnClickListener(this)
        binding?.filterTitle?.setOnClickListener(this)
        binding?.methodChoose?.setOnClickListener(this)
         binding?.settings?.setOnClickListener(this)
        binding?.person?.setOnClickListener(this)
        fManager = supportFragmentManager
        typeList = ArrayList()
        typeList!!.add(TAB_SELF)
        typeList!!.add(TAB_QUCILK)
       // selectTab(TAB_ONE_KEY)
        "USDT".also {
            TAB_USDT = it
            TAB_TITLES[0] = TAB_USDT
        }
        "BTC".also {
            TAB_BTC = it
            TAB_TITLES[1] = TAB_BTC
        }
        "BUSD".also {
            TAB_BUSD = it
            TAB_TITLES[2] = TAB_BUSD
        }

        "BNB".also {
            TAB_BNB = it
            TAB_TITLES[3] = TAB_BNB
        }
        "ETH".also {
            TAB_ETH = it
            TAB_TITLES[4] = TAB_ETH
        }

        "DOGE".also {
            TAB_DOGE = it
            TAB_TITLES[5] = TAB_DOGE
        }

        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_FIXED
        init()
        binding?.viewPager?.adapter =
            object : FragmentStatePagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return fragmentList!![position]
                }

                override fun getCount(): Int {
                    return fragmentList!!.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return TAB_TITLES[position]
                }

                override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

                }
            }
        binding?.tabLayout?.setupWithViewPager(binding?.viewPager, true)

        for (i in TAB_TITLES.indices) {
            try {
                val tab: TabLayout.Tab? = binding?.tabLayout?.getTabAt(i)
                if (tab != null) {
                    tab.setCustomView(R.layout.view_tab_normal)
                    if (tab.customView != null) {
                        val textView =
                            tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                        textView.text = TAB_TITLES[i]
                    }
                }
            } catch (throwable: Throwable) {
                FryingUtil.printError(throwable)
            }
        }
       // c2CSupportCoins
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.c2c_one_key) {
            init()
            type = 0
            refresh(type)
           // selectTab(TAB_ONE_KEY)
        } else if (id == R.id.c2c_customer) {
            init()
            type = 1
            refresh(type)
           // selectTab(TAB_CUSTOMER)
        }
        else if (id == R.id.money_choose){
                binding?.moneyChoose?.isChecked = true
                moneyDialog()
        }
        else if (id == R.id.settings){
            settingsDialog()
        }
        else if (id == R.id.action_bar_back){
            finish()
            return
        }

        else if (id == R.id.area_choose){
            DeepControllerWindow(mContext as Activity, null, tab , typeList, object : DeepControllerWindow.OnReturnListener<String> {
                override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                    window.dismiss()
                    tab = item
                    when(item){
                        TAB_SELF -> {
                            binding?.areaChoose?.setText(R.string.self_selection_area)
                        }
                        TAB_QUCILK -> {
                            BlackRouter.getInstance().build(RouterConstData.C2C_QIULK).go(mContext)
                        }
                    }
                }

            }).show()
        }
        else if (id == R.id.rate){

        }
        else if (id == R.id.bills){
            BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
        }
        else if (id == R.id.person){
            BlackRouter.getInstance().build(RouterConstData.C2C_MINE).go(mContext)
        }
        else if (id == R.id.method_choose){

                binding?.methodChoose?.isChecked = true
                methodDialog()

        }
        else if (id == R.id.filter_title){

                binding?.filterTitle?.isChecked = true
                filterDialog()
        }
    }
    private fun moneyDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.money_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.TOP
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 550
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            dialog.dismiss()
            binding?.moneyChoose?.isChecked = false
        }
        dialog.findViewById<View>(R.id.one).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "100"
        }
        dialog.findViewById<View>(R.id.two).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "1000"
        }
        dialog.findViewById<View>(R.id.three).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "5000"
        }
        dialog.findViewById<View>(R.id.four).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "10000"
        }
        dialog.findViewById<View>(R.id.five).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "100000"
        }
        dialog.findViewById<View>(R.id.six).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = "200000"
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.findViewById<TextView>(R.id.put_money).text = ""
        }
    }
    private fun methodDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.money_choose_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.TOP
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 550
            //设置dialog动画
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            dialog.dismiss()
            binding?.methodChoose?.isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.one).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.two).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.three).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.four).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = true
        }
    }
    private fun filterDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.filter_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            params.dimAmount = 0.2f
            //设置背景昏暗度
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            dialog.dismiss()
            binding?.filterTitle?.isChecked = false
        }
        dialog.findViewById<View>(R.id.reset).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.first).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.second).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.third).isChecked = false
            dialog.findViewById<SwitchCompat>(R.id.finger_print_password).isChecked = false
            dialog.findViewById<SwitchCompat>(R.id.message).isChecked = false
            dialog.findViewById<TextView>(R.id.put_money).text = ""
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.one).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.two).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.three).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.four).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = true
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.first).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.first).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.second).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.third).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.second).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.first).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.second).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.third).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.third).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.first).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.second).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.third).isChecked = true
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.dismiss()
            binding?.filterTitle?.isChecked = false
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
        dialog.findViewById<SpanTextView>(R.id.merchant).setOnClickListener{
            v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_APPLY1).go(this)
        }
        dialog.findViewById<SpanTextView>(R.id.btn_bills).setOnClickListener{
                v ->
            BlackRouter.getInstance().build(RouterConstData.C2C_BILLS).go(this)
        }
        dialog.show()
    }
    private fun init(){
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            val coinType = "USDT"
            bundle.putString(ConstData.COIN_TYPE,coinType)
            bundle.putBoolean("isVisibility", binding?.c2cOneKey?.isChecked ?: false)
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
//            assetsWalletFragment = it
//            assetsWalletFragment?.setEventListener(this)
        })
    }
    private fun refresh(type :Int){
        if (type == 0){
            binding?.c2cOneKey?.isChecked = false
            binding?.c2cCustomer?.isChecked = true
        }
        else{
            binding?.c2cOneKey?.isChecked = true
            binding?.c2cCustomer?.isChecked = false
        }
    }


}