package com.black.c2c.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.fragment.BaseFragment
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.widget.SpanCheckedTextView
import com.black.base.widget.SpanTextView
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cCustomerBuyBinding
import com.black.util.CommonUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_c2c_order.*
import java.util.*

class C2CCustomerBuyFragment : BaseFragment(), View.OnClickListener{
    companion object {

        private val TAB_TITLES = arrayOfNulls<String>(6)
        private var TAB_USDT: String? = null
        private var TAB_BTC: String? = null
        private var TAB_BUSD: String? = null
        private var TAB_BNB: String? = null
        private var TAB_ETH: String? = null
        private var TAB_DOGE: String? = null

    }

    private var binding: FragmentC2cCustomerBuyBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var money: String? = null
    private var paymethod: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_customer_buy, container, false)
        binding?.moneyChoose?.setOnClickListener(this)
        binding?.filterTitle?.setOnClickListener(this)
        binding?.methodChoose?.setOnClickListener(this)
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

        binding?.coinTab?.setSelectedTabIndicatorHeight(0)
        binding?.coinTab?.tabMode = TabLayout.MODE_SCROLLABLE
        init(paymethod,money)
        binding?.viewPager?.adapter =
            object : FragmentStatePagerAdapter(childFragmentManager) {
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
        binding?.coinTab?.setupWithViewPager(binding?.viewPager, true)

        for (i in TAB_TITLES.indices) {
            try {
                val tab: TabLayout.Tab? = binding?.coinTab?.getTabAt(i)
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
        return binding?.root
        // c2CSupportCoins
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.money_choose -> {
                binding?.moneyChoose?.isChecked = true
                moneyDialog()
            }

            R.id.method_choose -> {
                binding?.methodChoose?.isChecked = true
                methodDialog()
            }
            R.id.filter_title -> {
                binding?.filterTitle?.isChecked = true
                filterDialog()
            }
        }
    }
    @SuppressLint("CutPasteId")
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
                   binding?.moneyChoose?.isChecked = dialog.findViewById<EditText>(R.id.put_money) != null
            money = if (dialog.findViewById<EditText>(R.id.put_money) == null) null else dialog.findViewById<EditText>(R.id.put_money).text.toString().trim { it <= ' ' }
            init(paymethod, money)
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
    @SuppressLint("CutPasteId")
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
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            binding?.methodChoose?.isChecked = dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked || dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked || dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked
            var paymethod = if (dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked) "1" else null
            paymethod += if (dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked) ",2" else " "
            paymethod += if (dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked) ",3" else " "
            init(paymethod, money)
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.one).setOnClickListener {
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = true
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked = false
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.two).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked != true
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.three).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked != true
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.four).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked != true
        }
    }
    @SuppressLint("CutPasteId")
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
            binding?.filterTitle?.isChecked = dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked || dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked || dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked || dialog.findViewById<EditText>(R.id.put_money).text != null
            paymethod = if (dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked) "1" else null
            paymethod += if (dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked) ",2" else " "
            paymethod += if (dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked) ",3" else " "
            money =if (dialog.findViewById<EditText>(R.id.put_money) == null) null else dialog.findViewById<EditText>(R.id.put_money).text.toString().trim { it <= ' ' }
            init(paymethod, money)

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
            dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.two).isChecked != true
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.three).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.three).isChecked != true
        }
        dialog.findViewById<SpanCheckedTextView>(R.id.four).setOnClickListener { v ->
            dialog.findViewById<SpanCheckedTextView>(R.id.one).isChecked = false
            dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked =
                dialog.findViewById<SpanCheckedTextView>(R.id.four).isChecked != true
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
    private fun init(paymethods: String?, money: String?){
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }

        fragmentList?.clear()
        fragmentList?.add(C2CCustomerBuyItemFragment().also {
            val bundle = Bundle()
            val coinType = "USDT"
            bundle.putString(ConstData.COIN_TYPE,coinType)
            bundle.putString(ConstData.BIRTH,paymethods)
            bundle.putString(ConstData.PAIR, money)
            bundle.putString(ConstData.COIN_INFO,"B")
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

}