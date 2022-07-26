package com.black.c2c.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.black.base.fragment.BaseFragment
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cCustomerBinding
import com.black.router.BlackRouter
import java.util.*

class C2CCustomerFragment : BaseFragment(), View.OnClickListener {
    companion object {
        private val TAG = C2CCustomerFragment::class.java.simpleName
        private const val TAB_BUY = 4
        private const val TAB_SALE = 8
    }

    private var binding: FragmentC2cCustomerBinding? = null

    private var currentTab = 0
    private var fManager: FragmentManager? = null
    private var c2CCustomerBuyFragment: C2CCustomerBuyFragment? = null
    private var c2CCustomerSaleFragment: C2CCustomerSaleFragment? = null
    private var supportCoins: ArrayList<C2CSupportCoin?>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        val bundle = arguments
        if (bundle != null) {
            supportCoins = bundle.getParcelableArrayList(ConstData.C2C_SUPPORT_COINS)
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_customer, container, false)
        binding?.headOrder?.setOnClickListener(this)
        binding?.btnC2cTabBuy?.setOnClickListener(this)
        binding?.btnC2cTabSell?.setOnClickListener(this)
        fManager = childFragmentManager
        selectTab(TAB_BUY)
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.head_order -> {
                BlackRouter.getInstance().build(RouterConstData.C2C_ORDER).go(mContext)
            }
            R.id.btn_c2c_tab_buy -> {
                selectTab(TAB_BUY)
            }
            R.id.btn_c2c_tab_sell -> {
                selectTab(TAB_SALE)
            }
        }
    }

    private fun selectTab(i: Int) {
        if (currentTab != i) {
            currentTab = i
            refreshTabView(i)
            refreshLayout(i)
        }
    }

    private fun refreshTabView(index: Int) {
        if (index == TAB_BUY) {
            binding?.btnC2cTabBuy?.isChecked = true
            binding?.btnC2cTabBuy?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            binding?.btnC2cTabSell?.isChecked = false
            binding?.btnC2cTabSell?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_16).toFloat())
        } else if (index == TAB_SALE) {
            binding?.btnC2cTabBuy?.isChecked = false
            binding?.btnC2cTabBuy?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_16).toFloat())
            binding?.btnC2cTabSell?.isChecked = true
            binding?.btnC2cTabSell?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
        }
    }

    private fun refreshLayout(position: Int) {
        val transaction = fManager!!.beginTransaction()
        hideFragments(transaction)
        if (position == TAB_BUY) {
            if (c2CCustomerBuyFragment == null || !c2CCustomerBuyFragment!!.isAdded) {
                c2CCustomerBuyFragment = C2CCustomerBuyFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2CCustomerBuyFragment!!.arguments = bundle
                transaction.add(R.id.container, c2CCustomerBuyFragment!!)
            } else {
                transaction.show(c2CCustomerBuyFragment!!)
            }
        } else if (position == TAB_SALE) {
            if (c2CCustomerSaleFragment == null || !c2CCustomerSaleFragment!!.isAdded) {
                c2CCustomerSaleFragment = C2CCustomerSaleFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2CCustomerSaleFragment!!.arguments = bundle
                transaction.add(R.id.container, c2CCustomerSaleFragment!!)
            } else {
                transaction.show(c2CCustomerSaleFragment!!)
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        if (c2CCustomerBuyFragment != null) {
            transaction.hide(c2CCustomerBuyFragment!!)
        }
        if (c2CCustomerSaleFragment != null) {
            transaction.hide(c2CCustomerSaleFragment!!)
        }
    }

    fun setSupportCoins(supportCoins: ArrayList<C2CSupportCoin?>?) {
        this.supportCoins = supportCoins
        if (c2CCustomerBuyFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
            c2CCustomerBuyFragment!!.arguments = bundle
            c2CCustomerBuyFragment!!.setSupportCoins(supportCoins)
        }
        if (c2CCustomerSaleFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
            c2CCustomerSaleFragment!!.arguments = bundle
            c2CCustomerSaleFragment!!.setSupportCoins(supportCoins)
        }
    }
}