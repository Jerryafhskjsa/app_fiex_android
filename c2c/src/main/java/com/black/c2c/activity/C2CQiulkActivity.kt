package com.black.c2c.activity

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cMainBinding
import com.black.c2c.databinding.ActivityC2cOldBinding
import com.black.c2c.fragment.C2CCustomerFragment
import com.black.c2c.fragment.C2COneKeyFragment
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.google.android.material.tabs.TabLayout
import java.util.ArrayList

@Route(value = [RouterConstData.C2C_QIULK])
class C2CQiulkActivity: BaseActionBarActivity(), View.OnClickListener {
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

    private var binding: ActivityC2cOldBinding? = null

    private var currentTab = 0
    private var type = 0
    private  var otherType = false
    private  var otherType2 = false
    private  var otherType3 = false
    private var tab = TAB_QUCILK
    private var fManager: FragmentManager? = null
    private var typeList: MutableList<String>? = null
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
        else if (id == R.id.method_choose){



        }
        else if (id == R.id.filter_title){


        }
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