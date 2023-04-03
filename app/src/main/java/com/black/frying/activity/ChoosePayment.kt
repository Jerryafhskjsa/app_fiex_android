package com.black.frying.activity

import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.frying.fragment.BuyFragment
import com.black.frying.fragment.SellFragment
import com.black.router.annotation.Route
import com.fbsex.exchange.R
import java.util.*
import com.fbsex.exchange.databinding.ActivityBuySellBillBinding

//买卖记录
@Route(value = [RouterConstData.CHOOSEPAYMENT])
class ChoosePayment: BaseActionBarActivity(), View.OnClickListener {
  companion object {
        private val TAB_TITLES = arrayOfNulls<String>(2) //标题
    }

    private var actionType = ConstData.TAB_EXCHANGE

    private var headTitleView: TextView? = null

    private var binding: ActivityBuySellBillBinding? = null

    private var fragmentList: ArrayList<Fragment>? = null
    private var rechargeFragment: BuyFragment? = null
    private var extractFragment: SellFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionType = intent.getIntExtra(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
        TAB_TITLES[0] = "Buy"
        TAB_TITLES[1] = "Sell"
        binding = DataBindingUtil.setContentView(this, R.layout.activity_buy_sell_bill)
        binding?.extractRecord?.setOnClickListener(this)
        binding?.rechargeRecord?.setOnClickListener(this)
        init()
        binding!!.viewPager.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
        }
        binding?.viewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                refreshCurrentType(position)
            }

        })
        refreshCurrentType(actionType)
        changeFragment(if (actionType == ConstData.TAB_EXCHANGE) 0 else 1)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.history_record)
    }

    override fun onClick(v: View) {
        val i = v.id
       if (i == R.id.recharge_record) {
            if (ConstData.TAB_EXCHANGE != actionType) {
                actionType = ConstData.TAB_EXCHANGE
                refreshCurrentType(actionType)
                changeFragment(0)
            }
        } else if (i == R.id.extract_record) {
            if (ConstData.TAB_WITHDRAW != actionType) {
                actionType = ConstData.TAB_WITHDRAW
                refreshCurrentType(actionType)
                changeFragment(1)
            }
        }
    }

    private fun refreshCurrentType(type: Int) {
        if (ConstData.TAB_EXCHANGE == type) {
            binding!!.extractRecord.isChecked = false
            binding!!.extractRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            binding!!.rechargeRecord.isChecked = true
            binding!!.rechargeRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            headTitleView?.text = "Buy"
        } else if (ConstData.TAB_WITHDRAW == type) {
            binding!!.extractRecord.isChecked = true
            binding!!.extractRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            binding!!.rechargeRecord.isChecked = false
            binding!!.rechargeRecord.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            headTitleView?.text = "Sell"
        }
    }

    private fun changeFragment(position: Int) {
        binding?.viewPager?.setCurrentItem(position, true)
    }

    private fun init() {
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(BuyFragment().also {
            val bundle = Bundle()
            bundle.putString(ConstData.WALLET,"B")
            it.arguments = bundle
            rechargeFragment = it
        })
        fragmentList?.add(SellFragment().also {
            val bundle = Bundle()
            bundle.putString(ConstData.WALLET,"S")
            it.arguments = bundle
            extractFragment = it
        })
    }
}