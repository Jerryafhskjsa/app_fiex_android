package com.black.c2c.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cNewBinding
import com.black.c2c.fragment.C2CCustomerFragment
import com.black.c2c.fragment.C2COneKeyFragment
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import java.util.*

@Route(value = [RouterConstData.C2C_NEW])
class C2CNewActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private const val TAB_ONE_KEY = 1
        private const val TAB_CUSTOMER = 2
    }

    private var binding: ActivityC2cNewBinding? = null

    private var currentTab = 0
    private var fManager: FragmentManager? = null
    private var c2COneKeyFragment: C2COneKeyFragment? = null
    private var c2CCustomerFragment: C2CCustomerFragment? = null
    private var supportCoins: ArrayList<C2CSupportCoin?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_new)
        binding?.c2cOneKey?.setOnClickListener(this)
        binding?.c2cCustomer?.setOnClickListener(this)
        fManager = supportFragmentManager
        selectTab(TAB_ONE_KEY)
        c2CSupportCoins
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.c2c_one_key) {
            selectTab(TAB_ONE_KEY)
        } else if (id == R.id.c2c_customer) {
            selectTab(TAB_CUSTOMER)
        }
    }

    private fun selectTab(tabIndex: Int) {
        if (currentTab != tabIndex) {
            currentTab = tabIndex
            onHeaderTabChanged()
        }
    }

    private fun onHeaderTabChanged() {
        binding?.c2cOneKey?.isChecked = currentTab == TAB_ONE_KEY
        binding?.c2cCustomer?.isChecked = currentTab == TAB_CUSTOMER
        refreshFragment()
    }

    //选择对应工作区
    private fun refreshFragment() {
        val transaction = fManager!!.beginTransaction()
        hideFragments(transaction)
        if (currentTab == TAB_ONE_KEY) {
            if (c2COneKeyFragment == null || !c2COneKeyFragment!!.isAdded) {
                c2COneKeyFragment = C2COneKeyFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, c2COneKeySupportCoinList)
                c2COneKeyFragment!!.arguments = bundle
                transaction.add(R.id.work_space, c2COneKeyFragment!!)
            } else {
                transaction.show(c2COneKeyFragment!!)
            }
        } else if (currentTab == TAB_CUSTOMER) {
            if (c2CCustomerFragment == null || !c2CCustomerFragment!!.isAdded) {
                c2CCustomerFragment = C2CCustomerFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
                c2CCustomerFragment!!.arguments = bundle
                transaction.add(R.id.work_space, c2CCustomerFragment!!)
            } else {
                transaction.show(c2CCustomerFragment!!)
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        if (c2COneKeyFragment != null) {
            transaction.hide(c2COneKeyFragment!!)
        }
        if (c2CCustomerFragment != null) {
            transaction.hide(c2CCustomerFragment!!)
        }
    }

    private val c2CSupportCoins: Unit
        get() {
            C2CApiServiceHelper.getCoinTypeList(this, object : NormalCallback<HttpRequestResultDataList<C2CSupportCoin?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultDataList<C2CSupportCoin?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        supportCoins = returnData.data
                        refreshC2CSupportCoins()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                    }
                }
            })
        }

    private fun refreshC2CSupportCoins() {
        if (c2CCustomerFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, supportCoins)
            c2CCustomerFragment!!.arguments = bundle
            c2CCustomerFragment!!.setSupportCoins(supportCoins)
        }
        if (c2COneKeyFragment != null) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(ConstData.C2C_SUPPORT_COINS, c2COneKeySupportCoinList)
            c2COneKeyFragment!!.arguments = bundle
            c2COneKeyFragment!!.setSupportCoins(c2COneKeySupportCoinList)
        }
    }

    private val c2COneKeySupportCoinList: ArrayList<C2CSupportCoin>?
        get() {
            if (supportCoins == null || supportCoins!!.isEmpty()) {
                return null
            }
            val list = ArrayList<C2CSupportCoin>()
            for (supportCoin in supportCoins!!) {
                if (supportCoin != null &&
                        (TextUtils.equals(supportCoin.coinType, "USDT") ||
                                TextUtils.equals(supportCoin.coinType, "DC"))) {
                    list.add(supportCoin)
                }
            }
            return list
        }
}