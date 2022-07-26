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
import com.black.c2c.databinding.FragmentC2cOneKeyBinding
import com.black.router.BlackRouter
import java.util.*

class C2COneKeyFragment : BaseFragment(), View.OnClickListener {
    companion object {
        private val TAG = C2COneKeyFragment::class.java.simpleName
        private const val TAB_BUY = 16
        private const val TAB_SALE = 32
    }

    private var binding: FragmentC2cOneKeyBinding? = null

    private var currentTab = 0
    private var fManager: FragmentManager? = null
    private var c2COneKeyBuyFragment: C2COneKeyBuyFragment? = null
    private var c2COneKeySaleFragment: C2COneKeySaleFragment? = null
    private var supportCoins: ArrayList<C2CSupportCoin>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        val bundle = arguments
        if (bundle != null) {
            supportCoins = bundle.getParcelableArrayList(ConstData.C2C_SUPPORT_COINS)
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_one_key, container, false)
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
        val transaction = fManager?.beginTransaction()
        hideFragments(transaction)
        if (position == TAB_BUY) {
            if (c2COneKeyBuyFragment == null || true != c2COneKeyBuyFragment?.isAdded) {
                c2COneKeyBuyFragment = C2COneKeyBuyFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2COneKeyBuyFragment?.arguments = bundle
                transaction?.add(R.id.container, c2COneKeyBuyFragment!!)
            } else {
                transaction?.show(c2COneKeyBuyFragment!!)
            }
        } else if (position == TAB_SALE) {
            if (c2COneKeySaleFragment == null || true != c2COneKeySaleFragment?.isAdded) {
                c2COneKeySaleFragment = C2COneKeySaleFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2COneKeySaleFragment?.arguments = bundle
                transaction?.add(R.id.container, c2COneKeySaleFragment!!)
            } else {
                transaction?.show(c2COneKeySaleFragment!!)
            }
        }
        transaction?.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction?) {
        if (c2COneKeyBuyFragment != null) {
            transaction?.hide(c2COneKeyBuyFragment!!)
        }
        if (c2COneKeySaleFragment != null) {
            transaction?.hide(c2COneKeySaleFragment!!)
        }
    }

    fun setSupportCoins(supportCoins: ArrayList<C2CSupportCoin>?) {
        this.supportCoins = supportCoins
        if (c2COneKeyBuyFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
            c2COneKeyBuyFragment?.arguments = bundle
            c2COneKeyBuyFragment?.setSupportCoins(supportCoins)
        }
        if (c2COneKeySaleFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
            c2COneKeySaleFragment?.arguments = bundle
            c2COneKeySaleFragment?.setSupportCoins(supportCoins)
        }
    }

    private val c2CSupportCoins: Unit
        get() {
            val result = ArrayList<C2CSupportCoin>()
            var supportCoin = C2CSupportCoin()
            supportCoin.coinType = "USDT"
            result.add(supportCoin)
            supportCoin = C2CSupportCoin()
            supportCoin.coinType = "BTC"
            result.add(supportCoin)
            supportCoin = C2CSupportCoin()
            supportCoin.coinType = "ETH"
            result.add(supportCoin)
            supportCoin = C2CSupportCoin()
            supportCoin.coinType = "EOS"
            result.add(supportCoin)
            supportCoins = result
            if (c2COneKeyBuyFragment != null) {
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2COneKeyBuyFragment?.arguments = bundle
                c2COneKeyBuyFragment?.setSupportCoins(result)
            }
            if (c2COneKeySaleFragment != null) {
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2COneKeySaleFragment?.arguments = bundle
                c2COneKeySaleFragment?.setSupportCoins(result)
            }
        }
}